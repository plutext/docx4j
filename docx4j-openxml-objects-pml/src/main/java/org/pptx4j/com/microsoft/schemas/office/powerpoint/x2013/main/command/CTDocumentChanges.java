
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2013.main.command;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.com.microsoft.schemas.office.drawing.x2013.main.command.CTChangesData;
import org.pptx4j.pml.CTExtensionList;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_DocumentChanges complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DocumentChanges"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="chgData" type="{http://schemas.microsoft.com/office/drawing/2013/main/command}CT_ChangesData" minOccurs="0"/&gt;
 *         &lt;element name="docMkLst" type="{http://schemas.microsoft.com/office/powerpoint/2013/main/command}CT_DocumentMonikerList"/&gt;
 *         &lt;element name="sldChg" type="{http://schemas.microsoft.com/office/powerpoint/2013/main/command}CT_SlideChanges" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="sldMasterChg" type="{http://schemas.microsoft.com/office/powerpoint/2013/main/command}CT_MainMasterChanges" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="cmAuthorChg" type="{http://schemas.microsoft.com/office/powerpoint/2013/main/command}CT_CommentAuthorChanges" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="chg" use="required" type="{http://schemas.microsoft.com/office/powerpoint/2013/main/command}ST_DocumentChangeBits" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DocumentChanges", propOrder = {
    "chgData",
    "docMkLst",
    "sldChg",
    "sldMasterChg",
    "cmAuthorChg",
    "extLst"
})
public class CTDocumentChanges implements Child
{

    protected CTChangesData chgData;
    @XmlElement(required = true)
    protected CTDocumentMonikerList docMkLst;
    protected List<CTSlideChanges> sldChg;
    protected List<CTMainMasterChanges> sldMasterChg;
    protected List<CTCommentAuthorChanges> cmAuthorChg;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "chg", required = true)
    protected List<STDocumentChangeBit> chg;
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
     * Gets the value of the docMkLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTDocumentMonikerList }
     *     
     */
    public CTDocumentMonikerList getDocMkLst() {
        return docMkLst;
    }

    /**
     * Sets the value of the docMkLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDocumentMonikerList }
     *     
     */
    public void setDocMkLst(CTDocumentMonikerList value) {
        this.docMkLst = value;
    }

    /**
     * Gets the value of the sldChg property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sldChg property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSldChg().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTSlideChanges }
     * 
     * 
     */
    public List<CTSlideChanges> getSldChg() {
        if (sldChg == null) {
            sldChg = new ArrayList<CTSlideChanges>();
        }
        return this.sldChg;
    }

    /**
     * Gets the value of the sldMasterChg property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sldMasterChg property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSldMasterChg().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTMainMasterChanges }
     * 
     * 
     */
    public List<CTMainMasterChanges> getSldMasterChg() {
        if (sldMasterChg == null) {
            sldMasterChg = new ArrayList<CTMainMasterChanges>();
        }
        return this.sldMasterChg;
    }

    /**
     * Gets the value of the cmAuthorChg property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cmAuthorChg property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCmAuthorChg().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTCommentAuthorChanges }
     * 
     * 
     */
    public List<CTCommentAuthorChanges> getCmAuthorChg() {
        if (cmAuthorChg == null) {
            cmAuthorChg = new ArrayList<CTCommentAuthorChanges>();
        }
        return this.cmAuthorChg;
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
     * {@link STDocumentChangeBit }
     * 
     * 
     */
    public List<STDocumentChangeBit> getChg() {
        if (chg == null) {
            chg = new ArrayList<STDocumentChangeBit>();
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
