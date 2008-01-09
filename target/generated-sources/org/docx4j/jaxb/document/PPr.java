/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.jaxb.document;

import java.math.BigInteger;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *         &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_PPrBase"/>
 *         &lt;sequence>
 *           &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}rPr" minOccurs="0"/>
 *           &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}sectPr" minOccurs="0"/>
 *         &lt;/sequence>
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
    "pStyle",
    "keepNext",
    "keepLines",
    "pageBreakBefore",
    "widowControl",
    "numPr",
    "suppressLineNumbers",
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
    "outlineLvl",
    "rPr",
    "sectPr"
})
public class PPr
    implements Child
{

    protected PPr.PStyle pStyle;
    protected BooleanDefaultTrue keepNext;
    protected BooleanDefaultTrue keepLines;
    protected BooleanDefaultTrue pageBreakBefore;
    protected BooleanDefaultTrue widowControl;
    protected PPr.NumPr numPr;
    protected BooleanDefaultTrue suppressLineNumbers;
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
    protected Spacing spacing;
    protected Ind ind;
    protected BooleanDefaultTrue contextualSpacing;
    protected BooleanDefaultTrue mirrorIndents;
    protected BooleanDefaultTrue suppressOverlap;
    protected Jc jc;
    protected PPr.OutlineLvl outlineLvl;
    protected RPr rPr;
    protected SectPr sectPr;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the pStyle property.
     * 
     * @return
     *     possible object is
     *     {@link PPr.PStyle }
     *     
     */
    public PPr.PStyle getPStyle() {
        return pStyle;
    }

    /**
     * Sets the value of the pStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link PPr.PStyle }
     *     
     */
    public void setPStyle(PPr.PStyle value) {
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
     *     {@link PPr.NumPr }
     *     
     */
    public PPr.NumPr getNumPr() {
        return numPr;
    }

    /**
     * Sets the value of the numPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link PPr.NumPr }
     *     
     */
    public void setNumPr(PPr.NumPr value) {
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
     *     {@link Spacing }
     *     
     */
    public Spacing getSpacing() {
        return spacing;
    }

    /**
     * Sets the value of the spacing property.
     * 
     * @param value
     *     allowed object is
     *     {@link Spacing }
     *     
     */
    public void setSpacing(Spacing value) {
        this.spacing = value;
    }

    /**
     * Gets the value of the ind property.
     * 
     * @return
     *     possible object is
     *     {@link Ind }
     *     
     */
    public Ind getInd() {
        return ind;
    }

    /**
     * Sets the value of the ind property.
     * 
     * @param value
     *     allowed object is
     *     {@link Ind }
     *     
     */
    public void setInd(Ind value) {
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
     * Gets the value of the outlineLvl property.
     * 
     * @return
     *     possible object is
     *     {@link PPr.OutlineLvl }
     *     
     */
    public PPr.OutlineLvl getOutlineLvl() {
        return outlineLvl;
    }

    /**
     * Sets the value of the outlineLvl property.
     * 
     * @param value
     *     allowed object is
     *     {@link PPr.OutlineLvl }
     *     
     */
    public void setOutlineLvl(PPr.OutlineLvl value) {
        this.outlineLvl = value;
    }

    /**
     * Gets the value of the rPr property.
     * 
     * @return
     *     possible object is
     *     {@link RPr }
     *     
     */
    public RPr getRPr() {
        return rPr;
    }

    /**
     * Sets the value of the rPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link RPr }
     *     
     */
    public void setRPr(RPr value) {
        this.rPr = value;
    }

    /**
     * Gets the value of the sectPr property.
     * 
     * @return
     *     possible object is
     *     {@link SectPr }
     *     
     */
    public SectPr getSectPr() {
        return sectPr;
    }

    /**
     * Sets the value of the sectPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link SectPr }
     *     
     */
    public void setSectPr(SectPr value) {
        this.sectPr = value;
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
        "numId"
    })
    public static class NumPr
        implements Child
    {

        protected PPr.NumPr.Ilvl ilvl;
        protected PPr.NumPr.NumId numId;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the ilvl property.
         * 
         * @return
         *     possible object is
         *     {@link PPr.NumPr.Ilvl }
         *     
         */
        public PPr.NumPr.Ilvl getIlvl() {
            return ilvl;
        }

        /**
         * Sets the value of the ilvl property.
         * 
         * @param value
         *     allowed object is
         *     {@link PPr.NumPr.Ilvl }
         *     
         */
        public void setIlvl(PPr.NumPr.Ilvl value) {
            this.ilvl = value;
        }

        /**
         * Gets the value of the numId property.
         * 
         * @return
         *     possible object is
         *     {@link PPr.NumPr.NumId }
         *     
         */
        public PPr.NumPr.NumId getNumId() {
            return numId;
        }

        /**
         * Sets the value of the numId property.
         * 
         * @param value
         *     allowed object is
         *     {@link PPr.NumPr.NumId }
         *     
         */
        public void setNumId(PPr.NumPr.NumId value) {
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
        public static class Ilvl
            implements Child
        {

            @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
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
        public static class NumId
            implements Child
        {

            @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
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
     * Associated Outline
     * 							Level
     * 
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="val" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class OutlineLvl
        implements Child
    {

        @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
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
    public static class PStyle
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

}
