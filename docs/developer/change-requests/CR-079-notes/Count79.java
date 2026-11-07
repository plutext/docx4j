import java.io.*; import java.nio.file.*; import java.util.*; import java.util.zip.*;
import jakarta.xml.bind.*; import javax.xml.stream.*; import org.docx4j.jaxb.Context;
/** How many main parts raise an event, and so take the stylesheet today. */
public class Count79 { public static void main(String[] a) throws Exception {
	XMLInputFactory xif = XMLInputFactory.newInstance(); xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
	int n = 0, hit = 0, bad = 0, rootAc = 0;
	for (String l : Files.readAllLines(Paths.get(a[0]))) { String f = l.split("\t")[1];
		byte[] x; try (ZipFile z = new ZipFile(f)) { x = z.getInputStream(z.getEntry("word/document.xml")).readAllBytes(); } catch (Exception e) { bad++; continue; }
		boolean[] h = {false};
		Unmarshaller u = Context.jc.createUnmarshaller();
		u.setEventHandler(e -> { if (e.getSeverity() != ValidationEvent.WARNING) h[0] = true; return true; });
		try { Object o = u.unmarshal(xif.createXMLStreamReader(new ByteArrayInputStream(x))); if (o instanceof org.docx4j.mce.AlternateContent) rootAc++; } catch (Exception e) { h[0] = true; }
		n++; if (h[0]) hit++; }
	System.out.println("main parts " + n + ", raising an event " + hit + ", unreadable " + bad + ", mc root " + rootAc);
} }
