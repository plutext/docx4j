
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2013.main.command;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.docx4j.com.microsoft.schemas.office.drawing.x2013.main.command.CTChangesData;
import org.pptx4j.pml.CTExtensionList;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_CommentChanges complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CommentChanges"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="chgData" type="{http://schemas.microsoft.com/office/drawing/2013/main/command}CT_ChangesData" minOccurs="0"/&gt;
 *         &lt;element name="cmMkLst" type="{http://schemas.microsoft.com/office/powerpoint/2013/main/command}CT_CommentMonikerList"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="chg" use="required" type="{http://schemas.microsoft.com/office/powerpoint/2013/main/command}ST_CommentChangeBits" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CommentChanges", propOrder = {
    "chgData",
    "cmMkLst",
    "extLst"
})
public class CTCommentChanges implements Child
{

    protected CTChangesData chgData;
    @XmlElement(required = true)
    protected CTCommentMonikerList cmMkLst;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "chg", required = true)
    protected List<STCommentChangeBit> chg;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the chgData property.
     * 
     * @return
     *     possible object is
     *     {@link CTChangesData }
     *     
     */
    public CTChangesData getChgData() {
        return chgData;
    }

    /**
     * Sets the value of the chgData property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTChangesData }
     *     
     */
    public void setChgData(CTChangesData value) {
        this.chgData = value;
    }

    /**
     * Gets the value of the cmMkLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTCommentMonikerList }
     *     
     */
    public CTCommentMonikerList getCmMkLst() {
        return cmMkLst;
    }

    /**
     * Sets the value of the cmMkLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCommentMonikerList }
     *     
     */
    public void setCmMkLst(CTCommentMonikerList value) {
        this.cmMkLst = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtensionList }
     *     
     */
    public CTExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtensionList }
     *     
     */
    public void setExtLst(CTExtensionList value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the chg property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the chg property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChg().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link STCommentChangeBit }
     * 
     * 
     */
    public List<STCommentChangeBit> getChg() {
        if (chg == null) {
            chg = new ArrayList<STCommentChangeBit>();
        }
        return this.chg;
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
