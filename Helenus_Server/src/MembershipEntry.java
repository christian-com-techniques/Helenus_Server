import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "membershipentry")
public class MembershipEntry {
	int id;
    private int heartbeatCounter;
    private long joinedtstamp;
    private long lastupdtstamp;
    private String ipAddress;
    boolean failedFlag;

    public MembershipEntry() {
            
    }
    
    public MembershipEntry(int id, String ipAddress) {
    	this.id = id;
        this.heartbeatCounter = 0;
        this.joinedtstamp = new Date().getTime()/1000;
        this.lastupdtstamp = this.joinedtstamp;
        this.ipAddress = ipAddress;
        this.failedFlag = false;
    }
    
    public int getHeartbeatCounter() {
            return heartbeatCounter;
    }
    
    public long getJoinedtstamp() {
            return joinedtstamp;
    }
    
    public long getLastupdtstamp() {
            return lastupdtstamp;
    }
    
    public String getIPAddress() {
            return ipAddress;
    }
    
    public int getID() {
        return id;
}
    
    public boolean getFailedFlag() {
            return failedFlag;
    }
    
    public void incrHeartbeatCount() {
            heartbeatCounter++;
        long currentTime = new Date().getTime()/1000;
        lastupdtstamp = currentTime;
    }
    
    public void setFailedFlag(boolean failed) {
            failedFlag = failed;
    }
    
    public void setHeartbeat(int heartbeatCounter) {
            this.heartbeatCounter = heartbeatCounter;
    }
    
    public void setLastUpdTstamp(long lastupdtstamp) {
            this.lastupdtstamp = lastupdtstamp;
    }

}