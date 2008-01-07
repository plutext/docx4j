
package org.docx4j.jaxb.document;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TcPrBase complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TcPrBase">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}tcW" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}hMerge" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}vMerge" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}tcBorders" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}shd" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}noWrap" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}vAlign" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TcPrBase", propOrder = {
    "tcW",
    "hMerge",
    "vMerge",
    "tcBorders",
    "shd",
    "noWrap",
    "vAlign"
})
public class TcPrBase
    implements Child
{

    protected TcW tcW;
    protected HMerge hMerge;
    protected VMerge vMerge;
    protected TcBorders tcBorders;
    protected Shd shd;
    protected OnOff noWrap;
    protected CTVerticalJc vAlign;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the tcW property.
     * 
     * @return
     *     possible object is
     *     {@link TcW }
     *     
     */
    public TcW getTcW() {
        return tcW;
    }

    /**
     * Sets the value of the tcW property.
     * 
     * @param value
     *     allowed object is
     *     {@link TcW }
     *     
     */
    public void setTcW(TcW value) {
        this.tcW = value;
    }

    /**
     * Gets the value of the hMerge property.
     * 
     * @return
     *     possible object is
     *     {@link HMerge }
     *     
     */
    public HMerge getHMerge() {
        return hMerge;
    }

    /**
     * Sets the value of the hMerge property.
     * 
     * @param value
     *     allowed object is
     *     {@link HMerge }
     *     
     */
    public void setHMerge(HMerge value) {
        this.hMerge = value;
    }

    /**
     * Gets the value of the vMerge property.
     * 
     * @return
     *     possible object is
     *     {@link VMerge }
     *     
     */
    public VMerge getVMerge() {
        return vMerge;
    }

    /**
     * Sets the value of the vMerge property.
     * 
     * @param value
     *     allowed object is
     *     {@link VMerge }
     *     
     */
    public void setVMerge(VMerge value) {
        this.vMerge = value;
    }

    /**
     * Gets the value of the tcBorders property.
     * 
     * @return
     *     possible object is
     *     {@link TcBorders }
     *     
     */
    public TcBorders getTcBorders() {
        return tcBorders;
    }

    /**
     * Sets the value of the tcBorders property.
     * 
     * @param value
     *     allowed object is
     *     {@link TcBorders }
     *     
     */
    public void setTcBorders(TcBorders value) {
        this.tcBorders = value;
    }

    /**
     * Gets the value of the shd property.
     * 
     * @return
     *     possible object is
     *     {@link Shd }
     *     
     */
    public Shd getShd() {
        return shd;
    }

    /**
     * Sets the value of the shd property.
     * 
     * @param value
     *     allowed object is
     *     {@link Shd }
     *     
     */
    public void setShd(Shd value) {
        this.shd = value;
    }

    /**
     * Gets the value of the noWrap property.
     * 
     * @return
     *     possible object is
     *     {@link OnOff }
     *     
     */
    public OnOff getNoWrap() {
        return noWrap;
    }

    /**
     * Sets the value of the noWrap property.
     * 
     * @param value
     *     allowed object is
     *     {@link OnOff }
     *     
     */
    public void setNoWrap(OnOff value) {
        this.noWrap = value;
    }

    /**
     * Gets the value of the vAlign property.
     * 
     * @return
     *     possible object is
     *     {@link CTVerticalJc }
     *     
     */
    public CTVerticalJc getVAlign() {
        return vAlign;
    }

    /**
     * Sets the value of the vAlign property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTVerticalJc }
     *     
     */
    public void setVAlign(CTVerticalJc value) {
        this.vAlign = value;
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
