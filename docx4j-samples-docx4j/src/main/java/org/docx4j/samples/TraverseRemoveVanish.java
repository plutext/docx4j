package org.docx4j.samples;

import java.io.File;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.utils.SingleTraversalUtilVisitorCallback;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.Body;
import org.docx4j.wml.R;

public class TraverseRemoveVanish {

	
	/**
	 * Example of how to remove all text in a run
	 * which has w:rPr/w:vanish
	 *  
	 */
	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir") + "/Hidden.docx";
				
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));		
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
				
//		System.out.println(documentPart.getXML());
		
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)documentPart.getJaxbElement();
		Body body =  wmlDocumentEl.getBody();
						
		SingleTraversalUtilVisitorCallback runVisitor 
			= new SingleTraversalUtilVisitorCallback(
					new TraversalUtilRunVisitor());
		runVisitor.walkJAXBElements(body);
		
//		System.out.println(documentPart.getXML());
		
		// Save it
		String outputfilepath = System.getProperty("user.dir") + "/OUT_TraverseRemoveVanish.docx";
		Docx4J.save(wordMLPackage, new File(outputfilepath), Docx4J.FLAG_NONE); //(FLAG_NONE == default == zipped docx)
		
		System.out.println("Saved: " + outputfilepath);
		
	}
	
	public static class TraversalUtilRunVisitor extends TraversalUtilVisitor<R> {
		
		@Override
		public void apply(R run, Object parent, List<Object> siblings) {

			if (run.getRPr()!=null
					&& run.getRPr().getVanish()!=null
							&& run.getRPr().getVanish().isVal()
					) {
				
				run.getContent().clear();
								
			}
		}
	
	}	
		
}
