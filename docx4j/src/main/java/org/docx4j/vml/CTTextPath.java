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
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TextPath complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TextPath">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attribute name="on" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="fitshape" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="fitpath" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="trim" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="xscale" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="string" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="style" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TextPath", propOrder = {
    "any"
})
public class CTTextPath
    implements Child
{

    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAttribute
    protected String on;
    @XmlAttribute
    protected String fitshape;
    @XmlAttribute
    protected String fitpath;
    @XmlAttribute
    protected String trim;
    @XmlAttribute
    protected String xscale;
    @XmlAttribute
    protected String string;
    @XmlAttribute
    protected String style;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();
    @XmlTransient
    private Object parent;

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
     * Gets the value of the on property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOn() {
        return on;
    }

    /**
     * Sets the value of the on property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOn(String value) {
        this.on = value;
    }

    /**
     * Gets the value of the fitshape property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFitshape() {
        return fitshape;
    }

    /**
     * Sets the value of the fitshape property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFitshape(String value) {
        this.fitshape = value;
    }

    /**
     * Gets the value of the fitpath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFitpath() {
        return fitpath;
    }

    /**
     * Sets the value of the fitpath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFitpath(String value) {
        this.fitpath = value;
    }

    /**
     * Gets the value of the trim property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTrim() {
        return trim;
    }

    /**
     * Sets the value of the trim property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTrim(String value) {
        this.trim = value;
    }

    /**
     * Gets the value of the xscale property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXscale() {
        return xscale;
    }

    /**
     * Sets the value of the xscale property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXscale(String value) {
        this.xscale = value;
    }

    /**
     * Gets the value of the string property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getString() {
        return string;
    }

    /**
     * Sets the value of the string property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setString(String value) {
        this.string = value;
    }

    /**
     * Gets the value of the style property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStyle() {
        return style;
    }

    /**
     * Sets the value of the style property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStyle(String value) {
        this.style = value;
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
