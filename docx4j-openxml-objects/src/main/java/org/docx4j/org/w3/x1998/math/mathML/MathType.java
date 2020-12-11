
package org.docx4j.org.w3.x1998.math.mathML;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for math.type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="math.type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;group ref="{http://www.w3.org/1998/Math/MathML}expression.content"/&gt;
 *       &lt;attGroup ref="{http://www.w3.org/1998/Math/MathML}common.attrib"/&gt;
 *       &lt;anyAttribute processContents='skip' namespace='##other'/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "math.type", propOrder = {
    "cn",
    "ci",
    "exponentiale",
    "pi",
    "_true",
    "_false",
    "apply",
    "logbase",
    "degree"
})
public class MathType implements Child
{

    protected CnType cn;
    protected CiType ci;
    protected ConstantType exponentiale;
    protected ConstantType pi;
    @XmlElement(name = "true")
    protected ConstantType _true;
    @XmlElement(name = "false")
    protected ConstantType _false;
    protected ApplyType apply;
    protected QualifierType logbase;
    protected QualifierType degree;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the cn property.
     * 
     * @return
     *     possible object is
     *     {@link CnType }
     *     
     */
    public CnType getCn() {
        return cn;
    }

    /**
     * Sets the value of the cn property.
     * 
     * @param value
     *     allowed object is
     *     {@link CnType }
     *     
     */
    public void setCn(CnType value) {
        this.cn = value;
    }

    /**
     * Gets the value of the ci property.
     * 
     * @return
     *     possible object is
     *     {@link CiType }
     *     
     */
    public CiType getCi() {
        return ci;
    }

    /**
     * Sets the value of the ci property.
     * 
     * @param value
     *     allowed object is
     *     {@link CiType }
     *     
     */
    public void setCi(CiType value) {
        this.ci = value;
    }

    /**
     * Gets the value of the exponentiale property.
     * 
     * @return
     *     possible object is
     *     {@link ConstantType }
     *     
     */
    public ConstantType getExponentiale() {
        return exponentiale;
    }

    /**
     * Sets the value of the exponentiale property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantType }
     *     
     */
    public void setExponentiale(ConstantType value) {
        this.exponentiale = value;
    }

    /**
     * Gets the value of the pi property.
     * 
     * @return
     *     possible object is
     *     {@link ConstantType }
     *     
     */
    public ConstantType getPi() {
        return pi;
    }

    /**
     * Sets the value of the pi property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantType }
     *     
     */
    public void setPi(ConstantType value) {
        this.pi = value;
    }

    /**
     * Gets the value of the true property.
     * 
     * @return
     *     possible object is
     *     {@link ConstantType }
     *     
     */
    public ConstantType getTrue() {
        return _true;
    }

    /**
     * Sets the value of the true property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantType }
     *     
     */
    public void setTrue(ConstantType value) {
        this._true = value;
    }

    /**
     * Gets the value of the false property.
     * 
     * @return
     *     possible object is
     *     {@link ConstantType }
     *     
     */
    public ConstantType getFalse() {
        return _false;
    }

    /**
     * Sets the value of the false property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantType }
     *     
     */
    public void setFalse(ConstantType value) {
        this._false = value;
    }

    /**
     * Gets the value of the apply property.
     * 
     * @return
     *     possible object is
     *     {@link ApplyType }
     *     
     */
    public ApplyType getApply() {
        return apply;
    }

    /**
     * Sets the value of the apply property.
     * 
     * @param value
     *     allowed object is
     *     {@link ApplyType }
     *     
     */
    public void setApply(ApplyType value) {
        this.apply = value;
    }

    /**
     * Gets the value of the logbase property.
     * 
     * @return
     *     possible object is
     *     {@link QualifierType }
     *     
     */
    public QualifierType getLogbase() {
        return logbase;
    }

    /**
     * Sets the value of the logbase property.
     * 
     * @param value
     *     allowed object is
     *     {@link QualifierType }
     *     
     */
    public void setLogbase(QualifierType value) {
        this.logbase = value;
    }

    /**
     * Gets the value of the degree property.
     * 
     * @return
     *     possible object is
     *     {@link QualifierType }
     *     
     */
    public QualifierType getDegree() {
        return degree;
    }

    /**
     * Sets the value of the degree property.
     * 
     * @param value
     *     allowed object is
     *     {@link QualifierType }
     *     
     */
    public void setDegree(QualifierType value) {
        this.degree = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
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
