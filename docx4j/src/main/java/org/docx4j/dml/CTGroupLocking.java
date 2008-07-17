
package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_GroupLocking complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GroupLocking">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ext" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtension" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="noGrp" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="noUngrp" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="noSelect" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="noRot" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="noChangeAspect" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="noMove" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="noResize" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GroupLocking", propOrder = {
    "ext"
})
public class CTGroupLocking
    implements Child
{

    protected CTOfficeArtExtension ext;
    @XmlAttribute
    protected Boolean noGrp;
    @XmlAttribute
    protected Boolean noUngrp;
    @XmlAttribute
    protected Boolean noSelect;
    @XmlAttribute
    protected Boolean noRot;
    @XmlAttribute
    protected Boolean noChangeAspect;
    @XmlAttribute
    protected Boolean noMove;
    @XmlAttribute
    protected Boolean noResize;
    @XmlTransient
    private Object parent;

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
     * Gets the value of the noUngrp property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNoUngrp() {
        if (noUngrp == null) {
            return false;
        } else {
            return noUngrp;
        }
    }

    /**
     * Sets the value of the noUngrp property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoUngrp(Boolean value) {
        this.noUngrp = value;
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
