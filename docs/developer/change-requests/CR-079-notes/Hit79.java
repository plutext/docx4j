public class Hit79 { public static void main(String[] a) throws Exception {
	Cost79.part = org.docx4j.openpackaging.packages.WordprocessingMLPackage.createPackage().getMainDocumentPart();
	for (String f : a) { byte[] x; try (java.util.zip.ZipFile z = new java.util.zip.ZipFile(f)) { x = z.getInputStream(z.getEntry("word/document.xml")).readAllBytes(); }
		Cost79.repeats = 0; Cost79.run(x, 3); long five = Cost79.repeats; Cost79.repeats = 0; Cost79.run(x, 2);
		System.out.println(new java.io.File(f).getName() + " five=" + five + " two=" + Cost79.repeats); } } }
