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
import org.docx4j.model.fields.FieldInstruction;
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
	 *  TODO: a \* format switch on the field itself (instruction.getParameters())
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
		FieldInstruction instruction = FieldInstruction.parse(field.getInstr());
		String styleId = StyleRefMarkers.retrievedStyleId(context.getWmlPackage(), true,
				instruction.getName(), instruction.getParameters());
		if (styleId == null) return null;
		Element retrieve = doc.createElementNS(FO_NS, "fo:retrieve-marker");
		retrieve.setAttribute("retrieve-class-name",
				StyleRefMarkers.className(styleId, StyleRefMarkers.wantsNumber(instruction.getParameters())));
		retrieve.setAttribute("retrieve-position",
				StyleRefMarkers.wantsLast(instruction.getParameters()) ? "last-starting-within-page"
						: "first-starting-within-page");
		retrieve.setAttribute("retrieve-boundary", "document");
		Node ret = wrap(context, retrieve, doc);
		// the field's run formatting (which \* MERGEFORMAT keeps), and a font chosen on
		// the stored result, which is text of the same kind as what will be retrieved
		String sample = (content == null ? null : content.getTextContent());
		applyStyle(context, field, ret, (sample == null || sample.trim().isEmpty()) ? "A" : sample);
		return ret;
	}

	protected static class PageHandler implements FldSimpleNodeWriterHandler {
		@Override
		public String getName() { return "PAGE"; }
		@Override
		public int getProcessType() { return PROCESS_APPLY_STYLE; }

		@Override
		public Node toNode(AbstractWmlConversionContext context, FieldInstruction instruction, Node cachedResult, Document doc) throws TransformerException {
			return doc.createElementNS(FO_NS, "fo:page-number");
		}

		@Override
		public String getSampleText(AbstractWmlConversionContext context, FieldInstruction instruction, Node cachedResult) {
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
		public Node toNode(AbstractWmlConversionContext context, FieldInstruction instruction, Node cachedResult, Document doc) throws TransformerException {
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
		public String getSampleText(AbstractWmlConversionContext context, FieldInstruction instruction, Node cachedResult) {
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
		public Node toNode(AbstractWmlConversionContext context, FieldInstruction instruction, Node cachedResult, Document doc) throws TransformerException {
			Node error = fieldError(context, instruction, cachedResult, doc, ERROR_BOOKMARK_NOT_DEFINED);
			if (error != null) return error;
			Node node = super.toNode(context, instruction, cachedResult, doc);
			String shape = XsltFOFunctions.realTabs() && cachedResult != null
					? placeholderShape(getTextcontent(cachedResult)) : null;
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
		public Node toNode(AbstractWmlConversionContext context, FieldInstruction instruction, Node cachedResult, Document doc) throws TransformerException {
			Node error = fieldError(context, instruction, cachedResult, doc, ERROR_REFERENCE_SOURCE_NOT_FOUND);
			return error != null ? error : super.toNode(context, instruction, cachedResult, doc);
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
	static Node fieldError(AbstractWmlConversionContext context, FieldInstruction instruction, Node content, Document doc, String text) {
		if ("cached".equalsIgnoreCase(org.docx4j.Docx4jProperties.getProperty(
				"docx4j.convert.out.fo.fieldErrors", "en"))) return null;
		List<String> params = instruction.getParameters();
		if (params == null || params.isEmpty() || !context.isMissingBookmark(params.get(0))) return null;
		Element wrapper = doc.createElementNS(FO_NS, "fo:inline");
		wrapper.setAttribute(WordLayoutFixups.HINT_FIELD_ERROR, "1");
		// (treeCopy, not importNode: on the XSLT pathway the content is Xalan's, which refuses it)
		if (content != null) org.docx4j.XmlUtils.treeCopy(content, wrapper);
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
		registerHandler(new FormCheckBoxHandler());
	}

	/**
	 * A legacy check box, drawn as Word draws it: a stroked square with nothing in the text
	 * layer, what follows set past it.  The instruction is {@code FormCheckBoxes}'; one
	 * that is not (a document's own fldSimple) keeps its stored result.
	 *
	 * <p>Measured on probe formcheckbox-metrics (Word's goldens, 5 November 2026; Arial and
	 * Times New Roman alike, modes 12 and 15 alike, 7 to 36pt): at a size s the square's side
	 * is 1.15s - 2.45 and it stands 0.98 from the line's position, a 0.72pt line drawn on
	 * those edges; the text after it starts 2.4 past the side; its foot is 0.219s - 1.42
	 * below the baseline.  Each within 0.25pt: Word's own values fall on a grid of 0.24pt.  A
	 * checked box adds both diagonals, a 0.48pt line, running 0.24 past the far corner.  A
	 * box the size of its run lifts neither the line nor a table row.</p>
	 *
	 * @since 17.3.6
	 */
	protected static class FormCheckBoxHandler implements FldSimpleNodeWriterHandler {

		private static final String SVG_NS = "http://www.w3.org/2000/svg";
		private static final double INSET = 0.98, GAP = 2.4, LINE = 0.72, CROSS = 0.48, CROSS_RUN = 0.24;

		@Override
		public String getName() {
			return "FORMCHECKBOX";
		}

		@Override
		public int getProcessType() {
			return PROCESS_WRAP_APPLY_STYLE; // the field's run, which the line's height counts
		}

		@Override
		public Node toNode(AbstractWmlConversionContext context, FieldInstruction instruction, Node cachedResult,
				Document doc) throws TransformerException {
			List<String> parameters = instruction.getParameters();
			if (parameters == null || parameters.size() < 3 || !parameters.get(0).matches("[0-9]+")) return cachedResult;
			double size = Integer.parseInt(parameters.get(0)) / 2d;
			boolean checked = "1".equals(parameters.get(1));
			String colour = parameters.get(2).matches("[0-9A-Fa-f]{6}") ? "#" + parameters.get(2) : "#000000";

			double side = Math.max(1, 1.15 * size - 2.45);
			double foot = 0.219 * size - 1.42;
			double below = LINE / 2 + CROSS_RUN; // the viewport's room under the square
			double width = side + GAP; // the advance: the side and 2.4, the square 0.98 in
			double height = LINE / 2 + side + below;
			double top = LINE / 2;

			Element object = doc.createElementNS(FO_NS, "fo:instream-foreign-object");
			object.setAttribute("content-width", pt(width));
			object.setAttribute("content-height", pt(height));
			object.setAttribute("alignment-adjust", pt(-(foot + below)));

			Element svg = doc.createElementNS(SVG_NS, "svg:svg");
			svg.setAttribute("class", WordLayoutFixups.CHECK_BOX_CLASS);
			svg.setAttribute("width", pt(width));
			svg.setAttribute("height", pt(height));
			svg.setAttribute("viewBox", "0 0 " + number(width) + " " + number(height));
			Element square = doc.createElementNS(SVG_NS, "svg:rect");
			square.setAttribute("x", number(INSET));
			square.setAttribute("y", number(top));
			square.setAttribute("width", number(side));
			square.setAttribute("height", number(side));
			square.setAttribute("fill", "none");
			square.setAttribute("stroke", colour);
			square.setAttribute("stroke-width", number(LINE));
			svg.appendChild(square);
			if (checked) {
				double far = side + CROSS_RUN;
				Element cross = doc.createElementNS(SVG_NS, "svg:path");
				cross.setAttribute("d", "M" + number(INSET) + " " + number(top) + "L" + number(INSET + far) + " " + number(top + far)
						+ "M" + number(INSET + far) + " " + number(top) + "L" + number(INSET) + " " + number(top + far));
				cross.setAttribute("fill", "none");
				cross.setAttribute("stroke", colour);
				cross.setAttribute("stroke-width", number(CROSS));
				svg.appendChild(cross);
			}
			object.appendChild(svg);
			return object;
		}

		private static String pt(double v) {
			return number(v) + "pt";
		}

		private static String number(double v) {
			return String.format(java.util.Locale.ROOT, "%.3f", v);
		}
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
