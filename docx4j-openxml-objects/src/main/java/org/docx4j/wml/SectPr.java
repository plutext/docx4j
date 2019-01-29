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
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_SectPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SectPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_HdrFtrReferences" maxOccurs="6" minOccurs="0"/>
 *         &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_SectPrContents" minOccurs="0"/>
 *         &lt;element name="sectPrChange" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_SectPrChange" minOccurs="0"/>
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
@XmlType(name = "CT_SectPr", propOrder = {
    "egHdrFtrReferences",
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
    "footnoteColumns",
    "sectPrChange"
})
@XmlRootElement(name = "sectPr")
public class SectPr
    implements Child
{

    @XmlElements({
        @XmlElement(name = "headerReference", type = HeaderReference.class),
        @XmlElement(name = "footerReference", type = FooterReference.class)
    })
    protected List<CTRel> egHdrFtrReferences = new ArrayListWml<CTRel>(this);
    protected CTFtnProps footnotePr;
    protected CTEdnProps endnotePr;
    protected SectPr.Type type;
    protected SectPr.PgSz pgSz;
    protected SectPr.PgMar pgMar;
    protected CTPaperSource paperSrc;
    protected SectPr.PgBorders pgBorders;
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
    protected CTSectPrChange sectPrChange;
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
     * Gets the value of the egHdrFtrReferences property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the egHdrFtrReferences property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEGHdrFtrReferences().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HeaderReference }
     * {@link FooterReference }
     * 
     * 
     */
    public List<CTRel> getEGHdrFtrReferences() {
        if (egHdrFtrReferences == null) {
            egHdrFtrReferences = new ArrayListWml<CTRel>(this);
        }
        return this.egHdrFtrReferences;
    }

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
     *     {@link SectPr.Type }
     *     
     */
    public SectPr.Type getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link SectPr.Type }
     *     
     */
    public void setType(SectPr.Type value) {
        this.type = value;
    }

    /**
     * Gets the value of the pgSz property.
     * 
     * @return
     *     possible object is
     *     {@link SectPr.PgSz }
     *     
     */
    public SectPr.PgSz getPgSz() {
        return pgSz;
    }

    /**
     * Sets the value of the pgSz property.
     * 
     * @param value
     *     allowed object is
     *     {@link SectPr.PgSz }
     *     
     */
    public void setPgSz(SectPr.PgSz value) {
        this.pgSz = value;
    }

    /**
     * Gets the value of the pgMar property.
     * 
     * @return
     *     possible object is
     *     {@link SectPr.PgMar }
     *     
     */
    public SectPr.PgMar getPgMar() {
        return pgMar;
    }

    /**
     * Sets the value of the pgMar property.
     * 
     * @param value
     *     allowed object is
     *     {@link SectPr.PgMar }
     *     
     */
    public void setPgMar(SectPr.PgMar value) {
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
     *     {@link SectPr.PgBorders }
     *     
     */
    public SectPr.PgBorders getPgBorders() {
        return pgBorders;
    }

    /**
     * Sets the value of the pgBorders property.
     * 
     * @param value
     *     allowed object is
     *     {@link SectPr.PgBorders }
     *     
     */
    public void setPgBorders(SectPr.PgBorders value) {
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
     * Gets the value of the sectPrChange property.
     * 
     * @return
     *     possible object is
     *     {@link CTSectPrChange }
     *     
     */
    public CTSectPrChange getSectPrChange() {
        return sectPrChange;
    }

    /**
     * Sets the value of the sectPrChange property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSectPrChange }
     *     
     */
    public void setSectPrChange(CTSectPrChange value) {
        this.sectPrChange = value;
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
     *       &lt;/sequence>
     *       &lt;attribute name="zOrder" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_PageBorderZOrder" />
     *       &lt;attribute name="display" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_PageBorderDisplay" />
     *       &lt;attribute name="offsetFrom" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_PageBorderOffset" />
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
        "right"
    })
    public static class PgBorders implements Child
    {

        protected CTBorder top;
        protected CTBorder left;
        protected CTBorder bottom;
        protected CTBorder right;
        @XmlAttribute(name = "zOrder", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected STPageBorderZOrder zOrder;
        @XmlAttribute(name = "display", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected STPageBorderDisplay display;
        @XmlAttribute(name = "offsetFrom", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected STPageBorderOffset offsetFrom;
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
         * Gets the value of the zOrder property.
         * 
         * @return
         *     possible object is
         *     {@link STPageBorderZOrder }
         *     
         */
        public STPageBorderZOrder getZOrder() {
            return zOrder;
        }

        /**
         * Sets the value of the zOrder property.
         * 
         * @param value
         *     allowed object is
         *     {@link STPageBorderZOrder }
         *     
         */
        public void setZOrder(STPageBorderZOrder value) {
            this.zOrder = value;
        }

        /**
         * Gets the value of the display property.
         * 
         * @return
         *     possible object is
         *     {@link STPageBorderDisplay }
         *     
         */
        public STPageBorderDisplay getDisplay() {
            return display;
        }

        /**
         * Sets the value of the display property.
         * 
         * @param value
         *     allowed object is
         *     {@link STPageBorderDisplay }
         *     
         */
        public void setDisplay(STPageBorderDisplay value) {
            this.display = value;
        }

        /**
         * Gets the value of the offsetFrom property.
         * 
         * @return
         *     possible object is
         *     {@link STPageBorderOffset }
         *     
         */
        public STPageBorderOffset getOffsetFrom() {
            return offsetFrom;
        }

        /**
         * Sets the value of the offsetFrom property.
         * 
         * @param value
         *     allowed object is
         *     {@link STPageBorderOffset }
         *     
         */
        public void setOffsetFrom(STPageBorderOffset value) {
            this.offsetFrom = value;
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
     *       &lt;attribute name="top" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_SignedTwipsMeasure" />
     *       &lt;attribute name="right" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
     *       &lt;attribute name="bottom" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_SignedTwipsMeasure" />
     *       &lt;attribute name="left" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
     *       &lt;attribute name="header" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
     *       &lt;attribute name="footer" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
     *       &lt;attribute name="gutter" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class PgMar implements Child
    {

        @XmlAttribute(name = "top", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
        protected BigInteger top;
        @XmlAttribute(name = "right", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
        protected BigInteger right;
        @XmlAttribute(name = "bottom", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
        protected BigInteger bottom;
        @XmlAttribute(name = "left", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
        protected BigInteger left;
        @XmlAttribute(name = "header", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
        protected BigInteger header;
        @XmlAttribute(name = "footer", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
        protected BigInteger footer;
        @XmlAttribute(name = "gutter", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
        protected BigInteger gutter;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the top property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getTop() {
            return top;
        }

        /**
         * Sets the value of the top property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setTop(BigInteger value) {
            this.top = value;
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
         * Gets the value of the bottom property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getBottom() {
            return bottom;
        }

        /**
         * Sets the value of the bottom property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setBottom(BigInteger value) {
            this.bottom = value;
        }

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
         * Gets the value of the header property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getHeader() {
            return header;
        }

        /**
         * Sets the value of the header property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setHeader(BigInteger value) {
            this.header = value;
        }

        /**
         * Gets the value of the footer property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getFooter() {
            return footer;
        }

        /**
         * Sets the value of the footer property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setFooter(BigInteger value) {
            this.footer = value;
        }

        /**
         * Gets the value of the gutter property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getGutter() {
            return gutter;
        }

        /**
         * Sets the value of the gutter property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setGutter(BigInteger value) {
            this.gutter = value;
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
     *       &lt;attribute name="w" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
     *       &lt;attribute name="h" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
     *       &lt;attribute name="orient" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_PageOrientation" />
     *       &lt;attribute name="code" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class PgSz implements Child
    {

        @XmlAttribute(name = "w", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected BigInteger w;
        @XmlAttribute(name = "h", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected BigInteger h;
        @XmlAttribute(name = "orient", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected STPageOrientation orient;
        @XmlAttribute(name = "code", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected BigInteger code;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the w property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getW() {
            return w;
        }

        /**
         * Sets the value of the w property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setW(BigInteger value) {
            this.w = value;
        }

        /**
         * Gets the value of the h property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getH() {
            return h;
        }

        /**
         * Sets the value of the h property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setH(BigInteger value) {
            this.h = value;
        }

        /**
         * Gets the value of the orient property.
         * 
         * @return
         *     possible object is
         *     {@link STPageOrientation }
         *     
         */
        public STPageOrientation getOrient() {
            return orient;
        }

        /**
         * Sets the value of the orient property.
         * 
         * @param value
         *     allowed object is
         *     {@link STPageOrientation }
         *     
         */
        public void setOrient(STPageOrientation value) {
            this.orient = value;
        }

        /**
         * Gets the value of the code property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getCode() {
            return code;
        }

        /**
         * Sets the value of the code property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setCode(BigInteger value) {
            this.code = value;
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
     *             &lt;enumeration value="nextPage"/>
     *             &lt;enumeration value="nextColumn"/>
     *             &lt;enumeration value="continuous"/>
     *             &lt;enumeration value="evenPage"/>
     *             &lt;enumeration value="oddPage"/>
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
    public static class Type implements Child
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
