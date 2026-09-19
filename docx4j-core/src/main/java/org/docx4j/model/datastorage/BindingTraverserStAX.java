/**
 *  Copyright 2025, Plutext Pty Ltd.
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

 **/
package org.docx4j.model.datastorage;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.xmlgraphics.image.loader.ImageSize;
import org.docx4j.TraversalUtil;
import org.docx4j.jaxb.McMode;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.model.sdt.QueryString;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.SdtStAXHandler;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.CTDataBinding;
import org.docx4j.wml.CTSdtText;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.P;
import org.docx4j.wml.P.Hyperlink;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Tag;
import org.docx4j.wml.Tc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

/**
 * Use StAX to bind content controls, bypassing the need
 * to unmarshal the entire MDP. 
 * 
 * Feature parity with the XSLT pathway per
 * docs/developer/change-requests/CR-001-binding-traverser-parity.md
 * @author jharrop
 */
public class BindingTraverserStAX extends BindingTraverserCommonImpl {
	
	private static Logger log = LoggerFactory.getLogger(BindingTraverserStAX.class);		
	
	JaxbXmlPart part;
	org.docx4j.openpackaging.packages.OpcPackage pkg;
	//XPathsPart xPathsPart;
	Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap;
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc;
		
	public Object traverseToBind(JaxbXmlPart part,
			org.docx4j.openpackaging.packages.OpcPackage pkg,
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap)
			throws Docx4JException {

		throw new Docx4JException("Not implemented; use streamToBind instead.");
	}
	
	/**
	 * Bind the content controls; avoid unmarshalling the entire part.
	 * 
	 * @param part
	 * @param pkg
	 * @param xpathsMap
	 * @throws Docx4JException
	 */
	public void streamToBind(JaxbXmlPart part,
			org.docx4j.openpackaging.packages.OpcPackage pkg,
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap)
			throws Docx4JException {
	
		log.info("Using BindingTraverserStAX");

		this.part = part;
		this.pkg = pkg;
		this.xpathsMap = xpathsMap;

		// Don't clone, since this unmarshals! TODO clone the byte array??
		//Object clone = XmlUtils.deepCopy(part.getJaxbElement());

		// od:RptPosCon needs to know a repeat instance's position among its
		// identically-tagged siblings, but by the time StaXBindingHandler hands a
		// repeat instance to BindingTraverserNonXSLT, its siblings aren't in reach.
		// So pre-scan the part (cheaply, without JAXB) collecting each instance's
		// position, keyed by sdt id.
		try {
			XMLStreamReader xmlr = part.getXMLStreamReader(null);
			repeatPositions = scanRepeatPositions(xmlr);
			xmlr.close();
		} catch (Exception e) {
			throw new Docx4JException(e.getMessage(), e);
		}

		// Use StAX; be sure you are using https://github.com/FasterXML/woodstox
		try {
			part.pipe(new StaXBindingHandler(), null);  // no real need for a filter here, see https://docs.oracle.com/cd/E19575-01/819-3669/bnbgh/index.html
		} catch (Exception e) {
			throw new Docx4JException(e.getMessage(), e);
		}
	}

	/**
	 * For each outermost od:rptd sdt, its 1-based position among the identically-tagged
	 * sdt children of its parent element, and the count of those (per-occurrence, since
	 * the tag carries od:RptOcc), keyed by the sdt's w:id.
	 */
	private Map<BigInteger, int[]> repeatPositions = null;

