
public class KVEntry<T> {

    private String key;
    private T value;
    private Boolean redistribute;
	
    public KVEntry(String key, T value) {
        this.key = key;
        this.value = value;
        this.redistribute = false;
    }
	
    public String getKey() {
        return key;
    }
	
    public T getValue() {
        return value;
    }
	
    public void setValue(T value) {
        this.value = value;
    }
	
    public void setRedistribute(Boolean value) {
        this.redistribute = value;
    }
    
    public Boolean getRedistribute() {
        return this.redistribute;
    }
}
