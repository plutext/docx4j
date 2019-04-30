
package org.docx4j.com.microsoft.schemas.office.drawing.x2012.main;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.docx4j.dml.STBlackWhiteMode;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_BackgroundPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_BackgroundPr"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="bwMode" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_BlackWhiteMode" /&gt;
 *       &lt;attribute name="bwPure" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_BlackWhiteMode" /&gt;
 *       &lt;attribute name="bwNormal" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_BlackWhiteMode" /&gt;
 *       &lt;attribute name="targetScreenSize" type="{http://schemas.microsoft.com/office/drawing/2012/main}ST_TargetScreenSz" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_BackgroundPr")
public class CTBackgroundPr implements Child
{

    @XmlAttribute(name = "bwMode")
    protected STBlackWhiteMode bwMode;
    @XmlAttribute(name = "bwPure")
    protected STBlackWhiteMode bwPure;
    @XmlAttribute(name = "bwNormal")
    protected STBlackWhiteMode bwNormal;
    @XmlAttribute(name = "targetScreenSize")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String targetScreenSize;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the bwMode property.
     * 
     * @return
     *     possible object is
     *     {@link STBlackWhiteMode }
     *     
     */
    public STBlackWhiteMode getBwMode() {
        return bwMode;
    }

    /**
     * Sets the value of the bwMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link STBlackWhiteMode }
     *     
     */
    public void setBwMode(STBlackWhiteMode value) {
        this.bwMode = value;
    }

    /**
     * Gets the value of the bwPure property.
     * 
     * @return
     *     possible object is
     *     {@link STBlackWhiteMode }
     *     
     */
    public STBlackWhiteMode getBwPure() {
        return bwPure;
    }

    /**
     * Sets the value of the bwPure property.
     * 
     * @param value
     *     allowed object is
     *     {@link STBlackWhiteMode }
     *     
     */
    public void setBwPure(STBlackWhiteMode value) {
        this.bwPure = value;
    }

    /**
     * Gets the value of the bwNormal property.
     * 
     * @return
     *     possible object is
     *     {@link STBlackWhiteMode }
     *     
     */
    public STBlackWhiteMode getBwNormal() {
        return bwNormal;
    }

    /**
     * Sets the value of the bwNormal property.
     * 
     * @param value
     *     allowed object is
     *     {@link STBlackWhiteMode }
     *     
     */
    public void setBwNormal(STBlackWhiteMode value) {
        this.bwNormal = value;
    }

    /**
     * Gets the value of the targetScreenSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetScreenSize() {
        return targetScreenSize;
    }

    /**
     * Sets the value of the targetScreenSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetScreenSize(String value) {
        this.targetScreenSize = value;
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
