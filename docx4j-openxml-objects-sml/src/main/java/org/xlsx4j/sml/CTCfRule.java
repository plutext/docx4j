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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_CfRule complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CfRule">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="formula" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Formula" maxOccurs="3" minOccurs="0"/>
 *         &lt;element name="colorScale" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ColorScale" minOccurs="0"/>
 *         &lt;element name="dataBar" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_DataBar" minOccurs="0"/>
 *         &lt;element name="iconSet" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_IconSet" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_CfType" />
 *       &lt;attribute name="dxfId" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_DxfId" />
 *       &lt;attribute name="priority" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="stopIfTrue" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="aboveAverage" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="percent" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="bottom" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="operator" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_ConditionalFormattingOperator" />
 *       &lt;attribute name="text" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="timePeriod" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_TimePeriod" />
 *       &lt;attribute name="rank" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="stdDev" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="equalAverage" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CfRule", propOrder = {
    "formula",
    "colorScale",
    "dataBar",
    "iconSet",
    "extLst"
})
public class CTCfRule implements Child
{

    protected List<String> formula;
    protected CTColorScale colorScale;
    protected CTDataBar dataBar;
    protected CTIconSet iconSet;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "type")
    protected STCfType type;
    @XmlAttribute(name = "dxfId")
    protected Long dxfId;
    @XmlAttribute(name = "priority", required = true)
    protected int priority;
    @XmlAttribute(name = "stopIfTrue")
    protected Boolean stopIfTrue;
    @XmlAttribute(name = "aboveAverage")
    protected Boolean aboveAverage;
    @XmlAttribute(name = "percent")
    protected Boolean percent;
    @XmlAttribute(name = "bottom")
    protected Boolean bottom;
    @XmlAttribute(name = "operator")
    protected STConditionalFormattingOperator operator;
    @XmlAttribute(name = "text")
    protected String text;
    @XmlAttribute(name = "timePeriod")
    protected STTimePeriod timePeriod;
    @XmlAttribute(name = "rank")
    @XmlSchemaType(name = "unsignedInt")
    protected Long rank;
    @XmlAttribute(name = "stdDev")
    protected Integer stdDev;
    @XmlAttribute(name = "equalAverage")
    protected Boolean equalAverage;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the formula property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the formula property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFormula().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getFormula() {
        if (formula == null) {
            formula = new ArrayList<String>();
        }
        return this.formula;
    }

    /**
     * Gets the value of the colorScale property.
     * 
     * @return
     *     possible object is
     *     {@link CTColorScale }
     *     
     */
    public CTColorScale getColorScale() {
        return colorScale;
    }

    /**
     * Sets the value of the colorScale property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColorScale }
     *     
     */
    public void setColorScale(CTColorScale value) {
        this.colorScale = value;
    }

    /**
     * Gets the value of the dataBar property.
     * 
     * @return
     *     possible object is
     *     {@link CTDataBar }
     *     
     */
    public CTDataBar getDataBar() {
        return dataBar;
    }

    /**
     * Sets the value of the dataBar property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDataBar }
     *     
     */
    public void setDataBar(CTDataBar value) {
        this.dataBar = value;
    }

    /**
     * Gets the value of the iconSet property.
     * 
     * @return
     *     possible object is
     *     {@link CTIconSet }
     *     
     */
    public CTIconSet getIconSet() {
        return iconSet;
    }

    /**
     * Sets the value of the iconSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTIconSet }
     *     
     */
    public void setIconSet(CTIconSet value) {
        this.iconSet = value;
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
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STCfType }
     *     
     */
    public STCfType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STCfType }
     *     
     */
    public void setType(STCfType value) {
        this.type = value;
    }

    /**
     * Gets the value of the dxfId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDxfId() {
        return dxfId;
    }

    /**
     * Sets the value of the dxfId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDxfId(Long value) {
        this.dxfId = value;
    }

    /**
     * Gets the value of the priority property.
     * 
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the value of the priority property.
     * 
     */
    public void setPriority(int value) {
        this.priority = value;
    }

    /**
     * Gets the value of the stopIfTrue property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isStopIfTrue() {
        if (stopIfTrue == null) {
            return false;
        } else {
            return stopIfTrue;
        }
    }

    /**
     * Sets the value of the stopIfTrue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setStopIfTrue(Boolean value) {
        this.stopIfTrue = value;
    }

    /**
     * Gets the value of the aboveAverage property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAboveAverage() {
        if (aboveAverage == null) {
            return true;
        } else {
            return aboveAverage;
        }
    }

    /**
     * Sets the value of the aboveAverage property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAboveAverage(Boolean value) {
        this.aboveAverage = value;
    }

    /**
     * Gets the value of the percent property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPercent() {
        if (percent == null) {
            return false;
        } else {
            return percent;
        }
    }

    /**
     * Sets the value of the percent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPercent(Boolean value) {
        this.percent = value;
    }

    /**
     * Gets the value of the bottom property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isBottom() {
        if (bottom == null) {
            return false;
        } else {
            return bottom;
        }
    }

    /**
     * Sets the value of the bottom property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setBottom(Boolean value) {
        this.bottom = value;
    }

    /**
     * Gets the value of the operator property.
     * 
     * @return
     *     possible object is
     *     {@link STConditionalFormattingOperator }
     *     
     */
    public STConditionalFormattingOperator getOperator() {
        return operator;
    }

    /**
     * Sets the value of the operator property.
     * 
     * @param value
     *     allowed object is
     *     {@link STConditionalFormattingOperator }
     *     
     */
    public void setOperator(STConditionalFormattingOperator value) {
        this.operator = value;
    }

    /**
     * Gets the value of the text property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setText(String value) {
        this.text = value;
    }

    /**
     * Gets the value of the timePeriod property.
     * 
     * @return
     *     possible object is
     *     {@link STTimePeriod }
     *     
     */
    public STTimePeriod getTimePeriod() {
        return timePeriod;
    }

    /**
     * Sets the value of the timePeriod property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTimePeriod }
     *     
     */
    public void setTimePeriod(STTimePeriod value) {
        this.timePeriod = value;
    }

    /**
     * Gets the value of the rank property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getRank() {
        return rank;
    }

    /**
     * Sets the value of the rank property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setRank(Long value) {
        this.rank = value;
    }

    /**
     * Gets the value of the stdDev property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getStdDev() {
        return stdDev;
    }

    /**
     * Sets the value of the stdDev property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setStdDev(Integer value) {
        this.stdDev = value;
    }

    /**
     * Gets the value of the equalAverage property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isEqualAverage() {
        if (equalAverage == null) {
            return false;
        } else {
            return equalAverage;
        }
    }

    /**
     * Sets the value of the equalAverage property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEqualAverage(Boolean value) {
        this.equalAverage = value;
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
