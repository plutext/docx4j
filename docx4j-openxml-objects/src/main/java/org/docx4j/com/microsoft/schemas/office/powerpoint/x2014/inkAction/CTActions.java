
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
import org.docx4j.org.w3.x2003.inkML.DefinitionsType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Actions complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Actions"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.w3.org/2003/InkML}definitions" minOccurs="0"/&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="actionGroup" type="{http://schemas.microsoft.com/office/powerpoint/2014/inkAction}CT_ActionGroup"/&gt;
 *           &lt;element name="action" type="{http://schemas.microsoft.com/office/powerpoint/2014/inkAction}CT_Action"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}id"/&gt;
 *       &lt;attribute name="lengthUnit" use="required" type="{http://www.w3.org/2003/InkML}ST_StandardLengthUnits" /&gt;
 *       &lt;attribute name="timeUnit" use="required" type="{http://www.w3.org/2003/InkML}ST_StandardTimeUnits" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Actions", propOrder = {
    "definitions",
    "actionGroupOrAction"
})
public class CTActions implements Child
{

    @XmlElement(namespace = "http://www.w3.org/2003/InkML")
    protected DefinitionsType definitions;
    @XmlElements({
        @XmlElement(name = "actionGroup", type = CTActionGroup.class),
        @XmlElement(name = "action", type = CTAction.class)
    })
    protected List<Object> actionGroupOrAction;
    @XmlAttribute(name = "id", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "lengthUnit", required = true)
    protected String lengthUnit;
    @XmlAttribute(name = "timeUnit", required = true)
    protected String timeUnit;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the definitions property.
     * 
     * @return
     *     possible object is
     *     {@link DefinitionsType }
     *     
     */
    public DefinitionsType getDefinitions() {
        return definitions;
    }

    /**
     * Sets the value of the definitions property.
     * 
     * @param value
     *     allowed object is
     *     {@link DefinitionsType }
     *     
     */
    public void setDefinitions(DefinitionsType value) {
        this.definitions = value;
    }

    /**
     * Gets the value of the actionGroupOrAction property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the actionGroupOrAction property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getActionGroupOrAction().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTActionGroup }
     * {@link CTAction }
     * 
     * 
     */
    public List<Object> getActionGroupOrAction() {
        if (actionGroupOrAction == null) {
            actionGroupOrAction = new ArrayList<Object>();
        }
        return this.actionGroupOrAction;
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
     * Gets the value of the lengthUnit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLengthUnit() {
        return lengthUnit;
    }

    /**
     * Sets the value of the lengthUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLengthUnit(String value) {
        this.lengthUnit = value;
    }

    /**
     * Gets the value of the timeUnit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimeUnit() {
        return timeUnit;
    }

    /**
     * Sets the value of the timeUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimeUnit(String value) {
        this.timeUnit = value;
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
