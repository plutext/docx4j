
package org.docx4j.org.w3.x2003.inkML;

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
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * http://www.w3.org/TR/InkML/#traceGroup
 * 
 * <p>Java class for traceGroup.type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="traceGroup.type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence maxOccurs="unbounded"&gt;
 *         &lt;element name="trace" type="{http://www.w3.org/2003/InkML}trace.type" minOccurs="0"/&gt;
 *         &lt;element name="traceGroup" type="{http://www.w3.org/2003/InkML}traceGroup.type" minOccurs="0"/&gt;
 *         &lt;element name="traceView" type="{http://www.w3.org/2003/InkML}traceView.type" minOccurs="0"/&gt;
 *         &lt;element name="annotation" type="{http://www.w3.org/2003/InkML}annotation.type" minOccurs="0"/&gt;
 *         &lt;element name="annotationXML" type="{http://www.w3.org/2003/InkML}annotationXML.type" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}id"/&gt;
 *       &lt;attribute name="contextRef" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;attribute name="brushRef" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "traceGroup.type", propOrder = {
    "traceAndTraceGroupAndTraceView"
})
public class TraceGroupType implements Child
{

    @XmlElements({
        @XmlElement(name = "trace", type = TraceType.class),
        @XmlElement(name = "traceGroup", type = TraceGroupType.class),
        @XmlElement(name = "traceView", type = TraceViewType.class),
        @XmlElement(name = "annotation", type = AnnotationType.class),
        @XmlElement(name = "annotationXML", type = AnnotationXMLType.class)
    })
    protected List<Object> traceAndTraceGroupAndTraceView;
    @XmlAttribute(name = "id", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "contextRef")
    @XmlSchemaType(name = "anyURI")
    protected String contextRef;
    @XmlAttribute(name = "brushRef")
    @XmlSchemaType(name = "anyURI")
    protected String brushRef;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the traceAndTraceGroupAndTraceView property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the traceAndTraceGroupAndTraceView property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTraceAndTraceGroupAndTraceView().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TraceType }
     * {@link TraceGroupType }
     * {@link TraceViewType }
     * {@link AnnotationType }
     * {@link AnnotationXMLType }
     * 
     * 
     */
    public List<Object> getTraceAndTraceGroupAndTraceView() {
        if (traceAndTraceGroupAndTraceView == null) {
            traceAndTraceGroupAndTraceView = new ArrayList<Object>();
        }
        return this.traceAndTraceGroupAndTraceView;
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
