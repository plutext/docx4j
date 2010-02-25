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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SVG.font-face-src.content complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SVG.font-face-src.content">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element ref="{http://www.w3.org/2000/svg}font-face-uri"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}font-face-name"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SVG.font-face-src.content", propOrder = {
    "fontFaceUriOrFontFaceName"
})
public class SVGFontFaceSrcContent {

    @XmlElements({
        @XmlElement(name = "font-face-name", type = FontFaceName.class),
        @XmlElement(name = "font-face-uri", type = FontFaceUri.class)
    })
    protected List<Object> fontFaceUriOrFontFaceName;

    /**
     * Gets the value of the fontFaceUriOrFontFaceName property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fontFaceUriOrFontFaceName property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFontFaceUriOrFontFaceName().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FontFaceName }
     * {@link FontFaceUri }
     * 
     * 
     */
    public List<Object> getFontFaceUriOrFontFaceName() {
        if (fontFaceUriOrFontFaceName == null) {
            fontFaceUriOrFontFaceName = new ArrayList<Object>();
        }
        return this.fontFaceUriOrFontFaceName;
    }

}
