
package org.xlsx4j.sml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Rst complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Rst">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="t" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Xstring_Whitespace" minOccurs="0"/>
 *         &lt;element name="r" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_RElt" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="rPh" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PhoneticRun" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="phoneticPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PhoneticPr" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Rst", propOrder = {
    "t",
    "r",
    "rPh",
    "phoneticPr"
})
public class CTRst implements Child
{

    protected CTXstringWhitespace t;
    protected List<CTRElt> r;
    protected List<CTPhoneticRun> rPh;
    protected CTPhoneticPr phoneticPr;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the t property.
     * 
     * @return
     *     possible object is
     *     {@link CTXstringWhitespace }
     *     
     */
    public CTXstringWhitespace getT() {
        return t;
    }

    /**
     * Sets the value of the t property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTXstringWhitespace }
     *     
     */
    public void setT(CTXstringWhitespace value) {
        this.t = value;
    }

    /**
     * Gets the value of the r property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the r property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getR().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTRElt }
     * 
     * 
     */
    public List<CTRElt> getR() {
        if (r == null) {
            r = new ArrayList<CTRElt>();
        }
        return this.r;
    }

    /**
     * Gets the value of the rPh property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rPh property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRPh().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTPhoneticRun }
     * 
     * 
     */
    public List<CTPhoneticRun> getRPh() {
        if (rPh == null) {
            rPh = new ArrayList<CTPhoneticRun>();
        }
        return this.rPh;
    }

    /**
     * Gets the value of the phoneticPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTPhoneticPr }
     *     
     */
    public CTPhoneticPr getPhoneticPr() {
        return phoneticPr;
    }

    /**
     * Sets the value of the phoneticPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPhoneticPr }
     *     
     */
    public void setPhoneticPr(CTPhoneticPr value) {
        this.phoneticPr = value;
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
