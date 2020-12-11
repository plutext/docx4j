
package org.docx4j.org.w3.x1998.math.mathML;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for apply.type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="apply.type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://www.w3.org/1998/Math/MathML}operator.content"/&gt;
 *         &lt;group ref="{http://www.w3.org/1998/Math/MathML}expression.content" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
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
@XmlType(name = "apply.type", propOrder = {
    "abs",
    "floor",
    "ceiling",
    "quotient",
    "divide",
    "rem",
    "minus",
    "plus",
    "times",
    "power",
    "root",
    "max",
    "min",
    "and",
    "or",
    "xor",
    "not",
    "exponentiale",
    "pi",
    "_true",
    "_false",
    "eq",
    "neq",
    "leq",
    "lt",
    "geq",
    "gt",
    "exp",
    "ln",
    "log",
    "sin",
    "cos",
    "tan",
    "arcsin",
    "arccos",
    "arctan",
    "expressionContent"
})
public class ApplyType implements Child
{

    protected OperatorType abs;
    protected OperatorType floor;
    protected OperatorType ceiling;
    protected OperatorType quotient;
    protected OperatorType divide;
    protected OperatorType rem;
    protected OperatorType minus;
    protected OperatorType plus;
    protected OperatorType times;
    protected OperatorType power;
    protected OperatorType root;
    protected OperatorType max;
    protected OperatorType min;
    protected OperatorType and;
    protected OperatorType or;
    protected OperatorType xor;
    protected OperatorType not;
    protected ConstantType exponentiale;
    protected ConstantType pi;
    @XmlElement(name = "true")
    protected ConstantType _true;
    @XmlElement(name = "false")
    protected ConstantType _false;
    protected OperatorType eq;
    protected OperatorType neq;
    protected OperatorType leq;
    protected OperatorType lt;
    protected OperatorType geq;
    protected OperatorType gt;
    protected OperatorType exp;
    protected OperatorType ln;
    protected OperatorType log;
    protected OperatorType sin;
    protected OperatorType cos;
    protected OperatorType tan;
    protected OperatorType arcsin;
    protected OperatorType arccos;
    protected OperatorType arctan;
    @XmlElementRefs({
        @XmlElementRef(name = "pi", namespace = "http://www.w3.org/1998/Math/MathML", type = JAXBElement.class),
        @XmlElementRef(name = "true", namespace = "http://www.w3.org/1998/Math/MathML", type = JAXBElement.class),
        @XmlElementRef(name = "logbase", namespace = "http://www.w3.org/1998/Math/MathML", type = JAXBElement.class),
        @XmlElementRef(name = "apply", namespace = "http://www.w3.org/1998/Math/MathML", type = JAXBElement.class),
        @XmlElementRef(name = "degree", namespace = "http://www.w3.org/1998/Math/MathML", type = JAXBElement.class),
        @XmlElementRef(name = "cn", namespace = "http://www.w3.org/1998/Math/MathML", type = JAXBElement.class),
        @XmlElementRef(name = "ci", namespace = "http://www.w3.org/1998/Math/MathML", type = JAXBElement.class),
        @XmlElementRef(name = "exponentiale", namespace = "http://www.w3.org/1998/Math/MathML", type = JAXBElement.class),
        @XmlElementRef(name = "false", namespace = "http://www.w3.org/1998/Math/MathML", type = JAXBElement.class)
    })
    protected List<JAXBElement<?>> expressionContent;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the abs property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getAbs() {
        return abs;
    }

