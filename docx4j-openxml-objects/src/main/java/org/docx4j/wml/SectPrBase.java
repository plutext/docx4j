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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_SectPrBase complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SectPrBase">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_SectPrContents" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}AG_SectPrAttributes"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SectPrBase", propOrder = {
    "footnotePr",
    "endnotePr",
    "type",
    "pgSz",
    "pgMar",
    "paperSrc",
    "pgBorders",
    "lnNumType",
    "pgNumType",
    "cols",
    "formProt",
    "vAlign",
    "noEndnote",
    "titlePg",
    "textDirection",
    "bidi",
    "rtlGutter",
    "docGrid",
    "printerSettings",
    "footnoteColumns"
})
public class SectPrBase implements Child
{

    protected CTFtnProps footnotePr;
    protected CTEdnProps endnotePr;
    protected org.docx4j.wml.SectPr.Type type;
    protected org.docx4j.wml.SectPr.PgSz pgSz;
    protected org.docx4j.wml.SectPr.PgMar pgMar;
    protected CTPaperSource paperSrc;
    protected org.docx4j.wml.SectPr.PgBorders pgBorders;
    protected CTLineNumber lnNumType;
    protected CTPageNumber pgNumType;
    protected CTColumns cols;
    protected BooleanDefaultTrue formProt;
    protected CTVerticalJc vAlign;
    protected BooleanDefaultTrue noEndnote;
    protected BooleanDefaultTrue titlePg;
    protected TextDirection textDirection;
    protected BooleanDefaultTrue bidi;
    protected BooleanDefaultTrue rtlGutter;
    protected CTDocGrid docGrid;
    protected CTRel printerSettings;
    @XmlElement(namespace = "http://schemas.microsoft.com/office/word/2012/wordml")
    protected CTDecimalNumber footnoteColumns;
    @XmlAttribute(name = "rsidRPr", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String rsidRPr;
    @XmlAttribute(name = "rsidDel", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String rsidDel;
    @XmlAttribute(name = "rsidR", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String rsidR;
    @XmlAttribute(name = "rsidSect", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String rsidSect;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the footnotePr property.
     * 
     * @return
     *     possible object is
     *     {@link CTFtnProps }
     *     
     */
    public CTFtnProps getFootnotePr() {
        return footnotePr;
    }

    /**
     * Sets the value of the footnotePr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFtnProps }
     *     
     */
    public void setFootnotePr(CTFtnProps value) {
        this.footnotePr = value;
    }

    /**
     * Gets the value of the endnotePr property.
     * 
     * @return
     *     possible object is
     *     {@link CTEdnProps }
     *     
     */
    public CTEdnProps getEndnotePr() {
        return endnotePr;
    }

    /**
     * Sets the value of the endnotePr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEdnProps }
     *     
     */
    public void setEndnotePr(CTEdnProps value) {
        this.endnotePr = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.wml.SectPr.Type }
     *     
     */
    public org.docx4j.wml.SectPr.Type getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.wml.SectPr.Type }
     *     
     */
    public void setType(org.docx4j.wml.SectPr.Type value) {
        this.type = value;
    }

    /**
     * Gets the value of the pgSz property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.wml.SectPr.PgSz }
     *     
     */
    public org.docx4j.wml.SectPr.PgSz getPgSz() {
        return pgSz;
    }

    /**
     * Sets the value of the pgSz property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.wml.SectPr.PgSz }
     *     
     */
    public void setPgSz(org.docx4j.wml.SectPr.PgSz value) {
        this.pgSz = value;
    }

    /**
     * Gets the value of the pgMar property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.wml.SectPr.PgMar }
     *     
     */
    public org.docx4j.wml.SectPr.PgMar getPgMar() {
        return pgMar;
    }

    /**
     * Sets the value of the pgMar property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.wml.SectPr.PgMar }
     *     
     */
    public void setPgMar(org.docx4j.wml.SectPr.PgMar value) {
        this.pgMar = value;
    }

    /**
     * Gets the value of the paperSrc property.
     * 
     * @return
     *     possible object is
     *     {@link CTPaperSource }
     *     
     */
    public CTPaperSource getPaperSrc() {
        return paperSrc;
    }

    /**
     * Sets the value of the paperSrc property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPaperSource }
     *     
     */
    public void setPaperSrc(CTPaperSource value) {
        this.paperSrc = value;
    }

    /**
     * Gets the value of the pgBorders property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.wml.SectPr.PgBorders }
     *     
     */
    public org.docx4j.wml.SectPr.PgBorders getPgBorders() {
        return pgBorders;
    }

    /**
     * Sets the value of the pgBorders property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.wml.SectPr.PgBorders }
     *     
     */
    public void setPgBorders(org.docx4j.wml.SectPr.PgBorders value) {
        this.pgBorders = value;
    }

    /**
     * Gets the value of the lnNumType property.
     * 
     * @return
     *     possible object is
     *     {@link CTLineNumber }
     *     
     */
    public CTLineNumber getLnNumType() {
        return lnNumType;
    }

    /**
     * Sets the value of the lnNumType property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLineNumber }
     *     
     */
    public void setLnNumType(CTLineNumber value) {
        this.lnNumType = value;
    }

    /**
     * Gets the value of the pgNumType property.
     * 
     * @return
     *     possible object is
     *     {@link CTPageNumber }
     *     
     */
    public CTPageNumber getPgNumType() {
        return pgNumType;
    }

    /**
     * Sets the value of the pgNumType property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPageNumber }
     *     
     */
    public void setPgNumType(CTPageNumber value) {
        this.pgNumType = value;
    }

    /**
     * Gets the value of the cols property.
     * 
     * @return
     *     possible object is
     *     {@link CTColumns }
     *     
     */
    public CTColumns getCols() {
        return cols;
    }

    /**
     * Sets the value of the cols property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColumns }
     *     
     */
    public void setCols(CTColumns value) {
        this.cols = value;
    }

    /**
     * Gets the value of the formProt property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getFormProt() {
        return formProt;
    }

    /**
     * Sets the value of the formProt property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setFormProt(BooleanDefaultTrue value) {
        this.formProt = value;
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
     * Gets the value of the noEndnote property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getNoEndnote() {
        return noEndnote;
    }

    /**
     * Sets the value of the noEndnote property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setNoEndnote(BooleanDefaultTrue value) {
        this.noEndnote = value;
    }

    /**
     * Gets the value of the titlePg property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getTitlePg() {
        return titlePg;
    }

    /**
     * Sets the value of the titlePg property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setTitlePg(BooleanDefaultTrue value) {
        this.titlePg = value;
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
     * Gets the value of the rtlGutter property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getRtlGutter() {
        return rtlGutter;
    }

    /**
     * Sets the value of the rtlGutter property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setRtlGutter(BooleanDefaultTrue value) {
        this.rtlGutter = value;
    }

    /**
     * Gets the value of the docGrid property.
     * 
     * @return
     *     possible object is
     *     {@link CTDocGrid }
     *     
     */
    public CTDocGrid getDocGrid() {
        return docGrid;
    }

    /**
     * Sets the value of the docGrid property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDocGrid }
     *     
     */
    public void setDocGrid(CTDocGrid value) {
        this.docGrid = value;
    }

    /**
     * Gets the value of the printerSettings property.
     * 
     * @return
     *     possible object is
     *     {@link CTRel }
     *     
     */
    public CTRel getPrinterSettings() {
        return printerSettings;
    }

    /**
     * Sets the value of the printerSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRel }
     *     
     */
    public void setPrinterSettings(CTRel value) {
        this.printerSettings = value;
    }

    /**
     * Gets the value of the footnoteColumns property.
     * 
     * @return
     *     possible object is
     *     {@link CTDecimalNumber }
     *     
     */
    public CTDecimalNumber getFootnoteColumns() {
        return footnoteColumns;
    }

    /**
     * Sets the value of the footnoteColumns property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDecimalNumber }
     *     
     */
    public void setFootnoteColumns(CTDecimalNumber value) {
        this.footnoteColumns = value;
    }

    /**
     * Gets the value of the rsidRPr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRsidRPr() {
        return rsidRPr;
    }

    /**
     * Sets the value of the rsidRPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRsidRPr(String value) {
        this.rsidRPr = value;
    }

    /**
     * Gets the value of the rsidDel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRsidDel() {
        return rsidDel;
    }

    /**
     * Sets the value of the rsidDel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRsidDel(String value) {
        this.rsidDel = value;
    }

    /**
     * Gets the value of the rsidR property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRsidR() {
        return rsidR;
    }

    /**
     * Sets the value of the rsidR property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRsidR(String value) {
        this.rsidR = value;
    }

    /**
     * Gets the value of the rsidSect property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRsidSect() {
        return rsidSect;
    }

    /**
     * Sets the value of the rsidSect property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRsidSect(String value) {
        this.rsidSect = value;
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
