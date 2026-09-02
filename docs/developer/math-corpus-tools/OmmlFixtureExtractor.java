import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.math.CTOMath;
import org.docx4j.math.ObjectFactory;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Corpus tool for CR-007-math-omml-mathml: pull every OMML equation out of the
 * Word-generated .docx files and write each as a standalone {@code <m:oMath>}
 * fixture, paired by basename with the MathML that produced it.
 *
 * <p>This is a one-off generator, not part of the build. Run it against the
 * published fat jar so no classpath assembly is needed (see the bundle):</p>
 *
 * <pre>
 *   javac -cp docx4j-bundle-&lt;v&gt;.jar OmmlFixtureExtractor.java
 *   java  -cp docx4j-bundle-&lt;v&gt;.jar:. OmmlFixtureExtractor &lt;docxDir&gt; &lt;outDir&gt;
 * </pre>
 *
 * <p>For each {@code <docxDir>/foo.docx} (Word's output for {@code foo.mml}) it
 * writes {@code <outDir>/foo.omml.xml} — or {@code foo.0.omml.xml},
 * {@code foo.1.omml.xml}, … when a document holds more than one equation. Keep
 * the source {@code foo.mml} beside it (same basename) so a test can load the
 * pair: MathML in, Word's OMML expected out (import direction), and the OMML in,
 * Word's MathML expected out (export direction) once the MathML capture is added.</p>
 *
 * <p>Only {@code m:oMath} is extracted (the unit both converters operate on); a
 * display {@code m:oMathPara} contributes its child {@code m:oMath}, so nothing
 * is double-counted.</p>
 */
public class OmmlFixtureExtractor {

	private static final ObjectFactory MATH_OF = new ObjectFactory();

	public static void main(String[] args) throws Exception {

		if (args.length < 2) {
			System.err.println("Usage: OmmlFixtureExtractor <docxDir> <outDir>");
			System.exit(2);
		}

		File docxDir = new File(args[0]);
		File outDir = new File(args[1]);
		if (!docxDir.isDirectory()) {
			System.err.println("Not a directory: " + docxDir);
			System.exit(2);
		}
		outDir.mkdirs();

		File[] docxFiles = docxDir.listFiles(
				(dir, name) -> name.toLowerCase().endsWith(".docx"));
		if (docxFiles == null || docxFiles.length == 0) {
			System.err.println("No .docx files in " + docxDir);
			System.exit(1);
		}
		Arrays.sort(docxFiles);

		int docsWithMath = 0, equations = 0;
		for (File docx : docxFiles) {
			String base = docx.getName().substring(0, docx.getName().length() - ".docx".length());
			List<Object> oMaths;
			try {
				WordprocessingMLPackage pkg = WordprocessingMLPackage.load(docx);
				MainDocumentPart mdp = pkg.getMainDocumentPart();
				// The m prefix is registered for the math namespace in docx4j's
				// XPath namespace context (NamespacePrefixMappings).
				oMaths = mdp.getJAXBNodesViaXPath("//m:oMath", true);
			} catch (Exception e) {
				System.err.println("SKIP " + docx.getName() + ": " + e.getMessage());
				continue;
			}

			if (oMaths.isEmpty()) {
				System.out.println("no oMath: " + docx.getName());
				continue;
			}
			docsWithMath++;

			boolean many = oMaths.size() > 1;
			for (int i = 0; i < oMaths.size(); i++) {
				CTOMath om = (CTOMath) XmlUtils.unwrap(oMaths.get(i));
				JAXBElement<CTOMath> jbe = MATH_OF.createOMath(om);
				// pretty-print, suppress the XML declaration: fixtures are fragments
				String xml = XmlUtils.marshaltoString(jbe, true, true);
				// docx4j's marshaller pre-declares its whole namespace registry on
				// the root; keep only the prefixes the fragment actually uses.
				xml = stripUnusedNamespaceDeclarations(xml);

				String name = base + (many ? ("." + i) : "") + ".omml.xml";
				Files.write(new File(outDir, name).toPath(), xml.getBytes(StandardCharsets.UTF_8));
				equations++;
			}
		}

		System.out.println("---");
		System.out.println(docxFiles.length + " docx, " + docsWithMath
				+ " with math, " + equations + " equations written to " + outDir);
	}

	/**
	 * Remove {@code xmlns:PREFIX} declarations on the root element whose prefix
	 * is not referenced anywhere in the fragment (element names or attribute
	 * names). Leaves a fixture that declares only what it uses (typically m, w).
	 */
	private static String stripUnusedNamespaceDeclarations(String xml) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new InputSource(new StringReader(xml)));
		Element root = doc.getDocumentElement();

		Set<String> used = new HashSet<>();
		collectUsedPrefixes(root, used);

		NamedNodeMap attrs = root.getAttributes();
		List<String> toRemove = new java.util.ArrayList<>();
		for (int i = 0; i < attrs.getLength(); i++) {
			Attr a = (Attr) attrs.item(i);
			String name = a.getName();
			if (name.startsWith("xmlns:") && !used.contains(name.substring(6))) {
				toRemove.add(name);
			}
		}
		for (String name : toRemove) {
			root.removeAttribute(name);
		}

		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		StringWriter sw = new StringWriter();
		t.transform(new DOMSource(doc), new StreamResult(sw));
		return sw.toString();
	}

	private static void collectUsedPrefixes(Node node, Set<String> used) {
		if (node.getPrefix() != null) {
			used.add(node.getPrefix());
		}
		NamedNodeMap attrs = node.getAttributes();
		if (attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				Node a = attrs.item(i);
				String p = a.getPrefix();
				// a namespace declaration's own prefix is "xmlns"; ignore it
				if (p != null && !"xmlns".equals(p)) {
					used.add(p);
				}
			}
		}
		NodeList kids = node.getChildNodes();
		for (int i = 0; i < kids.getLength(); i++) {
			collectUsedPrefixes(kids.item(i), used);
		}
	}
}
