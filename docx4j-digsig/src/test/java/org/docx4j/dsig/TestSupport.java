package org.docx4j.dsig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.UnaryOperator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.digitalsignature.SignatureStatus;
import org.docx4j.openpackaging.parts.digitalsignature.TrustCertificateUnconditionally;
import org.docx4j.openpackaging.parts.digitalsignature.XmlSignaturePart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * What the tests share: a throwaway signing key, the fixtures, and reading and rewriting
 * a signed file at the zip level (so that docx4j isn't what we test docx4j with).
 */
public class TestSupport {

	public static final String XMLDSIG = "http://www.w3.org/2000/09/xmldsig#";
	public static final String OPC_DSIG = "http://schemas.openxmlformats.org/package/2006/digital-signature";

	private static final File RESOURCES = new File("src/test/resources");

	/** 
	 * The files to sign: docx, pptx and xlsx, with their strict and macro-enabled variants;
	 * and the files given to Office to make the goldens. 
	 */
	public static List<File> corpus() {
		List<File> corpus = new ArrayList<File>(files("corpus"));
		corpus.addAll(files("goldens/originals"));
		return corpus;
	}

	/**
	 * Files signed by Office.  A name containing TAMPERED is a golden altered after signing.
	 */
	public static List<File> goldens() {
		return files("goldens");
	}

	private static List<File> files(String dir) {
		// Not Office's lock files (~$name), should a fixture be open in Office
		File[] files = new File(RESOURCES, dir).listFiles(f -> f.isFile() && !f.getName().startsWith("~"));
		Arrays.sort(files);
		return Arrays.asList(files);
	}

	public static boolean isTampered(File f) {
		return f.getName().contains("TAMPERED");
	}

	public static String extension(File f) {
		return f.getName().substring(f.getName().lastIndexOf('.'));
	}

	/** A new RSA key with a self-signed certificate, so no private key need be kept in the repository */
	public static KeyStore.PrivateKeyEntry newSigner(String commonName) throws Exception {

		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair kp = kpg.generateKeyPair();

		X500Name name = new X500Name("CN=" + commonName);
		long now = System.currentTimeMillis();
		X509Certificate cert = new JcaX509CertificateConverter().getCertificate(
				new JcaX509v3CertificateBuilder(name, BigInteger.valueOf(now),
						new Date(now - 86400000L), new Date(now + 365 * 86400000L), name, kp.getPublic())
				.build(new JcaContentSignerBuilder("SHA256withRSA").build(kp.getPrivate())));

		return new KeyStore.PrivateKeyEntry(kp.getPrivate(), new Certificate[] { cert });
	}

	/** Load, add one signature per signer in a single sign(), and save */
	public static void sign(File in, File out, KeyStore.PrivateKeyEntry... signers) throws Exception {

		OpcPackage pkg = OpcPackage.load(in);
		SignatureHelper helper = new SignatureHelper(pkg);
		for (KeyStore.PrivateKeyEntry signer : signers) {
			helper.configureSignature(signer);
		}
		helper.sign();
		pkg.save(out);
	}

	/**
	 * The status of each signature in the file, by signature part name, not checking the certificate.
	 * A signature which can't be validated at all is thrown as its DigitalSignatureException.
	 */
	public static Map<String, SignatureStatus> statuses(File signed) throws Exception {

		OpcPackage pkg = OpcPackage.load(signed);
		SignatureHelper helper = new SignatureHelper(pkg);

		Map<String, SignatureStatus> result = new TreeMap<String, SignatureStatus>();
		for (XmlSignaturePart sp : helper.getSignatureParts()) {
			sp.setCertificateTrustChecker(new TrustCertificateUnconditionally());
			result.put(sp.getPartName().getName(), sp.getSignatureStatus(helper.getSignatureConfig()));
		}
		return result;
	}

	// ---- zip level

	public static Map<String, byte[]> entries(File zip) throws IOException {

		Map<String, byte[]> entries = new LinkedHashMap<String, byte[]>();
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zip))) {
			for (ZipEntry ze = zis.getNextEntry(); ze != null; ze = zis.getNextEntry()) {
				entries.put(ze.getName(), zis.readAllBytes());
			}
		}
		return entries;
	}

	/** Copy the zip, replacing the content of one entry */
	public static void rewrite(File in, File out, String entryName, UnaryOperator<byte[]> change) throws IOException {

		Map<String, byte[]> entries = entries(in);
		if (!entries.containsKey(entryName)) {
			throw new IOException(in.getName() + " has no entry " + entryName);
		}
		entries.put(entryName, change.apply(entries.get(entryName)));

		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(out))) {
			for (Map.Entry<String, byte[]> e : entries.entrySet()) {
				zos.putNextEntry(new ZipEntry(e.getKey()));
				zos.write(e.getValue());
				zos.closeEntry();
			}
		}
	}

	public static List<String> signatureEntries(File signed) throws IOException {

		List<String> names = new ArrayList<String>();
		for (String name : entries(signed).keySet()) {
			if (name.startsWith("_xmlsignatures/") && name.endsWith(".xml")) {
				names.add(name);
			}
		}
		return names;
	}

	/** The zip entry of an XML part named in the first signature's Manifest (not a rels part) */
	public static String aSignedXmlPart(File signed) throws Exception {

		for (String uri : signedParts(signed, signatureEntries(signed).get(0)).keySet()) {
			String partName = uri.substring(1, uri.indexOf('?'));
			if (partName.endsWith(".xml")) {
				return partName;
			}
		}
		throw new IllegalStateException("No XML part in the manifest of " + signed.getName());
	}

	/**
	 * What a signature covers: each Manifest reference URI, and for a rels part the
	 * relationships its transform selects (as "id:rId1" or "type:...").
	 */
	public static Map<String, TreeSet<String>> signedParts(File signed, String signatureEntry) throws Exception {

		Document doc = parse(entries(signed).get(signatureEntry));
		Map<String, TreeSet<String>> result = new TreeMap<String, TreeSet<String>>();

		NodeList manifests = doc.getElementsByTagNameNS(XMLDSIG, "Manifest");
		for (int m = 0; m < manifests.getLength(); m++) {
			NodeList references = ((Element) manifests.item(m)).getElementsByTagNameNS(XMLDSIG, "Reference");
			for (int i = 0; i < references.getLength(); i++) {
				Element reference = (Element) references.item(i);
				TreeSet<String> relationships = new TreeSet<String>();
				NodeList byId = reference.getElementsByTagNameNS(OPC_DSIG, "RelationshipReference");
				for (int j = 0; j < byId.getLength(); j++) {
					relationships.add("id:" + ((Element) byId.item(j)).getAttribute("SourceId"));
				}
				NodeList byType = reference.getElementsByTagNameNS(OPC_DSIG, "RelationshipsGroupReference");
				for (int j = 0; j < byType.getLength(); j++) {
					relationships.add("type:" + ((Element) byType.item(j)).getAttribute("SourceType"));
				}
				result.put(reference.getAttribute("URI"), relationships);
			}
		}
		return result;
	}

	public static Document parse(byte[] xml) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
	}

	public static byte[] serialize(Document doc) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(baos));
		return baos.toByteArray();
	}

}
