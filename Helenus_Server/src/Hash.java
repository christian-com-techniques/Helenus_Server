import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;


public class Hash {

	public static long value(String value, int length) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		  
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(value.getBytes("UTF-8"));
		
		String sha1 = byteToHex(md.digest());
		
		if(length > 16) {
			length = 16;
		}
		
		sha1 = sha1.replaceAll("[^\\d.]", "");
		String cut = sha1.substring(0, length-1);
		
		while(cut.length() < length) {
			cut += "1";
		}
		
		long re = Long.parseLong(String.valueOf(Long.parseLong(cut, 16)).substring(0, length));
		return re;

	}
	
	private static String byteToHex(final byte[] hash) {
	    Formatter formatter = new Formatter();
	    for (byte b : hash) {
	        formatter.format("%02x", b);
	    }
	    String result = formatter.toString();
	    formatter.close();
	    return result;
	}
	
}
