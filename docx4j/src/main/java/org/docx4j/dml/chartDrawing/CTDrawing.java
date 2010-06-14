
package org.docx4j.dml.chartDrawing;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_Drawing complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Drawing">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/chartDrawing}EG_Anchor" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace="http://schemas.openxmlformats.org/drawingml/2006/chartDrawing", name = "CT_Drawing", propOrder = {
    "egAnchor"
})
public class CTDrawing {

    @XmlElements({
        @XmlElement(name = "relSizeAnchor", type = CTRelSizeAnchor.class),
        @XmlElement(name = "absSizeAnchor", type = CTAbsSizeAnchor.class)
    })
    protected List<Object> egAnchor;

    /**
     * Gets the value of the egAnchor property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the egAnchor property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEGAnchor().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTRelSizeAnchor }
     * {@link CTAbsSizeAnchor }
     * 
     * 
     */
    public List<Object> getEGAnchor() {
        if (egAnchor == null) {
            egAnchor = new ArrayList<Object>();
        }
        return this.egAnchor;
    }

}
