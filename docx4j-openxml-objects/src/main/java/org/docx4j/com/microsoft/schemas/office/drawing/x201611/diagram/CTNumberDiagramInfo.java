
package org.docx4j.com.microsoft.schemas.office.drawing.x201611.diagram;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_NumberDiagramInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_NumberDiagramInfo"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="buPr" type="{http://schemas.microsoft.com/office/drawing/2016/11/diagram}CT_DiagramAutoBullet"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="lvl" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" /&gt;
 *       &lt;attribute name="ptType" use="required" type="{http://schemas.microsoft.com/office/drawing/2016/11/diagram}ST_STorageType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_NumberDiagramInfo", propOrder = {
    "buPr"
})
public class CTNumberDiagramInfo implements Child
{

    @XmlElement(required = true)
    protected CTDiagramAutoBullet buPr;
    @XmlAttribute(name = "lvl", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long lvl;
    @XmlAttribute(name = "ptType", required = true)
    protected STSTorageType ptType;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the buPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTDiagramAutoBullet }
     *     
     */
    public CTDiagramAutoBullet getBuPr() {
        return buPr;
    }

    /**
     * Sets the value of the buPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDiagramAutoBullet }
     *     
     */
    public void setBuPr(CTDiagramAutoBullet value) {
        this.buPr = value;
    }

    /**
     * Gets the value of the lvl property.
     * 
     */
    public long getLvl() {
        return lvl;
    }

    /**
     * Sets the value of the lvl property.
     * 
     */
    public void setLvl(long value) {
        this.lvl = value;
    }

    /**
     * Gets the value of the ptType property.
     * 
     * @return
     *     possible object is
     *     {@link STSTorageType }
     *     
     */
    public STSTorageType getPtType() {
        return ptType;
    }

    /**
     * Sets the value of the ptType property.
     * 
     * @param value
     *     allowed object is
     *     {@link STSTorageType }
     *     
     */
    public void setPtType(STSTorageType value) {
        this.ptType = value;
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
