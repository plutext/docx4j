
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;;


/**
 * <p>Java class for CT_GeoDataPointToEntityQueryResults complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GeoDataPointToEntityQueryResults"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="geoDataPointToEntityQueryResult" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_GeoDataPointToEntityQueryResult" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GeoDataPointToEntityQueryResults", propOrder = {
    "geoDataPointToEntityQueryResult"
})
public class CTGeoDataPointToEntityQueryResults implements Child
{

    protected List<CTGeoDataPointToEntityQueryResult> geoDataPointToEntityQueryResult;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the geoDataPointToEntityQueryResult property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the geoDataPointToEntityQueryResult property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGeoDataPointToEntityQueryResult().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTGeoDataPointToEntityQueryResult }
     * 
     * 
     */
    public List<CTGeoDataPointToEntityQueryResult> getGeoDataPointToEntityQueryResult() {
        if (geoDataPointToEntityQueryResult == null) {
            geoDataPointToEntityQueryResult = new ArrayList<CTGeoDataPointToEntityQueryResult>();
        }
        return this.geoDataPointToEntityQueryResult;
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
