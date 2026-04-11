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

import jakarta.xml.bind.UnmarshalException;
import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;


/**
 * <p>Java class for ST_VerticalJc.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_VerticalJc">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="top"/>
 *     &lt;enumeration value="center"/>
 *     &lt;enumeration value="both"/>
 *     &lt;enumeration value="bottom"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_VerticalJc")
@XmlEnum
public enum STVerticalJc {


    /**
     * Align Top
     * 
     */
    @XmlEnumValue("top")
    TOP("top"),

    /**
     * Align Center
     * 
     */
    @XmlEnumValue("center")
    CENTER("center"),

    /**
     * Vertical Justification
     * 
     */
    @XmlEnumValue("both")
    BOTH("both"),

    /**
     * Align Bottom
     * 
     */
    @XmlEnumValue("bottom")
    BOTTOM("bottom");
    private final String value;

    STVerticalJc(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STVerticalJc fromValue(String v) {
        for (STVerticalJc c: STVerticalJc.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    /**
     * Custom Adapter to force JAXB RI to respect validation errors. (MOXy doesn't need it)
     * @since 11.5.13
     */    
    public static class Adapter extends XmlAdapter<String, STVerticalJc> {
    	
    	/*
			The JAXB RI uses a built-in "blind" converter for Enums. If it sees val which doesn't exist, 
			it fails the conversion internally and assigns null to the field.
			
			With this @XmlJavaTypeAdapter, 	JAXB hands the raw string to unmarshal() here
			
			TODO with auto-generation, will need to look at using a JAXB bindings 
			(.xjb) file to inject this adapter automatically.  
			At that point we should add similar to every enum ST
		 * 
    	 */
    	
        @Override
        public STVerticalJc unmarshal(String v) throws Exception {
            try {
                return STVerticalJc.fromValue(v);
            } catch (IllegalArgumentException e) {
                // By wrapping this in UnmarshalException, JAXB is forced to 
                // invoke your ValidationEventHandler.
                throw new UnmarshalException("Invalid value for STVerticalJc: " + v);
            }
        }

        @Override
        public String marshal(STVerticalJc v) {
            return (v != null) ? v.value() : null;
        }
    }    
    
    
}
