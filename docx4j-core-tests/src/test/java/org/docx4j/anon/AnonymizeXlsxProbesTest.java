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
package org.docx4j.anon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import org.docx4j.XmlUtils;
import org.docx4j.anon.AnonymizeResult.Action;
import org.docx4j.anon.AnonymizeResult.PartAction;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.DefaultXmlPart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.CommentsPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.ConnectionsPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.ExternalLinkPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.QueryTablePart;
import org.docx4j.openpackaging.parts.SpreadsheetML.SharedStrings;
import org.docx4j.openpackaging.parts.SpreadsheetML.TablePart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorkbookPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EmbeddedPackagePart;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafileEmfPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.junit.Test;
import org.xlsx4j.jaxb.Context;
import org.xlsx4j.sml.CTComments;
import org.xlsx4j.sml.CTConnections;
import org.xlsx4j.sml.CTExternalLink;
import org.xlsx4j.sml.CTQueryTable;
import org.xlsx4j.sml.CTSst;
import org.xlsx4j.sml.CTTable;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.Workbook;
import org.xlsx4j.sml.Worksheet;

/**
 * The xlsx probes of CR-019 phase 3: one generated workbook per gap (built here,
 * never a customer file), each planted with words and numbers that must not
 * survive. The generic check is {@link #allXml}; the specific checks are the
 * structure the tool promises to keep: formulas that still compute, a table
 * whose columns still match its header cells, sheet names every formula agrees
 * on.
 */
public class AnonymizeXlsxProbesTest {

	static final String X = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
	static final String R = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
	static final String NS = "xmlns=\"" + X + "\" xmlns:r=\"" + R + "\"";

	// ---- helpers

	static Object sml(String xml, Class<?> type) throws Exception {
		return XmlUtils.unwrap(XmlUtils.unmarshalString(xml, Context.jcSML, type));
	}

	static SpreadsheetMLPackage book(String... sheetNames) throws Exception {
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.createPackage();
		for (int i = 0; i < sheetNames.length; i++) {
			pkg.createWorksheetPart(new PartName("/xl/worksheets/sheet" + (i + 1) + ".xml"), sheetNames[i], i + 1);
		}
		return pkg;
	}

	static WorksheetPart sheet(SpreadsheetMLPackage pkg, int n) throws Exception {
		return (WorksheetPart) pkg.getParts().get(new PartName("/xl/worksheets/sheet" + n + ".xml"));
	}

	static Part part(OpcPackage pkg, String name) throws Exception {
		return pkg.getParts().get(new PartName(name));
	}

	/** the shared strings part, its entries as given (index = position) */
	static SharedStrings sharedStrings(SpreadsheetMLPackage pkg, String... entries) throws Exception {
		StringBuilder sb = new StringBuilder("<sst " + NS + ">");
		for (String e : entries) sb.append(e.startsWith("<si>") ? e : "<si><t xml:space=\"preserve\">" + e + "</t></si>");
		sb.append("</sst>");
		SharedStrings sst = new SharedStrings(new PartName("/xl/sharedStrings.xml"));
		sst.setContents((CTSst) sml(sb.toString(), CTSst.class));
		pkg.getWorkbookPart().addTargetPart(sst);
		return sst;
	}

	/** a row of cells from "r|t|v|f" specs (t and f may be empty) */
	static Row row(int r, String... cells) throws Exception {
		StringBuilder sb = new StringBuilder("<row " + NS + " r=\"" + r + "\">");
		for (String spec : cells) {
			String[] p = spec.split("\\|", -1);
			sb.append("<c r=\"").append(p[0]).append("\"");
			if (!p[1].isEmpty()) sb.append(" t=\"").append(p[1]).append("\"");
			sb.append(">");
			if (p.length > 3 && !p[3].isEmpty()) sb.append("<f>").append(p[3].replace("&", "&amp;").replace("<", "&lt;")).append("</f>");
			if (!p[2].isEmpty()) sb.append("<v>").append(p[2]).append("</v>");
			sb.append("</c>");
		}
		sb.append("</row>");
		return (Row) sml(sb.toString(), Row.class);
	}

	static Cell cell(WorksheetPart ws, String ref) throws Exception {
		for (Row r : ws.getContents().getSheetData().getRow()) {
			for (Cell c : r.getC()) if (ref.equals(c.getR())) return c;
		}
		return null;
	}

	static Relationship externalRel(Part source, String type, String target) {
		Relationship rel = new org.docx4j.relationships.ObjectFactory().createRelationship();
		rel.setType(type);
		rel.setTarget(target);
		rel.setTargetMode("External");
		source.getRelationshipsPart(true).addRelationship(rel);
		return rel;
	}

	/** a real VMLPart (since CR-026 the binding reads a vmlDrawing's namespace-less &lt;xml&gt; root) */
	static org.docx4j.openpackaging.parts.VMLPart vmlPart(String name, String xml) throws Exception {
		org.docx4j.openpackaging.parts.VMLPart p = new org.docx4j.openpackaging.parts.VMLPart(new PartName(name));
		p.setJaxbElement((org.docx4j.vml.root.Xml) XmlUtils.unwrap(XmlUtils.unmarshalString(xml, org.docx4j.jaxb.Context.jc, org.docx4j.vml.root.Xml.class)));
		return p;
	}

