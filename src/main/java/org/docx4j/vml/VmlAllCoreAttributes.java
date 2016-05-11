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

import java.math.BigInteger;

import org.docx4j.vml.officedrawing.STHrAlign;
import org.docx4j.vml.officedrawing.STInsetMode;

/**
 * Corresponds to attGroup ref=";urn:schemas-microsoft-com:vml}AG_AllCoreAttributes"
 * in the xsd.
 * 
 * @author jharrop
 * @since 3.0.1
 */
public interface VmlAllCoreAttributes {
	
    /**
     * Gets the value of the href property.
     * 
     * @return
     *     possible object is
     *     ;@link String }
     *     
     */
    public String getHref();

    /**
     * Sets the value of the href property.
     * 
     * @param value
     *     allowed object is
     *     ;@link String }
     *     
     */
    public void setHref(String value) ;

    /**
     * Gets the value of the target property.
     * 
     * @return
     *     possible object is
     *     ;@link String }
     *     
     */
    public String getTarget() ;

    /**
     * Sets the value of the target property.
     * 
     * @param value
     *     allowed object is
     *     ;@link String }
     *     
     */
    public void setTarget(String value) ;

    /**
     * Gets the value of the clazz property.
     * 
     * @return
     *     possible object is
     *     ;@link String }
     *     
     */
    public String getClazz() ;

