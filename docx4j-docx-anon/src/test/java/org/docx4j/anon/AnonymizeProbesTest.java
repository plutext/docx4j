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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.docx4j.XmlUtils;
import org.docx4j.anon.AnonymizeResult.Action;
import org.docx4j.anon.AnonymizeResult.PartAction;
import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.dml.CTTextBody;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.dml.diagram.CTCxnList;
import org.docx4j.dml.diagram.CTDataModel;
import org.docx4j.dml.diagram.CTPt;
import org.docx4j.dml.diagram.CTPtList;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.DrawingML.DiagramDataPart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsExtensiblePart;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageJpegPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafileEmfPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.OleObjectBinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.PeoplePart;
import org.docx4j.openpackaging.parts.WordprocessingML.VbaProjectBinaryPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.w15.CTPeople;
import org.docx4j.w15.CTPerson;
import org.docx4j.w15.CTPresenceInfo;
import org.docx4j.w16cex.CTCommentExtensible;
import org.docx4j.w16cex.CTCommentsExtensible;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTDataBinding;
import org.docx4j.wml.CTDocProtect;
import org.docx4j.wml.CTDocVar;
import org.docx4j.wml.CTDocVars;
import org.docx4j.wml.CTMailMerge;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.CTPPrChange;
import org.docx4j.wml.CTRPrChange;
import org.docx4j.wml.CTRel;
import org.docx4j.wml.CTSdtContentRun;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.Comments;
import org.docx4j.wml.DelText;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RunDel;
import org.docx4j.wml.RunIns;
import org.docx4j.wml.STDocProtect;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.SdtRun;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tag;
import org.docx4j.wml.Text;
import org.junit.Test;

/**
 * One generated document per gap of CR-019 (in the style of the fidelity
 * harness's probes: built here, never a customer file), each planted with
 * words that must not survive. The generic check is {@link #allXml}: every
 * XML part and relationships part of the output, searched for the planted
 * words; the specific checks are the structure the tool promises to keep.
 */
public class AnonymizeProbesTest {

	static final ObjectFactory F = new ObjectFactory();
	static final org.docx4j.relationships.ObjectFactory RF = new org.docx4j.relationships.ObjectFactory();

	// ---- helpers

	static XMLGregorianCalendar date(int year) throws Exception {
		GregorianCalendar gc = new GregorianCalendar(year, 5, 15, 10, 30, 0);
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
	}

	static P paragraph(String text) {
		P p = F.createP();
		p.getContent().add(run(text));
		return p;
	}

	static R run(String text) {
		R r = F.createR();
		Text t = F.createText();
		t.setValue(text);
		t.setSpace("preserve");
		r.getContent().add(F.createRT(t));
		return r;
	}

	static R instrRun(String instr) {
		R r = F.createR();
		Text t = F.createText();
		t.setValue(instr);
		t.setSpace("preserve");
		r.getContent().add(F.createRInstrText(t));
		return r;
	}

	static R fldCharRun(STFldCharType type) {
		R r = F.createR();
		FldChar fc = F.createFldChar();
		fc.setFldCharType(type);
		r.getContent().add(F.createRFldChar(fc));
		return r;
	}

	/** BEGIN, instruction, SEPARATE, result, END */
	static P field(String instr, String result) {
		P p = F.createP();
		p.getContent().add(fldCharRun(STFldCharType.BEGIN));
		p.getContent().add(instrRun(instr));
		p.getContent().add(fldCharRun(STFldCharType.SEPARATE));
		p.getContent().add(run(result));
		p.getContent().add(fldCharRun(STFldCharType.END));
		return p;
	}

	static Relationship externalRel(Base source, String type, String target) {
		Relationship rel = RF.createRelationship();
		rel.setType(type);
		rel.setTarget(target);
		rel.setTargetMode("External");
		source.getRelationshipsPart(true).addRelationship(rel);
		return rel;
	}

