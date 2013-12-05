import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.swing.event.AncestorEvent;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class ConnectionHandler implements Runnable {

    private boolean shouldRun = true;
    private Config conf;
    private int bufferSize = 2048;
    private static MembershipList list = new MembershipList();
    private KeyValueController<String> kvc;
    private KeyValueController<String> kvc_backup;
    private static ArrayList<LookupCacheEntry> lookuprequestCache;
    private static ArrayList<LookupCacheEntry> writerequestCache;
    
    public ConnectionHandler(Config conf, KeyValueController<String> keyValueController, KeyValueController<String> keyValueController_Backup) {
    	this.conf = conf;
    	lookuprequestCache = new ArrayList<LookupCacheEntry>();
    	writerequestCache = new ArrayList<LookupCacheEntry>();
        kvc = keyValueController;
        kvc_backup = keyValueController_Backup;
        kvc_backup.setBackup(true);
    }
    
    @Override
    public void run() {
		
        int port = conf.intFor("contactPort");
        String ip = conf.valueFor("bindIP");
		
        DatagramSocket rcvSocket = null;
        try {
            rcvSocket = new DatagramSocket(port);
        } catch (SocketException e1) {
            System.out.println("Can't listen on port "+port +"\n");
            return;
        }
        
        byte[] buffer = new byte[bufferSize];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        System.out.println("Waiting for UDP packets: Started");

        while(shouldRun) {
            try {
                rcvSocket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            
            String msg = new String(buffer, 0, packet.getLength());
            //System.out.println("\nMessage from: " + packet.getAddress().getHostAddress() +", msg: "+msg);
            
            InputSource source = new InputSource(new StringReader(msg));

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;
            Element a = null;
            
            try {
                db = dbf.newDocumentBuilder();
                Document doc = db.parse(source);
                a = doc.getDocumentElement();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
                   
            
           
            
            if(a.getNodeName() == "join") {

                String newMember = packet.getAddress().getHostAddress();
                
    	        int id = 0;
                try {
                    id = (int)(Hash.value(newMember, 6));
                } catch (NoSuchAlgorithmException e1) {
                    e1.printStackTrace();
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
                
                //System.out.println(newMember + " is joining the cluster.");
				
                if(!list.ipExists(newMember)) {
					
                    list.add(id, newMember);

                    ArrayList<MembershipEntry> memList = list.get();

                    try {
                        String marshalledMessage = DstrMarshaller.toXML(memList);
                        Supplier.send(newMember, port, marshalledMessage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JAXBException e) {
                        e.printStackTrace();
                    }
					
                }

                
                
                // Go this way, when the node receives a leave-request from another node
            } else if(a.getNodeName() == "leave") {
                
                // Go this way, when the node gets a membershiplist from another node
            } else if(a.getNodeName() == "membershipList") {
                    
                ArrayList<MembershipEntry> receivedMemList = new ArrayList<MembershipEntry>();
                
                try {
                    receivedMemList = DstrMarshaller.unmarshallXML(msg);
                } catch (JAXBException e) {
                    e.printStackTrace();
                }
                    
                MembershipController.updateMembershipList(list, receivedMemList);
                
            }
            
            
                
            
            if(a.getNodeName() == "insert") {
                
            	NodeList n = a.getChildNodes();
            	String key = "";
            	String value = null;
            	String type = "";
            	String consistencyLevel = "";
            	int clientPort = 0;
            	String clientIP = "";
            	
            	for(int i=0;i<n.getLength();i++) {
                    if(n.item(i).getNodeName().equals("key")) {
                        key = n.item(i).getTextContent();
                    }
                    if(n.item(i).getNodeName().equals("value")) {
                        value = n.item(i).getTextContent();
                    }
                    if(n.item(i).getNodeName().equals("type")) {
                        type = n.item(i).getTextContent();
                    }
                    if(n.item(i).getNodeName().equals("clientip")) {
                        clientIP = n.item(i).getTextContent();
                    }
                    if(n.item(i).getNodeName().equals("port")) {
                        clientPort = Integer.valueOf(n.item(i).getTextContent());
                    }
                    if(n.item(i).getNodeName().equals("consistencylevel")) {
                    	consistencyLevel = n.item(i).getTextContent();
                    }
            	}

            	//Here I assume, that the values are just strings. By calling Str.isInteger / Str.isDouble
            	// it is possible to make a roughly check if the input is an int or double. It would be
            	//possible to save the data in the correct format since generics are used in KVEntry
            	//KeyValueController<String> kvc = new KeyValueController<String>();
            	
            	if(type.equals("clientrequest")) {
                    clientIP = packet.getAddress().getHostAddress();  
            		LookupCacheEntry lce = new LookupCacheEntry(clientIP, clientPort, consistencyLevel);
            		writerequestCache.add(lce);
            		
            		kvc.insert(key, value, false, clientIP, clientPort);
            		
            	} else if (type.equals("serverrequest")) {
            		
            		String serverIP = packet.getAddress().getHostAddress();
            		int serverPort = port;
            		
        			String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<writeconsistencycheck><key>"+key+"</key><clientip>"+clientIP+"</clientip><clientport>"+clientPort+"</clientport></writeconsistencycheck>\n";
                    try {
						Supplier.send(serverIP, serverPort, message);
					} catch (IOException e) {
						e.printStackTrace();
					}
            		
                	kvc.insert(key, value, true, clientIP, clientPort);
            	}
            	
            } else if (a.getNodeName() == "writeconsistencycheck") {
            
            	NodeList n = a.getChildNodes();
            	String key = "";
            	int clientPort = 0;
            	String clientIP = "";
            	
            	for(int i=0;i<n.getLength();i++) {
                    if(n.item(i).getNodeName().equals("key")) {
                        key = n.item(i).getTextContent();
                    }
                    if(n.item(i).getNodeName().equals("clientip")) {
                        clientIP = n.item(i).getTextContent();
                    }
                    if(n.item(i).getNodeName().equals("clientport")) {
                        clientPort = Integer.valueOf(n.item(i).getTextContent());
                    }

            	}
            	
                MembershipList ownList = ConnectionHandler.getMembershipList();
                int membershiplistSize = ownList.get().size();
                
                for(int i=0;i<writerequestCache.size();i++) {
                	if(writerequestCache.get(i).getIp().equals(clientIP)) {
                		int numOfWriteFeedback = writerequestCache.get(i).getCounter();
                		writerequestCache.get(i).setCounter(numOfWriteFeedback+1);
                		
                		if(writerequestCache.get(i).getConsistencyLevel().equals("ONE")) {
                			writerequestCache.remove(i);
                			String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<writeconsistencycheck><value>true</value></writeconsistencycheck>\n";
                			try {
        						Supplier.send(clientIP, clientPort, message);
        					} catch (IOException e) {
        						e.printStackTrace();
        					}
                		} else if(writerequestCache.get(i).getConsistencyLevel().equals("QUO.")) {
                			if(writerequestCache.get(i).getCounter() > membershiplistSize/2) {
                    			writerequestCache.remove(i);
                    			String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<writeconsistencycheck><value>true</value></writeconsistencycheck>\n";
                                try {
            						Supplier.send(clientIP, clientPort, message);
            					} catch (IOException e) {
            						e.printStackTrace();
            					}
                			}
            
                		} else if(writerequestCache.get(i).getConsistencyLevel().equals("ALL")) {
                			if(writerequestCache.get(i).getCounter() == membershiplistSize) {
                    			writerequestCache.remove(i);
                    			String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<writeconsistencycheck><value>true</value></writeconsistencycheck>\n";
                                try {
            						Supplier.send(clientIP, clientPort, message);
            					} catch (IOException e) {
            						e.printStackTrace();
            					}
                			}
                		}
                		
                	}
                }
            	
            } else if (a.getNodeName() == "backup") {
                NodeList n = a.getChildNodes();
                String key = "";
                String value = null;

                for(int i=0;i<n.getLength();i++) {
                    if(n.item(i).getNodeName().equals("key")) {
                        key = n.item(i).getTextContent();
                    }
                    if(n.item(i).getNodeName().equals("value")) {
                        value = n.item(i).getTextContent();
                    }
            	}

                kvc_backup.insert(key, value, true, "", 0);
            	
                //System.out.println("Key: " + Integer.toString(key) + " | Value: " + value + " inserted as backup.");
                
            } else if(a.getNodeName() == "delete") {
            
            	NodeList n = a.getChildNodes();
            	String key = "";
            	String type = "";
            	
            	for(int i=0;i<n.getLength();i++) {
                    if(n.item(i).getNodeName().equals("key")) {
                        key = n.item(i).getTextContent();
                    }
                    if(n.item(i).getNodeName().equals("type")) {
                        type = n.item(i).getTextContent();
                    }
            	}
            	
            	if(type.equals("clientrequest")) {
                	kvc.delete(key, false);
            	} else if(type.equals("serverrequest")) {
                	kvc.delete(key, true);
            	}

                System.out.println("Key: " + key + " deleted.");

            } else if(a.getNodeName() == "update") {
                
            	System.out.println(msg);
            	
            	NodeList n = a.getChildNodes();
            	String key = "";
            	String value = null;
            	String type = "";
            	
            	for(int i=0;i<n.getLength();i++) {
                    if(n.item(i).getNodeName().equals("key")) {
                        key = n.item(i).getTextContent();
                    }
                    if(n.item(i).getNodeName().equals("value")) {
                        value = n.item(i).getTextContent();
                    }
                    if(n.item(i).getNodeName().equals("type")) {
                        type = n.item(i).getTextContent();
                    }
            	}

            	if(type.equals("clientrequest")) {
            		kvc.update(key, value, false);
            	} else if(type.equals("serverrequest")) {
                	kvc.update(key, value, true);	
            	}

            	
                System.out.println("Key: " + key + " | Value: " + value + " updated.");

            } else if(a.getNodeName() == "lookup") {
                
            	NodeList n = a.getChildNodes();
            	String key = "";
            	String type = null;
            	int clientPort = 0;
            	String clientIP = "";
            	String consistencyLevel = "";
            	
            	for(int i=0;i<n.getLength();i++) {
                    if(n.item(i).getNodeName().equals("key")) {
                        key = n.item(i).getTextContent();
                    }
                    if(n.item(i).getNodeName().equals("type")) {
                        type = n.item(i).getTextContent();
                    }
                    if(n.item(i).getNodeName().equals("port")) {
                        clientPort = Integer.valueOf(n.item(i).getTextContent());
                    }
                    if(n.item(i).getNodeName().equals("clientip")) {
                        clientIP = n.item(i).getTextContent();
                    }
                    if(n.item(i).getNodeName().equals("consistencylevel")) {
                    	consistencyLevel = n.item(i).getTextContent();
                    }
            	}

                System.out.println("Key: " + key + " lookup.");

                
            	if(type.equals("clientrequest")) {
                    clientIP = packet.getAddress().getHostAddress();
                    
            		LookupCacheEntry lce = new LookupCacheEntry(clientIP, clientPort, consistencyLevel);
            		lookuprequestCache.add(lce);
                    kvc.lookup(key, false, ip, clientIP, clientPort);
                    
            	} else if (type.equals("send")) {
                    String serverIP = packet.getAddress().getHostAddress();
                    kvc.lookup(key, true, ip, clientIP, clientPort);
	            	
            	} else if(type.equals("receive")) {
                    String value = null;
                    for(int i=0;i<n.getLength();i++) {
                    	if(n.item(i).getNodeName().equals("value")) {
                            value = n.item(i).getTextContent();
                    	}
                    }
                	
                    MembershipList ownList = ConnectionHandler.getMembershipList();
                    int membershiplistSize = ownList.get().size();
                    
                    for(int i=0;i<lookuprequestCache.size();i++) {
                    	if(lookuprequestCache.get(i).getIp().equals(clientIP)) {
                    		lookuprequestCache.get(i).addValue(value);
                    		
                    		if(lookuprequestCache.get(i).getConsistencyLevel().equals("ONE")) {
                    			
                    			value = lookuprequestCache.get(i).getConOne();
                    			lookuprequestCache.remove(i);
                    			String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<lookup><key>"+key+"</key><value>"+value+"</value><type>serverresponse</type></lookup>\n";
                                try {
            						Supplier.send(clientIP, clientPort, message);
            					} catch (IOException e) {
            						e.printStackTrace();
            					}
                    		} else if(lookuprequestCache.get(i).getConsistencyLevel().equals("QUO.")) {
                    			
                    			value = lookuprequestCache.get(i).getConQuorum(membershiplistSize);
                    			lookuprequestCache.remove(i);
                    			if(!value.equals("")) {
                        			String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<lookup><key>"+key+"</key><value>"+value+"</value><type>serverresponse</type></lookup>\n";
                                    try {
                						Supplier.send(clientIP, clientPort, message);
                					} catch (IOException e) {
                						e.printStackTrace();
                					}
                    			}
                    			
                    		} else if(lookuprequestCache.get(i).getConsistencyLevel().equals("ALL")) {
                    		
                    			value = lookuprequestCache.get(i).getConAll(membershiplistSize);
                    			lookuprequestCache.remove(i);
                    			if(!value.equals("")) {
                        			String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<lookup><key>"+key+"</key><value>"+value+"</value><type>serverresponse</type></lookup>\n";
                                    try {
                						Supplier.send(clientIP, clientPort, message);
                					} catch (IOException e) {
                						e.printStackTrace();
                					}
                    			}
                    			
                    		}
                    		
                    	}
                    }
                    
                    /*
                	if(clientIP.equals("") && clientPort == 0) {
                		System.out.println("key: "+String.valueOf(key)+", value: "+value);
                	} else {
                        String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<lookup><key>"+key+"</key><value>"+value+"</value><type>serverresponse</type></lookup>\n";
                        try {
    						Supplier.send(clientIP, clientPort, message);
    					} catch (IOException e) {
    						e.printStackTrace();
    					}
                	}
                    */
                    
            	} else if(type.equals("null")) {
                	if(clientIP.equals("") && clientPort == 0) {
                		System.out.println("Key does not exist");
                	} else {
                        String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<lookup><key>"+key+"</key><value>No entry found</value><type>serverresponse</type></lookup>\n";
                        try {
    						Supplier.send(clientIP, clientPort, message);
    					} catch (IOException e) {
    						e.printStackTrace();
    					}
                	}
            	}
            	
            } else if(a.getNodeName() == "show") {
                
            	//KeyValueController<String> kvc = new KeyValueController<String>();
            	ArrayList<KVEntry<String>> al = kvc.showStore();
            	
            	System.out.println("Local Key-Values ------------------------");
            	
            	for(int i=0;i<al.size();i++) {
                    System.out.println("key: "+al.get(i).getKey()+", value: "+al.get(i).getValue());
            	}
            	
            	System.out.println("Membershiplist --------------------------");
            	
            	MembershipList ml = list;
            	ArrayList<MembershipEntry> memList = ml.get();
            	
            	for(int i=0;i<memList.size();i++) {
                    System.out.println("IP: "+memList.get(i).getIPAddress());
            	}
            	
            }
            
            
            
        }
		
    }
	
    public static MembershipList getMembershipList() {
        return list;
    }

    public void kill() {
        this.shouldRun = false;
    }

}
