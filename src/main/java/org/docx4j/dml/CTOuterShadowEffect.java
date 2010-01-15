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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_OuterShadowEffect complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_OuterShadowEffect">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_ColorChoice"/>
 *       &lt;/sequence>
 *       &lt;attribute name="blurRad" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" default="0" />
 *       &lt;attribute name="dist" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" default="0" />
 *       &lt;attribute name="dir" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedAngle" default="0" />
 *       &lt;attribute name="sx" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" default="100000" />
 *       &lt;attribute name="sy" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" default="100000" />
 *       &lt;attribute name="kx" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_FixedAngle" default="0" />
 *       &lt;attribute name="ky" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_FixedAngle" default="0" />
 *       &lt;attribute name="algn" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_RectAlignment" default="b" />
 *       &lt;attribute name="rotWithShape" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_OuterShadowEffect", propOrder = {
    "scrgbClr",
    "srgbClr",
    "hslClr",
    "sysClr",
    "schemeClr",
    "prstClr"
})
public class CTOuterShadowEffect {

    protected CTScRgbColor scrgbClr;
    protected CTSRgbColor srgbClr;
    protected CTHslColor hslClr;
    protected CTSystemColor sysClr;
    protected CTSchemeColor schemeClr;
    protected CTPresetColor prstClr;
    @XmlAttribute
    protected Long blurRad;
    @XmlAttribute
    protected Long dist;
    @XmlAttribute
    protected Integer dir;
    @XmlAttribute
    protected Integer sx;
    @XmlAttribute
    protected Integer sy;
    @XmlAttribute
    protected Integer kx;
    @XmlAttribute
    protected Integer ky;
    @XmlAttribute
    protected STRectAlignment algn;
    @XmlAttribute
    protected Boolean rotWithShape;

    /**
     * Gets the value of the scrgbClr property.
     * 
     * @return
     *     possible object is
     *     {@link CTScRgbColor }
     *     
     */
    public CTScRgbColor getScrgbClr() {
        return scrgbClr;
    }

    /**
     * Sets the value of the scrgbClr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTScRgbColor }
     *     
     */
    public void setScrgbClr(CTScRgbColor value) {
        this.scrgbClr = value;
    }

    /**
     * Gets the value of the srgbClr property.
     * 
     * @return
     *     possible object is
     *     {@link CTSRgbColor }
     *     
     */
    public CTSRgbColor getSrgbClr() {
        return srgbClr;
    }

    /**
     * Sets the value of the srgbClr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSRgbColor }
     *     
     */
    public void setSrgbClr(CTSRgbColor value) {
        this.srgbClr = value;
    }

    /**
     * Gets the value of the hslClr property.
     * 
     * @return
     *     possible object is
     *     {@link CTHslColor }
     *     
     */
    public CTHslColor getHslClr() {
        return hslClr;
    }

    /**
     * Sets the value of the hslClr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTHslColor }
     *     
     */
    public void setHslClr(CTHslColor value) {
        this.hslClr = value;
    }

    /**
     * Gets the value of the sysClr property.
     * 
     * @return
     *     possible object is
     *     {@link CTSystemColor }
     *     
     */
    public CTSystemColor getSysClr() {
        return sysClr;
    }

    /**
     * Sets the value of the sysClr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSystemColor }
     *     
     */
    public void setSysClr(CTSystemColor value) {
        this.sysClr = value;
    }

    /**
     * Gets the value of the schemeClr property.
     * 
     * @return
     *     possible object is
     *     {@link CTSchemeColor }
     *     
     */
    public CTSchemeColor getSchemeClr() {
        return schemeClr;
    }

    /**
     * Sets the value of the schemeClr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSchemeColor }
     *     
     */
    public void setSchemeClr(CTSchemeColor value) {
        this.schemeClr = value;
    }

    /**
     * Gets the value of the prstClr property.
     * 
     * @return
     *     possible object is
     *     {@link CTPresetColor }
     *     
     */
    public CTPresetColor getPrstClr() {
        return prstClr;
    }

    /**
     * Sets the value of the prstClr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPresetColor }
     *     
     */
    public void setPrstClr(CTPresetColor value) {
        this.prstClr = value;
    }

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

}
