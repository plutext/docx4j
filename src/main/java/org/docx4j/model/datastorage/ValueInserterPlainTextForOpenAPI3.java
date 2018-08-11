package org.docx4j.model.datastorage;


import java.math.BigInteger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.datastorage.BindingTraverserXSLT.BookmarkCounter;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Text;
import org.docx4j.wml.P.Hyperlink;
import org.docx4j.wml.R;
import org.opendope.xpaths.Xpaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

/**
 * @author jharrop
 * @6.0.1
 */
public class ValueInserterPlainTextForOpenAPI3 extends ValueInserterPlainTextImpl {
	
	/*
		In OpenAPI 3, a Reference Object is a simple object to allow referencing 
		other components in the specification, internally and externally.
	
		The Reference Object is defined by JSON Reference and follows the same structure, 
		behavior and rules; reference resolution is accomplished as defined by the 
		JSON Reference specification
		
		Here we add a bookmark around components, currently just schemas.
		TODO: support other components (pretty trivial).     
	 */
	
	private static Logger log = LoggerFactory.getLogger(ValueInserterPlainTextForOpenAPI3.class);		
	
	public DocumentFragment toOpenXml(Xpaths.Xpath.DataBinding dataBinding, RPr rPr, boolean multiLine, BookmarkCounter bookmarkCounter, 
			String val, 
			JaxbXmlPart sourcePart) throws Docx4JException {
				
		try {
			if (val==null || val.equals("")) {
				return BindingTraverserXSLT.createPlaceholder(rPr, "p");
			}
		} catch (Exception e) {
			throw new Docx4JException(e.getMessage(), e);
		}
		
		// Is this a component definition, which needs a bookmark?
		// The challenge we have is that the incoming XPath will be something like:
		//   local-name(/yaml/components[1]/schemas[1]/*[2][1])
		// (assume value Pets here)
		// but we're wanting to generate a bookmark name
		//   components_schemas_Pets
		// There may be a generalised way to tell whether 2 XPaths are the "same":
		//   given the node result each XPath produces when evaluated, can you get a canonical XPath to that node
		//   and are these the same?  Or maybe each XPath returns the same node object?
		// But here, we can use an easy to understand heuristic.
		
		String bookmarkName = getBookmarkName(dataBinding.getXpath(), val);
		
		if (bookmarkName==null) {
			// Usual case
			return super.toOpenXml(dataBinding, rPr, multiLine, bookmarkCounter, val, sourcePart);
		}
		
		org.w3c.dom.Document docContainer = XmlUtils.neww3cDomDocument();
		DocumentFragment docfrag = docContainer.createDocumentFragment();
		
		/*
		    <w:bookmarkStart w:id="1" w:name="components_schemas_Pets"/>
		    <w:r>
		        <w:t>Pets</w:t>
		    </w:r>
		    <w:bookmarkEnd w:id="1"/>
		 */
		int id = bookmarkCounter.bookmarkId.get();
		
		// w:bookmarkStart w:id=\"" + id + "\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" />" +
		addBookmarkStart( bookmarkName,  id,  docfrag);
		/*
		 * BE WARNED: putting w:bookmarkStart inside a text sdt causes Word
		 * (2016) at least to not open docx, giving decidedly unhelpful
		 * HRESULT 0x800004005 at line 0 column 0.
		 * 
		 * So in a new IntegrityAfterBind step, we'll move this bookmark outside of the control.
		 * (The alternative to that would be to add something new to bind.xslt that either
		 *  allowed us to insert a bookmark before handing over to Java for the sdt content,
		 *  OR Java handles inserting the entire SDT (and so could insert a bookmark before) ). 
		 */
		
		// the run
		addRun( val, docfrag);
		
		// "<w:bookmarkEnd w:id=\"" + id + "\" />";
		addBookmarkEnd(id, docfrag);
		
		// ready for next time
		bookmarkCounter.bookmarkId.getAndIncrement(); 
		
		return docfrag;
	}

	private void addRun(String val, 
			DocumentFragment docfrag) {

		R r = new R();
		Text t = new Text();
		t.setValue(val);
		r.getContent().add(t);
	    
		Document tmpDoc = XmlUtils.marshaltoW3CDomDocument(r);
		XmlUtils.treeCopy(tmpDoc.getDocumentElement(), docfrag);
		
	}
	
	private void addBookmarkStart(String bookmarkName, int id, 
			DocumentFragment docfrag) {
		
	    CTBookmark bookmark = Context.getWmlObjectFactory().createCTBookmark(); 
	    JAXBElement<org.docx4j.wml.CTBookmark> bookmarkWrapped 
	    	= Context.getWmlObjectFactory().createPBookmarkStart(bookmark);

        bookmark.setName( bookmarkName ); 
        bookmark.setId( BigInteger.valueOf( id) ); 
	    
		Document tmpDoc = XmlUtils.marshaltoW3CDomDocument(bookmark);
		XmlUtils.treeCopy(tmpDoc.getDocumentElement(), docfrag);
		
	}

	private void addBookmarkEnd(int id, DocumentFragment docfrag) {
		
		CTMarkupRange markuprange = Context.getWmlObjectFactory().createCTMarkupRange(); 
	    markuprange.setId( BigInteger.valueOf(id ) ); 
	    
		Document tmpDoc = XmlUtils.marshaltoW3CDomDocument(markuprange);
		XmlUtils.treeCopy(tmpDoc.getDocumentElement(), docfrag);
	}
	

	private final static String COMPONENT_SCHEMAS = "local-name(/yaml/components[1]/schemas[1]/"; 
	private String getBookmarkName(String xpath, String val) {

		// Given:
		//   local-name(/yaml/components[1]/schemas[1]/*[2][1])
		// (assume value Pets here)
		// return bookmark name
		//   components_schemas_Pets
		
		if (xpath.startsWith(COMPONENT_SCHEMAS)) {
			String substring = xpath.substring(COMPONENT_SCHEMAS.length());
			
			if(substring.length()>0
					&& !substring.contains("/")) {
				return "components_schemas_" + val;				
			}
		}
		return null; // don't bookmark
	}
	
	protected void addHyperlinkToDocFrag(JaxbXmlPart sourcePart, DocumentFragment docfrag, String url) throws Docx4JException {
		
		if (url.startsWith("http")
				|| url.startsWith("mailto")) {
			super.addHyperlinkToDocFrag(sourcePart, docfrag, url);
			
		} else {
			// Handle eg #/components/schemas/Pets
			String bookmarkName = refToBookmarkName(url);
			try {
				Document tmpDoc = XmlUtils.marshaltoW3CDomDocument(
						BindingHandler.getHyperlinkResolver().generateHyperlink(bookmarkName, url));
				XmlUtils.treeCopy(tmpDoc.getDocumentElement(), docfrag);
			} catch (JAXBException e) {
				throw new Docx4JException(e.getMessage(), e);
			}
		}

	}
	
	private String refToBookmarkName(String ref) {
		
		// INPUT eg: "#/components/schemas/Pets"
		// OUTPUTS   components_schemas_Pets
		return ref.substring(2).replace("/", "_");
	}
	
	
//	public static void main(String[] args) throws Exception {
//
//		String sub = "#/components/schemas/Pets".substring(2);
//		
//		System.out.println(  sub.replace("/", "_")
//				);
//	}	

}
