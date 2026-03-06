/*
 * Copyright 2011 Plutext Pty Ltd and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.docx4j.model.datastorage;

import static org.docx4j.XmlUtils.marshaltoW3CDomDocument;
import static org.docx4j.XmlUtils.prepareJAXBResult;
import static org.docx4j.XmlUtils.transform;
import static org.docx4j.model.datastorage.RemovalHandler.Quantifier.ALL;
import static org.docx4j.model.datastorage.RemovalHandler.Quantifier.ALL_BUT_PLACEHOLDERS;
import static org.docx4j.model.datastorage.RemovalHandler.Quantifier.ALL_BUT_PLACEHOLDERS_CONTENT;
import static org.docx4j.model.datastorage.RemovalHandler.Quantifier.NAMED;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.util.JAXBResult;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.ArrayUtils;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.utils.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Tool to remove content controls (Structured Document Tags) from an OpenXML document part.
 * 
 * <p>
 * This tool removes SDTs tagged with a certain quantifier from the document part.
 * </p>
 * <p>
 * Restrictions:
 * </p>
 * <ul>
 * <li>
 * As it does text processing and no real namespace qualification exist, the quantifiers must be prefixed exactly be
 * "od", not any other prefix.</li>
 * <li>
 * In case of qualified removal (in effect not {@link Quantifier#ALL}), bindings containing more than one qualifier are
 * not supported, that is, when you tag <code>od:repeat=/this&amp;od:xpath=/that</code>, the SDT is removed whenever you
 * specify to remove either repeat or bind tags. (multiple qualifiers are not recommended in any case!)</li>
 * </ul>
 *
 * @author Karsten Tinnefeld
 * @version $Revision: $ $Date: $
 */
public class RemovalHandler {

		private static Logger log = LoggerFactory.getLogger(RemovalHandler.class);		
	
    	static Templates removalTemplate;			

        /**
         * Initializes the removal handler.
         *
         * This tool is thread safe and should be reused, as initialization is
         * relatively expensive.
         */
        public RemovalHandler() {

    		try {
    			final Source xsltSource = new StreamSource(
    						ResourceUtils.getResourceViaProperty(
    								"docx4j.model.datastorage.RemovalHandler.xslt",
    								"org/docx4j/model/datastorage/RemovalHandler.xslt"));
    			removalTemplate = XmlUtils.getTransformerTemplate(xsltSource);

            } catch (Exception e) {
                    throw new IllegalStateException(
                                    "Error instantiating SDT removal stylesheet", e);
            }
        }

        
        /**
         * Removes Structured Document Tags from the main document part, headers, and footer, 
         * preserving their contents.
         *
         * @param wordMLPackage
         *            The docx package to modify (in situ).
         *            
         * @throws Docx4JException
         *             In case any transformation error occurs.
         * @since 6.1.0
         */
        public void removeSDTs(WordprocessingMLPackage wordMLPackage) throws Docx4JException {

        	removeSDTs(wordMLPackage, getQuantifier() , (String[])null);
        }
        
        /**
         * Removes Structured Document Tags from the main document part, headers, and footer, 
         * preserving their contents.
         *
         * In case key "empty" is specified, value bindings (xpath) are removed only
         * if they have void contents (e.g. the XML points nowhere).
         *
         * @param wordMLPackage
         *            The docx package to modify (in situ).
         * @param quantifier
         *            The quantifier regarding which kinds of parts are to be
         *            removed.
         * @param keys
         *            In case of {@link Quantifier#NAMED}, quantifier names. All
         *            strings except "xpath", "condition", "repeat", "empty" are
         *            ignored.
         * @throws Docx4JException
         *             In case any transformation error occurs.
         */
        public void removeSDTs(WordprocessingMLPackage wordMLPackage,
                        final Quantifier quantifier, final String... keys) throws Docx4JException {

                removeSDTs(wordMLPackage.getMainDocumentPart(), quantifier, keys);

                // Remove from headers/footers
                RelationshipsPart rp = wordMLPackage.getMainDocumentPart()
                                .getRelationshipsPart();
                for (Relationship r : rp.getRelationships().getRelationship()) {

                        if (r.getType().equals(Namespaces.HEADER)) {
                                removeSDTs((HeaderPart) rp.getPart(r), quantifier, keys);
                        } else if (r.getType().equals(Namespaces.FOOTER)) {
                                removeSDTs((FooterPart) rp.getPart(r), quantifier, keys);
                        }
                }
        }

        
        /**
         * Removes Structured Document Tags from a document part, preserving their
         * contents.
         *
         * In case key "empty" is specified, value bindings (xpath) are removed only
         * if they have void contents (e.g. the XML points nowhere).
         *
         * @param part
         *            The document part to modify (in situ).
         * @param quantifier
         *            The quantifier regarding which kinds of parts are to be
         *            removed.
         * @param keys
         *            In case of {@link Quantifier#NAMED}, quantifier names. All
         *            strings except "xpath", "condition", "repeat", "empty" are
         *            ignored.
         * @throws Docx4JException
         *             In case any transformation error occurs.
         */
        public void removeSDTs(final JaxbXmlPart<? extends Object> part)
                        throws Docx4JException {

        	removeSDTs(part, getQuantifier() , (String[])null);
        }
        
