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
import org.docx4j.wml.Text;

public class TraverseRemoveVariables {

	
	/**
	 * Example of how to remove any variables 
	 * of the form ${colour}, ${icecream}
	 * remaining after VariableReplace
	 *  
	 */
	public static void main(String[] args) throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") + "/Hidden.docx";
				
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));		
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
				
//		System.out.println(documentPart.getXML());
		
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)documentPart.getJaxbElement();
		Body body =  wmlDocumentEl.getBody();
						
		SingleTraversalUtilVisitorCallback textVisitor 
			= new SingleTraversalUtilVisitorCallback(
					new TraversalUtilTextVisitor());
		textVisitor.walkJAXBElements(wordMLPackage.getMainDocumentPart().getContents().getBody());
		
//		System.out.println(documentPart.getXML());
		
		// Save it
		String outputfilepath = System.getProperty("user.dir") + "/OUT_TraverseRemoveVanish.docx";
		Docx4J.save(wordMLPackage, new File(outputfilepath), Docx4J.FLAG_NONE); //(FLAG_NONE == default == zipped docx)
		
		System.out.println("Saved: " + outputfilepath);
	}
	
	public static class TraversalUtilTextVisitor extends TraversalUtilVisitor<Text> {
		
		@Override
		public void apply(Text text, Object parent, List<Object> siblings) {

			if (text.getValue().contains("${")
					) {
				
				int startPos = text.getValue().indexOf("${");
				int endPos = text.getValue().indexOf("}", startPos);
				if (endPos > -1) {
					text.setValue(text.getValue().substring(0, startPos) + text.getValue().substring(endPos+1));
				} else {
					System.out.println("Malformed variable in " + text.getValue());
				}
			}
			System.out.println(text.getValue());
		}
	
	}	
		
}
