
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;;


/**
 * <p>Java class for CT_Data complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Data"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice maxOccurs="unbounded"&gt;
 *           &lt;element name="numDim" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_NumericDimension"/&gt;
 *           &lt;element name="strDim" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_StringDimension"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="extLst" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_ExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" use="required" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_DataId" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Data", propOrder = {
    "numDimOrStrDim",
    "extLst"
})
public class CTData implements Child
{

    @XmlElements({
        @XmlElement(name = "numDim", type = CTNumericDimension.class),
        @XmlElement(name = "strDim", type = CTStringDimension.class)
    })
    protected List<Object> numDimOrStrDim;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "id", required = true)
    protected long id;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the numDimOrStrDim property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the numDimOrStrDim property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNumDimOrStrDim().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTNumericDimension }
     * {@link CTStringDimension }
     * 
     * 
     */
    public List<Object> getNumDimOrStrDim() {
        if (numDimOrStrDim == null) {
            numDimOrStrDim = new ArrayList<Object>();
        }
        return this.numDimOrStrDim;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtensionList }
     *     
     */
    public CTExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtensionList }
     *     
     */
    public void setExtLst(CTExtensionList value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the id property.
     * 
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(long value) {
        this.id = value;
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
