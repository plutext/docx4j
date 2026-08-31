package org.docx4j.markdown;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.commonmark.node.ListBlock;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.Lvl;
import org.docx4j.wml.NumFmt;
import org.docx4j.wml.NumberFormat;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;

/**
 * Real numbering for imported markdown lists.
 *
 * <p>Each top-level list is pre-scanned for the list type (bullet/ordered) at
 * each nesting depth, giving a "signature" from which a 9-level abstractNum is
 * built (so ordered-inside-bullet and vice versa get correct markers).
 * Each top-level list containing an ordered level gets its OWN abstractNum +
 * num, so ordered lists restart correctly (and honour their start value);
 * nested ordered levels restart via OOXML's default level-restart behavior.
 * Bullet-only lists share one num per signature.</p>
 */
class ImportNumbering {

	private static final int LEVELS = 9;
	private static final int TWIPS_PER_LEVEL = 720;
	private static final int HANGING = 360;

	/** Word's default bullet glyph/font cycle. */
	private static final String[] BULLET_CHARS = { "\uF0B7", "o", "\uF0A7" };
	private static final String[] BULLET_FONTS = { "Symbol", "Courier New", "Wingdings" };

	private final ObjectFactory factory = Context.getWmlObjectFactory();
	private final NumberingDefinitionsPart ndp;
	private final Map<String, BigInteger> bulletNumBySignature = new HashMap<>();

	ImportNumbering(WordprocessingMLPackage pkg) throws InvalidFormatException {
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		NumberingDefinitionsPart existing = mdp.getNumberingDefinitionsPart();
		if (existing == null) {
			existing = new NumberingDefinitionsPart();
			existing.setJaxbElement(factory.createNumbering());
			mdp.addTargetPart(existing);
		}
		this.ndp = existing;
	}

	/**
	 * The numId to use for the given top-level list (and, at deeper ilvls,
	 * for the lists nested within it).
	 */
	BigInteger numIdFor(ListBlock topLevelList) {

		NumberFormat[] signature = new NumberFormat[LEVELS];
		scan(topLevelList, 0, signature);
		for (int i = 0; i < LEVELS; i++) {
			if (signature[i] == null) {
				signature[i] = (i == 0) ? NumberFormat.BULLET : signature[i - 1];
			}
		}

		boolean bulletOnly = true;
		StringBuilder key = new StringBuilder();
		for (NumberFormat f : signature) {
			bulletOnly &= (f == NumberFormat.BULLET);
			key.append(f == NumberFormat.BULLET ? 'b' : 'd');
		}

		if (bulletOnly) {
			return bulletNumBySignature.computeIfAbsent(key.toString(),
					k -> create(signature, 1));
		}

		int start = 1;
		if (topLevelList instanceof OrderedList) {
			Integer s = ((OrderedList) topLevelList).getMarkerStartNumber();
			if (s != null && s > 0) {
				start = s;
			}
		}
		return create(signature, start);
	}

	private void scan(ListBlock list, int depth, NumberFormat[] signature) {
		if (depth >= LEVELS) {
			return;
		}
		if (signature[depth] == null) {
			signature[depth] = (list instanceof OrderedList) ? NumberFormat.DECIMAL : NumberFormat.BULLET;
		}
		for (Node item = list.getFirstChild(); item != null; item = item.getNext()) {
			for (Node child = item.getFirstChild(); child != null; child = child.getNext()) {
				if (child instanceof ListBlock) {
					scan((ListBlock) child, depth + 1, signature);
				}
			}
		}
	}

	private BigInteger create(NumberFormat[] signature, int topLevelStart) {

		Numbering.AbstractNum abstractNum = factory.createNumberingAbstractNum();
		Numbering.AbstractNum.MultiLevelType mlt = factory.createNumberingAbstractNumMultiLevelType();
		mlt.setVal("hybridMultilevel");
		abstractNum.setMultiLevelType(mlt);

		int bulletCycle = 0;
		for (int i = 0; i < LEVELS; i++) {

			Lvl lvl = factory.createLvl();
			lvl.setIlvl(BigInteger.valueOf(i));

			Lvl.Start startEl = factory.createLvlStart();
			startEl.setVal(BigInteger.valueOf(i == 0 ? topLevelStart : 1));
			lvl.setStart(startEl);

			NumFmt numFmt = factory.createNumFmt();
			numFmt.setVal(signature[i]);
			lvl.setNumFmt(numFmt);

			Lvl.LvlText lvlText = factory.createLvlLvlText();
			if (signature[i] == NumberFormat.BULLET) {
				int c = bulletCycle++ % BULLET_CHARS.length;
				lvlText.setVal(BULLET_CHARS[c]);
				RPr rPr = factory.createRPr();
				RFonts rFonts = factory.createRFonts();
				rFonts.setAscii(BULLET_FONTS[c]);
				rFonts.setHAnsi(BULLET_FONTS[c]);
				rPr.setRFonts(rFonts);
				lvl.setRPr(rPr);
			} else {
				lvlText.setVal("%" + (i + 1) + ".");
			}
			lvl.setLvlText(lvlText);

			Jc jc = factory.createJc();
			jc.setVal(JcEnumeration.LEFT);
			lvl.setLvlJc(jc);

			PPr pPr = factory.createPPr();
			PPrBase.Ind ind = factory.createPPrBaseInd();
			ind.setLeft(BigInteger.valueOf((long) TWIPS_PER_LEVEL * (i + 1)));
			ind.setHanging(BigInteger.valueOf(HANGING));
			pPr.setInd(ind);
			lvl.setPPr(pPr);

			abstractNum.getLvl().add(lvl);
		}

		Numbering.Num num = ndp.addAbstractListNumberingDefinition(abstractNum);
		return num.getNumId();
	}

}
