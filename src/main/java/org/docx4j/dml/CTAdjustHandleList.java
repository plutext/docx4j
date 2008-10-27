
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
 * <p>Java class for CT_AdjustHandleList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_AdjustHandleList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="ahXY" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_XYAdjustHandle"/>
 *         &lt;element name="ahPolar" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_PolarAdjustHandle"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_AdjustHandleList", propOrder = {
    "ahXYOrAhPolar"
})
public class CTAdjustHandleList
    implements Child
{

    @XmlElements({
        @XmlElement(name = "ahPolar", type = CTPolarAdjustHandle.class),
        @XmlElement(name = "ahXY", type = CTXYAdjustHandle.class)
    })
    protected List<Object> ahXYOrAhPolar;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the ahXYOrAhPolar property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ahXYOrAhPolar property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAhXYOrAhPolar().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTPolarAdjustHandle }
     * {@link CTXYAdjustHandle }
     * 
     * 
     */
    public List<Object> getAhXYOrAhPolar() {
        if (ahXYOrAhPolar == null) {
            ahXYOrAhPolar = new ArrayList<Object>();
        }
        return this.ahXYOrAhPolar;
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
