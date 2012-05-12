package org.docx4j.convert.out;

import org.w3c.dom.Node;

public class ConvertUtils {
	
    public static String getAttributeValue(Node node, String name) {
    	
//    	log.debug("node " + node.getNodeName() );
//    	
//    	log.debug("@" + name);
        String value = "";
        
        Node attribute = node.getAttributes().getNamedItem(name);
        
//        if (attribute == null) {
//        	log.debug("@" + name + " missing");
//        }
        
        if (attribute != null &&  ((org.w3c.dom.Attr)attribute).getNodeValue()!= null)
        {
            value = ((org.w3c.dom.Attr)attribute).getNodeValue();
        }
//        log.debug(" = " + value);

        return value;
    }
    
    public static String getAttributeValueNS(Node node, String namespaceURI, String localname) {
    	
//    	log.debug("node " + node.getNodeName() );
//    	
//    	log.debug("@" + localname);
        String value = "";
        
        Node attribute = node.getAttributes().getNamedItemNS(namespaceURI, localname);
        
//        if (attribute == null) {
//        	log.debug("@" + localname + " missing");
//        }
        
        if (attribute != null &&  ((org.w3c.dom.Attr)attribute).getNodeValue()!= null)
        {
            value = ((org.w3c.dom.Attr)attribute).getNodeValue();
        }
//        log.debug(" = " + value);

        return value;
    }

	

}
