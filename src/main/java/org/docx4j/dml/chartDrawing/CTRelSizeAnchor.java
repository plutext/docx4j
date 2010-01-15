
package org.docx4j.dml.chartDrawing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_RelSizeAnchor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_RelSizeAnchor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="from" type="{http://schemas.openxmlformats.org/drawingml/2006/chartDrawing}CT_Marker"/>
 *         &lt;element name="to" type="{http://schemas.openxmlformats.org/drawingml/2006/chartDrawing}CT_Marker"/>
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/chartDrawing}EG_ObjectChoices"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_RelSizeAnchor", propOrder = {
    "from",
    "to",
    "sp",
    "grpSp",
    "graphicFrame",
    "cxnSp",
    "pic"
})
public class CTRelSizeAnchor {

    @XmlElement(required = true)
    protected CTMarker from;
    @XmlElement(required = true)
    protected CTMarker to;
    protected CTShape sp;
    protected CTGroupShape grpSp;
    protected CTGraphicFrame graphicFrame;
    protected CTConnector cxnSp;
    protected CTPicture pic;

    /**
     * Gets the value of the from property.
     * 
     * @return
     *     possible object is
     *     {@link CTMarker }
     *     
     */
    public CTMarker getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMarker }
     *     
     */
    public void setFrom(CTMarker value) {
        this.from = value;
    }

    /**
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link CTMarker }
     *     
     */
    public CTMarker getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMarker }
     *     
     */
    public void setTo(CTMarker value) {
        this.to = value;
    }

    /**
     * Gets the value of the sp property.
     * 
     * @return
     *     possible object is
     *     {@link CTShape }
     *     
     */
    public CTShape getSp() {
        return sp;
    }

    /**
     * Sets the value of the sp property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShape }
     *     
     */
    public void setSp(CTShape value) {
        this.sp = value;
    }

    /**
     * Gets the value of the grpSp property.
     * 
     * @return
     *     possible object is
     *     {@link CTGroupShape }
     *     
     */
    public CTGroupShape getGrpSp() {
        return grpSp;
    }

    /**
     * Sets the value of the grpSp property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGroupShape }
     *     
     */
    public void setGrpSp(CTGroupShape value) {
        this.grpSp = value;
    }

    /**
     * Gets the value of the graphicFrame property.
     * 
     * @return
     *     possible object is
     *     {@link CTGraphicFrame }
     *     
     */
    public CTGraphicFrame getGraphicFrame() {
        return graphicFrame;
    }

    /**
     * Sets the value of the graphicFrame property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGraphicFrame }
     *     
     */
    public void setGraphicFrame(CTGraphicFrame value) {
        this.graphicFrame = value;
    }

    /**
     * Gets the value of the cxnSp property.
     * 
     * @return
     *     possible object is
     *     {@link CTConnector }
     *     
     */
    public CTConnector getCxnSp() {
        return cxnSp;
    }

    /**
     * Sets the value of the cxnSp property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTConnector }
     *     
     */
    public void setCxnSp(CTConnector value) {
        this.cxnSp = value;
    }

    /**
     * Gets the value of the pic property.
     * 
     * @return
     *     possible object is
     *     {@link CTPicture }
     *     
     */
    public CTPicture getPic() {
        return pic;
    }

    /**
     * Sets the value of the pic property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPicture }
     *     
     */
    public void setPic(CTPicture value) {
        this.pic = value;
    }

}
