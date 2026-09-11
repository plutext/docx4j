package org.docx4j.model.pagination;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.SAXParserFactory;

import org.docx4j.convert.out.ConversionFeatures;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Br;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.STBrType;
import org.junit.Test;

/**
 * Paginate.resolveBreaks: the area tree reader's part-relative offsets become offsets in
 * the paragraph's text, a hyphen FOP added is taken off, and a paragraph the
 * preprocessing split at its page break has its second part based where that break is.
 */
public class PaginateResolveBreaksTest {

	private static final ObjectFactory factory = new ObjectFactory();

	private static PaginationMap parse(String areaTree) throws Exception {
		PaginationAreaTreeHandler h = new PaginationAreaTreeHandler();
		SAXParserFactory.newInstance().newSAXParser().parse(
				new ByteArrayInputStream(areaTree.getBytes(StandardCharsets.UTF_8)), h);
		return h.getMap();
	}

	private static String page(int nr, String body) {
		return "<pageViewport key=\"P" + nr + "\" nr=\"" + nr + "\" formatted-nr=\"" + nr + "\"><page>"
				+ "<regionViewport><regionBody><mainReference><span><flow>" + body + "</flow></span></mainReference></regionBody></regionViewport>"
				+ "</page></pageViewport>";
	}

	private static String blockOf(String prodId, String anchor, String... words) {
		StringBuilder sb = new StringBuilder("<block prod-id=\"" + prodId + "\"><lineArea><inlineparent prod-id=\"" + anchor + "\"><text>");
		for (int i = 0; i < words.length; i++) {
			if (i > 0) sb.append("<space> </space>");
			sb.append("<word>").append(words[i]).append("</word>");
		}
		return sb.append("</text></inlineparent></lineArea></block>").toString();
	}

	private static Set<String> features() {
		Set<String> f = new HashSet<String>();
		f.add(ConversionFeatures.PP_COMMON_MOVE_PAGEBREAK);
		return f;
	}

	@Test
	public void theHyphenFopAddedComesOff() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		mdp.addParagraphOfText("incomprehensible");   // no hyphen in the text
		List<P> paragraphs = Paginate.bodyParagraphs(mdp);
		List<String> keys = Paginate.keys(paragraphs);

		PaginationMap map = parse("<areaTree><pageSequence>"
				+ page(1, blockOf("p-P1", "r-P1-0", "incompre-"))
				+ page(2, blockOf("p-P1", "r-P1-0", "hensible"))
				+ "</pageSequence></areaTree>");
		Paginate.resolveBreaks(map, pkg, paragraphs, keys, features());
		assertArrayEquals(new int[] { 8 }, map.getBreaks("P1"));

		// the document's own hyphen stays counted
		mdp.getContent().clear();
		mdp.addParagraphOfText("incompre-hensible");
		paragraphs = Paginate.bodyParagraphs(mdp);
		map = parse("<areaTree><pageSequence>"
				+ page(1, blockOf("p-P1", "r-P1-0", "incompre-"))
				+ page(2, blockOf("p-P1", "r-P1-0", "hensible"))
				+ "</pageSequence></areaTree>");
		Paginate.resolveBreaks(map, pkg, paragraphs, Paginate.keys(paragraphs), features());
		assertArrayEquals(new int[] { 9 }, map.getBreaks("P1"));
	}

	@Test
	public void aSplitParagraphsSecondPartStartsAtItsBreak() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		P p = mdp.addParagraphOfText("aaaa");
		R br = factory.createR();
		Br b = factory.createBr();
		b.setType(STBrType.PAGE);
		br.getContent().add(b);
		p.getContent().add(br);
		p.getContent().add(mdp.createParagraphOfText("bbbb cccc").getContent().get(0));
		List<P> paragraphs = Paginate.bodyParagraphs(mdp);
		List<String> keys = Paginate.keys(paragraphs);

		// part 1 (p-P1~1) starts on page 2 with its own run count; then page 3 inside it
		PaginationMap map = parse("<areaTree><pageSequence>"
				+ page(1, blockOf("p-P1", "r-P1-0", "aaaa"))
				+ page(2, blockOf("p-P1~1", "r-P1~1-0", "bbbb"))
				+ page(3, blockOf("p-P1~1", "r-P1~1-0", "cccc"))
				+ "</pageSequence></areaTree>");
		Paginate.resolveBreaks(map, pkg, paragraphs, keys, features());
		// the break is at 4; 'cccc' after the 4 characters of 'bbbb' and the space FOP
		// dropped at the line end, which the resolver steps over: 4 + 4 + 1
		assertArrayEquals(new int[] { 4, 9 }, map.getBreaks("P1"));
		assertEquals(Integer.valueOf(3), map.getLastPageIndex("P1"));
	}

	@Test
	public void aPartTheDocumentCannotAccountForIsDropped() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		mdp.addParagraphOfText("no break here");
		List<P> paragraphs = Paginate.bodyParagraphs(mdp);
		PaginationMap map = parse("<areaTree><pageSequence>"
				+ page(1, blockOf("p-P1", "r-P1-0", "no"))
				+ page(2, blockOf("p-P1~1", "r-P1~1-0", "break"))
				+ "</pageSequence></areaTree>");
		Paginate.resolveBreaks(map, pkg, paragraphs, Paginate.keys(paragraphs), features());
		assertEquals(0, map.getBreaks("P1").length);
	}
}
