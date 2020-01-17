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
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Otherwise complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Otherwise"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="alg" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_Algorithm" minOccurs="0"/&gt;
 *         &lt;element name="shape" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_Shape" minOccurs="0"/&gt;
 *         &lt;element name="presOf" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_PresentationOf" minOccurs="0"/&gt;
 *         &lt;element name="constrLst" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_Constraints" minOccurs="0"/&gt;
 *         &lt;element name="ruleLst" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_Rules" minOccurs="0"/&gt;
 *         &lt;element name="forEach" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_ForEach"/&gt;
 *         &lt;element name="layoutNode" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_LayoutNode"/&gt;
 *         &lt;element name="choose" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}CT_Choose"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" default="" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Otherwise", propOrder = {
    "algOrShapeOrPresOf"
})
public class CTOtherwise implements Child
{

    @XmlElements({
        @XmlElement(name = "alg", type = CTAlgorithm.class),
        @XmlElement(name = "shape", type = CTShape.class),
        @XmlElement(name = "presOf", type = CTPresentationOf.class),
        @XmlElement(name = "constrLst", type = CTConstraints.class),
        @XmlElement(name = "ruleLst", type = CTRules.class),
        @XmlElement(name = "forEach", type = CTForEach.class),
        @XmlElement(name = "layoutNode", type = CTLayoutNode.class),
        @XmlElement(name = "choose", type = CTChoose.class),
        @XmlElement(name = "extLst", type = CTOfficeArtExtensionList.class)
    })
    protected List<Object> algOrShapeOrPresOf = new ArrayListDml<Object>(this);

    @XmlAttribute(name = "name")
    protected String name;
    @XmlTransient
    private Object parent;

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
     * {@link CTAlgorithm }
     * {@link CTShape }
     * {@link CTPresentationOf }
     * {@link CTConstraints }
     * {@link CTRules }
     * {@link CTForEach }
     * {@link CTLayoutNode }
     * {@link CTChoose }
     * {@link CTOfficeArtExtensionList }
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
