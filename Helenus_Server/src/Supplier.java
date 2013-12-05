import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class Supplier {

    // For simulating packet loss
    private static double packetLossProbability = 0.0;
        
    public static void send(String ip, int port, String message) throws IOException {
              
        double r = Math.random();
                
        if(r > packetLossProbability) {
            byte[] msgtosend = message.getBytes();
            ip = ip.replace("\"", "");
            InetAddress sendip = InetAddress.getByName(ip);
            DatagramPacket packet = new DatagramPacket(msgtosend, msgtosend.length, sendip, port);
            DatagramSocket dsocket = new DatagramSocket();
            dsocket.send(packet);
            dsocket.close();
        }
                
    }
        
}