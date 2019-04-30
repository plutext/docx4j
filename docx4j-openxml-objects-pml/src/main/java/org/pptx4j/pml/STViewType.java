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
 * <p>Java class for ST_ViewType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ViewType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="sldView"/&gt;
 *     &lt;enumeration value="sldMasterView"/&gt;
 *     &lt;enumeration value="notesView"/&gt;
 *     &lt;enumeration value="handoutView"/&gt;
 *     &lt;enumeration value="notesMasterView"/&gt;
 *     &lt;enumeration value="outlineView"/&gt;
 *     &lt;enumeration value="sldSorterView"/&gt;
 *     &lt;enumeration value="sldThumbnailView"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_ViewType")
@XmlEnum
public enum STViewType {


    /**
     * Normal Slide View
     * 
     */
    @XmlEnumValue("sldView")
    SLD_VIEW("sldView"),

    /**
     * Slide Master View
     * 
     */
    @XmlEnumValue("sldMasterView")
    SLD_MASTER_VIEW("sldMasterView"),

    /**
     * Notes View
     * 
     */
    @XmlEnumValue("notesView")
    NOTES_VIEW("notesView"),

    /**
     * Handout View
     * 
     */
    @XmlEnumValue("handoutView")
    HANDOUT_VIEW("handoutView"),

    /**
     * Notes Master View
     * 
     */
    @XmlEnumValue("notesMasterView")
    NOTES_MASTER_VIEW("notesMasterView"),

    /**
     * Outline View
     * 
     */
    @XmlEnumValue("outlineView")
    OUTLINE_VIEW("outlineView"),

    /**
     * Slide Sorter View
     * 
     */
    @XmlEnumValue("sldSorterView")
    SLD_SORTER_VIEW("sldSorterView"),

    /**
     * Slide Thumbnail View
     * 
     */
    @XmlEnumValue("sldThumbnailView")
    SLD_THUMBNAIL_VIEW("sldThumbnailView");
    private final String value;

    STViewType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STViewType fromValue(String v) {
        for (STViewType c: STViewType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
