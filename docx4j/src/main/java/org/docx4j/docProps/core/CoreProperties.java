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

package org.docx4j.docProps.core;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import org.docx4j.docProps.core.dc.elements.SimpleLiteral;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="category" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contentStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contentType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element ref="{http://purl.org/dc/terms/}created" minOccurs="0"/>
 *         &lt;element ref="{http://purl.org/dc/elements/1.1/}creator" minOccurs="0"/>
 *         &lt;element ref="{http://purl.org/dc/elements/1.1/}description" minOccurs="0"/>
 *         &lt;element ref="{http://purl.org/dc/elements/1.1/}identifier" minOccurs="0"/>
 *         &lt;element name="keywords" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element ref="{http://purl.org/dc/elements/1.1/}language" minOccurs="0"/>
 *         &lt;element name="lastModifiedBy" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lastPrinted" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element ref="{http://purl.org/dc/terms/}modified" minOccurs="0"/>
 *         &lt;element name="revision" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element ref="{http://purl.org/dc/elements/1.1/}subject" minOccurs="0"/>
 *         &lt;element ref="{http://purl.org/dc/elements/1.1/}title" minOccurs="0"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {

})
@XmlRootElement(name = "coreProperties")
public class CoreProperties {

    protected String category;
    protected String contentStatus;
    protected String contentType;
    @XmlElement(namespace = "http://purl.org/dc/terms/")
    protected SimpleLiteral created;
    @XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
    protected SimpleLiteral creator;
    @XmlElementRef(name = "description", namespace = "http://purl.org/dc/elements/1.1/", type = JAXBElement.class)
    protected JAXBElement<SimpleLiteral> description;
    @XmlElementRef(name = "identifier", namespace = "http://purl.org/dc/elements/1.1/", type = JAXBElement.class)
    protected JAXBElement<SimpleLiteral> identifier;
    protected String keywords;
    @XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
    protected SimpleLiteral language;
    protected String lastModifiedBy;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastPrinted;
    @XmlElement(namespace = "http://purl.org/dc/terms/")
    protected SimpleLiteral modified;
    protected String revision;
    @XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
    protected SimpleLiteral subject;
    @XmlElementRef(name = "title", namespace = "http://purl.org/dc/elements/1.1/", type = JAXBElement.class)
    protected JAXBElement<SimpleLiteral> title;
    protected String version;

    /**
     * Gets the value of the category property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the value of the category property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCategory(String value) {
        this.category = value;
    }

    /**
     * Gets the value of the contentStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentStatus() {
        return contentStatus;
    }

    /**
     * Sets the value of the contentStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentStatus(String value) {
        this.contentStatus = value;
    }

    /**
     * Gets the value of the contentType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the value of the contentType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentType(String value) {
        this.contentType = value;
    }

    /**
     * Gets the value of the created property.
     * 
     * @return
     *     possible object is
     *     {@link SimpleLiteral }
     *     
     */
    public SimpleLiteral getCreated() {
        return created;
    }

    /**
     * Sets the value of the created property.
     * 
     * @param value
     *     allowed object is
     *     {@link SimpleLiteral }
     *     
     */
    public void setCreated(SimpleLiteral value) {
        this.created = value;
    }

    /**
     * Gets the value of the creator property.
     * 
     * @return
     *     possible object is
     *     {@link SimpleLiteral }
     *     
     */
    public SimpleLiteral getCreator() {
        return creator;
    }

    /**
     * Sets the value of the creator property.
     * 
     * @param value
     *     allowed object is
     *     {@link SimpleLiteral }
     *     
     */
    public void setCreator(SimpleLiteral value) {
        this.creator = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     *     {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     *     {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     *     
     */
    public JAXBElement<SimpleLiteral> getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     *     {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     *     {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     *     
     */
    public void setDescription(JAXBElement<SimpleLiteral> value) {
        this.description = ((JAXBElement<SimpleLiteral> ) value);
    }

    /**
     * Gets the value of the identifier property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     *     {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     *     
     */
    public JAXBElement<SimpleLiteral> getIdentifier() {
        return identifier;
    }

    /**
     * Sets the value of the identifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     *     {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     *     
     */
    public void setIdentifier(JAXBElement<SimpleLiteral> value) {
        this.identifier = ((JAXBElement<SimpleLiteral> ) value);
    }

    /**
     * Gets the value of the keywords property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * Sets the value of the keywords property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeywords(String value) {
        this.keywords = value;
    }

    /**
     * Gets the value of the language property.
     * 
     * @return
     *     possible object is
     *     {@link SimpleLiteral }
     *     
     */
    public SimpleLiteral getLanguage() {
        return language;
    }

    /**
     * Sets the value of the language property.
     * 
     * @param value
     *     allowed object is
     *     {@link SimpleLiteral }
     *     
     */
    public void setLanguage(SimpleLiteral value) {
        this.language = value;
    }

    /**
     * Gets the value of the lastModifiedBy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    /**
     * Sets the value of the lastModifiedBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastModifiedBy(String value) {
        this.lastModifiedBy = value;
    }

    /**
     * Gets the value of the lastPrinted property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastPrinted() {
        return lastPrinted;
    }

    /**
     * Sets the value of the lastPrinted property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastPrinted(XMLGregorianCalendar value) {
        this.lastPrinted = value;
    }

    /**
     * Gets the value of the modified property.
     * 
     * @return
     *     possible object is
     *     {@link SimpleLiteral }
     *     
     */
    public SimpleLiteral getModified() {
        return modified;
    }

    /**
     * Sets the value of the modified property.
     * 
     * @param value
     *     allowed object is
     *     {@link SimpleLiteral }
     *     
     */
    public void setModified(SimpleLiteral value) {
        this.modified = value;
    }

    /**
     * Gets the value of the revision property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRevision() {
        return revision;
    }

    /**
     * Sets the value of the revision property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRevision(String value) {
        this.revision = value;
    }

    /**
     * Gets the value of the subject property.
     * 
     * @return
     *     possible object is
     *     {@link SimpleLiteral }
     *     
     */
    public SimpleLiteral getSubject() {
        return subject;
    }

    /**
     * Sets the value of the subject property.
     * 
     * @param value
     *     allowed object is
     *     {@link SimpleLiteral }
     *     
     */
    public void setSubject(SimpleLiteral value) {
        this.subject = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     *     {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     *     
     */
    public JAXBElement<SimpleLiteral> getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     *     {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     *     
     */
    public void setTitle(JAXBElement<SimpleLiteral> value) {
        this.title = ((JAXBElement<SimpleLiteral> ) value);
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

}
