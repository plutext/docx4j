/*
 *  Copyright 2010-2012, Plutext Pty Ltd.
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
package org.pptx4j.pml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_OleObject complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_OleObject"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="embed" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_OleObjectEmbed"/&gt;
 *         &lt;element name="link" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_OleObjectLink"/&gt;
 *         &lt;element name="pic" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_Picture"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attGroup ref="{http://schemas.openxmlformats.org/presentationml/2006/main}AG_Ole"/&gt;
 *       &lt;attribute name="progId" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_OleObject", propOrder = {
    "embed",
    "link",
    "pic"
})
public class CTOleObject implements Child
{

    protected CTOleObjectEmbed embed;
    protected CTOleObjectLink link;
    protected Pic pic;
    @XmlAttribute(name = "progId")
    protected String progId;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "showAsIcon")
    protected Boolean showAsIcon;
    @XmlAttribute(name = "id", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String id;
    @XmlAttribute(name = "imgW")
    protected Integer imgW;
    @XmlAttribute(name = "imgH")
    protected Integer imgH;
    @XmlTransient
    private Object parent;

    // @since 3.0.1
    // TODO investigate why missing, and add to xsd if nec
    @XmlAttribute(name = "spid")
    protected String spid;
    public String getSpid() {
        return spid;
    }
    public void setSpid(String value) {
        this.spid = value;
    }    
    
    /**
     * Gets the value of the embed property.
     * 
     * @return
     *     possible object is
     *     {@link CTOleObjectEmbed }
     *     
     */
    public CTOleObjectEmbed getEmbed() {
        return embed;
    }

    /**
     * Sets the value of the embed property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOleObjectEmbed }
     *     
     */
    public void setEmbed(CTOleObjectEmbed value) {
        this.embed = value;
    }

    /**
     * Gets the value of the link property.
     * 
     * @return
     *     possible object is
     *     {@link CTOleObjectLink }
     *     
     */
    public CTOleObjectLink getLink() {
        return link;
    }

    /**
     * Sets the value of the link property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOleObjectLink }
     *     
     */
    public void setLink(CTOleObjectLink value) {
        this.link = value;
    }

    /**
     * Gets the value of the pic property.
     * 
     * @return
     *     possible object is
     *     {@link Pic }
     *     
     */
    public Pic getPic() {
        return pic;
    }

    /**
     * Sets the value of the pic property.
     * 
     * @param value
     *     allowed object is
     *     {@link Pic }
     *     
     */
    public void setPic(Pic value) {
        this.pic = value;
    }

    /**
     * Gets the value of the progId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProgId() {
        return progId;
    }

    /**
     * Sets the value of the progId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProgId(String value) {
        this.progId = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        if (name == null) {
            return "";
        } else {
            return name;
        }
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the showAsIcon property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowAsIcon() {
        if (showAsIcon == null) {
            return false;
        } else {
            return showAsIcon;
        }
    }

    /**
     * Sets the value of the showAsIcon property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowAsIcon(Boolean value) {
        this.showAsIcon = value;
    }

    /**
     * Relationship ID
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
     * Gets the value of the imgW property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getImgW() {
        return imgW;
    }

    /**
     * Sets the value of the imgW property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setImgW(Integer value) {
        this.imgW = value;
    }

    /**
     * Gets the value of the imgH property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getImgH() {
        return imgH;
    }

    /**
     * Sets the value of the imgH property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setImgH(Integer value) {
        this.imgH = value;
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
