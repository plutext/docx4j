package org.docx4j.samples;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.common.Exporter;
import org.docx4j.convert.out.fo.FOExporterVisitor;
import org.docx4j.convert.out.fo.FOExporterXslt;
import org.docx4j.convert.out.fo.renderers.FORendererApacheFOP;
import org.docx4j.fonts.BestMatchingMapper;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Example showing how to export to PDF in
 * a multi-threaded environment by re-using FopFactory.
 * 
 * This example uses ConfiguredPDFDocumentHandler,
 * introduced in docx4j 11.5.14, so will not work
 * as expected in earlier versions of docx4j.
 * 
 *  @since 11.5.14.
 */
public class ManyThreads {
	
    final static String outputdirpath = System.getProperty("user.dir") + "/tmp/";
	
	public static void main(String[] args) throws Exception {
		
		//Uncomment to clear output dir before running
		
//		Path dir = Path.of(outputdirpath);
//	    try (var paths = Files.walk(dir)) {
//	        paths.sorted(Comparator.reverseOrder())
//	             .forEach(path -> {
//	                 try {
//	                     Files.delete(path);
//	                 } catch (IOException e) {
//	                     throw new RuntimeException("Failed to delete " + path, e);
//	                 }
//	             });
//	    }
//	    Files.createDirectories(dir);
	    
		fopFactory = getFopFactory();
		
		Docx4jProperties.setProperty("docx4j.convert.out.fo.renderers.ConfiguredPDFDocumentHandler", true);
		// NB: should be set to true. If you try to reuse FopFactory with this set to false,
		// you will may see Font not found warnings.  This is an intermittent multi-threaded failure 
		// when reusing FopFactory without the recommended per-document PDF font configuration. 
		// The failure occurs in org.apache.fop.apps.FOUserAgent.getRendererConfiguration
		
		int total = 1000;
        System.out.println("Starting parallel processing... " + Runtime.getRuntime().availableProcessors() + " threads.");
        long start = System.currentTimeMillis();        
        createPDFs(total, Runtime.getRuntime().availableProcessors());
        long stop = System.currentTimeMillis();
        long elapsed = stop - start;
        System.out.println("Created " + total + " in " + elapsed + "ms");
    }
	
	private static FopFactory fopFactory;
	
	public static FopFactory getFopFactory() throws Docx4JException, FOPException {
		if (fopFactory == null) {
			synchronized(ManyThreads.class) {
				if (fopFactory == null) {
					init();
				}
			}
		}
		return fopFactory;
	}
	

	static void init() throws Docx4JException, FOPException {
		
		// Use a sample document to create a FopFactory
		// which will then be re-used.
		
        // Load the sample docx package
        String inputfilepath = System.getProperty("user.dir")
//        		+ "/embedded.docx";
        		+ "/sample-docs/word/sample-docx.docx";        
		
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));

		// Set up font mapper (optional)
//		Mapper fontMapper = new IdentityPlusMapper();  // Best where the fonts in the docx are installed, 
													   // though we do have metrically compatible substitutes for
													   // the automapped fonts described below.
		Mapper fontMapper = new BestMatchingMapper();  // Good for Linux (and OSX?)
		wordMLPackage.setFontMapper(fontMapper);

		
    	FOSettings foSettings = new FOSettings(wordMLPackage);
    	// Now you can inspect generated font settings in case of any issues
    	System.out.println(XmlUtils.marshaltoString(foSettings.getFopConfig(), Context.getFopConfigContext()));
    	
		FopFactoryBuilder fopFactoryBuilder = FORendererApacheFOP.getFopFactoryBuilder(foSettings) ;
		
		// You can specify a HyphenBaseResourceResolver, but its probably not necessary if your specify your own resolver above
//		fopFactoryBuilder.setHyphenBaseResourceResolver( 
//				ResourceResolverFactory.createInternalResourceResolver( (new File(".")).toURI(), new ClasspathResolverURIAdapter()));
		fopFactory = fopFactoryBuilder.build();
		
        foSettings.setOpcPackage(wordMLPackage);
	    FOUserAgent foUserAgent = FORendererApacheFOP.getFOUserAgent(foSettings, fopFactory);
	    	    
        // Use unique filenames to avoid file-lock conflicts between threads
        String outputfilepath = outputdirpath + "00.pdf";
        try (OutputStream os = new FileOutputStream(outputfilepath)) {
            // Instantiating the visitor instance manually
            Exporter<FOSettings> exporter = FOExporterVisitor.getInstance();
            
            // Execute programmatic conversion directly to the target outputstream
            exporter.export(foSettings, os);
        } catch (Exception e) { }
		
	}
    /**
     * @param iterations Number of PDFs to generate
     * @param threadCount Number of concurrent threads
     */
    public static void createPDFs(int iterations, int threadCount) throws Exception {
    	
    	
        // Create a fixed thread pool
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 1; i <= iterations; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    createPDF(index);
                } catch (Exception e) {
                    System.err.println("Error generating PDF " + index + ": " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }

        // Shut down the executor and wait for tasks to finish
        executor.shutdown();
        if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
            executor.shutdownNow();
        }
    }

    public static void createPDF(int id) throws Exception {
        String filename =  "embedded.docx";
        if (Math.random() > 0.5) {
            		filename = "embedded_w.docx";
        }
        String inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/embedded fonts/" + filename;

        
        System.out.println(id + " : " + filename);
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));

        //		Mapper fontMapper = new IdentityPlusMapper();  // Best where the fonts in the docx are installed, 
															   // though we do have metrically compatible substitutes for
															   // the automapped fonts described below.
        Mapper fontMapper = new BestMatchingMapper();  // Good for Linux (and OSX?)
        wordMLPackage.setFontMapper(fontMapper);        
        
        // Configure FO Settings
        FOSettings foSettings = new FOSettings();
        foSettings.setOpcPackage(wordMLPackage);
        
        // Note: we don't create a fopFactory again here
        
	    FOUserAgent foUserAgent = FORendererApacheFOP.getFOUserAgent(foSettings, getFopFactory());
	    // configure foUserAgent as desired
	    foUserAgent.setTitle(filename + id);

//	    foUserAgent.getRendererOptions().put("pdf-a-mode", "PDF/A-1b");
//	    
//	    // PDF/A-1a, PDF/A-2a and PDF/A-3a require accessibility to be enabled
//	    // see further https://stackoverflow.com/a/54587413/1031689
//	    foUserAgent.setAccessibility(true); // suppress "missing language information" messages from FOUserAgent .processEvent
	    
        // Use unique filenames to avoid file-lock conflicts between threads
        String outputfilepath = outputdirpath + id + "_" + filename + ".pdf";
        try (OutputStream os = new FileOutputStream(outputfilepath)) {
        	
            // Instantiate your chosen exporter manually
//          Exporter<FOSettings> exporter = FOExporterXslt.getInstance();     // XSLT: Fully featured, old default
            Exporter<FOSettings> exporter = FOExporterVisitor.getInstance();  // Non XSLT: Faster, and fully featured since 17.0.4
            
            // Execute programmatic conversion directly to the target outputstream
            exporter.export(foSettings, os);
        }
                
		if (wordMLPackage.getMainDocumentPart().getFontTablePart()!=null) {
			wordMLPackage.getMainDocumentPart().getFontTablePart().deleteEmbeddedFontTempFiles();
		}		
        
    }	
}
