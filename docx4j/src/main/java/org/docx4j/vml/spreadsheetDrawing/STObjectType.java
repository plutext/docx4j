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
 * <p>Java class for ST_ObjectType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ObjectType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Button"/>
 *     &lt;enumeration value="Checkbox"/>
 *     &lt;enumeration value="Dialog"/>
 *     &lt;enumeration value="Drop"/>
 *     &lt;enumeration value="Edit"/>
 *     &lt;enumeration value="GBox"/>
 *     &lt;enumeration value="Label"/>
 *     &lt;enumeration value="LineA"/>
 *     &lt;enumeration value="List"/>
 *     &lt;enumeration value="Movie"/>
 *     &lt;enumeration value="Note"/>
 *     &lt;enumeration value="Pict"/>
 *     &lt;enumeration value="Radio"/>
 *     &lt;enumeration value="RectA"/>
 *     &lt;enumeration value="Scroll"/>
 *     &lt;enumeration value="Spin"/>
 *     &lt;enumeration value="Shape"/>
 *     &lt;enumeration value="Group"/>
 *     &lt;enumeration value="Rect"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_ObjectType")
@XmlEnum
public enum STObjectType {


    /**
     * Pushbutton
     * 
     */
    @XmlEnumValue("Button")
    BUTTON("Button"),

    /**
     * Checkbox
     * 
     */
    @XmlEnumValue("Checkbox")
    CHECKBOX("Checkbox"),

    /**
     * Dialog
     * 
     */
    @XmlEnumValue("Dialog")
    DIALOG("Dialog"),

    /**
     * Dropdown Box
     * 
     */
    @XmlEnumValue("Drop")
    DROP("Drop"),

    /**
     * Editable Text Field
     * 
     */
    @XmlEnumValue("Edit")
    EDIT("Edit"),

    /**
     * Group Box
     * 
     */
    @XmlEnumValue("GBox")
    G_BOX("GBox"),

    /**
     * Label
     * 
     */
    @XmlEnumValue("Label")
    LABEL("Label"),

    /**
     * Auditing Line
     * 
     */
    @XmlEnumValue("LineA")
    LINE_A("LineA"),

    /**
     * List Box
     * 
     */
    @XmlEnumValue("List")
    LIST("List"),

    /**
     * Movie
     * 
     */
    @XmlEnumValue("Movie")
    MOVIE("Movie"),

    /**
     * Comment
     * 
     */
    @XmlEnumValue("Note")
    NOTE("Note"),

    /**
     * Image
     * 
     */
    @XmlEnumValue("Pict")
    PICT("Pict"),

    /**
     * Radio Button
     * 
     */
    @XmlEnumValue("Radio")
    RADIO("Radio"),

    /**
     * Auditing Rectangle
     * 
     */
    @XmlEnumValue("RectA")
    RECT_A("RectA"),

    /**
     * Scroll Bar
     * 
     */
    @XmlEnumValue("Scroll")
    SCROLL("Scroll"),

    /**
     * Spin Button
     * 
     */
    @XmlEnumValue("Spin")
    SPIN("Spin"),

    /**
     * Plain Shape
     * 
     */
    @XmlEnumValue("Shape")
    SHAPE("Shape"),

    /**
     * Group
     * 
     */
    @XmlEnumValue("Group")
    GROUP("Group"),

    /**
     * Plain Rectangle
     * 
     */
    @XmlEnumValue("Rect")
    RECT("Rect");
    private final String value;

    STObjectType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STObjectType fromValue(String v) {
        for (STObjectType c: STObjectType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
