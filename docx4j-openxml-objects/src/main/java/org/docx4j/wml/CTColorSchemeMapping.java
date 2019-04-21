/*
 *  Copyright 2007-2013, Plutext Pty Ltd.
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


package org.docx4j.wml; 

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ColorSchemeMapping complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ColorSchemeMapping">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="bg1" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_ColorSchemeIndex" />
 *       &lt;attribute name="t1" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_ColorSchemeIndex" />
 *       &lt;attribute name="bg2" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_ColorSchemeIndex" />
 *       &lt;attribute name="t2" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_ColorSchemeIndex" />
 *       &lt;attribute name="accent1" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_ColorSchemeIndex" />
 *       &lt;attribute name="accent2" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_ColorSchemeIndex" />
 *       &lt;attribute name="accent3" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_ColorSchemeIndex" />
 *       &lt;attribute name="accent4" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_ColorSchemeIndex" />
 *       &lt;attribute name="accent5" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_ColorSchemeIndex" />
 *       &lt;attribute name="accent6" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_ColorSchemeIndex" />
 *       &lt;attribute name="hyperlink" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_ColorSchemeIndex" />
 *       &lt;attribute name="followedHyperlink" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_ColorSchemeIndex" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ColorSchemeMapping")
public class CTColorSchemeMapping implements Child
{

    @XmlAttribute(name = "bg1", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STColorSchemeIndex bg1;
    @XmlAttribute(name = "t1", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STColorSchemeIndex t1;
    @XmlAttribute(name = "bg2", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STColorSchemeIndex bg2;
    @XmlAttribute(name = "t2", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STColorSchemeIndex t2;
    @XmlAttribute(name = "accent1", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STColorSchemeIndex accent1;
    @XmlAttribute(name = "accent2", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STColorSchemeIndex accent2;
    @XmlAttribute(name = "accent3", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STColorSchemeIndex accent3;
    @XmlAttribute(name = "accent4", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STColorSchemeIndex accent4;
    @XmlAttribute(name = "accent5", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STColorSchemeIndex accent5;
    @XmlAttribute(name = "accent6", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STColorSchemeIndex accent6;
    @XmlAttribute(name = "hyperlink", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STColorSchemeIndex hyperlink;
    @XmlAttribute(name = "followedHyperlink", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STColorSchemeIndex followedHyperlink;
    @XmlTransient
    private Object parent;

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
     * Gets the value of the t1 property.
     * 
     * @return
     *     possible object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public STColorSchemeIndex getT1() {
        return t1;
    }

    /**
     * Sets the value of the t1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public void setT1(STColorSchemeIndex value) {
        this.t1 = value;
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
     * Gets the value of the t2 property.
     * 
     * @return
     *     possible object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public STColorSchemeIndex getT2() {
        return t2;
    }

    /**
     * Sets the value of the t2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public void setT2(STColorSchemeIndex value) {
        this.t2 = value;
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
     * Gets the value of the hyperlink property.
     * 
     * @return
     *     possible object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public STColorSchemeIndex getHyperlink() {
        return hyperlink;
    }

    /**
     * Sets the value of the hyperlink property.
     * 
     * @param value
     *     allowed object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public void setHyperlink(STColorSchemeIndex value) {
        this.hyperlink = value;
    }

    /**
     * Gets the value of the followedHyperlink property.
     * 
     * @return
     *     possible object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public STColorSchemeIndex getFollowedHyperlink() {
        return followedHyperlink;
    }

    /**
     * Sets the value of the followedHyperlink property.
     * 
     * @param value
     *     allowed object is
     *     {@link STColorSchemeIndex }
     *     
     */
    public void setFollowedHyperlink(STColorSchemeIndex value) {
        this.followedHyperlink = value;
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
