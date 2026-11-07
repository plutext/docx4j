import java.io.*; import java.util.*; import java.util.zip.*;
import javax.xml.transform.*; import javax.xml.transform.dom.DOMResult; import javax.xml.transform.stream.StreamSource;
import org.docx4j.XmlUtils;
/** The stylesheet with and without the rule at the root, on a document that doesn't take it: variants in turn in one JVM. */
public class CostRoot {
  public static void main(String[] a) throws Exception {
    Templates[] t = { XmlUtils.getTransformerTemplate(new StreamSource(new File(a[0]))), XmlUtils.getTransformerTemplate(new StreamSource(new File(a[1]))), null };
    t[2] = t[1];
    Map<String,Object> main = Collections.<String,Object>singletonMap("mainDocumentPart", Boolean.TRUE);
    System.out.println("bytes\titers\thead_ms\tnew_ms\tnew_main_ms\tnew%\tnew_main%");
    for (int f = 2; f < a.length; f++) {
      byte[] x; try (ZipFile z = new ZipFile(a[f])) { x = z.getInputStream(z.getEntry("word/document.xml")).readAllBytes(); }
      org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(x));
      int iters = x.length > 4_000_000 ? 9 : 21, warm = 3;
      long[][] m = new long[3][iters];
      for (int i = 0; i < warm + iters; i++) for (int k = 0; k < 3; k++) { int v = (k + i) % 3;
        DOMResult r = new DOMResult(); long s = System.nanoTime();
        XmlUtils.transform(doc, t[v], v == 2 ? main : null, r);
        s = System.nanoTime() - s; if (i >= warm) m[v][i - warm] = s; }
      double[] md = new double[3]; for (int v = 0; v < 3; v++) { Arrays.sort(m[v]); md[v] = m[v][iters / 2] / 1e6; }
      System.out.printf("%d\t%d\t%.1f\t%.1f\t%.1f\t%+.1f\t%+.1f%n", x.length, iters, md[0], md[1], md[2], 100 * (md[1] / md[0] - 1), 100 * (md[2] / md[0] - 1));
    }
  }
}
