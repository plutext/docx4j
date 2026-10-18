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
package org.docx4j.model.text;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;

/**
 * docx4j's text model (CR-046; the portfolio's <em>WordprocessingML Text Extraction</em>, WD): a
 * text is {@code serialize(scope(view(resolve(package))))} (WD REQ-201). This class makes the block
 * model ({@link TextResult}); {@link PlainText} writes plain text from it.
 *
 * <pre>
 * TextResult result = Texts.of(pkg, new TextOptions().view(TextView.MARKUP));
 * String text = PlainText.of(result);
 * for (TextWarning w : result.warnings()) ...
 * </pre>
 *
 * <ul>
 * <li><b>resolve</b>: each {@code mc:AlternateContent} is read from the branch the ISO rule selects
 * over the published understood set, by namespace URI ({@link org.docx4j.jaxb.McPurpose#TEXT}).</li>
 * <li><b>view</b>: {@code accepted} (the default), {@code original} or {@code markup}; the first two
 * are the text of the document Word's Accept All or Reject All leaves (CR-043), worked out without
 * resolving anything ({@link org.docx4j.model.revisions.ResolvedView}).</li>
 * <li><b>scope</b>: the main story, each text box once after its anchoring paragraph (after the
 * table for one in a cell), then the footnotes and endnotes in reference order; headers, footers and
 * comments where included, and the glossary, last, under {@code include: glossary}; hidden text
 * excluded; placeholders included; a bound control's value from its custom XML part; charts (their
 * title and table), SmartArt and legacy WordArt after their paragraph; {@code altChunk}s expanded
 * where they are; embedded packages read under {@code embedded: true}; images' descriptions by an
 * {@link ImageDescriber}; math as LaTeX; the document properties in the metadata.</li>
 * </ul>
 *
 * <p><b>Cost.</b> Each call reads the package's tracked changes once, in the {@code accepted} and
 * {@code original} views, to know what Accept All or Reject All would remove or join, whether it
 * reads the whole package or one paragraph of it. Read a document once and use the blocks, rather
 * than calling {@link #of(WordprocessingMLPackage, Object, TextOptions)} per paragraph. Under
 * {@code fields: update} a package is read twice, the first time for its bookmarks' text, since a REF
 * may come before its bookmark. In {@code markup}, an equation with tracked changes has the package's
 * tracked changes read twice more, once for each of its two forms (once a call, whatever the number of
 * such equations).</p>
 *
 * <p>The package is never changed (REQ-204): what a chunk or an embedded package holds is read into
 * blocks apart from it, and {@code fields: update} computes in the walk. Two option values are not
 * implemented and are refused ({@link TextOptions}).</p>
 *
 * @since 17.3.4 (CR-046 phases 1 and 2)
 */
public final class Texts {

	private Texts() {}

	/** The text of a package with the default options. */
	public static TextResult of(WordprocessingMLPackage pkg) {
		return of(pkg, new TextOptions());
	}

	/**
	 * The text of a package.
	 *
	 * @throws UnsupportedOperationException for an option value not implemented yet (REQ-1001)
	 */
	public static TextResult of(WordprocessingMLPackage pkg, TextOptions options) {
		if (pkg == null) throw new IllegalArgumentException("No package");
		TextOptions o = options == null ? new TextOptions() : options;
		o.checkImplemented();
		return new Extractor(pkg, o, Extractor.Mode.VIEW).extract();
	}

