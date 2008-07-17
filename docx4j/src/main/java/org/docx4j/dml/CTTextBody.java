
package org.docx4j.dml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TextBody complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TextBody">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bodyPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextBodyProperties"/>
 *         &lt;element name="lstStyle" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextListStyle" minOccurs="0"/>
 *         &lt;element name="p" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextParagraph" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TextBody", propOrder = {
    "bodyPr",
    "lstStyle",
    "p"
})
public class CTTextBody
    implements Child
{

    @XmlElement(required = true)
    protected CTTextBodyProperties bodyPr;
    protected CTTextListStyle lstStyle;
    @XmlElement(required = true)
    protected List<CTTextParagraph> p;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the bodyPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextBodyProperties }
     *     
     */
    public CTTextBodyProperties getBodyPr() {
        return bodyPr;
    }

    /**
     * Sets the value of the bodyPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextBodyProperties }
     *     
     */
    public void setBodyPr(CTTextBodyProperties value) {
        this.bodyPr = value;
    }

    /**
     * Gets the value of the lstStyle property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextListStyle }
     *     
     */
    public CTTextListStyle getLstStyle() {
        return lstStyle;
    }

    /**
     * Sets the value of the lstStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextListStyle }
     *     
     */
    public void setLstStyle(CTTextListStyle value) {
        this.lstStyle = value;
    }

    /**
     * Gets the value of the p property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the p property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getP().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTTextParagraph }
     * 
     * 
     */
    public List<CTTextParagraph> getP() {
        if (p == null) {
            p = new ArrayList<CTTextParagraph>();
        }
        return this.p;
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
