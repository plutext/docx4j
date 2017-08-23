/*
 *  Copyright 2007-2013, Plutext Pty Ltd.
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


package org.docx4j.wml; 


import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Java class for sdt element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="sdt">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="sdtPr" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_SdtPr" minOccurs="0"/>
 *           &lt;element name="sdtEndPr" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_SdtEndPr" minOccurs="0"/>
 *           &lt;element name="sdtContent" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_SdtContentBlock" minOccurs="0"/>
 *         &lt;/sequence>
 *       &lt;/restriction>
 *     &lt;/complexContent>
 *   &lt;/complexType>
 * &lt;/element>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sdtPr",
    "sdtEndPr",
    "sdtContent"
})
@XmlRootElement(name = "sdt")
public class SdtBlock implements SdtElement, Child
{

	private static Logger log = LoggerFactory.getLogger(SdtBlock.class);		
	
    protected SdtPr sdtPr;
    protected CTSdtEndPr sdtEndPr;
    protected SdtContentBlock sdtContent;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the sdtPr property.
     * 
     * @return
     *     possible object is
     *     {@link SdtPr }
     *     
     */
    public SdtPr getSdtPr() {
        return sdtPr;
    }

    /**
     * Sets the value of the sdtPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link SdtPr }
     *     
     */
    public void setSdtPr(SdtPr value) {
        this.sdtPr = value;
        value.setParent(this); // unmarshalling does this automatically; this helps user in other cases
    }

    /**
     * Gets the value of the sdtEndPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTSdtEndPr }
     *     
     */
    public CTSdtEndPr getSdtEndPr() {
        return sdtEndPr;
    }

    /**
     * Sets the value of the sdtEndPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSdtEndPr }
     *     
     */
    public void setSdtEndPr(CTSdtEndPr value) {
        this.sdtEndPr = value;
    }

    /**
     * Gets the value of the sdtContent property.
     * 
     * @return
     *     possible object is
     *     {@link SdtContentBlock }
     *     
     */
    public SdtContent getSdtContent() {
        return sdtContent;
    }

    /**
     * Sets the value of the sdtContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link SdtContentBlock }
     *     
     */
    public void setSdtContent(SdtContent value) {
        this.sdtContent = (SdtContentBlock)value;
        ((SdtContentBlock)value).setParent(this); // unmarshalling does this automatically; this helps user in other cases
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

    /* Replace any nested content controls with their content. */
    public void flatten() {
    	 
		/*    	
		<w:sdt xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
			<w:sdtPr><w:tag w:val="30" /><w:id w:val="871785936" /></w:sdtPr>
			<w:sdtContent>
				<w:p w:rsidR="00283267" w:rsidRDefault="00E8712C">
					<w:r><w:t>S</w:t></w:r>
					<w:sdt><w:sdtPr><w:tag w:val="0" /><w:id w:val="589321610" /></w:sdtPr><w:sdtContent><w:r><w:t>Para1</w:t></w:r></w:sdtContent></w:sdt>
				</w:p>
			</w:sdtContent>
		</w:sdt>
		*/
    	
		log.info("Flattening sdt: " + sdtPr.getId().toString() );
		boolean startAgain;
    	do {
        	startAgain = false;
//        	java.util.Iterator it = sdtContent.getBlockLevelElements().iterator();
//	    	while ( it.hasNext() ) {
//	    		
//	    		Object o = it.next();
	    		
	    	for (Object o : sdtContent.getEGContentBlockContent() ) {
	    		
	    		if (o instanceof SdtBlock) { // A block level SDT - but this doesn't happen
	    			log.debug("Interesting .. detected BLOCK level nested sdt: " + ((SdtBlock)o).sdtPr.getId().toString() );
	    			sdtContent.replaceElement(o, ((SdtBlock)o).getSdtContent().getContent() );
	    			// need to refresh the list we are iterating
	    			startAgain = true;
	    			break;
	    		} else if ( o instanceof org.docx4j.wml.P ) {
    				log.debug( "Paragraph object: ");
    				org.docx4j.wml.P p = (org.docx4j.wml.P)o;   				
    				flattenP(p);
 	    		} else if (o instanceof javax.xml.bind.JAXBElement) {
	    			
//	    			if ( ((JAXBElement)o).getDeclaredType().getName().equals("org.docx4j.wml.P") ) {
//	    				log.debug( "Paragraph object: ");
//	    				org.docx4j.wml.P p = (org.docx4j.wml.P)((JAXBElement)o).getValue();
//	    				
//	    				flattenP(p);
//	    				// Is this necessary?
//	    				((JAXBElement)o).setValue(p);
//	    			} else {
	    				
	    				log.debug( "JAXB: " + ((JAXBElement)o).getValue().getClass().getName() );
	    				
//	    			}
	    				
	    		} else {
	    			log.debug(o.getClass().getName() + ".. not an sdt");
	    		}
	    	}
    	} while (startAgain);
    }
    	
    public void flattenP(org.docx4j.wml.P p) {
    	
		/*    	
				<w:p w:rsidR="00283267" w:rsidRDefault="00E8712C">
					<w:r><w:t>S</w:t></w:r>
					<w:sdt><w:sdtPr><w:tag w:val="0" /><w:id w:val="589321610" /></w:sdtPr><w:sdtContent><w:r><w:t>Para1</w:t></w:r></w:sdtContent></w:sdt>
				</w:p>
		*/
    	
		log.info("Flattening nested p " );
		boolean startAgain;
    	do {
        	startAgain = false;
	    	for (Object o : p.getContent() ) {
	    		
	    		if (o instanceof SdtRun) {  // This code path not used
	    			log.debug(".. detected nested sdt " );
	    			p.replaceElement(o, ((SdtRun)o).getSdtContent().getContent() );
	    			// need to refresh the list we are iterating
	    			startAgain = true;
	    			break;
	    		} else if (o instanceof javax.xml.bind.JAXBElement) {
	    			
	    			if ( ((JAXBElement)o).getDeclaredType().getName().equals("org.docx4j.wml.SdtRun") ) {
	    				log.debug( ((JAXBElement)o).getDeclaredType().getName() + ".. detected SdtRun");
	    				org.docx4j.wml.SdtRun sdtRun = (org.docx4j.wml.SdtRun)((JAXBElement)o).getValue();
	    				p.replaceElement(o, sdtRun.getSdtContent().getContent() );
	    			} else {
	    				log.debug( ((JAXBElement)o).getDeclaredType().getName() + ".. not an sdt");	    				
	    			}
	    				
	    		} else {
	    			log.debug(o.getClass().getName() + ".. not an sdt");
	    		}
	    	}
    	} while (startAgain);
    	
    	
    }
    
    
}
