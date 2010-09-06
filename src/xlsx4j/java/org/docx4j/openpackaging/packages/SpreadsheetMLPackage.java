/*
 *  Copyright 2010, Plutext Pty Ltd.
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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.model.styles.StyleTree;
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
import org.docx4j.wml.Style;
import org.pptx4j.convert.out.svginhtml.SvgExporter;
import org.pptx4j.jaxb.Context;
import org.pptx4j.model.ResolvedLayout;
import org.pptx4j.model.ShapeWrapper;
import org.pptx4j.model.TextStyles;
import org.pptx4j.pml.GroupShape;
import org.pptx4j.pml.SldLayout;



/**
 * @author jharrop
 *
 */
public class SpreadsheetMLPackage extends OpcPackage {
	
	protected static Logger log = Logger.getLogger(PresentationMLPackage.class);
		
	
	/**
	 * Constructor.  Also creates a new content type manager
	 * 
	 */	
	public SpreadsheetMLPackage() {
		super();
		setContentType(new ContentType(ContentTypes.PRESENTATIONML_MAIN)); 		
	}
	/**
	 * Constructor.
	 *  
	 * @param contentTypeManager
	 *            The content type manager to use 
	 */
	public SpreadsheetMLPackage(ContentTypeManager contentTypeManager) {
		super(contentTypeManager);
		setContentType(new ContentType(ContentTypes.PRESENTATIONML_MAIN));
	}
	
	
	/**
	 * Convenience method to create a PresentationMLPackage
	 * from an existing File (.pptx or .xml Flat OPC).
     *
	 * @param pptxFile
	 *            The pptx file 
	 */	
	public static PresentationMLPackage load(java.io.File pptxFile) throws Docx4JException {
		
		return (PresentationMLPackage)OpcPackage.load(pptxFile);
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
			
//			// Slide part
//			SlidePart slidePart = new SlidePart();
//			pp.addSlideIdListEntry(slidePart);
//
//			slidePart.setJaxbElement( SlidePart.createSld() );
			
			// Slide layout part
			SlideLayoutPart layoutPart = new SlideLayoutPart(); 
			layoutPart.setJaxbElement( SlideLayoutPart.createSldLayout() );
			
//			slidePart.addTargetPart(layoutPart);
			
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
	
	/**
	 * Create a slide and add it to the package
	 * 
	 * @param pp
	 * @param layoutPart
	 * @param partName
	 * @return the slide
	 * @throws InvalidFormatException
	 * @throws JAXBException
	 */
	public static SlidePart createSlidePart(MainPresentationPart pp, SlideLayoutPart layoutPart, PartName partName) 
		throws InvalidFormatException, JAXBException {
		
		// Slide part
		SlidePart slidePart = new SlidePart(partName);
		pp.addSlideIdListEntry(slidePart);

		slidePart.setJaxbElement( SlidePart.createSld() );
		
		// Slide layout part
		slidePart.addTargetPart(layoutPart);
		
		return slidePart;
	}
	
	
	
	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/pptx/pptx-basic.xml";
		
		PresentationMLPackage presentationMLPackage = 
			(PresentationMLPackage)PresentationMLPackage.load(new java.io.File(inputfilepath));		

		
//		ThemePart tp = (ThemePart)presentationMLPackage.getParts().getParts().get(
//				new PartName("/ppt/theme/theme1.xml"));
//		FontScheme fontScheme = tp.getFontScheme();
//		List<Style> styles = new ArrayList<Style>();
//		
//		// presentation.xml
//		MainPresentationPart pp = (MainPresentationPart)presentationMLPackage.getParts().getParts().get(
//				new PartName("/ppt/presentation.xml"));
//		styles.addAll(
//				TextStyles.generateWordStylesFromPresentationPart(
//						pp.getJaxbElement().getDefaultTextStyle(),
//						"", fontScheme));
//
//		// master
//		SlideMasterPart master = (SlideMasterPart)presentationMLPackage.getParts().getParts().get(
//				new PartName("/ppt/slideMasters/slideMaster1.xml"));
//		styles.addAll(
//				TextStyles.generateWordStylesForMaster(
//						master.getJaxbElement().getTxStyles(), 
//						1, fontScheme));
		
		Iterator partIterator = presentationMLPackage.getParts().getParts().entrySet().iterator();
	    while (partIterator.hasNext()) {
	    	
	        Map.Entry pairs = (Map.Entry)partIterator.next();
	        
	        Part p = (Part)pairs.getValue();
	        if (p instanceof SlidePart) {

//	        	ResolvedLayout rl = ((SlidePart)p).getResolvedLayout();		        	
//	        	System.out.println( XmlUtils.marshaltoString(rl.getShapeTree(), false, true, Context.jcPML,
//	        			"http://schemas.openxmlformats.org/presentationml/2006/main", "spTree", GroupShape.class) );
	        	
	        	System.out.println(
	        			SvgExporter.svg(presentationMLPackage, (SlidePart)p)
	        			);
	        }
	    }
		
		
//		System.out.println(presentationMLPackage.getParts().getParts().size());
//		Map<String, ShapeWrapper> index = ShapeWrapper.indexPlaceHolders(	presentationMLPackage.getParts().getParts());
//		
//		SlidePart slidePart = (SlidePart)presentationMLPackage.getParts().getParts().get(
//				new PartName("/ppt/slides/slide1.xml"));
//
//		GroupShape shapeTree = slidePart.getEffectiveShapeTree( index );
//		
//		System.out.println( XmlUtils.marshaltoString(shapeTree, true, Context.jcPML));
		
		System.out.println("\n\n done .. \n\n");
		
	}	
}
