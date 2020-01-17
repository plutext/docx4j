/*
 *  Copyright 2013, Plutext Pty Ltd.
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


package org.docx4j.w14;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_Ligatures.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Ligatures">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="standard"/>
 *     &lt;enumeration value="contextual"/>
 *     &lt;enumeration value="historical"/>
 *     &lt;enumeration value="discretional"/>
 *     &lt;enumeration value="standardContextual"/>
 *     &lt;enumeration value="standardHistorical"/>
 *     &lt;enumeration value="contextualHistorical"/>
 *     &lt;enumeration value="standardDiscretional"/>
 *     &lt;enumeration value="contextualDiscretional"/>
 *     &lt;enumeration value="historicalDiscretional"/>
 *     &lt;enumeration value="standardContextualHistorical"/>
 *     &lt;enumeration value="standardContextualDiscretional"/>
 *     &lt;enumeration value="standardHistoricalDiscretional"/>
 *     &lt;enumeration value="contextualHistoricalDiscretional"/>
 *     &lt;enumeration value="all"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_Ligatures")
@XmlEnum
public enum STLigatures {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("standard")
    STANDARD("standard"),
    @XmlEnumValue("contextual")
    CONTEXTUAL("contextual"),
    @XmlEnumValue("historical")
    HISTORICAL("historical"),
    @XmlEnumValue("discretional")
    DISCRETIONAL("discretional"),
    @XmlEnumValue("standardContextual")
    STANDARD_CONTEXTUAL("standardContextual"),
    @XmlEnumValue("standardHistorical")
    STANDARD_HISTORICAL("standardHistorical"),
    @XmlEnumValue("contextualHistorical")
    CONTEXTUAL_HISTORICAL("contextualHistorical"),
    @XmlEnumValue("standardDiscretional")
    STANDARD_DISCRETIONAL("standardDiscretional"),
    @XmlEnumValue("contextualDiscretional")
    CONTEXTUAL_DISCRETIONAL("contextualDiscretional"),
    @XmlEnumValue("historicalDiscretional")
    HISTORICAL_DISCRETIONAL("historicalDiscretional"),
    @XmlEnumValue("standardContextualHistorical")
    STANDARD_CONTEXTUAL_HISTORICAL("standardContextualHistorical"),
    @XmlEnumValue("standardContextualDiscretional")
    STANDARD_CONTEXTUAL_DISCRETIONAL("standardContextualDiscretional"),
    @XmlEnumValue("standardHistoricalDiscretional")
    STANDARD_HISTORICAL_DISCRETIONAL("standardHistoricalDiscretional"),
    @XmlEnumValue("contextualHistoricalDiscretional")
    CONTEXTUAL_HISTORICAL_DISCRETIONAL("contextualHistoricalDiscretional"),
    @XmlEnumValue("all")
    ALL("all");
    private final String value;

    STLigatures(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STLigatures fromValue(String v) {
        for (STLigatures c: STLigatures.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
