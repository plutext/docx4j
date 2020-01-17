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
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TimeNodeList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TimeNodeList"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="par" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLTimeNodeParallel"/&gt;
 *         &lt;element name="seq" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLTimeNodeSequence"/&gt;
 *         &lt;element name="excl" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLTimeNodeExclusive"/&gt;
 *         &lt;element name="anim" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLAnimateBehavior"/&gt;
 *         &lt;element name="animClr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLAnimateColorBehavior"/&gt;
 *         &lt;element name="animEffect" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLAnimateEffectBehavior"/&gt;
 *         &lt;element name="animMotion" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLAnimateMotionBehavior"/&gt;
 *         &lt;element name="animRot" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLAnimateRotationBehavior"/&gt;
 *         &lt;element name="animScale" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLAnimateScaleBehavior"/&gt;
 *         &lt;element name="cmd" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLCommandBehavior"/&gt;
 *         &lt;element name="set" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLSetBehavior"/&gt;
 *         &lt;element name="audio" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLMediaNodeAudio"/&gt;
 *         &lt;element name="video" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLMediaNodeVideo"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TimeNodeList", propOrder = {
    "parOrSeqOrExcl"
})
public class CTTimeNodeList implements Child
{

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
    @XmlTransient
    private Object parent;

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