    /**
     * Sets the value of the abs property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setAbs(OperatorType value) {
        this.abs = value;
    }

    /**
     * Gets the value of the floor property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getFloor() {
        return floor;
    }

    /**
     * Sets the value of the floor property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setFloor(OperatorType value) {
        this.floor = value;
    }

    /**
     * Gets the value of the ceiling property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getCeiling() {
        return ceiling;
    }

    /**
     * Sets the value of the ceiling property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setCeiling(OperatorType value) {
        this.ceiling = value;
    }

    /**
     * Gets the value of the quotient property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getQuotient() {
        return quotient;
    }

    /**
     * Sets the value of the quotient property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setQuotient(OperatorType value) {
        this.quotient = value;
    }

    /**
     * Gets the value of the divide property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getDivide() {
        return divide;
    }

    /**
     * Sets the value of the divide property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setDivide(OperatorType value) {
        this.divide = value;
    }

    /**
     * Gets the value of the rem property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getRem() {
        return rem;
    }

    /**
     * Sets the value of the rem property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setRem(OperatorType value) {
        this.rem = value;
    }

    /**
     * Gets the value of the minus property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getMinus() {
        return minus;
    }

    /**
     * Sets the value of the minus property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setMinus(OperatorType value) {
        this.minus = value;
    }

    /**
     * Gets the value of the plus property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getPlus() {
        return plus;
    }

    /**
     * Sets the value of the plus property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setPlus(OperatorType value) {
        this.plus = value;
    }

    /**
     * Gets the value of the times property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getTimes() {
        return times;
    }

    /**
     * Sets the value of the times property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setTimes(OperatorType value) {
        this.times = value;
    }

    /**
     * Gets the value of the power property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getPower() {
        return power;
    }

    /**
     * Sets the value of the power property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setPower(OperatorType value) {
        this.power = value;
    }

    /**
     * Gets the value of the root property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getRoot() {
        return root;
    }

    /**
     * Sets the value of the root property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setRoot(OperatorType value) {
        this.root = value;
    }

    /**
     * Gets the value of the max property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getMax() {
        return max;
    }

    /**
     * Sets the value of the max property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setMax(OperatorType value) {
        this.max = value;
    }

    /**
     * Gets the value of the min property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getMin() {
        return min;
    }

    /**
     * Sets the value of the min property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setMin(OperatorType value) {
        this.min = value;
    }

    /**
     * Gets the value of the and property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getAnd() {
        return and;
    }

    /**
     * Sets the value of the and property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setAnd(OperatorType value) {
        this.and = value;
    }

    /**
     * Gets the value of the or property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getOr() {
        return or;
    }

    /**
     * Sets the value of the or property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setOr(OperatorType value) {
        this.or = value;
    }

    /**
     * Gets the value of the xor property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getXor() {
        return xor;
    }

    /**
     * Sets the value of the xor property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setXor(OperatorType value) {
        this.xor = value;
    }

    /**
     * Gets the value of the not property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getNot() {
        return not;
    }

    /**
     * Sets the value of the not property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setNot(OperatorType value) {
        this.not = value;
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
     * Gets the value of the eq property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getEq() {
        return eq;
    }

    /**
     * Sets the value of the eq property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setEq(OperatorType value) {
        this.eq = value;
    }

    /**
     * Gets the value of the neq property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getNeq() {
        return neq;
    }

    /**
     * Sets the value of the neq property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setNeq(OperatorType value) {
        this.neq = value;
    }

    /**
     * Gets the value of the leq property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getLeq() {
        return leq;
    }

    /**
     * Sets the value of the leq property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setLeq(OperatorType value) {
        this.leq = value;
    }

    /**
     * Gets the value of the lt property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getLt() {
        return lt;
    }

    /**
     * Sets the value of the lt property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setLt(OperatorType value) {
        this.lt = value;
    }

    /**
     * Gets the value of the geq property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getGeq() {
        return geq;
    }

    /**
     * Sets the value of the geq property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setGeq(OperatorType value) {
        this.geq = value;
    }

    /**
     * Gets the value of the gt property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getGt() {
        return gt;
    }

    /**
     * Sets the value of the gt property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setGt(OperatorType value) {
        this.gt = value;
    }

    /**
     * Gets the value of the exp property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getExp() {
        return exp;
    }

    /**
     * Sets the value of the exp property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setExp(OperatorType value) {
        this.exp = value;
    }

    /**
     * Gets the value of the ln property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getLn() {
        return ln;
    }

    /**
     * Sets the value of the ln property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setLn(OperatorType value) {
        this.ln = value;
    }

    /**
     * Gets the value of the log property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getLog() {
        return log;
    }

    /**
     * Sets the value of the log property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setLog(OperatorType value) {
        this.log = value;
    }

    /**
     * Gets the value of the sin property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getSin() {
        return sin;
    }

    /**
     * Sets the value of the sin property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setSin(OperatorType value) {
        this.sin = value;
    }

    /**
     * Gets the value of the cos property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getCos() {
        return cos;
    }

    /**
     * Sets the value of the cos property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setCos(OperatorType value) {
        this.cos = value;
    }

    /**
     * Gets the value of the tan property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getTan() {
        return tan;
    }

    /**
     * Sets the value of the tan property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setTan(OperatorType value) {
        this.tan = value;
    }

    /**
     * Gets the value of the arcsin property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getArcsin() {
        return arcsin;
    }

    /**
     * Sets the value of the arcsin property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setArcsin(OperatorType value) {
        this.arcsin = value;
    }

    /**
     * Gets the value of the arccos property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getArccos() {
        return arccos;
    }

    /**
     * Sets the value of the arccos property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setArccos(OperatorType value) {
        this.arccos = value;
    }

    /**
     * Gets the value of the arctan property.
     * 
     * @return
     *     possible object is
     *     {@link OperatorType }
     *     
     */
    public OperatorType getArctan() {
        return arctan;
    }

    /**
     * Sets the value of the arctan property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperatorType }
     *     
     */
    public void setArctan(OperatorType value) {
        this.arctan = value;
    }

    /**
     * Gets the value of the expressionContent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the expressionContent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExpressionContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link ConstantType }{@code >}
     * {@link JAXBElement }{@code <}{@link ConstantType }{@code >}
     * {@link JAXBElement }{@code <}{@link QualifierType }{@code >}
     * {@link JAXBElement }{@code <}{@link ApplyType }{@code >}
     * {@link JAXBElement }{@code <}{@link QualifierType }{@code >}
     * {@link JAXBElement }{@code <}{@link CnType }{@code >}
     * {@link JAXBElement }{@code <}{@link CiType }{@code >}
     * {@link JAXBElement }{@code <}{@link ConstantType }{@code >}
     * {@link JAXBElement }{@code <}{@link ConstantType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getExpressionContent() {
        if (expressionContent == null) {
            expressionContent = new ArrayList<JAXBElement<?>>();
        }
        return this.expressionContent;
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
