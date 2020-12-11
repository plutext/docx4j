
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chart;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PivotOptions16 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PivotOptions16"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="showExpandCollapseFieldButtons" type="{http://schemas.microsoft.com/office/drawing/2014/chart}CT_BooleanFalse" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PivotOptions16", propOrder = {
    "showExpandCollapseFieldButtons"
})
public class CTPivotOptions16 implements Child
{

    protected CTBooleanFalse showExpandCollapseFieldButtons;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the showExpandCollapseFieldButtons property.
     * 
     * @return
     *     possible object is
     *     {@link CTBooleanFalse }
     *     
     */
    public CTBooleanFalse getShowExpandCollapseFieldButtons() {
        return showExpandCollapseFieldButtons;
    }

    /**
     * Sets the value of the showExpandCollapseFieldButtons property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBooleanFalse }
     *     
     */
    public void setShowExpandCollapseFieldButtons(CTBooleanFalse value) {
        this.showExpandCollapseFieldButtons = value;
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
