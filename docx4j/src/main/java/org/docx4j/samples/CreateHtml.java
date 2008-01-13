package org.docx4j.samples;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

public class CreateHtml {
	    
	    public static void main(String[] args) 
	            throws Exception {

			String inputfilepath = "/home/jharrop/tmp/Styles-lots.docx";
//			String inputfilepath = "/home/jharrop/tmp/wordml2html.docx";
			WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
	    				
	        //  send output to System.out
			javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(System.out);
			wordMLPackage.html(result);
	        	        
	    }
	}