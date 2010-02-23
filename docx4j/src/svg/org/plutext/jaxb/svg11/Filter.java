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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
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
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.w3.org/2000/svg}desc"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}title"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}metadata"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.w3.org/2000/svg}animate"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}set"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}feBlend"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}feColorMatrix"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}feComponentTransfer"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}feComposite"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}feConvolveMatrix"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}feDiffuseLighting"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}feDisplacementMap"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}feFlood"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}feGaussianBlur"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}feImage"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}feMerge"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}feMorphology"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}feOffset"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}feSpecularLighting"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}feTile"/>
 *           &lt;element ref="{http://www.w3.org/2000/svg}feTurbulence"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.Presentation.attrib"/>
 *       &lt;attribute name="x" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="y" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="width" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="height" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="filterRes" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="filterUnits">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="userSpaceOnUse"/>
 *             &lt;enumeration value="objectBoundingBox"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="primitiveUnits">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="userSpaceOnUse"/>
 *             &lt;enumeration value="objectBoundingBox"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "descOrTitleOrMetadata",
    "animateOrSetOrFeBlend"
})
@XmlRootElement(name = "filter")
public class Filter {

    @XmlElements({
        @XmlElement(name = "desc", type = Desc.class),
        @XmlElement(name = "metadata", type = Metadata.class),
        @XmlElement(name = "title", type = Title.class)
    })
    protected List<Object> descOrTitleOrMetadata;
    @XmlElements({
        @XmlElement(name = "feColorMatrix", type = FeColorMatrix.class),
        @XmlElement(name = "feConvolveMatrix", type = FeConvolveMatrix.class),
        @XmlElement(name = "feComposite", type = FeComposite.class),
        @XmlElement(name = "feGaussianBlur", type = FeGaussianBlur.class),
        @XmlElement(name = "feTile", type = FeTile.class),
        @XmlElement(name = "feFlood", type = FeFlood.class),
        @XmlElement(name = "feSpecularLighting", type = FeSpecularLighting.class),
        @XmlElement(name = "animate", type = Animate.class),
        @XmlElement(name = "set", type = Set.class),
        @XmlElement(name = "feOffset", type = FeOffset.class),
        @XmlElement(name = "feMerge", type = FeMerge.class),
        @XmlElement(name = "feImage", type = FeImage.class),
        @XmlElement(name = "feMorphology", type = FeMorphology.class),
        @XmlElement(name = "feDisplacementMap", type = FeDisplacementMap.class),
        @XmlElement(name = "feBlend", type = FeBlend.class),
        @XmlElement(name = "feDiffuseLighting", type = FeDiffuseLighting.class),
        @XmlElement(name = "feTurbulence", type = FeTurbulence.class),
        @XmlElement(name = "feComponentTransfer", type = FeComponentTransfer.class)
    })
    protected List<Object> animateOrSetOrFeBlend;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String x;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String y;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String width;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String height;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String filterRes;
    @XmlAttribute
    protected String filterUnits;
    @XmlAttribute
    protected String primitiveUnits;
    @XmlAttribute(name = "flood-color")
    @XmlSchemaType(name = "anySimpleType")
    protected String floodColor;
    @XmlAttribute(name = "flood-opacity")
    @XmlSchemaType(name = "anySimpleType")
    protected String floodOpacity;
    @XmlAttribute(name = "lighting-color")
    @XmlSchemaType(name = "anySimpleType")
    protected String lightingColor;

    /**
     * Gets the value of the descOrTitleOrMetadata property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the descOrTitleOrMetadata property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDescOrTitleOrMetadata().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Desc }
     * {@link Metadata }
     * {@link Title }
     * 
     * 
     */
    public List<Object> getDescOrTitleOrMetadata() {
        if (descOrTitleOrMetadata == null) {
            descOrTitleOrMetadata = new ArrayList<Object>();
        }
        return this.descOrTitleOrMetadata;
    }

    /**
     * Gets the value of the animateOrSetOrFeBlend property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the animateOrSetOrFeBlend property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAnimateOrSetOrFeBlend().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FeColorMatrix }
     * {@link FeConvolveMatrix }
     * {@link FeComposite }
     * {@link FeGaussianBlur }
     * {@link FeTile }
     * {@link FeFlood }
     * {@link FeSpecularLighting }
     * {@link Animate }
     * {@link Set }
     * {@link FeOffset }
     * {@link FeMerge }
     * {@link FeImage }
     * {@link FeMorphology }
     * {@link FeDisplacementMap }
     * {@link FeBlend }
     * {@link FeDiffuseLighting }
     * {@link FeTurbulence }
     * {@link FeComponentTransfer }
     * 
     * 
     */
    public List<Object> getAnimateOrSetOrFeBlend() {
        if (animateOrSetOrFeBlend == null) {
            animateOrSetOrFeBlend = new ArrayList<Object>();
        }
        return this.animateOrSetOrFeBlend;
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
     * Gets the value of the width property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWidth(String value) {
        this.width = value;
    }

    /**
     * Gets the value of the height property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHeight() {
        return height;
    }

    /**
     * Sets the value of the height property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHeight(String value) {
        this.height = value;
    }

    /**
     * Gets the value of the filterRes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFilterRes() {
        return filterRes;
    }

    /**
     * Sets the value of the filterRes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFilterRes(String value) {
        this.filterRes = value;
    }

    /**
     * Gets the value of the filterUnits property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFilterUnits() {
        return filterUnits;
    }

    /**
     * Sets the value of the filterUnits property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFilterUnits(String value) {
        this.filterUnits = value;
    }

    /**
     * Gets the value of the primitiveUnits property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrimitiveUnits() {
        return primitiveUnits;
    }

    /**
     * Sets the value of the primitiveUnits property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrimitiveUnits(String value) {
        this.primitiveUnits = value;
    }

    /**
     * Gets the value of the floodColor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFloodColor() {
        return floodColor;
    }

    /**
     * Sets the value of the floodColor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFloodColor(String value) {
        this.floodColor = value;
    }

    /**
     * Gets the value of the floodOpacity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFloodOpacity() {
        return floodOpacity;
    }

    /**
     * Sets the value of the floodOpacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFloodOpacity(String value) {
        this.floodOpacity = value;
    }

    /**
     * Gets the value of the lightingColor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLightingColor() {
        return lightingColor;
    }

    /**
     * Sets the value of the lightingColor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLightingColor(String value) {
        this.lightingColor = value;
    }

}
