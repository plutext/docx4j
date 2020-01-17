
package org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_MarkerLayout complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_MarkerLayout"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="symbol" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}ST_MarkerStyle" /&gt;
 *       &lt;attribute name="size" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}ST_MarkerSize" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_MarkerLayout")
public class CTMarkerLayout implements Child
{

    @XmlAttribute(name = "symbol")
    protected STMarkerStyle symbol;
    @XmlAttribute(name = "size")
    protected Short size;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the symbol property.
     * 
     * @return
     *     possible object is
     *     {@link STMarkerStyle }
     *     
     */
    public STMarkerStyle getSymbol() {
        return symbol;
    }

    /**
     * Sets the value of the symbol property.
     * 
     * @param value
     *     allowed object is
     *     {@link STMarkerStyle }
     *     
     */
    public void setSymbol(STMarkerStyle value) {
        this.symbol = value;
    }

    /**
     * Gets the value of the size property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getSize() {
        return size;
    }

    /**
     * Sets the value of the size property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setSize(Short value) {
        this.size = value;
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
