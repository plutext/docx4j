
package org.docx4j.com.microsoft.schemas.ink.x2010.main;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_CtxNode complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CtxNode"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="property" type="{http://schemas.microsoft.com/ink/2010/main}CT_Property" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="sourceLink" type="{http://schemas.microsoft.com/ink/2010/main}CT_CtxLink" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="destinationLink" type="{http://schemas.microsoft.com/ink/2010/main}CT_CtxLink" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://schemas.microsoft.com/ink/2010/main}ST_Guid" /&gt;
 *       &lt;attribute name="type" use="required" type="{http://schemas.microsoft.com/ink/2010/main}ST_CtxNodeType" /&gt;
 *       &lt;attribute name="rotatedBoundingBox" type="{http://schemas.microsoft.com/ink/2010/main}ST_Points" /&gt;
 *       &lt;attribute name="alignmentLevel" type="{http://www.w3.org/2001/XMLSchema}int" default="0" /&gt;
 *       &lt;attribute name="contentType" type="{http://www.w3.org/2001/XMLSchema}int" default="0" /&gt;
 *       &lt;attribute name="ascender" type="{http://schemas.microsoft.com/ink/2010/main}ST_Points" default="0,0" /&gt;
 *       &lt;attribute name="descender" type="{http://schemas.microsoft.com/ink/2010/main}ST_Points" default="0,0" /&gt;
 *       &lt;attribute name="baseline" type="{http://schemas.microsoft.com/ink/2010/main}ST_Points" default="0,0" /&gt;
 *       &lt;attribute name="midline" type="{http://schemas.microsoft.com/ink/2010/main}ST_Points" default="0,0" /&gt;
 *       &lt;attribute name="customRecognizerId" type="{http://schemas.microsoft.com/ink/2010/main}ST_Guid" /&gt;
 *       &lt;attribute name="mathML" type="{http://www.w3.org/2001/XMLSchema}string" default="" /&gt;
 *       &lt;attribute name="mathStruct" type="{http://www.w3.org/2001/XMLSchema}string" default="" /&gt;
 *       &lt;attribute name="mathSymbol" type="{http://www.w3.org/2001/XMLSchema}string" default="" /&gt;
 *       &lt;attribute name="beginModifierType" type="{http://www.w3.org/2001/XMLSchema}string" default="" /&gt;
 *       &lt;attribute name="endModifierType" type="{http://www.w3.org/2001/XMLSchema}string" default="" /&gt;
 *       &lt;attribute name="rotationAngle" type="{http://www.w3.org/2001/XMLSchema}int" default="0" /&gt;
 *       &lt;attribute name="hotPoints" type="{http://schemas.microsoft.com/ink/2010/main}ST_Points" /&gt;
 *       &lt;attribute name="centroid" type="{http://schemas.microsoft.com/ink/2010/main}ST_Point" /&gt;
 *       &lt;attribute name="semanticType" type="{http://schemas.microsoft.com/ink/2010/main}ST_SemanticType" default="none" /&gt;
 *       &lt;attribute name="shapeName" type="{http://www.w3.org/2001/XMLSchema}string" default="" /&gt;
 *       &lt;attribute name="shapeGeometry" type="{http://schemas.microsoft.com/ink/2010/main}ST_Points" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CtxNode", propOrder = {
    "property",
    "sourceLink",
    "destinationLink"
})
public class CTCtxNode implements Child
{

