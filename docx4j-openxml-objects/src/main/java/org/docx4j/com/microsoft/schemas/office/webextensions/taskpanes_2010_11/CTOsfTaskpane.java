
package org.docx4j.com.microsoft.schemas.office.webextensions.taskpanes_2010_11;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.docx4j.com.microsoft.schemas.office.webextensions.webextension_2010_11.CTWebExtensionPartRef;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_OsfTaskpane complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_OsfTaskpane"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="webextensionref" type="{http://schemas.microsoft.com/office/webextensions/webextension/2010/11}CT_WebExtensionPartRef"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="dockstate" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="visibility" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="width" use="required" type="{http://www.w3.org/2001/XMLSchema}double" /&gt;
 *       &lt;attribute name="row" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" /&gt;
 *       &lt;attribute name="locked" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_OsfTaskpane", propOrder = {
    "webextensionref",
    "extLst"
})
@XmlRootElement(name="taskpane")
public class CTOsfTaskpane implements Child
{

    @XmlElement(required = true)
    protected CTWebExtensionPartRef webextensionref;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "dockstate", required = true)
    protected String dockstate;
    @XmlAttribute(name = "visibility", required = true)
    protected boolean visibility;
    @XmlAttribute(name = "width", required = true)
    protected double width;
    @XmlAttribute(name = "row", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long row;
    @XmlAttribute(name = "locked")
    protected Boolean locked;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the webextensionref property.
     * 
     * @return
     *     possible object is
     *     {@link CTWebExtensionPartRef }
     *     
     */
    public CTWebExtensionPartRef getWebextensionref() {
        return webextensionref;
    }

    /**
     * Sets the value of the webextensionref property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTWebExtensionPartRef }
     *     
     */
    public void setWebextensionref(CTWebExtensionPartRef value) {
        this.webextensionref = value;
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
     * Gets the value of the dockstate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDockstate() {
        return dockstate;
    }

    /**
     * Sets the value of the dockstate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDockstate(String value) {
        this.dockstate = value;
    }

    /**
     * Gets the value of the visibility property.
     * 
     */
    public boolean isVisibility() {
        return visibility;
    }

    /**
     * Sets the value of the visibility property.
     * 
     */
    public void setVisibility(boolean value) {
        this.visibility = value;
    }

    /**
     * Gets the value of the width property.
     * 
     */
    public double getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     * 
     */
    public void setWidth(double value) {
        this.width = value;
    }

    /**
     * Gets the value of the row property.
     * 
     */
    public long getRow() {
        return row;
    }

    /**
     * Sets the value of the row property.
     * 
     */
    public void setRow(long value) {
        this.row = value;
    }

    /**
     * Gets the value of the locked property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLocked() {
        if (locked == null) {
            return false;
        } else {
            return locked;
        }
    }

    /**
     * Sets the value of the locked property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLocked(Boolean value) {
        this.locked = value;
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
