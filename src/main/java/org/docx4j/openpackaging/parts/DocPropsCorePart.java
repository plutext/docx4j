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
package org.docx4j.openpackaging.parts;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.xpath.XPath;

import org.docx4j.XmlUtils;
import org.docx4j.docProps.core.CoreProperties;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMappings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.utils.XPathFactoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;


public class DocPropsCorePart extends JaxbXmlPart<CoreProperties> {
	
	/*
	 * <?xml version="1.0" encoding="UTF-8" standalone="yes"?> 
	 * <cp:coreProperties 
		 * xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" 
		 * xmlns:dc="http://purl.org/dc/elements/1.1/" 
		 * xmlns:dcterms="http://purl.org/dc/terms/" 
		 * xmlns:dcmitype="http://purl.org/dc/dcmitype/" 
		 * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
		 * <dc:creator>Jason Harrop</dc:creator>
		 * <cp:lastModifiedBy>Jason Harrop</cp:lastModifiedBy>
		 * <cp:revision>2</cp:revision>
		 * <dcterms:created xsi:type="dcterms:W3CDTF">2007-08-12T00:34:00Z</dcterms:created>
		 * <dcterms:modified xsi:type="dcterms:W3CDTF">2007-08-12T00:34:00Z</dcterms:modified>
	 * </cp:coreProperties>
	 */
	
	
	private static Logger log = LoggerFactory.getLogger(DocPropsCorePart.class);
	
	private static XPath xPath = XPathFactoryUtil.newXPath();
	
	 /** 
	 * @throws InvalidFormatException
	 */
	public DocPropsCorePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public DocPropsCorePart() throws InvalidFormatException {
		super(new PartName("/docProps/core.xml"));
		init();
	}
	
	public void init() {
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.PACKAGE_COREPROPERTIES));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.PROPERTIES_CORE);
		
		setJAXBContext(Context.jcDocPropsCore);						
		
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
	@Override
    public CoreProperties unmarshal( java.io.InputStream is ) throws JAXBException {
		
		// TODO: delete this method?
    	
		try {
	        XMLInputFactory xif = XMLInputFactory.newInstance();
	        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
	        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false); // a DTD is merely ignored, its presence doesn't cause an exception
	        XMLStreamReader xsr = xif.createXMLStreamReader(is);												
			
			setJAXBContext(org.docx4j.jaxb.Context.jcDocPropsCore);
			Unmarshaller u = jc.createUnmarshaller();
			
			//u.setSchema(org.docx4j.jaxb.WmlSchema.schema);
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

			log.info("unmarshalling " + this.getClass().getName());									
						
			jaxbElement = (CoreProperties) u.unmarshal( xsr );

		} catch (Exception e ) {
			e.printStackTrace();
		}
    	
		return jaxbElement;
    	
    }

	// Core Props and Extended Props can both be bound as if they
	// were a custom xml part.  Copied from XmlPart.
	
	public String xpathGetString(String xpathString, String prefixMappings)  throws Docx4JException {
		
		Document doc = XmlUtils.marshaltoW3CDomDocument(
				getJaxbElement(), Context.jcDocPropsCore );
		
		try {
			String result;
			synchronized(xPath) {
				getNamespaceContext().registerPrefixMappings(prefixMappings);
				result = xPath.evaluate(xpathString, doc );
			}
			log.debug(xpathString + " ---> " + result);
			return result;
		} catch (Exception e) {
			throw new Docx4JException("Problems evaluating xpath '" + xpathString + "'", e);
		}
	}
	private NamespacePrefixMappings nsContext;
	private NamespacePrefixMappings getNamespaceContext() {
		if (nsContext==null) {
			nsContext = new NamespacePrefixMappings();
			xPath.setNamespaceContext(nsContext);
		}
		return nsContext;
	}
    
	
}

	
