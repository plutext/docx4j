/*
 *  Copyright 2010-2012, Plutext Pty Ltd.
 *   
 *  This file is part of pptx4j, a component of docx4j.

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
 * <p>Java class for ST_PhotoAlbumFrameShape.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PhotoAlbumFrameShape"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="frameStyle1"/&gt;
 *     &lt;enumeration value="frameStyle2"/&gt;
 *     &lt;enumeration value="frameStyle3"/&gt;
 *     &lt;enumeration value="frameStyle4"/&gt;
 *     &lt;enumeration value="frameStyle5"/&gt;
 *     &lt;enumeration value="frameStyle6"/&gt;
 *     &lt;enumeration value="frameStyle7"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_PhotoAlbumFrameShape")
@XmlEnum
public enum STPhotoAlbumFrameShape {


    /**
     * Rectangle Photo Frame
     * 
     */
    @XmlEnumValue("frameStyle1")
    FRAME_STYLE_1("frameStyle1"),

    /**
     * Rounded Rectangle Photo Frame
     * 
     */
    @XmlEnumValue("frameStyle2")
    FRAME_STYLE_2("frameStyle2"),

    /**
     * Simple White Photo Frame
     * 
     */
    @XmlEnumValue("frameStyle3")
    FRAME_STYLE_3("frameStyle3"),

    /**
     * Simple Black Photo Frame
     * 
     */
    @XmlEnumValue("frameStyle4")
    FRAME_STYLE_4("frameStyle4"),

    /**
     * Compound Black Photo Frame
     * 
     */
    @XmlEnumValue("frameStyle5")
    FRAME_STYLE_5("frameStyle5"),

    /**
     * Center Shadow Photo Frame
     * 
     */
    @XmlEnumValue("frameStyle6")
    FRAME_STYLE_6("frameStyle6"),

    /**
     * Soft Edge Photo Frame
     * 
     */
    @XmlEnumValue("frameStyle7")
    FRAME_STYLE_7("frameStyle7");
    private final String value;

    STPhotoAlbumFrameShape(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPhotoAlbumFrameShape fromValue(String v) {
        for (STPhotoAlbumFrameShape c: STPhotoAlbumFrameShape.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
