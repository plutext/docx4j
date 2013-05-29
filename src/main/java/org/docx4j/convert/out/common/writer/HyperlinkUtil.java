package org.docx4j.convert.out.common.writer;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.AbstractWmlConversionContext;
import org.docx4j.model.fields.HyperlinkModel;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** This class combines the generation of hyperlinks for 
 *  html and pdf.  
 *
 */
public class HyperlinkUtil {
	private final static Logger log = Logger.getLogger(HyperlinkUtil.class);
	public static final int HTML_OUTPUT = 1;
	public static final int FO_OUTPUT = 2;

	public static Node toNode(int outputType, AbstractWmlConversionContext context, HyperlinkModel model, Node content, Document doc) throws TransformerException {
	Node ret = content;
		try {
			context.handleHyperlink(model);
			switch (outputType) {
				case HTML_OUTPUT:
					ret = toHtmlNode(context, model, content, doc);
					break;
				case FO_OUTPUT:
					ret = toFoNode(context, model, content, doc);
					break;
				default:
					throw new IllegalArgumentException("Invalid output type: " + outputType);
			}
			XmlUtils.treeCopy(content.getChildNodes(), ret);
		} catch (Docx4JException e) {
			//If handleHyperlink throws an exception should the old model
			//be used or should the hyperlink be surpressed?
			//current solution: no hyperlink
			log.error("Excetion handling the hyperlinkModel: " + model, e);
		}
		return ret;
	}

	private static Node toFoNode(AbstractWmlConversionContext context, HyperlinkModel model, Node content, Document doc) {
		
		Element ret = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:basic-link");
		String internalTarget = model.getInternalTarget();
		String externalTarget = model.getExternalTarget();
		
		if (internalTarget==null
				&& externalTarget==null) {
			log.error("No targets found for " );
		}
		
		String location = null;
		
		if (model.isExternal()) {
			location = externalTarget;
			if ((internalTarget != null) && (internalTarget.length() > 0)) {
				location = location + "#" + internalTarget;
			}
			location = "url(" + location + ")";
			ret.setAttribute("external-destination", location);
		}
		else {
			ret.setAttribute("internal-destination", internalTarget);
		}
		if ((model.getTooltip() != null) && (model.getTooltip().length() > 0)) {
			ret.setAttribute("role", model.getTooltip());
		}
		return ret;
	}

	private static Node toHtmlNode(AbstractWmlConversionContext context, HyperlinkModel model, Node content, Document doc) {
		
		Element ret = doc.createElement("a");
		String internalTarget = model.getInternalTarget();
		String externalTarget = model.getExternalTarget();
		String location = null;
		
		if (model.isExternal()) {
			location = externalTarget;
			if ((internalTarget != null) && (internalTarget.length() > 0)) {
				location = location + "#" + internalTarget;
			}
			ret.setAttribute("href", location);
		}
		else {
			ret.setAttribute("href", "#" + internalTarget);
		}
		if ((model.getTgtFrame() != null) && (model.getTgtFrame().length() > 0)) {
			ret.setAttribute("target", model.getTgtFrame());
		}
		
		if ((model.getTooltip() != null) && (model.getTooltip().length() > 0)) {
			ret.setAttribute("alt", model.getTooltip());
		}
		return ret;
	}
	
}
