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


package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ChartBuildStep.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ChartBuildStep"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="category"/&gt;
 *     &lt;enumeration value="ptInCategory"/&gt;
 *     &lt;enumeration value="series"/&gt;
 *     &lt;enumeration value="ptInSeries"/&gt;
 *     &lt;enumeration value="allPts"/&gt;
 *     &lt;enumeration value="gridLegend"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_ChartBuildStep")
@XmlEnum
public enum STChartBuildStep {


    /**
     * Category
     * 
     */
    @XmlEnumValue("category")
    CATEGORY("category"),

    /**
     * Category Points
     * 
     */
    @XmlEnumValue("ptInCategory")
    PT_IN_CATEGORY("ptInCategory"),

    /**
     * Series
     * 
     */
    @XmlEnumValue("series")
    SERIES("series"),

    /**
     * Series Points
     * 
     */
    @XmlEnumValue("ptInSeries")
    PT_IN_SERIES("ptInSeries"),

    /**
     * All Points
     * 
     */
    @XmlEnumValue("allPts")
    ALL_PTS("allPts"),

    /**
     * Grid and Legend
     * 
     */
    @XmlEnumValue("gridLegend")
    GRID_LEGEND("gridLegend");
    private final String value;

    STChartBuildStep(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STChartBuildStep fromValue(String v) {
        for (STChartBuildStep c: STChartBuildStep.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
