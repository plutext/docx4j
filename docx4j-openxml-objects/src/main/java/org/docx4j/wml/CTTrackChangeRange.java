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

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_TrackChangeRange complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TrackChangeRange">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TrackChange">
 *       &lt;attribute name="displacedByCustomXml" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DisplacedByCustomXml" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TrackChangeRange")
public class CTTrackChangeRange
    extends CTTrackChange
{

    @XmlAttribute(name = "displacedByCustomXml", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STDisplacedByCustomXml displacedByCustomXml;

    /**
     * Gets the value of the displacedByCustomXml property.
     * 
     * @return
     *     possible object is
     *     {@link STDisplacedByCustomXml }
     *     
     */
    public STDisplacedByCustomXml getDisplacedByCustomXml() {
        return displacedByCustomXml;
    }

    /**
     * Sets the value of the displacedByCustomXml property.
     * 
     * @param value
     *     allowed object is
     *     {@link STDisplacedByCustomXml }
     *     
     */
    public void setDisplacedByCustomXml(STDisplacedByCustomXml value) {
        this.displacedByCustomXml = value;
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
