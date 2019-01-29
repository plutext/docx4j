package org.docx4j.model.datastorage;

public class XmlNameUtil
{

    public static String descapeXmlTypeName(String name)
    {
    	
        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = name.length(); i < len; ++i) {

            char c = name.charAt(i);
            if (c == '_') {
            	//escape if next is x
            	if (i==len-1) {
                	writeChar(sb, c);            		
            		continue;
            	} 
            	if (name.charAt(i+1)== 'x') {
            		String hex = name.substring(i+2, i+6);
                	writeChar(sb, descape(hex));            		
            		i=i+6;
            		continue;
            	} else {
                	writeChar(sb, c);            		
            		continue;
            	}            		
            }
        	writeChar(sb, c);            		        	
        }
        
        return sb.toString();
    }
    
    /**
     * translated into XML names in a way in which the invalid characters 
     * are translated into escaped numeric entity encoding.
     * 
     * Curly braces, $, "/" all occur in JSON and cause problems
     * in element names.  Also, sometimes the name would start
     * with a number.  
     * @param name
     * @return
     */
    public static String escapeXmlTypeName(String name)
    {
    	/* We can use the same escape rules as 
    	 * https://docs.microsoft.com/en-us/sql/relational-databases/xml/invalid-characters-and-escape-rules?view=sql-server-2017
    	 * 
    	 * the underscore is chosen as the escape character. Following are the escape rules that are used for encoding:
    	 * 
    	 * Any UCS-2 character that is not a valid XML name character, according to 
    	 * the XML 1.0 specification, is escaped as xHHHH\. The HHHH stands for the 
    	 * four-digit hexadecimal UCS-2 code for the character in the most significant bit-first order. 
    	 * For example, the table name Order Details is encoded as Order_x0020_Details.
    	 * 
    	 * The underscore character does not have to be escaped unless it is followed by the character x. 
    	 * For example, the table name Order_Details is not encoded.
    	 */
    	
        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = name.length(); i < len; ++i) {
            char c = name.charAt(i);
            if (c >= 'a' && c <= 'z') {
            	writeChar(sb, c);
            	continue;
            }
            if (c >= 'A' && c <= 'Z') {
            	writeChar(sb, c);
            	continue;
            }
            if (c == '_') {
            	//escape if next is x
            	if (i==len-1) {
                	writeChar(sb, c);            		
            		continue;
            	} 
            	if (name.charAt(i+1)== 'x') {
                	escapeChar(sb, c);            		
            		continue;
            	} else {
                	writeChar(sb, c);            		
            		continue;
            	}            		
            }
            if (c > 127) {
            	escapeChar(sb, c);
            	continue;
            }
            if (c >= '0' && c <= '9') {
            	if (i>0) {
                	writeChar(sb, c);            		
            		continue;
            	} else {
            		// can't start with a number
                	escapeChar(sb, c);            		
            		continue;
            	}
            }
            if (c == '.' || c == '-') {
            	if (i>0) {
                	writeChar(sb, c);            		
            		continue;
            	} else {
            		// can't start with this
                	escapeChar(sb, c);            		
            		continue;
            	}
            }
            // Ok, need to replace
        	escapeChar(sb, c);            		
        }
        return sb.toString();
    }
    
    private static void writeChar(StringBuilder sb, char c) {
    	sb.append(c);
    }

    private static void escapeChar(StringBuilder sb, char c) {
    	sb.append("_x" + String.format("%04x", (int) c) + "_");
    }

    private static char descape(String hexString) {
    	int value = Integer.parseInt(hexString, 16);
    	return (char)value;
    }
    
//    public static void main(String[] args) { 
//    	char c = 'c'; 
//    	String hex = String.format("%04x", (int) c);
//    	System.out.println(hex);
//    	System.out.println(descape(hex));
//    }

//    public static void main(String[] args) { 
//    	String res = escapeXmlTypeName("200");
//    	System.out.println(descapeXmlTypeName(res));
//    }
    
}
