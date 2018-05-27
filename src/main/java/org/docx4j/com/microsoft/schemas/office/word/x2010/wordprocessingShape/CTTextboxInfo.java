
package org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.docx4j.wml.CTTxbxContent;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TextboxInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TextboxInfo"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}txbxContent" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" default="0" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TextboxInfo", propOrder = {
    "txbxContent",
    "extLst"
})
public class CTTextboxInfo implements Child
{

    @XmlElement(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected CTTxbxContent txbxContent;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "id")
    @XmlSchemaType(name = "unsignedShort")
    protected Integer id;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the txbxContent property.
     * 
     * @return
     *     possible object is
     *     {@link CTTxbxContent }
     *     
     */
    public CTTxbxContent getTxbxContent() {
        return txbxContent;
    }

    /**
     * Sets the value of the txbxContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTxbxContent }
     *     
     */
    public void setTxbxContent(CTTxbxContent value) {
        this.txbxContent = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public CTOfficeArtExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public void setExtLst(CTOfficeArtExtensionList value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getId() {
        if (id == null) {
            return  0;
        } else {
            return id;
        }
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setId(Integer value) {
        this.id = value;
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
