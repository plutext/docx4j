
package org.docx4j.com.microsoft.schemas.office.drawing.x2010.main;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ContentPartLocking complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ContentPartLocking"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attGroup ref="{http://schemas.openxmlformats.org/drawingml/2006/main}AG_Locking"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ContentPartLocking", propOrder = {
    "extLst"
})
public class CTContentPartLocking implements Child
{

    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "noGrp")
    protected Boolean noGrp;
    @XmlAttribute(name = "noSelect")
    protected Boolean noSelect;
    @XmlAttribute(name = "noRot")
    protected Boolean noRot;
    @XmlAttribute(name = "noChangeAspect")
    protected Boolean noChangeAspect;
    @XmlAttribute(name = "noMove")
    protected Boolean noMove;
    @XmlAttribute(name = "noResize")
    protected Boolean noResize;
    @XmlAttribute(name = "noEditPoints")
    protected Boolean noEditPoints;
    @XmlAttribute(name = "noAdjustHandles")
    protected Boolean noAdjustHandles;
    @XmlAttribute(name = "noChangeArrowheads")
    protected Boolean noChangeArrowheads;
    @XmlAttribute(name = "noChangeShapeType")
    protected Boolean noChangeShapeType;
    @XmlTransient
    private Object parent;

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
     * Gets the value of the noGrp property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNoGrp() {
        if (noGrp == null) {
            return false;
        } else {
            return noGrp;
        }
    }

    /**
     * Sets the value of the noGrp property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoGrp(Boolean value) {
        this.noGrp = value;
    }

    /**
     * Gets the value of the noSelect property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNoSelect() {
        if (noSelect == null) {
            return false;
        } else {
            return noSelect;
        }
    }

    /**
     * Sets the value of the noSelect property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoSelect(Boolean value) {
        this.noSelect = value;
    }

    /**
     * Gets the value of the noRot property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNoRot() {
        if (noRot == null) {
            return false;
        } else {
            return noRot;
        }
    }

    /**
     * Sets the value of the noRot property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoRot(Boolean value) {
        this.noRot = value;
    }

    /**
     * Gets the value of the noChangeAspect property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNoChangeAspect() {
        if (noChangeAspect == null) {
            return false;
        } else {
            return noChangeAspect;
        }
    }

    /**
     * Sets the value of the noChangeAspect property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoChangeAspect(Boolean value) {
        this.noChangeAspect = value;
    }

    /**
     * Gets the value of the noMove property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNoMove() {
        if (noMove == null) {
            return false;
        } else {
            return noMove;
        }
    }

    /**
     * Sets the value of the noMove property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoMove(Boolean value) {
        this.noMove = value;
    }

    /**
     * Gets the value of the noResize property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNoResize() {
        if (noResize == null) {
            return false;
        } else {
            return noResize;
        }
    }

    /**
     * Sets the value of the noResize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoResize(Boolean value) {
        this.noResize = value;
    }

    /**
     * Gets the value of the noEditPoints property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNoEditPoints() {
        if (noEditPoints == null) {
            return false;
        } else {
            return noEditPoints;
        }
    }

    /**
     * Sets the value of the noEditPoints property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoEditPoints(Boolean value) {
        this.noEditPoints = value;
    }

    /**
     * Gets the value of the noAdjustHandles property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNoAdjustHandles() {
        if (noAdjustHandles == null) {
            return false;
        } else {
            return noAdjustHandles;
        }
    }

    /**
     * Sets the value of the noAdjustHandles property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoAdjustHandles(Boolean value) {
        this.noAdjustHandles = value;
    }

    /**
     * Gets the value of the noChangeArrowheads property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNoChangeArrowheads() {
        if (noChangeArrowheads == null) {
            return false;
        } else {
            return noChangeArrowheads;
        }
    }

    /**
     * Sets the value of the noChangeArrowheads property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoChangeArrowheads(Boolean value) {
        this.noChangeArrowheads = value;
    }

    /**
     * Gets the value of the noChangeShapeType property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNoChangeShapeType() {
        if (noChangeShapeType == null) {
            return false;
        } else {
            return noChangeShapeType;
        }
    }

    /**
     * Sets the value of the noChangeShapeType property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoChangeShapeType(Boolean value) {
        this.noChangeShapeType = value;
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
