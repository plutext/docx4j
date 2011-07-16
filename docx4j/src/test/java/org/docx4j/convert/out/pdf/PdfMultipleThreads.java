package org.docx4j.convert.out.pdf;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.xml.bind.JAXBContext;

import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.samples.AbstractSample;


/**
 * This class does a number of docx to PDF
 * conversions in parallel, as specified by
 * TOTAL.
 * 
 * If you are getting error:
 *  
 * 	ValidationException: For "fo:root", "fo:layout-master-set" must be declared before "fo:page-sequence"
 * 
 * it is probably because you don't have
 * enough heap space.  -Xmx512m is enough
 * for 9 threads (on Linux).
 * 
 * Be sure to use VM args:
 * 
 * -Dlog4j.configuration=log4j.xml -Xmx512m
 * 
 * Win 7 x64 can do 18 threads with extra perm gen:
 * 
 * -Xmx1G -XX:MaxPermSize=128m
 *
 */
public class PdfMultipleThreads extends AbstractSample {
	
	final static int TOTAL=6; // <---- number of threads

	final static int REPS=5; // repeat the test, so enable JIT compilation
	
	public static boolean abort = false;
    
	// Set up font mapper, this can be shared between
	// threads
	static Mapper fontMapper = new IdentityPlusMapper();
	
	static DecimalFormat df = new DecimalFormat("#.##");
	
    public static void main(String[] args) 
            throws Exception {
    	    	
    	Thread[] t = new Thread[TOTAL+1];
    	
    	JAXBContext blgh = org.docx4j.jaxb.Context.jc;

		PhysicalFont font 
				= PhysicalFonts.getPhysicalFonts().get("Comic Sans MS");
		fontMapper.getFontMappings().put("Calibri", font);
    	
		// OK, initial overhead done; let's start the test
		
    	for (int r=1; r<=REPS; r++) {
    		
        	long startTime = System.currentTimeMillis();
    	
	    	for (int i=1; i<=TOTAL; i++) {
		        t[i] = new Thread(new CreatePdf());
		        t[i].setName("fo"+i);
		        t[i].start();
	    	}
	    		        
	        boolean alive = true;
	        
	        while(alive) {
	
	        	alive = false;
		    	for (int i=1; i<=TOTAL; i++) {
			        if (t[i].isAlive() ) {
			        	alive = true;
				    	if (abort) {
				        	System.out.println(i + " is alive; trying to interrupt");
				        	//t[i].interrupt();
				        	t[i].stop();
				    	} else {
				        	System.out.println(i + " is alive");				    		
				    	}
			        	
			        } else {
			        	System.out.println(i + " is finished");			        	
			        }
			        
		    	}
		    	
		    	if (alive) {
			        System.out.println( reportMemory() );
			    	Thread.sleep(1000);
		    	}
	        }	
	        long timeElapsed = System.currentTimeMillis() - startTime;
	        int elapsedSec = Math.round(timeElapsed/ 1000 );
	        float sec = timeElapsed/ (TOTAL*1000);
	    	System.out.println("Iteration " + r + " of  " + REPS 
	    			+ " took " + elapsedSec + "sec");
	    	System.out.println("Average " + df.format(sec) + "sec per thread");
	    	
	    	System.out.println( reportMemory() );
	    	
	    	System.out.println( "gc..");
	    	System.gc();        	
	    	System.out.println( reportMemory() );
	    	
	    	System.out.println( "gc..");
	    	System.gc();        	
	    	System.out.println( reportMemory() );
	    	
    	}
    	System.out.println( "All done!" );
    	
    }
    
    public static String reportMemory() {
    	
    	Runtime runtime = Runtime.getRuntime();

        NumberFormat format = NumberFormat.getInstance();

        StringBuilder sb = new StringBuilder();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        //sb.append("free memory: " + format.format(freeMemory / (1024*1024)) + "M\n");
        sb.append("allocated memory: " + format.format(allocatedMemory / (1024*1024)) + "M\n");
        //sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / (1024*1024)) + "M\n");
        sb.append("--------------\n");	        
        sb.append("[ Xmx: " + format.format(maxMemory / (1024*1024)) + "M ]\n");
        
        return sb.toString();
    }
    
	 private static class CreatePdf implements Runnable {
		 
	 public void run() {	 
		 
		 try {
	        	
    	boolean save = true;
    	
    	inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/sample-docx.xml";
    	
    	
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		
//			// Set up font mapper
//			Mapper fontMapper = new IdentityPlusMapper();
		wordMLPackage.setFontMapper(fontMapper);
//			
//			// Example of mapping missing font Algerian to installed font Comic Sans MS
//			PhysicalFont font 
//					= PhysicalFonts.getPhysicalFonts().get("Comic Sans MS");
//			fontMapper.getFontMappings().put("Calibri", font);
					
		org.docx4j.convert.out.pdf.PdfConversion c 
			= new org.docx4j.convert.out.pdf.viaXSLFO.Conversion(wordMLPackage);
		
		if (save) {
			((org.docx4j.convert.out.pdf.viaXSLFO.Conversion)c).setSaveFO(
					new java.io.File(
								inputfilepath + Thread.currentThread().getName() + ".fo"));
			OutputStream os = new java.io.FileOutputStream(
								inputfilepath + Thread.currentThread().getName() + ".pdf");			
			c.output(os);
			System.out.println("Saved " + inputfilepath + Thread.currentThread().getName() + ".pdf");
		} 
		 } catch (InterruptedException e) {
			 e.printStackTrace();
		 } catch (Exception e) {
			 PdfMultipleThreads.abort=true;
			 e.printStackTrace();
		 }
    }
	    
	    
	}
}