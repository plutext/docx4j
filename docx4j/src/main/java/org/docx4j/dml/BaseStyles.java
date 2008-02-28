
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_BaseStyles complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_BaseStyles">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fontScheme">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="majorFont" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_FontCollection"/>
 *                   &lt;element name="minorFont" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_FontCollection"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_BaseStyles", propOrder = {
    "fontScheme"
})
public class BaseStyles {

    @XmlElement(required = true)
    protected BaseStyles.FontScheme fontScheme;

    /**
     * Gets the value of the fontScheme property.
     * 
     * @return
     *     possible object is
     *     {@link BaseStyles.FontScheme }
     *     
     */
    public BaseStyles.FontScheme getFontScheme() {
        return fontScheme;
    }

    /**
     * Sets the value of the fontScheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link BaseStyles.FontScheme }
     *     
     */
    public void setFontScheme(BaseStyles.FontScheme value) {
        this.fontScheme = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="majorFont" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_FontCollection"/>
     *         &lt;element name="minorFont" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_FontCollection"/>
     *       &lt;/sequence>
     *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "majorFont",
        "minorFont"
    })
    public static class FontScheme {

        @XmlElement(required = true)
        protected FontCollection majorFont;
        @XmlElement(required = true)
        protected FontCollection minorFont;
        @XmlAttribute(required = true)
        protected String name;

        /**
         * Gets the value of the majorFont property.
         * 
         * @return
         *     possible object is
         *     {@link FontCollection }
         *     
         */
        public FontCollection getMajorFont() {
            return majorFont;
        }

        /**
         * Sets the value of the majorFont property.
         * 
         * @param value
         *     allowed object is
         *     {@link FontCollection }
         *     
         */
        public void setMajorFont(FontCollection value) {
            this.majorFont = value;
        }

        /**
         * Gets the value of the minorFont property.
         * 
         * @return
         *     possible object is
         *     {@link FontCollection }
         *     
         */
        public FontCollection getMinorFont() {
            return minorFont;
        }

        /**
         * Sets the value of the minorFont property.
         * 
         * @param value
         *     allowed object is
         *     {@link FontCollection }
         *     
         */
        public void setMinorFont(FontCollection value) {
            this.minorFont = value;
        }

        /**
         * Gets the value of the name property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the value of the name property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setName(String value) {
            this.name = value;
        }

    }

}
