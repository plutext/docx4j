/*
 *  Copyright 2010, Plutext Pty Ltd.
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


package org.pptx4j.pml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_HtmlPublishWebBrowserSupport.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_HtmlPublishWebBrowserSupport">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="v4"/>
 *     &lt;enumeration value="v3"/>
 *     &lt;enumeration value="v3v4"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_HtmlPublishWebBrowserSupport")
@XmlEnum
public enum STHtmlPublishWebBrowserSupport {


    /**
     * Browser v4
     * 
     */
    @XmlEnumValue("v4")
    V_4("v4"),

    /**
     * Browser v3
     * 
     */
    @XmlEnumValue("v3")
    V_3("v3"),

    /**
     * Browser v3v4
     * 
     */
    @XmlEnumValue("v3v4")
    V_3_V_4("v3v4");
    private final String value;

    STHtmlPublishWebBrowserSupport(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STHtmlPublishWebBrowserSupport fromValue(String v) {
        for (STHtmlPublishWebBrowserSupport c: STHtmlPublishWebBrowserSupport.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
