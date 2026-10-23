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
package org.docx4j.convert.out.html;

import java.math.BigInteger;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.*;

/**
 * CR-056: a document an attacker writes, built with docx4j: links whose target is a script scheme, a
 * style whose ID closes a {@code <style>} element, and the benign forms beside them. The outputs must
 * keep every benign link and write no script scheme and no raw style ID.
 */
public final class HostileDocx {

	private static final ObjectFactory F = Context.getWmlObjectFactory();

	public static final String SCRIPT_LINK = "javascript:alert('H1_LINK')";
	public static final String SCRIPT_FIELD = "javascript:alert('H2_FIELD')";
	/** A browser drops tabs and line breaks in a scheme and ignores leading controls and spaces. */
	public static final String SCRIPT_SNEAKY = " \u0001java\tscri\npt:alert('H3_SNEAKY')";
	public static final String VBSCRIPT_LINK = "vbscript:MsgBox('H4_VB')";
	public static final String DATA_LINK = "data:text/html,<script>alert('H5_DATA')</script>";
	/** A script scheme written with character references, as a CommonMark renderer decodes them (CR-056 §14). */
	public static final String ENTITY_LINK = "javascript&#58;alert('H8_ENTITY')";
	public static final String ENTITY_LINK_2 = "&#106;avascript&colon;alert('H9_ENTITY')";
	public static final String STYLE_ID = "x</style><script>alert('H6_STYLEID')</script>";
	public static final String TABLE_STYLE_ID = "t</style><script>alert('H7_TBLSTYLE')</script>";
	public static final String SPACED_STYLE_ID = "Body Text 2";
	/** A style ID that begins with a digit: an invalid selector unescaped, so its rule was dropped. */
	public static final String DIGIT_STYLE_ID = "1Heading";
	/** A scheme outside the default set: refused unless {@code docx4j.convert.out.linkSchemes} adds it. */
	public static final String CUSTOM_LINK = "onenote:///C:/notes/section.one#page";

	public static final String[] BENIGN = { "http://example.org/a", "https://example.org/b", "mailto:a@example.org",
			"ftp://example.org/c", "file:///C:/docs/d.docx", "tel:+61000000", "news:comp.lang.java", "sub/dir/page.html",
			"#bookmark1", "C:\\docs\\e.docx", "\\\\server\\share\\f.docx" };

	private HostileDocx() {}

	public static WordprocessingMLPackage build() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		// styles: a hostile paragraph style, a hostile table style, a style whose ID has spaces
		Styles styles = mdp.getStyleDefinitionsPart().getJaxbElement();
		styles.getStyle().add(paragraphStyle(STYLE_ID));
		styles.getStyle().add(paragraphStyle(SPACED_STYLE_ID));
		styles.getStyle().add(paragraphStyle(DIGIT_STYLE_ID));
		Style ts = F.createStyle(); ts.setType("table"); ts.setStyleId(TABLE_STYLE_ID);
		Style.Name tn = F.createStyleName(); tn.setVal("HostileTable"); ts.setName(tn); ts.setTblPr(F.createCTTblPrBase());
		styles.getStyle().add(ts);
		// links by relationship
		for (String target : new String[] { SCRIPT_LINK, SCRIPT_SNEAKY, VBSCRIPT_LINK, DATA_LINK, CUSTOM_LINK, ENTITY_LINK, ENTITY_LINK_2 }) mdp.getContent().add(link(mdp, target, "text for " + target));
		for (String target : BENIGN) mdp.getContent().add(link(mdp, target, "benign " + target));
		// a HYPERLINK field
		mdp.getContent().add(field(SCRIPT_FIELD, "field link"));
		mdp.getContent().add(field("https://example.org/field", "benign field"));
		// the styles in use
		mdp.getContent().add(styled(STYLE_ID, "styled"));
		mdp.getContent().add(styled(SPACED_STYLE_ID, "spaced"));
		mdp.getContent().add(styled(DIGIT_STYLE_ID, "digit"));
		Tbl tbl = F.createTbl(); TblPr tp = F.createTblPr(); CTTblPrBase.TblStyle tst = F.createCTTblPrBaseTblStyle(); tst.setVal(TABLE_STYLE_ID); tp.setTblStyle(tst); tbl.setTblPr(tp);
		Tr tr = F.createTr(); Tc tc = F.createTc(); tc.getContent().add(styled(null, "cell")); tr.getContent().add(tc); tbl.getContent().add(tr);
		mdp.getContent().add(tbl);
		return pkg;
	}

	static Style paragraphStyle(String id) {
		Style st = F.createStyle(); st.setType("paragraph"); st.setStyleId(id);
		Style.Name nm = F.createStyleName(); nm.setVal("Name of " + id); st.setName(nm);
		PPr ppr = F.createPPr(); Jc jc = F.createJc(); jc.setVal(JcEnumeration.CENTER); ppr.setJc(jc); st.setPPr(ppr);
		return st;
	}

	static P link(MainDocumentPart mdp, String target, String text) {
		Relationship rel = new org.docx4j.relationships.ObjectFactory().createRelationship();
		rel.setType(Namespaces.HYPERLINK); rel.setTarget(target); rel.setTargetMode("External");
		mdp.getRelationshipsPart().addRelationship(rel);
		P p = F.createP(); P.Hyperlink h = F.createPHyperlink(); h.setId(rel.getId());
		h.getContent().add(run(text)); p.getContent().add(h);
		return p;
	}

	static P field(String target, String text) {
		P p = F.createP();
		R r1 = F.createR(); FldChar b = F.createFldChar(); b.setFldCharType(STFldCharType.BEGIN); r1.getContent().add(b); p.getContent().add(r1);
		R r2 = F.createR(); Text instr = F.createText(); instr.setValue(" HYPERLINK \"" + target + "\" "); instr.setSpace("preserve");
		r2.getContent().add(F.createRInstrText(instr)); p.getContent().add(r2);
		R r3 = F.createR(); FldChar s = F.createFldChar(); s.setFldCharType(STFldCharType.SEPARATE); r3.getContent().add(s); p.getContent().add(r3);
		p.getContent().add(run(text));
		R r4 = F.createR(); FldChar e = F.createFldChar(); e.setFldCharType(STFldCharType.END); r4.getContent().add(e); p.getContent().add(r4);
		return p;
	}

	static P styled(String styleId, String text) {
		P p = F.createP();
		if (styleId != null) { PPr ppr = F.createPPr(); PPrBase.PStyle ps = F.createPPrBasePStyle(); ps.setVal(styleId); ppr.setPStyle(ps); p.setPPr(ppr); }
		p.getContent().add(run(text));
		return p;
	}

	static R run(String t) { R r = F.createR(); Text x = F.createText(); x.setValue(t); r.getContent().add(x); return r; }

	/** Counts the occurrences of a string. */
	public static int count(String haystack, String needle) {
		int n = 0;
		for (int i = haystack.indexOf(needle); i >= 0; i = haystack.indexOf(needle, i + needle.length())) n++;
		return n;
	}
}
