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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.vml.officedrawing.CTCallout;
import org.docx4j.vml.officedrawing.CTClipPath;
import org.docx4j.vml.officedrawing.CTExtrusion;
import org.docx4j.vml.officedrawing.CTInk;
import org.docx4j.vml.officedrawing.CTLock;
import org.docx4j.vml.officedrawing.CTSignatureLine;
import org.docx4j.vml.officedrawing.CTSkew;
import org.docx4j.vml.officedrawing.STBWMode;
import org.docx4j.vml.officedrawing.STConnectorType;
import org.docx4j.vml.officedrawing.STHrAlign;
import org.docx4j.vml.officedrawing.STInsetMode;
import org.docx4j.vml.presentationDrawing.CTEmpty;
import org.docx4j.vml.presentationDrawing.CTRel;
import org.docx4j.vml.spreadsheetDrawing.CTClientData;
import org.docx4j.vml.wordprocessingDrawing.CTAnchorLock;
import org.docx4j.vml.wordprocessingDrawing.CTBorder;
import org.docx4j.vml.wordprocessingDrawing.CTWrap;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Shape complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Shape">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;group ref="{urn:schemas-microsoft-com:vml}EG_ShapeElements"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:office:office}ink"/>
 *         &lt;element ref="{urn:schemas-microsoft-com:office:powerpoint}iscomment"/>
 *       &lt;/choice>
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_Type"/>
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_AllCoreAttributes"/>
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_Adj"/>
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_Path"/>
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_AllShapeAttributes"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}gfxdata"/>
 *       &lt;attribute name="equationxml" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "urn:schemas-microsoft-com:vml", name = "CT_Shape", propOrder = {
    "pathOrFormulasOrHandles"
})
@XmlRootElement(name = "shape")
public class CTShape implements Child, VmlShapeElements, VmlAllCoreAttributes, VmlAllShapeAttributes
{

