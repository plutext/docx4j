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

package org.docx4j.openpackaging.parts;


import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;


import org.apache.log4j.Logger;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.tree.DefaultElement;



/**
 * @author jharrop
 *
 */
public class DocPropsCustomPart extends JaxbXmlPart {
	
	/* Specification > Shared ML > Metadata > Custom Properties
	 * 
	 */
		
	
	/*
		<?xml version="1.0" encoding="UTF-8" standalone="yes"?> 
		<Properties 
			xmlns="http://schemas.openxmlformats.org/officeDocument/2006/custom-properties" 
			xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
			<property fmtid="{D5CDD505-2E9C-101B-9397-08002B2CF9AE}" pid="2" name="Grouping">
				<vt:lpwstr>p</vt:lpwstr>
			</property>
			<property fmtid="{D5CDD505-2E9C-101B-9397-08002B2CF9AE}" pid="3" name="CheckinComments">
				<vt:lpwstr>true</vt:lpwstr>
			</property>
		</Properties>
		
	*/
	
	
	
	private static Logger log = Logger.getLogger(DocPropsCustomPart.class);
	
	
	/* fmtid (Format ID) Uniquely relates a custom property with an OLE property.
	 * 
	 * The value of this attribute is a Globally Unique Identifier in the form of 
	 * {HHHHHHHH-HHHH-HHHH-HHHH-HHHHHHHH} where each H is a hexidecimal.
	 * 
	 * 
	 */
	public static final String fmtidValLpwstr = "{D5CDD505-2E9C-101B-9397-08002B2CF9AE}";
	
	 /** 
	 * @throws InvalidFormatException
	 */
	public DocPropsCustomPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public DocPropsCustomPart() throws InvalidFormatException {
		super(new PartName("/docProps/custom.xml"));
		init();
	}
	
	public void init() {
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.OFFICEDOCUMENT_CUSTOMPROPERTIES));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.PROPERTIES_CUSTOM);

	}

    /**
     * Unmarshal XML data from the specified InputStream and return the 
     * resulting content tree.  Validation event location information may
     * be incomplete when using this form of the unmarshal API.
     *
     * <p>
     * Implements <a href="#unmarshalGlobal">Unmarshal Global Root Element</a>.
     * 
     * @param is the InputStream to unmarshal XML data from
     * @return the newly created root object of the java content tree 
     *
     * @throws JAXBException 
     *     If any unexpected errors occur while unmarshalling
     */
    public Object unmarshal( java.io.InputStream is ) throws JAXBException {
    	
		try {
			
//			if (jc==null) {
//				setJAXBContext(Context.jc);				
//			}
		    		    
			setJAXBContext(org.docx4j.jaxb.Context.jcDocPropsCustom);
			Unmarshaller u = jc.createUnmarshaller();
			
			//u.setSchema(org.docx4j.jaxb.WmlSchema.schema);
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

			System.out.println("unmarshalling " + this.getClass().getName() + " \n\n" );									
						
			jaxbElement = u.unmarshal( is );
			
			
			System.out.println("\n\n" + this.getClass().getName() + " unmarshalled \n\n" );									

		} catch (Exception e ) {
			e.printStackTrace();
		}
    	
		return jaxbElement;
    	
    }
    
    public String getProperty(String propName) {
    	
		// NB, at present this assumes the property is a string

    	org.docx4j.docProps.custom.Properties customProps = (org.docx4j.docProps.custom.Properties)getJaxbElement();
		for (org.docx4j.docProps.custom.Properties.Property prop: customProps.getProperty() ) {
			
			System.out.println(prop.getName());
			
			if (prop.getName().equals(propName)) {
				log.info("GOT IT! returning " + prop.getLpwstr());
				return prop.getLpwstr();
			}
			
		}
		return null;    	
    }
    
    public void setProperty(String propName, String propValue) {
    	
		// NB, at present this assumes the property is a string
    	
		// Ok, let's add one.
		org.docx4j.docProps.custom.ObjectFactory factory = new org.docx4j.docProps.custom.ObjectFactory();
		org.docx4j.docProps.custom.Properties.Property newProp = factory.createPropertiesProperty();
		
		// .. set it up
		newProp.setName("mynewcustomprop");
		newProp.setFmtid(this.fmtidValLpwstr ); // Magic string
		newProp.setPid( getNextPid() ); 
		newProp.setLpwstr("SomeValue");
		
		// .. add it
    	org.docx4j.docProps.custom.Properties customProps = (org.docx4j.docProps.custom.Properties)getJaxbElement();
		customProps.getProperty().add(newProp);
    	
    	
    }
    
    /**
     * Find the first available Pid 
     *  
     * @return
     */
    public int getNextPid() {
    	
    	int highestSeen = 0;
    	
    	org.docx4j.docProps.custom.Properties customProps = (org.docx4j.docProps.custom.Properties)getJaxbElement();
		for (org.docx4j.docProps.custom.Properties.Property prop: customProps.getProperty() ) {			
			if (prop.getPid()>highestSeen) {
				highestSeen =prop.getPid();  				
			}
		}
		log.debug("Returning " +  highestSeen+1);
		return highestSeen+1;    	
    }
	
		
