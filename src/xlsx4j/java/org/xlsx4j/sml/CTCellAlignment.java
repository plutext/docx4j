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
 * <p>Java class for CT_CellAlignment complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CellAlignment">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="horizontal" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_HorizontalAlignment" />
 *       &lt;attribute name="vertical" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_VerticalAlignment" />
 *       &lt;attribute name="textRotation" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="wrapText" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="indent" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="relativeIndent" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="justifyLastLine" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="shrinkToFit" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="readingOrder" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CellAlignment")
public class CTCellAlignment implements Child
{

    @XmlAttribute(name = "horizontal")
    protected STHorizontalAlignment horizontal;
    @XmlAttribute(name = "vertical")
    protected STVerticalAlignment vertical;
    @XmlAttribute(name = "textRotation")
    @XmlSchemaType(name = "unsignedInt")
    protected Long textRotation;
    @XmlAttribute(name = "wrapText")
    protected Boolean wrapText;
    @XmlAttribute(name = "indent")
    @XmlSchemaType(name = "unsignedInt")
    protected Long indent;
    @XmlAttribute(name = "relativeIndent")
    protected Integer relativeIndent;
    @XmlAttribute(name = "justifyLastLine")
    protected Boolean justifyLastLine;
    @XmlAttribute(name = "shrinkToFit")
    protected Boolean shrinkToFit;
    @XmlAttribute(name = "readingOrder")
    @XmlSchemaType(name = "unsignedInt")
    protected Long readingOrder;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the horizontal property.
     * 
     * @return
     *     possible object is
     *     {@link STHorizontalAlignment }
     *     
     */
    public STHorizontalAlignment getHorizontal() {
        return horizontal;
    }

    /**
     * Sets the value of the horizontal property.
     * 
     * @param value
     *     allowed object is
     *     {@link STHorizontalAlignment }
     *     
     */
    public void setHorizontal(STHorizontalAlignment value) {
        this.horizontal = value;
    }

    /**
     * Gets the value of the vertical property.
     * 
     * @return
     *     possible object is
     *     {@link STVerticalAlignment }
     *     
     */
    public STVerticalAlignment getVertical() {
        return vertical;
    }

    /**
     * Sets the value of the vertical property.
     * 
     * @param value
     *     allowed object is
     *     {@link STVerticalAlignment }
     *     
     */
    public void setVertical(STVerticalAlignment value) {
        this.vertical = value;
    }

    /**
     * Gets the value of the textRotation property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getTextRotation() {
        return textRotation;
    }

    /**
     * Sets the value of the textRotation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTextRotation(Long value) {
        this.textRotation = value;
    }

    /**
     * Gets the value of the wrapText property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isWrapText() {
        return wrapText;
    }

    /**
     * Sets the value of the wrapText property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setWrapText(Boolean value) {
        this.wrapText = value;
    }

    /**
     * Gets the value of the indent property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getIndent() {
        return indent;
    }

    /**
     * Sets the value of the indent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setIndent(Long value) {
        this.indent = value;
    }

    /**
     * Gets the value of the relativeIndent property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getRelativeIndent() {
        return relativeIndent;
    }

    /**
     * Sets the value of the relativeIndent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRelativeIndent(Integer value) {
        this.relativeIndent = value;
    }

    /**
     * Gets the value of the justifyLastLine property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isJustifyLastLine() {
        return justifyLastLine;
    }

    /**
     * Sets the value of the justifyLastLine property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setJustifyLastLine(Boolean value) {
        this.justifyLastLine = value;
    }

    /**
     * Gets the value of the shrinkToFit property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isShrinkToFit() {
        return shrinkToFit;
    }

    /**
     * Sets the value of the shrinkToFit property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShrinkToFit(Boolean value) {
        this.shrinkToFit = value;
    }

    /**
     * Gets the value of the readingOrder property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getReadingOrder() {
        return readingOrder;
    }

    /**
     * Sets the value of the readingOrder property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setReadingOrder(Long value) {
        this.readingOrder = value;
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
