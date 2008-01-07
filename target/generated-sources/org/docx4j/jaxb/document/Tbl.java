
package org.docx4j.jaxb.document;

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
 * <p>Java class for CT_Tbl complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Tbl">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}tblPr"/>
 *         &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_ContentRowContent" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Tbl", propOrder = {
    "tblPr",
    "egContentRowContent"
})
public class Tbl
    implements Child
{

    @XmlElement(required = true)
    protected TblPr tblPr;
    @XmlElement(name = "tr")
    protected List<Row> egContentRowContent;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the tblPr property.
     * 
     * @return
     *     possible object is
     *     {@link TblPr }
     *     
     */
    public TblPr getTblPr() {
        return tblPr;
    }

    /**
     * Sets the value of the tblPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link TblPr }
     *     
     */
    public void setTblPr(TblPr value) {
        this.tblPr = value;
    }

    /**
     * Gets the value of the egContentRowContent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the egContentRowContent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEGContentRowContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Row }
     * 
     * 
     */
    public List<Row> getEGContentRowContent() {
        if (egContentRowContent == null) {
            egContentRowContent = new ArrayList<Row>();
        }
        return this.egContentRowContent;
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
