/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
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
package org.docx4j.platformtests;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;


import org.docx4j.XmlUtils;
import org.docx4j.finders.ClassFinder;
import org.docx4j.model.revisions.TrackedChanges;
import org.docx4j.model.table.TblFactory;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.docx4j.TraversalUtil;
import org.docx4j.wml.*;

/**
 * docx4j's adapter for open-agreements/docx-platform-tests, adapter protocol version 1: one operation on one
 * document, as the suite's runner calls it ({@code --protocol-version 1 --operation op.json --input in.docx
 * --output out.docx}). Exit 0 with the output written; 1 on an error (diagnostics on stderr); 2 for an operation
 * docx4j has no API for (a one-line reason on stdout, no output); 3 for another protocol version.
 *
 * <p>What is docx4j's API and what is glue is in README.md. The rule: an operation docx4j provides is called; a
 * target is located (the first paragraph holding a text, the n-th table) and the typed content tree edited in
 * place, which is how docx4j edits a document; anything needing an algorithm docx4j does not provide (matching
 * text across runs, splitting a run at a text offset, merging cells, removing comments) is declined.
 */
public final class Adapter {

	static final String PROTOCOL_VERSION = "1";
	static final ObjectFactory W = new ObjectFactory();

	/** An operation docx4j has no API for: exit 2, the reason on stdout. */
	static final class Unsupported extends Exception {
		Unsupported(String reason) { super(reason); }
	}

	public static void main(String[] args) {
		System.exit(run(args));
	}

