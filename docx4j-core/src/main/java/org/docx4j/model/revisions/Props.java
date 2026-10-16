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
package org.docx4j.model.revisions;

import java.lang.reflect.Field;

import javax.xml.namespace.QName;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.revisions.TrackedChange.Para;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.CTParaRPrOriginal;
import org.docx4j.wml.CTRPrChange;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.SectPr;

/**
 * Recorded properties back into live ones (CR-043 section 3.4): the records are not the live
 * types ({@code CTRPrChange.RPr} is a list, {@code w:pPrChange} holds a {@code PPrBase}), so they
 * go through their XML, whose elements are the same, and come back as the live type.
 */
final class Props {

	private Props() {}

	/** {@code recorded}, marshalled as {@code w:<local>} of {@code recordedType}, read back as {@code liveType}. */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static <T> T convert(Object recorded, Class<?> recordedType, String local, Class<T> liveType) {
		if (recorded == null) return null;
		try {
			Marshaller m = Context.jc.createMarshaller();
			org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder().newDocument();
			m.marshal(new JAXBElement(new QName(Trees.W_NS, local), recordedType, recorded), doc);
			return (T) XmlUtils.unmarshal(doc.getDocumentElement(), Context.jc, liveType);
		} catch (Exception e) {
			throw new RuntimeException("Could not read the recorded " + local + " as " + liveType.getSimpleName(), e);
		}
	}

	/** A run's recorded formatting ({@code w:rPrChange/w:rPr}) as a live {@code w:rPr}; null when it records none. */
	static RPr rPrOf(CTRPrChange.RPr recorded) {
		RPr rPr = convert(recorded, CTRPrChange.RPr.class, "rPr", RPr.class);
		return rPr == null || Trees.isEmpty(rPr) ? null : rPr;
	}

	/** A mark's recorded formatting ({@code w:pPr/w:rPr/w:rPrChange/w:rPr}) as a live mark {@code w:rPr}. */
	static ParaRPr paraRPrOf(CTParaRPrOriginal recorded) {
		return convert(recorded, CTParaRPrOriginal.class, "rPr", ParaRPr.class);
	}

	/** A paragraph's recorded properties ({@code w:pPrChange/w:pPr}) as a live {@code w:pPr}. */
	static PPr pPrOf(PPrBase recorded) {
		return convert(recorded, PPrBase.class, "pPr", PPr.class);
	}

	/** A run's direct formatting replaced by what its {@code w:rPrChange} recorded, wholesale (checks 24, 28). */
	static void restoreRPr(R run, CTRPrChange change) {
		RPr original = rPrOf(change.getRPr());
		run.setRPr(original);
		if (original != null) original.setParent(run);
	}

	/**
	 * A paragraph's properties replaced by what its {@code w:pPrChange} recorded, keeping the mark's
	 * run properties and the section (the record is a {@code CT_PPrBase}, which has neither).
	 */
	static void restorePPr(Para para, PPrBase recorded, org.docx4j.openpackaging.packages.WordprocessingMLPackage pkg) {
		PPr current = para.p.getPPr();
		if (current == null) return;
		ParaRPr rPr = current.getRPr();
		SectPr sectPr = current.getSectPr();
		PPr restored = recorded == null ? new PPr() : pPrOf(recorded);
		if (restored == null) restored = new PPr();
		restored.setRPr(rPr);
		if (rPr != null) rPr.setParent(restored);
		restored.setSectPr(sectPr);
		if (sectPr != null) sectPr.setParent(restored);
		restored.setPPrChange(null);
		para.p.setPPr(restored);
		restored.setParent(para.p);
		dropLevelIndent(para, pkg);
		writeRecordedLevel(para.p.getPPr());
		pruneParagraph(para.p);
	}

