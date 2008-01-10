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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_SectPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SectPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_SectPrContents" minOccurs="0"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SectPr", propOrder = {
    "type",
    "pgSz",
    "pgMar",
    "pgNumType",
    "titlePg"
})
public class SectPr
    implements Child
{

    protected SectType type;
    protected PageSz pgSz;
    protected PageMar pgMar;
    protected PageNumber pgNumType;
    protected BooleanDefaultTrue titlePg;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link SectType }
     *     
     */
    public SectType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link SectType }
     *     
     */
    public void setType(SectType value) {
        this.type = value;
    }

    /**
     * Gets the value of the pgSz property.
     * 
     * @return
     *     possible object is
     *     {@link PageSz }
     *     
     */
    public PageSz getPgSz() {
        return pgSz;
    }

    /**
     * Sets the value of the pgSz property.
     * 
     * @param value
     *     allowed object is
     *     {@link PageSz }
     *     
     */
    public void setPgSz(PageSz value) {
        this.pgSz = value;
    }

    /**
     * Gets the value of the pgMar property.
     * 
     * @return
     *     possible object is
     *     {@link PageMar }
     *     
     */
    public PageMar getPgMar() {
        return pgMar;
    }

    /**
     * Sets the value of the pgMar property.
     * 
     * @param value
     *     allowed object is
     *     {@link PageMar }
     *     
     */
    public void setPgMar(PageMar value) {
        this.pgMar = value;
    }

    /**
     * Gets the value of the pgNumType property.
     * 
     * @return
     *     possible object is
     *     {@link PageNumber }
     *     
     */
    public PageNumber getPgNumType() {
        return pgNumType;
    }

    /**
     * Sets the value of the pgNumType property.
     * 
     * @param value
     *     allowed object is
     *     {@link PageNumber }
     *     
     */
    public void setPgNumType(PageNumber value) {
        this.pgNumType = value;
    }

    /**
     * Gets the value of the titlePg property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getTitlePg() {
        return titlePg;
    }

    /**
     * Sets the value of the titlePg property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setTitlePg(BooleanDefaultTrue value) {
        this.titlePg = value;
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
