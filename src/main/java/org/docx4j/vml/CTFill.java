/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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

import java.math.BigDecimal;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Fill complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Fill">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:schemas-microsoft-com:office:office}fill" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_Id"/>
 *       &lt;attribute name="type" type="{urn:schemas-microsoft-com:vml}ST_FillType" />
 *       &lt;attribute name="on" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="color" type="{urn:schemas-microsoft-com:vml}ST_ColorType" />
 *       &lt;attribute name="opacity" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="color2" type="{urn:schemas-microsoft-com:vml}ST_ColorType" />
 *       &lt;attribute name="src" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}href"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}althref"/>
 *       &lt;attribute name="size" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="origin" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="position" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="aspect" type="{urn:schemas-microsoft-com:vml}ST_ImageAspect" />
 *       &lt;attribute name="colors" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="angle" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *       &lt;attribute name="alignshape" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="focus" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="focussize" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="focusposition" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="method" type="{urn:schemas-microsoft-com:vml}ST_FillMethod" />
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}detectmouseclick"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}title"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}opacity2"/>
 *       &lt;attribute name="recolor" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute name="rotate" type="{urn:schemas-microsoft-com:vml}ST_TrueFalse" />
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}id"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}relid"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Fill", propOrder = {
    "fill"
})
public class CTFill implements Child
{

    @XmlElement(namespace = "urn:schemas-microsoft-com:office:office")
    protected org.docx4j.vml.officedrawing.CTFill fill;
    @XmlAttribute(name = "type")
    protected STFillType type;
    @XmlAttribute(name = "on")
    protected org.docx4j.vml.STTrueFalse on;
    @XmlAttribute(name = "color")
    protected String color;
    @XmlAttribute(name = "opacity")
    protected String opacity;
    @XmlAttribute(name = "color2")
    protected String color2;
    @XmlAttribute(name = "src")
    protected String src;
    @XmlAttribute(name = "href", namespace = "urn:schemas-microsoft-com:office:office")
    protected String href;
    @XmlAttribute(name = "althref", namespace = "urn:schemas-microsoft-com:office:office")
    protected String althref;
    @XmlAttribute(name = "size")
    protected String size;
    @XmlAttribute(name = "origin")
    protected String origin;
    @XmlAttribute(name = "position")
    protected String position;
    @XmlAttribute(name = "aspect")
    protected STImageAspect aspect;
    @XmlAttribute(name = "colors")
    protected String colors;
    @XmlAttribute(name = "angle")
    protected BigDecimal angle;
    @XmlAttribute(name = "alignshape")
    protected org.docx4j.vml.STTrueFalse alignshape;
    @XmlAttribute(name = "focus")
    protected String focus;
    @XmlAttribute(name = "focussize")
    protected String focussize;
    @XmlAttribute(name = "focusposition")
    protected String focusposition;
    @XmlAttribute(name = "method")
    protected STFillMethod method;
    @XmlAttribute(name = "detectmouseclick", namespace = "urn:schemas-microsoft-com:office:office")
    protected org.docx4j.vml.officedrawing.STTrueFalse detectmouseclick;
    @XmlAttribute(name = "title", namespace = "urn:schemas-microsoft-com:office:office")
    protected String title;
    @XmlAttribute(name = "opacity2", namespace = "urn:schemas-microsoft-com:office:office")
    protected String opacity2;
    @XmlAttribute(name = "recolor")
    protected org.docx4j.vml.STTrueFalse recolor;
    @XmlAttribute(name = "rotate")
    protected org.docx4j.vml.STTrueFalse rotate;
    @XmlAttribute(name = "id", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String id;
    @XmlAttribute(name = "relid", namespace = "urn:schemas-microsoft-com:office:office")
    protected String relid;
    @XmlAttribute(name = "id")
    protected String vmlId;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the fill property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.officedrawing.CTFill }
     *     
     */
    public org.docx4j.vml.officedrawing.CTFill getFill() {
        return fill;
    }

