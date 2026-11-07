import java.io.*; import java.util.*; import java.util.zip.*;
import javax.xml.transform.*; import javax.xml.transform.dom.*; import javax.xml.transform.stream.*;
import org.docx4j.XmlUtils;
/** Time in the stylesheet: stock, a new root template, and a test inside the existing template. */
public class Sheet79 {
	public static void main(String[] a) throws Exception {
		Templates[] t = new Templates[3]; String[] n = {"stock.xslt", "form1.xslt", "form2.xslt"};
		for (int i = 0; i < 3; i++) t[i] = XmlUtils.getTransformerTemplate(new StreamSource(new File(n[i])));
		// does each form do what it says on a root element?
		byte[] v; try (ZipFile z = new ZipFile(a[0])) { v = z.getInputStream(z.getEntry("word/document.xml")).readAllBytes(); }
		for (int i = 0; i < 3; i++) {
			org.w3c.dom.Document d = XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(v));
			DOMResult r = new DOMResult(); XmlUtils.transform(d, t[i], null, r);
			System.out.println(n[i] + " root of the mc:AlternateContent file -> " + ((org.w3c.dom.Document) r.getNode()).getDocumentElement().getNodeName());
		}
		int iters = Integer.parseInt(a[1]);
		System.out.println("file\tbytes\tstock_ms\tform1_ms\tform2_ms\tform1%\tform2%");
		for (int k = 2; k < a.length; k++) {
			byte[] x; try (ZipFile z = new ZipFile(a[k])) { x = z.getInputStream(z.getEntry("word/document.xml")).readAllBytes(); }
			org.w3c.dom.Document d = XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(x));
			for (int i = 0; i < 2; i++) for (int v2 = 0; v2 < 3; v2++) XmlUtils.transform(d, t[v2], null, new DOMResult());
			double[] best = {1e18, 1e18, 1e18};
			for (int i = 0; i < iters; i++) for (int j = 0; j < 3; j++) { int v2 = (i + j) % 3;
				long s = System.nanoTime(); XmlUtils.transform(d, t[v2], null, new DOMResult()); double ms = (System.nanoTime() - s) / 1e6;
				if (ms < best[v2]) best[v2] = ms; }
			System.out.printf("%s\t%d\t%.0f\t%.0f\t%.0f\t%+.1f\t%+.1f%n", new File(a[k]).getName().replaceAll("^(.{14}).*", "$1"), x.length, best[0], best[1], best[2], 100 * (best[1] / best[0] - 1), 100 * (best[2] / best[0] - 1));
		}
	}
}
