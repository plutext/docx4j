/**
 *  Copyright 2010-2025, Plutext Pty Ltd.
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
import org.jvnet.jaxb.lang.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Feature parity with the XSLT pathway per
 * docs/developer/change-requests/CR-001-binding-traverser-parity.md
 * @author jharrop
 */
public class BindingTraverserNonXSLT extends BindingTraverserCommonImpl {
	
	private static Logger log = LoggerFactory.getLogger(BindingTraverserNonXSLT.class);		
	
	JaxbXmlPart part;
	org.docx4j.openpackaging.packages.OpcPackage pkg;
	//XPathsPart xPathsPart;
	Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap;
	
	/**
	 * Entry point.  Traverse a clone of the part.
	 */
	public Object traverseToBind(JaxbXmlPart part,
			org.docx4j.openpackaging.packages.OpcPackage pkg,
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap)
			throws Docx4JException {

		this.part = part;
		this.pkg = pkg;
		this.xpathsMap = xpathsMap;

		Object clone = XmlUtils.deepCopy(part.getJaxbElement());

		processRptPosCons(clone);

		BindingTraversor bt = new BindingTraversor();
		new TraversalUtil(clone, bt);

		return clone;
	}

	/**
	 * Traverse a sub-tree.  Typically invoked from BindingTraverserStAX
	 * when a non-bound SDT is encountered (since that might contain
	 * nested SDTs which *are* bound).
	 * 
	 * @param part
	 * @param jaxbObject
	 * @param xpathsMap
	 * @throws Docx4JException
	 */
	public void traverseToBind(JaxbXmlPart part, Object jaxbObject,
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap)
			throws Docx4JException {

		this.part = part;  // required for image rels etc
		this.pkg = part.getPackage();
		this.xpathsMap = xpathsMap;

		processRptPosCons(jaxbObject);

		BindingTraversor bt = new BindingTraversor();
		new TraversalUtil(jaxbObject, bt);

	}

	/**
	 * Sentinel: enclosing repeat instance's position is unknown, so leave
	 * any od:RptPosCon relating to it alone.
	 */
	private static final int[] UNKNOWN_POSITION = new int[0];

	private int[] fragmentRootRepeatPosition = null;

	/**
	 * Where traverseToBind(part, jaxbObject, xpathsMap) will be invoked with an
	 * od:rptd sdt (ie a single repeat instance, as BindingTraverserStAX does),
	 * supply the instance's 1-based position and the total number of instances,
	 * so od:RptPosCon descendants which relate to that instance can be evaluated.
	 * If not supplied, such descendants are left as they are.
	 *
	 * @since 17.0.4
	 */
	public void setFragmentRootRepeatPosition(int pos, int size) {
		this.fragmentRootRepeatPosition = new int[]{pos, size};
	}

	/**
	 * Process od:RptPosCon sdts: keep the sdt where its position condition is
	 * satisfied by the position of the nearest enclosing od:rptd repeat instance
	 * (among its identically-tagged siblings); otherwise remove it.  This mirrors
	 * what bind.xslt does.
	 */
	private void processRptPosCons(Object root) {

		Object unwrapped = XmlUtils.unwrap(root);

		Deque<int[]> rptdStack = new ArrayDeque<int[]>();

		String rootTag = getSdtTagVal(unwrapped);
		if (rootTag!=null
				&& QueryString.parseQueryString(rootTag, true).containsKey(OpenDoPEHandler.BINDING_RESULT_RPTD)) {
			// the root itself is a repeat instance (fragment case); its siblings aren't
			// available here, so its position must have been supplied
			rptdStack.push(fragmentRootRepeatPosition==null ? UNKNOWN_POSITION : fragmentRootRepeatPosition);
		}

		walkForRptPosCon(unwrapped, rptdStack);
	}

