
package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Vector3D complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Vector3D">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="dx" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate" />
 *       &lt;attribute name="dy" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate" />
 *       &lt;attribute name="dz" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Vector3D")
public class CTVector3D
    implements Child
{

    @XmlAttribute(required = true)
    protected long dx;
    @XmlAttribute(required = true)
    protected long dy;
    @XmlAttribute(required = true)
    protected long dz;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the dx property.
     * 
     */
    public long getDx() {
        return dx;
    }

    /**
     * Sets the value of the dx property.
     * 
     */
    public void setDx(long value) {
        this.dx = value;
    }

    /**
     * Gets the value of the dy property.
     * 
     */
    public long getDy() {
        return dy;
    }

    /**
     * Sets the value of the dy property.
     * 
     */
    public void setDy(long value) {
        this.dy = value;
    }

    /**
     * Gets the value of the dz property.
     * 
     */
    public long getDz() {
        return dz;
    }

    /**
     * Sets the value of the dz property.
     * 
     */
    public void setDz(long value) {
        this.dz = value;
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
