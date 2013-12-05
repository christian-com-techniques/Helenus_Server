import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "membershipList")
public class MembershipList {
        
    @XmlElement(name = "membershipentry", type = MembershipEntry.class)
    private ArrayList<MembershipEntry> membershipList = null;

    public MembershipList() {
        membershipList = new ArrayList<MembershipEntry>();
    }
        
    public MembershipList(ArrayList<MembershipEntry> me) {
        this.membershipList = me;
    }

    
    
    public void add(int id, String ip) {                
        
        //System.out.println("Adding: " + ip);
        MembershipEntry mE = new MembershipEntry(id, ip);
        this.add(mE);
    }

    public void add(MembershipEntry mE) {
         

        if(membershipList.size() == 0) {
            membershipList.add(mE);
            
        } else {
            
            //We use this to make sure, the membershipList is sorted by ID all the time.
            //If a new value is added and the successor-id is greater than the current id,
            //the current node is added at position i and the node with an higher ID than
            //i is shifted to the right.
            for(int i=0;i<membershipList.size();i++) {
                                    
                if(membershipList.get(i).getID() > mE.getID()) {
                    membershipList.add(i, mE);
                    break;
                } 
	            
                if(i+1 == membershipList.size()) {
                    membershipList.add(i+1, mE);
                    break;
                }
            }
	        
        }
    }
    

    public ArrayList<MembershipEntry> get() {
        return membershipList;
    }
    
    
    public boolean ipExists(String ip) {
        
    	for(int i=0;i<membershipList.size();i++) {
            if(membershipList.get(i).getIPAddress().equals(ip)) {
                return true;
            }
    	}
    	
    	return false;
    }
    

        
    public void incrHeartbeatCounter(String ownIP) throws SocketException, UnknownHostException {
        for(int i=0;i<membershipList.size();i++) {
            if(membershipList.get(i).getIPAddress().equals(ownIP)) {
                membershipList.get(i).incrHeartbeatCount();
                break;
            }
        }
    }
        
}
