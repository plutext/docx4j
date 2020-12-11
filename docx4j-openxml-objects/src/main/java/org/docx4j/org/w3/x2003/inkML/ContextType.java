
package org.docx4j.org.w3.x2003.inkML;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * http://www.w3.org/TR/InkML/#contextElement
 * 
 * <p>Java class for context.type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="context.type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="canvas" type="{http://www.w3.org/2003/InkML}canvas.type" minOccurs="0"/&gt;
 *         &lt;element name="canvasTransform" type="{http://www.w3.org/2003/InkML}canvasTransform.type" minOccurs="0"/&gt;
 *         &lt;element name="traceFormat" type="{http://www.w3.org/2003/InkML}traceFormat.type" minOccurs="0"/&gt;
 *         &lt;element name="inkSource" type="{http://www.w3.org/2003/InkML}inkSource.type" minOccurs="0"/&gt;
 *         &lt;element name="brush" type="{http://www.w3.org/2003/InkML}brush.type" minOccurs="0"/&gt;
 *         &lt;element name="timestamp" type="{http://www.w3.org/2003/InkML}timestamp.type" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}id"/&gt;
 *       &lt;attribute name="contextRef" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;attribute name="canvasRef" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;attribute name="canvasTransformRef" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;attribute name="traceFormatRef" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;attribute name="inkSourceRef" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;attribute name="brushRef" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;attribute name="timestampRef" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "context.type", propOrder = {
    "canvas",
    "canvasTransform",
    "traceFormat",
    "inkSource",
    "brush",
    "timestamp"
})
public class ContextType implements Child
{

    protected CanvasType canvas;
    protected CanvasTransformType canvasTransform;
    protected TraceFormatType traceFormat;
    protected InkSourceType inkSource;
    protected BrushType brush;
    protected TimestampType timestamp;
    @XmlAttribute(name = "id", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "contextRef")
    @XmlSchemaType(name = "anyURI")
    protected String contextRef;
    @XmlAttribute(name = "canvasRef")
    @XmlSchemaType(name = "anyURI")
    protected String canvasRef;
    @XmlAttribute(name = "canvasTransformRef")
    @XmlSchemaType(name = "anyURI")
    protected String canvasTransformRef;
    @XmlAttribute(name = "traceFormatRef")
    @XmlSchemaType(name = "anyURI")
    protected String traceFormatRef;
    @XmlAttribute(name = "inkSourceRef")
    @XmlSchemaType(name = "anyURI")
    protected String inkSourceRef;
    @XmlAttribute(name = "brushRef")
    @XmlSchemaType(name = "anyURI")
    protected String brushRef;
    @XmlAttribute(name = "timestampRef")
    @XmlSchemaType(name = "anyURI")
    protected String timestampRef;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the canvas property.
     * 
     * @return
     *     possible object is
     *     {@link CanvasType }
     *     
     */
    public CanvasType getCanvas() {
        return canvas;
    }

    /**
     * Sets the value of the canvas property.
     * 
     * @param value
     *     allowed object is
     *     {@link CanvasType }
     *     
     */
    public void setCanvas(CanvasType value) {
        this.canvas = value;
    }

    /**
     * Gets the value of the canvasTransform property.
     * 
     * @return
     *     possible object is
     *     {@link CanvasTransformType }
     *     
     */
    public CanvasTransformType getCanvasTransform() {
        return canvasTransform;
    }

    /**
     * Sets the value of the canvasTransform property.
     * 
     * @param value
     *     allowed object is
     *     {@link CanvasTransformType }
     *     
     */
    public void setCanvasTransform(CanvasTransformType value) {
        this.canvasTransform = value;
    }

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
     * Gets the value of the inkSource property.
     * 
     * @return
     *     possible object is
     *     {@link InkSourceType }
     *     
     */
    public InkSourceType getInkSource() {
        return inkSource;
    }

    /**
     * Sets the value of the inkSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link InkSourceType }
     *     
     */
    public void setInkSource(InkSourceType value) {
        this.inkSource = value;
    }

    /**
     * Gets the value of the brush property.
     * 
     * @return
     *     possible object is
     *     {@link BrushType }
     *     
     */
    public BrushType getBrush() {
        return brush;
    }

    /**
     * Sets the value of the brush property.
     * 
     * @param value
     *     allowed object is
     *     {@link BrushType }
     *     
     */
    public void setBrush(BrushType value) {
        this.brush = value;
    }

    /**
     * Gets the value of the timestamp property.
     * 
     * @return
     *     possible object is
     *     {@link TimestampType }
     *     
     */
    public TimestampType getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the value of the timestamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimestampType }
     *     
     */
    public void setTimestamp(TimestampType value) {
        this.timestamp = value;
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
     * Gets the value of the canvasRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCanvasRef() {
        return canvasRef;
    }

    /**
     * Sets the value of the canvasRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCanvasRef(String value) {
        this.canvasRef = value;
    }

    /**
     * Gets the value of the canvasTransformRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCanvasTransformRef() {
        return canvasTransformRef;
    }

    /**
     * Sets the value of the canvasTransformRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCanvasTransformRef(String value) {
        this.canvasTransformRef = value;
    }

    /**
     * Gets the value of the traceFormatRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTraceFormatRef() {
        return traceFormatRef;
    }

    /**
     * Sets the value of the traceFormatRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTraceFormatRef(String value) {
        this.traceFormatRef = value;
    }

    /**
     * Gets the value of the inkSourceRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInkSourceRef() {
        return inkSourceRef;
    }

    /**
     * Sets the value of the inkSourceRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInkSourceRef(String value) {
        this.inkSourceRef = value;
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
     * Gets the value of the timestampRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimestampRef() {
        return timestampRef;
    }

    /**
     * Sets the value of the timestampRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimestampRef(String value) {
        this.timestampRef = value;
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
