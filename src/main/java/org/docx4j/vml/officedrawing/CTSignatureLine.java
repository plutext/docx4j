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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.docx4j.vml.STExt;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_SignatureLine complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SignatureLine">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_Ext"/>
 *       &lt;attribute name="issignatureline" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" />
 *       &lt;attribute name="id" type="{urn:schemas-microsoft-com:office:office}ST_Guid" />
 *       &lt;attribute name="provid" type="{urn:schemas-microsoft-com:office:office}ST_Guid" />
 *       &lt;attribute name="signinginstructionsset" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" />
 *       &lt;attribute name="allowcomments" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" />
 *       &lt;attribute name="showsigndate" type="{urn:schemas-microsoft-com:office:office}ST_TrueFalse" />
 *       &lt;attribute name="suggestedsigner" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="suggestedsigner2" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="suggestedsigneremail" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="signinginstructions" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="addlxml" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="sigprovurl" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SignatureLine")
public class CTSignatureLine implements Child
{

    @XmlAttribute(name = "issignatureline")
    protected STTrueFalse issignatureline;
    
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String id;
    
    @XmlAttribute(name = "provid")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String provid;
    
    @XmlAttribute(name = "signinginstructionsset")
    protected STTrueFalse signinginstructionsset;
    
    @XmlAttribute(name = "allowcomments")
    protected STTrueFalse allowcomments;
    
    @XmlAttribute(name = "showsigndate")
    protected STTrueFalse showsigndate;
    
    @XmlAttribute(name = "suggestedsigner", namespace = "urn:schemas-microsoft-com:office:office")
    protected String suggestedsigner;
    
    @XmlAttribute(name = "suggestedsigner2", namespace = "urn:schemas-microsoft-com:office:office")
    protected String suggestedsigner2;
    
    @XmlAttribute(name = "suggestedsigneremail", namespace = "urn:schemas-microsoft-com:office:office")
    protected String suggestedsigneremail;
    
    // v3.2.0: namespace added manually to the three below; see https://github.com/plutext/docx4j/issues/121 
    // (compare xsd/vml/vml-officedrawing.xsd)
    
    @XmlAttribute(name = "signinginstructions", namespace = "urn:schemas-microsoft-com:office:office")
    protected String signinginstructions;
    
    @XmlAttribute(name = "addlxml", namespace = "urn:schemas-microsoft-com:office:office")
    protected String addlxml;
    
    @XmlAttribute(name = "sigprovurl", namespace = "urn:schemas-microsoft-com:office:office")
    protected String sigprovurl;
    
    // -- end manually added namespaces
    
    @XmlAttribute(name = "ext", namespace = "urn:schemas-microsoft-com:vml")
    protected STExt ext;
    
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the issignatureline property.
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getIssignatureline() {
        return issignatureline;
    }

    /**
     * Sets the value of the issignatureline property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setIssignatureline(STTrueFalse value) {
        this.issignatureline = value;
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
     * Gets the value of the provid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProvid() {
        return provid;
    }

    /**
     * Sets the value of the provid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProvid(String value) {
        this.provid = value;
    }

    /**
     * Gets the value of the signinginstructionsset property.
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getSigninginstructionsset() {
        return signinginstructionsset;
    }

    /**
     * Sets the value of the signinginstructionsset property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setSigninginstructionsset(STTrueFalse value) {
        this.signinginstructionsset = value;
    }

    /**
     * Gets the value of the allowcomments property.
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getAllowcomments() {
        return allowcomments;
    }

    /**
     * Sets the value of the allowcomments property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setAllowcomments(STTrueFalse value) {
        this.allowcomments = value;
    }

    /**
     * Gets the value of the showsigndate property.
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getShowsigndate() {
        return showsigndate;
    }

    /**
     * Sets the value of the showsigndate property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setShowsigndate(STTrueFalse value) {
        this.showsigndate = value;
    }

    /**
     * Gets the value of the suggestedsigner property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSuggestedsigner() {
        return suggestedsigner;
    }

    /**
     * Sets the value of the suggestedsigner property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSuggestedsigner(String value) {
        this.suggestedsigner = value;
    }

    /**
     * Gets the value of the suggestedsigner2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSuggestedsigner2() {
        return suggestedsigner2;
    }

    /**
     * Sets the value of the suggestedsigner2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSuggestedsigner2(String value) {
        this.suggestedsigner2 = value;
    }

    /**
     * Gets the value of the suggestedsigneremail property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSuggestedsigneremail() {
        return suggestedsigneremail;
    }

    /**
     * Sets the value of the suggestedsigneremail property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSuggestedsigneremail(String value) {
        this.suggestedsigneremail = value;
    }

    /**
     * Gets the value of the signinginstructions property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSigninginstructions() {
        return signinginstructions;
    }

    /**
     * Sets the value of the signinginstructions property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSigninginstructions(String value) {
        this.signinginstructions = value;
    }

    /**
     * Gets the value of the addlxml property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddlxml() {
        return addlxml;
    }

    /**
     * Sets the value of the addlxml property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddlxml(String value) {
        this.addlxml = value;
    }

    /**
     * Gets the value of the sigprovurl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSigprovurl() {
        return sigprovurl;
    }

    /**
     * Sets the value of the sigprovurl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSigprovurl(String value) {
        this.sigprovurl = value;
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
