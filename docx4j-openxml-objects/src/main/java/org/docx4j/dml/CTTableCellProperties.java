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
 * <p>Java class for CT_TableCellProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TableCellProperties"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="lnL" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_LineProperties" minOccurs="0"/&gt;
 *         &lt;element name="lnR" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_LineProperties" minOccurs="0"/&gt;
 *         &lt;element name="lnT" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_LineProperties" minOccurs="0"/&gt;
 *         &lt;element name="lnB" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_LineProperties" minOccurs="0"/&gt;
 *         &lt;element name="lnTlToBr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_LineProperties" minOccurs="0"/&gt;
 *         &lt;element name="lnBlToTr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_LineProperties" minOccurs="0"/&gt;
 *         &lt;element name="cell3D" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Cell3D" minOccurs="0"/&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_FillProperties" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="marL" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate32" default="91440" /&gt;
 *       &lt;attribute name="marR" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate32" default="91440" /&gt;
 *       &lt;attribute name="marT" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate32" default="45720" /&gt;
 *       &lt;attribute name="marB" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate32" default="45720" /&gt;
 *       &lt;attribute name="vert" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextVerticalType" default="horz" /&gt;
 *       &lt;attribute name="anchor" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextAnchoringType" default="t" /&gt;
 *       &lt;attribute name="anchorCtr" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="horzOverflow" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextHorzOverflowType" default="clip" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TableCellProperties", propOrder = {
    "lnL",
    "lnR",
    "lnT",
    "lnB",
    "lnTlToBr",
    "lnBlToTr",
    "cell3D",
    "noFill",
    "solidFill",
    "gradFill",
    "blipFill",
    "pattFill",
    "grpFill",
    "extLst"
})
public class CTTableCellProperties implements Child
{

