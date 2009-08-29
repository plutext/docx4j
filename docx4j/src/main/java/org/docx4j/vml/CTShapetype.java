/*
 *  Copyright 2007-2009, Plutext Pty Ltd.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.docx4j.vml.officedrawing.STHrAlign;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Shapetype complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Shapetype">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="adj" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="path" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="href" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="target" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="class" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="title" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="alt" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="onmouseover" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="print" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="coordsize" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="coordorigin" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="filled" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="stroked" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="fillcolor" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="strokecolor" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="style" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}wrapcoords"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}preferrelative"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}spt"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}oned"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}regroupid"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}doubleclicknotify"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}button"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}userhidden"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}bullet"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}hr"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}hrstd"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}hrnoshade"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}hrheight"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}hrwidth"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}hrpct"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}hralign"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}relativeposition"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}allowincell"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}allowoverlap"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}userdrawn"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}tableproperties"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}tablelimits"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}bordertopcolor"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}borderleftcolor"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}borderbottomcolor"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}borderrightcolor"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}master"/>
 *       &lt;attribute name="strokeweight" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Shapetype", propOrder = {
    "any"
})
public class CTShapetype
    implements Child
{

    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAttribute(required = true)
    protected String id;
    @XmlAttribute
    protected String adj;
    @XmlAttribute
    protected String path;
    @XmlAttribute
    protected String href;
    @XmlAttribute
    protected String target;
    @XmlAttribute(name = "class")
    protected String clazz;
    @XmlAttribute
    protected String title;
    @XmlAttribute
    protected String alt;
    @XmlAttribute
    protected String onmouseover;
    @XmlAttribute
    protected String print;
    @XmlAttribute
    protected String coordsize;
    @XmlAttribute
    protected String coordorigin;
    @XmlAttribute
    protected String filled;
    @XmlAttribute
    protected String stroked;
    @XmlAttribute
    protected String fillcolor;
    @XmlAttribute
    protected String strokecolor;
    @XmlAttribute
    protected String style;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String wrapcoords;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String preferrelative;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected Float spt;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String oned;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected BigInteger regroupid;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String doubleclicknotify;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String button;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String userhidden;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String bullet;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String hr;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String hrstd;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String hrnoshade;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected Float hrheight;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected Float hrwidth;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected Float hrpct;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected STHrAlign hralign;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String relativeposition;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String allowincell;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String allowoverlap;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String userdrawn;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected Integer tableproperties;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String tablelimits;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String bordertopcolor;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String borderleftcolor;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String borderbottomcolor;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String borderrightcolor;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String master;
    @XmlAttribute
    protected String strokeweight;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
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
     * Gets the value of the onmouseover property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOnmouseover() {
        return onmouseover;
    }

    /**
     * Sets the value of the onmouseover property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOnmouseover(String value) {
        this.onmouseover = value;
    }

    /**
     * Gets the value of the print property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrint() {
        return print;
    }

    /**
     * Sets the value of the print property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrint(String value) {
        this.print = value;
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
     * Gets the value of the filled property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFilled() {
        return filled;
    }

    /**
     * Sets the value of the filled property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFilled(String value) {
        this.filled = value;
    }

    /**
     * Gets the value of the stroked property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStroked() {
        return stroked;
    }

    /**
     * Sets the value of the stroked property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStroked(String value) {
        this.stroked = value;
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
     * Gets the value of the preferrelative property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreferrelative() {
        return preferrelative;
    }

    /**
     * Sets the value of the preferrelative property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreferrelative(String value) {
        this.preferrelative = value;
    }

    /**
     * Gets the value of the spt property.
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
     * Gets the value of the oned property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOned() {
        return oned;
    }

    /**
     * Sets the value of the oned property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOned(String value) {
        this.oned = value;
    }

    /**
     * Gets the value of the regroupid property.
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
     * Gets the value of the doubleclicknotify property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDoubleclicknotify() {
        return doubleclicknotify;
    }

    /**
     * Sets the value of the doubleclicknotify property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDoubleclicknotify(String value) {
        this.doubleclicknotify = value;
    }

    /**
     * Gets the value of the button property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getButton() {
        return button;
    }

    /**
     * Sets the value of the button property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setButton(String value) {
        this.button = value;
    }

    /**
     * Gets the value of the userhidden property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserhidden() {
        return userhidden;
    }

    /**
     * Sets the value of the userhidden property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserhidden(String value) {
        this.userhidden = value;
    }

    /**
     * Gets the value of the bullet property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBullet() {
        return bullet;
    }

    /**
     * Sets the value of the bullet property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBullet(String value) {
        this.bullet = value;
    }

    /**
     * Gets the value of the hr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHr() {
        return hr;
    }

    /**
     * Sets the value of the hr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHr(String value) {
        this.hr = value;
    }

    /**
     * Gets the value of the hrstd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHrstd() {
        return hrstd;
    }

    /**
     * Sets the value of the hrstd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHrstd(String value) {
        this.hrstd = value;
    }

    /**
     * Gets the value of the hrnoshade property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHrnoshade() {
        return hrnoshade;
    }

    /**
     * Sets the value of the hrnoshade property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHrnoshade(String value) {
        this.hrnoshade = value;
    }

    /**
     * Gets the value of the hrheight property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getHrheight() {
        return hrheight;
    }

    /**
     * Sets the value of the hrheight property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setHrheight(Float value) {
        this.hrheight = value;
    }

    /**
     * Gets the value of the hrwidth property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getHrwidth() {
        return hrwidth;
    }

    /**
     * Sets the value of the hrwidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setHrwidth(Float value) {
        this.hrwidth = value;
    }

    /**
     * Gets the value of the hrpct property.
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
     * Gets the value of the hralign property.
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
     * Gets the value of the relativeposition property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelativeposition() {
        return relativeposition;
    }

    /**
     * Sets the value of the relativeposition property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelativeposition(String value) {
        this.relativeposition = value;
    }

    /**
     * Gets the value of the allowincell property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAllowincell() {
        return allowincell;
    }

    /**
     * Sets the value of the allowincell property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAllowincell(String value) {
        this.allowincell = value;
    }

    /**
     * Gets the value of the allowoverlap property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAllowoverlap() {
        return allowoverlap;
    }

    /**
     * Sets the value of the allowoverlap property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAllowoverlap(String value) {
        this.allowoverlap = value;
    }

    /**
     * Gets the value of the userdrawn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserdrawn() {
        return userdrawn;
    }

    /**
     * Sets the value of the userdrawn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserdrawn(String value) {
        this.userdrawn = value;
    }

    /**
     * Gets the value of the tableproperties property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTableproperties() {
        return tableproperties;
    }

    /**
     * Sets the value of the tableproperties property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTableproperties(Integer value) {
        this.tableproperties = value;
    }

    /**
     * Gets the value of the tablelimits property.
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
     * Gets the value of the bordertopcolor property.
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
     * Gets the value of the borderleftcolor property.
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
     * Gets the value of the borderbottomcolor property.
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
     * Gets the value of the borderrightcolor property.
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
     * Gets the value of the master property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaster() {
        return master;
    }

    /**
     * Sets the value of the master property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaster(String value) {
        this.master = value;
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
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
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
