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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_SheetView complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SheetView">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pane" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Pane" minOccurs="0"/>
 *         &lt;element name="selection" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Selection" maxOccurs="4" minOccurs="0"/>
 *         &lt;element name="pivotSelection" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PivotSelection" maxOccurs="4" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="windowProtection" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showFormulas" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showGridLines" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="showRowColHeaders" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="showZeros" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="rightToLeft" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="tabSelected" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showRuler" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="showOutlineSymbols" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="defaultGridColor" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="showWhiteSpace" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="view" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_SheetViewType" default="normal" />
 *       &lt;attribute name="topLeftCell" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_CellRef" />
 *       &lt;attribute name="colorId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="64" />
 *       &lt;attribute name="zoomScale" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="100" />
 *       &lt;attribute name="zoomScaleNormal" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="zoomScaleSheetLayoutView" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="zoomScalePageLayoutView" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="workbookViewId" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SheetView", propOrder = {
    "pane",
    "selection",
    "pivotSelection",
    "extLst"
})
@XmlRootElement(name = "sheetView")
public class SheetView implements Child
{

    protected CTPane pane;
    protected List<CTSelection> selection;
    protected List<CTPivotSelection> pivotSelection;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "windowProtection")
    protected Boolean windowProtection;
    @XmlAttribute(name = "showFormulas")
    protected Boolean showFormulas;
    @XmlAttribute(name = "showGridLines")
    protected Boolean showGridLines;
    @XmlAttribute(name = "showRowColHeaders")
    protected Boolean showRowColHeaders;
    @XmlAttribute(name = "showZeros")
    protected Boolean showZeros;
    @XmlAttribute(name = "rightToLeft")
    protected Boolean rightToLeft;
    @XmlAttribute(name = "tabSelected")
    protected Boolean tabSelected;
    @XmlAttribute(name = "showRuler")
    protected Boolean showRuler;
    @XmlAttribute(name = "showOutlineSymbols")
    protected Boolean showOutlineSymbols;
    @XmlAttribute(name = "defaultGridColor")
    protected Boolean defaultGridColor;
    @XmlAttribute(name = "showWhiteSpace")
    protected Boolean showWhiteSpace;
    @XmlAttribute(name = "view")
    protected STSheetViewType view;
    @XmlAttribute(name = "topLeftCell")
    protected String topLeftCell;
    @XmlAttribute(name = "colorId")
    @XmlSchemaType(name = "unsignedInt")
    protected Long colorId;
    @XmlAttribute(name = "zoomScale")
    @XmlSchemaType(name = "unsignedInt")
    protected Long zoomScale;
    @XmlAttribute(name = "zoomScaleNormal")
    @XmlSchemaType(name = "unsignedInt")
    protected Long zoomScaleNormal;
    @XmlAttribute(name = "zoomScaleSheetLayoutView")
    @XmlSchemaType(name = "unsignedInt")
    protected Long zoomScaleSheetLayoutView;
    @XmlAttribute(name = "zoomScalePageLayoutView")
    @XmlSchemaType(name = "unsignedInt")
    protected Long zoomScalePageLayoutView;
    @XmlAttribute(name = "workbookViewId", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long workbookViewId;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the pane property.
     * 
     * @return
     *     possible object is
     *     {@link CTPane }
     *     
     */
    public CTPane getPane() {
        return pane;
    }

    /**
     * Sets the value of the pane property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPane }
     *     
     */
    public void setPane(CTPane value) {
        this.pane = value;
    }

    /**
     * Gets the value of the selection property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the selection property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSelection().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTSelection }
     * 
     * 
     */
    public List<CTSelection> getSelection() {
        if (selection == null) {
            selection = new ArrayList<CTSelection>();
        }
        return this.selection;
    }

    /**
     * Gets the value of the pivotSelection property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pivotSelection property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPivotSelection().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTPivotSelection }
     * 
     * 
     */
    public List<CTPivotSelection> getPivotSelection() {
        if (pivotSelection == null) {
            pivotSelection = new ArrayList<CTPivotSelection>();
        }
        return this.pivotSelection;
    }

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
     * Gets the value of the windowProtection property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isWindowProtection() {
        if (windowProtection == null) {
            return false;
        } else {
            return windowProtection;
        }
    }

    /**
     * Sets the value of the windowProtection property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setWindowProtection(Boolean value) {
        this.windowProtection = value;
    }

    /**
     * Gets the value of the showFormulas property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowFormulas() {
        if (showFormulas == null) {
            return false;
        } else {
            return showFormulas;
        }
    }

    /**
     * Sets the value of the showFormulas property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowFormulas(Boolean value) {
        this.showFormulas = value;
    }

    /**
     * Gets the value of the showGridLines property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowGridLines() {
        if (showGridLines == null) {
            return true;
        } else {
            return showGridLines;
        }
    }

    /**
     * Sets the value of the showGridLines property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowGridLines(Boolean value) {
        this.showGridLines = value;
    }

    /**
     * Gets the value of the showRowColHeaders property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowRowColHeaders() {
        if (showRowColHeaders == null) {
            return true;
        } else {
            return showRowColHeaders;
        }
    }

    /**
     * Sets the value of the showRowColHeaders property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowRowColHeaders(Boolean value) {
        this.showRowColHeaders = value;
    }

    /**
     * Gets the value of the showZeros property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowZeros() {
        if (showZeros == null) {
            return true;
        } else {
            return showZeros;
        }
    }

    /**
     * Sets the value of the showZeros property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowZeros(Boolean value) {
        this.showZeros = value;
    }

    /**
     * Gets the value of the rightToLeft property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRightToLeft() {
        if (rightToLeft == null) {
            return false;
        } else {
            return rightToLeft;
        }
    }

    /**
     * Sets the value of the rightToLeft property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRightToLeft(Boolean value) {
        this.rightToLeft = value;
    }

    /**
     * Gets the value of the tabSelected property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isTabSelected() {
        if (tabSelected == null) {
            return false;
        } else {
            return tabSelected;
        }
    }

    /**
     * Sets the value of the tabSelected property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTabSelected(Boolean value) {
        this.tabSelected = value;
    }

    /**
     * Gets the value of the showRuler property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowRuler() {
        if (showRuler == null) {
            return true;
        } else {
            return showRuler;
        }
    }

    /**
     * Sets the value of the showRuler property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowRuler(Boolean value) {
        this.showRuler = value;
    }

    /**
     * Gets the value of the showOutlineSymbols property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowOutlineSymbols() {
        if (showOutlineSymbols == null) {
            return true;
        } else {
            return showOutlineSymbols;
        }
    }

    /**
     * Sets the value of the showOutlineSymbols property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowOutlineSymbols(Boolean value) {
        this.showOutlineSymbols = value;
    }

    /**
     * Gets the value of the defaultGridColor property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDefaultGridColor() {
        if (defaultGridColor == null) {
            return true;
        } else {
            return defaultGridColor;
        }
    }

    /**
     * Sets the value of the defaultGridColor property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDefaultGridColor(Boolean value) {
        this.defaultGridColor = value;
    }

    /**
     * Gets the value of the showWhiteSpace property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowWhiteSpace() {
        if (showWhiteSpace == null) {
            return true;
        } else {
            return showWhiteSpace;
        }
    }

    /**
     * Sets the value of the showWhiteSpace property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowWhiteSpace(Boolean value) {
        this.showWhiteSpace = value;
    }

    /**
     * Gets the value of the view property.
     * 
     * @return
     *     possible object is
     *     {@link STSheetViewType }
     *     
     */
    public STSheetViewType getView() {
        if (view == null) {
            return STSheetViewType.NORMAL;
        } else {
            return view;
        }
    }

    /**
     * Sets the value of the view property.
     * 
     * @param value
     *     allowed object is
     *     {@link STSheetViewType }
     *     
     */
    public void setView(STSheetViewType value) {
        this.view = value;
    }

    /**
     * Gets the value of the topLeftCell property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTopLeftCell() {
        return topLeftCell;
    }

    /**
     * Sets the value of the topLeftCell property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTopLeftCell(String value) {
        this.topLeftCell = value;
    }

    /**
     * Gets the value of the colorId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getColorId() {
        if (colorId == null) {
            return  64L;
        } else {
            return colorId;
        }
    }

    /**
     * Sets the value of the colorId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setColorId(Long value) {
        this.colorId = value;
    }

    /**
     * Gets the value of the zoomScale property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getZoomScale() {
        if (zoomScale == null) {
            return  100L;
        } else {
            return zoomScale;
        }
    }

    /**
     * Sets the value of the zoomScale property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setZoomScale(Long value) {
        this.zoomScale = value;
    }

    /**
     * Gets the value of the zoomScaleNormal property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getZoomScaleNormal() {
        if (zoomScaleNormal == null) {
            return  0L;
        } else {
            return zoomScaleNormal;
        }
    }

    /**
     * Sets the value of the zoomScaleNormal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setZoomScaleNormal(Long value) {
        this.zoomScaleNormal = value;
    }

    /**
     * Gets the value of the zoomScaleSheetLayoutView property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getZoomScaleSheetLayoutView() {
        if (zoomScaleSheetLayoutView == null) {
            return  0L;
        } else {
            return zoomScaleSheetLayoutView;
        }
    }

    /**
     * Sets the value of the zoomScaleSheetLayoutView property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setZoomScaleSheetLayoutView(Long value) {
        this.zoomScaleSheetLayoutView = value;
    }

    /**
     * Gets the value of the zoomScalePageLayoutView property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getZoomScalePageLayoutView() {
        if (zoomScalePageLayoutView == null) {
            return  0L;
        } else {
            return zoomScalePageLayoutView;
        }
    }

    /**
     * Sets the value of the zoomScalePageLayoutView property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setZoomScalePageLayoutView(Long value) {
        this.zoomScalePageLayoutView = value;
    }

    /**
     * Gets the value of the workbookViewId property.
     * 
     */
    public long getWorkbookViewId() {
        return workbookViewId;
    }

    /**
     * Sets the value of the workbookViewId property.
     * 
     */
    public void setWorkbookViewId(long value) {
        this.workbookViewId = value;
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
