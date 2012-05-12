
package org.docx4j.bibliography;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_PersonType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PersonType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Last" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="First" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Middle" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PersonType", propOrder = {
    "last",
    "first",
    "middle"
})
public class CTPersonType {

    @XmlElement(name = "Last")
    protected List<String> last;
    @XmlElement(name = "First")
    protected List<String> first;
    @XmlElement(name = "Middle")
    protected List<String> middle;

    /**
     * Gets the value of the last property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the last property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLast().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getLast() {
        if (last == null) {
            last = new ArrayList<String>();
        }
        return this.last;
    }

    /**
     * Gets the value of the first property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the first property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFirst().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getFirst() {
        if (first == null) {
            first = new ArrayList<String>();
        }
        return this.first;
    }

    /**
     * Gets the value of the middle property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the middle property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMiddle().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getMiddle() {
        if (middle == null) {
            middle = new ArrayList<String>();
        }
        return this.middle;
    }

}