	static DefaultXmlPart xmlPart(String name, String contentType, String relType, String xml) throws Exception {
		DefaultXmlPart p = new DefaultXmlPart(new PartName(name));
		p.setContentType(new ContentType(contentType));
		p.setRelationshipType(relType);
		p.setDocument(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
		return p;
	}

	/** Every XML part and relationships part of the package, as one string. */
	static String allXml(OpcPackage pkg) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(pkg.getRelationshipsPart().getXML());
		for (Part p : pkg.getParts().getParts().values()) {
			if (p instanceof JaxbXmlPart) {
				Object c = ((JaxbXmlPart<?>) p).getContents();
				if (c != null) sb.append(((JaxbXmlPart<?>) p).getXML());
			} else if (p instanceof XmlPart) {
				sb.append(XmlUtils.w3CDomNodeToString(((XmlPart) p).getDocument()));
			}
			RelationshipsPart rp = p.getRelationshipsPart();
			if (rp != null && rp.getRelationships() != null) sb.append(rp.getXML());
		}
		return sb.toString();
	}

	static void assertGone(String all, String... words) {
		String lower = all.toLowerCase(Locale.ROOT);
		for (String w : words) {
			assertFalse("'" + w + "' survived", lower.contains(w.toLowerCase(Locale.ROOT)));
		}
	}

	static void assertClean(AnonymizeResult r) {
		assertTrue(r.summary(), r.isClean());
		assertEquals("verified: " + r.getLeaks(), Boolean.TRUE, r.getVerified());
	}

	static PartAction action(AnonymizeResult r, String partName) {
		return AnonymizeCorpusTest.action(r, partName);
	}