	private static Map<BigInteger, int[]> scanRepeatPositions(XMLStreamReader xmlr)
			throws javax.xml.stream.XMLStreamException {

		final String W = Namespaces.NS_WORD12;

		// group key: parent element instance number + tag value
		Map<String, List<BigInteger>> groups = new LinkedHashMap<String, List<BigInteger>>();

		java.util.Deque<Integer> openElements = new java.util.ArrayDeque<Integer>();
		int nextElementNumber = 0;

		int sdtDepth = 0;
		boolean inOuterSdtPr = false;
		int currentParent = -1;
		String currentTag = null;
		String currentId = null;

		while (xmlr.hasNext()) {
			int eventType = xmlr.next();
			if (eventType == XMLStreamConstants.START_ELEMENT) {
				String localName = xmlr.getLocalName();
				boolean isW = W.equals(xmlr.getNamespaceURI());
				if (isW && localName.equals("sdt")) {
					sdtDepth++;
					if (sdtDepth==1) {
						currentParent = (openElements.peek()==null ? -1 : openElements.peek());
						currentTag = null;
						currentId = null;
						inOuterSdtPr = false;
					}
				} else if (isW && sdtDepth==1 && localName.equals("sdtPr")) {
					inOuterSdtPr = true;
				} else if (isW && sdtDepth==1 && inOuterSdtPr
						&& localName.equals("tag") && currentTag==null) {
					currentTag = xmlr.getAttributeValue(W, "val");
				} else if (isW && sdtDepth==1 && inOuterSdtPr
						&& localName.equals("id") && currentId==null) {
					currentId = xmlr.getAttributeValue(W, "val");
				}
				openElements.push(nextElementNumber++);
			} else if (eventType == XMLStreamConstants.END_ELEMENT) {
				openElements.pop();
				String localName = xmlr.getLocalName();
				boolean isW = W.equals(xmlr.getNamespaceURI());
				if (isW && localName.equals("sdtPr") && sdtDepth==1) {
					inOuterSdtPr = false;
				} else if (isW && localName.equals("sdt")) {
					if (sdtDepth==1 && currentTag!=null && currentId!=null
							&& QueryString.parseQueryString(currentTag, true)
									.containsKey(OpenDoPEHandler.BINDING_RESULT_RPTD)) {
						String key = currentParent + "\u0000" + currentTag;  // NUL separator (an escape, not a raw byte: keeps the file text)
						List<BigInteger> group = groups.get(key);
						if (group==null) {
							group = new ArrayList<BigInteger>();
							groups.put(key, group);
						}
						try {
							group.add(new BigInteger(currentId));
						} catch (NumberFormatException nfe) {
							log.warn("Malformed sdt id " + currentId);
						}
					}
					sdtDepth--;
				}
			}
		}

		Map<BigInteger, int[]> result = new HashMap<BigInteger, int[]>();
		for (List<BigInteger> group : groups.values()) {
			for (int i=0; i<group.size(); i++) {
				result.put(group.get(i), new int[]{i+1, group.size()});
			}
		}
		return result;
	}
	
	
	private class StaXBindingHandler extends SdtStAXHandler  {
			
