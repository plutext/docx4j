/*
 *  Copyright 2014, Plutext Pty Ltd.
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
package org.docx4j.vml;

import org.docx4j.vml.officedrawing.STBWMode;
import org.docx4j.vml.officedrawing.STConnectorType;

/**
 * Corresponds to attGroup ref="{urn:schemas-microsoft-com:vml}AG_AllShapeAttributes"
 * in the xsd.
 * 
 * @author jharrop
 * @since 3.0.1
 */
public interface VmlAllShapeAttributes {
	
    /**
     * Gets the value of the opacity property.
     * 
     * @return
     *     possible object is
     *     ;@link String }
     *     
     */
    public String getOpacity() ;

    /**
     * Sets the value of the opacity property.
     * 
     * @param value
     *     allowed object is
     *     ;@link String }
     *     
     */
    public void setOpacity(String value) ;

    /**
     * Gets the value of the stroked property.
     * 
     * @return
     *     possible object is
     *     ;@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getStroked() ;

    /**
     * Sets the value of the stroked property.
     * 
     * @param value
     *     allowed object is
     *     ;@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setStroked(org.docx4j.vml.STTrueFalse value) ;

    /**
     * Gets the value of the strokecolor property.
     * 
     * @return
     *     possible object is
     *     ;@link String }
     *     
     */
    public String getStrokecolor() ;

    /**
     * Sets the value of the strokecolor property.
     * 
     * @param value
     *     allowed object is
     *     ;@link String }
     *     
     */
    public void setStrokecolor(String value) ;

    /**
     * Gets the value of the strokeweight property.
     * 
     * @return
     *     possible object is
     *     ;@link String }
     *     
     */
    public String getStrokeweight() ;

    /**
     * Sets the value of the strokeweight property.
     * 
     * @param value
     *     allowed object is
     *     ;@link String }
     *     
     */
    public void setStrokeweight(String value) ;

    /**
     * Gets the value of the insetpen property.
     * 
     * @return
     *     possible object is
     *     ;@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getInsetpen() ;

    /**
     * Sets the value of the insetpen property.
     * 
     * @param value
     *     allowed object is
     *     ;@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setInsetpen(org.docx4j.vml.STTrueFalse value) ;

    /**
     * Gets the value of the chromakey property.
     * 
     * @return
     *     possible object is
     *     ;@link String }
     *     
     */
    public String getChromakey() ;

    /**
     * Sets the value of the chromakey property.
     * 
     * @param value
     *     allowed object is
     *     ;@link String }
     *     
     */
    public void setChromakey(String value) ;

    /**
     * Optional Number
     * 
     * @return
     *     possible object is
     *     ;@link Float }
     *     
     */
    public Float getSpt() ;

    /**
     * Sets the value of the spt property.
     * 
     * @param value
     *     allowed object is
     *     ;@link Float }
     *     
     */
    public void setSpt(Float value) ;

    /**
     * Shape Connector Type
     * 
     * @return
     *     possible object is
     *     ;@link STConnectorType }
     *     
     */
    public STConnectorType getConnectortype() ;

    /**
     * Sets the value of the connectortype property.
     * 
     * @param value
     *     allowed object is
     *     ;@link STConnectorType }
     *     
     */
    public void setConnectortype(STConnectorType value) ;

    /**
     * Black-and-White Mode
     * 
     * @return
     *     possible object is
     *     ;@link STBWMode }
     *     
     */
    public STBWMode getBwmode() ;

    /**
     * Sets the value of the bwmode property.
     * 
     * @param value
     *     allowed object is
     *     ;@link STBWMode }
     *     
     */
    public void setBwmode(STBWMode value) ;

    /**
     * Pure Black-and-White Mode
     * 
     * @return
     *     possible object is
     *     ;@link STBWMode }
     *     
     */
    public STBWMode getBwpure() ;

    /**
     * Sets the value of the bwpure property.
     * 
     * @param value
     *     allowed object is
     *     ;@link STBWMode }
     *     
     */
    public void setBwpure(STBWMode value) ;

    /**
     * Normal Black-and-White Mode
     * 
     * @return
     *     possible object is
     *     ;@link STBWMode }
     *     
     */
    public STBWMode getBwnormal() ;

    /**
     * Sets the value of the bwnormal property.
     * 
     * @param value
     *     allowed object is
     *     ;@link STBWMode }
     *     
     */
    public void setBwnormal(STBWMode value) ;

    /**
     * Force Dashed Outline
     * 
     * @return
     *     possible object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getForcedash() ;

    /**
     * Sets the value of the forcedash property.
     * 
     * @param value
     *     allowed object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setForcedash(org.docx4j.vml.officedrawing.STTrueFalse value) ;

    /**
     * Embedded Object Icon Toggle
     * 
     * @return
     *     possible object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getOleicon() ;

    /**
     * Sets the value of the oleicon property.
     * 
     * @param value
     *     allowed object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setOleicon(org.docx4j.vml.officedrawing.STTrueFalse value) ;

    /**
     * Embedded Object Toggle
     * 
     * @return
     *     possible object is
     *     ;@link String }
     *     
     */
    public String getOle() ;

    /**
     * Sets the value of the ole property.
     * 
     * @param value
     *     allowed object is
     *     ;@link String }
     *     
     */
    public void setOle(String value) ;

    /**
     * Relative Resize Toggle
     * 
     * @return
     *     possible object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getPreferrelative() ;

    /**
     * Sets the value of the preferrelative property.
     * 
     * @param value
     *     allowed object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setPreferrelative(org.docx4j.vml.officedrawing.STTrueFalse value) ;

    /**
     * Clip to Wrapping Polygon
     * 
     * @return
     *     possible object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getCliptowrap() ;

    /**
     * Sets the value of the cliptowrap property.
     * 
     * @param value
     *     allowed object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setCliptowrap(org.docx4j.vml.officedrawing.STTrueFalse value) ;

    /**
     * Clipping Toggle
     * 
     * @return
     *     possible object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getClip() ;

    /**
     * Sets the value of the clip property.
     * 
     * @param value
     *     allowed object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setClip(org.docx4j.vml.officedrawing.STTrueFalse value) ;
	

}
