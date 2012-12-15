package org.docx4j.model.datastorage;

import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTDataBinding;

public class BindingHandler {
	
	private static Logger log = Logger.getLogger(BindingHandler.class);		
	
//	static Templates xslt;			
	private static XPathFactory xPathFactory;
	private static XPath xPath;
	static {
//		try {
//			Source xsltSource = new StreamSource(
//						org.docx4j.utils.ResourceUtils.getResource(
//								"org/docx4j/model/datastorage/bind.xslt"));
//			xslt = XmlUtils.getTransformerTemplate(xsltSource);
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (TransformerConfigurationException e) {
//			e.printStackTrace();
//		}
		
		xPathFactory = XPathFactory.newInstance();
		xPath = xPathFactory.newXPath();		
	}


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
	 * If you do this, you will need to post-process with RemovalHandler, since a
	 * content control with SdtPr w:dataBinding and w:text
	 * which contains a w:hyperlink will prevent Word 2007 from
	 * opening the docx.
	 * 
	 * Due to the architecture of this class, this is a static flag changing the
	 * behavior of all following calls to {@link #applyBindings}.
	 * 
	 * @param hyperlinkStyleID
	 *            The style to use for hyperlinks (eg Hyperlink)
	 */
	public static void setHyperlinkStyle (
			String hyperlinkStyleID) {
		hyperlinkStyleId = hyperlinkStyleID;
	}
	public static String getHyperlinkStyleId() {
		return hyperlinkStyleId;
	}
	
	private static String hyperlinkStyleId = null;
	
	
	
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
		

		public static void applyBindings(WordprocessingMLPackage wordMLPackage) throws Docx4JException {

			// A component can apply in both the main document part,
			// and in headers/footers. See further
			// http://forums.opendope.org/Support-components-in-headers-footers-tp2964174p2964174.html
			
			if (hyperlinkStyleId !=null) {
					wordMLPackage.getMainDocumentPart().getPropertyResolver().activateStyle(hyperlinkStyleId);
			}			

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
	
		public static void applyBindings(JaxbXmlPart part) throws Docx4JException {
			
			org.docx4j.openpackaging.packages.OpcPackage pkg 
				= part.getPackage();		
				// Binding is a concept which applies more broadly
				// than just Word documents.
			
			if (hyperlinkStyleId !=null && pkg instanceof WordprocessingMLPackage) {
				((WordprocessingMLPackage)pkg).getMainDocumentPart().getPropertyResolver().activateStyle(hyperlinkStyleId);
			}
						
			XPathsPart xPathsPart = null;
			
			if ( ((WordprocessingMLPackage)pkg).getMainDocumentPart().getXPathsPart() == null) {
				log.error("OpenDoPE XPaths part missing");
				//throw new Docx4JException("OpenDoPE XPaths part missing");
			} else {
				xPathsPart = ((WordprocessingMLPackage)pkg).getMainDocumentPart().getXPathsPart();
				//log.debug(XmlUtils.marshaltoString(xPaths, true, true));
			}
			
			
			BindingTraverserInterface traverser = new BindingTraverserXSLT();
			
			part.setJaxbElement(
					traverser.traverseToBind(part, pkg, xPathsPart) );
					
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
					log.error("Couldn't locate part by storeItemId " + storeItemId);
					return null;
				}
				
				if (log.isDebugEnabled() ) {
					String r = part.xpathGetString(xpath, prefixMappings);
					log.debug(xpath + " yielded result " + r);
					return r;
				} else {
					return part.xpathGetString(xpath, prefixMappings);
				}
			} catch (Docx4JException e) {
				e.printStackTrace();
				return null;
			}
		}



}
