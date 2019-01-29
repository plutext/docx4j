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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_EdnDocProps complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_EdnDocProps">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_EdnProps">
 *       &lt;sequence>
 *         &lt;element name="endnote" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FtnEdnSepRef" maxOccurs="3" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_EdnDocProps", propOrder = {
    "endnote"
})
public class CTEdnDocProps
    extends CTEdnProps
{

    protected List<CTFtnEdnSepRef> endnote = new ArrayListWml<CTFtnEdnSepRef>(this);

    /**
     * Gets the value of the endnote property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the endnote property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEndnote().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTFtnEdnSepRef }
     * 
     * 
     */
    public List<CTFtnEdnSepRef> getEndnote() {
        if (endnote == null) {
            endnote = new ArrayListWml<CTFtnEdnSepRef>(this);
        }
        return this.endnote;
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
