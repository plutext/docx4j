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


package org.docx4j.dml.spreadsheetdrawing;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTShapeStyle;
import org.docx4j.dml.CTTextBody;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Shape complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Shape"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="nvSpPr" type="{http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing}CT_ShapeNonVisual"/&gt;
 *         &lt;element name="spPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeProperties"/&gt;
 *         &lt;element name="style" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeStyle" minOccurs="0"/&gt;
 *         &lt;element name="txBody" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextBody" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="macro" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="textlink" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="fLocksText" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *       &lt;attribute name="fPublished" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Shape", propOrder = {
    "nvSpPr",
    "spPr",
    "style",
    "txBody"
})
public class CTShape implements Child
{

    @XmlElement(required = true)
    protected CTShapeNonVisual nvSpPr;
    @XmlElement(required = true)
    protected CTShapeProperties spPr;
    protected CTShapeStyle style;
    protected CTTextBody txBody;
    @XmlAttribute(name = "macro")
    protected String macro;
    @XmlAttribute(name = "textlink")
    protected String textlink;
    @XmlAttribute(name = "fLocksText")
    protected Boolean fLocksText;
    @XmlAttribute(name = "fPublished")
    protected Boolean fPublished;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the nvSpPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTShapeNonVisual }
     *     
     */
    public CTShapeNonVisual getNvSpPr() {
        return nvSpPr;
    }

    /**
     * Sets the value of the nvSpPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShapeNonVisual }
     *     
     */
    public void setNvSpPr(CTShapeNonVisual value) {
        this.nvSpPr = value;
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
     * Gets the value of the style property.
     * 
     * @return
     *     possible object is
     *     {@link CTShapeStyle }
     *     
     */
    public CTShapeStyle getStyle() {
        return style;
    }

    /**
     * Sets the value of the style property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShapeStyle }
     *     
     */
    public void setStyle(CTShapeStyle value) {
        this.style = value;
    }

    /**
     * Gets the value of the txBody property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextBody }
     *     
     */
    public CTTextBody getTxBody() {
        return txBody;
    }

    /**
     * Sets the value of the txBody property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextBody }
     *     
     */
    public void setTxBody(CTTextBody value) {
        this.txBody = value;
    }

    /**
     * Gets the value of the macro property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMacro() {
        return macro;
    }

    /**
     * Sets the value of the macro property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMacro(String value) {
        this.macro = value;
    }

    /**
     * Gets the value of the textlink property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextlink() {
        return textlink;
    }

    /**
     * Sets the value of the textlink property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextlink(String value) {
        this.textlink = value;
    }

    /**
     * Gets the value of the fLocksText property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFLocksText() {
        if (fLocksText == null) {
            return true;
        } else {
            return fLocksText;
        }
    }

    /**
     * Sets the value of the fLocksText property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFLocksText(Boolean value) {
        this.fLocksText = value;
    }

    /**
     * Gets the value of the fPublished property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFPublished() {
        if (fPublished == null) {
            return false;
        } else {
            return fPublished;
        }
    }

    /**
     * Sets the value of the fPublished property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFPublished(Boolean value) {
        this.fPublished = value;
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
