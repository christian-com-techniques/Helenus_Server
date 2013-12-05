import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class UserinputHandler implements Runnable {

    private static String configFileName;
    private static Config conf;
    private static boolean shouldRun = true;
    static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private KeyValueController<String> kvc;
    private KeyValueController<String> kvc_backup;

    public UserinputHandler(KeyValueController<String> keyValueController, KeyValueController<String> keyValueController_Backup) {
        kvc = keyValueController;
        kvc_backup = keyValueController_Backup;
    }

    @Override
    public void run() {

        configFileName = MyKV.getConfigFile();
		
        try {
            conf = new Config(configFileName);
        }
        catch (IOException e) {
            System.out.println("Failed to load config file: " + configFileName);
            return;
        }
    	
        int port = conf.intFor("contactPort");
        
        while(shouldRun) {
    		 
            System.out.print("> ");
    		 
            String userinput = null;
            try {
                userinput = reader.readLine();
            } catch (IOException e) {
                System.err.println("Failed to get user input. " + e);
                continue;
            }
             
            if(userinput.equalsIgnoreCase("exit")) {
                break;
            }
             
            String[] cm = userinput.split(" ");
            String[] keva = userinput.split("\"");

            if(cm.length == 0) {
             	System.out.println("No arguments passed: insert, delete, update, lookup, show");
             	continue;
            }
             
            // Insert request
            if(cm[0].equals("insert")) {
     			
                if (cm.length < 3) {
                    System.out.println("Not a valid argument.\nSyntax: insert <key> <value>");
                    continue;
                }
     			
                String key = "";
                key = cm[1];
                
                /*
                try {
                    key = cm[1];
                } catch (NumberFormatException e) {
                    System.out.println("Key must be a number.\nSyntax: insert <key> <value>");
                    continue;
                }
     			
                if(key < 0 || key > 1000000) {
                    System.out.println("Key must be a number between 0 and 1000000");
                    continue;
                }
     			*/
                
                String value = null;

                if(userinput.indexOf("\"") < 0) {
                    value = cm[2];
                } else {
                    value = keva[1];
                }
     					

            	//KeyValueController<String> kvc = new KeyValueController<String>();
            	kvc.insert(key, value, false, "", 0);

     		// Delete request
            } else if(cm[0].equals("delete")) {
     			
                if (cm.length != 2) {
                    System.out.println("Not a valid argument.\nSyntax: delete <key>");
                    continue;
                }

                String key = "";
                key = cm[1];
                
                /*
                try {
                    key = cm[1];
                } catch (NumberFormatException e) {
                    System.out.println("Key must be a number.\nSyntax: insert <key> <value>");
                    continue;
                }
     			*/
            	//KeyValueController<String> kvc = new KeyValueController<String>();
            	kvc.delete(key, false);
     		
     		// Update request
            } else if(cm[0].equals("update")) {
     			
                if (cm.length < 3) {
                    System.out.println("Not a valid argument.\nSyntax: update <key> <value>");
                    continue;
                }
     			
                String key = cm[1];
                String value = null;
     			
                if(userinput.indexOf("\"") < 0) {
                    value = cm[2];
                } else {
                    value = keva[1];
                }
     			
            	//KeyValueController<String> kvc = new KeyValueController<String>();
            	kvc.update(key, value, false);
     			
     		// Lookup request
            } else if(cm[0].equals("lookup")) {
     			
                if (cm.length != 2) {
                    System.out.println("Not a valid argument.\nSyntax: lookup <key>");
                    continue;
                }
     		
                
                String key = cm[1];
                
                /*
                try {
                    key = Integer.parseInt(cm[1]);
     		} catch (NumberFormatException e) {
                    System.out.println("Key must be a number.\nSyntax: lookup <key>");
                    continue;
                }
                */

            	//KeyValueController<String> kvc = new KeyValueController<String>();
            	kvc.lookup(key, false, "", "", 0);
            
                //print membership list and all key-value-pair of the local machine
            } else if(cm[0].equals("show")) {
     			
            	//KeyValueController<String> kvc = new KeyValueController<String>();
            	ArrayList<KVEntry<String>> al = kvc.showStore();
                ArrayList<KVEntry<String>> bl = kvc_backup.showStore();

            	System.out.println("Local Key-Values ------------------------");
            	
            	for(int i=0;i<al.size();i++) {
                    System.out.println("key: "+al.get(i).getKey()+", value: "+al.get(i).getValue());
            	}

                System.out.println("Local Key-Value Backups -----------------");
                
                for(int i=0;i<bl.size();i++) {
                    System.out.println("key: "+bl.get(i).getKey()+", value: "+bl.get(i).getValue()+", redist: "+bl.get(i).getRedistribute());
            	}
            	
            	System.out.println("Local Membershiplist --------------------");
            	
            	MembershipList mL = ConnectionHandler.getMembershipList();
            	ArrayList<MembershipEntry> aL = mL.get();
            	
            	for(int i=0;i<aL.size();i++) {
                    System.out.println("id: "+ +aL.get(i).getID()+", ip: "+aL.get(i).getIPAddress());
            	}
            	
            	
            } else {
                System.out.println("Not a valid argument: "+cm[0]);
                continue;
            }
    		 
        }

    }
    

    

    
}
