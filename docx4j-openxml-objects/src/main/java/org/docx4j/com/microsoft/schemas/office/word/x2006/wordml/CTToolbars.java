
package org.docx4j.com.microsoft.schemas.office.word.x2006.wordml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Toolbars complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Toolbars"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="acdManifest" type="{http://schemas.microsoft.com/office/word/2006/wordml}CT_AcdManifest" minOccurs="0"/&gt;
 *         &lt;element name="toolbarData" type="{http://schemas.microsoft.com/office/word/2006/wordml}CT_Rel" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Toolbars", propOrder = {
    "acdManifest",
    "toolbarData"
})
public class CTToolbars implements Child
{

    protected CTAcdManifest acdManifest;
    protected CTRel toolbarData;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the acdManifest property.
     * 
     * @return
     *     possible object is
     *     {@link CTAcdManifest }
     *     
     */
    public CTAcdManifest getAcdManifest() {
        return acdManifest;
    }

    /**
     * Sets the value of the acdManifest property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAcdManifest }
     *     
     */
    public void setAcdManifest(CTAcdManifest value) {
        this.acdManifest = value;
    }

    /**
     * Gets the value of the toolbarData property.
     * 
     * @return
     *     possible object is
     *     {@link CTRel }
     *     
     */
    public CTRel getToolbarData() {
        return toolbarData;
    }

    /**
     * Sets the value of the toolbarData property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRel }
     *     
     */
    public void setToolbarData(CTRel value) {
        this.toolbarData = value;
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
