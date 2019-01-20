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


package org.docx4j.vml;

import java.math.BigInteger;
import org.docx4j.vml.ArrayListVml;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.vml.officedrawing.CTCallout;
import org.docx4j.vml.officedrawing.CTClipPath;
import org.docx4j.vml.officedrawing.CTDiagram;
import org.docx4j.vml.officedrawing.CTExtrusion;
import org.docx4j.vml.officedrawing.CTLock;
import org.docx4j.vml.officedrawing.CTSignatureLine;
import org.docx4j.vml.officedrawing.CTSkew;
import org.docx4j.vml.officedrawing.STBWMode;
import org.docx4j.vml.officedrawing.STConnectorType;
import org.docx4j.vml.officedrawing.STHrAlign;
import org.docx4j.vml.officedrawing.STInsetMode;
import org.docx4j.vml.officedrawing.STTrueFalse;
import org.docx4j.vml.presentationDrawing.CTRel;
import org.docx4j.vml.spreadsheetDrawing.CTClientData;
import org.docx4j.vml.wordprocessingDrawing.CTAnchorLock;
import org.docx4j.vml.wordprocessingDrawing.CTBorder;
import org.docx4j.vml.wordprocessingDrawing.CTWrap;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Group complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Group">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;group ref="{urn:schemas-microsoft-com:vml}EG_ShapeElements"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}group"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}shape"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}shapetype"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}arc"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}curve"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}image"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}line"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}oval"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}polyline"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}rect"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:vml}roundrect"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:office:office}diagram"/>
 *       &lt;/choice>
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_Fill"/>
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_AllCoreAttributes"/>
 *       &lt;attribute name="editas" type="{urn:schemas-microsoft-com:vml}ST_EditAs" />
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}tableproperties"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}tablelimits"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Group", propOrder = {
    "pathOrFormulasOrHandles"
})
public class CTGroup implements Child, VmlShapeElements, VmlAllCoreAttributes
{

