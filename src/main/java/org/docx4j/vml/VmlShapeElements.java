/*
 *  Copyright 2014, Plutext Pty Ltd.
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

import java.util.List;

import javax.xml.bind.JAXBElement;

import org.docx4j.vml.officedrawing.CTCallout;
import org.docx4j.vml.officedrawing.CTClipPath;
import org.docx4j.vml.officedrawing.CTExtrusion;
import org.docx4j.vml.officedrawing.CTLock;
import org.docx4j.vml.officedrawing.CTSignatureLine;
import org.docx4j.vml.officedrawing.CTSkew;
import org.docx4j.vml.presentationDrawing.CTRel;
import org.docx4j.vml.spreadsheetDrawing.CTClientData;
import org.docx4j.vml.wordprocessingDrawing.CTAnchorLock;
import org.docx4j.vml.wordprocessingDrawing.CTBorder;
import org.docx4j.vml.wordprocessingDrawing.CTWrap;

/**
 * Corresponds to group ref="{urn:schemas-microsoft-com:vml}EG_ShapeElements"
 * in the xsd.
 * 
 * @author jharrop
 * @since 3.0.1
 */
public interface VmlShapeElements {
	
    /**
     * Gets the value of the egShapeElements property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the egShapeElements property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEGShapeElements().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link CTClientData }{@code >}
     * {@link JAXBElement }{@code <}{@link CTClipPath }{@code >}
     * {@link JAXBElement }{@code <}{@link CTImageData }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSkew }{@code >}
     * {@link JAXBElement }{@code <}{@link CTHandles }{@code >}
     * {@link JAXBElement }{@code <}{@link CTAnchorLock }{@code >}
     * {@link JAXBElement }{@code <}{@link CTFill }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBorder }{@code >}
     * {@link JAXBElement }{@code <}{@link CTExtrusion }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBorder }{@code >}
     * {@link JAXBElement }{@code <}{@link CTTextbox }{@code >}
     * {@link JAXBElement }{@code <}{@link CTFormulas }{@code >}
     * {@link JAXBElement }{@code <}{@link CTTextPath }{@code >}
     * {@link JAXBElement }{@code <}{@link CTShadow }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBorder }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSignatureLine }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBorder }{@code >}
     * {@link JAXBElement }{@code <}{@link CTStroke }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPath }{@code >}
     * {@link JAXBElement }{@code <}{@link CTLock }{@code >}
     * {@link JAXBElement }{@code <}{@link CTWrap }{@code >}
     * {@link JAXBElement }{@code <}{@link CTRel }{@code >}
     * {@link JAXBElement }{@code <}{@link CTCallout }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getEGShapeElements();

}
