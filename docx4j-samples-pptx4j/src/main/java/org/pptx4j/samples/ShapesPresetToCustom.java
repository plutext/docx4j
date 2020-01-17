package org.pptx4j.samples;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.docx4j.dml.CTCustomGeometry2D;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.STShapeType;
import org.docx4j.jaxb.XPathBinderAssociationIsPartialException;
import org.docx4j.model.shapes.PresetGeometries;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.pptx4j.Pptx4jException;


/**
 * Converts predefined shapes (eg <a:prstGeom prst="moon">) to equivalent a:custGeom.
 * 
 * See further http://blogs.msdn.com/b/openspecification/archive/2011/11/14/how-to-use-the-presetshapedefinitions-xml-file-and-fun-with-drawingml.aspx
 * 
 * Note: leftArrow corrupts the pptx, so it is not converted here.  Someone motivated might explore why?
 * 
 * @author jharrop
 */
public class ShapesPresetToCustom {
	
	public static void main(String[] args) throws Docx4JException, Pptx4jException, JAXBException {
		
		
		// A pptx containing all shapes, created using the AutoShapes sample
		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/AutoShapes.pptx";
		
		PresentationMLPackage presentationMLPackage = 
			(PresentationMLPackage)OpcPackage.load(new java.io.File(inputfilepath));

		int slideCount = presentationMLPackage.getMainPresentationPart().getSlideCount();
		
		for (int i=0; i<slideCount; i++) {
		
			System.out.println("\n Slide " + i);
			convert( presentationMLPackage.getMainPresentationPart().getSlide(i) );
		}

		presentationMLPackage.save(
				new File(System.getProperty("user.dir") + "/OUT_ShapesPresetToCustom.pptx"));
	}
	
	private static void convert(SlidePart slide) throws XPathBinderAssociationIsPartialException, JAXBException {
		
		// We don't have traversal infrastructure for slides right now,
		// so use XPath
		
		List<Object> results = slide.getJAXBNodesViaXPath("//p:spPr", false);
		
		for(Object o: results) {
			
			//System.out.println(o.getClass().getName());
			CTShapeProperties spPr = (CTShapeProperties)o;
			
			if (spPr.getPrstGeom()==null) {
				System.out.println("- this shape not preset");
			} else {
				STShapeType shapeType = spPr.getPrstGeom().getPrst();
				
				CTCustomGeometry2D customGeo = PresetGeometries.getInstance().get(shapeType.value());
				
				if (customGeo==null) {
					System.out.println("- definition MISSING for " + shapeType.value() );	
				} else if (shapeType.value().equals("leftArrow") ) {
					System.out.println("- skipping " + shapeType.value() + " (corrupts pptx)");	// tested 2010 x64; I wonder why									
				} else {
					// really should clone here
					spPr.setCustGeom(customGeo);
					
					spPr.setPrstGeom(null);
					System.out.println("- " + shapeType.value() + " processed");					
					
				}
				
			}
			
		}
		
	}

}
