
package org.docx4j.com.microsoft.schemas.office.powerpoint.x2014.inkAction;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.docx4j.org.w3.x2003.inkML.CTMatrix;
import org.docx4j.org.w3.x2003.inkML.TraceType;
import org.docx4j.org.w3.x2003.inkML.TraceViewType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ActionData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ActionData"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="transform" type="{http://www.w3.org/2003/InkML}CT_Matrix" minOccurs="0"/&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element ref="{http://www.w3.org/2003/InkML}trace"/&gt;
 *           &lt;element ref="{http://www.w3.org/2003/InkML}traceView"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}id"/&gt;
 *       &lt;attribute name="name" type="{http://schemas.microsoft.com/office/powerpoint/2014/inkAction}ST_DataName" default="stroke" /&gt;
 *       &lt;attribute name="ref" type="{http://www.w3.org/2001/XMLSchema}anyURI" default="" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ActionData", propOrder = {
    "transform",
    "traceOrTraceView"
})
public class CTActionData implements Child
{

    protected CTMatrix transform;
    @XmlElements({
        @XmlElement(name = "trace", namespace = "http://www.w3.org/2003/InkML", type = TraceType.class),
        @XmlElement(name = "traceView", namespace = "http://www.w3.org/2003/InkML", type = TraceViewType.class)
    })
    protected List<Object> traceOrTraceView;
    @XmlAttribute(name = "id", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "ref")
    @XmlSchemaType(name = "anyURI")
    protected String ref;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the transform property.
     * 
     * @return
     *     possible object is
     *     {@link CTMatrix }
     *     
     */
    public CTMatrix getTransform() {
        return transform;
    }

    /**
     * Sets the value of the transform property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMatrix }
     *     
     */
    public void setTransform(CTMatrix value) {
        this.transform = value;
    }

    /**
     * Gets the value of the traceOrTraceView property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the traceOrTraceView property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTraceOrTraceView().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TraceType }
     * {@link TraceViewType }
     * 
     * 
     */
    public List<Object> getTraceOrTraceView() {
        if (traceOrTraceView == null) {
            traceOrTraceView = new ArrayList<Object>();
        }
        return this.traceOrTraceView;
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
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        if (name == null) {
            return "stroke";
        } else {
            return name;
        }
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the ref property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRef() {
        if (ref == null) {
            return "";
        } else {
            return ref;
        }
    }

    /**
     * Sets the value of the ref property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRef(String value) {
        this.ref = value;
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
