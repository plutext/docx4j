package org.docx4j.dsig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.digitalsignature.SignatureStatus;
import org.docx4j.vml.officedrawing.CTSignatureLine;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.docx4j.dsig.crypt.SignatureDetail;

/**
 * Excel signature lines (CR-002 phase 3): signing a line Excel made, and making one.
 * goldens/sigline.xlsx is that line as Excel signed it. 
 */
public class ExcelSignatureLineTest {

	private static final File ORIGINAL = new File("src/test/resources/goldens/originals/sigline.xlsx");
	private static final File GOLDEN = new File("src/test/resources/goldens/sigline.xlsx");
	private static final File CORPUS = new File("src/test/resources/corpus");

	private static final String MS_DIGSIG = "http://schemas.microsoft.com/office/2006/digsig";

	private static KeyStore.PrivateKeyEntry signer;

	@BeforeClass
	public static void createSigner() throws Exception {
		signer = TestSupport.newSigner("DigSig ExcelSignatureLineTest");
	}

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void signLineExcelMade() throws Exception {

		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.load(ORIGINAL);
		SignatureHelper helper = new SignatureHelper(pkg);

		List<CTSignatureLine> lines = helper.getXlsxSignatureLines(pkg.getWorkbookPart().getWorksheet(0));
		assertEquals(1, lines.size());

		SignatureDetail detail = helper.configureSignature(signer);
		detail.setVisualSignature(lines.get(0), png("x"), "x");
		helper.sign();

		File signed = tmp.newFile("signed.xlsx");
		pkg.save(signed);

		assertEquals(SignatureStatus.VALID, TestSupport.statuses(signed).get("/_xmlsignatures/sig1.xml"));

		// The same shape of signature as Excel's
		Document ours = signature(signed), excels = signature(GOLDEN);
		assertEquals(ids(excels, "Object"), ids(ours, "Object"));
		assertEquals(signedInfoReferences(excels), signedInfoReferences(ours));
		for (String field : new String[] { "SetupID", "SignatureText", "SignatureType", "SignatureProviderId" }) {
			assertEquals(field, signatureInfo(excels, field), signatureInfo(ours, field));
		}
	}

	@Test
	public void makeLineAndSignIt() throws Exception {

		File signed = makeLineAndSignIt("simple.xlsx");

		Map<String, byte[]> entries = TestSupport.entries(signed);
		String sheet = new String(entries.get("xl/worksheets/sheet1.xml"), StandardCharsets.UTF_8);
		assertTrue(sheet, sheet.contains("legacyDrawing"));
	}

	/** Comments are in the legacy drawing part too; the line must join them there */
	@Test
	public void makeLineOnSheetWithComments() throws Exception {

		int vmlPartsBefore = vmlParts(new File(CORPUS, "comments.xlsx")).size();
		File signed = makeLineAndSignIt("comments.xlsx");

		List<String> vmlParts = vmlParts(signed);
		assertEquals(vmlPartsBefore, vmlParts.size());

		String vml = new String(TestSupport.entries(signed).get(vmlParts.get(0)), StandardCharsets.UTF_8);
		assertTrue("the comment's shape is still there", vml.contains("_x0000_s1025"));
		assertTrue("the line's shape has the next id", vml.contains("_x0000_s1026"));
	}

	private File makeLineAndSignIt(String name) throws Exception {

		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.load(new File(CORPUS, name));
		SignatureHelper helper = new SignatureHelper(pkg);
		WorksheetPart sheet = pkg.getWorkbookPart().getWorksheet(0);

		CTSignatureLine line = helper.createCTSignatureLine("the boss", true);
		helper.createXlsxSignatureLine(sheet, line, png("X ______"), "1, 0, 1, 0, 5, 0, 7, 8", null);

		assertEquals(1, helper.getXlsxSignatureLines(sheet).size());

		SignatureDetail detail = helper.configureSignature(signer);
		detail.setVisualSignature(line, png("the boss"), "the boss");
		helper.sign();

		File signed = tmp.newFile("signed-" + name);
		pkg.save(signed);

		assertEquals(SignatureStatus.VALID, TestSupport.statuses(signed).get("/_xmlsignatures/sig1.xml"));
		assertEquals(line.getId(), signatureInfo(signature(signed), "SetupID"));

		// and the line is there for whoever loads the file next
		SpreadsheetMLPackage reloaded = SpreadsheetMLPackage.load(signed);
		List<CTSignatureLine> lines = new SignatureHelper(reloaded)
				.getXlsxSignatureLines(reloaded.getWorkbookPart().getWorksheet(0));
		assertEquals(1, lines.size());
		assertEquals(line.getId(), lines.get(0).getId());

		return signed;
	}

	static byte[] png(String text) throws Exception {
		BufferedImage image = new BufferedImage(256, 128, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 256, 128);
		g.setColor(Color.BLACK);
		g.drawString(text, 20, 60);
		g.drawLine(20, 80, 236, 80);
		g.dispose();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "png", baos);
		return baos.toByteArray();
	}

	private static List<String> vmlParts(File xlsx) throws Exception {
		List<String> names = new ArrayList<String>();
		for (String name : TestSupport.entries(xlsx).keySet()) {
			if (name.endsWith(".vml")) names.add(name);
		}
		return names;
	}

	private static Document signature(File signed) throws Exception {
		return TestSupport.parse(TestSupport.entries(signed).get(TestSupport.signatureEntries(signed).get(0)));
	}

	private static List<String> ids(Document signature, String localName) {
		List<String> ids = new ArrayList<String>();
		NodeList nl = signature.getElementsByTagNameNS(TestSupport.XMLDSIG, localName);
		for (int i = 0; i < nl.getLength(); i++) {
			ids.add(((Element) nl.item(i)).getAttribute("Id"));
		}
		return ids;
	}

	private static List<String> signedInfoReferences(Document signature) {
		List<String> uris = new ArrayList<String>();
		Element signedInfo = (Element) signature.getElementsByTagNameNS(TestSupport.XMLDSIG, "SignedInfo").item(0);
		NodeList nl = signedInfo.getElementsByTagNameNS(TestSupport.XMLDSIG, "Reference");
		for (int i = 0; i < nl.getLength(); i++) {
			uris.add(((Element) nl.item(i)).getAttribute("URI"));
		}
		return uris;
	}

	private static String signatureInfo(Document signature, String field) {
		return signature.getElementsByTagNameNS(MS_DIGSIG, field).item(0).getTextContent();
	}

}
