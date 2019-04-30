/*
 *  Copyright 2010-2012, Plutext Pty Ltd.
 *   
 *  This file is part of pptx4j, a component of docx4j.

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
package org.pptx4j.pml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TransitionSoundAction complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TransitionSoundAction"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="stSnd" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TransitionStartSoundAction"/&gt;
 *         &lt;element name="endSnd" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_Empty"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TransitionSoundAction", propOrder = {
    "stSnd",
    "endSnd"
})
public class CTTransitionSoundAction implements Child
{

    protected CTTransitionStartSoundAction stSnd;
    protected CTEmpty endSnd;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the stSnd property.
     * 
     * @return
     *     possible object is
     *     {@link CTTransitionStartSoundAction }
     *     
     */
    public CTTransitionStartSoundAction getStSnd() {
        return stSnd;
    }

    /**
     * Sets the value of the stSnd property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTransitionStartSoundAction }
     *     
     */
    public void setStSnd(CTTransitionStartSoundAction value) {
        this.stSnd = value;
    }

    /**
     * Gets the value of the endSnd property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmpty }
     *     
     */
    public CTEmpty getEndSnd() {
        return endSnd;
    }

    /**
     * Sets the value of the endSnd property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmpty }
     *     
     */
    public void setEndSnd(CTEmpty value) {
        this.endSnd = value;
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
