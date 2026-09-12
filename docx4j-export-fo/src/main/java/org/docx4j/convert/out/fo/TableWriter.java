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
		// cell borders; WordLayoutFixups.cellLineWidth gives it back.  @since 17.1.0
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
	 * <p><b>Modes 11 and 12 take it too</b>, measured on the table-grid-edge-compat12
	 * and -compat11 probes (w:tblInd 108, Word's default cell margins and the table's
	 * own w:tblCellMar 108): Word's first cell text is at 77.3pt in both, exactly as in
	 * mode 14, where the mode-15 geometry would put it at 83.1.  17.1.0 briefly
	 * restricted the shift to mode 14 alone, and then capped it at w:tblInd below mode
	 * 14, on the strength of one corpus document; the table-grid-edge-signed-compat12
	 * golden settled both.</p>
	 *
	 * <p><b>The shift is unconditional in every mode below 15</b>, whatever the sign of
	 * w:tblInd and whether or not the table has one.  Measured on
	 * table-grid-edge-signed-compat12: Word's first cell text is at 66.5pt for
	 * w:tblInd -108, at 54.0 for -360, and at 72.0 for no w:tblInd at all (fixed layout
	 * and autofit alike) on a 72pt margin - that is margin + tblInd exactly, so the grid
	 * edge is margin + tblInd - cellMargin throughout.  The cap min(shift, max(0, tblInd))
	 * 17.1.0 briefly took put them at 72.3 / 59.7 / 77.7.  Modes 14 and 15 of the same
	 * probe match the geometry already implemented.</p>
	 *
	 * <p>The corpus document behind that cap - mode 12, no w:tblInd, whose first row is a
	 * single w:gridSpan="3" cell holding a paragraph Word centres on 297.65pt, the exact
	 * centre of its 453.6pt text column - is a <em>content-autofit</em> table (w:tblW
	 * auto, every w:tcW auto), and what it really shows is where such a table's grid goes:
	 * Word's borders run 68.66..526.78 on a text column of 70.85..524.45, so the grid is
	 * one cell margin wider than the column at each end and it is the cell <em>content</em>
	 * that spans the column.  That is {@link #autofitGridAllowanceTwips}; with it the
	 * centre is the column's whatever the cell margin, and the shift needs no cap.</p>
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
		int gridShift = 0;
		/* A centred table is centred whether it is wider than the text column or
		 * narrower.  The test used to be width > available, so a table narrower than the
		 * column fell through to indent = 0 and sat at the left margin: measured on a
		 * corpus document whose one-column table carries w:tblPr/w:jc="center", Word puts
		 * the cell's "1" at x=174.3 and ours was at 75.7 - 99pt out, the whole table.
		 * (available - width) is negative for a wider table and positive for a narrower
		 * one, and half of it is the centring offset in both directions.  @since 17.1.0 */
		if (centred && width > 0 && available > 0) {
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
			int mode = compatibilityMode(context);
			if (mode < 15) {
				int shift = leftCellMarginTwips(table, tblPr);
				indent -= shift;
				// Word does not shift a table nested in a w:tc (see isNested); whether
				// this one is is not known until the FO is assembled, since in the XSLT
				// pathway the w:tbl reaching here was unmarshalled on its own.
				gridShift = shift;
				if (shift != 0) {
					tableRoot.setAttribute(WordLayoutFixups.HINT_GRID_SHIFT,
							UnitsOfMeasurement.twipToBest(shift));
				}
			}
		}
		if (tblPr != null && tblPr.getTblpPr() != null && floatingTablesEnabled()) {
			indent = applyFloatingPosition(context, table, tableRoot, tblPr.getTblpPr(), indent, gridShift);
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
	 * <p>The shift is taken in every mode below 15 (measured: probes
	 * {@code table-grid-edge-compat11}, {@code -compat12} and {@code -compat14}), capped
	 * at {@code w:tblInd} below mode 14 - see {@link #applyStartIndent}.  Below mode 14
	 * that cap alone puts a nested table (which carries no {@code w:tblInd} of its own)
	 * on the containing cell's content edge, and this pass then has nothing to give
	 * back; in mode 14, where the shift is unconditional, it has.</p>
	 *
	 * @since 17.1.0
	 */
	/** docx4j.convert.out.fo.tables.position (default true): whether a table's w:tblpPr
	 *  is honoured at all.  @since 17.1.0 */
	static boolean floatingTablesEnabled() {
		return WordLayoutFixups.isEnabled()
				&& org.docx4j.Docx4jProperties.getProperty("docx4j.convert.out.fo.tables.position", true);
	}

	/** docx4j.convert.out.fo.tables.float (default true): whether a text-anchored floating
	 *  table becomes an fo:float with the text flowing beside it.  @since 17.1.0 */
	static boolean floatingTablesWrap() {
		return org.docx4j.Docx4jProperties.getProperty("docx4j.convert.out.fo.tables.float", true);
	}

	/** A table taking more than this share of the text column has no room for text beside
	 *  it, so it is left in the flow (where the text follows it, which is where Word's
	 *  wrapping puts it too).  Measured: a CV built out of twelve text-anchored tables,
	 *  one of which takes 73% of the column, has Word putting the next table below it,
	 *  never beside it - FOP will fit whatever it can in the rest of the band, which cost
	 *  that document a page and 0.03 of line parity. */
	private static final double FLOAT_MAX_SHARE = 0.6;

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
	 * @since 17.1.0
	 */
	private int applyFloatingPosition(AbstractWmlConversionContext context, AbstractTableWriterModel table,
			Element tableRoot, org.docx4j.wml.CTTblPPr tblpPr, int indent, int gridShift) {

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
		if (xInFrame != null) {
			// the frame's own position, with no grid-edge shift in it
			indent = frameLeft - marginLeft + xInFrame;
			gridShift = 0;
		}

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
		if (topTwips == null && frame == null) {
			// text-anchored: an fo:float at the anchor paragraph, text beside it
			return floatBesideText(dims, tblpPr, tableRoot, indent, width, gridShift);
		}
		if (width > 0 && dims.getWritableWidthTwips() > 0
				&& width <= FLOAT_MAX_SHARE * dims.getWritableWidthTwips()) {
			// text can fit beside it, which is what decides whether Word's wrapping is
			// worth reproducing where the table is mid-document (WordLayoutFixups)
			tableRoot.setAttribute(WordLayoutFixups.HINT_TBLP_NARROW, "true");
		}

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

	/**
	 * A table anchored to the text (the default {@code w:vertAnchor="text"}, which is what
	 * 69 of the 74 {@code w:tblpPr} of the corpora say) sits beside the text: Word puts it
	 * {@code w:tblpY} below the top of the paragraph it is anchored to - the paragraph the
	 * {@code w:tbl} precedes - and flows that paragraph's text past it, narrowing only the
	 * lines the table's own band covers.
	 *
	 * <p>Measured on the table-floating probe (a 200pt table, {@code horzAnchor="margin"
	 * tblpX=4500 tblpY=1440}, in a 451.3pt column): Word's anchor paragraph starts at
	 * y=111.7 and the table's top edge is at 183.7 = 111.7 + 72, its first cell text at
	 * y=195.3 x=302.7 = 72 + 225 + 5.4; the anchor paragraph's own five lines run the full
	 * width above the table and the next paragraph's lines stop at 285.8, ie one
	 * {@code w:leftFromText} (9pt) short of the table.  docx4j laid the table out in the
	 * flow, which took the whole column width and pushed everything after it 102pt down
	 * the page.</p>
	 *
	 * <p>The FO is an {@code fo:float} at that point in the flow, at the edge the table is
	 * nearer, padded so the table sits where Word puts it and {@code w:leftFromText} /
	 * {@code w:rightFromText} away from the text ({@link WordLayoutFixups#anchorFloatingTables}
	 * builds it, the same machinery as an anchored picture, &#xa7;9.1).  FOP's floats are
	 * single-sided, so the text runs down one side only, where Word would run it down
	 * both; and a float begins at the line it is anchored at, so {@code tblpY} is padding
	 * above the table rather than a band the text flows past, which narrows the lines
	 * beside the padding too.</p>
	 *
	 * <p>Left in the flow, where the text follows the table instead of running beside it:
	 * a table which fills more than {@value #FLOAT_MAX_SHARE} of the column (nothing fits
	 * beside it, which is what Word's wrapping comes to as well), one whose horizontal
	 * position falls outside the text column (a page-anchored table in the margin), and a
	 * table in a section of more than one column, since FOP drops a float from a
	 * multi-column region silently.</p>
	 *
	 * @return the start-indent to use
	 * @since 17.1.0
	 */
	private int floatBesideText(org.docx4j.model.structure.PageDimensions dims,
			org.docx4j.wml.CTTblPPr tblpPr, Element tableRoot, int indent, int width, int gridShift) {

		if (!floatingTablesWrap()) return indent;
		int column = dims.getWritableWidthTwips();
		if (width <= 0 || column <= 0) return indent;
		if (dims.getColsNum() > 1) return indent;
		if (width > FLOAT_MAX_SHARE * column) return indent;
		/* Below mode 15 applyStartIndent has already moved the grid edge back by one cell
		 * margin (§6.1), which is about the grid and not about where Word puts the frame;
		 * a table with no w:tblpX therefore arrived here at -108 twips and was declined the
		 * float outright.  The band is measured from the unshifted position.  @since 17.1.0 */
		int bandStart = indent + gridShift;
		if (bandStart < 0 || bandStart + width > column) return indent;

		int leftFromText = intValue(tblpPr.getLeftFromText(), 0);
		int rightFromText = intValue(tblpPr.getRightFromText(), 0);
		boolean right = bandStart + width / 2.0 > column / 2.0;
		int padLeft = right ? leftFromText : bandStart;
		int padRight = right ? column - bandStart - width : rightFromText;
		int padTop = 0;
		if (tblpPr.getTblpY() != null && !org.docx4j.wml.STVAnchor.PAGE.equals(tblpPr.getVertAnchor())
				&& !org.docx4j.wml.STVAnchor.MARGIN.equals(tblpPr.getVertAnchor())) {
			padTop = Math.max(0, tblpPr.getTblpY().intValue());
		}
		tableRoot.setAttribute(WordLayoutFixups.HINT_TBLP_FLOAT, right ? "right" : "left");
		tableRoot.setAttribute(WordLayoutFixups.HINT_TBLP_PAD,
				UnitsOfMeasurement.twipToBest(Math.max(0, padLeft)) + " "
				+ UnitsOfMeasurement.twipToBest(Math.max(0, padRight)) + " "
				+ UnitsOfMeasurement.twipToBest(padTop));
		// the start-indent stands: the fixups zero it only where they do build the float
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

	/**
	 * Below compatibility mode 15 the grid edge sits one left cell margin back from the
	 * text margin (see {@link #applyStartIndent}), and it is the cell <em>content</em>
	 * that spans the text column: measured on a mode-12 corpus table (w:tblW auto, every
	 * w:tcW auto, w:tcMar 41 twips) on a 70.85..524.45pt text column, Word's grid runs
	 * 68.66..526.78 - one cell margin outside the column at each end - so a paragraph its
	 * gridSpan row centres lands on 297.65pt, the column's own centre.  Sizing the grid to
	 * the column instead centred it on 292.25 and cost that document two of Word's fifteen
	 * pages, which is what the (now removed) cap on the shift was papering over.
	 *
	 * @since 17.1.0
	 */
	@Override
	protected int autofitGridAllowanceTwips(AbstractWmlConversionContext context,
			AbstractTableWriterModel table, org.docx4j.wml.CTTblPrBase tblPr) {
		if (compatibilityMode(context) >= 15) return 0;
		return 2 * leftCellMarginTwips(table, tblPr);
	}

	/**
	 * The left cell margin the grid edge is measured from, in twips: the <em>first
	 * cell's</em> own w:tcMar/w:left where it has one, else w:tblPr/w:tblCellMar/w:left
	 * (the table style's is already merged into the effective tblPr), else Word's
	 * default 108.
	 *
	 * <p>The first cell wins because it is that cell's text the shift puts on
	 * margin + w:tblInd: measured on a mode-12 corpus table whose cells carry
	 * w:tcMar w:left="41" while the table declares no w:tblCellMar, Word's grid runs
	 * 68.66..526.78 on a 70.85..524.45 text column - 2.19pt outside it at each end,
	 * which is the cell's 41 twips (2.05pt) and not the default 108 (5.4pt).</p>
	 */
	private static int leftCellMarginTwips(AbstractTableWriterModel table,
			org.docx4j.wml.CTTblPrBase tblPr) {
		Integer own = firstCellLeftMarginTwips(table);
		if (own != null) return own.intValue();
		return leftCellMarginTwips(tblPr);
	}

	/** w:tcMar/w:left on the table's very first cell, or null where it declares none. */
	private static Integer firstCellLeftMarginTwips(AbstractTableWriterModel table) {
		try {
			if (table == null || table.getRows() == null || table.getRows().isEmpty()) return null;
			org.docx4j.model.table.TableModelRow row = table.getRows().get(0);
			if (row == null || row.size() == 0) return null;
			org.docx4j.model.table.TableModelCell cell = row.get(0);
			if (cell == null || cell.getTcPr() == null || cell.getTcPr().getTcMar() == null) return null;
			org.docx4j.wml.TblWidth left = cell.getTcPr().getTcMar().getLeft();
			if (left == null || left.getW() == null) return null;
			if (left.getType() != null && !"dxa".equals(left.getType())) return null;
			return Integer.valueOf(left.getW().intValue());
		} catch (Exception e) {
			logger.debug("No first-cell margin: " + e.getMessage());
			return null;
		}
	}

	private static int leftCellMarginTwips(org.docx4j.wml.CTTblPrBase tblPr) {
		if (tblPr != null && tblPr.getTblCellMar() != null && tblPr.getTblCellMar().getLeft() != null) {
			org.docx4j.wml.TblWidth left = tblPr.getTblCellMar().getLeft();
			if (left.getW() != null && (left.getType() == null || "dxa".equals(left.getType()))) {
				return left.getW().intValue();
			}
		}
		// the built-in Normal Table's 108 is in the effective tblPr where it applies (17.1.1); none otherwise
		return 0;
	}

	private static int writableWidthTwips(AbstractWmlConversionContext context) {
		try {
			return context.getSections().getCurrentSection().getPageDimensions().getWritableWidthTwips();
		} catch (Exception e) {
			logger.debug("No section page dimensions: " + e.getMessage());
			return -1;
		}
	}

	/** docx4j.convert.out.fo.tables.minimumAtBreakOpportunities: whether the autofit
	 *  sizer takes a cell's minimum width to be its widest run between the line
	 *  manager's break opportunities ({@link org.docx4j.fop.wordlayout.WordBreakOpportunities})
	 *  - a URL measured to its {@code ?} and its hyphens, where it will be broken - or,
	 *  {@code false}, its widest white-space-delimited token, as 17.1.0 measured.
	 *  @since 17.1.1 */
	public static final String MINIMUM_AT_BREAK_OPPORTUNITIES = "docx4j.convert.out.fo.tables.minimumAtBreakOpportunities";

	private static boolean minimumAtBreakOpportunities() {
		return org.docx4j.Docx4jProperties.getProperty(MINIMUM_AT_BREAK_OPPORTUNITIES, true);
	}

	/**
	 * Measure the cell's converted FO content: every span carries the physical
	 * font-family and (via its ancestors) the font-size that FOP will use, so the
	 * widths are FOP's own.  The minimum is the widest run between the line manager's
	 * break opportunities - UAX #14 as FOP applies it, with Word's solidus rules
	 * ({@link org.docx4j.fop.wordlayout.WordBreakOpportunities}), so that the
	 * measurement agrees with the engine that lays the text out.  Until 17.1.1 it was
	 * the widest white-space-delimited token: a 117-character URL which Word and the
	 * line manager both break after its {@code ?} and its hyphens was measured whole,
	 * its column sized to it (286pt where Word gives 216) and the column beside it
	 * starved to 44pt (Word: 77), which then broke a word a letter to a line.  A unit
	 * may run across spans, since FOP's text managers cannot break at the seam of two
	 * fo:inlines; nested tables and leaders end one.
	 *
	 * @since 17.0.5
	 */
	@Override
	protected double[] measureCellContent(AbstractWmlConversionContext context, org.docx4j.convert.out.common.writer.AbstractTableWriterModelCell cell) {
		Node content = cell.getContent();
		if (content == null) return new double[] { 0, 0, 0 };
		double[] out = new double[3];   // {min, max, widest picture}
		org.docx4j.fonts.Mapper mapper = context.getWmlPackage() == null ? null
				: context.getWmlPackage().getFontMapper();
		boolean atBreaks = minimumAtBreakOpportunities();
		NodeList children = content.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i) instanceof Element) measureBlockTree((Element) children.item(i), out, mapper, atBreaks);
		}
		return out;
	}

	/**
	 * One paragraph's measurement in progress: the widest unit so far, the whole
	 * content on one line, the unit being built, the ordinary spaces after it (dropped
	 * if a break comes next), and the widest picture, which cannot be squeezed.
	 * @since 17.1.1
	 */
	private static final class LineMeasure {
		double maxUnit, total, unit, pendingSpace, widestPicture;

		/** A break opportunity: the unit is complete, and the spaces after it fall at
		 *  the line end, where FOP drops them. */
		void endUnit() {
			maxUnit = Math.max(maxUnit, unit);
			unit = 0;
			pendingSpace = 0;
		}

		/** A character that stays with the unit - and so do the spaces before it that
		 *  no break separated from it (the space before a lone solidus, say). */
		void add(double w) {
			unit += pendingSpace + w;
			pendingSpace = 0;
		}

		double widest() {
			return Math.max(maxUnit, unit);
		}
	}

	private static void measureBlockTree(Element el, double[] out, org.docx4j.fonts.Mapper mapper, boolean atBreaks) {
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
			LineMeasure line = new LineMeasure();
			measureInline(el, line, true, mapper, atBreaks);
			out[0] = Math.max(out[0], line.widest());
			out[1] = Math.max(out[1], line.total);
			if (out.length > 2) out[2] = Math.max(out[2], line.widestPicture);
			return;
		}
		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i) instanceof Element) measureBlockTree((Element) children.item(i), out, mapper, atBreaks);
		}
	}

	/** Walk inline content; nested blocks (paragraphs inside a list item) each count as a line. */
	private static void measureInline(Element el, LineMeasure line, boolean top, org.docx4j.fonts.Mapper mapper, boolean atBreaks) {
		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (n.getNodeType() == Node.TEXT_NODE) {
				// consecutive text nodes are one FOText to FOP (FObjMixed appends
				// characters until a child element flushes them), so one text here
				StringBuilder text = new StringBuilder(n.getNodeValue());
				while (i + 1 < children.getLength() && children.item(i + 1).getNodeType() == Node.TEXT_NODE) {
					text.append(children.item(++i).getNodeValue());
				}
				measureText(text, fontFor(el, mapper), sizeFor(el), line, atBreaks);
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
					line.endUnit();
					double gw = c.hasAttribute(org.docx4j.model.images.WordXmlPictureE20.HINT_ANCHOR) ? 0
							: graphicWidthPt(c);
					if (gw > 0) {
						line.maxUnit = Math.max(line.maxUnit, gw);
						line.total += gw;
						line.widestPicture = Math.max(line.widestPicture, gw);   // a picture cannot be squeezed
					}
				} else if ("leader".equals(ln) || "table".equals(ln)) {
					line.endUnit();
				} else if ("block".equals(ln) && !top) {
					LineMeasure inner = new LineMeasure();
					measureInline(c, inner, false, mapper, atBreaks);
					line.maxUnit = Math.max(line.maxUnit, inner.widest());
					line.total = Math.max(line.total, inner.total);
					line.widestPicture = Math.max(line.widestPicture, inner.widestPicture);
				} else {
					measureInline(c, line, false, mapper, atBreaks);
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

	/**
	 * Measure one text node's characters into the line.  With {@code atBreaks} the
	 * unit ends wherever {@link org.docx4j.fop.wordlayout.WordBreakOpportunities#breakBefore}
	 * says a line may break; a run of ordinary spaces is held back and dropped when a
	 * break follows it (FOP drops that glue at the line end, and with the default
	 * white-space-treatment the next line starts flush), or counted into the unit when
	 * none does - the space before a lone solidus, say.  A trailing run of spaces is a
	 * break: FOP's text manager gives the whitespace that ends an FOText a break
	 * opportunity unconditionally.  Without {@code atBreaks} any white space ends the
	 * unit, as 17.1.0 measured.  A zero-width space (U+200B, U+2060, U+FEFF) is zero
	 * wide in either mode, as FOP has it, rather than the half em a font without the
	 * glyph would otherwise be charged.
	 */
	private static void measureText(CharSequence text, org.docx4j.fonts.PhysicalFont pf, double sizePt,
			LineMeasure line, boolean atBreaks) {
		org.docx4j.fonts.fop.fonts.Typeface tf = org.docx4j.fonts.TextMeasurer.typeface(pf);
		boolean[] brk = atBreaks ? org.docx4j.fop.wordlayout.WordBreakOpportunities.breakBefore(text) : null;
		int n = text.length();
		for (int i = 0; i < n; ) {
			int cp = Character.codePointAt(text, i);
			int at = i;
			i += Character.charCount(cp);
			double w = org.apache.fop.util.CharUtilities.isZeroWidthSpace(cp) ? 0
					: org.docx4j.fonts.TextMeasurer.glyphWidthPt(tf, cp, sizePt);
			line.total += w;
			if (brk == null) {
				if (Character.isWhitespace(cp)) line.endUnit(); else line.add(w);
				continue;
			}
			if (brk[at]) line.endUnit();
			if (cp == ' ' || cp == '\t') {
				line.pendingSpace += w;
			} else if (org.apache.fop.util.CharUtilities.isExplicitBreak(cp)) {
				line.endUnit();
			} else {
				line.add(w);
			}
		}
		if (brk != null && line.pendingSpace > 0) line.endUnit();
	}

	/**
	 * The face FOP will set this element's text in: the nearest {@code font-family}
	 * names the family (RunFontSelector writes the regular face's name for all four
	 * faces), and the nearest {@code font-weight} and {@code font-style} pick the
	 * face, resolved as FOP's own configuration resolves them.  Until 17.1.1 only the
	 * family was consulted, so every bold cell was measured in the regular face -
	 * 79.6pt for a heading FOP then drew at 89.1 - and the columns of a table with bold
	 * headings were split some 11-13% narrow, with the difference handed to a
	 * neighbour whose text then stayed on one line where Word wraps it.  A family
	 * without the face is measured, as it is drawn, in the regular one.
	 */
	private static org.docx4j.fonts.PhysicalFont fontFor(Element el, org.docx4j.fonts.Mapper mapper) {
		org.docx4j.fonts.PhysicalFont regular = null;
		boolean familySeen = false, weightSeen = false, styleSeen = false;
		boolean bold = false, italic = false;
		for (Node n = el; n instanceof Element && !(familySeen && weightSeen && styleSeen); n = n.getParentNode()) {
			Element e = (Element) n;
			if (!familySeen) {
				String f = e.getAttribute("font-family");
				if (f != null && f.length() > 0) {
					familySeen = true;
					regular = org.docx4j.fonts.PhysicalFonts.get(f);
				}
			}
			if (!weightSeen) {
				String w = e.getAttribute("font-weight");
				if (w != null && w.length() > 0) {
					weightSeen = true;
					bold = isBoldWeight(w);
				}
			}
			if (!styleSeen) {
				String s = e.getAttribute("font-style");
				if (s != null && s.length() > 0) {
					styleSeen = true;
					italic = "italic".equalsIgnoreCase(s) || "oblique".equalsIgnoreCase(s);
				}
			}
		}
		if (regular == null || (!bold && !italic)) return regular;
		org.docx4j.fonts.PhysicalFont face = org.docx4j.fonts.fop.util.FopConfigUtil.renderedFace(mapper, regular, bold, italic);
		return face == null ? regular : face;
	}

	/** "bold", "bolder", or a number FOP resolves to the bold face (it tries 700 and up before
	 *  400 for anything over 500). */
	private static boolean isBoldWeight(String w) {
		w = w.trim();
		if ("bold".equalsIgnoreCase(w) || "bolder".equalsIgnoreCase(w)) return true;
		try {
			return Integer.parseInt(w) >= 600;
		} catch (NumberFormatException e) {
			return false;
		}
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
			if (cellSpacing > 0 && !table.isContentSizedColumns()) {
				// Word: each column loses a whole gap and a half of the outer gaps (a 150pt
				// column with 3.6pt spacing holds a 139.2pt cell); FOP's separate model
				// takes one gap per column, so give up the extra half here.
				//
				// Only against a *grid* width, which is what includes the gaps.  A
				// content-autofit width is built from the measured content plus w:tblCellMar
				// and never had the gap in it, so taking it out here charged the spacing a
				// second time: measured on a w:tblW auto letterhead with
				// <w:tblCellSpacing w:w="15"/>, w:tblCellMar 15 and one 2799-twip grid
				// column, the autofit width 2736tw (136.8pt) became a 136.05pt column and,
				// less the cell's own 0.75pt of padding either side, a 134.57pt measure,
				// where the cell's one line needs 135.3 and is one line in Word.
				// 136.8 - the 1.5pt of border-separation is exactly Word's 135.3.
				// (@since 17.1.0)
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
		hideMark(tableCell);
  	}

	/**
	 * {@code docx4j.convert.out.fo.tables.hideMark} (default {@code true}): a cell with
	 * {@code w:hideMark} whose last paragraph paints nothing takes no line for it, as
	 * Word sizes the row.  {@code false} gives the mark its line, as 17.1.0 did.
	 * @since 17.1.1
	 */
	public static final String HIDE_MARK = "docx4j.convert.out.fo.tables.hideMark";

	/**
	 * <b>{@code w:hideMark}: the cell mark's height is ignored when the row is sized</b>
	 * (ECMA-376 17.4.23).  Word writes it on every cell of a table it imports from HTML,
	 * and an empty row of such cells is then as tall as its margins and borders alone.
	 * Measured on the {@code table-hidemark} probe (cell margins 15 twips, 12pt marks):
	 * three empty rows with the flag are 8.4pt together, 2.8 each, and the same with 6pt
	 * marks is identical - the mark's size plays no part; with 15 twips of cell spacing
	 * 5pt each; without the flag a full 13.8pt line each; a cell with text and the flag
	 * is unchanged.  A corpus letter template's three empty rows are 25.9pt in Word (its
	 * own margins, spacing and borders) and were 41.4 in docx4j, at four places in each
	 * of two documents, each a page too long.  107 corpus documents carry the flag on
	 * 13,977 cells, 1,792 of them empty.
	 *
	 * <p>The cell's last paragraph is the one whose mark is the cell mark.  Where it
	 * paints nothing - white space only, no graphic, leader or field - its block is
	 * taken out of the cell's content before it is written, so the row is sized by the
	 * cell's padding and borders (and a cell left with no block gets FOP's empty one
	 * from {@code WordLayoutFixups.blockForEmptyCell}).  Earlier empty paragraphs of the
	 * cell keep their lines: only the mark is hidden.</p>
	 *
	 * @since 17.1.1
	 */
	private static void hideMark(TableModelCell tableCell) {
		if (tableCell.isDummy() || tableCell.getTcPr() == null) return;
		org.docx4j.wml.BooleanDefaultTrue flag = tableCell.getTcPr().getHideMark();
		if (flag == null || !flag.isVal()) return;
		if (!org.docx4j.Docx4jProperties.getProperty(HIDE_MARK, true)) return;
		Node content = ((AbstractTableWriterModelCell) tableCell).getContent();
		if (content == null) return;
		Element last = null;
		NodeList kids = content.getChildNodes();
		for (int i = 0; i < kids.getLength(); i++) {
			Node n = kids.item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE) continue;
			String name = localName((Element) n);
			if ("block".equals(name) || "list-block".equals(name) || "table".equals(name) || "block-container".equals(name)) last = (Element) n;
		}
		if (last == null || !"block".equals(localName(last))) return;
		if (paintsAnything(last)) return;
		content.removeChild(last);
	}

	/** Whether anything in this block paints: text other than white space, or any
	 *  element which is not an inline wrapper. */
	private static boolean paintsAnything(Element el) {
		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (n.getNodeType() == Node.TEXT_NODE || n.getNodeType() == Node.CDATA_SECTION_NODE) {
				String t = n.getNodeValue();
				if (t == null) continue;
				for (int k = 0; k < t.length(); k++) {
					char c = t.charAt(k);
					if (!Character.isWhitespace(c) && c != '\u00a0' && c != '\u200b' && c != '\ufeff') return true;
				}
				continue;
			}
			if (n.getNodeType() != Node.ELEMENT_NODE) continue;
			String name = localName((Element) n);
			if ("inline".equals(name) || "basic-link".equals(name) || "wrapper".equals(name) || "bidi-override".equals(name)) {
				if (paintsAnything((Element) n)) return true;
				continue;
			}
			return true;
		}
		return false;
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
  		// @since 17.1.0
  		rowContainer.setAttribute("end-indent", "0in");

  	}

	/**
	 * {@code docx4j.convert.out.fo.tables.rowKeepWithNext} (default {@code true}): a row
	 * whose first paragraph carries {@code w:keepNext} is kept with the next row, and
	 * the last such row keeps the table with the paragraph after it, as Word does.
	 * {@code false} restores 17.1.0, where the keep lived only on the cells' blocks and
	 * FOP dropped it at the table's last row.  See {@link #applyTableRowCustomAttributes}.
	 * @since 17.1.1
	 */
	public static final String ROW_KEEP_WITH_NEXT = "docx4j.convert.out.fo.tables.rowKeepWithNext";

	/**
	 * <b>A row whose first paragraph keeps with the next keeps with the next row; the
	 * last such row keeps the table with what follows it.</b>  Word has no row-level keep:
	 * the paragraph's {@code w:keepNext} does the work.  Measured on the
	 * {@code table-row-keepnext} probe, Word consults the <b>first paragraph of the
	 * row's first cell</b> and nothing else: a row whose first cell's paragraph keeps and
	 * whose last cell's does not is kept (R2, the whole table moves to the next page);
	 * one whose last cell's paragraph keeps and whose first cell's does not is not (R3,
	 * its first row stays behind); nor is one whose first cell's <i>second</i> paragraph
	 * keeps while its first does not, though every other paragraph of the row keeps (R4).
	 * Applied to the last row it keeps the table with the paragraph after the table
	 * (R5), and a one-row table holding a kept table and a keeping paragraph moves
	 * whole (R6).
	 *
	 * <p>docx4j writes {@code w:keepNext} as {@code keep-with-next="always"} on each
	 * paragraph's {@code fo:block}.  Between two rows FOP honours that: the cell's last
	 * block's keep becomes the cell's ({@code TableCellLayoutManager}), the cells' the
	 * step's ({@code TableStepper}), so the penalty between the rows is infinite.  At the
	 * <b>last row</b> it is lost: {@code TableContentLayoutManager} removes the break
	 * element after the last row group ("the breaking after the table will be handled by
	 * TableLM"), and what the table's layout manager passes on is the last
	 * <i>row's</i> own {@code keep-with-next} ({@code RowGroupLayoutManager}), never its
	 * cells'.  So a table whose last row keeps with next could break from the paragraph
	 * after it, and a nested table from the rest of its cell.</p>
	 *
	 * <p>Measured on a 311-page report of 1,183 tables and 10,188 {@code w:keepNext}, most
	 * of them six-row tables every paragraph of which keeps with the next and the last row
	 * with an empty paragraph after the table.  docx4j gave the document 277 pages: our
	 * pages began with the invisible paragraph Word kept on the page before 22 times to
	 * Word's twice, and with the kept unit that much shorter it fit the remaining space
	 * where Word's did not - Word pushed a unit to the next page, leaving its space unused,
	 * some 170 times to our 59.  Word's kept units are the table <i>and</i> the paragraph
	 * after it.</p>
	 *
	 * <p>The rule marks the {@code fo:table-row} itself {@code keep-with-next="always"}
	 * where the first block of the row's first cell carries it - a paragraph by its block,
	 * a numbered paragraph by its list body's block, a nested table by its own first row
	 * (which this rule marked when that table was written, so the keep climbs through the
	 * cell of the table it sits in).  Header rows are not marked: a repeated header keeps
	 * with the row after it by construction.  The keep on the row is what FOP propagates
	 * out of the table.</p>
	 *
	 * @since 17.1.1
	 */
  	@Override
  	protected void applyTableRowCustomAttributes(AbstractWmlConversionContext context, AbstractTableWriterModel table,
  			TransformState transformState, Element row, int rowIndex, boolean isHeader) {

  		if (isHeader) return;
  		if (!org.docx4j.Docx4jProperties.getProperty(ROW_KEEP_WITH_NEXT, true)) return;
  		if (rowIndex < 0 || rowIndex >= table.getRows().size()) return;
  		if (rowKeepsWithNext(table.getRows().get(rowIndex))) {
  			row.setAttribute("keep-with-next", "always");
  		}
  	}

  	/** The first paragraph of the row's first cell keeps with next (Word's rule). */
  	static boolean rowKeepsWithNext(org.docx4j.model.table.TableModelRow rowModel) {
  		for (TableModelCell cell : rowModel.getRowContents()) {
  			if (cell.isDummy()) continue;
  			Node content = ((AbstractTableWriterModelCell) cell).getContent();
  			return content != null && firstBlockKeepsWithNext(content.getChildNodes());
  		}
  		return false;
  	}

  	/** Whether the first block-level element among these children keeps with next. */
  	private static boolean firstBlockKeepsWithNext(NodeList children) {
  		for (int i = 0; i < children.getLength(); i++) {
  			Node n = children.item(i);
  			if (n.getNodeType() != Node.ELEMENT_NODE) continue;
  			Element e = (Element) n;
  			String name = localName(e);
  			if ("block".equals(name)) {
  				return "always".equals(e.getAttribute("keep-with-next"));
  			} else if ("list-block".equals(name)) {
  				// the paragraph's keep is on the list-item-body's block
  				Element body = firstDescendant(e, "list-item-body");
  				return body != null && firstBlockKeepsWithNext(body.getChildNodes());
  			} else if ("table".equals(name)) {
  				// a nested table: its first row, marked by this rule when it was written
  				Element first = firstRow(e);
  				return first != null && "always".equals(first.getAttribute("keep-with-next"));
  			} else if ("block-container".equals(name) || "wrapper".equals(name)) {
  				return firstBlockKeepsWithNext(e.getChildNodes());
  			}
  			// not block-level content (a marker, a float): look on
  		}
  		return false;
  	}

  	private static Element firstDescendant(Element e, String localName) {
  		NodeList all = e.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", localName);
  		return all.getLength() == 0 ? null : (Element) all.item(0);
  	}

  	/** The first fo:table-row of this table's first body (header rows are never marked). */
  	private static Element firstRow(Element tbl) {
  		NodeList kids = tbl.getChildNodes();
  		for (int i = 0; i < kids.getLength(); i++) {
  			Node n = kids.item(i);
  			if (n.getNodeType() != Node.ELEMENT_NODE || !"table-body".equals(localName((Element) n))) continue;
  			NodeList rows = n.getChildNodes();
  			for (int k = 0; k < rows.getLength(); k++) {
  				Node r = rows.item(k);
  				if (r.getNodeType() == Node.ELEMENT_NODE && "table-row".equals(localName((Element) r))) return (Element) r;
  			}
  		}
  		return null;
  	}

  	private static String localName(Element e) {
  		String name = e.getLocalName();
  		return name == null ? e.getNodeName().replaceFirst("^fo:", "") : name;
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

	/**
	 * {@code w:textDirection}: a cell whose text Word turns on its side.  The container
	 * carrying {@code reference-orientation} is a <b>reference area</b>, and a reference
	 * area turned 90&#xb0; has to be given both of its dimensions: without them FOP gives
	 * the rotated area an inline-progression-dimension of 0 and the viewport a
	 * block-progression-dimension of 0 (measured on the area tree: {@code <block ipd="0"
	 * bpd="28800" is-reference-area="true">} inside {@code <block ipd="28800" bpd="0"
	 * is-viewport-area="true">}), so the text is laid out on a line of no measure, takes
	 * no width in the cell and is painted past the page edge - a corpus certificate whose
	 * table has {@code w:textDirection btLr} in its stub column had Word's first line at
	 * {@code y=53.5 x=99.9..494.8} and ours at {@code y=76.1 x=323.5..672.7} on a 595.3pt
	 * page.
	 *
	 * <p>The two properties are stated in the container's <em>own</em> (rotated) frame,
	 * and FOP swaps them onto the viewport: {@code inline-progression-dimension} becomes
	 * the viewport's height (how far the rotated line may run down the page) and
	 * {@code block-progression-dimension} its width in the cell.  So the
	 * block-progression-dimension is the cell's content width - its grid width less the
	 * cell margins - and the inline-progression-dimension is how tall the cell is: the
	 * row's {@code w:trHeight} where it states one, and otherwise the cell's minimum
	 * content width, the height the rotated text can always be wrapped into.</p>
	 *
	 * @since 17.1.0
	 */
	@Override
	protected Element interposeBlockContainer(AbstractWmlConversionContext context, Document doc,
			Element cellNode, AbstractTableWriterModel table, AbstractTableWriterModelCell cell,
			int cellWidthTwips) {

		TcPr tcPr = cell.getTcPr();
		Element ret = interposeBlockContainer(doc, cellNode, tcPr);
		if (ret == cellNode || !WordLayoutFixups.isEnabled()) return ret;

		double width = cellWidthTwips / 20d
				- WordLayoutFixups.lengthPt(cellNode.getAttribute("padding-left"))
				- WordLayoutFixups.lengthPt(cellNode.getAttribute("padding-right"));
		if (width > 0) {
			ret.setAttribute("block-progression-dimension", WordLayoutFixups.pt(width));
		}

		// how far down the page the rotated line may run
		Element row = (cellNode.getParentNode() instanceof Element) ? (Element)cellNode.getParentNode() : null;
		double height = row == null ? 0 : WordLayoutFixups.lengthPt(row.getAttribute("height"));
		if (height <= 0) {
			// no w:trHeight: the row is as tall as the rotated text needs, and how much
			// that is depends on how many rotated lines the cell's width takes - which is
			// not known here.  The cell's *minimum* content width (its longest unbreakable
			// unit) is the row height the text can always be wrapped into, and measures
			// better across the corpus than its whole content on one line, which inflates
			// every row whose text Word wraps.  Capped at the page's text height, which no
			// row can exceed.
			double[] measured = measureCellContent(context, cell);
			height = measured == null ? 0 : measured[0];
			org.docx4j.model.structure.PageDimensions dims = pageDimensions(context);
			if (dims != null && dims.getWritableHeightTwips() > 0) {
				height = Math.min(height, dims.getWritableHeightTwips() / 20d);
			}
		}
		if (height > 0) {
			ret.setAttribute("inline-progression-dimension", WordLayoutFixups.pt(height));
		}
		return ret;
	}
  	
	
}
