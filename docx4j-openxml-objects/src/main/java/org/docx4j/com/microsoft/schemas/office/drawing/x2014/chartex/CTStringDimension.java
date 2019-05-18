
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;;


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
public class CTStringDimension implements Child
{

    @XmlElementRefs({
        @XmlElementRef(name = "nf", namespace = "http://schemas.microsoft.com/office/drawing/2014/chartex", type = JAXBElement.class),
        @XmlElementRef(name = "lvl", namespace = "http://schemas.microsoft.com/office/drawing/2014/chartex", type = JAXBElement.class),
        @XmlElementRef(name = "f", namespace = "http://schemas.microsoft.com/office/drawing/2014/chartex", type = JAXBElement.class)
    })
    protected List<JAXBElement<?>> content;
    @XmlAttribute(name = "type", required = true)
    protected STStringDimensionType type;
    @XmlTransient
    private Object parent;

    /**
     * Gets the rest of the content model. 
     * 
     * <p>
     * You are getting this "catch-all" property because of the following reason: 
     * The field name "Lvl" is used by two different parts of a schema. See: 
     * line 127 of file:/bvols/@git/repos/docx4j/xsd/odrawxml/office_drawing_2014_chartex.xsd
     * line 125 of file:/bvols/@git/repos/docx4j/xsd/odrawxml/office_drawing_2014_chartex.xsd
     * <p>
     * To get rid of this property, apply a property customization to one 
     * of both of the following declarations to change their names: 
     * Gets the value of the content property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
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
     * {@link JAXBElement }{@code <}{@link CTStringLevel }{@code >}
     * {@link JAXBElement }{@code <}{@link CTFormula }{@code >}
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
