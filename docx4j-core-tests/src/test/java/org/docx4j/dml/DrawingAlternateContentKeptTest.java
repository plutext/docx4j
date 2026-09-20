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
package org.docx4j.dml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.dml.chart.CTChartSpace;
import org.docx4j.dml.spreadsheetdrawing.CTDrawing;
import org.docx4j.dml.spreadsheetdrawing.CTOneCellAnchor;
import org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor;
import org.docx4j.jaxb.McSelection;
import org.docx4j.jaxb.NamespacePrefixMappings;
import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.docx4j.openpackaging.parts.DrawingML.Drawing;
import org.docx4j.utils.ResourceUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * CR-024: the mc:AlternateContent Office writes inside DrawingML parts is admitted
 * by the schema where it is written - a spreadsheet drawing's root (xdr:wsDr), its
 * anchors' object position, and a chart's c:chartSpace - so a load-and-save keeps
 * both branches, as CR-021 keeps the WordprocessingML, SpreadsheetML and
 * PresentationML hosts.  Before, each was resolved to its Fallback on load: a check
 * box's drawing became a bare {@code <xdr:wsDr/>} (Excel's Fallback there is empty),
 * a slicer's or timeline's shape became a "works in Excel 2010 or higher" box, and
 * a chart lost its c14 style.
 *
 * <p>Every JAXB part is unmarshalled before the save.  docx4j writes an untouched
 * part back from its bytes, so the ordinary path hides the loss until something
 * touches the part; the forced path is what a consumer that edits sees.</p>
 *
 * <p>Kept content is DOM: the anchors and c:style are local elements of their schemas,
 * so a kept branch's children are {@link Element}s (lossless on re-marshal); c14:style
 * is a global element of a bound schema and comes back typed (CR-024 §5).</p>
 */
public class DrawingAlternateContentKeptTest {

	private static final Logger log = LoggerFactory.getLogger(DrawingAlternateContentKeptTest.class);

	private String savedPreference;

	@Before
	public void clearPreference() {
		savedPreference = Docx4jProperties.getProperty(McSelection.PROPERTY);
		Docx4jProperties.setProperty(McSelection.PROPERTY, "");
	}

	@After
	public void restorePreference() {
		Docx4jProperties.setProperty(McSelection.PROPERTY, savedPreference == null ? "" : savedPreference);
	}

	/** xdr:wsDr > mc:AlternateContent (Choice a14: twoCellAnchor; Fallback empty). */
	@Test
	public void checkBoxDrawingKeptWithItsEmptyFallback() throws Exception {
		OpcPackage pkg = OpcPackage.load(ResourceUtils.getResource("cr022-checkbox.xlsx"));
		CTDrawing drawing = onlyPart(pkg, Drawing.class).getContents();
		AlternateContent ac = theCheckBoxElement(drawing);

		// the reader's view: Office 2007's (the empty Fallback) by default, the a14 shape on request
		assertSame(ac.getFallback(), McSelection.selectedBranch(ac));
		assertTrue("an empty Fallback selects nothing", McSelection.select(ac).isEmpty());
		Docx4jProperties.setProperty(McSelection.PROPERTY, "a14");
		assertSame(ac.getChoice().get(0), McSelection.selectedBranch(ac));
		assertEquals(1, McSelection.select(ac).size());
		Docx4jProperties.setProperty(McSelection.PROPERTY, "");

		byte[] saved = forcedResave(pkg);
		String xml = entry(saved, "xl/drawings/drawing1.xml");
		assertTrue(xml, xml.contains("<mc:AlternateContent"));
		assertTrue(xml, xml.contains("Requires=\"a14\""));
		assertTrue(xml, xml.contains("<xdr:twoCellAnchor"));
		assertTrue(xml, xml.contains("<a14:compatExt spid=\"_x0000_s1025\"/>"));
		assertTrue("the empty Fallback stays, empty: " + xml,
				xml.contains("<mc:Fallback/>") || xml.contains("<mc:Fallback></mc:Fallback>"));
		assertRequiresDeclared("xl/drawings/drawing1.xml", xml);

		OpcPackage again = OpcPackage.load(new ByteArrayInputStream(saved));
		theCheckBoxElement(onlyPart(again, Drawing.class).getContents());
	}