        /**
         * Removes Structured Document Tags from a document part, preserving their
         * contents.
         *
         * In case key "empty" is specified, value bindings (xpath) are removed only
         * if they have void contents (e.g. the XML points nowhere).
         *
         * @param part
         *            The document part to modify (in situ).
         * @param quantifier
         *            The quantifier regarding which kinds of parts are to be
         *            removed.
         * @param keys
         *            In case of {@link Quantifier#NAMED}, quantifier names. All
         *            strings except "xpath", "condition", "repeat", "empty" are
         *            ignored.
         * @throws Docx4JException
         *             In case any transformation error occurs.
         */
        public void removeSDTs(final JaxbXmlPart<? extends Object> part,
                        final Quantifier quantifier, final String... keys)
                        throws Docx4JException {

                final Map<String, Object> parameters = new HashMap<String, Object>();
                parameters.put("all", quantifier == ALL);
                parameters.put("all_but_placeholders", quantifier == ALL_BUT_PLACEHOLDERS);
                parameters.put("all_but_placeholders_content", quantifier == ALL_BUT_PLACEHOLDERS_CONTENT);
                if (quantifier == NAMED)
                        parameters.put("types", ArrayUtils.toString(keys));

    			javax.xml.transform.Source source = null;		
    			javax.xml.transform.Result result = null;
    			
    			// If we're using a StAXSource
    			XMLStreamReader xmlReader = null;
//    	        XMLStreamWriter xmlWriter = null; 
    	        ByteArrayOutputStream baos = null;
    	        
    	        org.w3c.dom.Document doc = null;
    			if ( ((JaxbXmlPart)part).isUnmarshalled() 
    				||  /* don't want to use StAX */ !Docx4jProperties.getProperty("docx4j.model.datastorage.BindingHandler.Implementation", "BindingTraverserXSLT").equals("BindingTraverserStAX"))  {
    				
    				log.debug( ((JaxbXmlPart)part).getPartName().getName() + " not using StAX.");		
    				doc = XmlUtils.marshaltoW3CDomDocument(
    					part.getJaxbElement() );
    				source = new javax.xml.transform.dom.DOMSource(doc);				
    				result = prepareJAXBResult(Context.jc);
    				
    			} else {
    				log.debug( ((JaxbXmlPart)part).getPartName().getName() + " not yet unmarshalled; using StAX.");
    				try {
    					xmlReader = part.getXMLStreamReader(null);
    					source = new StAXSource(xmlReader);
//    			        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();            
    			        baos = new ByteArrayOutputStream(); 
//    					xmlWriter = outputFactory.createXMLStreamWriter(baos, "UTF-8");	
    					result = new StreamResult(baos);  // Xalan TransformerImpl doesn't support StAXResult: https://issues.apache.org/jira/browse/XALANJ-2550 
//    					result = new StAXResult(xmlWriter);
    				} catch (Exception e) {
    					throw new Docx4JException(e.getMessage(), e);
    				}
    			}

    			try {
    				transform(source, removalTemplate, parameters, result);

    				if (result instanceof JAXBResult) {
    					
    					try {
    						((JaxbXmlPart)part).setJaxbElement(((JAXBResult)result).getResult());

    						// this will fail if there is unexpected content, 
    						// since JaxbValidationEventHandler fails by default
    						
    					} catch (Exception e) {
    	
    						log.error(e.getMessage(), e);				
    						log.error("Input in question:" + XmlUtils.w3CDomNodeToString(doc));				
    						log.error("Now trying DOMResult..");				
    						
    						result = new DOMResult(); 
    						org.docx4j.XmlUtils.transform(doc, removalTemplate, parameters,  result);
    	
    						if (log.isDebugEnabled()) {
    							
    							org.w3c.dom.Document docResult = ((org.w3c.dom.Document)((DOMResult)result).getNode());
    							
    							//log.debug("After ODI: " + XmlUtils.w3CDomNodeToString(docResult));
    							
    							Object o = XmlUtils.unmarshal(((org.w3c.dom.Document)((DOMResult)result).getNode()) );
    							((JaxbXmlPart)part).setJaxbElement(o);
    						} else 
    						{
    							//part.unmarshal( ((org.w3c.dom.Document)result.getNode()).getDocumentElement() );
    							Object o = XmlUtils.unmarshal(((org.w3c.dom.Document)((DOMResult)result).getNode()) );
    							((JaxbXmlPart)part).setJaxbElement(o);
    						}
    					}
    						
    				} else {
    			        
    			        try {
    				        xmlReader.close();
    				        baos.flush();
    				        if (log.isDebugEnabled() ) {
    				        	byte[] bytes = baos.toByteArray();
    				        	log.debug(new String(bytes));
    				        	((JaxbXmlPart)part).replacePartContent(bytes);
    				        } else {
    				        	((JaxbXmlPart)part).replacePartContent(baos.toByteArray());
    				        }
    				        baos.close(); 
    					} catch (Exception e) {
    						throw new Docx4JException(e.getMessage(), e);				
    					}
    					
    				}					
    				
    			} catch (Exception e) {
    				
    				throw new Docx4JException("Error unmarshalling document part for SDT removal", e);			
    			}
    					
    		}
        
        
        private Quantifier defaultQuantifier = null;
        private Quantifier getQuantifier() {
        	
        	if (defaultQuantifier!=null) return defaultQuantifier;
        	
        	String q = Docx4jProperties.getProperty("docx4j.model.datastorage.RemovalHandler.Quantifier", "ALL");
        	
        	if (q.equals("ALL")) {
        		defaultQuantifier = Quantifier.ALL;
        	} else if (q.equals("ALL_BUT_PLACEHOLDERS")) {
        		defaultQuantifier = Quantifier.ALL_BUT_PLACEHOLDERS;
        	} else if (q.equals("ALL_BUT_PLACEHOLDERS_CONTENT")) {
        		defaultQuantifier = Quantifier.ALL_BUT_PLACEHOLDERS_CONTENT;
        	} else if (q.equals("DEFAULT")) {
        		defaultQuantifier = Quantifier.DEFAULT;
        	} else if (q.equals("NAMED")) {
        		defaultQuantifier = Quantifier.NAMED;
        	} else {
        		log.warn("Unknown Quantifier property value: " + q);
        		defaultQuantifier = Quantifier.ALL;        		
        	}
        	
        	return defaultQuantifier;
        }