	/** Every XML part and relationships part of the package, as one string. */
	static String allXml(WordprocessingMLPackage pkg) throws Exception {
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

	static byte[] resource(String name) throws Exception {
		try (InputStream is = AnonymizeProbesTest.class.getResourceAsStream("/" + name)) {
			assertNotNull(name, is);
			return is.readAllBytes();
		}
	}

	// ---- gap 1: identity

	@Test
	public void identity() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();

		// a comment by Jane Reviewer
		CommentsPart cp = new CommentsPart();
		Comments comments = F.createComments();
		Comments.Comment c = F.createCommentsComment();
		c.setId(BigInteger.ONE);
		c.setAuthor("Jane Reviewer");
		c.setInitials("JR");
		c.setDate(date(2024));
		c.getContent().add(paragraph("Please check the Zorbling clause"));
		comments.getComment().add(c);
		cp.setContents(comments);
		mdp.addTargetPart(cp);

		P p = paragraph("The Zorbling clause applies.");
		R ref = F.createR();
		R.CommentReference cr = F.createRCommentReference();
		cr.setId(BigInteger.ONE);
		ref.getContent().add(cr);
		p.getContent().add(ref);

		// an insertion by Jane, a deletion by Bob Editor
		RunIns ins = F.createRunIns();
		ins.setId(BigInteger.valueOf(2));
		ins.setAuthor("Jane Reviewer");
		ins.setDate(date(2024));
		ins.getCustomXmlOrSmartTagOrSdt().add(run(" Inserted by Jane."));
		p.getContent().add(ins);

		RunDel del = F.createRunDel();
		del.setId(BigInteger.valueOf(3));
		del.setAuthor("Bob Editor");
		del.setDate(date(2023));
		R delRun = F.createR();
		DelText dt = F.createDelText();
		dt.setValue("Deleted by Bob.");
		delRun.getContent().add(dt);
		del.getCustomXmlOrSmartTagOrSdt().add(delRun);
		p.getContent().add(del);

		// a formatting change by Bob on a run, and on the paragraph
		R changed = run("Bolded by Bob.");
		RPr rpr = F.createRPr();
		CTRPrChange rc = F.createCTRPrChange();
		rc.setId(BigInteger.valueOf(4));
		rc.setAuthor("Bob Editor");
		rc.setDate(date(2023));
		rc.setRPr(new CTRPrChange.RPr());
		rpr.setRPrChange(rc);
		changed.setRPr(rpr);
		p.getContent().add(changed);

		PPr ppr = F.createPPr();
		CTPPrChange pc = F.createCTPPrChange();
		pc.setId(BigInteger.valueOf(5));
		pc.setAuthor("Jane Reviewer");
		pc.setDate(date(2024));
		pc.setPPr(F.createPPrBase());
		ppr.setPPrChange(pc);
		p.setPPr(ppr);
		mdp.getContent().add(p);

		// people.xml with presence
		org.docx4j.w15.ObjectFactory w15 = new org.docx4j.w15.ObjectFactory();
		CTPeople people = w15.createCTPeople();
		CTPerson person = w15.createCTPerson();
		person.setAuthor("Jane Reviewer");
		CTPresenceInfo pi = w15.createCTPresenceInfo();
		pi.setProviderId("AD");
		pi.setUserId("S::jane.reviewer@acme.example::1234-5678");
		person.setPresenceInfo(pi);
		people.getPerson().add(person);
		PeoplePart pp = new PeoplePart();
		pp.setContents(people);
		mdp.addTargetPart(pp);

		// commentsExtensible with a UTC date
		org.docx4j.w16cex.ObjectFactory cex = new org.docx4j.w16cex.ObjectFactory();
		CTCommentsExtensible ces = cex.createCTCommentsExtensible();
		CTCommentExtensible ce = cex.createCTCommentExtensible();
		ce.setDurableId("1A2B3C4D");
		ce.setDateUtc(date(2024));
		ces.getCommentExtensible().add(ce);
		CommentsExtensiblePart cep = new CommentsExtensiblePart();
		cep.setContents(ces);
		mdp.addTargetPart(cep);

		// docProps
		pkg.getDocPropsCorePart().getContents().setLastModifiedBy("Jane Reviewer");

		AnonymizeResult r = new Anonymize(pkg).go();
		assertClean(r);
		AnonymizeCorpusTest.reload(pkg);

		String all = allXml(pkg);
		assertGone(all, "Jane", "Reviewer", "Bob", "Editor", "Zorbling", "acme", "\"JR\"", "2024-", "2023-");

		// the review structure survives: two authors, consistently named
		assertEquals(2, r.getAuthorsRenamed());
		assertEquals("Author 1", c.getAuthor());
		assertEquals("Author 1", ins.getAuthor());
		assertEquals("Author 2", del.getAuthor());
		assertEquals("Author 2", rc.getAuthor());
		assertEquals("Author 1", pc.getAuthor());
		assertEquals("Author 1", person.getAuthor());
		assertEquals("Author 1", pi.getUserId());
		assertEquals("None", pi.getProviderId());
		assertEquals("A1", c.getInitials());
		assertEquals(Names.FIXED_DATE.toXMLFormat(), ce.getDateUtc().toXMLFormat());
		assertEquals(Names.FIXED_DATE.toXMLFormat(), del.getDate().toXMLFormat());
		assertNull(pkg.getDocPropsCorePart().getContents().getLastModifiedBy());
		// the comment still has text, and the deletion still has deleted text
		assertTrue(c.getContent().size() == 1);
		assertFalse(dt.getValue().isEmpty());
	}

