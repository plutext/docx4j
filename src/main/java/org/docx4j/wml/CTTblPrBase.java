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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TblPrBase complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TblPrBase">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="tblStyle" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="tblpPr" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TblPPr" minOccurs="0"/>
 *         &lt;element name="tblOverlap" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TblOverlap" minOccurs="0"/>
 *         &lt;element name="bidiVisual" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="tblStyleRowBandSize" minOccurs="0">
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
 *         &lt;element name="tblStyleColBandSize" minOccurs="0">
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
 *         &lt;element name="tblW" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TblWidth" minOccurs="0"/>
 *         &lt;element name="jc" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Jc" minOccurs="0"/>
 *         &lt;element name="tblCellSpacing" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TblWidth" minOccurs="0"/>
 *         &lt;element name="tblInd" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TblWidth" minOccurs="0"/>
 *         &lt;element name="tblBorders" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TblBorders" minOccurs="0"/>
 *         &lt;element name="shd" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Shd" minOccurs="0"/>
 *         &lt;element name="tblLayout" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TblLayoutType" minOccurs="0"/>
 *         &lt;element name="tblCellMar" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TblCellMar" minOccurs="0"/>
 *         &lt;element name="tblLook" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TblLook" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TblPrBase", propOrder = {
    "tblStyle",
    "tblpPr",
    "tblOverlap",
    "bidiVisual",
    "tblStyleRowBandSize",
    "tblStyleColBandSize",
    "tblW",
    "jc",
    "tblCellSpacing",
    "tblInd",
    "tblBorders",
    "shd",
    "tblLayout",
    "tblCellMar",
    "tblLook",
	"tblCaption"
})
public class CTTblPrBase implements Child
{

    protected CTTblPrBase.TblStyle tblStyle;
    protected CTTblPPr tblpPr;
    protected CTTblOverlap tblOverlap;
    protected BooleanDefaultTrue bidiVisual;
    protected CTTblPrBase.TblStyleRowBandSize tblStyleRowBandSize;
    protected CTTblPrBase.TblStyleColBandSize tblStyleColBandSize;
    protected TblWidth tblW;
    protected Jc jc;
    protected TblWidth tblCellSpacing;
    protected TblWidth tblInd;
    protected TblBorders tblBorders;
    protected CTShd shd;
    protected CTTblLayoutType tblLayout;
    protected CTTblCellMar tblCellMar;
    protected CTTblLook tblLook;
	protected CTString tblCaption;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the tblStyle property.
     * 
     * @return
     *     possible object is
     *     {@link CTTblPrBase.TblStyle }
     *     
     */
    public CTTblPrBase.TblStyle getTblStyle() {
        return tblStyle;
    }

    /**
     * Sets the value of the tblStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTblPrBase.TblStyle }
     *     
     */
    public void setTblStyle(CTTblPrBase.TblStyle value) {
        this.tblStyle = value;
    }

    /**
     * Gets the value of the tblpPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTTblPPr }
     *     
     */
    public CTTblPPr getTblpPr() {
        return tblpPr;
    }

    /**
     * Sets the value of the tblpPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTblPPr }
     *     
     */
    public void setTblpPr(CTTblPPr value) {
        this.tblpPr = value;
    }

    /**
     * Gets the value of the tblOverlap property.
     * 
     * @return
     *     possible object is
     *     {@link CTTblOverlap }
     *     
     */
    public CTTblOverlap getTblOverlap() {
        return tblOverlap;
    }

    /**
     * Sets the value of the tblOverlap property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTblOverlap }
     *     
     */
    public void setTblOverlap(CTTblOverlap value) {
        this.tblOverlap = value;
    }

    /**
     * Gets the value of the bidiVisual property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getBidiVisual() {
        return bidiVisual;
    }

    /**
     * Sets the value of the bidiVisual property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setBidiVisual(BooleanDefaultTrue value) {
        this.bidiVisual = value;
    }

    /**
     * Gets the value of the tblStyleRowBandSize property.
     * 
     * @return
     *     possible object is
     *     {@link CTTblPrBase.TblStyleRowBandSize }
     *     
     */
    public CTTblPrBase.TblStyleRowBandSize getTblStyleRowBandSize() {
        return tblStyleRowBandSize;
    }

    /**
     * Sets the value of the tblStyleRowBandSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTblPrBase.TblStyleRowBandSize }
     *     
     */
    public void setTblStyleRowBandSize(CTTblPrBase.TblStyleRowBandSize value) {
        this.tblStyleRowBandSize = value;
    }

    /**
     * Gets the value of the tblStyleColBandSize property.
     * 
     * @return
     *     possible object is
     *     {@link CTTblPrBase.TblStyleColBandSize }
     *     
     */
    public CTTblPrBase.TblStyleColBandSize getTblStyleColBandSize() {
        return tblStyleColBandSize;
    }

