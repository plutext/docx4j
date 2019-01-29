
package org.opendope.conditions;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.docx4j.openpackaging.parts.opendope.ConditionsPart;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "conditionref")
public class Conditionref implements Evaluable {

    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String id;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

	public boolean evaluate(WordprocessingMLPackage pkg, 
			Map<String, CustomXmlPart> customXmlDataStorageParts,
			Map<String, Condition> conditionsMap,
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap) {
		
		//Condition particle = ConditionsPart.getConditionById(conditions, id);
		Condition particle = conditionsMap.get(id);
    	return particle.evaluate(pkg, customXmlDataStorageParts, conditionsMap, xpathsMap);
    }
	
	public void listXPaths( List<org.opendope.xpaths.Xpaths.Xpath> theList, 
			Map<String, Condition> conditionsMap,
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap) {
		
		//Condition particle = ConditionsPart.getConditionById(conditions, id);
		Condition particle = conditionsMap.get(id);

    	particle.listXPaths(theList, conditionsMap, xpathsMap);
		
	}
	
	/**
	 * Map the IDs used in this condition to new values; useful for merging ConditionParts.
	 * 
	 * @param xpathIdMap
	 * @param conditionIdMap
	 * @since 3.0.0
	 */
	public void mapIds(Map<String, String> xpathIdMap, Map<String, String> conditionIdMap) {
		
		if (conditionIdMap==null) return;
		
		String newId = conditionIdMap.get(getId());
		if (newId!=null) {
			setId(newId);
		}
	}
	
	
	public String toString(Map<String, Condition> conditionsMap,
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap) {

		//Condition particle = ConditionsPart.getConditionById(conditions, id);
		Condition particle = conditionsMap.get(id);
		return particle.toString(conditionsMap, xpathsMap);
	}
    
	public Condition repeat(String xpathBase,
			int index,
			Map<String, Condition> conditionsMap,
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap)	{

		// Condition particle = ConditionsPart.getConditionById(conditions, id);
		Condition particle = conditionsMap.get(id);

		Condition newCondition = particle.repeat(xpathBase, index, conditionsMap, xpathsMap);
		
		this.id = newCondition.getId();

    	return null;
	}	
	
}