	private void walkForRptPosCon(Object node, Deque<int[]> rptdStack) {

		List<Object> children = TraversalUtil.getChildrenImpl(node);
		if (children==null || children.isEmpty()) return;

		List<Object> removals = null;

		for (Object child : new ArrayList<Object>(children)) {

			Object u = XmlUtils.unwrap(child);
			String tagVal = getSdtTagVal(u);

			if (tagVal==null) {
				walkForRptPosCon(u, rptdStack);
				continue;
			}

			HashMap<String, String> map = QueryString.parseQueryString(tagVal, true);

			if (map.containsKey(OpenDoPEHandler.BINDING_ROLE_RPT_POS_CON)) {

				Boolean keep = evaluateRptPosConSdt(tagVal, rptdStack.peek());
				if (keep!=null && !keep) {
					if (removals==null) removals = new ArrayList<Object>();
					removals.add(child);
				} else {
					walkForRptPosCon(u, rptdStack);
				}

			} else if (map.containsKey(OpenDoPEHandler.BINDING_RESULT_RPTD)) {

				rptdStack.push(computeRepeatPosition(u, tagVal, children));
				walkForRptPosCon(u, rptdStack);
				rptdStack.pop();

			} else {
				walkForRptPosCon(u, rptdStack);
			}
		}

		if (removals!=null) {
			for (Object r : removals) {
				if (!children.remove(r)) {
					log.error("Couldn't remove od:RptPosCon sdt from parent "
							+ node.getClass().getName());
				}
			}
		}
	}

	/**
	 * The 1-based position of this repeat instance among its identically-tagged
	 * sdt siblings, and the count of those siblings; equivalent to bind.xslt's
	 * $pos and count($vNodeSet).  Instances of one repeat occurrence share their
	 * (od:RptOcc stamped) tag, so exact tag match groups per occurrence.
	 */
	private int[] computeRepeatPosition(Object rptdSdt, String tagVal, List<Object> siblings) {

		int pos = -1;
		int size = 0;
		for (Object sibling : siblings) {
			Object u = XmlUtils.unwrap(sibling);
			if (tagVal.equals(getSdtTagVal(u))) {
				size++;
				if (u==rptdSdt) pos = size;
			}
		}
		if (pos<0) {
			log.error("Repeat instance not found among its siblings; tag " + tagVal);
			return UNKNOWN_POSITION;
		}
		return new int[]{pos, size};
	}

	/**
	 * @return TRUE keep, FALSE remove, null leave alone
	 */
	private Boolean evaluateRptPosConSdt(String tagVal, int[] posSize) {

		if (posSize==null) {
			// no enclosing repeat instance: bind.xslt omits in this case
			log.warn("od:RptPosCon outside any repeat instance: omitting. " + tagVal);
			return Boolean.FALSE;
		}
		if (posSize==UNKNOWN_POSITION) return null;

		String expression = null;
		try {
			expression = BindingTraverserXSLT.getRepeatPositionCondition(xpathsMap, tagVal);
		} catch (Exception e) {
			log.error("Can't get repeat position condition for " + tagVal, e);
		}
		if (expression==null) return null;

		return evaluateRptPosCon(expression, posSize[0], posSize[1]);
	}

	private static String getSdtTagVal(Object o) {

		if (o instanceof SdtElement) {
			SdtPr sdtPr = ((SdtElement)o).getSdtPr();
			if (sdtPr!=null && sdtPr.getTag()!=null) {
				return sdtPr.getTag().getVal();
			}
		}
		return null;
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
			if (sdtPr.getDataBinding()!=null && pic!=null
					&& replaceBlipEmbed(sdt, pkg, part, xpathsMap)) {
				// authored drawing preserved; just its image rel was replaced
				// (as bind.xslt's picture3 mode does)

			} else if (sdtPr.getDataBinding()!=null && pic!=null) {

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
				// Do nothing
			} else if (map!=null && map.containsKey(OpenDoPEHandler.BINDING_RESULT_RPTD) ) {
				// Do nothing
				
			} else if (map!=null && map.containsKey(OpenDoPEHandler.BINDING_ROLE_RPT_POS_CON) ) {
				// Already handled by processRptPosCons (those which remain are the
				// ones whose position condition was satisfied, or couldn't be
				// evaluated); their literal content is wanted as-is

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

				// covers w15:dataBinding too (getDataBinding returns either)
				List<Object> boundContent = generateBoundContent(pkg, part, sdtPr,
						xpathsMap, isMultiline(sdtPr));
				if (boundContent!=null) {
					applyBoundContent(sdt, boundContent);
				}

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

		

		
		
		
	}
	
	

}
