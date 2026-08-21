package org.docx4j.model.datastorage;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.docx4j.utils.XPathFactoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

public abstract class BindingTraverserCommonImpl implements BindingTraverserInterface {
	
	private static Logger log = LoggerFactory.getLogger(BindingTraverserCommonImpl.class);		
	
	
	public abstract Object traverseToBind(JaxbXmlPart part,
			org.docx4j.openpackaging.packages.OpcPackage pkg,
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap)
			throws Docx4JException;
	
	
	protected AtomicInteger bookmarkId = null;

	@Override
	public void setStartingIdForNewBookmarks(AtomicInteger bookmarkId) {
		this.bookmarkId = bookmarkId;
		
	}
	
	
	@Override
	public AtomicInteger getNextBookmarkId() {
		return bookmarkId;

	}

	/**
	 * Evaluate an od:RptPosCon condition (eg "position()&lt;last()-1") for the repeat
	 * instance at 1-based position pos, in a repeat with size instances.
	 *
	 * The expression is applied as an XPath predicate over a node-set of size nodes,
	 * so position() and last() behave as in bind.xslt (which evaluates the expression
	 * against the node-set of repeat instances).
	 *
	 * @since 17.0.4
	 */
	protected static boolean evaluateRptPosCon(String expression, int pos, int size) {

		if (expression==null) return false;
		if (pos<1 || size<1 || pos>size) {
			log.warn("Unexpected repeat position " + pos + " of " + size);
			return false;
		}
		try {
			org.w3c.dom.Document doc = XmlUtils.neww3cDomDocument();
			org.w3c.dom.Element root = doc.createElement("r");
			doc.appendChild(root);
			for (int i=0; i<size; i++) {
				root.appendChild(doc.createElement("i"));
			}
			NodeList matched = (NodeList)XPathFactoryUtil.newXPath().evaluate(
					"i[" + expression + "]", root, XPathConstants.NODESET);
			org.w3c.dom.Node target = root.getChildNodes().item(pos-1);
			for (int i=0; i<matched.getLength(); i++) {
				if (matched.item(i).isSameNode(target)) return true;
			}
			return false;
		} catch (XPathExpressionException e) {
			log.error("Can't evaluate repeat position condition '" + expression + "'", e);
			return false;
		}
	}

}
