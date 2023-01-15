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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	@Override
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, byte[] bytes)  throws Docx4JException {
		return addAltChunk(type, bytes, -1); 
	}
	
	/* (non-Javadoc)
	 * @see org.docx4j.openpackaging.parts.WordprocessingML.AltChunkInterface#addAltChunkOfTypeHTML(byte[])
	 */
	@Override
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, byte[] bytes, int index)  throws Docx4JException {
		
		AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(type); 
		Relationship altChunkRel = this.addTargetPart(afiPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS); 
		// now that its attached to the package ..
		afiPart.registerInContentTypeManager();
		
		afiPart.setBinaryData(bytes); 		
		
		// .. the bit in document body 
		CTAltChunk ac = Context.getWmlObjectFactory().createCTAltChunk(); 
		ac.setId(altChunkRel.getId() ); 
		if (this instanceof ContentAccessor) {
			if (index<0) {
				((ContentAccessor)this).getContent().add(ac);
			} else {
				((ContentAccessor)this).getContent().add(index, ac);				
			}
		} else {
			throw new Docx4JException(this.getClass().getName() + " doesn't implement ContentAccessor");
		}
		
		return afiPart;
	}

	@Override
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, InputStream is)   throws Docx4JException {
		return addAltChunk(type, is, -1); 
	}
	
	/* (non-Javadoc)
	 * @see org.docx4j.openpackaging.parts.WordprocessingML.AltChunkInterface#addAltChunkOfTypeHTML(java.io.InputStream)
	 */
	@Override
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, InputStream is, int index)   throws Docx4JException {
		
		AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(type); 
		Relationship altChunkRel = this.addTargetPart(afiPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS); 
		// now that its attached to the package ..
		afiPart.registerInContentTypeManager();		
		
		afiPart.setBinaryData(is); 
		
		// .. the bit in document body 
		CTAltChunk ac = Context.getWmlObjectFactory().createCTAltChunk(); 
		ac.setId(altChunkRel.getId() ); 
		if (this instanceof ContentAccessor) {
			if (index<0) {
				((ContentAccessor)this).getContent().add(ac);
			} else {
				((ContentAccessor)this).getContent().add(index, ac);				
			}
		} else {
			throw new Docx4JException(this.getClass().getName() + " doesn't implement ContentAccessor");
		}
		
		return afiPart;
	}

	@Override
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, byte[] bytes,
			ContentAccessor attachmentPoint)   throws Docx4JException {
		return addAltChunk(type, bytes, attachmentPoint, -1); 

	}
	
	/* (non-Javadoc)
	 * @see org.docx4j.openpackaging.parts.WordprocessingML.AltChunkInterface#addAltChunkOfTypeHTML(byte[], org.docx4j.wml.ContentAccessor)
	 */
	@Override
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, byte[] bytes,
			ContentAccessor attachmentPoint, int index)   throws Docx4JException {
		
		AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(type); 
		Relationship altChunkRel = this.addTargetPart(afiPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS); 
		// now that its attached to the package ..
		afiPart.registerInContentTypeManager();
		
		afiPart.setBinaryData(bytes); 		
		
		// .. the bit in document body 
		CTAltChunk ac = Context.getWmlObjectFactory().createCTAltChunk(); 
		ac.setId(altChunkRel.getId() ); 
		if (index<0) {
			attachmentPoint.getContent().add(ac);
		} else {
			attachmentPoint.getContent().add(index, ac);				
		}
					
		return afiPart;
	}

	@Override
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, InputStream is,
			ContentAccessor attachmentPoint) throws Docx4JException {
		return addAltChunk(type, is, attachmentPoint, -1); 
	} 
	
	/* (non-Javadoc)
	 * @see org.docx4j.openpackaging.parts.WordprocessingML.AltChunkInterface#addAltChunkOfTypeHTML(java.io.InputStream, org.docx4j.wml.ContentAccessor)
	 */
	@Override
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, InputStream is,
			ContentAccessor attachmentPoint, int index) throws Docx4JException {
		
		AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(type); 
		Relationship altChunkRel = this.addTargetPart(afiPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS); 
		// now that its attached to the package ..
		afiPart.registerInContentTypeManager();		
		
		afiPart.setBinaryData(is); 
		
		// .. the bit in document body 
		CTAltChunk ac = Context.getWmlObjectFactory().createCTAltChunk(); 
		ac.setId(altChunkRel.getId() ); 
		if (index<0) {
			attachmentPoint.getContent().add(ac);
		} else {
			attachmentPoint.getContent().add(index, ac);				
		}
					
		return afiPart;
	}

		
	@SuppressWarnings("unchecked")
	@Override
	public void convertAltChunks() throws Docx4JException {
		
		if (!(this instanceof ContentAccessor)) {
				throw new Docx4JException(this.getClass().getName() + " doesn't implement ContentAccessor");
		}	
		PartName partName = this.getPartName();
		
		List<Object> contentList = ((ContentAccessor)this).getContent();
		
	    AltChunkFinder bf = new AltChunkFinder();
		new TraversalUtil(contentList, bf);

		CTAltChunk altChunk;
		boolean encounteredDocxAltChunk = false;
		log.info("Detected " + bf.getAltChunks().size() );
		for (int i = bf.getAltChunks().size() - 1; i >= 0; i--) {
			LocatedChunk locatedChunk = bf.getAltChunks().get(i);
			
			boolean deleteThisAltChunk = false;
			
			altChunk = locatedChunk.getAltChunk();
			AlternativeFormatInputPart afip 
				=  (AlternativeFormatInputPart)this.getRelationshipsPart().getPart(
						altChunk.getId() );
			
			// Can we process it?
			AltChunkType type = afip.getAltChunkType();

			if (type==null ) {
				
				log.warn("Unrecognized type for part " + afip.getPartName().getName() );
				
			} else if (type.equals(AltChunkType.Xhtml)
					|| type.equals(AltChunkType.Mht)  ) {
				
				Object xHTMLImporter= null;
				Method convertMethod = null;
			    try {
			    	Class<?> xhtmlImporterClass = Class.forName("org.docx4j.convert.in.xhtml.XHTMLImporterImpl");
				    Constructor<?> ctor = xhtmlImporterClass.getConstructor(WordprocessingMLPackage.class);
				    xHTMLImporter = ctor.newInstance(this.getPackage()); // OpcPackage, but that's ok.
				    if (type.equals(AltChunkType.Xhtml)) {
				    	convertMethod = xhtmlImporterClass.getMethod("convert", InputStream.class, String.class );
				    } else {
				    	convertMethod = xhtmlImporterClass.getMethod("convertMHT", InputStream.class, String.class );				    	
				    }
			        
			        if (convertMethod==null) {
			        	throw new Docx4JException("XHTMLImporterImpl convertMethod not found");
			        }
			    } catch (Exception e) {
			        log.warn("docx4j-XHTMLImport jar not found. Please add this to your classpath.");
					log.warn(e.getMessage(), e);
					return;
			    }		
				
	            List<Object> results = null;
				try {
					// results = xHTMLImporter.convert(toString(afip.getBuffer()), null);
					ByteArrayInputStream bais = new ByteArrayInputStream(afip.getBytes());
			        results = (List<Object>)convertMethod.invoke(xHTMLImporter, bais, null);					
					
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					// Skip this one
					continue;
				} 
				
				int index = locatedChunk.getIndex(); 
				locatedChunk.getContentList().remove(index); // handles case where it is nested eg in a tc
				locatedChunk.getContentList().addAll(index, results);	
				
				log.info(afip.getPartName().getName() + "; Converted altChunk of type XHTML ");
				
				deleteThisAltChunk = true;
				
			} else if (type.equals(AltChunkType.Xml) ) {
//				log.warn("Skipping altChunk of type XML "); // what does Word do??
				// Assume its Flat OPC XML
				log.warn(afip.getPartName().getName() + " is AltChunkType.Xml; assuming this is Flat OPC XML, use Docx4j Enterprise MergeDocx to convert this." );				
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
					
					
					log.info(afip.getPartName().getName() + "; Converted altChunk of type text ");
					deleteThisAltChunk = true;
				}

			} else if (type.equals(AltChunkType.WordprocessingML)
					 || type.equals(AltChunkType.OfficeWordMacroEnabled)
					 || type.equals(AltChunkType.OfficeWordTemplate)
					 ||type.equals(AltChunkType.OfficeWordMacroEnabledTemplate) ) {
				
				log.warn(afip.getPartName().getName() + " is " + type.getContentType() + "; use Docx4j Enterprise MergeDocx to convert this." );				
				continue;
				
			} else if (type.equals(AltChunkType.Rtf) ) {
				log.warn(afip.getPartName().getName() + "; skipping altChunk of type RTF ");
				continue;
			} else if (type.equals(AltChunkType.Html) ) {
				log.warn(afip.getPartName().getName() + "; skipping altChunk of type HTML ");
				continue;
				// if there was a pretty printer on class path,
				// could use it via reflection?
			}
					
			
			if (deleteThisAltChunk) {
				// Now delete the original part				
				this.getRelationshipsPart().removePart(afip.getPartName());
			}
		}
		
	}
	
	private void extensionMissing(Exception e) {
		log.warn("\n" + e.getClass().getName() + ": " + e.getMessage() + "\n");
		log.warn("* Skipping altChunk of type docx ");
		log.warn("* You don't appear to have the MergeDocx extension,");
		log.warn("* which is necessary to merge docx, or process altChunk.");
		log.warn("* MergeDocx is part of the Enterprise Edition of docx4j.");
		log.warn("* Please email sales@plutext.com or visit www.plutext.com if you want to try it.");
	}
	
	private String toString(ByteBuffer bb) throws UnsupportedEncodingException {

		byte[] bytes = null;
        bytes = new byte[bb.limit()];
        bb.get(bytes);	        				
		return new String(bytes, "UTF-8");
	}
	
}
