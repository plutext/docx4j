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

import org.docx4j.UnitsOfMeasurement;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.writer.AbstractTableWriter;
import org.docx4j.convert.out.common.writer.AbstractTableWriterModel;
import org.docx4j.convert.out.common.writer.AbstractTableWriterModelCell;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.table.tc.TextDir;
import org.docx4j.model.table.TableModelCell;
import org.docx4j.wml.TcPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/*
 *  @author Alberto Zerolo, Adam Schmideg, Jason Harrop
 *  
*/
public class TableWriter extends AbstractTableWriter {
	protected final static Logger logger = LoggerFactory.getLogger(TableWriter.class);
	protected final static String TABLE_BORDER_MODEL = "border-collapse";
	
//	@Override
//	protected Logger getLog() {
//		return logger;
//	}
	
  	@Override
	protected Element createNode(Document doc, int nodeType) {
  	Element ret = null;
  		switch (nodeType) {
  			case NODE_TABLE:
  				ret = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:table");
  				break;
  			case NODE_TABLE_COLUMN_GROUP:
  				break;
  			case NODE_TABLE_COLUMN:
  				ret = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:table-column");
  				break;
  			case NODE_TABLE_HEADER:
  				ret = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:table-header");
				break;
  			case NODE_TABLE_HEADER_ROW:
  				ret = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:table-row");
				break;
  			case NODE_TABLE_HEADER_CELL:
  				ret = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:table-cell");
				break;
  			case NODE_TABLE_BODY:
  				ret = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:table-body");
				break;
  			case NODE_TABLE_BODY_ROW:
  				ret = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:table-row");
				break;
  			case NODE_TABLE_BODY_CELL:
  				ret = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:table-cell");
				break;
  		}
  		return ret;
  	}

	@Override
	protected void applyAttributes(AbstractWmlConversionContext context, List<Property> properties, Element element) {
		XsltFOFunctions.applyFoAttributes(properties, element);
	}

	/** PDF paginates, so an over-wide table is scaled to the page as Word does. @since 17.0.5 */
	@Override
	protected boolean fitsTableToPage() {
		return true;
	}
	
	@Override
	protected void applyTableCustomAttributes(AbstractWmlConversionContext context, 
			AbstractTableWriterModel table, TransformState transformState, Element tableRoot) {
		
	int cellSpacing = ((table.getEffectiveTableStyle().getTblPr() != null) &&
					   (table.getEffectiveTableStyle().getTblPr().getTblCellSpacing() != null) &&
					   (table.getEffectiveTableStyle().getTblPr().getTblCellSpacing().getW() != null) ?
					   table.getEffectiveTableStyle().getTblPr().getTblCellSpacing().getW().intValue() : 0);	   
		// border model
	    // borderConflictResolutionRequired in TableModel is correct, but xsl-fo only knows about a
	    // cellSpacing (border-separation) on the table level. For this reason, cellSpacings on row-level
	    // are ignored.
				if (cellSpacing > 0) {
			tableRoot.setAttribute(TABLE_BORDER_MODEL, "separate"); // this is the default in CSS
			tableRoot.setAttribute("border-separation", 
					//WW seems only to store cellSpacing/2 but displays and applies cellSpacing * 2
					UnitsOfMeasurement.twipToBest(cellSpacing * 2));
			// Word puts a full gap (2 x tblCellSpacing) between the table border and the
			// outer cells, where the separate-border model puts half of one; the other
			// half comes as padding on the table (measured, CR-001 table-cellspacing:
			// Word's outer cell border sits 7.2pt inside the table border for 72 twips).
			// The columns give it back (see applyColumnCustomAttributes) so the table
			// keeps its grid width.  @since 17.0.5
			tableRoot.setAttribute("padding", UnitsOfMeasurement.twipToBest(cellSpacing));
		}
		else {
			tableRoot.setAttribute(TABLE_BORDER_MODEL, "collapse");
		}
		// table width
		if (table.getTableWidth() > 0) {
			tableRoot.setAttribute("width", UnitsOfMeasurement.twipToBest(table.getTableWidth()) );
		}

		// Word's content-based autofit sized these columns to hold their widest cell on
		// one line, so a line must not be re-broken by the width FOP charges for the
		// cell borders; WordLayoutFixups.cellLineWidth gives it back.  @since 17.0.6
		if (table.isContentSizedColumns() && WordLayoutFixups.isEnabled()) {
			tableRoot.setAttribute(WordLayoutFixups.HINT_CONTENT_SIZED, "1");
		}

		applyStartIndent(context, table, tableRoot);


		// Hebrew: columns appear in reverse order
		// see http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/bidiVisual.html
		// @since 3.0.2
		if ((table.getEffectiveTableStyle().getTblPr() != null) 
				&& (table.getEffectiveTableStyle().getTblPr().getBidiVisual()!=null) 
				&& (table.getEffectiveTableStyle().getTblPr().getBidiVisual().isVal()) ) {

			tableRoot.setAttribute("writing-mode", "rl-tb");
			
		}
				
	}

