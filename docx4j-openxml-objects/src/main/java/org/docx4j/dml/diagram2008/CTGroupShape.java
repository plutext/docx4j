
package org.docx4j.dml.diagram2008;

import org.docx4j.dml.ArrayListDml;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTGroupShapeProperties;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_GroupShape complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GroupShape"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="nvGrpSpPr" type="{http://schemas.microsoft.com/office/drawing/2008/diagram}CT_GroupShapeNonVisual"/&gt;
 *         &lt;element name="grpSpPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GroupShapeProperties"/&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="sp" type="{http://schemas.microsoft.com/office/drawing/2008/diagram}CT_Shape"/&gt;
 *           &lt;element name="grpSp" type="{http://schemas.microsoft.com/office/drawing/2008/diagram}CT_GroupShape"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GroupShape", propOrder = {
    "nvGrpSpPr",
    "grpSpPr",
    "spOrGrpSp",
    "extLst"
})
public class CTGroupShape implements Child
{

    @XmlElement(required = true)
    protected CTGroupShapeNonVisual nvGrpSpPr;
    @XmlElement(required = true)
    protected CTGroupShapeProperties grpSpPr;
    @XmlElements({
        @XmlElement(name = "sp", type = CTShape.class),
        @XmlElement(name = "grpSp", type = CTGroupShape.class)
    })
    protected List<Object> spOrGrpSp = new ArrayListDml<Object>(this);
    protected CTOfficeArtExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the nvGrpSpPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTGroupShapeNonVisual }
     *     
     */
    public CTGroupShapeNonVisual getNvGrpSpPr() {
        return nvGrpSpPr;
    }

    /**
     * Sets the value of the nvGrpSpPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGroupShapeNonVisual }
     *     
     */
    public void setNvGrpSpPr(CTGroupShapeNonVisual value) {
        this.nvGrpSpPr = value;
    }

    /**
     * Gets the value of the grpSpPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTGroupShapeProperties }
     *     
     */
    public CTGroupShapeProperties getGrpSpPr() {
        return grpSpPr;
    }

    /**
     * Sets the value of the grpSpPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGroupShapeProperties }
     *     
     */
    public void setGrpSpPr(CTGroupShapeProperties value) {
        this.grpSpPr = value;
    }

    /**
     * Gets the value of the spOrGrpSp property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the spOrGrpSp property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSpOrGrpSp().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTShape }
     * {@link CTGroupShape }
     * 
     * 
     */
    public List<Object> getSpOrGrpSp() {
        if (spOrGrpSp == null) {
            spOrGrpSp = new ArrayListDml<Object>(this);
        }
        return this.spOrGrpSp;
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