    protected CTLineProperties lnL;
    protected CTLineProperties lnR;
    protected CTLineProperties lnT;
    protected CTLineProperties lnB;
    protected CTLineProperties lnTlToBr;
    protected CTLineProperties lnBlToTr;
    protected CTCell3D cell3D;
    protected CTNoFillProperties noFill;
    protected CTSolidColorFillProperties solidFill;
    protected CTGradientFillProperties gradFill;
    protected CTBlipFillProperties blipFill;
    protected CTPatternFillProperties pattFill;
    protected CTGroupFillProperties grpFill;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "marL")
    protected Integer marL;
    @XmlAttribute(name = "marR")
    protected Integer marR;
    @XmlAttribute(name = "marT")
    protected Integer marT;
    @XmlAttribute(name = "marB")
    protected Integer marB;
    @XmlAttribute(name = "vert")
    protected STTextVerticalType vert;
    @XmlAttribute(name = "anchor")
    protected STTextAnchoringType anchor;
    @XmlAttribute(name = "anchorCtr")
    protected Boolean anchorCtr;
    @XmlAttribute(name = "horzOverflow")
    protected STTextHorzOverflowType horzOverflow;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the lnL property.
     * 
     * @return
     *     possible object is
     *     {@link CTLineProperties }
     *     
     */
    public CTLineProperties getLnL() {
        return lnL;
    }

    /**
     * Sets the value of the lnL property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLineProperties }
     *     
     */
    public void setLnL(CTLineProperties value) {
        this.lnL = value;
    }

    /**
     * Gets the value of the lnR property.
     * 
     * @return
     *     possible object is
     *     {@link CTLineProperties }
     *     
     */
    public CTLineProperties getLnR() {
        return lnR;
    }

    /**
     * Sets the value of the lnR property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLineProperties }
     *     
     */
    public void setLnR(CTLineProperties value) {
        this.lnR = value;
    }

    /**
     * Gets the value of the lnT property.
     * 
     * @return
     *     possible object is
     *     {@link CTLineProperties }
     *     
     */
    public CTLineProperties getLnT() {
        return lnT;
    }

    /**
     * Sets the value of the lnT property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLineProperties }
     *     
     */
    public void setLnT(CTLineProperties value) {
        this.lnT = value;
    }

    /**
     * Gets the value of the lnB property.
     * 
     * @return
     *     possible object is
     *     {@link CTLineProperties }
     *     
     */
    public CTLineProperties getLnB() {
        return lnB;
    }

    /**
     * Sets the value of the lnB property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLineProperties }
     *     
     */
    public void setLnB(CTLineProperties value) {
        this.lnB = value;
    }

    /**
     * Gets the value of the lnTlToBr property.
     * 
     * @return
     *     possible object is
     *     {@link CTLineProperties }
     *     
     */
    public CTLineProperties getLnTlToBr() {
        return lnTlToBr;
    }

    /**
     * Sets the value of the lnTlToBr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLineProperties }
     *     
     */
    public void setLnTlToBr(CTLineProperties value) {
        this.lnTlToBr = value;
    }

    /**
     * Gets the value of the lnBlToTr property.
     * 
     * @return
     *     possible object is
     *     {@link CTLineProperties }
     *     
     */
    public CTLineProperties getLnBlToTr() {
        return lnBlToTr;
    }

    /**
     * Sets the value of the lnBlToTr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLineProperties }
     *     
     */
    public void setLnBlToTr(CTLineProperties value) {
        this.lnBlToTr = value;
    }

    /**
     * Gets the value of the cell3D property.
     * 
     * @return
     *     possible object is
     *     {@link CTCell3D }
     *     
     */
    public CTCell3D getCell3D() {
        return cell3D;
    }

    /**
     * Sets the value of the cell3D property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCell3D }
     *     
     */
    public void setCell3D(CTCell3D value) {
        this.cell3D = value;
    }

    /**
     * Gets the value of the noFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTNoFillProperties }
     *     
     */
    public CTNoFillProperties getNoFill() {
        return noFill;
    }

    /**
     * Sets the value of the noFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNoFillProperties }
     *     
     */
    public void setNoFill(CTNoFillProperties value) {
        this.noFill = value;
    }

    /**
     * Gets the value of the solidFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTSolidColorFillProperties }
     *     
     */
    public CTSolidColorFillProperties getSolidFill() {
        return solidFill;
    }

    /**
     * Sets the value of the solidFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSolidColorFillProperties }
     *     
     */
    public void setSolidFill(CTSolidColorFillProperties value) {
        this.solidFill = value;
    }

    /**
     * Gets the value of the gradFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTGradientFillProperties }
     *     
     */
    public CTGradientFillProperties getGradFill() {
        return gradFill;
    }

    /**
     * Sets the value of the gradFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGradientFillProperties }
     *     
     */
    public void setGradFill(CTGradientFillProperties value) {
        this.gradFill = value;
    }

    /**
     * Gets the value of the blipFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTBlipFillProperties }
     *     
     */
    public CTBlipFillProperties getBlipFill() {
        return blipFill;
    }

    /**
     * Sets the value of the blipFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBlipFillProperties }
     *     
     */
    public void setBlipFill(CTBlipFillProperties value) {
        this.blipFill = value;
    }

    /**
     * Gets the value of the pattFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTPatternFillProperties }
     *     
     */
    public CTPatternFillProperties getPattFill() {
        return pattFill;
    }

    /**
     * Sets the value of the pattFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPatternFillProperties }
     *     
     */
    public void setPattFill(CTPatternFillProperties value) {
        this.pattFill = value;
    }

    /**
     * Gets the value of the grpFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTGroupFillProperties }
     *     
     */
    public CTGroupFillProperties getGrpFill() {
        return grpFill;
    }

    /**
     * Sets the value of the grpFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGroupFillProperties }
     *     
     */
    public void setGrpFill(CTGroupFillProperties value) {
        this.grpFill = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public CTOfficeArtExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public void setExtLst(CTOfficeArtExtensionList value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the marL property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getMarL() {
        if (marL == null) {
            return  91440;
        } else {
            return marL;
        }
    }

    /**
     * Sets the value of the marL property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMarL(Integer value) {
        this.marL = value;
    }

    /**
     * Gets the value of the marR property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getMarR() {
        if (marR == null) {
            return  91440;
        } else {
            return marR;
        }
    }

    /**
     * Sets the value of the marR property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMarR(Integer value) {
        this.marR = value;
    }

    /**
     * Gets the value of the marT property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getMarT() {
        if (marT == null) {
            return  45720;
        } else {
            return marT;
        }
    }

    /**
     * Sets the value of the marT property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMarT(Integer value) {
        this.marT = value;
    }

    /**
     * Gets the value of the marB property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getMarB() {
        if (marB == null) {
            return  45720;
        } else {
            return marB;
        }
    }

    /**
     * Sets the value of the marB property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMarB(Integer value) {
        this.marB = value;
    }

    /**
     * Gets the value of the vert property.
     * 
     * @return
     *     possible object is
     *     {@link STTextVerticalType }
     *     
     */
    public STTextVerticalType getVert() {
        if (vert == null) {
            return STTextVerticalType.HORZ;
        } else {
            return vert;
        }
    }

    /**
     * Sets the value of the vert property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTextVerticalType }
     *     
     */
    public void setVert(STTextVerticalType value) {
        this.vert = value;
    }

    /**
     * Gets the value of the anchor property.
     * 
     * @return
     *     possible object is
     *     {@link STTextAnchoringType }
     *     
     */
    public STTextAnchoringType getAnchor() {
        if (anchor == null) {
            return STTextAnchoringType.T;
        } else {
            return anchor;
        }
    }

    /**
     * Sets the value of the anchor property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTextAnchoringType }
     *     
     */
    public void setAnchor(STTextAnchoringType value) {
        this.anchor = value;
    }

    /**
     * Gets the value of the anchorCtr property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAnchorCtr() {
        if (anchorCtr == null) {
            return false;
        } else {
            return anchorCtr;
        }
    }

    /**
     * Sets the value of the anchorCtr property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAnchorCtr(Boolean value) {
        this.anchorCtr = value;
    }

    /**
     * Gets the value of the horzOverflow property.
     * 
     * @return
     *     possible object is
     *     {@link STTextHorzOverflowType }
     *     
     */
    public STTextHorzOverflowType getHorzOverflow() {
        if (horzOverflow == null) {
            return STTextHorzOverflowType.CLIP;
        } else {
            return horzOverflow;
        }
    }

    /**
     * Sets the value of the horzOverflow property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTextHorzOverflowType }
     *     
     */
    public void setHorzOverflow(STTextHorzOverflowType value) {
        this.horzOverflow = value;
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
