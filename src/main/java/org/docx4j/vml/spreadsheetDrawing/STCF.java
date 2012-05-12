/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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


package org.docx4j.vml.spreadsheetDrawing;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_CF.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_CF">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="PictOld"/>
 *     &lt;enumeration value="Pict"/>
 *     &lt;enumeration value="Bitmap"/>
 *     &lt;enumeration value="PictPrint"/>
 *     &lt;enumeration value="PictScreen"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_CF")
@XmlEnum
public enum STCF {


    /**
     * WMF
     * 
     */
    @XmlEnumValue("PictOld")
    PICT_OLD("PictOld"),

    /**
     * EMF
     * 
     */
    @XmlEnumValue("Pict")
    PICT("Pict"),

    /**
     * Bitmap
     * 
     */
    @XmlEnumValue("Bitmap")
    BITMAP("Bitmap"),

    /**
     * Printer Picture
     * 
     */
    @XmlEnumValue("PictPrint")
    PICT_PRINT("PictPrint"),

    /**
     * Screen Picture EMF
     * 
     */
    @XmlEnumValue("PictScreen")
    PICT_SCREEN("PictScreen");
    private final String value;

    STCF(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STCF fromValue(String v) {
        for (STCF c: STCF.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
