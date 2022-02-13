
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
 *         &lt;element name="substitution" maxOccurs="unbounded"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="from"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;attribute name="font-family" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                           &lt;attribute name="font-weight" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                           &lt;attribute name="font-style" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="to"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;attribute name="font-family" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                           &lt;attribute name="font-weight" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                           &lt;attribute name="font-style" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
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
    "substitution"
})
@XmlRootElement(name = "substitutions")
public class Substitutions {

    @XmlElement(required = true)
    protected List<Substitutions.Substitution> substitution;

    /**
     * Gets the value of the substitution property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the substitution property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSubstitution().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Substitutions.Substitution }
     * 
     * 
     */
    public List<Substitutions.Substitution> getSubstitution() {
        if (substitution == null) {
            substitution = new ArrayList<Substitutions.Substitution>();
        }
        return this.substitution;
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
     *         &lt;element name="from"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;attribute name="font-family" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                 &lt;attribute name="font-weight" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                 &lt;attribute name="font-style" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="to"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;attribute name="font-family" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                 &lt;attribute name="font-weight" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                 &lt;attribute name="font-style" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
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
        "from",
        "to"
    })
    public static class Substitution {

        @XmlElement(required = true)
        protected Substitutions.Substitution.From from;
        @XmlElement(required = true)
        protected Substitutions.Substitution.To to;

        /**
         * Gets the value of the from property.
         * 
         * @return
         *     possible object is
         *     {@link Substitutions.Substitution.From }
         *     
         */
        public Substitutions.Substitution.From getFrom() {
            return from;
        }

        /**
         * Sets the value of the from property.
         * 
         * @param value
         *     allowed object is
         *     {@link Substitutions.Substitution.From }
         *     
         */
        public void setFrom(Substitutions.Substitution.From value) {
            this.from = value;
        }

        /**
         * Gets the value of the to property.
         * 
         * @return
         *     possible object is
         *     {@link Substitutions.Substitution.To }
         *     
         */
        public Substitutions.Substitution.To getTo() {
            return to;
        }

        /**
         * Sets the value of the to property.
         * 
         * @param value
         *     allowed object is
         *     {@link Substitutions.Substitution.To }
         *     
         */
        public void setTo(Substitutions.Substitution.To value) {
            this.to = value;
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
         *       &lt;attribute name="font-family" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *       &lt;attribute name="font-weight" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *       &lt;attribute name="font-style" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class From {

            @XmlAttribute(name = "font-family", required = true)
            protected String fontFamily;
            @XmlAttribute(name = "font-weight")
            protected String fontWeight;
            @XmlAttribute(name = "font-style")
            protected String fontStyle;

            /**
             * Gets the value of the fontFamily property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getFontFamily() {
                return fontFamily;
            }

            /**
             * Sets the value of the fontFamily property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setFontFamily(String value) {
                this.fontFamily = value;
            }

            /**
             * Gets the value of the fontWeight property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getFontWeight() {
                return fontWeight;
            }

            /**
             * Sets the value of the fontWeight property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setFontWeight(String value) {
                this.fontWeight = value;
            }

            /**
             * Gets the value of the fontStyle property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getFontStyle() {
                return fontStyle;
            }

            /**
             * Sets the value of the fontStyle property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setFontStyle(String value) {
                this.fontStyle = value;
            }

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
         *       &lt;attribute name="font-family" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *       &lt;attribute name="font-weight" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *       &lt;attribute name="font-style" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class To {

            @XmlAttribute(name = "font-family", required = true)
            protected String fontFamily;
            @XmlAttribute(name = "font-weight")
            protected String fontWeight;
            @XmlAttribute(name = "font-style")
            protected String fontStyle;

            /**
             * Gets the value of the fontFamily property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getFontFamily() {
                return fontFamily;
            }

            /**
             * Sets the value of the fontFamily property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setFontFamily(String value) {
                this.fontFamily = value;
            }

            /**
             * Gets the value of the fontWeight property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getFontWeight() {
                return fontWeight;
            }

            /**
             * Sets the value of the fontWeight property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setFontWeight(String value) {
                this.fontWeight = value;
            }

            /**
             * Gets the value of the fontStyle property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getFontStyle() {
                return fontStyle;
            }

            /**
             * Sets the value of the fontStyle property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setFontStyle(String value) {
                this.fontStyle = value;
            }

        }

    }

}
