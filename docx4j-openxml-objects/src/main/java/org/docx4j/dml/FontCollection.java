/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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


package org.docx4j.dml;

import org.docx4j.dml.ArrayListDml;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_FontCollection complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_FontCollection"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="latin" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextFont"/&gt;
 *         &lt;element name="ea" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextFont"/&gt;
 *         &lt;element name="cs" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextFont"/&gt;
 *         &lt;element name="font" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="script" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="typeface" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextTypeface" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_FontCollection", propOrder = {
    "latin",
    "ea",
    "cs",
    "font",
    "extLst"
})
public class FontCollection implements Child
{

    @XmlElement(required = true)
    protected TextFont latin;
    @XmlElement(required = true)
    protected TextFont ea;
    @XmlElement(required = true)
    protected TextFont cs;
    protected List<FontCollection.Font> font = new ArrayListDml<FontCollection.Font>(this);
    protected CTOfficeArtExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the latin property.
     * 
     * @return
     *     possible object is
     *     {@link TextFont }
     *     
     */
    public TextFont getLatin() {
        return latin;
    }

    /**
     * Sets the value of the latin property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextFont }
     *     
     */
    public void setLatin(TextFont value) {
        this.latin = value;
    }

    /**
     * Gets the value of the ea property.
     * 
     * @return
     *     possible object is
     *     {@link TextFont }
     *     
     */
    public TextFont getEa() {
        return ea;
    }

    /**
     * Sets the value of the ea property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextFont }
     *     
     */
    public void setEa(TextFont value) {
        this.ea = value;
    }

    /**
     * Gets the value of the cs property.
     * 
     * @return
     *     possible object is
     *     {@link TextFont }
     *     
     */
    public TextFont getCs() {
        return cs;
    }

    /**
     * Sets the value of the cs property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextFont }
     *     
     */
    public void setCs(TextFont value) {
        this.cs = value;
    }

    /**
     * Gets the value of the font property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the font property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFont().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FontCollection.Font }
     * 
     * 
     */
    public List<FontCollection.Font> getFont() {
        if (font == null) {
            font = new ArrayListDml<FontCollection.Font>(this);
        }
        return this.font;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public CTOfficeArtExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public void setExtLst(CTOfficeArtExtensionList value) {
        this.extLst = value;
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="script" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;attribute name="typeface" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextTypeface" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Font implements Child
    {

        @XmlAttribute(name = "script", required = true)
        protected String script;
        @XmlAttribute(name = "typeface", required = true)
        protected String typeface;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the script property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getScript() {
            return script;
        }

        /**
         * Sets the value of the script property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setScript(String value) {
            this.script = value;
        }

        /**
         * Gets the value of the typeface property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTypeface() {
            return typeface;
        }

        /**
         * Sets the value of the typeface property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTypeface(String value) {
            this.typeface = value;
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

}
