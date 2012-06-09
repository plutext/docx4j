/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
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
