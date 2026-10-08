
package org.docx4j.com.microsoft.schemas.office.x2006.digsig;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_SignatureInfoV1 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SignatureInfoV1">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.microsoft.com/office/2006/digsig}EG_RequiredChildren"/>
 *         &lt;group ref="{http://schemas.microsoft.com/office/2006/digsig}EG_OptionalChildren" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SignatureInfoV1", propOrder = {
    "setupID",
    "signatureText",
    "signatureImage",
    "signatureComments",
    "windowsVersion",
    "officeVersion",
    "applicationVersion",
    "monitors",
    "horizontalResolution",
    "verticalResolution",
    "colorDepth",
    "signatureProviderId",
    "signatureProviderUrl",
    "signatureProviderDetails",
    "signatureType",
    "delegateSuggestedSigner",
    "delegateSuggestedSigner2",
    "delegateSuggestedSignerEmail",
    "manifestHashAlgorithm"
})
@XmlRootElement(name="SignatureInfoV1")
public class CTSignatureInfoV1 {

    @XmlElement(name = "SetupID", required = true)
    protected String setupID;
    @XmlElement(name = "SignatureText", required = true)
    protected String signatureText;
    @XmlElement(name = "SignatureImage", required = true)
    protected byte[] signatureImage;
    @XmlElement(name = "SignatureComments", required = true)
    protected String signatureComments;
    @XmlElement(name = "WindowsVersion", required = true)
    protected String windowsVersion;
    @XmlElement(name = "OfficeVersion", required = true)
    protected String officeVersion;
    @XmlElement(name = "ApplicationVersion", required = true)
    protected String applicationVersion;
    @XmlElement(name = "Monitors")
    protected int monitors;
    @XmlElement(name = "HorizontalResolution")
    protected int horizontalResolution;
    @XmlElement(name = "VerticalResolution")
    protected int verticalResolution;
    @XmlElement(name = "ColorDepth")
    protected int colorDepth;
    @XmlElement(name = "SignatureProviderId", required = true)
    protected String signatureProviderId;
    @XmlElement(name = "SignatureProviderUrl", required = true)
    protected String signatureProviderUrl;
    @XmlElement(name = "SignatureProviderDetails")
    protected int signatureProviderDetails;
    @XmlElement(name = "SignatureType")
    protected int signatureType;
    @XmlElement(name = "DelegateSuggestedSigner")
    protected String delegateSuggestedSigner;
    @XmlElement(name = "DelegateSuggestedSigner2")
    protected String delegateSuggestedSigner2;
    @XmlElement(name = "DelegateSuggestedSignerEmail")
    protected String delegateSuggestedSignerEmail;
    @XmlElement(name = "ManifestHashAlgorithm")
    @XmlSchemaType(name = "anyURI")
    protected String manifestHashAlgorithm;

    /**
     * Gets the value of the setupID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSetupID() {
        return setupID;
    }

    /**
     * Sets the value of the setupID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSetupID(String value) {
        this.setupID = value;
    }

    /**
     * Gets the value of the signatureText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSignatureText() {
        return signatureText;
    }

    /**
     * Sets the value of the signatureText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSignatureText(String value) {
        this.signatureText = value;
    }

    /**
     * Gets the value of the signatureImage property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getSignatureImage() {
        return signatureImage;
    }

    /**
     * Sets the value of the signatureImage property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setSignatureImage(byte[] value) {
        this.signatureImage = value;
    }

    /**
     * Gets the value of the signatureComments (purpose) property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSignatureComments() {
        return signatureComments;
    }

    /**
     * Sets the value of the signatureComments (purpose) property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSignatureComments(String value) {
        this.signatureComments = value;
    }

    /**
     * Gets the value of the windowsVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWindowsVersion() {
        return windowsVersion;
    }

    /**
     * Sets the value of the windowsVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWindowsVersion(String value) {
        this.windowsVersion = value;
    }

    /**
     * Gets the value of the officeVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOfficeVersion() {
        return officeVersion;
    }

    /**
     * Sets the value of the officeVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOfficeVersion(String value) {
        this.officeVersion = value;
    }

    /**
     * Gets the value of the applicationVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getApplicationVersion() {
        return applicationVersion;
    }

    /**
     * Sets the value of the applicationVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setApplicationVersion(String value) {
        this.applicationVersion = value;
    }

    /**
     * Gets the value of the monitors property.
     * 
     */
    public int getMonitors() {
        return monitors;
    }

