/*
 *  Copyright 2010-2013, Plutext Pty Ltd.
 *   
 *  This file is part of xlsx4j, a component of docx4j.

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
package org.xlsx4j.sml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Comment complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Comment">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="text" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Rst"/>
 *         &lt;element name="commentPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CommentPr" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ref" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Ref" />
 *       &lt;attribute name="authorId" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="guid" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Guid" />
 *       &lt;attribute name="shapeId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Comment", propOrder = {
    "text",
    "commentPr"
})
public class CTComment implements Child
{

    @XmlElement(required = true)
    protected CTRst text;
    protected CTCommentPr commentPr;
    @XmlAttribute(name = "ref", required = true)
    protected String ref;
    @XmlAttribute(name = "authorId", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long authorId;
    @XmlAttribute(name = "guid")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String guid;
    @XmlAttribute(name = "shapeId")
    @XmlSchemaType(name = "unsignedInt")
    protected Long shapeId;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the text property.
     * 
     * @return
     *     possible object is
     *     {@link CTRst }
     *     
     */
    public CTRst getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRst }
     *     
     */
    public void setText(CTRst value) {
        this.text = value;
    }

    /**
     * Gets the value of the commentPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTCommentPr }
     *     
     */
    public CTCommentPr getCommentPr() {
        return commentPr;
    }

    /**
     * Sets the value of the commentPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCommentPr }
     *     
     */
    public void setCommentPr(CTCommentPr value) {
        this.commentPr = value;
    }

    /**
     * Gets the value of the ref property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRef() {
        return ref;
    }

    /**
     * Sets the value of the ref property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRef(String value) {
        this.ref = value;
    }

    /**
     * Gets the value of the authorId property.
     * 
     */
    public long getAuthorId() {
        return authorId;
    }

    /**
     * Sets the value of the authorId property.
     * 
     */
    public void setAuthorId(long value) {
        this.authorId = value;
    }

    /**
     * Gets the value of the guid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGuid() {
        return guid;
    }

    /**
     * Sets the value of the guid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGuid(String value) {
        this.guid = value;
    }

    /**
     * Gets the value of the shapeId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getShapeId() {
        return shapeId;
    }

    /**
     * Sets the value of the shapeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setShapeId(Long value) {
        this.shapeId = value;
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
