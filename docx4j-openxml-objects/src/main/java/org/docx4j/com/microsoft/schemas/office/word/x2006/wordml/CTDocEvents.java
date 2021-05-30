
package org.docx4j.com.microsoft.schemas.office.word.x2006.wordml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_DocEvents complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DocEvents"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="eventDocNew" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *         &lt;element name="eventDocOpen" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *         &lt;element name="eventDocClose" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *         &lt;element name="eventDocSync" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *         &lt;element name="eventDocXmlAfterInsert" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *         &lt;element name="eventDocXmlBeforeDelete" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *         &lt;element name="eventDocContentControlAfterInsert" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *         &lt;element name="eventDocContentControlBeforeDelete" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *         &lt;element name="eventDocContentControlOnExit" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *         &lt;element name="eventDocContentControlOnEnter" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *         &lt;element name="eventDocStoreUpdate" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *         &lt;element name="eventDocContentControlContentUpdate" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *         &lt;element name="eventDocBuildingBlockAfterInsert" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DocEvents", propOrder = {
    "eventDocNew",
    "eventDocOpen",
    "eventDocClose",
    "eventDocSync",
    "eventDocXmlAfterInsert",
    "eventDocXmlBeforeDelete",
    "eventDocContentControlAfterInsert",
    "eventDocContentControlBeforeDelete",
    "eventDocContentControlOnExit",
    "eventDocContentControlOnEnter",
    "eventDocStoreUpdate",
    "eventDocContentControlContentUpdate",
    "eventDocBuildingBlockAfterInsert"
})
public class CTDocEvents implements Child
{

    protected Object eventDocNew;
    protected Object eventDocOpen;
    protected Object eventDocClose;
    protected Object eventDocSync;
    protected Object eventDocXmlAfterInsert;
    protected Object eventDocXmlBeforeDelete;
    protected Object eventDocContentControlAfterInsert;
    protected Object eventDocContentControlBeforeDelete;
    protected Object eventDocContentControlOnExit;
    protected Object eventDocContentControlOnEnter;
    protected Object eventDocStoreUpdate;
    protected Object eventDocContentControlContentUpdate;
    protected Object eventDocBuildingBlockAfterInsert;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the eventDocNew property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getEventDocNew() {
        return eventDocNew;
    }

    /**
     * Sets the value of the eventDocNew property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setEventDocNew(Object value) {
        this.eventDocNew = value;
    }

    /**
     * Gets the value of the eventDocOpen property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getEventDocOpen() {
        return eventDocOpen;
    }

    /**
     * Sets the value of the eventDocOpen property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setEventDocOpen(Object value) {
        this.eventDocOpen = value;
    }

    /**
     * Gets the value of the eventDocClose property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getEventDocClose() {
        return eventDocClose;
    }

    /**
     * Sets the value of the eventDocClose property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setEventDocClose(Object value) {
        this.eventDocClose = value;
    }

    /**
     * Gets the value of the eventDocSync property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getEventDocSync() {
        return eventDocSync;
    }

    /**
     * Sets the value of the eventDocSync property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setEventDocSync(Object value) {
        this.eventDocSync = value;
    }

    /**
     * Gets the value of the eventDocXmlAfterInsert property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getEventDocXmlAfterInsert() {
        return eventDocXmlAfterInsert;
    }

    /**
     * Sets the value of the eventDocXmlAfterInsert property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setEventDocXmlAfterInsert(Object value) {
        this.eventDocXmlAfterInsert = value;
    }

    /**
     * Gets the value of the eventDocXmlBeforeDelete property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getEventDocXmlBeforeDelete() {
        return eventDocXmlBeforeDelete;
    }

    /**
     * Sets the value of the eventDocXmlBeforeDelete property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setEventDocXmlBeforeDelete(Object value) {
        this.eventDocXmlBeforeDelete = value;
    }

    /**
     * Gets the value of the eventDocContentControlAfterInsert property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getEventDocContentControlAfterInsert() {
        return eventDocContentControlAfterInsert;
    }

    /**
     * Sets the value of the eventDocContentControlAfterInsert property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setEventDocContentControlAfterInsert(Object value) {
        this.eventDocContentControlAfterInsert = value;
    }

    /**
     * Gets the value of the eventDocContentControlBeforeDelete property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getEventDocContentControlBeforeDelete() {
        return eventDocContentControlBeforeDelete;
    }

    /**
     * Sets the value of the eventDocContentControlBeforeDelete property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setEventDocContentControlBeforeDelete(Object value) {
        this.eventDocContentControlBeforeDelete = value;
    }

    /**
     * Gets the value of the eventDocContentControlOnExit property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getEventDocContentControlOnExit() {
        return eventDocContentControlOnExit;
    }

    /**
     * Sets the value of the eventDocContentControlOnExit property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setEventDocContentControlOnExit(Object value) {
        this.eventDocContentControlOnExit = value;
    }

    /**
     * Gets the value of the eventDocContentControlOnEnter property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getEventDocContentControlOnEnter() {
        return eventDocContentControlOnEnter;
    }

    /**
     * Sets the value of the eventDocContentControlOnEnter property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setEventDocContentControlOnEnter(Object value) {
        this.eventDocContentControlOnEnter = value;
    }

    /**
     * Gets the value of the eventDocStoreUpdate property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getEventDocStoreUpdate() {
        return eventDocStoreUpdate;
    }

    /**
     * Sets the value of the eventDocStoreUpdate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setEventDocStoreUpdate(Object value) {
        this.eventDocStoreUpdate = value;
    }

    /**
     * Gets the value of the eventDocContentControlContentUpdate property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getEventDocContentControlContentUpdate() {
        return eventDocContentControlContentUpdate;
    }

    /**
     * Sets the value of the eventDocContentControlContentUpdate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setEventDocContentControlContentUpdate(Object value) {
        this.eventDocContentControlContentUpdate = value;
    }

    /**
     * Gets the value of the eventDocBuildingBlockAfterInsert property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getEventDocBuildingBlockAfterInsert() {
        return eventDocBuildingBlockAfterInsert;
    }

    /**
     * Sets the value of the eventDocBuildingBlockAfterInsert property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setEventDocBuildingBlockAfterInsert(Object value) {
        this.eventDocBuildingBlockAfterInsert = value;
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
