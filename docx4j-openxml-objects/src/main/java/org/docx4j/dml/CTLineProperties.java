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


package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_LineProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_LineProperties"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_LineFillProperties" minOccurs="0"/&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_LineDashProperties" minOccurs="0"/&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_LineJoinProperties" minOccurs="0"/&gt;
 *         &lt;element name="headEnd" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_LineEndProperties" minOccurs="0"/&gt;
 *         &lt;element name="tailEnd" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_LineEndProperties" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="w" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_LineWidth" /&gt;
 *       &lt;attribute name="cap" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_LineCap" /&gt;
 *       &lt;attribute name="cmpd" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_CompoundLine" /&gt;
 *       &lt;attribute name="algn" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PenAlignment" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_LineProperties", propOrder = {
    "noFill",
    "solidFill",
    "gradFill",
    "pattFill",
    "prstDash",
    "custDash",
    "round",
    "bevel",
    "miter",
    "headEnd",
    "tailEnd",
    "extLst"
})
public class CTLineProperties implements Child
{

    protected CTNoFillProperties noFill;
    protected CTSolidColorFillProperties solidFill;
    protected CTGradientFillProperties gradFill;
    protected CTPatternFillProperties pattFill;
    protected CTPresetLineDashProperties prstDash;
    protected CTDashStopList custDash;
    protected CTLineJoinRound round;
    protected CTLineJoinBevel bevel;
    protected CTLineJoinMiterProperties miter;
    protected CTLineEndProperties headEnd;
    protected CTLineEndProperties tailEnd;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "w")
    protected Integer w;
    @XmlAttribute(name = "cap")
    protected STLineCap cap;
    @XmlAttribute(name = "cmpd")
    protected STCompoundLine cmpd;
    @XmlAttribute(name = "algn")
    protected STPenAlignment algn;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the noFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTNoFillProperties }
     *     
     */
    public CTNoFillProperties getNoFill() {
        return noFill;
    }

    /**
     * Sets the value of the noFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNoFillProperties }
     *     
     */
    public void setNoFill(CTNoFillProperties value) {
        this.noFill = value;
    }

    /**
     * Gets the value of the solidFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTSolidColorFillProperties }
     *     
     */
    public CTSolidColorFillProperties getSolidFill() {
        return solidFill;
    }

    /**
     * Sets the value of the solidFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSolidColorFillProperties }
     *     
     */
    public void setSolidFill(CTSolidColorFillProperties value) {
        this.solidFill = value;
    }

    /**
     * Gets the value of the gradFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTGradientFillProperties }
     *     
     */
    public CTGradientFillProperties getGradFill() {
        return gradFill;
    }

    /**
     * Sets the value of the gradFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGradientFillProperties }
     *     
     */
    public void setGradFill(CTGradientFillProperties value) {
        this.gradFill = value;
    }

    /**
     * Gets the value of the pattFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTPatternFillProperties }
     *     
     */
    public CTPatternFillProperties getPattFill() {
        return pattFill;
    }

    /**
     * Sets the value of the pattFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPatternFillProperties }
     *     
     */
    public void setPattFill(CTPatternFillProperties value) {
        this.pattFill = value;
    }

    /**
     * Gets the value of the prstDash property.
     * 
     * @return
     *     possible object is
     *     {@link CTPresetLineDashProperties }
     *     
     */
    public CTPresetLineDashProperties getPrstDash() {
        return prstDash;
    }

    /**
     * Sets the value of the prstDash property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPresetLineDashProperties }
     *     
     */
    public void setPrstDash(CTPresetLineDashProperties value) {
        this.prstDash = value;
    }

    /**
     * Gets the value of the custDash property.
     * 
     * @return
     *     possible object is
     *     {@link CTDashStopList }
     *     
     */
    public CTDashStopList getCustDash() {
        return custDash;
    }

    /**
     * Sets the value of the custDash property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDashStopList }
     *     
     */
    public void setCustDash(CTDashStopList value) {
        this.custDash = value;
    }

    /**
     * Gets the value of the round property.
     * 
     * @return
     *     possible object is
     *     {@link CTLineJoinRound }
     *     
     */
    public CTLineJoinRound getRound() {
        return round;
    }

    /**
     * Sets the value of the round property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLineJoinRound }
     *     
     */
    public void setRound(CTLineJoinRound value) {
        this.round = value;
    }

    /**
     * Gets the value of the bevel property.
     * 
     * @return
     *     possible object is
     *     {@link CTLineJoinBevel }
     *     
     */
    public CTLineJoinBevel getBevel() {
        return bevel;
    }

    /**
     * Sets the value of the bevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLineJoinBevel }
     *     
     */
    public void setBevel(CTLineJoinBevel value) {
        this.bevel = value;
    }

    /**
     * Gets the value of the miter property.
     * 
     * @return
     *     possible object is
     *     {@link CTLineJoinMiterProperties }
     *     
     */
    public CTLineJoinMiterProperties getMiter() {
        return miter;
    }

    /**
     * Sets the value of the miter property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLineJoinMiterProperties }
     *     
     */
    public void setMiter(CTLineJoinMiterProperties value) {
        this.miter = value;
    }

    /**
     * Gets the value of the headEnd property.
     * 
     * @return
     *     possible object is
     *     {@link CTLineEndProperties }
     *     
     */
    public CTLineEndProperties getHeadEnd() {
        return headEnd;
    }

    /**
     * Sets the value of the headEnd property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLineEndProperties }
     *     
     */
    public void setHeadEnd(CTLineEndProperties value) {
        this.headEnd = value;
    }

    /**
     * Gets the value of the tailEnd property.
     * 
     * @return
     *     possible object is
     *     {@link CTLineEndProperties }
     *     
     */
    public CTLineEndProperties getTailEnd() {
        return tailEnd;
    }

    /**
     * Sets the value of the tailEnd property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLineEndProperties }
     *     
     */
    public void setTailEnd(CTLineEndProperties value) {
        this.tailEnd = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public CTOfficeArtExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public void setExtLst(CTOfficeArtExtensionList value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the w property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getW() {
        return w;
    }

    /**
     * Sets the value of the w property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setW(Integer value) {
        this.w = value;
    }

    /**
     * Gets the value of the cap property.
     * 
     * @return
     *     possible object is
     *     {@link STLineCap }
     *     
     */
    public STLineCap getCap() {
        return cap;
    }

    /**
     * Sets the value of the cap property.
     * 
     * @param value
     *     allowed object is
     *     {@link STLineCap }
     *     
     */
    public void setCap(STLineCap value) {
        this.cap = value;
    }

    /**
     * Gets the value of the cmpd property.
     * 
     * @return
     *     possible object is
     *     {@link STCompoundLine }
     *     
     */
    public STCompoundLine getCmpd() {
        return cmpd;
    }

    /**
     * Sets the value of the cmpd property.
     * 
     * @param value
     *     allowed object is
     *     {@link STCompoundLine }
     *     
     */
    public void setCmpd(STCompoundLine value) {
        this.cmpd = value;
    }

    /**
     * Gets the value of the algn property.
     * 
     * @return
     *     possible object is
     *     {@link STPenAlignment }
     *     
     */
    public STPenAlignment getAlgn() {
        return algn;
    }

    /**
     * Sets the value of the algn property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPenAlignment }
     *     
     */
    public void setAlgn(STPenAlignment value) {
        this.algn = value;
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
