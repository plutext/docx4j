
package org.opendope.SmartArt.dataHierarchy;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element ref="{http://opendope.org/SmartArt/DataHierarchy}textBody"/>
 *         &lt;element ref="{http://opendope.org/SmartArt/DataHierarchy}sibTransBody" minOccurs="0"/>
 *         &lt;element ref="{http://opendope.org/SmartArt/DataHierarchy}image" minOccurs="0"/>
 *         &lt;element ref="{http://opendope.org/SmartArt/DataHierarchy}list" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "textBody",
    "sibTransBody",
    "image",
    "list"
})
@XmlRootElement(name = "listItem")
public class ListItem {

    @XmlElement(required = true)
    protected TextBody textBody;
    protected SibTransBody sibTransBody;
    protected Image image;
    protected List list;
    @XmlAttribute(required = true)
    protected String id;

    /**
     * Gets the value of the textBody property.
     * 
     * @return
     *     possible object is
     *     {@link TextBody }
     *     
     */
    public TextBody getTextBody() {
        return textBody;
    }

    /**
     * Sets the value of the textBody property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextBody }
     *     
     */
    public void setTextBody(TextBody value) {
        this.textBody = value;
    }

    /**
     * Gets the value of the sibTransBody property.
     * 
     * @return
     *     possible object is
     *     {@link SibTransBody }
     *     
     */
    public SibTransBody getSibTransBody() {
        return sibTransBody;
    }

    /**
     * Sets the value of the sibTransBody property.
     * 
     * @param value
     *     allowed object is
     *     {@link SibTransBody }
     *     
     */
    public void setSibTransBody(SibTransBody value) {
        this.sibTransBody = value;
    }

    /**
     * Gets the value of the image property.
     * 
     * @return
     *     possible object is
     *     {@link Image }
     *     
     */
    public Image getImage() {
        return image;
    }

    /**
     * Sets the value of the image property.
     * 
     * @param value
     *     allowed object is
     *     {@link Image }
     *     
     */
    public void setImage(Image value) {
        this.image = value;
    }

    /**
     * Gets the value of the list property.
     * 
     * @return
     *     possible object is
     *     {@link List }
     *     
     */
    public List getList() {
        return list;
    }

    /**
     * Sets the value of the list property.
     * 
     * @param value
     *     allowed object is
     *     {@link List }
     *     
     */
    public void setList(List value) {
        this.list = value;
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

}