	/**
	 * The text of one story part: the main document part (its story and text boxes; no notes), a
	 * header or a footer. Under {@code fields: update} there is no first reading for bookmarks, so a
	 * REF is computed only when its bookmark comes before it in the part; otherwise it keeps its cached
	 * result.
	 *
	 * @throws IllegalArgumentException for any other part
	 */
	public static TextResult of(Part part, TextOptions options) {
		TextOptions o = options == null ? new TextOptions() : options;
		o.checkImplemented();
		WordprocessingMLPackage pkg = part.getPackage() instanceof WordprocessingMLPackage ? (WordprocessingMLPackage) part.getPackage() : null;
		Extractor.Mode mode = pkg == null ? Extractor.Mode.PLANLESS : Extractor.Mode.VIEW;
		if (part instanceof MainDocumentPart) {
			return new Extractor(pkg, o, mode).extract(((MainDocumentPart) part).getJaxbElement(), TextBlock.Story.MAIN, "body", part);
		}
		if (part instanceof HeaderPart) {
			return new Extractor(pkg, o, mode).extract(((HeaderPart) part).getJaxbElement(), TextBlock.Story.HEADER, "header:" + rId(pkg, part), part);
		}
		if (part instanceof FooterPart) {
			return new Extractor(pkg, o, mode).extract(((FooterPart) part).getJaxbElement(), TextBlock.Story.FOOTER, "footer:" + rId(pkg, part), part);
		}
		throw new IllegalArgumentException("Not a story part: " + part.getPartName() + "; use Texts.of(package)");
	}

	/**
	 * The text of content in a package (a paragraph, a table, a body, a header's content), as the
	 * main story: the view is the package's, so a join or a move is resolved as in the whole
	 * document. Its addresses start {@code content}. Each call reads the whole package's tracked
	 * changes (the class description's "Cost"). Under {@code fields: update}, a REF is computed only
	 * when its bookmark comes before it in the content.
	 */
	public static TextResult of(WordprocessingMLPackage pkg, Object content, TextOptions options) {
		TextOptions o = options == null ? new TextOptions() : options;
		o.checkImplemented();
		return new Extractor(pkg, o, Extractor.Mode.VIEW).extract(content, TextBlock.Story.MAIN, "content", null);
	}

	/**
	 * The text of content with no package: in the {@code accepted} and {@code original} views only
	 * run-level revisions are resolved (no paragraph is joined, a move is its two halves), and hidden
	 * text is only what the runs themselves hide, since there are no styles.
	 */
	public static TextResult of(Object content, TextOptions options) {
		TextOptions o = options == null ? new TextOptions() : options;
		o.checkImplemented();
		return new Extractor(null, o, Extractor.Mode.PLANLESS).extract(content, TextBlock.Story.MAIN, "content", null);
	}

	/**
	 * The text of a package that Accept All or Reject All has already resolved
	 * ({@link org.docx4j.model.revisions.TrackedChanges}), read with no view logic: the copy route of
	 * WD REQ-406, which the view walk of {@link #of(WordprocessingMLPackage, TextOptions)} is held
	 * equal to. The options' view says which resolution it was ({@code accepted} or {@code original}).
	 */
	public static TextResult ofResolved(WordprocessingMLPackage resolved, TextOptions options) {
		TextOptions o = options == null ? new TextOptions() : options;
		if (o.getView() == TextView.MARKUP) throw new IllegalArgumentException("A resolved package has no markup view");
		o.checkImplemented();
		return new Extractor(resolved, o, Extractor.Mode.RESOLVED).extract();
	}

	/** Plain text of a package (WD section 7.1): {@code PlainText.of(Texts.of(pkg, options))}. */
	public static String plainText(WordprocessingMLPackage pkg, TextOptions options) {
		return PlainText.of(of(pkg, options));
	}

	/**
	 * WD REQ-1201's normalisation, under which texts are compared ({@code mce.branches_differ},
	 * REQ-307; the conformance suite): whitespace, the CriticMarkup marks, Markdown's emphasis
	 * characters and the characters backslash, braces and dollar removed. No Unicode normalisation:
	 * zero-width and tag characters, and the hyphens, are kept.
	 */
	public static String normalize(String value) {
		if (value == null) return "";
		return value.replaceAll("\\{\\+\\+|\\+\\+}|\\{--|--}|\\{>>|<<}", "")
				.replaceAll("[\\s\\p{Z}*_\\\\{}$]", "");
	}

	private static String rId(WordprocessingMLPackage pkg, Part part) {
		if (pkg == null || pkg.getMainDocumentPart() == null) return part.getPartName().getName();
		RelationshipsPart rels = pkg.getMainDocumentPart().getRelationshipsPart(false);
		if (rels != null) {
			for (Relationship rel : rels.getRelationships().getRelationship()) {
				if (rels.getPart(rel) == part) return rel.getId();
			}
		}
		return part.getPartName().getName();
	}
}