	// ---- gap 2: external targets and field instructions

	@Test
	public void externalTargetsAndInstructions() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();

		// an external hyperlink with a tooltip
		Relationship rel = externalRel(mdp, Namespaces.HYPERLINK, "https://portal.acme.example/secret/deal");
		P p1 = F.createP();
		P.Hyperlink h = F.createPHyperlink();
		h.setId(rel.getId());
		h.setTooltip("Open the Acme deal room");
		h.getContent().add(run("the deal room"));
		p1.getContent().add(h);
		mdp.getContent().add(p1);

		// a bookmark, an internal hyperlink to it, a REF and a PAGEREF to it
		P p2 = F.createP();
		CTBookmark bs = F.createCTBookmark();
		bs.setId(BigInteger.ONE);
		bs.setName("SecretClause");
		p2.getContent().add(F.createBodyBookmarkStart(bs));
		p2.getContent().add(run("Clause 9 text"));
		CTMarkupRange be = F.createCTMarkupRange();
		be.setId(BigInteger.ONE);
		p2.getContent().add(F.createBodyBookmarkEnd(be));
		mdp.getContent().add(p2);

		P p3 = F.createP();
		P.Hyperlink internal = F.createPHyperlink();
		internal.setAnchor("SecretClause");
		internal.getContent().add(run("see the clause"));
		p3.getContent().add(internal);
		mdp.getContent().add(p3);

		mdp.getContent().add(field(" REF SecretClause \\h ", "Clause 9 text"));
		mdp.getContent().add(field(" PAGEREF SecretClause \\h ", "4"));

		// field instructions with arguments
		mdp.getContent().add(field(" HYPERLINK \"https://portal.acme.example/secret\" \\o \"Acme tip\" ", "link"));
		mdp.getContent().add(field(" INCLUDEPICTURE \"C:\\\\Users\\\\jane\\\\Pictures\\\\acme-logo.png\" \\* MERGEFORMAT \\d ", ""));
		mdp.getContent().add(field(" DOCPROPERTY Company \\* MERGEFORMAT ", "Acme Corp"));
		mdp.getContent().add(field(" MERGEFIELD ClientSecretName \\* Upper \\b \"Dear \" ", "«ClientSecretName»"));
		mdp.getContent().add(field(" DATE \\@ \"dddd, d MMMM yyyy\" ", "Monday, 1 July 2024"));
		mdp.getContent().add(field(" STYLEREF \"Heading 1\" \\* MERGEFORMAT ", "Heading text"));
		mdp.getContent().add(field(" TOC \\o \"1-3\" \\h \\z \\u ", "table of contents"));

		// a simple field
		P p4 = F.createP();
		CTSimpleField sf = F.createCTSimpleField();
		sf.setInstr(" DOCVARIABLE AcmeSecretVar \\* MERGEFORMAT ");
		sf.getContent().add(run("value"));
		p4.getContent().add(F.createPFldSimple(sf));
		mdp.getContent().add(p4);

		AnonymizeResult r = new Anonymize(pkg).go();
		assertClean(r);
		AnonymizeCorpusTest.reload(pkg);

		String all = allXml(pkg);
		assertGone(all, "acme", "portal", "secret", "jane", "Pictures", "logo", "deal room", "Clause 9", "ClientSecretName", "Dear");

		assertTrue(r.getExternalTargetsReplaced() >= 1);
		assertTrue(rel.getTarget(), rel.getTarget().startsWith(MetadataScrubber.EXTERNAL_PLACEHOLDER));
		assertNull(h.getTooltip());

		// the bookmark, the anchor and the REF/PAGEREF arguments still agree
		String bm = bs.getName();
		assertTrue(bm, bm.startsWith("bm"));
		assertEquals(bm, internal.getAnchor());
		assertTrue(all.contains(" REF " + bm + " \\h "));
		assertTrue(all.contains(" PAGEREF " + bm + " \\h "));

