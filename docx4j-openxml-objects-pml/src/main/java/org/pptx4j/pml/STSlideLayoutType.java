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
 * <p>Java class for ST_SlideLayoutType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_SlideLayoutType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="title"/>
 *     &lt;enumeration value="tx"/>
 *     &lt;enumeration value="twoColTx"/>
 *     &lt;enumeration value="tbl"/>
 *     &lt;enumeration value="txAndChart"/>
 *     &lt;enumeration value="chartAndTx"/>
 *     &lt;enumeration value="dgm"/>
 *     &lt;enumeration value="chart"/>
 *     &lt;enumeration value="txAndClipArt"/>
 *     &lt;enumeration value="clipArtAndTx"/>
 *     &lt;enumeration value="titleOnly"/>
 *     &lt;enumeration value="blank"/>
 *     &lt;enumeration value="txAndObj"/>
 *     &lt;enumeration value="objAndTx"/>
 *     &lt;enumeration value="objOnly"/>
 *     &lt;enumeration value="obj"/>
 *     &lt;enumeration value="txAndMedia"/>
 *     &lt;enumeration value="mediaAndTx"/>
 *     &lt;enumeration value="objOverTx"/>
 *     &lt;enumeration value="txOverObj"/>
 *     &lt;enumeration value="txAndTwoObj"/>
 *     &lt;enumeration value="twoObjAndTx"/>
 *     &lt;enumeration value="twoObjOverTx"/>
 *     &lt;enumeration value="fourObj"/>
 *     &lt;enumeration value="vertTx"/>
 *     &lt;enumeration value="clipArtAndVertTx"/>
 *     &lt;enumeration value="vertTitleAndTx"/>
 *     &lt;enumeration value="vertTitleAndTxOverChart"/>
 *     &lt;enumeration value="twoObj"/>
 *     &lt;enumeration value="objAndTwoObj"/>
 *     &lt;enumeration value="twoObjAndObj"/>
 *     &lt;enumeration value="cust"/>
 *     &lt;enumeration value="secHead"/>
 *     &lt;enumeration value="twoTxTwoObj"/>
 *     &lt;enumeration value="objTx"/>
 *     &lt;enumeration value="picTx"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_SlideLayoutType")
@XmlEnum
public enum STSlideLayoutType {


    /**
     * Slide Layout Type Enumeration ( Title )
     * 
     */
    @XmlEnumValue("title")
    TITLE("title"),

    /**
     * Slide Layout Type Enumeration ( Text )
     * 
     */
    @XmlEnumValue("tx")
    TX("tx"),

    /**
     * Slide Layout Type Enumeration ( Two Column Text )
     * 
     */
    @XmlEnumValue("twoColTx")
    TWO_COL_TX("twoColTx"),

    /**
     * Slide Layout Type Enumeration ( Table )
     * 
     */
    @XmlEnumValue("tbl")
    TBL("tbl"),

    /**
     * Slide Layout Type Enumeration ( Text and Chart )
     * 
     */
    @XmlEnumValue("txAndChart")
    TX_AND_CHART("txAndChart"),

    /**
     * Slide Layout Type Enumeration ( Chart and Text )
     * 
     */
    @XmlEnumValue("chartAndTx")
    CHART_AND_TX("chartAndTx"),

    /**
     * Slide Layout Type Enumeration ( Diagram )
     * 
     */
    @XmlEnumValue("dgm")
    DGM("dgm"),

    /**
     * Chart
     * 
     */
    @XmlEnumValue("chart")
    CHART("chart"),

    /**
     * Text and Clip Art
     * 
     */
    @XmlEnumValue("txAndClipArt")
    TX_AND_CLIP_ART("txAndClipArt"),

    /**
     * Clip Art and Text
     * 
     */
    @XmlEnumValue("clipArtAndTx")
    CLIP_ART_AND_TX("clipArtAndTx"),

    /**
     * Slide Layout Type Enumeration ( Title Only )
     * 
     */
    @XmlEnumValue("titleOnly")
    TITLE_ONLY("titleOnly"),

    /**
     * Slide Layout Type Enumeration ( Blank )
     * 
     */
    @XmlEnumValue("blank")
    BLANK("blank"),