	/**
	 * Where the table's grid edge goes, as Word puts it.
	 *
	 * <p>Where the compatibility mode is below 15 (Word 2013), Word places the
	 * <em>text</em> of the first column at the text margin plus w:tblInd, so the
	 * grid edge itself sits one left cell margin further back: margin + tblInd -
	 * tblCellMar/left.  docx4j put the grid edge at margin + tblInd and then added
	 * the cell margin as padding, so such a table's content (and its overflow past
	 * the right margin) was one left cell margin - usually 108 twips = 5.4pt - too
	 * far right.  Measured against Word for tblInd 0 and 108 (CR-001, real
	 * documents and the table-indent-compat14 probe).</p>
	 *
	 * <p>Word 2013 changed this: in mode 15 the <em>grid edge</em> goes at margin +
	 * tblInd, and the text one cell margin further right again (measured, probe
	 * table-indent-compat15: first cell text at 77.8pt for tblInd 0 and 83.1pt for
	 * tblInd 108, against 72.0 and 77.3 in mode 14).  A document with no
	 * compatibilityMode setting is mode 12 - Word opens it in compatibility mode -
	 * and so takes the older rule.</p>
	 *
	 * <p>A w:jc="center" table wider than the text column is a separate case: Word
	 * centres it, letting it overhang both margins, where we left-aligned it at the
	 * margin.  Then the grid edge is the negative half-overflow, and no cell margin
	 * is taken off (Word centres the grid, not the text).</p>
	 *
	 * @since 17.0.5
	 */
	private void applyStartIndent(AbstractWmlConversionContext context, AbstractTableWriterModel table, Element tableRoot) {

		org.docx4j.wml.CTTblPrBase tblPr = table.getEffectiveTableStyle().getTblPr();

		boolean centred = tblPr != null && tblPr.getJc() != null
				&& org.docx4j.wml.JcEnumeration.CENTER.equals(tblPr.getJc().getVal());
		int width = table.getTableWidth();
		int available = writableWidthTwips(context);
		int indent;
		if (centred && width > 0 && available > 0 && width > available) {
			indent = (available - width) / 2;
		} else {
			// Word ignores w:tblInd on a centred or right aligned table (as PropertyFactory does)
			indent = 0;
			if (!centred && (tblPr == null || tblPr.getJc() == null
					|| !org.docx4j.wml.JcEnumeration.RIGHT.equals(tblPr.getJc().getVal()))) {
				org.docx4j.wml.TblWidth tblInd = tblPr == null ? null : tblPr.getTblInd();
				if (tblInd != null && tblInd.getW() != null
						&& (tblInd.getType() == null || "dxa".equals(tblInd.getType()))) {
					indent = tblInd.getW().intValue();
				}
			}
			if (compatibilityMode(context) == 14) {
				int shift = leftCellMarginTwips(tblPr);
				indent -= shift;
				// Word does not shift a table nested in a w:tc (see isNested); whether
				// this one is is not known until the FO is assembled, since in the XSLT
				// pathway the w:tbl reaching here was unmarshalled on its own.
				if (shift != 0) {
					tableRoot.setAttribute(WordLayoutFixups.HINT_GRID_SHIFT,
							UnitsOfMeasurement.twipToBest(shift));
				}
			}
		}
		if (tblPr != null && tblPr.getTblpPr() != null && floatingTablesEnabled()) {
			indent = applyFloatingPosition(context, table, tableRoot, tblPr.getTblpPr(), indent);
		}
		tableRoot.setAttribute("start-indent", UnitsOfMeasurement.twipToBest(indent));
	}

