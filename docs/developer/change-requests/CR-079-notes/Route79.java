import java.io.*; import java.util.*; import java.util.zip.*;
import org.docx4j.Docx4jProperties;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
/** What a stylesheet on every read of the part would cost, on the real path: docx4j.jaxb.preprocess.always. */
public class Route79 {
	static WordprocessingMLPackage pkg;
	static double once(byte[] x, boolean always) throws Exception {
		Docx4jProperties.setProperty("docx4j.jaxb.preprocess.always", always ? "MainDocumentPart" : "None");
		MainDocumentPart p = new MainDocumentPart(); p.setPackage(pkg);
		long s = System.nanoTime(); Object o = p.unmarshal(new ByteArrayInputStream(x)); double ms = (System.nanoTime() - s) / 1e6;
		if (o == null) throw new Error("null"); return ms;
	}
	public static void main(String[] a) throws Exception {
		pkg = WordprocessingMLPackage.createPackage();
		System.out.println("file\tbytes\tas_today_ms\tstylesheet_always_ms\tratio");
		for (String f : a) {
			byte[] x; try (ZipFile z = new ZipFile(f)) { x = z.getInputStream(z.getEntry("word/document.xml")).readAllBytes(); }
			int iters = x.length > 4_000_000 ? 5 : x.length > 500_000 ? 20 : 60, warm = x.length > 4_000_000 ? 2 : x.length > 500_000 ? 10 : 60;
			for (int i = 0; i < warm; i++) { once(x, false); once(x, true); }
			double b0 = 1e18, b1 = 1e18;
			for (int i = 0; i < iters; i++) { b0 = Math.min(b0, once(x, false)); b1 = Math.min(b1, once(x, true)); }
			System.out.printf("%s\t%d\t%.1f\t%.1f\t%.1fx%n", new File(f).getName().replaceAll("^(.{14}).*", "$1"), x.length, b0, b1, b1 / b0);
		}
	}
}
