
package org.docx4j.dml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_EffectContainer complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_EffectContainer">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_Effect" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;attribute name="type" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_EffectContainerType" default="sib" />
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}token" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_EffectContainer", propOrder = {
    "egEffect"
})
public class CTEffectContainer implements Child
{

    @XmlElements({
        @XmlElement(name = "duotone", type = CTDuotoneEffect.class),
        @XmlElement(name = "effect", type = CTEffectReference.class),
        @XmlElement(name = "lum", type = CTLuminanceEffect.class),
        @XmlElement(name = "alphaRepl", type = CTAlphaReplaceEffect.class),
        @XmlElement(name = "relOff", type = CTRelativeOffsetEffect.class),
        @XmlElement(name = "biLevel", type = CTBiLevelEffect.class),
        @XmlElement(name = "alphaInv", type = CTAlphaInverseEffect.class),
        @XmlElement(name = "alphaOutset", type = CTAlphaOutsetEffect.class),
        @XmlElement(name = "fillOverlay", type = CTFillOverlayEffect.class),
        @XmlElement(name = "hsl", type = CTHSLEffect.class),
        @XmlElement(name = "alphaFloor", type = CTAlphaFloorEffect.class),
        @XmlElement(name = "clrChange", type = CTColorChangeEffect.class),
        @XmlElement(name = "alphaMod", type = CTAlphaModulateEffect.class),
        @XmlElement(name = "innerShdw", type = CTInnerShadowEffect.class),
        @XmlElement(name = "alphaModFix", type = CTAlphaModulateFixedEffect.class),
        @XmlElement(name = "alphaCeiling", type = CTAlphaCeilingEffect.class),
        @XmlElement(name = "cont", type = CTEffectContainer.class),
        @XmlElement(name = "alphaBiLevel", type = CTAlphaBiLevelEffect.class),
        @XmlElement(name = "tint", type = CTTintEffect.class),
        @XmlElement(name = "outerShdw", type = CTOuterShadowEffect.class),
        @XmlElement(name = "xfrm", type = CTTransformEffect.class),
        @XmlElement(name = "blend", type = CTBlendEffect.class),
        @XmlElement(name = "softEdge", type = CTSoftEdgesEffect.class),
        @XmlElement(name = "reflection", type = CTReflectionEffect.class),
        @XmlElement(name = "fill", type = CTFillEffect.class),
        @XmlElement(name = "prstShdw", type = CTPresetShadowEffect.class),
        @XmlElement(name = "clrRepl", type = CTColorReplaceEffect.class),
        @XmlElement(name = "glow", type = CTGlowEffect.class),
        @XmlElement(name = "grayscl", type = CTGrayscaleEffect.class),
        @XmlElement(name = "blur", type = CTBlurEffect.class)
    })
    protected List<Object> egEffect;
    @XmlAttribute
    protected STEffectContainerType type;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String name;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the egEffect property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the egEffect property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEGEffect().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTDuotoneEffect }
     * {@link CTEffectReference }
     * {@link CTLuminanceEffect }
     * {@link CTAlphaReplaceEffect }
     * {@link CTRelativeOffsetEffect }
     * {@link CTBiLevelEffect }
     * {@link CTAlphaInverseEffect }
     * {@link CTAlphaOutsetEffect }
     * {@link CTFillOverlayEffect }
     * {@link CTHSLEffect }
     * {@link CTAlphaFloorEffect }
     * {@link CTColorChangeEffect }
     * {@link CTAlphaModulateEffect }
     * {@link CTInnerShadowEffect }
     * {@link CTAlphaModulateFixedEffect }
     * {@link CTAlphaCeilingEffect }
     * {@link CTEffectContainer }
     * {@link CTAlphaBiLevelEffect }
     * {@link CTTintEffect }
     * {@link CTOuterShadowEffect }
     * {@link CTTransformEffect }
     * {@link CTBlendEffect }
     * {@link CTSoftEdgesEffect }
     * {@link CTReflectionEffect }
     * {@link CTFillEffect }
     * {@link CTPresetShadowEffect }
     * {@link CTColorReplaceEffect }
     * {@link CTGlowEffect }
     * {@link CTGrayscaleEffect }
     * {@link CTBlurEffect }
     * 
     * 
     */
    public List<Object> getEGEffect() {
        if (egEffect == null) {
            egEffect = new ArrayList<Object>();
        }
        return this.egEffect;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STEffectContainerType }
     *     
     */
    public STEffectContainerType getType() {
        if (type == null) {
            return STEffectContainerType.SIB;
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STEffectContainerType }
     *     
     */
    public void setType(STEffectContainerType value) {
        this.type = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
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
