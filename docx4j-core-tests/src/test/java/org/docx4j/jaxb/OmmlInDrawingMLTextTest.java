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
package org.docx4j.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.docx4j.XmlUtils;
import org.docx4j.com.microsoft.schemas.office.drawing.x2010.main.CTTextMath;
import org.docx4j.convert.out.mathml.OmmlToMathML;
import org.docx4j.dml.CTTextCharacterProperties;
import org.docx4j.math.CTCtrlPr;
import org.docx4j.math.CTOMath;
import org.docx4j.math.CTR;
import org.docx4j.utils.ResourceUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jakarta.xml.bind.JAXBElement;

/**
 * CR-025: an equation in a DrawingML text body (a14:m holding m:oMath, MS-ODRAWXML 2.3.1)
 * carries a:rPr on its m:r and m:ctrlPr, there being no w:rPr in a slide.  The OMML types
 * admit it, so the equation unmarshals typed in the WordprocessingML context (it used to
 * fail: "unexpected element a:rPr") and is written back as PowerPoint wrote it.  In the
 * PresentationML context the equation stays DOM (CR-021 §8.9), as before.
 *
 * The fragment is loadAndSave.pptx's slide 2: 16 runs and 7 control-properties elements,
 * every one with an a:rPr naming Cambria Math.
 */
public class OmmlInDrawingMLTextTest {

	private static final String A14 = "http://schemas.microsoft.com/office/drawing/2010/main";
	private static final String DML = "http://schemas.openxmlformats.org/drawingml/2006/main";
	private static final String MATH = "http://schemas.openxmlformats.org/officeDocument/2006/math";

	private static String fragment;

