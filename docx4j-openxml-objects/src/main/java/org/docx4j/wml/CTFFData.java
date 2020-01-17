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
 * <p>Java class for CT_FFData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_FFData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="name" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FFName"/>
 *         &lt;element name="enabled" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue"/>
 *         &lt;element name="calcOnExit" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue"/>
 *         &lt;element name="entryMacro" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_MacroName" minOccurs="0"/>
 *         &lt;element name="exitMacro" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_MacroName" minOccurs="0"/>
 *         &lt;element name="helpText" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FFHelpText" minOccurs="0"/>
 *         &lt;element name="statusText" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FFStatusText" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="checkBox" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FFCheckBox"/>
 *           &lt;element name="ddList" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FFDDList"/>
 *           &lt;element name="textInput" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FFTextInput"/>
 *         &lt;/choice>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_FFData", propOrder = {
    "nameOrEnabledOrCalcOnExit"
})
public class CTFFData implements Child
{

    @XmlElementRefs({
        @XmlElementRef(name = "entryMacro", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "name", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "enabled", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "textInput", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "calcOnExit", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "checkBox", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "helpText", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "exitMacro", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "statusText", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "ddList", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class)
    })
    protected List<JAXBElement<?>> nameOrEnabledOrCalcOnExit = new ArrayListWml<JAXBElement<?>>(this);
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the nameOrEnabledOrCalcOnExit property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nameOrEnabledOrCalcOnExit property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNameOrEnabledOrCalcOnExit().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link CTMacroName }{@code >}
     * {@link JAXBElement }{@code <}{@link CTFFName }{@code >}
     * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
     * {@link JAXBElement }{@code <}{@link CTFFTextInput }{@code >}
     * {@link JAXBElement }{@code <}{@link BooleanDefaultTrue }{@code >}
     * {@link JAXBElement }{@code <}{@link CTFFCheckBox }{@code >}
     * {@link JAXBElement }{@code <}{@link CTFFHelpText }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMacroName }{@code >}
     * {@link JAXBElement }{@code <}{@link CTFFStatusText }{@code >}
     * {@link JAXBElement }{@code <}{@link CTFFDDList }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getNameOrEnabledOrCalcOnExit() {
        if (nameOrEnabledOrCalcOnExit == null) {
            nameOrEnabledOrCalcOnExit = new ArrayListWml<JAXBElement<?>>(this);
        }
        return this.nameOrEnabledOrCalcOnExit;
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
