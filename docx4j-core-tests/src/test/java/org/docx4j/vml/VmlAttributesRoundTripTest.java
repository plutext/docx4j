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
package org.docx4j.vml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * CR-048: the attributes Word writes on VML shapes survive a load and a re-marshal - {@code w14:anchorId}
 * on every shape type, {@code o:gfxdata} beyond {@code v:shape} - over every fixture here whose
 * WordprocessingML parts hold VML. The two rewrites CR-048's D3 accepts are the only differences
 * allowed: {@code o:spt} as a float ({@code 202} written {@code 202.0}) and {@code o:gfxdata}'s base64
 * layout (the same bytes).
 */
public class VmlAttributesRoundTripTest {

	private static final String VML = "urn:schemas-microsoft-com:vml";
	private static final String O = "urn:schemas-microsoft-com:office:office";
	private static final String W14 = "http://schemas.microsoft.com/office/word/2010/wordml";

	static Path resources() {
		for (Path dir : List.of(Path.of("src/test/resources"), Path.of("docx4j-core-tests/src/test/resources")))
			if (Files.isDirectory(dir.resolve("2010"))) return dir;
		throw new IllegalStateException("Run from the repository root or docx4j-core-tests");
	}

	/** The fixtures whose WordprocessingML parts hold VML. */
	static List<Path> fixtures() throws Exception {
		List<Path> out = new ArrayList<Path>();
		try (Stream<Path> s = Files.walk(resources())) {
			for (Path f : s.filter(p -> p.toString().endsWith(".docx")).sorted().collect(Collectors.toList())) {
				try (ZipFile zip = new ZipFile(f.toFile())) {
					boolean vml = zip.stream().anyMatch(e -> e.getName().startsWith("word/") && e.getName().endsWith(".xml") && read(zip, e).contains("<v:"));
					if (vml) out.add(f);
				} catch (java.util.zip.ZipException notAZip) {
					// a fixture that is deliberately not a package
				}
			}
		}
		return out;
	}

	private static String read(ZipFile zip, ZipEntry e) {
		try {
			return new String(zip.getInputStream(e).readAllBytes(), StandardCharsets.UTF_8);
		} catch (Exception ex) {
			return "";
		}
	}

