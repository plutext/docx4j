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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.Docx4jProperties;
import org.docx4j.TraversalUtil;
import org.docx4j.model.datastorage.BindingHandler;
import org.docx4j.model.datastorage.OpenDoPEReverter;
import org.docx4j.model.datastorage.migration.FromMergeFields;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.model.fields.merge.MailMerger;
import org.docx4j.model.fields.seq.FieldUpdaterSEQ;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.R;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.Text;
import org.jvnet.jaxb.lang.Child;
import org.docx4j.XmlUtils;
import org.docx4j.mce.AlternateContent;
import org.docx4j.model.bookmarks.BookmarksIntegrity;
import org.docx4j.model.fields.FieldUpdater;
import org.docx4j.model.pagination.Paginate;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.toc.TocHelper;
import org.docx4j.wml.Body;
import org.docx4j.wml.Document;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.P;
import org.junit.Test;

/**
 * CR-051 phase 1, stage B: the consumers of section 10.3 that meet a block-level
 * {@code mc:AlternateContent} in {@code preserve} mode (or kept whole by WD REQ-305), each of
 * which threw, logged and skipped, or read past the element before.
 */
public class McBlockLevelConsumersTest {

	private static String p(String text) {
		return "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	private static String ac(String choice, String fallback) {
		return "<mc:AlternateContent><mc:Choice Requires=\"w14\">" + choice + "</mc:Choice><mc:Fallback>" + fallback + "</mc:Fallback></mc:AlternateContent>";
	}

	private static String xml(Object o) {
		return XmlUtils.marshaltoString(o, true, false);
	}

	/** A complex DOCPROPERTY field in a Choice's paragraph is updated; its parent is the Choice, not a ContentAccessor (item 1). */
	@Test
	public void fieldUpdaterUpdatesAFieldInABranch() throws Exception {
		String field = "<w:p><w:r><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r><w:instrText xml:space=\"preserve\"> DOCPROPERTY Foo \\* MERGEFORMAT </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r><w:r><w:t>stale</w:t></w:r><w:r><w:fldChar w:fldCharType=\"end\"/></w:r></w:p>";
		WordprocessingMLPackage pkg = McProbeDocx.body(p("before") + ac(field, p("FB")) + p("after")).load(McBlockLevel.PRESERVE);
		pkg.addDocPropsCustomPart();
		pkg.getDocPropsCustomPart().setProperty("Foo", "FRESH");
		new FieldUpdater(pkg).update(true);
		String out = xml(pkg.getMainDocumentPart().getJaxbElement());
		assertTrue(out, out.contains("FRESH"));
		assertFalse(out, out.contains("stale"));
		assertTrue("the element is still whole", out.contains("<mc:Choice") && out.contains("<mc:Fallback"));
	}

	/** An unmatched bookmark start standing directly in a Choice (a block-level marker, its parent the Choice) is removed by the remediation (item 1). */
	@Test
	public void bookmarksIntegrityRemediatesInABranch() throws Exception {
		String choice = "<w:bookmarkStart w:id=\"7\" w:name=\"orphan\"/>" + p("C");
		WordprocessingMLPackage pkg = McProbeDocx.body(p("before") + ac(choice, p("FB"))).load(McBlockLevel.PRESERVE);
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		assertTrue(xml(mdp.getJaxbElement()).contains("orphan"));
		new BookmarksIntegrity().check(mdp, true);
		String out = xml(mdp.getJaxbElement());
		assertFalse(out, out.contains("orphan"));
		assertTrue(out, out.contains("<mc:Choice"));
	}

	/** The TOC helper descends into the branch the exporters draw (item 4): the Fallback by default. */
	@Test
	public void tocHelperFindsParagraphsInTheDrawnBranch() throws Exception {
		Document doc = (Document) XmlUtils.unmarshalString("<w:document " + McProbeDocx.NS + "><w:body>" + p("before")
				+ ac(p("C"), p("FB")) + "</w:body></w:document>");
		List<Object> found = TocHelper.getAllElementsFromObject(doc.getBody(), P.class);
		assertEquals(found.toString(), 2, found.size());
		assertTrue(xml(found.get(1)).contains("FB"));
	}

