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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PPrBase complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PPrBase">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pStyle" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="keepNext" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="keepLines" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="pageBreakBefore" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="framePr" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FramePr" minOccurs="0"/>
 *         &lt;element name="widowControl" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="numPr" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="ilvl" minOccurs="0">
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
 *                   &lt;element name="numId" minOccurs="0">
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
 *                   &lt;element name="numberingChange" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TrackChangeNumbering" minOccurs="0"/>
 *                   &lt;element name="ins" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TrackChange" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="suppressLineNumbers" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="pBdr" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="top" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
 *                   &lt;element name="left" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
 *                   &lt;element name="bottom" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
 *                   &lt;element name="right" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
 *                   &lt;element name="between" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
 *                   &lt;element name="bar" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="shd" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Shd" minOccurs="0"/>
 *         &lt;element name="tabs" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Tabs" minOccurs="0"/>
 *         &lt;element name="suppressAutoHyphens" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="kinsoku" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="wordWrap" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="overflowPunct" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="topLinePunct" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="autoSpaceDE" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="autoSpaceDN" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="bidi" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="adjustRightInd" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="snapToGrid" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="spacing" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="before" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
 *                 &lt;attribute name="beforeLines" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *                 &lt;attribute name="beforeAutospacing" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *                 &lt;attribute name="after" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
 *                 &lt;attribute name="afterLines" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *                 &lt;attribute name="afterAutospacing" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *                 &lt;attribute name="line" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_SignedTwipsMeasure" />
 *                 &lt;attribute name="lineRule" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_LineSpacingRule" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="ind" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="left" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_SignedTwipsMeasure" />
 *                 &lt;attribute name="leftChars" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *                 &lt;attribute name="right" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_SignedTwipsMeasure" />
 *                 &lt;attribute name="rightChars" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *                 &lt;attribute name="hanging" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
 *                 &lt;attribute name="hangingChars" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *                 &lt;attribute name="firstLine" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
 *                 &lt;attribute name="firstLineChars" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="contextualSpacing" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="mirrorIndents" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="suppressOverlap" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="jc" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Jc" minOccurs="0"/>
 *         &lt;element name="textDirection" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TextDirection" minOccurs="0"/>
 *         &lt;element name="textAlignment" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="val" use="required">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                       &lt;enumeration value="top"/>
 *                       &lt;enumeration value="center"/>
 *                       &lt;enumeration value="baseline"/>
 *                       &lt;enumeration value="bottom"/>
 *                       &lt;enumeration value="auto"/>
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="textboxTightWrap" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TextboxTightWrap" minOccurs="0"/>
 *         &lt;element name="outlineLvl" minOccurs="0">
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
 *         &lt;element name="cnfStyle" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Cnf" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.microsoft.com/office/word/2012/wordml}collapsed"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PPrBase", propOrder = {
    "pStyle",
    "keepNext",
    "keepLines",
    "pageBreakBefore",
    "framePr",
    "widowControl",
    "numPr",
    "suppressLineNumbers",
    "pBdr",
    "shd",
    "tabs",
    "suppressAutoHyphens",
    "kinsoku",
    "wordWrap",
    "overflowPunct",
    "topLinePunct",
    "autoSpaceDE",
    "autoSpaceDN",
    "bidi",
    "adjustRightInd",
    "snapToGrid",
    "spacing",
    "ind",
    "contextualSpacing",
    "mirrorIndents",
    "suppressOverlap",
    "jc",
    "textDirection",
    "textAlignment",
    "textboxTightWrap",
    "outlineLvl",
    "divId",
    "cnfStyle",
    "collapsed"
})
public class PPrBase implements Child
{

