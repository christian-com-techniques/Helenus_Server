import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;


public class DstrMarshaller {

    public static String toXML(ArrayList<MembershipEntry> memList) throws JAXBException {
                
        JAXBContext ctx = JAXBContext.newInstance(MembershipList.class);
        Marshaller msh = ctx.createMarshaller();
        StringWriter sw = new StringWriter();
        msh.marshal(new MembershipList(memList), sw);

        return sw.toString();
    }
        
    // Just on case if you want to implement a JSON-marshaller
    public static String toJSON(ArrayList<MembershipEntry> memList) {
        return "";
    }
        
    public static ArrayList<MembershipEntry> unmarshallXML(String xml) throws JAXBException {
        MembershipList memList = new MembershipList ();
        
        JAXBContext context = JAXBContext.newInstance(MembershipList.class);
        Unmarshaller um = context.createUnmarshaller();
        StringReader reader = new StringReader(xml);

        memList = (MembershipList) um.unmarshal(reader);
 
        return memList.get();

    }
        
    public static ArrayList<MembershipEntry> unmarshallJSON(String json) {
        ArrayList<MembershipEntry> mE = new ArrayList<MembershipEntry>();
        return mE;
    }
        
}