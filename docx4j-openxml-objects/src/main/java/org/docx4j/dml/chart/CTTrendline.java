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


package org.docx4j.dml.chart;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTShapeProperties;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Trendline complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Trendline"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="spPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeProperties" minOccurs="0"/&gt;
 *         &lt;element name="trendlineType" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_TrendlineType"/&gt;
 *         &lt;element name="order" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Order" minOccurs="0"/&gt;
 *         &lt;element name="period" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Period" minOccurs="0"/&gt;
 *         &lt;element name="forward" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Double" minOccurs="0"/&gt;
 *         &lt;element name="backward" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Double" minOccurs="0"/&gt;
 *         &lt;element name="intercept" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Double" minOccurs="0"/&gt;
 *         &lt;element name="dispRSqr" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean" minOccurs="0"/&gt;
 *         &lt;element name="dispEq" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean" minOccurs="0"/&gt;
 *         &lt;element name="trendlineLbl" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_TrendlineLbl" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_ExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Trendline", propOrder = {
    "name",
    "spPr",
    "trendlineType",
    "order",
    "period",
    "forward",
    "backward",
    "intercept",
    "dispRSqr",
    "dispEq",
    "trendlineLbl",
    "extLst"
})
public class CTTrendline implements Child
{

    protected String name;
    protected CTShapeProperties spPr;
    @XmlElement(required = true)
    protected CTTrendlineType trendlineType;
    protected CTOrder order;
    protected CTPeriod period;
    protected CTDouble forward;
    protected CTDouble backward;
    protected CTDouble intercept;
    protected CTBoolean dispRSqr;
    protected CTBoolean dispEq;
    protected CTTrendlineLbl trendlineLbl;
    protected CTExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the spPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTShapeProperties }
     *     
     */
    public CTShapeProperties getSpPr() {
        return spPr;
    }

    /**
     * Sets the value of the spPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShapeProperties }
     *     
     */
    public void setSpPr(CTShapeProperties value) {
        this.spPr = value;
    }

    /**
     * Gets the value of the trendlineType property.
     * 
     * @return
     *     possible object is
     *     {@link CTTrendlineType }
     *     
     */
    public CTTrendlineType getTrendlineType() {
        return trendlineType;
    }

    /**
     * Sets the value of the trendlineType property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTrendlineType }
     *     
     */
    public void setTrendlineType(CTTrendlineType value) {
        this.trendlineType = value;
    }

    /**
     * Gets the value of the order property.
     * 
     * @return
     *     possible object is
     *     {@link CTOrder }
     *     
     */
    public CTOrder getOrder() {
        return order;
    }

    /**
     * Sets the value of the order property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOrder }
     *     
     */
    public void setOrder(CTOrder value) {
        this.order = value;
    }

    /**
     * Gets the value of the period property.
     * 
     * @return
     *     possible object is
     *     {@link CTPeriod }
     *     
     */
    public CTPeriod getPeriod() {
        return period;
    }

    /**
     * Sets the value of the period property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPeriod }
     *     
     */
    public void setPeriod(CTPeriod value) {
        this.period = value;
    }

    /**
     * Gets the value of the forward property.
     * 
     * @return
     *     possible object is
     *     {@link CTDouble }
     *     
     */
    public CTDouble getForward() {
        return forward;
    }

    /**
     * Sets the value of the forward property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDouble }
     *     
     */
    public void setForward(CTDouble value) {
        this.forward = value;
    }

    /**
     * Gets the value of the backward property.
     * 
     * @return
     *     possible object is
     *     {@link CTDouble }
     *     
     */
    public CTDouble getBackward() {
        return backward;
    }

    /**
     * Sets the value of the backward property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDouble }
     *     
     */
    public void setBackward(CTDouble value) {
        this.backward = value;
    }

    /**
     * Gets the value of the intercept property.
     * 
     * @return
     *     possible object is
     *     {@link CTDouble }
     *     
     */
    public CTDouble getIntercept() {
        return intercept;
    }

    /**
     * Sets the value of the intercept property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDouble }
     *     
     */
    public void setIntercept(CTDouble value) {
        this.intercept = value;
    }

    /**
     * Gets the value of the dispRSqr property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getDispRSqr() {
        return dispRSqr;
    }

    /**
     * Sets the value of the dispRSqr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setDispRSqr(CTBoolean value) {
        this.dispRSqr = value;
    }

    /**
     * Gets the value of the dispEq property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getDispEq() {
        return dispEq;
    }

    /**
     * Sets the value of the dispEq property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setDispEq(CTBoolean value) {
        this.dispEq = value;
    }

    /**
     * Gets the value of the trendlineLbl property.
     * 
     * @return
     *     possible object is
     *     {@link CTTrendlineLbl }
     *     
     */
    public CTTrendlineLbl getTrendlineLbl() {
        return trendlineLbl;
    }

    /**
     * Sets the value of the trendlineLbl property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTrendlineLbl }
     *     
     */
    public void setTrendlineLbl(CTTrendlineLbl value) {
        this.trendlineLbl = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtensionList }
     *     
     */
    public CTExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtensionList }
     *     
     */
    public void setExtLst(CTExtensionList value) {
        this.extLst = value;
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
