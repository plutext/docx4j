import java.io.*; import java.nio.file.*; import java.util.*; import java.util.zip.*;
import javax.xml.stream.*; import jakarta.xml.bind.*;
import org.docx4j.jaxb.*; import org.docx4j.wml.*;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
/** The listener's cost: stock, a subclass that only delegates, and the checks of CR-079 s4. Interleaved in one JVM. */
public class Cost79 {
	static long calls, repeats;
	static class Pass extends Docx4jUnmarshallerListener {
		Pass(JaxbXmlPart p, XMLStreamReader r) { super(p, r); }
		@Override public void afterUnmarshal(Object t, Object p) { calls++; super.afterUnmarshal(t, p); }
	}
	/** w:body and w:sdtContent: D5's alternative. */
	static class Two extends Docx4jUnmarshallerListener {
		Two(JaxbXmlPart p, XMLStreamReader r) { super(p, r); }
		@Override public void afterUnmarshal(Object t, Object p) {
			calls++;
			if (t instanceof Body) { if (p instanceof Document) { Object h = ((Document) p).getBody(); if (h != null && h != t) repeats++; } }
			else if (t instanceof SdtContent) { if (p instanceof SdtElement) { Object h = ((SdtElement) p).getSdtContent(); if (h != null && h != t) repeats++; } }
			super.afterUnmarshal(t, p);
		}
	}
	/** All five, the hot ones first. */
	static class Five extends Docx4jUnmarshallerListener {
		Five(JaxbXmlPart p, XMLStreamReader r) { super(p, r); }
		@Override public void afterUnmarshal(Object t, Object p) {
			calls++;
			if (t instanceof RPr) { if (p instanceof R) { Object h = ((R) p).getRPr(); if (h != null && h != t) repeats++; } }
			else if (t instanceof PPr) { if (p instanceof P) { Object h = ((P) p).getPPr(); if (h != null && h != t) repeats++; } }
			else if (t instanceof Body) { if (p instanceof Document) { Object h = ((Document) p).getBody(); if (h != null && h != t) repeats++; } }
			else if (t instanceof SdtContent) { if (p instanceof SdtElement) { Object h = ((SdtElement) p).getSdtContent(); if (h != null && h != t) repeats++; } }
			else if (t instanceof CTTxbxContent) { if (p instanceof org.docx4j.vml.CTTextbox) { Object h = ((org.docx4j.vml.CTTextbox) p).getTxbxContent(); if (h != null && h != t) repeats++; } }
			super.afterUnmarshal(t, p);
		}
	}
	static final XMLInputFactory XIF = XMLInputFactory.newInstance();
	static { XIF.setProperty(XMLInputFactory.SUPPORT_DTD, false); XIF.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false); }
	static JaxbXmlPart part;
	static long run(byte[] x, int v) throws Exception {
		XMLStreamReader r = XIF.createXMLStreamReader(new ByteArrayInputStream(x));
		Unmarshaller u = Context.jc.createUnmarshaller();
		u.setEventHandler(e -> true);
		Unmarshaller.Listener l = v == 0 ? new Docx4jUnmarshallerListener(part, r) : v == 1 ? new Pass(part, r) : v == 2 ? new Two(part, r) : new Five(part, r);
		u.setListener(l);
		long t = System.nanoTime();
		Object o = u.unmarshal(r);
		t = System.nanoTime() - t;
		if (o == null) throw new Error();
		return t;
	}
	public static void main(String[] a) throws Exception {
		part = WordprocessingMLPackage.createPackage().getMainDocumentPart();
		System.out.println("file\tbytes\tcalls\titers\tstock_ms\tpass_ms\ttwo_ms\tfive_ms\tpass%\ttwo%\tfive%\trepeats");
		for (String f : a) {
			byte[] x;
			try (ZipFile z = new ZipFile(f)) { x = z.getInputStream(z.getEntry("word/document.xml")).readAllBytes(); }
			int iters = x.length > 4_000_000 ? 12 : x.length > 500_000 ? 40 : 300;
			int warm = x.length > 4_000_000 ? 4 : x.length > 500_000 ? 15 : 200;
			for (int i = 0; i < warm; i++) for (int v = 0; v < 4; v++) run(x, v);
			long[][] t = new long[4][iters];
			calls = 0; repeats = 0; run(x, 1); long c = calls; repeats = 0;
			for (int i = 0; i < iters; i++) for (int k = 0; k < 4; k++) { int v = (k + i) % 4; t[v][i] = run(x, v); }
			double[] m = new double[4];
			for (int v = 0; v < 4; v++) { Arrays.sort(t[v]); m[v] = t[v][iters / 2] / 1e6; }   // median
			double[] b = new double[4];
			for (int v = 0; v < 4; v++) b[v] = t[v][0] / 1e6;                                  // best
			System.out.printf("%s\t%d\t%d\t%d\tmedian %.2f\t%.2f\t%.2f\t%.2f\t%+.1f\t%+.1f\t%+.1f\t%d%n", new File(f).getName().replaceAll("^(.{14}).*", "$1"), x.length, c, iters, m[0], m[1], m[2], m[3],
				100 * (m[1] / m[0] - 1), 100 * (m[2] / m[0] - 1), 100 * (m[3] / m[0] - 1), repeats);
			System.out.printf("\t\t\t\tbest   %.2f\t%.2f\t%.2f\t%.2f\t%+.1f\t%+.1f\t%+.1f%n", b[0], b[1], b[2], b[3],
				100 * (b[1] / b[0] - 1), 100 * (b[2] / b[0] - 1), 100 * (b[3] / b[0] - 1));
		}
	}
}
