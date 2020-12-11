/**
 *  Copyright 2013, Plutext Pty Ltd.
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.docx4j.Docx4jProperties;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.finders.RangeFinder;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTDataBinding;
import org.opendope.xpaths.Xpaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindingHandler {
	
	private static Logger log = LoggerFactory.getLogger(BindingHandler.class);		
	
	/**
	 * Configure, how the handler handles links found in Custom XML.
	 * 
	 * If hyperlinkStyleId is set to <code>null</code>, strings 
	 * containing 'http://' or 'https://' are not converted to 
	 * w:hyperlink. This is the default behavior.
	 * 
	 * If hyperlinkStyleId is set to <code>"someWordHyperlinkStyleName"</code>, 
	 * strings containing 'http://' or 'https://' or or 'mailto:' are converted to w:hyperlink.  
	 * The default Word hyperlink style name is "Hyperlink".
	 * 
	 * Due to the architecture of this class, this is a static flag changing the
	 * behavior of all following calls to {@link #applyBindings}.
	 * 
	 * @param hyperlinkStyleID
	 *            The style to use for hyperlinks (eg Hyperlink)
	 * @deprecated
	 */
	public static void setHyperlinkStyle (
			String hyperlinkStyleID) {
		getHyperlinkResolver().setHyperlinkStyle(hyperlinkStyleID);
	}
	/**
	 * @deprecated
	 */
	public static String getHyperlinkStyleId() {
		return getHyperlinkResolver().getHyperlinkStyleId();
	}
		
	
	/**
	 * @return the hyperlinkResolver
	 * @since 3.0.0
	 */
	public static BindingHyperlinkResolver getHyperlinkResolver() {
		
		if (hyperlinkResolver==null) {
			hyperlinkResolver = new BindingHyperlinkResolver(); 
		}
		return hyperlinkResolver;
	}
	/**
	 * @param hyperlinkResolver the hyperlinkResolver to set
	 * @since 3.0.0
	 */
	public static void setHyperlinkResolver(
			BindingHyperlinkResolver hyperlinkResolver) {
		BindingHandler.hyperlinkResolver = hyperlinkResolver;
	}
	
	private static BindingHyperlinkResolver hyperlinkResolver;
	
	private static ValueInserterPlainText valueInserterPlainText;

	public static ValueInserterPlainText getValueInserterPlainText() {
		if (valueInserterPlainText==null) {
			// Standard implementation
			valueInserterPlainText = new ValueInserterPlainTextImpl();
		}
		return valueInserterPlainText;
	}

	/**
	 * Allow user to customise what is inserted into the document when the
	 * bind is performed.
	 * 
	 * @param valueInserterPlainText
	 * @since 6.0.1
	 */
	public static void setValueInserterPlainText(ValueInserterPlainText valueInserterPlainText) {
		BindingHandler.valueInserterPlainText = valueInserterPlainText;
	}
	
	
	private DomToXPathMap domToXPathMap = null;
	public void setDomToXPathMap(DomToXPathMap domToXPathMap) {
		this.domToXPathMap = domToXPathMap;
	}

	private AtomicInteger bookmarkId = null;

	/**
	 * Provide a way to set the starting bookmark ID number
	 * for the purposes of Binding Traverse.
	 * 
	 * For efficiency, user code needs to pass this value through
	 * from the previous stage (repeats/condition handing).
	 * 
	 * If it isn't, the value will be calculated (less efficient).
	 *  
	 * New bookmarks could be created from XHTML, or renumbered
	 * in Flat OPC XML (TODO).
	 * 
	 * @param bookmarkId
	 * @since 3.2.1
	 */
	public void setStartingIdForNewBookmarks(AtomicInteger bookmarkId) {
		this.bookmarkId = bookmarkId;
		
	}
		
	protected AtomicInteger initBookmarkIdStart() {
		
		// The efficient case, where this value is set by user,
		// from previous step
		if (bookmarkId!=null) return bookmarkId;

		// The inefficient case, where we calculate again
		log.warn("Recalculating starting value for new bookmarks.  For efficiency, you should set this in your code.");
		int highestId = 0;
		
		RangeFinder rt = new RangeFinder("CTBookmark", "CTMarkupRange");
		new TraversalUtil(wordMLPackage.getMainDocumentPart().getContent(), rt);
		
		for (CTBookmark bm : rt.getStarts()) {
			
			BigInteger id = bm.getId();
			if (id!=null && id.intValue()>highestId) {
				highestId = id.intValue();
			}
		}
		return new AtomicInteger(highestId+1);
	}	
	
	
	
	
	/* ---------------------------------------------------------------------------
	 * Apply bindings
	 * 
	 * There are only 2 things we need to do here:
	 * 
	 * 1. inject the XML (form data) into the package.
	 *    - this is simply a matter of attaching it to this part.
	 *    
	 * 2. copy the XML data into the sdt's, so it is there
	 *    for PDF, HTML output.  (we don't actually need to
	 *    do anything for it to appear in the Word 2007 UI - 
	 *    Word does this step itself).  This will be a new
	 *    static method in this class.
	 */
	
	private WordprocessingMLPackage wordMLPackage;
		
	private BindingHandler() {}
	
	public BindingHandler(WordprocessingMLPackage wordMLPackage) {
		this.wordMLPackage = wordMLPackage;
	}

		@Deprecated
		public static void applyBindings(WordprocessingMLPackage wordMLPackage) throws Docx4JException {

			BindingHandler bh = new BindingHandler( wordMLPackage);
			bh.applyBindings();
		}

		public  void applyBindings() throws Docx4JException {

			// A component can apply in both the main document part,
			// and in headers/footers. See further
			// http://forums.opendope.org/Support-components-in-headers-footers-tp2964174p2964174.html
			
			getHyperlinkResolver().activateHyperlinkStyle(wordMLPackage);

			applyBindings(wordMLPackage.getMainDocumentPart());
	
			// Add headers/footers
			RelationshipsPart rp = wordMLPackage.getMainDocumentPart()
					.getRelationshipsPart();
			for (Relationship r : rp.getRelationships().getRelationship()) {
	
				if (r.getType().equals(Namespaces.HEADER)) {
					applyBindings((HeaderPart) rp.getPart(r));
				} else if (r.getType().equals(Namespaces.FOOTER)) {
					applyBindings((FooterPart) rp.getPart(r));
				}
			}
		}

		Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap = null;
		
		public Map<String, org.opendope.xpaths.Xpaths.Xpath> getXpathsMap() {
			return xpathsMap;
		}
		
		public void applyBindings(JaxbXmlPart part) throws Docx4JException {
			
//			org.docx4j.openpackaging.packages.OpcPackage pkg 
//				= part.getPackage();		
//				// Binding is a concept which applies more broadly
//				// than just Word documents.
			
//			if (pkg instanceof WordprocessingMLPackage) {
				getHyperlinkResolver().activateHyperlinkStyle(wordMLPackage);
//			}
				
			
			if ( wordMLPackage.getMainDocumentPart().getXPathsPart() == null) {
				log.warn("OpenDoPE XPaths part missing"); // OK if no OpenDoPE stuff is used
				xpathsMap = new HashMap<String, org.opendope.xpaths.Xpaths.Xpath>();
			} else {
				org.opendope.xpaths.Xpaths xPaths = wordMLPackage.getMainDocumentPart().getXPathsPart()
						.getJaxbElement();
				//log.debug(XmlUtils.marshaltoString(xPaths, true, true));
				
				xpathsMap = new HashMap<String, org.opendope.xpaths.Xpaths.Xpath>(xPaths.getXpath().size());
				
				for (Xpaths.Xpath xp : xPaths.getXpath() ) {
					
					if (xpathsMap.put(xp.getId(), xp)!=null) {
						log.error("Duplicates in XPaths part: " + xp.getId());
					}
					// TODO key should include storeItemID?
				}
				
			}
				
//			} else {
//				xPathsPart = ((WordprocessingMLPackage)pkg).getMainDocumentPart().getXPathsPart();
//				//log.debug(XmlUtils.marshaltoString(xPaths, true, true));
//			}
			
			
			BindingTraverserInterface traverser = null;
			
			if ( Docx4jProperties.getProperty("docx4j.model.datastorage.BindingHandler.Implementation", "BindingTraverserXSLT")
					.equals("BindingTraverserNonXSLT") ) {
				// Use the non-XSLT approach.  This is faster, but doesn't have feature parity.
				log.info("Using BindingTraverserNonXSLT, which is faster, but missing some features");
				traverser = new BindingTraverserNonXSLT();
			} else {
				// Slower, fully featured. The default.
				log.info("Using BindingTraverserXSLT, which is slower, but fully featured");
				traverser = new BindingTraverserXSLT();
				((BindingTraverserXSLT)traverser).setDomToXPathMap(this.domToXPathMap);
			}
			
			traverser.setStartingIdForNewBookmarks(initBookmarkIdStart());
			
				part.setJaxbElement(
						traverser.traverseToBind(part, wordMLPackage, xpathsMap) );
			
			bookmarkId = traverser.getNextBookmarkId();
					
		}


		// These Store Item ID's come from Building Blocks.dotx glossary document
		// Of these, only CoverPageProps is documented as an "Office Well Defined Custom XML Part",
		// but even then, that documentation does not allocate a store item ID.
		public static final String CORE_PROPERTIES_STOREITEMID = 		"{6C3C8BC8-F283-45AE-878A-BAB7291924A1}";
		public static final String EXTENDED_PROPERTIES_STOREITEMID = 	"{6668398D-A668-4E3E-A5EB-62B293D839F1}";
		public static final String COVERPAGE_PROPERTIES_STOREITEMID = 	"{55AF091B-3C7A-41E3-B477-F2FDAA23CFDA}";
		

		public static String xpathGetString(
				WordprocessingMLPackage pkg, Map<String, CustomXmlPart> customXmlDataStorageParts,
				CTDataBinding db) {
			
			return xpathGetString(
					 pkg,  customXmlDataStorageParts,
					db.getStoreItemID(), db.getXpath(), db.getPrefixMappings() );
		}
		
		/**
		 * Used by OpenDoPE handler, but not directly by bind.xslt anymore.
		 * Not multiLine aware.
		 * 
		 * @param customXmlDataStorageParts
		 * @param storeItemId
		 * @param xpath
		 * @param prefixMappings a string such as "xmlns:ns0='http://schemas.medchart'"
		 * @return
		 */
		public static String xpathGetString(
				WordprocessingMLPackage pkg, Map<String, CustomXmlPart> customXmlDataStorageParts,
				String storeItemId, String xpath, String prefixMappings) {
			
			log.debug(xpath + " with " + prefixMappings);
			
			if (xpath.contains("preceding-sibling")) {
				xpath = xpath.replace("][1]", "]"); // replace segment eg phase[1][1] to match map				
			}
			
			try {
				
				if (storeItemId.toUpperCase().equals(CORE_PROPERTIES_STOREITEMID)  ) {
					
					return pkg.getDocPropsCorePart().xpathGetString(xpath, prefixMappings);
					
				} else if (storeItemId.toUpperCase().equals(EXTENDED_PROPERTIES_STOREITEMID) ) {
					
					return pkg.getDocPropsExtendedPart().xpathGetString(xpath, prefixMappings);
				} 
				
				CustomXmlPart part  = customXmlDataStorageParts.get(storeItemId.toLowerCase());
					// Also handles cover page properties (since we've allocated it a store item id)
					// Note that Word does not create that part until the user provides one or more prop values
				
				if (part==null) {
					throw new InputIntegrityException("Couldn't locate part by storeItemId " + storeItemId);
//					log.error("Couldn't locate part by storeItemId " + storeItemId);
//					return null;
				}
				
//				if (log.isDebugEnabled() ) {
//					log.debug("Invoking " + part.getClass().getName() + ".cachedXPathGetString");
//				}
				String r = part.cachedXPathGetString(xpath, prefixMappings);  // typically org.docx4j.openpackaging.parts.CustomXmlDataStoragePart.cachedXPathGetString
				if (r==null) {
					// never expect null, since an empty result set is converted to an empty string
					log.error(xpath + " unexpectedly null!");
					return r;
				} else if (r.equals("")) {
					log.debug("XML element is missing (or empty) for xpath: " + xpath);
					// if WARN is enabled for org.docx4j.openpackaging.parts.XmlPart, logs will tell you which
					return r;
				} else if (log.isDebugEnabled() ) {
					log.debug(xpath + " yielded result '" + r + "'");
					return r;
				} else {
					return r;
				}
			} catch (Docx4JException e) {
				log.error(e.getMessage(), e);
//				return null;
				throw new InputIntegrityException(e.getMessage());
			}
		}



}
