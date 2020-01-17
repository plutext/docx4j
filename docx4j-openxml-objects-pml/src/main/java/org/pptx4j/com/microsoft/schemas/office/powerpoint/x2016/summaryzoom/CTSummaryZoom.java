
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2016.summaryzoom;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.pptx4j.pml.CTExtensionList;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_SummaryZoom complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SummaryZoom"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="summaryZmObj" type="{http://schemas.microsoft.com/office/powerpoint/2016/summaryzoom}CT_SummaryZoomObject" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="gridLayout" type="{http://schemas.microsoft.com/office/powerpoint/2016/summaryzoom}CT_GridLayout"/&gt;
 *           &lt;element name="fixedLayout" type="{http://schemas.microsoft.com/office/powerpoint/2016/summaryzoom}CT_FixedLayout"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SummaryZoom", propOrder = {
    "summaryZmObj",
    "gridLayout",
    "fixedLayout",
    "extLst"
})
public class CTSummaryZoom implements Child
{

    protected List<CTSummaryZoomObject> summaryZmObj;
    protected CTGridLayout gridLayout;
    protected CTFixedLayout fixedLayout;
    protected CTExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the summaryZmObj property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the summaryZmObj property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSummaryZmObj().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTSummaryZoomObject }
     * 
     * 
     */
    public List<CTSummaryZoomObject> getSummaryZmObj() {
        if (summaryZmObj == null) {
            summaryZmObj = new ArrayList<CTSummaryZoomObject>();
        }
        return this.summaryZmObj;
    }

    /**
     * Gets the value of the gridLayout property.
     * 
     * @return
     *     possible object is
     *     {@link CTGridLayout }
     *     
     */
    public CTGridLayout getGridLayout() {
        return gridLayout;
    }

    /**
     * Sets the value of the gridLayout property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGridLayout }
     *     
     */
    public void setGridLayout(CTGridLayout value) {
        this.gridLayout = value;
    }

    /**
     * Gets the value of the fixedLayout property.
     * 
     * @return
     *     possible object is
     *     {@link CTFixedLayout }
     *     
     */
    public CTFixedLayout getFixedLayout() {
        return fixedLayout;
    }

    /**
     * Sets the value of the fixedLayout property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFixedLayout }
     *     
     */
    public void setFixedLayout(CTFixedLayout value) {
        this.fixedLayout = value;
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
