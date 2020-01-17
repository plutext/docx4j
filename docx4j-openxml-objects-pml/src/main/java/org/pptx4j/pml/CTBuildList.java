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
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_BuildList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_BuildList"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="bldP" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLBuildParagraph"/&gt;
 *         &lt;element name="bldDgm" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLBuildDiagram"/&gt;
 *         &lt;element name="bldOleChart" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLOleBuildChart"/&gt;
 *         &lt;element name="bldGraphic" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLGraphicalObjectBuild"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_BuildList", propOrder = {
    "bldPOrBldDgmOrBldOleChart"
})
public class CTBuildList implements Child
{

    @XmlElements({
        @XmlElement(name = "bldP", type = CTTLBuildParagraph.class),
        @XmlElement(name = "bldDgm", type = CTTLBuildDiagram.class),
        @XmlElement(name = "bldOleChart", type = CTTLOleBuildChart.class),
        @XmlElement(name = "bldGraphic", type = CTTLGraphicalObjectBuild.class)
    })
    protected List<Object> bldPOrBldDgmOrBldOleChart;
    @XmlTransient
    private Object parent;

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
