
package org.docx4j.dml.diagram2008;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTNonVisualDrawingShapeProps;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ShapeNonVisual complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ShapeNonVisual"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="cNvPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualDrawingProps"/&gt;
 *         &lt;element name="cNvSpPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualDrawingShapeProps"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ShapeNonVisual", propOrder = {
    "cNvPr",
    "cNvSpPr"
})
public class CTShapeNonVisual implements Child
{

    @XmlElement(required = true)
    protected CTNonVisualDrawingProps cNvPr;
    @XmlElement(required = true)
    protected CTNonVisualDrawingShapeProps cNvSpPr;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the cNvPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTNonVisualDrawingProps }
     *     
     */
    public CTNonVisualDrawingProps getCNvPr() {
        return cNvPr;
    }

    /**
     * Sets the value of the cNvPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNonVisualDrawingProps }
     *     
     */
    public void setCNvPr(CTNonVisualDrawingProps value) {
        this.cNvPr = value;
    }

    /**
     * Gets the value of the cNvSpPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTNonVisualDrawingShapeProps }
     *     
     */
    public CTNonVisualDrawingShapeProps getCNvSpPr() {
        return cNvSpPr;
    }

    /**
     * Sets the value of the cNvSpPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNonVisualDrawingShapeProps }
     *     
     */
    public void setCNvSpPr(CTNonVisualDrawingShapeProps value) {
        this.cNvSpPr = value;
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
