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


package org.docx4j.dml.diagram;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Constraint complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Constraint"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attGroup ref="{http://schemas.openxmlformats.org/drawingml/2006/diagram}AG_ConstraintAttributes"/&gt;
 *       &lt;attGroup ref="{http://schemas.openxmlformats.org/drawingml/2006/diagram}AG_ConstraintRefAttributes"/&gt;
 *       &lt;attribute name="op" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_BoolOperator" default="none" /&gt;
 *       &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}double" default="0" /&gt;
 *       &lt;attribute name="fact" type="{http://www.w3.org/2001/XMLSchema}double" default="1" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Constraint", propOrder = {
    "extLst"
})
public class CTConstraint implements Child
{

    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "op")
    protected STBoolOperator op;
    @XmlAttribute(name = "val")
    protected Double val;
    @XmlAttribute(name = "fact")
    protected Double fact;
    @XmlAttribute(name = "type", required = true)
    protected STConstraintType type;
    @XmlAttribute(name = "for")
    protected STConstraintRelationship _for;
    @XmlAttribute(name = "forName")
    protected String forName;
    @XmlAttribute(name = "ptType")
    protected STElementType ptType;
    @XmlAttribute(name = "refType")
    protected STConstraintType refType;
    @XmlAttribute(name = "refFor")
    protected STConstraintRelationship refFor;
    @XmlAttribute(name = "refForName")
    protected String refForName;
    @XmlAttribute(name = "refPtType")
    protected STElementType refPtType;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public CTOfficeArtExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public void setExtLst(CTOfficeArtExtensionList value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the op property.
     * 
     * @return
     *     possible object is
     *     {@link STBoolOperator }
     *     
     */
    public STBoolOperator getOp() {
        if (op == null) {
            return STBoolOperator.NONE;
        } else {
            return op;
        }
    }

    /**
     * Sets the value of the op property.
     * 
     * @param value
     *     allowed object is
     *     {@link STBoolOperator }
     *     
     */
    public void setOp(STBoolOperator value) {
        this.op = value;
    }

    /**
     * Gets the value of the val property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public double getVal() {
        if (val == null) {
            return  0.0D;
        } else {
            return val;
        }
    }

    /**
     * Sets the value of the val property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setVal(Double value) {
        this.val = value;
    }

    /**
     * Gets the value of the fact property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public double getFact() {
        if (fact == null) {
            return  1.0D;
        } else {
            return fact;
        }
    }

    /**
     * Sets the value of the fact property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setFact(Double value) {
        this.fact = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STConstraintType }
     *     
     */
    public STConstraintType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STConstraintType }
     *     
     */
    public void setType(STConstraintType value) {
        this.type = value;
    }

    /**
     * Gets the value of the for property.
     * 
     * @return
     *     possible object is
     *     {@link STConstraintRelationship }
     *     
     */
    public STConstraintRelationship getFor() {
        if (_for == null) {
            return STConstraintRelationship.SELF;
        } else {
            return _for;
        }
    }

    /**
     * Sets the value of the for property.
     * 
     * @param value
     *     allowed object is
     *     {@link STConstraintRelationship }
     *     
     */
    public void setFor(STConstraintRelationship value) {
        this._for = value;
    }

    /**
     * Gets the value of the forName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getForName() {
        if (forName == null) {
            return "";
        } else {
            return forName;
        }
    }

    /**
     * Sets the value of the forName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setForName(String value) {
        this.forName = value;
    }

    /**
     * Gets the value of the ptType property.
     * 
     * @return
     *     possible object is
     *     {@link STElementType }
     *     
     */
    public STElementType getPtType() {
        if (ptType == null) {
            return STElementType.ALL;
        } else {
            return ptType;
        }
    }

    /**
     * Sets the value of the ptType property.
     * 
     * @param value
     *     allowed object is
     *     {@link STElementType }
     *     
     */
    public void setPtType(STElementType value) {
        this.ptType = value;
    }

    /**
     * Gets the value of the refType property.
     * 
     * @return
     *     possible object is
     *     {@link STConstraintType }
     *     
     */
    public STConstraintType getRefType() {
        if (refType == null) {
            return STConstraintType.NONE;
        } else {
            return refType;
        }
    }

    /**
     * Sets the value of the refType property.
     * 
     * @param value
     *     allowed object is
     *     {@link STConstraintType }
     *     
     */
    public void setRefType(STConstraintType value) {
        this.refType = value;
    }

    /**
     * Gets the value of the refFor property.
     * 
     * @return
     *     possible object is
     *     {@link STConstraintRelationship }
     *     
     */
    public STConstraintRelationship getRefFor() {
        if (refFor == null) {
            return STConstraintRelationship.SELF;
        } else {
            return refFor;
        }
    }

    /**
     * Sets the value of the refFor property.
     * 
     * @param value
     *     allowed object is
     *     {@link STConstraintRelationship }
     *     
     */
    public void setRefFor(STConstraintRelationship value) {
        this.refFor = value;
    }

    /**
     * Gets the value of the refForName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRefForName() {
        if (refForName == null) {
            return "";
        } else {
            return refForName;
        }
    }

    /**
     * Sets the value of the refForName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRefForName(String value) {
        this.refForName = value;
    }

    /**
     * Gets the value of the refPtType property.
     * 
     * @return
     *     possible object is
     *     {@link STElementType }
     *     
     */
    public STElementType getRefPtType() {
        if (refPtType == null) {
            return STElementType.ALL;
        } else {
            return refPtType;
        }
    }

    /**
     * Sets the value of the refPtType property.
     * 
     * @param value
     *     allowed object is
     *     {@link STElementType }
     *     
     */
    public void setRefPtType(STElementType value) {
        this.refPtType = value;
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
