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
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{http://www.w3.org/2000/svg}desc"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}title"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}metadata"/>
 *       &lt;/choice>
 *       &lt;attribute name="viewBox" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="preserveAspectRatio" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" default="xMidYMid meet" />
 *       &lt;attribute name="zoomAndPan" default="magnify">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="disable"/>
 *             &lt;enumeration value="magnify"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="viewTarget" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "descOrTitleOrMetadata"
})
@XmlRootElement(name = "view")
public class View {

    @XmlElements({
        @XmlElement(name = "metadata", type = Metadata.class),
        @XmlElement(name = "title", type = Title.class),
        @XmlElement(name = "desc", type = Desc.class)
    })
    protected List<Object> descOrTitleOrMetadata;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String viewBox;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String preserveAspectRatio;
    @XmlAttribute
    protected String zoomAndPan;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String viewTarget;

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
     * {@link Metadata }
     * {@link Title }
     * {@link Desc }
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
     * Gets the value of the viewBox property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getViewBox() {
        return viewBox;
    }

    /**
     * Sets the value of the viewBox property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setViewBox(String value) {
        this.viewBox = value;
    }

    /**
     * Gets the value of the preserveAspectRatio property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreserveAspectRatio() {
        if (preserveAspectRatio == null) {
            return "xMidYMid meet";
        } else {
            return preserveAspectRatio;
        }
    }

    /**
     * Sets the value of the preserveAspectRatio property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreserveAspectRatio(String value) {
        this.preserveAspectRatio = value;
    }

    /**
     * Gets the value of the zoomAndPan property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getZoomAndPan() {
        if (zoomAndPan == null) {
            return "magnify";
        } else {
            return zoomAndPan;
        }
    }

    /**
     * Sets the value of the zoomAndPan property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setZoomAndPan(String value) {
        this.zoomAndPan = value;
    }

    /**
     * Gets the value of the viewTarget property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getViewTarget() {
        return viewTarget;
    }

    /**
     * Sets the value of the viewTarget property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setViewTarget(String value) {
        this.viewTarget = value;
    }

}
