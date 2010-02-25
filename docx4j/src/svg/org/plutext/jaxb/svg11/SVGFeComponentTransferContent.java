/*
 *  Copyright 2010, Plutext Pty Ltd.
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


package org.plutext.jaxb.svg11;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SVG.feComponentTransfer.content complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SVG.feComponentTransfer.content">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2000/svg}feFuncR" minOccurs="0"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}feFuncG" minOccurs="0"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}feFuncB" minOccurs="0"/>
 *         &lt;element ref="{http://www.w3.org/2000/svg}feFuncA" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SVG.feComponentTransfer.content", propOrder = {
    "feFuncR",
    "feFuncG",
    "feFuncB",
    "feFuncA"
})
public class SVGFeComponentTransferContent {

    protected FeFuncR feFuncR;
    protected FeFuncG feFuncG;
    protected FeFuncB feFuncB;
    protected FeFuncA feFuncA;

    /**
     * Gets the value of the feFuncR property.
     * 
     * @return
     *     possible object is
     *     {@link FeFuncR }
     *     
     */
    public FeFuncR getFeFuncR() {
        return feFuncR;
    }

    /**
     * Sets the value of the feFuncR property.
     * 
     * @param value
     *     allowed object is
     *     {@link FeFuncR }
     *     
     */
    public void setFeFuncR(FeFuncR value) {
        this.feFuncR = value;
    }

    /**
     * Gets the value of the feFuncG property.
     * 
     * @return
     *     possible object is
     *     {@link FeFuncG }
     *     
     */
    public FeFuncG getFeFuncG() {
        return feFuncG;
    }

    /**
     * Sets the value of the feFuncG property.
     * 
     * @param value
     *     allowed object is
     *     {@link FeFuncG }
     *     
     */
    public void setFeFuncG(FeFuncG value) {
        this.feFuncG = value;
    }

    /**
     * Gets the value of the feFuncB property.
     * 
     * @return
     *     possible object is
     *     {@link FeFuncB }
     *     
     */
    public FeFuncB getFeFuncB() {
        return feFuncB;
    }

    /**
     * Sets the value of the feFuncB property.
     * 
     * @param value
     *     allowed object is
     *     {@link FeFuncB }
     *     
     */
    public void setFeFuncB(FeFuncB value) {
        this.feFuncB = value;
    }

    /**
     * Gets the value of the feFuncA property.
     * 
     * @return
     *     possible object is
     *     {@link FeFuncA }
     *     
     */
    public FeFuncA getFeFuncA() {
        return feFuncA;
    }

    /**
     * Sets the value of the feFuncA property.
     * 
     * @param value
     *     allowed object is
     *     {@link FeFuncA }
     *     
     */
    public void setFeFuncA(FeFuncA value) {
        this.feFuncA = value;
    }

}