		// keywords, switches and pictures survive; arguments do not
		assertTrue(all.contains("HYPERLINK \""));
		assertTrue(all.contains("\\o \""));
		assertTrue(all.contains(" INCLUDEPICTURE \""));
		assertTrue(all.contains("\\* MERGEFORMAT"));
		assertTrue(all.contains(" MERGEFIELD "));
		assertTrue(all.contains("\\* Upper"));
		assertTrue(all.contains("\\@ \"dddd, d MMMM yyyy\""));
		assertTrue("a built-in style name is kept for STYLEREF", all.contains(" STYLEREF \"Heading 1\" "));
		assertTrue(all.contains(" TOC \\o \"1-3\" \\h \\z \\u "));
		assertTrue(sf.getInstr(), sf.getInstr().startsWith(" DOCVARIABLE "));
		assertFalse(sf.getInstr().contains("AcmeSecretVar"));
	}

	// ---- gap 3: settings and content-control properties

	@Test
	public void settingsAndContentControls() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		DocumentSettingsPart dsp = mdp.getDocumentSettingsPart();
		assertNotNull(dsp);
		CTSettings settings = dsp.getContents();

		Relationship dataSource = externalRel(dsp, "http://schemas.openxmlformats.org/officeDocument/2006/relationships/mailMergeSource", "file:///C:/Users/jane/clients.xlsx");
		CTMailMerge mm = F.createCTMailMerge();
		CTMailMerge.ConnectString cs = F.createCTMailMergeConnectString();
		cs.setVal("Provider=Microsoft.ACE.OLEDB.12.0;Data Source=C:\\Users\\jane\\clients.xlsx");
		mm.setConnectString(cs);
		CTMailMerge.Query q = F.createCTMailMergeQuery();
		q.setVal("SELECT * FROM `AcmeClients$`");
		mm.setQuery(q);
		CTRel ds = F.createCTRel();
		ds.setId(dataSource.getId());
		mm.setDataSource(ds);
		settings.setMailMerge(mm);

		CTDocVars dv = F.createCTDocVars();
		CTDocVar v = F.createCTDocVar();
		v.setName("ClientRef");
		v.setVal("ACME-2024-SECRET");
		dv.getDocVar().add(v);
		settings.setDocVars(dv);

		Relationship template = externalRel(dsp, Namespaces.ATTACHED_TEMPLATE, "file:///C:/Users/jane/Templates/AcmeLetter.dotm");
		CTRel at = F.createCTRel();
		at.setId(template.getId());
		settings.setAttachedTemplate(at);

		CTDocProtect dp = F.createCTDocProtect();
		dp.setEdit(STDocProtect.READ_ONLY);
		dp.setEnforcement(true);
		dp.setHash("hashhashhash".getBytes(StandardCharsets.US_ASCII));
		dp.setSalt("saltsaltsalt".getBytes(StandardCharsets.US_ASCII));
		dp.setCryptProviderType(org.docx4j.wml.STCryptProv.RSA_AES);
		settings.setDocumentProtection(dp);

		// a content control with alias, tag and binding
		P p = F.createP();
		SdtRun sdt = F.createSdtRun();
		SdtPr pr = F.createSdtPr();
		SdtPr.Alias alias = F.createSdtPrAlias();
		alias.setVal("Acme Client Name");
		pr.getRPrOrAliasOrLock().add(alias);
		Tag tag = F.createTag();
		tag.setVal("acme_client_name");
		pr.setTag(tag);
		CTDataBinding db = F.createCTDataBinding();
		db.setXpath("/acme:client[1]/acme:name[1]");
		db.setStoreItemID("{11111111-2222-3333-4444-555555555555}");
		pr.setDataBinding(db);
		sdt.setSdtPr(pr);
		CTSdtContentRun content = F.createCTSdtContentRun();
		content.getContent().add(run("Zorbling Industries"));
		sdt.setSdtContent(content);
		p.getContent().add(sdt);
		mdp.getContent().add(p);

		AnonymizeResult r = new Anonymize(pkg).go();
		assertClean(r);
		AnonymizeCorpusTest.reload(pkg);

		String all = allXml(pkg);
		assertGone(all, "jane", "clients", "AcmeClients", "ClientRef", "SECRET", "AcmeLetter", "dotm",
				"hashhash", "saltsalt", "Acme", "client_name", "acme:client", "Zorbling", "Provider=");

		assertNull(settings.getMailMerge());
		assertNull(settings.getDocVars());
		assertNull(settings.getAttachedTemplate());
		assertNotNull("the protection stays, without its password", settings.getDocumentProtection());
		assertTrue(settings.getDocumentProtection().isEnforcement());
		assertNull(settings.getDocumentProtection().getHash());
		assertNull(settings.getDocumentProtection().getSalt());
		assertNull(settings.getDocumentProtection().getCryptProviderType());
		assertNotNull("the alias is kept, scrambled", pr.getByClass(SdtPr.Alias.class));
		assertNotEquals("Acme Client Name", alias.getVal());
		assertNull(pr.getTag());
		assertNull(pr.getDataBinding());
		assertTrue(r.getNotes().toString(), r.getNotes().contains("w:mailMerge removed"));
		assertTrue(r.getNotes().toString(), r.getNotes().contains("w:docVars removed"));
	}

	// ---- gap 4: media and embedded objects

	private WordprocessingMLPackage mediaProbe() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();

		// an EMF, shown by a drawing (with a descr naming the original file)
		MetafileEmfPart emf = new MetafileEmfPart(new PartName("/word/media/image1.emf"));
		emf.setBinaryData(resource("probe.emf"));
		Relationship emfRel = mdp.addTargetPart(emf);
		String drawing = "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
				+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<w:r><w:drawing><wp:inline><wp:extent cx=\"914400\" cy=\"914400\"/>"
				+ "<wp:docPr id=\"1\" name=\"Picture 1\" descr=\"C:\\Users\\jane\\AcmeOrgChart.emf\"/>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
				+ "<pic:pic><pic:nvPicPr><pic:cNvPr id=\"0\" name=\"AcmeOrgChart.emf\"/><pic:cNvPicPr/></pic:nvPicPr>"
				+ "<pic:blipFill><a:blip r:embed=\"" + emfRel.getId() + "\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
				+ "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"914400\" cy=\"914400\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic>"
				+ "</a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>";
		mdp.getContent().add(XmlUtils.unmarshalString(drawing));

		// an OLE object (a real embedded Word 97-2003 document, docx4j's own, from
		// ole-inserted-doc.docx: junk bytes would stop Word opening the probe itself)
		// with the EMF as its picture
		OleObjectBinaryPart ole = new OleObjectBinaryPart();
		ole.setBinaryData(realOleBytes());
		Relationship oleRel = mdp.addTargetPart(ole);
		MetafileEmfPart preview = new MetafileEmfPart(new PartName("/word/media/image2.emf"));
		preview.setBinaryData(resource("probe.emf"));
		Relationship previewRel = mdp.addTargetPart(preview);
		String object = "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
				+ " xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<w:r><w:object w:dxaOrig=\"1512\" w:dyaOrig=\"994\">"
				+ "<v:shapetype id=\"_x0000_t75\" coordsize=\"21600,21600\" o:spt=\"75\" o:preferrelative=\"t\""
				+ " path=\"m@4@5l@4@11@9@11@9@5xe\" filled=\"f\" stroked=\"f\"><v:stroke joinstyle=\"miter\"/>"
				+ "<v:formulas><v:f eqn=\"if lineDrawn pixelLineWidth 0\"/><v:f eqn=\"sum @0 1 0\"/><v:f eqn=\"sum 0 0 @1\"/>"
				+ "<v:f eqn=\"prod @2 1 2\"/><v:f eqn=\"prod @3 21600 pixelWidth\"/><v:f eqn=\"prod @3 21600 pixelHeight\"/>"
				+ "<v:f eqn=\"sum @0 0 1\"/><v:f eqn=\"prod @6 1 2\"/><v:f eqn=\"prod @7 21600 pixelWidth\"/>"
				+ "<v:f eqn=\"sum @8 21600 0\"/><v:f eqn=\"prod @7 21600 pixelHeight\"/><v:f eqn=\"sum @10 21600 0\"/></v:formulas>"
				+ "<v:path o:extrusionok=\"f\" gradientshapeok=\"t\" o:connecttype=\"rect\"/><o:lock v:ext=\"edit\" aspectratio=\"t\"/></v:shapetype>"
				+ "<v:shape id=\"_x0000_i1025\" type=\"#_x0000_t75\" style=\"width:75.6pt;height:49.8pt\" o:ole=\"\" alt=\"Acme figures\">"
				+ "<v:imagedata r:id=\"" + previewRel.getId() + "\" o:title=\"AcmeFigures\"/></v:shape>"
				+ "<o:OLEObject Type=\"Embed\" ProgID=\"Word.Document.8\" ShapeID=\"_x0000_i1025\" DrawAspect=\"Icon\" ObjectID=\"_1833807432\" r:id=\"" + oleRel.getId() + "\"/>"
				+ "</w:object></w:r></w:p>";
		mdp.getContent().add(XmlUtils.unmarshalString(object));

		// an altChunk of HTML
		mdp.addAltChunk(AltChunkType.Html,
				"<html><body><p>SecretHtml paragraph from the Acme intranet</p></body></html>".getBytes(StandardCharsets.UTF_8));

		// a thumbnail
		ImageJpegPart thumb = new ImageJpegPart(new PartName("/docProps/thumbnail.jpeg"));
		thumb.setBinaryData(MediaReplacer.PNG_IMAGE_DATA);
		pkg.addTargetPart(thumb);

		mdp.getContent().add(paragraph("Body text about Acme."));
		return pkg;
	}

	/** the embedded Word 97-2003 document inside docx4j's own ole-inserted-doc.docx */
	static byte[] realOleBytes() throws Exception {
		WordprocessingMLPackage source = AnonymizeCorpusTest.load("ole-inserted-doc.docx");
		for (Part p : source.getParts().getParts().values()) {
			if (p.getPartName().getName().startsWith("/word/embeddings/")) {
				return ((org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart) p).getBytes();
			}
		}
		throw new AssertionError("no embedding in ole-inserted-doc.docx");
	}

	/** a VBA project, on its own: its bytes are junk, so this document is never sent to Word */
	private WordprocessingMLPackage vbaProbe() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		VbaProjectBinaryPart vba = new VbaProjectBinaryPart();
		vba.setBinaryData("AcmeMacroBytes".getBytes(StandardCharsets.US_ASCII));
		pkg.getMainDocumentPart().addTargetPart(vba);
		pkg.getMainDocumentPart().getContent().add(paragraph("A macro-enabled document."));
		return pkg;
	}

	@Test
	public void vbaStrictAndKeep() throws Exception {
		WordprocessingMLPackage pkg = vbaProbe();
		AnonymizeResult r = new Anonymize(pkg).go();
		assertClean(r);
		assertNull(pkg.getParts().get(new PartName("/word/vbaProject.bin")));
		for (Part p : pkg.getParts().getParts().values()) {
			assertFalse(p.getPartName().getName(), p instanceof VbaProjectBinaryPart);
		}
		AnonymizeCorpusTest.reload(pkg);

		pkg = vbaProbe();
		r = new Anonymize(pkg, Anonymize.Mode.KEEP).go();
		assertFalse(r.isClean());
		assertNotNull(pkg.getParts().get(new PartName("/word/vbaProject.bin")));
		assertEquals(1, r.getKeptUnsafe().size());
	}

	@Test
	public void mediaAndObjectsStrict() throws Exception {
		WordprocessingMLPackage pkg = mediaProbe();
		List<PartName> before = new java.util.ArrayList<PartName>(pkg.getParts().getParts().keySet());
		assertTrue(before.toString(), before.contains(new PartName("/word/media/image1.emf")));

		AnonymizeResult r = new Anonymize(pkg).go();
		assertClean(r);
		AnonymizeCorpusTest.reload(pkg);

		String all = allXml(pkg);
		assertGone(all, "Acme", "jane", "OrgChart", "SecretHtml", "<w:altChunk", "OLEObject", "Figures");

		assertNull(pkg.getParts().get(new PartName("/word/media/image1.emf")));
		assertNull(pkg.getParts().get(new PartName("/word/media/image2.emf")));
		assertNull(pkg.getParts().get(new PartName("/docProps/thumbnail.jpeg")));
		for (Part p : pkg.getParts().getParts().values()) {
			assertFalse(p.getPartName().getName(), p instanceof OleObjectBinaryPart);
			assertFalse(p.getPartName().getName(), p.getPartName().getName().startsWith("/word/afchunk"));
		}
		assertEquals(Action.REPLACED, action(r, "/word/media/image1.emf").action);
		assertTrue(r.getNotes().toString(), r.getNotes().contains("o:OLEObject removed (its picture stays, as the labelled placeholder)"));
		assertTrue(r.getNotes().toString(), r.getNotes().contains("w:altChunk replaced by a marker paragraph"));

		// the footprints: the drawing shows the plain placeholder, the object's picture the
		// labelled one, the altChunk is a marker paragraph
		assertNotNull(pkg.getParts().get(new PartName(MediaReplacer.PLACEHOLDER_PART_NAME)));
		assertNotNull(pkg.getParts().get(new PartName(MediaReplacer.OBJECT_PLACEHOLDER_PART_NAME)));
		String rels = mdp(pkg).getRelationshipsPart().getXML();
		assertTrue(rels, rels.contains("media/anon-object-removed.png"));
		assertTrue(rels, rels.contains("media/anon-placeholder.png"));
		assertTrue(all.contains("imagedata"));
		assertTrue(all, all.contains(Placeholders.ALTCHUNK_REMOVED));
		byte[] label = ((org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart)
				pkg.getParts().get(new PartName(MediaReplacer.OBJECT_PLACEHOLDER_PART_NAME))).getBytes();
		assertTrue("a drawn image, not the 2x2 pixels", label.length > 1000);
	}

	private static MainDocumentPart mdp(WordprocessingMLPackage pkg) {
		return pkg.getMainDocumentPart();
	}

	@Test
	public void mediaAndObjectsKeep() throws Exception {
		WordprocessingMLPackage pkg = mediaProbe();
		AnonymizeResult r = new Anonymize(pkg, Anonymize.Mode.KEEP).go();
		assertFalse(r.isClean());
		List<PartAction> kept = r.getKeptUnsafe();
		assertTrue(kept.toString(), kept.size() >= 2); // OLE, altChunk
		String all = allXml(pkg);
		assertTrue("KEEP leaves the object markup", all.contains("OLEObject"));
		assertTrue(all.contains("altChunk"));
		// the thumbnail still goes: always removed
		assertNull(pkg.getParts().get(new PartName("/docProps/thumbnail.jpeg")));
		// and the text was still scrambled
		assertGone(all, "Body text about");
		AnonymizeCorpusTest.reload(pkg);
	}

	// ---- gap 5: diagram text

	@Test
	public void diagramData() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();

		org.docx4j.dml.diagram.ObjectFactory dgm = new org.docx4j.dml.diagram.ObjectFactory();
		org.docx4j.dml.ObjectFactory dml = new org.docx4j.dml.ObjectFactory();
		CTDataModel model = dgm.createCTDataModel();
		CTPtList pts = dgm.createCTPtList();
		CTPt pt = dgm.createCTPt();
		pt.setModelId("{0A1B2C3D-0000-0000-0000-000000000001}");
		CTTextBody body = dml.createCTTextBody();
		body.setBodyPr(dml.createCTTextBodyProperties());
		CTTextParagraph para = dml.createCTTextParagraph();
		CTRegularTextRun run = dml.createCTRegularTextRun();
		run.setT("Acquire Zorbling Industries");
		para.getEGTextRun().add(run);
		body.getP().add(para);
		pt.setT(body);
		pts.getPt().add(pt);
		model.setPtLst(pts);
		model.setCxnLst(new CTCxnList());
		DiagramDataPart data = new DiagramDataPart(new PartName("/word/diagrams/data1.xml"));
		data.setContents(model);
		mdp.addTargetPart(data);
		mdp.getContent().add(paragraph("A diagram follows."));

		AnonymizeResult r = new Anonymize(pkg).go();
		assertClean(r);
		String all = allXml(pkg);
		assertGone(all, "Acquire", "Zorbling", "Industries");
		assertEquals(Action.SCRUBBED, action(r, "/word/diagrams/data1.xml").action);
		assertFalse(run.getT().isEmpty());
	}

	// ---- styles, numbering, digits, other scripts

	@Test
	public void stylesNumberingDigitsAndScripts() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();

		Style custom = F.createStyle();
		custom.setType("paragraph");
		custom.setStyleId("AcmeBodyText");
		custom.setCustomStyle(true);
		Style.Name name = F.createStyleName();
		name.setVal("Acme Body Text");
		custom.setName(name);
		Style.BasedOn basedOn = F.createStyleBasedOn();
		basedOn.setVal("Normal");
		custom.setBasedOn(basedOn);
		mdp.getStyleDefinitionsPart().getContents().getStyle().add(custom);

		P styled = paragraph("Account 12345678 belongs to Владимир Петров.");
		PPr ppr = F.createPPr();
		PPrBase.PStyle ps = F.createPPrBasePStyle();
		ps.setVal("AcmeBodyText");
		ppr.setPStyle(ps);
		styled.setPPr(ppr);
		mdp.getContent().add(styled);

		P heading = paragraph("A heading");
		PPr hppr = F.createPPr();
		PPrBase.PStyle hps = F.createPPrBasePStyle();
		hps.setVal("Heading1");
		hppr.setPStyle(hps);
		heading.setPPr(hppr);
		mdp.getContent().add(heading);

		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		Numbering numbering = ndp.unmarshalDefaultNumbering();
		numbering.getAbstractNum().get(0).getLvl().get(0).getLvlText().setVal("Schedule %1");
		mdp.addTargetPart(ndp);

		AnonymizeResult r = new Anonymize(pkg).go();
		assertClean(r);
		AnonymizeCorpusTest.reload(pkg);

		String all = allXml(pkg);
		assertGone(all, "Acme", "12345678", "Владимир", "Петров", "Schedule", "Account");

		// the custom style is renamed at its definition and at its reference, consistently
		assertTrue(custom.getStyleId(), custom.getStyleId().startsWith("s"));
		assertEquals(custom.getStyleId(), ps.getVal());
		assertEquals("Normal", basedOn.getVal());
		assertEquals("Heading1", hps.getVal());
		Style h1 = mdp.getStyleDefinitionsPart().getStyleById("Heading1");
		assertNotNull(h1);
		assertEquals("heading 1", h1.getName().getVal());

		// digits are still digits, the Cyrillic is still Cyrillic
		String text = ((Text) XmlUtils.unwrap(((R) styled.getContent().get(0)).getContent().get(0))).getValue();
		assertTrue(text, text.matches(".*\\d{8}.*"));
		int cyrillic = 0;
		for (char ch : text.toCharArray()) if (ch >= '\u0400' && ch <= '\u04FF') cyrillic++;
		assertEquals(text, "ВладимирПетров".length(), cyrillic);
		assertTrue(r.hasCyrillic);
		assertEquals("%1 keeps its place", true, numbering.getAbstractNum().get(0).getLvl().get(0).getLvlText().getVal().contains("%1"));
	}

	// ---- the verification catches a survivor

	@Test
	public void verifyCatchesASurvivor() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		P p = paragraph("The Zorbling clause applies.");
		mdp.getContent().add(p);

		Verify.Extraction before = Verify.extract(pkg);
		Anonymize anon = new Anonymize(pkg);
		anon.setVerify(false);
		AnonymizeResult r = anon.go();
		assertNull(r.getVerified());
		assertTrue(r.isClean());

		// plant a survivor
		Text t = (Text) XmlUtils.unwrap(((R) p.getContent().get(0)).getContent().get(0));
		t.setValue(t.getValue() + " Zorbling");
		List<Verify.Leak> leaks = Verify.compare(before, Verify.extract(pkg));
		assertEquals(leaks.toString(), 1, leaks.size());
		assertEquals("zorbling", leaks.get(0).token);
		assertEquals("/word/document.xml", leaks.get(0).partName);
		assertEquals("t/text()", leaks.get(0).where);
	}

	@Test
	public void tokensAreLetterRunsOfThree() {
		assertEquals("[the, zorbling, clause, applies]", Verify.tokens("The Zorbling clause applies. 12345 ab").toString());
		assertEquals("[владимир]", Verify.tokens("Владимир 2024").toString());
		assertTrue(Verify.isLoremFragment("sit"));
		assertTrue(Verify.isLoremFragment("amet"));
		assertFalse(Verify.isLoremFragment("zorbling"));
	}

	@Test
	public void namesAreConsistent() {
		assertEquals(Names.identifier("_Toc123"), Names.identifier("_Toc123"));
		assertNotEquals(Names.identifier("_Toc123"), Names.identifier("_Toc124"));
		assertTrue(Names.identifier("x").startsWith("bm"));
		Names n = new Names(null);
		assertEquals("Author 1", n.author("Jane"));
		assertEquals("Author 2", n.author("Bob"));
		assertEquals("Author 1", n.author("Jane"));
		assertTrue(n.isBuiltInStyleName("heading 1"));
		assertTrue(n.isBuiltInStyleName("Table Grid"));
		assertFalse(n.isBuiltInStyleName("Acme Body Text"));
	}

}
