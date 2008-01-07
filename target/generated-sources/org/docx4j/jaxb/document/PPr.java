
package org.docx4j.jaxb.document;

import java.math.BigInteger;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_PPrBase"/>
 *         &lt;sequence>
 *           &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}rPr" minOccurs="0"/>
 *           &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}sectPr" minOccurs="0"/>
 *         &lt;/sequence>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "pStyle",
    "keepNext",
    "keepLines",
    "pageBreakBefore",
    "numPr",
    "tabs",
    "spacing",
    "ind",
    "jc",
    "outlineLvl",
    "rPr",
    "sectPr"
})
public class PPr
    implements Child
{

    protected PPr.PStyle pStyle;
    protected BooleanDefaultTrue keepNext;
    protected BooleanDefaultTrue keepLines;
    @XmlElement(required = true)
    protected BooleanDefaultTrue pageBreakBefore;
    protected NumPr numPr;
    protected Tabs tabs;
    protected Spacing spacing;
    protected Ind ind;
    protected Jc jc;
    protected PPr.OutlineLvl outlineLvl;
    protected RPr rPr;
    protected SectPr sectPr;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the pStyle property.
     * 
     * @return
     *     possible object is
     *     {@link PPr.PStyle }
     *     
     */
    public PPr.PStyle getPStyle() {
        return pStyle;
    }

    /**
     * Sets the value of the pStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link PPr.PStyle }
     *     
     */
    public void setPStyle(PPr.PStyle value) {
        this.pStyle = value;
    }

    /**
     * Gets the value of the keepNext property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getKeepNext() {
        return keepNext;
    }

    /**
     * Sets the value of the keepNext property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setKeepNext(BooleanDefaultTrue value) {
        this.keepNext = value;
    }

    /**
     * Gets the value of the keepLines property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getKeepLines() {
        return keepLines;
    }

    /**
     * Sets the value of the keepLines property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setKeepLines(BooleanDefaultTrue value) {
        this.keepLines = value;
    }

    /**
     * Gets the value of the pageBreakBefore property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getPageBreakBefore() {
        return pageBreakBefore;
    }

    /**
     * Sets the value of the pageBreakBefore property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setPageBreakBefore(BooleanDefaultTrue value) {
        this.pageBreakBefore = value;
    }

    /**
     * Gets the value of the numPr property.
     * 
     * @return
     *     possible object is
     *     {@link NumPr }
     *     
     */
    public NumPr getNumPr() {
        return numPr;
    }

    /**
     * Sets the value of the numPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link NumPr }
     *     
     */
    public void setNumPr(NumPr value) {
        this.numPr = value;
    }

    /**
     * Gets the value of the tabs property.
     * 
     * @return
     *     possible object is
     *     {@link Tabs }
     *     
     */
    public Tabs getTabs() {
        return tabs;
    }

    /**
     * Sets the value of the tabs property.
     * 
     * @param value
     *     allowed object is
     *     {@link Tabs }
     *     
     */
    public void setTabs(Tabs value) {
        this.tabs = value;
    }

    /**
     * Gets the value of the spacing property.
     * 
     * @return
     *     possible object is
     *     {@link Spacing }
     *     
     */
    public Spacing getSpacing() {
        return spacing;
    }

    /**
     * Sets the value of the spacing property.
     * 
     * @param value
     *     allowed object is
     *     {@link Spacing }
     *     
     */
    public void setSpacing(Spacing value) {
        this.spacing = value;
    }

    /**
     * Gets the value of the ind property.
     * 
     * @return
     *     possible object is
     *     {@link Ind }
     *     
     */
    public Ind getInd() {
        return ind;
    }

    /**
     * Sets the value of the ind property.
     * 
     * @param value
     *     allowed object is
     *     {@link Ind }
     *     
     */
    public void setInd(Ind value) {
        this.ind = value;
    }

    /**
     * Gets the value of the jc property.
     * 
     * @return
     *     possible object is
     *     {@link Jc }
     *     
     */
    public Jc getJc() {
        return jc;
    }

    /**
     * Sets the value of the jc property.
     * 
     * @param value
     *     allowed object is
     *     {@link Jc }
     *     
     */
    public void setJc(Jc value) {
        this.jc = value;
    }

    /**
     * Gets the value of the outlineLvl property.
     * 
     * @return
     *     possible object is
     *     {@link PPr.OutlineLvl }
     *     
     */
    public PPr.OutlineLvl getOutlineLvl() {
        return outlineLvl;
    }

    /**
     * Sets the value of the outlineLvl property.
     * 
     * @param value
     *     allowed object is
     *     {@link PPr.OutlineLvl }
     *     
     */
    public void setOutlineLvl(PPr.OutlineLvl value) {
        this.outlineLvl = value;
    }

    /**
     * Gets the value of the rPr property.
     * 
     * @return
     *     possible object is
     *     {@link RPr }
     *     
     */
    public RPr getRPr() {
        return rPr;
    }

    /**
     * Sets the value of the rPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link RPr }
     *     
     */
    public void setRPr(RPr value) {
        this.rPr = value;
    }

    /**
     * Gets the value of the sectPr property.
     * 
     * @return
     *     possible object is
     *     {@link SectPr }
     *     
     */
    public SectPr getSectPr() {
        return sectPr;
    }

    /**
     * Sets the value of the sectPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link SectPr }
     *     
     */
    public void setSectPr(SectPr value) {
        this.sectPr = value;
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


    /**
     * Associated Outline
     * 							Level
     * 
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="val" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class OutlineLvl
        implements Child
    {

        @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
        protected BigInteger val;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the val property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getVal() {
            return val;
        }

        /**
         * Sets the value of the val property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setVal(BigInteger value) {
            this.val = value;
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class PStyle
        implements Child
    {

        @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected String val;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the val property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getVal() {
            return val;
        }

        /**
         * Sets the value of the val property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setVal(String value) {
            this.val = value;
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

}
