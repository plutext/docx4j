
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2010.main;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.pptx4j.pml.STTransitionSideDirectionType;


/**
 * <p>Java class for CT_PrismTransition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PrismTransition"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="dir" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TransitionSideDirectionType" default="l" /&gt;
 *       &lt;attribute name="isContent" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="isInverted" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PrismTransition")
public class CTPrismTransition implements Child
{

    @XmlAttribute(name = "dir")
    protected STTransitionSideDirectionType dir;
    @XmlAttribute(name = "isContent")
    protected Boolean isContent;
    @XmlAttribute(name = "isInverted")
    protected Boolean isInverted;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the dir property.
     * 
     * @return
     *     possible object is
     *     {@link STTransitionSideDirectionType }
     *     
     */
    public STTransitionSideDirectionType getDir() {
        if (dir == null) {
            return STTransitionSideDirectionType.L;
        } else {
            return dir;
        }
    }

    /**
     * Sets the value of the dir property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTransitionSideDirectionType }
     *     
     */
    public void setDir(STTransitionSideDirectionType value) {
        this.dir = value;
    }

    /**
     * Gets the value of the isContent property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIsContent() {
        if (isContent == null) {
            return false;
        } else {
            return isContent;
        }
    }

    /**
     * Sets the value of the isContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsContent(Boolean value) {
        this.isContent = value;
    }

    /**
     * Gets the value of the isInverted property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIsInverted() {
        if (isInverted == null) {
            return false;
        } else {
            return isInverted;
        }
    }

    /**
     * Sets the value of the isInverted property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsInverted(Boolean value) {
        this.isInverted = value;
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
