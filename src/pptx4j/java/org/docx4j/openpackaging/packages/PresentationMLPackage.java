/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */

package org.docx4j.openpackaging.packages;


import org.apache.log4j.Logger;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.DocPropsCorePart;
import org.docx4j.openpackaging.parts.DocPropsCustomPart;
import org.docx4j.openpackaging.parts.DocPropsExtendedPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart;
import org.docx4j.openpackaging.parts.PresentationML.SlideMasterPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;



/**
 * @author jharrop
 *
 */
public class PresentationMLPackage  extends OpcPackage {
	
	protected static Logger log = Logger.getLogger(PresentationMLPackage.class);
		
	
	/**
	 * Constructor.  Also creates a new content type manager
	 * 
	 */	
	public PresentationMLPackage() {
		super();
		setContentType(new ContentType(ContentTypes.WORDPROCESSINGML_DOCUMENT)); // FIXME
	}
	/**
	 * Constructor.
	 *  
	 * @param contentTypeManager
	 *            The content type manager to use 
	 */
	public PresentationMLPackage(ContentTypeManager contentTypeManager) {
		super(contentTypeManager);
		setContentType(new ContentType(ContentTypes.WORDPROCESSINGML_DOCUMENT));
	}
	
	
	/**
	 * Convenience method to create a PresentationMLPackage
	 * from an existing File (.pptx or .xml Flat OPC).
     *
	 * @param docxFile
	 *            The docx file 
	 */	
	public static PresentationMLPackage load(java.io.File docxFile) throws Docx4JException {
		
		return (PresentationMLPackage)OpcPackage.load(docxFile);
	}
	
	public boolean setPartShortcut(Part part, String relationshipType) {
		if (relationshipType.equals(Namespaces.PROPERTIES_CORE)) {
			docPropsCorePart = (DocPropsCorePart)part;
			log.info("Set shortcut for docPropsCorePart");
			return true;			
		} else if (relationshipType.equals(Namespaces.PROPERTIES_EXTENDED)) {
			docPropsExtendedPart = (DocPropsExtendedPart)part;
			log.info("Set shortcut for docPropsExtendedPart");
			return true;			
		} else if (relationshipType.equals(Namespaces.PROPERTIES_CUSTOM)) {
			docPropsCustomPart = (DocPropsCustomPart)part;
			log.info("Set shortcut for docPropsCustomPart");
			return true;			
//		} else if (relationshipType.equals(Namespaces.DOCUMENT)) {
//			mainDoc = (MainDocumentPart)part;
//			log.info("Set shortcut for mainDoc");
//			return true;			
		} else {	
			return false;
		}
	}
	

	
	/**
	 * Create an empty presentation.
	 * 
	 * @return
	 * @throws InvalidFormatException
	 */
	public static PresentationMLPackage createPackage() throws InvalidFormatException {
		
		
		// Create a package
		PresentationMLPackage pmlPack = new PresentationMLPackage();

		// Presentation part
		MainPresentationPart pp;
		try {
			
			pp = new MainPresentationPart();
			pp.setJaxbElement(MainPresentationPart.createJaxbPresentationElement() );
			pmlPack.addTargetPart(pp);		
			
			// Slide part
			SlidePart slidePart = new SlidePart();
			pp.addSlideIdListEntry(slidePart);

			slidePart.setJaxbElement( SlidePart.createSld() );
			
			// Slide layout part
			SlideLayoutPart layoutPart = new SlideLayoutPart(); 
			layoutPart.setJaxbElement( SlideLayoutPart.createSldLayout() );
			
			slidePart.addTargetPart(layoutPart);
			
			// Slide Master part
			SlideMasterPart masterPart = new SlideMasterPart();
			pp.addSlideMasterIdListEntry(masterPart);

			masterPart.setJaxbElement(masterPart.createSldMaster() );
			masterPart.addSlideLayoutIdListEntry(layoutPart);
			
			layoutPart.addTargetPart(masterPart);
			
			// Theme part
			ThemePart themePart = new ThemePart(new PartName("/ppt/theme/theme1.xml"));
			java.io.InputStream is = org.docx4j.utils.ResourceUtils.getResource(
						"org/docx4j/openpackaging/parts/PresentationML/theme.xml");
			themePart.unmarshal(is);
			
			// .. add it in 2 places ..
			masterPart.addTargetPart(themePart);
			pp.addTargetPart(themePart);
			
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidFormatException("Couldn't create package", e);
		}
		
		

		// Return the new package
		return pmlPack;
		
	}
	
	private static String SAMPLE_SHAPE = 			
		"<p:sp   xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">"
		+ "<p:nvSpPr>"
		+ "<p:cNvPr id=\"4\" name=\"Title 3\" />"
		+ "<p:cNvSpPr>"
			+ "<a:spLocks noGrp=\"1\" />"
		+ "</p:cNvSpPr>"
		+ "<p:nvPr>"
			+ "<p:ph type=\"title\" />"
		+ "</p:nvPr>"
	+ "</p:nvSpPr>"
	+ "<p:spPr />"
	+ "<p:txBody>"
		+ "<a:bodyPr />"
		+ "<a:lstStyle />"
		+ "<a:p>"
			+ "<a:r>"
				+ "<a:rPr lang=\"en-US\" smtClean=\"0\" />"
				+ "<a:t>Hello World</a:t>"
			+ "</a:r>"
			+ "<a:endParaRPr lang=\"en-US\" />"
		+ "</a:p>"
	+ "</p:txBody>"
+ "</p:sp>";

	
	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/pptx-otherparts.xml";
		
		PresentationMLPackage presentationMLPackage = 
			(PresentationMLPackage)PresentationMLPackage.load(new java.io.File(inputfilepath));		
		
		System.out.println("\n\n saving .. \n\n");
		

		System.out.println("\n\n done .. \n\n");
		
	}	
}
