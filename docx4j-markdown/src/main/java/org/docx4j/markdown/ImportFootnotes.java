package org.docx4j.markdown;

import java.math.BigInteger;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.CTFootnotes;
import org.docx4j.wml.CTFtnDocProps;
import org.docx4j.wml.CTFtnEdn;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RStyle;

/**
 * Real footnotes for imported markdown: lazily sets up the footnotes part
 * (with separator/continuationSeparator, and the matching settings.xml
 * footnotePr) the first time a footnote is added.
 */
class ImportFootnotes {

	static final String FOOTNOTE_TEXT = "FootnoteText";
	static final String FOOTNOTE_REFERENCE = "FootnoteReference";

	private static final String W_NS_DECL =
			"xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final String SEPARATORS_XML =
			"<w:footnotes " + W_NS_DECL + ">"
			+ "<w:footnote w:id=\"-1\" w:type=\"separator\">"
			+ "<w:p><w:pPr><w:spacing w:after=\"0\" w:line=\"240\" w:lineRule=\"auto\"/></w:pPr>"
			+ "<w:r><w:separator/></w:r></w:p></w:footnote>"
			+ "<w:footnote w:id=\"0\" w:type=\"continuationSeparator\">"
			+ "<w:p><w:pPr><w:spacing w:after=\"0\" w:line=\"240\" w:lineRule=\"auto\"/></w:pPr>"
			+ "<w:r><w:continuationSeparator/></w:r></w:p></w:footnote>"
			+ "</w:footnotes>";

	private static final String FOOTNOTE_PR_XML =
			"<w:footnotePr " + W_NS_DECL + ">"
			+ "<w:footnote w:id=\"-1\"/>"
			+ "<w:footnote w:id=\"0\"/>"
			+ "</w:footnotePr>";

	private final ObjectFactory factory = Context.getWmlObjectFactory();
	private final WordprocessingMLPackage pkg;
	private FootnotesPart footnotesPart;

	ImportFootnotes(WordprocessingMLPackage pkg) {
		this.pkg = pkg;
	}

	/**
	 * Add a footnote with the given block content (its first paragraph is
	 * given the FootnoteText style and the footnoteRef marker), and return
	 * the footnote id for use in a w:footnoteReference.
	 */
	BigInteger add(List<Object> blockContent) throws Docx4JException {

		FootnotesPart part = getFootnotesPart();

		BigInteger id = nextId(part);
		CTFtnEdn footnote = factory.createCTFtnEdn();
		footnote.setId(id);

		P first;
		if (!blockContent.isEmpty() && blockContent.get(0) instanceof P) {
			first = (P) blockContent.get(0);
		} else {
			first = factory.createP();
			blockContent.add(0, first);
		}

		// restyle the first paragraph and prepend the footnoteRef marker
		if (first.getPPr() == null) {
			first.setPPr(factory.createPPr());
		}
		org.docx4j.wml.PPrBase.PStyle pStyle = factory.createPPrBasePStyle();
		pStyle.setVal(FOOTNOTE_TEXT);
		first.getPPr().setPStyle(pStyle);

		R spaceRun = factory.createR();
		org.docx4j.wml.Text space = factory.createText();
		space.setValue(" ");
		space.setSpace("preserve");
		spaceRun.getContent().add(space);
		first.getContent().add(0, spaceRun);

		R markerRun = factory.createR();
		RPr rPr = factory.createRPr();
		RStyle rStyle = factory.createRStyle();
		rStyle.setVal(FOOTNOTE_REFERENCE);
		rPr.setRStyle(rStyle);
		markerRun.setRPr(rPr);
		markerRun.getContent().add(factory.createRFootnoteRef(factory.createRFootnoteRef()));
		first.getContent().add(0, markerRun);

		footnote.getContent().addAll(blockContent);
		part.getJaxbElement().getFootnote().add(footnote);
		return id;
	}

	private BigInteger nextId(FootnotesPart part) {
		BigInteger max = BigInteger.ZERO;
		for (CTFtnEdn f : part.getJaxbElement().getFootnote()) {
			if (f.getId() != null && f.getId().compareTo(max) > 0) {
				max = f.getId();
			}
		}
		return max.add(BigInteger.ONE);
	}

	private FootnotesPart getFootnotesPart() throws Docx4JException {

		if (footnotesPart != null) {
			return footnotesPart;
		}

		MainDocumentPart mdp = pkg.getMainDocumentPart();
		footnotesPart = mdp.getFootnotesPart();
		if (footnotesPart != null) {
			return footnotesPart;
		}

		try {
			footnotesPart = new FootnotesPart();
			footnotesPart.setJaxbElement(
					(CTFootnotes) XmlUtils.unwrap(XmlUtils.unmarshalString(SEPARATORS_XML)));
			mdp.addTargetPart(footnotesPart);

			// settings.xml usually carries the matching footnote properties
			DocumentSettingsPart dsp = mdp.getDocumentSettingsPart();
			if (dsp == null) {
				dsp = new DocumentSettingsPart();
				mdp.addTargetPart(dsp);
			}
			CTSettings settings = dsp.getContents();
			if (settings == null) {
				settings = factory.createCTSettings();
				dsp.setJaxbElement(settings);
			}
			if (settings.getFootnotePr() == null) {
				settings.setFootnotePr((CTFtnDocProps) XmlUtils.unmarshalString(
						FOOTNOTE_PR_XML, Context.jc, CTFtnDocProps.class));
			}
		} catch (jakarta.xml.bind.JAXBException e) {
			throw new Docx4JException("Could not initialise footnotes part", e);
		}

		return footnotesPart;
	}

}
