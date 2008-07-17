
package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PatternFillProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PatternFillProperties">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fgClr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color" minOccurs="0"/>
 *         &lt;element name="bgClr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="prst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PresetPatternVal" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PatternFillProperties", propOrder = {
    "fgClr",
    "bgClr"
})
public class CTPatternFillProperties implements Child
{

    protected CTColor fgClr;
    protected CTColor bgClr;
    @XmlAttribute
    protected STPresetPatternVal prst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the fgClr property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getFgClr() {
        return fgClr;
    }

    /**
     * Sets the value of the fgClr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setFgClr(CTColor value) {
        this.fgClr = value;
    }

    /**
     * Gets the value of the bgClr property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getBgClr() {
        return bgClr;
    }

    /**
     * Sets the value of the bgClr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setBgClr(CTColor value) {
        this.bgClr = value;
    }

    /**
     * Gets the value of the prst property.
     * 
     * @return
     *     possible object is
     *     {@link STPresetPatternVal }
     *     
     */
    public STPresetPatternVal getPrst() {
        return prst;
    }

    /**
     * Sets the value of the prst property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPresetPatternVal }
     *     
     */
    public void setPrst(STPresetPatternVal value) {
        this.prst = value;
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
