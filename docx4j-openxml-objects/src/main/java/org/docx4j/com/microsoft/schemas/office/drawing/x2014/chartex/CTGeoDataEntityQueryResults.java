
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_GeoDataEntityQueryResults complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GeoDataEntityQueryResults"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="geoDataEntityQueryResult" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_GeoDataEntityQueryResult" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GeoDataEntityQueryResults", propOrder = {
    "geoDataEntityQueryResult"
})
public class CTGeoDataEntityQueryResults {

    protected List<CTGeoDataEntityQueryResult> geoDataEntityQueryResult;

    /**
     * Gets the value of the geoDataEntityQueryResult property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the javax XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the geoDataEntityQueryResult property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGeoDataEntityQueryResult().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTGeoDataEntityQueryResult }
     * 
     * 
     */
    public List<CTGeoDataEntityQueryResult> getGeoDataEntityQueryResult() {
        if (geoDataEntityQueryResult == null) {
            geoDataEntityQueryResult = new ArrayList<CTGeoDataEntityQueryResult>();
        }
        return this.geoDataEntityQueryResult;
    }

}
