/*
 *  Copyright 2010-2013, Plutext Pty Ltd.
 *   
 *  This file is part of xlsx4j, a component of docx4j.

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
package org.xlsx4j.sml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_CalcPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CalcPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="calcId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="calcMode" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_CalcMode" default="auto" />
 *       &lt;attribute name="fullCalcOnLoad" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="refMode" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_RefMode" default="A1" />
 *       &lt;attribute name="iterate" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="iterateCount" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="100" />
 *       &lt;attribute name="iterateDelta" type="{http://www.w3.org/2001/XMLSchema}double" default="0.001" />
 *       &lt;attribute name="fullPrecision" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="calcCompleted" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="calcOnSave" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="concurrentCalc" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="concurrentManualCount" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="forceFullCalc" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CalcPr")
public class CTCalcPr implements Child
{

    @XmlAttribute(name = "calcId")
    @XmlSchemaType(name = "unsignedInt")
    protected Long calcId;
    @XmlAttribute(name = "calcMode")
    protected STCalcMode calcMode;
    @XmlAttribute(name = "fullCalcOnLoad")
    protected Boolean fullCalcOnLoad;
    @XmlAttribute(name = "refMode")
    protected STRefMode refMode;
    @XmlAttribute(name = "iterate")
    protected Boolean iterate;
    @XmlAttribute(name = "iterateCount")
    @XmlSchemaType(name = "unsignedInt")
    protected Long iterateCount;
    @XmlAttribute(name = "iterateDelta")
    protected Double iterateDelta;
    @XmlAttribute(name = "fullPrecision")
    protected Boolean fullPrecision;
    @XmlAttribute(name = "calcCompleted")
    protected Boolean calcCompleted;
    @XmlAttribute(name = "calcOnSave")
    protected Boolean calcOnSave;
    @XmlAttribute(name = "concurrentCalc")
    protected Boolean concurrentCalc;
    @XmlAttribute(name = "concurrentManualCount")
    @XmlSchemaType(name = "unsignedInt")
    protected Long concurrentManualCount;
    @XmlAttribute(name = "forceFullCalc")
    protected Boolean forceFullCalc;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the calcId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCalcId() {
        return calcId;
    }

    /**
     * Sets the value of the calcId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCalcId(Long value) {
        this.calcId = value;
    }

    /**
     * Gets the value of the calcMode property.
     * 
     * @return
     *     possible object is
     *     {@link STCalcMode }
     *     
     */
    public STCalcMode getCalcMode() {
        if (calcMode == null) {
            return STCalcMode.AUTO;
        } else {
            return calcMode;
        }
    }

    /**
     * Sets the value of the calcMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link STCalcMode }
     *     
     */
    public void setCalcMode(STCalcMode value) {
        this.calcMode = value;
    }

    /**
     * Gets the value of the fullCalcOnLoad property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFullCalcOnLoad() {
        if (fullCalcOnLoad == null) {
            return false;
        } else {
            return fullCalcOnLoad;
        }
    }

    /**
     * Sets the value of the fullCalcOnLoad property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFullCalcOnLoad(Boolean value) {
        this.fullCalcOnLoad = value;
    }

    /**
     * Gets the value of the refMode property.
     * 
     * @return
     *     possible object is
     *     {@link STRefMode }
     *     
     */
    public STRefMode getRefMode() {
        if (refMode == null) {
            return STRefMode.A_1;
        } else {
            return refMode;
        }
    }

    /**
     * Sets the value of the refMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link STRefMode }
     *     
     */
    public void setRefMode(STRefMode value) {
        this.refMode = value;
    }

    /**
     * Gets the value of the iterate property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIterate() {
        if (iterate == null) {
            return false;
        } else {
            return iterate;
        }
    }

    /**
     * Sets the value of the iterate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIterate(Boolean value) {
        this.iterate = value;
    }

    /**
     * Gets the value of the iterateCount property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getIterateCount() {
        if (iterateCount == null) {
            return  100L;
        } else {
            return iterateCount;
        }
    }

    /**
     * Sets the value of the iterateCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setIterateCount(Long value) {
        this.iterateCount = value;
    }

    /**
     * Gets the value of the iterateDelta property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public double getIterateDelta() {
        if (iterateDelta == null) {
            return  0.0010D;
        } else {
            return iterateDelta;
        }
    }

    /**
     * Sets the value of the iterateDelta property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setIterateDelta(Double value) {
        this.iterateDelta = value;
    }

    /**
     * Gets the value of the fullPrecision property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFullPrecision() {
        if (fullPrecision == null) {
            return true;
        } else {
            return fullPrecision;
        }
    }

    /**
     * Sets the value of the fullPrecision property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFullPrecision(Boolean value) {
        this.fullPrecision = value;
    }

    /**
     * Gets the value of the calcCompleted property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCalcCompleted() {
        if (calcCompleted == null) {
            return true;
        } else {
            return calcCompleted;
        }
    }

    /**
     * Sets the value of the calcCompleted property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCalcCompleted(Boolean value) {
        this.calcCompleted = value;
    }

    /**
     * Gets the value of the calcOnSave property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCalcOnSave() {
        if (calcOnSave == null) {
            return true;
        } else {
            return calcOnSave;
        }
    }

    /**
     * Sets the value of the calcOnSave property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCalcOnSave(Boolean value) {
        this.calcOnSave = value;
    }

    /**
     * Gets the value of the concurrentCalc property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isConcurrentCalc() {
        if (concurrentCalc == null) {
            return true;
        } else {
            return concurrentCalc;
        }
    }

    /**
     * Sets the value of the concurrentCalc property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setConcurrentCalc(Boolean value) {
        this.concurrentCalc = value;
    }

    /**
     * Gets the value of the concurrentManualCount property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getConcurrentManualCount() {
        return concurrentManualCount;
    }

    /**
     * Sets the value of the concurrentManualCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setConcurrentManualCount(Long value) {
        this.concurrentManualCount = value;
    }

    /**
     * Gets the value of the forceFullCalc property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isForceFullCalc() {
        return forceFullCalc;
    }

    /**
     * Sets the value of the forceFullCalc property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setForceFullCalc(Boolean value) {
        this.forceFullCalc = value;
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
