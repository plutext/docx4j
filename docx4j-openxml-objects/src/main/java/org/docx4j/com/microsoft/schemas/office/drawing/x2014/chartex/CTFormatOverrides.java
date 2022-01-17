
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_FormatOverrides complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_FormatOverrides"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="fmtOvr" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_FormatOverride" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_FormatOverrides", propOrder = {
    "fmtOvr"
})
public class CTFormatOverrides {

    protected List<CTFormatOverride> fmtOvr;

    /**
     * Gets the value of the fmtOvr property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the fmtOvr property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFmtOvr().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTFormatOverride }
     * 
     * 
     */
    public List<CTFormatOverride> getFmtOvr() {
        if (fmtOvr == null) {
            fmtOvr = new ArrayList<CTFormatOverride>();
        }
        return this.fmtOvr;
    }

}
