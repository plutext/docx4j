
package org.docx4j.com.microsoft.schemas.office.word.x2006.wordml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Tcg complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Tcg"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="keymaps" type="{http://schemas.microsoft.com/office/word/2006/wordml}CT_Keymaps" minOccurs="0"/&gt;
 *         &lt;element name="keymapsBad" type="{http://schemas.microsoft.com/office/word/2006/wordml}CT_Keymaps" minOccurs="0"/&gt;
 *         &lt;element name="toolbars" type="{http://schemas.microsoft.com/office/word/2006/wordml}CT_Toolbars" minOccurs="0"/&gt;
 *         &lt;element name="acds" type="{http://schemas.microsoft.com/office/word/2006/wordml}CT_Acds" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Tcg", propOrder = {
    "keymaps",
    "keymapsBad",
    "toolbars",
    "acds"
})
@XmlRootElement(name="tcg")
public class CTTcg implements Child
{

    protected CTKeymaps keymaps;
    protected CTKeymaps keymapsBad;
    protected CTToolbars toolbars;
    protected CTAcds acds;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the keymaps property.
     * 
     * @return
     *     possible object is
     *     {@link CTKeymaps }
     *     
     */
    public CTKeymaps getKeymaps() {
        return keymaps;
    }

    /**
     * Sets the value of the keymaps property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTKeymaps }
     *     
     */
    public void setKeymaps(CTKeymaps value) {
        this.keymaps = value;
    }

    /**
     * Gets the value of the keymapsBad property.
     * 
     * @return
     *     possible object is
     *     {@link CTKeymaps }
     *     
     */
    public CTKeymaps getKeymapsBad() {
        return keymapsBad;
    }

    /**
     * Sets the value of the keymapsBad property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTKeymaps }
     *     
     */
    public void setKeymapsBad(CTKeymaps value) {
        this.keymapsBad = value;
    }

    /**
     * Gets the value of the toolbars property.
     * 
     * @return
     *     possible object is
     *     {@link CTToolbars }
     *     
     */
    public CTToolbars getToolbars() {
        return toolbars;
    }

    /**
     * Sets the value of the toolbars property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTToolbars }
     *     
     */
    public void setToolbars(CTToolbars value) {
        this.toolbars = value;
    }

    /**
     * Gets the value of the acds property.
     * 
     * @return
     *     possible object is
     *     {@link CTAcds }
     *     
     */
    public CTAcds getAcds() {
        return acds;
    }

    /**
     * Sets the value of the acds property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAcds }
     *     
     */
    public void setAcds(CTAcds value) {
        this.acds = value;
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
