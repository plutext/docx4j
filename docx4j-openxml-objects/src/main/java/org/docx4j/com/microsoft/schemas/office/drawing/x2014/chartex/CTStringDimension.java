
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementRefs;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_StringDimension complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_StringDimension"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;sequence&gt;
 *           &lt;element name="f" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_Formula"/&gt;
 *           &lt;element name="nf" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_Formula" minOccurs="0"/&gt;
 *           &lt;element name="lvl" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_StringLevel" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;/sequence&gt;
 *         &lt;element name="lvl" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_StringLevel" maxOccurs="unbounded"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="type" use="required" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_StringDimensionType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_StringDimension", propOrder = {
    "content"
})
public class CTStringDimension {

    @XmlElementRefs({
        @XmlElementRef(name = "f", namespace = "http://schemas.microsoft.com/office/drawing/2014/chartex", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "nf", namespace = "http://schemas.microsoft.com/office/drawing/2014/chartex", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "lvl", namespace = "http://schemas.microsoft.com/office/drawing/2014/chartex", type = JAXBElement.class, required = false)
    })
    protected List<JAXBElement<?>> content;
    @XmlAttribute(name = "type", required = true)
    protected STStringDimensionType type;

    /**
     * Gets the rest of the content model. 
     * 
     * <p>
     * You are getting this "catch-all" property because of the following reason: 
     * The field name "Lvl" is used by two different parts of a schema. See: 
     * line 131 of file:/bvols/@git/repos/docx4j/xsd/odrawxml/office_drawing_2014_chartex.xsd
     * line 129 of file:/bvols/@git/repos/docx4j/xsd/odrawxml/office_drawing_2014_chartex.xsd
     * <p>
     * To get rid of this property, apply a property customization to one 
     * of both of the following declarations to change their names: 
     * Gets the value of the content property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the content property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link CTFormula }{@code >}
     * {@link JAXBElement }{@code <}{@link CTFormula }{@code >}
     * {@link JAXBElement }{@code <}{@link CTStringLevel }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getContent() {
        if (content == null) {
            content = new ArrayList<JAXBElement<?>>();
        }
        return this.content;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STStringDimensionType }
     *     
     */
    public STStringDimensionType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STStringDimensionType }
     *     
     */
    public void setType(STStringDimensionType value) {
        this.type = value;
    }

}
