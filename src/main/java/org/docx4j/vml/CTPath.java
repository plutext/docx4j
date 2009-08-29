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
import org.docx4j.vml.officedrawing.STConnectType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Path complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Path">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="v" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="limo" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="textboxrect" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="fillok" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="strokeok" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="shadowok" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="arrowok" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="gradientshapeok" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="textpathok" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}connecttype"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}connectlocs"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}connectangles"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}extrusionok"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Path", propOrder = {
    "any"
})
public class CTPath
    implements Child
{

    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAttribute
    protected String id;
    @XmlAttribute
    protected String v;
    @XmlAttribute
    protected String limo;
    @XmlAttribute
    protected String textboxrect;
    @XmlAttribute
    protected String fillok;
    @XmlAttribute
    protected String strokeok;
    @XmlAttribute
    protected String shadowok;
    @XmlAttribute
    protected String arrowok;
    @XmlAttribute
    protected String gradientshapeok;
    @XmlAttribute
    protected String textpathok;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected STConnectType connecttype;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String connectlocs;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String connectangles;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String extrusionok;
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
     * Gets the value of the v property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getV() {
        return v;
    }

    /**
     * Sets the value of the v property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setV(String value) {
        this.v = value;
    }

    /**
     * Gets the value of the limo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLimo() {
        return limo;
    }

    /**
     * Sets the value of the limo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLimo(String value) {
        this.limo = value;
    }

    /**
     * Gets the value of the textboxrect property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextboxrect() {
        return textboxrect;
    }

    /**
     * Sets the value of the textboxrect property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextboxrect(String value) {
        this.textboxrect = value;
    }

    /**
     * Gets the value of the fillok property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFillok() {
        return fillok;
    }

    /**
     * Sets the value of the fillok property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFillok(String value) {
        this.fillok = value;
    }

    /**
     * Gets the value of the strokeok property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrokeok() {
        return strokeok;
    }

    /**
     * Sets the value of the strokeok property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrokeok(String value) {
        this.strokeok = value;
    }

    /**
     * Gets the value of the shadowok property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShadowok() {
        return shadowok;
    }

    /**
     * Sets the value of the shadowok property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShadowok(String value) {
        this.shadowok = value;
    }

    /**
     * Gets the value of the arrowok property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArrowok() {
        return arrowok;
    }

    /**
     * Sets the value of the arrowok property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArrowok(String value) {
        this.arrowok = value;
    }

    /**
     * Gets the value of the gradientshapeok property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGradientshapeok() {
        return gradientshapeok;
    }

    /**
     * Sets the value of the gradientshapeok property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGradientshapeok(String value) {
        this.gradientshapeok = value;
    }

    /**
     * Gets the value of the textpathok property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextpathok() {
        return textpathok;
    }

    /**
     * Sets the value of the textpathok property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextpathok(String value) {
        this.textpathok = value;
    }

    /**
     * Gets the value of the connecttype property.
     * 
     * @return
     *     possible object is
     *     {@link STConnectType }
     *     
     */
    public STConnectType getConnecttype() {
        return connecttype;
    }

    /**
     * Sets the value of the connecttype property.
     * 
     * @param value
     *     allowed object is
     *     {@link STConnectType }
     *     
     */
    public void setConnecttype(STConnectType value) {
        this.connecttype = value;
    }

    /**
     * Gets the value of the connectlocs property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConnectlocs() {
        return connectlocs;
    }

    /**
     * Sets the value of the connectlocs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConnectlocs(String value) {
        this.connectlocs = value;
    }

    /**
     * Gets the value of the connectangles property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConnectangles() {
        return connectangles;
    }

    /**
     * Sets the value of the connectangles property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConnectangles(String value) {
        this.connectangles = value;
    }

    /**
     * Gets the value of the extrusionok property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExtrusionok() {
        return extrusionok;
    }

    /**
     * Sets the value of the extrusionok property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExtrusionok(String value) {
        this.extrusionok = value;
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
