
package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_NonVisualDrawingShapeProps complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_NonVisualDrawingShapeProps">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="spLocks" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeLocking" minOccurs="0"/>
 *         &lt;element name="ext" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtension" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="txBox" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_NonVisualDrawingShapeProps", propOrder = {
    "spLocks",
    "ext"
})
public class CTNonVisualDrawingShapeProps
    implements Child
{

    protected CTShapeLocking spLocks;
    protected CTOfficeArtExtension ext;
    @XmlAttribute
    protected Boolean txBox;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the spLocks property.
     * 
     * @return
     *     possible object is
     *     {@link CTShapeLocking }
     *     
     */
    public CTShapeLocking getSpLocks() {
        return spLocks;
    }

    /**
     * Sets the value of the spLocks property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShapeLocking }
     *     
     */
    public void setSpLocks(CTShapeLocking value) {
        this.spLocks = value;
    }

    /**
     * Gets the value of the ext property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtension }
     *     
     */
    public CTOfficeArtExtension getExt() {
        return ext;
    }

    /**
     * Sets the value of the ext property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtension }
     *     
     */
    public void setExt(CTOfficeArtExtension value) {
        this.ext = value;
    }

    /**
     * Gets the value of the txBox property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isTxBox() {
        if (txBox == null) {
            return false;
        } else {
            return txBox;
        }
    }

    /**
     * Sets the value of the txBox property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTxBox(Boolean value) {
        this.txBox = value;
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