	/** Paginate's bookmark keys come from the paragraphs the layout has (DRAW, item 5): a bookmark in the drawn branch's paragraph is keyed. */
	@Test
	public void paginateKeysABookmarkInTheDrawnBranch() throws Exception {
		String fallback = "<w:p w14:paraId=\"0000000A\"><w:bookmarkStart w:id=\"1\" w:name=\"inBranch\"/><w:r><w:t>FB</w:t></w:r><w:bookmarkEnd w:id=\"1\"/></w:p>";
		WordprocessingMLPackage pkg = McProbeDocx.body(p("before") + ac(p("C"), fallback)).load(McBlockLevel.PRESERVE);
		Map<String, String> keys = Paginate.bookmarkKeys(pkg.getMainDocumentPart());
		assertEquals(keys.toString(), "0000000A", keys.get("inBranch"));
	}

	/** A header whose element holds two blank paragraphs in the drawn branch reserves space (item 8). */
	@Test
	public void headerReservesWhatTheDrawnBranchHolds() throws Exception {
		HeaderPart header = new HeaderPart(new PartName("/word/header1.xml"));
		header.setJaxbElement((Hdr) XmlUtils.unmarshalString("<w:hdr " + McProbeDocx.NS + ">"
				+ ac("<w:p/>", "<w:p/><w:p/>") + "</w:hdr>", org.docx4j.jaxb.Context.jc, Hdr.class));
		assertFalse("two blank paragraphs in the Fallback: Word reserves them",
				org.docx4j.model.structure.HeaderFooterPolicy.reservesNothing(header));
		HeaderPart one = new HeaderPart(new PartName("/word/header2.xml"));
		one.setJaxbElement((Hdr) XmlUtils.unmarshalString("<w:hdr " + McProbeDocx.NS + ">"
				+ ac("<w:p/><w:p/>", "<w:p/>") + "</w:hdr>", org.docx4j.jaxb.Context.jc, Hdr.class));
		assertTrue("one blank paragraph in the Fallback", org.docx4j.model.structure.HeaderFooterPolicy.reservesNothing(one));
	}

	/** McSplice.contentOf is the one helper: a Choice, a Fallback, a control, a VML text box and a list all answer. */
	@Test
	public void contentOfAnswersForEveryParent() throws Exception {
		Document doc = (Document) XmlUtils.unmarshalString("<w:document " + McProbeDocx.NS + "><w:body>" + ac(p("C"), p("FB"))
				+ "<w:sdt><w:sdtPr/><w:sdtContent>" + p("S") + "</w:sdtContent></w:sdt></w:body></w:document>");
		Body body = doc.getBody();
		AlternateContent element = (AlternateContent) XmlUtils.unwrap(body.getContent().get(0));
		assertEquals(1, McSplice.contentOf(element.getChoice().get(0)).size());
		assertEquals(1, McSplice.contentOf(element.getFallback()).size());
		assertEquals(1, McSplice.contentOf(XmlUtils.unwrap(body.getContent().get(1))).size());
		assertEquals(2, McSplice.contentOf(body.getContent()).size());
		assertNull(McSplice.contentOf(doc.getBody().getContent().get(0) == null ? null : "a string"));
	}

	// --- the sites the first stage B report listed as untested ------------------------------------------

	private static String complexField(String instr, String result) {
		return "<w:p><w:r><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r><w:instrText xml:space=\"preserve\"> " + instr + " </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r><w:r><w:t>" + result + "</w:t></w:r><w:r><w:fldChar w:fldCharType=\"end\"/></w:r></w:p>";
	}

