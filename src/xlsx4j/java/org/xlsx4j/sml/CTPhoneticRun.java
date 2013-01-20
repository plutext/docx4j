
package org.xlsx4j.sml;

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
 * <p>Java class for CT_PhoneticRun complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PhoneticRun">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="t" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Xstring_Whitespace"/>
 *       &lt;/sequence>
 *       &lt;attribute name="sb" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="eb" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PhoneticRun", propOrder = {
    "t"
})
public class CTPhoneticRun implements Child
{

    @XmlElement(required = true)
    protected CTXstringWhitespace t;
    @XmlAttribute(name = "sb", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long sb;
    @XmlAttribute(name = "eb", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long eb;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the t property.
     * 
     * @return
     *     possible object is
     *     {@link CTXstringWhitespace }
     *     
     */
    public CTXstringWhitespace getT() {
        return t;
    }

    /**
     * Sets the value of the t property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTXstringWhitespace }
     *     
     */
    public void setT(CTXstringWhitespace value) {
        this.t = value;
    }

    /**
     * Gets the value of the sb property.
     * 
     */
    public long getSb() {
        return sb;
    }

    /**
     * Sets the value of the sb property.
     * 
     */
    public void setSb(long value) {
        this.sb = value;
    }

    /**
     * Gets the value of the eb property.
     * 
     */
    public long getEb() {
        return eb;
    }

    /**
     * Sets the value of the eb property.
     * 
     */
    public void setEb(long value) {
        this.eb = value;
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
