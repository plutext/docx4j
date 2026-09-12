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
package org.docx4j.fonts;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.fonts.RunFontSelector.RunFontActionType;
import org.docx4j.fonts.RunFontSelector.RunFontCharacterVisitor;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Styles;
import org.docx4j.wml.Text;
import org.junit.Assume;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Shared set-up for the CR-016 RunFontSelector tests: a package whose document fonts
 * are names of our own, mapped to the two Liberation faces the test classpath carries,
 * so that an assertion reads which <em>document</em> font the selector chose whatever
 * the machine has installed.
 */
final class FontsTestSupport {

	static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	static final String A = "http://schemas.openxmlformats.org/drawingml/2006/main";

	/** Physical faces from docx4j-export-fo-fonts-liberation, on the test classpath. */
	static final String SANS = "Liberation Sans";
	static final String SERIF = "Liberation Serif";

	private FontsTestSupport() {}

	/** A package with these styles (a w:styles document) and settings (w:settings), the
	 *  theme part given as an a:fontScheme (null for no theme part), and a body of these
	 *  w:p elements; the Mapper cleared and mapping only what the test says. */
	static WordprocessingMLPackage packageWith(String stylesXml, String settingsXml, String fontSchemeXml,
			String bodyXml, String... mappings) throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		// the parts first: getFontMapper() below populates the mapper, which builds the
		// PropertyResolver, which snapshots the styles part as it then is
		if (stylesXml != null) {
			pkg.getMainDocumentPart().getStyleDefinitionsPart().setJaxbElement(
					(Styles) XmlUtils.unwrap(XmlUtils.unmarshalString(stylesXml)));
		}
		if (settingsXml != null) {
			DocumentSettingsPart dsp = pkg.getMainDocumentPart().getDocumentSettingsPart();
			if (dsp == null) {
				dsp = new DocumentSettingsPart();
				pkg.getMainDocumentPart().addTargetPart(dsp);
			}
			dsp.setJaxbElement((CTSettings) XmlUtils.unwrap(XmlUtils.unmarshalString(settingsXml)));
		}
		if (fontSchemeXml != null) {
			ThemePart tp = new ThemePart();
			tp.setJaxbElement((org.docx4j.dml.Theme) XmlUtils.unwrap(XmlUtils.unmarshalString(themeXml(fontSchemeXml))));
			pkg.getMainDocumentPart().addTargetPart(tp);
		}
		pkg.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document) XmlUtils.unwrap(XmlUtils.unmarshalString(
				"<w:document xmlns:w=\"" + W + "\"><w:body>" + bodyXml + "</w:body></w:document>")));
		Mapper mapper = pkg.getFontMapper(); // discovery happens in the Mapper's static initialiser
		Assume.assumeTrue("Liberation Sans/Serif not on the classpath",
				PhysicalFonts.get(SANS) != null && PhysicalFonts.get(SERIF) != null);
		mapper.getFontMappings().clear();
		for (int i = 0; i + 1 < mappings.length; i += 2) {
			mapper.put(mappings[i], PhysicalFonts.get(mappings[i + 1]));
		}
		return pkg;
	}

	static String styles(String docDefaultsRPr, String moreStyles) {
		return "<w:styles xmlns:w=\"" + W + "\"><w:docDefaults><w:rPrDefault><w:rPr>" + docDefaultsRPr
				+ "</w:rPr></w:rPrDefault><w:pPrDefault/></w:docDefaults>"
				+ "<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/></w:style>"
				+ "<w:style w:type=\"character\" w:default=\"1\" w:styleId=\"DefaultParagraphFont\"><w:name w:val=\"Default Paragraph Font\"/></w:style>"
				+ (moreStyles == null ? "" : moreStyles) + "</w:styles>";
	}

	static String settings(String themeFontLangAttrs) {
		return "<w:settings xmlns:w=\"" + W + "\"><w:themeFontLang " + themeFontLangAttrs + "/></w:settings>";
	}

	/** a:fontScheme with these Latin faces and the given a:font script entries in both collections */
	static String fontScheme(String majorLatin, String minorLatin, String scriptFonts) {
		return "<a:fontScheme name=\"Office\">"
				+ "<a:majorFont><a:latin typeface=\"" + majorLatin + "\"/><a:ea typeface=\"\"/><a:cs typeface=\"\"/>" + scriptFonts + "</a:majorFont>"
				+ "<a:minorFont><a:latin typeface=\"" + minorLatin + "\"/><a:ea typeface=\"\"/><a:cs typeface=\"\"/>" + scriptFonts + "</a:minorFont>"
				+ "</a:fontScheme>";
	}

	private static String themeXml(String fontScheme) {
		String fill = "<a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill>";
		String ln = "<a:ln w=\"9525\">" + fill + "</a:ln>";
		return "<a:theme xmlns:a=\"" + A + "\" name=\"Office\"><a:themeElements>"
				+ "<a:clrScheme name=\"Office\"><a:dk1><a:sysClr val=\"windowText\" lastClr=\"000000\"/></a:dk1>"
				+ "<a:lt1><a:sysClr val=\"window\" lastClr=\"FFFFFF\"/></a:lt1><a:dk2><a:srgbClr val=\"1F497D\"/></a:dk2>"
				+ "<a:lt2><a:srgbClr val=\"EEECE1\"/></a:lt2><a:accent1><a:srgbClr val=\"4F81BD\"/></a:accent1>"
				+ "<a:accent2><a:srgbClr val=\"C0504D\"/></a:accent2><a:accent3><a:srgbClr val=\"9BBB59\"/></a:accent3>"
				+ "<a:accent4><a:srgbClr val=\"8064A2\"/></a:accent4><a:accent5><a:srgbClr val=\"4BACC6\"/></a:accent5>"
				+ "<a:accent6><a:srgbClr val=\"F79646\"/></a:accent6><a:hlink><a:srgbClr val=\"0000FF\"/></a:hlink>"
				+ "<a:folHlink><a:srgbClr val=\"800080\"/></a:folHlink></a:clrScheme>"
				+ fontScheme
				+ "<a:fmtScheme name=\"Office\"><a:fillStyleLst>" + fill + fill + fill + "</a:fillStyleLst>"
				+ "<a:lnStyleLst>" + ln + ln + ln + "</a:lnStyleLst>"
				+ "<a:effectStyleLst><a:effectStyle><a:effectLst/></a:effectStyle><a:effectStyle><a:effectLst/></a:effectStyle>"
				+ "<a:effectStyle><a:effectLst/></a:effectStyle></a:effectStyleLst>"
				+ "<a:bgFillStyleLst>" + fill + fill + fill + "</a:bgFillStyleLst></a:fmtScheme>"
				+ "</a:themeElements></a:theme>";
	}

	/** A w:p holding one run with these run properties (null for none) and text. */
	static String p(String rPrXml, String text) {
		return "<w:p><w:r>" + (rPrXml == null ? "" : "<w:rPr>" + rPrXml + "</w:rPr>")
				+ "<w:t xml:space=\"preserve\">" + text + "</w:t></w:r></w:p>";
	}

	/** The font-family of every inline the selector produced for paragraph i's first run,
	 *  without the kerned/no-ligature suffix. */
	static List<String> families(WordprocessingMLPackage pkg, int i) throws Exception {
		RunFontSelector rfs = xslFoSelector(pkg);
		P p = (P) pkg.getMainDocumentPart().getContent().get(i);
		R r = (R) p.getContent().get(0);
		RPr rPr = r.getRPr();
		Text t = (Text) XmlUtils.unwrap(r.getContent().get(0));
		Object o = rfs.fontSelector(p.getPPr(), rPr, t);
		List<String> out = new ArrayList<String>();
		if (o instanceof DocumentFragment) {
			for (Node n = ((DocumentFragment) o).getFirstChild(); n != null; n = n.getNextSibling()) {
				if (n instanceof Element) out.add(plain(((Element) n).getAttribute("font-family")));
			}
		}
		return out;
	}

	static String plain(String fontFamily) {
		if (fontFamily == null) return null;
		for (String suffix : new String[] { RunFontSelector.NOLIGA_SUFFIX, RunFontSelector.KERNED_SUFFIX }) {
			if (fontFamily.endsWith(suffix)) return fontFamily.substring(0, fontFamily.length() - suffix.length());
		}
		return fontFamily;
	}

	/** The XSL FO character visitor, as FOConversionContext builds it. */
	static RunFontSelector xslFoSelector(WordprocessingMLPackage pkg) {
		return new RunFontSelector(pkg, new RunFontCharacterVisitor() {
			DocumentFragment df; StringBuilder sb = new StringBuilder(); Element span; String lastFont; String fallbackFontName;
			private org.w3c.dom.Document document; private boolean spanReusable = true; private RunFontSelector rfs;
			public void setDocument(org.w3c.dom.Document d) { document = d; df = d.createDocumentFragment(); }
			public boolean isReusable() { return spanReusable; }
			public void addCharacterToCurrent(char c) { sb.append(c); }
			public void addCodePointToCurrent(int cp) { sb.append(new String(Character.toChars(cp))); }
			public void finishPrevious() {
				if (sb.length() > 0) {
					if (span == null) { span = rfs.createElement(document); if (lastFont != null) rfs.setAttribute(span, lastFont); }
					df.appendChild(span); span.setTextContent(sb.toString()); sb.setLength(0);
				}
			}
			public void createNew() { span = rfs.createElement(document); }
			public void setMustCreateNewFlag(boolean val) { spanReusable = !val; }
			public void fontAction(String fontname) {
				if (fontname == null) rfs.setAttribute(span, fallbackFontName);
				else { rfs.setAttribute(span, fontname); lastFont = fontname; }
			}
			public Object getResult() { span = null; return df; }
			public void setRunFontSelector(RunFontSelector r) { rfs = r; }
			public void setFallbackFont(String fontname) { fallbackFontName = fontname; }
		}, RunFontActionType.XSL_FO);
	}
}
