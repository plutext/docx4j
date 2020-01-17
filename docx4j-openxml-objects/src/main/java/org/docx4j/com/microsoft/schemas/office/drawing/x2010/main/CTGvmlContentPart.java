
package org.docx4j.com.microsoft.schemas.office.drawing.x2010.main;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.docx4j.dml.CTTransform2D;
import org.docx4j.dml.STBlackWhiteMode;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_GvmlContentPart complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GvmlContentPart"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="nvContentPartPr" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_GvmlContentPartNonVisual" minOccurs="0"/&gt;
 *         &lt;element name="xfrm" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Transform2D" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="bwMode" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_BlackWhiteMode" /&gt;
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}id use="required""/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GvmlContentPart", propOrder = {
    "nvContentPartPr",
    "xfrm",
    "extLst"
})
public class CTGvmlContentPart implements Child
{

    protected CTGvmlContentPartNonVisual nvContentPartPr;
    protected CTTransform2D xfrm;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "bwMode")
    protected STBlackWhiteMode bwMode;
    @XmlAttribute(name = "id", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships", required = true)
    protected String id;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the nvContentPartPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTGvmlContentPartNonVisual }
     *     
     */
    public CTGvmlContentPartNonVisual getNvContentPartPr() {
        return nvContentPartPr;
    }

    /**
     * Sets the value of the nvContentPartPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGvmlContentPartNonVisual }
     *     
     */
    public void setNvContentPartPr(CTGvmlContentPartNonVisual value) {
        this.nvContentPartPr = value;
    }

    /**
     * Gets the value of the xfrm property.
     * 
     * @return
     *     possible object is
     *     {@link CTTransform2D }
     *     
     */
    public CTTransform2D getXfrm() {
        return xfrm;
    }

    /**
     * Sets the value of the xfrm property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTransform2D }
     *     
     */
    public void setXfrm(CTTransform2D value) {
        this.xfrm = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public CTOfficeArtExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public void setExtLst(CTOfficeArtExtensionList value) {
        this.extLst = value;
    }

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
     * Gets the value of the id property.
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
