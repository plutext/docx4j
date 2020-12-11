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
 *         &lt;element name="numPicBullet" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="pict" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Picture"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="numPicBulletId" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="abstractNum" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="nsid" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_LongHexNumber" minOccurs="0"/>
 *                   &lt;element name="multiLevelType" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="val" use="required">
 *                             &lt;simpleType>
 *                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                 &lt;enumeration value="singleLevel"/>
 *                                 &lt;enumeration value="multilevel"/>
 *                                 &lt;enumeration value="hybridMultilevel"/>
 *                               &lt;/restriction>
 *                             &lt;/simpleType>
 *                           &lt;/attribute>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="tmpl" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_LongHexNumber" minOccurs="0"/>
 *                   &lt;element name="name" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="styleLink" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="numStyleLink" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="lvl" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Lvl" maxOccurs="9" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="abstractNumId" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="num" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="abstractNumId">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="val" use="required">
 *                             &lt;simpleType>
 *                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *                               &lt;/restriction>
 *                             &lt;/simpleType>
 *                           &lt;/attribute>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="lvlOverride" maxOccurs="9" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="startOverride" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;attribute name="val" use="required">
 *                                       &lt;simpleType>
 *                                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *                                         &lt;/restriction>
 *                                       &lt;/simpleType>
 *                                     &lt;/attribute>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                             &lt;element name="lvl" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Lvl" minOccurs="0"/>
 *                           &lt;/sequence>
 *                           &lt;attribute name="ilvl" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="numId" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="numIdMacAtCleanup" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="val" use="required">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
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
    "numPicBullet",
    "abstractNum",
    "num",
    "numIdMacAtCleanup"
})
@XmlRootElement(name = "numbering")
public class Numbering implements Child
{
    @XmlAttribute(name = "Ignorable", namespace = "http://schemas.openxmlformats.org/markup-compatibility/2006")
    protected String ignorable;

