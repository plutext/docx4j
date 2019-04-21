package org.pptx4j.samples;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.dml.CTBlip;
import org.docx4j.dml.CTBlipFillProperties;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.pptx4j.Pptx4jException;

/**
 * Example of how to replace text and images in a Pptx.
 * 
 * Text is replaced using the familiar VariableReplace approach.
 * 
 * Images are replaced by replacing their byte content.
 * 
 * @author jharrop
 *
 */
public class TemplateReplaceSimple {
	
	public static void main(String[] args) throws Docx4JException, Pptx4jException, JAXBException, IOException {
		
		// Input file
		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/image.pptx";

		// String replacements
		HashMap<String, String> mappings = new HashMap<String, String>();
		mappings.put("colour", "green");

		// Image replacements
		List<ImageReplacementDetails>  imageReplacements = new ArrayList<ImageReplacementDetails>();
		
		ImageReplacementDetails example1 = new ImageReplacementDetails();
		example1.slideIndex = 0;
		example1.imageRelId = "rId2";
		example1.replacementImageBytes = FileUtils.readFileToByteArray(new File("test.png"));
		imageReplacements.add(example1);
		
		
		
		PresentationMLPackage presentationMLPackage = 
				(PresentationMLPackage)OpcPackage.load(new java.io.File(inputfilepath));
			
		
		// First, the text replacements
		List<SlidePart> slideParts= 
				presentationMLPackage.getMainPresentationPart().getSlideParts();
		for (SlidePart slidePart : slideParts) {
			
			slidePart.variableReplace(mappings);
		}
		
		
		// Second, the image replacements.
		// We have a design choice here.  
		// Either we can replace text placeholders with images,
		// or we can replace existing images with new images, but keep the XML specifying size etc
		// Here I opt for the latter, so what we need is the relId and image bytes.
		for( ImageReplacementDetails ird : imageReplacements) {
			
			// its a bit inefficient to potentially traverse a single slide
			// multiple times, but I've done it this way to keep this example simple
			SlidePart slidePart= 
					presentationMLPackage.getMainPresentationPart().getSlide(ird.slideIndex);
						
			SlidePicFinder traverser = new SlidePicFinder();
			new TraversalUtil(slidePart.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame(), traverser);
			
			for(org.pptx4j.pml.Pic pic : traverser.pics) {
				
				CTBlipFillProperties  blipFill = pic.getBlipFill();
				if (blipFill!=null) {
					CTBlip blip = blipFill.getBlip();
					if (blip.getEmbed()!=null) {
						String relId = blip.getEmbed();
						
						// is this the one we want?
						if (relId.equals(ird.imageRelId)) {
							Part part = slidePart.getRelationshipsPart().getPart(relId);
							try {
								BinaryPartAbstractImage imagePart = (BinaryPartAbstractImage)part;
								// you'll need to ensure that you replace like with like,
								// ie png for png, not eg jpeg for png!
								imagePart.setBinaryData(ird.replacementImageBytes);
							} catch (ClassCastException cce) {
								System.out.println(part.getClass().getName());
							}
						} else {
							System.out.println(relId + " isn't a match for this replacement. ");							
						}
					} else {
						System.out.println("No a:blip/@r:embed");
					}
				}
			}
		}
		
		System.out.println("\n\n saving .. \n\n");
		String outputfilepath = System.getProperty("user.dir") + "/OUT_VariableReplace.pptx";
		presentationMLPackage.save(new java.io.File(outputfilepath));

		System.out.println("\n\n done .. \n\n");
		
		
	}
	
	static class ImageReplacementDetails {
		
		int slideIndex;
		
		String imageRelId;
		
		byte[] replacementImageBytes;
		
	}
	

	static class SlidePicFinder extends CallbackImpl {
		
		List<org.pptx4j.pml.Pic> pics = new ArrayList<org.pptx4j.pml.Pic>();

		public List<Object> apply(Object o) {

			if (o instanceof org.pptx4j.pml.Pic) {
				pics.add((org.pptx4j.pml.Pic) o);
				System.out.println("added pic");
			}
			return null;
		}

	}


	}
	 
	

