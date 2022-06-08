package org.docx4j.samples;
import java.io.File;
import java.util.List;

import org.docx4j.model.datastorage.UpdateXmlFromDocumentSurface;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlPart;



/**
 * Copy content control content back to the custom XML part.
 * 
 * Microsoft Word does this automatically for a content control
 * which has a w:databinding element.  
 * 
 * It doesn't do it for a rich text control.  Here we also
 * do that, ie for a content control with a tag such as:
 * 
 *       'od:progid=Word.Document'
 *       
 * The content is converted back to escaped WordML, and
 * injected following the relevant XPath.
 * 
 * This class provides a way
 * to update the XML part for cases
 * where editing is done in something other
 * than Word.
 * 
 * Of course, this sample won't work if RemovalHandler
 * has been used to remove all SDTs or the XML part! 
  * 
 * Limitations:
 * - only the Main Document Part (for escaped WordML)
 * 
 * @author jharrop
 *
 */
public class ContentControlsBindInverse {

	public static void main(String[] args) throws Docx4JException {

		String inputfilepath = System.getProperty("user.dir") + "/inverse_manual.docx";
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		
		// Requires docx4j 6.0.0
		UpdateXmlFromDocumentSurface updater = new UpdateXmlFromDocumentSurface(wordMLPackage, false);
		List<CustomXmlPart> parts = updater.updateCustomXmlParts();

		System.out.println(
				parts.get(0).getXML()
		);
//		for(CustomXmlPart cxp : parts) {
//			System.out.println("\n\n\n");
//			System.out.println(cxp.getXML());
//		}
		
		// or we can save the docx 
		updater.getPkg().save(new File(System.getProperty("user.dir") + "/OUT_UpdateXmlFromDocumentSurface.docx"));
		
		
		// If you want to save a docx containing the updated part(s)
		String outputfilepath = System.getProperty("user.dir") + "/inverse_OUT.docx";
		wordMLPackage.save(new java.io.File(outputfilepath));
		
		// That's all that is necessary.
		// The below is just to illustrate that the resulting Custom XML can be bound again
		
//		// round trip test - UNCOMMENT TO TRY
//		BindingHandler.applyBindings(wordMLPackage.getMainDocumentPart());
//		wordMLPackage.save(
//				new java.io.File(System.getProperty("user.dir") + "/inverse_OUT_AltChunk.docx"));
//		
//		wordMLPackage = (WordprocessingMLPackage) ProcessAltChunk.process(wordMLPackage);
//		
//		// remove content controls: commented out since you probably won't want this
////		RemovalHandler rh = new RemovalHandler();
////		rh.removeSDTs(wordMLPackage, Quantifier.ALL);
//		
//		wordMLPackage.save(
//				new java.io.File(System.getProperty("user.dir") + "/inverse_OUT_RoundTripped.docx"));
		
		
		
	}

}
