
package org.docx4j.org.w3.x2003.inkML;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for standardChannelName.type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="standardChannelName.type"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="X"/&gt;
 *     &lt;enumeration value="Y"/&gt;
 *     &lt;enumeration value="Z"/&gt;
 *     &lt;enumeration value="F"/&gt;
 *     &lt;enumeration value="S"/&gt;
 *     &lt;enumeration value="B1"/&gt;
 *     &lt;enumeration value="B2"/&gt;
 *     &lt;enumeration value="B3"/&gt;
 *     &lt;enumeration value="B4"/&gt;
 *     &lt;enumeration value="OTx"/&gt;
 *     &lt;enumeration value="OTy"/&gt;
 *     &lt;enumeration value="OA"/&gt;
 *     &lt;enumeration value="OE"/&gt;
 *     &lt;enumeration value="OR"/&gt;
 *     &lt;enumeration value="C"/&gt;
 *     &lt;enumeration value="CR"/&gt;
 *     &lt;enumeration value="CG"/&gt;
 *     &lt;enumeration value="CB"/&gt;
 *     &lt;enumeration value="CC"/&gt;
 *     &lt;enumeration value="CM"/&gt;
 *     &lt;enumeration value="CY"/&gt;
 *     &lt;enumeration value="CK"/&gt;
 *     &lt;enumeration value="A"/&gt;
 *     &lt;enumeration value="W"/&gt;
 *     &lt;enumeration value="BW"/&gt;
 *     &lt;enumeration value="BH"/&gt;
 *     &lt;enumeration value="T"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "standardChannelName.type")
@XmlEnum
public enum StandardChannelNameType {

    X("X"),
    Y("Y"),
    Z("Z"),
    F("F"),
    S("S"),
    @XmlEnumValue("B1")
    B_1("B1"),
    @XmlEnumValue("B2")
    B_2("B2"),
    @XmlEnumValue("B3")
    B_3("B3"),
    @XmlEnumValue("B4")
    B_4("B4"),
    @XmlEnumValue("OTx")
    O_TX("OTx"),
    @XmlEnumValue("OTy")
    O_TY("OTy"),
    OA("OA"),
    OE("OE"),
    OR("OR"),
    C("C"),
    CR("CR"),
    CG("CG"),
    CB("CB"),
    CC("CC"),
    CM("CM"),
    CY("CY"),
    CK("CK"),
    A("A"),
    W("W"),
    BW("BW"),
    BH("BH"),
    T("T");
    private final String value;

    StandardChannelNameType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static StandardChannelNameType fromValue(String v) {
        for (StandardChannelNameType c: StandardChannelNameType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
