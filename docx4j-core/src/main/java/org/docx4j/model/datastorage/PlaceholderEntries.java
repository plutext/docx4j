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
package org.docx4j.model.datastorage;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.GlossaryDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTDocPart;
import org.docx4j.wml.CTDocPartBehavior;
import org.docx4j.wml.CTDocPartBehaviors;
import org.docx4j.wml.CTDocPartCategory;
import org.docx4j.wml.CTDocPartGallery;
import org.docx4j.wml.CTDocPartName;
import org.docx4j.wml.CTDocPartPr;
import org.docx4j.wml.CTDocPartType;
import org.docx4j.wml.CTDocPartTypes;
import org.docx4j.wml.CTDocParts;
import org.docx4j.wml.CTGuid;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.CTPlaceholder;
import org.docx4j.wml.GlossaryDocument;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.ProofErr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STDocPartBehavior;
import org.docx4j.wml.STDocPartGallery;
import org.docx4j.wml.STDocPartType;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CR-063: a content control's placeholder in Word's form. Word keeps a placeholder's text in the
 * glossary document part, as a building block ({@code w:docPart}) the control names in
 * {@code w:placeholder}; the control shows a copy of the entry's content while {@code w:showingPlcHdr}
 * is on. Every control Word makes names its default entry, {@value #DEFAULT_ENTRY}
 * ({@value #DEFAULT_TEXT}).
 *
 * <p>The binders (bind.xslt and {@link BindingTraverserCommonImpl}) use this where an empty value
 * gives the placeholder:</p>
 * <ul>
 * <li>{@link #referenceFor} decides which entry the control shows (D2 to D4): the entry it names,
 * where the glossary holds it; otherwise, for a text control, the default entry (or the entry of a
 * configured placeholder run), or Word's entry for a date, list, building block gallery or repeating
 * section item (D8, §13), named in the control and written into the glossary, which is made where
 * the document has none (D7: the glossary document part and a styles part holding Placeholder
 * Text). A picture or check box (Word names no entry for them), or a kind whose entry is unmeasured,
 * naming no entry is left alone, with today's placeholder run.</li>
 * <li>{@link #contentOf} gives what the control shows of the entry: a copy of its paragraphs'
 * run-level content, as the glossary holds it (D9).</li>
 * </ul>
 *
 * <p>Word's save drops a {@code w:placeholder} whose entry no glossary holds (check 65), and an entry
 * whose Placeholder Text style the glossary's own styles lack loses it (check 65's 65b), which is why
 * the entry and the styles part are written, not the reference alone. Binding run more than once,
 * or over a glossary already holding the entry, adds nothing.</p>
 *
 * @since 17.3.5
 */
public final class PlaceholderEntries {

	private static final Logger log = LoggerFactory.getLogger(PlaceholderEntries.class);

	/** Word's default placeholder entry, which every control Word makes names. */
	public static final String DEFAULT_ENTRY = "DefaultPlaceholder_-1854013440";
	/** The default entry's text, as Word 365 writes it. */
	public static final String DEFAULT_TEXT = "Click or tap here to enter text.";
	/** The character style of a placeholder's runs; {@code w:showingPlcHdr} is decided on it. */
	public static final String PLACEHOLDER_STYLE = "PlaceholderText";

	/**
	 * Word's default entry for a kind of control (CR-063 §13, D8: Word 365's save of one control of each
	 * kind, 2026-10-25): its name and its text. Its {@code w:guid} is new to each document docx4j writes
	 * it into, as Word's is (§16: seven Word files, seven GUIDs for the text default).
	 * @since 17.3.5
	 */
	public static final class DefaultEntry {
		public final String name, text;
		DefaultEntry(String name, String text) { this.name = name; this.text = text; }
	}
	/** Rich text and plain text controls. */
	public static final DefaultEntry TEXT = new DefaultEntry(DEFAULT_ENTRY, DEFAULT_TEXT);
	/** Date pickers ({@code w:date}). */
	public static final DefaultEntry DATE = new DefaultEntry("DefaultPlaceholder_-1854013437", "Click or tap to enter a date.");
	/** Drop-down lists and combo boxes. */
	public static final DefaultEntry LIST = new DefaultEntry("DefaultPlaceholder_-1854013438", "Choose an item.");
	/** Building block galleries ({@code w:docPartList}). */
	public static final DefaultEntry BUILDING_BLOCK = new DefaultEntry("DefaultPlaceholder_-1854013436", "Choose a building block.");
	/** Repeating section items ({@code w15:repeatingSectionItem}). */
	public static final DefaultEntry REPEATING_SECTION_ITEM = new DefaultEntry("DefaultPlaceholder_-1854013435",
			"Enter any content that you want to repeat, including other content controls. You can also insert this control around table rows in order to repeat parts of a table.");

	private PlaceholderEntries() {}

	/**
	 * The entry the control shows for an empty value, named in its {@code w:placeholder}:
	 * <ul>
	 * <li>the entry it names, where the glossary holds it (D2);</li>
	 * <li>for a control naming none, or naming an entry the glossary lacks: Word's default entry for
	 * its kind ({@link #defaultEntryFor}: text, date, list, building block gallery, repeating section
	 * item; D3, D8), or, for a text control where a placeholder run is configured
	 * ({@code docx4j.model.datastorage.placeholder}), an entry of that run's own, one per distinct run
	 * per document (D4). The entry is written into the glossary, which is made where the document has
	 * none, and the control's reference is set to it (a missing entry's name is replaced, not given
	 * invented text);</li>
	 * <li>null for a kind Word gives no entry (picture, check box, group, citation, equation ...) naming
	 * no entry the glossary holds: it keeps its reference and today's placeholder run.</li>
	 * </ul>
	 * Idempotent: a second call, or a second binding run, changes nothing.
	 *
	 * @param pkg the document; null gives null
	 * @param sdtPr the control's properties, whose {@code w:placeholder} may be set
	 * @return the entry's name, or null where the content is to stay as it is
	 */
	public static String referenceFor(WordprocessingMLPackage pkg, SdtPr sdtPr) {
		if (pkg == null || sdtPr == null || pkg.getMainDocumentPart() == null) return null;
		try {
			ensurePlaceholderStyle(pkg);
			String named = nameOf(sdtPr);
			GlossaryDocumentPart glossary = pkg.getMainDocumentPart().getGlossaryDocumentPart();
			if (named != null && !named.isEmpty() && entryOf(glossary, named) != null) {
				return named;
			}
			DefaultEntry kind = defaultEntryFor(sdtPr);
			if (kind == null) {
				return null;
			}
			R configured = kind == TEXT ? BindingTraverserXSLT.configuredPlaceholderRun() : null;
			String entry = configured == null ? ensureDefaultEntry(pkg, kind) : ensureEntryFor(pkg, configured);
			if (entry == null) return null;
			if (!entry.equals(named)) {
				if (named != null && !named.isEmpty()) {
					log.info("Placeholder entry " + named + " is not in the glossary; the control names " + entry + " instead");
				}
				setReference(sdtPr, entry);
			}
			return entry;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * What a control shows of an entry: a copy of each paragraph of its {@code w:docPartBody}, its
	 * run-level content as the glossary holds it. The control's {@code w:sdtPr/w:rPr} is not applied
	 * (D9, check 68: Word brings an entry back exactly as the glossary holds it, a bold control's
	 * placeholder not bold; the control's rPr governs a value written into it). Paragraph properties,
	 * proofing marks and bookmarks are left in the glossary, whose styles they name.
	 *
	 * @param pkg the document
	 * @param entry the entry's name
	 * @return the paragraphs, each with its content; null where the glossary doesn't hold the entry or
	 *         it has no paragraph
	 */
	public static List<P> contentOf(WordprocessingMLPackage pkg, String entry) {
		if (pkg == null || entry == null || pkg.getMainDocumentPart() == null) return null;
		CTDocPart docPart = entryOf(pkg.getMainDocumentPart().getGlossaryDocumentPart(), entry);
		if (docPart == null || docPart.getDocPartBody() == null) return null;
		List<P> out = new ArrayList<P>();
		for (Object o : docPart.getDocPartBody().getContent()) {
			Object u = XmlUtils.unwrap(o);
			if (!(u instanceof P)) continue;
			P copy = XmlUtils.deepCopy((P) u);
			copy.setPPr(null);
			Iterator<Object> it = copy.getContent().iterator();
			while (it.hasNext()) {
				Object c = XmlUtils.unwrap(it.next());
				if (c instanceof ProofErr || c instanceof CTBookmark || c instanceof CTMarkupRange) {
					it.remove();
				}
			}
			out.add(copy);
		}
		return out.isEmpty() ? null : out;
	}

	/**
	 * The entry the control's {@code w:placeholder} names: null where it has none, "" where the element
	 * names nothing.
	 */
	public static String nameOf(SdtPr sdtPr) {
		CTPlaceholder ph = placeholderOf(sdtPr);
		if (ph == null) return null;
		return ph.getDocPart() == null || ph.getDocPart().getVal() == null ? "" : ph.getDocPart().getVal();
	}

	private static CTPlaceholder placeholderOf(SdtPr sdtPr) {
		for (Object o : sdtPr.getRPrOrAliasOrLock()) {
			Object u = XmlUtils.unwrap(o);
			if (u instanceof CTPlaceholder) return (CTPlaceholder) u;
		}
		return null;
	}

	/** Names {@code entry} in the control's {@code w:placeholder}, replacing the one it has. */
	public static void setReference(SdtPr sdtPr, String entry) {
		ObjectFactory of = Context.getWmlObjectFactory();
		CTPlaceholder ph = placeholderOf(sdtPr);
		if (ph == null) {
			ph = of.createCTPlaceholder();
			JAXBElement<CTPlaceholder> el = of.createSdtPrPlaceholder(ph);
			// where Word writes it: before w:showingPlcHdr and the binding
			List<Object> items = sdtPr.getRPrOrAliasOrLock();
			int at = items.size();
			for (int i = 0; i < items.size(); i++) {
				Object o = items.get(i);
				String name = o instanceof JAXBElement ? ((JAXBElement<?>) o).getName().getLocalPart() : null;
				if ("showingPlcHdr".equals(name) || "dataBinding".equals(name) || "temporary".equals(name)) {
					at = i;
					break;
				}
			}
			items.add(at, el);
			ph.setParent(sdtPr);
		}
		CTPlaceholder.DocPart dp = ph.getDocPart();
		if (dp == null) {
			dp = of.createCTPlaceholderDocPart();
			ph.setDocPart(dp);
		}
		dp.setVal(entry);
	}

	/**
	 * Word's default entry for this control's kind (§13): {@link #TEXT} for an untyped, rich text
	 * ({@code w:richText}) or plain text ({@code w:text}) control, {@link #DATE}, {@link #LIST} (drop-down
	 * list, combo box), {@link #BUILDING_BLOCK} ({@code w:docPartList}), {@link #REPEATING_SECTION_ITEM};
	 * null for the kinds Word names no entry for (picture, check box, the outer repeating section) or
	 * whose entry is unmeasured (equation, citation, group, bibliography, {@code w:docPartObj}, entity
	 * picker).
	 */
	public static DefaultEntry defaultEntryFor(SdtPr sdtPr) {
		for (Object o : sdtPr.getRPrOrAliasOrLock()) {
			if (!(o instanceof JAXBElement)) continue;
			String name = ((JAXBElement<?>) o).getName().getLocalPart();
			switch (name) {
				case "date": return DATE;
				case "comboBox": case "dropDownList": return LIST;
				case "docPartList": return BUILDING_BLOCK;
				case "repeatingSectionItem": return REPEATING_SECTION_ITEM;
				case "equation": case "docPartObj": case "picture": case "citation": case "group": case "bibliography":
				case "checkbox": case "entityPicker": case "repeatingSection":
					return null;
				default:
			}
		}
		return TEXT;
	}

	/** The glossary's entry of this name, or null. */
	public static CTDocPart entryOf(GlossaryDocumentPart glossary, String name) {
		if (glossary == null || name == null) return null;
		GlossaryDocument doc;
		try {
			doc = glossary.getContents();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
		if (doc == null || doc.getDocParts() == null) return null;
		for (CTDocPart part : doc.getDocParts().getDocPart()) {
			if (name.equals(nameOf(part))) return part;
		}
		return null;
	}

	/** An entry's name, from its {@code w:docPartPr/w:name}; null where it has none. */
	public static String nameOf(CTDocPart part) {
		if (part.getDocPartPr() == null) return null;
		for (Object o : part.getDocPartPr().getNameOrStyleOrCategory()) {
			Object u = XmlUtils.unwrap(o);
			if (u instanceof CTDocPartName) return ((CTDocPartName) u).getVal();
		}
		return null;
	}

	/** Word's default entry for a kind, written where the glossary lacks it. */
	private static String ensureDefaultEntry(WordprocessingMLPackage pkg, DefaultEntry kind) throws Exception {
		GlossaryDocumentPart glossary = ensureGlossary(pkg);
		if (entryOf(glossary, kind.name) == null) {
			R run = Context.getWmlObjectFactory().createR();
			RPr rPr = Context.getWmlObjectFactory().createRPr();
			org.docx4j.wml.RStyle rStyle = Context.getWmlObjectFactory().createRStyle();
			rStyle.setVal(PLACEHOLDER_STYLE);
			rPr.setRStyle(rStyle);
			run.setRPr(rPr);
			org.docx4j.wml.Text t = Context.getWmlObjectFactory().createText();
			t.setValue(kind.text);
			run.getContent().add(t);
			addEntry(glossary, docPartOf(kind.name, newGuid(), run));
			log.info("Glossary entry " + kind.name + " added");
		}
		return kind.name;
	}

	/**
	 * The entry holding a configured placeholder run (D4), as Word makes an entry for placeholder text
	 * of its own (check 65): named with 32 upper-case hex digits, its own GUID, the default's
	 * {@code w:docPartPr}, its body one paragraph holding the run. One entry per distinct run per
	 * document: an entry whose body is that run already is reused.
	 */
	private static String ensureEntryFor(WordprocessingMLPackage pkg, R configured) throws Exception {
		GlossaryDocumentPart glossary = ensureGlossary(pkg);
		String wanted = XmlUtils.marshaltoString(configured, true, false);
		GlossaryDocument doc = glossary.getContents();
		if (doc.getDocParts() != null) {
			for (CTDocPart part : doc.getDocParts().getDocPart()) {
				String name = nameOf(part);
				if (name == null || part.getDocPartBody() == null) continue;
				List<Object> body = part.getDocPartBody().getContent();
				if (body.size() != 1) continue;
				Object p = XmlUtils.unwrap(body.get(0));
				if (!(p instanceof P) || ((P) p).getContent().size() != 1) continue;
				Object r = XmlUtils.unwrap(((P) p).getContent().get(0));
				if (r instanceof R && wanted.equals(XmlUtils.marshaltoString(r, true, false))) {
					return name;
				}
			}
		}
		String name = UUID.randomUUID().toString().replace("-", "").toUpperCase();
		addEntry(glossary, docPartOf(name, newGuid(), XmlUtils.deepCopy(configured)));
		log.info("Glossary entry " + name + " added for the configured placeholder");
		return name;
	}

	/** A {@code w:guid} for an entry, new to the document, as Word gives one it adds. */
	private static String newGuid() {
		return "{" + UUID.randomUUID().toString().toUpperCase() + "}";
	}

	private static void addEntry(GlossaryDocumentPart glossary, CTDocPart part) throws Exception {
		GlossaryDocument doc = glossary.getContents();
		if (doc.getDocParts() == null) {
			CTDocParts parts = Context.getWmlObjectFactory().createCTDocParts();
			doc.setDocParts(parts);
			parts.setParent(doc);
		}
		doc.getDocParts().getDocPart().add(part);
		part.setParent(doc.getDocParts());
	}

	/**
	 * A placeholder entry as Word writes one: category General, gallery placeholder, type
	 * {@code bbPlcHdr}, behavior content, a GUID of its own, and a body of one paragraph holding the run.
	 */
	private static CTDocPart docPartOf(String name, String guid, R run) {
		ObjectFactory of = Context.getWmlObjectFactory();
		CTDocPart part = of.createCTDocPart();
		CTDocPartPr pr = of.createCTDocPartPr();
		part.setDocPartPr(pr);

		CTDocPartName n = of.createCTDocPartName();
		n.setVal(name);
		pr.getNameOrStyleOrCategory().add(n);

		CTDocPartCategory category = of.createCTDocPartCategory();
		CTDocPartCategory.Name categoryName = of.createCTDocPartCategoryName();
		categoryName.setVal("General");
		category.setName(categoryName);
		CTDocPartGallery gallery = of.createCTDocPartGallery();
		gallery.setVal(STDocPartGallery.PLACEHOLDER);
		category.setGallery(gallery);
		pr.getNameOrStyleOrCategory().add(category);

		CTDocPartTypes types = of.createCTDocPartTypes();
		CTDocPartType type = of.createCTDocPartType();
		type.setVal(STDocPartType.BB_PLC_HDR);
		types.getType().add(type);
		pr.getNameOrStyleOrCategory().add(types);

		CTDocPartBehaviors behaviors = of.createCTDocPartBehaviors();
		CTDocPartBehavior behavior = of.createCTDocPartBehavior();
		behavior.setVal(STDocPartBehavior.CONTENT);
		behaviors.getBehavior().add(behavior);
		pr.getNameOrStyleOrCategory().add(behaviors);

		CTGuid g = of.createCTGuid();
		g.setVal(guid);
		pr.getNameOrStyleOrCategory().add(g);

		Body body = of.createBody();
		P p = of.createP();
		p.getContent().add(run);
		body.getContent().add(p);
		part.setDocPartBody(body);
		return part;
	}

	/**
	 * The document's glossary, made where it has none (D7): the glossary document part and, related
	 * from it, a styles part holding Default Paragraph Font and Placeholder Text as Word defines them.
	 * A glossary the document has gets the two styles where its styles part lacks them, and a styles
	 * part where it has none; Word completes the rest (settings, webSettings, fontTable) on save.
	 */
	static GlossaryDocumentPart ensureGlossary(WordprocessingMLPackage pkg) throws Exception {
		MainDocumentPart main = pkg.getMainDocumentPart();
		GlossaryDocumentPart glossary = main.getGlossaryDocumentPart();
		if (glossary == null) {
			glossary = new GlossaryDocumentPart();
			GlossaryDocument doc = Context.getWmlObjectFactory().createGlossaryDocument();
			CTDocParts parts = Context.getWmlObjectFactory().createCTDocParts();
			doc.setDocParts(parts);
			parts.setParent(doc);
			glossary.setJaxbElement(doc);
			main.addTargetPart(glossary);
			log.info("Glossary document part added, for the placeholder entries");
		}
		ensureGlossaryStyles(glossary);
		return glossary;
	}

	/**
	 * D10 (check 65c): the main styles part holds Placeholder Text, and Default Paragraph Font which it
	 * is based on, where a placeholder run is written. Word adds the style to a document's styles when it
	 * writes a placeholder of its own; where the document's styles lack it, Word drops the style from
	 * every placeholder run on opening and shows the placeholder black (Word 365, check 65c; Word 2010,
	 * core-ts's check 35). A style the part already defines is left as it is; a document with no
	 * styles part is left alone.
	 *
	 * @param pkg the document
	 * @since 17.3.5
	 */
	public static void ensurePlaceholderStyle(WordprocessingMLPackage pkg) {
		if (pkg == null || pkg.getMainDocumentPart() == null) return;
		StyleDefinitionsPart stylesPart = pkg.getMainDocumentPart().getStyleDefinitionsPart();
		if (stylesPart == null) return;
		try {
			Styles styles = stylesPart.getContents();
			if (styles == null) return;
			ensureStyles(styles);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private static void ensureGlossaryStyles(GlossaryDocumentPart glossary) throws Exception {
		StyleDefinitionsPart stylesPart = glossary.getStyleDefinitionsPart();
		if (stylesPart == null) {
			stylesPart = new StyleDefinitionsPart(new PartName("/word/glossary/styles.xml"));
			stylesPart.setJaxbElement(Context.getWmlObjectFactory().createStyles());
			glossary.addTargetPart(stylesPart);
		}
		Styles styles = stylesPart.getContents();
		if (styles == null) {
			styles = Context.getWmlObjectFactory().createStyles();
			stylesPart.setJaxbElement(styles);
		}
		ensureStyles(styles);
	}

	/** Default Paragraph Font and Placeholder Text added where missing; a style the part defines is kept. */
	private static void ensureStyles(Styles styles) {
		boolean hasDefaultParagraphFont = false, hasPlaceholderText = false;
		for (Style s : styles.getStyle()) {
			if ("DefaultParagraphFont".equals(s.getStyleId())) hasDefaultParagraphFont = true;
			if (PLACEHOLDER_STYLE.equals(s.getStyleId())) hasPlaceholderText = true;
		}
		if (!hasDefaultParagraphFont) {
			Style s = defaultParagraphFont();
			styles.getStyle().add(s);
			s.setParent(styles);
		}
		if (!hasPlaceholderText) {
			Style s = placeholderText();
			styles.getStyle().add(s);
			s.setParent(styles);
		}
	}

	/** Default Paragraph Font as Word defines it, which Placeholder Text is based on. */
	private static Style defaultParagraphFont() {
		ObjectFactory of = Context.getWmlObjectFactory();
		Style s = of.createStyle();
		s.setType("character");
		s.setDefault(Boolean.TRUE);
		s.setStyleId("DefaultParagraphFont");
		Style.Name name = of.createStyleName();
		name.setVal("Default Paragraph Font");
		s.setName(name);
		Style.UiPriority pri = of.createStyleUiPriority();
		pri.setVal(BigInteger.ONE);
		s.setUiPriority(pri);
		s.setSemiHidden(new BooleanDefaultTrue());
		s.setUnhideWhenUsed(new BooleanDefaultTrue());
		return s;
	}

	/** Placeholder Text as Word defines it: based on Default Paragraph Font, grey 666666. */
	private static Style placeholderText() {
		ObjectFactory of = Context.getWmlObjectFactory();
		Style s = of.createStyle();
		s.setType("character");
		s.setStyleId(PLACEHOLDER_STYLE);
		Style.Name name = of.createStyleName();
		name.setVal("Placeholder Text");
		s.setName(name);
		Style.BasedOn basedOn = of.createStyleBasedOn();
		basedOn.setVal("DefaultParagraphFont");
		s.setBasedOn(basedOn);
		Style.UiPriority pri = of.createStyleUiPriority();
		pri.setVal(BigInteger.valueOf(99));
		s.setUiPriority(pri);
		s.setUnhideWhenUsed(new BooleanDefaultTrue());
		RPr rPr = of.createRPr();
		org.docx4j.wml.Color color = of.createColor();
		color.setVal("666666");
		rPr.setColor(color);
		s.setRPr(rPr);
		return s;
	}
}
