/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.wml;

import java.util.ArrayList;
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
import org.docx4j.math.CTOMath;
import org.docx4j.math.CTOMathPara;
import org.apache.log4j.Logger;
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
 *         &lt;element name="pPr" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_PPr" minOccurs="0"/>
 *         &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_PContent" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="rsidRPr" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_LongHexNumber" />
 *       &lt;attribute name="rsidR" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_LongHexNumber" />
 *       &lt;attribute name="rsidDel" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_LongHexNumber" />
 *       &lt;attribute name="rsidP" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_LongHexNumber" />
 *       &lt;attribute name="rsidRDefault" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_LongHexNumber" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "pPr",
    "paragraphContent"
})
@XmlRootElement(name = "p")
public class P implements Child, ContentAccessor
{
	private static Logger log = Logger.getLogger(P.class);	

    protected PPr pPr;
    @XmlElementRefs({
        @XmlElementRef(name = "bookmarkStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlMoveToRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "moveTo", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "moveFrom", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "bookmarkEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlInsRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "commentRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = CommentRangeStart.class),
        @XmlElementRef(name = "hyperlink", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "moveToRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "permStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "sdt", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "ins", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = RunIns.class),
        @XmlElementRef(name = "r", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = R.class),
        @XmlElementRef(name = "oMath", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlDelRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "smartTag", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlMoveFromRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "commentRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = CommentRangeEnd.class),
        @XmlElementRef(name = "permEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "fldSimple", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "proofErr", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = ProofErr.class),
        @XmlElementRef(name = "moveFromRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "del", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = RunDel.class),
        @XmlElementRef(name = "subDoc", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlInsRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlDelRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "oMathPara", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "moveToRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "customXml", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "moveFromRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlMoveToRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlMoveFromRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class)
    })
    protected List<Object> paragraphContent;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String rsidRPr;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String rsidR;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String rsidDel;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String rsidP;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String rsidRDefault;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the pPr property.
     * 
     * @return
     *     possible object is
     *     {@link PPr }
     *     
     */
    public PPr getPPr() {
        return pPr;
    }

    /**
     * Sets the value of the pPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link PPr }
     *     
     */
    public void setPPr(PPr value) {
        this.pPr = value;
    }

    /**
     * Gets the value of the paragraphContent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the paragraphContent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParagraphContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link CTBookmark }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
     * {@link JAXBElement }{@code <}{@link RunTrackChange }{@code >}
     * {@link JAXBElement }{@code <}{@link RunTrackChange }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMarkupRange }{@code >}
     * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
     * {@link CommentRangeStart }
     * {@link JAXBElement }{@code <}{@link P.Hyperlink }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMarkupRange }{@code >}
     * {@link JAXBElement }{@code <}{@link RangePermissionStart }{@code >}
     * {@link JAXBElement }{@code <}{@link SdtRun }{@code >}
     * {@link RunIns }
     * {@link R }
     * {@link JAXBElement }{@code <}{@link CTOMath }{@code >}
     * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSmartTagRun }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
     * {@link CommentRangeEnd }
     * {@link JAXBElement }{@code <}{@link CTPerm }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSimpleField }{@code >}
     * {@link ProofErr }
     * {@link JAXBElement }{@code <}{@link CTMarkupRange }{@code >}
     * {@link RunDel }
     * {@link JAXBElement }{@code <}{@link CTRel }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
     * {@link JAXBElement }{@code <}{@link CTOMathPara }{@code >}
     * {@link JAXBElement }{@code <}{@link CTCustomXmlRun }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMoveBookmark }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMoveBookmark }{@code >}
     * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
     * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
     * 
     * 
     */
    @Deprecated
    public List<Object> getParagraphContent() {
        if (paragraphContent == null) {
            paragraphContent = new ArrayList<Object>();
        }
        return this.paragraphContent;
    }

    /**
     * Get the content of this element.
     * @since 2.7
     */    
    public List<Object> getContent() {
        if (paragraphContent == null) {
            paragraphContent = new ArrayList<Object>();
        }
        return this.paragraphContent;
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
     * Gets the value of the rsidP property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRsidP() {
        return rsidP;
    }

    /**
     * Sets the value of the rsidP property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRsidP(String value) {
        this.rsidP = value;
    }

    /**
     * Gets the value of the rsidRDefault property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRsidRDefault() {
        return rsidRDefault;
    }

    /**
     * Sets the value of the rsidRDefault property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRsidRDefault(String value) {
        this.rsidRDefault = value;
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
     * Hyperlink
     * 
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_PContent" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;attribute name="tgtFrame" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
     *       &lt;attribute name="tooltip" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
     *       &lt;attribute name="docLocation" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
     *       &lt;attribute name="history" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
     *       &lt;attribute name="anchor" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
     *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}id"/>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "paragraphContent"
    })
    @XmlRootElement(name = "hyperlink")    
    public static class Hyperlink
        implements Child, ContentAccessor
    {

        @XmlElementRefs({
            @XmlElementRef(name = "subDoc", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "customXmlInsRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "sdt", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "commentRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = CommentRangeStart.class),
            @XmlElementRef(name = "moveTo", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "customXmlDelRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "smartTag", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "ins", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = RunIns.class),
            @XmlElementRef(name = "fldSimple", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "bookmarkEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "bookmarkStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "customXmlDelRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "customXmlMoveToRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "oMath", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
            @XmlElementRef(name = "r", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = R.class),
            @XmlElementRef(name = "moveFromRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "permEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "commentRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = CommentRangeEnd.class),
            @XmlElementRef(name = "moveToRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "hyperlink", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "customXmlMoveFromRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "moveFromRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "proofErr", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = ProofErr.class),
            @XmlElementRef(name = "del", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = RunDel.class),
            @XmlElementRef(name = "permStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "oMathPara", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
            @XmlElementRef(name = "customXml", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "customXmlMoveFromRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "moveFrom", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "customXmlInsRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "customXmlMoveToRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "moveToRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class)
        })
        protected List<Object> paragraphContent;
        @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected String tgtFrame;
        @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected String tooltip;
        @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected String docLocation;
        @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected Boolean history;
        @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected String anchor;
        @XmlAttribute(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
        protected String id;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the paragraphContent property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the paragraphContent property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getParagraphContent().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link JAXBElement }{@code <}{@link CTRel }{@code >}
         * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
         * {@link JAXBElement }{@code <}{@link SdtRun }{@code >}
         * {@link CommentRangeStart }
         * {@link JAXBElement }{@code <}{@link RunTrackChange }{@code >}
         * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
         * {@link JAXBElement }{@code <}{@link CTSmartTagRun }{@code >}
         * {@link RunIns }
         * {@link JAXBElement }{@code <}{@link CTSimpleField }{@code >}
         * {@link JAXBElement }{@code <}{@link CTMarkupRange }{@code >}
         * {@link JAXBElement }{@code <}{@link CTBookmark }{@code >}
         * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
         * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
         * {@link JAXBElement }{@code <}{@link CTOMath }{@code >}
         * {@link R }
         * {@link JAXBElement }{@code <}{@link CTMoveBookmark }{@code >}
         * {@link JAXBElement }{@code <}{@link CTPerm }{@code >}
         * {@link CommentRangeEnd }
         * {@link JAXBElement }{@code <}{@link CTMarkupRange }{@code >}
         * {@link JAXBElement }{@code <}{@link P.Hyperlink }{@code >}
         * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
         * {@link JAXBElement }{@code <}{@link CTMarkupRange }{@code >}
         * {@link ProofErr }
         * {@link RunDel }
         * {@link JAXBElement }{@code <}{@link RangePermissionStart }{@code >}
         * {@link JAXBElement }{@code <}{@link CTOMathPara }{@code >}
         * {@link JAXBElement }{@code <}{@link CTCustomXmlRun }{@code >}
         * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
         * {@link JAXBElement }{@code <}{@link RunTrackChange }{@code >}
         * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
         * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
         * {@link JAXBElement }{@code <}{@link CTMoveBookmark }{@code >}
         * 
         * 
         */
        @Deprecated
        public List<Object> getParagraphContent() {
            if (paragraphContent == null) {
                paragraphContent = new ArrayList<Object>();
            }
            return this.paragraphContent;
        }

        /**
         * Get the content of this element.
         * @since 2.7
         */        
        public List<Object> getContent() {
            if (paragraphContent == null) {
                paragraphContent = new ArrayList<Object>();
            }
            return this.paragraphContent;
        }
        
        /**
         * Gets the value of the tgtFrame property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTgtFrame() {
            return tgtFrame;
        }

        /**
         * Sets the value of the tgtFrame property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTgtFrame(String value) {
            this.tgtFrame = value;
        }

        /**
         * Gets the value of the tooltip property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTooltip() {
            return tooltip;
        }

        /**
         * Sets the value of the tooltip property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTooltip(String value) {
            this.tooltip = value;
        }

        /**
         * Gets the value of the docLocation property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDocLocation() {
            return docLocation;
        }

        /**
         * Sets the value of the docLocation property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDocLocation(String value) {
            this.docLocation = value;
        }

        /**
         * Gets the value of the history property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public boolean isHistory() {
            if (history == null) {
                return true;
            } else {
                return history;
            }
        }

        /**
         * Sets the value of the history property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setHistory(Boolean value) {
            this.history = value;
        }

        /**
         * Gets the value of the anchor property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getAnchor() {
            return anchor;
        }

        /**
         * Sets the value of the anchor property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setAnchor(String value) {
            this.anchor = value;
        }

        /**
         * 
         * 								Hyperlink Target
         * 							
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setId(String value) {
            this.id = value;
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

    // Not generated
    /** Get the text content of all runs in the P.  WARNING: this only gets ./w:r/w:t.
     *  That is incomplete.  Try org.docx4j.TextUtils.extractText instead.  */
    @Deprecated
    public String toString() {

    	StringBuilder result = new StringBuilder();
    	
    	List<Object> children = getParagraphContent();
    	
//    	System.out.println("p.toString");
    	
		for (Object o : children ) {					
//			System.out.println("  " + o.getClass().getName() );
			if ( o instanceof org.docx4j.wml.R) {
//		    	System.out.println("Hit R");
				org.docx4j.wml.R  run = (org.docx4j.wml.R)o;
		    	List runContent = run.getContent();
				for (Object o2 : runContent ) {					
					if ( o2 instanceof javax.xml.bind.JAXBElement) {
						// TODO - unmarshall directly to Text.
						if ( ((JAXBElement)o2).getDeclaredType().getName().equals("org.docx4j.wml.Text") ) {
//					    	System.out.println("Found Text");
							org.docx4j.wml.Text t = (org.docx4j.wml.Text)((JAXBElement)o2).getValue();
							result.append( t.getValue() );					
						}
					} else {
//				    	System.out.println(o2.getClass().getName());						
					}
				}
			} 
		}
		return result.toString();
    	
    }

    public void replaceElement(Object current, List insertions) {

    	int index = paragraphContent.indexOf(current);    	
    	if (index > -1 ) {    		
    		paragraphContent.addAll(index+1, insertions);  
    		Object removed = paragraphContent.remove(index);
    		// sanity check
    		if (!current.equals(removed)) {
    			log.error("removed wrong object?");
    		}    		
    	} else {
    		// Not found
    		log.error("Couldn't find replacement target.");
    	}
    }    
    
}
