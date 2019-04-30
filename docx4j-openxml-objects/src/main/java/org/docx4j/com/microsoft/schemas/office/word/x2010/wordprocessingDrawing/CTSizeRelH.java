
package org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingDrawing;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_SizeRelH complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SizeRelH"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="pctWidth" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositivePercentage"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="relativeFrom" use="required" type="{http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing}ST_SizeRelFromH" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SizeRelH", propOrder = {
    "pctWidth"
})
public class CTSizeRelH implements Child
{

    protected int pctWidth;
    @XmlAttribute(name = "relativeFrom", required = true)
    protected STSizeRelFromH relativeFrom;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the pctWidth property.
     * 
     */
    public int getPctWidth() {
        return pctWidth;
    }

    /**
     * Sets the value of the pctWidth property.
     * 
     */
    public void setPctWidth(int value) {
        this.pctWidth = value;
    }

    /**
     * Gets the value of the relativeFrom property.
     * 
     * @return
     *     possible object is
     *     {@link STSizeRelFromH }
     *     
     */
    public STSizeRelFromH getRelativeFrom() {
        return relativeFrom;
    }

    /**
     * Sets the value of the relativeFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link STSizeRelFromH }
     *     
     */
    public void setRelativeFrom(STSizeRelFromH value) {
        this.relativeFrom = value;
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
