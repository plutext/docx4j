
package org.docx4j.com.microsoft.schemas.office.webextensions.webextension_2010_11;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.docx4j.dml.CTBlip;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.jvnet.jaxb2_commons.ppp.Child;

/**
 * @author jharrop
 * @since 3.3.2
 */


/**
 * <p>Java class for CT_OsfWebExtension complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_OsfWebExtension"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="reference" type="{http://schemas.microsoft.com/office/webextensions/webextension/2010/11}CT_OsfWebExtensionReference"/&gt;
 *         &lt;element name="alternateReferences" type="{http://schemas.microsoft.com/office/webextensions/webextension/2010/11}CT_OsfWebExtensionReferenceList" minOccurs="0"/&gt;
 *         &lt;element name="properties" type="{http://schemas.microsoft.com/office/webextensions/webextension/2010/11}CT_OsfWebExtensionPropertyBag"/&gt;
 *         &lt;element name="bindings" type="{http://schemas.microsoft.com/office/webextensions/webextension/2010/11}CT_OsfWebExtensionBindingList"/&gt;
 *         &lt;element name="snapshot" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Blip" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="frozen" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_OsfWebExtension", propOrder = {
    "reference",
    "alternateReferences",
    "properties",
    "bindings",
    "snapshot",
    "extLst"
})
@XmlRootElement(name="webextension")
public class CTOsfWebExtension implements Child
{

    @XmlElement(required = true)
    protected CTOsfWebExtensionReference reference;
    protected CTOsfWebExtensionReferenceList alternateReferences;
    @XmlElement(required = true)
    protected CTOsfWebExtensionPropertyBag properties;
    @XmlElement(required = true)
    protected CTOsfWebExtensionBindingList bindings;
    protected CTBlip snapshot;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "frozen")
    protected Boolean frozen;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the reference property.
     * 
     * @return
     *     possible object is
     *     {@link CTOsfWebExtensionReference }
     *     
     */
    public CTOsfWebExtensionReference getReference() {
        return reference;
    }

    /**
     * Sets the value of the reference property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOsfWebExtensionReference }
     *     
     */
    public void setReference(CTOsfWebExtensionReference value) {
        this.reference = value;
    }

    /**
     * Gets the value of the alternateReferences property.
     * 
     * @return
     *     possible object is
     *     {@link CTOsfWebExtensionReferenceList }
     *     
     */
    public CTOsfWebExtensionReferenceList getAlternateReferences() {
        return alternateReferences;
    }

    /**
     * Sets the value of the alternateReferences property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOsfWebExtensionReferenceList }
     *     
     */
    public void setAlternateReferences(CTOsfWebExtensionReferenceList value) {
        this.alternateReferences = value;
    }

    /**
     * Gets the value of the properties property.
     * 
     * @return
     *     possible object is
     *     {@link CTOsfWebExtensionPropertyBag }
     *     
     */
    public CTOsfWebExtensionPropertyBag getProperties() {
        return properties;
    }

    /**
     * Sets the value of the properties property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOsfWebExtensionPropertyBag }
     *     
     */
    public void setProperties(CTOsfWebExtensionPropertyBag value) {
        this.properties = value;
    }

    /**
     * Gets the value of the bindings property.
     * 
     * @return
     *     possible object is
     *     {@link CTOsfWebExtensionBindingList }
     *     
     */
    public CTOsfWebExtensionBindingList getBindings() {
        return bindings;
    }

    /**
     * Sets the value of the bindings property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOsfWebExtensionBindingList }
     *     
     */
    public void setBindings(CTOsfWebExtensionBindingList value) {
        this.bindings = value;
    }

    /**
     * Gets the value of the snapshot property.
     * 
     * @return
     *     possible object is
     *     {@link CTBlip }
     *     
     */
    public CTBlip getSnapshot() {
        return snapshot;
    }

    /**
     * Sets the value of the snapshot property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBlip }
     *     
     */
    public void setSnapshot(CTBlip value) {
        this.snapshot = value;
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
     * Gets the value of the frozen property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFrozen() {
        if (frozen == null) {
            return false;
        } else {
            return frozen;
        }
    }

    /**
     * Sets the value of the frozen property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFrozen(Boolean value) {
        this.frozen = value;
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
