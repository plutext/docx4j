
package org.docx4j.com.microsoft.schemas.office.drawing.x201611.diagram;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTTextAutonumberBullet;
import org.docx4j.dml.CTTextBlipBullet;
import org.docx4j.dml.CTTextCharBullet;
import org.docx4j.dml.CTTextNoBullet;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_DiagramAutoBullet complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DiagramAutoBullet"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_TextBullet"/&gt;
 *       &lt;attribute name="prefix" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="leadZeros" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DiagramAutoBullet", propOrder = {
    "buNone",
    "buAutoNum",
    "buChar",
    "buBlip"
})
public class CTDiagramAutoBullet implements Child
{

    @XmlElement(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main")
    protected CTTextNoBullet buNone;
    @XmlElement(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main")
    protected CTTextAutonumberBullet buAutoNum;
    @XmlElement(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main")
    protected CTTextCharBullet buChar;
    @XmlElement(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main")
    protected CTTextBlipBullet buBlip;
    @XmlAttribute(name = "prefix")
    protected String prefix;
    @XmlAttribute(name = "leadZeros")
    protected Boolean leadZeros;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the buNone property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextNoBullet }
     *     
     */
    public CTTextNoBullet getBuNone() {
        return buNone;
    }

    /**
     * Sets the value of the buNone property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextNoBullet }
     *     
     */
    public void setBuNone(CTTextNoBullet value) {
        this.buNone = value;
    }

    /**
     * Gets the value of the buAutoNum property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextAutonumberBullet }
     *     
     */
    public CTTextAutonumberBullet getBuAutoNum() {
        return buAutoNum;
    }

    /**
     * Sets the value of the buAutoNum property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextAutonumberBullet }
     *     
     */
    public void setBuAutoNum(CTTextAutonumberBullet value) {
        this.buAutoNum = value;
    }

    /**
     * Gets the value of the buChar property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextCharBullet }
     *     
     */
    public CTTextCharBullet getBuChar() {
        return buChar;
    }

    /**
     * Sets the value of the buChar property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextCharBullet }
     *     
     */
    public void setBuChar(CTTextCharBullet value) {
        this.buChar = value;
    }

    /**
     * Gets the value of the buBlip property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextBlipBullet }
     *     
     */
    public CTTextBlipBullet getBuBlip() {
        return buBlip;
    }

    /**
     * Sets the value of the buBlip property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextBlipBullet }
     *     
     */
    public void setBuBlip(CTTextBlipBullet value) {
        this.buBlip = value;
    }

    /**
     * Gets the value of the prefix property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the value of the prefix property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrefix(String value) {
        this.prefix = value;
    }

    /**
     * Gets the value of the leadZeros property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isLeadZeros() {
        return leadZeros;
    }

    /**
     * Sets the value of the leadZeros property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLeadZeros(Boolean value) {
        this.leadZeros = value;
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
