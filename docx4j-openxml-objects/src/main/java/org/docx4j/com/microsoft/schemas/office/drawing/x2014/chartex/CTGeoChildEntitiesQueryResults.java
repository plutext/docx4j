
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_GeoChildEntitiesQueryResults complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GeoChildEntitiesQueryResults"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="geoChildEntitiesQueryResult" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_GeoChildEntitiesQueryResult" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GeoChildEntitiesQueryResults", propOrder = {
    "geoChildEntitiesQueryResult"
})
public class CTGeoChildEntitiesQueryResults {

    protected List<CTGeoChildEntitiesQueryResult> geoChildEntitiesQueryResult;

    /**
     * Gets the value of the geoChildEntitiesQueryResult property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the geoChildEntitiesQueryResult property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGeoChildEntitiesQueryResult().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTGeoChildEntitiesQueryResult }
     * 
     * 
     */
    public List<CTGeoChildEntitiesQueryResult> getGeoChildEntitiesQueryResult() {
        if (geoChildEntitiesQueryResult == null) {
            geoChildEntitiesQueryResult = new ArrayList<CTGeoChildEntitiesQueryResult>();
        }
        return this.geoChildEntitiesQueryResult;
    }

}
