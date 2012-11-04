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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_SplitTransition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SplitTransition">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="orient" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_Direction" default="horz" />
 *       &lt;attribute name="dir" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TransitionInOutDirectionType" default="out" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SplitTransition")
public class CTSplitTransition {

    @XmlAttribute(name = "orient")
    protected STDirection orient;
    @XmlAttribute(name = "dir")
    protected STTransitionInOutDirectionType dir;

    /**
     * Gets the value of the orient property.
     * 
     * @return
     *     possible object is
     *     {@link STDirection }
     *     
     */
    public STDirection getOrient() {
        if (orient == null) {
            return STDirection.HORZ;
        } else {
            return orient;
        }
    }

    /**
     * Sets the value of the orient property.
     * 
     * @param value
     *     allowed object is
     *     {@link STDirection }
     *     
     */
    public void setOrient(STDirection value) {
        this.orient = value;
    }

    /**
     * Gets the value of the dir property.
     * 
     * @return
     *     possible object is
     *     {@link STTransitionInOutDirectionType }
     *     
     */
    public STTransitionInOutDirectionType getDir() {
        if (dir == null) {
            return STTransitionInOutDirectionType.OUT;
        } else {
            return dir;
        }
    }

    /**
     * Sets the value of the dir property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTransitionInOutDirectionType }
     *     
     */
    public void setDir(STTransitionInOutDirectionType value) {
        this.dir = value;
    }

}
