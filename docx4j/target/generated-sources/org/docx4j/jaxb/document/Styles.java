
package org.docx4j.jaxb.document;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


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
 *         &lt;element name="rPrDefault" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}rPr" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="pPrDefault" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}pPr" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="latentStyles" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="lsdException" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="name" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
 *                           &lt;attribute name="locked" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
 *                           &lt;attribute name="uiPriority" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *                           &lt;attribute name="semiHidden" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
 *                           &lt;attribute name="unhideWhenUsed" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
 *                           &lt;attribute name="qFormat" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="defLockedState" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
 *                 &lt;attribute name="defUIPriority" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *                 &lt;attribute name="defSemiHidden" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
 *                 &lt;attribute name="defUnhideWhenUsed" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
 *                 &lt;attribute name="defQFormat" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
 *                 &lt;attribute name="count" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="style" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="name" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attGroup ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_String"/>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="aliases" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attGroup ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_String"/>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="basedOn" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attGroup ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_String"/>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="next" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attGroup ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_String"/>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="link" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attGroup ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_String"/>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="qFormat" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *                   &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}pPr" minOccurs="0"/>
 *                   &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}rPr" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="type">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                       &lt;enumeration value="paragraph"/>
 *                       &lt;enumeration value="character"/>
 *                       &lt;enumeration value="table"/>
 *                       &lt;enumeration value="numbering"/>
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *                 &lt;attribute name="styleId" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
 *                 &lt;attribute name="default" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
 *                 &lt;attribute name="customStyle" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
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
@XmlType(name = "", propOrder = {
    "rPrDefault",
    "pPrDefault",
    "latentStyles",
    "style"
})
@XmlRootElement(name = "styles")
public class Styles
    implements Child
{

    protected Styles.RPrDefault rPrDefault;
    protected Styles.PPrDefault pPrDefault;
    protected Styles.LatentStyles latentStyles;
    protected List<Styles.Style> style;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the rPrDefault property.
     * 
     * @return
     *     possible object is
     *     {@link Styles.RPrDefault }
     *     
     */
    public Styles.RPrDefault getRPrDefault() {
        return rPrDefault;
    }

    /**
     * Sets the value of the rPrDefault property.
     * 
     * @param value
     *     allowed object is
     *     {@link Styles.RPrDefault }
     *     
     */
    public void setRPrDefault(Styles.RPrDefault value) {
        this.rPrDefault = value;
    }

    /**
     * Gets the value of the pPrDefault property.
     * 
     * @return
     *     possible object is
     *     {@link Styles.PPrDefault }
     *     
     */
    public Styles.PPrDefault getPPrDefault() {
        return pPrDefault;
    }

    /**
     * Sets the value of the pPrDefault property.
     * 
     * @param value
     *     allowed object is
     *     {@link Styles.PPrDefault }
     *     
     */
    public void setPPrDefault(Styles.PPrDefault value) {
        this.pPrDefault = value;
    }

    /**
     * Gets the value of the latentStyles property.
     * 
     * @return
     *     possible object is
     *     {@link Styles.LatentStyles }
     *     
     */
    public Styles.LatentStyles getLatentStyles() {
        return latentStyles;
    }

    /**
     * Sets the value of the latentStyles property.
     * 
     * @param value
     *     allowed object is
     *     {@link Styles.LatentStyles }
     *     
     */
    public void setLatentStyles(Styles.LatentStyles value) {
        this.latentStyles = value;
    }

    /**
     * Gets the value of the style property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the style property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStyle().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Styles.Style }
     * 
     * 
     */
    public List<Styles.Style> getStyle() {
        if (style == null) {
            style = new ArrayList<Styles.Style>();
        }
        return this.style;
    }

    /**
     * Gets the parent object in the object tree representing the unmarshalled xml document.
     * 
     * @return
     *     The parent object.
     */
    public Object getParent() {
        return this.parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    /**
     * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
     * 
     * @param parent
     *     The parent object in the object tree.
     * @param unmarshaller
     *     The unmarshaller that generated the instance.
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        setParent(parent);
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
     *         &lt;element name="lsdException" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="name" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
     *                 &lt;attribute name="locked" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
     *                 &lt;attribute name="uiPriority" type="{http://www.w3.org/2001/XMLSchema}integer" />
     *                 &lt;attribute name="semiHidden" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
     *                 &lt;attribute name="unhideWhenUsed" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
     *                 &lt;attribute name="qFormat" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="defLockedState" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
     *       &lt;attribute name="defUIPriority" type="{http://www.w3.org/2001/XMLSchema}integer" />
     *       &lt;attribute name="defSemiHidden" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
     *       &lt;attribute name="defUnhideWhenUsed" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
     *       &lt;attribute name="defQFormat" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
     *       &lt;attribute name="count" type="{http://www.w3.org/2001/XMLSchema}integer" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "lsdException"
    })
    public static class LatentStyles
        implements Child
    {

        protected List<Styles.LatentStyles.LsdException> lsdException;
        @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected String defLockedState;
        @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected BigInteger defUIPriority;
        @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected String defSemiHidden;
        @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected String defUnhideWhenUsed;
        @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected String defQFormat;
        @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected BigInteger count;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the lsdException property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the lsdException property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getLsdException().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Styles.LatentStyles.LsdException }
         * 
         * 
         */
        public List<Styles.LatentStyles.LsdException> getLsdException() {
            if (lsdException == null) {
                lsdException = new ArrayList<Styles.LatentStyles.LsdException>();
            }
            return this.lsdException;
        }

        /**
         * Gets the value of the defLockedState property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDefLockedState() {
            return defLockedState;
        }

        /**
         * Sets the value of the defLockedState property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDefLockedState(String value) {
            this.defLockedState = value;
        }

        /**
         * Gets the value of the defUIPriority property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getDefUIPriority() {
            return defUIPriority;
        }

        /**
         * Sets the value of the defUIPriority property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setDefUIPriority(BigInteger value) {
            this.defUIPriority = value;
        }

        /**
         * Gets the value of the defSemiHidden property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDefSemiHidden() {
            return defSemiHidden;
        }

        /**
         * Sets the value of the defSemiHidden property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDefSemiHidden(String value) {
            this.defSemiHidden = value;
        }

        /**
         * Gets the value of the defUnhideWhenUsed property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDefUnhideWhenUsed() {
            return defUnhideWhenUsed;
        }

        /**
         * Sets the value of the defUnhideWhenUsed property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDefUnhideWhenUsed(String value) {
            this.defUnhideWhenUsed = value;
        }

        /**
         * Gets the value of the defQFormat property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDefQFormat() {
            return defQFormat;
        }

        /**
         * Sets the value of the defQFormat property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDefQFormat(String value) {
            this.defQFormat = value;
        }

        /**
         * Gets the value of the count property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getCount() {
            return count;
        }

        /**
         * Sets the value of the count property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setCount(BigInteger value) {
            this.count = value;
        }

        /**
         * Gets the parent object in the object tree representing the unmarshalled xml document.
         * 
         * @return
         *     The parent object.
         */
        public Object getParent() {
            return this.parent;
        }

        public void setParent(Object parent) {
            this.parent = parent;
        }

        /**
         * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
         * 
         * @param parent
         *     The parent object in the object tree.
         * @param unmarshaller
         *     The unmarshaller that generated the instance.
         */
        public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
            setParent(parent);
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
         *       &lt;attribute name="name" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
         *       &lt;attribute name="locked" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
         *       &lt;attribute name="uiPriority" type="{http://www.w3.org/2001/XMLSchema}integer" />
         *       &lt;attribute name="semiHidden" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
         *       &lt;attribute name="unhideWhenUsed" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
         *       &lt;attribute name="qFormat" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class LsdException
            implements Child
        {

            @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
            protected String name;
            @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
            protected String locked;
            @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
            protected BigInteger uiPriority;
            @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
            protected String semiHidden;
            @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
            protected String unhideWhenUsed;
            @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
            protected String qFormat;
            @XmlTransient
            private Object parent;

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
             * Gets the value of the locked property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getLocked() {
                return locked;
            }

            /**
             * Sets the value of the locked property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setLocked(String value) {
                this.locked = value;
            }

            /**
             * Gets the value of the uiPriority property.
             * 
             * @return
             *     possible object is
             *     {@link BigInteger }
             *     
             */
            public BigInteger getUiPriority() {
                return uiPriority;
            }

            /**
             * Sets the value of the uiPriority property.
             * 
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *     
             */
            public void setUiPriority(BigInteger value) {
                this.uiPriority = value;
            }

            /**
             * Gets the value of the semiHidden property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getSemiHidden() {
                return semiHidden;
            }

            /**
             * Sets the value of the semiHidden property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setSemiHidden(String value) {
                this.semiHidden = value;
            }

            /**
             * Gets the value of the unhideWhenUsed property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getUnhideWhenUsed() {
                return unhideWhenUsed;
            }

            /**
             * Sets the value of the unhideWhenUsed property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setUnhideWhenUsed(String value) {
                this.unhideWhenUsed = value;
            }

            /**
             * Gets the value of the qFormat property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getQFormat() {
                return qFormat;
            }

            /**
             * Sets the value of the qFormat property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setQFormat(String value) {
                this.qFormat = value;
            }

            /**
             * Gets the parent object in the object tree representing the unmarshalled xml document.
             * 
             * @return
             *     The parent object.
             */
            public Object getParent() {
                return this.parent;
            }

            public void setParent(Object parent) {
                this.parent = parent;
            }

            /**
             * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
             * 
             * @param parent
             *     The parent object in the object tree.
             * @param unmarshaller
             *     The unmarshaller that generated the instance.
             */
            public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
                setParent(parent);
            }

        }

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
     *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}pPr" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "pPr"
    })
    public static class PPrDefault
        implements Child
    {

        protected PPr pPr;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the pPr property.
         * 
         * @return
         *     possible object is
         *     {@link PPr }
         *     
         */
        public PPr getPPr() {
            return pPr;
        }

        /**
         * Sets the value of the pPr property.
         * 
         * @param value
         *     allowed object is
         *     {@link PPr }
         *     
         */
        public void setPPr(PPr value) {
            this.pPr = value;
        }

        /**
         * Gets the parent object in the object tree representing the unmarshalled xml document.
         * 
         * @return
         *     The parent object.
         */
        public Object getParent() {
            return this.parent;
        }

        public void setParent(Object parent) {
            this.parent = parent;
        }

        /**
         * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
         * 
         * @param parent
         *     The parent object in the object tree.
         * @param unmarshaller
         *     The unmarshaller that generated the instance.
         */
        public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
            setParent(parent);
        }

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
     *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}rPr" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "rPr"
    })
    public static class RPrDefault
        implements Child
    {

        protected RPr rPr;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the rPr property.
         * 
         * @return
         *     possible object is
         *     {@link RPr }
         *     
         */
        public RPr getRPr() {
            return rPr;
        }

        /**
         * Sets the value of the rPr property.
         * 
         * @param value
         *     allowed object is
         *     {@link RPr }
         *     
         */
        public void setRPr(RPr value) {
            this.rPr = value;
        }

        /**
         * Gets the parent object in the object tree representing the unmarshalled xml document.
         * 
         * @return
         *     The parent object.
         */
        public Object getParent() {
            return this.parent;
        }

        public void setParent(Object parent) {
            this.parent = parent;
        }

        /**
         * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
         * 
         * @param parent
         *     The parent object in the object tree.
         * @param unmarshaller
         *     The unmarshaller that generated the instance.
         */
        public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
            setParent(parent);
        }

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
     *         &lt;element name="name" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attGroup ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_String"/>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="aliases" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attGroup ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_String"/>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="basedOn" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attGroup ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_String"/>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="next" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attGroup ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_String"/>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="link" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attGroup ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_String"/>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="qFormat" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
     *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}pPr" minOccurs="0"/>
     *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}rPr" minOccurs="0"/>
     *       &lt;/sequence>
     *       &lt;attribute name="type">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *             &lt;enumeration value="paragraph"/>
     *             &lt;enumeration value="character"/>
     *             &lt;enumeration value="table"/>
     *             &lt;enumeration value="numbering"/>
     *           &lt;/restriction>
     *         &lt;/simpleType>
     *       &lt;/attribute>
     *       &lt;attribute name="styleId" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
     *       &lt;attribute name="default" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
     *       &lt;attribute name="customStyle" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "name",
        "aliases",
        "basedOn",
        "next",
        "link",
        "qFormat",
        "pPr",
        "rPr"
    })
    public static class Style
        implements Child
    {

        protected Styles.Style.Name name;
        protected Styles.Style.Aliases aliases;
        protected Styles.Style.BasedOn basedOn;
        protected Styles.Style.Next next;
        protected Styles.Style.Link link;
        protected BooleanDefaultTrue qFormat;
        protected PPr pPr;
        protected RPr rPr;
        @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected String type;
        @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected String styleId;
        @XmlAttribute(name = "default", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected String _default;
        @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected String customStyle;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the name property.
         * 
         * @return
         *     possible object is
         *     {@link Styles.Style.Name }
         *     
         */
        public Styles.Style.Name getName() {
            return name;
        }

        /**
         * Sets the value of the name property.
         * 
         * @param value
         *     allowed object is
         *     {@link Styles.Style.Name }
         *     
         */
        public void setName(Styles.Style.Name value) {
            this.name = value;
        }

        /**
         * Gets the value of the aliases property.
         * 
         * @return
         *     possible object is
         *     {@link Styles.Style.Aliases }
         *     
         */
        public Styles.Style.Aliases getAliases() {
            return aliases;
        }

        /**
         * Sets the value of the aliases property.
         * 
         * @param value
         *     allowed object is
         *     {@link Styles.Style.Aliases }
         *     
         */
        public void setAliases(Styles.Style.Aliases value) {
            this.aliases = value;
        }

        /**
         * Gets the value of the basedOn property.
         * 
         * @return
         *     possible object is
         *     {@link Styles.Style.BasedOn }
         *     
         */
        public Styles.Style.BasedOn getBasedOn() {
            return basedOn;
        }

        /**
         * Sets the value of the basedOn property.
         * 
         * @param value
         *     allowed object is
         *     {@link Styles.Style.BasedOn }
         *     
         */
        public void setBasedOn(Styles.Style.BasedOn value) {
            this.basedOn = value;
        }

        /**
         * Gets the value of the next property.
         * 
         * @return
         *     possible object is
         *     {@link Styles.Style.Next }
         *     
         */
        public Styles.Style.Next getNext() {
            return next;
        }

        /**
         * Sets the value of the next property.
         * 
         * @param value
         *     allowed object is
         *     {@link Styles.Style.Next }
         *     
         */
        public void setNext(Styles.Style.Next value) {
            this.next = value;
        }

        /**
         * Gets the value of the link property.
         * 
         * @return
         *     possible object is
         *     {@link Styles.Style.Link }
         *     
         */
        public Styles.Style.Link getLink() {
            return link;
        }

        /**
         * Sets the value of the link property.
         * 
         * @param value
         *     allowed object is
         *     {@link Styles.Style.Link }
         *     
         */
        public void setLink(Styles.Style.Link value) {
            this.link = value;
        }

        /**
         * Gets the value of the qFormat property.
         * 
         * @return
         *     possible object is
         *     {@link BooleanDefaultTrue }
         *     
         */
        public BooleanDefaultTrue getQFormat() {
            return qFormat;
        }

        /**
         * Sets the value of the qFormat property.
         * 
         * @param value
         *     allowed object is
         *     {@link BooleanDefaultTrue }
         *     
         */
        public void setQFormat(BooleanDefaultTrue value) {
            this.qFormat = value;
        }

        /**
         * Style Paragraph
         * 										Properties
         * 
         * @return
         *     possible object is
         *     {@link PPr }
         *     
         */
        public PPr getPPr() {
            return pPr;
        }

        /**
         * Sets the value of the pPr property.
         * 
         * @param value
         *     allowed object is
         *     {@link PPr }
         *     
         */
        public void setPPr(PPr value) {
            this.pPr = value;
        }

        /**
         * Run
         * 										Properties
         * 
         * @return
         *     possible object is
         *     {@link RPr }
         *     
         */
        public RPr getRPr() {
            return rPr;
        }

        /**
         * Sets the value of the rPr property.
         * 
         * @param value
         *     allowed object is
         *     {@link RPr }
         *     
         */
        public void setRPr(RPr value) {
            this.rPr = value;
        }

        /**
         * Gets the value of the type property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the value of the type property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setType(String value) {
            this.type = value;
        }

        /**
         * Gets the value of the styleId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getStyleId() {
            return styleId;
        }

        /**
         * Sets the value of the styleId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setStyleId(String value) {
            this.styleId = value;
        }

        /**
         * Gets the value of the default property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDefault() {
            return _default;
        }

        /**
         * Sets the value of the default property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDefault(String value) {
            this._default = value;
        }

        /**
         * Gets the value of the customStyle property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCustomStyle() {
            return customStyle;
        }

        /**
         * Sets the value of the customStyle property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCustomStyle(String value) {
            this.customStyle = value;
        }

        /**
         * Gets the parent object in the object tree representing the unmarshalled xml document.
         * 
         * @return
         *     The parent object.
         */
        public Object getParent() {
            return this.parent;
        }

        public void setParent(Object parent) {
            this.parent = parent;
        }

        /**
         * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
         * 
         * @param parent
         *     The parent object in the object tree.
         * @param unmarshaller
         *     The unmarshaller that generated the instance.
         */
        public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
            setParent(parent);
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
         *       &lt;attGroup ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_String"/>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Aliases
            implements Child
        {

            @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
            protected String val;
            @XmlTransient
            private Object parent;

            /**
             * Gets the value of the val property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getVal() {
                return val;
            }

            /**
             * Sets the value of the val property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setVal(String value) {
                this.val = value;
            }

            /**
             * Gets the parent object in the object tree representing the unmarshalled xml document.
             * 
             * @return
             *     The parent object.
             */
            public Object getParent() {
                return this.parent;
            }

            public void setParent(Object parent) {
                this.parent = parent;
            }

            /**
             * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
             * 
             * @param parent
             *     The parent object in the object tree.
             * @param unmarshaller
             *     The unmarshaller that generated the instance.
             */
            public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
                setParent(parent);
            }

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
         *       &lt;attGroup ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_String"/>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class BasedOn
            implements Child
        {

            @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
            protected String val;
            @XmlTransient
            private Object parent;

            /**
             * Gets the value of the val property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getVal() {
                return val;
            }

            /**
             * Sets the value of the val property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setVal(String value) {
                this.val = value;
            }

            /**
             * Gets the parent object in the object tree representing the unmarshalled xml document.
             * 
             * @return
             *     The parent object.
             */
            public Object getParent() {
                return this.parent;
            }

            public void setParent(Object parent) {
                this.parent = parent;
            }

            /**
             * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
             * 
             * @param parent
             *     The parent object in the object tree.
             * @param unmarshaller
             *     The unmarshaller that generated the instance.
             */
            public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
                setParent(parent);
            }

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
         *       &lt;attGroup ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_String"/>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Link
            implements Child
        {

            @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
            protected String val;
            @XmlTransient
            private Object parent;

            /**
             * Gets the value of the val property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getVal() {
                return val;
            }

            /**
             * Sets the value of the val property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setVal(String value) {
                this.val = value;
            }

            /**
             * Gets the parent object in the object tree representing the unmarshalled xml document.
             * 
             * @return
             *     The parent object.
             */
            public Object getParent() {
                return this.parent;
            }

            public void setParent(Object parent) {
                this.parent = parent;
            }

            /**
             * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
             * 
             * @param parent
             *     The parent object in the object tree.
             * @param unmarshaller
             *     The unmarshaller that generated the instance.
             */
            public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
                setParent(parent);
            }

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
         *       &lt;attGroup ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_String"/>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Name
            implements Child
        {

            @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
            protected String val;
            @XmlTransient
            private Object parent;

            /**
             * Gets the value of the val property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getVal() {
                return val;
            }

            /**
             * Sets the value of the val property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setVal(String value) {
                this.val = value;
            }

            /**
             * Gets the parent object in the object tree representing the unmarshalled xml document.
             * 
             * @return
             *     The parent object.
             */
            public Object getParent() {
                return this.parent;
            }

            public void setParent(Object parent) {
                this.parent = parent;
            }

            /**
             * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
             * 
             * @param parent
             *     The parent object in the object tree.
             * @param unmarshaller
             *     The unmarshaller that generated the instance.
             */
            public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
                setParent(parent);
            }

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
         *       &lt;attGroup ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_String"/>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Next
            implements Child
        {

            @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
            protected String val;
            @XmlTransient
            private Object parent;

            /**
             * Gets the value of the val property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getVal() {
                return val;
            }

            /**
             * Sets the value of the val property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setVal(String value) {
                this.val = value;
            }

            /**
             * Gets the parent object in the object tree representing the unmarshalled xml document.
             * 
             * @return
             *     The parent object.
             */
            public Object getParent() {
                return this.parent;
            }

            public void setParent(Object parent) {
                this.parent = parent;
            }

            /**
             * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
             * 
             * @param parent
             *     The parent object in the object tree.
             * @param unmarshaller
             *     The unmarshaller that generated the instance.
             */
            public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
                setParent(parent);
            }

        }

    }

}
