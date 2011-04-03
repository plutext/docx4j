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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ImageData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ImageData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_Id"/>
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_ImageAttributes"/>
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_Chromakey"/>
 *       &lt;attribute name="embosscolor" type="{urn:schemas-microsoft-com:vml}ST_ColorType" />
 *       &lt;attribute name="recolortarget" type="{urn:schemas-microsoft-com:vml}ST_ColorType" />
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}href"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}althref"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}title"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}oleid"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}detectmouseclick"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}movie"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}relid"/>
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}id"/>
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}pict"/>
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}href"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ImageData")
public class CTImageData implements Child
{

    @XmlAttribute(name = "embosscolor")
    protected String embosscolor;
    @XmlAttribute(name = "recolortarget")
    protected String recolortarget;
    @XmlAttribute(name = "href", namespace = "urn:schemas-microsoft-com:office:office")
    protected String ohref;
    @XmlAttribute(name = "althref", namespace = "urn:schemas-microsoft-com:office:office")
    protected String althref;
    @XmlAttribute(name = "title", namespace = "urn:schemas-microsoft-com:office:office")
    protected String title;
    @XmlAttribute(name = "oleid", namespace = "urn:schemas-microsoft-com:office:office")
    protected Float oleid;
    @XmlAttribute(name = "detectmouseclick", namespace = "urn:schemas-microsoft-com:office:office")
    protected org.docx4j.vml.officedrawing.STTrueFalse detectmouseclick;
    @XmlAttribute(name = "movie", namespace = "urn:schemas-microsoft-com:office:office")
    protected Float movie;
    @XmlAttribute(name = "relid", namespace = "urn:schemas-microsoft-com:office:office")
    protected String relid;
    @XmlAttribute(name = "id", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String id;
    @XmlAttribute(name = "pict", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String pict;
    @XmlAttribute(name = "href", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String href;
    @XmlAttribute(name = "id")
    protected String vmlId;
    @XmlAttribute(name = "src")
    protected String src;
    @XmlAttribute(name = "cropleft")
    protected String cropleft;
    @XmlAttribute(name = "croptop")
    protected String croptop;
    @XmlAttribute(name = "cropright")
    protected String cropright;
    @XmlAttribute(name = "cropbottom")
    protected String cropbottom;
    @XmlAttribute(name = "gain")
    protected String gain;
    @XmlAttribute(name = "blacklevel")
    protected String blacklevel;
    @XmlAttribute(name = "gamma")
    protected String gamma;
    @XmlAttribute(name = "grayscale")
    protected org.docx4j.vml.STTrueFalse grayscale;
    @XmlAttribute(name = "bilevel")
    protected org.docx4j.vml.STTrueFalse bilevel;
    @XmlAttribute(name = "chromakey")
    protected String chromakey;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the embosscolor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmbosscolor() {
        return embosscolor;
    }

    /**
     * Sets the value of the embosscolor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmbosscolor(String value) {
        this.embosscolor = value;
    }

    /**
     * Gets the value of the recolortarget property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecolortarget() {
        return recolortarget;
    }

    /**
     * Sets the value of the recolortarget property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecolortarget(String value) {
        this.recolortarget = value;
    }

    /**
     * Original Image Reference
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOhref() {
        return ohref;
    }

    /**
     * Sets the value of the ohref property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOhref(String value) {
        this.ohref = value;
    }

    /**
     * Alternate Image Reference
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlthref() {
        return althref;
    }

    /**
     * Sets the value of the althref property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlthref(String value) {
        this.althref = value;
    }

    /**
     * Image Data Title
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
     * Image Embedded Object ID
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getOleid() {
        return oleid;
    }

    /**
     * Sets the value of the oleid property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setOleid(Float value) {
        this.oleid = value;
    }

    /**
     * Detect Mouse Click
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getDetectmouseclick() {
        return detectmouseclick;
    }

    /**
     * Sets the value of the detectmouseclick property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setDetectmouseclick(org.docx4j.vml.officedrawing.STTrueFalse value) {
        this.detectmouseclick = value;
    }

    /**
     * Movie Reference
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getMovie() {
        return movie;
    }

    /**
     * Sets the value of the movie property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setMovie(Float value) {
        this.movie = value;
    }

    /**
     * Relationship to Part
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelid() {
        return relid;
    }

    /**
     * Sets the value of the relid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelid(String value) {
        this.relid = value;
    }

    /**
     * Explicit Relationship to Image Data
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
     * Explicit Relationship to Alternate Image Data
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPict() {
        return pict;
    }

    /**
     * Sets the value of the pict property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPict(String value) {
        this.pict = value;
    }

    /**
     * Explicit Relationship to Hyperlink Target
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
     * Gets the value of the src property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSrc() {
        return src;
    }

    /**
     * Sets the value of the src property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSrc(String value) {
        this.src = value;
    }

    /**
     * Gets the value of the cropleft property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCropleft() {
        return cropleft;
    }

    /**
     * Sets the value of the cropleft property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCropleft(String value) {
        this.cropleft = value;
    }

    /**
     * Gets the value of the croptop property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCroptop() {
        return croptop;
    }

    /**
     * Sets the value of the croptop property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCroptop(String value) {
        this.croptop = value;
    }

    /**
     * Gets the value of the cropright property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCropright() {
        return cropright;
    }

    /**
     * Sets the value of the cropright property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCropright(String value) {
        this.cropright = value;
    }

    /**
     * Gets the value of the cropbottom property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCropbottom() {
        return cropbottom;
    }

    /**
     * Sets the value of the cropbottom property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCropbottom(String value) {
        this.cropbottom = value;
    }

    /**
     * Gets the value of the gain property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGain() {
        return gain;
    }

    /**
     * Sets the value of the gain property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGain(String value) {
        this.gain = value;
    }

    /**
     * Gets the value of the blacklevel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBlacklevel() {
        return blacklevel;
    }

    /**
     * Sets the value of the blacklevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBlacklevel(String value) {
        this.blacklevel = value;
    }

    /**
     * Gets the value of the gamma property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGamma() {
        return gamma;
    }

    /**
     * Sets the value of the gamma property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGamma(String value) {
        this.gamma = value;
    }

    /**
     * Gets the value of the grayscale property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getGrayscale() {
        return grayscale;
    }

    /**
     * Sets the value of the grayscale property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setGrayscale(org.docx4j.vml.STTrueFalse value) {
        this.grayscale = value;
    }

    /**
     * Gets the value of the bilevel property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getBilevel() {
        return bilevel;
    }

    /**
     * Sets the value of the bilevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setBilevel(org.docx4j.vml.STTrueFalse value) {
        this.bilevel = value;
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