	private static AlternateContent theCheckBoxElement(CTDrawing drawing) {
		assertEquals("one anchor-position child", 1, drawing.getEGAnchor().size());
		Object o = drawing.getEGAnchor().get(0);
		assertTrue("kept as mc:AlternateContent, not resolved: " + o.getClass(), o instanceof AlternateContent);
		AlternateContent ac = (AlternateContent) o;
		assertEquals(1, ac.getChoice().size());
		assertEquals("a14", ac.getChoice().get(0).getRequires());
		assertEquals("twoCellAnchor", localName(ac.getChoice().get(0).getAny()));
		assertNotNull("Excel writes a Fallback", ac.getFallback());
		assertTrue("and it is empty", ac.getFallback().getAny().isEmpty());
		return ac;
	}

	private static final Pattern FALLBACK_SP = Pattern.compile("<mc:Fallback>\\s*<xdr:sp ");

	/** xdr:twoCellAnchor > mc:AlternateContent (Choice a14 / tsle / sle15: graphicFrame; Fallback: sp). */
	@Test
	public void slicerAndTimelineShapesKept() throws Exception {
		OpcPackage pkg = OpcPackage.load(ResourceUtils.getResource("cr022-slicers-timelines.xlsx"));
		assertEquals("a14 tsle sle15", requiresOfTheTwoCellAnchors(pkg));

		byte[] saved = forcedResave(pkg);
		for (String name : new String[] { "xl/drawings/drawing1.xml", "xl/drawings/drawing2.xml" }) {
			String xml = entry(saved, name);
			assertTrue(name + ": " + xml, xml.contains("<xdr:graphicFrame"));
			assertTrue(name + ": the Fallback box: " + xml, FALLBACK_SP.matcher(xml).find());
			assertRequiresDeclared(name, xml);
		}
		String d1 = entry(saved, "xl/drawings/drawing1.xml");
		assertTrue(d1, d1.contains("Requires=\"tsle\""));
		// Excel declares xmlns:tsle on the Choice; the Choice is a typed object, so the declaration
		// is not kept there - the DOM content's namespace is hoisted to the root under the prefix
		// the table gives it (CR-024 §4: without the entry it would be ns#, and Requires="tsle" undeclared)
		assertTrue("the part declares the Requires prefix: " + d1,
				d1.contains("xmlns:tsle=\"http://schemas.microsoft.com/office/drawing/2012/timeslicer\""));
		String d2 = entry(saved, "xl/drawings/drawing2.xml");
		assertTrue(d2, d2.contains("Requires=\"sle15\""));
		assertTrue(d2, d2.contains("xmlns:sle15=\"http://schemas.microsoft.com/office/drawing/2012/slicer\""));

		assertEquals("a14 tsle sle15", requiresOfTheTwoCellAnchors(OpcPackage.load(new ByteArrayInputStream(saved))));
	}

	private static String requiresOfTheTwoCellAnchors(OpcPackage pkg) throws Exception {
		StringBuilder sb = new StringBuilder();
		for (Drawing d : parts(pkg, Drawing.class)) {
			for (Object o : d.getContents().getEGAnchor()) {
				CTTwoCellAnchor anchor = (CTTwoCellAnchor) o;
				AlternateContent ac = anchor.getAlternateContent();
				assertNotNull("the anchor's object is an mc:AlternateContent", ac);
				assertNull("not resolved to the Fallback's box", anchor.getSp());
				assertNull(anchor.getGraphicFrame());
				assertEquals(1, ac.getChoice().size());
				assertEquals("graphicFrame", localName(ac.getChoice().get(0).getAny()));
				assertEquals("sp", localName(ac.getFallback().getAny()));
				if (sb.length() > 0) sb.append(' ');
				sb.append(ac.getChoice().get(0).getRequires());
			}
		}
		return sb.toString();
	}

