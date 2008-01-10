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

package org.docx4j.wml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TcBorders complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TcBorders">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TblBorders">
 *       &lt;sequence>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}tl2br" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}tr2bl" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TcBorders", propOrder = {
    "tl2Br",
    "tr2Bl"
})
public class TcBorders
    extends TblBorders
    implements Child
{

    @XmlElement(name = "tl2br")
    protected Tl2Br tl2Br;
    @XmlElement(name = "tr2bl")
    protected Tr2Bl tr2Bl;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the tl2Br property.
     * 
     * @return
     *     possible object is
     *     {@link Tl2Br }
     *     
     */
    public Tl2Br getTl2Br() {
        return tl2Br;
    }

    /**
     * Sets the value of the tl2Br property.
     * 
     * @param value
     *     allowed object is
     *     {@link Tl2Br }
     *     
     */
    public void setTl2Br(Tl2Br value) {
        this.tl2Br = value;
    }

    /**
     * Gets the value of the tr2Bl property.
     * 
     * @return
     *     possible object is
     *     {@link Tr2Bl }
     *     
     */
    public Tr2Bl getTr2Bl() {
        return tr2Bl;
    }

    /**
     * Sets the value of the tr2Bl property.
     * 
     * @param value
     *     allowed object is
     *     {@link Tr2Bl }
     *     
     */
    public void setTr2Bl(Tr2Bl value) {
        this.tr2Bl = value;
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