    protected List<Numbering.NumPicBullet> numPicBullet = new ArrayListWml<Numbering.NumPicBullet>(this);
    protected List<Numbering.AbstractNum> abstractNum = new ArrayListWml<Numbering.AbstractNum>(this);
    protected List<Numbering.Num> num  = new ArrayListWml<Numbering.Num>(this);
    protected Numbering.NumIdMacAtCleanup numIdMacAtCleanup;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the numPicBullet property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the numPicBullet property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNumPicBullet().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Numbering.NumPicBullet }
     * 
     * 
     */
    public List<Numbering.NumPicBullet> getNumPicBullet() {
        if (numPicBullet == null) {
            numPicBullet = new ArrayListWml<Numbering.NumPicBullet>(this);
        }
        return this.numPicBullet;
    }

    /**
     * Gets the value of the abstractNum property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractNum property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractNum().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Numbering.AbstractNum }
     * 
     * 
     */
    public List<Numbering.AbstractNum> getAbstractNum() {
        if (abstractNum == null) {
            abstractNum = new ArrayListWml<Numbering.AbstractNum>(this);
        }
        return this.abstractNum;
    }

    /**
     * Gets the value of the num property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the num property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNum().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Numbering.Num }
     * 
     * 
     */
    public List<Numbering.Num> getNum() {
        if (num == null) {
            num = new ArrayListWml<Numbering.Num>(this);
        }
        return this.num;
    }

    /**
     * Gets the value of the numIdMacAtCleanup property.
     * 
     * @return
     *     possible object is
     *     {@link Numbering.NumIdMacAtCleanup }
     *     
     */
    public Numbering.NumIdMacAtCleanup getNumIdMacAtCleanup() {
        return numIdMacAtCleanup;
    }

    /**
     * Sets the value of the numIdMacAtCleanup property.
     * 
     * @param value
     *     allowed object is
     *     {@link Numbering.NumIdMacAtCleanup }
     *     
     */
    public void setNumIdMacAtCleanup(Numbering.NumIdMacAtCleanup value) {
        this.numIdMacAtCleanup = value;
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
     *         &lt;element name="nsid" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_LongHexNumber" minOccurs="0"/>
     *         &lt;element name="multiLevelType" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="val" use="required">
     *                   &lt;simpleType>
     *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                       &lt;enumeration value="singleLevel"/>
     *                       &lt;enumeration value="multilevel"/>
     *                       &lt;enumeration value="hybridMultilevel"/>
     *                     &lt;/restriction>
     *                   &lt;/simpleType>
     *                 &lt;/attribute>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="tmpl" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_LongHexNumber" minOccurs="0"/>
     *         &lt;element name="name" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="styleLink" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="numStyleLink" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="lvl" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Lvl" maxOccurs="9" minOccurs="0"/>
     *       &lt;/sequence>
     *       &lt;attribute name="abstractNumId" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "nsid",
        "multiLevelType",
        "tmpl",
        "name",
        "styleLink",
        "numStyleLink",
        "lvl"
    })
    @XmlRootElement(name = "abstractNum")    
    public static class AbstractNum
        implements Child
    {

        protected CTLongHexNumber nsid;
        protected Numbering.AbstractNum.MultiLevelType multiLevelType;
        protected CTLongHexNumber tmpl;
        protected Numbering.AbstractNum.Name name;
        protected Numbering.AbstractNum.StyleLink styleLink;
        protected Numbering.AbstractNum.NumStyleLink numStyleLink;
        protected List<Lvl> lvl;
        @XmlAttribute(name = "abstractNumId", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
        protected BigInteger abstractNumId;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the nsid property.
         * 
         * @return
         *     possible object is
         *     {@link CTLongHexNumber }
         *     
         */
        public CTLongHexNumber getNsid() {
            return nsid;
        }

        /**
         * Sets the value of the nsid property.
         * 
         * @param value
         *     allowed object is
         *     {@link CTLongHexNumber }
         *     
         */
        public void setNsid(CTLongHexNumber value) {
            this.nsid = value;
        }

        /**
         * Gets the value of the multiLevelType property.
         * 
         * @return
         *     possible object is
         *     {@link Numbering.AbstractNum.MultiLevelType }
         *     
         */
        public Numbering.AbstractNum.MultiLevelType getMultiLevelType() {
            return multiLevelType;
        }

        /**
         * Sets the value of the multiLevelType property.
         * 
         * @param value
         *     allowed object is
         *     {@link Numbering.AbstractNum.MultiLevelType }
         *     
         */
        public void setMultiLevelType(Numbering.AbstractNum.MultiLevelType value) {
            this.multiLevelType = value;
        }

        /**
         * Gets the value of the tmpl property.
         * 
         * @return
         *     possible object is
         *     {@link CTLongHexNumber }
         *     
         */
        public CTLongHexNumber getTmpl() {
            return tmpl;
        }

        /**
         * Sets the value of the tmpl property.
         * 
         * @param value
         *     allowed object is
         *     {@link CTLongHexNumber }
         *     
         */
        public void setTmpl(CTLongHexNumber value) {
            this.tmpl = value;
        }

        /**
         * Gets the value of the name property.
         * 
         * @return
         *     possible object is
         *     {@link Numbering.AbstractNum.Name }
         *     
         */
        public Numbering.AbstractNum.Name getName() {
            return name;
        }

        /**
         * Sets the value of the name property.
         * 
         * @param value
         *     allowed object is
         *     {@link Numbering.AbstractNum.Name }
         *     
         */
        public void setName(Numbering.AbstractNum.Name value) {
            this.name = value;
        }

        /**
         * Gets the value of the styleLink property.
         * 
         * @return
         *     possible object is
         *     {@link Numbering.AbstractNum.StyleLink }
         *     
         */
        public Numbering.AbstractNum.StyleLink getStyleLink() {
            return styleLink;
        }

        /**
         * Sets the value of the styleLink property.
         * 
         * @param value
         *     allowed object is
         *     {@link Numbering.AbstractNum.StyleLink }
         *     
         */
        public void setStyleLink(Numbering.AbstractNum.StyleLink value) {
            this.styleLink = value;
        }

        /**
         * Gets the value of the numStyleLink property.
         * 
         * @return
         *     possible object is
         *     {@link Numbering.AbstractNum.NumStyleLink }
         *     
         */
        public Numbering.AbstractNum.NumStyleLink getNumStyleLink() {
            return numStyleLink;
        }

        /**
         * Sets the value of the numStyleLink property.
         * 
         * @param value
         *     allowed object is
         *     {@link Numbering.AbstractNum.NumStyleLink }
         *     
         */
        public void setNumStyleLink(Numbering.AbstractNum.NumStyleLink value) {
            this.numStyleLink = value;
        }

        /**
         * Gets the value of the lvl property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the lvl property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getLvl().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Lvl }
         * 
         * 
         */
        public List<Lvl> getLvl() {
            if (lvl == null) {
                lvl = new ArrayList<Lvl>();
            }
            return this.lvl;
        }

        /**
         * Gets the value of the abstractNumId property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getAbstractNumId() {
            return abstractNumId;
        }

        /**
         * Sets the value of the abstractNumId property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setAbstractNumId(BigInteger value) {
            this.abstractNumId = value;
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
         *       &lt;attribute name="val" use="required">
         *         &lt;simpleType>
         *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
         *             &lt;enumeration value="singleLevel"/>
         *             &lt;enumeration value="multilevel"/>
         *             &lt;enumeration value="hybridMultilevel"/>
         *           &lt;/restriction>
         *         &lt;/simpleType>
         *       &lt;/attribute>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class MultiLevelType implements Child
        {

            @XmlAttribute(name = "val", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
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
        public static class Name implements Child
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
        public static class NumStyleLink implements Child
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
        public static class StyleLink implements Child
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
     *         &lt;element name="abstractNumId">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="val" use="required">
     *                   &lt;simpleType>
     *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
     *                     &lt;/restriction>
     *                   &lt;/simpleType>
     *                 &lt;/attribute>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="lvlOverride" maxOccurs="9" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="startOverride" minOccurs="0">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;attribute name="val" use="required">
     *                             &lt;simpleType>
     *                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
     *                               &lt;/restriction>
     *                             &lt;/simpleType>
     *                           &lt;/attribute>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                   &lt;element name="lvl" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Lvl" minOccurs="0"/>
     *                 &lt;/sequence>
     *                 &lt;attribute name="ilvl" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="numId" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "abstractNumId",
        "lvlOverride"
    })
    @XmlRootElement(name = "num")
    public static class Num implements Child
    {

        @XmlElement(required = true)
        protected Numbering.Num.AbstractNumId abstractNumId;
        protected List<Numbering.Num.LvlOverride> lvlOverride;
        @XmlAttribute(name = "numId", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
        protected BigInteger numId;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the abstractNumId property.
         * 
         * @return
         *     possible object is
         *     {@link Numbering.Num.AbstractNumId }
         *     
         */
        public Numbering.Num.AbstractNumId getAbstractNumId() {
            return abstractNumId;
        }

        /**
         * Sets the value of the abstractNumId property.
         * 
         * @param value
         *     allowed object is
         *     {@link Numbering.Num.AbstractNumId }
         *     
         */
        public void setAbstractNumId(Numbering.Num.AbstractNumId value) {
            this.abstractNumId = value;
        }

        /**
         * Gets the value of the lvlOverride property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the lvlOverride property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getLvlOverride().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Numbering.Num.LvlOverride }
         * 
         * 
         */
        public List<Numbering.Num.LvlOverride> getLvlOverride() {
            if (lvlOverride == null) {
                lvlOverride = new ArrayList<Numbering.Num.LvlOverride>();
            }
            return this.lvlOverride;
        }

        /**
         * Gets the value of the numId property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getNumId() {
            return numId;
        }

        /**
         * Sets the value of the numId property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setNumId(BigInteger value) {
            this.numId = value;
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
         *       &lt;attribute name="val" use="required">
         *         &lt;simpleType>
         *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
         *           &lt;/restriction>
         *         &lt;/simpleType>
         *       &lt;/attribute>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class AbstractNumId implements Child
        {

            @XmlAttribute(name = "val", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
            protected BigInteger val;
            @XmlTransient
            private Object parent;

            /**
             * Gets the value of the val property.
             * 
             * @return
             *     possible object is
             *     {@link BigInteger }
             *     
             */
            public BigInteger getVal() {
                return val;
            }

            /**
             * Sets the value of the val property.
             * 
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *     
             */
            public void setVal(BigInteger value) {
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
         *       &lt;sequence>
         *         &lt;element name="startOverride" minOccurs="0">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;attribute name="val" use="required">
         *                   &lt;simpleType>
         *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
         *                     &lt;/restriction>
         *                   &lt;/simpleType>
         *                 &lt;/attribute>
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *         &lt;element name="lvl" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Lvl" minOccurs="0"/>
         *       &lt;/sequence>
         *       &lt;attribute name="ilvl" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "startOverride",
            "lvl"
        })
        @XmlRootElement(name = "lvlOverride")
        public static class LvlOverride
            implements Child
        {

            protected Numbering.Num.LvlOverride.StartOverride startOverride;
            protected Lvl lvl;
            @XmlAttribute(name = "ilvl", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
            protected BigInteger ilvl;
            @XmlTransient
            private Object parent;

            /**
             * Gets the value of the startOverride property.
             * 
             * @return
             *     possible object is
             *     {@link Numbering.Num.LvlOverride.StartOverride }
             *     
             */
            public Numbering.Num.LvlOverride.StartOverride getStartOverride() {
                return startOverride;
            }

            /**
             * Sets the value of the startOverride property.
             * 
             * @param value
             *     allowed object is
             *     {@link Numbering.Num.LvlOverride.StartOverride }
             *     
             */
            public void setStartOverride(Numbering.Num.LvlOverride.StartOverride value) {
                this.startOverride = value;
            }

            /**
             * Gets the value of the lvl property.
             * 
             * @return
             *     possible object is
             *     {@link Lvl }
             *     
             */
            public Lvl getLvl() {
                return lvl;
            }

            /**
             * Sets the value of the lvl property.
             * 
             * @param value
             *     allowed object is
             *     {@link Lvl }
             *     
             */
            public void setLvl(Lvl value) {
                this.lvl = value;
            }

            /**
             * Gets the value of the ilvl property.
             * 
             * @return
             *     possible object is
             *     {@link BigInteger }
             *     
             */
            public BigInteger getIlvl() {
                return ilvl;
            }

            /**
             * Sets the value of the ilvl property.
             * 
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *     
             */
            public void setIlvl(BigInteger value) {
                this.ilvl = value;
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
             *       &lt;attribute name="val" use="required">
             *         &lt;simpleType>
             *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
             *           &lt;/restriction>
             *         &lt;/simpleType>
             *       &lt;/attribute>
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class StartOverride implements Child
            {

                @XmlAttribute(name = "val", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
                protected BigInteger val;
                @XmlTransient
                private Object parent;

                /**
                 * Gets the value of the val property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getVal() {
                    return val;
                }

                /**
                 * Sets the value of the val property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setVal(BigInteger value) {
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="val" use="required">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
     *           &lt;/restriction>
     *         &lt;/simpleType>
     *       &lt;/attribute>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class NumIdMacAtCleanup implements Child
    {

        @XmlAttribute(name = "val", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
        protected BigInteger val;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the val property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getVal() {
            return val;
        }

        /**
         * Sets the value of the val property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setVal(BigInteger value) {
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
     *       &lt;sequence>
     *         &lt;element name="pict" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Picture"/>
     *       &lt;/sequence>
     *       &lt;attribute name="numPicBulletId" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "pict"
    })
    @XmlRootElement(name="numPicBullet")
    public static class NumPicBullet implements Child
    {

        @XmlElement(required = true)
        protected Pict pict;
        @XmlAttribute(name = "numPicBulletId", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
        protected BigInteger numPicBulletId;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the pict property.
         * 
         * @return
         *     possible object is
         *     {@link Pict }
         *     
         */
        public Pict getPict() {
            return pict;
        }

        /**
         * Sets the value of the pict property.
         * 
         * @param value
         *     allowed object is
         *     {@link Pict }
         *     
         */
        public void setPict(Pict value) {
            this.pict = value;
        }

        /**
         * Gets the value of the numPicBulletId property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getNumPicBulletId() {
            return numPicBulletId;
        }

        /**
         * Sets the value of the numPicBulletId property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setNumPicBulletId(BigInteger value) {
            this.numPicBulletId = value;
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
