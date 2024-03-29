/*
 *  Copyright 2009-2012, Plutext Pty Ltd.
 *   
 *  This file is part of pptx4j, a component of docx4j.

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
package org.docx4j.math;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementRefs;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTMarkup;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.CTMoveBookmark;
import org.docx4j.wml.CTPerm;
import org.docx4j.wml.CTTrackChange;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.ProofErr;
import org.docx4j.wml.RangePermissionStart;
import org.docx4j.wml.RunDel;
import org.docx4j.wml.RunIns;
import org.docx4j.wml.RunTrackChange;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_OMath complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_OMath">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.openxmlformats.org/officeDocument/2006/math}EG_OMathElements" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_OMath", propOrder = {
    "egoMathElements"
})
public class CTOMath
    implements Child
{

    @XmlElementRefs({
        @XmlElementRef(name = "moveToRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "moveFromRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "ins", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = RunIns.class),
        @XmlElementRef(name = "bookmarkEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "commentRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = CommentRangeEnd.class),
        @XmlElementRef(name = "sPre", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "del", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = RunDel.class),
        @XmlElementRef(name = "oMathPara", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlDelRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "nary", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlDelRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "sSup", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "borderBox", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "limLow", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "d", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "groupChr", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "acc", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlMoveFromRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "func", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "permEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "moveFrom", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "moveToRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "rad", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "eqArr", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlInsRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "bookmarkStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "f", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlMoveToRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlInsRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "oMath", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "moveTo", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "sSub", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlMoveFromRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "m", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "moveFromRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "permStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "bar", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "phant", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "limUpp", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "proofErr", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = ProofErr.class),
        @XmlElementRef(name = "box", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "sSubSup", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlMoveToRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "r", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
        @XmlElementRef(name = "commentRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = CommentRangeStart.class)
    })
    protected List<Object> egoMathElements;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the egoMathElements property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the egoMathElements property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEGOMathElements().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link CTMoveBookmark }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMoveBookmark }{@code >}
     * {@link RunIns }
     * {@link JAXBElement }{@code <}{@link CTMarkupRange }{@code >}
     * {@link CommentRangeEnd }
     * {@link JAXBElement }{@code <}{@link CTSPre }{@code >}
     * {@link RunDel }
     * {@link JAXBElement }{@code <}{@link CTOMathPara }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
     * {@link JAXBElement }{@code <}{@link CTNary }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSSup }{@code >}
     * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBorderBox }{@code >}
     * {@link JAXBElement }{@code <}{@link CTD }{@code >}
     * {@link JAXBElement }{@code <}{@link CTLimLow }{@code >}
     * {@link JAXBElement }{@code <}{@link CTAcc }{@code >}
     * {@link JAXBElement }{@code <}{@link CTGroupChr }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPerm }{@code >}
     * {@link JAXBElement }{@code <}{@link CTFunc }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMarkupRange }{@code >}
     * {@link JAXBElement }{@code <}{@link RunTrackChange }{@code >}
     * {@link JAXBElement }{@code <}{@link CTRad }{@code >}
     * {@link JAXBElement }{@code <}{@link CTEqArr }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBookmark }{@code >}
     * {@link JAXBElement }{@code <}{@link CTF }{@code >}
     * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
     * {@link JAXBElement }{@code <}{@link CTOMath }{@code >}
     * {@link JAXBElement }{@code <}{@link RunTrackChange }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSSub }{@code >}
     * {@link JAXBElement }{@code <}{@link CTM }{@code >}
     * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
     * {@link JAXBElement }{@code <}{@link RangePermissionStart }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMarkupRange }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPhant }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBar }{@code >}
     * {@link ProofErr }
     * {@link JAXBElement }{@code <}{@link CTLimUpp }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSSubSup }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBox }{@code >}
     * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
     * {@link JAXBElement }{@code <}{@link CTR }{@code >}
     * {@link CommentRangeStart }
     * 
     * 
     */
    public List<Object> getEGOMathElements() {
        if (egoMathElements == null) {
            egoMathElements = new ArrayList<Object>();
        }
        return this.egoMathElements;
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
