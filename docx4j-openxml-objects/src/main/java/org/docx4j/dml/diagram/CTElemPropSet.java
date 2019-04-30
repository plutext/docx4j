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


package org.docx4j.dml.diagram;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTShapeStyle;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ElemPropSet complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ElemPropSet"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="presLayoutVars" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_LayoutVariablePropertySet" minOccurs="0"/&gt;
 *         &lt;element name="style" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeStyle" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="presAssocID" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_ModelId" /&gt;
 *       &lt;attribute name="presName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="presStyleLbl" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="presStyleIdx" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="presStyleCnt" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="loTypeId" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="loCatId" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="qsTypeId" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="qsCatId" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="csTypeId" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="csCatId" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="coherent3DOff" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="phldrT" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="phldr" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="custAng" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="custFlipVert" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="custFlipHor" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="custSzX" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="custSzY" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="custScaleX" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="custScaleY" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="custT" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="custLinFactX" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="custLinFactY" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="custLinFactNeighborX" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="custLinFactNeighborY" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="custRadScaleRad" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="custRadScaleInc" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ElemPropSet", propOrder = {
    "presLayoutVars",
    "style"
})
public class CTElemPropSet implements Child
{

    protected CTLayoutVariablePropertySet presLayoutVars;
    protected CTShapeStyle style;
    @XmlAttribute(name = "presAssocID")
    protected String presAssocID;
    @XmlAttribute(name = "presName")
    protected String presName;
    @XmlAttribute(name = "presStyleLbl")
    protected String presStyleLbl;
    @XmlAttribute(name = "presStyleIdx")
    protected Integer presStyleIdx;
    @XmlAttribute(name = "presStyleCnt")
    protected Integer presStyleCnt;
    @XmlAttribute(name = "loTypeId")
    protected String loTypeId;
    @XmlAttribute(name = "loCatId")
    protected String loCatId;
    @XmlAttribute(name = "qsTypeId")
    protected String qsTypeId;
    @XmlAttribute(name = "qsCatId")
    protected String qsCatId;
    @XmlAttribute(name = "csTypeId")
    protected String csTypeId;
    @XmlAttribute(name = "csCatId")
    protected String csCatId;
    @XmlAttribute(name = "coherent3DOff")
    protected Boolean coherent3DOff;
    @XmlAttribute(name = "phldrT")
    protected String phldrT;
    @XmlAttribute(name = "phldr")
    protected Boolean phldr;
    @XmlAttribute(name = "custAng")
    protected Integer custAng;
    @XmlAttribute(name = "custFlipVert")
    protected Boolean custFlipVert;
    @XmlAttribute(name = "custFlipHor")
    protected Boolean custFlipHor;
    @XmlAttribute(name = "custSzX")
    protected Integer custSzX;
    @XmlAttribute(name = "custSzY")
    protected Integer custSzY;
    @XmlAttribute(name = "custScaleX")
    protected Integer custScaleX;
    @XmlAttribute(name = "custScaleY")
    protected Integer custScaleY;
    @XmlAttribute(name = "custT")
    protected Boolean custT;
    @XmlAttribute(name = "custLinFactX")
    protected Integer custLinFactX;
    @XmlAttribute(name = "custLinFactY")
    protected Integer custLinFactY;
    @XmlAttribute(name = "custLinFactNeighborX")
    protected Integer custLinFactNeighborX;
    @XmlAttribute(name = "custLinFactNeighborY")
    protected Integer custLinFactNeighborY;
    @XmlAttribute(name = "custRadScaleRad")
    protected Integer custRadScaleRad;
    @XmlAttribute(name = "custRadScaleInc")
    protected Integer custRadScaleInc;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the presLayoutVars property.
     * 
     * @return
     *     possible object is
     *     {@link CTLayoutVariablePropertySet }
     *     
     */
    public CTLayoutVariablePropertySet getPresLayoutVars() {
        return presLayoutVars;
    }

    /**
     * Sets the value of the presLayoutVars property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLayoutVariablePropertySet }
     *     
     */
    public void setPresLayoutVars(CTLayoutVariablePropertySet value) {
        this.presLayoutVars = value;
    }

    /**
     * Gets the value of the style property.
     * 
     * @return
     *     possible object is
     *     {@link CTShapeStyle }
     *     
     */
    public CTShapeStyle getStyle() {
        return style;
    }

    /**
     * Sets the value of the style property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShapeStyle }
     *     
     */
    public void setStyle(CTShapeStyle value) {
        this.style = value;
    }

