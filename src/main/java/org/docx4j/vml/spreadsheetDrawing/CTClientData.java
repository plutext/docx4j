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


package org.docx4j.vml.spreadsheetDrawing;

import java.math.BigInteger;
import org.docx4j.vml.ArrayListVml;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ClientData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ClientData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="MoveWithCells" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="SizeWithCells" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="Anchor" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Locked" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="DefaultSize" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="PrintObject" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="Disabled" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="AutoFill" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="AutoLine" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="AutoPict" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="FmlaMacro" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="TextHAlign" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="TextVAlign" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="LockText" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="JustLastX" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="SecretEdit" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="Default" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="Help" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="Cancel" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="Dismiss" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="Accel" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="Accel2" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="Row" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="Column" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="Visible" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="RowHidden" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="ColHidden" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="VTEdit" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="MultiLine" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="VScroll" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="ValidIds" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="FmlaRange" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="WidthMin" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="Sel" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="NoThreeD2" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="SelType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="MultiSel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="LCT" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ListItem" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="DropStyle" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Colored" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="DropLines" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="Checked" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="FmlaLink" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="FmlaPict" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="NoThreeD" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="FirstButton" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="FmlaGroup" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Val" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="Min" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="Max" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="Inc" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="Page" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="Horiz" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="Dx" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="MapOCX" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="CF" type="{urn:schemas-microsoft-com:office:excel}ST_CF"/>
 *         &lt;element name="Camera" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="RecalcAlways" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="AutoScale" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="DDE" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="UIObj" type="{urn:schemas-microsoft-com:office:excel}ST_TrueFalseBlank"/>
 *         &lt;element name="ScriptText" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ScriptExtended" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ScriptLanguage" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *         &lt;element name="ScriptLocation" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *         &lt;element name="FmlaTxbx" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *       &lt;attribute name="ObjectType" use="required" type="{urn:schemas-microsoft-com:office:excel}ST_ObjectType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ClientData", propOrder = {
    "moveWithCellsOrSizeWithCellsOrAnchor"
})
public class CTClientData implements Child
{

    @XmlElementRefs({
        @XmlElementRef(name = "Checked", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "VScroll", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "Anchor", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "ListItem", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "AutoPict", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "ScriptExtended", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "Max", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "SizeWithCells", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "Horiz", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "FmlaTxbx", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "Accel2", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "MultiSel", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "UIObj", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "FmlaPict", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "LCT", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "JustLastX", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "DropStyle", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "Colored", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "Visible", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "NoThreeD", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "PrintObject", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "SelType", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "Accel", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "Default", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "DDE", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "VTEdit", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "Help", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "Sel", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "Column", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "FmlaMacro", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "TextHAlign", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "DropLines", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "RowHidden", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "Dismiss", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "Disabled", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "DefaultSize", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "ScriptText", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "TextVAlign", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "ValidIds", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "MoveWithCells", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "FmlaGroup", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "Min", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "LockText", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "SecretEdit", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "FirstButton", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "Locked", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "MapOCX", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "FmlaLink", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "ColHidden", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "Camera", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "Inc", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "AutoScale", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "AutoLine", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "Row", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "NoThreeD2", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "Cancel", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "Val", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "CF", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "Dx", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "WidthMin", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "ScriptLocation", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "ScriptLanguage", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "MultiLine", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "AutoFill", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "RecalcAlways", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "FmlaRange", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "Page", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class)
    })
    protected List<JAXBElement<?>> moveWithCellsOrSizeWithCellsOrAnchor = new ArrayListVml<JAXBElement<?>>(this);
    @XmlAttribute(name = "ObjectType", required = true)
    protected STObjectType objectType;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the moveWithCellsOrSizeWithCellsOrAnchor property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the moveWithCellsOrSizeWithCellsOrAnchor property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMoveWithCellsOrSizeWithCellsOrAnchor().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link STCF }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getMoveWithCellsOrSizeWithCellsOrAnchor() {
        if (moveWithCellsOrSizeWithCellsOrAnchor == null) {
            moveWithCellsOrSizeWithCellsOrAnchor = new ArrayListVml<JAXBElement<?>>(this);
        }
        return this.moveWithCellsOrSizeWithCellsOrAnchor;
    }

    /**
     * Gets the value of the objectType property.
     * 
     * @return
     *     possible object is
     *     {@link STObjectType }
     *     
     */
    public STObjectType getObjectType() {
        return objectType;
    }

    /**
     * Sets the value of the objectType property.
     * 
     * @param value
     *     allowed object is
     *     {@link STObjectType }
     *     
     */
    public void setObjectType(STObjectType value) {
        this.objectType = value;
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
