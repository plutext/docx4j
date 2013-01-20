
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_OleUpdate.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_OleUpdate">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="OLEUPDATE_ALWAYS"/>
 *     &lt;enumeration value="OLEUPDATE_ONCALL"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_OleUpdate")
@XmlEnum
public enum STOleUpdate {

    OLEUPDATE_ALWAYS,
    OLEUPDATE_ONCALL;

    public String value() {
        return name();
    }

    public static STOleUpdate fromValue(String v) {
        return valueOf(v);
    }

}
