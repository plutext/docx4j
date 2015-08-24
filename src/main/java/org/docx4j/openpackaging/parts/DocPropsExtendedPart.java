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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.XmlUtils;
import org.docx4j.docProps.extended.Properties;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMappings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.utils.XPathFactoryUtil;
import org.w3c.dom.Document;




public class DocPropsExtendedPart extends JaxbXmlPart<Properties> {
	
	/*
	 * <?xml version="1.0" encoding="UTF-8" standalone="yes"?> 
	 * <Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" 
	 * xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
		 * <Template>Normal.dotm</Template>
		 * <TotalTime>0</TotalTime>
		 * <Pages>1</Pages><Words>3</Words><Characters>20</Characters>
		 * <Application>Microsoft Office Word</Application>
		 * <DocSecurity>0</DocSecurity>
		 * <Lines>1</Lines><Paragraphs>1</Paragraphs>
		 * <ScaleCrop>false</ScaleCrop>
		 * <Company>Plutext Pty Ltd</Company>
		 * <LinksUpToDate>false</LinksUpToDate><CharactersWithSpaces>22</CharactersWithSpaces>
		 * <SharedDoc>false</SharedDoc>
		 * <HyperlinksChanged>false</HyperlinksChanged>
		 * <AppVersion>12.0000</AppVersion>
	 * </Properties>
	 */
	
	
	private static Logger log = LoggerFactory.getLogger(DocPropsExtendedPart.class);
	
	private static XPath xPath = XPathFactoryUtil.newXPath();
	
	 /** 
	 * @throws InvalidFormatException
	 */
	public DocPropsExtendedPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public DocPropsExtendedPart() throws InvalidFormatException {
		super(new PartName("/docProps/app.xml"));
		init();
	}
	
	public void init() {
		
		setJAXBContext(Context.jcDocPropsExtended);
		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.OFFICEDOCUMENT_EXTENDEDPROPERTIES));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.PROPERTIES_EXTENDED);
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
    public Properties unmarshal( java.io.InputStream is ) throws JAXBException {
		
		// TODO: delete this method?		
    	
		try {
			
	        XMLInputFactory xif = XMLInputFactory.newInstance();
	        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
	        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false); // a DTD is merely ignored, its presence doesn't cause an exception
	        XMLStreamReader xsr = xif.createXMLStreamReader(is);												
			
//			if (jc==null) {
//				setJAXBContext(Context.jc);				
//			}
		    		    
			setJAXBContext(org.docx4j.jaxb.Context.jcDocPropsExtended);
			Unmarshaller u = jc.createUnmarshaller();
			
			//u.setSchema(org.docx4j.jaxb.WmlSchema.schema);
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

			log.info("unmarshalling " + this.getClass().getName() );									
			jaxbElement = (Properties) u.unmarshal( xsr );

		} catch (Exception e ) {
			e.printStackTrace();
		}
    	
		return jaxbElement;
    	
    }

	// Core Props and Extended Props can both be bound as if they
	// were a custom xml part.  Copied from XmlPart.
	
	public String xpathGetString(String xpathString, String prefixMappings)  throws Docx4JException {
		
		Document doc = XmlUtils.marshaltoW3CDomDocument(
				getJaxbElement(), Context.jcDocPropsExtended );
		
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
	
	/**
	 * @param val
	 * @since 3.3.0
	 */
	public void setDocSecurity(int val) {
		
		// See http://webapp.docx4java.org/OnlineDemo/ecma376/SharedML/DocSecurity.html
		// It is set by Word 2013 restrict editing read only, or comments, to 8.
		
		// May also be set by Excel to something other than 0
		// for for certain protect sheet, protect workbook values,
		// but not for the default values, so nothing is implemented in xlsx4j yet


		if (this.getJaxbElement()==null) {
			this.setJaxbElement(new Properties());
		}
		this.getJaxbElement().setDocSecurity(val);
		
	}
	
	
}

	
