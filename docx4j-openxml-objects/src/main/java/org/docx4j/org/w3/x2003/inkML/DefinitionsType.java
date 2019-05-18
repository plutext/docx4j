
package org.docx4j.org.w3.x2003.inkML;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * http://www.w3.org/TR/InkML/#definitionsElement
 * 
 * <p>Java class for definitions.type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="definitions.type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="brush" type="{http://www.w3.org/2003/InkML}brush.type"/&gt;
 *         &lt;element name="canvas" type="{http://www.w3.org/2003/InkML}canvas.type"/&gt;
 *         &lt;element name="canvasTransform" type="{http://www.w3.org/2003/InkML}canvasTransform.type"/&gt;
 *         &lt;element name="context" type="{http://www.w3.org/2003/InkML}context.type"/&gt;
 *         &lt;element name="inkSource" type="{http://www.w3.org/2003/InkML}inkSource.type"/&gt;
 *         &lt;element name="mapping" type="{http://www.w3.org/2003/InkML}mapping.type"/&gt;
 *         &lt;element name="timestamp" type="{http://www.w3.org/2003/InkML}timestamp.type"/&gt;
 *         &lt;element name="trace" type="{http://www.w3.org/2003/InkML}trace.type"/&gt;
 *         &lt;element name="traceFormat" type="{http://www.w3.org/2003/InkML}traceFormat.type"/&gt;
 *         &lt;element name="traceGroup" type="{http://www.w3.org/2003/InkML}traceGroup.type"/&gt;
 *         &lt;element name="traceView" type="{http://www.w3.org/2003/InkML}traceView.type"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "definitions.type", propOrder = {
    "brushOrCanvasOrCanvasTransform"
})
public class DefinitionsType implements Child
{

    @XmlElements({
        @XmlElement(name = "brush", type = BrushType.class),
        @XmlElement(name = "canvas", type = CanvasType.class),
        @XmlElement(name = "canvasTransform", type = CanvasTransformType.class),
        @XmlElement(name = "context", type = ContextType.class),
        @XmlElement(name = "inkSource", type = InkSourceType.class),
        @XmlElement(name = "mapping", type = MappingType.class),
        @XmlElement(name = "timestamp", type = TimestampType.class),
        @XmlElement(name = "trace", type = TraceType.class),
        @XmlElement(name = "traceFormat", type = TraceFormatType.class),
        @XmlElement(name = "traceGroup", type = TraceGroupType.class),
        @XmlElement(name = "traceView", type = TraceViewType.class)
    })
    protected List<Object> brushOrCanvasOrCanvasTransform;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the brushOrCanvasOrCanvasTransform property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the brushOrCanvasOrCanvasTransform property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBrushOrCanvasOrCanvasTransform().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BrushType }
     * {@link CanvasType }
     * {@link CanvasTransformType }
     * {@link ContextType }
     * {@link InkSourceType }
     * {@link MappingType }
     * {@link TimestampType }
     * {@link TraceType }
     * {@link TraceFormatType }
     * {@link TraceGroupType }
     * {@link TraceViewType }
     * 
     * 
     */
    public List<Object> getBrushOrCanvasOrCanvasTransform() {
        if (brushOrCanvasOrCanvasTransform == null) {
            brushOrCanvasOrCanvasTransform = new ArrayList<Object>();
        }
        return this.brushOrCanvasOrCanvasTransform;
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
