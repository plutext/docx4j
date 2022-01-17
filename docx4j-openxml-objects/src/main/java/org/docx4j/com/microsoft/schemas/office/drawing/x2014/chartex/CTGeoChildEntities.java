
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_GeoChildEntities complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GeoChildEntities"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="geoHierarchyEntity" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_GeoHierarchyEntity" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GeoChildEntities", propOrder = {
    "geoHierarchyEntity"
})
public class CTGeoChildEntities {

    protected List<CTGeoHierarchyEntity> geoHierarchyEntity;

    /**
     * Gets the value of the geoHierarchyEntity property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the geoHierarchyEntity property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGeoHierarchyEntity().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTGeoHierarchyEntity }
     * 
     * 
     */
    public List<CTGeoHierarchyEntity> getGeoHierarchyEntity() {
        if (geoHierarchyEntity == null) {
            geoHierarchyEntity = new ArrayList<CTGeoHierarchyEntity>();
        }
        return this.geoHierarchyEntity;
    }

}
