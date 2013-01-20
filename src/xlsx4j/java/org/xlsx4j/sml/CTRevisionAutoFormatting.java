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
 * <p>Java class for CT_RevisionAutoFormatting complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_RevisionAutoFormatting">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attGroup ref="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}AG_AutoFormat"/>
 *       &lt;attribute name="sheetId" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="ref" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Ref" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_RevisionAutoFormatting")
public class CTRevisionAutoFormatting implements Child
{

    @XmlAttribute(name = "sheetId", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long sheetId;
    @XmlAttribute(name = "ref", required = true)
    protected String ref;
    @XmlAttribute(name = "autoFormatId")
    @XmlSchemaType(name = "unsignedInt")
    protected Long autoFormatId;
    @XmlAttribute(name = "applyNumberFormats")
    protected Boolean applyNumberFormats;
    @XmlAttribute(name = "applyBorderFormats")
    protected Boolean applyBorderFormats;
    @XmlAttribute(name = "applyFontFormats")
    protected Boolean applyFontFormats;
    @XmlAttribute(name = "applyPatternFormats")
    protected Boolean applyPatternFormats;
    @XmlAttribute(name = "applyAlignmentFormats")
    protected Boolean applyAlignmentFormats;
    @XmlAttribute(name = "applyWidthHeightFormats")
    protected Boolean applyWidthHeightFormats;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the sheetId property.
     * 
     */
    public long getSheetId() {
        return sheetId;
    }

    /**
     * Sets the value of the sheetId property.
     * 
     */
    public void setSheetId(long value) {
        this.sheetId = value;
    }

    /**
     * Gets the value of the ref property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRef() {
        return ref;
    }

    /**
     * Sets the value of the ref property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRef(String value) {
        this.ref = value;
    }

    /**
     * Gets the value of the autoFormatId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getAutoFormatId() {
        return autoFormatId;
    }

    /**
     * Sets the value of the autoFormatId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setAutoFormatId(Long value) {
        this.autoFormatId = value;
    }

    /**
     * Gets the value of the applyNumberFormats property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyNumberFormats() {
        return applyNumberFormats;
    }

    /**
     * Sets the value of the applyNumberFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyNumberFormats(Boolean value) {
        this.applyNumberFormats = value;
    }

    /**
     * Gets the value of the applyBorderFormats property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyBorderFormats() {
        return applyBorderFormats;
    }

    /**
     * Sets the value of the applyBorderFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyBorderFormats(Boolean value) {
        this.applyBorderFormats = value;
    }

    /**
     * Gets the value of the applyFontFormats property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyFontFormats() {
        return applyFontFormats;
    }

    /**
     * Sets the value of the applyFontFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyFontFormats(Boolean value) {
        this.applyFontFormats = value;
    }

    /**
     * Gets the value of the applyPatternFormats property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyPatternFormats() {
        return applyPatternFormats;
    }

    /**
     * Sets the value of the applyPatternFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyPatternFormats(Boolean value) {
        this.applyPatternFormats = value;
    }

    /**
     * Gets the value of the applyAlignmentFormats property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyAlignmentFormats() {
        return applyAlignmentFormats;
    }

    /**
     * Sets the value of the applyAlignmentFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyAlignmentFormats(Boolean value) {
        this.applyAlignmentFormats = value;
    }

    /**
     * Gets the value of the applyWidthHeightFormats property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyWidthHeightFormats() {
        return applyWidthHeightFormats;
    }

    /**
     * Sets the value of the applyWidthHeightFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyWidthHeightFormats(Boolean value) {
        this.applyWidthHeightFormats = value;
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
