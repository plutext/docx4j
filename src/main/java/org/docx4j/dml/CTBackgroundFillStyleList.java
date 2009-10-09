
package org.docx4j.dml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_BackgroundFillStyleList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_BackgroundFillStyleList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_FillProperties" maxOccurs="unbounded" minOccurs="3"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_BackgroundFillStyleList", propOrder = {
    "egFillProperties"
})
public class CTBackgroundFillStyleList
    implements Child
{

    @XmlElements({
        @XmlElement(name = "blipFill", type = CTBlipFillProperties.class),
        @XmlElement(name = "pattFill", type = CTPatternFillProperties.class),
        @XmlElement(name = "grpFill", type = CTGroupFillProperties.class),
        @XmlElement(name = "solidFill", type = CTSolidColorFillProperties.class),
        @XmlElement(name = "noFill", type = CTNoFillProperties.class),
        @XmlElement(name = "gradFill", type = CTGradientFillProperties.class)
    })
    protected List<Object> egFillProperties;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the egFillProperties property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the egFillProperties property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEGFillProperties().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTBlipFillProperties }
     * {@link CTPatternFillProperties }
     * {@link CTGroupFillProperties }
     * {@link CTSolidColorFillProperties }
     * {@link CTNoFillProperties }
     * {@link CTGradientFillProperties }
     * 
     * 
     */
    public List<Object> getEGFillProperties() {
        if (egFillProperties == null) {
            egFillProperties = new ArrayList<Object>();
        }
        return this.egFillProperties;
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
