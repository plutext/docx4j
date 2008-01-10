/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.jaxb.document;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for Spacing complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Spacing">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="before" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
 *       &lt;attribute name="beforeLines" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="beforeAutospacing" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
 *       &lt;attribute name="after" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
 *       &lt;attribute name="afterLines" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="afterAutospacing" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_OnOff" />
 *       &lt;attribute name="line" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_SignedTwipsMeasure" />
 *       &lt;attribute name="lineRule" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_LineSpacingRule" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Spacing")
public class Spacing
    implements Child
{

    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger before;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger beforeLines;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String beforeAutospacing;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger after;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger afterLines;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String afterAutospacing;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger line;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected List<String> lineRule;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the before property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getBefore() {
        return before;
    }

    /**
     * Sets the value of the before property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setBefore(BigInteger value) {
        this.before = value;
    }

    /**
     * Gets the value of the beforeLines property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getBeforeLines() {
        return beforeLines;
    }

    /**
     * Sets the value of the beforeLines property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setBeforeLines(BigInteger value) {
        this.beforeLines = value;
    }

    /**
     * Gets the value of the beforeAutospacing property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBeforeAutospacing() {
        return beforeAutospacing;
    }

    /**
     * Sets the value of the beforeAutospacing property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBeforeAutospacing(String value) {
        this.beforeAutospacing = value;
    }

    /**
     * Gets the value of the after property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getAfter() {
        return after;
    }

    /**
     * Sets the value of the after property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setAfter(BigInteger value) {
        this.after = value;
    }

    /**
     * Gets the value of the afterLines property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getAfterLines() {
        return afterLines;
    }

    /**
     * Sets the value of the afterLines property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setAfterLines(BigInteger value) {
        this.afterLines = value;
    }

    /**
     * Gets the value of the afterAutospacing property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAfterAutospacing() {
        return afterAutospacing;
    }

    /**
     * Sets the value of the afterAutospacing property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAfterAutospacing(String value) {
        this.afterAutospacing = value;
    }

    /**
     * Gets the value of the line property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getLine() {
        return line;
    }

    /**
     * Sets the value of the line property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setLine(BigInteger value) {
        this.line = value;
    }

    /**
     * Gets the value of the lineRule property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lineRule property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLineRule().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getLineRule() {
        if (lineRule == null) {
            lineRule = new ArrayList<String>();
        }
        return this.lineRule;
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