    /**
     * Sets the value of the monitors property.
     * 
     */
    public void setMonitors(int value) {
        this.monitors = value;
    }

    /**
     * Gets the value of the horizontalResolution property.
     * 
     */
    public int getHorizontalResolution() {
        return horizontalResolution;
    }

    /**
     * Sets the value of the horizontalResolution property.
     * 
     */
    public void setHorizontalResolution(int value) {
        this.horizontalResolution = value;
    }

    /**
     * Gets the value of the verticalResolution property.
     * 
     */
    public int getVerticalResolution() {
        return verticalResolution;
    }

    /**
     * Sets the value of the verticalResolution property.
     * 
     */
    public void setVerticalResolution(int value) {
        this.verticalResolution = value;
    }

    /**
     * Gets the value of the colorDepth property.
     * 
     */
    public int getColorDepth() {
        return colorDepth;
    }

    /**
     * Sets the value of the colorDepth property.
     * 
     */
    public void setColorDepth(int value) {
        this.colorDepth = value;
    }

    /**
     * Gets the value of the signatureProviderId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSignatureProviderId() {
        return signatureProviderId;
    }

    /**
     * Sets the value of the signatureProviderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSignatureProviderId(String value) {
        this.signatureProviderId = value;
    }

    /**
     * Gets the value of the signatureProviderUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSignatureProviderUrl() {
        return signatureProviderUrl;
    }

    /**
     * Sets the value of the signatureProviderUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSignatureProviderUrl(String value) {
        this.signatureProviderUrl = value;
    }

    /**
     * Gets the value of the signatureProviderDetails property.
     * 
     */
    public int getSignatureProviderDetails() {
        return signatureProviderDetails;
    }

    /**
     * Sets the value of the signatureProviderDetails property.
     * 
     */
    public void setSignatureProviderDetails(int value) {
        this.signatureProviderDetails = value;
    }

    /**
     * Gets the value of the signatureType property.
     * 
     */
    public int getSignatureType() {
        return signatureType;
    }

    /**
     * Sets the value of the signatureType property.
     * 
     * When this signature is of type 2 (Signature Line), there shall be two additional objects 
     * in the signature:  
     * 
     * One with an Id value of idValidSigLnImg, whose value shall define the image of a valid signature; and 
     * 
     * one with an Id value of idInvalidSigLnImg, whose value shall define the image of an invalid signature.
     * 
     */
    public void setSignatureType(int value) {
        this.signatureType = value;
    }

    /**
     * Gets the value of the delegateSuggestedSigner property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDelegateSuggestedSigner() {
        return delegateSuggestedSigner;
    }

    /**
     * Sets the value of the delegateSuggestedSigner property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDelegateSuggestedSigner(String value) {
        this.delegateSuggestedSigner = value;
    }

    /**
     * Gets the value of the delegateSuggestedSigner2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDelegateSuggestedSigner2() {
        return delegateSuggestedSigner2;
    }

    /**
     * Sets the value of the delegateSuggestedSigner2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDelegateSuggestedSigner2(String value) {
        this.delegateSuggestedSigner2 = value;
    }

    /**
     * Gets the value of the delegateSuggestedSignerEmail property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDelegateSuggestedSignerEmail() {
        return delegateSuggestedSignerEmail;
    }

    /**
     * Sets the value of the delegateSuggestedSignerEmail property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDelegateSuggestedSignerEmail(String value) {
        this.delegateSuggestedSignerEmail = value;
    }

    /**
     * Gets the value of the manifestHashAlgorithm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getManifestHashAlgorithm() {
        return manifestHashAlgorithm;
    }

    /**
     * Sets the value of the manifestHashAlgorithm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setManifestHashAlgorithm(String value) {
        this.manifestHashAlgorithm = value;
    }

}
