
package org.docx4j.com.microsoft.schemas.office.word.x2006.wordml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_VbaSuppData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_VbaSuppData"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="docEvents" type="{http://schemas.microsoft.com/office/word/2006/wordml}CT_DocEvents" minOccurs="0"/&gt;
 *         &lt;element name="mcds" type="{http://schemas.microsoft.com/office/word/2006/wordml}CT_Mcds" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_VbaSuppData", propOrder = {
    "docEvents",
    "mcds"
})
public class CTVbaSuppData implements Child
{

    protected CTDocEvents docEvents;
    protected CTMcds mcds;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the docEvents property.
     * 
     * @return
     *     possible object is
     *     {@link CTDocEvents }
     *     
     */
    public CTDocEvents getDocEvents() {
        return docEvents;
    }

    /**
     * Sets the value of the docEvents property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDocEvents }
     *     
     */
    public void setDocEvents(CTDocEvents value) {
        this.docEvents = value;
    }

    /**
     * Gets the value of the mcds property.
     * 
     * @return
     *     possible object is
     *     {@link CTMcds }
     *     
     */
    public CTMcds getMcds() {
        return mcds;
    }

    /**
     * Sets the value of the mcds property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMcds }
     *     
     */
    public void setMcds(CTMcds value) {
        this.mcds = value;
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
