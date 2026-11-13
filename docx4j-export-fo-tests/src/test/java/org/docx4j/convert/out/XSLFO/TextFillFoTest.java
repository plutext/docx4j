/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 */
package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.docx4j.wml.Style;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A body run's solid {@code w14:textFill} as its colour in the FO output, both routes (CR-083 phase 5, step 1): over
 * {@code w:color} (wb50-01), with its {@code w14:alpha} as "#RRGGBBAA" (wb50-02), through the theme, from a character
 * style; a gradient keeps {@code w:color} until the FO renderer fork draws it. Core-tests' TextEffectPropertiesTest has
 * the properties and HTML.
 */
public class TextFillFoTest extends AbstractXSLFOTest {

	private static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
			+ "xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\"";
	private static final int[] PATHWAYS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String solid(String colour) {
		return "<w14:textFill><w14:solidFill>" + colour + "</w14:solidFill></w14:textFill>";
	}

	private static WordprocessingMLPackage doc() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		List<Style> styles = mdp.getStyleDefinitionsPart().getContents().getStyle();
		styles.add((Style) XmlUtils.unmarshalString("<w:style " + NS + " w:type=\"character\" w:styleId=\"GreenFill\">"
				+ "<w:name w:val=\"GreenFill\"/><w:rPr>" + solid("<w14:srgbClr w14:val=\"00B050\"/>") + "</w:rPr></w:style>"));
		List<Object> body = mdp.getContent();
		body.add(p("<w:r><w:rPr><w:color w:val=\"000000\"/>" + solid("<w14:srgbClr w14:val=\"C00000\"/>")
				+ "</w:rPr><w:t>FILLED</w:t></w:r>"));
		body.add(p("<w:r><w:rPr><w:color w:val=\"000000\"/>"
				+ solid("<w14:srgbClr w14:val=\"C00000\"><w14:alpha w14:val=\"40000\"/></w14:srgbClr>")
				+ "</w:rPr><w:t>ALPHA</w:t></w:r>"));
		body.add(p("<w:r><w:rPr>" + solid("<w14:schemeClr w14:val=\"accent2\"/>") + "</w:rPr><w:t>SCHEME</w:t></w:r>"));
		body.add(p("<w:r><w:rPr><w:rStyle w:val=\"GreenFill\"/><w:color w:val=\"000000\"/></w:rPr><w:t>STYLED</w:t></w:r>"));
		body.add(p("<w:r><w:rPr><w:color w:val=\"2F5597\"/><w14:textFill><w14:gradFill><w14:gsLst>"
				+ "<w14:gs w14:pos=\"0\"><w14:srgbClr w14:val=\"FF0000\"/></w14:gs>"
				+ "<w14:gs w14:pos=\"100000\"><w14:srgbClr w14:val=\"0000FF\"/></w14:gs></w14:gsLst>"
				+ "<w14:lin w14:ang=\"0\" w14:scaled=\"0\"/></w14:gradFill></w14:textFill></w:rPr><w:t>GRADIENT</w:t></w:r>"));
		return pkg;
	}

	private static P p(String content) throws Exception {
		return (P) XmlUtils.unmarshalString("<w:p " + NS + ">" + content + "</w:p>");
	}

	private static String foString(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return baos.toString("UTF-8");
	}

	/** The value of an attribute on the innermost element enclosing a text which has it */
	private static String attribute(Document fo, String text, String name) {
		Element e = enclosing(fo.getDocumentElement(), text);
		assertNotNull(text, e);
		for (; e != null; e = e.getParentNode() instanceof Element ? (Element) e.getParentNode() : null) {
			if (e.hasAttribute(name)) return e.getAttribute(name).toUpperCase();
		}
		return null;
	}

	private static Element enclosing(Element e, String text) {
		for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() == Node.TEXT_NODE && n.getNodeValue().trim().equals(text)) return e;
			if (n instanceof Element) {
				Element found = enclosing((Element) n, text);
				if (found != null) return found;
			}
		}
		return null;
	}

	@Test
	public void aSolidFillIsTheRunsColour() throws Exception {
		for (int flags : PATHWAYS) {
			String pathway = flags == Docx4J.FLAG_NONE ? "visitor: " : "XSLT: ";
			String s = foString(doc(), flags);
			Document fo = XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(s.getBytes("UTF-8")));
			assertEquals(pathway + "FILLED", "#C00000", attribute(fo, "FILLED", "color"));
			assertEquals(pathway + "ALPHA", "#C0000099", attribute(fo, "ALPHA", "color"));
			assertEquals(pathway + "SCHEME", "#E97132", attribute(fo, "SCHEME", "color"));
			assertEquals(pathway + "STYLED", "#00B050", attribute(fo, "STYLED", "color"));
			assertEquals(pathway + "GRADIENT", "#2F5597", attribute(fo, "GRADIENT", "color"));
		}
	}
}