	/**
	 * A mark's formatting put back as its {@code w:rPrChange} recorded it, its own revisions
	 * (inserted, deleted, moved) kept (check 27).
	 */
	static void restoreMarkProperties(Para para, org.docx4j.wml.ParaRPrChange change) {
		PPr pPr = para.p.getPPr();
		if (pPr == null || pPr.getRPr() == null) return;
		ParaRPr current = pPr.getRPr();
		ParaRPr restored = change.getRPr() == null ? new ParaRPr() : paraRPrOf(change.getRPr());
		if (restored == null) restored = new ParaRPr();
		restored.setIns(current.getIns());
		restored.setDel(current.getDel());
		restored.setMoveFrom(current.getMoveFrom());
		restored.setMoveTo(current.getMoveTo());
		restored.setRPrChange(null);
		pPr.setRPr(restored);
		restored.setParent(pPr);
		pruneParagraph(para.p);
	}

	/** Drops {@code w:pPr/w:rPr} and {@code w:pPr} once nothing is left in them, so that resolving leaves no husk. */
	static void pruneParagraph(org.docx4j.wml.P p) {
		PPr pPr = p.getPPr();
		if (pPr == null) return;
		if (pPr.getRPr() != null && Trees.isEmpty(pPr.getRPr())) pPr.setRPr(null);
		if (Trees.isEmpty(pPr)) p.setPPr(null);
	}

	/** Drops a run's {@code w:rPr} once nothing is left in it. */
	static void pruneRun(R run) {
		if (run.getRPr() != null && Trees.isEmpty(run.getRPr())) run.setRPr(null);
	}

	/**
	 * Word records a list paragraph's indent in {@code w:pPrChange} even where the list level gives
	 * it, and on reject writes no indent back (check 18 section 9, check 25): a restored
	 * {@code w:ind} whose every attribute is the level's is dropped, leaving the level to give it.
	 */
	private static void dropLevelIndent(Para para, org.docx4j.openpackaging.packages.WordprocessingMLPackage pkg) {
		PPr pPr = para.p.getPPr();
		if (pPr == null || pPr.getNumPr() == null || pPr.getInd() == null) return;
		NumberingDefinitionsPart ndp = null;
		if (pkg != null && pkg.getMainDocumentPart() != null) ndp = pkg.getMainDocumentPart().getNumberingDefinitionsPart();
		if (ndp == null) return;
		PPrBase.Ind level;
		try {
			level = ndp.getInd(pPr.getNumPr());
		} catch (RuntimeException e) {
			return;
		}
		if (level == null || !indentWithin(pPr.getInd(), level)) return;
		pPr.setInd(null);
	}

	/** True when every attribute {@code ind} sets, {@code level} sets to the same value. */
	private static boolean indentWithin(PPrBase.Ind ind, PPrBase.Ind level) {
		boolean any = false;
		for (Field f : PPrBase.Ind.class.getDeclaredFields()) {
			if (java.lang.reflect.Modifier.isStatic(f.getModifiers()) || "parent".equals(f.getName())) continue;
			try {
				f.setAccessible(true);
				Object mine = f.get(ind);
				if (mine == null) continue;
				any = true;
				Object theirs = f.get(level);
				if (theirs == null || !String.valueOf(mine).equals(String.valueOf(theirs))) return false;
			} catch (RuntimeException | IllegalAccessException e) {
				return false;
			}
		}
		return any;
	}

	/**
	 * A restored {@code w:numPr} that names a list but no level is at level 0, and Word writes the 0
	 * (check 25's {@code 25e}, check 18 section 9).
	 */
	private static void writeRecordedLevel(PPr pPr) {
		if (pPr == null) return;
		PPrBase.NumPr numPr = pPr.getNumPr();
		if (numPr == null || numPr.getNumId() == null || numPr.getIlvl() != null) return;
		if (numPr.getNumId().getVal() == null || numPr.getNumId().getVal().signum() == 0) return;
		PPrBase.NumPr.Ilvl ilvl = new PPrBase.NumPr.Ilvl();
		ilvl.setVal(java.math.BigInteger.ZERO);
		ilvl.setParent(numPr);
		numPr.setIlvl(ilvl);
	}
}
