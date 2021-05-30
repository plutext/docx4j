
package org.docx4j.com.microsoft.schemas.office.word.x2006.wordml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Keymap complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Keymap"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice minOccurs="0"&gt;
 *         &lt;element name="fci" type="{http://schemas.microsoft.com/office/word/2006/wordml}CT_Fci"/&gt;
 *         &lt;element name="macro" type="{http://schemas.microsoft.com/office/word/2006/wordml}CT_MacroWll"/&gt;
 *         &lt;element name="acd" type="{http://schemas.microsoft.com/office/word/2006/wordml}CT_AcdKeymap"/&gt;
 *         &lt;element name="wll" type="{http://schemas.microsoft.com/office/word/2006/wordml}CT_MacroWll"/&gt;
 *         &lt;element name="wch" type="{http://schemas.microsoft.com/office/word/2006/wordml}CT_LongHexNumber"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="chmPrimary" type="{http://schemas.microsoft.com/office/word/2006/wordml}ST_ShortHexNumber" /&gt;
 *       &lt;attribute name="chmSecondary" type="{http://schemas.microsoft.com/office/word/2006/wordml}ST_ShortHexNumber" /&gt;
 *       &lt;attribute name="kcmPrimary" type="{http://schemas.microsoft.com/office/word/2006/wordml}ST_ShortHexNumber" /&gt;
 *       &lt;attribute name="kcmSecondary" type="{http://schemas.microsoft.com/office/word/2006/wordml}ST_ShortHexNumber" /&gt;
 *       &lt;attribute name="mask" type="{http://schemas.microsoft.com/office/word/2006/wordml}ST_OnOff" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Keymap", propOrder = {
    "fci",
    "macro",
    "acd",
    "wll",
    "wch"
})
public class CTKeymap implements Child
{

    protected CTFci fci;
    protected CTMacroWll macro;
    protected CTAcdKeymap acd;
    protected CTMacroWll wll;
    protected CTLongHexNumber wch;
    @XmlAttribute(name = "chmPrimary", namespace = "http://schemas.microsoft.com/office/word/2006/wordml")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] chmPrimary;
    @XmlAttribute(name = "chmSecondary", namespace = "http://schemas.microsoft.com/office/word/2006/wordml")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] chmSecondary;
    @XmlAttribute(name = "kcmPrimary", namespace = "http://schemas.microsoft.com/office/word/2006/wordml")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] kcmPrimary;
    @XmlAttribute(name = "kcmSecondary", namespace = "http://schemas.microsoft.com/office/word/2006/wordml")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] kcmSecondary;
    @XmlAttribute(name = "mask", namespace = "http://schemas.microsoft.com/office/word/2006/wordml")
    protected String mask;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the fci property.
     * 
     * @return
     *     possible object is
     *     {@link CTFci }
     *     
     */
    public CTFci getFci() {
        return fci;
    }

    /**
     * Sets the value of the fci property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFci }
     *     
     */
    public void setFci(CTFci value) {
        this.fci = value;
    }

    /**
     * Gets the value of the macro property.
     * 
     * @return
     *     possible object is
     *     {@link CTMacroWll }
     *     
     */
    public CTMacroWll getMacro() {
        return macro;
    }

    /**
     * Sets the value of the macro property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMacroWll }
     *     
     */
    public void setMacro(CTMacroWll value) {
        this.macro = value;
    }

    /**
     * Gets the value of the acd property.
     * 
     * @return
     *     possible object is
     *     {@link CTAcdKeymap }
     *     
     */
    public CTAcdKeymap getAcd() {
        return acd;
    }

    /**
     * Sets the value of the acd property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAcdKeymap }
     *     
     */
    public void setAcd(CTAcdKeymap value) {
        this.acd = value;
    }

    /**
     * Gets the value of the wll property.
     * 
     * @return
     *     possible object is
     *     {@link CTMacroWll }
     *     
     */
    public CTMacroWll getWll() {
        return wll;
    }

    /**
     * Sets the value of the wll property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMacroWll }
     *     
     */
    public void setWll(CTMacroWll value) {
        this.wll = value;
    }

    /**
     * Gets the value of the wch property.
     * 
     * @return
     *     possible object is
     *     {@link CTLongHexNumber }
     *     
     */
    public CTLongHexNumber getWch() {
        return wch;
    }

    /**
     * Sets the value of the wch property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLongHexNumber }
     *     
     */
    public void setWch(CTLongHexNumber value) {
        this.wch = value;
    }

    /**
     * Gets the value of the chmPrimary property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getChmPrimary() {
        return chmPrimary;
    }

    /**
     * Sets the value of the chmPrimary property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChmPrimary(byte[] value) {
        this.chmPrimary = value;
    }

    /**
     * Gets the value of the chmSecondary property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getChmSecondary() {
        return chmSecondary;
    }

    /**
     * Sets the value of the chmSecondary property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChmSecondary(byte[] value) {
        this.chmSecondary = value;
    }

    /**
     * Gets the value of the kcmPrimary property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getKcmPrimary() {
        return kcmPrimary;
    }

    /**
     * Sets the value of the kcmPrimary property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKcmPrimary(byte[] value) {
        this.kcmPrimary = value;
    }

    /**
     * Gets the value of the kcmSecondary property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getKcmSecondary() {
        return kcmSecondary;
    }

    /**
     * Sets the value of the kcmSecondary property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKcmSecondary(byte[] value) {
        this.kcmSecondary = value;
    }

    /**
     * Gets the value of the mask property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMask() {
        return mask;
    }

    /**
     * Sets the value of the mask property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMask(String value) {
        this.mask = value;
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
