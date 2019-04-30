/*
 *  Copyright 2010-2012, Plutext Pty Ltd.
 *   
 *  This file is part of pptx4j, a component of docx4j.

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
package org.pptx4j.pml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TLCommonTimeNodeData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLCommonTimeNodeData"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="stCondLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLTimeConditionList" minOccurs="0"/&gt;
 *         &lt;element name="endCondLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLTimeConditionList" minOccurs="0"/&gt;
 *         &lt;element name="endSync" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLTimeCondition" minOccurs="0"/&gt;
 *         &lt;element name="iterate" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLIterateData" minOccurs="0"/&gt;
 *         &lt;element name="childTnLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TimeNodeList" minOccurs="0"/&gt;
 *         &lt;element name="subTnLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TimeNodeList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLTimeNodeID" /&gt;
 *       &lt;attribute name="presetID" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="presetClass" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLTimeNodePresetClassType" /&gt;
 *       &lt;attribute name="presetSubtype" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="dur" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLTime" /&gt;
 *       &lt;attribute name="repeatCount" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLTime" default="1000" /&gt;
 *       &lt;attribute name="repeatDur" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLTime" /&gt;
 *       &lt;attribute name="spd" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" default="100" /&gt;
 *       &lt;attribute name="accel" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedPercentage" default="0" /&gt;
 *       &lt;attribute name="decel" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedPercentage" default="0" /&gt;
 *       &lt;attribute name="autoRev" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="restart" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLTimeNodeRestartType" /&gt;
 *       &lt;attribute name="fill" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLTimeNodeFillType" /&gt;
 *       &lt;attribute name="syncBehavior" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLTimeNodeSyncType" /&gt;
 *       &lt;attribute name="tmFilter" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="evtFilter" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="display" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="masterRel" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLTimeNodeMasterRelation" /&gt;
 *       &lt;attribute name="bldLvl" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="grpId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" /&gt;
 *       &lt;attribute name="afterEffect" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="nodeType" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLTimeNodeType" /&gt;
 *       &lt;attribute name="nodePh" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLCommonTimeNodeData", propOrder = {
    "stCondLst",
    "endCondLst",
    "endSync",
    "iterate",
    "childTnLst",
    "subTnLst"
})
public class CTTLCommonTimeNodeData implements Child
{

    protected CTTLTimeConditionList stCondLst;
    protected CTTLTimeConditionList endCondLst;
    protected CTTLTimeCondition endSync;
    protected CTTLIterateData iterate;
    protected CTTimeNodeList childTnLst;
    protected CTTimeNodeList subTnLst;
    @XmlAttribute(name = "id")
    protected Long id;
    @XmlAttribute(name = "presetID")
    protected Integer presetID;
    @XmlAttribute(name = "presetClass")
    protected STTLTimeNodePresetClassType presetClass;
    @XmlAttribute(name = "presetSubtype")
    protected Integer presetSubtype;
    @XmlAttribute(name = "dur")
    protected String dur;
    @XmlAttribute(name = "repeatCount")
    protected String repeatCount;
    @XmlAttribute(name = "repeatDur")
    protected String repeatDur;
    @XmlAttribute(name = "spd")
    protected Integer spd;
    @XmlAttribute(name = "accel")
    protected Integer accel;
    @XmlAttribute(name = "decel")
    protected Integer decel;
    @XmlAttribute(name = "autoRev")
    protected Boolean autoRev;
    @XmlAttribute(name = "restart")
    protected STTLTimeNodeRestartType restart;
    @XmlAttribute(name = "fill")
    protected STTLTimeNodeFillType fill;
    @XmlAttribute(name = "syncBehavior")
    protected STTLTimeNodeSyncType syncBehavior;
    @XmlAttribute(name = "tmFilter")
    protected String tmFilter;
    @XmlAttribute(name = "evtFilter")
    protected String evtFilter;
    @XmlAttribute(name = "display")
    protected Boolean display;
    @XmlAttribute(name = "masterRel")
    protected STTLTimeNodeMasterRelation masterRel;
    @XmlAttribute(name = "bldLvl")
    protected Integer bldLvl;
    @XmlAttribute(name = "grpId")
    @XmlSchemaType(name = "unsignedInt")
    protected Long grpId;
    @XmlAttribute(name = "afterEffect")
    protected Boolean afterEffect;
    @XmlAttribute(name = "nodeType")
    protected STTLTimeNodeType nodeType;
    @XmlAttribute(name = "nodePh")
    protected Boolean nodePh;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the stCondLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLTimeConditionList }
     *     
     */
    public CTTLTimeConditionList getStCondLst() {
        return stCondLst;
    }

    /**
     * Sets the value of the stCondLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLTimeConditionList }
     *     
     */
    public void setStCondLst(CTTLTimeConditionList value) {
        this.stCondLst = value;
    }

    /**
     * Gets the value of the endCondLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLTimeConditionList }
     *     
     */
    public CTTLTimeConditionList getEndCondLst() {
        return endCondLst;
    }

    /**
     * Sets the value of the endCondLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLTimeConditionList }
     *     
     */
    public void setEndCondLst(CTTLTimeConditionList value) {
        this.endCondLst = value;
    }

    /**
     * Gets the value of the endSync property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLTimeCondition }
     *     
     */
    public CTTLTimeCondition getEndSync() {
        return endSync;
    }

    /**
     * Sets the value of the endSync property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLTimeCondition }
     *     
     */
    public void setEndSync(CTTLTimeCondition value) {
        this.endSync = value;
    }

    /**
     * Gets the value of the iterate property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLIterateData }
     *     
     */
    public CTTLIterateData getIterate() {
        return iterate;
    }

    /**
     * Sets the value of the iterate property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLIterateData }
     *     
     */
    public void setIterate(CTTLIterateData value) {
        this.iterate = value;
    }

    /**
     * Gets the value of the childTnLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTTimeNodeList }
     *     
     */
    public CTTimeNodeList getChildTnLst() {
        return childTnLst;
    }

    /**
     * Sets the value of the childTnLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTimeNodeList }
     *     
     */
    public void setChildTnLst(CTTimeNodeList value) {
        this.childTnLst = value;
    }

    /**
     * Gets the value of the subTnLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTTimeNodeList }
     *     
     */
    public CTTimeNodeList getSubTnLst() {
        return subTnLst;
    }

    /**
     * Sets the value of the subTnLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTimeNodeList }
     *     
     */
    public void setSubTnLst(CTTimeNodeList value) {
        this.subTnLst = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setId(Long value) {
        this.id = value;
    }

    /**
     * Gets the value of the presetID property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPresetID() {
        return presetID;
    }

    /**
     * Sets the value of the presetID property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPresetID(Integer value) {
        this.presetID = value;
    }

    /**
     * Gets the value of the presetClass property.
     * 
     * @return
     *     possible object is
     *     {@link STTLTimeNodePresetClassType }
     *     
     */
    public STTLTimeNodePresetClassType getPresetClass() {
        return presetClass;
    }

    /**
     * Sets the value of the presetClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTLTimeNodePresetClassType }
     *     
     */
    public void setPresetClass(STTLTimeNodePresetClassType value) {
        this.presetClass = value;
    }

    /**
     * Gets the value of the presetSubtype property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPresetSubtype() {
        return presetSubtype;
    }

    /**
     * Sets the value of the presetSubtype property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPresetSubtype(Integer value) {
        this.presetSubtype = value;
    }

    /**
     * Gets the value of the dur property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDur() {
        return dur;
    }

    /**
     * Sets the value of the dur property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDur(String value) {
        this.dur = value;
    }

    /**
     * Gets the value of the repeatCount property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRepeatCount() {
        if (repeatCount == null) {
            return "1000";
        } else {
            return repeatCount;
        }
    }

    /**
     * Sets the value of the repeatCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRepeatCount(String value) {
        this.repeatCount = value;
    }

    /**
     * Gets the value of the repeatDur property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRepeatDur() {
        return repeatDur;
    }

    /**
     * Sets the value of the repeatDur property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRepeatDur(String value) {
        this.repeatDur = value;
    }

    /**
     * Gets the value of the spd property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getSpd() {
        if (spd == null) {
            return  100;
        } else {
            return spd;
        }
    }

    /**
     * Sets the value of the spd property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSpd(Integer value) {
        this.spd = value;
    }

    /**
     * Gets the value of the accel property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getAccel() {
        if (accel == null) {
            return  0;
        } else {
            return accel;
        }
    }

    /**
     * Sets the value of the accel property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAccel(Integer value) {
        this.accel = value;
    }

    /**
     * Gets the value of the decel property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getDecel() {
        if (decel == null) {
            return  0;
        } else {
            return decel;
        }
    }

    /**
     * Sets the value of the decel property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDecel(Integer value) {
        this.decel = value;
    }

    /**
     * Gets the value of the autoRev property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAutoRev() {
        if (autoRev == null) {
            return false;
        } else {
            return autoRev;
        }
    }

    /**
     * Sets the value of the autoRev property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoRev(Boolean value) {
        this.autoRev = value;
    }

    /**
     * Gets the value of the restart property.
     * 
     * @return
     *     possible object is
     *     {@link STTLTimeNodeRestartType }
     *     
     */
    public STTLTimeNodeRestartType getRestart() {
        return restart;
    }

    /**
     * Sets the value of the restart property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTLTimeNodeRestartType }
     *     
     */
    public void setRestart(STTLTimeNodeRestartType value) {
        this.restart = value;
    }

    /**
     * Gets the value of the fill property.
     * 
     * @return
     *     possible object is
     *     {@link STTLTimeNodeFillType }
     *     
     */
    public STTLTimeNodeFillType getFill() {
        return fill;
    }

    /**
     * Sets the value of the fill property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTLTimeNodeFillType }
     *     
     */
    public void setFill(STTLTimeNodeFillType value) {
        this.fill = value;
    }

    /**
     * Gets the value of the syncBehavior property.
     * 
     * @return
     *     possible object is
     *     {@link STTLTimeNodeSyncType }
     *     
     */
    public STTLTimeNodeSyncType getSyncBehavior() {
        return syncBehavior;
    }

    /**
     * Sets the value of the syncBehavior property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTLTimeNodeSyncType }
     *     
     */
    public void setSyncBehavior(STTLTimeNodeSyncType value) {
        this.syncBehavior = value;
    }

    /**
     * Gets the value of the tmFilter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTmFilter() {
        return tmFilter;
    }

    /**
     * Sets the value of the tmFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTmFilter(String value) {
        this.tmFilter = value;
    }

    /**
     * Gets the value of the evtFilter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEvtFilter() {
        return evtFilter;
    }

    /**
     * Sets the value of the evtFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEvtFilter(String value) {
        this.evtFilter = value;
    }

    /**
     * Gets the value of the display property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDisplay() {
        return display;
    }

    /**
     * Sets the value of the display property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDisplay(Boolean value) {
        this.display = value;
    }

    /**
     * Gets the value of the masterRel property.
     * 
     * @return
     *     possible object is
     *     {@link STTLTimeNodeMasterRelation }
     *     
     */
    public STTLTimeNodeMasterRelation getMasterRel() {
        return masterRel;
    }

    /**
     * Sets the value of the masterRel property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTLTimeNodeMasterRelation }
     *     
     */
    public void setMasterRel(STTLTimeNodeMasterRelation value) {
        this.masterRel = value;
    }

    /**
     * Gets the value of the bldLvl property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getBldLvl() {
        return bldLvl;
    }

    /**
     * Sets the value of the bldLvl property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setBldLvl(Integer value) {
        this.bldLvl = value;
    }

    /**
     * Gets the value of the grpId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getGrpId() {
        return grpId;
    }

    /**
     * Sets the value of the grpId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setGrpId(Long value) {
        this.grpId = value;
    }

    /**
     * Gets the value of the afterEffect property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAfterEffect() {
        return afterEffect;
    }

    /**
     * Sets the value of the afterEffect property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAfterEffect(Boolean value) {
        this.afterEffect = value;
    }

    /**
     * Gets the value of the nodeType property.
     * 
     * @return
     *     possible object is
     *     {@link STTLTimeNodeType }
     *     
     */
    public STTLTimeNodeType getNodeType() {
        return nodeType;
    }

    /**
     * Sets the value of the nodeType property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTLTimeNodeType }
     *     
     */
    public void setNodeType(STTLTimeNodeType value) {
        this.nodeType = value;
    }

    /**
     * Gets the value of the nodePh property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isNodePh() {
        return nodePh;
    }

    /**
     * Sets the value of the nodePh property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNodePh(Boolean value) {
        this.nodePh = value;
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
