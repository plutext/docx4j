package org.docx4j.model.fields;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.Text;

/**
 * This class puts fields into a "canonical" representation
 * (see FieldRef for description).
 * 
 * It does this in 2 steps:
 * - step 1: use XSLT to convert simple fields into complex ones
 * - step 2: put all the instructions into a single run
 * 
 * @author jharrop
 *
 */
public class FieldsPreprocessor {
	
	private static Logger log = Logger.getLogger(FieldsPreprocessor.class);		

    private final static QName _RInstrText_QNAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", 
    		"instrText");
	
	static Templates xslt;			
	private static XPathFactory xPathFactory;
	private static XPath xPath;
	static {
		try {
			Source xsltSource = new StreamSource(
						org.docx4j.utils.ResourceUtils.getResource(
								"org/docx4j/model/fields/FieldsSimpleToComplex.xslt"));
			xslt = XmlUtils.getTransformerTemplate(xsltSource);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
		
		xPathFactory = XPathFactory.newInstance();
		xPath = xPathFactory.newXPath();		
	}

	/**
	 * Convert any w:fldSimple in this part to complex field. 
	 * @param part
	 * @throws Docx4JException
	 */
	public static void complexifyFields(JaxbXmlPart part) throws Docx4JException {
		
		org.w3c.dom.Document doc = XmlUtils.marshaltoW3CDomDocument(
				part.getJaxbElement() ); 	
		
		XPathsPart xPathsPart = null;
				
		JAXBContext jc = Context.jc;
		try {
			// Use constructor which takes Unmarshaller, rather than JAXBContext,
			// so we can set JaxbValidationEventHandler
			Unmarshaller u = jc.createUnmarshaller();
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
			javax.xml.bind.util.JAXBResult result = new javax.xml.bind.util.JAXBResult(u );
								
			org.docx4j.XmlUtils.transform(doc, xslt, null, result);
			
			part.setJaxbElement(result);
		} catch (Exception e) {
			throw new Docx4JException("Problems transforming fields", e);			
		}
				
	}
	
	
	public static P canonicalise(P p, List<FieldRef> fieldRefs) {
		/*
		 * Result is something like:
		 * 
		        <w:p>
		            <w:r>
		                <w:fldChar w:fldCharType="begin"/>
		                <w:instrText xml:space="preserve"> DATE  </w:instrText>
		                <w:fldChar w:fldCharType="separate"/>
		            </w:r>
		            <w:r>
		                <w:t>4/12/2011</w:t>
		            </w:r>
		            <w:r>
		                <w:fldChar w:fldCharType="end"/>
		            </w:r>
		        </w:p>		  
		 */
		
		// TODO merge adjacent instrText elements

		P newP = Context.getWmlObjectFactory().createP();
		newP.setPPr(p.getPPr());
		
		int depth = 0;
		R newR = Context.getWmlObjectFactory().createR();
		
		RPr fieldRPr = null;
		
		FieldRef currentField = null;
		
		boolean seenSeparate=false;
		
		for (Object o : p.getContent() ) {
			
			if ( o instanceof R ) {
				
				R existingRun = (R)o;
				for (Object o2 : existingRun.getContent() ) {

					if (isCharType(o2, STFldCharType.BEGIN)) {
						
//						log.debug("begin.. ");
						seenSeparate = false;
						
						depth++;
						if (depth==1 ) { 
						
// CONTRIB https://github.com/meletis/docx4j/commit/85455e6815b7b8eb73142a1821add3a39087c70e
// comments out:							
							
							// Add any content the run contains before the BEGIN
//							if (newR.getContent().size()>0) {
//								newP.getContent().add(newR);
//
//								newR.setRPr(existingRun.getRPr() ); // if any
//							}

							newR = Context.getWmlObjectFactory().createR();
							newR.getContent().add(o2);
							
							// Setup our FieldRef object - only top level fields for now
							currentField = new FieldRef();							
							fieldRefs.add(currentField);
							currentField.setParent(newP);							
							currentField.setBeginRun(newR);

						}
					}
					else if (isCharType(o2, STFldCharType.END)) {
						
//						log.debug(".. end ");
						
						if (!seenSeparate) {
//							log.debug(".. ADDING SEP ..  ");
							// Word 2010 can produce a docx where:
							//  <w:r>
							//    <w:fldChar w:fldCharType="separate"/>
							//  </w:r>
							// is missing, so add it
							R separateR = Context.getWmlObjectFactory().createR();							
							FldChar fldChar = Context.getWmlObjectFactory().createFldChar();
							fldChar.setFldCharType(STFldCharType.SEPARATE);
							newR.getContent().add(fldChar);
							newP.getContent().add(separateR);
							
							newR = Context.getWmlObjectFactory().createR();
							currentField.setResultsSlot(newR); 
						}
						
						depth--;
						if (depth==0 ) {
							// Top level field end - gets its own w:r
							newP.getContent().add(newR);
							
							newR = Context.getWmlObjectFactory().createR();
							newR.getContent().add(o2);
							newP.getContent().add(newR);
							
							currentField.setEndRun(newR);
							
							newR = Context.getWmlObjectFactory().createR();
						} else {
							newR.getContent().add(o2);							
						}
						
					} else if (isCharType(o2, STFldCharType.SEPARATE)) {
						
//						log.debug(".. sep ..  ");
						seenSeparate = true;
						
						newR.getContent().add(o2);
						if (depth==1 ) {
							// Top level field separator
							newP.getContent().add(newR);
							newR = Context.getWmlObjectFactory().createR();
							
							// May as well set this; we'll insert our result into
							// this (or recreate it).
							newR.setRPr(fieldRPr ); 
							
							currentField.setResultsSlot(newR); // FIXME: ensure newR is actually added!
							
						}
					} else if (o2 instanceof JAXBElement
							&& ((JAXBElement)o2).getName().equals(_RInstrText_QNAME)) {
						
//						log.debug("Processing " +((JAXBElement<Text>)o2).getValue().getValue() );
						
						currentField.setInstrText( (JAXBElement<Text>)o2);

						newR.getContent().add(o2);	
						
						fieldRPr = existingRun.getRPr();
						newR.setRPr(fieldRPr);
						
					} else {
						newR.getContent().add(o2);

// CONTRIB https://github.com/meletis/docx4j/commit/85455e6815b7b8eb73142a1821add3a39087c70e
// adds:							
						newR.setRPr(existingRun.getRPr());
			            newP.getContent().add(newR);
			            newR = Context.getWmlObjectFactory().createR();
          }
				}
				
			} else {
				// its not an R, 

				// Add the previous run, if necessary
				if (newR.getContent().size()>0) {
					newP.getContent().add(newR);
					newR = Context.getWmlObjectFactory().createR();
				}
				
				newP.getContent().add(o);
				
				// TODO .. detect separator, and remove stuff?
				// This model can't really do that right now, since it
				// works only within the paragraph.
			}
			
		}
		if (newR.getContent().size()>0
				&& !newP.getContent().contains(newR) ) {
			newP.getContent().add(newR);
		}
		
//		log.debug(XmlUtils.marshaltoString(newP, true));
		
		return newP;
	}
	
//	public static boolean containsCharType(Object o, STFldCharType charType) {
//		
//		if (o instanceof R) {
//			for (Object o2 : ((R)o).getContent() ) {
//				
//				if (isCharType(o2, charType)) {
//						return true;
//				}				
//			}
//		} 
//		return false;
//	}	

	public static boolean isCharType(Object o2, STFldCharType charType) {
		
		o2 = XmlUtils.unwrap(o2);
		
		if (o2 instanceof org.docx4j.wml.FldChar) {
			FldChar fldChar = (FldChar)o2;
			if (fldChar.getFldCharType().equals(charType) ) {
				return true;
			}
		}
		return false;
	}	
	
}