	static SpreadsheetMLPackage reload(SpreadsheetMLPackage pkg) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		pkg.save(baos);
		return (SpreadsheetMLPackage) OpcPackage.load(new ByteArrayInputStream(baos.toByteArray()));
	}

	// ---- gap: text, numbers, formulas, names, protection

	static SpreadsheetMLPackage dataBook() throws Exception {
		SpreadsheetMLPackage pkg = book("Sales Data", "Summary 2024");
		WorkbookPart wb = pkg.getWorkbookPart();
		WorksheetPart s1 = sheet(pkg, 1), s2 = sheet(pkg, 2);

		// shared strings: headers, values, one split across rich-text runs
		sharedStrings(pkg,
				"Customer", "Amount", "Region",                          // 0-2
				"Zorbling Corp", "North Quibblex",                        // 3-4
				"<si><r><t>Zorb</t></r><r><rPr><b/></rPr><t>ling Ltd</t></r></si>", // 5
				"Grendlewick");                                           // 6

		List<Row> rows = s1.getContents().getSheetData().getRow();
		rows.add(row(1, "A1|s|0", "B1|s|1", "C1|s|2"));
		rows.add(row(2, "A2|s|3", "B2||1234.5", "C2|s|4", "D2|str|Yes|IF(A2=\"Zorbling Corp\",\"Yes\",\"No\")"));
		rows.add(row(3, "A3|s|5", "B3||42", "C3|s|6", "D3||1357.95|SUM(Sales[Amount])"));
		rows.add(row(4, "A4|b|1", "B4|e|#N/A", "C4||0", "D4||2.5|'Summary 2024'!B2*1.1+VLOOKUP(A2,Rates,2,FALSE)"));
		rows.add(row(5, "A5||1E+20", "B5||-0.75"));
		s2.getContents().getSheetData().getRow().add(row(2, "B2||99"));

		// a table over the data, a calculated column
		TablePart table = new TablePart(new PartName("/xl/tables/table1.xml"));
		table.setContents((CTTable) sml("<table " + NS + " id=\"1\" name=\"Sales\" displayName=\"Sales\" ref=\"A1:D3\" totalsRowShown=\"0\" comment=\"Zorbling sales\">"
				+ "<autoFilter ref=\"A1:D3\"/>"
				+ "<tableColumns count=\"4\"><tableColumn id=\"1\" name=\"Customer\"/><tableColumn id=\"2\" name=\"Amount\"/>"
				+ "<tableColumn id=\"3\" name=\"Region\"/><tableColumn id=\"4\" name=\"Flag\"><calculatedColumnFormula>IF([Amount]&gt;100,\"big\",\"small\")</calculatedColumnFormula></tableColumn></tableColumns>"
				+ "<tableStyleInfo name=\"TableStyleMedium2\" showRowStripes=\"1\"/></table>", CTTable.class));
		s1.addTargetPart(table);

		// defined names, one built in
		Workbook w = wb.getContents();
		w.setDefinedNames((org.xlsx4j.sml.DefinedNames) sml("<definedNames " + NS + ">"
				+ "<definedName name=\"Rates\" comment=\"Zorbling rates\">'Sales Data'!$A$2:$B$3</definedName>"
				+ "<definedName name=\"_xlnm.Print_Area\" localSheetId=\"0\">'Sales Data'!$A$1:$D$5</definedName>"
				+ "</definedNames>", org.xlsx4j.sml.DefinedNames.class));

		// hyperlinks: one out, one within the book; a validation; a conditional format; header and footer
		Worksheet ws = s1.getContents();
		Relationship link = externalRel(s1, Namespaces.HYPERLINK, "https://zorbling.example.com/secret");
		ws.setHyperlinks((org.xlsx4j.sml.CTHyperlinks) sml("<hyperlinks " + NS + ">"
				+ "<hyperlink ref=\"A2\" r:id=\"" + link.getId() + "\" display=\"Visit Zorbling\" tooltip=\"Zorbling site\"/>"
				+ "<hyperlink ref=\"A3\" location=\"'Summary 2024'!A1\" display=\"Go to summary\"/></hyperlinks>", org.xlsx4j.sml.CTHyperlinks.class));
		ws.setDataValidations((org.xlsx4j.sml.CTDataValidations) sml("<dataValidations " + NS + " count=\"1\">"
				+ "<dataValidation type=\"list\" sqref=\"C2:C3\" showErrorMessage=\"1\" errorTitle=\"Wrong region\" error=\"Pick a Quibblex region\" promptTitle=\"Region\" prompt=\"Enter the Quibblex region\">"
				+ "<formula1>\"North Quibblex,South Quibblex\"</formula1></dataValidation></dataValidations>", org.xlsx4j.sml.CTDataValidations.class));
		ws.getConditionalFormatting().add((org.xlsx4j.sml.CTConditionalFormatting) sml("<conditionalFormatting " + NS + " sqref=\"A2:A3\">"
				+ "<cfRule type=\"containsText\" priority=\"1\" operator=\"containsText\" text=\"Zorbling\">"
				+ "<formula>NOT(ISERROR(SEARCH(\"Zorbling\",A2)))</formula></cfRule></conditionalFormatting>", org.xlsx4j.sml.CTConditionalFormatting.class));
		ws.setHeaderFooter((org.xlsx4j.sml.CTHeaderFooter) sml("<headerFooter " + NS + "><oddHeader>&amp;L&amp;\"Arial,Bold\"Zorbling Corp&amp;CPage &amp;P of &amp;N&amp;R&amp;D</oddHeader>"
				+ "<oddFooter>&amp;CConfidential Grendlewick</oddFooter></headerFooter>", org.xlsx4j.sml.CTHeaderFooter.class));

		// protection and file sharing
		ws.setSheetProtection((org.xlsx4j.sml.CTSheetProtection) sml("<sheetProtection " + NS + " algorithmName=\"SHA-512\" hashValue=\"c2VjcmV0aGFzaA==\" saltValue=\"c2FsdA==\" spinCount=\"100000\" sheet=\"1\" objects=\"1\"/>", org.xlsx4j.sml.CTSheetProtection.class));
		w.setWorkbookProtection((org.xlsx4j.sml.CTWorkbookProtection) sml("<workbookProtection " + NS + " workbookAlgorithmName=\"SHA-512\" workbookHashValue=\"d29ya2Jvb2toYXNo\" workbookSaltValue=\"c2FsdA==\" workbookSpinCount=\"100000\" lockStructure=\"1\"/>", org.xlsx4j.sml.CTWorkbookProtection.class));
		w.setFileSharing((org.xlsx4j.sml.CTFileSharing) sml("<fileSharing " + NS + " userName=\"Jane Reviewer\" readOnlyRecommended=\"1\"/>", org.xlsx4j.sml.CTFileSharing.class));
		return pkg;
	}

	@Test
	public void textNumbersFormulasAndNames() throws Exception {
		SpreadsheetMLPackage pkg = dataBook();
		WorksheetPart s1 = sheet(pkg, 1);
		TablePart table = (TablePart) part(pkg, "/xl/tables/table1.xml");

		AnonymizeResult r = new Anonymize(pkg).go();
		assertClean(r);
		String all = allXml(pkg);
		assertGone(all, "Zorbling", "Quibblex", "Grendlewick", "Customer", "Amount", "Region", "Sales Data", "Summary 2024",
				"Rates", "Visit", "zorbling.example.com", "Jane", "Reviewer", "Confidential", "c2VjcmV0aGFzaA", "d29ya2Jvb2toYXNo",
				"1234.5", "1357.95", "\"big\"", "\"small\"", "\"Yes\"", "\"No\"");

		// sheets are Sheet1 and Sheet2, and every formula agrees
		Workbook w = pkg.getWorkbookPart().getContents();
		assertEquals("Sheet1", w.getSheets().getSheet().get(0).getName());
		assertEquals("Sheet2", w.getSheets().getSheet().get(1).getName());
		String d4 = cell(s1, "D4").getF().getValue();
		assertTrue(d4, d4.matches("'Sheet2'!B2\\*[0-9]\\.[0-9]\\+VLOOKUP\\(A2,n_[0-9]+,[0-9],FALSE\\)"));
		String rates = w.getDefinedNames().getDefinedName().get(0).getName();
		assertTrue(rates, rates.startsWith("n_"));
		assertTrue(d4, d4.contains(rates));
		assertEquals("'Sheet1'!$A$2:$B$3", w.getDefinedNames().getDefinedName().get(0).getValue());
		assertEquals("_xlnm.Print_Area", w.getDefinedNames().getDefinedName().get(1).getName());
		assertEquals("'Sheet1'!$A$1:$D$5", w.getDefinedNames().getDefinedName().get(1).getValue());

		// the table's columns still equal the header cells, and the structured reference names them
		CTTable t = table.getContents();
		SharedStrings sst = pkg.getWorkbookPart().getSharedStrings();
		List<String> headers = new java.util.ArrayList<String>();
		for (int i = 0; i < 3; i++) headers.add(sst.getContents().getSi().get(i).getT().getValue());
		for (int i = 0; i < 3; i++) assertEquals(headers.get(i), t.getTableColumns().getTableColumn().get(i).getName());
		String d3 = cell(s1, "D3").getF().getValue();
		assertEquals("SUM(" + t.getDisplayName() + "[" + headers.get(1) + "])", d3);
		assertFalse(t.getDisplayName(), t.getDisplayName().contains(" "));
		String calc = t.getTableColumns().getTableColumn().get(3).getCalculatedColumnFormula().getValue();
		assertTrue(calc, calc.startsWith("IF([" + headers.get(1) + "]>"));
		assertEquals("TableStyleMedium2", t.getTableStyleInfo().getName());

		// numbers keep their shape; booleans and errors stay; the split string is one word
		assertTrue(cell(s1, "B2").getV(), cell(s1, "B2").getV().matches("[1-9][0-9]{3}\\.[0-9]"));
		assertTrue(cell(s1, "B3").getV(), cell(s1, "B3").getV().matches("[1-9][0-9]"));
		assertEquals("0", cell(s1, "C4").getV());
		assertTrue(cell(s1, "A5").getV(), cell(s1, "A5").getV().matches("[0-9]E\\+20"));
		assertTrue(cell(s1, "B5").getV(), cell(s1, "B5").getV().matches("-[0-9]\\.[0-9]{2}"));
		assertEquals("1", cell(s1, "A4").getV());
		assertEquals("#N/A", cell(s1, "B4").getV());
		org.xlsx4j.sml.CTRst split = sst.getContents().getSi().get(5);
		assertEquals(2, split.getR().size());
		assertEquals(4, split.getR().get(0).getT().getValue().length());

		// hyperlinks, validation, header codes, protection
		org.xlsx4j.sml.CTHyperlinks links = s1.getContents().getHyperlinks();
		assertNull(links.getHyperlink().get(0).getTooltip());
		assertEquals("'Sheet2'!A1", links.getHyperlink().get(1).getLocation());
		assertTrue(links.getHyperlink().get(0).getId() != null);
		assertEquals(1, r.getExternalTargetsReplaced());
		String header = s1.getContents().getHeaderFooter().getOddHeader();
		assertTrue(header, header.matches("&L&\"Arial,Bold\"[^&]+&C[^&]+&P[^&]+&N&R&D")); // the codes stay, the words go
		assertNull(s1.getContents().getSheetProtection().getHashValue());
		assertNull(s1.getContents().getSheetProtection().getSaltValue());
		assertEquals(Boolean.TRUE, s1.getContents().getSheetProtection().isSheet());
		assertNull(w.getWorkbookProtection().getWorkbookHashValue());
		assertEquals("Author 1", w.getFileSharing().getUserName());
		String f1 = s1.getContents().getDataValidations().getDataValidation().get(0).getFormula1();
		assertTrue(f1, f1.startsWith("\"") && f1.contains(","));
		reload(pkg);
	}

	@Test
	public void numbersKeptOnRequest() throws Exception {
		SpreadsheetMLPackage pkg = dataBook();
		Anonymize anon = new Anonymize(pkg);
		anon.setKeepNumbers(true);
		AnonymizeResult r = anon.go();
		assertClean(r);
		WorksheetPart s1 = sheet(pkg, 1);
		assertEquals("1234.5", cell(s1, "B2").getV());
		assertEquals("1357.95", cell(s1, "D3").getV());
		assertTrue(cell(s1, "D4").getF().getValue(), cell(s1, "D4").getF().getValue().startsWith("'Sheet2'!B2*1.1+"));
		assertGone(allXml(pkg), "Zorbling", "Customer");
	}

	// ---- gap: comments (legacy and threaded), the VML their shapes sit in

	@Test
	public void commentsLegacyAndThreaded() throws Exception {
		SpreadsheetMLPackage pkg = book("Notes");
		WorksheetPart s1 = sheet(pkg, 1);
		sharedStrings(pkg, "Zorbling total");
		s1.getContents().getSheetData().getRow().add(row(1, "A1|s|0", "B1||100"));

		CommentsPart comments = new CommentsPart(new PartName("/xl/comments1.xml"));
		comments.setContents((CTComments) sml("<comments " + NS + "><authors><author>Jane Reviewer</author><author>Bob Editor</author></authors>"
				+ "<commentList><comment ref=\"A1\" authorId=\"0\"><text><r><rPr><b/><sz val=\"9\"/></rPr><t>Jane Reviewer:</t></r><r><t xml:space=\"preserve\">\nCheck the Quibblex figure</t></r></text></comment>"
				+ "<comment ref=\"B1\" authorId=\"1\"><text><t>Bob says: Grendlewick</t></text></comment></commentList></comments>", CTComments.class));
		s1.addTargetPart(comments);

		// the comment shapes, in the legacy VML drawing part
		org.docx4j.openpackaging.parts.VMLPart vml = vmlPart("/xl/drawings/vmlDrawing1.vml",
				"<xml xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\">"
				+ "<o:shapelayout v:ext=\"edit\"><o:idmap v:ext=\"edit\" data=\"1\"/></o:shapelayout>"
				+ "<v:shapetype id=\"_x0000_t202\" coordsize=\"21600,21600\" o:spt=\"202\" path=\"m,l,21600r21600,l21600,xe\"><v:stroke joinstyle=\"miter\"/><v:path gradientshapeok=\"t\" o:connecttype=\"rect\"/></v:shapetype>"
				+ "<v:shape id=\"_x0000_s1025\" type=\"#_x0000_t202\" style=\"position:absolute;margin-left:59pt;margin-top:1pt;width:108pt;height:59pt;z-index:1;visibility:hidden\" fillcolor=\"#ffffe1\" o:insetmode=\"auto\" alt=\"Zorbling note\">"
				+ "<v:fill color2=\"#ffffe1\"/><v:shadow on=\"t\" color=\"black\" obscured=\"t\"/><v:path o:connecttype=\"none\"/>"
				+ "<v:textbox style=\"mso-direction-alt:auto\"><div style=\"text-align:left\">Quibblex preview</div></v:textbox>"
				+ "<x:ClientData ObjectType=\"Note\"><x:MoveWithCells/><x:SizeWithCells/><x:Anchor>1, 15, 0, 2, 3, 15, 4, 2</x:Anchor><x:AutoFill>False</x:AutoFill><x:Row>0</x:Row><x:Column>0</x:Column></x:ClientData>"
				+ "</v:shape></xml>");
		Relationship vmlRel = s1.addTargetPart(vml);
		s1.getContents().setLegacyDrawing((org.xlsx4j.sml.CTLegacyDrawing) sml("<legacyDrawing " + NS + " r:id=\"" + vmlRel.getId() + "\"/>", org.xlsx4j.sml.CTLegacyDrawing.class));

		// threaded comments and persons: not bound, DOM parts
		String tc = "http://schemas.microsoft.com/office/spreadsheetml/2018/threadedcomments";
		DefaultXmlPart persons = xmlPart("/xl/persons/person.xml", ContentTypes.SPREADSHEETML_PERSONS,
				"http://schemas.microsoft.com/office/2017/10/relationships/person",
				"<personList xmlns=\"" + tc + "\"><person displayName=\"Bob Editor\" id=\"{CCAE90FC-5FCA-9E45-894F-854181BF0476}\" userId=\"S::bob.editor@zorbling.example::e3c6b3cb\" providerId=\"AD\"/></personList>");
		pkg.getWorkbookPart().addTargetPart(persons);
		DefaultXmlPart threaded = xmlPart("/xl/threadedComments/threadedComment1.xml", ContentTypes.SPREADSHEETML_THREADED_COMMENTS,
				"http://schemas.microsoft.com/office/2017/10/relationships/threadedComment",
				"<ThreadedComments xmlns=\"" + tc + "\"><threadedComment ref=\"B1\" dT=\"2026-05-18T23:01:55.03\" personId=\"{CCAE90FC-5FCA-9E45-894F-854181BF0476}\" id=\"{F3B2BDD4-661F-B54E-B2DF-E968B40A7F95}\">"
				+ "<text>Threaded: Grendlewick needs review</text></threadedComment></ThreadedComments>");
		s1.addTargetPart(threaded);

		AnonymizeResult r = new Anonymize(pkg).go();
		assertClean(r);
		String all = allXml(pkg);
		assertGone(all, "Zorbling", "Quibblex", "Grendlewick", "Jane", "Reviewer", "Bob", "Editor", "zorbling.example", "2026-05");
		assertEquals(Action.SCRUBBED, action(r, "/xl/comments1.xml").action);
		assertEquals(Action.SCRUBBED, action(r, "/xl/drawings/vmlDrawing1.vml").action);
		String vmlXml = vml.getXML();
		assertTrue("the shape's text box was scrambled, not dropped", vmlXml.contains("<div"));
		assertTrue("its anchor is structure", vmlXml.contains("1, 15, 0, 2, 3, 15, 4, 2"));
		assertEquals(Action.SCRUBBED, action(r, "/xl/persons/person.xml").action);
		assertEquals(Action.SCRUBBED, action(r, "/xl/threadedComments/threadedComment1.xml").action);
		List<String> authors = comments.getContents().getAuthors().getAuthor();
		assertTrue(authors.toString(), authors.get(0).matches("Author [12]") && authors.get(1).matches("Author [12]"));
		String personsXml = XmlUtils.w3CDomNodeToString(persons.getDocument());
		assertTrue(personsXml, personsXml.contains("providerId=\"None\""));
		assertTrue(personsXml, personsXml.matches("(?s).*userId=\"Author [12]\".*"));
		assertTrue(vmlXml, vmlXml.contains("ObjectType=\"Note\""));
		assertEquals(2, r.getAuthorsRenamed());
		reload(pkg);
	}

	// ---- gap: connections, query tables, external links

	@Test
	public void connectionsQueryTablesAndExternalLinks() throws Exception {
		SpreadsheetMLPackage pkg = book("Data");
		WorksheetPart s1 = sheet(pkg, 1);
		WorkbookPart wb = pkg.getWorkbookPart();
		s1.getContents().getSheetData().getRow().add(row(1, "A1||5|'[1]Budget Zorbling'!A1+[1]Other!B2", "B1||7|Rates_Zorbling"));

		ConnectionsPart connections = new ConnectionsPart(new PartName("/xl/connections.xml"));
		connections.setContents((CTConnections) sml("<connections " + NS + "><connection id=\"1\" name=\"Zorbling CRM\" description=\"Customers of Zorbling\" type=\"1\" refreshedVersion=\"6\" saveData=\"1\" sourceFile=\"C:\\\\Zorbling\\\\crm.odc\">"
				+ "<dbPr connection=\"Provider=SQLOLEDB.1;Password=hunter2;User ID=sa;Data Source=zorbling-db\" command=\"SELECT Name, Salary FROM Staff\"/>"
				+ "<parameters count=\"1\"><parameter name=\"Region\" sqlType=\"12\" parameterType=\"prompt\" prompt=\"Which Quibblex region?\"/></parameters></connection>"
				+ "<connection id=\"2\" name=\"Zorbling web\" type=\"4\" refreshedVersion=\"6\"><webPr url=\"https://zorbling.example.com/prices\" htmlTables=\"1\"/></connection></connections>", CTConnections.class));
		wb.addTargetPart(connections);

		QueryTablePart qt = new QueryTablePart(new PartName("/xl/queryTables/queryTable1.xml"));
		qt.setContents((CTQueryTable) sml("<queryTable " + NS + " name=\"Zorbling_CRM\" connectionId=\"1\" autoFormatId=\"16\" applyNumberFormats=\"0\" applyBorderFormats=\"0\" applyFontFormats=\"0\" applyPatternFormats=\"0\" applyAlignmentFormats=\"0\" applyWidthHeightFormats=\"0\">"
				+ "<queryTableRefresh nextId=\"3\"><queryTableFields count=\"2\"><queryTableField id=\"1\" name=\"Name\" tableColumnId=\"1\"/><queryTableField id=\"2\" name=\"Salary\" tableColumnId=\"2\"/></queryTableFields></queryTableRefresh></queryTable>", CTQueryTable.class));
		s1.addTargetPart(qt);

		ExternalLinkPart ext = new ExternalLinkPart(new PartName("/xl/externalLinks/externalLink1.xml"));
		Relationship extRel = wb.addTargetPart(ext);
		Relationship book = externalRel(ext, "http://schemas.openxmlformats.org/officeDocument/2006/relationships/externalLinkPath", "file:///C:/Zorbling/Budget%20Secret.xlsx");
		ext.setContents((CTExternalLink) sml("<externalLink " + NS + "><externalBook r:id=\"" + book.getId() + "\">"
				+ "<sheetNames><sheetName val=\"Budget Zorbling\"/><sheetName val=\"Other\"/></sheetNames>"
				+ "<definedNames><definedName name=\"Rates_Zorbling\" refersTo=\"='Budget Zorbling'!$B$2\"/></definedNames>"
				+ "<sheetDataSet><sheetData sheetId=\"0\"><row r=\"1\"><cell r=\"A1\"><v>12345</v></cell><cell r=\"B1\" t=\"str\"><v>Grendlewick</v></cell></row></sheetData></sheetDataSet>"
				+ "</externalBook></externalLink>", CTExternalLink.class));
		wb.getContents().setExternalReferences((org.xlsx4j.sml.CTExternalReferences) sml("<externalReferences " + NS + "><externalReference r:id=\"" + extRel.getId() + "\"/></externalReferences>", org.xlsx4j.sml.CTExternalReferences.class));

		AnonymizeResult r = new Anonymize(pkg).go();
		assertClean(r);
		String all = allXml(pkg);
		assertGone(all, "Zorbling", "Quibblex", "Grendlewick", "hunter2", "SQLOLEDB", "zorbling-db", "SELECT Name", "Salary", "crm.odc",
				"zorbling.example.com", "Budget", "Secret", "12345");

		// the parts stay, with the structure a connection bug lives in; the external sheet names
		// map the same way in the link part and in the formula which uses them
		assertEquals(Action.SCRUBBED, action(r, "/xl/connections.xml").action);
		assertEquals(Action.SCRUBBED, action(r, "/xl/queryTables/queryTable1.xml").action);
		assertEquals(Action.SCRUBBED, action(r, "/xl/externalLinks/externalLink1.xml").action);
		assertEquals("Provider=None", connections.getContents().getConnection().get(0).getDbPr().getConnection());
		assertTrue(book.getTarget(), book.getTarget().startsWith(MetadataScrubber.EXTERNAL_PLACEHOLDER));
		String sheetName = ext.getContents().getExternalBook().getSheetNames().getSheetName().get(0).getVal();
		String a1 = cell(s1, "A1").getF().getValue();
		assertTrue(a1, a1.startsWith("'[1]" + sheetName + "'!A1+[1]"));
		String other = ext.getContents().getExternalBook().getSheetNames().getSheetName().get(1).getVal();
		assertTrue(a1, a1.endsWith("[1]" + other + "!B2"));
		String extName = ext.getContents().getExternalBook().getDefinedNames().getDefinedName().get(0).getName();
		assertTrue(extName, extName.startsWith("n_"));
		assertEquals(extName, cell(s1, "B1").getF().getValue());
		assertEquals(1, ext.getContents().getExternalBook().getSheetDataSet().getSheetData().size());
		reload(pkg);
	}

	// ---- gap: objects - OLE in STRICT and KEEP

	static SpreadsheetMLPackage oleBook() throws Exception {
		SpreadsheetMLPackage pkg = book("Objects");
		WorksheetPart s1 = sheet(pkg, 1);
		s1.getContents().getSheetData().getRow().add(row(1, "A1||1"));

		EmbeddedPackagePart docx = new EmbeddedPackagePart(new PartName("/xl/embeddings/Microsoft_Word_Document.docx"));
		docx.setContentType(new ContentType(ContentTypes.WORDPROCESSINGML_DOCUMENT));
		docx.setBinaryData(AnonymizeProbesTest.resource("anon/chart.docx"));
		Relationship docxRel = s1.addTargetPart(docx);
		MetafileEmfPart preview = new MetafileEmfPart(new PartName("/xl/media/image1.emf"));
		preview.setBinaryData(AnonymizeProbesTest.resource("anon/probe.emf"));
		Relationship previewRel = s1.addTargetPart(preview);

		// the VML shape of the object, whose picture is the preview
		org.docx4j.openpackaging.parts.VMLPart vml =
				new org.docx4j.openpackaging.parts.VMLPart(new PartName("/xl/drawings/vmlDrawing1.vml"));
		Relationship vmlRel = s1.addTargetPart(vml);
		Relationship vmlImage = vml.addTargetPart(preview); // the image relationship belongs to the drawing
		vml.setJaxbElement((org.docx4j.vml.root.Xml) XmlUtils.unwrap(XmlUtils.unmarshalString(
				"<xml xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\">"
				+ "<v:shapetype id=\"_x0000_t75\" coordsize=\"21600,21600\" o:spt=\"75\" o:preferrelative=\"t\" path=\"m@4@5l@4@11@9@11@9@5xe\" filled=\"f\" stroked=\"f\"/>"
				+ "<v:shape id=\"_x0000_s1025\" type=\"#_x0000_t75\" style=\"position:absolute;margin-left:100pt;margin-top:10pt;width:200pt;height:100pt;z-index:1\">"
				+ "<v:imagedata o:relid=\"" + vmlImage.getId() + "\" o:title=\"Zorbling contract\"/>"
				+ "<x:ClientData ObjectType=\"Pict\"><x:SizeWithCells/><x:Anchor>1, 10, 0, 5, 5, 10, 8, 5</x:Anchor><x:CF>Pict</x:CF><x:AutoPict/></x:ClientData></v:shape></xml>",
				org.docx4j.jaxb.Context.jc, org.docx4j.vml.root.Xml.class)));
		s1.getContents().setLegacyDrawing((org.xlsx4j.sml.CTLegacyDrawing) sml("<legacyDrawing " + NS + " r:id=\"" + vmlRel.getId() + "\"/>", org.xlsx4j.sml.CTLegacyDrawing.class));
		s1.getContents().setOleObjects((org.xlsx4j.sml.CTOleObjects) sml("<oleObjects " + NS + "><oleObject progId=\"Word.Document.12\" shapeId=\"1025\" r:id=\"" + docxRel.getId() + "\">"
				+ "<objectPr defaultSize=\"0\" altText=\"Zorbling contract\" r:id=\"" + previewRel.getId() + "\"><anchor moveWithCells=\"1\"><from><xdr:col xmlns:xdr=\"http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing\">1</xdr:col><xdr:colOff xmlns:xdr=\"http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing\">0</xdr:colOff><xdr:row xmlns:xdr=\"http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing\">0</xdr:row><xdr:rowOff xmlns:xdr=\"http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing\">0</xdr:rowOff></from>"
				+ "<to><xdr:col xmlns:xdr=\"http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing\">5</xdr:col><xdr:colOff xmlns:xdr=\"http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing\">0</xdr:colOff><xdr:row xmlns:xdr=\"http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing\">8</xdr:row><xdr:rowOff xmlns:xdr=\"http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing\">0</xdr:rowOff></to></anchor></objectPr>"
				+ "</oleObject></oleObjects>", org.xlsx4j.sml.CTOleObjects.class));
		return pkg;
	}

	@Test
	public void oleStrict() throws Exception {
		SpreadsheetMLPackage pkg = oleBook();
		WorksheetPart s1 = sheet(pkg, 1);
		AnonymizeResult r = new Anonymize(pkg).go();
		assertClean(r);
		String all = allXml(pkg);
		assertGone(all, "Zorbling", "oleObject", "embeddings", "image1.emf");
		assertNull(part(pkg, "/xl/embeddings/Microsoft_Word_Document.docx"));
		assertNull(part(pkg, "/xl/media/image1.emf"));
		assertNotNull("the VML shape stays", part(pkg, "/xl/drawings/vmlDrawing1.vml"));
		assertNotNull("its picture is the labelled placeholder, under /xl/media", part(pkg, MediaReplacer.objectPlaceholderPartName(pkg)));
		assertNull(s1.getContents().getOleObjects());
		// no relationship of the sheet or the drawing dangles
		for (Part p : new Part[] { s1, part(pkg, "/xl/drawings/vmlDrawing1.vml") }) {
			for (Relationship rel : p.getRelationshipsPart().getRelationships().getRelationship()) {
				if ("External".equals(rel.getTargetMode())) continue;
				assertNotNull(p.getPartName() + " -> " + rel.getTarget() + " dangles", p.getRelationshipsPart().getPart(rel));
			}
		}
		reload(pkg);
	}

	@Test
	public void oleKeep() throws Exception {
		SpreadsheetMLPackage pkg = oleBook();
		AnonymizeResult r = new Anonymize(pkg, Anonymize.Mode.KEEP).go();
		assertFalse(r.isClean());
		assertNotNull(part(pkg, "/xl/embeddings/Microsoft_Word_Document.docx"));
		assertNotNull(sheet(pkg, 1).getContents().getOleObjects());
		boolean embedding = false;
		for (PartAction a : r.getKeptUnsafe()) if (a.partName.startsWith("/xl/embeddings/")) embedding = true;
		assertTrue(r.getKeptUnsafe().toString(), embedding);
		assertGone(sheet(pkg, 1).getXML(), "Zorbling"); // the alt text was still scrambled
		reload(pkg);
	}

	// ---- the formula scrubber on its own

	@Test
	public void formulas() throws Exception {
		SpreadsheetMLPackage pkg = book("Sales Data", "Other");
		Names names = new Names(pkg);
		ScrambleText scrambler = new ScrambleText(pkg, names);
		SmlFormulas f = new SmlFormulas(scrambler, names);

		assertEquals("SUM(A1:B2)", f.scrub("SUM(A1:B2)"));
		assertEquals("'Sheet1'!$A$1", f.scrub("'Sales Data'!$A$1"));
		assertEquals("Sheet2!A1:A10", f.scrub("Other!A1:A10"));
		assertEquals("_xlfn.XLOOKUP(A1,B:B,C:C)", f.scrub("_xlfn.XLOOKUP(A1,B:B,C:C)"));
		assertEquals("SUM(1:1)", f.scrub("SUM(1:1)"));
		assertEquals("TRUE", f.scrub("TRUE"));
		assertEquals("#REF!+#N/A", f.scrub("#REF!+#N/A"));
		assertEquals("R[-1]C+RC[1]", f.scrub("R[-1]C+RC[1]"));
		String s = f.scrub("IF(A1=\"Zorbling\",\"yes\",\"no\")");
		assertTrue(s, s.matches("IF\\(A1=\"[a-zA-Z ]{8}\",\"[a-zA-Z ]{3}\",\"[a-zA-Z ]{2}\"\\)")); // lorem has spaces
		assertFalse(s, s.contains("Zorbling"));
		String n = f.scrub("A1*1.25+100");
		assertTrue(n, n.matches("A1\\*[0-9]\\.[0-9]{2}\\+[1-9][0-9]{2}"));
		String d = f.scrub("Tax_Rate*A1");
		assertTrue(d, d.startsWith("n_") && d.endsWith("*A1"));
		assertEquals(Names.definedName("Tax_Rate") + "*A1", d);
		String t = f.scrub("SUM(Sales[[#This Row],[Amount]])");
		assertTrue(t, t.startsWith("SUM(" + scrambler.consistentIdentifier("Sales") + "[[#This Row],[" + scrambler.consistent("Amount") + "]])"));
		assertEquals("SUM(" + scrambler.consistentIdentifier("Sales") + "[#Headers])", f.scrub("SUM(Sales[#Headers])"));
		assertFalse(scrambler.consistentIdentifier("SimpleInvoiceTable"), scrambler.consistentIdentifier("SimpleInvoiceTable").contains(" "));
		assertEquals("[1]" + scrambler.consistent("Budget") + "!A1", f.scrub("[1]Budget!A1"));

		scrambler.setKeepNumbers(true);
		assertEquals("A1*1.25+100", f.scrub("A1*1.25+100"));
	}

	@Test
	public void verifyKnowsTheFormulaLanguage() {
		List<String> tokens = Verify.formulaTokens("IF(SUM(Sales[Amount])>ABC123,\"Zorbling\",'My Sheet'!A1)+#REF!");
		assertTrue(tokens.toString(), tokens.contains("zorbling"));
		assertTrue(tokens.toString(), tokens.contains("sales"));
		assertTrue(tokens.toString(), tokens.contains("amount"));
		assertTrue(tokens.toString(), tokens.contains("sheet"));
		assertFalse("a function", tokens.contains("sum"));
		assertFalse("a cell reference", tokens.contains("abc"));
		assertFalse("an error literal", tokens.contains("ref"));
		assertFalse("a structured keyword", Verify.formulaTokens("T[#Headers]").contains("headers"));
		assertEquals(java.util.Arrays.asList("zorbling", "corp", "page"), Verify.headerFooterTokens("&L&\"Arial,Bold\"Zorbling Corp&CPage &P&KFF0000&D"));
	}

}
