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
 * <p>Java class for CT_ColorMapping complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ColorMapping">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="bg1" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_ColorSchemeIndex" />
 *       &lt;attribute name="tx1" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_ColorSchemeIndex" />
 *       &lt;attribute name="bg2" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_ColorSchemeIndex" />
 *       &lt;attribute name="tx2" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_ColorSchemeIndex" />
 *       &lt;attribute name="accent1" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_ColorSchemeIndex" />
 *       &lt;attribute name="accent2" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_ColorSchemeIndex" />
 *       &lt;attribute name="accent3" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_ColorSchemeIndex" />
 *       &lt;attribute name="accent4" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_ColorSchemeIndex" />
 *       &lt;attribute name="accent5" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_ColorSchemeIndex" />
 *       &lt;attribute name="accent6" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_ColorSchemeIndex" />
 *       &lt;attribute name="hlink" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_ColorSchemeIndex" />
 *       &lt;attribute name="folHlink" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_ColorSchemeIndex" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ColorMapping", propOrder = {
    "extLst"
})
public class CTColorMapping {

    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(required = true)
    protected STColorSchemeIndex bg1;
    @XmlAttribute(required = true)
    protected STColorSchemeIndex tx1;
    @XmlAttribute(required = true)
    protected STColorSchemeIndex bg2;
    @XmlAttribute(required = true)
    protected STColorSchemeIndex tx2;
    @XmlAttribute(required = true)
    protected STColorSchemeIndex accent1;
    @XmlAttribute(required = true)
    protected STColorSchemeIndex accent2;
    @XmlAttribute(required = true)
    protected STColorSchemeIndex accent3;
    @XmlAttribute(required = true)
    protected STColorSchemeIndex accent4;
    @XmlAttribute(required = true)
    protected STColorSchemeIndex accent5;
    @XmlAttribute(required = true)
    protected STColorSchemeIndex accent6;
    @XmlAttribute(required = true)
    protected STColorSchemeIndex hlink;
    @XmlAttribute(required = true)
    protected STColorSchemeIndex folHlink;

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
     * Gets the value of the bg1 property.
     * 
     * @return
     *     possible object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public STColorSchemeIndex getBg1() {
        return bg1;
    }

    /**
     * Sets the value of the bg1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public void setBg1(STColorSchemeIndex value) {
        this.bg1 = value;
    }

    /**
     * Gets the value of the tx1 property.
     * 
     * @return
     *     possible object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public STColorSchemeIndex getTx1() {
        return tx1;
    }

    /**
     * Sets the value of the tx1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public void setTx1(STColorSchemeIndex value) {
        this.tx1 = value;
    }

    /**
     * Gets the value of the bg2 property.
     * 
     * @return
     *     possible object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public STColorSchemeIndex getBg2() {
        return bg2;
    }

    /**
     * Sets the value of the bg2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public void setBg2(STColorSchemeIndex value) {
        this.bg2 = value;
    }

    /**
     * Gets the value of the tx2 property.
     * 
     * @return
     *     possible object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public STColorSchemeIndex getTx2() {
        return tx2;
    }

    /**
     * Sets the value of the tx2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public void setTx2(STColorSchemeIndex value) {
        this.tx2 = value;
    }

    /**
     * Gets the value of the accent1 property.
     * 
     * @return
     *     possible object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public STColorSchemeIndex getAccent1() {
        return accent1;
    }

    /**
     * Sets the value of the accent1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public void setAccent1(STColorSchemeIndex value) {
        this.accent1 = value;
    }

    /**
     * Gets the value of the accent2 property.
     * 
     * @return
     *     possible object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public STColorSchemeIndex getAccent2() {
        return accent2;
    }

    /**
     * Sets the value of the accent2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public void setAccent2(STColorSchemeIndex value) {
        this.accent2 = value;
    }

    /**
     * Gets the value of the accent3 property.
     * 
     * @return
     *     possible object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public STColorSchemeIndex getAccent3() {
        return accent3;
    }

    /**
     * Sets the value of the accent3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public void setAccent3(STColorSchemeIndex value) {
        this.accent3 = value;
    }

    /**
     * Gets the value of the accent4 property.
     * 
     * @return
     *     possible object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public STColorSchemeIndex getAccent4() {
        return accent4;
    }

    /**
     * Sets the value of the accent4 property.
     * 
     * @param value
     *     allowed object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public void setAccent4(STColorSchemeIndex value) {
        this.accent4 = value;
    }

    /**
     * Gets the value of the accent5 property.
     * 
     * @return
     *     possible object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public STColorSchemeIndex getAccent5() {
        return accent5;
    }

    /**
     * Sets the value of the accent5 property.
     * 
     * @param value
     *     allowed object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public void setAccent5(STColorSchemeIndex value) {
        this.accent5 = value;
    }

    /**
     * Gets the value of the accent6 property.
     * 
     * @return
     *     possible object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public STColorSchemeIndex getAccent6() {
        return accent6;
    }

    /**
     * Sets the value of the accent6 property.
     * 
     * @param value
     *     allowed object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public void setAccent6(STColorSchemeIndex value) {
        this.accent6 = value;
    }

    /**
     * Gets the value of the hlink property.
     * 
     * @return
     *     possible object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public STColorSchemeIndex getHlink() {
        return hlink;
    }

    /**
     * Sets the value of the hlink property.
     * 
     * @param value
     *     allowed object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public void setHlink(STColorSchemeIndex value) {
        this.hlink = value;
    }

    /**
     * Gets the value of the folHlink property.
     * 
     * @return
     *     possible object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public STColorSchemeIndex getFolHlink() {
        return folHlink;
    }

    /**
     * Sets the value of the folHlink property.
     * 
     * @param value
     *     allowed object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public void setFolHlink(STColorSchemeIndex value) {
        this.folHlink = value;
    }

}
