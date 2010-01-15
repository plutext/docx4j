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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_AnimationElementChoice complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_AnimationElementChoice">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="dgm" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_AnimationDgmElement"/>
 *         &lt;element name="chart" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_AnimationChartElement"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_AnimationElementChoice", propOrder = {
    "dgm",
    "chart"
})
public class CTAnimationElementChoice {

    protected CTAnimationDgmElement dgm;
    protected CTAnimationChartElement chart;

    /**
     * Gets the value of the dgm property.
     * 
     * @return
     *     possible object is
     *     {@link CTAnimationDgmElement }
     *     
     */
    public CTAnimationDgmElement getDgm() {
        return dgm;
    }

    /**
     * Sets the value of the dgm property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAnimationDgmElement }
     *     
     */
    public void setDgm(CTAnimationDgmElement value) {
        this.dgm = value;
    }

    /**
     * Gets the value of the chart property.
     * 
     * @return
     *     possible object is
     *     {@link CTAnimationChartElement }
     *     
     */
    public CTAnimationChartElement getChart() {
        return chart;
    }

    /**
     * Sets the value of the chart property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAnimationChartElement }
     *     
     */
    public void setChart(CTAnimationChartElement value) {
        this.chart = value;
    }

}
