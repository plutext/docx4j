package org.glox4j.samples;

import java.util.Map.Entry;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.XmlUtils;
import org.docx4j.dml.diagram.CTDataModel;
import org.docx4j.dml.diagram.CTDescription;
import org.docx4j.dml.diagram.CTDiagramDefinitionHeader;
import org.docx4j.dml.diagram.CTElemPropSet;
import org.docx4j.dml.diagram.CTName;
import org.docx4j.dml.diagram.CTPt;
import org.docx4j.dml.diagram.CTSampleData;
import org.docx4j.dml.diagram.ObjectFactory;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.DrawingML.DiagramColorsPart;
import org.docx4j.openpackaging.parts.DrawingML.DiagramDataPart;
import org.docx4j.openpackaging.parts.DrawingML.DiagramLayoutHeaderPart;
import org.docx4j.openpackaging.parts.DrawingML.DiagramLayoutPart;
import org.docx4j.openpackaging.parts.DrawingML.DiagramStylePart;
import org.docx4j.wml.P;
import org.glox4j.openpackaging.packages.GloxPackage;

/**
 * This sample can be used to generate a
 * SmartArt glox file from an exemplar
 * docx or pptx containing SmartArt.
 * 
 * The exemplar is assumed to contain
 * only one piece of SmartArt.
 * 
 * @author jharrop
 *
 */
public class ExtractGloxFromExemplar {
	
	private static Logger log = LoggerFactory.getLogger(ExtractGloxFromExemplar.class);						

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/Hier2Level.glox";
//		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/extracted/CirclePictureHierarchy.docx";
		String outputdir = inputfilepath.substring(0, inputfilepath.lastIndexOf("/")+1);
		String outputfilename = inputfilepath.substring(inputfilepath.lastIndexOf("/"))+ ".glox";
		
		OpcPackage opcPackage = OpcPackage.load(new java.io.File(inputfilepath));		
		
		GloxPackage gloxPackage = new GloxPackage();		
		
		// DiagramLayoutPart - from the exemplar docx or pptx	
		// .. just the first one we encounter
		DiagramLayoutPart source = null;
		for (Entry<PartName,Part> entry : opcPackage.getParts().getParts().entrySet() ) {
			
			if (entry.getValue().getContentType().equals( 
					ContentTypes.DRAWINGML_DIAGRAM_LAYOUT )) {
				source = (DiagramLayoutPart)entry.getValue();
				break;
			}
		}
		if (source==null) {
			System.out.println("No SmartArt found in " + inputfilepath);
			return;	
		}

		// .. don't bother cloning, just attach it
		DiagramLayoutPart target = new DiagramLayoutPart(new PartName("/diagrams/layout1.xml"));
		target.setJaxbElement(
				XmlUtils.deepCopy(source.getJaxbElement()) );
		gloxPackage.addTargetPart(target);
		
		// DiagramLayoutHeaderPart
		DiagramLayoutHeaderPart diagramLayoutHeaderPart = new DiagramLayoutHeaderPart();
		ObjectFactory factory = new ObjectFactory();
		CTDiagramDefinitionHeader header = factory.createCTDiagramDefinitionHeader();
		diagramLayoutHeaderPart.setJaxbElement(header);
		
		String uniqueId = target.getJaxbElement().getUniqueId(); 
		if (uniqueId!=null) {
			header.setUniqueId( uniqueId );
			System.out.println("Creating glox for " + uniqueId);
			
			// Can we make a filename out of this?
			if (uniqueId.indexOf("/")>0 
					&& uniqueId.lastIndexOf("/")!= uniqueId.length()-1 ) {
				outputfilename = uniqueId.substring(uniqueId.lastIndexOf("/")+1) + ".glox";
			}
		}
		
		if (target.getJaxbElement().getTitle()==null
				|| target.getJaxbElement().getTitle().isEmpty()
				|| (target.getJaxbElement().getTitle().size()==1
						&& target.getJaxbElement().getTitle().get(0).getVal().isEmpty())) {
			CTName title = factory.createCTName();
			title.setVal("some title");
			header.getTitle().add(title);
		} else {
			header.getTitle().addAll(target.getJaxbElement().getTitle() );			
		}

		if (target.getJaxbElement().getDesc()==null
				|| target.getJaxbElement().getDesc().isEmpty()
				|| (target.getJaxbElement().getDesc().size()==1
						&& target.getJaxbElement().getDesc().get(0).getVal().isEmpty())) {
			CTDescription desc = factory.createCTDescription();
			desc.setVal("some desc");
			header.getDesc().add(desc);
		} else {
			header.getDesc().addAll(target.getJaxbElement().getDesc() );			
		}
		
		gloxPackage.addTargetPart(diagramLayoutHeaderPart);
		

		// All done..
		String outfile = outputdir + outputfilename;
		System.out.println("Writing " + outfile);
		gloxPackage.save(new java.io.File(outfile ));
		
		System.out.println("Done!");
		
	}
	
	
}