    @XmlElementRefs({
        @XmlElementRef(name = "ClientData", namespace = "urn:schemas-microsoft-com:office:excel", type = JAXBElement.class),
        @XmlElementRef(name = "clippath", namespace = "urn:schemas-microsoft-com:office:office", type = JAXBElement.class),
        @XmlElementRef(name = "borderleft", namespace = "urn:schemas-microsoft-com:office:word", type = JAXBElement.class),
        @XmlElementRef(name = "textpath", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "shadow", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "signatureline", namespace = "urn:schemas-microsoft-com:office:office", type = JAXBElement.class),
        @XmlElementRef(name = "borderbottom", namespace = "urn:schemas-microsoft-com:office:word", type = JAXBElement.class),
        @XmlElementRef(name = "stroke", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "path", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "iscomment", namespace = "urn:schemas-microsoft-com:office:powerpoint", type = JAXBElement.class),
        @XmlElementRef(name = "wrap", namespace = "urn:schemas-microsoft-com:office:word", type = JAXBElement.class),
        @XmlElementRef(name = "ink", namespace = "urn:schemas-microsoft-com:office:office", type = JAXBElement.class),
        @XmlElementRef(name = "imagedata", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "skew", namespace = "urn:schemas-microsoft-com:office:office", type = JAXBElement.class),
        @XmlElementRef(name = "handles", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "anchorlock", namespace = "urn:schemas-microsoft-com:office:word", type = JAXBElement.class),
        @XmlElementRef(name = "fill", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "borderright", namespace = "urn:schemas-microsoft-com:office:word", type = JAXBElement.class),
        @XmlElementRef(name = "extrusion", namespace = "urn:schemas-microsoft-com:office:office", type = JAXBElement.class),
        @XmlElementRef(name = "textbox", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "formulas", namespace = "urn:schemas-microsoft-com:vml", type = JAXBElement.class),
        @XmlElementRef(name = "bordertop", namespace = "urn:schemas-microsoft-com:office:word", type = JAXBElement.class),
        @XmlElementRef(name = "lock", namespace = "urn:schemas-microsoft-com:office:office", type = JAXBElement.class),
        @XmlElementRef(name = "textdata", namespace = "urn:schemas-microsoft-com:office:powerpoint", type = JAXBElement.class),
        @XmlElementRef(name = "callout", namespace = "urn:schemas-microsoft-com:office:office", type = JAXBElement.class)
    })
    protected List<JAXBElement<?>> pathOrFormulasOrHandles  = new ArrayListVml<JAXBElement<?>>(this);
    @XmlAttribute(name = "gfxdata", namespace = "urn:schemas-microsoft-com:office:office")
    protected byte[] gfxdata;
    @XmlAttribute(name = "equationxml")
    protected String equationxml;
    @XmlAttribute(name = "type")
    protected String type;
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
    @XmlAttribute(name = "adj")
    protected String adj;
    @XmlAttribute(name = "path")
    protected String path;
    @XmlAttribute(name = "opacity")
    protected String opacity;
    @XmlAttribute(name = "stroked")
    protected org.docx4j.vml.STTrueFalse stroked;
    @XmlAttribute(name = "strokecolor")
    protected String strokecolor;
    @XmlAttribute(name = "strokeweight")
    protected String strokeweight;
    @XmlAttribute(name = "insetpen")
    protected org.docx4j.vml.STTrueFalse insetpen;
    @XmlAttribute(name = "filled")
    protected org.docx4j.vml.STTrueFalse filled;
    @XmlAttribute(name = "fillcolor")
    protected String fillcolor;
    @XmlAttribute(name = "chromakey")
    protected String chromakey;
    @XmlAttribute(name = "spt", namespace = "urn:schemas-microsoft-com:office:office")
    protected Float spt;
    @XmlAttribute(name = "connectortype", namespace = "urn:schemas-microsoft-com:office:office")
    protected STConnectorType connectortype;
    @XmlAttribute(name = "bwmode", namespace = "urn:schemas-microsoft-com:office:office")
    protected STBWMode bwmode;
    @XmlAttribute(name = "bwpure", namespace = "urn:schemas-microsoft-com:office:office")
    protected STBWMode bwpure;
    @XmlAttribute(name = "bwnormal", namespace = "urn:schemas-microsoft-com:office:office")
    protected STBWMode bwnormal;
    @XmlAttribute(name = "forcedash", namespace = "urn:schemas-microsoft-com:office:office")
    protected org.docx4j.vml.officedrawing.STTrueFalse forcedash;
    @XmlAttribute(name = "oleicon", namespace = "urn:schemas-microsoft-com:office:office")
    protected org.docx4j.vml.officedrawing.STTrueFalse oleicon;
    @XmlAttribute(name = "ole", namespace = "urn:schemas-microsoft-com:office:office")
    protected String ole;
    @XmlAttribute(name = "preferrelative", namespace = "urn:schemas-microsoft-com:office:office")
    protected org.docx4j.vml.officedrawing.STTrueFalse preferrelative;
    @XmlAttribute(name = "cliptowrap", namespace = "urn:schemas-microsoft-com:office:office")
    protected org.docx4j.vml.officedrawing.STTrueFalse cliptowrap;
    @XmlAttribute(name = "clip", namespace = "urn:schemas-microsoft-com:office:office")
    protected org.docx4j.vml.officedrawing.STTrueFalse clip;
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
     * {@link JAXBElement }{@code <}{@link CTClipPath }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBorder }{@code >}
     * {@link JAXBElement }{@code <}{@link CTTextPath }{@code >}
     * {@link JAXBElement }{@code <}{@link CTShadow }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSignatureLine }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBorder }{@code >}
     * {@link JAXBElement }{@code <}{@link CTStroke }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPath }{@code >}
     * {@link JAXBElement }{@code <}{@link CTEmpty }{@code >}
     * {@link JAXBElement }{@code <}{@link CTWrap }{@code >}
     * {@link JAXBElement }{@code <}{@link CTInk }{@code >}
     * {@link JAXBElement }{@code <}{@link CTImageData }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSkew }{@code >}
     * {@link JAXBElement }{@code <}{@link CTHandles }{@code >}
     * {@link JAXBElement }{@code <}{@link CTAnchorLock }{@code >}
     * {@link JAXBElement }{@code <}{@link CTFill }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBorder }{@code >}
     * {@link JAXBElement }{@code <}{@link CTExtrusion }{@code >}
     * {@link JAXBElement }{@code <}{@link CTTextbox }{@code >}
     * {@link JAXBElement }{@code <}{@link CTFormulas }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBorder }{@code >}
     * {@link JAXBElement }{@code <}{@link CTLock }{@code >}
     * {@link JAXBElement }{@code <}{@link CTRel }{@code >}
     * {@link JAXBElement }{@code <}{@link CTCallout }{@code >}
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
     * Where this shape is specified in a mc fallback,
     * gfxdata basically contains the same wps:wsp
     * content as is found in mc:Choice/@Requires="wps".
     * That is, a consumer which isn't formally capable
     * of undertanding wps can still use this hint, if
     * it can understand a bit.
     * Encoded Package
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getGfxdata() {
        return gfxdata;
    }

    /**
     * Sets the value of the gfxdata property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setGfxdata(byte[] value) {
        this.gfxdata = ((byte[]) value);
    }

    /**
     * Gets the value of the equationxml property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEquationxml() {
        return equationxml;
    }

    /**
     * Sets the value of the equationxml property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEquationxml(String value) {
        this.equationxml = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
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
     * Gets the value of the adj property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdj() {
        return adj;
    }

    /**
     * Sets the value of the adj property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdj(String value) {
        this.adj = value;
    }

    /**
     * Gets the value of the path property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the value of the path property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPath(String value) {
        this.path = value;
    }

    /**
     * Gets the value of the opacity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOpacity() {
        return opacity;
    }

    /**
     * Sets the value of the opacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOpacity(String value) {
        this.opacity = value;
    }

    /**
     * Gets the value of the stroked property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getStroked() {
        return stroked;
    }

    /**
     * Sets the value of the stroked property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setStroked(org.docx4j.vml.STTrueFalse value) {
        this.stroked = value;
    }

    /**
     * Gets the value of the strokecolor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrokecolor() {
        return strokecolor;
    }

    /**
     * Sets the value of the strokecolor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrokecolor(String value) {
        this.strokecolor = value;
    }

    /**
     * Gets the value of the strokeweight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrokeweight() {
        return strokeweight;
    }

    /**
     * Sets the value of the strokeweight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrokeweight(String value) {
        this.strokeweight = value;
    }

    /**
     * Gets the value of the insetpen property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getInsetpen() {
        return insetpen;
    }

    /**
     * Sets the value of the insetpen property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setInsetpen(org.docx4j.vml.STTrueFalse value) {
        this.insetpen = value;
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
     * Gets the value of the chromakey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChromakey() {
        return chromakey;
    }

    /**
     * Sets the value of the chromakey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChromakey(String value) {
        this.chromakey = value;
    }

    /**
     * Optional Number
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getSpt() {
        return spt;
    }

    /**
     * Sets the value of the spt property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setSpt(Float value) {
        this.spt = value;
    }

    /**
     * Shape Connector Type
     * 
     * @return
     *     possible object is
     *     {@link STConnectorType }
     *     
     */
    public STConnectorType getConnectortype() {
        if (connectortype == null) {
            return STConnectorType.STRAIGHT;
        } else {
            return connectortype;
        }
    }

    /**
     * Sets the value of the connectortype property.
     * 
     * @param value
     *     allowed object is
     *     {@link STConnectorType }
     *     
     */
    public void setConnectortype(STConnectorType value) {
        this.connectortype = value;
    }

    /**
     * Black-and-White Mode
     * 
     * @return
     *     possible object is
     *     {@link STBWMode }
     *     
     */
    public STBWMode getBwmode() {
        return bwmode;
    }

    /**
     * Sets the value of the bwmode property.
     * 
     * @param value
     *     allowed object is
     *     {@link STBWMode }
     *     
     */
    public void setBwmode(STBWMode value) {
        this.bwmode = value;
    }

    /**
     * Pure Black-and-White Mode
     * 
     * @return
     *     possible object is
     *     {@link STBWMode }
     *     
     */
    public STBWMode getBwpure() {
        return bwpure;
    }

    /**
     * Sets the value of the bwpure property.
     * 
     * @param value
     *     allowed object is
     *     {@link STBWMode }
     *     
     */
    public void setBwpure(STBWMode value) {
        this.bwpure = value;
    }

    /**
     * Normal Black-and-White Mode
     * 
     * @return
     *     possible object is
     *     {@link STBWMode }
     *     
     */
    public STBWMode getBwnormal() {
        return bwnormal;
    }

    /**
     * Sets the value of the bwnormal property.
     * 
     * @param value
     *     allowed object is
     *     {@link STBWMode }
     *     
     */
    public void setBwnormal(STBWMode value) {
        this.bwnormal = value;
    }

    /**
     * Force Dashed Outline
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getForcedash() {
        return forcedash;
    }

    /**
     * Sets the value of the forcedash property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setForcedash(org.docx4j.vml.officedrawing.STTrueFalse value) {
        this.forcedash = value;
    }

    /**
     * Embedded Object Icon Toggle
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getOleicon() {
        return oleicon;
    }

    /**
     * Sets the value of the oleicon property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setOleicon(org.docx4j.vml.officedrawing.STTrueFalse value) {
        this.oleicon = value;
    }

    /**
     * Embedded Object Toggle
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOle() {
        return ole;
    }

    /**
     * Sets the value of the ole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOle(String value) {
        this.ole = value;
    }

    /**
     * Relative Resize Toggle
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getPreferrelative() {
        return preferrelative;
    }

    /**
     * Sets the value of the preferrelative property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setPreferrelative(org.docx4j.vml.officedrawing.STTrueFalse value) {
        this.preferrelative = value;
    }

    /**
     * Clip to Wrapping Polygon
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getCliptowrap() {
        return cliptowrap;
    }

    /**
     * Sets the value of the cliptowrap property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setCliptowrap(org.docx4j.vml.officedrawing.STTrueFalse value) {
        this.cliptowrap = value;
    }

    /**
     * Clipping Toggle
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getClip() {
        return clip;
    }

    /**
     * Sets the value of the clip property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setClip(org.docx4j.vml.officedrawing.STTrueFalse value) {
        this.clip = value;
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
