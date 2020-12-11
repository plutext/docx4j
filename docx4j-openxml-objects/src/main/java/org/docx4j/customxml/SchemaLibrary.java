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


package org.docx4j.customxml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
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
 *         &lt;element name="schema" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="uri" type="{http://www.w3.org/2001/XMLSchema}string" default="" />
 *                 &lt;attribute name="manifestLocation" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="schemaLocation" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
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
    "schema"
})
@XmlRootElement(name = "schemaLibrary")
public class SchemaLibrary
    implements Child
{

    protected List<SchemaLibrary.Schema> schema;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the schema property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the schema property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSchema().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SchemaLibrary.Schema }
     * 
     * 
     */
    public List<SchemaLibrary.Schema> getSchema() {
        if (schema == null) {
            schema = new ArrayList<SchemaLibrary.Schema>();
        }
        return this.schema;
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
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="uri" type="{http://www.w3.org/2001/XMLSchema}string" default="" />
     *       &lt;attribute name="manifestLocation" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="schemaLocation" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Schema
        implements Child
    {

        @XmlAttribute(namespace = "http://schemas.openxmlformats.org/schemaLibrary/2006/main")
        protected String uri;
        @XmlAttribute(namespace = "http://schemas.openxmlformats.org/schemaLibrary/2006/main")
        protected String manifestLocation;
        @XmlAttribute(namespace = "http://schemas.openxmlformats.org/schemaLibrary/2006/main")
        protected String schemaLocation;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the uri property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getUri() {
            if (uri == null) {
                return "";
            } else {
                return uri;
            }
        }

        /**
         * Sets the value of the uri property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setUri(String value) {
            this.uri = value;
        }

        /**
         * Gets the value of the manifestLocation property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getManifestLocation() {
            return manifestLocation;
        }

        /**
         * Sets the value of the manifestLocation property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setManifestLocation(String value) {
            this.manifestLocation = value;
        }

        /**
         * Gets the value of the schemaLocation property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSchemaLocation() {
            return schemaLocation;
        }

        /**
         * Sets the value of the schemaLocation property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSchemaLocation(String value) {
            this.schemaLocation = value;
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

}