	/** Each run's text, in the order of a walk of every branch (a field result the updaters write is a bare Text). */
	private static List<String> runTexts(List<Object> content) {
		final List<String> out = new ArrayList<String>();
		new TraversalUtil(content, new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof R) {
					StringBuilder sb = new StringBuilder();
					for (Object item : ((R) o).getContent()) {
						if (item instanceof Text) sb.append(((Text) item).getValue());
						else if (item instanceof JAXBElement && "t".equals(((JAXBElement<?>) item).getName().getLocalPart())) sb.append(((Text) XmlUtils.unwrap(item)).getValue());
					}
					if (sb.length() > 0) out.add(sb.toString());
				}
				return null;
			}
		}, McMode.ALL);
		return out;
	}

	private static AlternateContent elementAt(WordprocessingMLPackage pkg, int index) throws Exception {
		return (AlternateContent) XmlUtils.unwrap(pkg.getMainDocumentPart().getContent().get(index));
	}

	/** A MERGEFIELD paragraph directly in each branch is merged (item 1: "Unexpected parent" before). */
	@Test
	public void mailMergerMergesAFieldInABranch() throws Exception {
		WordprocessingMLPackage pkg = McProbeDocx.body(p("before")
				+ ac(complexField("MERGEFIELD NAME", "«NAME»"), complexField("MERGEFIELD NAME", "«NAME»")) + p("after")).load(McBlockLevel.PRESERVE);
		Map<DataFieldName, String> data = new HashMap<DataFieldName, String>();
		data.put(new DataFieldName("NAME"), "Alice");
		MailMerger.performMerge(pkg, data, true);
		String out = xml(pkg.getMainDocumentPart().getJaxbElement());
		assertEquals(out, 2, countOf(out, "Alice"));
		assertFalse(out, out.contains("fldChar"));
		assertTrue(out, out.contains("<mc:Choice") && out.contains("<mc:Fallback"));
	}

	/** A merge paragraph in a branch left empty by a field with no value is removed (item 7: the removal did not look into a branch). */
	@Test
	public void mailMergerRemovesAnEmptiedParagraphInABranch() throws Exception {
		WordprocessingMLPackage pkg = McProbeDocx.body(p("before")
				+ ac(complexField("MERGEFIELD NOVALUE", "«NOVALUE»"), complexField("MERGEFIELD NOVALUE", "«NOVALUE»")) + p("after")).load(McBlockLevel.PRESERVE);
		MailMerger.performMerge(pkg, new HashMap<DataFieldName, String>(), true);
		AlternateContent element = elementAt(pkg, 1);
		assertEquals("the Choice's emptied paragraph is gone: " + xml(element), 0, element.getChoice().get(0).getAny().size());
		assertEquals("the Fallback's too", 0, element.getFallback().getAny().size());
	}

	/** SEQ fields in each branch's paragraph are numbered (item 1: "Unexpected parent" before), each branch from the counter at the element. */
	@Test
	public void seqUpdaterNumbersFieldsInABranch() throws Exception {
		WordprocessingMLPackage pkg = McProbeDocx.body(complexField("SEQ Figure \\* ARABIC", "x")
				+ ac(complexField("SEQ Figure \\* ARABIC", "c"), complexField("SEQ Figure \\* ARABIC", "f"))
				+ complexField("SEQ Figure \\* ARABIC", "y")).load(McBlockLevel.PRESERVE);
		new FieldUpdaterSEQ(pkg).update();
		assertEquals("[1, 2, 2, 3]", runTexts(pkg.getMainDocumentPart().getContent()).toString());
	}

	static final String STORE_ITEM = "{11111111-2222-3333-4444-555555555555}";

	private static String boundControl(int id) {
		return "<w:sdt><w:sdtPr><w:id w:val=\"" + id + "\"/><w:dataBinding w:xpath=\"/root/name\" w:storeItemID=\"" + STORE_ITEM + "\"/><w:text/></w:sdtPr>"
				+ "<w:sdtContent>" + p("stale") + "</w:sdtContent></w:sdt>";
	}

	/**
	 * A package built with a custom XML part, a bound control directly in each branch (each unmarshalled
	 * with its declared type, as {@link #conditionInBranches} explains), saved and loaded again in
	 * preserve mode, its main part not yet unmarshalled.
	 */
	private static WordprocessingMLPackage withCustomXml() throws Exception {
		WordprocessingMLPackage built = WordprocessingMLPackage.createPackage();
		built.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString("<w:document " + McProbeDocx.NS + "><w:body>"
				+ p("before") + ac("", "") + p("after") + "</w:body></w:document>"));
		AlternateContent element = elementAt(built, 1);
		element.getChoice().get(0).getAny().add(sdtBlock(boundControl(1), element.getChoice().get(0)));
		element.getFallback().getAny().add(sdtBlock(boundControl(2), element.getFallback()));
		return savedAndLoaded(built);
	}

	/** As {@link #withCustomXml()}, from a body given as XML (controls directly in branches included, CR-051 section 11.3). */
	static WordprocessingMLPackage withCustomXmlFromXml(String body) throws Exception {
		WordprocessingMLPackage built = WordprocessingMLPackage.createPackage();
		built.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString("<w:document " + McProbeDocx.NS + "><w:body>" + body + "</w:body></w:document>"));
		return savedAndLoaded(built);
	}

	private static WordprocessingMLPackage savedAndLoaded(WordprocessingMLPackage built) throws Exception {
		CustomXmlDataStoragePart part = new CustomXmlDataStoragePart();
		org.docx4j.model.datastorage.CustomXmlDataStorageImpl data = new org.docx4j.model.datastorage.CustomXmlDataStorageImpl();
		data.setDocument(new ByteArrayInputStream("<root><name>BOUND VALUE</name></root>".getBytes(StandardCharsets.UTF_8)));
		part.setData(data);
		built.getMainDocumentPart().addTargetPart(part);
		CustomXmlDataStoragePropertiesPart props = new CustomXmlDataStoragePropertiesPart();
		org.docx4j.customXmlProperties.DatastoreItem item = new org.docx4j.customXmlProperties.ObjectFactory().createDatastoreItem();
		item.setItemID(STORE_ITEM);
		props.setJaxbElement(item);
		part.addTargetPart(props);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		org.docx4j.Docx4J.save(built, bytes);
		return WordprocessingMLPackage.load(new ByteArrayInputStream(bytes.toByteArray()), McBlockLevel.PRESERVE);
	}

	/**
	 * The StAX binder binds a block-level control directly in a Choice as an SdtBlock (item 2: "TODO
	 * stack.peek() Choice" before, the control streamed through unbound).  Checked on the binder's own
	 * output in the part store, which is what the StAX path writes.
	 */
	@Test
	public void staxBinderBindsAControlDirectlyInAChoice() throws Exception {
		String property = "docx4j.model.datastorage.BindingHandler.Implementation";
		String before = Docx4jProperties.getProperty(property);
		try {
			Docx4jProperties.setProperty(property, "BindingTraverserStAX");
			WordprocessingMLPackage pkg = withCustomXml();
			assertFalse("the StAX path needs a part not yet unmarshalled", pkg.getMainDocumentPart().isUnmarshalled());
			new BindingHandler(pkg).applyBindings(pkg.getMainDocumentPart());
			assertFalse("still not unmarshalled: the binder streamed it", pkg.getMainDocumentPart().isUnmarshalled());
			String out = new String(((org.docx4j.openpackaging.io3.stores.ZipPartStore) pkg.getSourcePartStore())
					.getByteArray("word/document.xml").getBytes(), StandardCharsets.UTF_8);
			assertEquals(out, 2, countOf(out, "BOUND VALUE"));
			assertFalse(out, out.contains("stale"));
			assertTrue(out, out.contains("<mc:Choice") && out.contains("<mc:Fallback"));
		} finally {
			Docx4jProperties.setProperty(property, before == null ? "BindingTraverserNonXSLT" : before);
		}
	}

	private static String condition(String tag, String text) {
		return "<w:sdt><w:sdtPr><w:id w:val=\"7\"/><w:tag w:val=\"" + tag + "\"/></w:sdtPr><w:sdtContent>" + p(text) + "</w:sdtContent></w:sdt>";
	}

	/**
	 * A package with a condition control directly in each branch and an XPaths part, built in memory
	 * (CR-051 section 3.4: content unmarshalled outside a part stays whole in both modes); each
	 * control is unmarshalled with its declared type and put into the branch.  Written before CR-051
	 * section 11.3 let such a control through a load; {@code McBranchSdtTest} covers the loads.
	 */
	private static WordprocessingMLPackage conditionInBranches(String tag, String inChoice, String inFallback) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString("<w:document " + McProbeDocx.NS + "><w:body>"
				+ p("before") + ac("", "") + "</w:body></w:document>"));
		AlternateContent element = elementAt(pkg, 1);
		element.getChoice().get(0).getAny().add(sdtBlock(condition(tag, inChoice), element.getChoice().get(0)));
		element.getFallback().getAny().add(sdtBlock(condition(tag, inFallback), element.getFallback()));
		XPathsPart xpaths = new XPathsPart(new org.docx4j.openpackaging.parts.PartName("/customXml/item1.xml"));
		xpaths.setJaxbElement(new org.opendope.xpaths.Xpaths());
		pkg.getMainDocumentPart().addTargetPart(xpaths);
		return pkg;
	}

	private static SdtBlock sdtBlock(String xml, Object parent) throws Exception {
		SdtBlock sdt = (SdtBlock) XmlUtils.unmarshalString(xml.replace("<w:sdt>", "<w:sdt " + McProbeDocx.NS + ">"), Context.jc, SdtBlock.class);
		sdt.setParent(parent);
		return sdt;
	}

	private static List<SdtBlock> sdts(WordprocessingMLPackage pkg) {
		final List<SdtBlock> out = new ArrayList<SdtBlock>();
		new TraversalUtil(pkg.getMainDocumentPart().getContent(), new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof SdtBlock) out.add((SdtBlock) o);
				return null;
			}
		}, McMode.ALL);
		return out;
	}

	/** The reverter replaces a condition control whose parent is a Choice or a Fallback (item 1: "TODO ...Choice" before, left alone). */
	@Test
	public void reverterReplacesAConditionDirectlyInABranch() throws Exception {
		WordprocessingMLPackage template = conditionInBranches("od:condition=c1", "TemplateChoice", "TemplateFallback");
		WordprocessingMLPackage instance = conditionInBranches("od:resultConditionFalse=c1", "", "");
		new OpenDoPEReverter(template, instance).revert();
		List<SdtBlock> restored = sdts(instance);
		assertEquals(2, restored.size());
		assertEquals("[before, TemplateChoice, TemplateFallback]", runTexts(instance.getMainDocumentPart().getContent()).toString());
		assertTrue(((Child) restored.get(0)).getParent() instanceof AlternateContent.Choice);
		assertTrue(((Child) restored.get(1)).getParent() instanceof AlternateContent.Fallback);
	}

	/** The same altChunk r:id in a Choice and a Fallback converts in both, and the part goes once (item 10: NullPointerException on the second before). */
	@Test
	public void altChunkNamedInTwoBranchesConvertsInBoth() throws Exception {
		WordprocessingMLPackage built = WordprocessingMLPackage.createPackage();
		AlternativeFormatInputPart chunk = new AlternativeFormatInputPart(AltChunkType.TextPlain);
		chunk.setBinaryData("CHUNK TEXT".getBytes(StandardCharsets.UTF_8));
		Relationship rel = built.getMainDocumentPart().addTargetPart(chunk);
		String altChunk = "<w:altChunk r:id=\"" + rel.getId() + "\"/>";
		built.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString("<w:document " + McProbeDocx.NS + "><w:body>"
				+ p("before") + ac(altChunk, altChunk) + p("after") + "</w:body></w:document>"));
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		org.docx4j.Docx4J.save(built, bytes);
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(bytes.toByteArray()), McBlockLevel.PRESERVE);
		// the loaded part's AltChunkType comes from its content type (CR-053 D6)
		Part loaded = pkg.getMainDocumentPart().getRelationshipsPart().getPart(rel.getId());
		assertEquals(AltChunkType.TextPlain, ((AlternativeFormatInputPart) loaded).getAltChunkType());
		pkg.getMainDocumentPart().convertAltChunks();
		String out = xml(pkg.getMainDocumentPart().getJaxbElement());
		assertEquals(out, 2, countOf(out, "CHUNK TEXT"));
		assertFalse(out, out.contains("altChunk"));
		assertNull("the part is gone", pkg.getMainDocumentPart().getRelationshipsPart().getPart(rel.getId()));
	}

	/** FromMergeFields (and FromFormText, which shares the helper) migrates a MERGEFIELD paragraph directly in a branch (item 1: ClassCastException before); the clone keeps preserve mode. */
	@Test
	public void migratorConvertsAFieldInABranch() throws Exception {
		WordprocessingMLPackage pkg = McProbeDocx.body(p("before")
				+ ac(complexField("MERGEFIELD NAME", "«NAME»"), complexField("MERGEFIELD NAME", "«NAME»")) + p("after")).load(McBlockLevel.PRESERVE);
		WordprocessingMLPackage out = new FromMergeFields().migrate(pkg);
		assertEquals(McBlockLevel.PRESERVE, out.getMcBlockLevel());
		String xml = xml(out.getMainDocumentPart().getJaxbElement());
		assertTrue(xml, xml.contains("<mc:Choice") && xml.contains("<mc:Fallback"));
		assertFalse(xml, xml.contains("MERGEFIELD"));
		assertEquals("an inline content control in each branch", 2, countOf(xml, "<w:sdt>"));
	}

	private static int countOf(String s, String token) {
		int n = 0;
		for (int i = s.indexOf(token); i >= 0; i = s.indexOf(token, i + 1)) n++;
		return n;
	}
}
