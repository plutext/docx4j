
package org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingDrawing;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_SizeRelV complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SizeRelV"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="pctHeight" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositivePercentage"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="relativeFrom" use="required" type="{http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing}ST_SizeRelFromV" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SizeRelV", propOrder = {
    "pctHeight"
})
public class CTSizeRelV implements Child
{

    protected int pctHeight;
    @XmlAttribute(name = "relativeFrom", required = true)
    protected STSizeRelFromV relativeFrom;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the pctHeight property.
     * 
     */
    public int getPctHeight() {
        return pctHeight;
    }

    /**
     * Sets the value of the pctHeight property.
     * 
     */
    public void setPctHeight(int value) {
        this.pctHeight = value;
    }

    /**
     * Gets the value of the relativeFrom property.
     * 
     * @return
     *     possible object is
     *     {@link STSizeRelFromV }
     *     
     */
    public STSizeRelFromV getRelativeFrom() {
        return relativeFrom;
    }

    /**
     * Sets the value of the relativeFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link STSizeRelFromV }
     *     
     */
    public void setRelativeFrom(STSizeRelFromV value) {
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
