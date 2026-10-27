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
package org.docx4j.model.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.CTTwipsMeasure;
import org.docx4j.wml.Document;
import org.docx4j.wml.LenientInteger;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.Style;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * CR-066 phase 4 (its section 10): {@code value.out_of_range} findings, one per attribute occurrence, in every
 * WordprocessingML part, with Word's reading; and an inspection that logs none of the marshal-time range WARNs
 * (D14).
 */
public class InspectRangesTest {

	private static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	private static final String BODY =
			"<w:p><w:pPr><w:spacing w:before=\"65537\"/><w:ind w:left=\"66256\"/></w:pPr><w:r><w:t>wrapped</w:t></w:r></w:p>"
			+ "<w:p><w:pPr><w:spacing w:before=\"65535\"/><w:ind w:left=\"32767\"/></w:pPr><w:r><w:t>at the edge</w:t></w:r></w:p>"
			+ "<w:tbl><w:tblPr><w:tblW w:w=\"65536\" w:type=\"pct\"/></w:tblPr><w:tblGrid><w:gridCol w:w=\"70000\"/></w:tblGrid>"
			+ "<w:tr><w:trPr><w:trHeight w:val=\"65537\" w:hRule=\"exact\"/></w:trPr>"
			+ "<w:tc><w:tcPr><w:tcW w:w=\"70000\" w:type=\"dxa\"/></w:tcPr><w:p><w:r><w:t>cell</w:t></w:r></w:p></w:tc>"
			+ "<w:tc><w:tcPr><w:tcW w:w=\"99999\" w:type=\"auto\"/></w:tcPr><w:p/></w:tc></w:tr></w:tbl>"
			+ "<w:p><w:pPr><w:tabs><w:tab w:val=\"left\" w:pos=\"32768\"/></w:tabs><w:spacing w:line=\"65535\" w:lineRule=\"exact\"/>"
			+ "<w:ind w:firstLine=\"65535\"/></w:pPr><w:r><w:rPr><w:w w:val=\"65636\"/></w:rPr><w:t>scaled</w:t></w:r></w:p>"
			+ "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/><w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\""
			+ " w:header=\"708\" w:footer=\"708\" w:gutter=\"0\"/><w:pgNumType w:start=\"4294967295\"/><w:cols w:space=\"708\"/></w:sectPr>";
	private static final String[] EXPECTED = {
			"/w:document[1]/w:body[1]/w:p[1]/w:pPr[1]/w:spacing[1]|attribute=w:spacing/@w:before; value=65537; storage=uint16; range=0..65535; word_reads=1",
			"/w:document[1]/w:body[1]/w:p[1]/w:pPr[1]/w:ind[1]|attribute=w:ind/@w:left; value=66256; storage=int16; range=-32768..32767; word_reads=720",
			"/w:document[1]/w:body[1]/w:tbl[1]/w:tblPr[1]/w:tblW[1]|attribute=w:tblW/@w:w; type=pct; value=65536; storage=int16; range=-32768..32767; word_reads=0",
			"/w:document[1]/w:body[1]/w:tbl[1]/w:tblGrid[1]/w:gridCol[1]|attribute=w:gridCol/@w:w; value=70000; storage=16 bits; range=-32768..65535; word_reads=changed",
			"/w:document[1]/w:body[1]/w:tbl[1]/w:tr[1]/w:trPr[1]/w:trHeight[1]|attribute=w:trHeight/@w:val; hRule=exact; value=65537; storage=int16; range=1..32768; word_reads=exact 1",
			"/w:document[1]/w:body[1]/w:tbl[1]/w:tr[1]/w:tc[1]/w:tcPr[1]/w:tcW[1]|attribute=w:tcW/@w:w; type=dxa; value=70000; storage=int16; range=-32768..32767; word_reads=4464",
			"/w:document[1]/w:body[1]/w:p[3]/w:pPr[1]/w:tabs[1]/w:tab[1]|attribute=w:tab/@w:pos; value=32768; storage=int16; range=-31680..31680; word_reads=-31680",
			"/w:document[1]/w:body[1]/w:p[3]/w:pPr[1]/w:spacing[1]|attribute=w:spacing/@w:line; lineRule=exact; value=65535; storage=int16; range=1..32768; word_reads=atLeast 1",
			"/w:document[1]/w:body[1]/w:p[3]/w:pPr[1]/w:ind[1]|attribute=w:ind/@w:firstLine; value=65535; storage=int16; range=0..32767; word_reads=hanging 1",
			"/w:document[1]/w:body[1]/w:p[3]/w:r[1]/w:rPr[1]/w:w[1]|attribute=w:w/@w:val; value=65636; storage=uint16; range=0..65535; word_reads=100",
			"/w:document[1]/w:body[1]/w:sectPr[1]/w:pgNumType[1]|attribute=w:pgNumType/@w:start; value=4294967295; storage=int32; range=-2147483648..2147483647; word_reads=-1",
	};

