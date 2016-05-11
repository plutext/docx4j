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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TcPrInner complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TcPrInner">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cnfStyle" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Cnf" minOccurs="0"/>
 *         &lt;element name="tcW" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TblWidth" minOccurs="0"/>
 *         &lt;element name="gridSpan" minOccurs="0">
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
 *         &lt;element name="hMerge" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="val">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                       &lt;enumeration value="continue"/>
 *                       &lt;enumeration value="restart"/>
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="vMerge" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="val">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                       &lt;enumeration value="continue"/>
 *                       &lt;enumeration value="restart"/>
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="tcBorders" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="top" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
 *                   &lt;element name="left" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
 *                   &lt;element name="bottom" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
 *                   &lt;element name="right" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
 *                   &lt;element name="insideH" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
 *                   &lt;element name="insideV" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
 *                   &lt;element name="tl2br" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
 *                   &lt;element name="tr2bl" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="shd" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Shd" minOccurs="0"/>
 *         &lt;element name="noWrap" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="tcMar" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TcMar" minOccurs="0"/>
 *         &lt;element name="textDirection" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TextDirection" minOccurs="0"/>
 *         &lt;element name="tcFitText" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="vAlign" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_VerticalJc" minOccurs="0"/>
 *         &lt;element name="hideMark" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_CellMarkupElements" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TcPrInner", propOrder = {
    "cnfStyle",
    "tcW",
    "gridSpan",
    "hMerge",
    "vMerge",
    "tcBorders",
    "shd",
    "noWrap",
    "tcMar",
    "textDirection",
    "tcFitText",
    "vAlign",
    "hideMark",
    "cellIns",
    "cellDel",
    "cellMerge"
})
public class TcPrInner implements Child
{

    protected CTCnf cnfStyle;
    protected TblWidth tcW;
    protected TcPrInner.GridSpan gridSpan;
    protected TcPrInner.HMerge hMerge;
    protected TcPrInner.VMerge vMerge;
    protected TcPrInner.TcBorders tcBorders;
    protected CTShd shd;
    protected BooleanDefaultTrue noWrap;
    protected TcMar tcMar;
    protected TextDirection textDirection;
    protected BooleanDefaultTrue tcFitText;
    protected CTVerticalJc vAlign;
    protected BooleanDefaultTrue hideMark;
    protected CTTrackChange cellIns;
    protected CTTrackChange cellDel;
    protected CTCellMergeTrackChange cellMerge;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the cnfStyle property.
     * 
     * @return
     *     possible object is
     *     {@link CTCnf }
     *     
     */
    public CTCnf getCnfStyle() {
        return cnfStyle;
    }

    /**
     * Sets the value of the cnfStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCnf }
     *     
     */
    public void setCnfStyle(CTCnf value) {
        this.cnfStyle = value;
    }

    /**
     * Gets the value of the tcW property.
     * 
     * @return
     *     possible object is
     *     {@link TblWidth }
     *     
     */
    public TblWidth getTcW() {
        return tcW;
    }

    /**
     * Sets the value of the tcW property.
     * 
     * @param value
     *     allowed object is
     *     {@link TblWidth }
     *     
     */
    public void setTcW(TblWidth value) {
        this.tcW = value;
    }

    /**
     * Gets the value of the gridSpan property.
     * 
     * @return
     *     possible object is
     *     {@link TcPrInner.GridSpan }
     *     
     */
    public TcPrInner.GridSpan getGridSpan() {
        return gridSpan;
    }

    /**
     * Sets the value of the gridSpan property.
     * 
     * @param value
     *     allowed object is
     *     {@link TcPrInner.GridSpan }
     *     
     */
    public void setGridSpan(TcPrInner.GridSpan value) {
        this.gridSpan = value;
    }

    /**
     * Gets the value of the hMerge property.
     * 
     * @return
     *     possible object is
     *     {@link TcPrInner.HMerge }
     *     
     */
    public TcPrInner.HMerge getHMerge() {
        return hMerge;
    }

    /**
     * Sets the value of the hMerge property.
     * 
     * @param value
     *     allowed object is
     *     {@link TcPrInner.HMerge }
     *     
     */
    public void setHMerge(TcPrInner.HMerge value) {
        this.hMerge = value;
    }

