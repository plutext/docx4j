
package org.docx4j.org.w3.x2003.inkML;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.docx4j.org.w3.x1998.math.mathML.MathType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * http://www.w3.org/TR/InkML/#mappingElement
 * 
 * <p>Java class for mapping.type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="mapping.type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;sequence&gt;
 *           &lt;sequence&gt;
 *             &lt;element name="bind" type="{http://www.w3.org/2003/InkML}bind.type" maxOccurs="unbounded" minOccurs="0"/&gt;
 *             &lt;choice minOccurs="0"&gt;
 *               &lt;element name="table" type="{http://www.w3.org/2003/InkML}table.type"/&gt;
 *               &lt;element name="affine" type="{http://www.w3.org/2003/InkML}affine.type"/&gt;
 *               &lt;element ref="{http://www.w3.org/1998/Math/MathML}math"/&gt;
 *             &lt;/choice&gt;
 *           &lt;/sequence&gt;
 *           &lt;sequence&gt;
 *             &lt;element name="mapping" type="{http://www.w3.org/2003/InkML}mapping.type" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;/sequence&gt;
 *         &lt;/sequence&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}id"/&gt;
 *       &lt;attribute name="type" default="unknown"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="identity"/&gt;
 *             &lt;enumeration value="product"/&gt;
 *             &lt;enumeration value="table"/&gt;
 *             &lt;enumeration value="affine"/&gt;
 *             &lt;enumeration value="mathml"/&gt;
 *             &lt;enumeration value="unknown"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="mappingRef" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mapping.type", propOrder = {
    "content"
})
public class MappingType implements Child
{

    @XmlElementRefs({
        @XmlElementRef(name = "affine", namespace = "http://www.w3.org/2003/InkML", type = JAXBElement.class),
        @XmlElementRef(name = "bind", namespace = "http://www.w3.org/2003/InkML", type = JAXBElement.class),
        @XmlElementRef(name = "table", namespace = "http://www.w3.org/2003/InkML", type = JAXBElement.class),
        @XmlElementRef(name = "mapping", namespace = "http://www.w3.org/2003/InkML", type = JAXBElement.class),
        @XmlElementRef(name = "math", namespace = "http://www.w3.org/1998/Math/MathML", type = JAXBElement.class)
    })
    @XmlMixed
    protected List<Serializable> content;
    @XmlAttribute(name = "id", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "type")
    protected String type;
    @XmlAttribute(name = "mappingRef")
    @XmlSchemaType(name = "anyURI")
    protected String mappingRef;
    @XmlTransient
    private Object parent;

    /**
     * http://www.w3.org/TR/InkML/#mappingElement Gets the value of the content property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the content property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link AffineType }{@code >}
     * {@link JAXBElement }{@code <}{@link BindType }{@code >}
     * {@link JAXBElement }{@code <}{@link TableType }{@code >}
     * {@link String }
     * {@link JAXBElement }{@code <}{@link MappingType }{@code >}
     * {@link JAXBElement }{@code <}{@link MathType }{@code >}
     * 
     * 
     */
    public List<Serializable> getContent() {
        if (content == null) {
            content = new ArrayList<Serializable>();
        }
        return this.content;
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
            return "unknown";
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
     * Gets the value of the mappingRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMappingRef() {
        return mappingRef;
    }

    /**
     * Sets the value of the mappingRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMappingRef(String value) {
        this.mappingRef = value;
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
