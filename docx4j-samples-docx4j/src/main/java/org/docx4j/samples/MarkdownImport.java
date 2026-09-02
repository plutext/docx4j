package org.docx4j.samples;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.docx4j.Docx4J;
import org.docx4j.markdown.MarkdownImporter;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

public class MarkdownImport {

	public static void main(String[] args) throws Exception {
		
		MarkdownImporter mi = new MarkdownImporter();
		WordprocessingMLPackage pkg = mi.createPackage(Files.readString(Path.of("some-markdown.md"), StandardCharsets.UTF_8));
		Docx4J.save(pkg, new File("OUT.docx"));
	}
	
}
