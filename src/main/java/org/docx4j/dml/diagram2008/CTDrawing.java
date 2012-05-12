
package org.docx4j.dml.diagram2008;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
 *         &lt;element name="spTree" type="{http://schemas.microsoft.com/office/drawing/2008/diagram}CT_GroupShape"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Drawing", propOrder = {
    "spTree"
})
@XmlRootElement(name = "drawing")
public class CTDrawing {

    @XmlElement(required = true)
    protected CTGroupShape spTree;

    /**
     * Gets the value of the spTree property.
     * 
     * @return
     *     possible object is
     *     {@link CTGroupShape }
     *     
     */
    public CTGroupShape getSpTree() {
        return spTree;
    }

    /**
     * Sets the value of the spTree property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGroupShape }
     *     
     */
    public void setSpTree(CTGroupShape value) {
        this.spTree = value;
    }

}
