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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_Shd.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Shd">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="nil"/>
 *     &lt;enumeration value="clear"/>
 *     &lt;enumeration value="solid"/>
 *     &lt;enumeration value="horzStripe"/>
 *     &lt;enumeration value="vertStripe"/>
 *     &lt;enumeration value="reverseDiagStripe"/>
 *     &lt;enumeration value="diagStripe"/>
 *     &lt;enumeration value="horzCross"/>
 *     &lt;enumeration value="diagCross"/>
 *     &lt;enumeration value="thinHorzStripe"/>
 *     &lt;enumeration value="thinVertStripe"/>
 *     &lt;enumeration value="thinReverseDiagStripe"/>
 *     &lt;enumeration value="thinDiagStripe"/>
 *     &lt;enumeration value="thinHorzCross"/>
 *     &lt;enumeration value="thinDiagCross"/>
 *     &lt;enumeration value="pct5"/>
 *     &lt;enumeration value="pct10"/>
 *     &lt;enumeration value="pct12"/>
 *     &lt;enumeration value="pct15"/>
 *     &lt;enumeration value="pct20"/>
 *     &lt;enumeration value="pct25"/>
 *     &lt;enumeration value="pct30"/>
 *     &lt;enumeration value="pct35"/>
 *     &lt;enumeration value="pct37"/>
 *     &lt;enumeration value="pct40"/>
 *     &lt;enumeration value="pct45"/>
 *     &lt;enumeration value="pct50"/>
 *     &lt;enumeration value="pct55"/>
 *     &lt;enumeration value="pct60"/>
 *     &lt;enumeration value="pct62"/>
 *     &lt;enumeration value="pct65"/>
 *     &lt;enumeration value="pct70"/>
 *     &lt;enumeration value="pct75"/>
 *     &lt;enumeration value="pct80"/>
 *     &lt;enumeration value="pct85"/>
 *     &lt;enumeration value="pct87"/>
 *     &lt;enumeration value="pct90"/>
 *     &lt;enumeration value="pct95"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_Shd")
@XmlEnum
public enum STShd {


    /**
     * No Pattern
     * 
     */
    @XmlEnumValue("nil")
    NIL("nil"),

    /**
     * No Pattern
     * 
     */
    @XmlEnumValue("clear")
    CLEAR("clear"),

    /**
     *  100% Fill Pattern
     * 
     */
    @XmlEnumValue("solid")
    SOLID("solid"),

    /**
     * Horizontal Stripe
     * 						Pattern
     * 
     */
    @XmlEnumValue("horzStripe")
    HORZ_STRIPE("horzStripe"),

    /**
     * Vertical Stripe Pattern
     * 
     */
    @XmlEnumValue("vertStripe")
    VERT_STRIPE("vertStripe"),

    /**
     * Reverse Diagonal Stripe
     * 						Pattern
     * 
     */
    @XmlEnumValue("reverseDiagStripe")
    REVERSE_DIAG_STRIPE("reverseDiagStripe"),

    /**
     * Diagonal Stripe Pattern
     * 
     */
    @XmlEnumValue("diagStripe")
    DIAG_STRIPE("diagStripe"),

    /**
     * Horizontal Cross
     * 						Pattern
     * 
     */
    @XmlEnumValue("horzCross")
    HORZ_CROSS("horzCross"),

    /**
     * Diagonal Cross Pattern
     * 
     */
    @XmlEnumValue("diagCross")
    DIAG_CROSS("diagCross"),

    /**
     * Thin Horizontal Stripe
     * 						Pattern
     * 
     */
    @XmlEnumValue("thinHorzStripe")
    THIN_HORZ_STRIPE("thinHorzStripe"),

    /**
     * Thin Vertical Stripe
     * 						Pattern
     * 
     */
    @XmlEnumValue("thinVertStripe")
    THIN_VERT_STRIPE("thinVertStripe"),