	/**
	 * Word puts a table nested in a {@code w:tc} on the containing cell's <em>content</em>
	 * edge and adds its own cell margin on top of that, so the mode-14 grid-edge shift
	 * must not be applied to it: measured on a mode-14 header (page margin 28.35pt, outer
	 * {@code w:tblInd} 108, cell margin 108), Word's clip for a nested table runs from
	 * 33.9 = 28.35 + 5.4 and its text lands at 39.1, where docx4j drew it at 34.0 - one
	 * cell margin left, on every cell of every nested table (45 of them in 11 corpus
	 * documents).  {@link WordLayoutFixups#nestedTableGridEdge} gives the shift back to
	 * the tables that turn out to be nested.
	 *
	 * <p>The shift is Word 2010's, ie compatibility mode <em>14</em> exactly.  Below that,
	 * measured on a document with no {@code compatibilityMode} setting at all (mode 12)
	 * whose first row is one {@code w:gridSpan="3"} centred cell: Word centres it on
	 * 297.65, the exact page centre, so its grid edge is at margin + {@code w:tblInd}
	 * as in mode 15, and docx4j - which took the shift below mode 15 - centred it 5.4pt
	 * left.  The probes {@code table-indent-compat14} and {@code table-grid-edge-compat14}
	 * establish the rule for mode 14 itself.
	 *
	 * @since 17.0.6
	 */
	/** docx4j.convert.out.fo.tables.position (default true): whether a table's w:tblpPr
	 *  is honoured at all.  @since 17.0.6 */
	static boolean floatingTablesEnabled() {
		return WordLayoutFixups.isEnabled()
				&& org.docx4j.Docx4jProperties.getProperty("docx4j.convert.out.fo.tables.position", true);
	}

