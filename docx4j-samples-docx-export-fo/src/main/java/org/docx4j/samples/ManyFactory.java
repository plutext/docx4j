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

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.docx4j.Docx4jProperties;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.common.Exporter;
import org.docx4j.convert.out.fo.FOExporterVisitor;
import org.docx4j.convert.out.fo.FOExporterXslt;
import org.docx4j.convert.out.fo.renderers.FORendererApacheFOP;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * This example uses a new FopFactory for each export.
 * 
 * Whilst not the recommend approach, it does work.
 * 
 *  @since 11.5.14.
 */
public class ManyFactory {
	
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
		
		// 
		Docx4jProperties.setProperty("docx4j.convert.out.fo.renderers.ConfiguredPDFDocumentHandler", false);
		
		int total = 1000;
        System.out.println("Starting parallel processing... " + Runtime.getRuntime().availableProcessors() + " threads.");
        long start = System.currentTimeMillis();        
        createPDFs(total, Runtime.getRuntime().availableProcessors());
        long stop = System.currentTimeMillis();
        long elapsed = stop - start;
        System.out.println("Created " + total + " in " + elapsed + "ms");
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

        Mapper fontMapper = new IdentityPlusMapper();  
        wordMLPackage.setFontMapper(fontMapper);        
        		
    	FOSettings foSettings = new FOSettings(wordMLPackage);
    	// Now you can inspect generated font settings in case of any issues
//    	System.out.println(XmlUtils.marshaltoString(foSettings.getFopConfig(), Context.getFopConfigContext()));
    	
		FopFactoryBuilder fopFactoryBuilder = FORendererApacheFOP.getFopFactoryBuilder(foSettings) ;
		
		// You can specify a HyphenBaseResourceResolver, but its probably not necessary if your specify your own resolver above
//		fopFactoryBuilder.setHyphenBaseResourceResolver( 
//				ResourceResolverFactory.createInternalResourceResolver( (new File(".")).toURI(), new ClasspathResolverURIAdapter()));
		FopFactory fopFactory = fopFactoryBuilder.build();
		
	    FOUserAgent foUserAgent = FORendererApacheFOP.getFOUserAgent(foSettings, fopFactory);
        
	    // configure foUserAgent as desired
	    foUserAgent.setTitle(filename + id);
        
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
