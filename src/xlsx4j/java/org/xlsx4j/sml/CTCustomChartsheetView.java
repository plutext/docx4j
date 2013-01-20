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
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_CustomChartsheetView complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CustomChartsheetView">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pageMargins" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PageMargins" minOccurs="0"/>
 *         &lt;element name="pageSetup" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CsPageSetup" minOccurs="0"/>
 *         &lt;element name="headerFooter" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_HeaderFooter" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="guid" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Guid" />
 *       &lt;attribute name="scale" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="100" />
 *       &lt;attribute name="state" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_SheetState" default="visible" />
 *       &lt;attribute name="zoomToFit" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CustomChartsheetView", propOrder = {
    "pageMargins",
    "pageSetup",
    "headerFooter"
})
public class CTCustomChartsheetView implements Child
{

    protected CTPageMargins pageMargins;
    protected CTCsPageSetup pageSetup;
    protected CTHeaderFooter headerFooter;
    @XmlAttribute(name = "guid", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String guid;
    @XmlAttribute(name = "scale")
    @XmlSchemaType(name = "unsignedInt")
    protected Long scale;
    @XmlAttribute(name = "state")
    protected STSheetState state;
    @XmlAttribute(name = "zoomToFit")
    protected Boolean zoomToFit;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the pageMargins property.
     * 
     * @return
     *     possible object is
     *     {@link CTPageMargins }
     *     
     */
    public CTPageMargins getPageMargins() {
        return pageMargins;
    }

    /**
     * Sets the value of the pageMargins property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPageMargins }
     *     
     */
    public void setPageMargins(CTPageMargins value) {
        this.pageMargins = value;
    }

    /**
     * Gets the value of the pageSetup property.
     * 
     * @return
     *     possible object is
     *     {@link CTCsPageSetup }
     *     
     */
    public CTCsPageSetup getPageSetup() {
        return pageSetup;
    }

    /**
     * Sets the value of the pageSetup property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCsPageSetup }
     *     
     */
    public void setPageSetup(CTCsPageSetup value) {
        this.pageSetup = value;
    }

    /**
     * Gets the value of the headerFooter property.
     * 
     * @return
     *     possible object is
     *     {@link CTHeaderFooter }
     *     
     */
    public CTHeaderFooter getHeaderFooter() {
        return headerFooter;
    }

    /**
     * Sets the value of the headerFooter property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTHeaderFooter }
     *     
     */
    public void setHeaderFooter(CTHeaderFooter value) {
        this.headerFooter = value;
    }

    /**
     * Gets the value of the guid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGuid() {
        return guid;
    }

    /**
     * Sets the value of the guid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGuid(String value) {
        this.guid = value;
    }

    /**
     * Gets the value of the scale property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getScale() {
        if (scale == null) {
            return  100L;
        } else {
            return scale;
        }
    }

    /**
     * Sets the value of the scale property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setScale(Long value) {
        this.scale = value;
    }

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link STSheetState }
     *     
     */
    public STSheetState getState() {
        if (state == null) {
            return STSheetState.VISIBLE;
        } else {
            return state;
        }
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link STSheetState }
     *     
     */
    public void setState(STSheetState value) {
        this.state = value;
    }

    /**
     * Gets the value of the zoomToFit property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isZoomToFit() {
        if (zoomToFit == null) {
            return false;
        } else {
            return zoomToFit;
        }
    }

    /**
     * Sets the value of the zoomToFit property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setZoomToFit(Boolean value) {
        this.zoomToFit = value;
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
