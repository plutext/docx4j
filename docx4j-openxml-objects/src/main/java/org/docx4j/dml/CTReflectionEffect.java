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


package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ReflectionEffect complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ReflectionEffect"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="blurRad" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" default="0" /&gt;
 *       &lt;attribute name="stA" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedPercentage" default="100000" /&gt;
 *       &lt;attribute name="stPos" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedPercentage" default="0" /&gt;
 *       &lt;attribute name="endA" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedPercentage" default="0" /&gt;
 *       &lt;attribute name="endPos" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedPercentage" default="100000" /&gt;
 *       &lt;attribute name="dist" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" default="0" /&gt;
 *       &lt;attribute name="dir" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedAngle" default="0" /&gt;
 *       &lt;attribute name="fadeDir" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedAngle" default="5400000" /&gt;
 *       &lt;attribute name="sx" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" default="100000" /&gt;
 *       &lt;attribute name="sy" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" default="100000" /&gt;
 *       &lt;attribute name="kx" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_FixedAngle" default="0" /&gt;
 *       &lt;attribute name="ky" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_FixedAngle" default="0" /&gt;
 *       &lt;attribute name="algn" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_RectAlignment" default="b" /&gt;
 *       &lt;attribute name="rotWithShape" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ReflectionEffect")
public class CTReflectionEffect implements Child
{

    @XmlAttribute(name = "blurRad")
    protected Long blurRad;
    @XmlAttribute(name = "stA")
    protected Integer stA;
    @XmlAttribute(name = "stPos")
    protected Integer stPos;
    @XmlAttribute(name = "endA")
    protected Integer endA;
    @XmlAttribute(name = "endPos")
    protected Integer endPos;
    @XmlAttribute(name = "dist")
    protected Long dist;
    @XmlAttribute(name = "dir")
    protected Integer dir;
    @XmlAttribute(name = "fadeDir")
    protected Integer fadeDir;
    @XmlAttribute(name = "sx")
    protected Integer sx;
    @XmlAttribute(name = "sy")
    protected Integer sy;
    @XmlAttribute(name = "kx")
    protected Integer kx;
    @XmlAttribute(name = "ky")
    protected Integer ky;
    @XmlAttribute(name = "algn")
    protected STRectAlignment algn;
    @XmlAttribute(name = "rotWithShape")
    protected Boolean rotWithShape;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the blurRad property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getBlurRad() {
        if (blurRad == null) {
            return  0L;
        } else {
            return blurRad;
        }
    }

    /**
     * Sets the value of the blurRad property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setBlurRad(Long value) {
        this.blurRad = value;
    }

    /**
     * Gets the value of the stA property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getStA() {
        if (stA == null) {
            return  100000;
        } else {
            return stA;
        }
    }

    /**
     * Sets the value of the stA property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setStA(Integer value) {
        this.stA = value;
    }

    /**
     * Gets the value of the stPos property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getStPos() {
        if (stPos == null) {
            return  0;
        } else {
            return stPos;
        }
    }

    /**
     * Sets the value of the stPos property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setStPos(Integer value) {
        this.stPos = value;
    }

    /**
     * Gets the value of the endA property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getEndA() {
        if (endA == null) {
            return  0;
        } else {
            return endA;
        }
    }

    /**
     * Sets the value of the endA property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setEndA(Integer value) {
        this.endA = value;
    }

    /**
     * Gets the value of the endPos property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getEndPos() {
        if (endPos == null) {
            return  100000;
        } else {
            return endPos;
        }
    }

    /**
     * Sets the value of the endPos property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setEndPos(Integer value) {
        this.endPos = value;
    }

    /**
     * Gets the value of the dist property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getDist() {
        if (dist == null) {
            return  0L;
        } else {
            return dist;
        }
    }

    /**
     * Sets the value of the dist property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDist(Long value) {
        this.dist = value;
    }

    /**
     * Gets the value of the dir property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getDir() {
        if (dir == null) {
            return  0;
        } else {
            return dir;
        }
    }

    /**
     * Sets the value of the dir property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDir(Integer value) {
        this.dir = value;
    }

    /**
     * Gets the value of the fadeDir property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getFadeDir() {
        if (fadeDir == null) {
            return  5400000;
        } else {
            return fadeDir;
        }
    }

    /**
     * Sets the value of the fadeDir property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setFadeDir(Integer value) {
        this.fadeDir = value;
    }

    /**
     * Gets the value of the sx property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getSx() {
        if (sx == null) {
            return  100000;
        } else {
            return sx;
        }
    }

    /**
     * Sets the value of the sx property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSx(Integer value) {
        this.sx = value;
    }

    /**
     * Gets the value of the sy property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getSy() {
        if (sy == null) {
            return  100000;
        } else {
            return sy;
        }
    }

    /**
     * Sets the value of the sy property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSy(Integer value) {
        this.sy = value;
    }

    /**
     * Gets the value of the kx property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getKx() {
        if (kx == null) {
            return  0;
        } else {
            return kx;
        }
    }

    /**
     * Sets the value of the kx property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setKx(Integer value) {
        this.kx = value;
    }

    /**
     * Gets the value of the ky property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getKy() {
        if (ky == null) {
            return  0;
        } else {
            return ky;
        }
    }

    /**
     * Sets the value of the ky property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setKy(Integer value) {
        this.ky = value;
    }

    /**
     * Gets the value of the algn property.
     * 
     * @return
     *     possible object is
     *     {@link STRectAlignment }
     *     
     */
    public STRectAlignment getAlgn() {
        if (algn == null) {
            return STRectAlignment.B;
        } else {
            return algn;
        }
    }

    /**
     * Sets the value of the algn property.
     * 
     * @param value
     *     allowed object is
     *     {@link STRectAlignment }
     *     
     */
    public void setAlgn(STRectAlignment value) {
        this.algn = value;
    }

    /**
     * Gets the value of the rotWithShape property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRotWithShape() {
        if (rotWithShape == null) {
            return true;
        } else {
            return rotWithShape;
        }
    }

    /**
     * Sets the value of the rotWithShape property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRotWithShape(Boolean value) {
        this.rotWithShape = value;
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
