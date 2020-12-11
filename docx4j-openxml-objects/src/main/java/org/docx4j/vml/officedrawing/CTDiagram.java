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


package org.docx4j.vml.officedrawing;

import java.math.BigInteger;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.vml.STExt;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Diagram complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Diagram">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="relationtable" type="{urn:schemas-microsoft-com:office:office}CT_RelationTable" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_Ext"/>
 *       &lt;attribute name="dgmstyle" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="autoformat" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" />
 *       &lt;attribute name="reverse" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" />
 *       &lt;attribute name="autolayout" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" />
 *       &lt;attribute name="dgmscalex" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="dgmscaley" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="dgmfontsize" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="constrainbounds" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="dgmbasetextscale" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Diagram", propOrder = {
    "relationtable"
})
public class CTDiagram implements Child
{

    protected CTRelationTable relationtable;
    @XmlAttribute(name = "dgmstyle")
    protected BigInteger dgmstyle;
    @XmlAttribute(name = "autoformat")
    protected STTrueFalse autoformat;
    @XmlAttribute(name = "reverse")
    protected STTrueFalse reverse;
    @XmlAttribute(name = "autolayout")
    protected STTrueFalse autolayout;
    @XmlAttribute(name = "dgmscalex")
    protected BigInteger dgmscalex;
    @XmlAttribute(name = "dgmscaley")
    protected BigInteger dgmscaley;
    @XmlAttribute(name = "dgmfontsize")
    protected BigInteger dgmfontsize;
    @XmlAttribute(name = "constrainbounds")
    protected String constrainbounds;
    @XmlAttribute(name = "dgmbasetextscale")
    protected BigInteger dgmbasetextscale;
    @XmlAttribute(name = "ext", namespace = "urn:schemas-microsoft-com:vml")
    protected STExt ext;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the relationtable property.
     * 
     * @return
     *     possible object is
     *     {@link CTRelationTable }
     *     
     */
    public CTRelationTable getRelationtable() {
        return relationtable;
    }

    /**
     * Sets the value of the relationtable property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRelationTable }
     *     
     */
    public void setRelationtable(CTRelationTable value) {
        this.relationtable = value;
    }

    /**
     * Gets the value of the dgmstyle property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDgmstyle() {
        return dgmstyle;
    }

    /**
     * Sets the value of the dgmstyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDgmstyle(BigInteger value) {
        this.dgmstyle = value;
    }

    /**
     * Gets the value of the autoformat property.
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getAutoformat() {
        return autoformat;
    }

    /**
     * Sets the value of the autoformat property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setAutoformat(STTrueFalse value) {
        this.autoformat = value;
    }

    /**
     * Gets the value of the reverse property.
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getReverse() {
        return reverse;
    }

    /**
     * Sets the value of the reverse property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setReverse(STTrueFalse value) {
        this.reverse = value;
    }

    /**
     * Gets the value of the autolayout property.
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getAutolayout() {
        return autolayout;
    }

    /**
     * Sets the value of the autolayout property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setAutolayout(STTrueFalse value) {
        this.autolayout = value;
    }

    /**
     * Gets the value of the dgmscalex property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDgmscalex() {
        return dgmscalex;
    }

    /**
     * Sets the value of the dgmscalex property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDgmscalex(BigInteger value) {
        this.dgmscalex = value;
    }

    /**
     * Gets the value of the dgmscaley property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDgmscaley() {
        return dgmscaley;
    }

    /**
     * Sets the value of the dgmscaley property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDgmscaley(BigInteger value) {
        this.dgmscaley = value;
    }

    /**
     * Gets the value of the dgmfontsize property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDgmfontsize() {
        return dgmfontsize;
    }

    /**
     * Sets the value of the dgmfontsize property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDgmfontsize(BigInteger value) {
        this.dgmfontsize = value;
    }

    /**
     * Gets the value of the constrainbounds property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConstrainbounds() {
        return constrainbounds;
    }

    /**
     * Sets the value of the constrainbounds property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConstrainbounds(String value) {
        this.constrainbounds = value;
    }

    /**
     * Gets the value of the dgmbasetextscale property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDgmbasetextscale() {
        return dgmbasetextscale;
    }

    /**
     * Sets the value of the dgmbasetextscale property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDgmbasetextscale(BigInteger value) {
        this.dgmbasetextscale = value;
    }

    /**
     * Gets the value of the ext property.
     * 
     * @return
     *     possible object is
     *     {@link STExt }
     *     
     */
    public STExt getExt() {
        return ext;
    }

    /**
     * Sets the value of the ext property.
     * 
     * @param value
     *     allowed object is
     *     {@link STExt }
     *     
     */
    public void setExt(STExt value) {
        this.ext = value;
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