	/** Each VML element's attributes, by namespace URI and local name, in document order. */
	static List<Map<String, String>> vml(String xml, List<String> names) throws Exception {
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setNamespaceAware(true);
		Element root = f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))).getDocumentElement();
		List<Map<String, String>> out = new ArrayList<Map<String, String>>();
		collect(root, out, names);
		return out;
	}

	private static void collect(Node n, List<Map<String, String>> out, List<String> names) {
		for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (c.getNodeType() != Node.ELEMENT_NODE) continue;
			if (VML.equals(c.getNamespaceURI())) {
				Map<String, String> attrs = new LinkedHashMap<String, String>();
				NamedNodeMap map = c.getAttributes();
				for (int i = 0; i < map.getLength(); i++) {
					Node a = map.item(i);
					if ("http://www.w3.org/2000/xmlns/".equals(a.getNamespaceURI())) continue;
					attrs.put((a.getNamespaceURI() == null ? "" : "{" + a.getNamespaceURI() + "}") + a.getLocalName(), a.getNodeValue());
				}
				out.add(attrs);
				names.add(c.getLocalName());
			}
			collect(c, out, names);
		}
	}

	@Test
	public void wordsVmlAttributesSurvive() throws Exception {
		List<Path> files = fixtures();
		assertTrue(files.size() >= 15);
		List<String> problems = new ArrayList<String>();
		int elements = 0, anchorIds = 0, gfxdata = 0;
		for (Path f : files) {
			// in preserve mode (CR-051): the default resolves a block-level mc:AlternateContent to the
			// branch Word reads, as Word's save does, which drops a VML Fallback there (mc-fun/mc-txbx-*);
			// this round trip is about reproducing the input
			WordprocessingMLPackage pkg = WordprocessingMLPackage.load(f.toFile(), org.docx4j.jaxb.McBlockLevel.PRESERVE);
			try (ZipFile zip = new ZipFile(f.toFile())) {
				for (Part part : pkg.getParts().getParts().values()) {
					if (!(part instanceof JaxbXmlPart) || !part.getPartName().getName().startsWith("/word/")) continue;
					ZipEntry entry = zip.getEntry(part.getPartName().getName().substring(1));
					if (entry == null) continue;
					String source = read(zip, entry);
					if (!source.contains("<v:")) continue;
					JaxbXmlPart<?> jp = (JaxbXmlPart<?>) part;
					String saved = XmlUtils.marshaltoString(jp.getJaxbElement(), true, true, jp.getJAXBContext());
					List<String> sourceNames = new ArrayList<String>(), savedNames = new ArrayList<String>();
					List<Map<String, String>> before = vml(source, sourceNames), after = vml(saved, savedNames);
					String where = f.getFileName() + " " + part.getPartName().getName();
					if (!sourceNames.equals(savedNames)) {
						problems.add(where + ": VML elements " + sourceNames + " became " + savedNames);
						continue;
					}
					for (int i = 0; i < before.size(); i++) {
						elements++;
						for (Map.Entry<String, String> a : before.get(i).entrySet()) {
							String name = a.getKey(), value = a.getValue(), kept = after.get(i).get(name);
							if (name.equals("{" + W14 + "}anchorId")) anchorIds++;
							if (name.equals("{" + O + "}gfxdata")) gfxdata++;
							if (kept == null) problems.add(where + ": v:" + sourceNames.get(i) + " lost " + name);
							else if (!kept.equals(value) && !sameValue(name, value, kept))
								problems.add(where + ": v:" + sourceNames.get(i) + " " + name + " '" + value + "' became '" + kept + "'");
						}
					}
				}
			}
		}
		assertTrue(problems.size() + " problems over " + elements + " VML elements:\n" + String.join("\n", problems), problems.isEmpty());
		assertTrue("anchorIds checked: " + anchorIds, anchorIds >= 46);
		assertTrue("gfxdata checked: " + gfxdata, gfxdata >= 51);
	}

	/** The rewrites CR-048's D3 accepts: o:spt as a float, o:gfxdata's base64 layout. */
	private static boolean sameValue(String name, String value, String kept) {
		if (name.equals("{" + O + "}spt")) return Float.parseFloat(value) == Float.parseFloat(kept);
		if (name.equals("{" + O + "}gfxdata")) return Arrays.equals(Base64.getMimeDecoder().decode(value), Base64.getMimeDecoder().decode(kept));
		return false;
	}

	/** The cases CR-048 names: loadAndSave's shapetype, the regenerated Fallbacks' shapes, the wps rect. */
	@Test
	public void namedCases() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(resources().resolve("loadAndSave.docx").toFile());
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, true);
		assertTrue(xml, xml.contains("w14:anchorId=\"7969C50B\""));

		String resave = XmlUtils.marshaltoString(WordprocessingMLPackage.load(resources().resolve("text-extraction/extraction-probe.word-resave.docx").toFile())
				.getMainDocumentPart().getJaxbElement(), true, true);
		List<String> names = new ArrayList<String>();
		int shapes = 0;
		for (Map<String, String> attrs : vml(resave, names)) if (attrs.containsKey("{" + W14 + "}anchorId")) shapes++;
		assertEquals(9, shapes); // eight regenerated v:shape and the text-box v:shapetype

		String wps = XmlUtils.marshaltoString(WordprocessingMLPackage.load(resources().resolve("2010/DrawingML_GraphicData_wps.docx").toFile())
				.getMainDocumentPart().getJaxbElement(), true, true);
		names.clear();
		boolean rect = false;
		List<Map<String, String>> elements = vml(wps, names);
		for (int i = 0; i < elements.size(); i++) {
			if (names.get(i).equals("rect")) rect = elements.get(i).containsKey("{" + W14 + "}anchorId") && elements.get(i).containsKey("{" + O + "}gfxdata");
		}
		assertTrue(rect);
	}

	/** [MS-DOCX] 2.2.6: a saved part carrying a VML w14:anchorId declares w14 and lists it in mc:Ignorable. */
	@Test
	public void savedPartsListW14Ignorable() throws Exception {
		check("loadAndSave.docx", "word/document.xml");
		check("numPicBullet-word365-AlternateContent.docx", "word/numbering.xml");
	}

	private static void check(String fixture, String partName) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(resources().resolve(fixture).toFile());
		for (Part part : pkg.getParts().getParts().values()) if (part instanceof JaxbXmlPart) ((JaxbXmlPart<?>) part).getJaxbElement();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pkg.save(out);
		String xml = null;
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()))) {
			for (ZipEntry e; (e = zip.getNextEntry()) != null; ) if (e.getName().equals(partName)) xml = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
		}
		assertTrue(fixture + " " + partName + " saved", xml != null);
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setNamespaceAware(true);
		Element root = f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))).getDocumentElement();
		assertEquals(fixture, W14, root.lookupNamespaceURI("w14"));
		String ignorable = root.getAttributeNS("http://schemas.openxmlformats.org/markup-compatibility/2006", "Ignorable");
		assertTrue(fixture + ": mc:Ignorable=" + ignorable, Arrays.asList(ignorable.trim().split("\\s+")).contains("w14"));
		List<String> names = new ArrayList<String>();
		boolean kept = false;
		for (Map<String, String> attrs : vml(xml, names)) if (attrs.containsKey("{" + W14 + "}anchorId")) kept = true;
		assertTrue(fixture + " " + partName + " keeps a VML w14:anchorId", kept);
	}
}
