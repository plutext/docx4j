
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_DvAspect.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_DvAspect">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="DVASPECT_CONTENT"/>
 *     &lt;enumeration value="DVASPECT_ICON"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_DvAspect")
@XmlEnum
public enum STDvAspect {

    DVASPECT_CONTENT,
    DVASPECT_ICON;

    public String value() {
        return name();
    }

    public static STDvAspect fromValue(String v) {
        return valueOf(v);
    }

}
