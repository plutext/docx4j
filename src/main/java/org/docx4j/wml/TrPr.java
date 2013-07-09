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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_TrPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TrPr">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TrPrBase">
 *       &lt;sequence>
 *         &lt;element name="ins" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TrackChange" minOccurs="0"/>
 *         &lt;element name="del" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TrackChange" minOccurs="0"/>
 *         &lt;element name="trPrChange" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TrPrChange" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TrPr", propOrder = {
    "ins",
    "del",
    "trPrChange"
})
public class TrPr
    extends CTTrPrBase
{

    protected CTTrackChange ins;
    protected CTTrackChange del;
    protected CTTrPrChange trPrChange;

    /**
     * Gets the value of the ins property.
     * 
     * @return
     *     possible object is
     *     {@link CTTrackChange }
     *     
     */
    public CTTrackChange getIns() {
        return ins;
    }

    /**
     * Sets the value of the ins property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTrackChange }
     *     
     */
    public void setIns(CTTrackChange value) {
        this.ins = value;
    }

    /**
     * Gets the value of the del property.
     * 
     * @return
     *     possible object is
     *     {@link CTTrackChange }
     *     
     */
    public CTTrackChange getDel() {
        return del;
    }

    /**
     * Sets the value of the del property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTrackChange }
     *     
     */
    public void setDel(CTTrackChange value) {
        this.del = value;
    }

    /**
     * Gets the value of the trPrChange property.
     * 
     * @return
     *     possible object is
     *     {@link CTTrPrChange }
     *     
     */
    public CTTrPrChange getTrPrChange() {
        return trPrChange;
    }

    /**
     * Sets the value of the trPrChange property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTrPrChange }
     *     
     */
    public void setTrPrChange(CTTrPrChange value) {
        this.trPrChange = value;
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
