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

import java.math.BigDecimal;
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
 *       &lt;sequence>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="src" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="cropleft" type="{urn:schemas-microsoft-com:vml}ST_Float" />
 *       &lt;attribute name="croptop" type="{urn:schemas-microsoft-com:vml}ST_Float" />
 *       &lt;attribute name="cropright" type="{urn:schemas-microsoft-com:vml}ST_Float" />
 *       &lt;attribute name="cropbottom" type="{urn:schemas-microsoft-com:vml}ST_Float" />
 *       &lt;attribute name="gain" type="{urn:schemas-microsoft-com:vml}ST_Float" />
 *       &lt;attribute name="blacklevel" type="{urn:schemas-microsoft-com:vml}ST_Float" />
 *       &lt;attribute name="gamma" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *       &lt;attribute name="chromakey" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="grayscale" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="bilevel" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="embosscolor" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}href"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}althref"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}title"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}oleid"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}detectmouseclick"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}movie"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ImageData", propOrder = {
    "any"
})
public class CTImageData
    implements Child
{

    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAttribute
    protected String id;
    @XmlAttribute
    protected String src;
    @XmlAttribute
    protected String cropleft;
    @XmlAttribute
    protected String croptop;
    @XmlAttribute
    protected String cropright;
    @XmlAttribute
    protected String cropbottom;
    @XmlAttribute
    protected String gain;
    @XmlAttribute
    protected String blacklevel;
    @XmlAttribute
    protected BigDecimal gamma;
    @XmlAttribute
    protected String chromakey;
    @XmlAttribute
    protected String grayscale;
    @XmlAttribute
    protected String bilevel;
    @XmlAttribute
    protected String embosscolor;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String href;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String althref;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String title;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected Float oleid;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected String detectmouseclick;
    @XmlAttribute(namespace = "urn:schemas-microsoft-com:office:office")
    protected Float movie;
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
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getGamma() {
        return gamma;
    }

    /**
     * Sets the value of the gamma property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setGamma(BigDecimal value) {
        this.gamma = value;
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
     * Gets the value of the grayscale property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGrayscale() {
        return grayscale;
    }

    /**
     * Sets the value of the grayscale property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGrayscale(String value) {
        this.grayscale = value;
    }

    /**
     * Gets the value of the bilevel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBilevel() {
        return bilevel;
    }

    /**
     * Sets the value of the bilevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBilevel(String value) {
        this.bilevel = value;
    }

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
     * Gets the value of the althref property.
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
     * Gets the value of the oleid property.
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
     * Gets the value of the detectmouseclick property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDetectmouseclick() {
        return detectmouseclick;
    }

    /**
     * Sets the value of the detectmouseclick property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDetectmouseclick(String value) {
        this.detectmouseclick = value;
    }

    /**
     * Gets the value of the movie property.
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
