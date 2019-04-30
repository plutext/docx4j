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
 * <p>Java class for ST_TLDiagramBuildType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TLDiagramBuildType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="whole"/&gt;
 *     &lt;enumeration value="depthByNode"/&gt;
 *     &lt;enumeration value="depthByBranch"/&gt;
 *     &lt;enumeration value="breadthByNode"/&gt;
 *     &lt;enumeration value="breadthByLvl"/&gt;
 *     &lt;enumeration value="cw"/&gt;
 *     &lt;enumeration value="cwIn"/&gt;
 *     &lt;enumeration value="cwOut"/&gt;
 *     &lt;enumeration value="ccw"/&gt;
 *     &lt;enumeration value="ccwIn"/&gt;
 *     &lt;enumeration value="ccwOut"/&gt;
 *     &lt;enumeration value="inByRing"/&gt;
 *     &lt;enumeration value="outByRing"/&gt;
 *     &lt;enumeration value="up"/&gt;
 *     &lt;enumeration value="down"/&gt;
 *     &lt;enumeration value="allAtOnce"/&gt;
 *     &lt;enumeration value="cust"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_TLDiagramBuildType")
@XmlEnum
public enum STTLDiagramBuildType {


    /**
     * Diagram Build Type Enum ( Whole )
     * 
     */
    @XmlEnumValue("whole")
    WHOLE("whole"),

    /**
     * Diagram Build Type Enum ( Depth By Node )
     * 
     */
    @XmlEnumValue("depthByNode")
    DEPTH_BY_NODE("depthByNode"),

    /**
     * Diagram Build Type Enum ( Depth By Branch )
     * 
     */
    @XmlEnumValue("depthByBranch")
    DEPTH_BY_BRANCH("depthByBranch"),

    /**
     * Diagram Build Type Enum ( Breadth By Node )
     * 
     */
    @XmlEnumValue("breadthByNode")
    BREADTH_BY_NODE("breadthByNode"),

    /**
     * Diagram Build Type Enum ( Breadth By Level )
     * 
     */
    @XmlEnumValue("breadthByLvl")
    BREADTH_BY_LVL("breadthByLvl"),

    /**
     * Diagram Build Type Enum ( Clockwise )
     * 
     */
    @XmlEnumValue("cw")
    CW("cw"),

    /**
     * Diagram Build Type Enum ( Clockwise-In )
     * 
     */
    @XmlEnumValue("cwIn")
    CW_IN("cwIn"),

    /**
     * Diagram Build Type Enum ( Clockwise-Out )
     * 
     */
    @XmlEnumValue("cwOut")
    CW_OUT("cwOut"),

    /**
     * Diagram Build Type Enum ( Counter-Clockwise )
     * 
     */
    @XmlEnumValue("ccw")
    CCW("ccw"),

    /**
     * Diagram Build Type Enum ( Counter-Clockwise-In )
     * 
     */
    @XmlEnumValue("ccwIn")
    CCW_IN("ccwIn"),

    /**
     * Diagram Build Type Enum ( Counter-Clockwise-Out )
     * 
     */
    @XmlEnumValue("ccwOut")
    CCW_OUT("ccwOut"),

    /**
     * Diagram Build Type Enum ( In-By-Ring )
     * 
     */
    @XmlEnumValue("inByRing")
    IN_BY_RING("inByRing"),

    /**
     * Diagram Build Type Enum ( Out-By-Ring )
     * 
     */
    @XmlEnumValue("outByRing")
    OUT_BY_RING("outByRing"),

    /**
     * Diagram Build Type Enum ( Up )
     * 
     */
    @XmlEnumValue("up")
    UP("up"),

    /**
     * Diagram Build Type Enum ( Down )
     * 
     */
    @XmlEnumValue("down")
    DOWN("down"),

    /**
     * Diagram Build Type Enum ( All At Once )
     * 
     */
    @XmlEnumValue("allAtOnce")
    ALL_AT_ONCE("allAtOnce"),

    /**
     * Diagram Build Type Enum ( Custom )
     * 
     */
    @XmlEnumValue("cust")
    CUST("cust");
    private final String value;

    STTLDiagramBuildType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTLDiagramBuildType fromValue(String v) {
        for (STTLDiagramBuildType c: STTLDiagramBuildType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
