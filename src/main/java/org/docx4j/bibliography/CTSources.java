
package org.docx4j.bibliography;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_Sources complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Sources">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Source" type="{http://schemas.openxmlformats.org/officeDocument/2006/bibliography}CT_SourceType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="SelectedStyle" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String" />
 *       &lt;attribute name="StyleName" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String" />
 *       &lt;attribute name="URI" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Sources", propOrder = {
    "source"
})
public class CTSources {

    @XmlElement(name = "Source")
    protected List<CTSourceType> source;
    @XmlAttribute(name = "SelectedStyle")
    protected String selectedStyle;
    @XmlAttribute(name = "StyleName")
    protected String styleName;
    @XmlAttribute(name = "URI")
    protected String uri;

    /**
     * Gets the value of the source property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the source property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSource().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTSourceType }
     * 
     * 
     */
    public List<CTSourceType> getSource() {
        if (source == null) {
            source = new ArrayList<CTSourceType>();
        }
        return this.source;
    }

    /**
     * Gets the value of the selectedStyle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSelectedStyle() {
        return selectedStyle;
    }

    /**
     * Sets the value of the selectedStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSelectedStyle(String value) {
        this.selectedStyle = value;
    }

    /**
     * Gets the value of the styleName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStyleName() {
        return styleName;
    }

    /**
     * Sets the value of the styleName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStyleName(String value) {
        this.styleName = value;
    }

    /**
     * Gets the value of the uri property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getURI() {
        return uri;
    }

    /**
     * Sets the value of the uri property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setURI(String value) {
        this.uri = value;
    }

}
