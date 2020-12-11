
package org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTHslColor;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.docx4j.dml.CTPresetColor;
import org.docx4j.dml.CTSRgbColor;
import org.docx4j.dml.CTScRgbColor;
import org.docx4j.dml.CTSchemeColor;
import org.docx4j.dml.CTSystemColor;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ColorStyle complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ColorStyle"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_ColorChoice" maxOccurs="unbounded"/&gt;
 *         &lt;element name="variation" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_ColorStyleVariation" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="meth" use="required" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}ST_ColorStyleMethod" /&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ColorStyle", propOrder = {
    "egColorChoice",
    "variation",
    "extLst"
})
public class CTColorStyle implements Child
{

    @XmlElements({
        @XmlElement(name = "scrgbClr", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = CTScRgbColor.class),
        @XmlElement(name = "srgbClr", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = CTSRgbColor.class),
        @XmlElement(name = "hslClr", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = CTHslColor.class),
        @XmlElement(name = "sysClr", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = CTSystemColor.class),
        @XmlElement(name = "schemeClr", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = CTSchemeColor.class),
        @XmlElement(name = "prstClr", namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", type = CTPresetColor.class)
    })
    protected List<Object> egColorChoice;
    protected List<CTColorStyleVariation> variation;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "meth", required = true)
    protected String meth;
    @XmlAttribute(name = "id")
    @XmlSchemaType(name = "unsignedInt")
    protected Long id;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the egColorChoice property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the egColorChoice property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEGColorChoice().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTScRgbColor }
     * {@link CTSRgbColor }
     * {@link CTHslColor }
     * {@link CTSystemColor }
     * {@link CTSchemeColor }
     * {@link CTPresetColor }
     * 
     * 
     */
    public List<Object> getEGColorChoice() {
        if (egColorChoice == null) {
            egColorChoice = new ArrayList<Object>();
        }
        return this.egColorChoice;
    }

    /**
     * Gets the value of the variation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the variation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVariation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTColorStyleVariation }
     * 
     * 
     */
    public List<CTColorStyleVariation> getVariation() {
        if (variation == null) {
            variation = new ArrayList<CTColorStyleVariation>();
        }
        return this.variation;
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
     * Gets the value of the meth property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMeth() {
        return meth;
    }

    /**
     * Sets the value of the meth property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMeth(String value) {
        this.meth = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setId(Long value) {
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
