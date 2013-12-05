import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.JAXBException;


public class MyKV {

    private static boolean running = true;
    private static Config conf;
    private static MembershipList ownList;
    private static final String configFileName = "mykv.conf";
    private static int contactPort = 0;
    private static String myIP = "";
	
    public static void main(String[] args) throws InterruptedException, SocketException, UnknownHostException, JAXBException, NoSuchAlgorithmException, UnsupportedEncodingException {
	        
        try {
            conf = new Config(configFileName);
        }
        catch (IOException e) {
            System.out.println("Failed to load config file: " + configFileName);
            return;
        }
	        
        myIP = conf.valueFor("bindIP");
        ownList = new MembershipList();
	        
        String contactIP = conf.valueFor("contactIP");
        contactPort = conf.intFor("contactPort");
	        
        int id = (int)(Hash.value(myIP, 6));
        //System.out.println(String.valueOf(id));
	        
        if(myIP.equals(contactIP))
            ownList.add(id, myIP);
	        
        KeyValueController<String> kvc = new KeyValueController<String>();
        KeyValueController<String> kvc_backup = new KeyValueController<String>();
        kvc_backup.setBackup(true);
        
        ConnectionHandler connectionHandler = new ConnectionHandler(conf, kvc, kvc_backup);
        Thread handlerThread = new Thread(connectionHandler, "Connection Handler");
        handlerThread.start();
	        
        UserinputHandler userinputHandler = new UserinputHandler(kvc, kvc_backup);
        Thread userinputThread = new Thread(userinputHandler, "Userinput Handler");
        userinputThread.start();
	        
        while(running) {
            ownList.incrHeartbeatCounter(myIP);
            MembershipController.trackFailing(ownList, conf.intFor("TFail")/1000, kvc_backup);
            MembershipController.sendGossip(ownList, contactIP, contactPort, myIP);
	            
            //The cleanUp may only be called when the membership list got an update
            kvc.cleanUp();
            kvc_backup.cleanUp();

            MembershipList mL = ConnectionHandler.getMembershipList();
            ownList = mL;
            Thread.sleep(conf.intFor("TGossip"));
        }
	                
	        
        handlerThread.interrupt();
        userinputThread.interrupt();
	        
    }
	
	
    public static int getContactPort() {
        return contactPort;
    }
	
    public static String getmyIP() {
        return myIP;
    }
	
    public static String getConfigFile() {
        return configFileName;
    }

}
