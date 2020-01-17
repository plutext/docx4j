
package org.docx4j.org.w3.x2003.inkML;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * http://www.w3.org/TR/InkML/#inkSourceElement
 * 
 * <p>Java class for inkSource.type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="inkSource.type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="traceFormat" type="{http://www.w3.org/2003/InkML}traceFormat.type"/&gt;
 *         &lt;element name="sampleRate" type="{http://www.w3.org/2003/InkML}sampleRate.type" minOccurs="0"/&gt;
 *         &lt;element name="latency" type="{http://www.w3.org/2003/InkML}latency.type" minOccurs="0"/&gt;
 *         &lt;element name="activeArea" type="{http://www.w3.org/2003/InkML}activeArea.type" minOccurs="0"/&gt;
 *         &lt;element name="sourceProperty" type="{http://www.w3.org/2003/InkML}sourceProperty.type" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="channelProperties" type="{http://www.w3.org/2003/InkML}channelProperties.type" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}id use="required""/&gt;
 *       &lt;attribute name="manufacturer" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="model" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="serialNo" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="specificationRef" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "inkSource.type", propOrder = {
    "traceFormat",
    "sampleRate",
    "latency",
    "activeArea",
    "sourceProperty",
    "channelProperties"
})
public class InkSourceType implements Child
{

    @XmlElement(required = true)
    protected TraceFormatType traceFormat;
    protected SampleRateType sampleRate;
    protected LatencyType latency;
    protected ActiveAreaType activeArea;
    protected List<SourcePropertyType> sourceProperty;
    protected ChannelPropertiesType channelProperties;
    @XmlAttribute(name = "id", namespace = "http://www.w3.org/XML/1998/namespace", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "manufacturer")
    protected String manufacturer;
    @XmlAttribute(name = "model")
    protected String model;
    @XmlAttribute(name = "serialNo")
    protected String serialNo;
    @XmlAttribute(name = "specificationRef")
    @XmlSchemaType(name = "anyURI")
    protected String specificationRef;
    @XmlAttribute(name = "description")
    protected String description;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the traceFormat property.
     * 
     * @return
     *     possible object is
     *     {@link TraceFormatType }
     *     
     */
    public TraceFormatType getTraceFormat() {
        return traceFormat;
    }

    /**
     * Sets the value of the traceFormat property.
     * 
     * @param value
     *     allowed object is
     *     {@link TraceFormatType }
     *     
     */
    public void setTraceFormat(TraceFormatType value) {
        this.traceFormat = value;
    }

    /**
     * Gets the value of the sampleRate property.
     * 
     * @return
     *     possible object is
     *     {@link SampleRateType }
     *     
     */
    public SampleRateType getSampleRate() {
        return sampleRate;
    }

    /**
     * Sets the value of the sampleRate property.
     * 
     * @param value
     *     allowed object is
     *     {@link SampleRateType }
     *     
     */
    public void setSampleRate(SampleRateType value) {
        this.sampleRate = value;
    }

    /**
     * Gets the value of the latency property.
     * 
     * @return
     *     possible object is
     *     {@link LatencyType }
     *     
     */
    public LatencyType getLatency() {
        return latency;
    }

    /**
     * Sets the value of the latency property.
     * 
     * @param value
     *     allowed object is
     *     {@link LatencyType }
     *     
     */
    public void setLatency(LatencyType value) {
        this.latency = value;
    }

    /**
     * Gets the value of the activeArea property.
     * 
     * @return
     *     possible object is
     *     {@link ActiveAreaType }
     *     
     */
    public ActiveAreaType getActiveArea() {
        return activeArea;
    }

    /**
     * Sets the value of the activeArea property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActiveAreaType }
     *     
     */
    public void setActiveArea(ActiveAreaType value) {
        this.activeArea = value;
    }

    /**
     * Gets the value of the sourceProperty property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sourceProperty property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSourceProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SourcePropertyType }
     * 
     * 
     */
    public List<SourcePropertyType> getSourceProperty() {
        if (sourceProperty == null) {
            sourceProperty = new ArrayList<SourcePropertyType>();
        }
        return this.sourceProperty;
    }

    /**
     * Gets the value of the channelProperties property.
     * 
     * @return
     *     possible object is
     *     {@link ChannelPropertiesType }
     *     
     */
    public ChannelPropertiesType getChannelProperties() {
        return channelProperties;
    }

    /**
     * Sets the value of the channelProperties property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChannelPropertiesType }
     *     
     */
    public void setChannelProperties(ChannelPropertiesType value) {
        this.channelProperties = value;
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
     * Gets the value of the manufacturer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getManufacturer() {
        return manufacturer;
    }

    /**
     * Sets the value of the manufacturer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setManufacturer(String value) {
        this.manufacturer = value;
    }

    /**
     * Gets the value of the model property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the value of the model property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModel(String value) {
        this.model = value;
    }

    /**
     * Gets the value of the serialNo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSerialNo() {
        return serialNo;
    }

    /**
     * Sets the value of the serialNo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSerialNo(String value) {
        this.serialNo = value;
    }

    /**
     * Gets the value of the specificationRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpecificationRef() {
        return specificationRef;
    }

    /**
     * Sets the value of the specificationRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpecificationRef(String value) {
        this.specificationRef = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
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
