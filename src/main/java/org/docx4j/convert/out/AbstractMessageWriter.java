package org.docx4j.convert.out;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

public abstract class AbstractMessageWriter {
	protected static Logger log = Logger.getLogger(AbstractMessageWriter.class);
	
	public DocumentFragment notImplemented(NodeIterator nodes, String message) {

		Node n = nodes.nextNode();
		log.warn("NOT IMPLEMENTED: support for "+ n.getNodeName() + "; " + message);
		
		if (log.isDebugEnabled() ) {
			
			if (message==null) message="";
			
			log.debug( XmlUtils.w3CDomNodeToString(n)  );

			// Return something which will show up in the HTML
			return message("NOT IMPLEMENTED: support for " + n.getNodeName() + " - " + message);
		} else {
			
			// Put it in a comment node instead?
			
			return null;
		}
	}
	
	public DocumentFragment message(String message) {
		
		if (!log.isDebugEnabled()) return null;

		String documentFragment = getOutputPrefix() 
			+ message
			+ getOutputSuffix();  

		javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory
				.newInstance();
		dbf.setNamespaceAware(true);
		StringReader reader = new StringReader(documentFragment);
		InputSource inputSource = new InputSource(reader);
		Document doc = null;
		try {
			doc = dbf.newDocumentBuilder().parse(inputSource);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reader.close();

		DocumentFragment docfrag = doc.createDocumentFragment();
		docfrag.appendChild(doc.getDocumentElement());
		return docfrag;		
	}

	protected abstract String getOutputPrefix();
	
	protected abstract String getOutputSuffix();
}
