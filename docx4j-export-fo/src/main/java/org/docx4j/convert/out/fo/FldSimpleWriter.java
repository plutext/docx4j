/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
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

 */
package org.docx4j.convert.out.fo;

import java.util.List;

import javax.xml.transform.TransformerException;

import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.ConversionSectionWrapper;
import org.docx4j.convert.out.common.writer.AbstractFldSimpleWriter;
import org.docx4j.convert.out.common.writer.AbstractPagerefHandler;
import org.docx4j.convert.out.common.writer.HyperlinkUtil;
import org.docx4j.convert.out.common.writer.RefHandler;
import org.docx4j.model.fields.FldSimpleModel;
import org.docx4j.model.fields.FormattingSwitchHelper;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.model.properties.Property;
import org.docx4j.utils.FoNumberFormatUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class FldSimpleWriter extends AbstractFldSimpleWriter {
	// NB see super class for definition of other handlers.
	protected static final String FO_NS = "http://www.w3.org/1999/XSL/Format";
	protected static final String XSL_NS = "http://www.w3.org/1999/XSL/Transform";
	
	/** What the page number will render as, per the section's page number format -
	 *  eg "๑" for thaiNumbers - so the right font can be selected for it.
	 *  (NUMPAGES/SECTIONPAGES use the page number format too; see the comment in
	 *  AbstractFOExporter.getSectionPageInformation.)
	 *
	 *  TODO: a \* format switch on the field itself (model.getFldParameters())
	 *  overrides the section format, and is ignored here.  Note that honouring it
	 *  for PAGE would take more than a different sample: fo:page-number is
	 *  formatted by FOP per the page-sequence's @format, which is per section.
	 *  The section format is the overwhelmingly common case.
	 *
	 * @since 17.0.3
	 */
	protected static String pageNumberSample(AbstractWmlConversionContext context) {
		String pageFormat = context.getSections().getCurrentSection().getPageNumberInformation().getPageFormat();
		return FoNumberFormatUtil.format(1, FormattingSwitchHelper.getFoPageNumberFormat(pageFormat));
	}

	/**
	 * {@code STYLEREF} in a header or footer becomes an {@code fo:retrieve-marker}, which
	 * FOP resolves page by page as Word does ({@link StyleRefMarkers}); every other field
	 * takes the handlers' route.
	 *
	 * @since 17.2.0
	 */
	@Override
	public Node toNode(AbstractWmlConversionContext context, Object unmarshalledNode, Node content,
			TransformState state, Document doc) throws TransformerException {
		Node retrieved = styleRefMarker(context, (CTSimpleField) unmarshalledNode, content, doc);
		if (retrieved != null) return retrieved;
		return super.toNode(context, unmarshalledNode, content, state, doc);
	}

	private Node styleRefMarker(AbstractWmlConversionContext context, CTSimpleField field, Node content,
			Document doc) throws TransformerException {
		boolean inHeaderOrFooter = context.getCurrentPart() instanceof HeaderPart
				|| context.getCurrentPart() instanceof FooterPart;
		if (!inHeaderOrFooter) return null;
		FldSimpleModel model = new FldSimpleModel();
		model.build(field, content);
		String styleId = StyleRefMarkers.retrievedStyleId(context.getWmlPackage(), true,
				model.getFldName(), model.getFldParameters());
		if (styleId == null) return null;
		Element retrieve = doc.createElementNS(FO_NS, "fo:retrieve-marker");
		retrieve.setAttribute("retrieve-class-name",
				StyleRefMarkers.className(styleId, StyleRefMarkers.wantsNumber(model.getFldParameters())));
		retrieve.setAttribute("retrieve-position",
				StyleRefMarkers.wantsLast(model.getFldParameters()) ? "last-starting-within-page"
						: "first-starting-within-page");
		retrieve.setAttribute("retrieve-boundary", "document");
		Node ret = wrap(context, retrieve, doc);
		// the field's run formatting (which \* MERGEFORMAT keeps), and a font chosen on
		// the stored result, which is text of the same kind as what will be retrieved
		String sample = (content == null ? null : content.getTextContent());
		applyStyle(context, model, ret, (sample == null || sample.trim().isEmpty()) ? "A" : sample);
		return ret;
	}

	protected static class PageHandler implements FldSimpleNodeWriterHandler {
		@Override
		public String getName() { return "PAGE"; }
		@Override
		public int getProcessType() { return PROCESS_APPLY_STYLE; }

		@Override
		public Node toNode(AbstractWmlConversionContext context, FldSimpleModel model, Document doc) throws TransformerException {
			return doc.createElementNS(FO_NS, "fo:page-number");
		}

		@Override
		public String getSampleText(AbstractWmlConversionContext context, FldSimpleModel model) {
			return pageNumberSample(context);
		}
	}
	
	protected abstract static class AbstractPagesHandler  implements FldSimpleNodeWriterHandler {
		protected String fieldName = null;
		protected AbstractPagesHandler(String fieldName) {
			this.fieldName = fieldName;
		}
		
		@Override
		public String getName() { return fieldName; }
		@Override
		public int getProcessType() { return PROCESS_APPLY_STYLE; }
		
		@Override
		public Node toNode(AbstractWmlConversionContext context, FldSimpleModel model, Document doc) throws TransformerException {
		Element ret = null;
			if (((FOConversionContext)context).isRequires2Pass()) {
				ret = doc.createElementNS(FO_NS, "fo:inline");
				ret.appendChild(doc.createTextNode("${" + getParameterName(context) + "}"));
			}
			else {
//				ret = doc.createElementNS(FO_NS, "fo:page-number-citation-last");
//				String refId = getRefid(context);
//				ret.setAttribute("ref-id", getRefid(context));
				
				// Workaround for missing space before fo:page-number-citation-last in FOP 1.1 output;
				// See http://apache-fop.1065347.n5.nabble.com/preserving-a-trailing-space-in-inline-td40644.html
				// Since this method returns a node, wrap the two nodes in an fo:wrapper

				ret = doc.createElementNS(FO_NS, "fo:wrapper");
				
				Element pncl = doc.createElementNS(FO_NS, "fo:page-number-citation-last");
				String refId = getRefid(context);
				pncl.setAttribute("ref-id", getRefid(context));
				
				ret.appendChild(pncl);
				
				ret.appendChild(
						// &#x200b;
						doc.createTextNode("\u200b")
				);
				
			}
			return ret;
		}

		@Override
		public String getSampleText(AbstractWmlConversionContext context, FldSimpleModel model) {
			return pageNumberSample(context);
		}

		protected abstract String getRefid(AbstractWmlConversionContext context);

		protected abstract String getParameterName(AbstractWmlConversionContext context);
	}
	
	protected static class NumpagesHandler extends AbstractPagesHandler {
		protected NumpagesHandler() {
			super("NUMPAGES");
		}

		@Override
		protected String getParameterName(AbstractWmlConversionContext context) {
			//The value of the numpages should be the same throughout the document,
			//but the page number formatting might change depending on the section. 
			//For this reason there is a numpages value per section.
			return "field_numpages_" + context.getSections().getCurrentSection().getId() + "_value";
		}

		@Override
		protected String getRefid(AbstractWmlConversionContext context) {
			/* Apache FOP ignores the id in the fo:root, for this reason 
			 * we pass the id of the last section.
			 * If it isn't a vanilla document then we have a 2 pass and this isn't
			 * used.
			 */
			List<ConversionSectionWrapper> wrappers = context.getSections().getList();
			return "section_" + wrappers.get(wrappers.size() - 1).getId();
		}
	}
	
	protected static class SectionpagesHandler extends AbstractPagesHandler {
		protected SectionpagesHandler() {
			super("SECTIONPAGES");
		}

		@Override
		protected String getParameterName(AbstractWmlConversionContext context) {
			return "field_sectionpages_" + context.getSections().getCurrentSection().getId() + "_value";
		}

		@Override
		protected String getRefid(AbstractWmlConversionContext context) {
			return "section_" + context.getSections().getCurrentSection().getId();
		}
	}
	
	protected static class PagerefHandler extends AbstractPagerefHandler {
		protected PagerefHandler() {
			super(HyperlinkUtil.FO_OUTPUT);
		}

		@Override
		public Node toNode(AbstractWmlConversionContext context, FldSimpleModel model, Document doc) throws TransformerException {
			Node error = fieldError(context, model, doc, ERROR_BOOKMARK_NOT_DEFINED);
			if (error != null) return error;
			Node node = super.toNode(context, model, doc);
			String shape = XsltFOFunctions.realTabs() && model.getContent() != null
					? placeholderShape(getTextcontent(model.getContent())) : null;
			if (shape != null) markCitations(node, shape);
			return node;
		}

		/**
		 * The width FOP should reserve for the page number while it is unknown, from the
		 * number Word last wrote here: its digits as zeros ("60" is "00"), or a roman number
		 * as it stands; null for anything else.  The citation is measured before the page it
		 * cites is laid out, and FOP's placeholder of three characters set a table of
		 * contents entry's two-digit number 6pt too wide, so an entry whose number fitted
		 * after its text was broken before the tab (corpus document 11657, entry 4.38: Word
		 * "...ichage.type» 60" on one line).  Word renumbers at export, so the number can
		 * change; its width seldom does.  @since 17.3.1
		 */
		static String placeholderShape(String cached) {
			if (cached == null) return null;
			String t = cached.trim();
			if (t.matches("[0-9]{1,6}")) return t.replaceAll("[0-9]", "0");
			if (t.matches("[ivxlcdm]{1,8}|[IVXLCDM]{1,8}")) return t;
			return null;
		}

		private static void markCitations(Node node, String shape) {
			if (node instanceof Element && "page-number-citation".equals(node.getLocalName())) {
				((Element) node).setAttribute(WordLayoutFixups.HINT_CITATION_PLACEHOLDER, shape);
			}
			for (Node c = node.getFirstChild(); c != null; c = c.getNextSibling()) markCitations(c, shape);
		}

		@Override
		protected Node createPageref(AbstractWmlConversionContext context, Document doc, String bookmarkId) {
		Element ret = doc.createElementNS(FO_NS, "fo:page-number-citation");
			ret.setAttribute("ref-id", bookmarkId);
			return ret;
		}
		
	}
	
	protected static class FoRefHandler extends RefHandler {
		protected FoRefHandler() {
			super(HyperlinkUtil.FO_OUTPUT);
		}

		@Override
		public Node toNode(AbstractWmlConversionContext context, FldSimpleModel model, Document doc) throws TransformerException {
			Node error = fieldError(context, model, doc, ERROR_REFERENCE_SOURCE_NOT_FOUND);
			return error != null ? error : super.toNode(context, model, doc);
		}
	}

	/** Word's text for a PAGEREF whose bookmark is missing, as an English Word prints it. */
	static final String ERROR_BOOKMARK_NOT_DEFINED = "Error! Bookmark not defined.";

	/** Word's text for a REF whose bookmark is missing, as an English Word prints it. */
	static final String ERROR_REFERENCE_SOURCE_NOT_FOUND = "Error! Reference source not found.";

	/**
	 * Word's error text for a REF or PAGEREF field whose bookmark the document does not hold,
	 * in the field result's formatting, or null where the field is not such a field.
	 *
	 * <p>Word's PDF export re-evaluates REF and PAGEREF, whatever the field cached, and for a
	 * missing bookmark prints its error text in the language of its own user interface (the
	 * corpora's cached error strings follow the author's Word, not the document's language);
	 * here that is English, the Word the layout rules were measured on.  The text takes the
	 * result's formatting with bold toggled ({@link WordLayoutFixups#HINT_FIELD_ERROR}, which
	 * needs the whole FO tree).  Measured over the four corpora: every one of the ~1,000 such
	 * fields in 12 documents (word-layout-rules.md &#xa7;4.4, "A reference whose bookmark is
	 * gone").  {@code docx4j.convert.out.fo.fieldErrors}: {@code en}, the default, or
	 * {@code cached} for the cached result (17.1.0 to 17.3.0).</p>
	 *
	 * @since 17.3.1
	 */
	static Node fieldError(AbstractWmlConversionContext context, FldSimpleModel model, Document doc, String text) {
		if ("cached".equalsIgnoreCase(org.docx4j.Docx4jProperties.getProperty(
				"docx4j.convert.out.fo.fieldErrors", "en"))) return null;
		List<String> params = model.getFldParameters();
		if (params == null || params.isEmpty() || !context.isMissingBookmark(params.get(0))) return null;
		Element wrapper = doc.createElementNS(FO_NS, "fo:inline");
		wrapper.setAttribute(WordLayoutFixups.HINT_FIELD_ERROR, "1");
		// (treeCopy, not importNode: on the XSLT pathway the content is Xalan's, which refuses it)
		if (model.getContent() != null) org.docx4j.XmlUtils.treeCopy(model.getContent(), wrapper);
		// the text goes where the result's first text was, in that text's formatting
		List<org.w3c.dom.Text> texts = new java.util.ArrayList<org.w3c.dom.Text>();
		collectTexts(wrapper, texts);
		if (texts.isEmpty()) {
			wrapper.appendChild(doc.createTextNode(text));
		} else {
			texts.get(0).setData(text);
			for (int i = 1; i < texts.size(); i++) texts.get(i).setData("");
		}
		return wrapper;
	}

	private static void collectTexts(Node node, List<org.w3c.dom.Text> out) {
		for (Node n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof org.w3c.dom.Text) {
				if (((org.w3c.dom.Text) n).getData().trim().length() > 0) out.add((org.w3c.dom.Text) n);
			} else {
				collectTexts(n, out);
			}
		}
	}

	protected FldSimpleWriter() {
		super(FO_NS, "fo:inline");
	}

	@Override
	protected void registerHandlers() {
		super.registerHandlers();
		registerHandler(new PageHandler());
		registerHandler(new HyperlinkWriter());
		registerHandler(new FoRefHandler());
		registerHandler(new PagerefHandler());
		registerHandler(new NumpagesHandler());
		registerHandler(new SectionpagesHandler());
	}

	@Override
	protected void applyProperties(List<Property> properties, Node node) {
		XsltFOFunctions.applyFoAttributes(properties, (Element)node);
	}

	/**
	 * RunFontSelector puts the physical font in @font-family; copy that
	 * to the node we generated (an fo:page-number, or the fo:wrapper
	 * containing an fo:page-number-citation-last).
	 *
	 * @since 17.0.3
	 */
	@Override
	protected void applyFont(Element source, Element target) {

		String fontFamily = source.getAttribute("font-family");
		if ((fontFamily != null) && (fontFamily.length() > 0)
				&& (target.getAttribute("font-family").length() == 0)) {
			target.setAttribute("font-family", fontFamily);
		}
	}
}
