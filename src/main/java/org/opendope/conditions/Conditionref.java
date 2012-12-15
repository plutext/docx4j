
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
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
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
			Conditions conditions,
			org.opendope.xpaths.Xpaths xPaths) {
		
		Condition particle = ConditionsPart.getConditionById(conditions, id);
    	return particle.evaluate(pkg, customXmlDataStorageParts, conditions, xPaths);
    }
	
	public void listXPaths( List<org.opendope.xpaths.Xpaths.Xpath> theList, 
			Conditions conditions,
			org.opendope.xpaths.Xpaths xPaths) {
		
		Condition particle = ConditionsPart.getConditionById(conditions, id);
    	particle.listXPaths(theList, conditions, xPaths);
		
	}
	
	public String toString(Conditions conditions,
			org.opendope.xpaths.Xpaths xPaths) {

		Condition particle = ConditionsPart.getConditionById(conditions, id);
		return particle.toString(conditions, xPaths);
	}
    
	public Condition repeat(String xpathBase,
			int index,
			Conditions conditions,
			org.opendope.xpaths.Xpaths xPaths)	{

		Condition particle = ConditionsPart.getConditionById(conditions, id);
		Condition newCondition = particle.repeat(xpathBase, index, conditions, xPaths);
		
		this.id = newCondition.getId();

    	return null;
	}	
	
}
