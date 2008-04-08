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

package org.docx4j.wml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TcPrInner complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TcPrInner">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TcPrBase">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_CellMarkupElements" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TcPrInner", propOrder = {
    "cellIns",
    "cellDel",
    "cellMerge"
})
public class CTTcPrInner
    extends CTTcPrBase
    implements Child
{

    protected CTTrackChange cellIns;
    protected CTTrackChange cellDel;
    protected CTCellMergeTrackChange cellMerge;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the cellIns property.
     * 
     * @return
     *     possible object is
     *     {@link CTTrackChange }
     *     
     */
    public CTTrackChange getCellIns() {
        return cellIns;
    }

    /**
     * Sets the value of the cellIns property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTrackChange }
     *     
     */
    public void setCellIns(CTTrackChange value) {
        this.cellIns = value;
    }

    /**
     * Gets the value of the cellDel property.
     * 
     * @return
     *     possible object is
     *     {@link CTTrackChange }
     *     
     */
    public CTTrackChange getCellDel() {
        return cellDel;
    }

    /**
     * Sets the value of the cellDel property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTrackChange }
     *     
     */
    public void setCellDel(CTTrackChange value) {
        this.cellDel = value;
    }

    /**
     * Gets the value of the cellMerge property.
     * 
     * @return
     *     possible object is
     *     {@link CTCellMergeTrackChange }
     *     
     */
    public CTCellMergeTrackChange getCellMerge() {
        return cellMerge;
    }

    /**
     * Sets the value of the cellMerge property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCellMergeTrackChange }
     *     
     */
    public void setCellMerge(CTCellMergeTrackChange value) {
        this.cellMerge = value;
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
