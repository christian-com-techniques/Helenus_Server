import java.util.ArrayList;


public class LookupCacheEntry {

    private String ip;
    private int port;
    private ArrayList<String> collectedValues;
    String consistencyLevel;
    int writeconsistencyCounter;
	
    public LookupCacheEntry(String ip, int port, String consistencyLevel) {
        this.ip = ip;
        this.port = port;
        this.collectedValues = new ArrayList<String>();
        this.consistencyLevel = consistencyLevel;
        writeconsistencyCounter = 0;
    }
	
    public String getIp() {
        return ip;
    }
	
    public void addValue(String value) {
    	collectedValues.add(value);
    }
    
    public String getConsistencyLevel() {
    	return consistencyLevel;
    }
    
    public String getConOne() {
    	return collectedValues.get(0);
    }
    
    public String getConQuorum(int numberOfNodes) {
    	String matchValue = "";
    	int match = 0;
		String cacheValue;
    	
    	for(int i=0;i<collectedValues.size();i++) {
    		cacheValue = collectedValues.get(i);
    		int counter = 0;
    		
        	for(int j=0;j<collectedValues.size();j++) {
        		if(collectedValues.get(j).equals(cacheValue)) {
        			counter++;
        			
        			if(counter > match) {
        				match = counter;
        				matchValue = collectedValues.get(j);
        			}
        		}
        	}
        	
    	}
    	
    	if(match > numberOfNodes/2) {
        	return matchValue;
    	} else {
    		return "";
    	}
    	
    }
    
    public String getConAll(int numberOfNodes) {
    	int counter = 0;
    	String cacheValue = collectedValues.get(0);
    	
    	for(int i=0;i<collectedValues.size();i++) {
    		if(collectedValues.get(i).equals(cacheValue)) {
    			counter ++;
    		}
    	}
    	
    	if(counter == numberOfNodes) {
    		return cacheValue;
    	} else {
    		return "";
    	}
    	
    }
    
    public int getCounter() {
    	return writeconsistencyCounter;
    }
    
    public void setCounter(int number) {
    	writeconsistencyCounter = number;
    }
    
    public ArrayList<String> getList() {
    	return collectedValues;
    }
    
}