	static int run(String[] args) {
		if (has(args, "--print-library-version")) {
			System.out.println("docx4j-core " + libraryVersion());
			return 0;
		}
		String protocol = arg(args, "--protocol-version");
		if (!PROTOCOL_VERSION.equals(protocol)) {
			System.out.println("the docx4j adapter speaks protocol v" + PROTOCOL_VERSION + ", got " + protocol);
			return 3;
		}
		String operationPath = arg(args, "--operation"), input = arg(args, "--input"), output = arg(args, "--output");
		if (operationPath == null || input == null || output == null) {
			System.err.println("missing required --operation/--input/--output argument");
			return 1;
		}
		File out = new File(output);
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> op = (Map<String, Object>) Json.parse(
					new String(Files.readAllBytes(new File(operationPath).toPath()), StandardCharsets.UTF_8));
			WordprocessingMLPackage pkg = apply(op, new File(input));
			pkg.save(out);
			return 0;
		} catch (Unsupported u) {
			System.out.println(u.getMessage());
			out.delete();
			return 2;
		} catch (Throwable t) {
			t.printStackTrace();
			out.delete();
			return 1;
		}
	}

	static WordprocessingMLPackage apply(Map<String, Object> op, File input) throws Exception {
		String name = (String) op.get("operationName");
		if (name == null) throw new IllegalArgumentException("operation.json has no operationName");
		switch (name) {
		// ---- composing a document (replacesInput) ----
		case "composeDocumentWithParagraphs": {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
			for (Object o : list(op, "paragraphDescriptorList")) {
				Map<String, Object> d = map(o);
				pkg.getMainDocumentPart().getContent().add(paragraph(pkg, str(d, "paragraphText"), map(d.get("runFormatting"))));
			}
			return pkg;
		}
		case "composeDocumentWithTable": {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
			List<Object> rows = list(op, "tableCellTextRows");
			int cols = list(rows.get(0)).size();
			int width = pkg.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips() / cols;
			Tbl tbl = TblFactory.createTable(rows.size(), cols, width);
			for (int r = 0; r < rows.size(); r++) {
				List<Object> cells = list(rows.get(r));
				for (int c = 0; c < cols; c++) setCellText(pkg, cell(tbl, r, c), c < cells.size() ? (String) cells.get(c) : "");
			}
			pkg.getMainDocumentPart().getContent().add(tbl);
			return pkg;
		}
		case "composeDocumentWithHyperlink": {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
			MainDocumentPart mdp = pkg.getMainDocumentPart();
			Relationship rel = new org.docx4j.relationships.ObjectFactory().createRelationship();
			rel.setType(Namespaces.HYPERLINK);
			rel.setTarget(str(op, "hyperlinkTargetUrl"));
			rel.setTargetMode("External");
			mdp.getRelationshipsPart().addRelationship(rel);
			P.Hyperlink link = W.createPHyperlink();
			link.setId(rel.getId());
			link.getContent().add(run(str(op, "hyperlinkDisplayText"), null));
			P p = W.createP();
			p.getContent().add(W.createPHyperlink(link));
			mdp.getContent().add(p);
			return pkg;
		}
		case "composeDocumentWithHeaderText": {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
			pkg.getMainDocumentPart().getContent().add(paragraph(pkg, str(op, "bodyText"), null));
			setHeaderOrFooter(pkg, true, str(op, "headerText"));
			return pkg;
		}
		case "composeDocumentWithNumberedList": {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
			MainDocumentPart mdp = pkg.getMainDocumentPart();
			NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
			mdp.addTargetPart(ndp);
			ndp.unmarshalDefaultNumbering();
			String format = str(op, "numberFormat");
			String numId = numIdWithFormat(ndp.getJaxbElement(), format);
			if (numId == null) throw new Unsupported("docx4j's default numbering definitions have no list of format '" + format + "'");
			List<Object> texts = list(op, "listItemTexts");
			List<Object> levels = op.get("itemLevels") == null ? null : list(op, "itemLevels");
			for (int i = 0; i < texts.size(); i++) {
				P p = paragraph(pkg, (String) texts.get(i), null);
				setNumbering(p, numId, levels == null ? 0 : ((Number) levels.get(i)).intValue());
				mdp.getContent().add(p);
			}
			return pkg;
		}
		case "composeDocumentWithCompatibilityMode": {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
			pkg.getMainDocumentPart().getDocumentSettingsPart(true).setCompatibilityMode(((Number) op.get("compatibilityMode")).intValue());
			pkg.getMainDocumentPart().getContent().add(paragraph(pkg, str(op, "bodyText"), null));
			return pkg;
		}
		default:
			break;
		}

		// ---- editing the input (preservesInput) ----
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(input);
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		switch (name) {
		case "acceptAllTrackedChanges":
			TrackedChanges.acceptAll(pkg);
			return pkg;
		case "rejectAllTrackedChanges":
			TrackedChanges.rejectAll(pkg);
			return pkg;
		case "replaceFirstTextOccurrence": {
			String find = str(op, "findText");
			P p = paragraphContaining(mdp, find);
			for (Text t : texts(p)) {
				int i = t.getValue().indexOf(find);
				if (i >= 0) {
					setText(t, t.getValue().substring(0, i) + str(op, "replaceText") + t.getValue().substring(i + find.length()));
					return pkg;
				}
			}
			throw new Unsupported("the match spans w:t elements; docx4j has no find-and-replace across runs (the adapter replaces within one w:t)");
		}
		case "appendParagraphWithText":
			mdp.getContent().add(paragraph(pkg, str(op, "paragraphText"), map(op.get("runFormatting"))));
			return pkg;
		case "insertParagraphAfterAnchorText": {
			P anchor = paragraphContaining(mdp, str(op, "anchorText"));
			List<Object> siblings = siblings(mdp, anchor);
			siblings.add(indexOf(siblings, anchor) + 1, paragraph(pkg, str(op, "paragraphText"), null));
			return pkg;
		}
		case "appendTableRow": {
			Tbl tbl = table(mdp, ((Number) op.get("tableIndex")).intValue());
			List<Object> texts = list(op, "cellTexts");
			Tr tr = W.createTr();
			List<TblGridCol> grid = tbl.getTblGrid() == null ? new ArrayList<TblGridCol>() : tbl.getTblGrid().getGridCol();
			int cols = Math.max(grid.size(), texts.size());
			for (int c = 0; c < cols; c++) {
				Tc tc = W.createTc();
				if (c < grid.size() && grid.get(c).getW() != null) {
					TcPr tcPr = W.createTcPr();
					TblWidth w = W.createTblWidth();
					w.setW(grid.get(c).getW());
					w.setType("dxa");
					tcPr.setTcW(w);
					tc.setTcPr(tcPr);
				}
				tc.getContent().add(paragraph(pkg, c < texts.size() ? (String) texts.get(c) : "", null));
				tr.getContent().add(tc);
			}
			tbl.getContent().add(tr);
			return pkg;
		}
		case "deleteTableRowAtIndex": {
			Tbl tbl = table(mdp, ((Number) op.get("tableIndex")).intValue());
			tbl.getContent().remove(indexOf(tbl.getContent(), rows(tbl).get(((Number) op.get("rowIndex")).intValue())));
			return pkg;
		}
		case "setTableCellText": {
			Tbl tbl = table(mdp, ((Number) op.get("tableIndex")).intValue());
			setCellText(pkg, cell(tbl, ((Number) op.get("rowIndex")).intValue(), ((Number) op.get("columnIndex")).intValue()),
					str(op, "replacementCellText"));
			return pkg;
		}
		case "mergeTableCellsInRow":
			throw new Unsupported("docx4j has no cell-merge API (a merge moves the merged cells' content and widths; an adapter-side algorithm)");
		case "addCommentOnFirstTextOccurrence": {
			String anchorText = str(op, "anchorText");
			List<R> runs = wholeRuns(paragraphContaining(mdp, anchorText), anchorText);
			addComment(pkg, runs, str(op, "commentText"), str(op, "commentAuthorName"), str(op, "commentAuthorInitials"));
			return pkg;
		}
		case "removeAllComments":
			throw new Unsupported("docx4j has no API to remove comments with their anchors and parts");
		case "applyParagraphStyleToAnchor": {
			P p = paragraphContaining(mdp, str(op, "anchorText"));
			if (p.getPPr() == null) p.setPPr(W.createPPr());
			PPrBase.PStyle style = W.createPPrBasePStyle();
			style.setVal(str(op, "paragraphStyleId"));
			p.getPPr().setPStyle(style);
			return pkg;
		}
		case "applyNumberingToAnchorParagraph":
			setNumbering(paragraphContaining(mdp, str(op, "anchorText")), String.valueOf(op.get("numberingInstanceId")),
					((Number) op.get("indentationLevel")).intValue());
			return pkg;
		case "formatFirstTextOccurrence": {
			String find = str(op, "findText");
			for (R r : wholeRuns(paragraphContaining(mdp, find), find)) format(r, map(op.get("runFormatting")));
			return pkg;
		}
		case "setDefaultFooterText":
			setHeaderOrFooter(pkg, false, str(op, "footerText"));
			return pkg;
		default:
			throw new Unsupported("the docx4j adapter does not implement operation '" + name + "'");
		}
	}

	// ---- content ----

	static P paragraph(WordprocessingMLPackage pkg, String text, Map<String, Object> formatting) {
		P p = W.createP();
		if (text != null && !text.isEmpty()) p.getContent().add(run(text, formatting));
		return p;
	}

	static R run(String text, Map<String, Object> formatting) {
		R r = W.createR();
		Text t = W.createText();
		setText(t, text);
		r.getContent().add(W.createRT(t));
		format(r, formatting);
		return r;
	}

	static void setText(Text t, String value) {
		t.setValue(value);
		if (!value.equals(value.trim())) t.setSpace("preserve");
	}

	static void format(R r, Map<String, Object> formatting) {
		if (formatting == null || formatting.isEmpty()) return;
		RPr rPr = r.getRPr() == null ? W.createRPr() : r.getRPr();
		if (formatting.containsKey("bold")) {
			BooleanDefaultTrue b = W.createBooleanDefaultTrue();
			b.setVal(Boolean.TRUE.equals(formatting.get("bold")));
			rPr.setB(b);
		}
		if (formatting.containsKey("italic")) {
			BooleanDefaultTrue i = W.createBooleanDefaultTrue();
			i.setVal(Boolean.TRUE.equals(formatting.get("italic")));
			rPr.setI(i);
		}
		if (formatting.containsKey("fontSizeHalfPoints")) {
			HpsMeasure sz = W.createHpsMeasure();
			sz.setVal(BigInteger.valueOf(((Number) formatting.get("fontSizeHalfPoints")).longValue()));
			rPr.setSz(sz);
		}
		r.setRPr(rPr);
	}

	static void setNumbering(P p, String numId, int ilvl) {
		if (p.getPPr() == null) p.setPPr(W.createPPr());
		PPrBase.NumPr numPr = W.createPPrBaseNumPr();
		PPrBase.NumPr.Ilvl level = W.createPPrBaseNumPrIlvl();
		level.setVal(BigInteger.valueOf(ilvl));
		numPr.setIlvl(level);
		PPrBase.NumPr.NumId id = W.createPPrBaseNumPrNumId();
		id.setVal(new BigInteger(numId));
		numPr.setNumId(id);
		p.getPPr().setNumPr(numPr);
	}

	static String numIdWithFormat(Numbering numbering, String format) {
		for (Numbering.Num num : numbering.getNum()) {
			if (num.getAbstractNumId() == null) continue;
			for (Numbering.AbstractNum a : numbering.getAbstractNum()) {
				if (!a.getAbstractNumId().equals(num.getAbstractNumId().getVal())) continue;
				for (Lvl lvl : a.getLvl()) {
					if (lvl.getIlvl() != null && lvl.getIlvl().intValue() == 0 && lvl.getNumFmt() != null && format.equals(lvl.getNumFmt().getVal().value())) {
						return num.getNumId().toString();
					}
				}
			}
		}
		return null;
	}

	/** The paragraphs of the main document in document order (table cells and content controls included). */
	static List<P> paragraphs(MainDocumentPart mdp) {
		ClassFinder finder = new ClassFinder(P.class);
		new TraversalUtil(mdp.getContent(), finder);
		List<P> ps = new ArrayList<P>();
		for (Object o : finder.results) ps.add((P) o);
		return ps;
	}

	/** The w:t elements of a paragraph (not w:delText), in document order. */
	static List<Text> texts(P p) {
		ClassFinder finder = new ClassFinder(Text.class);
		new TraversalUtil(p.getContent(), finder);
		List<Text> ts = new ArrayList<Text>();
		for (Object o : finder.results) ts.add((Text) o);
		return ts;
	}

	static String text(P p) {
		StringBuilder sb = new StringBuilder();
		for (Text t : texts(p)) sb.append(t.getValue());
		return sb.toString();
	}

	static P paragraphContaining(MainDocumentPart mdp, String text) {
		for (P p : paragraphs(mdp)) if (text(p).contains(text)) return p;
		throw new IllegalArgumentException("no paragraph contains '" + text + "'");
	}

	/**
	 * The runs, direct children of the paragraph and contiguous, whose text is exactly the given text. Where the text
	 * lies inside the one w:t of a run, that run is split in three (its properties copied), which is editing the content
	 * tree; text spanning runs is unsupported, since matching across runs is an algorithm docx4j does not provide.
	 */
	static List<R> wholeRuns(P p, String text) throws Unsupported {
		List<Object> content = p.getContent();
		for (int start = 0; start < content.size(); start++) {
			StringBuilder sb = new StringBuilder();
			List<R> runs = new ArrayList<R>();
			for (int i = start; i < content.size(); i++) {
				Object o = XmlUtils.unwrap(content.get(i));
				if (!(o instanceof R)) break;
				runs.add((R) o);
				for (Object c : ((R) o).getContent()) {
					Object u = XmlUtils.unwrap(c);
					if (u instanceof Text) sb.append(((Text) u).getValue());
				}
				if (sb.toString().equals(text)) return runs;
				if (!text.startsWith(sb.toString())) break;
			}
		}
		for (int i = 0; i < content.size(); i++) {
			Object o = XmlUtils.unwrap(content.get(i));
			if (!(o instanceof R) || ((R) o).getContent().size() != 1) continue;
			Object only = XmlUtils.unwrap(((R) o).getContent().get(0));
			if (!(only instanceof Text)) continue;
			String value = ((Text) only).getValue();
			int at = value.indexOf(text);
			if (at < 0) continue;
			R run = (R) o;
			List<R> parts = new ArrayList<R>();
			for (String piece : new String[] { value.substring(0, at), text, value.substring(at + text.length()) }) {
				if (piece.isEmpty()) { parts.add(null); continue; }
				R copy = run(piece, null);
				if (run.getRPr() != null) copy.setRPr(XmlUtils.deepCopy(run.getRPr()));
				parts.add(copy);
			}
			content.remove(i);
			int k = i;
			for (R part : parts) if (part != null) { part.setParent(p); content.add(k++, part); }
			List<R> match = new ArrayList<R>();
			match.add(parts.get(1));
			return match;
		}
		throw new Unsupported("the text spans runs; docx4j has no API to match text across runs");
	}

	static void addComment(WordprocessingMLPackage pkg, List<R> runs, String text, String author, String initials) throws Exception {
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		CommentsPart part = mdp.getCommentsPart();
		if (part == null) {
			part = new CommentsPart();
			mdp.addTargetPart(part);
			part.setJaxbElement(W.createComments());
		}
		BigInteger id = BigInteger.ZERO;
		for (Comments.Comment c : part.getJaxbElement().getComment()) {
			if (c.getId() != null && c.getId().compareTo(id) >= 0) id = c.getId().add(BigInteger.ONE);
		}
		Comments.Comment comment = W.createCommentsComment();
		comment.setId(id);
		comment.setAuthor(author);
		comment.setInitials(initials);
		comment.getContent().add(paragraph(pkg, text, null));
		part.getJaxbElement().getComment().add(comment);

		P p = (P) runs.get(0).getParent();
		List<Object> content = p.getContent();
		CommentRangeStart start = W.createCommentRangeStart();
		start.setId(id);
		content.add(indexOf(content, runs.get(0)), start);
		CommentRangeEnd end = W.createCommentRangeEnd();
		end.setId(id);
		int after = indexOf(content, runs.get(runs.size() - 1)) + 1;
		content.add(after, end);
		R ref = W.createR();
		R.CommentReference reference = W.createRCommentReference();
		reference.setId(id);
		ref.getContent().add(W.createRCommentReference(reference));
		content.add(after + 1, ref);
	}

	/** Adds the default header or footer of the document's last section, or replaces its text. */
	static void setHeaderOrFooter(WordprocessingMLPackage pkg, boolean header, String text) throws Exception {
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		Body body = mdp.getJaxbElement().getBody();
		if (body.getSectPr() == null) body.setSectPr(W.createSectPr());
		SectPr sectPr = body.getSectPr();
		for (CTRel ref : sectPr.getEGHdrFtrReferences()) {
			if (header != (ref instanceof HeaderReference)) continue;
			HdrFtrRef type = ref instanceof HeaderReference ? ((HeaderReference) ref).getType() : ((FooterReference) ref).getType();
			if (type != HdrFtrRef.DEFAULT) continue;
			Object part = mdp.getRelationshipsPart().getPart(ref.getId());
			List<Object> content = header ? ((HeaderPart) part).getContent() : ((FooterPart) part).getContent();
			content.clear();
			content.add(paragraph(pkg, text, null));
			return;
		}
		if (header) {
			HeaderPart part = new HeaderPart();
			Relationship rel = mdp.addTargetPart(part);
			Hdr hdr = W.createHdr();
			hdr.getContent().add(paragraph(pkg, text, null));
			part.setJaxbElement(hdr);
			HeaderReference ref = W.createHeaderReference();
			ref.setId(rel.getId());
			ref.setType(HdrFtrRef.DEFAULT);
			sectPr.getEGHdrFtrReferences().add(ref);
		} else {
			FooterPart part = new FooterPart();
			Relationship rel = mdp.addTargetPart(part);
			Ftr ftr = W.createFtr();
			ftr.getContent().add(paragraph(pkg, text, null));
			part.setJaxbElement(ftr);
			FooterReference ref = W.createFooterReference();
			ref.setId(rel.getId());
			ref.setType(HdrFtrRef.DEFAULT);
			sectPr.getEGHdrFtrReferences().add(ref);
		}
	}

	// ---- tables ----

	/** The n-th table directly in the body. */
	static Tbl table(MainDocumentPart mdp, int index) {
		int n = 0;
		for (Object o : mdp.getContent()) {
			Object u = XmlUtils.unwrap(o);
			if (u instanceof Tbl && n++ == index) return (Tbl) u;
		}
		throw new IllegalArgumentException("no table " + index + " in the body");
	}

	static List<Tr> rows(Tbl tbl) {
		List<Tr> rows = new ArrayList<Tr>();
		for (Object o : tbl.getContent()) {
			Object u = XmlUtils.unwrap(o);
			if (u instanceof Tr) rows.add((Tr) u);
		}
		return rows;
	}

	static Tc cell(Tbl tbl, int row, int column) {
		int n = 0;
		for (Object o : rows(tbl).get(row).getContent()) {
			Object u = XmlUtils.unwrap(o);
			if (u instanceof Tc && n++ == column) return (Tc) u;
		}
		throw new IllegalArgumentException("no cell " + column + " in row " + row);
	}

	/** A cell's content replaced by one paragraph of the text, keeping the cell's and its first paragraph's properties. */
	static void setCellText(WordprocessingMLPackage pkg, Tc tc, String text) {
		PPr pPr = null;
		for (Object o : tc.getContent()) {
			Object u = XmlUtils.unwrap(o);
			if (u instanceof P) { pPr = ((P) u).getPPr(); break; }
		}
		tc.getContent().clear();
		P p = paragraph(pkg, text, null);
		if (pPr != null) p.setPPr(pPr);
		tc.getContent().add(p);
	}

	// ---- lists ----

	static List<Object> siblings(MainDocumentPart mdp, P p) {
		Object parent = p.getParent();
		if (parent instanceof ContentAccessor) return ((ContentAccessor) parent).getContent();
		return mdp.getContent();
	}

	static int indexOf(List<Object> list, Object target) {
		for (int i = 0; i < list.size(); i++) if (XmlUtils.unwrap(list.get(i)) == target) return i;
		throw new IllegalStateException("not found in its parent's content");
	}

	// ---- arguments and the operation's JSON ----

	static boolean has(String[] args, String name) {
		for (String a : args) if (a.equals(name)) return true;
		return false;
	}

	static String arg(String[] args, String name) {
		for (int i = 0; i < args.length - 1; i++) if (args[i].equals(name)) return args[i + 1];
		return null;
	}

	static String str(Map<String, Object> m, String key) {
		Object v = m.get(key);
		if (!(v instanceof String)) throw new IllegalArgumentException("operation parameter '" + key + "' is not a string");
		return (String) v;
	}

	@SuppressWarnings("unchecked")
	static Map<String, Object> map(Object o) {
		return (Map<String, Object>) o;
	}

	@SuppressWarnings("unchecked")
	static List<Object> list(Object o) {
		return (List<Object>) o;
	}

	static List<Object> list(Map<String, Object> m, String key) {
		Object v = m.get(key);
		if (!(v instanceof List)) throw new IllegalArgumentException("operation parameter '" + key + "' is not a list");
		return list(v);
	}

	static String libraryVersion() {
		try (InputStream in = WordprocessingMLPackage.class.getClassLoader()
				.getResourceAsStream("META-INF/maven/org.docx4j/docx4j-core/pom.properties")) {
			if (in == null) return "unknown";
			Properties p = new Properties();
			p.load(in);
			return p.getProperty("version", "unknown");
		} catch (Exception e) {
			return "unknown";
		}
	}
}
