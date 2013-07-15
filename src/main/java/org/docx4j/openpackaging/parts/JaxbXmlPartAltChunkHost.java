/**
 *  Copyright 2012, Plutext Pty Ltd.
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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.List;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.dom.DOMResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.JAXBAssociation;
import org.docx4j.jaxb.JaxbValidationEventHandler;
import org.docx4j.jaxb.XPathBinderAssociationIsPartialException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io3.stores.PartStore;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkInterface;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;
import org.docx4j.relationships.Relationship;
import org.docx4j.utils.AltChunkFinder;
import org.docx4j.utils.AltChunkFinder.LocatedChunk;
import org.docx4j.wml.CTAltChunk;
import org.docx4j.wml.ContentAccessor;
import org.w3c.dom.Node;

/**
 * @author jharrop
 * @since 3.0.0
 */
public abstract class JaxbXmlPartAltChunkHost<E> extends JaxbXmlPartXPathAware<E> implements AltChunkInterface {
	
	protected static Logger log = LoggerFactory.getLogger(JaxbXmlPartAltChunkHost.class);

	public JaxbXmlPartAltChunkHost(PartName partName)
			throws InvalidFormatException {
		super(partName);
		// TODO Auto-generated constructor stub
	}


	/* (non-Javadoc)
	 * @see org.docx4j.openpackaging.parts.WordprocessingML.AltChunkInterface#addAltChunkOfTypeHTML(byte[])
	 */
	@Override
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, byte[] bytes)  throws Docx4JException {
		
		AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(type); 
		Relationship altChunkRel = this.addTargetPart(afiPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS); 
		// now that its attached to the package ..
		afiPart.registerInContentTypeManager();
		
		afiPart.setBinaryData(bytes); 		
		
		// .. the bit in document body 
		CTAltChunk ac = Context.getWmlObjectFactory().createCTAltChunk(); 
		ac.setId(altChunkRel.getId() ); 
		if (this instanceof ContentAccessor) {
		 ((ContentAccessor)this).getContent().add(ac); 
		} else {
			throw new Docx4JException(this.getClass().getName() + " doesn't implement ContentAccessor");
		}
		
		return afiPart;
	}

	/* (non-Javadoc)
	 * @see org.docx4j.openpackaging.parts.WordprocessingML.AltChunkInterface#addAltChunkOfTypeHTML(java.io.InputStream)
	 */
	@Override
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, InputStream is)   throws Docx4JException {
		
		AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(type); 
		Relationship altChunkRel = this.addTargetPart(afiPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS); 
		// now that its attached to the package ..
		afiPart.registerInContentTypeManager();		
		
		afiPart.setBinaryData(is); 
		
		// .. the bit in document body 
		CTAltChunk ac = Context.getWmlObjectFactory().createCTAltChunk(); 
		ac.setId(altChunkRel.getId() ); 
		if (this instanceof ContentAccessor) {
		 ((ContentAccessor)this).getContent().add(ac); 
		} else {
			throw new Docx4JException(this.getClass().getName() + " doesn't implement ContentAccessor");
		}
		
		return afiPart;
	}

	/* (non-Javadoc)
	 * @see org.docx4j.openpackaging.parts.WordprocessingML.AltChunkInterface#addAltChunkOfTypeHTML(byte[], org.docx4j.wml.ContentAccessor)
	 */
	@Override
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, byte[] bytes,
			ContentAccessor attachmentPoint)   throws Docx4JException {
		
		AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(type); 
		Relationship altChunkRel = this.addTargetPart(afiPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS); 
		// now that its attached to the package ..
		afiPart.registerInContentTypeManager();
		
		afiPart.setBinaryData(bytes); 		
		
		// .. the bit in document body 
		CTAltChunk ac = Context.getWmlObjectFactory().createCTAltChunk(); 
		ac.setId(altChunkRel.getId() ); 
		attachmentPoint.getContent().add(ac);
					
		return afiPart;
	}

	/* (non-Javadoc)
	 * @see org.docx4j.openpackaging.parts.WordprocessingML.AltChunkInterface#addAltChunkOfTypeHTML(java.io.InputStream, org.docx4j.wml.ContentAccessor)
	 */
	@Override
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, InputStream is,
			ContentAccessor attachmentPoint) throws Docx4JException {
		
		AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(type); 
		Relationship altChunkRel = this.addTargetPart(afiPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS); 
		// now that its attached to the package ..
		afiPart.registerInContentTypeManager();		
		
		afiPart.setBinaryData(is); 
		
		// .. the bit in document body 
		CTAltChunk ac = Context.getWmlObjectFactory().createCTAltChunk(); 
		ac.setId(altChunkRel.getId() ); 
		attachmentPoint.getContent().add(ac);
					
		return afiPart;
	}

	/* (non-Javadoc)
	 * @see org.docx4j.openpackaging.parts.WordprocessingML.AltChunkInterface#processAltChunksOfTypeHTML()
	 */
	/**
	 * To convert an altChunk of type XHTML, this method requires docx4j-XHTMLImport.jar (LGPL) and its dependencies.
	 * */
	@Override
	public WordprocessingMLPackage convertAltChunks() throws Docx4JException {
		
		// TODO: Currently only processes AltChunks in main document part.

		if (!(this instanceof ContentAccessor)) {
				throw new Docx4JException(this.getClass().getName() + " doesn't implement ContentAccessor");
		}	
		PartName partName = this.getPartName();
				
		WordprocessingMLPackage clonePkg = (WordprocessingMLPackage)this.getPackage().clone(); // consistent with MergeDocx approach
		JaxbXmlPartAltChunkHost clonedPart = (JaxbXmlPartAltChunkHost)clonePkg.getParts().get(partName); 
				
		List<Object> contentList = ((ContentAccessor)clonedPart).getContent();
		
	    AltChunkFinder bf = new AltChunkFinder();
		new TraversalUtil(contentList, bf);

		CTAltChunk altChunk;
		boolean encounteredDocxAltChunk = false;
		for (LocatedChunk locatedChunk : bf.getAltChunks()) {
			
			altChunk = locatedChunk.getAltChunk();
			AlternativeFormatInputPart afip 
				=  (AlternativeFormatInputPart)clonedPart.getRelationshipsPart().getPart(
						altChunk.getId() );
			
			// Can we process it?
			AltChunkType type = afip.getAltChunkType();

			if (type.equals(AltChunkType.Xhtml) ) {
				
				Class<?> xhtmlImporterClass;
	            List<Object> results = null;
			    try {
			        xhtmlImporterClass = Class.forName("org.docx4j.convert.in.xhtml.XHTMLImporter");
			    } catch (ClassNotFoundException e) {
			        log.error("docx4j-XHTMLImport jar not found. Please add this to your classpath.");
					log.error(e.getMessage(), e);
					// Skip this one
					continue;
			    }				
				try {
					
					// results = XHTMLImporter.convert(toString(afip.getBuffer()), null, clonePkg);
			        Method convertMethod = xhtmlImporterClass.getMethod("convert", String.class, String.class, WordprocessingMLPackage.class );
			        results = (List<Object>)convertMethod.invoke(null, toString(afip.getBuffer()), null, clonePkg);
					
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					// Skip this one
					continue;
				} 
				
				int index = locatedChunk.getIndex(); 
				locatedChunk.getContentList().remove(index); // handles case where it is nested eg in a tc
				locatedChunk.getContentList().addAll(index, results);	
				
				log.info("Converted altChunk of type XHTML ");
				
			} else if (type.equals(AltChunkType.Mht) ) {
				log.warn("Skipping altChunk of type MHT ");
				continue;
			} else if (type.equals(AltChunkType.Xml) ) {
				log.warn("Skipping altChunk of type XML "); // what does Word do??
				continue;
			} else if (type.equals(AltChunkType.TextPlain) ) {
				
				String result= null;
				try {
					result = toString(afip.getBuffer());
				} catch (UnsupportedEncodingException e) {
					log.error(e.getMessage(), e);
					// Skip this one
					continue;
				}
				
				if (result!=null) {
					int index = locatedChunk.getIndex();
					locatedChunk.getContentList().remove(index); // handles case where it is nested eg in a tc
					
					org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
					org.docx4j.wml.P  para = factory.createP();
					locatedChunk.getContentList().add(index, para);	
				
					org.docx4j.wml.R  run = factory.createR();
					para.getContent().add(run);

					org.docx4j.wml.Text  t = factory.createText();
					t.setValue(result);
					run.getContent().add(t);		
					
					
					log.info("Converted altChunk of type text ");
				}

			} else if (type.equals(AltChunkType.WordprocessingML)
					 || type.equals(AltChunkType.OfficeWordMacroEnabled)
					 || type.equals(AltChunkType.OfficeWordTemplate)
					 ||type.equals(AltChunkType.OfficeWordMacroEnabledTemplate) ) {
				encounteredDocxAltChunk = true;
				continue;
				
			} else if (type.equals(AltChunkType.Rtf) ) {
				log.warn("Skipping altChunk of type RTF ");
				continue;
			} else if (type.equals(AltChunkType.Html) ) {
				log.warn("Skipping altChunk of type HTML ");
				continue;
				// if there was a pretty printer on class path,
				// could use it via reflection?
			}
						
		}
		
		if (encounteredDocxAltChunk) {
			
			// Docx AltChunks are handled by MergeDocx, if available
			try {
				// Use reflection, so docx4j can be built
				// by users who don't have the MergeDocx utility
				Class<?> documentBuilder = Class.forName("com.plutext.merge.ProcessAltChunk");			
				//Method method = documentBuilder.getMethod("merge", wmlPkgList.getClass());			
				Method[] methods = documentBuilder.getMethods(); 
				Method method = null;
				for (int j=0; j<methods.length; j++) {
					System.out.println(methods[j].getName());
					if (methods[j].getName().equals("process")) {
						method = methods[j];
						break;
					}
				}			
				if (method==null) {
					// User doesn't have MergeDocx
					throw new NoSuchMethodException();
				}
				
				// User has MergeDocx
				return (WordprocessingMLPackage)method.invoke(null, clonePkg);
				
			} catch (SecurityException e) {
				e.printStackTrace();
				log.warn("* Skipping altChunk of type docx ");
				return clonePkg;
			} catch (ClassNotFoundException e) {
				extensionMissing(e);
				return clonePkg;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				log.warn("* Skipping altChunk of type docx ");
				return clonePkg;
			} catch (NoSuchMethodException e) {
				extensionMissing(e);
				return clonePkg;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				log.warn("* Skipping altChunk of type docx ");
				return clonePkg;
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				log.warn("* Skipping altChunk of type docx ");
				return clonePkg;
			} 
			
		} else {
			return clonePkg;
		}
	}
	
	private void extensionMissing(Exception e) {
		log.warn("\n" + e.getClass().getName() + ": " + e.getMessage() + "\n");
		log.warn("* Skipping altChunk of type docx ");
		log.warn("* You don't appear to have the MergeDocx paid extension,");
		log.warn("* which is necessary to merge docx, or process altChunk.");
		log.warn("* Purchases of this extension support the docx4j project.");
		log.warn("* Please email sales@plutext.com or visit www.plutext.com if you want to buy it.");
	}
	
	private String toString(ByteBuffer bb) throws UnsupportedEncodingException {

		byte[] bytes = null;
        bytes = new byte[bb.limit()];
        bb.get(bytes);	        				
		return new String(bytes, "UTF-8");
	}
	
}
