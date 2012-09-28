/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */


package org.docx4j.wml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;

/**
 * <p>Java class for CT_SdtPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SdtPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="rPr" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_RPr" minOccurs="0"/>
 *         &lt;element name="alias" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="lock" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Lock" minOccurs="0"/>
 *         &lt;element name="placeholder" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Placeholder" minOccurs="0"/>
 *         &lt;element name="showingPlcHdr" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="dataBinding" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_DataBinding" minOccurs="0"/>
 *         &lt;element name="temporary" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}id" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}tag" minOccurs="0"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="equation">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="comboBox" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_SdtComboBox"/>
 *           &lt;element name="date" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_SdtDate"/>
 *           &lt;element name="docPartObj" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_SdtDocPart"/>
 *           &lt;element name="docPartList" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_SdtDocPart"/>
 *           &lt;element name="dropDownList" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_SdtDropDownList"/>
 *           &lt;element name="picture">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="richText">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="text" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_SdtText"/>
 *           &lt;element name="citation">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="group">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="bibliography">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *         &lt;/choice>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SdtPr", propOrder = {
    "rPrOrAliasOrLock"
})
@XmlRootElement(name = "sdtPr")
public class SdtPr
    implements Child
{
	private static Logger log = Logger.getLogger(SdtPr.class);

    @XmlElementRefs({
        @XmlElementRef(name = "date", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "dataBinding", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "comboBox", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "docPartList", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "rPr", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "richText", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "lock", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "showingPlcHdr", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "group", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "id", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = Id.class),
        @XmlElementRef(name = "citation", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "text", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "placeholder", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "picture", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "temporary", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "alias", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "equation", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "docPartObj", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "tag", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = Tag.class),
        @XmlElementRef(name = "dropDownList", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "bibliography", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class)
    })
    protected List<Object> rPrOrAliasOrLock;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the rPrOrAliasOrLock property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rPrOrAliasOrLock property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRPrOrAliasOrLock().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link CTSdtDate }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSdtComboBox }{@code >}
     * {@link JAXBElement }{@code <}{@link CTDataBinding }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSdtDocPart }{@code >}
     * {@link JAXBElement }{@code <}{@link SdtPr.RichText }{@code >}
     * {@link JAXBElement }{@code <}{@link RPr }{@code >}
     * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
     * {@link JAXBElement }{@code <}{@link CTLock }{@code >}
     * {@link JAXBElement }{@code <}{@link SdtPr.Group }{@code >}
     * {@link Id }
     * {@link JAXBElement }{@code <}{@link SdtPr.Citation }{@code >}
     * {@link JAXBElement }{@code <}{@link SdtPr.Picture }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPlaceholder }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSdtText }{@code >}
     * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
     * {@link JAXBElement }{@code <}{@link SdtPr.Alias }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSdtDocPart }{@code >}
     * {@link JAXBElement }{@code <}{@link SdtPr.Equation }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSdtDropDownList }{@code >}
     * {@link Tag }
     * {@link JAXBElement }{@code <}{@link SdtPr.Bibliography }{@code >}
     * 
     * 
     */
    public List<Object> getRPrOrAliasOrLock() {
        if (rPrOrAliasOrLock == null) {
            rPrOrAliasOrLock = new ArrayList<Object>();
        }
        return this.rPrOrAliasOrLock;
    }
    
    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link Id }
     *     
     */
    public Id getId() {
    	
    	for (Object o : getRPrOrAliasOrLock()) {
    		if ( o instanceof Id ) {
    			log.debug("found id");
    			return (Id)o;
    		} 
    	}
    	
        return null;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link Id }
     *     
     */
    public void setId(Id value) {
    	
    	Id existingId = getId(); 
    	
    	if (existingId!=null) {
    		if (!existingId.equals(value)) {
    			log.debug("Changing SDT ID from " + existingId + " to " + value);
        		rPrOrAliasOrLock.remove(existingId);
        		if (value!=null) {
        			rPrOrAliasOrLock.add(value);
        		}
    		}    	
    		// else - they are the same, so do nothing
    	} else if (value!=null) {
    		//ObjectFactory factory = new ObjectFactory();
    		//JAXBElement idWrapper = factory.createSdtPrId(value);
    		rPrOrAliasOrLock.add(value);
    	}
    }
    
    // Not generated!
    public java.math.BigInteger setId() {

    	Id id = new Id();
    	java.math.BigInteger newIdVal = java.math.BigInteger.valueOf(Math.abs(new java.util.Random().nextInt()));
    	id.setVal( newIdVal );
    	setId(id);    	
		log.debug("Generated random id: " + newIdVal.toString() );
		return newIdVal;
    }

    /**
     * Gets the value of the tag property.
     * 
     * @return
     *     possible object is
     *     {@link SdtPr.Tag }
     *     
     */
    public Tag getTag() {
    	
    	for (Object o : getRPrOrAliasOrLock()) {
    		if ( o instanceof Tag ) {
    			log.debug("found tag");
    			return (Tag)o;
    		} 
    	}
    	
        return null;
    }

    /**
     * Sets the value of the tag property.
     * 
     * @param value
     *     allowed object is
     *     {@link SdtPr.Tag }
     *     
     */
    public void setTag(Tag value) {
        
    	Tag existingTag = getTag(); 
    	
    	if (existingTag!=null) {
    		if (!existingTag.equals(value)) {
    			log.debug("Changing SDT tag from " + existingTag + " to " + value);
        		rPrOrAliasOrLock.remove(existingTag);
        		if (value!=null) {
        			rPrOrAliasOrLock.add(value);
        		}
    		}
    		// else - they are the same, so do nothing
    	} else if (value!=null) {
    		//ObjectFactory factory = new ObjectFactory();
    		//JAXBElement idWrapper = factory.createSdtPrId(value);
    		rPrOrAliasOrLock.add(value);
    	}
    }
    
    public CTDataBinding getDataBinding() {
    	
    	for (Object o : getRPrOrAliasOrLock()) {
    		o = XmlUtils.unwrap(o);
    		log.debug("inspecting " + o.getClass().getName() );
    		if ( o instanceof CTDataBinding ) {
    			return (CTDataBinding)o;
    		} 
    	}
        return null;
    }
    
    public void setDataBinding(CTDataBinding value) {
        
    	CTDataBinding existingBinding = getDataBinding(); 
    	
    	if (existingBinding!=null) {
    		if (!existingBinding.equals(value)) {
    			log.debug("Changing DataBinding tag from " + existingBinding + " to " + value);
        		rPrOrAliasOrLock.remove(existingBinding);
        		if (value!=null) {
        			rPrOrAliasOrLock.add(value);
        		}
    		}
    		// else - they are the same, so do nothing
    	} else if (value!=null) {
    		//ObjectFactory factory = new ObjectFactory();
    		//JAXBElement idWrapper = factory.createSdtPrId(value);
    		rPrOrAliasOrLock.add(value);
    	}
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
     *       &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @XmlRootElement(name = "alias")
    public static class Alias
        implements Child
    {

        @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
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
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @XmlRootElement(name = "bibliography")
    public static class Bibliography
        implements Child
    {

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
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @XmlRootElement(name = "citation")
    public static class Citation
        implements Child
    {

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
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @XmlRootElement(name = "equation")
    public static class Equation
        implements Child
    {

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
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @XmlRootElement(name = "group")
    public static class Group
        implements Child
    {

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
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @XmlRootElement(name = "picture")
    public static class Picture
        implements Child
    {

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
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @XmlRootElement(name = "richText")
    public static class RichText
        implements Child
    {

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
