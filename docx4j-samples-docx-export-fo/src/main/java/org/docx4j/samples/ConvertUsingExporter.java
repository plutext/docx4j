package org.docx4j.samples;

import java.io.FileOutputStream;
import java.io.OutputStream;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.common.Exporter;
import org.docx4j.convert.out.fo.FOExporterVisitor;
import org.docx4j.convert.out.fo.FOExporterXslt;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Demo of converting a docx to PDF via XSL FO bypassiing 
 * the standard Docx4J facade. Instead, directly invoke 
 * FOExporterVisitor or FOExporterXslt.
 */
public class ConvertUsingExporter  {
	
	protected static String inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/sample-docx.docx";
	protected static String outputfilepath;
	
	
    public static void main(String[] args) throws Exception {

        // Define destination file string path
        String outputfilepath;
        if (inputfilepath == null) {
            outputfilepath = System.getProperty("user.dir") + "/OUT_FontContent.pdf";
        } else {
            outputfilepath = inputfilepath + ".pdf";
        }

        
        // Load the docx package
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));

		// Set up font mapper (optional)
		Mapper fontMapper = new IdentityPlusMapper();  // Best where the fonts in the docx are installed, 
													   // though we do have metrically compatible substitutes for
													   // the automapped fonts described below.
//		Mapper fontMapper = new BestMatchingMapper();  // Good for Linux (and OSX?)
		wordMLPackage.setFontMapper(fontMapper);

        // Configure FO Settings
        FOSettings foSettings = new FOSettings();
        foSettings.setOpcPackage(wordMLPackage);
        
        // If you need fine-grained control over hyphenation, PDF/A mode,
        // document title, then be explicit here about FopFactoryBuilder, FOUserAgent.
        // For more, see ConvertOutPDFviaXSLFO sample.
        

        // Create output stream and invoke the FOExporterVisitor directly
        try (OutputStream os = new FileOutputStream(outputfilepath)) {
            
            System.out.println("Exporting using FOExporterVisitor (non-XSLT) directly...");
            
            // Instantiate your chosen exporter manually
//          Exporter<FOSettings> exporter = FOExporterXslt.getInstance();     // XSLT: Fully featured, old default
            Exporter<FOSettings> exporter = FOExporterVisitor.getInstance();  // Non XSLT: Faster, and fully featured since 17.0.4
            
            // Execute programmatic conversion directly to the target outputstream
            exporter.export(foSettings, os);
            
            System.out.println("Saved: " + outputfilepath);
        }
    }
}	