    /**
     * Sets the value of the clazz property.
     * 
     * @param value
     *     allowed object is
     *     ;@link String }
     *     
     */
    public void setClazz(String value) ;

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     ;@link String }
     *     
     */
    public String getTitle() ;

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     ;@link String }
     *     
     */
    public void setTitle(String value) ;

    /**
     * Gets the value of the alt property.
     * 
     * @return
     *     possible object is
     *     ;@link String }
     *     
     */
    public String getAlt() ;

    /**
     * Sets the value of the alt property.
     * 
     * @param value
     *     allowed object is
     *     ;@link String }
     *     
     */
    public void setAlt(String value) ;

    /**
     * Gets the value of the coordsize property.
     * 
     * @return
     *     possible object is
     *     ;@link String }
     *     
     */
    public String getCoordsize() ;

    /**
     * Sets the value of the coordsize property.
     * 
     * @param value
     *     allowed object is
     *     ;@link String }
     *     
     */
    public void setCoordsize(String value) ;

    /**
     * Gets the value of the coordorigin property.
     * 
     * @return
     *     possible object is
     *     ;@link String }
     *     
     */
    public String getCoordorigin() ;

    /**
     * Sets the value of the coordorigin property.
     * 
     * @param value
     *     allowed object is
     *     ;@link String }
     *     
     */
    public void setCoordorigin(String value) ;

    /**
     * Gets the value of the wrapcoords property.
     * 
     * @return
     *     possible object is
     *     ;@link String }
     *     
     */
    public String getWrapcoords() ;

    /**
     * Sets the value of the wrapcoords property.
     * 
     * @param value
     *     allowed object is
     *     ;@link String }
     *     
     */
    public void setWrapcoords(String value) ;

    /**
     * Gets the value of the print property.
     * 
     * @return
     *     possible object is
     *     ;@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getPrint() ;

    /**
     * Sets the value of the print property.
     * 
     * @param value
     *     allowed object is
     *     ;@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setPrint(org.docx4j.vml.STTrueFalse value) ;

    /**
     * Gets the value of the style property.
     * 
     * @return
     *     possible object is
     *     ;@link String }
     *     
     */
    public String getStyle() ;

    /**
     * Sets the value of the style property.
     * 
     * @param value
     *     allowed object is
     *     ;@link String }
     *     
     */
    public void setStyle(String value) ;

    /**
     * Gets the value of the vmlId property.
     * 
     * @return
     *     possible object is
     *     ;@link String }
     *     
     */
    public String getVmlId() ;

    /**
     * Sets the value of the vmlId property.
     * 
     * @param value
     *     allowed object is
     *     ;@link String }
     *     
     */
    public void setVmlId(String value) ;

    /**
     * Optional String
     * 
     * @return
     *     possible object is
     *     ;@link String }
     *     
     */
    public String getSpid() ;

    /**
     * Sets the value of the spid property.
     * 
     * @param value
     *     allowed object is
     *     ;@link String }
     *     
     */
    public void setSpid(String value) ;

    /**
     * Shape Handle Toggle
     * 
     * @return
     *     possible object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getOned() ;

    /**
     * Sets the value of the oned property.
     * 
     * @param value
     *     allowed object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setOned(org.docx4j.vml.officedrawing.STTrueFalse value) ;

    /**
     * Regroup ID
     * 
     * @return
     *     possible object is
     *     ;@link BigInteger }
     *     
     */
    public BigInteger getRegroupid() ;

    /**
     * Sets the value of the regroupid property.
     * 
     * @param value
     *     allowed object is
     *     ;@link BigInteger }
     *     
     */
    public void setRegroupid(BigInteger value) ;

    /**
     * Double-click Notification Toggle
     * 
     * @return
     *     possible object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getDoubleclicknotify() ;

    /**
     * Sets the value of the doubleclicknotify property.
     * 
     * @param value
     *     allowed object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setDoubleclicknotify(org.docx4j.vml.officedrawing.STTrueFalse value) ;

    /**
     * Button Behavior Toggle
     * 
     * @return
     *     possible object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getButton() ;

    /**
     * Sets the value of the button property.
     * 
     * @param value
     *     allowed object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setButton(org.docx4j.vml.officedrawing.STTrueFalse value) ;

    /**
     * Hide Script Anchors
     * 
     * @return
     *     possible object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getUserhidden() ;

    /**
     * Sets the value of the userhidden property.
     * 
     * @param value
     *     allowed object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setUserhidden(org.docx4j.vml.officedrawing.STTrueFalse value) ;

    /**
     * Graphical Bullet
     * 
     * @return
     *     possible object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getBullet() ;

    /**
     * Sets the value of the bullet property.
     * 
     * @param value
     *     allowed object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setBullet(org.docx4j.vml.officedrawing.STTrueFalse value) ;

    /**
     * Horizontal Rule Toggle
     * 
     * @return
     *     possible object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getHr() ;

    /**
     * Sets the value of the hr property.
     * 
     * @param value
     *     allowed object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setHr(org.docx4j.vml.officedrawing.STTrueFalse value) ;

    /**
     * Horizontal Rule Standard Display Toggle
     * 
     * @return
     *     possible object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getHrstd() ;

    /**
     * Sets the value of the hrstd property.
     * 
     * @param value
     *     allowed object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setHrstd(org.docx4j.vml.officedrawing.STTrueFalse value) ;

    /**
     * Horizontal Rule 3D Shading Toggle
     * 
     * @return
     *     possible object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getHrnoshade() ;

    /**
     * Sets the value of the hrnoshade property.
     * 
     * @param value
     *     allowed object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setHrnoshade(org.docx4j.vml.officedrawing.STTrueFalse value) ;

    /**
     * Horizontal Rule Length Percentage
     * 
     * @return
     *     possible object is
     *     ;@link Float }
     *     
     */
    public Float getHrpct() ;

    /**
     * Sets the value of the hrpct property.
     * 
     * @param value
     *     allowed object is
     *     ;@link Float }
     *     
     */
    public void setHrpct(Float value) ;

    /**
     * Horizontal Rule Alignment
     * 
     * @return
     *     possible object is
     *     ;@link STHrAlign }
     *     
     */
    public STHrAlign getHralign() ;

    /**
     * Sets the value of the hralign property.
     * 
     * @param value
     *     allowed object is
     *     ;@link STHrAlign }
     *     
     */
    public void setHralign(STHrAlign value) ;

    /**
     * Allow in Table Cell
     * 
     * @return
     *     possible object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getAllowincell() ;

    /**
     * Sets the value of the allowincell property.
     * 
     * @param value
     *     allowed object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setAllowincell(org.docx4j.vml.officedrawing.STTrueFalse value) ;

    /**
     * Allow Shape Overlap
     * 
     * @return
     *     possible object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getAllowoverlap() ;

    /**
     * Sets the value of the allowoverlap property.
     * 
     * @param value
     *     allowed object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setAllowoverlap(org.docx4j.vml.officedrawing.STTrueFalse value) ;

    /**
     * Exists In Master Slide
     * 
     * @return
     *     possible object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getUserdrawn() ;

    /**
     * Sets the value of the userdrawn property.
     * 
     * @param value
     *     allowed object is
     *     ;@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setUserdrawn(org.docx4j.vml.officedrawing.STTrueFalse value) ;

    /**
     * Border Top Color
     * 
     * @return
     *     possible object is
     *     ;@link String }
     *     
     */
    public String getBordertopcolor() ;

    /**
     * Sets the value of the bordertopcolor property.
     * 
     * @param value
     *     allowed object is
     *     ;@link String }
     *     
     */
    public void setBordertopcolor(String value) ;

    /**
     * Border Left Color
     * 
     * @return
     *     possible object is
     *     ;@link String }
     *     
     */
    public String getBorderleftcolor() ;

    /**
     * Sets the value of the borderleftcolor property.
     * 
     * @param value
     *     allowed object is
     *     ;@link String }
     *     
     */
    public void setBorderleftcolor(String value) ;

    /**
     * Bottom Border Color
     * 
     * @return
     *     possible object is
     *     ;@link String }
     *     
     */
    public String getBorderbottomcolor() ;

    /**
     * Sets the value of the borderbottomcolor property.
     * 
     * @param value
     *     allowed object is
     *     ;@link String }
     *     
     */
    public void setBorderbottomcolor(String value) ;

    /**
     * Border Right Color
     * 
     * @return
     *     possible object is
     *     ;@link String }
     *     
     */
    public String getBorderrightcolor() ;

    /**
     * Sets the value of the borderrightcolor property.
     * 
     * @param value
     *     allowed object is
     *     ;@link String }
     *     
     */
    public void setBorderrightcolor(String value) ;

    /**
     * Diagram Node Layout Identifier
     * 
     * @return
     *     possible object is
     *     ;@link BigInteger }
     *     
     */
    public BigInteger getDgmlayout() ;

    /**
     * Sets the value of the dgmlayout property.
     * 
     * @param value
     *     allowed object is
     *     ;@link BigInteger }
     *     
     */
    public void setDgmlayout(BigInteger value) ;

    /**
     * Diagram Node Identifier
     * 
     * @return
     *     possible object is
     *     ;@link BigInteger }
     *     
     */
    public BigInteger getDgmnodekind() ;

    /**
     * Sets the value of the dgmnodekind property.
     * 
     * @param value
     *     allowed object is
     *     ;@link BigInteger }
     *     
     */
    public void setDgmnodekind(BigInteger value) ;

    /**
     * Diagram Node Recent Layout Identifier
     * 
     * @return
     *     possible object is
     *     ;@link BigInteger }
     *     
     */
    public BigInteger getDgmlayoutmru() ;

    /**
     * Sets the value of the dgmlayoutmru property.
     * 
     * @param value
     *     allowed object is
     *     ;@link BigInteger }
     *     
     */
    public void setDgmlayoutmru(BigInteger value) ;

    /**
     * Text Inset Mode
     * 
     * @return
     *     possible object is
     *     ;@link STInsetMode }
     *     
     */
    public STInsetMode getInsetmode() ;

    /**
     * Sets the value of the insetmode property.
     * 
     * @param value
     *     allowed object is
     *     ;@link STInsetMode }
     *     
     */
    public void setInsetmode(STInsetMode value) ;




    /**
     * Gets the value of the filled property.
     * 
     * @return
     *     possible object is
     *     ;@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getFilled() ;

    /**
     * Sets the value of the filled property.
     * 
     * @param value
     *     allowed object is
     *     ;@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setFilled(org.docx4j.vml.STTrueFalse value) ;

    /**
     * Gets the value of the fillcolor property.
     * 
     * @return
     *     possible object is
     *     ;@link String }
     *     
     */
    public String getFillcolor() ;

    /**
     * Sets the value of the fillcolor property.
     * 
     * @param value
     *     allowed object is
     *     ;@link String }
     *     
     */
    public void setFillcolor(String value) ;



}