	@BeforeClass
	public static void readFragment() throws Exception {
		String xml = null;
		try (ZipInputStream zip = new ZipInputStream(ResourceUtils.getResource("loadAndSave.pptx"))) {
			for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
				if (entry.getName().equals("ppt/slides/slide2.xml")) {
					xml = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
				}
			}
		}
		assertNotNull(xml);
		int start = xml.indexOf("<a14:m>");
		int end = xml.indexOf("</a14:m>") + "</a14:m>".length();
		assertTrue("slide 2 carries an a14:m", start > 0 && end > start);
		fragment = xml.substring(start, end).replace("<a14:m>",
				"<a14:m xmlns:a14=\"" + A14 + "\" xmlns:a=\"" + DML + "\" xmlns:m=\"" + MATH + "\">");
		assertEquals(16, count(fragment, "<m:r><a:rPr"));
		assertEquals(7, count(fragment, "<m:ctrlPr><a:rPr"));
	}

	@Test
	public void typedInTheWordprocessingMLContext() throws Exception {
		CTTextMath textMath = unmarshal(Context.jc);
		assertTrue("the equation is typed, not DOM", textMath.getAny() instanceof JAXBElement);
		CTOMath oMath = (CTOMath) ((JAXBElement<?>) textMath.getAny()).getValue();

		List<CTR> runs = new ArrayList<>();
		List<CTCtrlPr> ctrls = new ArrayList<>();
		collect(oMath, runs, ctrls);
		assertEquals(16, runs.size());
		assertEquals(7, ctrls.size());
		for (CTR r : runs) {
			Object first = r.getContent().get(0);
			assertTrue("a run's first child is the a:rPr element", first instanceof JAXBElement
					&& ((JAXBElement<?>) first).getValue() instanceof CTTextCharacterProperties);
			assertEquals(DML, ((JAXBElement<?>) first).getName().getNamespaceURI());
			assertEquals("Cambria Math", ((CTTextCharacterProperties) ((JAXBElement<?>) first).getValue()).getLatin().getTypeface());
		}
		for (CTCtrlPr c : ctrls) {
			assertNotNull("m:ctrlPr keeps its a:rPr", c.getRPrDml());
			assertEquals("Cambria Math", c.getRPrDml().getLatin().getTypeface());
		}

		String out = XmlUtils.marshaltoString(wrap(textMath), true, false, Context.jc);
		assertEquals(16, count(out, "<m:r><a:rPr"));
		assertEquals(7, count(out, "<m:ctrlPr><a:rPr"));
		assertEquivalent(fragment, out);
	}

	@Test
	public void domInThePresentationMLContext() throws Exception {
		CTTextMath textMath = unmarshal(org.pptx4j.jaxb.Context.jcPML);
		assertTrue("no m: classes in jcPML, so the equation stays DOM", textMath.getAny() instanceof Element);
		String out = XmlUtils.marshaltoString(wrap(textMath), true, false, org.pptx4j.jaxb.Context.jcPML);
		assertEquals(16, count(out, "<m:r><a:rPr"));
		assertEquals(7, count(out, "<m:ctrlPr><a:rPr"));
		assertEquivalent(fragment, out);
	}

	@Test
	public void mathMLFromTheTypedEquation() throws Exception {
		CTTextMath textMath = unmarshal(Context.jc);
		CTOMath oMath = (CTOMath) ((JAXBElement<?>) textMath.getAny()).getValue();
		String withRPr = new OmmlToMathML().toMathMLString(oMath);
		assertTrue(withRPr.contains("<mi>") || withRPr.contains("<mo>"));

		// the same equation with its a:rPr stripped gives the same MathML
		String stripped = fragment.replaceAll("<a:rPr[^>]*/>", "").replaceAll("<a:rPr[^>]*>.*?</a:rPr>", "");
		assertEquals(0, count(stripped, "<a:rPr"));
		CTTextMath bare = (CTTextMath) ((JAXBElement<?>) XmlUtils.unmarshalString(stripped, Context.jc)).getValue();
		String withoutRPr = new OmmlToMathML().toMathMLString((CTOMath) ((JAXBElement<?>) bare.getAny()).getValue());
		assertEquals(withoutRPr, withRPr);
	}

	private static CTTextMath unmarshal(jakarta.xml.bind.JAXBContext jc) throws Exception {
		Object o = XmlUtils.unmarshalString(fragment, jc);
		return (CTTextMath) (o instanceof JAXBElement ? ((JAXBElement<?>) o).getValue() : o);
	}

	private static Object wrap(CTTextMath textMath) {
		return new org.docx4j.com.microsoft.schemas.office.drawing.x2010.main.ObjectFactory().createM(textMath);
	}

	private static void collect(Object o, List<CTR> runs, List<CTCtrlPr> ctrls) {
		Object v = XmlUtils.unwrap(o);
		if (v instanceof CTR) {
			runs.add((CTR) v);
			return;
		}
		if (v instanceof CTCtrlPr) {
			ctrls.add((CTCtrlPr) v);
			return;
		}
		if (v == null || v.getClass().getName().startsWith("java.")
				|| v instanceof CTTextCharacterProperties) {
			return;
		}
		for (Object child : childrenOf(v)) {
			collect(child, runs, ctrls);
		}
	}

	/** Every bean-property value of a generated OMML class, lists flattened. */
	private static List<Object> childrenOf(Object v) {
		List<Object> children = new ArrayList<>();
		for (java.lang.reflect.Method m : v.getClass().getMethods()) {
			if (m.getParameterCount() != 0 || !m.getName().startsWith("get")
					|| m.getName().equals("getClass") || m.getName().equals("getParent")) {
				continue;
			}
			try {
				Object r = m.invoke(v);
				if (r instanceof List) {
					children.addAll((List<?>) r);
				} else if (r != null) {
					children.add(r);
				}
			} catch (Exception e) {
				// not a bean property
			}
		}
		return children;
	}

	private static int count(String s, String needle) {
		int n = 0;
		for (int i = s.indexOf(needle); i >= 0; i = s.indexOf(needle, i + 1)) {
			n++;
		}
		return n;
	}

	/** xsd:boolean's two lexical forms: PowerPoint writes i="1", JAXB writes a typed one back as i="true". */
	private static String bool(String v) {
		return "1".equals(v) ? "true" : "0".equals(v) ? "false" : v;
	}

	/** Same elements, attributes and text, namespace-aware; namespace declarations ignored,
	 *  booleans compared by value. */
	private static void assertEquivalent(String expected, String actual) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		Document e = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(expected.getBytes(StandardCharsets.UTF_8)));
		Document a = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(actual.getBytes(StandardCharsets.UTF_8)));
		assertSameElement("", e.getDocumentElement(), a.getDocumentElement());
	}

	private static void assertSameElement(String path, Element e, Element a) {
		path = path + "/" + e.getLocalName();
		assertEquals(path, e.getNamespaceURI(), a.getNamespaceURI());
		assertEquals(path, e.getLocalName(), a.getLocalName());
		NamedNodeMap ea = e.getAttributes();
		for (int i = 0; i < ea.getLength(); i++) {
			Attr attr = (Attr) ea.item(i);
			if ("http://www.w3.org/2000/xmlns/".equals(attr.getNamespaceURI())) {
				continue;
			}
			assertEquals(path + "/@" + attr.getName(), bool(attr.getValue()),
					bool(a.getAttributeNS(attr.getNamespaceURI(), attr.getLocalName())));
		}
		List<Node> ec = children(e), ac = children(a);
		assertEquals(path + " child count", ec.size(), ac.size());
		for (int i = 0; i < ec.size(); i++) {
			Node x = ec.get(i), y = ac.get(i);
			assertEquals(path + " child " + i, x.getNodeType(), y.getNodeType());
			if (x instanceof Element) {
				assertSameElement(path, (Element) x, (Element) y);
			} else {
				assertEquals(path + " text " + i, x.getNodeValue(), y.getNodeValue());
			}
		}
	}

	private static List<Node> children(Element e) {
		List<Node> list = new ArrayList<>();
		NodeList nl = e.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n instanceof Element || (n.getNodeType() == Node.TEXT_NODE && !n.getNodeValue().trim().isEmpty())) {
				list.add(n);
			}
		}
		return list;
	}
}
