/*
 *  Copyright 2013, Plutext Pty Ltd.
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


package org.docx4j.w14; 

import org.jvnet.jaxb2_commons.ppp.Child;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.wml.CTEmpty;



/**
 * <p>Java class for CT_TextOutlineEffect complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TextOutlineEffect">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.microsoft.com/office/word/2010/wordml}EG_FillProperties" minOccurs="0"/>
 *         &lt;group ref="{http://schemas.microsoft.com/office/word/2010/wordml}EG_LineDashProperties" minOccurs="0"/>
 *         &lt;group ref="{http://schemas.microsoft.com/office/word/2010/wordml}EG_LineJoinProperties" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="w" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_LineWidth" />
 *       &lt;attribute name="cap" type="{http://schemas.microsoft.com/office/word/2010/wordml}ST_LineCap" />
 *       &lt;attribute name="cmpd" type="{http://schemas.microsoft.com/office/word/2010/wordml}ST_CompoundLine" />
 *       &lt;attribute name="algn" type="{http://schemas.microsoft.com/office/word/2010/wordml}ST_PenAlignment" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TextOutlineEffect", propOrder = {
    "noFill",
    "solidFill",
    "gradFill",
    "prstDash",
    "round",
    "bevel",
    "miter"
})
public class CTTextOutlineEffect implements Child
{

    protected CTEmpty noFill;
    protected CTSolidColorFillProperties solidFill;
    protected CTGradientFillProperties gradFill;
    protected CTPresetLineDashProperties prstDash;
    protected CTEmpty round;
    protected CTEmpty bevel;
    protected CTLineJoinMiterProperties miter;
    @XmlAttribute(name = "w", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected Integer w;
    @XmlAttribute(name = "cap", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected STLineCap cap;
    @XmlAttribute(name = "cmpd", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected STCompoundLine cmpd;
    @XmlAttribute(name = "algn", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected STPenAlignment algn;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the noFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmpty }
     *     
     */
    public CTEmpty getNoFill() {
        return noFill;
    }

    /**
     * Sets the value of the noFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmpty }
     *     
     */
    public void setNoFill(CTEmpty value) {
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
     * Gets the value of the round property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmpty }
     *     
     */
    public CTEmpty getRound() {
        return round;
    }

    /**
     * Sets the value of the round property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmpty }
     *     
     */
    public void setRound(CTEmpty value) {
        this.round = value;
    }

    /**
     * Gets the value of the bevel property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmpty }
     *     
     */
    public CTEmpty getBevel() {
        return bevel;
    }

    /**
     * Sets the value of the bevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmpty }
     *     
     */
    public void setBevel(CTEmpty value) {
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