    /**
     * Gets the value of the vMerge property.
     * 
     * @return
     *     possible object is
     *     {@link TcPrInner.VMerge }
     *     
     */
    public TcPrInner.VMerge getVMerge() {
        return vMerge;
    }

    /**
     * Sets the value of the vMerge property.
     * 
     * @param value
     *     allowed object is
     *     {@link TcPrInner.VMerge }
     *     
     */
    public void setVMerge(TcPrInner.VMerge value) {
        this.vMerge = value;
    }

    /**
     * Gets the value of the tcBorders property.
     * 
     * @return
     *     possible object is
     *     {@link TcPrInner.TcBorders }
     *     
     */
    public TcPrInner.TcBorders getTcBorders() {
        return tcBorders;
    }

    /**
     * Sets the value of the tcBorders property.
     * 
     * @param value
     *     allowed object is
     *     {@link TcPrInner.TcBorders }
     *     
     */
    public void setTcBorders(TcPrInner.TcBorders value) {
        this.tcBorders = value;
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
     * Gets the value of the noWrap property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getNoWrap() {
        return noWrap;
    }

    /**
     * Sets the value of the noWrap property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setNoWrap(BooleanDefaultTrue value) {
        this.noWrap = value;
    }

    /**
     * Gets the value of the tcMar property.
     * 
     * @return
     *     possible object is
     *     {@link TcMar }
     *     
     */
    public TcMar getTcMar() {
        return tcMar;
    }

    /**
     * Sets the value of the tcMar property.
     * 
     * @param value
     *     allowed object is
     *     {@link TcMar }
     *     
     */
    public void setTcMar(TcMar value) {
        this.tcMar = value;
    }

    /**
     * Gets the value of the textDirection property.
     * 
     * @return
     *     possible object is
     *     {@link TextDirection }
     *     
     */
    public TextDirection getTextDirection() {
        return textDirection;
    }

    /**
     * Sets the value of the textDirection property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextDirection }
     *     
     */
    public void setTextDirection(TextDirection value) {
        this.textDirection = value;
    }

    /**
     * Gets the value of the tcFitText property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getTcFitText() {
        return tcFitText;
    }

    /**
     * Sets the value of the tcFitText property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setTcFitText(BooleanDefaultTrue value) {
        this.tcFitText = value;
    }

    /**
     * Gets the value of the vAlign property.
     * 
     * @return
     *     possible object is
     *     {@link CTVerticalJc }
     *     
     */
    public CTVerticalJc getVAlign() {
        return vAlign;
    }

    /**
     * Sets the value of the vAlign property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTVerticalJc }
     *     
     */
    public void setVAlign(CTVerticalJc value) {
        this.vAlign = value;
    }

    /**
     * Gets the value of the hideMark property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getHideMark() {
        return hideMark;
    }

    /**
     * Sets the value of the hideMark property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setHideMark(BooleanDefaultTrue value) {
        this.hideMark = value;
    }

    /**
     * Gets the value of the cellIns property.
     * 
     * @return
     *     possible object is
     *     {@link CTTrackChange }
     *     
     */
    public CTTrackChange getCellIns() {
        return cellIns;
    }

    /**
     * Sets the value of the cellIns property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTrackChange }
     *     
     */
    public void setCellIns(CTTrackChange value) {
        this.cellIns = value;
    }

    /**
     * Gets the value of the cellDel property.
     * 
     * @return
     *     possible object is
     *     {@link CTTrackChange }
     *     
     */
    public CTTrackChange getCellDel() {
        return cellDel;
    }

    /**
     * Sets the value of the cellDel property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTrackChange }
     *     
     */
    public void setCellDel(CTTrackChange value) {
        this.cellDel = value;
    }

    /**
     * Gets the value of the cellMerge property.
     * 
     * @return
     *     possible object is
     *     {@link CTCellMergeTrackChange }
     *     
     */
    public CTCellMergeTrackChange getCellMerge() {
        return cellMerge;
    }

    /**
     * Sets the value of the cellMerge property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCellMergeTrackChange }
     *     
     */
    public void setCellMerge(CTCellMergeTrackChange value) {
        this.cellMerge = value;
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
    public static class GridSpan implements Child
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
     *       &lt;attribute name="val">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *             &lt;enumeration value="continue"/>
     *             &lt;enumeration value="restart"/>
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
    public static class HMerge implements Child
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
     *       &lt;sequence>
     *         &lt;element name="top" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
     *         &lt;element name="left" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
     *         &lt;element name="bottom" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
     *         &lt;element name="right" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
     *         &lt;element name="insideH" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
     *         &lt;element name="insideV" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
     *         &lt;element name="tl2br" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
     *         &lt;element name="tr2bl" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
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
        "top",
        "left",
        "bottom",
        "right",
        "insideH",
        "insideV",
        "tl2Br",
        "tr2Bl"
    })
    public static class TcBorders implements Child
    {