	/**
	 * A floating table (w:tblPr/w:tblpPr), as Word places it.
	 *
	 * <p><b>Horizontally</b> the frame is the page for {@code horzAnchor="page"} and the
	 * text column otherwise, and the table's <em>grid edge</em> goes at {@code tblpX}
	 * within it, or where {@code tblpXSpec} says (left / centre / right of the frame).
	 * Measured on the table-floating probe (mode 15, 72pt margins,
	 * {@code horzAnchor="margin" tblpX=4500}): Word's first cell text is at 302.7pt =
	 * 72 + 225 + one 5.4pt cell margin, so the grid edge is at the margin + tblpX with
	 * no compatibility-mode adjustment; and on a real cover page
	 * ({@code horzAnchor="margin" tblpXSpec="center"}, a 450.05pt table on a 594pt
	 * zero-margin page) Word's first cell text is at 77.8 = (594-450.05)/2 + 5.4.</p>
	 *
	 * <p><b>Vertically</b> only the positions Word measures from the page or the margin
	 * box are reproduced, since those are the ones XSL-FO can express: the table is then
	 * taken out of the flow into an absolutely positioned container (hints for
	 * {@link WordLayoutFixups#anchorFloatingTables}), which is what the cover pages and
	 * letterheads of the corpus need. {@code tblpY} against {@code vertAnchor="text"} -
	 * the common case, an offset from the paragraph the table is anchored to - leaves
	 * the table in the flow, because reserving its height there is what Word does only
	 * for a table too wide to have text beside it, and XSL-FO cannot wrap text around
	 * the rest.</p>
	 *
	 * @param indent the start-indent (twips, from the text margin) the non-floating
	 *        rules chose
	 * @return the start-indent to use
	 * @since 17.0.6
	 */
	private int applyFloatingPosition(AbstractWmlConversionContext context, AbstractTableWriterModel table,
			Element tableRoot, org.docx4j.wml.CTTblPPr tblpPr, int indent) {

		org.docx4j.model.structure.PageDimensions dims = pageDimensions(context);
		if (dims == null || dims.getPgSz() == null || dims.getPgSz().getW() == null
				|| dims.getPgSz().getH() == null || dims.getPgMar() == null) {
			return indent;
		}
		int pageW = dims.getPgSz().getW().intValue();
		int pageH = dims.getPgSz().getH().intValue();
		int marginLeft = intValue(dims.getPgMar().getLeft(), 0);
		int marginTop = intValue(dims.getPgMar().getTop(), 0);
		int marginBottom = intValue(dims.getPgMar().getBottom(), 0);
		int width = table.getTableWidth();

		// horizontal: the frame, then the grid edge within it
		boolean horzPage = org.docx4j.wml.STHAnchor.PAGE.equals(tblpPr.getHorzAnchor());
		int frameLeft = horzPage ? 0 : marginLeft;
		int frameWidth = horzPage ? pageW : dims.getWritableWidthTwips();
		Integer xInFrame = null;
		if (tblpPr.getTblpXSpec() != null) {
			switch (tblpPr.getTblpXSpec()) {
				case CENTER: xInFrame = width > 0 ? (frameWidth - width) / 2 : 0; break;
				case RIGHT: case OUTSIDE: xInFrame = width > 0 ? frameWidth - width : 0; break;
				default: xInFrame = 0; break; // left, inside
			}
		} else if (tblpPr.getTblpX() != null) {
			xInFrame = tblpPr.getTblpX().intValue();
		}
		if (xInFrame != null) indent = frameLeft - marginLeft + xInFrame;

		// vertical: only a position measured from the page or the margin box takes the
		// table out of the flow
		boolean vertPage = org.docx4j.wml.STVAnchor.PAGE.equals(tblpPr.getVertAnchor());
		Integer topTwips = null;
		int[] frame = null;
		String align = null;
		if (tblpPr.getTblpYSpec() != null && !org.docx4j.wml.STYAlign.INLINE.equals(tblpPr.getTblpYSpec())) {
			// measured: a cover-page table with tblpYSpec="bottom" and no vertAnchor has
			// its last line at y=765.4 on an A4 page with a 70.9pt bottom margin, ie its
			// bottom edge on the bottom margin, so the frame is the margin box
			frame = vertPage ? new int[] { 0, pageH }
					: new int[] { marginTop, pageH - marginTop - marginBottom };
			switch (tblpPr.getTblpYSpec()) {
				case CENTER: align = "center"; break;
				case BOTTOM: case OUTSIDE: align = "after"; break;
				default: align = "before"; break; // top, inside
			}
		} else if (tblpPr.getTblpY() != null
				&& (vertPage || org.docx4j.wml.STVAnchor.MARGIN.equals(tblpPr.getVertAnchor()))) {
			topTwips = (vertPage ? 0 : marginTop) + tblpPr.getTblpY().intValue();
		}
		if (topTwips == null && frame == null) return indent; // stays in the flow

		tableRoot.setAttribute(WordLayoutFixups.HINT_TBLP_LEFT,
				UnitsOfMeasurement.twipToBest(marginLeft + indent));
		if (topTwips != null) {
			tableRoot.setAttribute(WordLayoutFixups.HINT_TBLP_TOP, UnitsOfMeasurement.twipToBest(topTwips));
		} else {
			tableRoot.setAttribute(WordLayoutFixups.HINT_TBLP_FRAME,
					UnitsOfMeasurement.twipToBest(frame[0]) + " " + UnitsOfMeasurement.twipToBest(frame[1]));
			tableRoot.setAttribute(WordLayoutFixups.HINT_TBLP_ALIGN, align);
		}
		// the start-indent stands: the fixups zero it only where they do position the
		// table (which they decline to do for a table with text before it)
		return indent;
	}

