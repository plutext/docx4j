/*
 *  Copyright 2007-2013, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */


package org.docx4j.wml; 

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="val" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="black", 
 *             &lt;enumeration value="blue", 
 *             &lt;enumeration value="cyan", 
 *             &lt;enumeration value="green", 
 *             &lt;enumeration value="magenta", 
 *             &lt;enumeration value="red", 
 *             &lt;enumeration value="yellow", 
 *             &lt;enumeration value="white", 
 *             &lt;enumeration value="darkBlue", 
 *             &lt;enumeration value="darkCyan", 
 *             &lt;enumeration value="darkGreen", 
 *             &lt;enumeration value="darkMagenta", 
 *             &lt;enumeration value="darkRed", 
 *             &lt;enumeration value="darkYellow", 
 *             &lt;enumeration value="darkGray", 
 *             &lt;enumeration value="lightGray", 
 *             &lt;enumeration value="none", 
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "highlight")
public class Highlight implements Child
{
	
	protected static Logger log = LoggerFactory.getLogger(Highlight.class);
	
	// See http://www.w3.org/TR/css3-color/#svg-color, except for darkYellow (for which I've used gold)
	private final static String[][] colors = { { "black", "000000" }, { "blue", "0000FF" },
			{ "cyan", "00FFFF" }, { "green", "008000" },
			{ "magenta", "FF00FF" }, { "red", "FF0000" },
			{ "yellow", "FFFF00" }, { "white", "FFFFFF" },
			{ "darkBlue", "00008B" }, { "darkCyan", "008B8B" },
			{ "darkGreen", "006400" }, { "darkMagenta", "8B008B" },
			{ "darkRed", "8B0000" }, { "darkYellow", "FFD700" },
			{ "darkGray", "A9A9A9" }, { "lightGray", "D3D3D3" } };
	

    @XmlAttribute(name = "val", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
    protected String val;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the val property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVal() {
        return val;
    }

    public String getHexVal() {
    	
    	if (val==null) return null;
    	
    	for (int i = 0; i<colors.length; i++) {
    		if (val.equals(colors[i][0])) {
    			return "#" + colors[i][1]; 
    		}
    	}     	
		log.error("Unexpected w:highlight value '" + val + "'");
		return null;
    }
    
    /**
     * Sets the value of the val property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVal(String value) {
    	
    	if (value==null) {
    		this.val = value;
    		return;
    	}
    	
    	boolean inEnumeration = false;
    	for (int i = 0; i<colors.length; i++) {
    		if (value.equals(colors[i][0])) {
    			inEnumeration = true; 
    			break;
    		}
    	}
    	
    	if (inEnumeration) {
    		this.val = value;
    		return;
    	} else if (value.trim().startsWith("#")) {
    		value=value.trim().substring(1).toUpperCase();
    		
        	for (int i = 0; i<colors.length; i++) {
        		if (value.equals(colors[i][1])) {
        			val = colors[i][0]; 
        			return;
        		}
        	}
        	
    		log.error("use enumerated color, or implement algorithm to map to closest color: '" + value + "'");
        	
    	} else if (value.trim().contains("rgb")) {
    		
    		log.warn("TODO: implement rgb to color for '" + value + "'");
    	}
		log.error("Can't set w:highlight from '" + value + "'");    	
    	this.val = null;
    }

    /**
     * Gets the parent object in the object tree representing the unmarshalled xml document.
     * 
     * @return
     *     The parent object.
     */
    public Object getParent() {
        return this.parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    /**
     * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
     * 
     * @param parent
     *     The parent object in the object tree.
     * @param unmarshaller
     *     The unmarshaller that generated the instance.
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        setParent(parent);
    }

}

