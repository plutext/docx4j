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

import java.math.BigDecimal;
import java.math.BigInteger;
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
 * <p>Java class for CT_Fill complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Fill">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="type" type="{urn:schemas-microsoft-com:vml}ST_FillType" default="solid" />
 *       &lt;attribute name="on" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="color" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="opacity" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="color2" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="opacity2" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="src" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="href" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="althref" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="title" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="size" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="origin" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="position" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="aspect" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="colors" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="angle" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *       &lt;attribute name="focus" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="focussize" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="focusposition" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="method" type="{urn:schemas-microsoft-com:vml}ST_FillMethod" />
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}detectmouseclick"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}title"/>
 *       &lt;attribute name="alignshape" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}opacity2"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Fill", propOrder = {
    "any"
})
public class CTFill
    implements Child
{

    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAttribute
    protected String id;
    @XmlAttribute
    protected STFillType type;
    @XmlAttribute
    protected String on;
    @XmlAttribute
    protected String color;
    @XmlAttribute
    protected String opacity;
    @XmlAttribute
    protected String color2;
    @XmlAttribute
    protected String opacity2;
    @XmlAttribute
    protected String src;
    @XmlAttribute
    protected String href;
    @XmlAttribute
    protected String althref;
    @XmlAttribute
    protected String title;
    @XmlAttribute
    protected String size;
    @XmlAttribute
    protected String origin;
    @XmlAttribute
    protected String position;
    @XmlAttribute
    protected String aspect;
    @XmlAttribute
    protected String colors;
    @XmlAttribute
    protected BigDecimal angle;
    @XmlAttribute
    protected String focus;
    @XmlAttribute
    protected String focussize;
    @XmlAttribute
    protected String focusposition;
    @XmlAttribute
    protected STFillMethod method;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String detectmouseclick;
    @XmlAttribute(name = "title", namespace = "urn:schemas-microsoft-com:office:office")
    protected String titleo;
    @XmlAttribute
    protected String alignshape;
    @XmlAttribute(name = "opacity2", namespace = "urn:schemas-microsoft-com:office:office")
    protected BigInteger opacity2O;
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
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STFillType }
     *     
     */
    public STFillType getType() {
        if (type == null) {
            return STFillType.SOLID;
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STFillType }
     *     
     */
    public void setType(STFillType value) {
        this.type = value;
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
     * Gets the value of the color property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the value of the color property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColor(String value) {
        this.color = value;
    }

    /**
     * Gets the value of the opacity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOpacity() {
        return opacity;
    }

    /**
     * Sets the value of the opacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOpacity(String value) {
        this.opacity = value;
    }

    /**
     * Gets the value of the color2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColor2() {
        return color2;
    }

    /**
     * Sets the value of the color2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColor2(String value) {
        this.color2 = value;
    }

    /**
     * Gets the value of the opacity2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOpacity2() {
        return opacity2;
    }

    /**
     * Sets the value of the opacity2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOpacity2(String value) {
        this.opacity2 = value;
    }

    /**
     * Gets the value of the src property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSrc() {
        return src;
    }

    /**
     * Sets the value of the src property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSrc(String value) {
        this.src = value;
    }

    /**
     * Gets the value of the href property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the value of the href property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHref(String value) {
        this.href = value;
    }

    /**
     * Gets the value of the althref property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlthref() {
        return althref;
    }

    /**
     * Sets the value of the althref property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlthref(String value) {
        this.althref = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the size property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSize() {
        return size;
    }

    /**
     * Sets the value of the size property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSize(String value) {
        this.size = value;
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
     * Gets the value of the position property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPosition() {
        return position;
    }

    /**
     * Sets the value of the position property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPosition(String value) {
        this.position = value;
    }

    /**
     * Gets the value of the aspect property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAspect() {
        return aspect;
    }

    /**
     * Sets the value of the aspect property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAspect(String value) {
        this.aspect = value;
    }

    /**
     * Gets the value of the colors property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColors() {
        return colors;
    }

    /**
     * Sets the value of the colors property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColors(String value) {
        this.colors = value;
    }

    /**
     * Gets the value of the angle property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAngle() {
        return angle;
    }

    /**
     * Sets the value of the angle property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAngle(BigDecimal value) {
        this.angle = value;
    }

    /**
     * Gets the value of the focus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFocus() {
        return focus;
    }

    /**
     * Sets the value of the focus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFocus(String value) {
        this.focus = value;
    }

    /**
     * Gets the value of the focussize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFocussize() {
        return focussize;
    }

    /**
     * Sets the value of the focussize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFocussize(String value) {
        this.focussize = value;
    }

    /**
     * Gets the value of the focusposition property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFocusposition() {
        return focusposition;
    }

    /**
     * Sets the value of the focusposition property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFocusposition(String value) {
        this.focusposition = value;
    }

    /**
     * Gets the value of the method property.
     * 
     * @return
     *     possible object is
     *     {@link STFillMethod }
     *     
     */
    public STFillMethod getMethod() {
        return method;
    }

    /**
     * Sets the value of the method property.
     * 
     * @param value
     *     allowed object is
     *     {@link STFillMethod }
     *     
     */
    public void setMethod(STFillMethod value) {
        this.method = value;
    }

    /**
     * Gets the value of the detectmouseclick property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDetectmouseclick() {
        return detectmouseclick;
    }

    /**
     * Sets the value of the detectmouseclick property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDetectmouseclick(String value) {
        this.detectmouseclick = value;
    }

    /**
     * Gets the value of the titleo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitleo() {
        return titleo;
    }

    /**
     * Sets the value of the titleo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitleo(String value) {
        this.titleo = value;
    }

    /**
     * Gets the value of the alignshape property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlignshape() {
        return alignshape;
    }

    /**
     * Sets the value of the alignshape property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlignshape(String value) {
        this.alignshape = value;
    }

    /**
     * Gets the value of the opacity2O property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getOpacity2O() {
        return opacity2O;
    }

    /**
     * Sets the value of the opacity2O property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setOpacity2O(BigInteger value) {
        this.opacity2O = value;
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
