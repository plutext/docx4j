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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_TimeNodeList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TimeNodeList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="par" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLTimeNodeParallel"/>
 *         &lt;element name="seq" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLTimeNodeSequence"/>
 *         &lt;element name="excl" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLTimeNodeExclusive"/>
 *         &lt;element name="anim" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLAnimateBehavior"/>
 *         &lt;element name="animClr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLAnimateColorBehavior"/>
 *         &lt;element name="animEffect" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLAnimateEffectBehavior"/>
 *         &lt;element name="animMotion" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLAnimateMotionBehavior"/>
 *         &lt;element name="animRot" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLAnimateRotationBehavior"/>
 *         &lt;element name="animScale" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLAnimateScaleBehavior"/>
 *         &lt;element name="cmd" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLCommandBehavior"/>
 *         &lt;element name="set" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLSetBehavior"/>
 *         &lt;element name="audio" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLMediaNodeAudio"/>
 *         &lt;element name="video" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLMediaNodeVideo"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TimeNodeList", propOrder = {
    "parOrSeqOrExcl"
})
public class CTTimeNodeList {

    @XmlElements({
        @XmlElement(name = "par", type = CTTLTimeNodeParallel.class),
        @XmlElement(name = "seq", type = CTTLTimeNodeSequence.class),
        @XmlElement(name = "excl", type = CTTLTimeNodeExclusive.class),
        @XmlElement(name = "anim", type = CTTLAnimateBehavior.class),
        @XmlElement(name = "animClr", type = CTTLAnimateColorBehavior.class),
        @XmlElement(name = "animEffect", type = CTTLAnimateEffectBehavior.class),
        @XmlElement(name = "animMotion", type = CTTLAnimateMotionBehavior.class),
        @XmlElement(name = "animRot", type = CTTLAnimateRotationBehavior.class),
        @XmlElement(name = "animScale", type = CTTLAnimateScaleBehavior.class),
        @XmlElement(name = "cmd", type = CTTLCommandBehavior.class),
        @XmlElement(name = "set", type = CTTLSetBehavior.class),
        @XmlElement(name = "audio", type = CTTLMediaNodeAudio.class),
        @XmlElement(name = "video", type = CTTLMediaNodeVideo.class)
    })
    protected List<Object> parOrSeqOrExcl;

    /**
     * Gets the value of the parOrSeqOrExcl property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the parOrSeqOrExcl property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParOrSeqOrExcl().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTTLTimeNodeParallel }
     * {@link CTTLTimeNodeSequence }
     * {@link CTTLTimeNodeExclusive }
     * {@link CTTLAnimateBehavior }
     * {@link CTTLAnimateColorBehavior }
     * {@link CTTLAnimateEffectBehavior }
     * {@link CTTLAnimateMotionBehavior }
     * {@link CTTLAnimateRotationBehavior }
     * {@link CTTLAnimateScaleBehavior }
     * {@link CTTLCommandBehavior }
     * {@link CTTLSetBehavior }
     * {@link CTTLMediaNodeAudio }
     * {@link CTTLMediaNodeVideo }
     * 
     * 
     */
    public List<Object> getParOrSeqOrExcl() {
        if (parOrSeqOrExcl == null) {
            parOrSeqOrExcl = new ArrayList<Object>();
        }
        return this.parOrSeqOrExcl;
    }

}
