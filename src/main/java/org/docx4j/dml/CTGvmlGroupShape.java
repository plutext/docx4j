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
 * <p>Java class for CT_GvmlGroupShape complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GvmlGroupShape">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="nvGrpSpPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GvmlGroupShapeNonVisual"/>
 *         &lt;element name="grpSpPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GroupShapeProperties"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="txSp" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GvmlTextShape"/>
 *           &lt;element name="sp" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GvmlShape"/>
 *           &lt;element name="cxnSp" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GvmlConnector"/>
 *           &lt;element name="pic" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GvmlPicture"/>
 *           &lt;element name="graphicFrame" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GvmlGraphicalObjectFrame"/>
 *           &lt;element name="grpSp" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GvmlGroupShape"/>
 *         &lt;/choice>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GvmlGroupShape", propOrder = {
    "nvGrpSpPr",
    "grpSpPr",
    "txSpOrSpOrCxnSp",
    "extLst"
})
public class CTGvmlGroupShape {

    @XmlElement(required = true)
    protected CTGvmlGroupShapeNonVisual nvGrpSpPr;
    @XmlElement(required = true)
    protected CTGroupShapeProperties grpSpPr;
    @XmlElements({
        @XmlElement(name = "sp", type = CTGvmlShape.class),
        @XmlElement(name = "grpSp", type = CTGvmlGroupShape.class),
        @XmlElement(name = "cxnSp", type = CTGvmlConnector.class),
        @XmlElement(name = "graphicFrame", type = CTGvmlGraphicalObjectFrame.class),
        @XmlElement(name = "pic", type = CTGvmlPicture.class),
        @XmlElement(name = "txSp", type = CTGvmlTextShape.class)
    })
    protected List<Object> txSpOrSpOrCxnSp = new ArrayListDml<Object>(this);
    protected CTOfficeArtExtensionList extLst;

    /**
     * Gets the value of the nvGrpSpPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTGvmlGroupShapeNonVisual }
     *     
     */
    public CTGvmlGroupShapeNonVisual getNvGrpSpPr() {
        return nvGrpSpPr;
    }

    /**
     * Sets the value of the nvGrpSpPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGvmlGroupShapeNonVisual }
     *     
     */
    public void setNvGrpSpPr(CTGvmlGroupShapeNonVisual value) {
        this.nvGrpSpPr = value;
    }

    /**
     * Gets the value of the grpSpPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTGroupShapeProperties }
     *     
     */
    public CTGroupShapeProperties getGrpSpPr() {
        return grpSpPr;
    }

    /**
     * Sets the value of the grpSpPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGroupShapeProperties }
     *     
     */
    public void setGrpSpPr(CTGroupShapeProperties value) {
        this.grpSpPr = value;
    }

    /**
     * Gets the value of the txSpOrSpOrCxnSp property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the txSpOrSpOrCxnSp property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTxSpOrSpOrCxnSp().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTGvmlShape }
     * {@link CTGvmlGroupShape }
     * {@link CTGvmlConnector }
     * {@link CTGvmlGraphicalObjectFrame }
     * {@link CTGvmlPicture }
     * {@link CTGvmlTextShape }
     * 
     * 
     */
    public List<Object> getTxSpOrSpOrCxnSp() {
        if (txSpOrSpOrCxnSp == null) {
            txSpOrSpOrCxnSp = new ArrayListDml<Object>(this);
        }
        return this.txSpOrSpOrCxnSp;
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

}