        protected CTBorder top;
        protected CTBorder left;
        protected CTBorder bottom;
        protected CTBorder right;
        protected CTBorder insideH;
        protected CTBorder insideV;
        @XmlElement(name = "tl2br")
        protected CTBorder tl2Br;
        @XmlElement(name = "tr2bl")
        protected CTBorder tr2Bl;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the top property.
         * 
         * @return
         *     possible object is
         *     {@link CTBorder }
         *     
         */
        public CTBorder getTop() {
            return top;
        }

        /**
         * Sets the value of the top property.
         * 
         * @param value
         *     allowed object is
         *     {@link CTBorder }
         *     
         */
        public void setTop(CTBorder value) {
            this.top = value;
        }

        /**
         * Gets the value of the left property.
         * 
         * @return
         *     possible object is
         *     {@link CTBorder }
         *     
         */
        public CTBorder getLeft() {
            return left;
        }

        /**
         * Sets the value of the left property.
         * 
         * @param value
         *     allowed object is
         *     {@link CTBorder }
         *     
         */
        public void setLeft(CTBorder value) {
            this.left = value;
        }

        /**
         * Gets the value of the bottom property.
         * 
         * @return
         *     possible object is
         *     {@link CTBorder }
         *     
         */
        public CTBorder getBottom() {
            return bottom;
        }

        /**
         * Sets the value of the bottom property.
         * 
         * @param value
         *     allowed object is
         *     {@link CTBorder }
         *     
         */
        public void setBottom(CTBorder value) {
            this.bottom = value;
        }

        /**
         * Gets the value of the right property.
         * 
         * @return
         *     possible object is
         *     {@link CTBorder }
         *     
         */
        public CTBorder getRight() {
            return right;
        }

        /**
         * Sets the value of the right property.
         * 
         * @param value
         *     allowed object is
         *     {@link CTBorder }
         *     
         */
        public void setRight(CTBorder value) {
            this.right = value;
        }

        /**
         * Gets the value of the insideH property.
         * 
         * @return
         *     possible object is
         *     {@link CTBorder }
         *     
         */
        public CTBorder getInsideH() {
            return insideH;
        }

        /**
         * Sets the value of the insideH property.
         * 
         * @param value
         *     allowed object is
         *     {@link CTBorder }
         *     
         */
        public void setInsideH(CTBorder value) {
            this.insideH = value;
        }

        /**
         * Gets the value of the insideV property.
         * 
         * @return
         *     possible object is
         *     {@link CTBorder }
         *     
         */
        public CTBorder getInsideV() {
            return insideV;
        }

        /**
         * Sets the value of the insideV property.
         * 
         * @param value
         *     allowed object is
         *     {@link CTBorder }
         *     
         */
        public void setInsideV(CTBorder value) {
            this.insideV = value;
        }

        /**
         * Gets the value of the tl2Br property.
         * 
         * @return
         *     possible object is
         *     {@link CTBorder }
         *     
         */
        public CTBorder getTl2Br() {
            return tl2Br;
        }

        /**
         * Sets the value of the tl2Br property.
         * 
         * @param value
         *     allowed object is
         *     {@link CTBorder }
         *     
         */
        public void setTl2Br(CTBorder value) {
            this.tl2Br = value;
        }

        /**
         * Gets the value of the tr2Bl property.
         * 
         * @return
         *     possible object is
         *     {@link CTBorder }
         *     
         */
        public CTBorder getTr2Bl() {
            return tr2Bl;
        }

        /**
         * Sets the value of the tr2Bl property.
         * 
         * @param value
         *     allowed object is
         *     {@link CTBorder }
         *     
         */
        public void setTr2Bl(CTBorder value) {
            this.tr2Bl = value;
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
     *       &lt;attribute name="val">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *             &lt;enumeration value="continue"/>
     *             &lt;enumeration value="restart"/>
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
    public static class VMerge implements Child
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