    /**
     * Thin Reverse Diagonal Stripe
     * 						Pattern
     * 
     */
    @XmlEnumValue("thinReverseDiagStripe")
    THIN_REVERSE_DIAG_STRIPE("thinReverseDiagStripe"),

    /**
     * Thin Diagonal Stripe
     * 						Pattern
     * 
     */
    @XmlEnumValue("thinDiagStripe")
    THIN_DIAG_STRIPE("thinDiagStripe"),

    /**
     * Thin Horizontal Cross
     * 						Pattern
     * 
     */
    @XmlEnumValue("thinHorzCross")
    THIN_HORZ_CROSS("thinHorzCross"),

    /**
     * Thin Diagonal Cross
     * 						Pattern
     * 
     */
    @XmlEnumValue("thinDiagCross")
    THIN_DIAG_CROSS("thinDiagCross"),

    /**
     *  5% Fill Pattern
     * 
     */
    @XmlEnumValue("pct5")
    PCT_5("pct5"),

    /**
     *  10% Fill Pattern
     * 
     */
    @XmlEnumValue("pct10")
    PCT_10("pct10"),

    /**
     *  12.5% Fill Pattern
     * 
     */
    @XmlEnumValue("pct12")
    PCT_12("pct12"),

    /**
     *  15% Fill Pattern
     * 
     */
    @XmlEnumValue("pct15")
    PCT_15("pct15"),

    /**
     *  20% Fill Pattern
     * 
     */
    @XmlEnumValue("pct20")
    PCT_20("pct20"),

    /**
     *  25% Fill Pattern
     * 
     */
    @XmlEnumValue("pct25")
    PCT_25("pct25"),

    /**
     *  30% Fill Pattern
     * 
     */
    @XmlEnumValue("pct30")
    PCT_30("pct30"),

    /**
     *  35% Fill Pattern
     * 
     */
    @XmlEnumValue("pct35")
    PCT_35("pct35"),

    /**
     *  37.5% Fill Pattern
     * 
     */
    @XmlEnumValue("pct37")
    PCT_37("pct37"),

    /**
     *  40% Fill Pattern
     * 
     */
    @XmlEnumValue("pct40")
    PCT_40("pct40"),

    /**
     *  45% Fill Pattern
     * 
     */
    @XmlEnumValue("pct45")
    PCT_45("pct45"),

    /**
     *  50% Fill Pattern
     * 
     */
    @XmlEnumValue("pct50")
    PCT_50("pct50"),

    /**
     *  55% Fill Pattern
     * 
     */
    @XmlEnumValue("pct55")
    PCT_55("pct55"),

    /**
     *  60% Fill Pattern
     * 
     */
    @XmlEnumValue("pct60")
    PCT_60("pct60"),

    /**
     *  62.5% Fill Pattern
     * 
     */
    @XmlEnumValue("pct62")
    PCT_62("pct62"),

    /**
     *  65% Fill Pattern
     * 
     */
    @XmlEnumValue("pct65")
    PCT_65("pct65"),

    /**
     *  70% Fill Pattern
     * 
     */
    @XmlEnumValue("pct70")
    PCT_70("pct70"),

    /**
     *  75% Fill Pattern
     * 
     */
    @XmlEnumValue("pct75")
    PCT_75("pct75"),

    /**
     *  80% Fill Pattern
     * 
     */
    @XmlEnumValue("pct80")
    PCT_80("pct80"),

    /**
     *  85% Fill Pattern
     * 
     */
    @XmlEnumValue("pct85")
    PCT_85("pct85"),

    /**
     *  87.5% Fill Pattern
     * 
     */
    @XmlEnumValue("pct87")
    PCT_87("pct87"),

    /**
     *  90% Fill Pattern
     * 
     */
    @XmlEnumValue("pct90")
    PCT_90("pct90"),

    /**
     *  95% Fill Pattern
     * 
     */
    @XmlEnumValue("pct95")
    PCT_95("pct95");
    private final String value;

    STShd(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STShd fromValue(String v) {
        for (STShd c: STShd.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
