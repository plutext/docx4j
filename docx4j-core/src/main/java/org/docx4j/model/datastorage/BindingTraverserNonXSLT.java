/**
 *  Copyright 2010-2013, Plutext Pty Ltd.
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

import org.apache.commons.codec.binary.Base64;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.docx4j.TraversalUtil;
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
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTDataBinding;
import org.docx4j.wml.CTSdtText;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.P;
import org.docx4j.wml.P.Hyperlink;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.SdtContentBlock;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.SdtRun;
import org.docx4j.wml.Tag;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * TODO add HTML import, FlatOPC support
 * @author jharrop
 */
public class BindingTraverserNonXSLT extends BindingTraverserCommonImpl {
	
	private static Logger log = LoggerFactory.getLogger(BindingTraverserNonXSLT.class);		
	
	JaxbXmlPart part;
	org.docx4j.openpackaging.packages.OpcPackage pkg;
	//XPathsPart xPathsPart;
	Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap; // not currently used; will be when this is fixed to handle XHTML, images, RepeatPositionCondition, FlatOPC
	
	public Object traverseToBind(JaxbXmlPart part,
			org.docx4j.openpackaging.packages.OpcPackage pkg,
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap)
			throws Docx4JException {
		
		this.part = part;
		this.pkg = pkg;
		this.xpathsMap = xpathsMap;
		
		Object clone = XmlUtils.deepCopy(part.getJaxbElement());
		
		BindingTraversor bt = new BindingTraversor();
		new TraversalUtil(clone, bt);
		
		return clone;
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
	
	
	class BindingTraversor extends CallbackImpl {

		@Override
		public List<Object> apply(Object o) {
			
			//System.out.println("traversing " + o.getClass().getName() ) ;

			if (o instanceof org.docx4j.wml.SdtBlock
					|| o instanceof org.docx4j.wml.SdtRun
					|| o instanceof org.docx4j.wml.CTSdtRow
					|| o instanceof org.docx4j.wml.CTSdtCell) { // SdtCell as
																// well, here
				handleSdt(o);

			} 
			return null;
		}
		
		private void handleSdt(Object o) {
			
			SdtElement sdt = (SdtElement)o;
			SdtPr sdtPr = sdt.getSdtPr();
			
			Tag tag = sdtPr.getTag();			
			HashMap<String, String> map = null;
			if (tag!=null) {
				map = QueryString.parseQueryString(
					tag.getVal(), true);
			}			
			
			SdtPr.Picture pic = getPicture(sdtPr);
			if (sdtPr.getDataBinding()!=null && pic!=null) {
				
				Object sdtParent = ((Child)o).getParent();
								
				sdt.getSdtContent().getContent().clear();				
				sdt.getSdtContent().getContent().add(
						this.xpathInjectImage(
								(WordprocessingMLPackage)pkg, part, 
								sdtPr.getDataBinding(), sdt, 
								sdtParent));
				
				// TODO v3 XSLT approach
				// .. if the sdt contains a template picture,
				// find its a:blip, and just replace that.
				
			} else if (map!=null && map.containsKey(OpenDoPEHandler.BINDING_CONTENTTYPE)
						&& map.get(OpenDoPEHandler.BINDING_CONTENTTYPE).equals("application/xhtml+xml")) {
					// Convert XHTML.
				log.error("TODO: add HTML import support");
				
			} else if (map!=null && map.containsKey(OpenDoPEHandler.BINDING_ROLE_CONDITIONAL) ) {
				// Do nothing
			} else if (map!=null && map.containsKey(OpenDoPEHandler.BINDING_RESULT_RPTD) ) {
				// Do nothing
				
			} else if (map!=null && map.containsKey(OpenDoPEHandler.BINDING_ROLE_RPT_POS_CON) ) {
				// This may be tricky to do here .. 
				
			} else if (map!=null && map.containsKey(OpenDoPEHandler.BINDING_ROLE_XPATH) ) {
				
				boolean isMultiline = isMultiline(sdtPr);
				
				sdt.getSdtContent().getContent().clear();
				
				sdt.getSdtContent().getContent().addAll(
						this.xpathGenerateRuns(
							(WordprocessingMLPackage)pkg, part, 
							sdtPr,
							sdtPr.getDataBinding(), 
							//sdtParent, contentChild, 
							null, isMultiline));
				
			} else if (sdtPr.getDataBinding()!=null && !isRichText(sdtPr) ) {
				// TODO and not(w:sdtPr/w:docPartGallery)
				// .. but which is that?
		        //@XmlElementRef(name = "docPartList", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
		        //@XmlElementRef(name = "docPartObj", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
				
				sdt.getSdtContent().getContent().clear();
				
				sdt.getSdtContent().getContent().addAll(
						this.xpathGenerateRuns(
							(WordprocessingMLPackage)pkg, part, 
							sdtPr,
							sdtPr.getDataBinding(), 
							//sdtParent, contentChild, 
							null, false));
				
			} else {
				if(log.isDebugEnabled()) {
                    log.debug("Not processing " + XmlUtils.marshaltoString(sdtPr, true));
                }
				 
			}
			
		}
		
		private Object  xpathInjectImage(WordprocessingMLPackage wmlPackage,
				JaxbXmlPart sourcePart,
				CTDataBinding dataBinding, 
				SdtElement sdt,
				Object sdtParent
				) {

			
//			<w:drawing>
//			<wp:inline distT="0" distB="0" distL="0" distR="0">
//				<wp:extent cx="3238500" cy="2362200" />		
			ExtentFinder ef = new ExtentFinder();
			new TraversalUtil(sdt.getSdtContent().getContent(), ef);
			
			sdtParent = XmlUtils.unwrap(sdtParent);
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
				
				// Base64 decode it
				byte[] bytes = Base64.decodeBase64( r.getBytes("UTF8") );
				
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
				if (sdtParent instanceof Tr) {
					tc.getContent().add(p);
				}
				org.docx4j.wml.R  run = factory.createR();		
				if (sdtParent instanceof Body
						|| sdtParent instanceof Tr 
						|| sdtParent instanceof Tc ) {
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
				
				
				if (sdtParent instanceof Body
						|| sdtParent instanceof Tc
						|| sdtParent instanceof SdtContentBlock) {
					return p;
				} else if ( sdtParent instanceof Tr  ) {
					return tc;
				} else if ( sdtParent instanceof P ) {
					return run;
				} else if ( sdtParent instanceof SdtElement) {		
					List<Object> sdtContent = sdt.getSdtContent().getContent();
					if (sdtContent.size()==0) {
//						if (sdtParent instanceof SdtRun) {
//							return run;							
//						} else 
						if (sdtParent instanceof SdtRun) {
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

		private  boolean isRichText(SdtPr sdtPr) {
			
			for (Object o : sdtPr.getRPrOrAliasOrLock() ) {
				o = XmlUtils.unwrap(o);
				if (o instanceof SdtPr.RichText) return true;
			}
			return false;
		}
		
		private boolean isMultiline(SdtPr sdtPr) {

			for (Object o : sdtPr.getRPrOrAliasOrLock() ) {
				
				o = XmlUtils.unwrap(o);
				if (o instanceof CTSdtText) {
					return ((CTSdtText)o).isMultiLine();
				}
			}
			return false;
		}

		
		public List<Object> xpathGenerateRuns(
				WordprocessingMLPackage pkg, 
				JaxbXmlPart sourcePart,
				SdtPr sdtPr,
				CTDataBinding dataBinding,
//				String sdtParent,
//				String contentChild,				
				RPr rPr, boolean multiLine
				//String tag
				) {
			
			/**
			 * TODO test cases:
			 * 
			 * - multiline data, including cases which start/end with empty token
			 * - multiline data with w:multiLine absent or set to 0 ie false
			 * - cases with and without rPr
			 * - inline and block level sdt
			 */
			
			Map<String, CustomXmlPart> customXmlDataStorageParts = pkg.getCustomXmlDataStorageParts();

			String r = BindingHandler.xpathGetString(pkg, customXmlDataStorageParts, dataBinding);
			if (r==null) return null;
			
			List<Object> contents = new ArrayList<Object>();
			
			try {
				log.info(dataBinding.getXpath() + " yielded result " + r);
				
				org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
				
				StringTokenizer st = new StringTokenizer(r, "\n\r\f"); // tokenize on the newline character, the carriage-return character, and the form-feed character
				
				if (multiLine) {
					// our docfrag may contain several runs
					boolean firsttoken = true;
					while (st.hasMoreTokens()) {						
						String line = (String) st.nextToken();
						
						if (firsttoken) {
							firsttoken = false;
						} else {
							addBrRunToDocFrag(contents, rPr);
						}
						
						processString(sourcePart, contents, line, sdtPr, rPr);						
					}
					
				} else {
					// not multiline, so remove any CRLF in data;
					// our docfrag wil contain a single run
					StringBuilder sb = new StringBuilder();
					while (st.hasMoreTokens()) {						
						sb.append( st.nextToken() );
					}
					
					processString(sourcePart, contents, sb.toString(), sdtPr, rPr);
				}				
				
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return null;
			}
			
			return contents;			
		}

		private void addBrRunToDocFrag(List<Object> contents, RPr rPr) throws JAXBException {
			
			// Not sure whether there is ever anything of interest in the rPr, 
			// but add it anyway
			org.docx4j.wml.R  run = Context.getWmlObjectFactory().createR();		
			if (rPr!=null) {
				run.setRPr(rPr);
			}
			run.getRunContent().add(Context.getWmlObjectFactory().createBr());
			
			contents.add(run);
		}
		
		private void processString(JaxbXmlPart sourcePart, List<Object> contents, String text, SdtPr sdtPr, RPr rPr) throws JAXBException {
			
			int pos = BindingHandler.getHyperlinkResolver().getIndexOfURL(text);
			if (pos==-1 || BindingHandler.getHyperlinkStyleId() == null) {				
				addRunToDocFrag(sourcePart, contents,  text,  rPr);
				return;
			} 
			
			// There is a hyperlink to deal with
			
			// We'll need to remove:
			//   <w:dataBinding w:storeItemID="{5448916C-134B-45E6-B8FE-88CC1FFC17C3}" w:xpath="/myxml[1]/element2[1]" w:prefixMappings=""/>
			//   <w:text w:multiLine="true"/>
			// or Word can't open the resulting docx, but we can't do it here,
			sdtPr.setDataBinding(null);
			
			Object sdtPrText = null;
			for (Object o : sdtPr.getRPrOrAliasOrLock() ) {
				Object unwrapped = XmlUtils.unwrap(o);
				if (unwrapped instanceof CTSdtText) {
					sdtPrText = o;
					break;
				}
			}
			if (sdtPrText!=null) {
				sdtPr.getRPrOrAliasOrLock().remove(sdtPrText);
			}
			
			if (pos==0) {
				int spacePos = text.indexOf(" ");
				if (spacePos==-1) {
					addHyperlinkToDocFrag(sourcePart, contents,  text);
					return;					
				}
				
				// Could contain more than one hyperlink, so process recursively					
				String first = text.substring(0, spacePos);
				String rest = text.substring(spacePos);
				
				addHyperlinkToDocFrag( sourcePart,  contents,  first);
				// .. now the recursive bit ..
				processString(sourcePart,  contents,  rest, sdtPr, rPr);	
				return;
			}
			
			String first = text.substring(0, pos);
			String rest = text.substring(pos);
			
			addRunToDocFrag( sourcePart,  contents,  first, rPr);
			// .. now the recursive bit ..
			processString(sourcePart,  contents,  rest, sdtPr, rPr);				
		}
		
		private void addRunToDocFrag(JaxbXmlPart sourcePart, List<Object> contents, String string, RPr rPr) {
			
			org.docx4j.wml.R  run = Context.getWmlObjectFactory().createR();		
			if (rPr!=null) {
				run.setRPr(rPr);
			}
			org.docx4j.wml.Text text = Context.getWmlObjectFactory().createText();
			run.getRunContent().add(text);
			if (string.startsWith(" ") || string.endsWith(" ") ) {
				// TODO: tab character?
				text.setSpace("preserve");
			}
			text.setValue(string);
						
			contents.add(run);
		}
		
		private void addHyperlinkToDocFrag(JaxbXmlPart sourcePart, List<Object> contents, String url) throws JAXBException {
			
			// We need to add a relationship to word/_rels/document.xml.rels
			// but since its external, we don't use the 
			// usual wordMLPackage.getMainDocumentPart().addTargetPart
			// mechanism
			org.docx4j.relationships.ObjectFactory factory =
				new org.docx4j.relationships.ObjectFactory();
			
			org.docx4j.relationships.Relationship rel = factory.createRelationship();
			rel.setType( Namespaces.HYPERLINK  );
			rel.setTarget(url);
			rel.setTargetMode("External");  
									
			sourcePart.getRelationshipsPart().addRelationship(rel);
			
			// addRelationship sets the rel's @Id
			
			String hpl = "<w:hyperlink r:id=\"" + rel.getId() + "\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" " +
	        "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" >" +
	        "<w:r>" +
	        "<w:rPr>" +
	        "<w:rStyle w:val=\"" + BindingHandler.getHyperlinkStyleId() + "\" />" +  // TODO: enable this style in the document!
	        "</w:rPr>" +
	        "<w:t>" + url + "</w:t>" +
	        "</w:r>" +
	        "</w:hyperlink>";
					
			contents.add((Hyperlink)XmlUtils.unmarshalString(hpl));
		}
		
		
	}
	
	

}
