
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2013.main.command;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_CommentChangeBit.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_CommentChangeBit"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="add"/&gt;
 *     &lt;enumeration value="del"/&gt;
 *     &lt;enumeration value="mod"/&gt;
 *     &lt;enumeration value="replId"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_CommentChangeBit")
@XmlEnum
public enum STCommentChangeBit {

    @XmlEnumValue("add")
    ADD("add"),
    @XmlEnumValue("del")
    DEL("del"),
    @XmlEnumValue("mod")
    MOD("mod"),
    @XmlEnumValue("replId")
    REPL_ID("replId");
    private final String value;

    STCommentChangeBit(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STCommentChangeBit fromValue(String v) {
        for (STCommentChangeBit c: STCommentChangeBit.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
