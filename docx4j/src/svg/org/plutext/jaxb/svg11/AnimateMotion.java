/*
 *  Copyright 2010, Plutext Pty Ltd.
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


package org.plutext.jaxb.svg11;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.w3.org/2000/svg}desc"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}title"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}metadata"/>
 *         &lt;/choice>
 *         &lt;element ref="{http://www.w3.org/2000/svg}mpath" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.AnimationTiming.attrib"/>
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.Animation.attrib"/>
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.AnimationAddtion.attrib"/>
 *       &lt;attribute name="calcMode" default="paced">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="discrete"/>
 *             &lt;enumeration value="linear"/>
 *             &lt;enumeration value="paced"/>
 *             &lt;enumeration value="spline"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="values" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="keyTimes" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="keySplines" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="from" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="to" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="by" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="path" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="keyPoints" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="rotate" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="origin" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "descOrTitleOrMetadata",
    "mpath"
})
@XmlRootElement(name = "animateMotion")
public class AnimateMotion {

    @XmlElements({
        @XmlElement(name = "desc", type = Desc.class),
        @XmlElement(name = "metadata", type = Metadata.class),
        @XmlElement(name = "title", type = Title.class)
    })
    protected List<Object> descOrTitleOrMetadata;
    protected Mpath mpath;
    @XmlAttribute
    protected String calcMode;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String values;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String keyTimes;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String keySplines;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String from;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String to;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String by;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String path;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String keyPoints;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String rotate;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String origin;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String begin;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String dur;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String end;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String min;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String max;
    @XmlAttribute
    protected String restart;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String repeatCount;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String repeatDur;
    @XmlAttribute
    protected String fill;
    @XmlAttribute
    protected String additive;
    @XmlAttribute
    protected String accumulate;

    /**
     * Gets the value of the descOrTitleOrMetadata property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the descOrTitleOrMetadata property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDescOrTitleOrMetadata().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Desc }
     * {@link Metadata }
     * {@link Title }
     * 
     * 
     */
    public List<Object> getDescOrTitleOrMetadata() {
        if (descOrTitleOrMetadata == null) {
            descOrTitleOrMetadata = new ArrayList<Object>();
        }
        return this.descOrTitleOrMetadata;
    }

    /**
     * Gets the value of the mpath property.
     * 
     * @return
     *     possible object is
     *     {@link Mpath }
     *     
     */
    public Mpath getMpath() {
        return mpath;
    }

    /**
     * Sets the value of the mpath property.
     * 
     * @param value
     *     allowed object is
     *     {@link Mpath }
     *     
     */
    public void setMpath(Mpath value) {
        this.mpath = value;
    }

    /**
     * Gets the value of the calcMode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCalcMode() {
        if (calcMode == null) {
            return "paced";
        } else {
            return calcMode;
        }
    }

    /**
     * Sets the value of the calcMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCalcMode(String value) {
        this.calcMode = value;
    }

    /**
     * Gets the value of the values property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValues() {
        return values;
    }

    /**
     * Sets the value of the values property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValues(String value) {
        this.values = value;
    }

    /**
     * Gets the value of the keyTimes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeyTimes() {
        return keyTimes;
    }

    /**
     * Sets the value of the keyTimes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeyTimes(String value) {
        this.keyTimes = value;
    }

    /**
     * Gets the value of the keySplines property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeySplines() {
        return keySplines;
    }

    /**
     * Sets the value of the keySplines property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeySplines(String value) {
        this.keySplines = value;
    }

    /**
     * Gets the value of the from property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFrom(String value) {
        this.from = value;
    }

    /**
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTo(String value) {
        this.to = value;
    }

    /**
     * Gets the value of the by property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBy() {
        return by;
    }

    /**
     * Sets the value of the by property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBy(String value) {
        this.by = value;
    }

    /**
     * Gets the value of the path property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the value of the path property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPath(String value) {
        this.path = value;
    }

    /**
     * Gets the value of the keyPoints property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeyPoints() {
        return keyPoints;
    }

    /**
     * Sets the value of the keyPoints property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeyPoints(String value) {
        this.keyPoints = value;
    }

    /**
     * Gets the value of the rotate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRotate() {
        return rotate;
    }

    /**
     * Sets the value of the rotate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRotate(String value) {
        this.rotate = value;
    }

    /**
     * Gets the value of the origin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Sets the value of the origin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrigin(String value) {
        this.origin = value;
    }

    /**
     * Gets the value of the begin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBegin() {
        return begin;
    }

    /**
     * Sets the value of the begin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBegin(String value) {
        this.begin = value;
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
     * Gets the value of the end property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEnd() {
        return end;
    }

    /**
     * Sets the value of the end property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnd(String value) {
        this.end = value;
    }

    /**
     * Gets the value of the min property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMin() {
        return min;
    }

    /**
     * Sets the value of the min property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMin(String value) {
        this.min = value;
    }

    /**
     * Gets the value of the max property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMax() {
        return max;
    }

    /**
     * Sets the value of the max property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMax(String value) {
        this.max = value;
    }

    /**
     * Gets the value of the restart property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRestart() {
        if (restart == null) {
            return "always";
        } else {
            return restart;
        }
    }

    /**
     * Sets the value of the restart property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRestart(String value) {
        this.restart = value;
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
        return repeatCount;
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
     * Gets the value of the fill property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFill() {
        if (fill == null) {
            return "remove";
        } else {
            return fill;
        }
    }

    /**
     * Sets the value of the fill property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFill(String value) {
        this.fill = value;
    }

    /**
     * Gets the value of the additive property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdditive() {
        if (additive == null) {
            return "replace";
        } else {
            return additive;
        }
    }

    /**
     * Sets the value of the additive property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdditive(String value) {
        this.additive = value;
    }

    /**
     * Gets the value of the accumulate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccumulate() {
        if (accumulate == null) {
            return "none";
        } else {
            return accumulate;
        }
    }

    /**
     * Sets the value of the accumulate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccumulate(String value) {
        this.accumulate = value;
    }

}
