
package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PresetGeometry2D complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PresetGeometry2D">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="avLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GeomGuideList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="prst" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_ShapeType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PresetGeometry2D", propOrder = {
    "avLst"
})
public class CTPresetGeometry2D
    implements Child
{

    protected CTGeomGuideList avLst;
    @XmlAttribute(required = true)
    protected STShapeType prst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the avLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTGeomGuideList }
     *     
     */
    public CTGeomGuideList getAvLst() {
        return avLst;
    }

    /**
     * Sets the value of the avLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGeomGuideList }
     *     
     */
    public void setAvLst(CTGeomGuideList value) {
        this.avLst = value;
    }

    /**
     * Gets the value of the prst property.
     * 
     * @return
     *     possible object is
     *     {@link STShapeType }
     *     
     */
    public STShapeType getPrst() {
        return prst;
    }

    /**
     * Sets the value of the prst property.
     * 
     * @param value
     *     allowed object is
     *     {@link STShapeType }
     *     
     */
    public void setPrst(STShapeType value) {
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
