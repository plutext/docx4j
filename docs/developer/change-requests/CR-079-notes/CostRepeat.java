import java.io.*; import java.util.*; import java.util.zip.*;
import javax.xml.stream.*; import jakarta.xml.bind.*;
import org.docx4j.jaxb.*; import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.*; import org.docx4j.openpackaging.parts.PartName;
/** The listener's cost with CR-079's repeated-child checks (as the main part) and without (as a header), the variants in turn in one JVM. */
public class CostRepeat {
  static final XMLInputFactory XIF = XMLInputFactory.newInstance();
  static { XIF.setProperty(XMLInputFactory.SUPPORT_DTD, false); XIF.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false); }
  static long run(byte[] x, JaxbXmlPart part) throws Exception {
    XMLStreamReader r = XIF.createXMLStreamReader(new ByteArrayInputStream(x));
    Unmarshaller u = Context.jc.createUnmarshaller(); u.setEventHandler(e -> true);
    u.setListener(new Docx4jUnmarshallerListener(part, r));
    long t = System.nanoTime(); Object o = u.unmarshal(r); t = System.nanoTime() - t; if (o == null) throw new Error(); return t;
  }
  public static void main(String[] a) throws Exception {
    JaxbXmlPart[] parts = { new MainDocumentPart(), new HeaderPart(new PartName("/word/header9.xml")) };
    System.out.println("bytes\titers\twith_ms\twithout_ms\twith%");
    for (String f : a) {
      byte[] x; try (ZipFile z = new ZipFile(f)) { x = z.getInputStream(z.getEntry("word/document.xml")).readAllBytes(); }
      int iters = x.length > 4_000_000 ? 15 : 41, warm = 4;
      long[][] t = new long[2][iters];
      for (int i = 0; i < warm + iters; i++) for (int k = 0; k < 2; k++) { int v = (k + i) % 2; long s = run(x, parts[v]); if (i >= warm) t[v][i - warm] = s; }
      double[] m = new double[2]; for (int v = 0; v < 2; v++) { Arrays.sort(t[v]); m[v] = t[v][iters / 2] / 1e6; }
      System.out.printf("%d\t%d\t%.1f\t%.1f\t%+.1f%n", x.length, iters, m[0], m[1], 100 * (m[0] / m[1] - 1));
    }
  }
}