    protected PPrBase.PStyle pStyle;
    protected BooleanDefaultTrue keepNext;
    protected BooleanDefaultTrue keepLines;
    protected BooleanDefaultTrue pageBreakBefore;
    protected CTFramePr framePr;
    protected BooleanDefaultTrue widowControl;
    protected PPrBase.NumPr numPr;
    protected BooleanDefaultTrue suppressLineNumbers;
    protected PPrBase.PBdr pBdr;
    protected CTShd shd;
    protected Tabs tabs;
    protected BooleanDefaultTrue suppressAutoHyphens;
    protected BooleanDefaultTrue kinsoku;
    protected BooleanDefaultTrue wordWrap;
    protected BooleanDefaultTrue overflowPunct;
    protected BooleanDefaultTrue topLinePunct;
    protected BooleanDefaultTrue autoSpaceDE;
    protected BooleanDefaultTrue autoSpaceDN;
    protected BooleanDefaultTrue bidi;
    protected BooleanDefaultTrue adjustRightInd;
    protected BooleanDefaultTrue snapToGrid;
    protected PPrBase.Spacing spacing;
    protected PPrBase.Ind ind;
    protected BooleanDefaultTrue contextualSpacing;
    protected BooleanDefaultTrue mirrorIndents;
    protected BooleanDefaultTrue suppressOverlap;
    protected Jc jc;
    protected TextDirection textDirection;
    protected PPrBase.TextAlignment textAlignment;
    protected CTTextboxTightWrap textboxTightWrap;
    protected PPrBase.OutlineLvl outlineLvl;
    protected PPrBase.DivId divId;
    protected CTCnf cnfStyle;
    @XmlElement(namespace = "http://schemas.microsoft.com/office/word/2012/wordml", required = false)
    protected BooleanDefaultTrue collapsed;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the pStyle property.
     * 
     * @return
     *     possible object is
     *     {@link PPrBase.PStyle }
     *     
     */
    public PPrBase.PStyle getPStyle() {
        return pStyle;
    }

    /**
     * Sets the value of the pStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link PPrBase.PStyle }
     *     
     */
    public void setPStyle(PPrBase.PStyle value) {
        this.pStyle = value;
    }

    /**
     * Gets the value of the keepNext property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getKeepNext() {
        return keepNext;
    }

    /**
     * Sets the value of the keepNext property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setKeepNext(BooleanDefaultTrue value) {
        this.keepNext = value;
    }

    /**
     * Gets the value of the keepLines property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getKeepLines() {
        return keepLines;
    }

    /**
     * Sets the value of the keepLines property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setKeepLines(BooleanDefaultTrue value) {
        this.keepLines = value;
    }

    /**
     * Gets the value of the pageBreakBefore property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getPageBreakBefore() {
        return pageBreakBefore;
    }

    /**
     * Sets the value of the pageBreakBefore property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setPageBreakBefore(BooleanDefaultTrue value) {
        this.pageBreakBefore = value;
    }

    /**
     * Gets the value of the framePr property.
     * 
     * @return
     *     possible object is
     *     {@link CTFramePr }
     *     
     */
    public CTFramePr getFramePr() {
        return framePr;
    }

    /**
     * Sets the value of the framePr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFramePr }
     *     
     */
    public void setFramePr(CTFramePr value) {
        this.framePr = value;
    }

    /**
     * Gets the value of the widowControl property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getWidowControl() {
        return widowControl;
    }

    /**
     * Sets the value of the widowControl property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setWidowControl(BooleanDefaultTrue value) {
        this.widowControl = value;
    }

    /**
     * Gets the value of the numPr property.
     * 
     * @return
     *     possible object is
     *     {@link PPrBase.NumPr }
     *     
     */
    public PPrBase.NumPr getNumPr() {
        return numPr;
    }

    /**
     * Sets the value of the numPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link PPrBase.NumPr }
     *     
     */
    public void setNumPr(PPrBase.NumPr value) {
        this.numPr = value;
    }

    /**
     * Gets the value of the suppressLineNumbers property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSuppressLineNumbers() {
        return suppressLineNumbers;
    }

    /**
     * Sets the value of the suppressLineNumbers property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSuppressLineNumbers(BooleanDefaultTrue value) {
        this.suppressLineNumbers = value;
    }

    /**
     * Gets the value of the pBdr property.
     * 
     * @return
     *     possible object is
     *     {@link PPrBase.PBdr }
     *     
     */
    public PPrBase.PBdr getPBdr() {
        return pBdr;
    }