	/** xdr:oneCellAnchor > mc:AlternateContent (Choice a14: sp; Fallback: sp), loadAndSave.xlsx. */
	@Test
	public void oneCellAnchorShapeKept() throws Exception {
		OpcPackage pkg = OpcPackage.load(ResourceUtils.getResource("loadAndSave.xlsx"));
		theOneCellAnchorElement(pkg);
		byte[] saved = forcedResave(pkg);
		String xml = entry(saved, "xl/drawings/drawing1.xml");
		assertTrue(xml, xml.contains("<xdr:oneCellAnchor>"));
		assertEquals("one anchor carries it", 1, count(xml, "<mc:AlternateContent"));
		assertRequiresDeclared("xl/drawings/drawing1.xml", xml);
		theOneCellAnchorElement(OpcPackage.load(new ByteArrayInputStream(saved)));
	}

	private static void theOneCellAnchorElement(OpcPackage pkg) throws Exception {
		int found = 0;
		for (Object o : onlyPart(pkg, Drawing.class).getContents().getEGAnchor()) {
			if (o instanceof CTOneCellAnchor && ((CTOneCellAnchor) o).getAlternateContent() != null) {
				AlternateContent ac = ((CTOneCellAnchor) o).getAlternateContent();
				assertEquals("a14", ac.getChoice().get(0).getRequires());
				assertEquals("sp", localName(ac.getChoice().get(0).getAny()));
				assertEquals("sp", localName(ac.getFallback().getAny()));
				assertNull(((CTOneCellAnchor) o).getSp());
				found++;
			}
		}
		assertEquals(1, found);
	}

	private static final Pattern C_STYLE_2 = Pattern.compile("<c:style [^>]*val=\"2\"/>");

	/** c:chartSpace > mc:AlternateContent (Choice c14: c14:style 102; Fallback: c:style 2), in all three formats. */
	@Test
	public void chartStyleKeptInWordPowerPointAndExcel() throws Exception {
		for (String file : new String[] { "loadAndSave.docx", "loadAndSave.pptx", "loadAndSave.xlsx" }) {
			OpcPackage pkg = OpcPackage.load(ResourceUtils.getResource(file));
			CTChartSpace chartSpace = onlyPart(pkg, Chart.class).getContents();
			AlternateContent ac = theChartStyleElement(file, chartSpace);

			assertSame(file, ac.getFallback(), McSelection.selectedBranch(ac));
			Docx4jProperties.setProperty(McSelection.PROPERTY, "c14");
			assertSame(file, ac.getChoice().get(0), McSelection.selectedBranch(ac));
			Docx4jProperties.setProperty(McSelection.PROPERTY, "");

			byte[] saved = forcedResave(pkg);
			String xml = entry(saved, onlyPart(pkg, Chart.class).getPartName().getName().substring(1));
			assertTrue(file + ": " + xml, xml.contains("<c14:style val=\"102\"/>"));
			// the kept DOM element re-declares the namespaces that were in scope at Excel's root (c16r2)
			assertTrue(file + ": " + xml, C_STYLE_2.matcher(xml).find());
			assertTrue(file + ": " + xml, xml.contains("Requires=\"c14\""));
			assertRequiresDeclared(file, xml);

			theChartStyleElement(file, onlyPart(OpcPackage.load(new ByteArrayInputStream(saved)), Chart.class).getContents());
		}
	}

	private static AlternateContent theChartStyleElement(String file, CTChartSpace chartSpace) {
		assertNull(file + ": c:style is inside the Fallback, not resolved into the chart", chartSpace.getStyle());
		AlternateContent ac = chartSpace.getAlternateContent();
		assertNotNull(file + ": the style element kept", ac);
		assertEquals(file, "c14", ac.getChoice().get(0).getRequires());
		Object c14 = XmlUtils.unwrap(ac.getChoice().get(0).getAny().get(0));  // a JAXBElement: a global element with a named type
		assertTrue(file + ": c14:style is a global element of a bound schema, so typed: " + c14.getClass(),
				c14 instanceof org.docx4j.dml.chart.x2007.CTStyle);
		assertEquals(file, 102, ((org.docx4j.dml.chart.x2007.CTStyle) c14).getVal());
		assertEquals(file + ": c:style is local, so DOM", "style", localName(ac.getFallback().getAny()));
		assertEquals(file, "2", ((Element) ac.getFallback().getAny().get(0)).getAttribute("val"));
		return ac;
	}

