
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;


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
public class CTData {

    @XmlElements({
        @XmlElement(name = "numDim", type = CTNumericDimension.class),
        @XmlElement(name = "strDim", type = CTStringDimension.class)
    })
    protected List<Object> numDimOrStrDim;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "id", required = true)
    protected long id;

    /**
     * Gets the value of the numDimOrStrDim property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
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

}