    /**
     * Gets the value of the presAssocID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPresAssocID() {
        return presAssocID;
    }

    /**
     * Sets the value of the presAssocID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPresAssocID(String value) {
        this.presAssocID = value;
    }

    /**
     * Gets the value of the presName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPresName() {
        return presName;
    }

    /**
     * Sets the value of the presName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPresName(String value) {
        this.presName = value;
    }

    /**
     * Gets the value of the presStyleLbl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPresStyleLbl() {
        return presStyleLbl;
    }

    /**
     * Sets the value of the presStyleLbl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPresStyleLbl(String value) {
        this.presStyleLbl = value;
    }

    /**
     * Gets the value of the presStyleIdx property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPresStyleIdx() {
        return presStyleIdx;
    }

    /**
     * Sets the value of the presStyleIdx property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPresStyleIdx(Integer value) {
        this.presStyleIdx = value;
    }

    /**
     * Gets the value of the presStyleCnt property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPresStyleCnt() {
        return presStyleCnt;
    }

    /**
     * Sets the value of the presStyleCnt property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPresStyleCnt(Integer value) {
        this.presStyleCnt = value;
    }

    /**
     * Gets the value of the loTypeId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLoTypeId() {
        return loTypeId;
    }

    /**
     * Sets the value of the loTypeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLoTypeId(String value) {
        this.loTypeId = value;
    }

    /**
     * Gets the value of the loCatId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLoCatId() {
        return loCatId;
    }

    /**
     * Sets the value of the loCatId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLoCatId(String value) {
        this.loCatId = value;
    }

    /**
     * Gets the value of the qsTypeId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQsTypeId() {
        return qsTypeId;
    }

    /**
     * Sets the value of the qsTypeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQsTypeId(String value) {
        this.qsTypeId = value;
    }

    /**
     * Gets the value of the qsCatId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQsCatId() {
        return qsCatId;
    }

    /**
     * Sets the value of the qsCatId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQsCatId(String value) {
        this.qsCatId = value;
    }

    /**
     * Gets the value of the csTypeId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCsTypeId() {
        return csTypeId;
    }

    /**
     * Sets the value of the csTypeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCsTypeId(String value) {
        this.csTypeId = value;
    }

    /**
     * Gets the value of the csCatId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCsCatId() {
        return csCatId;
    }

    /**
     * Sets the value of the csCatId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCsCatId(String value) {
        this.csCatId = value;
    }

    /**
     * Gets the value of the coherent3DOff property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCoherent3DOff() {
        return coherent3DOff;
    }

    /**
     * Sets the value of the coherent3DOff property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCoherent3DOff(Boolean value) {
        this.coherent3DOff = value;
    }

    /**
     * Gets the value of the phldrT property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPhldrT() {
        return phldrT;
    }

    /**
     * Sets the value of the phldrT property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPhldrT(String value) {
        this.phldrT = value;
    }

    /**
     * Gets the value of the phldr property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isPhldr() {
        return phldr;
    }

    /**
     * Sets the value of the phldr property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPhldr(Boolean value) {
        this.phldr = value;
    }

    /**
     * Gets the value of the custAng property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCustAng() {
        return custAng;
    }

    /**
     * Sets the value of the custAng property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCustAng(Integer value) {
        this.custAng = value;
    }

    /**
     * Gets the value of the custFlipVert property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCustFlipVert() {
        return custFlipVert;
    }

    /**
     * Sets the value of the custFlipVert property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCustFlipVert(Boolean value) {
        this.custFlipVert = value;
    }

    /**
     * Gets the value of the custFlipHor property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCustFlipHor() {
        return custFlipHor;
    }

    /**
     * Sets the value of the custFlipHor property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCustFlipHor(Boolean value) {
        this.custFlipHor = value;
    }

    /**
     * Gets the value of the custSzX property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCustSzX() {
        return custSzX;
    }

    /**
     * Sets the value of the custSzX property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCustSzX(Integer value) {
        this.custSzX = value;
    }

    /**
     * Gets the value of the custSzY property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCustSzY() {
        return custSzY;
    }

    /**
     * Sets the value of the custSzY property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCustSzY(Integer value) {
        this.custSzY = value;
    }

    /**
     * Gets the value of the custScaleX property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCustScaleX() {
        return custScaleX;
    }

    /**
     * Sets the value of the custScaleX property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCustScaleX(Integer value) {
        this.custScaleX = value;
    }

    /**
     * Gets the value of the custScaleY property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCustScaleY() {
        return custScaleY;
    }

    /**
     * Sets the value of the custScaleY property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCustScaleY(Integer value) {
        this.custScaleY = value;
    }

    /**
     * Gets the value of the custT property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCustT() {
        return custT;
    }

    /**
     * Sets the value of the custT property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCustT(Boolean value) {
        this.custT = value;
    }

    /**
     * Gets the value of the custLinFactX property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCustLinFactX() {
        return custLinFactX;
    }

    /**
     * Sets the value of the custLinFactX property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCustLinFactX(Integer value) {
        this.custLinFactX = value;
    }

    /**
     * Gets the value of the custLinFactY property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCustLinFactY() {
        return custLinFactY;
    }

    /**
     * Sets the value of the custLinFactY property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCustLinFactY(Integer value) {
        this.custLinFactY = value;
    }

    /**
     * Gets the value of the custLinFactNeighborX property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCustLinFactNeighborX() {
        return custLinFactNeighborX;
    }

    /**
     * Sets the value of the custLinFactNeighborX property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCustLinFactNeighborX(Integer value) {
        this.custLinFactNeighborX = value;
    }

    /**
     * Gets the value of the custLinFactNeighborY property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCustLinFactNeighborY() {
        return custLinFactNeighborY;
    }

    /**
     * Sets the value of the custLinFactNeighborY property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCustLinFactNeighborY(Integer value) {
        this.custLinFactNeighborY = value;
    }

    /**
     * Gets the value of the custRadScaleRad property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCustRadScaleRad() {
        return custRadScaleRad;
    }

    /**
     * Sets the value of the custRadScaleRad property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCustRadScaleRad(Integer value) {
        this.custRadScaleRad = value;
    }

    /**
     * Gets the value of the custRadScaleInc property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCustRadScaleInc() {
        return custRadScaleInc;
    }

    /**
     * Sets the value of the custRadScaleInc property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCustRadScaleInc(Integer value) {
        this.custRadScaleInc = value;
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
