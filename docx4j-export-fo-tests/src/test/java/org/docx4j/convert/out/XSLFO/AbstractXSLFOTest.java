package org.docx4j.convert.out.XSLFO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.docx4j.XmlUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public abstract class AbstractXSLFOTest {
	
	protected boolean isPresent(org.w3c.dom.Document domDoc, String xpathExpression) {
		
		List<Node> xpaths = xpath(domDoc, xpathExpression);	
		return (xpaths.size()>0);
	}

	protected boolean isAbsent(org.w3c.dom.Document domDoc, String xpathExpression) {
		
		List<Node> xpaths = xpath(domDoc, xpathExpression);	
		return (xpaths.size()==0);
	}
	
	protected org.w3c.dom.Document w3cDomDocumentFromByteArray(byte[] bytes) throws SAXException, IOException, ParserConfigurationException {

		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(bytes));
	}

	/**
	 * FOP's area tree for this package: what actually ended up on the page, so a test
	 * can count the lines a paragraph broke into or read a line's extent, rather than
	 * only asserting the FO it was given.
	 *
	 * <p>setOpcPackage (not setWmlPackage) is what builds the fop configuration from
	 * the fonts in use; FORendererApacheFOP then renders the area tree through the
	 * same font setup as PDF, so the measurements are the ones PDF would get.</p>
	 *
	 * @since 17.0.5
	 */
	protected org.w3c.dom.Document areaTree(
			org.docx4j.openpackaging.packages.WordprocessingMLPackage pkg, int flags) throws Exception {

		org.docx4j.convert.out.FOSettings foSettings = org.docx4j.Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg); // creates the fop config from the fonts in use
		foSettings.setApacheFopMime(org.apache.fop.apps.MimeConstants.MIME_FOP_AREA_TREE);

		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		org.docx4j.Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	/** The number of lines FOP broke the text into, across the whole area tree. */
	protected int lineCount(org.w3c.dom.Document areaTree) {
		return areaTree.getElementsByTagName("lineArea").getLength();
	}

    private List<Node> xpath(Node node, String xpathExpression) {
    	
        
        // create XPath
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        

		xpath.setNamespaceContext(new FONSContext());
        
        try {
            List<Node> result = new ArrayList<Node>();
            NodeList nl = (NodeList) xpath.evaluate(xpathExpression, node, XPathConstants.NODESET);
            for( int i=0; i<nl.getLength(); i++ ) {
                result.add(nl.item(i));
            }
            return result;
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static class FONSContext implements NamespaceContext {

		@Override
		public String getNamespaceURI(String prefix) {
			if (prefix.equals("fo")) return "http://www.w3.org/1999/XSL/Format";
			return null;
		}
		
		// fo="http://www.w3.org/1999/XSL/Format"

		@Override
		public String getPrefix(String namespaceURI) {
			if (namespaceURI.equals("http://www.w3.org/1999/XSL/Format")) return "fo";
			return null;
		}

		@Override
		public Iterator getPrefixes(String namespaceURI) {
			// TODO Auto-generated method stub
			return null;
		}

    }
    
}
