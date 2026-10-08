
package org.docx4j.org.openxmlformats.schemas.xpackage.x2006.digitalSignature;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RelationshipReference" type="{http://schemas.openxmlformats.org/package/2006/digital-signature}CT_RelationshipReference" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "relationshipReference"
})
@XmlRootElement(name = "Docx4jRelationshipReferences")
public class Docx4JRelationshipReferences {

    @XmlElement(name = "RelationshipReference")
    protected List<CTRelationshipReference> relationshipReference;

    /**
     * Gets the value of the relationshipReference property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the relationshipReference property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRelationshipReference().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTRelationshipReference }
     * 
     * 
     */
    public List<CTRelationshipReference> getRelationshipReference() {
        if (relationshipReference == null) {
            relationshipReference = new ArrayList<CTRelationshipReference>();
        }
        return this.relationshipReference;
    }

}
