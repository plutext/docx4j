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

import org.docx4j.dml.ArrayListDml;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_FillStyleList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_FillStyleList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_FillProperties" maxOccurs="unbounded" minOccurs="3"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_FillStyleList", propOrder = {
    "egFillProperties"
})
public class CTFillStyleList {

    @XmlElements({
        @XmlElement(name = "grpFill", type = CTGroupFillProperties.class),
        @XmlElement(name = "noFill", type = CTNoFillProperties.class),
        @XmlElement(name = "gradFill", type = CTGradientFillProperties.class),
        @XmlElement(name = "blipFill", type = CTBlipFillProperties.class),
        @XmlElement(name = "solidFill", type = CTSolidColorFillProperties.class),
        @XmlElement(name = "pattFill", type = CTPatternFillProperties.class)
    })
    protected List<Object> egFillProperties = new ArrayListDml<Object>(this);

    /**
     * Gets the value of the egFillProperties property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the egFillProperties property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEGFillProperties().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTGroupFillProperties }
     * {@link CTNoFillProperties }
     * {@link CTGradientFillProperties }
     * {@link CTBlipFillProperties }
     * {@link CTSolidColorFillProperties }
     * {@link CTPatternFillProperties }
     * 
     * 
     */
    public List<Object> getEGFillProperties() {
        if (egFillProperties == null) {
            egFillProperties = new ArrayListDml<Object>(this);
        }
        return this.egFillProperties;
    }

}
