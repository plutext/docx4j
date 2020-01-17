
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTShapeProperties;
import org.jvnet.jaxb2_commons.ppp.Child;;


/**
 * <p>Java class for CT_PlotArea complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PlotArea"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="plotAreaRegion" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_PlotAreaRegion"/&gt;
 *         &lt;element name="axis" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_Axis" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="spPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeProperties" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_ExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PlotArea", propOrder = {
    "plotAreaRegion",
    "axis",
    "spPr",
    "extLst"
})
public class CTPlotArea implements Child
{

    @XmlElement(required = true)
    protected CTPlotAreaRegion plotAreaRegion;
    protected List<CTAxis> axis;
    protected CTShapeProperties spPr;
    protected CTExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the plotAreaRegion property.
     * 
     * @return
     *     possible object is
     *     {@link CTPlotAreaRegion }
     *     
     */
    public CTPlotAreaRegion getPlotAreaRegion() {
        return plotAreaRegion;
    }

    /**
     * Sets the value of the plotAreaRegion property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPlotAreaRegion }
     *     
     */
    public void setPlotAreaRegion(CTPlotAreaRegion value) {
        this.plotAreaRegion = value;
    }

    /**
     * Gets the value of the axis property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the axis property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAxis().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTAxis }
     * 
     * 
     */
    public List<CTAxis> getAxis() {
        if (axis == null) {
            axis = new ArrayList<CTAxis>();
        }
        return this.axis;
    }

    /**
     * Gets the value of the spPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTShapeProperties }
     *     
     */
    public CTShapeProperties getSpPr() {
        return spPr;
    }

    /**
     * Sets the value of the spPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShapeProperties }
     *     
     */
    public void setSpPr(CTShapeProperties value) {
        this.spPr = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtensionList }
     *     
     */
    public CTExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtensionList }
     *     
     */
    public void setExtLst(CTExtensionList value) {
        this.extLst = value;
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