    /**
     * Slide Layout Type Enumeration ( Text and Object )
     * 
     */
    @XmlEnumValue("txAndObj")
    TX_AND_OBJ("txAndObj"),

    /**
     * Slide Layout Type Enumeration ( Object and Text )
     * 
     */
    @XmlEnumValue("objAndTx")
    OBJ_AND_TX("objAndTx"),

    /**
     * Object
     * 
     */
    @XmlEnumValue("objOnly")
    OBJ_ONLY("objOnly"),

    /**
     * Title and Object
     * 
     */
    @XmlEnumValue("obj")
    OBJ("obj"),

    /**
     * Slide Layout Type Enumeration ( Text and Media )
     * 
     */
    @XmlEnumValue("txAndMedia")
    TX_AND_MEDIA("txAndMedia"),

    /**
     * Slide Layout Type Enumeration ( Media and Text )
     * 
     */
    @XmlEnumValue("mediaAndTx")
    MEDIA_AND_TX("mediaAndTx"),

    /**
     * Slide Layout Type Enumeration ( Object over Text)
     * 
     */
    @XmlEnumValue("objOverTx")
    OBJ_OVER_TX("objOverTx"),

    /**
     * Slide Layout Type Enumeration ( Text over Object)
     * 
     */
    @XmlEnumValue("txOverObj")
    TX_OVER_OBJ("txOverObj"),

    /**
     * Text and Two Objects
     * 
     */
    @XmlEnumValue("txAndTwoObj")
    TX_AND_TWO_OBJ("txAndTwoObj"),

    /**
     * Two Objects and Text
     * 
     */
    @XmlEnumValue("twoObjAndTx")
    TWO_OBJ_AND_TX("twoObjAndTx"),

    /**
     * Two Objects over Text
     * 
     */
    @XmlEnumValue("twoObjOverTx")
    TWO_OBJ_OVER_TX("twoObjOverTx"),

    /**
     * Four Objects
     * 
     */
    @XmlEnumValue("fourObj")
    FOUR_OBJ("fourObj"),

    /**
     * Vertical Text
     * 
     */
    @XmlEnumValue("vertTx")
    VERT_TX("vertTx"),

    /**
     * Clip Art and Vertical Text
     * 
     */
    @XmlEnumValue("clipArtAndVertTx")
    CLIP_ART_AND_VERT_TX("clipArtAndVertTx"),

    /**
     * Vertical Title and Text
     * 
     */
    @XmlEnumValue("vertTitleAndTx")
    VERT_TITLE_AND_TX("vertTitleAndTx"),

    /**
     * Vertical Title and Text Over Chart
     * 
     */
    @XmlEnumValue("vertTitleAndTxOverChart")
    VERT_TITLE_AND_TX_OVER_CHART("vertTitleAndTxOverChart"),

    /**
     * Two Objects
     * 
     */
    @XmlEnumValue("twoObj")
    TWO_OBJ("twoObj"),

    /**
     * Object and Two Object
     * 
     */
    @XmlEnumValue("objAndTwoObj")
    OBJ_AND_TWO_OBJ("objAndTwoObj"),

    /**
     * Two Objects and Object
     * 
     */
    @XmlEnumValue("twoObjAndObj")
    TWO_OBJ_AND_OBJ("twoObjAndObj"),

    /**
     * Slide Layout Type Enumeration ( Custom )
     * 
     */
    @XmlEnumValue("cust")
    CUST("cust"),

    /**
     * Section Header
     * 
     */
    @XmlEnumValue("secHead")
    SEC_HEAD("secHead"),

    /**
     * Two Text and Two Objects
     * 
     */
    @XmlEnumValue("twoTxTwoObj")
    TWO_TX_TWO_OBJ("twoTxTwoObj"),

    /**
     * Title, Object, and Caption
     * 
     */
    @XmlEnumValue("objTx")
    OBJ_TX("objTx"),

    /**
     * Picture and Caption
     * 
     */
    @XmlEnumValue("picTx")
    PIC_TX("picTx");
    private final String value;

    STSlideLayoutType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STSlideLayoutType fromValue(String v) {
        for (STSlideLayoutType c: STSlideLayoutType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
