
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2010.main;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.pptx4j.pml.STTransitionInOutDirectionType;


/**
 * <p>Java class for CT_FlyThroughTransition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_FlyThroughTransition"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="dir" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TransitionInOutDirectionType" default="in" /&gt;
 *       &lt;attribute name="hasBounce" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_FlyThroughTransition")
public class CTFlyThroughTransition implements Child
{

    @XmlAttribute(name = "dir")
    protected STTransitionInOutDirectionType dir;
    @XmlAttribute(name = "hasBounce")
    protected Boolean hasBounce;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the dir property.
     * 
     * @return
     *     possible object is
     *     {@link STTransitionInOutDirectionType }
     *     
     */
    public STTransitionInOutDirectionType getDir() {
        if (dir == null) {
            return STTransitionInOutDirectionType.IN;
        } else {
            return dir;
        }
    }

    /**
     * Sets the value of the dir property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTransitionInOutDirectionType }
     *     
     */
    public void setDir(STTransitionInOutDirectionType value) {
        this.dir = value;
    }

    /**
     * Gets the value of the hasBounce property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHasBounce() {
        if (hasBounce == null) {
            return false;
        } else {
            return hasBounce;
        }
    }

    /**
     * Sets the value of the hasBounce property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHasBounce(Boolean value) {
        this.hasBounce = value;
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
