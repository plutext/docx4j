/*
 *  Copyright 2010, Plutext Pty Ltd.
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


package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_Comments complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Comments">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="authors" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Authors"/>
 *         &lt;element name="commentList" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CommentList"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Comments", propOrder = {
    "authors",
    "commentList",
    "extLst"
})
@XmlRootElement(name="comments")
public class CTComments {

    @XmlElement(required = true)
    protected CTAuthors authors;
    @XmlElement(required = true)
    protected CTCommentList commentList;
    protected CTExtensionList extLst;

    /**
     * Gets the value of the authors property.
     * 
     * @return
     *     possible object is
     *     {@link CTAuthors }
     *     
     */
    public CTAuthors getAuthors() {
        return authors;
    }

    /**
     * Sets the value of the authors property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAuthors }
     *     
     */
    public void setAuthors(CTAuthors value) {
        this.authors = value;
    }

    /**
     * Gets the value of the commentList property.
     * 
     * @return
     *     possible object is
     *     {@link CTCommentList }
     *     
     */
    public CTCommentList getCommentList() {
        return commentList;
    }

    /**
     * Sets the value of the commentList property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCommentList }
     *     
     */
    public void setCommentList(CTCommentList value) {
        this.commentList = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtensionList }
     *     
     */
    public CTExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtensionList }
     *     
     */
    public void setExtLst(CTExtensionList value) {
        this.extLst = value;
    }

}
