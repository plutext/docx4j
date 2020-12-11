
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2012.main;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_CommentThreading complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CommentThreading"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="parentCm" type="{http://schemas.microsoft.com/office/powerpoint/2012/main}CT_ParentCommentIdentifier" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="timeZoneBias" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CommentThreading", propOrder = {
    "parentCm"
})
public class CTCommentThreading implements Child
{

    protected CTParentCommentIdentifier parentCm;
    @XmlAttribute(name = "timeZoneBias")
    protected Integer timeZoneBias;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the parentCm property.
     * 
     * @return
     *     possible object is
     *     {@link CTParentCommentIdentifier }
     *     
     */
    public CTParentCommentIdentifier getParentCm() {
        return parentCm;
    }

    /**
     * Sets the value of the parentCm property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTParentCommentIdentifier }
     *     
     */
    public void setParentCm(CTParentCommentIdentifier value) {
        this.parentCm = value;
    }

    /**
     * Gets the value of the timeZoneBias property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTimeZoneBias() {
        return timeZoneBias;
    }

    /**
     * Sets the value of the timeZoneBias property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTimeZoneBias(Integer value) {
        this.timeZoneBias = value;
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