	/** The prefixes the kept branches and Excel's mc:Ignorable lists name (CR-024 §4). */
	@Test
	public void prefixesKnownBothWays() {
		String[][] table = {
				{ "x16r2", "http://schemas.microsoft.com/office/spreadsheetml/2015/02/main" },
				{ "oel", "http://schemas.microsoft.com/office/2019/extlst" },
				{ "sle", "http://schemas.microsoft.com/office/drawing/2010/slicer" },
				{ "sle15", "http://schemas.microsoft.com/office/drawing/2012/slicer" },
				{ "tsle", "http://schemas.microsoft.com/office/drawing/2012/timeslicer" },
				{ "a14", "http://schemas.microsoft.com/office/drawing/2010/main" },
				{ "c14", "http://schemas.microsoft.com/office/drawing/2007/8/2/chart" } };
		NamespacePrefixMappings npm = new NamespacePrefixMappings();
		for (String[] row : table) {
			assertEquals(row[1], row[0], NamespacePrefixMappings.getPreferredPrefixStatic(row[1], null, true));
			assertEquals(row[0], row[1], npm.getNamespaceURI(row[0]));
		}
	}

	// helpers

	private static final Pattern REQUIRES = Pattern.compile("Requires=\"([^\"]+)\"");

	/** Every prefix a Requires names is declared in the part (an undeclared one is a repair prompt, CR-023). */
	private static void assertRequiresDeclared(String name, String xml) {
		java.util.regex.Matcher m = REQUIRES.matcher(xml);
		int n = 0;
		while (m.find()) {
			for (String prefix : m.group(1).split(" ")) {
				assertTrue(name + " does not declare xmlns:" + prefix + ": " + xml, xml.contains("xmlns:" + prefix + "=\""));
			}
			n++;
		}
		assertTrue(name + ": no Requires at all", n > 0);
	}

	/** Every JAXB part unmarshalled, then saved: each part is re-marshalled from its objects. */
	private static byte[] forcedResave(OpcPackage pkg) throws Exception {
		for (Part p : new ArrayList<Part>(pkg.getParts().getParts().values())) {
			if (p instanceof JaxbXmlPart) {
				try {
					((JaxbXmlPart<?>) p).getContents();
				} catch (Exception e) {
					// a part docx4j cannot unmarshal (the legacy VML drawing of a form control: an
					// xlsx4j backlog item, not this CR's) is written back from its bytes, as before
					assertTrue("only the VML drawing is expected to fail: " + p.getPartName() + ": " + e,
							p.getPartName().getName().endsWith(".vml"));
					log.warn("not unmarshalled, written back from its bytes: " + p.getPartName());
				}
			}
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		pkg.save(bos);
		return bos.toByteArray();
	}

	private static <T extends Part> List<T> parts(OpcPackage pkg, Class<T> type) {
		List<T> found = new ArrayList<T>();
		for (Part p : pkg.getParts().getParts().values()) {
			if (type.isInstance(p)) found.add(type.cast(p));
		}
		return found;
	}

	private static <T extends Part> T onlyPart(OpcPackage pkg, Class<T> type) {
		List<T> found = parts(pkg, type);
		assertEquals("one " + type.getSimpleName() + " part", 1, found.size());
		return found.get(0);
	}

	/** The local name of a kept branch's single child, a DOM element. */
	private static String localName(List<Object> any) {
		assertEquals("one child", 1, any.size());
		Object o = any.get(0);
		assertTrue("a local element comes back as DOM: " + o.getClass(), o instanceof Element);
		return ((Element) o).getLocalName();
	}

	private static String entry(byte[] zip, String name) throws Exception {
		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip))) {
			ZipEntry e;
			while ((e = zis.getNextEntry()) != null) {
				if (e.getName().equals(name)) {
					return new String(readAll(zis), StandardCharsets.UTF_8);
				}
			}
		}
		fail("no " + name + " in the saved package");
		return null;
	}

	private static byte[] readAll(InputStream is) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[8192];
		int n;
		while ((n = is.read(buf)) > 0) bos.write(buf, 0, n);
		return bos.toByteArray();
	}

	private static int count(String s, String needle) {
		int n = 0, i = 0;
		while ((i = s.indexOf(needle, i)) >= 0) { n++; i += needle.length(); }
		return n;
	}
}
