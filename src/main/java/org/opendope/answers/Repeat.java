
package org.opendope.answers;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="row" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;choice maxOccurs="unbounded">
 *                     &lt;element ref="{http://opendope.org/answers}answer" minOccurs="0"/>
 *                     &lt;element ref="{http://opendope.org/answers}repeat" minOccurs="0"/>
 *                   &lt;/choice>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="qref" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "row"
})
@XmlRootElement(name = "repeat")
public class Repeat {

    @XmlElement(required = true)
    protected List<Repeat.Row> row;
    @XmlAttribute(required = true)
    protected String qref;

    /**
     * Gets the value of the row property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the row property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRow().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Repeat.Row }
     * 
     * 
     */
    public List<Repeat.Row> getRow() {
        if (row == null) {
            row = new ArrayList<Repeat.Row>();
        }
        return this.row;
    }

    /**
     * Gets the value of the qref property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQref() {
        return qref;
    }

    /**
     * Sets the value of the qref property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQref(String value) {
        this.qref = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;choice maxOccurs="unbounded">
     *           &lt;element ref="{http://opendope.org/answers}answer" minOccurs="0"/>
     *           &lt;element ref="{http://opendope.org/answers}repeat" minOccurs="0"/>
     *         &lt;/choice>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "answerOrRepeat"
    })
    public static class Row {

        @XmlElements({
            @XmlElement(name = "answer", type = Answer.class),
            @XmlElement(name = "repeat", type = Repeat.class)
        })
        protected List<Object> answerOrRepeat;

        /**
         * Gets the value of the answerOrRepeat property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the answerOrRepeat property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAnswerOrRepeat().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Answer }
         * {@link Repeat }
         * 
         * 
         */
        public List<Object> getAnswerOrRepeat() {
            if (answerOrRepeat == null) {
                answerOrRepeat = new ArrayList<Object>();
            }
            return this.answerOrRepeat;
        }

    }

}
