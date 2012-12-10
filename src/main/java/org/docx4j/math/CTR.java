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
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTFtnEdnRef;
import org.docx4j.wml.CTObject;
import org.docx4j.wml.CTRuby;
import org.docx4j.wml.DelText;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.Pict;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Text;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_R complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_R">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="rPr" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_RPR" minOccurs="0"/>
 *         &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_RPr" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_RunInnerContent"/>
 *           &lt;element name="t" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_Text" minOccurs="0"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_R", propOrder = {
    "content"
})
public class CTR
    implements Child
{

    @XmlElementRefs({
        @XmlElementRef(name = "drawing", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "noBreakHyphen", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "t", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = CTR.TMath.class),
        @XmlElementRef(name = "t", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "footnoteRef", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "monthLong", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "monthShort", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "rPr", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "delText", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = DelText.class),
        @XmlElementRef(name = "sym", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "tab", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "commentReference", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "yearShort", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "footnoteReference", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "lastRenderedPageBreak", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "pict", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "ruby", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "endnoteRef", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "delInstrText", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "dayLong", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "yearLong", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "softHyphen", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "endnoteReference", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "rPr", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = CTR.RPrMath.class),
        @XmlElementRef(name = "fldChar", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "object", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "separator", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "instrText", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "continuationSeparator", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "dayShort", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "pgNum", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "ptab", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "br", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = Br.class),
        @XmlElementRef(name = "cr", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "annotationRef", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class)
    })
    protected List<Object> content;
    @XmlTransient
    private Object parent;

    /**
     * Gets the rest of the content model. 
     * 
     * <p>
     * You are getting this "catch-all" property because of the following reason: 
     * The field name "RPr" is used by two different parts of a schema. See: 
     * line 7256 of file:/C:/Users/jharrop/git/plutext/docx4jGREAT/xsd/wml/wml.xsd
     * line 449 of file:/C:/Users/jharrop/git/plutext/docx4jGREAT/xsd/shared/shared-math-2ed.xsd
     * <p>
     * To get rid of this property, apply a property customization to one 
     * of both of the following declarations to change their names: 
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
     * {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.NoBreakHyphen }{@code >}
     * {@link JAXBElement }{@code <}{@link Drawing }{@code >}
     * {@link CTR.TMath }
     * {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.FootnoteRef }{@code >}
     * {@link JAXBElement }{@code <}{@link Text }{@code >}
     * {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.MonthLong }{@code >}
     * {@link JAXBElement }{@code <}{@link RPr }{@code >}
     * {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.MonthShort }{@code >}
     * {@link DelText }
     * {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.Sym }{@code >}
     * {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.Tab }{@code >}
     * {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.CommentReference }{@code >}
     * {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.YearShort }{@code >}
     * {@link JAXBElement }{@code <}{@link CTFtnEdnRef }{@code >}
     * {@link JAXBElement }{@code <}{@link Pict }{@code >}
     * {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.LastRenderedPageBreak }{@code >}
     * {@link JAXBElement }{@code <}{@link CTRuby }{@code >}
     * {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.EndnoteRef }{@code >}
     * {@link JAXBElement }{@code <}{@link Text }{@code >}
     * {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.DayLong }{@code >}
     * {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.YearLong }{@code >}
     * {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.SoftHyphen }{@code >}
     * {@link JAXBElement }{@code <}{@link CTFtnEdnRef }{@code >}
     * {@link CTR.RPrMath }
     * {@link JAXBElement }{@code <}{@link CTObject }{@code >}
     * {@link JAXBElement }{@code <}{@link FldChar }{@code >}
     * {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.Separator }{@code >}
     * {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.ContinuationSeparator }{@code >}
     * {@link JAXBElement }{@code <}{@link Text }{@code >}
     * {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.PgNum }{@code >}
     * {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.DayShort }{@code >}
     * {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.Ptab }{@code >}
     * {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.Cr }{@code >}
     * {@link Br }
     * {@link JAXBElement }{@code <}{@link org.docx4j.wml.R.AnnotationRef }{@code >}
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

    public static class RPrMath
        extends JAXBElement<CTRPR>
    {

        protected final static QName NAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "rPr");

        public RPrMath(CTRPR value) {
            super(NAME, ((Class) CTRPR.class), CTR.class, value);
        }

        public RPrMath() {
            super(NAME, ((Class) CTRPR.class), CTR.class, null);
        }

    }

    public static class TMath
        extends JAXBElement<CTText>
    {

        protected final static QName NAME = new QName("http://schemas.openxmlformats.org/officeDocument/2006/math", "t");

        public TMath(CTText value) {
            super(NAME, ((Class) CTText.class), CTR.class, value);
        }

        public TMath() {
            super(NAME, ((Class) CTText.class), CTR.class, null);
        }

    }

}
