
package org.docx4j.com.microsoft.schemas.office.drawing.x2010.main;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_GvmlContentPartNonVisual complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GvmlContentPartNonVisual"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="cNvPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualDrawingProps"/&gt;
 *         &lt;element name="cNvContentPartPr" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_NonVisualInkContentPartProperties" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GvmlContentPartNonVisual", propOrder = {
    "cNvPr",
    "cNvContentPartPr"
})
public class CTGvmlContentPartNonVisual implements Child
{

    @XmlElement(required = true)
    protected CTNonVisualDrawingProps cNvPr;
    protected CTNonVisualInkContentPartProperties cNvContentPartPr;
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
     * Gets the value of the cNvContentPartPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTNonVisualInkContentPartProperties }
     *     
     */
    public CTNonVisualInkContentPartProperties getCNvContentPartPr() {
        return cNvContentPartPr;
    }

    /**
     * Sets the value of the cNvContentPartPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNonVisualInkContentPartProperties }
     *     
     */
    public void setCNvContentPartPr(CTNonVisualInkContentPartProperties value) {
        this.cNvContentPartPr = value;
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