    /**
     * Sets the value of the tblStyleColBandSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTblPrBase.TblStyleColBandSize }
     *     
     */
    public void setTblStyleColBandSize(CTTblPrBase.TblStyleColBandSize value) {
        this.tblStyleColBandSize = value;
    }

    /**
     * Gets the value of the tblW property.
     * 
     * @return
     *     possible object is
     *     {@link TblWidth }
     *     
     */
    public TblWidth getTblW() {
        return tblW;
    }

    /**
     * Sets the value of the tblW property.
     * 
     * @param value
     *     allowed object is
     *     {@link TblWidth }
     *     
     */
    public void setTblW(TblWidth value) {
        this.tblW = value;
    }

    /**
     * Gets the value of the jc property.
     * 
     * @return
     *     possible object is
     *     {@link Jc }
     *     
     */
    public Jc getJc() {
        return jc;
    }

    /**
     * Sets the value of the jc property.
     * 
     * @param value
     *     allowed object is
     *     {@link Jc }
     *     
     */
    public void setJc(Jc value) {
        this.jc = value;
    }

    /**
     * Gets the value of the tblCellSpacing property.
     * 
     * @return
     *     possible object is
     *     {@link TblWidth }
     *     
     */
    public TblWidth getTblCellSpacing() {
        return tblCellSpacing;
    }

    /**
     * Sets the value of the tblCellSpacing property.
     * 
     * @param value
     *     allowed object is
     *     {@link TblWidth }
     *     
     */
    public void setTblCellSpacing(TblWidth value) {
        this.tblCellSpacing = value;
    }

    /**
     * Gets the value of the tblInd property.
     * 
     * @return
     *     possible object is
     *     {@link TblWidth }
     *     
     */
    public TblWidth getTblInd() {
        return tblInd;
    }

    /**
     * Sets the value of the tblInd property.
     * 
     * @param value
     *     allowed object is
     *     {@link TblWidth }
     *     
     */
    public void setTblInd(TblWidth value) {
        this.tblInd = value;
    }

    /**
     * Gets the value of the tblBorders property.
     * 
     * @return
     *     possible object is
     *     {@link TblBorders }
     *     
     */
    public TblBorders getTblBorders() {
        return tblBorders;
    }

    /**
     * Sets the value of the tblBorders property.
     * 
     * @param value
     *     allowed object is
     *     {@link TblBorders }
     *     
     */
    public void setTblBorders(TblBorders value) {
        this.tblBorders = value;
    }

    /**
     * Gets the value of the shd property.
     * 
     * @return
     *     possible object is
     *     {@link CTShd }
     *     
     */
    public CTShd getShd() {
        return shd;
    }

    /**
     * Sets the value of the shd property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShd }
     *     
     */
    public void setShd(CTShd value) {
        this.shd = value;
    }

    /**
     * Gets the value of the tblLayout property.
     * 
     * @return
     *     possible object is
     *     {@link CTTblLayoutType }
     *     
     */
    public CTTblLayoutType getTblLayout() {
        return tblLayout;
    }

    /**
     * Sets the value of the tblLayout property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTblLayoutType }
     *     
     */
    public void setTblLayout(CTTblLayoutType value) {
        this.tblLayout = value;
    }

    /**
     * Gets the value of the tblCellMar property.
     * 
     * @return
     *     possible object is
     *     {@link CTTblCellMar }
     *     
     */
    public CTTblCellMar getTblCellMar() {
        return tblCellMar;
    }

    /**
     * Sets the value of the tblCellMar property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTblCellMar }
     *     
     */
    public void setTblCellMar(CTTblCellMar value) {
        this.tblCellMar = value;
    }

    /**
     * Gets the value of the tblLook property.
     * 
     * @return
     *     possible object is
     *     {@link CTTblLook }
     *     
     */
    public CTTblLook getTblLook() {
        return tblLook;
    }

    /**
     * Sets the value of the tblLook property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTblLook }
     *     
     */
    public void setTblLook(CTTblLook value) {
        this.tblLook = value;
    }
	
	/**
	 * Gets the value of the tblCaption property.
	 *
	 * @return
	 *     possible object is
     *     {@link CTString }
     *     
     */
	public CTString getTblCaption() {
		return tblCaption;
	}
	
	/**
     * Sets the value of the tblCaption property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTString }
     *     
     */
    public void setTblCaption(CTString value) {
        this.tblCaption = value;
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
    public static class TblStyle implements Child
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
    public static class TblStyleColBandSize implements Child
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
    public static class TblStyleRowBandSize implements Child
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
