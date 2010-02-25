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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.w3.org/2000/svg}SVG.feConvolveMatrix.content">
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.FilterColor.attrib"/>
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.Core.attrib"/>
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.FilterPrimitiveWithIn.attrib"/>
 *       &lt;attribute name="order" use="required" type="{http://www.w3.org/2000/svg}NumberOptionalNumber.datatype" />
 *       &lt;attribute name="kernelMatrix" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="divisor" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="bias" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *       &lt;attribute name="targetX" type="{http://www.w3.org/2000/svg}Integer.datatype" />
 *       &lt;attribute name="targetY" type="{http://www.w3.org/2000/svg}Integer.datatype" />
 *       &lt;attribute name="edgeMode" default="duplicate">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="duplicate"/>
 *             &lt;enumeration value="wrap"/>
 *             &lt;enumeration value="none"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="kernelUnitLength" type="{http://www.w3.org/2000/svg}NumberOptionalNumber.datatype" />
 *       &lt;attribute name="preserveAlpha" type="{http://www.w3.org/2000/svg}Boolean.datatype" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class FeConvolveMatrix
    extends SVGFeConvolveMatrixContent
{

    @XmlAttribute(required = true)
    protected String order;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String kernelMatrix;
    @XmlAttribute
    protected String divisor;
    @XmlAttribute
    protected String bias;
    @XmlAttribute
    protected String targetX;
    @XmlAttribute
    protected String targetY;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String edgeMode;
    @XmlAttribute
    protected String kernelUnitLength;
    @XmlAttribute
    protected BooleanDatatype preserveAlpha;
    @XmlAttribute(name = "color-interpolation-filters")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String colorInterpolationFilters;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace")
    protected String base;
    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String space;
    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String lang;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String in;
    @XmlAttribute
    protected String x;
    @XmlAttribute
    protected String y;
    @XmlAttribute
    protected String width;
    @XmlAttribute
    protected String height;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String result;

    /**
     * Gets the value of the order property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrder() {
        return order;
    }

    /**
     * Sets the value of the order property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrder(String value) {
        this.order = value;
    }

    /**
     * Gets the value of the kernelMatrix property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKernelMatrix() {
        return kernelMatrix;
    }

    /**
     * Sets the value of the kernelMatrix property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKernelMatrix(String value) {
        this.kernelMatrix = value;
    }

    /**
     * Gets the value of the divisor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDivisor() {
        return divisor;
    }

    /**
     * Sets the value of the divisor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDivisor(String value) {
        this.divisor = value;
    }

    /**
     * Gets the value of the bias property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBias() {
        return bias;
    }

    /**
     * Sets the value of the bias property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBias(String value) {
        this.bias = value;
    }

    /**
     * Gets the value of the targetX property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetX() {
        return targetX;
    }

    /**
     * Sets the value of the targetX property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetX(String value) {
        this.targetX = value;
    }

    /**
     * Gets the value of the targetY property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetY() {
        return targetY;
    }

    /**
     * Sets the value of the targetY property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetY(String value) {
        this.targetY = value;
    }

    /**
     * Gets the value of the edgeMode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEdgeMode() {
        if (edgeMode == null) {
            return "duplicate";
        } else {
            return edgeMode;
        }
    }

    /**
     * Sets the value of the edgeMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEdgeMode(String value) {
        this.edgeMode = value;
    }

    /**
     * Gets the value of the kernelUnitLength property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKernelUnitLength() {
        return kernelUnitLength;
    }

    /**
     * Sets the value of the kernelUnitLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKernelUnitLength(String value) {
        this.kernelUnitLength = value;
    }

    /**
     * Gets the value of the preserveAlpha property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDatatype }
     *     
     */
    public BooleanDatatype getPreserveAlpha() {
        return preserveAlpha;
    }

    /**
     * Sets the value of the preserveAlpha property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDatatype }
     *     
     */
    public void setPreserveAlpha(BooleanDatatype value) {
        this.preserveAlpha = value;
    }

    /**
     * Gets the value of the colorInterpolationFilters property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColorInterpolationFilters() {
        return colorInterpolationFilters;
    }

    /**
     * Sets the value of the colorInterpolationFilters property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColorInterpolationFilters(String value) {
        this.colorInterpolationFilters = value;
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
     * Gets the value of the base property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBase() {
        return base;
    }

    /**
     * Sets the value of the base property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBase(String value) {
        this.base = value;
    }

    /**
     * Gets the value of the space property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpace() {
        return space;
    }

    /**
     * Sets the value of the space property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpace(String value) {
        this.space = value;
    }

    /**
     * Gets the value of the lang property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLang() {
        return lang;
    }

    /**
     * Sets the value of the lang property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLang(String value) {
        this.lang = value;
    }

    /**
     * Gets the value of the in property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIn() {
        return in;
    }

    /**
     * Sets the value of the in property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIn(String value) {
        this.in = value;
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
     * Gets the value of the result property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResult(String value) {
        this.result = value;
    }

}