        /**
         * A quantifier specifying kinds of SDTs.
         */
        public static enum Quantifier {

                /**
                 * Every SDT shall be removed.  From 3.3.0, this really means all SDTs in the main document part.
                 * If an SDT does not contain any 'real' content, then remove that XML as well.
                 */
                ALL,

                /**
                 * Ordinarily, if an SDT contains XML but no real content, 
                 * that XML is also removed.  
                 * Choose this option if you want to keep placeholder XML (but remove the SDT).
                 * Currently, to be identified as a placeholder, it must use rStyle 'PlaceholderText'.
                 * @since 6.1.1 
                 */
                ALL_BUT_PLACEHOLDERS_CONTENT,
                
                /**
                 * Ordinarily, if an SDT contains XML but no real content, 
                 * that XML is also removed.  
                 * Choose this option if you want to keep placeholder XML (keeping the SDT as well).
                 * Currently, to be identified as a placeholder, it must use rStyle 'PlaceholderText'.
                 * @since 6.1.0 
                 */
                ALL_BUT_PLACEHOLDERS,
                
                /**
                 * The default SDTs shall be removed, that is, condition and repeat.
                 * 
                 * (If you want to remove xpaths, either use Quantifier.ALL, or pass key "xpath")
                 */
                DEFAULT,
                
                /**
                 * Named SDTs shall be removed, the names given separately.
                 */
                NAMED;                
        }
}
