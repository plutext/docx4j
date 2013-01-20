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
 * <p>Java class for CT_BookView complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_BookView">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="visibility" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Visibility" default="visible" />
 *       &lt;attribute name="minimized" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showHorizontalScroll" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="showVerticalScroll" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="showSheetTabs" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="xWindow" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="yWindow" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="windowWidth" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="windowHeight" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="tabRatio" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="600" />
 *       &lt;attribute name="firstSheet" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="activeTab" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="autoFilterDateGrouping" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_BookView", propOrder = {
    "extLst"
})
public class CTBookView implements Child
{

    protected CTExtensionList extLst;
    @XmlAttribute(name = "visibility")
    protected STVisibility visibility;
    @XmlAttribute(name = "minimized")
    protected Boolean minimized;
    @XmlAttribute(name = "showHorizontalScroll")
    protected Boolean showHorizontalScroll;
    @XmlAttribute(name = "showVerticalScroll")
    protected Boolean showVerticalScroll;
    @XmlAttribute(name = "showSheetTabs")
    protected Boolean showSheetTabs;
    @XmlAttribute(name = "xWindow")
    protected Integer xWindow;
    @XmlAttribute(name = "yWindow")
    protected Integer yWindow;
    @XmlAttribute(name = "windowWidth")
    @XmlSchemaType(name = "unsignedInt")
    protected Long windowWidth;
    @XmlAttribute(name = "windowHeight")
    @XmlSchemaType(name = "unsignedInt")
    protected Long windowHeight;
    @XmlAttribute(name = "tabRatio")
    @XmlSchemaType(name = "unsignedInt")
    protected Long tabRatio;
    @XmlAttribute(name = "firstSheet")
    @XmlSchemaType(name = "unsignedInt")
    protected Long firstSheet;
    @XmlAttribute(name = "activeTab")
    @XmlSchemaType(name = "unsignedInt")
    protected Long activeTab;
    @XmlAttribute(name = "autoFilterDateGrouping")
    protected Boolean autoFilterDateGrouping;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtensionList }
     *     
     */
    public CTExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtensionList }
     *     
     */
    public void setExtLst(CTExtensionList value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the visibility property.
     * 
     * @return
     *     possible object is
     *     {@link STVisibility }
     *     
     */
    public STVisibility getVisibility() {
        if (visibility == null) {
            return STVisibility.VISIBLE;
        } else {
            return visibility;
        }
    }

    /**
     * Sets the value of the visibility property.
     * 
     * @param value
     *     allowed object is
     *     {@link STVisibility }
     *     
     */
    public void setVisibility(STVisibility value) {
        this.visibility = value;
    }

    /**
     * Gets the value of the minimized property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMinimized() {
        if (minimized == null) {
            return false;
        } else {
            return minimized;
        }
    }

    /**
     * Sets the value of the minimized property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMinimized(Boolean value) {
        this.minimized = value;
    }

    /**
     * Gets the value of the showHorizontalScroll property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowHorizontalScroll() {
        if (showHorizontalScroll == null) {
            return true;
        } else {
            return showHorizontalScroll;
        }
    }

    /**
     * Sets the value of the showHorizontalScroll property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowHorizontalScroll(Boolean value) {
        this.showHorizontalScroll = value;
    }

    /**
     * Gets the value of the showVerticalScroll property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowVerticalScroll() {
        if (showVerticalScroll == null) {
            return true;
        } else {
            return showVerticalScroll;
        }
    }

    /**
     * Sets the value of the showVerticalScroll property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowVerticalScroll(Boolean value) {
        this.showVerticalScroll = value;
    }

    /**
     * Gets the value of the showSheetTabs property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowSheetTabs() {
        if (showSheetTabs == null) {
            return true;
        } else {
            return showSheetTabs;
        }
    }

    /**
     * Sets the value of the showSheetTabs property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowSheetTabs(Boolean value) {
        this.showSheetTabs = value;
    }

    /**
     * Gets the value of the xWindow property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getXWindow() {
        return xWindow;
    }

    /**
     * Sets the value of the xWindow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setXWindow(Integer value) {
        this.xWindow = value;
    }

    /**
     * Gets the value of the yWindow property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getYWindow() {
        return yWindow;
    }

    /**
     * Sets the value of the yWindow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setYWindow(Integer value) {
        this.yWindow = value;
    }

    /**
     * Gets the value of the windowWidth property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getWindowWidth() {
        return windowWidth;
    }

    /**
     * Sets the value of the windowWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setWindowWidth(Long value) {
        this.windowWidth = value;
    }

    /**
     * Gets the value of the windowHeight property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getWindowHeight() {
        return windowHeight;
    }

    /**
     * Sets the value of the windowHeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setWindowHeight(Long value) {
        this.windowHeight = value;
    }

    /**
     * Gets the value of the tabRatio property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getTabRatio() {
        if (tabRatio == null) {
            return  600L;
        } else {
            return tabRatio;
        }
    }

    /**
     * Sets the value of the tabRatio property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTabRatio(Long value) {
        this.tabRatio = value;
    }

    /**
     * Gets the value of the firstSheet property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getFirstSheet() {
        if (firstSheet == null) {
            return  0L;
        } else {
            return firstSheet;
        }
    }

    /**
     * Sets the value of the firstSheet property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setFirstSheet(Long value) {
        this.firstSheet = value;
    }

    /**
     * Gets the value of the activeTab property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getActiveTab() {
        if (activeTab == null) {
            return  0L;
        } else {
            return activeTab;
        }
    }

    /**
     * Sets the value of the activeTab property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setActiveTab(Long value) {
        this.activeTab = value;
    }

    /**
     * Gets the value of the autoFilterDateGrouping property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAutoFilterDateGrouping() {
        if (autoFilterDateGrouping == null) {
            return true;
        } else {
            return autoFilterDateGrouping;
        }
    }

    /**
     * Sets the value of the autoFilterDateGrouping property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoFilterDateGrouping(Boolean value) {
        this.autoFilterDateGrouping = value;
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
