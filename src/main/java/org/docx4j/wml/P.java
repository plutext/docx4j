/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.wml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;

import javax.xml.bind.annotation.XmlAnyElement;

/**
 * <p>Java class for CT_P complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_P">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}pPr" minOccurs="0"/>
 *         &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_PContent" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_P", propOrder = {
    "pPr",
    "paragraphContent"
})
@XmlRootElement(name = "p")
public class P
    implements Child
{

    protected PPr pPr;
    
    @XmlAnyElement    
    @XmlElementRefs({
        @XmlElementRef(name = "ins", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "r", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = R.class),
        @XmlElementRef(name = "del", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class)
    })
    protected List<Object> paragraphContent;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the pPr property.
     * 
     * @return
     *     possible object is
     *     {@link PPr }
     *     
     */
    public PPr getPPr() {
        return pPr;
    }

    /**
     * Sets the value of the pPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link PPr }
     *     
     */
    public void setPPr(PPr value) {
        this.pPr = value;
    }

    /**
     * Gets the value of the paragraphContent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the paragraphContent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParagraphContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link R }
     * {@link JAXBElement }{@code <}{@link RunTrackChange }{@code >}
     * {@link JAXBElement }{@code <}{@link RunTrackChange }{@code >}
     * 
     * 
     */
    public List<Object> getParagraphContent() {
        if (paragraphContent == null) {
            paragraphContent = new ArrayList<Object>();
        }
        return this.paragraphContent;
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

    // Not generated
    /** Get the text content of all runs in the P */
    public String toString() {

    	StringBuilder result = new StringBuilder();
    	
    	List<Object> children = getParagraphContent();
    	
//    	System.out.println("p.toString");
    	
		for (Object o : children ) {					
//			System.out.println("  " + o.getClass().getName() );
			if ( o instanceof org.docx4j.wml.R) {
//		    	System.out.println("Hit R");
				org.docx4j.wml.R  run = (org.docx4j.wml.R)o;
		    	List<Object> runContent = run.getRunContent();
				for (Object o2 : runContent ) {					
					if ( o2 instanceof javax.xml.bind.JAXBElement) {
						// TODO - unmarshall directly to Text.
						if ( ((JAXBElement)o2).getDeclaredType().getName().equals("org.docx4j.wml.Text") ) {
//					    	System.out.println("Found Text");
							org.docx4j.wml.Text t = (org.docx4j.wml.Text)((JAXBElement)o2).getValue();
							result.append( t.getValue() );					
						}
					} else {
//				    	System.out.println(o2.getClass().getName());						
					}
				}
			} 
		}
		return result.toString();
    	
    }

}