		protected List<Object> handleSdt(SdtElement sdt) throws Docx4JException {
			
			// Here the result is just the input manipulated
			List<Object> results = new ArrayList<Object>();
			results.add(sdt);
			
			SdtPr sdtPr = sdt.getSdtPr();
			
			log.debug(XmlUtils.marshaltoString(sdtPr));
			
			Tag tag = sdtPr.getTag();			
			HashMap<String, String> map = null;
			if (tag!=null) {
				map = QueryString.parseQueryString(
					tag.getVal(), true);
			}			
			
			SdtPr.Picture pic = getPicture(sdtPr);
			if (sdtPr.getDataBinding()!=null && pic!=null
					&& replaceBlipEmbed(sdt, pkg, part, xpathsMap)) {
				// authored drawing preserved; just its image rel was replaced
				// (as bind.xslt's picture3 mode does)

			} else if (sdtPr.getDataBinding()!=null && pic!=null) {

				log.debug("pic handling");

				sdt.getSdtContent().getContent().clear();
				sdt.getSdtContent().getContent().add(
						this.xpathInjectImage(
								(WordprocessingMLPackage)pkg, part,
								sdtPr.getDataBinding(), sdt,
								stack.peek()));

			} else if (sdtPr.getDataBinding()!=null
					&& sdtPr.getByClass(org.docx4j.w14.CTSdtCheckbox.class)!=null) {
				// w14:checkbox cc; see CR-001-binding-traverser-parity phase 3
				applyCheckboxBinding(sdt, pkg);

			} else if (sdtPr.getDataBinding()!=null
					&& sdtPr.getByClass(org.docx4j.wml.CTSdtDate.class)!=null) {
				// w:date cc
				applyDateBinding(sdt, pkg);

			} else if (map!=null && "picture".equals(map.get(OpenDoPEHandler.BINDING_HANDLER))) {
				// od:Handler=picture rich text cc; see CR-001-binding-traverser-parity phase 4
				applyHandlerPicture(sdt, pkg, part, xpathsMap, map);

			} else if (map!=null && map.containsKey(OpenDoPEHandler.BINDING_CONTENTTYPE)
						&& map.get(OpenDoPEHandler.BINDING_CONTENTTYPE).equals("application/xhtml+xml")) {
				// Convert XHTML (via ImportXHTML if available, else altChunk);
				// see CR-001-binding-traverser-parity phase 5
				applyXHTMLBinding(sdt, pkg, part, xpathsMap);

			} else if (map!=null && "Word.Document".equals(map.get(OpenDoPEHandler.BINDING_PROGID))) {
				// Flat OPC injection as altChunk
				applyFlatOPCBinding(sdt, pkg, part, xpathsMap);
				
			} else if (map!=null && map.containsKey(OpenDoPEHandler.BINDING_ROLE_CONDITIONAL) ) {
				
				// Handle nested
				BindingTraverserNonXSLT traverser = new BindingTraverserNonXSLT();
				traverser.setDomToXPathMap(domToXPathMap);
				traverser.traverseToBind(part, sdt, xpathsMap);
				
			} else if (map!=null && map.containsKey(OpenDoPEHandler.BINDING_RESULT_RPTD) ) {

				// Handle nested
				BindingTraverserNonXSLT traverser = new BindingTraverserNonXSLT();
				traverser.setDomToXPathMap(domToXPathMap);
				// this repeat instance's position (from the pre-scan), so any od:RptPosCon
				// inside it can be evaluated
				int[] posSize = null;
				if (repeatPositions!=null && sdtPr.getId()!=null) {
					posSize = repeatPositions.get(sdtPr.getId().getVal());
				}
				if (posSize!=null) {
					traverser.setFragmentRootRepeatPosition(posSize[0], posSize[1]);
				}
				traverser.traverseToBind(part, sdt, xpathsMap);

			} else if (map!=null && map.containsKey(OpenDoPEHandler.BINDING_ROLE_RPT_POS_CON) ) {

				// An od:RptPosCon at this level is outside any repeat instance
				// (nested ones were handled via the od:rptd branch above), so its
				// position condition can't be satisfied; bind.xslt omits it
				log.warn("od:RptPosCon outside any repeat instance: omitting. " + tag.getVal());
				results.clear();

			} else if (map!=null && map.containsKey(OpenDoPEHandler.BINDING_ROLE_XPATH) ) {

				if (log.isDebugEnabled()) {
					log.debug(XmlUtils.marshaltoString(sdt));
				}
				// via BindingTraverserCommonImpl / ValueInserterPlainText, for parity
				// with the XSLT pathway (rPr applied, placeholder restored on empty
				// result, custom inserter honoured); see CR-001-binding-traverser-parity
				List<Object> boundContent = generateBoundContent(pkg, part, sdtPr,
						xpathsMap, isMultiline(sdtPr));
				if (boundContent!=null) {
					applyBoundContent(sdt, boundContent);
				}

			} else if (sdtPr.getDataBinding()!=null && !isRichText(sdtPr) ) {
				// TODO and not(w:sdtPr/w:docPartGallery)
				// .. but which is that?
		        //@XmlElementRef(name = "docPartList", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
		        //@XmlElementRef(name = "docPartObj", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
				
				log.debug("not rich text, " + sdtPr.getDataBinding() );
				// covers w15:dataBinding too (getDataBinding returns either)
				List<Object> boundContent = generateBoundContent(pkg, part, sdtPr,
						xpathsMap, isMultiline(sdtPr));
				if (boundContent!=null) {
					applyBoundContent(sdt, boundContent);
				}

			} else {
				
				// the sdt content might contain an SDT we have to process!
				log.info("Found an SDT without binding information; traversing for nested...");
				if (log.isDebugEnabled() ) {
					log.debug(XmlUtils.marshaltoString(sdtPr));
				}

				BindingTraverserNonXSLT traverser = new BindingTraverserNonXSLT();
				traverser.setDomToXPathMap(domToXPathMap);
				traverser.traverseToBind(part, sdt, xpathsMap);
				 
			}
			return results;
		}
		
