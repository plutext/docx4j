
package org.docx4j.org.w3.x2003.inkML;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * http://www.w3.org/TR/InkML/#inkElement
 * 
 * <p>Java class for ink.type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ink.type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="definitions" type="{http://www.w3.org/2003/InkML}definitions.type" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="context" type="{http://www.w3.org/2003/InkML}context.type" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="trace" type="{http://www.w3.org/2003/InkML}trace.type" maxOccurs="unbounded"/&gt;
 *         &lt;element name="traceGroup" type="{http://www.w3.org/2003/InkML}traceGroup.type" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="traceView" type="{http://www.w3.org/2003/InkML}traceView.type" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="annotation" type="{http://www.w3.org/2003/InkML}annotation.type" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="annotationXML" type="{http://www.w3.org/2003/InkML}annotationXML.type" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="documentID" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ink.type", propOrder = {
    "definitionsOrContextOrTrace"
})
@XmlRootElement(name = "ink")
public class InkType implements Child
{

    @XmlElements({
        @XmlElement(name = "definitions", type = DefinitionsType.class),
        @XmlElement(name = "context", type = ContextType.class),
        @XmlElement(name = "trace", type = TraceType.class),
        @XmlElement(name = "traceGroup", type = TraceGroupType.class),
        @XmlElement(name = "traceView", type = TraceViewType.class),
        @XmlElement(name = "annotation", type = AnnotationType.class),
        @XmlElement(name = "annotationXML", type = AnnotationXMLType.class)
    })
    protected List<Object> definitionsOrContextOrTrace;
    @XmlAttribute(name = "documentID")
    @XmlSchemaType(name = "anyURI")
    protected String documentID;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the definitionsOrContextOrTrace property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the definitionsOrContextOrTrace property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDefinitionsOrContextOrTrace().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DefinitionsType }
     * {@link ContextType }
     * {@link TraceType }
     * {@link TraceGroupType }
     * {@link TraceViewType }
     * {@link AnnotationType }
     * {@link AnnotationXMLType }
     * 
     * 
     */
    public List<Object> getDefinitionsOrContextOrTrace() {
        if (definitionsOrContextOrTrace == null) {
            definitionsOrContextOrTrace = new ArrayList<Object>();
        }
        return this.definitionsOrContextOrTrace;
    }

    /**
     * Gets the value of the documentID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocumentID() {
        return documentID;
    }

    /**
     * Sets the value of the documentID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocumentID(String value) {
        this.documentID = value;
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
