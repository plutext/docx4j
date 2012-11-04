/*
 *  Copyright 2010-2012, Plutext Pty Ltd.
 *   
 *  This file is part of pptx4j, a component of docx4j.

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
package org.pptx4j.pml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_BuildList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_BuildList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="bldP" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLBuildParagraph"/>
 *         &lt;element name="bldDgm" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLBuildDiagram"/>
 *         &lt;element name="bldOleChart" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLOleBuildChart"/>
 *         &lt;element name="bldGraphic" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLGraphicalObjectBuild"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_BuildList", propOrder = {
    "bldPOrBldDgmOrBldOleChart"
})
public class CTBuildList {

    @XmlElements({
        @XmlElement(name = "bldP", type = CTTLBuildParagraph.class),
        @XmlElement(name = "bldDgm", type = CTTLBuildDiagram.class),
        @XmlElement(name = "bldOleChart", type = CTTLOleBuildChart.class),
        @XmlElement(name = "bldGraphic", type = CTTLGraphicalObjectBuild.class)
    })
    protected List<Object> bldPOrBldDgmOrBldOleChart;

    /**
     * Gets the value of the bldPOrBldDgmOrBldOleChart property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bldPOrBldDgmOrBldOleChart property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBldPOrBldDgmOrBldOleChart().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTTLBuildParagraph }
     * {@link CTTLBuildDiagram }
     * {@link CTTLOleBuildChart }
     * {@link CTTLGraphicalObjectBuild }
     * 
     * 
     */
    public List<Object> getBldPOrBldDgmOrBldOleChart() {
        if (bldPOrBldDgmOrBldOleChart == null) {
            bldPOrBldDgmOrBldOleChart = new ArrayList<Object>();
        }
        return this.bldPOrBldDgmOrBldOleChart;
    }

}
