
package org.docx4j.mce;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
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
 *         &lt;element name="Choice" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;any maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="Requires" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute ref="{http://schemas.openxmlformats.org/markup-compatibility/2006}Ignorable"/>
 *                 &lt;attribute ref="{http://schemas.openxmlformats.org/markup-compatibility/2006}MustUnderstand"/>
 *                 &lt;attribute ref="{http://schemas.openxmlformats.org/markup-compatibility/2006}ProcessContent"/>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Fallback" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;any maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute ref="{http://schemas.openxmlformats.org/markup-compatibility/2006}Ignorable"/>
 *                 &lt;attribute ref="{http://schemas.openxmlformats.org/markup-compatibility/2006}MustUnderstand"/>
 *                 &lt;attribute ref="{http://schemas.openxmlformats.org/markup-compatibility/2006}ProcessContent"/>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/markup-compatibility/2006}Ignorable"/>
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/markup-compatibility/2006}MustUnderstand"/>
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/markup-compatibility/2006}ProcessContent"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "choice",
    "fallback"
})
@XmlRootElement(name = "AlternateContent")
public class AlternateContent implements Child {

    @XmlElement(name = "Choice")
    protected List<AlternateContent.Choice> choice = new ArrayListMce<AlternateContent.Choice>(this);
    @XmlElement(name = "Fallback")
    protected AlternateContent.Fallback fallback;
    @XmlAttribute(name = "Ignorable", namespace = "http://schemas.openxmlformats.org/markup-compatibility/2006")
    protected String ignorable;
    @XmlAttribute(name = "MustUnderstand", namespace = "http://schemas.openxmlformats.org/markup-compatibility/2006")
    protected String mustUnderstand;
    @XmlAttribute(name = "ProcessContent", namespace = "http://schemas.openxmlformats.org/markup-compatibility/2006")
    protected String processContent;

    /**
     * Gets the value of the choice property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the choice property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChoice().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AlternateContent.Choice }
     * 
     * 
     */
    public List<AlternateContent.Choice> getChoice() {
        if (choice == null) {
            choice = new ArrayListMce<AlternateContent.Choice>(this);
        }
        return this.choice;
    }

    /**
     * Gets the value of the fallback property.
     * 
     * @return
     *     possible object is
     *     {@link AlternateContent.Fallback }
     *     
     */
    public AlternateContent.Fallback getFallback() {
        return fallback;
    }

    /**
     * Sets the value of the fallback property.
     * 
     * @param value
     *     allowed object is
     *     {@link AlternateContent.Fallback }
     *     
     */
    public void setFallback(AlternateContent.Fallback value) {
        this.fallback = value;
    }

    /**
     * Gets the value of the ignorable property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIgnorable() {
        return ignorable;
    }

    /**
     * Sets the value of the ignorable property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIgnorable(String value) {
        this.ignorable = value;
    }

    /**
     * Gets the value of the mustUnderstand property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMustUnderstand() {
        return mustUnderstand;
    }

    /**
     * Sets the value of the mustUnderstand property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMustUnderstand(String value) {
        this.mustUnderstand = value;
    }

    /**
     * Gets the value of the processContent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcessContent() {
        return processContent;
    }

    /**
     * Sets the value of the processContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcessContent(String value) {
        this.processContent = value;
    }

    @XmlTransient
    private Object parent;
    
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
     *         &lt;any maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *       &lt;attribute name="Requires" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute ref="{http://schemas.openxmlformats.org/markup-compatibility/2006}Ignorable"/>
     *       &lt;attribute ref="{http://schemas.openxmlformats.org/markup-compatibility/2006}MustUnderstand"/>
     *       &lt;attribute ref="{http://schemas.openxmlformats.org/markup-compatibility/2006}ProcessContent"/>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "any"
    })
    public static class Choice implements Child {

        @XmlAnyElement(lax = true)
        protected List<Object> any = new ArrayListMce<Object>(this);
        @XmlAttribute(name = "Requires", required = true)
        protected String requires;
        @XmlAttribute(name = "Ignorable", namespace = "http://schemas.openxmlformats.org/markup-compatibility/2006")
        protected String ignorable;
        @XmlAttribute(name = "MustUnderstand", namespace = "http://schemas.openxmlformats.org/markup-compatibility/2006")
        protected String mustUnderstand;
        @XmlAttribute(name = "ProcessContent", namespace = "http://schemas.openxmlformats.org/markup-compatibility/2006")
        protected String processContent;

        /**
         * Gets the value of the any property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the any property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAny().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Object }
         * 
         * 
         */
        public List<Object> getAny() {
            if (any == null) {
                any = new ArrayListMce<Object>(this);
            }
            return this.any;
        }

        /**
         * Gets the value of the requires property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getRequires() {
            return requires;
        }

        /**
         * Sets the value of the requires property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRequires(String value) {
            this.requires = value;
        }

        /**
         * Gets the value of the ignorable property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getIgnorable() {
            return ignorable;
        }

        /**
         * Sets the value of the ignorable property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setIgnorable(String value) {
            this.ignorable = value;
        }

        /**
         * Gets the value of the mustUnderstand property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMustUnderstand() {
            return mustUnderstand;
        }

        /**
         * Sets the value of the mustUnderstand property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMustUnderstand(String value) {
            this.mustUnderstand = value;
        }

        /**
         * Gets the value of the processContent property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getProcessContent() {
            return processContent;
        }

        /**
         * Sets the value of the processContent property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setProcessContent(String value) {
            this.processContent = value;
        }

        @XmlTransient
        private Object parent;
        
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
     *         &lt;any maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *       &lt;attribute ref="{http://schemas.openxmlformats.org/markup-compatibility/2006}Ignorable"/>
     *       &lt;attribute ref="{http://schemas.openxmlformats.org/markup-compatibility/2006}MustUnderstand"/>
     *       &lt;attribute ref="{http://schemas.openxmlformats.org/markup-compatibility/2006}ProcessContent"/>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "any"
    })
    public static class Fallback implements Child {

        @XmlAnyElement(lax = true)
        protected List<Object> any = new ArrayListMce<Object>(this);
        @XmlAttribute(name = "Ignorable", namespace = "http://schemas.openxmlformats.org/markup-compatibility/2006")
        protected String ignorable;
        @XmlAttribute(name = "MustUnderstand", namespace = "http://schemas.openxmlformats.org/markup-compatibility/2006")
        protected String mustUnderstand;
        @XmlAttribute(name = "ProcessContent", namespace = "http://schemas.openxmlformats.org/markup-compatibility/2006")
        protected String processContent;

        /**
         * Gets the value of the any property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the any property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAny().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Object }
         * 
         * 
         */
        public List<Object> getAny() {
            if (any == null) {
                any = new ArrayListMce<Object>(this);
            }
            return this.any;
        }

        /**
         * Gets the value of the ignorable property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getIgnorable() {
            return ignorable;
        }

        /**
         * Sets the value of the ignorable property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setIgnorable(String value) {
            this.ignorable = value;
        }

        /**
         * Gets the value of the mustUnderstand property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMustUnderstand() {
            return mustUnderstand;
        }

        /**
         * Sets the value of the mustUnderstand property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMustUnderstand(String value) {
            this.mustUnderstand = value;
        }

        /**
         * Gets the value of the processContent property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getProcessContent() {
            return processContent;
        }

        /**
         * Sets the value of the processContent property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setProcessContent(String value) {
            this.processContent = value;
        }

        @XmlTransient
        private Object parent;
        
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
