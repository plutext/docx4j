
package org.opendope.answers;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
@XmlType(namespace = "http://opendope.org/answers", name = "", propOrder = {
    "answerOrRepeat"
})
@XmlRootElement(name = "answers")
public class Answers {

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
