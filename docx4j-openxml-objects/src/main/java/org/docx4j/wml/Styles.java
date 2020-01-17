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
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}docDefaults" minOccurs="0"/>
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
 *                           &lt;attribute name="locked" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *                           &lt;attribute name="uiPriority" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *                           &lt;attribute name="semiHidden" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *                           &lt;attribute name="unhideWhenUsed" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *                           &lt;attribute name="qFormat" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="defLockedState" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *                 &lt;attribute name="defUIPriority" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *                 &lt;attribute name="defSemiHidden" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *                 &lt;attribute name="defUnhideWhenUsed" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *                 &lt;attribute name="defQFormat" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *                 &lt;attribute name="count" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}style" maxOccurs="unbounded" minOccurs="0"/>
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
    "docDefaults",
    "latentStyles",
    "style"
})
@XmlRootElement(name = "styles")
public class Styles implements Child
{
    @XmlAttribute(name = "Ignorable", namespace = "http://schemas.openxmlformats.org/markup-compatibility/2006")
    protected String ignorable;

    protected DocDefaults docDefaults;
    protected Styles.LatentStyles latentStyles;
    protected List<Style> style  = new ArrayListWml<Style>(this);
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the docDefaults property.
     * 
     * @return
     *     possible object is
     *     {@link DocDefaults }
     *     
     */
    public DocDefaults getDocDefaults() {
        return docDefaults;
    }

    /**
     * Sets the value of the docDefaults property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocDefaults }
     *     
     */
    public void setDocDefaults(DocDefaults value) {
        this.docDefaults = value;
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
     * {@link Style }
     * 
     * 
     */
    public List<Style> getStyle() {
        if (style == null) {
            style = new ArrayListWml<Style>(this);
        }
        return this.style;
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
     *                 &lt;attribute name="locked" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
     *                 &lt;attribute name="uiPriority" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
     *                 &lt;attribute name="semiHidden" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
     *                 &lt;attribute name="unhideWhenUsed" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
     *                 &lt;attribute name="qFormat" type="{http://www.w3.org/2001/XMLSchema}boolean" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="defLockedState" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
     *       &lt;attribute name="defUIPriority" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
     *       &lt;attribute name="defSemiHidden" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
     *       &lt;attribute name="defUnhideWhenUsed" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
     *       &lt;attribute name="defQFormat" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
     *       &lt;attribute name="count" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
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
    public static class LatentStyles implements Child
    {

        protected List<Styles.LatentStyles.LsdException> lsdException;
        @XmlAttribute(name = "defLockedState", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected Boolean defLockedState;
        @XmlAttribute(name = "defUIPriority", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected BigInteger defUIPriority;
        @XmlAttribute(name = "defSemiHidden", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected Boolean defSemiHidden;
        @XmlAttribute(name = "defUnhideWhenUsed", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected Boolean defUnhideWhenUsed;
        @XmlAttribute(name = "defQFormat", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected Boolean defQFormat;
        @XmlAttribute(name = "count", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
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
         *     {@link Boolean }
         *     
         */
        public boolean isDefLockedState() {
            if (defLockedState == null) {
                return true;
            } else {
                return defLockedState;
            }
        }

        /**
         * Sets the value of the defLockedState property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setDefLockedState(Boolean value) {
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
         *     {@link Boolean }
         *     
         */
        public boolean isDefSemiHidden() {
            if (defSemiHidden == null) {
                return true;
            } else {
                return defSemiHidden;
            }
        }

        /**
         * Sets the value of the defSemiHidden property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setDefSemiHidden(Boolean value) {
            this.defSemiHidden = value;
        }

        /**
         * Gets the value of the defUnhideWhenUsed property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public boolean isDefUnhideWhenUsed() {
            if (defUnhideWhenUsed == null) {
                return true;
            } else {
                return defUnhideWhenUsed;
            }
        }

        /**
         * Sets the value of the defUnhideWhenUsed property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setDefUnhideWhenUsed(Boolean value) {
            this.defUnhideWhenUsed = value;
        }

        /**
         * Gets the value of the defQFormat property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public boolean isDefQFormat() {
            if (defQFormat == null) {
                return false;
            } else {
                return defQFormat;
            }
        }

        /**
         * Sets the value of the defQFormat property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setDefQFormat(Boolean value) {
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
         *       &lt;attribute name="locked" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
         *       &lt;attribute name="uiPriority" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
         *       &lt;attribute name="semiHidden" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
         *       &lt;attribute name="unhideWhenUsed" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
         *       &lt;attribute name="qFormat" type="{http://www.w3.org/2001/XMLSchema}boolean" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class LsdException implements Child
        {

            @XmlAttribute(name = "name", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
            protected String name;
            @XmlAttribute(name = "locked", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
            protected Boolean locked;
            @XmlAttribute(name = "uiPriority", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
            protected BigInteger uiPriority;
            @XmlAttribute(name = "semiHidden", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
            protected Boolean semiHidden;
            @XmlAttribute(name = "unhideWhenUsed", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
            protected Boolean unhideWhenUsed;
            @XmlAttribute(name = "qFormat", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
            protected Boolean qFormat;
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
             *     {@link Boolean }
             *     
             */
            public boolean isLocked() {
                if (locked == null) {
                    return true;
                } else {
                    return locked;
                }
            }

            /**
             * Sets the value of the locked property.
             * 
             * @param value
             *     allowed object is
             *     {@link Boolean }
             *     
             */
            public void setLocked(Boolean value) {
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
             *     {@link Boolean }
             *     
             */
            public boolean isSemiHidden() {
                if (semiHidden == null) {
                    return true;
                } else {
                    return semiHidden;
                }
            }

            /**
             * Sets the value of the semiHidden property.
             * 
             * @param value
             *     allowed object is
             *     {@link Boolean }
             *     
             */
            public void setSemiHidden(Boolean value) {
                this.semiHidden = value;
            }

            /**
             * Gets the value of the unhideWhenUsed property.
             * 
             * @return
             *     possible object is
             *     {@link Boolean }
             *     
             */
            public boolean isUnhideWhenUsed() {
                if (unhideWhenUsed == null) {
                    return true;
                } else {
                    return unhideWhenUsed;
                }
            }

            /**
             * Sets the value of the unhideWhenUsed property.
             * 
             * @param value
             *     allowed object is
             *     {@link Boolean }
             *     
             */
            public void setUnhideWhenUsed(Boolean value) {
                this.unhideWhenUsed = value;
            }

            /**
             * Gets the value of the qFormat property.
             * 
             * @return
             *     possible object is
             *     {@link Boolean }
             *     
             */
            public Boolean isQFormat() {
            	
            	if (qFormat==null) {
            		return ((Styles.LatentStyles)this.parent).isDefQFormat();
            	} else {            	
            		return qFormat;
            	}
            }

            /**
             * Sets the value of the qFormat property.
             * 
             * @param value
             *     allowed object is
             *     {@link Boolean }
             *     
             */
            public void setQFormat(Boolean value) {
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

}