    /**
     * Sets the value of the pBdr property.
     * 
     * @param value
     *     allowed object is
     *     {@link PPrBase.PBdr }
     *     
     */
    public void setPBdr(PPrBase.PBdr value) {
        this.pBdr = value;
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
     * Gets the value of the tabs property.
     * 
     * @return
     *     possible object is
     *     {@link Tabs }
     *     
     */
    public Tabs getTabs() {
        return tabs;
    }

    /**
     * Sets the value of the tabs property.
     * 
     * @param value
     *     allowed object is
     *     {@link Tabs }
     *     
     */
    public void setTabs(Tabs value) {
        this.tabs = value;
    }

    /**
     * Gets the value of the suppressAutoHyphens property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSuppressAutoHyphens() {
        return suppressAutoHyphens;
    }

    /**
     * Sets the value of the suppressAutoHyphens property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSuppressAutoHyphens(BooleanDefaultTrue value) {
        this.suppressAutoHyphens = value;
    }

    /**
     * Gets the value of the kinsoku property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getKinsoku() {
        return kinsoku;
    }

    /**
     * Sets the value of the kinsoku property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setKinsoku(BooleanDefaultTrue value) {
        this.kinsoku = value;
    }

    /**
     * Gets the value of the wordWrap property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getWordWrap() {
        return wordWrap;
    }

    /**
     * Sets the value of the wordWrap property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setWordWrap(BooleanDefaultTrue value) {
        this.wordWrap = value;
    }

    /**
     * Gets the value of the overflowPunct property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getOverflowPunct() {
        return overflowPunct;
    }

    /**
     * Sets the value of the overflowPunct property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setOverflowPunct(BooleanDefaultTrue value) {
        this.overflowPunct = value;
    }

    /**
     * Gets the value of the topLinePunct property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getTopLinePunct() {
        return topLinePunct;
    }

    /**
     * Sets the value of the topLinePunct property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setTopLinePunct(BooleanDefaultTrue value) {
        this.topLinePunct = value;
    }

    /**
     * Gets the value of the autoSpaceDE property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getAutoSpaceDE() {
        return autoSpaceDE;
    }

    /**
     * Sets the value of the autoSpaceDE property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setAutoSpaceDE(BooleanDefaultTrue value) {
        this.autoSpaceDE = value;
    }

    /**
     * Gets the value of the autoSpaceDN property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getAutoSpaceDN() {
        return autoSpaceDN;
    }

    /**
     * Sets the value of the autoSpaceDN property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setAutoSpaceDN(BooleanDefaultTrue value) {
        this.autoSpaceDN = value;
    }

    /**
     * Gets the value of the bidi property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getBidi() {
        return bidi;
    }

    /**
     * Sets the value of the bidi property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setBidi(BooleanDefaultTrue value) {
        this.bidi = value;
    }

    /**
     * Gets the value of the adjustRightInd property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getAdjustRightInd() {
        return adjustRightInd;
    }

    /**
     * Sets the value of the adjustRightInd property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setAdjustRightInd(BooleanDefaultTrue value) {
        this.adjustRightInd = value;
    }

    /**
     * Gets the value of the snapToGrid property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSnapToGrid() {
        return snapToGrid;
    }

    /**
     * Sets the value of the snapToGrid property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSnapToGrid(BooleanDefaultTrue value) {
        this.snapToGrid = value;
    }

    /**
     * Gets the value of the spacing property.
     * 
     * @return
     *     possible object is
     *     {@link PPrBase.Spacing }
     *     
     */
    public PPrBase.Spacing getSpacing() {
        return spacing;
    }

    /**
     * Sets the value of the spacing property.
     * 
     * @param value
     *     allowed object is
     *     {@link PPrBase.Spacing }
     *     
     */
    public void setSpacing(PPrBase.Spacing value) {
        this.spacing = value;
    }

    /**
     * Gets the value of the ind property.
     * 
     * @return
     *     possible object is
     *     {@link PPrBase.Ind }
     *     
     */
    public PPrBase.Ind getInd() {
        return ind;
    }

    /**
     * Sets the value of the ind property.
     * 
     * @param value
     *     allowed object is
     *     {@link PPrBase.Ind }
     *     
     */
    public void setInd(PPrBase.Ind value) {
        this.ind = value;
    }

    /**
     * Gets the value of the contextualSpacing property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getContextualSpacing() {
        return contextualSpacing;
    }

    /**
     * Sets the value of the contextualSpacing property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setContextualSpacing(BooleanDefaultTrue value) {
        this.contextualSpacing = value;
    }

    /**
     * Gets the value of the mirrorIndents property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getMirrorIndents() {
        return mirrorIndents;
    }

    /**
     * Sets the value of the mirrorIndents property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setMirrorIndents(BooleanDefaultTrue value) {
        this.mirrorIndents = value;
    }

    /**
     * Gets the value of the suppressOverlap property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSuppressOverlap() {
        return suppressOverlap;
    }

    /**
     * Sets the value of the suppressOverlap property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSuppressOverlap(BooleanDefaultTrue value) {
        this.suppressOverlap = value;
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
     * Gets the value of the textAlignment property.
     * 
     * @return
     *     possible object is
     *     {@link PPrBase.TextAlignment }
     *     
     */
    public PPrBase.TextAlignment getTextAlignment() {
        return textAlignment;
    }

    /**
     * Sets the value of the textAlignment property.
     * 
     * @param value
     *     allowed object is
     *     {@link PPrBase.TextAlignment }
     *     
     */
    public void setTextAlignment(PPrBase.TextAlignment value) {
        this.textAlignment = value;
    }

    /**
     * Gets the value of the textboxTightWrap property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextboxTightWrap }
     *     
     */
    public CTTextboxTightWrap getTextboxTightWrap() {
        return textboxTightWrap;
    }

    /**
     * Sets the value of the textboxTightWrap property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextboxTightWrap }
     *     
     */
    public void setTextboxTightWrap(CTTextboxTightWrap value) {
        this.textboxTightWrap = value;
    }

    /**
     * Gets the value of the outlineLvl property.
     * 
     * @return
     *     possible object is
     *     {@link PPrBase.OutlineLvl }
     *     
     */
    public PPrBase.OutlineLvl getOutlineLvl() {
        return outlineLvl;
    }

    /**
     * Sets the value of the outlineLvl property.
     * 
     * @param value
     *     allowed object is
     *     {@link PPrBase.OutlineLvl }
     *     
     */
    public void setOutlineLvl(PPrBase.OutlineLvl value) {
        this.outlineLvl = value;
    }

    /**
     * Gets the value of the divId property.
     * 
     * @return
     *     possible object is
     *     {@link PPrBase.DivId }
     *     
     */
    public PPrBase.DivId getDivId() {
        return divId;
    }

    /**
     * Sets the value of the divId property.
     * 
     * @param value
     *     allowed object is
     *     {@link PPrBase.DivId }
     *     
     */
    public void setDivId(PPrBase.DivId value) {
        this.divId = value;
    }

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
     * Gets the value of the collapsed property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getCollapsed() {
        return collapsed;
    }

    /**
     * Sets the value of the collapsed property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setCollapsed(BooleanDefaultTrue value) {
        this.collapsed = value;
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
     *       &lt;attribute name="left" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_SignedTwipsMeasure" />
     *       &lt;attribute name="leftChars" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
     *       &lt;attribute name="right" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_SignedTwipsMeasure" />
     *       &lt;attribute name="rightChars" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
     *       &lt;attribute name="hanging" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
     *       &lt;attribute name="hangingChars" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
     *       &lt;attribute name="firstLine" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
     *       &lt;attribute name="firstLineChars" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @XmlRootElement(name = "ind")        
    public static class Ind
        implements Child
    {

        @XmlAttribute(name = "left", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected BigInteger left;
        @XmlAttribute(name = "leftChars", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected BigInteger leftChars;
        @XmlAttribute(name = "right", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected BigInteger right;
        @XmlAttribute(name = "rightChars", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected BigInteger rightChars;
        @XmlAttribute(name = "hanging", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected BigInteger hanging;
        @XmlAttribute(name = "hangingChars", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected BigInteger hangingChars;
        @XmlAttribute(name = "firstLine", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected BigInteger firstLine;
        @XmlAttribute(name = "firstLineChars", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected BigInteger firstLineChars;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the left property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getLeft() {
            return left;
        }

        /**
         * Sets the value of the left property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setLeft(BigInteger value) {
            this.left = value;
        }

        /**
         * Gets the value of the leftChars property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getLeftChars() {
            return leftChars;
        }

        /**
         * Sets the value of the leftChars property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setLeftChars(BigInteger value) {
            this.leftChars = value;
        }

        /**
         * Gets the value of the right property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getRight() {
            return right;
        }

        /**
         * Sets the value of the right property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setRight(BigInteger value) {
            this.right = value;
        }

        /**
         * Gets the value of the rightChars property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getRightChars() {
            return rightChars;
        }

        /**
         * Sets the value of the rightChars property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setRightChars(BigInteger value) {
            this.rightChars = value;
        }

        /**
         * Gets the value of the hanging property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getHanging() {
            return hanging;
        }

        /**
         * Sets the value of the hanging property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setHanging(BigInteger value) {
            this.hanging = value;
        }

        /**
         * Gets the value of the hangingChars property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getHangingChars() {
            return hangingChars;
        }

        /**
         * Sets the value of the hangingChars property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setHangingChars(BigInteger value) {
            this.hangingChars = value;
        }

        /**
         * Gets the value of the firstLine property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getFirstLine() {
            return firstLine;
        }

        /**
         * Sets the value of the firstLine property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setFirstLine(BigInteger value) {
            this.firstLine = value;
        }

        /**
         * Gets the value of the firstLineChars property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getFirstLineChars() {
            return firstLineChars;
        }

        /**
         * Sets the value of the firstLineChars property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setFirstLineChars(BigInteger value) {
            this.firstLineChars = value;
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
     *         &lt;element name="ilvl" minOccurs="0">
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
     *         &lt;element name="numId" minOccurs="0">
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
     *         &lt;element name="numberingChange" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TrackChangeNumbering" minOccurs="0"/>
     *         &lt;element name="ins" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TrackChange" minOccurs="0"/>
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
        "ilvl",
        "numId",
        "numberingChange",
        "ins"
    })
    @XmlRootElement(name = "numPr")    
    public static class NumPr
        implements Child
    {

        protected PPrBase.NumPr.Ilvl ilvl;
        protected PPrBase.NumPr.NumId numId;
        protected CTTrackChangeNumbering numberingChange;
        protected CTTrackChange ins;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the ilvl property.
         * 
         * @return
         *     possible object is
         *     {@link PPrBase.NumPr.Ilvl }
         *     
         */
        public PPrBase.NumPr.Ilvl getIlvl() {
            return ilvl;
        }

        /**
         * Sets the value of the ilvl property.
         * 
         * @param value
         *     allowed object is
         *     {@link PPrBase.NumPr.Ilvl }
         *     
         */
        public void setIlvl(PPrBase.NumPr.Ilvl value) {
            this.ilvl = value;
        }

        /**
         * Gets the value of the numId property.
         * 
         * @return
         *     possible object is
         *     {@link PPrBase.NumPr.NumId }
         *     
         */
        public PPrBase.NumPr.NumId getNumId() {
            return numId;
        }

        /**
         * Sets the value of the numId property.
         * 
         * @param value
         *     allowed object is
         *     {@link PPrBase.NumPr.NumId }
         *     
         */
        public void setNumId(PPrBase.NumPr.NumId value) {
            this.numId = value;
        }

        /**
         * Gets the value of the numberingChange property.
         * 
         * @return
         *     possible object is
         *     {@link CTTrackChangeNumbering }
         *     
         */
        public CTTrackChangeNumbering getNumberingChange() {
            return numberingChange;
        }

        /**
         * Sets the value of the numberingChange property.
         * 
         * @param value
         *     allowed object is
         *     {@link CTTrackChangeNumbering }
         *     
         */
        public void setNumberingChange(CTTrackChangeNumbering value) {
            this.numberingChange = value;
        }

        /**
         * Gets the value of the ins property.
         * 
         * @return
         *     possible object is
         *     {@link CTTrackChange }
         *     
         */
        public CTTrackChange getIns() {
            return ins;
        }

        /**
         * Sets the value of the ins property.
         * 
         * @param value
         *     allowed object is
         *     {@link CTTrackChange }
         *     
         */
        public void setIns(CTTrackChange value) {
            this.ins = value;
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
        public static class Ilvl implements Child
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
        public static class NumId implements Child
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
    @XmlRootElement(name = "outlineLvl")        
    public static class OutlineLvl
        implements Child
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
     *         &lt;element name="top" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
     *         &lt;element name="left" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
     *         &lt;element name="bottom" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
     *         &lt;element name="right" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
     *         &lt;element name="between" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
     *         &lt;element name="bar" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
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
        "between",
        "bar"
    })
    @XmlRootElement(name = "pBdr")        
    public static class PBdr
        implements Child
    {

        protected CTBorder top;
        protected CTBorder left;
        protected CTBorder bottom;
        protected CTBorder right;
        protected CTBorder between;
        protected CTBorder bar;
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
         * Gets the value of the between property.
         * 
         * @return
         *     possible object is
         *     {@link CTBorder }
         *     
         */
        public CTBorder getBetween() {
            return between;
        }

        /**
         * Sets the value of the between property.
         * 
         * @param value
         *     allowed object is
         *     {@link CTBorder }
         *     
         */
        public void setBetween(CTBorder value) {
            this.between = value;
        }

        /**
         * Gets the value of the bar property.
         * 
         * @return
         *     possible object is
         *     {@link CTBorder }
         *     
         */
        public CTBorder getBar() {
            return bar;
        }

        /**
         * Sets the value of the bar property.
         * 
         * @param value
         *     allowed object is
         *     {@link CTBorder }
         *     
         */
        public void setBar(CTBorder value) {
            this.bar = value;
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
    public static class PStyle implements Child
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
     *       &lt;attribute name="before" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
     *       &lt;attribute name="beforeLines" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
     *       &lt;attribute name="beforeAutospacing" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
     *       &lt;attribute name="after" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
     *       &lt;attribute name="afterLines" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
     *       &lt;attribute name="afterAutospacing" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
     *       &lt;attribute name="line" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_SignedTwipsMeasure" />
     *       &lt;attribute name="lineRule" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_LineSpacingRule" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @XmlRootElement(name = "spacing")        
    public static class Spacing
        implements Child
    {

        @XmlAttribute(name = "before", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected BigInteger before;
        @XmlAttribute(name = "beforeLines", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected BigInteger beforeLines;
        @XmlAttribute(name = "beforeAutospacing", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected Boolean beforeAutospacing;
        @XmlAttribute(name = "after", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected BigInteger after;
        @XmlAttribute(name = "afterLines", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected BigInteger afterLines;
        @XmlAttribute(name = "afterAutospacing", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected Boolean afterAutospacing;
        @XmlAttribute(name = "line", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected BigInteger line;
        @XmlAttribute(name = "lineRule", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected STLineSpacingRule lineRule;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the before property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getBefore() {
            return before;
        }

        /**
         * Sets the value of the before property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setBefore(BigInteger value) {
            this.before = value;
        }

        /**
         * Gets the value of the beforeLines property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getBeforeLines() {
            return beforeLines;
        }

        /**
         * Sets the value of the beforeLines property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setBeforeLines(BigInteger value) {
            this.beforeLines = value;
        }

        /**
         * Gets the value of the beforeAutospacing property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public boolean isBeforeAutospacing() {
            if (beforeAutospacing == null) {
                return false;
            } else {
                return beforeAutospacing;
            }
        }

        /**
         * Sets the value of the beforeAutospacing property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setBeforeAutospacing(Boolean value) {
            this.beforeAutospacing = value;
        }

        /**
         * Gets the value of the after property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getAfter() {
            return after;
        }

        /**
         * Sets the value of the after property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setAfter(BigInteger value) {
            this.after = value;
        }

        /**
         * Gets the value of the afterLines property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getAfterLines() {
            return afterLines;
        }

        /**
         * Sets the value of the afterLines property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setAfterLines(BigInteger value) {
            this.afterLines = value;
        }

        /**
         * Gets the value of the afterAutospacing property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public boolean isAfterAutospacing() {
            if (afterAutospacing == null) {
                return false;
            } else {
                return afterAutospacing;
            }
        }

        /**
         * Sets the value of the afterAutospacing property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setAfterAutospacing(Boolean value) {
            this.afterAutospacing = value;
        }

        /**
         * Gets the value of the line property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getLine() {
            return line;
        }

        /**
         * Sets the value of the line property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setLine(BigInteger value) {
            this.line = value;
        }

        /**
         * Gets the value of the lineRule property.
         * 
         * @return
         *     possible object is
         *     {@link STLineSpacingRule }
         *     
         */
        public STLineSpacingRule getLineRule() {
            return lineRule;
        }

        /**
         * Sets the value of the lineRule property.
         * 
         * @param value
         *     allowed object is
         *     {@link STLineSpacingRule }
         *     
         */
        public void setLineRule(STLineSpacingRule value) {
            this.lineRule = value;
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
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *             &lt;enumeration value="top"/>
     *             &lt;enumeration value="center"/>
     *             &lt;enumeration value="baseline"/>
     *             &lt;enumeration value="bottom"/>
     *             &lt;enumeration value="auto"/>
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
    public static class TextAlignment implements Child
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

}
