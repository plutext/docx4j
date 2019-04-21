/*
 *  Copyright 2010-2013, Plutext Pty Ltd.
 *   
 *  This file is part of xlsx4j, a component of docx4j.

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
package org.xlsx4j.sml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_RPrElt complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_RPrElt">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="rFont" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_FontName" minOccurs="0"/>
 *         &lt;element name="charset" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_IntProperty" minOccurs="0"/>
 *         &lt;element name="family" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_IntProperty" minOccurs="0"/>
 *         &lt;element name="b" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_BooleanProperty" minOccurs="0"/>
 *         &lt;element name="i" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_BooleanProperty" minOccurs="0"/>
 *         &lt;element name="strike" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_BooleanProperty" minOccurs="0"/>
 *         &lt;element name="outline" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_BooleanProperty" minOccurs="0"/>
 *         &lt;element name="shadow" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_BooleanProperty" minOccurs="0"/>
 *         &lt;element name="condense" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_BooleanProperty" minOccurs="0"/>
 *         &lt;element name="extend" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_BooleanProperty" minOccurs="0"/>
 *         &lt;element name="color" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Color" minOccurs="0"/>
 *         &lt;element name="sz" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_FontSize" minOccurs="0"/>
 *         &lt;element name="u" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_UnderlineProperty" minOccurs="0"/>
 *         &lt;element name="vertAlign" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_VerticalAlignFontProperty" minOccurs="0"/>
 *         &lt;element name="scheme" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_FontScheme" minOccurs="0"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_RPrElt", propOrder = {
    "rFontOrCharsetOrFamily"
})
public class CTRPrElt implements Child
{

    @XmlElementRefs({
        @XmlElementRef(name = "scheme", namespace = "http://schemas.openxmlformats.org/spreadsheetml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "b", namespace = "http://schemas.openxmlformats.org/spreadsheetml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "i", namespace = "http://schemas.openxmlformats.org/spreadsheetml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "u", namespace = "http://schemas.openxmlformats.org/spreadsheetml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "rFont", namespace = "http://schemas.openxmlformats.org/spreadsheetml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "vertAlign", namespace = "http://schemas.openxmlformats.org/spreadsheetml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "outline", namespace = "http://schemas.openxmlformats.org/spreadsheetml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "sz", namespace = "http://schemas.openxmlformats.org/spreadsheetml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "charset", namespace = "http://schemas.openxmlformats.org/spreadsheetml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "strike", namespace = "http://schemas.openxmlformats.org/spreadsheetml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "shadow", namespace = "http://schemas.openxmlformats.org/spreadsheetml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "extend", namespace = "http://schemas.openxmlformats.org/spreadsheetml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "condense", namespace = "http://schemas.openxmlformats.org/spreadsheetml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "family", namespace = "http://schemas.openxmlformats.org/spreadsheetml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "color", namespace = "http://schemas.openxmlformats.org/spreadsheetml/2006/main", type = JAXBElement.class)
    })
    protected List<JAXBElement<?>> rFontOrCharsetOrFamily;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the rFontOrCharsetOrFamily property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rFontOrCharsetOrFamily property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRFontOrCharsetOrFamily().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link CTFontScheme }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBooleanProperty }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBooleanProperty }{@code >}
     * {@link JAXBElement }{@code <}{@link CTUnderlineProperty }{@code >}
     * {@link JAXBElement }{@code <}{@link CTFontName }{@code >}
     * {@link JAXBElement }{@code <}{@link CTVerticalAlignFontProperty }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBooleanProperty }{@code >}
     * {@link JAXBElement }{@code <}{@link CTFontSize }{@code >}
     * {@link JAXBElement }{@code <}{@link CTIntProperty }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBooleanProperty }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBooleanProperty }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBooleanProperty }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBooleanProperty }{@code >}
     * {@link JAXBElement }{@code <}{@link CTIntProperty }{@code >}
     * {@link JAXBElement }{@code <}{@link CTColor }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getRFontOrCharsetOrFamily() {
        if (rFontOrCharsetOrFamily == null) {
            rFontOrCharsetOrFamily = new ArrayList<JAXBElement<?>>();
        }
        return this.rFontOrCharsetOrFamily;
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
