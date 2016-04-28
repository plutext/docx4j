
package org.docx4j.com.microsoft.schemas.office.x2006.encryption;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_HashAlgorithm.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_HashAlgorithm">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="SHA1"/>
 *     &lt;enumeration value="SHA256"/>
 *     &lt;enumeration value="SHA384"/>
 *     &lt;enumeration value="SHA512"/>
 *     &lt;enumeration value="MD5"/>
 *     &lt;enumeration value="MD4"/>
 *     &lt;enumeration value="MD2"/>
 *     &lt;enumeration value="RIPEMD-128"/>
 *     &lt;enumeration value="RIPEMD-160"/>
 *     &lt;enumeration value="WHIRLPOOL"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_HashAlgorithm")
@XmlEnum
public enum STHashAlgorithm {


    /**
     * MUST conform to the algorithm as specified in [RFC4634] (http://tools.ietf.org/html/rfc4634).
     * 
     */
    @XmlEnumValue("SHA1")
    SHA_1("SHA1"),

    /**
     * see SHA1
     * 
     */
    @XmlEnumValue("SHA256")
    SHA_256("SHA256"),

    /**
     * see SHA1
     * 
     */
    @XmlEnumValue("SHA384")
    SHA_384("SHA384"),

    /**
     * see SHA1
     * 
     */
    @XmlEnumValue("SHA512")
    SHA_512("SHA512"),

    /**
     * MUST conform to MD5.
     * 
     */
    @XmlEnumValue("MD5")
    MD_5("MD5"),

    /**
     * MUST conform to the algorithm as specified in [RFC1320] (http://tools.ietf.org/html/rfc1320).
     * 
     */
    @XmlEnumValue("MD4")
    MD_4("MD4"),

    /**
     * MUST conform to the algorithm as specified in [RFC1319] (http://tools.ietf.org/html/rfc1319).
     * 
     */
    @XmlEnumValue("MD2")
    MD_2("MD2"),

    /**
     * MUST conform to the hash functions specified in [ISO/IEC 10118]. (https://en.wikipedia.org/wiki/RIPEMD)
     * 
     */
    @XmlEnumValue("RIPEMD-128")
    RIPEMD_128("RIPEMD-128"),

    /**
     * see RIPEMD-128 (https://en.wikipedia.org/wiki/RIPEMD)
     * 
     */
    @XmlEnumValue("RIPEMD-160")
    RIPEMD_160("RIPEMD-160"),

    /**
     * see RIPEMD-128 (https://en.wikipedia.org/wiki/ISO/IEC_10118-3)
     * 
     */
    WHIRLPOOL("WHIRLPOOL");
    private final String value;

    STHashAlgorithm(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STHashAlgorithm fromValue(String v) {
        for (STHashAlgorithm c: STHashAlgorithm.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
