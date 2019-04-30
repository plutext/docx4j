
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
 * <p>Java class for CT_NonVisualInkContentPartProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_NonVisualInkContentPartProperties"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="cpLocks" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_ContentPartLocking" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="isComment" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_NonVisualInkContentPartProperties", propOrder = {
    "cpLocks",
    "extLst"
})
public class CTNonVisualInkContentPartProperties implements Child
{

    protected CTContentPartLocking cpLocks;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "isComment")
    protected Boolean isComment;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the cpLocks property.
     * 
     * @return
     *     possible object is
     *     {@link CTContentPartLocking }
     *     
     */
    public CTContentPartLocking getCpLocks() {
        return cpLocks;
    }

    /**
     * Sets the value of the cpLocks property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTContentPartLocking }
     *     
     */
    public void setCpLocks(CTContentPartLocking value) {
        this.cpLocks = value;
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
     * Gets the value of the isComment property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIsComment() {
        if (isComment == null) {
            return true;
        } else {
            return isComment;
        }
    }

    /**
     * Sets the value of the isComment property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsComment(Boolean value) {
        this.isComment = value;
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
