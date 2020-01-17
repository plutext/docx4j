/*
 *  Copyright 2007-2013, Plutext Pty Ltd.
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

//import org.docx4j.XmlUtils;
//import org.docx4j.jaxb.Context;
//import org.docx4j.Docx4jProperties;

import org.docx4j.w14.CTSdtCheckbox;
import org.docx4j.w15.CTSdtAppearance;
import org.docx4j.w15.CTSdtRepeatedSection;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 *         &lt;element ref="{http://schemas.microsoft.com/office/word/2010/wordml}checkbox" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.microsoft.com/office/word/2010/wordml}entityPicker" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.microsoft.com/office/word/2012/wordml}appearance" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.microsoft.com/office/word/2012/wordml}color" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.microsoft.com/office/word/2012/wordml}dataBinding" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.microsoft.com/office/word/2012/wordml}repeatingSection" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.microsoft.com/office/word/2012/wordml}repeatingSectionItem" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.microsoft.com/office/word/2012/wordml}webExtensionCreated" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.microsoft.com/office/word/2012/wordml}webExtensionLinked" minOccurs="0"/>
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
	private static Logger log = LoggerFactory.getLogger(SdtPr.class);

	private static Object unwrap(Object o) {
		
		if (o==null) return null;
		
		if (o instanceof javax.xml.bind.JAXBElement) {
			log.debug("Unwrapped " + ((JAXBElement)o).getDeclaredType().getName() );
			log.debug("name: " + ((JAXBElement)o).getName() );
			return ((JAXBElement)o).getValue();
		} else {
			return o;
		}
	}
	
    @XmlElementRefs({
        @XmlElementRef(name = "placeholder", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "dataBinding", namespace = "http://schemas.microsoft.com/office/word/2012/wordml", type = JAXBElement.class),
        @XmlElementRef(name = "alias", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "checkbox", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class),
        @XmlElementRef(name = "showingPlcHdr", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "webExtensionCreated", namespace = "http://schemas.microsoft.com/office/word/2012/wordml", type = JAXBElement.class),
        @XmlElementRef(name = "comboBox", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "citation", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "dataBinding", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "temporary", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "repeatingSectionItem", namespace = "http://schemas.microsoft.com/office/word/2012/wordml", type = JAXBElement.class),
        @XmlElementRef(name = "tag", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = Tag.class),
        @XmlElementRef(name = "bibliography", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "entityPicker", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = JAXBElement.class),
        @XmlElementRef(name = "lock", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "docPartList", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "id", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = Id.class),
        @XmlElementRef(name = "text", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "date", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "color", namespace = "http://schemas.microsoft.com/office/word/2012/wordml", type = JAXBElement.class),
        @XmlElementRef(name = "equation", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "docPartObj", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "webExtensionLinked", namespace = "http://schemas.microsoft.com/office/word/2012/wordml", type = JAXBElement.class),
        @XmlElementRef(name = "appearance", namespace = "http://schemas.microsoft.com/office/word/2012/wordml", type = JAXBElement.class),
        @XmlElementRef(name = "dropDownList", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "picture", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "repeatingSection", namespace = "http://schemas.microsoft.com/office/word/2012/wordml", type = JAXBElement.class),
        @XmlElementRef(name = "rPr", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "group", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "richText", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class)
    })
    protected List<Object> rPrOrAliasOrLock  = new ArrayListWml<Object>(this);
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
     * {@link JAXBElement }{@code <}{@link CTPlaceholder }{@code >}
     * {@link JAXBElement }{@code <}{@link CTDataBinding }{@code >}
     * {@link JAXBElement }{@code <}{@link SdtPr.Alias }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSdtCheckbox }{@code >}
     * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
     * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSdtComboBox }{@code >}
     * {@link JAXBElement }{@code <}{@link SdtPr.Citation }{@code >}
     * {@link JAXBElement }{@code <}{@link CTDataBinding }{@code >}
     * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
     * {@link JAXBElement }{@code <}{@link CTEmpty }{@code >}
     * {@link Tag }
     * {@link JAXBElement }{@code <}{@link SdtPr.Bibliography }{@code >}
     * {@link JAXBElement }{@code <}{@link CTEmpty }{@code >}
     * {@link JAXBElement }{@code <}{@link CTLock }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSdtDocPart }{@code >}
     * {@link Id }
     * {@link JAXBElement }{@code <}{@link CTSdtText }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSdtDate }{@code >}
     * {@link JAXBElement }{@code <}{@link CTColor }{@code >}
     * {@link JAXBElement }{@code <}{@link SdtPr.Equation }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSdtDocPart }{@code >}
     * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSdtAppearance }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSdtDropDownList }{@code >}
     * {@link JAXBElement }{@code <}{@link SdtPr.Picture }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSdtRepeatedSection }{@code >}
     * {@link JAXBElement }{@code <}{@link SdtPr.Group }{@code >}
     * {@link JAXBElement }{@code <}{@link RPr }{@code >}
     * {@link JAXBElement }{@code <}{@link SdtPr.RichText }{@code >}
     * 
     * 
     */
    public List<Object> getRPrOrAliasOrLock() {
        if (rPrOrAliasOrLock == null) {
            rPrOrAliasOrLock =  new ArrayListWml<Object>(this);
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
     * return the first object 
     * @param clazz
     * @return
     * @since 3.0.1
     */
    public Object getByClass(Class clazz) {
    	
    	for (Object o : getRPrOrAliasOrLock()) {
    		Object o2 = unwrap(o);
    		if ( o2.getClass().equals(clazz) ) {
    			log.debug("found " + clazz.getName());
    			return o2;
    		} 
    	}
    	
        return null;    	
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
    			
//    			if (Docx4jProperties.getProperty("docx4j.wml.SdtPr.Tag.LengthExceeds64Warning", true)
//    					&& ((Tag)o).getVal().length()>64) {
//    				log.warn("w:tag too long for Word 2007/2010: " + ((Tag)o).getVal());
//    			}
    			
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
        		rPrOrAliasOrLock.remove(existingTag);
        		if (value==null) {
        			log.debug("Changing SDT tag from " + existingTag.getVal() + " to null");
        		} else {
        			log.debug("Changing SDT tag from " + existingTag.getVal() + " to " + value.getVal() );
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
    		o = unwrap(o);
    		log.debug("inspecting " + o.getClass().getName() );
    		if ( o instanceof CTDataBinding ) {
    			return (CTDataBinding)o;
    		} 
    	}
        return null;
    }
    
    public void setDataBinding(CTDataBinding value) {
        
    	Object existingBinding = getDataBinding(); 
    	
    	if (existingBinding!=null) {
    		if (!existingBinding.equals(value)) {
    			log.debug("Changing DataBinding tag from " + existingBinding + " to " + value);
    			
        		//rPrOrAliasOrLock.remove(existingBinding);
    			existingBinding = null;
    			for (Object o : getRPrOrAliasOrLock() ) {
    				Object unwrapped = unwrap(o);
    				if (unwrapped instanceof CTDataBinding) {
    					existingBinding = o;
    					break;
    				}
    			}
    			if (existingBinding!=null) {
    				getRPrOrAliasOrLock().remove(existingBinding);
    			}
    			
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
     * @param set
     * @since 6.1.0
     */
    public void setShowingPlcHdr(boolean set) {
    	
    	JAXBElement<BooleanDefaultTrue> plcHdr = null;
    	    	
    	// Is there an existing value
    	for (Object o : getRPrOrAliasOrLock()) {
    		if (o instanceof JAXBElement 
    				&& ((JAXBElement)o).getName().getLocalPart().equals("showingPlcHdr")) {
    			
    			plcHdr = (JAXBElement<BooleanDefaultTrue>)o;
    			break;
    		} 
    	}
    	
    	if (plcHdr==null) {
    		
    		if (set) {
    			plcHdr = ObjectFactory.get().createSdtPrShowingPlcHdr(new BooleanDefaultTrue() );
    			getRPrOrAliasOrLock().add(plcHdr);
    		}
    		
    	} else {
    		
    		if (set) {
    			// if its false, set it
    			if (!plcHdr.getValue().isVal()) {
    				plcHdr.setValue(new BooleanDefaultTrue() );
    			}
    		} else /* remove */ {
    			getRPrOrAliasOrLock().remove(plcHdr);
    		}
    		
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

        @XmlAttribute(name = "val", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
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
