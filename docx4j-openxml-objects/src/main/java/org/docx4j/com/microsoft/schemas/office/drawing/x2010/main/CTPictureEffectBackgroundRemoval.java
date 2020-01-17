
package org.docx4j.com.microsoft.schemas.office.drawing.x2010.main;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PictureEffectBackgroundRemoval complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PictureEffectBackgroundRemoval"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="foregroundMark" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectBackgroundRemovalForegroundMark" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="backgroundMark" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffectBackgroundRemovalBackgroundMark" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="t" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedPercentage" /&gt;
 *       &lt;attribute name="b" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedPercentage" /&gt;
 *       &lt;attribute name="l" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedPercentage" /&gt;
 *       &lt;attribute name="r" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedPercentage" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PictureEffectBackgroundRemoval", propOrder = {
    "foregroundMark",
    "backgroundMark"
})
public class CTPictureEffectBackgroundRemoval implements Child
{

    protected List<CTPictureEffectBackgroundRemovalForegroundMark> foregroundMark;
    protected List<CTPictureEffectBackgroundRemovalBackgroundMark> backgroundMark;
    @XmlAttribute(name = "t", required = true)
    protected int t;
    @XmlAttribute(name = "b", required = true)
    protected int b;
    @XmlAttribute(name = "l", required = true)
    protected int l;
    @XmlAttribute(name = "r", required = true)
    protected int r;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the foregroundMark property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the foregroundMark property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getForegroundMark().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTPictureEffectBackgroundRemovalForegroundMark }
     * 
     * 
     */
    public List<CTPictureEffectBackgroundRemovalForegroundMark> getForegroundMark() {
        if (foregroundMark == null) {
            foregroundMark = new ArrayList<CTPictureEffectBackgroundRemovalForegroundMark>();
        }
        return this.foregroundMark;
    }

    /**
     * Gets the value of the backgroundMark property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the backgroundMark property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBackgroundMark().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTPictureEffectBackgroundRemovalBackgroundMark }
     * 
     * 
     */
    public List<CTPictureEffectBackgroundRemovalBackgroundMark> getBackgroundMark() {
        if (backgroundMark == null) {
            backgroundMark = new ArrayList<CTPictureEffectBackgroundRemovalBackgroundMark>();
        }
        return this.backgroundMark;
    }

    /**
     * Gets the value of the t property.
     * 
     */
    public int getT() {
        return t;
    }

    /**
     * Sets the value of the t property.
     * 
     */
    public void setT(int value) {
        this.t = value;
    }

    /**
     * Gets the value of the b property.
     * 
     */
    public int getB() {
        return b;
    }

    /**
     * Sets the value of the b property.
     * 
     */
    public void setB(int value) {
        this.b = value;
    }

    /**
     * Gets the value of the l property.
     * 
     */
    public int getL() {
        return l;
    }

    /**
     * Sets the value of the l property.
     * 
     */
    public void setL(int value) {
        this.l = value;
    }

    /**
     * Gets the value of the r property.
     * 
     */
    public int getR() {
        return r;
    }

    /**
     * Sets the value of the r property.
     * 
     */
    public void setR(int value) {
        this.r = value;
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
