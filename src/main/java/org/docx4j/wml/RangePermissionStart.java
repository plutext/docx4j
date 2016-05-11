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

import java.math.BigInteger;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_PermStart complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PermStart">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Perm">
 *       &lt;attribute name="edGrp">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="none"/>
 *             &lt;enumeration value="everyone"/>
 *             &lt;enumeration value="administrators"/>
 *             &lt;enumeration value="contributors"/>
 *             &lt;enumeration value="editors"/>
 *             &lt;enumeration value="owners"/>
 *             &lt;enumeration value="current"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="ed" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
 *       &lt;attribute name="colFirst" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *       &lt;attribute name="colLast" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PermStart")
@XmlRootElement(name = "permStart")
public class RangePermissionStart
    extends CTPerm
{

    @XmlAttribute(name = "edGrp", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String edGrp;
    @XmlAttribute(name = "ed", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String ed;
    @XmlAttribute(name = "colFirst", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger colFirst;
    @XmlAttribute(name = "colLast", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger colLast;

    /**
     * Gets the value of the edGrp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEdGrp() {
        return edGrp;
    }

    /**
     * Sets the value of the edGrp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEdGrp(String value) {
        this.edGrp = value;
    }

    /**
     * Gets the value of the ed property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEd() {
        return ed;
    }

    /**
     * Sets the value of the ed property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEd(String value) {
        this.ed = value;
    }

    /**
     * Gets the value of the colFirst property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getColFirst() {
        return colFirst;
    }

    /**
     * Sets the value of the colFirst property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setColFirst(BigInteger value) {
        this.colFirst = value;
    }

    /**
     * Gets the value of the colLast property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getColLast() {
        return colLast;
    }

    /**
     * Sets the value of the colLast property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setColLast(BigInteger value) {
        this.colLast = value;
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
