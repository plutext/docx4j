package org.docx4j.convert.out.pdf;
import java.io.OutputStream;
import java.text.NumberFormat;

import javax.xml.bind.JAXBContext;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;


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
 *  -Xmx512m
 * 
 * Win 7 x64 can do 18 threads with extra perm gen:
 * 
 * -Xmx1G -XX:MaxPermSize=128m
 * 
 * In log4j config, to get pages per second stats, you can use 
 * 
    <logger name="org.apache.fop.area.AreaTreeHandler">
  		<level value="debug"/> 
	</logger>
 * 
 */
public class PdfMultipleThreads  {
	
	final static int TOTAL=4; // <---- number of threads

	final static int REPS=5; // repeat the test, so enable JIT compilation
	
	final static boolean LONGER_DOCX = false;
	
	final static int NUM_PAGES_SHORTER = 5;  
	final static int NUM_PAGES_LONGER = 27; 
	
	public static boolean abort = false;
	
	static String inputfilepath; 	    		
    
	// Set up font mapper, this can be shared between
	// threads
	static Mapper fontMapper = new IdentityPlusMapper();
		
    public static void main(String[] args) 
            throws Exception {
    	
    	if (LONGER_DOCX) {
    		// 27 pages
    		inputfilepath = System.getProperty("user.dir") 
    			+ "/docs/Docx4j_GettingStarted.xml";    		
    	} else {    		
    		inputfilepath = System.getProperty("user.dir") 
    			+ "/sample-docs/word/sample-docx.xml";
    	}
    	    	
    	Thread[] t = new Thread[TOTAL+1];
    	
    	long contextStartTime = System.currentTimeMillis();
    	JAXBContext blgh = org.docx4j.jaxb.Context.jc;
        long contextStartupTime = System.currentTimeMillis() - contextStartTime;
    	System.out.println( contextStartupTime + " ms to initialise JAXB context");
        

		PhysicalFont font 
				= PhysicalFonts.getPhysicalFonts().get("Comic Sans MS");
		fontMapper.put("Calibri", font);
    	
		// OK, initial overhead done; let's start the test
		
    	for (int r=1; r<=REPS; r++) {
    		
        	long startTime = System.currentTimeMillis();
    	
	    	for (int i=1; i<=TOTAL; i++) {
		        t[i] = new Thread(new CreatePdf());
		        t[i].setName("fo"+i);
		        t[i].start();
	    	}
	    		        
	        boolean alive = true;
	        
	        while(alive && !abort) {
	
	        	alive = false;
	        	
	        	
	        	boolean anyDead=false;
		    	for (int i=1; i<=TOTAL; i++) {
			        if (!t[i].isAlive() ) {
			        	anyDead=true;
			        	break;
			        }
		    	}
	        	
	        	
		    	for (int i=1; i<=TOTAL; i++) {
			        if (t[i].isAlive() ) {
			        	alive = true;
				    	if (abort) {
				        	System.out.println(i + " is alive; trying to interrupt");
				        	//t[i].interrupt();
				        	t[i].stop();
				    	} else if (anyDead){
				    		// Only print alive message as we're nearing the end
				        	System.out.println(i + " is alive");				    		
				    	}
			        	
			        } else {
			        	System.out.println(i + " is finished");			        	
			        }
			        
		    	}
		    	
		    	if (alive) {
			        //System.out.println( reportMemory() );
			    	Thread.sleep(500);
		    	}
		    	
		    	if (abort) { 
		        	System.out.println("ABORTING: iteration " + r);			        	
		    		break;
		    	}
	        }
	    	if (abort) break;

	        long timeElapsed = System.currentTimeMillis() - startTime;
	        double elapsedSec = (double)Math.round(10*timeElapsed/ 1000 )/10;
	        double sec = (double)Math.round(100* timeElapsed/ (TOTAL*1000) )/100;
	    	System.out.println("Iteration " + r + " of  " + REPS 
	    			+ " took " + elapsedSec + "sec");
	    	System.out.println("Average " + sec + "sec per thread");
	    	
	    	double ratebase = TOTAL/elapsedSec;
	    	if (LONGER_DOCX) {
		    	System.out.println((ratebase * NUM_PAGES_LONGER) + " pages/sec "); 	    		
	    	} else {
		    	System.out.println((ratebase * NUM_PAGES_SHORTER) + " pages/sec "); 	    			    		
	    	}
	    	/*
	    	 * 
03.08.2011 22:04:30 *DEBUG* AreaTreeHandler: Last page-sequence produced 5 pages. (AreaTreeHandler.java, line 276)
03.08.2011 22:04:30 *DEBUG* AreaTreeHandler: Initial heap size: 118040KB (AreaTreeHandler.java, line 478)
03.08.2011 22:04:30 *DEBUG* AreaTreeHandler: Current heap size: 97812KB (AreaTreeHandler.java, line 479)
03.08.2011 22:04:30 *DEBUG* AreaTreeHandler: Total memory used: -20227KB (AreaTreeHandler.java, line 480)
03.08.2011 22:04:30 *DEBUG* AreaTreeHandler: Total time used: 5473ms (AreaTreeHandler.java, line 481)
03.08.2011 22:04:30 *DEBUG* AreaTreeHandler: Pages rendered: 5 (AreaTreeHandler.java, line 482)
03.08.2011 22:04:30 *DEBUG* AreaTreeHandler: Avg render time: 1094ms/page (55pages/min) (AreaTreeHandler.java, line 487)
	    	 */

	    	System.out.println("Also of interest: " 
	    			+  contextStartupTime + " ms to initialise JAXB context (not counted in above times)");
	    	
	    	
	    	System.out.println( reportMemory() );
	    	
	    	System.out.println( "gc..");
	    	System.gc();        	
	    	System.out.println( reportMemory() );
	    	
	    	System.out.println( "gc..");
	    	System.gc();        	
	    	System.out.println( reportMemory() );
	    	
    	}
    	if (abort) {
    		System.out.println( "## ABORTED.  See logs" );    		
    	} else {
    		System.out.println( "All done!" );
    	}
    	
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
	        	    	
			WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
			
			wordMLPackage.setFontMapper(fontMapper);
								
//			((org.docx4j.convert.out.pdf.viaXSLFO.Conversion)c).setSaveFO(
//					new java.io.File(
//								inputfilepath + Thread.currentThread().getName() + ".fo"));
			OutputStream os = new java.io.FileOutputStream(
								inputfilepath + Thread.currentThread().getName() + ".pdf");	
			
			// FO exporter setup (required)
			// .. the FOSettings object
	    	FOSettings foSettings = Docx4J.createFOSettings();
			foSettings.setWmlPackage(wordMLPackage);			
			
			Docx4J.toFO(foSettings, os, Docx4J.FLAG_EXPORT_PREFER_XSL);			
			System.out.println("Saved " + inputfilepath + Thread.currentThread().getName() + ".pdf");

		 } catch (InterruptedException e) {
			 e.printStackTrace();
		 } catch (Exception e) {
			 PdfMultipleThreads.abort=true;
			 e.printStackTrace();
		 }
    }
	    
	    
	}
}