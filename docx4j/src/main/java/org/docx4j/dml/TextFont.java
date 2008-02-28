
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for CT_TextFont complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TextFont">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="typeface">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="panose">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}hexBinary">
 *             &lt;length value="10"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="pitchFamily" type="{http://www.w3.org/2001/XMLSchema}byte" default="0" />
 *       &lt;attribute name="charset" type="{http://www.w3.org/2001/XMLSchema}byte" default="1" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TextFont")
public class TextFont {

    @XmlAttribute
    protected String typeface;
    @XmlAttribute
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] panose;
    @XmlAttribute
    protected Byte pitchFamily;
    @XmlAttribute
    protected Byte charset;

    /**
     * Gets the value of the typeface property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTypeface() {
        return typeface;
    }

    /**
     * Sets the value of the typeface property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTypeface(String value) {
        this.typeface = value;
    }

    /**
     * Gets the value of the panose property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getPanose() {
        return panose;
    }

    /**
     * Sets the value of the panose property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPanose(byte[] value) {
        this.panose = ((byte[]) value);
    }

    /**
     * Gets the value of the pitchFamily property.
     * 
     * @return
     *     possible object is
     *     {@link Byte }
     *     
     */
    public byte getPitchFamily() {
        if (pitchFamily == null) {
            return ((byte) 0);
        } else {
            return pitchFamily;
        }
    }

    /**
     * Sets the value of the pitchFamily property.
     * 
     * @param value
     *     allowed object is
     *     {@link Byte }
     *     
     */
    public void setPitchFamily(Byte value) {
        this.pitchFamily = value;
    }

    /**
     * Gets the value of the charset property.
     * 
     * @return
     *     possible object is
     *     {@link Byte }
     *     
     */
    public byte getCharset() {
        if (charset == null) {
            return ((byte) 1);
        } else {
            return charset;
        }
    }

    /**
     * Sets the value of the charset property.
     * 
     * @param value
     *     allowed object is
     *     {@link Byte }
     *     
     */
    public void setCharset(Byte value) {
        this.charset = value;
    }

}
