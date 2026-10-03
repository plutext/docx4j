/*
   (c) Plutext Pty Ltd, 2014
   
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
package org.docx4j.convert.out.common.preprocess;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.docx4j.TraversalUtil;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.McMode;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.table.CellContext;
import org.docx4j.model.table.TableContext;
import org.docx4j.model.table.TableContextTracker;
import org.docx4j.openpackaging.exceptions.CyclicStylesException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.P;
import org.docx4j.wml.PPrBase.PStyle;
import org.docx4j.wml.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gives each paragraph in a table a paragraph style of its own which carries the table
 * style's formatting, for the outputs which name styles: HTML, whose CSS classes are style
 * ids, and the FO XSLT pathway, which resolves a paragraph by its w:pStyle alone.
 *
 * <p>A table style formats the paragraphs of its table - its own w:pPr/w:rPr, then the
 * conditional formats ({@code w:tblStylePr}) each paragraph is under - between the document
 * defaults and the paragraph's style (ECMA-376-1 &#xa7;17.7.2, &#xa7;17.7.6).  Since 17.3.1
 * that is {@link PropertyResolver}'s to work out, given the paragraph's
 * {@link CellContext} (CR-030): this preprocess only names the result.  For each paragraph
 * in a table it asks the resolver for the synthetic style which stands for (the paragraph's
 * style, its table level) - {@link PropertyResolver#styleIdFor}, named as since 17.2.0, e.g.
 * {@code Normal-PlainTable1-firstCol-firstRow-nwCell-BR} - adds that style, built by
 * {@link PropertyResolver#syntheticStyle}, to the styles part, and writes its id onto the
 * paragraph.  Code which needs the paragraph's own style back asks
 * {@link PropertyResolver#sourceStyleOf}.</p>
 *
 * <p>Until 17.3.1 this class composed the synthetic styles itself.  What changed with the
 * move (CR-030 phase 2): a text box anchored in a cell is a story of its own and gets none of
 * the table's formatting (D2); a paragraph naming a missing style is the default style's, where
 * it stopped the walk with a NullPointerException (D9); DEBUG logging no longer aborts it
 * (D3); two (paragraph style, table style) pairs whose joined names coincide get distinct
 * ids (D8); [MS-DOCX]'s overrideTableStyleFontSizeAndJustification exception applies below
 * compatibility mode 15 only, as Word 365 applies it (D6); and the settings part is no longer
 * rewritten.  The paragraphs renamed are the ones 17.2.0 renamed: those in a table which
 * names a table style, or which takes a default table style not named "Normal Table".</p>
 *
 * @since 3.0.2
 */
public class ParagraphStylesInTableFix {

	protected static Logger log = LoggerFactory.getLogger(ParagraphStylesInTableFix.class);

