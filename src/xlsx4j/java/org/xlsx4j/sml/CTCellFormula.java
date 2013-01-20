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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_CellFormula complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CellFormula">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://schemas.openxmlformats.org/spreadsheetml/2006/main>ST_Formula">
 *       &lt;attribute name="t" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_CellFormulaType" default="normal" />
 *       &lt;attribute name="aca" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="ref" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Ref" />
 *       &lt;attribute name="dt2D" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="dtr" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="del1" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="del2" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="r1" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_CellRef" />
 *       &lt;attribute name="r2" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_CellRef" />
 *       &lt;attribute name="ca" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="si" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="bx" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CellFormula", propOrder = {
    "value"
})
public class CTCellFormula implements Child
{

    @XmlValue
    protected String value;
    @XmlAttribute(name = "t")
    protected STCellFormulaType t;
    @XmlAttribute(name = "aca")
    protected Boolean aca;
    @XmlAttribute(name = "ref")
    protected String ref;
    @XmlAttribute(name = "dt2D")
    protected Boolean dt2D;
    @XmlAttribute(name = "dtr")
    protected Boolean dtr;
    @XmlAttribute(name = "del1")
    protected Boolean del1;
    @XmlAttribute(name = "del2")
    protected Boolean del2;
    @XmlAttribute(name = "r1")
    protected String r1;
    @XmlAttribute(name = "r2")
    protected String r2;
    @XmlAttribute(name = "ca")
    protected Boolean ca;
    @XmlAttribute(name = "si")
    @XmlSchemaType(name = "unsignedInt")
    protected Long si;
    @XmlAttribute(name = "bx")
    protected Boolean bx;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the t property.
     * 
     * @return
     *     possible object is
     *     {@link STCellFormulaType }
     *     
     */
    public STCellFormulaType getT() {
        if (t == null) {
            return STCellFormulaType.NORMAL;
        } else {
            return t;
        }
    }

    /**
     * Sets the value of the t property.
     * 
     * @param value
     *     allowed object is
     *     {@link STCellFormulaType }
     *     
     */
    public void setT(STCellFormulaType value) {
        this.t = value;
    }

    /**
     * Gets the value of the aca property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAca() {
        if (aca == null) {
            return false;
        } else {
            return aca;
        }
    }

    /**
     * Sets the value of the aca property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAca(Boolean value) {
        this.aca = value;
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
     * Gets the value of the dt2D property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDt2D() {
        if (dt2D == null) {
            return false;
        } else {
            return dt2D;
        }
    }

    /**
     * Sets the value of the dt2D property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDt2D(Boolean value) {
        this.dt2D = value;
    }

    /**
     * Gets the value of the dtr property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDtr() {
        if (dtr == null) {
            return false;
        } else {
            return dtr;
        }
    }

    /**
     * Sets the value of the dtr property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDtr(Boolean value) {
        this.dtr = value;
    }

    /**
     * Gets the value of the del1 property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDel1() {
        if (del1 == null) {
            return false;
        } else {
            return del1;
        }
    }

    /**
     * Sets the value of the del1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDel1(Boolean value) {
        this.del1 = value;
    }

    /**
     * Gets the value of the del2 property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDel2() {
        if (del2 == null) {
            return false;
        } else {
            return del2;
        }
    }

    /**
     * Sets the value of the del2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDel2(Boolean value) {
        this.del2 = value;
    }

    /**
     * Gets the value of the r1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getR1() {
        return r1;
    }

    /**
     * Sets the value of the r1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setR1(String value) {
        this.r1 = value;
    }

    /**
     * Gets the value of the r2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getR2() {
        return r2;
    }

    /**
     * Sets the value of the r2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setR2(String value) {
        this.r2 = value;
    }

    /**
     * Gets the value of the ca property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCa() {
        if (ca == null) {
            return false;
        } else {
            return ca;
        }
    }

    /**
     * Sets the value of the ca property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCa(Boolean value) {
        this.ca = value;
    }

    /**
     * Gets the value of the si property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getSi() {
        return si;
    }

    /**
     * Sets the value of the si property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setSi(Long value) {
        this.si = value;
    }

    /**
     * Gets the value of the bx property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isBx() {
        if (bx == null) {
            return false;
        } else {
            return bx;
        }
    }

    /**
     * Sets the value of the bx property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setBx(Boolean value) {
        this.bx = value;
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