    /**
     * Sets the value of the fill property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.officedrawing.CTFill }
     *     
     */
    public void setFill(org.docx4j.vml.officedrawing.CTFill value) {
        this.fill = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STFillType }
     *     
     */
    public STFillType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STFillType }
     *     
     */
    public void setType(STFillType value) {
        this.type = value;
    }

    /**
     * Gets the value of the on property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getOn() {
        return on;
    }

    /**
     * Sets the value of the on property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setOn(org.docx4j.vml.STTrueFalse value) {
        this.on = value;
    }

    /**
     * Gets the value of the color property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the value of the color property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColor(String value) {
        this.color = value;
    }

    /**
     * Gets the value of the opacity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOpacity() {
        return opacity;
    }

    /**
     * Sets the value of the opacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOpacity(String value) {
        this.opacity = value;
    }

    /**
     * Gets the value of the color2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColor2() {
        return color2;
    }

    /**
     * Sets the value of the color2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColor2(String value) {
        this.color2 = value;
    }

    /**
     * Gets the value of the src property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSrc() {
        return src;
    }

    /**
     * Sets the value of the src property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSrc(String value) {
        this.src = value;
    }

    /**
     * Hyperlink Target
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the value of the href property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHref(String value) {
        this.href = value;
    }

    /**
     * Alternate Image Reference Location
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlthref() {
        return althref;
    }

    /**
     * Sets the value of the althref property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlthref(String value) {
        this.althref = value;
    }

    /**
     * Gets the value of the size property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSize() {
        return size;
    }

    /**
     * Sets the value of the size property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSize(String value) {
        this.size = value;
    }

    /**
     * Gets the value of the origin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Sets the value of the origin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrigin(String value) {
        this.origin = value;
    }

    /**
     * Gets the value of the position property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPosition() {
        return position;
    }

    /**
     * Sets the value of the position property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPosition(String value) {
        this.position = value;
    }

    /**
     * Gets the value of the aspect property.
     * 
     * @return
     *     possible object is
     *     {@link STImageAspect }
     *     
     */
    public STImageAspect getAspect() {
        return aspect;
    }

    /**
     * Sets the value of the aspect property.
     * 
     * @param value
     *     allowed object is
     *     {@link STImageAspect }
     *     
     */
    public void setAspect(STImageAspect value) {
        this.aspect = value;
    }

    /**
     * Gets the value of the colors property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColors() {
        return colors;
    }

    /**
     * Sets the value of the colors property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColors(String value) {
        this.colors = value;
    }

    /**
     * Gets the value of the angle property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAngle() {
        return angle;
    }

    /**
     * Sets the value of the angle property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAngle(BigDecimal value) {
        this.angle = value;
    }

    /**
     * Gets the value of the alignshape property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getAlignshape() {
        return alignshape;
    }

    /**
     * Sets the value of the alignshape property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setAlignshape(org.docx4j.vml.STTrueFalse value) {
        this.alignshape = value;
    }

    /**
     * Gets the value of the focus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFocus() {
        return focus;
    }

    /**
     * Sets the value of the focus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFocus(String value) {
        this.focus = value;
    }

    /**
     * Gets the value of the focussize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFocussize() {
        return focussize;
    }

    /**
     * Sets the value of the focussize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFocussize(String value) {
        this.focussize = value;
    }

    /**
     * Gets the value of the focusposition property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFocusposition() {
        return focusposition;
    }

    /**
     * Sets the value of the focusposition property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFocusposition(String value) {
        this.focusposition = value;
    }

    /**
     * Gets the value of the method property.
     * 
     * @return
     *     possible object is
     *     {@link STFillMethod }
     *     
     */
    public STFillMethod getMethod() {
        return method;
    }

    /**
     * Sets the value of the method property.
     * 
     * @param value
     *     allowed object is
     *     {@link STFillMethod }
     *     
     */
    public void setMethod(STFillMethod value) {
        this.method = value;
    }

    /**
     * Detect Mouse Click
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public org.docx4j.vml.officedrawing.STTrueFalse getDetectmouseclick() {
        return detectmouseclick;
    }

    /**
     * Sets the value of the detectmouseclick property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.officedrawing.STTrueFalse }
     *     
     */
    public void setDetectmouseclick(org.docx4j.vml.officedrawing.STTrueFalse value) {
        this.detectmouseclick = value;
    }

    /**
     * Title
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Secondary Color Opacity
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOpacity2() {
        return opacity2;
    }

    /**
     * Sets the value of the opacity2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOpacity2(String value) {
        this.opacity2 = value;
    }

    /**
     * Gets the value of the recolor property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getRecolor() {
        return recolor;
    }

    /**
     * Sets the value of the recolor property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setRecolor(org.docx4j.vml.STTrueFalse value) {
        this.recolor = value;
    }

    /**
     * Gets the value of the rotate property.
     * 
     * @return
     *     possible object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public org.docx4j.vml.STTrueFalse getRotate() {
        return rotate;
    }

    /**
     * Sets the value of the rotate property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.docx4j.vml.STTrueFalse }
     *     
     */
    public void setRotate(org.docx4j.vml.STTrueFalse value) {
        this.rotate = value;
    }

    /**
     * Relationship to Part
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Relationship to Part
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelid() {
        return relid;
    }

    /**
     * Sets the value of the relid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelid(String value) {
        this.relid = value;
    }

    /**
     * Gets the value of the vmlId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVmlId() {
        return vmlId;
    }

    /**
     * Sets the value of the vmlId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVmlId(String value) {
        this.vmlId = value;
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
