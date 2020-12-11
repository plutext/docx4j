
package org.docx4j.com.microsoft.schemas.office.drawing.x201703.chart;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_DataDisplayOptions16 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DataDisplayOptions16"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="dispNaAsBlank" type="{http://schemas.microsoft.com/office/drawing/2017/03/chart}CT_BooleanFalse" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DataDisplayOptions16", propOrder = {
    "dispNaAsBlank"
})
public class CTDataDisplayOptions16 implements Child
{

    protected CTBooleanFalse dispNaAsBlank;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the dispNaAsBlank property.
     * 
     * @return
     *     possible object is
     *     {@link CTBooleanFalse }
     *     
     */
    public CTBooleanFalse getDispNaAsBlank() {
        return dispNaAsBlank;
    }

    /**
     * Sets the value of the dispNaAsBlank property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBooleanFalse }
     *     
     */
    public void setDispNaAsBlank(CTBooleanFalse value) {
        this.dispNaAsBlank = value;
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
