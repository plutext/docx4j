
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2012.main;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTColor;
import org.pptx4j.pml.CTExtensionList;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.pptx4j.pml.STDirection;


/**
 * <p>Java class for CT_ExtendedGuide complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ExtendedGuide"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="clr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" /&gt;
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" default="" /&gt;
 *       &lt;attribute name="orient" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_Direction" default="vert" /&gt;
 *       &lt;attribute name="pos" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate32" default="0" /&gt;
 *       &lt;attribute name="userDrawn" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ExtendedGuide", propOrder = {
    "clr",
    "extLst"
})
public class CTExtendedGuide implements Child
{

    @XmlElement(required = true)
    protected CTColor clr;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "id", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long id;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "orient")
    protected STDirection orient;
    @XmlAttribute(name = "pos")
    protected Integer pos;
    @XmlAttribute(name = "userDrawn")
    protected Boolean userDrawn;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the clr property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getClr() {
        return clr;
    }

    /**
     * Sets the value of the clr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setClr(CTColor value) {
        this.clr = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtensionList }
     *     
     */
    public CTExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtensionList }
     *     
     */
    public void setExtLst(CTExtensionList value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the id property.
     * 
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(long value) {
        this.id = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        if (name == null) {
            return "";
        } else {
            return name;
        }
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the orient property.
     * 
     * @return
     *     possible object is
     *     {@link STDirection }
     *     
     */
    public STDirection getOrient() {
        if (orient == null) {
            return STDirection.VERT;
        } else {
            return orient;
        }
    }

    /**
     * Sets the value of the orient property.
     * 
     * @param value
     *     allowed object is
     *     {@link STDirection }
     *     
     */
    public void setOrient(STDirection value) {
        this.orient = value;
    }

    /**
     * Gets the value of the pos property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getPos() {
        if (pos == null) {
            return  0;
        } else {
            return pos;
        }
    }

    /**
     * Sets the value of the pos property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPos(Integer value) {
        this.pos = value;
    }

    /**
     * Gets the value of the userDrawn property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUserDrawn() {
        if (userDrawn == null) {
            return false;
        } else {
            return userDrawn;
        }
    }

    /**
     * Sets the value of the userDrawn property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUserDrawn(Boolean value) {
        this.userDrawn = value;
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
