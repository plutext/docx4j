/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */


package org.docx4j.dml;

import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

//import org.docx4j.XmlUtils;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;
import org.docx4j.dml.picture.Pic;
import org.jvnet.jaxb2_commons.ppp.Child;

/**
 * <p>Java class for CT_GraphicalObjectData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GraphicalObjectData"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;any maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="uri" type="{http://www.w3.org/2001/XMLSchema}token" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GraphicalObjectData", propOrder = {
    "any"
})
public class GraphicData implements Child
{

	private static Object unwrap(Object o) {
		
		if (o==null) return null;
		
		if (o instanceof javax.xml.bind.JAXBElement) {
// 			log.debug("Unwrapped " + ((JAXBElement)o).getDeclaredType().getName() );
// 			log.debug("name: " + ((JAXBElement)o).getName() );
			return ((JAXBElement)o).getValue();
		} else {
			return o;
		}
	}
	
	@XmlAnyElement(lax = true)
    protected List<Object> any = new ArrayListDml<Object>(this);

    @XmlAttribute(name = "uri")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String uri;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayListDml<Object>(this);
        }
        return this.any;
    }
    
    @XmlTransient
    public org.docx4j.dml.picture.Pic getPic() {

		for (Object o : getAny() ) {
			
			if (o instanceof Pic) return (Pic)o;

			if (o instanceof JAXBElement
					&& ((JAXBElement)o).getDeclaredType().getName().equals("org.docx4j.dml.picture.Pic") ) {
				
					return (Pic)((JAXBElement)o).getValue();
			}
		}
    	return null;    	
    }

    @XmlTransient
    public CTWordprocessingShape getWordprocessingShape() {
    	
    	/*
          <a:graphicData uri="http://schemas.microsoft.com/office/word/2010/wordprocessingShape">
            <wps:wsp>
       	 */

		for (Object o : getAny() ) {
			
			if (o instanceof CTWordprocessingShape) return (CTWordprocessingShape)o;

			if (o instanceof JAXBElement
					&& (unwrap(o) instanceof  CTWordprocessingShape)) {
				
					return (CTWordprocessingShape)((JAXBElement)o).getValue();
			}
		}
    	return null;    	
    }        

    /**
     * Gets the value of the uri property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the value of the uri property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUri(String value) {
        this.uri = value;
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
