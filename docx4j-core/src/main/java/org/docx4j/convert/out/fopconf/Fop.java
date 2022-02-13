
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
 *         &lt;element name="strict-validation" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="strict-configuration" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="accessibility" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="base" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="hyphenation-base" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element ref="{http://purl.org/dc/elements/1.1/}hyphenation-pattern" maxOccurs="unbounded"/&gt;
 *         &lt;element name="fonts" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://purl.org/dc/elements/1.1/}substitutions" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="renderers"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="renderer"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element ref="{http://purl.org/dc/elements/1.1/}fonts"/&gt;
 *                           &lt;/sequence&gt;
 *                           &lt;attribute name="mime" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
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
 *       &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "strictValidation",
    "strictConfiguration",
    "accessibility",
    "base",
    "hyphenationBase",
    "hyphenationPattern",
    "fonts",
    "renderers"
})
@XmlRootElement(name = "fop")
public class Fop {

    @XmlElement(name = "strict-validation")
    protected Boolean strictValidation;
    @XmlElement(name = "strict-configuration")
    protected Boolean strictConfiguration;
    protected Boolean accessibility;
    protected String base;
    @XmlElement(name = "hyphenation-base", required = true)
    protected String hyphenationBase;
    @XmlElement(name = "hyphenation-pattern", required = true)
    protected List<HyphenationPattern> hyphenationPattern;
    protected Fop.Fonts fonts;
    @XmlElement(required = true)
    protected Fop.Renderers renderers;
    @XmlAttribute(name = "version", required = true)
    protected String version;

    /**
     * Gets the value of the strictValidation property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isStrictValidation() {
        return strictValidation;
    }

    /**
     * Sets the value of the strictValidation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setStrictValidation(Boolean value) {
        this.strictValidation = value;
    }

    /**
     * Gets the value of the strictConfiguration property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isStrictConfiguration() {
        return strictConfiguration;
    }

    /**
     * Sets the value of the strictConfiguration property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setStrictConfiguration(Boolean value) {
        this.strictConfiguration = value;
    }

    /**
     * Gets the value of the accessibility property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAccessibility() {
        return accessibility;
    }

    /**
     * Sets the value of the accessibility property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAccessibility(Boolean value) {
        this.accessibility = value;
    }

    /**
     * Gets the value of the base property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBase() {
        return base;
    }

    /**
     * Sets the value of the base property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBase(String value) {
        this.base = value;
    }

    /**
     * Gets the value of the hyphenationBase property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHyphenationBase() {
        return hyphenationBase;
    }

    /**
     * Sets the value of the hyphenationBase property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHyphenationBase(String value) {
        this.hyphenationBase = value;
    }

    /**
     * Gets the value of the hyphenationPattern property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the hyphenationPattern property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHyphenationPattern().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HyphenationPattern }
     * 
     * 
     */
    public List<HyphenationPattern> getHyphenationPattern() {
        if (hyphenationPattern == null) {
            hyphenationPattern = new ArrayList<HyphenationPattern>();
        }
        return this.hyphenationPattern;
    }

    /**
     * Gets the value of the fonts property.
     * 
     * @return
     *     possible object is
     *     {@link Fop.Fonts }
     *     
     */
    public Fop.Fonts getFonts() {
        return fonts;
    }

    /**
     * Sets the value of the fonts property.
     * 
     * @param value
     *     allowed object is
     *     {@link Fop.Fonts }
     *     
     */
    public void setFonts(Fop.Fonts value) {
        this.fonts = value;
    }

    /**
     * Gets the value of the renderers property.
     * 
     * @return
     *     possible object is
     *     {@link Fop.Renderers }
     *     
     */
    public Fop.Renderers getRenderers() {
        return renderers;
    }

    /**
     * Sets the value of the renderers property.
     * 
     * @param value
     *     allowed object is
     *     {@link Fop.Renderers }
     *     
     */
    public void setRenderers(Fop.Renderers value) {
        this.renderers = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
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
     *         &lt;element ref="{http://purl.org/dc/elements/1.1/}substitutions" minOccurs="0"/&gt;
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
        "substitutions"
    })
    public static class Fonts {

        protected Substitutions substitutions;

        /**
         * Gets the value of the substitutions property.
         * 
         * @return
         *     possible object is
         *     {@link Substitutions }
         *     
         */
        public Substitutions getSubstitutions() {
            return substitutions;
        }

        /**
         * Sets the value of the substitutions property.
         * 
         * @param value
         *     allowed object is
         *     {@link Substitutions }
         *     
         */
        public void setSubstitutions(Substitutions value) {
            this.substitutions = value;
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
     *       &lt;sequence&gt;
     *         &lt;element name="renderer"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element ref="{http://purl.org/dc/elements/1.1/}fonts"/&gt;
     *                 &lt;/sequence&gt;
     *                 &lt;attribute name="mime" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
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
        "renderer"
    })
    public static class Renderers {

        @XmlElement(required = true)
        protected Fop.Renderers.Renderer renderer;

        /**
         * Gets the value of the renderer property.
         * 
         * @return
         *     possible object is
         *     {@link Fop.Renderers.Renderer }
         *     
         */
        public Fop.Renderers.Renderer getRenderer() {
            return renderer;
        }

        /**
         * Sets the value of the renderer property.
         * 
         * @param value
         *     allowed object is
         *     {@link Fop.Renderers.Renderer }
         *     
         */
        public void setRenderer(Fop.Renderers.Renderer value) {
            this.renderer = value;
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
         *         &lt;element ref="{http://purl.org/dc/elements/1.1/}fonts"/&gt;
         *       &lt;/sequence&gt;
         *       &lt;attribute name="mime" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "fonts"
        })
        public static class Renderer {

            @XmlElement(required = true)
            protected org.docx4j.convert.out.fopconf.Fonts fonts;
            @XmlAttribute(name = "mime", required = true)
            protected String mime;

            /**
             * Gets the value of the fonts property.
             * 
             * @return
             *     possible object is
             *     {@link org.docx4j.convert.out.fopconf.Fonts }
             *     
             */
            public org.docx4j.convert.out.fopconf.Fonts getFonts() {
                return fonts;
            }

            /**
             * Sets the value of the fonts property.
             * 
             * @param value
             *     allowed object is
             *     {@link org.docx4j.convert.out.fopconf.Fonts }
             *     
             */
            public void setFonts(org.docx4j.convert.out.fopconf.Fonts value) {
                this.fonts = value;
            }

            /**
             * Gets the value of the mime property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getMime() {
                return mime;
            }

            /**
             * Sets the value of the mime property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setMime(String value) {
                this.mime = value;
            }

        }

    }

}