	public static void process(WordprocessingMLPackage wmlPackage) {

		/* w:compat/w:useWord2002TableStyleRules ("Emulate Word 2002 Table Style Rules",
		 * ECMA-376-1 17.15.1) would switch this whole step off: Word 2002 did not put a
		 * table style's w:pPr and w:rPr above docDefaults for the paragraphs of the table,
		 * which is exactly what the synthetic style built below does.  It is deliberately
		 * NOT read: measured over the three corpora, Word 365's own PDFs of the three
		 * mode-11 documents which state it apply the table style's properties anyway, and
		 * skipping this step for them cost 0.951 -> 0.105, 0.948 -> 0.248 and
		 * 0.930 -> 0.842 of Word's lines.  See word-layout-settings.md §4(e).
		 * @since 17.1.0 */

		/* Not when invoked from FOPAreaTreeHelper (LayoutMasterSetBuilder.fixExtents), which
		 * renders a package this has already been run on.  A second run is harmless since
		 * 17.3.1 (a synthetic id stands for its source, so the same ids come back), but it is
		 * a whole-document walk for nothing. */
		for (StackTraceElement frame : new Throwable().getStackTrace()) {
			if (frame.getClassName().contains("FOPAreaTreeHelper")) {
				return;
			}
		}

		PropertyResolver resolver;
		try {
			resolver = wmlPackage.getMainDocumentPart().getPropertyResolver();
		} catch (Docx4JException e) {
			log.error(e.getMessage(), e);
			return;
		}
		Style defaultTableStyle = wmlPackage.getMainDocumentPart().getStyleDefinitionsPart().getDefaultTableStyle();
		StyleRenamer styleRenamer = new StyleRenamer(resolver, defaultTableStyle);

		try {
			// CR-021: ALL - rewrites the exporter's copy, so styles are renamed in every branch
			new TraversalUtil(wmlPackage.getMainDocumentPart().getContents(), styleRenamer, McMode.ALL);

			/* Headers and footers too.  A table in a header is styled by its w:tblStyle
			 * exactly as one in the body is, and letterhead tables are common: measured
			 * on a document whose header table uses TableGridLight
			 * (<w:pPr><w:spacing w:after="0" w:line="240"/></w:pPr>), Word's four
			 * baselines in the row-spanning cell are 104.2 / 116.9 / 129.4 / 142.1 - a
			 * pitch of 12.7pt, the line box alone - where ours were 147.0 / 167.7 /
			 * 188.3 / 208.9, a pitch of 20.7pt, because docDefaults' w:after="200" (8pt)
			 * survived on all 16 header paragraphs.  That is 128pt of drift, and it also
			 * made the header extent 341.3pt.  @since 17.1.0 */
			RelationshipsPart relPart = wmlPackage.getMainDocumentPart().getRelationshipsPart();
			if (relPart!=null) {
				for (Relationship rs : relPart.getRelationships().getRelationship()) {
					List<Object> content = null;
					if (Namespaces.HEADER.equals(rs.getType())) {
						content = ((HeaderPart)relPart.getPart(rs)).getJaxbElement().getContent();
					} else if (Namespaces.FOOTER.equals(rs.getType())) {
						content = ((FooterPart)relPart.getPart(rs)).getJaxbElement().getContent();
					}
					if (content!=null) {
						// CR-021: ALL - rewrites the exporter's copy
						new TraversalUtil(content, styleRenamer, McMode.ALL);
					}
				}
			}
		} catch (Docx4JException e) {
			log.error(e.getMessage(), e);
		} catch (RuntimeException e) {
			// eg a header part which isn't loadable; the body is done, don't lose that
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Writes each table paragraph's synthetic style id onto it (see the class comment).
	 * @since 3.0.2; since 17.3.1 a {@link TableContextTracker}
	 */
	public static class StyleRenamer extends TableContextTracker {

		private final PropertyResolver resolver;
		private final Style defaultTableStyle;
		/** the synthetic styles this walk has added to the styles part */
		private final Set<String> added = new HashSet<String>();

		public StyleRenamer(PropertyResolver resolver, Style defaultTableStyle) {
			super(resolver);
			this.resolver = resolver;
			this.defaultTableStyle = defaultTableStyle;
		}

		@Override
		public List<Object> apply(Object o) {
			if (!(o instanceof P)) return null;
			P p = (P) o;
			CellContext cellContext = cellContext(p);
			TableContext table = tableContext();
			if (cellContext == null || table == null || !renamed(table)) {
				// outside a table, or in a text box (a story of its own): the paragraph is
				// left as written, and the resolver answers for it
				return null;
			}
			String pStyle = (p.getPPr() == null || p.getPPr().getPStyle() == null)
					? null : p.getPPr().getPStyle().getVal();
			String id = resolver.styleIdFor(pStyle, cellContext);
			if (added.add(id)) {
				try {
					resolver.activateStyle(resolver.syntheticStyle(id));
				} catch (CyclicStylesException e) {
					throw new RuntimeException(e);
				}
			}
			if (p.getPPr() == null) {
				p.setPPr(Context.getWmlObjectFactory().createPPr());
			}
			PStyle ps = Context.getWmlObjectFactory().createPPrBasePStyle();
			ps.setVal(id);
			p.getPPr().setPStyle(ps);
			return null;
		}

		/** Whether 17.2.0 renamed this table's paragraphs: it names a table style, or takes a
		 *  default table style which is not named "Normal Table" (Word's built-in, which
		 *  gives text nothing). */
		private boolean renamed(TableContext table) {
			if (table.namesStyle()) return table.getTableStyleId() != null;
			return table.getTableStyleId() != null
					&& defaultTableStyle != null
					&& !(defaultTableStyle.getName() != null
							&& "Normal Table".equals(defaultTableStyle.getName().getVal()));
		}
	}
}