	private final List<ILoggingEvent> events = new ArrayList<ILoggingEvent>();
	private ch.qos.logback.classic.Logger logger;
	private Level previous;
	private AppenderBase<ILoggingEvent> appender;

	@Before
	public void captureLenientIntegersLog() {
		appender = new AppenderBase<ILoggingEvent>() {
			@Override
			protected void append(ILoggingEvent event) {
				events.add(event);
			}
		};
		logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(LenientInteger.class);
		previous = logger.getLevel();
		logger.setLevel(Level.DEBUG);
		appender.setContext(logger.getLoggerContext());
		appender.start();
		logger.addAppender(appender);
	}

	@After
	public void releaseTheLog() {
		logger.detachAppender(appender);
		logger.setLevel(previous);
		appender.stop();
	}

	private static WordprocessingMLPackage document(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unwrap(
				XmlUtils.unmarshalString("<w:document xmlns:w=\"" + W + "\"><w:body>" + body + "</w:body></w:document>")));
		return pkg;
	}

	private static List<String> ranges(Inspection i) {
		List<String> out = new ArrayList<String>();
		for (InspectFinding f : i.findings()) {
			if (f.getKind().equals(InspectFinding.VALUE_OUT_OF_RANGE)) {
				out.add(f.getLocation().getPart() + "|" + f.getLocation().getPath() + "|" + f.getSummary());
			}
		}
		return out;
	}

	private static List<String> inMain(String[] expected) {
		List<String> out = new ArrayList<String>();
		for (String e : expected) out.add("/word/document.xml|" + e);
		return out;
	}

	@Test
	public void oneFindingPerAttributeWithWordsReading() throws Exception {
		WordprocessingMLPackage pkg = document(BODY);
		Inspection i = Inspect.of(pkg);
		assertEquals(inMain(EXPECTED), ranges(i));
		for (InspectFinding f : i.findings()) {
			if (!f.getKind().equals(InspectFinding.VALUE_OUT_OF_RANGE)) continue;
			assertSame(Visible.NOT_CONTENT, f.getVisible());
			assertSame(Reveal.Form.UNEXTRACTABLE, f.getReveal().getForm());
			assertEquals("", f.getExcerpt());
			assertSame(Group.INFO, f.group(new TextOptions(), Inspection.Format.PLAIN, Collections.<Inspection.Capability>emptySet()));
			assertSame(Group.INFO, f.group(new TextOptions(), Inspection.Format.BLOCKS, Collections.<Inspection.Capability>emptySet()));
		}
		assertEquals(Inspection.Status.COMPLETE, i.status());
	}

	/** A run's property in the run after one with the same finding is the same finding: run splitting does not count. */
	@Test
	public void adjacentRunsWithTheSamePropertyAreOneFinding() throws Exception {
		WordprocessingMLPackage pkg = document(
				"<w:p><w:r><w:rPr><w:w w:val=\"65636\"/></w:rPr><w:t>split </w:t></w:r><w:bookmarkStart w:id=\"1\" w:name=\"b\"/>"
				+ "<w:r><w:rPr><w:w w:val=\"65636\"/></w:rPr><w:t>in two</w:t></w:r>"
				+ "<w:r><w:t>plain</w:t></w:r><w:r><w:rPr><w:w w:val=\"65636\"/></w:rPr><w:t>again</w:t></w:r>"
				+ "<w:r><w:rPr><w:w w:val=\"65637\"/></w:rPr><w:t>another value</w:t></w:r></w:p>"
				+ "<w:p><w:pPr><w:rPr><w:w w:val=\"65636\"/></w:rPr></w:pPr><w:r><w:rPr><w:w w:val=\"65636\"/></w:rPr><w:t>mark and run</w:t></w:r></w:p>");
		List<String> found = ranges(Inspect.of(pkg));
		List<String> paths = new ArrayList<String>();
		for (String f : found) paths.add(f.split("\\|")[1]);
		assertEquals(java.util.Arrays.asList(
				"/w:document[1]/w:body[1]/w:p[1]/w:r[1]/w:rPr[1]/w:w[1]",   // the second run merges into it (a bookmark between is no run)
				"/w:document[1]/w:body[1]/w:p[1]/w:r[4]/w:rPr[1]/w:w[1]",   // a run without the property between: a finding again
				"/w:document[1]/w:body[1]/w:p[1]/w:r[5]/w:rPr[1]/w:w[1]",   // another value: its own finding
				"/w:document[1]/w:body[1]/w:p[2]/w:pPr[1]/w:rPr[1]/w:w[1]", // a paragraph mark's never merges
				"/w:document[1]/w:body[1]/w:p[2]/w:r[1]/w:rPr[1]/w:w[1]"), paths);
	}

	@Test
	public void anInspectionLogsNoneOfTheMarshalTimeWarns() throws Exception {
		WordprocessingMLPackage pkg = document(BODY);
		events.clear();
		Inspect.of(pkg);
		List<String> logged = new ArrayList<String>();
		for (ILoggingEvent e : events) logged.add(e.getLevel() + " " + e.getFormattedMessage());
		assertEquals("the inspection's report is its findings (CR-066 D14)", Collections.emptyList(), logged);
	}

	@Test
	public void heldAsWrittenThroughASaveAndAReload() throws Exception {
		WordprocessingMLPackage pkg = document(BODY);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Docx4J.save(pkg, out, Docx4J.FLAG_NONE);
		WordprocessingMLPackage again = WordprocessingMLPackage.load(new ByteArrayInputStream(out.toByteArray()));
		assertEquals(inMain(EXPECTED), ranges(Inspect.of(again)));
	}

	@Test
	public void theStylesAndSettingsPartsAreCheckedAfterTheMainDocument() throws Exception {
		WordprocessingMLPackage pkg = document("<w:p><w:pPr><w:ind w:left=\"66256\"/></w:pPr><w:r><w:t>x</w:t></w:r></w:p>");
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		ObjectFactory f = new ObjectFactory();
		Style style = f.createStyle();
		style.setType("paragraph");
		style.setStyleId("Wide");
		PPr ppr = f.createPPr();
		PPrBase.Ind ind = f.createPPrBaseInd();
		ind.setLeft(BigInteger.valueOf(7205001));
		ppr.setInd(ind);
		style.setPPr(ppr);
		mdp.getStyleDefinitionsPart().getJaxbElement().getStyle().add(style);
		DocumentSettingsPart settings = mdp.getDocumentSettingsPart();
		if (settings == null) {
			settings = new DocumentSettingsPart();
			settings.setJaxbElement(new CTSettings());
			mdp.addTargetPart(settings);
		}
		CTTwipsMeasure zone = f.createCTTwipsMeasure();
		zone.setVal(BigInteger.valueOf(65537));
		settings.getJaxbElement().setHyphenationZone(zone);
		List<String> found = ranges(Inspect.of(pkg));
		assertEquals(3, found.size());
		assertTrue(found.get(0), found.get(0).startsWith("/word/document.xml|/w:document[1]/w:body[1]/w:p[1]/w:pPr[1]/w:ind[1]|attribute=w:ind/@w:left; value=66256;"));
		assertTrue(found.get(1), found.get(1).startsWith("/word/settings.xml|/w:settings[1]/w:hyphenationZone[1]|attribute=w:hyphenationZone/@w:val; value=65537; storage=uint16; range=0..65535; word_reads=1"));
		int styleIndex = mdp.getStyleDefinitionsPart().getJaxbElement().getStyle().size();
		assertEquals("/word/styles.xml|/w:styles[1]/w:style[" + styleIndex + "]/w:pPr[1]/w:ind[1]|attribute=w:ind/@w:left; value=7205001; storage=int16; range=-32768..32767; word_reads=-3959", found.get(2));
	}
}