//	/* Create a Java object tree from the XML document 
//	 */
//	public void unmarshall(Document doc) {
//		
//		String name;
//		String value;
//		
//		try {
//			//debugPrint(doc);
//		    Element root = doc.getRootElement();
//		    Iterator elementIterator = root.elementIterator();
//		    while(elementIterator.hasNext()){
//		      Element element = (Element)elementIterator.next();
//		      
//			  if (element.getName()=="property" ) {
//				  
////					<property fmtid="{D5CDD505-2E9C-101B-9397-08002B2CF9AE}" pid="2" name="Grouping">
////						<vt:lpwstr>p</vt:lpwstr>
////					</property>
//				  
//				  name = element.attributeValue("name");
//				  
//				  if (element.element("lpwstr")!=null ) {
//					  value = element.element("lpwstr").getText();
//					  setProperty(name, value);					  
//					  log.info("Registering property " + name + "=" + value);
//				  } else {
//					  	// TODO - handle 
//					  log.warn("Handle " + name );
//					  debugPrint(doc);
//					  
//				  }
//			  } else {
//			      log.warn("Encountered unexpected element '" + element.getName() + "'");
//			  }
//		    }
//		} catch (Exception e ) {
//			e.printStackTrace();
//		}
//		
//	}
//	
//
//		
//	
//	/* Create an XML document from the Java object tree
//	 *  
//	 */
//	private Document marshall() {
//
////		<?xml version="1.0" encoding="UTF-8" standalone="yes"?> 
////		<Properties 
////			xmlns="http://schemas.openxmlformats.org/officeDocument/2006/custom-properties" 
////			xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
////			<property fmtid="{D5CDD505-2E9C-101B-9397-08002B2CF9AE}" pid="2" name="Grouping">
////				<vt:lpwstr>p</vt:lpwstr>
////			</property>
////			<property fmtid="{D5CDD505-2E9C-101B-9397-08002B2CF9AE}" pid="3" name="CheckinComments">
////				<vt:lpwstr>true</vt:lpwstr>
////			</property>
////		</Properties>
//
//		Document doc = DocumentHelper.createDocument();
//
//		// Note carefully how namespaces are handled here.
//		// It seems we have to be syntactically identical to how
//		// Word does it - semantic equivalence is not enough!
//		
//		Namespace docPropsVTypes = new Namespace("vt",
//				"http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes");
//		
//		Element elDocument = doc.addElement("Properties",
//				"http://schemas.openxmlformats.org/officeDocument/2006/custom-properties");
//		elDocument.addNamespace("vt",
//				"http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes");
//
//		Integer pid = 2; // NB @pid="1" will cause Word to regard the document as corrupt.
//		Iterator i = properties.entrySet().iterator();
//		while (i.hasNext()) {
//			Map.Entry e = (Map.Entry)i.next();	
//			Element property = elDocument.addElement("property",
//			"http://schemas.openxmlformats.org/officeDocument/2006/custom-properties");
//			property.addAttribute( "fmtid", fmtidVal);
//			property.addAttribute( "pid", pid.toString() );
//			property.addAttribute( "name", (String)e.getKey() );
//			Element lpwstr = new DefaultElement(new QName("lpwstr", docPropsVTypes));
//			lpwstr.setText( (String)e.getValue() );
//			property.add(lpwstr);
//			pid++;
//		}
//		return doc;
//	}
			
	
}

	
