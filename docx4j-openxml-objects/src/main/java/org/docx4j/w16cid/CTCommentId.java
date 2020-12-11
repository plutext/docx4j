
package org.docx4j.w16cid;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_CommentId complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CommentId"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="paraId" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_LongHexNumber" /&gt;
 *       &lt;attribute name="durableId" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_LongHexNumber" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * @since 3.3.5
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CommentId")
public class CTCommentId implements Child
{

    @XmlAttribute(name = "paraId", namespace = "http://schemas.microsoft.com/office/word/2016/wordml/cid")
    protected String paraId;
    @XmlAttribute(name = "durableId", namespace = "http://schemas.microsoft.com/office/word/2016/wordml/cid")
    protected String durableId;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the paraId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParaId() {
        return paraId;
    }

    /**
     * Sets the value of the paraId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParaId(String value) {
        this.paraId = value;
    }

    /**
     * Gets the value of the durableId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDurableId() {
        return durableId;
    }

    /**
     * Sets the value of the durableId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDurableId(String value) {
        this.durableId = value;
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
