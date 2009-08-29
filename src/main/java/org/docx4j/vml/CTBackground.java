/*
 *  Copyright 2007-2009, Plutext Pty Ltd.
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


package org.docx4j.vml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.docx4j.vml.officedrawing.STBWMode;
import org.docx4j.vml.officedrawing.STBWModePure;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Background complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Background">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}fill" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}stroke" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}imagedata" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}shadow" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}textbox" minOccurs="0"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}textpath" minOccurs="0"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="fill" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="fillcolor" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}bwmode"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}bwpure"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}bwnormal"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}targetscreensize"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Background", propOrder = {
    "fill",
    "stroke",
    "imagedata",
    "shadow",
    "textbox",
    "textpath",
    "any"
})
public class CTBackground
    implements Child
{

    protected CTFill fill;
    protected CTStroke stroke;
    protected CTImageData imagedata;
    protected CTShadow shadow;
    protected CTTextbox textbox;
    protected CTTextPath textpath;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAttribute(required = true)
    protected String id;
    @XmlAttribute(name = "fill")
    protected String fillAttr;
    @XmlAttribute
    protected String fillcolor;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected STBWMode bwmode;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected STBWModePure bwpure;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected STBWModePure bwnormal;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String targetscreensize;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the fill property.
     * 
     * @return
     *     possible object is
     *     {@link CTFill }
     *     
     */
    public CTFill getFill() {
        return fill;
    }

    /**
     * Sets the value of the fill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFill }
     *     
     */
    public void setFill(CTFill value) {
        this.fill = value;
    }

    /**
     * Gets the value of the stroke property.
     * 
     * @return
     *     possible object is
     *     {@link CTStroke }
     *     
     */
    public CTStroke getStroke() {
        return stroke;
    }

    /**
     * Sets the value of the stroke property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStroke }
     *     
     */
    public void setStroke(CTStroke value) {
        this.stroke = value;
    }

    /**
     * Gets the value of the imagedata property.
     * 
     * @return
     *     possible object is
     *     {@link CTImageData }
     *     
     */
    public CTImageData getImagedata() {
        return imagedata;
    }

    /**
     * Sets the value of the imagedata property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTImageData }
     *     
     */
    public void setImagedata(CTImageData value) {
        this.imagedata = value;
    }

    /**
     * Gets the value of the shadow property.
     * 
     * @return
     *     possible object is
     *     {@link CTShadow }
     *     
     */
    public CTShadow getShadow() {
        return shadow;
    }

    /**
     * Sets the value of the shadow property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShadow }
     *     
     */
    public void setShadow(CTShadow value) {
        this.shadow = value;
    }

    /**
     * Gets the value of the textbox property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextbox }
     *     
     */
    public CTTextbox getTextbox() {
        return textbox;
    }

    /**
     * Sets the value of the textbox property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextbox }
     *     
     */
    public void setTextbox(CTTextbox value) {
        this.textbox = value;
    }

    /**
     * Gets the value of the textpath property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextPath }
     *     
     */
    public CTTextPath getTextpath() {
        return textpath;
    }

    /**
     * Sets the value of the textpath property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextPath }
     *     
     */
    public void setTextpath(CTTextPath value) {
        this.textpath = value;
    }

    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the fillAttr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFillAttr() {
        return fillAttr;
    }

    /**
     * Sets the value of the fillAttr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFillAttr(String value) {
        this.fillAttr = value;
    }

    /**
     * Gets the value of the fillcolor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFillcolor() {
        return fillcolor;
    }

    /**
     * Sets the value of the fillcolor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFillcolor(String value) {
        this.fillcolor = value;
    }

    /**
     * Gets the value of the bwmode property.
     * 
     * @return
     *     possible object is
     *     {@link STBWMode }
     *     
     */
    public STBWMode getBwmode() {
        return bwmode;
    }

    /**
     * Sets the value of the bwmode property.
     * 
     * @param value
     *     allowed object is
     *     {@link STBWMode }
     *     
     */
    public void setBwmode(STBWMode value) {
        this.bwmode = value;
    }

    /**
     * Gets the value of the bwpure property.
     * 
     * @return
     *     possible object is
     *     {@link STBWModePure }
     *     
     */
    public STBWModePure getBwpure() {
        return bwpure;
    }

    /**
     * Sets the value of the bwpure property.
     * 
     * @param value
     *     allowed object is
     *     {@link STBWModePure }
     *     
     */
    public void setBwpure(STBWModePure value) {
        this.bwpure = value;
    }

    /**
     * Gets the value of the bwnormal property.
     * 
     * @return
     *     possible object is
     *     {@link STBWModePure }
     *     
     */
    public STBWModePure getBwnormal() {
        return bwnormal;
    }

    /**
     * Sets the value of the bwnormal property.
     * 
     * @param value
     *     allowed object is
     *     {@link STBWModePure }
     *     
     */
    public void setBwnormal(STBWModePure value) {
        this.bwnormal = value;
    }

    /**
     * Gets the value of the targetscreensize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetscreensize() {
        return targetscreensize;
    }

    /**
     * Sets the value of the targetscreensize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetscreensize(String value) {
        this.targetscreensize = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
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
