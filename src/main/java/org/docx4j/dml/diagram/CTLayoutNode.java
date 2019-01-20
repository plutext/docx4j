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

import org.docx4j.dml.ArrayListDml;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTOfficeArtExtensionList;


/**
 * <p>Java class for CT_LayoutNode complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_LayoutNode">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="alg" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_Algorithm" minOccurs="0"/>
 *         &lt;element name="shape" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_Shape" minOccurs="0"/>
 *         &lt;element name="presOf" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_PresentationOf" minOccurs="0"/>
 *         &lt;element name="constrLst" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_Constraints" minOccurs="0"/>
 *         &lt;element name="ruleLst" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_Rules" minOccurs="0"/>
 *         &lt;element name="varLst" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_LayoutVariablePropertySet" minOccurs="0"/>
 *         &lt;element name="forEach" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_ForEach"/>
 *         &lt;element name="layoutNode" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_LayoutNode"/>
 *         &lt;element name="choose" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_Choose"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/>
 *       &lt;/choice>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" default="" />
 *       &lt;attribute name="styleLbl" type="{http://www.w3.org/2001/XMLSchema}string" default="" />
 *       &lt;attribute name="chOrder" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_ChildOrderType" default="b" />
 *       &lt;attribute name="moveWith" type="{http://www.w3.org/2001/XMLSchema}string" default="" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_LayoutNode", propOrder = {
    "algOrShapeOrPresOf"
})
public class CTLayoutNode {

    @XmlElements({
        @XmlElement(name = "layoutNode", type = CTLayoutNode.class),
        @XmlElement(name = "ruleLst", type = CTRules.class),
        @XmlElement(name = "varLst", type = CTLayoutVariablePropertySet.class),
        @XmlElement(name = "forEach", type = CTForEach.class),
        @XmlElement(name = "extLst", type = CTOfficeArtExtensionList.class),
        @XmlElement(name = "choose", type = CTChoose.class),
        @XmlElement(name = "constrLst", type = CTConstraints.class),
        @XmlElement(name = "presOf", type = CTPresentationOf.class),
        @XmlElement(name = "shape", type = CTShape.class),
        @XmlElement(name = "alg", type = CTAlgorithm.class)
    })
    protected List<Object> algOrShapeOrPresOf = new ArrayListDml<Object>(this);
    @XmlAttribute
    protected String name;
    @XmlAttribute
    protected String styleLbl;
    @XmlAttribute
    protected STChildOrderType chOrder;
    @XmlAttribute
    protected String moveWith;

    /**
     * Gets the value of the algOrShapeOrPresOf property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the algOrShapeOrPresOf property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAlgOrShapeOrPresOf().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTLayoutNode }
     * {@link CTRules }
     * {@link CTLayoutVariablePropertySet }
     * {@link CTForEach }
     * {@link CTOfficeArtExtensionList }
     * {@link CTChoose }
     * {@link CTConstraints }
     * {@link CTPresentationOf }
     * {@link CTShape }
     * {@link CTAlgorithm }
     * 
     * 
     */
    public List<Object> getAlgOrShapeOrPresOf() {
        if (algOrShapeOrPresOf == null) {
            algOrShapeOrPresOf = new ArrayListDml<Object>(this);
        }
        return this.algOrShapeOrPresOf;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        if (name == null) {
            return "";
        } else {
            return name;
        }
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
     * Gets the value of the styleLbl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStyleLbl() {
        if (styleLbl == null) {
            return "";
        } else {
            return styleLbl;
        }
    }

    /**
     * Sets the value of the styleLbl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStyleLbl(String value) {
        this.styleLbl = value;
    }

    /**
     * Gets the value of the chOrder property.
     * 
     * @return
     *     possible object is
     *     {@link STChildOrderType }
     *     
     */
    public STChildOrderType getChOrder() {
        if (chOrder == null) {
            return STChildOrderType.B;
        } else {
            return chOrder;
        }
    }

    /**
     * Sets the value of the chOrder property.
     * 
     * @param value
     *     allowed object is
     *     {@link STChildOrderType }
     *     
     */
    public void setChOrder(STChildOrderType value) {
        this.chOrder = value;
    }

    /**
     * Gets the value of the moveWith property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMoveWith() {
        if (moveWith == null) {
            return "";
        } else {
            return moveWith;
        }
    }

    /**
     * Sets the value of the moveWith property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMoveWith(String value) {
        this.moveWith = value;
    }

}
