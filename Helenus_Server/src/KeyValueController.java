import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


public class KeyValueController<T> {

    private ArrayList<KVEntry<T>> store;
    private Boolean backup = false;

    public KeyValueController() {
        store = new ArrayList<KVEntry<T>>();
    }

    public void setBackup(Boolean value) { backup = value; }    
    
    public void insert(String key, T value, boolean insertHere, String clientIP, int clientPort) {

        MembershipList ownList = ConnectionHandler.getMembershipList();
    	int port = MyKV.getContactPort();
        String localIP = MyKV.getmyIP();

        if(insertHere) {

//            System.out.println("insertHere hit.");

            KVEntry<T> entry = new KVEntry<T>(key, value);

                        if(!backup) {
                
                //Insert backup entries into adjacent nodes
                for(int i = 0; i < ownList.get().size(); i++) {
                    
//                    System.out.println("Checking ip: " + ownList.get().get(i).getIPAddress() + " against my IP: " + localIP);
                    
                    if(ownList.get().get(i).getIPAddress().equals(localIP)) {
                        
                        String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<backup><key>"
                            + String.valueOf(key) + "</key><value>" 
                            + value + "</value></backup>\n";
                        
                        try {
//                            System.out.println("Sending backups to: " 
//                                               + ownList.get().get((i+1) % ownList.get().size()).getIPAddress() + " and: " 
//                                               + ownList.get().get((i+2) % ownList.get().size()).getIPAddress());
                            Supplier.send(
                                ownList.get().get((i+1) % ownList.get().size()).getIPAddress(), 
                                port, 
                                message);
                            Supplier.send(
                                ownList.get().get((i+2) % ownList.get().size()).getIPAddress(), 
                                port, 
                                message);
                        
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
			
            //If key already exists in store, do nothing
            for(int i=0;i<store.size();i++) {
                if(store.get(i).getKey().equals(key)) {
                    store.get(i).setRedistribute(false);
                    return;
                }
            }
			
            store.add(entry);
            
            return;
        }
		
		
        //The key of the value is hashed to determine, where the key-value-pair will be safed
        int hash = 0;
        try {
            hash = (int)Hash.value(String.valueOf(key), 6);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
		
//        System.out.println("Hash: " + hash);
        
    	//Loop through the membershiplist and send an insert-request to the first node with an
    	//id higher than the hash
        for(int i=0;i<ownList.get().size();i++) {
			
//            System.out.println("Checking IP: " + ownList.get().get(i).getIPAddress() + " ID: " + ownList.get().get(i).getID());
            if(ownList.get().get(i).getID() >= hash) {
                String ip = ownList.get().get(i).getIPAddress();

//                System.out.println("Sending Key: " + key + " Value: " + value + " Hash: " + hash + " to: " + ip);
				
                String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<insert><key>"+String.valueOf(key)+"</key><value>"+value+"</value><type>serverrequest</type><clientip>"+clientIP+"</clientip><port>"+clientPort+"</port></insert>\n";
                try {
                    Supplier.send(ip, port, message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
				
                break;
				
            }
			
            //If the last element of our membershipList is greater than the element which should be
            //added, we send it to the node with the lowest id (at position 0)
            if(i+1 == ownList.get().size()) {
                String ip = ownList.get().get(0).getIPAddress();
				
                String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<insert><key>"+String.valueOf(key)+"</key><value>"+value+"</value><type>serverrequest</type><clientip>"+clientIP+"</clientip><port>"+clientPort+"</port></insert>\n";
                try {
                    Supplier.send(ip, port, message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
				
                break;
				
            }
				
        }
				
    }
	

    public void delete(String key, boolean deleteHere) {

        if(deleteHere) {
            for(int i=0;i<store.size();i++) {
                if(store.get(i).getKey().equals(key)) {
                    store.remove(i);
                }
            }
            return;
        }
		
        int hash = 0;
        try {
            hash = (int)Hash.value(String.valueOf(key), 6);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
		
    	MembershipList ownList = ConnectionHandler.getMembershipList();
    	int port = MyKV.getContactPort();
		
        for(int i=0;i<ownList.get().size();i++) {
			
            if(ownList.get().get(i).getID() >= hash) {
                String ip = ownList.get().get(i).getIPAddress();
				
                String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<delete><key>"+String.valueOf(key)+"</key><type>serverrequest</type></delete>\n";
                try {
                    Supplier.send(ip, port, message);
                    Supplier.send(ownList.get().get((i+1) % ownList.get().size()).getIPAddress(), port, message);
                    Supplier.send(ownList.get().get((i+2) % ownList.get().size()).getIPAddress(), port, message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
				
                break;
				
            }
			
            if(i+1 == ownList.get().size()) {
                String ip = ownList.get().get(0).getIPAddress();
				
                String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<delete><key>"+String.valueOf(key)+"</key><type>serverrequest</type></delete>\n";
                try {
                    Supplier.send(ip, port, message);
                    Supplier.send(ownList.get().get((i+1) % ownList.get().size()).getIPAddress(), port, message);
                    Supplier.send(ownList.get().get((i+2) % ownList.get().size()).getIPAddress(), port, message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
				
                break;
				
            }
			
        }

    }
	
    public void update(String key, T newvalue, boolean updateHere) {
		
        if(updateHere) {
            for(int i=0;i<store.size();i++) {
                if(store.get(i).getKey().equals(key)) {
                    store.get(i).setValue(newvalue);
                }
            }
            return;
        }
		
        int hash = 0;
        try {
            hash = (int)Hash.value(String.valueOf(key), 6);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
		
    	MembershipList ownList = ConnectionHandler.getMembershipList();
    	int port = MyKV.getContactPort();
    	
        for(int i=0;i<ownList.get().size();i++) {
			
            if(ownList.get().get(i).getID() >= hash) {
                String ip = ownList.get().get(i).getIPAddress();
				
                String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<update><key>"+String.valueOf(key)+"</key><value>"+newvalue+"</value><type>serverrequest</type></update>\n";
                try {
                    Supplier.send(ip, port, message);
                    Supplier.send(ownList.get().get((i+1) % ownList.get().size()).getIPAddress(), port, message);
                    Supplier.send(ownList.get().get((i+2) % ownList.get().size()).getIPAddress(), port, message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
				
                break;
				
            }
			
            if(i+1 == ownList.get().size()) {
                String ip = ownList.get().get(0).getIPAddress();
				
                String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<update><key>"+String.valueOf(key)+"</key><value>"+newvalue+"</value><type>serverrequest</type></update>\n";
                try {
                    Supplier.send(ip, port, message);
                    Supplier.send(ownList.get().get((i+1) % ownList.get().size()).getIPAddress(), port, message);
                    Supplier.send(ownList.get().get((i+2) % ownList.get().size()).getIPAddress(), port, message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
				
                break;
				
            }
			
        }
		
    }
	
    public void lookup(String key, boolean lookupHere, String senderIP, String clientIP, int clientPort) {
		
        String value = null;
		
        if(lookupHere) {
            for(int i=0;i<store.size();i++) {
                if(store.get(i).getKey().equals(key)) {
                    value = (String)store.get(i).getValue();
                }
            }
			
            int senderPort = MyKV.getContactPort();
            			
	    if(value != null) {
		
		String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<lookup><key>"+String.valueOf(key)+"</key><value>"+value+"</value><type>receive</type><clientip>"+clientIP+"</clientip><port>"+clientPort+"</port></lookup>\n";
		try {
		    Supplier.send(senderIP, senderPort, message);
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
		return;
	    }
			
        }
		
		
        int hash = 0;
        try {
            hash = (int)Hash.value(String.valueOf(key), 6);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
		
    	MembershipList ownList = ConnectionHandler.getMembershipList();
    	int port = MyKV.getContactPort();
		
        for(int i=0;i<ownList.get().size();i++) {
			
            if(ownList.get().get(i).getID() >= hash) {
                String ip = ownList.get().get(i).getIPAddress();
				
                String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<lookup><key>"+String.valueOf(key)+"</key><type>send</type><clientip>"+clientIP+"</clientip><port>"+clientPort+"</port></lookup>\n";
                try {
                    Supplier.send(ip, port, message);
                    Supplier.send(ownList.get().get((i+1) % ownList.get().size()).getIPAddress(), port, message);
                    Supplier.send(ownList.get().get((i+2) % ownList.get().size()).getIPAddress(), port, message);
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
				
                break;
				
            }
			
            if(i+1 == ownList.get().size()) {
                String ip = ownList.get().get(0).getIPAddress();
				
                String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<lookup><key>"+String.valueOf(key)+"</key><type>send</type><clientip>"+clientIP+"</clientip><port>"+clientPort+"</port></lookup>\n";
                try {
                    Supplier.send(ip, port, message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
				
                break;
				
            }
			
        }
		
    }
	
    //We use this function to send packets which have mistakenly been hashed to our local node (due to
    //incomplete membership lists) to the right nodes and delete them locally.
    public void cleanUp() {
		
    	MembershipList ownList = ConnectionHandler.getMembershipList();
    	String localIP = MyKV.getmyIP();
    	
        if(!backup) {
            //Cleanup our keys. Redistribute mistakenly stored keys, or ones that belong in a recently joined member.
            for(int i=0;i<store.size();i++) {
                String key = store.get(i).getKey();
                String value = (String)store.get(i).getValue();
			
                int hash = 0;
                try {
                    hash = (int)Hash.value(String.valueOf(key), 6);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
			
                for(int j=0;j<ownList.get().size();j++) {
				
                    if(ownList.get().get(j).getID() >= hash) {
                        String ip = ownList.get().get(j).getIPAddress();

                        //We hash all values in our local key-value-store and check, if all the values
                        //are hashed to our machine (checked by IP). If not, the key-value-pair is sent
                        //to the machine where it should be according to the local membership list.
                        //The pair is deleted locally afterwards.
                        if(!ip.equals(localIP)) {
                            int port = MyKV.getContactPort();
                            String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<insert><key>"+String.valueOf(key)+"</key><value>"+value+"</value></insert>\n";
                            try {
                                Supplier.send(ip, port, message);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
						
                            for(int k=0;k<store.size();k++) {
                                if(store.get(k).getKey() == key) {
                                    store.remove(k);
                                }
                            }
						
                        } 
                        break;
	
                    }
				
                    if(j+1 == ownList.get().size()) {
                        String ip = ownList.get().get(0).getIPAddress();
					
					
                    }
					
                }
            }
        } else if(backup) {
            //Cleanup our backup keys. Make sure we're the nodes that are supposed to have the backups.
            //If not, something like a node drop or join has happened, and we need to redistribute the backup.
            for(int i=0;i<store.size();i++) {
                String key = store.get(i).getKey();
                String value = (String)store.get(i).getValue();

                

                int hash = 0;
                try {
                    hash = (int)Hash.value(String.valueOf(key), 6);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

//                System.out.println("Checking: " + key + " " + value + " " + hash);
	
                for(int j=0;j<ownList.get().size();j++) {
				
                    if(ownList.get().get(j).getID() >= hash || j+1 == ownList.get().size()) {
                        String ip = "";
                        if(ownList.get().get(j).getID() >= hash)
                            ip = ownList.get().get(j).getIPAddress();
                        else if(j+1 == ownList.get().size())
                            ip = ownList.get().get(0).getIPAddress();

                        
                        //We hash all values in our local key-value-store and check, if all the values
                        //are hashed to some machine, that we are one of the next two nodes in the ring. 
                        //If not, the key-value-pair is sent to the machine where it should be according 
                        //to the local membership list. The pair is deleted locally afterwards.
                        if((!(ownList.get().get((j+1) % ownList.get().size()).getIPAddress().equals(localIP) 
                              || ownList.get().get((j+2) % ownList.get().size()).getIPAddress().equals(localIP))) || store.get(i).getRedistribute())
                        {
                            
                            String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<insert><key>"+String.valueOf(key)+"</key><value>"+value+"</value></insert>\n";
                            int port = MyKV.getContactPort();

                            try {
                                Supplier.send(ip, port, message);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if(store.get(i).getRedistribute()) {
                                if(ip.equals(localIP)) {
                                    //System.out.println("Unmarking " + store.get(i).getKey() + " " + store.get(i).getValue() + " as redistribute.");
                                    store.get(i).setRedistribute(false);
                                }
                            }
                            else {
                                for(int k = 0; k < store.size(); k++) {
                                    if(store.get(k).getKey() == key) {
                                        store.remove(k);
                                    }
                                }
                            }
                            
                        }
                        break;
                        
                    }
				
                    if(j+1 == ownList.get().size()) {
                        String ip = ownList.get().get(0).getIPAddress();
					
					
                    }
					
                }
            }
        }
        
    }
	
    //Returns the whole local key-value store
    public ArrayList<KVEntry<T>> showStore() {
        return store;
    }
	
}