		private Object  xpathInjectImage(WordprocessingMLPackage wmlPackage,
				JaxbXmlPart sourcePart,
				CTDataBinding dataBinding, 
				SdtElement sdt,
				String sdtParent
				) {

			
//			<w:drawing>
//			<wp:inline distT="0" distB="0" distL="0" distR="0">
//				<wp:extent cx="3238500" cy="2362200" />		
			ExtentFinder ef = new ExtentFinder();
			// CR-021: ALL - the control's extent spans every branch
			new TraversalUtil(sdt.getSdtContent().getContent(), ef, McMode.ALL);
			
			//System.out.println("sdt's parent: " + sdtParent.getClass().getName() );
			
			// TODO: remove any images in package which are no longer used.
			// Needs to be done once after BindingHandler has been done
			// for all parts for which it is to be called (eg mdp, header parts etc).
			
			Map<String, CustomXmlPart> customXmlDataStorageParts = pkg.getCustomXmlDataStorageParts();
			CustomXmlPart part = customXmlDataStorageParts.get(dataBinding.getStoreItemID().toLowerCase());
			if (part==null) {
				log.error("Couldn't locate part by storeItemId " + dataBinding.getStoreItemID());
				return null;
			}
			try {
				String r = part.xpathGetString(dataBinding.getXpath(), dataBinding.getPrefixMappings());
				log.debug(dataBinding.getXpath() + " yielded result " + r);
				
				// Base64 decode it (lenient: ignores whitespace/non-alphabet chars)
				byte[] bytes = java.util.Base64.getMimeDecoder().decode(r);
				
				// Create image part and add it
		        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wmlPackage, sourcePart, bytes);
//				BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createPNGPart(wmlPackage, sourcePart, bytes);
				
		        String filenameHint = null;
		        String altText = null;
		        int id1 = 0;
		        int id2 = 1;		        		
		        Inline inline = null;
		        long cxl = 0;
		        long cyl = 0;
		        try {
		        	cxl = ef.getExtent().getCx();
		        	cyl = ef.getExtent().getCy();
		        } catch (Exception e) {}
		        if (cxl==0 || cyl==0) {
		        	// Let BPAI work out size
		        	log.debug("image size - from image");
			        inline = imagePart.createImageInline( filenameHint, altText, 
			    			id1, id2, false);
//			        inline = imagePart.createImageInline( filenameHint, altText, 
//			    			id1, id2, 100000, 100000, false);		        	
		        } else {
		        	// Use existing size
		        	log.debug("image size - from content control size");
	                // Respect aspect ratio of injected image
	                ImageSize size = imagePart.getImageInfo().getSize();
	                double ratio = (double) size.getHeightPx() / (double) size.getWidthPx();
	                log.debug("fit ratio: " + ratio);
	                if (ratio > 1) {
	                    cxl =  (long)((double) cyl / ratio);
	                } else {
	                    cyl =  (long)((double) cxl * ratio);
	                }
			        inline = imagePart.createImageInline( filenameHint, altText, 
			    			id1, id2, cxl, cyl, false);		        	
		        }
		        
		        // Now add the inline in w:p/w:r/w:drawing
				org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
				org.docx4j.wml.Tc tc  = factory.createTc();
				org.docx4j.wml.P  p   = factory.createP();
				if (sdtParent.equals("tr") ) {
					tc.getContent().add(p);
				}
				org.docx4j.wml.R  run = factory.createR();		
				if (sdtParent.equals("body")
						|| sdtParent.equals("tr") 
						|| sdtParent.equals("tc") ) {
					p.getContent().add(run);
				}
				org.docx4j.wml.Drawing drawing = factory.createDrawing();		
				run.getContent().add(drawing);		
				drawing.getAnchorOrInline().add(inline);
				
				
				/* return following node
				 * 
				 *     <w:p>
				          <w:r>
				            <w:drawing>
				              <wp:inline distT="0" distB="0" distL="0" distR="0">
				              	etc
					 */
				
				//System.out.println(XmlUtils.marshaltoString(run, false));
				
				if (sdtParent.equals("body")
						|| sdtParent.equals("tc") 
						|| sdtParent.equals("SdtContentBlock" /* FIXME */) ) {
					return p;
				} else if ( sdtParent.equals("tr")  ) {
					return tc;
				} else if ( sdtParent.equals("p") ) {
					return run;
				} else if ( sdtParent.equals("SdtElement") /* FIXME */) {		
					List<Object> sdtContent = sdt.getSdtContent().getContent();
					if (sdtContent.size()==0) {
//						if (sdtParent instanceof SdtRun) {
//							return run;							
//						} else 
						if (sdtParent.equals("SdtRun") /* FIXME */) {
							return run;							
						} else {
                            if(log.isErrorEnabled()) {
                                log.error("empty image template in sdt: " + XmlUtils.marshaltoString(sdt.getSdtPr(), true)
                                        + sdtParent.getClass().getName());
                            }
						}
					} else {
						Object contentChild = sdtContent.get(0);
						
						log.info("contentChild: " + contentChild.getClass().getName());
						if (contentChild instanceof P) {
							p.getContent().add(run);
							return p;						
						} else if (contentChild instanceof R) {
							return r;					
						} else {
							log.error("how to inject image for unexpected sdt's content: " + contentChild.getClass().getName());					
						}
					}
				} else {
					log.error("how to inject image for unexpected sdt's parent: " + sdtParent.getClass().getName());
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			} 
			return null;
		}
		
		
		private SdtPr.Picture getPicture(SdtPr sdtPr) {
			
			for (Object o : sdtPr.getRPrOrAliasOrLock() ) {
				o = XmlUtils.unwrap(o);
				if (o instanceof SdtPr.Picture) return (SdtPr.Picture)o;
			}
			return null;
		}

		

		
	}
		
    static class ExtentFinder extends CallbackImpl {
		
    	private CTPositiveSize2D extent;
		public CTPositiveSize2D getExtent() {
			return extent;
		}

		@Override
		public List<Object> apply(Object o) {
			
			if (o instanceof Drawing) {
				Object o2 = ((Drawing)o).getAnchorOrInline().get(0);
				if (o2 instanceof Anchor) {
					extent = ((Anchor)o2).getExtent();
					return null;
				}
				if (o2 instanceof Inline) {
					extent = ((Inline)o2).getExtent();
					return null;
				}
			}
			return null;
		}
    	
    	@Override
		public boolean shouldTraverse(Object o) {
    		return (extent==null);
		}
    	
	}

}
