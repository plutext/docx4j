package org.docx4j.samples;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.common.Exporter;
import org.docx4j.convert.out.fo.FOExporterVisitor;
import org.docx4j.convert.out.fo.FOExporterXslt;
import org.docx4j.convert.out.fo.renderers.FORendererApacheFOP;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

public class ManyThreads {
	
    final static String outputdirpath = System.getProperty("user.dir") + "/tmp/";
	
	public static void main(String[] args) throws Exception {
		
        System.out.println("Starting parallel processing... " + Runtime.getRuntime().availableProcessors() + " threads.");
        createPDFs(100, Runtime.getRuntime().availableProcessors());
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
		
		// Use a sample document to generate a fop config based on
		// the fonts used in that sample and the specified FontMapper.
		// This sample docx should contain all the fonts you expect
		// to see across the whole system.
    	// Alternatively/better, you can configure programmatically
		//      FOSettings foSettings = Docx4J.createFOSettings();
		//      foSettings.setFopConfig(your settings); 
		
        // Load the sample docx package
        String inputfilepath = System.getProperty("user.dir")
//        		+ "/embedded.docx";
        		+ "/sample-docs/word/sample-docx.docx";        
		
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));

		// Set up font mapper (optional)
		Mapper fontMapper = new IdentityPlusMapper();  // Best where the fonts in the docx are installed, 
													   // though we do have metrically compatible substitutes for
													   // the automapped fonts described below.
//		Mapper fontMapper = new BestMatchingMapper();  // Good for Linux (and OSX?)
		wordMLPackage.setFontMapper(fontMapper);

		
    	FOSettings foSettings = new FOSettings(wordMLPackage);
    	// Now you can inspect generated font settings in case of any issues
    	System.out.println(XmlUtils.marshaltoString(foSettings.getFopConfig(), Context.getFopConfigContext()));
    	
		FopFactoryBuilder fopFactoryBuilder = FORendererApacheFOP.getFopFactoryBuilder(foSettings) ;
		
		// You can specify a HyphenBaseResourceResolver, but its probably not necessary if your specify your own resolver above
//		fopFactoryBuilder.setHyphenBaseResourceResolver( 
//				ResourceResolverFactory.createInternalResourceResolver( (new File(".")).toURI(), new ClasspathResolverURIAdapter()));
		fopFactory = fopFactoryBuilder.build();
		
		

		// Pre-initialize the Font Cache
		/* FOP builds its font cache lazily on the very first PDF generation request. 
		 * If 5 threads start at the exact same millisecond, they all try to build the 
		 * font list simultaneously, corrupting the shared configuration state
		 * and resulting in stack traces include:		
		 * 
			at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.configuration.DefaultConfiguration.getChildren(DefaultConfiguration.java:137)
			at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.apps.FOUserAgent.getRendererConfiguration(FOUserAgent.java:691)
			at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.apps.FOUserAgent.getRendererConfig(FOUserAgent.java:668)
			at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.render.PrintRendererConfigurator.getRendererConfig(PrintRendererConfigurator.java:91)
			at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.render.PrintRendererConfigurator.buildFontList(PrintRendererConfigurator.java:166)
			at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.render.PrintRendererConfigurator.configure(PrintRendererConfigurator.java:114)
			at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.render.xml.XMLRendererMaker.configureRenderer(XMLRendererMaker.java:43)
	
			 * */
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
        String inputfilepath = System.getProperty("user.dir")
//        		+ "/embedded.docx";
            	+ "/sample-docs/word/sample-docx.docx";        
        
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));

        Mapper fontMapper = new IdentityPlusMapper();  
        wordMLPackage.setFontMapper(fontMapper);        
        
        // Configure FO Settings
        FOSettings foSettings = new FOSettings();
        foSettings.setOpcPackage(wordMLPackage);
        
	    FOUserAgent foUserAgent = FORendererApacheFOP.getFOUserAgent(foSettings, getFopFactory());
	    // configure foUserAgent as desired
	    foUserAgent.setTitle("my title " + id);
        
        // Use unique filenames to avoid file-lock conflicts between threads
        String outputfilepath = outputdirpath + id + ".pdf";
        try (OutputStream os = new FileOutputStream(outputfilepath)) {
        	
            // Instantiate your chosen exporter manually
            Exporter<FOSettings> exporter = FOExporterXslt.getInstance();     // XSLT: Fully featured
//          Exporter<FOSettings> exporter = FOExporterVisitor.getInstance();  // Non XSLT: Faster, but fewer features
            
            // Execute programmatic conversion directly to the target outputstream
            exporter.export(foSettings, os);
        }
        
		// Clean up, so any ObfuscatedFontPart temp files can be deleted 
        // But wait first...
        // 50ms is ok for 1000 iterations, but not 5000
        // 100ms is ok for 5000 iterations
       
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
		if (wordMLPackage.getMainDocumentPart().getFontTablePart()!=null) {
			wordMLPackage.getMainDocumentPart().getFontTablePart().deleteEmbeddedFontTempFiles();
		}		
        
    }	
}
