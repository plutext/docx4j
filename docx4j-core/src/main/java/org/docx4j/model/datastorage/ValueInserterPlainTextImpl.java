package org.docx4j.model.datastorage;

import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.datastorage.BindingTraverserXSLT.BookmarkCounter;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.RPr;
import org.opendope.xpaths.Xpaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;


public class ValueInserterPlainTextImpl implements ValueInserterPlainText {
	
	private static Logger log = LoggerFactory.getLogger(ValueInserterPlainTextImpl.class);		

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
		
		org.w3c.dom.Document docContainer = XmlUtils.neww3cDomDocument();
		DocumentFragment docfrag = docContainer.createDocumentFragment();
		
		StringTokenizer st = new StringTokenizer(val, "\n\r\f"); // tokenize on the newline character, the carriage-return character, and the form-feed character
		
		if (multiLine) {
			// our docfrag may contain several runs
			boolean firsttoken = true;
			while (st.hasMoreTokens()) {						
				String line = (String) st.nextToken();
				
				if (firsttoken) {
					firsttoken = false;
				} else {
					addBrRunToDocFrag(docfrag, rPr);
				}
				
				processString(sourcePart, docfrag, line, rPr);						
			}
			
		} else {
			// not multiline, so remove any CRLF in data;
			// our docfrag wil contain a single run
			StringBuilder sb = new StringBuilder();
			while (st.hasMoreTokens()) {						
				sb.append( st.nextToken() );
			}
			
			processString(sourcePart, docfrag, sb.toString(), rPr);
		}
		
		return docfrag;
	}

	
	protected void addBrRunToDocFrag(DocumentFragment docfrag, RPr rPr) throws Docx4JException {
		
		// Not sure whether there is ever anything of interest in the rPr, 
		// but add it anyway
		org.docx4j.wml.R  run = Context.getWmlObjectFactory().createR();		
		if (rPr!=null) {
			run.setRPr(rPr);
		}
		run.getRunContent().add(Context.getWmlObjectFactory().createBr());
		
		Document tmpDoc = XmlUtils.marshaltoW3CDomDocument(run);
		XmlUtils.treeCopy(tmpDoc.getDocumentElement(), docfrag);						
	}
	
	protected void processString(JaxbXmlPart sourcePart, DocumentFragment docfrag, String text, RPr rPr) throws Docx4JException {
				
		int pos = BindingHandler.getHyperlinkResolver().getIndexOfURL(text);
		if (pos==-1 ) {				
			addRunToDocFrag(sourcePart, docfrag,  text,  rPr);
			return;
		} else if (BindingHandler.getHyperlinkResolver().getHyperlinkStyleId() == null){
			log.warn("No Hyperlink style set, not linking");
			addRunToDocFrag(sourcePart, docfrag,  text,  rPr);
			return;
		}
		
		// There is a hyperlink to deal with
		
		// We'll need to remove:
		//   <w:dataBinding w:storeItemID="{5448916C-134B-45E6-B8FE-88CC1FFC17C3}" w:xpath="/myxml[1]/element2[1]" w:prefixMappings=""/>
		//   <w:text w:multiLine="true"/>
		// or Word can't open the resulting docx, but we can't do it here,
		// since sdtPr is in effect read only.  So it is done in bind.xslt
		
		if (pos==0) {
			int spacePos = text.indexOf(" ");
			if (spacePos==-1) {
				addHyperlinkToDocFrag(sourcePart, docfrag,  text);
				return;					
			}
			
			// Could contain more than one hyperlink, so process recursively					
			String first = text.substring(0, spacePos);
			String rest = text.substring(spacePos);
			
			addHyperlinkToDocFrag( sourcePart,  docfrag,  first);
			// .. now the recursive bit ..
			processString(sourcePart,  docfrag,  rest,  rPr);	
			return;
		}
		
		String first = text.substring(0, pos);
		String rest = text.substring(pos);
		
		addRunToDocFrag( sourcePart,  docfrag,  first, rPr);
		// .. now the recursive bit ..
		processString(sourcePart,  docfrag,  rest, rPr);				
	}
	
	private void addRunToDocFrag(JaxbXmlPart sourcePart, DocumentFragment docfrag, String string, RPr rPr) {
		
		org.docx4j.wml.R  run = Context.getWmlObjectFactory().createR();		
		if (rPr!=null) {
			run.setRPr(rPr);
		}
		org.docx4j.wml.Text text = Context.getWmlObjectFactory().createText();
		run.getRunContent().add(text);
		if (string.startsWith(" ") || string.endsWith(" ") ) {
			// TODO: tab character?
			log.debug("setting xml:space=preserve for '" + string + "'");
			text.setSpace("preserve");
		}
		text.setValue(string);
					
		Document tmpDoc = XmlUtils.marshaltoW3CDomDocument(run);
				
		// avoid WRONG_DOCUMENT_ERR: A node is used in a different document than the one that created it.
		// but  NOT_SUPPORTED_ERR: The implementation does not support the requested type of object or operation. 
		// at com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl.importNode
		// docfrag.appendChild(fragdoc.importNode(document, true));
		// so:			
		XmlUtils.treeCopy(tmpDoc.getDocumentElement(), docfrag);
				
	}
	
	protected void addHyperlinkToDocFrag(JaxbXmlPart sourcePart, DocumentFragment docfrag, String url) throws Docx4JException {
		
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
								
		sourcePart.getRelationshipsPart().addRelationship(rel);  // addRelationship sets the rel's @Id

		try {
			Document tmpDoc = XmlUtils.marshaltoW3CDomDocument(
					BindingHandler.getHyperlinkResolver().generateHyperlink(rel.getId(), url));
			XmlUtils.treeCopy(tmpDoc.getDocumentElement(), docfrag);
		} catch (JAXBException e) {
			throw new Docx4JException(e.getMessage(), e);
		}
	}
	


}
