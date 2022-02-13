
package org.docx4j.convert.out.fopconf;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="font" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="font-triplet" maxOccurs="unbounded"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                           &lt;attribute name="style" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                           &lt;attribute name="weight" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name="simulate-style" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *                 &lt;attribute name="embed-url" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="sub-font" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "font"
})
@XmlRootElement(name = "fonts")
public class Fonts {

    protected List<Fonts.Font> font;

    /**
     * Gets the value of the font property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the font property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFont().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Fonts.Font }
     * 
     * 
     */
    public List<Fonts.Font> getFont() {
        if (font == null) {
            font = new ArrayList<Fonts.Font>();
        }
        return this.font;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="font-triplet" maxOccurs="unbounded"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                 &lt;attribute name="style" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                 &lt;attribute name="weight" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name="simulate-style" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
     *       &lt;attribute name="embed-url" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;attribute name="sub-font" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "fontTriplet"
    })
    public static class Font {

        @XmlElement(name = "font-triplet", required = true)
        protected List<Fonts.Font.FontTriplet> fontTriplet;
        @XmlAttribute(name = "simulate-style", required = true)
        protected boolean simulateStyle;
        @XmlAttribute(name = "embed-url", required = true)
        protected String embedUrl;
        @XmlAttribute(name = "sub-font")
        protected String subFont;

        /**
         * Gets the value of the fontTriplet property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the fontTriplet property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getFontTriplet().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Fonts.Font.FontTriplet }
         * 
         * 
         */
        public List<Fonts.Font.FontTriplet> getFontTriplet() {
            if (fontTriplet == null) {
                fontTriplet = new ArrayList<Fonts.Font.FontTriplet>();
            }
            return this.fontTriplet;
        }

        /**
         * Gets the value of the simulateStyle property.
         * 
         */
        public boolean isSimulateStyle() {
            return simulateStyle;
        }

        /**
         * Sets the value of the simulateStyle property.
         * 
         */
        public void setSimulateStyle(boolean value) {
            this.simulateStyle = value;
        }

        /**
         * Gets the value of the embedUrl property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getEmbedUrl() {
            return embedUrl;
        }

        /**
         * Sets the value of the embedUrl property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setEmbedUrl(String value) {
            this.embedUrl = value;
        }

        /**
         * Gets the value of the subFont property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSubFont() {
            return subFont;
        }

        /**
         * Sets the value of the subFont property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSubFont(String value) {
            this.subFont = value;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *       &lt;attribute name="style" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *       &lt;attribute name="weight" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class FontTriplet {

            @XmlAttribute(name = "name", required = true)
            protected String name;
            @XmlAttribute(name = "style", required = true)
            protected String style;
            @XmlAttribute(name = "weight", required = true)
            protected String weight;

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

            /**
             * Gets the value of the style property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getStyle() {
                return style;
            }

            /**
             * Sets the value of the style property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setStyle(String value) {
                this.style = value;
            }

            /**
             * Gets the value of the weight property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getWeight() {
                return weight;
            }

            /**
             * Sets the value of the weight property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setWeight(String value) {
                this.weight = value;
            }

        }

    }

}
