/*
 *  Copyright 2007-2013, Plutext Pty Ltd.
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


package org.docx4j.wml; 

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementRefs;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

import org.docx4j.math.CTOMath;
import org.docx4j.math.CTOMathPara;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="comment" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TrackChange">
 *                 &lt;sequence>
 *                   &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_BlockLevelElts" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="initials" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
 *               &lt;/extension>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "comment"
})
@XmlRootElement(name = "comments")
public class Comments implements Child
{

    protected List<Comments.Comment> comment;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the comment property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the comment property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getComment().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Comments.Comment }
     * 
     * 
     */
    public List<Comments.Comment> getComment() {
        if (comment == null) {
            comment = new ArrayList<Comments.Comment>();
        }
        return this.comment;
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TrackChange">
     *       &lt;sequence>
     *         &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_BlockLevelElts" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *       &lt;attribute name="initials" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "content"
    })
    public static class Comment
        extends CTTrackChange
    {

        @XmlElementRefs({
            @XmlElementRef(name = "bookmarkEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "customXmlInsRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "customXmlMoveToRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "customXmlMoveFromRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "ins", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = RunIns.class),
            @XmlElementRef(name = "p", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = P.class),
            @XmlElementRef(name = "permEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "moveFromRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "moveFrom", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "proofErr", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = ProofErr.class),
            @XmlElementRef(name = "customXmlDelRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "moveFromRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "commentRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = CommentRangeEnd.class),
            @XmlElementRef(name = "customXmlInsRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "customXmlDelRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "del", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = RunDel.class),
            @XmlElementRef(name = "moveToRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "sdt", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = SdtBlock.class),
            @XmlElementRef(name = "oMathPara", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
            @XmlElementRef(name = "moveToRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "permStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "commentRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = CommentRangeStart.class),
            @XmlElementRef(name = "bookmarkStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "oMath", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/math", type = JAXBElement.class),
            @XmlElementRef(name = "customXml", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "tbl", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "customXmlMoveFromRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "moveTo", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "customXmlMoveToRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
            @XmlElementRef(name = "altChunk", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class)
        })
        protected List<Object> content = new ArrayListWml<Object>(this);
        @XmlAttribute(name = "initials", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected String initials;

        /**
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
         * {@link JAXBElement }{@code <}{@link CTMarkupRange }{@code >}
         * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
         * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
         * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
         * {@link RunIns }
         * {@link P }
         * {@link JAXBElement }{@code <}{@link CTPerm }{@code >}
         * {@link JAXBElement }{@code <}{@link CTMoveBookmark }{@code >}
         * {@link JAXBElement }{@code <}{@link RunTrackChange }{@code >}
         * {@link ProofErr }
         * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
         * {@link JAXBElement }{@code <}{@link CTMoveFromRangeEnd }{@code >}
         * {@link CommentRangeEnd }
         * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
         * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
         * {@link RunDel }
         * {@link JAXBElement }{@code <}{@link CTMoveToRangeEnd }{@code >}
         * {@link SdtBlock }
         * {@link JAXBElement }{@code <}{@link CTOMathPara }{@code >}
         * {@link JAXBElement }{@code <}{@link CTMoveBookmark }{@code >}
         * {@link JAXBElement }{@code <}{@link RangePermissionStart }{@code >}
         * {@link CommentRangeStart }
         * {@link JAXBElement }{@code <}{@link CTBookmark }{@code >}
         * {@link JAXBElement }{@code <}{@link CTOMath }{@code >}
         * {@link JAXBElement }{@code <}{@link CTCustomXmlBlock }{@code >}
         * {@link JAXBElement }{@code <}{@link Tbl }{@code >}
         * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
         * {@link JAXBElement }{@code <}{@link RunTrackChange }{@code >}
         * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
         * {@link JAXBElement }{@code <}{@link CTAltChunk }{@code >}
         * 
         * 
         */
        public List<Object> getContent() {
            if (content == null) {
                content  = new ArrayListWml<Object>(this);
            }
            return this.content;
        }
        
        /**
         * @deprecated
         */
        public List<Object> getEGBlockLevelElts() {
        	return getContent();
        }

        /**
         * Gets the value of the initials property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getInitials() {
            return initials;
        }

        /**
         * Sets the value of the initials property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setInitials(String value) {
            this.initials = value;
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

}
