/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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
 *         &lt;element name="tblPrEx" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TblPrEx" minOccurs="0"/>
 *         &lt;element name="trPr" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TrPr" minOccurs="0"/>
 *         &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_ContentCellContent" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="rsidRPr" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_LongHexNumber" />
 *       &lt;attribute name="rsidR" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_LongHexNumber" />
 *       &lt;attribute name="rsidDel" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_LongHexNumber" />
 *       &lt;attribute name="rsidTr" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_LongHexNumber" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "tblPrEx",
    "trPr",
    "egContentCellContent"
})
@XmlRootElement(name = "tr")
public class Tr implements Child, ContentAccessor
{

    protected CTTblPrEx tblPrEx;
    protected TrPr trPr;
    @XmlElementRefs({
        @XmlElementRef(name = "moveFrom", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "sdt", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlInsRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "moveTo", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "commentRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = CommentRangeStart.class),
        @XmlElementRef(name = "customXmlInsRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "tc", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "ins", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = RunIns.class),
        @XmlElementRef(name = "permStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "moveFromRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "oMath", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "customXml", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "bookmarkEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "moveToRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "bookmarkStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "commentRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = CommentRangeEnd.class),
        @XmlElementRef(name = "customXmlMoveFromRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlMoveFromRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "permEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "proofErr", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = ProofErr.class),
        @XmlElementRef(name = "del", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = RunDel.class),
        @XmlElementRef(name = "customXmlDelRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlMoveToRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "oMathPara", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "moveToRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlMoveToRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlDelRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "moveFromRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class)
    })
    protected List<Object> egContentCellContent;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String rsidRPr;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String rsidR;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String rsidDel;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String rsidTr;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the tblPrEx property.
     * 
     * @return
     *     possible object is
     *     {@link CTTblPrEx }
     *     
     */
    public CTTblPrEx getTblPrEx() {
        return tblPrEx;
    }

    /**
     * Sets the value of the tblPrEx property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTblPrEx }
     *     
     */
    public void setTblPrEx(CTTblPrEx value) {
        this.tblPrEx = value;
    }

    /**
     * Gets the value of the trPr property.
     * 
     * @return
     *     possible object is
     *     {@link TrPr }
     *     
     */
    public TrPr getTrPr() {
        return trPr;
    }

    /**
     * Sets the value of the trPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link TrPr }
     *     
     */
    public void setTrPr(TrPr value) {
        this.trPr = value;
    }

    /**
     * Gets the value of the egContentCellContent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the egContentCellContent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEGContentCellContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link RunTrackChange }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSdtCell }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
     * {@link JAXBElement }{@code <}{@link RunTrackChange }{@code >}
     * {@link CommentRangeStart }
     * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
     * {@link JAXBElement }{@code <}{@link Tc }{@code >}
     * {@link RunIns }
     * {@link JAXBElement }{@code <}{@link RangePermissionStart }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMoveBookmark }{@code >}
     * {@link JAXBElement }{@code <}{@link CTOMath }{@code >}
     * {@link JAXBElement }{@code <}{@link CTCustomXmlCell }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMarkupRange }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMoveBookmark }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBookmark }{@code >}
     * {@link CommentRangeEnd }
     * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPerm }{@code >}
     * {@link ProofErr }
     * {@link RunDel }
     * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
     * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
     * {@link JAXBElement }{@code <}{@link CTOMathPara }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMarkupRange }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMarkupRange }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
     * 
     * 
     */
    @Deprecated
    public List<Object> getEGContentCellContent() {
        if (egContentCellContent == null) {
            egContentCellContent = new ArrayList<Object>();
        }
        return this.egContentCellContent;
    }

    /**
     * Get the content of this element.
     * @since 2.7
     */    
    public List<Object> getContent() {
        if (egContentCellContent == null) {
            egContentCellContent = new ArrayList<Object>();
        }
        return this.egContentCellContent;
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
     * Gets the value of the rsidTr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRsidTr() {
        return rsidTr;
    }

    /**
     * Sets the value of the rsidTr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRsidTr(String value) {
        this.rsidTr = value;
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
