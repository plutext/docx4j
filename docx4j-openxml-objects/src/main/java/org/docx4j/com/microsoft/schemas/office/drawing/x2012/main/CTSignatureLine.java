
package org.docx4j.com.microsoft.schemas.office.drawing.x2012.main;

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
 * <p>Java class for CT_SignatureLine complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SignatureLine"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="isSignatureLine" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="id" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Guid" /&gt;
 *       &lt;attribute name="provId" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Guid" /&gt;
 *       &lt;attribute name="signingInstructionsSet" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="allowComments" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="showSignDate" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="suggestedSigner" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="suggestedSigner2" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="suggestedSignerEmail" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="signingInstructions" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="addlXml" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="sigProvUrl" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SignatureLine")
public class CTSignatureLine implements Child
{

    @XmlAttribute(name = "isSignatureLine")
    protected Boolean isSignatureLine;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String id;
    @XmlAttribute(name = "provId")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String provId;
    @XmlAttribute(name = "signingInstructionsSet")
    protected Boolean signingInstructionsSet;
    @XmlAttribute(name = "allowComments")
    protected Boolean allowComments;
    @XmlAttribute(name = "showSignDate")
    protected Boolean showSignDate;
    @XmlAttribute(name = "suggestedSigner")
    protected String suggestedSigner;
    @XmlAttribute(name = "suggestedSigner2")
    protected String suggestedSigner2;
    @XmlAttribute(name = "suggestedSignerEmail")
    protected String suggestedSignerEmail;
    @XmlAttribute(name = "signingInstructions")
    protected String signingInstructions;
    @XmlAttribute(name = "addlXml")
    protected String addlXml;
    @XmlAttribute(name = "sigProvUrl")
    protected String sigProvUrl;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the isSignatureLine property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsSignatureLine() {
        return isSignatureLine;
    }

    /**
     * Sets the value of the isSignatureLine property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsSignatureLine(Boolean value) {
        this.isSignatureLine = value;
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
     * Gets the value of the provId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProvId() {
        return provId;
    }

    /**
     * Sets the value of the provId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProvId(String value) {
        this.provId = value;
    }

    /**
     * Gets the value of the signingInstructionsSet property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSigningInstructionsSet() {
        return signingInstructionsSet;
    }

    /**
     * Sets the value of the signingInstructionsSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSigningInstructionsSet(Boolean value) {
        this.signingInstructionsSet = value;
    }

    /**
     * Gets the value of the allowComments property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAllowComments() {
        return allowComments;
    }

    /**
     * Sets the value of the allowComments property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAllowComments(Boolean value) {
        this.allowComments = value;
    }

    /**
     * Gets the value of the showSignDate property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isShowSignDate() {
        return showSignDate;
    }

    /**
     * Sets the value of the showSignDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowSignDate(Boolean value) {
        this.showSignDate = value;
    }

    /**
     * Gets the value of the suggestedSigner property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSuggestedSigner() {
        return suggestedSigner;
    }

    /**
     * Sets the value of the suggestedSigner property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSuggestedSigner(String value) {
        this.suggestedSigner = value;
    }

    /**
     * Gets the value of the suggestedSigner2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSuggestedSigner2() {
        return suggestedSigner2;
    }

    /**
     * Sets the value of the suggestedSigner2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSuggestedSigner2(String value) {
        this.suggestedSigner2 = value;
    }

    /**
     * Gets the value of the suggestedSignerEmail property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSuggestedSignerEmail() {
        return suggestedSignerEmail;
    }

    /**
     * Sets the value of the suggestedSignerEmail property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSuggestedSignerEmail(String value) {
        this.suggestedSignerEmail = value;
    }

    /**
     * Gets the value of the signingInstructions property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSigningInstructions() {
        return signingInstructions;
    }

    /**
     * Sets the value of the signingInstructions property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSigningInstructions(String value) {
        this.signingInstructions = value;
    }

    /**
     * Gets the value of the addlXml property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddlXml() {
        return addlXml;
    }

    /**
     * Sets the value of the addlXml property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddlXml(String value) {
        this.addlXml = value;
    }

    /**
     * Gets the value of the sigProvUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSigProvUrl() {
        return sigProvUrl;
    }

    /**
     * Sets the value of the sigProvUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSigProvUrl(String value) {
        this.sigProvUrl = value;
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
