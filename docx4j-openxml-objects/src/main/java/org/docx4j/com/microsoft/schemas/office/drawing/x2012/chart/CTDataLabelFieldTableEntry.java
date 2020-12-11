
package org.docx4j.com.microsoft.schemas.office.drawing.x2012.chart;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.chart.CTStrData;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_DataLabelFieldTableEntry complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DataLabelFieldTableEntry"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="txfldGUID" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="f" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="dlblFieldTableCache" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_StrData" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DataLabelFieldTableEntry", propOrder = {
    "txfldGUID",
    "f",
    "dlblFieldTableCache"
})
public class CTDataLabelFieldTableEntry implements Child
{

    @XmlElement(required = true)
    protected String txfldGUID;
    @XmlElement(required = true)
    protected String f;
    protected CTStrData dlblFieldTableCache;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the txfldGUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTxfldGUID() {
        return txfldGUID;
    }

    /**
     * Sets the value of the txfldGUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTxfldGUID(String value) {
        this.txfldGUID = value;
    }

    /**
     * Gets the value of the f property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getF() {
        return f;
    }

    /**
     * Sets the value of the f property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setF(String value) {
        this.f = value;
    }

    /**
     * Gets the value of the dlblFieldTableCache property.
     * 
     * @return
     *     possible object is
     *     {@link CTStrData }
     *     
     */
    public CTStrData getDlblFieldTableCache() {
        return dlblFieldTableCache;
    }

    /**
     * Sets the value of the dlblFieldTableCache property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStrData }
     *     
     */
    public void setDlblFieldTableCache(CTStrData value) {
        this.dlblFieldTableCache = value;
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
