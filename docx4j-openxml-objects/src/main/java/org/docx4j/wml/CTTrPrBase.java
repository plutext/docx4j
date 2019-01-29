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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TrPrBase complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TrPrBase">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="cnfStyle" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Cnf" minOccurs="0"/>
 *         &lt;element name="divId" minOccurs="0">
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
 *         &lt;element name="gridBefore" minOccurs="0">
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
 *         &lt;element name="gridAfter" minOccurs="0">
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
 *         &lt;element name="wBefore" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TblWidth" minOccurs="0"/>
 *         &lt;element name="wAfter" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TblWidth" minOccurs="0"/>
 *         &lt;element name="cantSplit" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="trHeight" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Height" minOccurs="0"/>
 *         &lt;element name="tblHeader" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="tblCellSpacing" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TblWidth" minOccurs="0"/>
 *         &lt;element name="jc" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Jc" minOccurs="0"/>
 *         &lt;element name="hidden" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TrPrBase", propOrder = {
    "cnfStyleOrDivIdOrGridBefore"
})
public class CTTrPrBase implements Child
{

    @XmlElementRefs({
        @XmlElementRef(name = "hidden", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "wBefore", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "tblCellSpacing", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "gridAfter", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "wAfter", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "divId", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "trHeight", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "cnfStyle", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "cantSplit", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "tblHeader", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "gridBefore", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "jc", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class)
    })
    protected List<JAXBElement<?>> cnfStyleOrDivIdOrGridBefore = new ArrayListWml<JAXBElement<?>>(this);
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the cnfStyleOrDivIdOrGridBefore property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cnfStyleOrDivIdOrGridBefore property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCnfStyleOrDivIdOrGridBefore().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
     * {@link JAXBElement }{@code <}{@link TblWidth }{@code >}
     * {@link JAXBElement }{@code <}{@link TblWidth }{@code >}
     * {@link JAXBElement }{@code <}{@link CTTrPrBase.GridAfter }{@code >}
     * {@link JAXBElement }{@code <}{@link CTTrPrBase.DivId }{@code >}
     * {@link JAXBElement }{@code <}{@link TblWidth }{@code >}
     * {@link JAXBElement }{@code <}{@link CTCnf }{@code >}
     * {@link JAXBElement }{@code <}{@link CTHeight }{@code >}
     * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
     * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
     * {@link JAXBElement }{@code <}{@link CTTrPrBase.GridBefore }{@code >}
     * {@link JAXBElement }{@code <}{@link Jc }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getCnfStyleOrDivIdOrGridBefore() {
        if (cnfStyleOrDivIdOrGridBefore == null) {
            cnfStyleOrDivIdOrGridBefore = new ArrayListWml<JAXBElement<?>>(this);
        }
        return this.cnfStyleOrDivIdOrGridBefore;
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
    public static class DivId implements Child
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
    public static class GridAfter implements Child
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
    public static class GridBefore implements Child
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