	private static int intValue(java.math.BigInteger v, int fallback) {
		return v == null ? fallback : v.intValue();
	}

	private static org.docx4j.model.structure.PageDimensions pageDimensions(AbstractWmlConversionContext context) {
		try {
			return context.getSections().getCurrentSection().getPageDimensions();
		} catch (Exception e) {
			logger.debug("No section page dimensions: " + e.getMessage());
			return null;
		}
	}

	/** The document's w:compatSetting compatibilityMode (12 when it has none).
	 *  @since 17.0.5 */
	private static int compatibilityMode(AbstractWmlConversionContext context) {
		try {
			return org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart
					.getCompatibilityMode(context.getWmlPackage());
		} catch (Exception e) {
			logger.debug("No compatibility mode: " + e.getMessage());
			return 12;
		}
	}

	/** The effective left cell margin in twips: w:tblPr/w:tblCellMar/w:left (the table
	 *  style's is already merged into the effective tblPr), else Word's default 108. */
	private static int leftCellMarginTwips(org.docx4j.wml.CTTblPrBase tblPr) {
		if (tblPr != null && tblPr.getTblCellMar() != null && tblPr.getTblCellMar().getLeft() != null) {
			org.docx4j.wml.TblWidth left = tblPr.getTblCellMar().getLeft();
			if (left.getW() != null && (left.getType() == null || "dxa".equals(left.getType()))) {
				return left.getW().intValue();
			}
		}
		return AbstractTableWriter.WORD_DEFAULT_CELL_MARGIN_TWIPS;
	}

	private static int writableWidthTwips(AbstractWmlConversionContext context) {
		try {
			return context.getSections().getCurrentSection().getPageDimensions().getWritableWidthTwips();
		} catch (Exception e) {
			logger.debug("No section page dimensions: " + e.getMessage());
			return -1;
		}
	}

