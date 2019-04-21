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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PivotAreaReference complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PivotAreaReference">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="x" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Index" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="field" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="count" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="selected" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="byPosition" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="relative" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="defaultSubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="sumSubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="countASubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="avgSubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="maxSubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="minSubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="productSubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="countSubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="stdDevSubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="stdDevPSubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="varSubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="varPSubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PivotAreaReference", propOrder = {
    "x",
    "extLst"
})
public class CTPivotAreaReference implements Child
{

    protected List<CTIndex> x;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "field")
    @XmlSchemaType(name = "unsignedInt")
    protected Long field;
    @XmlAttribute(name = "count")
    @XmlSchemaType(name = "unsignedInt")
    protected Long count;
    @XmlAttribute(name = "selected")
    protected Boolean selected;
    @XmlAttribute(name = "byPosition")
    protected Boolean byPosition;
    @XmlAttribute(name = "relative")
    protected Boolean relative;
    @XmlAttribute(name = "defaultSubtotal")
    protected Boolean defaultSubtotal;
    @XmlAttribute(name = "sumSubtotal")
    protected Boolean sumSubtotal;
    @XmlAttribute(name = "countASubtotal")
    protected Boolean countASubtotal;
    @XmlAttribute(name = "avgSubtotal")
    protected Boolean avgSubtotal;
    @XmlAttribute(name = "maxSubtotal")
    protected Boolean maxSubtotal;
    @XmlAttribute(name = "minSubtotal")
    protected Boolean minSubtotal;
    @XmlAttribute(name = "productSubtotal")
    protected Boolean productSubtotal;
    @XmlAttribute(name = "countSubtotal")
    protected Boolean countSubtotal;
    @XmlAttribute(name = "stdDevSubtotal")
    protected Boolean stdDevSubtotal;
    @XmlAttribute(name = "stdDevPSubtotal")
    protected Boolean stdDevPSubtotal;
    @XmlAttribute(name = "varSubtotal")
    protected Boolean varSubtotal;
    @XmlAttribute(name = "varPSubtotal")
    protected Boolean varPSubtotal;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the x property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the x property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getX().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTIndex }
     * 
     * 
     */
    public List<CTIndex> getX() {
        if (x == null) {
            x = new ArrayList<CTIndex>();
        }
        return this.x;
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

    /**
     * Gets the value of the field property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getField() {
        return field;
    }

    /**
     * Sets the value of the field property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setField(Long value) {
        this.field = value;
    }

    /**
     * Gets the value of the count property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCount() {
        return count;
    }

    /**
     * Sets the value of the count property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCount(Long value) {
        this.count = value;
    }

    /**
     * Gets the value of the selected property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSelected() {
        if (selected == null) {
            return true;
        } else {
            return selected;
        }
    }

    /**
     * Sets the value of the selected property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSelected(Boolean value) {
        this.selected = value;
    }

    /**
     * Gets the value of the byPosition property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isByPosition() {
        if (byPosition == null) {
            return false;
        } else {
            return byPosition;
        }
    }

    /**
     * Sets the value of the byPosition property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setByPosition(Boolean value) {
        this.byPosition = value;
    }

    /**
     * Gets the value of the relative property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRelative() {
        if (relative == null) {
            return false;
        } else {
            return relative;
        }
    }

    /**
     * Sets the value of the relative property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRelative(Boolean value) {
        this.relative = value;
    }

    /**
     * Gets the value of the defaultSubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDefaultSubtotal() {
        if (defaultSubtotal == null) {
            return false;
        } else {
            return defaultSubtotal;
        }
    }

    /**
     * Sets the value of the defaultSubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDefaultSubtotal(Boolean value) {
        this.defaultSubtotal = value;
    }

    /**
     * Gets the value of the sumSubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSumSubtotal() {
        if (sumSubtotal == null) {
            return false;
        } else {
            return sumSubtotal;
        }
    }

    /**
     * Sets the value of the sumSubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSumSubtotal(Boolean value) {
        this.sumSubtotal = value;
    }

    /**
     * Gets the value of the countASubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCountASubtotal() {
        if (countASubtotal == null) {
            return false;
        } else {
            return countASubtotal;
        }
    }

    /**
     * Sets the value of the countASubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCountASubtotal(Boolean value) {
        this.countASubtotal = value;
    }

    /**
     * Gets the value of the avgSubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAvgSubtotal() {
        if (avgSubtotal == null) {
            return false;
        } else {
            return avgSubtotal;
        }
    }

    /**
     * Sets the value of the avgSubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAvgSubtotal(Boolean value) {
        this.avgSubtotal = value;
    }

    /**
     * Gets the value of the maxSubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMaxSubtotal() {
        if (maxSubtotal == null) {
            return false;
        } else {
            return maxSubtotal;
        }
    }

    /**
     * Sets the value of the maxSubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMaxSubtotal(Boolean value) {
        this.maxSubtotal = value;
    }

    /**
     * Gets the value of the minSubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMinSubtotal() {
        if (minSubtotal == null) {
            return false;
        } else {
            return minSubtotal;
        }
    }

    /**
     * Sets the value of the minSubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMinSubtotal(Boolean value) {
        this.minSubtotal = value;
    }

    /**
     * Gets the value of the productSubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isProductSubtotal() {
        if (productSubtotal == null) {
            return false;
        } else {
            return productSubtotal;
        }
    }

    /**
     * Sets the value of the productSubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setProductSubtotal(Boolean value) {
        this.productSubtotal = value;
    }

    /**
     * Gets the value of the countSubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCountSubtotal() {
        if (countSubtotal == null) {
            return false;
        } else {
            return countSubtotal;
        }
    }

    /**
     * Sets the value of the countSubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCountSubtotal(Boolean value) {
        this.countSubtotal = value;
    }

    /**
     * Gets the value of the stdDevSubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isStdDevSubtotal() {
        if (stdDevSubtotal == null) {
            return false;
        } else {
            return stdDevSubtotal;
        }
    }

    /**
     * Sets the value of the stdDevSubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setStdDevSubtotal(Boolean value) {
        this.stdDevSubtotal = value;
    }

    /**
     * Gets the value of the stdDevPSubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isStdDevPSubtotal() {
        if (stdDevPSubtotal == null) {
            return false;
        } else {
            return stdDevPSubtotal;
        }
    }

    /**
     * Sets the value of the stdDevPSubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setStdDevPSubtotal(Boolean value) {
        this.stdDevPSubtotal = value;
    }

    /**
     * Gets the value of the varSubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isVarSubtotal() {
        if (varSubtotal == null) {
            return false;
        } else {
            return varSubtotal;
        }
    }

    /**
     * Sets the value of the varSubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVarSubtotal(Boolean value) {
        this.varSubtotal = value;
    }

    /**
     * Gets the value of the varPSubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isVarPSubtotal() {
        if (varPSubtotal == null) {
            return false;
        } else {
            return varPSubtotal;
        }
    }

    /**
     * Sets the value of the varPSubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVarPSubtotal(Boolean value) {
        this.varPSubtotal = value;
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
