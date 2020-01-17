
package org.docx4j.org.w3.x2003.inkML;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for booleanStr.type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="booleanStr.type"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="F"/&gt;
 *     &lt;enumeration value="T"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "booleanStr.type")
@XmlEnum
public enum BooleanStrType {

    F,
    T;

    public String value() {
        return name();
    }

    public static BooleanStrType fromValue(String v) {
        return valueOf(v);
    }

}
