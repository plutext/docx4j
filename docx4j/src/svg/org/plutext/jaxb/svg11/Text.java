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
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlMixed;
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
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{http://www.w3.org/2000/svg}desc"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}title"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}metadata"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}animate"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}set"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}animateMotion"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}animateColor"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}animateTransform"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}tspan"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}tref"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}textPath"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}altGlyph"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}a"/>
 *       &lt;/choice>
 *       &lt;attribute name="x" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="y" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="dx" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="dy" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="rotate" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="textLength" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="lengthAdjust">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="spacing"/>
 *             &lt;enumeration value="spacingAndGlyphs"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="transform" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "content"
})
@XmlRootElement(name = "text")
public class Text {

    @XmlElementRefs({
        @XmlElementRef(name = "metadata", namespace = "http://www.w3.org/2000/svg", type = Metadata.class),
        @XmlElementRef(name = "tspan", namespace = "http://www.w3.org/2000/svg", type = Tspan.class),
        @XmlElementRef(name = "animateTransform", namespace = "http://www.w3.org/2000/svg", type = AnimateTransform.class),
        @XmlElementRef(name = "altGlyph", namespace = "http://www.w3.org/2000/svg", type = AltGlyph.class),
        @XmlElementRef(name = "animate", namespace = "http://www.w3.org/2000/svg", type = Animate.class),
        @XmlElementRef(name = "a", namespace = "http://www.w3.org/2000/svg", type = A.class),
        @XmlElementRef(name = "textPath", namespace = "http://www.w3.org/2000/svg", type = TextPath.class),
        @XmlElementRef(name = "desc", namespace = "http://www.w3.org/2000/svg", type = Desc.class),
        @XmlElementRef(name = "set", namespace = "http://www.w3.org/2000/svg", type = Set.class),
        @XmlElementRef(name = "animateColor", namespace = "http://www.w3.org/2000/svg", type = AnimateColor.class),
        @XmlElementRef(name = "animateMotion", namespace = "http://www.w3.org/2000/svg", type = AnimateMotion.class),
        @XmlElementRef(name = "tref", namespace = "http://www.w3.org/2000/svg", type = Tref.class),
        @XmlElementRef(name = "title", namespace = "http://www.w3.org/2000/svg", type = Title.class)
    })
    @XmlMixed
    protected List<Object> content;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String x;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String y;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String dx;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String dy;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String rotate;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String textLength;
    @XmlAttribute
    protected String lengthAdjust;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String transform;

    /**
     * Gets the value of the content property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the content property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * {@link Metadata }
     * {@link Tspan }
     * {@link AnimateTransform }
     * {@link AltGlyph }
     * {@link Animate }
     * {@link A }
     * {@link TextPath }
     * {@link Desc }
     * {@link Set }
     * {@link AnimateColor }
     * {@link AnimateMotion }
     * {@link Tref }
     * {@link Title }
     * 
     * 
     */
    public List<Object> getContent() {
        if (content == null) {
            content = new ArrayList<Object>();
        }
        return this.content;
    }

    /**
     * Gets the value of the x property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getX() {
        return x;
    }

    /**
     * Sets the value of the x property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setX(String value) {
        this.x = value;
    }

    /**
     * Gets the value of the y property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getY() {
        return y;
    }

    /**
     * Sets the value of the y property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setY(String value) {
        this.y = value;
    }

    /**
     * Gets the value of the dx property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDx() {
        return dx;
    }

    /**
     * Sets the value of the dx property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDx(String value) {
        this.dx = value;
    }

    /**
     * Gets the value of the dy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDy() {
        return dy;
    }

    /**
     * Sets the value of the dy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDy(String value) {
        this.dy = value;
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
     * Gets the value of the textLength property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextLength() {
        return textLength;
    }

    /**
     * Sets the value of the textLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextLength(String value) {
        this.textLength = value;
    }

    /**
     * Gets the value of the lengthAdjust property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLengthAdjust() {
        return lengthAdjust;
    }

    /**
     * Sets the value of the lengthAdjust property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLengthAdjust(String value) {
        this.lengthAdjust = value;
    }

    /**
     * Gets the value of the transform property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransform() {
        return transform;
    }

    /**
     * Sets the value of the transform property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransform(String value) {
        this.transform = value;
    }

}