    @XmlElementRefs({
        @XmlElementRef(name = "ClientData", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "rect", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "clippath", namespace = "urn:schemas-microsoft-com:office:office", type = JAXBElement.class),
        @XmlElementRef(name = "curve", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "borderleft", namespace = "urn:schemas-microsoft-com:office:word", type = JAXBElement.class),
        @XmlElementRef(name = "polyline", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "textpath", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "shadow", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "shapetype", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "signatureline", namespace = "urn:schemas-microsoft-com:office:office", type = JAXBElement.class),
        @XmlElementRef(name = "borderbottom", namespace = "urn:schemas-microsoft-com:office:word", type = JAXBElement.class),
        @XmlElementRef(name = "stroke", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "oval", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "path", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "image", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "wrap", namespace = "urn:schemas-microsoft-com:office:word", type = JAXBElement.class),
        @XmlElementRef(name = "imagedata", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "group", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "handles", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "skew", namespace = "urn:schemas-microsoft-com:office:office", type = JAXBElement.class),
        @XmlElementRef(name = "anchorlock", namespace = "urn:schemas-microsoft-com:office:word", type = JAXBElement.class),
        @XmlElementRef(name = "fill", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "borderright", namespace = "urn:schemas-microsoft-com:office:word", type = JAXBElement.class),
        @XmlElementRef(name = "line", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "extrusion", namespace = "urn:schemas-microsoft-com:office:office", type = JAXBElement.class),
        @XmlElementRef(name = "textbox", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "formulas", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "diagram", namespace = "urn:schemas-microsoft-com:office:office", type = JAXBElement.class),
        @XmlElementRef(name = "roundrect", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "bordertop", namespace = "urn:schemas-microsoft-com:office:word", type = JAXBElement.class),
        @XmlElementRef(name = "arc", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "shape", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "lock", namespace = "urn:schemas-microsoft-com:office:office", type = JAXBElement.class),
        @XmlElementRef(name = "textdata", namespace = "urn:schemas-microsoft-com:office:powerpoint", type = JAXBElement.class),
        @XmlElementRef(name = "callout", namespace = "urn:schemas-microsoft-com:office:office", type = JAXBElement.class)
    })
    protected List<JAXBElement<?>> pathOrFormulasOrHandles = new ArrayListVml<JAXBElement<?>>(this);
    @XmlAttribute(name = "editas")
    protected STEditAs editas;
    @XmlAttribute(name = "tableproperties", namespace = "urn:schemas-microsoft-com:office:office")
    protected String tableproperties;
    @XmlAttribute(name = "tablelimits", namespace = "urn:schemas-microsoft-com:office:office")
    protected String tablelimits;
    @XmlAttribute(name = "filled")
    protected org.docx4j.vml.STTrueFalse filled;
    @XmlAttribute(name = "fillcolor")
    protected String fillcolor;
    @XmlAttribute(name = "href")
    protected String href;
    @XmlAttribute(name = "target")
    protected String target;
    @XmlAttribute(name = "class")
    protected String clazz;
    @XmlAttribute(name = "title")
    protected String title;
    @XmlAttribute(name = "alt")
    protected String alt;
    @XmlAttribute(name = "coordsize")
    protected String coordsize;
    @XmlAttribute(name = "coordorigin")
    protected String coordorigin;
    @XmlAttribute(name = "wrapcoords")
    protected String wrapcoords;
    @XmlAttribute(name = "print")
    protected org.docx4j.vml.STTrueFalse print;
    @XmlAttribute(name = "style")
    protected String style;
    @XmlAttribute(name = "id")
    protected String vmlId;
    @XmlAttribute(name = "spid", namespace = "urn:schemas-microsoft-com:office:office")
    protected String spid;
    @XmlAttribute(name = "oned", namespace = "urn:schemas-microsoft-com:office:office")
    protected org.docx4j.vml.officedrawing.STTrueFalse oned;
    @XmlAttribute(name = "regroupid", namespace = "urn:schemas-microsoft-com:office:office")
    protected BigInteger regroupid;
    @XmlAttribute(name = "doubleclicknotify", namespace = "urn:schemas-microsoft-com:office:office")
    protected org.docx4j.vml.officedrawing.STTrueFalse doubleclicknotify;
    @XmlAttribute(name = "button", namespace = "urn:schemas-microsoft-com:office:office")
    protected org.docx4j.vml.officedrawing.STTrueFalse button;
    @XmlAttribute(name = "userhidden", namespace = "urn:schemas-microsoft-com:office:office")
    protected org.docx4j.vml.officedrawing.STTrueFalse userhidden;
    @XmlAttribute(name = "bullet", namespace = "urn:schemas-microsoft-com:office:office")
    protected org.docx4j.vml.officedrawing.STTrueFalse bullet;
    @XmlAttribute(name = "hr", namespace = "urn:schemas-microsoft-com:office:office")
    protected org.docx4j.vml.officedrawing.STTrueFalse hr;
    @XmlAttribute(name = "hrstd", namespace = "urn:schemas-microsoft-com:office:office")
    protected org.docx4j.vml.officedrawing.STTrueFalse hrstd;
    @XmlAttribute(name = "hrnoshade", namespace = "urn:schemas-microsoft-com:office:office")
    protected org.docx4j.vml.officedrawing.STTrueFalse hrnoshade;
    @XmlAttribute(name = "hrpct", namespace = "urn:schemas-microsoft-com:office:office")
    protected Float hrpct;
    @XmlAttribute(name = "hralign", namespace = "urn:schemas-microsoft-com:office:office")
    protected STHrAlign hralign;
    @XmlAttribute(name = "allowincell", namespace = "urn:schemas-microsoft-com:office:office")
    protected org.docx4j.vml.officedrawing.STTrueFalse allowincell;
    @XmlAttribute(name = "allowoverlap", namespace = "urn:schemas-microsoft-com:office:office")
    protected org.docx4j.vml.officedrawing.STTrueFalse allowoverlap;
    @XmlAttribute(name = "userdrawn", namespace = "urn:schemas-microsoft-com:office:office")
    protected org.docx4j.vml.officedrawing.STTrueFalse userdrawn;
    @XmlAttribute(name = "bordertopcolor", namespace = "urn:schemas-microsoft-com:office:office")
    protected String bordertopcolor;
    @XmlAttribute(name = "borderleftcolor", namespace = "urn:schemas-microsoft-com:office:office")
    protected String borderleftcolor;
    @XmlAttribute(name = "borderbottomcolor", namespace = "urn:schemas-microsoft-com:office:office")
    protected String borderbottomcolor;
    @XmlAttribute(name = "borderrightcolor", namespace = "urn:schemas-microsoft-com:office:office")
    protected String borderrightcolor;
    @XmlAttribute(name = "dgmlayout", namespace = "urn:schemas-microsoft-com:office:office")
    protected BigInteger dgmlayout;
    @XmlAttribute(name = "dgmnodekind", namespace = "urn:schemas-microsoft-com:office:office")
    protected BigInteger dgmnodekind;
    @XmlAttribute(name = "dgmlayoutmru", namespace = "urn:schemas-microsoft-com:office:office")
    protected BigInteger dgmlayoutmru;
    @XmlAttribute(name = "insetmode", namespace = "urn:schemas-microsoft-com:office:office")
    protected STInsetMode insetmode;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the pathOrFormulasOrHandles property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pathOrFormulasOrHandles property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPathOrFormulasOrHandles().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link CTClientData }{@code >}
     * {@link JAXBElement }{@code <}{@link CTRect }{@code >}
     * {@link JAXBElement }{@code <}{@link CTClipPath }{@code >}
     * {@link JAXBElement }{@code <}{@link CTCurve }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPolyLine }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBorder }{@code >}
     * {@link JAXBElement }{@code <}{@link CTTextPath }{@code >}
     * {@link JAXBElement }{@code <}{@link CTShadow }{@code >}
     * {@link JAXBElement }{@code <}{@link CTShapetype }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSignatureLine }{@code >}
     * {@link JAXBElement }{@code <}{@link CTStroke }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBorder }{@code >}
     * {@link JAXBElement }{@code <}{@link CTOval }{@code >}
     * {@link JAXBElement }{@code <}{@link CTImage }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPath }{@code >}
     * {@link JAXBElement }{@code <}{@link CTWrap }{@code >}
     * {@link JAXBElement }{@code <}{@link CTImageData }{@code >}
     * {@link JAXBElement }{@code <}{@link CTGroup }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSkew }{@code >}
     * {@link JAXBElement }{@code <}{@link CTHandles }{@code >}
     * {@link JAXBElement }{@code <}{@link CTAnchorLock }{@code >}
     * {@link JAXBElement }{@code <}{@link CTLine }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBorder }{@code >}
     * {@link JAXBElement }{@code <}{@link CTFill }{@code >}
     * {@link JAXBElement }{@code <}{@link CTExtrusion }{@code >}
     * {@link JAXBElement }{@code <}{@link CTTextbox }{@code >}
     * {@link JAXBElement }{@code <}{@link CTFormulas }{@code >}
     * {@link JAXBElement }{@code <}{@link CTDiagram }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBorder }{@code >}
     * {@link JAXBElement }{@code <}{@link CTRoundRect }{@code >}
     * {@link JAXBElement }{@code <}{@link CTArc }{@code >}
     * {@link JAXBElement }{@code <}{@link CTShape }{@code >}
     * {@link JAXBElement }{@code <}{@link CTLock }{@code >}
     * {@link JAXBElement }{@code <}{@link CTCallout }{@code >}
     * {@link JAXBElement }{@code <}{@link CTRel }{@code >}
     * 
     * 
     */
    @Deprecated
    public List<JAXBElement<?>> getPathOrFormulasOrHandles() {
        if (pathOrFormulasOrHandles == null) {
            pathOrFormulasOrHandles = new ArrayListVml<JAXBElement<?>>(this);
        }
        return this.pathOrFormulasOrHandles;
    }
    
    /* (non-Javadoc)
     * @see org.docx4j.vml.VmlShapeElements#getEGShapeElements()
     * @since 3.0.1
     */
    public List<JAXBElement<?>> getEGShapeElements() {
        if (pathOrFormulasOrHandles == null) {
            pathOrFormulasOrHandles = new ArrayListVml<JAXBElement<?>>(this);
        }
        return this.pathOrFormulasOrHandles;    	
    }

    /**
     * Gets the value of the editas property.
     * 
     * @return
     *     possible object is
     *     {@link STEditAs }
     *     
     */
    public STEditAs getEditas() {
        return editas;
    }

    /**
     * Sets the value of the editas property.
     * 
     * @param value
     *     allowed object is
     *     {@link STEditAs }
     *     
     */
    public void setEditas(STEditAs value) {
        this.editas = value;
    }

    /**
     * Table Properties
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTableproperties() {
        return tableproperties;
    }

    /**
     * Sets the value of the tableproperties property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTableproperties(String value) {
        this.tableproperties = value;
    }

    /**
     * Table Row Height Limits
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTablelimits() {
        return tablelimits;
    }

    /**
     * Sets the value of the tablelimits property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTablelimits(String value) {
        this.tablelimits = value;
    }

    /**
     * Gets the value of the filled property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getFilled() {
        return filled;
    }

    /**
     * Sets the value of the filled property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setFilled(org.docx4j.vml.STTrueFalse value) {
        this.filled = value;
    }

    /**
     * Gets the value of the fillcolor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFillcolor() {
        return fillcolor;
    }

    /**
     * Sets the value of the fillcolor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFillcolor(String value) {
        this.fillcolor = value;
    }

    /**
     * Gets the value of the href property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the value of the href property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHref(String value) {
        this.href = value;
    }

    /**
     * Gets the value of the target property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTarget() {
        return target;
    }

    /**
     * Sets the value of the target property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTarget(String value) {
        this.target = value;
    }

    /**
     * Gets the value of the clazz property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Sets the value of the clazz property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClazz(String value) {
        this.clazz = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the alt property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlt() {
        return alt;
    }

    /**
     * Sets the value of the alt property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlt(String value) {
        this.alt = value;
    }

    /**
     * Gets the value of the coordsize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCoordsize() {
        return coordsize;
    }

    /**
     * Sets the value of the coordsize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCoordsize(String value) {
        this.coordsize = value;
    }

    /**
     * Gets the value of the coordorigin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCoordorigin() {
        return coordorigin;
    }

    /**
     * Sets the value of the coordorigin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCoordorigin(String value) {
        this.coordorigin = value;
    }

    /**
     * Gets the value of the wrapcoords property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWrapcoords() {
        return wrapcoords;
    }

    /**
     * Sets the value of the wrapcoords property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWrapcoords(String value) {
        this.wrapcoords = value;
    }

    /**
     * Gets the value of the print property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getPrint() {
        return print;
    }

    /**
     * Sets the value of the print property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setPrint(org.docx4j.vml.STTrueFalse value) {
        this.print = value;
    }

    /**
     * Gets the value of the style property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStyle() {
        return style;
    }

    /**
     * Sets the value of the style property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStyle(String value) {
        this.style = value;
    }

    /**
     * Gets the value of the vmlId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVmlId() {
        return vmlId;
    }

    /**
     * Sets the value of the vmlId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVmlId(String value) {
        this.vmlId = value;
    }

    /**
     * Optional String
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpid() {
        return spid;
    }

    /**
     * Sets the value of the spid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpid(String value) {
        this.spid = value;
    }

    /**
     * Shape Handle Toggle
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getOned() {
        return oned;
    }

    /**
     * Sets the value of the oned property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setOned(org.docx4j.vml.officedrawing.STTrueFalse value) {
        this.oned = value;
    }

    /**
     * Regroup ID
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRegroupid() {
        return regroupid;
    }

    /**
     * Sets the value of the regroupid property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRegroupid(BigInteger value) {
        this.regroupid = value;
    }

    /**
     * Double-click Notification Toggle
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getDoubleclicknotify() {
        return doubleclicknotify;
    }

    /**
     * Sets the value of the doubleclicknotify property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setDoubleclicknotify(org.docx4j.vml.officedrawing.STTrueFalse value) {
        this.doubleclicknotify = value;
    }

    /**
     * Button Behavior Toggle
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getButton() {
        return button;
    }

    /**
     * Sets the value of the button property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setButton(org.docx4j.vml.officedrawing.STTrueFalse value) {
        this.button = value;
    }

    /**
     * Hide Script Anchors
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getUserhidden() {
        return userhidden;
    }

    /**
     * Sets the value of the userhidden property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setUserhidden(org.docx4j.vml.officedrawing.STTrueFalse value) {
        this.userhidden = value;
    }

    /**
     * Graphical Bullet
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getBullet() {
        return bullet;
    }

    /**
     * Sets the value of the bullet property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setBullet(org.docx4j.vml.officedrawing.STTrueFalse value) {
        this.bullet = value;
    }

    /**
     * Horizontal Rule Toggle
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getHr() {
        return hr;
    }

    /**
     * Sets the value of the hr property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setHr(org.docx4j.vml.officedrawing.STTrueFalse value) {
        this.hr = value;
    }

    /**
     * Horizontal Rule Standard Display Toggle
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getHrstd() {
        return hrstd;
    }

    /**
     * Sets the value of the hrstd property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setHrstd(org.docx4j.vml.officedrawing.STTrueFalse value) {
        this.hrstd = value;
    }

    /**
     * Horizontal Rule 3D Shading Toggle
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getHrnoshade() {
        return hrnoshade;
    }

    /**
     * Sets the value of the hrnoshade property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setHrnoshade(org.docx4j.vml.officedrawing.STTrueFalse value) {
        this.hrnoshade = value;
    }

    /**
     * Horizontal Rule Length Percentage
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getHrpct() {
        return hrpct;
    }

    /**
     * Sets the value of the hrpct property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setHrpct(Float value) {
        this.hrpct = value;
    }

    /**
     * Horizontal Rule Alignment
     * 
     * @return
     *     possible object is
     *     {@link STHrAlign }
     *     
     */
    public STHrAlign getHralign() {
        if (hralign == null) {
            return STHrAlign.LEFT;
        } else {
            return hralign;
        }
    }

    /**
     * Sets the value of the hralign property.
     * 
     * @param value
     *     allowed object is
     *     {@link STHrAlign }
     *     
     */
    public void setHralign(STHrAlign value) {
        this.hralign = value;
    }

    /**
     * Allow in Table Cell
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getAllowincell() {
        return allowincell;
    }

    /**
     * Sets the value of the allowincell property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setAllowincell(org.docx4j.vml.officedrawing.STTrueFalse value) {
        this.allowincell = value;
    }

    /**
     * Allow Shape Overlap
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getAllowoverlap() {
        return allowoverlap;
    }

    /**
     * Sets the value of the allowoverlap property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setAllowoverlap(org.docx4j.vml.officedrawing.STTrueFalse value) {
        this.allowoverlap = value;
    }

    /**
     * Exists In Master Slide
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getUserdrawn() {
        return userdrawn;
    }

    /**
     * Sets the value of the userdrawn property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setUserdrawn(org.docx4j.vml.officedrawing.STTrueFalse value) {
        this.userdrawn = value;
    }

    /**
     * Border Top Color
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBordertopcolor() {
        return bordertopcolor;
    }

    /**
     * Sets the value of the bordertopcolor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBordertopcolor(String value) {
        this.bordertopcolor = value;
    }

    /**
     * Border Left Color
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBorderleftcolor() {
        return borderleftcolor;
    }

    /**
     * Sets the value of the borderleftcolor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBorderleftcolor(String value) {
        this.borderleftcolor = value;
    }

    /**
     * Bottom Border Color
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBorderbottomcolor() {
        return borderbottomcolor;
    }

    /**
     * Sets the value of the borderbottomcolor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBorderbottomcolor(String value) {
        this.borderbottomcolor = value;
    }

    /**
     * Border Right Color
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBorderrightcolor() {
        return borderrightcolor;
    }

    /**
     * Sets the value of the borderrightcolor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBorderrightcolor(String value) {
        this.borderrightcolor = value;
    }

    /**
     * Diagram Node Layout Identifier
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDgmlayout() {
        return dgmlayout;
    }

    /**
     * Sets the value of the dgmlayout property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDgmlayout(BigInteger value) {
        this.dgmlayout = value;
    }

    /**
     * Diagram Node Identifier
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDgmnodekind() {
        return dgmnodekind;
    }

    /**
     * Sets the value of the dgmnodekind property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDgmnodekind(BigInteger value) {
        this.dgmnodekind = value;
    }

    /**
     * Diagram Node Recent Layout Identifier
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDgmlayoutmru() {
        return dgmlayoutmru;
    }

    /**
     * Sets the value of the dgmlayoutmru property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDgmlayoutmru(BigInteger value) {
        this.dgmlayoutmru = value;
    }

    /**
     * Text Inset Mode
     * 
     * @return
     *     possible object is
     *     {@link STInsetMode }
     *     
     */
    public STInsetMode getInsetmode() {
        if (insetmode == null) {
            return STInsetMode.CUSTOM;
        } else {
            return insetmode;
        }
    }

    /**
     * Sets the value of the insetmode property.
     * 
     * @param value
     *     allowed object is
     *     {@link STInsetMode }
     *     
     */
    public void setInsetmode(STInsetMode value) {
        this.insetmode = value;
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