    protected List<CTProperty> property;
    protected List<CTCtxLink> sourceLink;
    protected List<CTCtxLink> destinationLink;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String id;
    @XmlAttribute(name = "type", required = true)
    protected String type;
    @XmlAttribute(name = "rotatedBoundingBox")
    protected List<String> rotatedBoundingBox;
    @XmlAttribute(name = "alignmentLevel")
    protected Integer alignmentLevel;
    @XmlAttribute(name = "contentType")
    protected Integer contentType;
    @XmlAttribute(name = "ascender")
    protected List<String> ascender;
    @XmlAttribute(name = "descender")
    protected List<String> descender;
    @XmlAttribute(name = "baseline")
    protected List<String> baseline;
    @XmlAttribute(name = "midline")
    protected List<String> midline;
    @XmlAttribute(name = "customRecognizerId")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String customRecognizerId;
    @XmlAttribute(name = "mathML")
    protected String mathML;
    @XmlAttribute(name = "mathStruct")
    protected String mathStruct;
    @XmlAttribute(name = "mathSymbol")
    protected String mathSymbol;
    @XmlAttribute(name = "beginModifierType")
    protected String beginModifierType;
    @XmlAttribute(name = "endModifierType")
    protected String endModifierType;
    @XmlAttribute(name = "rotationAngle")
    protected Integer rotationAngle;
    @XmlAttribute(name = "hotPoints")
    protected List<String> hotPoints;
    @XmlAttribute(name = "centroid")
    protected String centroid;
    @XmlAttribute(name = "semanticType")
    protected String semanticType;
    @XmlAttribute(name = "shapeName")
    protected String shapeName;
    @XmlAttribute(name = "shapeGeometry")
    protected List<String> shapeGeometry;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the property property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the property property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTProperty }
     * 
     * 
     */
    public List<CTProperty> getProperty() {
        if (property == null) {
            property = new ArrayList<CTProperty>();
        }
        return this.property;
    }

    /**
     * Gets the value of the sourceLink property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sourceLink property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSourceLink().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTCtxLink }
     * 
     * 
     */
    public List<CTCtxLink> getSourceLink() {
        if (sourceLink == null) {
            sourceLink = new ArrayList<CTCtxLink>();
        }
        return this.sourceLink;
    }

    /**
     * Gets the value of the destinationLink property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the destinationLink property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDestinationLink().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTCtxLink }
     * 
     * 
     */
    public List<CTCtxLink> getDestinationLink() {
        if (destinationLink == null) {
            destinationLink = new ArrayList<CTCtxLink>();
        }
        return this.destinationLink;
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
        return type;
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
     * Gets the value of the rotatedBoundingBox property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rotatedBoundingBox property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRotatedBoundingBox().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getRotatedBoundingBox() {
        if (rotatedBoundingBox == null) {
            rotatedBoundingBox = new ArrayList<String>();
        }
        return this.rotatedBoundingBox;
    }

    /**
     * Gets the value of the alignmentLevel property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getAlignmentLevel() {
        if (alignmentLevel == null) {
            return  0;
        } else {
            return alignmentLevel;
        }
    }

    /**
     * Sets the value of the alignmentLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAlignmentLevel(Integer value) {
        this.alignmentLevel = value;
    }

    /**
     * Gets the value of the contentType property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getContentType() {
        if (contentType == null) {
            return  0;
        } else {
            return contentType;
        }
    }

    /**
     * Sets the value of the contentType property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setContentType(Integer value) {
        this.contentType = value;
    }

    /**
     * Gets the value of the ascender property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ascender property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAscender().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAscender() {
        if (ascender == null) {
            ascender = new ArrayList<String>();
        }
        return this.ascender;
    }

    /**
     * Gets the value of the descender property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the descender property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDescender().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getDescender() {
        if (descender == null) {
            descender = new ArrayList<String>();
        }
        return this.descender;
    }

    /**
     * Gets the value of the baseline property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the baseline property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBaseline().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getBaseline() {
        if (baseline == null) {
            baseline = new ArrayList<String>();
        }
        return this.baseline;
    }

    /**
     * Gets the value of the midline property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the midline property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMidline().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getMidline() {
        if (midline == null) {
            midline = new ArrayList<String>();
        }
        return this.midline;
    }

    /**
     * Gets the value of the customRecognizerId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomRecognizerId() {
        return customRecognizerId;
    }

    /**
     * Sets the value of the customRecognizerId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomRecognizerId(String value) {
        this.customRecognizerId = value;
    }

    /**
     * Gets the value of the mathML property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMathML() {
        if (mathML == null) {
            return "";
        } else {
            return mathML;
        }
    }

    /**
     * Sets the value of the mathML property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMathML(String value) {
        this.mathML = value;
    }

    /**
     * Gets the value of the mathStruct property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMathStruct() {
        if (mathStruct == null) {
            return "";
        } else {
            return mathStruct;
        }
    }

    /**
     * Sets the value of the mathStruct property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMathStruct(String value) {
        this.mathStruct = value;
    }

    /**
     * Gets the value of the mathSymbol property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMathSymbol() {
        if (mathSymbol == null) {
            return "";
        } else {
            return mathSymbol;
        }
    }

    /**
     * Sets the value of the mathSymbol property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMathSymbol(String value) {
        this.mathSymbol = value;
    }

    /**
     * Gets the value of the beginModifierType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBeginModifierType() {
        if (beginModifierType == null) {
            return "";
        } else {
            return beginModifierType;
        }
    }

    /**
     * Sets the value of the beginModifierType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBeginModifierType(String value) {
        this.beginModifierType = value;
    }

    /**
     * Gets the value of the endModifierType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEndModifierType() {
        if (endModifierType == null) {
            return "";
        } else {
            return endModifierType;
        }
    }

    /**
     * Sets the value of the endModifierType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndModifierType(String value) {
        this.endModifierType = value;
    }

    /**
     * Gets the value of the rotationAngle property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getRotationAngle() {
        if (rotationAngle == null) {
            return  0;
        } else {
            return rotationAngle;
        }
    }

    /**
     * Sets the value of the rotationAngle property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRotationAngle(Integer value) {
        this.rotationAngle = value;
    }

    /**
     * Gets the value of the hotPoints property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the hotPoints property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHotPoints().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getHotPoints() {
        if (hotPoints == null) {
            hotPoints = new ArrayList<String>();
        }
        return this.hotPoints;
    }

    /**
     * Gets the value of the centroid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCentroid() {
        return centroid;
    }

    /**
     * Sets the value of the centroid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCentroid(String value) {
        this.centroid = value;
    }

    /**
     * Gets the value of the semanticType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSemanticType() {
        if (semanticType == null) {
            return "none";
        } else {
            return semanticType;
        }
    }

    /**
     * Sets the value of the semanticType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSemanticType(String value) {
        this.semanticType = value;
    }

    /**
     * Gets the value of the shapeName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShapeName() {
        if (shapeName == null) {
            return "";
        } else {
            return shapeName;
        }
    }

    /**
     * Sets the value of the shapeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShapeName(String value) {
        this.shapeName = value;
    }

    /**
     * Gets the value of the shapeGeometry property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the shapeGeometry property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getShapeGeometry().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getShapeGeometry() {
        if (shapeGeometry == null) {
            shapeGeometry = new ArrayList<String>();
        }
        return this.shapeGeometry;
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
