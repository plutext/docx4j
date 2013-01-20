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
 * <p>Java class for CT_CustomWorkbookView complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CustomWorkbookView">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="guid" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Guid" />
 *       &lt;attribute name="autoUpdate" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="mergeInterval" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="changesSavedWin" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="onlySync" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="personalView" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="includePrintSettings" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="includeHiddenRowCol" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="maximized" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="minimized" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showHorizontalScroll" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="showVerticalScroll" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="showSheetTabs" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="xWindow" type="{http://www.w3.org/2001/XMLSchema}int" default="0" />
 *       &lt;attribute name="yWindow" type="{http://www.w3.org/2001/XMLSchema}int" default="0" />
 *       &lt;attribute name="windowWidth" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="windowHeight" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="tabRatio" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="600" />
 *       &lt;attribute name="activeSheetId" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="showFormulaBar" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="showStatusbar" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="showComments" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Comments" default="commIndicator" />
 *       &lt;attribute name="showObjects" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Objects" default="all" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CustomWorkbookView", propOrder = {
    "extLst"
})
public class CTCustomWorkbookView implements Child
{

    protected CTExtensionList extLst;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "guid", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String guid;
    @XmlAttribute(name = "autoUpdate")
    protected Boolean autoUpdate;
    @XmlAttribute(name = "mergeInterval")
    @XmlSchemaType(name = "unsignedInt")
    protected Long mergeInterval;
    @XmlAttribute(name = "changesSavedWin")
    protected Boolean changesSavedWin;
    @XmlAttribute(name = "onlySync")
    protected Boolean onlySync;
    @XmlAttribute(name = "personalView")
    protected Boolean personalView;
    @XmlAttribute(name = "includePrintSettings")
    protected Boolean includePrintSettings;
    @XmlAttribute(name = "includeHiddenRowCol")
    protected Boolean includeHiddenRowCol;
    @XmlAttribute(name = "maximized")
    protected Boolean maximized;
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
    @XmlAttribute(name = "windowWidth", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long windowWidth;
    @XmlAttribute(name = "windowHeight", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long windowHeight;
    @XmlAttribute(name = "tabRatio")
    @XmlSchemaType(name = "unsignedInt")
    protected Long tabRatio;
    @XmlAttribute(name = "activeSheetId", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long activeSheetId;
    @XmlAttribute(name = "showFormulaBar")
    protected Boolean showFormulaBar;
    @XmlAttribute(name = "showStatusbar")
    protected Boolean showStatusbar;
    @XmlAttribute(name = "showComments")
    protected STComments showComments;
    @XmlAttribute(name = "showObjects")
    protected STObjects showObjects;
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
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
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
     * Gets the value of the autoUpdate property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAutoUpdate() {
        if (autoUpdate == null) {
            return false;
        } else {
            return autoUpdate;
        }
    }

    /**
     * Sets the value of the autoUpdate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoUpdate(Boolean value) {
        this.autoUpdate = value;
    }

    /**
     * Gets the value of the mergeInterval property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getMergeInterval() {
        return mergeInterval;
    }

    /**
     * Sets the value of the mergeInterval property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setMergeInterval(Long value) {
        this.mergeInterval = value;
    }

    /**
     * Gets the value of the changesSavedWin property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isChangesSavedWin() {
        if (changesSavedWin == null) {
            return false;
        } else {
            return changesSavedWin;
        }
    }

    /**
     * Sets the value of the changesSavedWin property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setChangesSavedWin(Boolean value) {
        this.changesSavedWin = value;
    }

    /**
     * Gets the value of the onlySync property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOnlySync() {
        if (onlySync == null) {
            return false;
        } else {
            return onlySync;
        }
    }

    /**
     * Sets the value of the onlySync property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOnlySync(Boolean value) {
        this.onlySync = value;
    }

    /**
     * Gets the value of the personalView property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPersonalView() {
        if (personalView == null) {
            return false;
        } else {
            return personalView;
        }
    }

    /**
     * Sets the value of the personalView property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPersonalView(Boolean value) {
        this.personalView = value;
    }

    /**
     * Gets the value of the includePrintSettings property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIncludePrintSettings() {
        if (includePrintSettings == null) {
            return true;
        } else {
            return includePrintSettings;
        }
    }

    /**
     * Sets the value of the includePrintSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludePrintSettings(Boolean value) {
        this.includePrintSettings = value;
    }

    /**
     * Gets the value of the includeHiddenRowCol property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIncludeHiddenRowCol() {
        if (includeHiddenRowCol == null) {
            return true;
        } else {
            return includeHiddenRowCol;
        }
    }

    /**
     * Sets the value of the includeHiddenRowCol property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludeHiddenRowCol(Boolean value) {
        this.includeHiddenRowCol = value;
    }

    /**
     * Gets the value of the maximized property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMaximized() {
        if (maximized == null) {
            return false;
        } else {
            return maximized;
        }
    }

    /**
     * Sets the value of the maximized property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMaximized(Boolean value) {
        this.maximized = value;
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
    public int getXWindow() {
        if (xWindow == null) {
            return  0;
        } else {
            return xWindow;
        }
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
    public int getYWindow() {
        if (yWindow == null) {
            return  0;
        } else {
            return yWindow;
        }
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
     */
    public long getWindowWidth() {
        return windowWidth;
    }

    /**
     * Sets the value of the windowWidth property.
     * 
     */
    public void setWindowWidth(long value) {
        this.windowWidth = value;
    }

    /**
     * Gets the value of the windowHeight property.
     * 
     */
    public long getWindowHeight() {
        return windowHeight;
    }

    /**
     * Sets the value of the windowHeight property.
     * 
     */
    public void setWindowHeight(long value) {
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
     * Gets the value of the activeSheetId property.
     * 
     */
    public long getActiveSheetId() {
        return activeSheetId;
    }

    /**
     * Sets the value of the activeSheetId property.
     * 
     */
    public void setActiveSheetId(long value) {
        this.activeSheetId = value;
    }

    /**
     * Gets the value of the showFormulaBar property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowFormulaBar() {
        if (showFormulaBar == null) {
            return true;
        } else {
            return showFormulaBar;
        }
    }

    /**
     * Sets the value of the showFormulaBar property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowFormulaBar(Boolean value) {
        this.showFormulaBar = value;
    }

    /**
     * Gets the value of the showStatusbar property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowStatusbar() {
        if (showStatusbar == null) {
            return true;
        } else {
            return showStatusbar;
        }
    }

    /**
     * Sets the value of the showStatusbar property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowStatusbar(Boolean value) {
        this.showStatusbar = value;
    }

    /**
     * Gets the value of the showComments property.
     * 
     * @return
     *     possible object is
     *     {@link STComments }
     *     
     */
    public STComments getShowComments() {
        if (showComments == null) {
            return STComments.COMM_INDICATOR;
        } else {
            return showComments;
        }
    }

    /**
     * Sets the value of the showComments property.
     * 
     * @param value
     *     allowed object is
     *     {@link STComments }
     *     
     */
    public void setShowComments(STComments value) {
        this.showComments = value;
    }

    /**
     * Gets the value of the showObjects property.
     * 
     * @return
     *     possible object is
     *     {@link STObjects }
     *     
     */
    public STObjects getShowObjects() {
        if (showObjects == null) {
            return STObjects.ALL;
        } else {
            return showObjects;
        }
    }

    /**
     * Sets the value of the showObjects property.
     * 
     * @param value
     *     allowed object is
     *     {@link STObjects }
     *     
     */
    public void setShowObjects(STObjects value) {
        this.showObjects = value;
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