		/**
	 * Measure the cell's converted FO content: every span carries the physical
	 * font-family and (via its ancestors) the font-size that FOP will use, so the
	 * widths are FOP's own.  Words are split at white space; a word may run across
	 * spans.  Nested tables and leaders end a word.
	 *
	 * @since 17.0.5
	 */
	@Override
	protected double[] measureCellContent(AbstractWmlConversionContext context, org.docx4j.convert.out.common.writer.AbstractTableWriterModelCell cell) {
		Node content = cell.getContent();
		if (content == null) return new double[] { 0, 0 };
		double[] out = new double[2];
		NodeList children = content.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i) instanceof Element) measureBlockTree((Element) children.item(i), out);
		}
		return out;
	}

	private static void measureBlockTree(Element el, double[] out) {
		String ln = el.getLocalName();
		if ("table".equals(ln)) {
			// a nested table: treat as unbreakable at its own width if known, else ignore
			String w = el.getAttribute("width");
			double pt = org.docx4j.convert.out.fo.WordLayoutFixups.lengthPt(w);
			out[0] = Math.max(out[0], pt);
			out[1] = Math.max(out[1], pt);
			return;
		}
		if ("block".equals(ln) || "list-block".equals(ln) || "block-container".equals(ln)) {
			// a paragraph (or a container of them): measure its inline content as one line
			double[] line = new double[3]; // {maxWord, total, currentWord}
			measureInline(el, line, true);
			line[0] = Math.max(line[0], line[2]);
			out[0] = Math.max(out[0], line[0]);
			out[1] = Math.max(out[1], line[1]);
			return;
		}
		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i) instanceof Element) measureBlockTree((Element) children.item(i), out);
		}
	}

	/** Walk inline content; nested blocks (paragraphs inside a list item) each count as a line. */
	private static void measureInline(Element el, double[] line, boolean top) {
		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (n.getNodeType() == Node.TEXT_NODE) {
				measureText(n.getNodeValue(), fontFor(el), sizeFor(el), line);
			} else if (n instanceof Element) {
				Element c = (Element) n;
				String ln = c.getLocalName();
				if ("external-graphic".equals(ln) || "instream-foreign-object".equals(ln)) {
					/* An image (or an equation) is a word of its own, and Word's autofit
					 * sizes the column to it: a cell holding nothing but a picture used to
					 * measure zero and collapse to its cell margins, which in a table where
					 * every w:tcW is auto left the text columns to take the whole width, one
					 * word per line.  An anchored picture is taken out of the flow (see
					 * WordLayoutFixups), so it does not widen anything. */
					line[0] = Math.max(line[0], line[2]);
					line[2] = 0;
					double gw = c.hasAttribute(org.docx4j.model.images.WordXmlPictureE20.HINT_ANCHOR) ? 0
							: graphicWidthPt(c);
					if (gw > 0) {
						line[0] = Math.max(line[0], gw);
						line[1] += gw;
					}
				} else if ("leader".equals(ln) || "table".equals(ln)) {
					line[0] = Math.max(line[0], line[2]);
					line[2] = 0;
				} else if ("block".equals(ln) && !top) {
					double[] inner = new double[3];
					measureInline(c, inner, false);
					inner[0] = Math.max(inner[0], inner[2]);
					line[0] = Math.max(line[0], inner[0]);
					line[1] = Math.max(line[1], inner[1]);
				} else {
					measureInline(c, line, false);
				}
			}
		}
	}

	/** The rendered width of an fo:external-graphic / fo:instream-foreign-object, in points. @since 17.0.5 */
	private static double graphicWidthPt(Element g) {
		String w = g.getAttribute("content-width");
		if (w.length() == 0) w = g.getAttribute("width");
		if (w.endsWith("px")) {
			// AbstractWordXmlPicture writes the picture's size in points but labels the
			// unit px, and FOP's default source resolution is 72dpi, so here a px is a point
			try {
				return Double.parseDouble(w.substring(0, w.length() - 2));
			} catch (NumberFormatException e) {
				return 0;
			}
		}
		return WordLayoutFixups.lengthPt(w);
	}

	private static void measureText(String text, org.docx4j.fonts.PhysicalFont pf, double sizePt, double[] line) {
		org.docx4j.fonts.fop.fonts.Typeface tf = org.docx4j.fonts.TextMeasurer.typeface(pf);
		for (int i = 0; i < text.length(); ) {
			int cp = text.codePointAt(i);
			i += Character.charCount(cp);
			double w = org.docx4j.fonts.TextMeasurer.glyphWidthPt(tf, cp, sizePt);
			line[1] += w;
			if (Character.isWhitespace(cp) || cp == ' ' && false) {
				line[0] = Math.max(line[0], line[2]);
				line[2] = 0;
			} else {
				line[2] += w;
			}
		}
	}

	private static org.docx4j.fonts.PhysicalFont fontFor(Element el) {
		for (Node n = el; n instanceof Element; n = n.getParentNode()) {
			String f = ((Element) n).getAttribute("font-family");
			if (f != null && f.length() > 0) return org.docx4j.fonts.PhysicalFonts.get(f);
		}
		return null;
	}

	private static double sizeFor(Element el) {
		for (Node n = el; n instanceof Element; n = n.getParentNode()) {
			String f = ((Element) n).getAttribute("font-size");
			if (f != null && f.length() > 0) return org.docx4j.convert.out.fo.WordLayoutFixups.lengthPt(f);
		}
		return 11;
	}

	@Override
	protected void applyColumnCustomAttributes(AbstractWmlConversionContext context, AbstractTableWriterModel table, TransformState transformState, Element column, int columnIndex, int columnWidth) {
                column.setAttribute("column-number", Integer.toString(columnIndex + 1));
		if (columnWidth > -1) {
			int cellSpacing = ((table.getEffectiveTableStyle().getTblPr() != null) &&
					(table.getEffectiveTableStyle().getTblPr().getTblCellSpacing() != null) &&
					(table.getEffectiveTableStyle().getTblPr().getTblCellSpacing().getW() != null)) ?
					table.getEffectiveTableStyle().getTblPr().getTblCellSpacing().getW().intValue() : 0;
			if (cellSpacing > 0) {
				// Word: each column loses a whole gap and a half of the outer gaps (a 150pt
				// column with 3.6pt spacing holds a 139.2pt cell); FOP's separate model
				// takes one gap per column, so give up the extra half here.
				columnWidth = Math.max(1, columnWidth - cellSpacing);
			}
	        column.setAttribute("column-width", UnitsOfMeasurement.twipToBest(columnWidth) );
		}
	}
  	
  	@Override
	protected void applyTableCellCustomAttributes(AbstractWmlConversionContext context, AbstractTableWriterModel table, 
			TransformState transformState, 
			TableModelCell tableCell, Element cellNode, boolean isHeader, boolean isDummyCell) {
  		
  		if (isDummyCell) {
			cellNode.setAttribute("border-style", "none");
			cellNode.setAttribute("background-color", "transparent");
			cellNode.appendChild(cellNode.getOwnerDocument().createElementNS("http://www.w3.org/1999/XSL/Format", "fo:block"));
			return;
			/* return prevents

				 org.apache.fop.fo.ValidationException: The column-number or number of cells in the row overflows the number of fo:table-columns specified for the table. 
					at org.apache.fop.events.ValidationExceptionFactory.createException(ValidationExceptionFactory.java:38)
					at org.apache.fop.events.EventExceptionManager.throwException(EventExceptionManager.java:58)
					at org.apache.fop.events.DefaultEventBroadcaster$1.invoke(DefaultEventBroadcaster.java:175)
					at $Proxy37.tooManyCells(Unknown Source)
					at org.apache.fop.fo.flow.table.TableCellContainer.addTableCellChild(TableCellContainer.java:75)
					
				review whether this is the correct fix.
					
			*/
  		}
  		
		if (tableCell.getExtraCols() > 0) {
			
			cellNode.setAttribute("number-columns-spanned", Integer.toString(tableCell.getExtraCols() + 1));
			
		}
		if (tableCell.getExtraRows() > 0) {
			cellNode.setAttribute("number-rows-spanned", Integer.toString(tableCell.getExtraRows() + 1));
		}
  	}
	
  	@Override
  	protected void applyTableRowContainerCustomAttributes(AbstractWmlConversionContext context, AbstractTableWriterModel table, 
  			TransformState transformState, 
  			Element rowContainer, boolean isHeader) {
  		
  		// since start-indent is inherited, we need to counteract any setting on the table itself
  		// see http://stackoverflow.com/questions/12391778/shift-a-fop-table-to-the-right
  		rowContainer.setAttribute("start-indent", "0in");
  		// end-indent likewise: a table given a negative one (a merged continuous section
  		// carrying its own page margins, ConversionSectionWrapperFactory) passed it down to
  		// every paragraph in every cell, which then ran that far past the cell's edge.
  		// @since 17.0.6
  		rowContainer.setAttribute("end-indent", "0in");

  	}
  	
    /**
     * In the FO case, if we need to rotate the text, we do that
     * by inserting a block-container.
     * 
     * @param cellNode
     * @return
     */
  	@Override
    protected Element interposeBlockContainer(Document doc, Element cellNode, TcPr tcPr) {
    	
  		if (tcPr==null || tcPr.getTextDirection()==null) {
  			// usual case
  			return cellNode;
  		} else {
  			
  			/* We need block-container, something like:
  			 * 
	          <table-cell>
	            <block-container reference-orientation="90">
	              <block>Hello</block>
	            </block-container>
	          </table-cell>
            */
  			
  			Element ret = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:block-container");
  			
  			TextDir textDir = new TextDir(tcPr.getTextDirection());
  			textDir.setXslFO(ret);
  			
  			cellNode.appendChild(ret);
  			
  			if (cellNode.hasAttribute("reference-orientation")) {
  				// remove it, since it doesn't work at that level
  				cellNode.removeAttribute("reference-orientation");
  			}
  			
  			return ret;
  			
  		}
    }
  	
	
}
