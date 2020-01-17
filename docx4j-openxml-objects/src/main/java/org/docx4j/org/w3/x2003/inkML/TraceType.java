
package org.docx4j.org.w3.x2003.inkML;

import java.math.BigInteger;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * http://www.w3.org/TR/InkML/#trace
 * 
 * <p>Java class for trace.type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="trace.type"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;http://www.w3.org/2003/InkML&gt;traceData.type"&gt;
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}id"/&gt;
 *       &lt;attribute name="type" default="penDown"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="penDown"/&gt;
 *             &lt;enumeration value="penUp"/&gt;
 *             &lt;enumeration value="indeterminate"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="continuation" default="begin"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="begin"/&gt;
 *             &lt;enumeration value="end"/&gt;
 *             &lt;enumeration value="middle"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="priorRef" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;attribute name="contextRef" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;attribute name="brushRef" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;attribute name="duration" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *       &lt;attribute name="timeOffset" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "trace.type", propOrder = {
    "value"
})
public class TraceType implements Child
{

    @XmlValue
    protected String value;
    @XmlAttribute(name = "id", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "type")
    protected String type;
    @XmlAttribute(name = "continuation")
    protected String continuation;
    @XmlAttribute(name = "priorRef")
    @XmlSchemaType(name = "anyURI")
    protected String priorRef;
    @XmlAttribute(name = "contextRef")
    @XmlSchemaType(name = "anyURI")
    protected String contextRef;
    @XmlAttribute(name = "brushRef")
    @XmlSchemaType(name = "anyURI")
    protected String brushRef;
    @XmlAttribute(name = "duration")
    protected BigInteger duration;
    @XmlAttribute(name = "timeOffset")
    protected BigInteger timeOffset;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        if (type == null) {
            return "penDown";
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the continuation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContinuation() {
        if (continuation == null) {
            return "begin";
        } else {
            return continuation;
        }
    }

    /**
     * Sets the value of the continuation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContinuation(String value) {
        this.continuation = value;
    }

    /**
     * Gets the value of the priorRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPriorRef() {
        return priorRef;
    }

    /**
     * Sets the value of the priorRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPriorRef(String value) {
        this.priorRef = value;
    }

    /**
     * Gets the value of the contextRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContextRef() {
        return contextRef;
    }

    /**
     * Sets the value of the contextRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContextRef(String value) {
        this.contextRef = value;
    }

    /**
     * Gets the value of the brushRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBrushRef() {
        return brushRef;
    }

    /**
     * Sets the value of the brushRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBrushRef(String value) {
        this.brushRef = value;
    }

    /**
     * Gets the value of the duration property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDuration() {
        return duration;
    }

    /**
     * Sets the value of the duration property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDuration(BigInteger value) {
        this.duration = value;
    }

    /**
     * Gets the value of the timeOffset property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTimeOffset() {
        return timeOffset;
    }

    /**
     * Sets the value of the timeOffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTimeOffset(BigInteger value) {
        this.timeOffset = value;
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
