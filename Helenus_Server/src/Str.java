
public class Str {

	//Not in use yet. Can be used to find out, if a value is an int or double to safe the key-value
	//pair with the right datatype (currently, everything is a string)
	
	public static boolean isDouble(String str) {
		return str.matches("-?\\d+(\\.\\d+)?");
	}
	
	public static boolean isInteger(String str) {
	    try { 
	        Integer.parseInt(str); 
	    } catch(NumberFormatException nfe) { 
	        return false; 
	    }
	    return true;
	}
	
}
