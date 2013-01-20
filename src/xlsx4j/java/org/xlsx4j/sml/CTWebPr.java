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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_WebPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_WebPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="tables" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Tables" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="xml" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="sourceData" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="parsePre" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="consecutive" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="firstRow" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="xl97" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="textDates" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="xl2000" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="url" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="post" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="htmlTables" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="htmlFormat" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_HtmlFmt" default="none" />
 *       &lt;attribute name="editPage" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_WebPr", propOrder = {
    "tables"
})
public class CTWebPr implements Child
{

    protected CTTables tables;
    @XmlAttribute(name = "xml")
    protected Boolean xml;
    @XmlAttribute(name = "sourceData")
    protected Boolean sourceData;
    @XmlAttribute(name = "parsePre")
    protected Boolean parsePre;
    @XmlAttribute(name = "consecutive")
    protected Boolean consecutive;
    @XmlAttribute(name = "firstRow")
    protected Boolean firstRow;
    @XmlAttribute(name = "xl97")
    protected Boolean xl97;
    @XmlAttribute(name = "textDates")
    protected Boolean textDates;
    @XmlAttribute(name = "xl2000")
    protected Boolean xl2000;
    @XmlAttribute(name = "url")
    protected String url;
    @XmlAttribute(name = "post")
    protected String post;
    @XmlAttribute(name = "htmlTables")
    protected Boolean htmlTables;
    @XmlAttribute(name = "htmlFormat")
    protected STHtmlFmt htmlFormat;
    @XmlAttribute(name = "editPage")
    protected String editPage;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the tables property.
     * 
     * @return
     *     possible object is
     *     {@link CTTables }
     *     
     */
    public CTTables getTables() {
        return tables;
    }

    /**
     * Sets the value of the tables property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTables }
     *     
     */
    public void setTables(CTTables value) {
        this.tables = value;
    }

    /**
     * Gets the value of the xml property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isXml() {
        if (xml == null) {
            return false;
        } else {
            return xml;
        }
    }

    /**
     * Sets the value of the xml property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setXml(Boolean value) {
        this.xml = value;
    }

    /**
     * Gets the value of the sourceData property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSourceData() {
        if (sourceData == null) {
            return false;
        } else {
            return sourceData;
        }
    }

    /**
     * Sets the value of the sourceData property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSourceData(Boolean value) {
        this.sourceData = value;
    }

    /**
     * Gets the value of the parsePre property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isParsePre() {
        if (parsePre == null) {
            return false;
        } else {
            return parsePre;
        }
    }

    /**
     * Sets the value of the parsePre property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setParsePre(Boolean value) {
        this.parsePre = value;
    }

    /**
     * Gets the value of the consecutive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isConsecutive() {
        if (consecutive == null) {
            return false;
        } else {
            return consecutive;
        }
    }

    /**
     * Sets the value of the consecutive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setConsecutive(Boolean value) {
        this.consecutive = value;
    }

    /**
     * Gets the value of the firstRow property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFirstRow() {
        if (firstRow == null) {
            return false;
        } else {
            return firstRow;
        }
    }

    /**
     * Sets the value of the firstRow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFirstRow(Boolean value) {
        this.firstRow = value;
    }

    /**
     * Gets the value of the xl97 property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isXl97() {
        if (xl97 == null) {
            return false;
        } else {
            return xl97;
        }
    }

    /**
     * Sets the value of the xl97 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setXl97(Boolean value) {
        this.xl97 = value;
    }

    /**
     * Gets the value of the textDates property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isTextDates() {
        if (textDates == null) {
            return false;
        } else {
            return textDates;
        }
    }

    /**
     * Sets the value of the textDates property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTextDates(Boolean value) {
        this.textDates = value;
    }

    /**
     * Gets the value of the xl2000 property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isXl2000() {
        if (xl2000 == null) {
            return false;
        } else {
            return xl2000;
        }
    }

    /**
     * Sets the value of the xl2000 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setXl2000(Boolean value) {
        this.xl2000 = value;
    }

    /**
     * Gets the value of the url property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the value of the url property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUrl(String value) {
        this.url = value;
    }

    /**
     * Gets the value of the post property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPost() {
        return post;
    }

    /**
     * Sets the value of the post property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPost(String value) {
        this.post = value;
    }

    /**
     * Gets the value of the htmlTables property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHtmlTables() {
        if (htmlTables == null) {
            return false;
        } else {
            return htmlTables;
        }
    }

    /**
     * Sets the value of the htmlTables property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHtmlTables(Boolean value) {
        this.htmlTables = value;
    }

    /**
     * Gets the value of the htmlFormat property.
     * 
     * @return
     *     possible object is
     *     {@link STHtmlFmt }
     *     
     */
    public STHtmlFmt getHtmlFormat() {
        if (htmlFormat == null) {
            return STHtmlFmt.NONE;
        } else {
            return htmlFormat;
        }
    }

    /**
     * Sets the value of the htmlFormat property.
     * 
     * @param value
     *     allowed object is
     *     {@link STHtmlFmt }
     *     
     */
    public void setHtmlFormat(STHtmlFmt value) {
        this.htmlFormat = value;
    }

    /**
     * Gets the value of the editPage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEditPage() {
        return editPage;
    }

    /**
     * Sets the value of the editPage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEditPage(String value) {
        this.editPage = value;
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
