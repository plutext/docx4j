/*
 *  Copyright 2011, Plutext Pty Ltd.
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

package org.docx4j.openpackaging.parts.DrawingML;

import java.io.File;

import javax.xml.transform.Templates;
import javax.xml.transform.stream.StreamSource;

import org.docx4j.XmlUtils;
import org.docx4j.dml.diagram.CTDiagramDefinition;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.glox4j.openpackaging.packages.GloxPackage;
import org.pptx4j.jaxb.Context;
import org.pptx4j.model.SlideSizesWellKnown;
import org.pptx4j.pml.CTGraphicalObjectFrame;
import org.pptx4j.pml.Presentation;
import org.w3c.dom.Document;

public class CreatePptxWithSmartArt extends CreateWithSmartArtAbstract {

	public CreatePptxWithSmartArt(CTDiagramDefinition diagramLayoutObj,
			Templates layoutTreeCreatorXslt,
			Templates layoutTree2DiagramDataXslt) {

		super( diagramLayoutObj,
				 layoutTreeCreatorXslt,
				 layoutTree2DiagramDataXslt);
	}

	public PresentationMLPackage createSmartArtPkg(
			SlideSizesWellKnown sz, boolean landscape,
			Document xml) throws Exception {
						
		// Create skeletal package, including a MainPresentationPart and a SlideLayoutPart
		PresentationMLPackage pMLPackage = PresentationMLPackage.createPackage(sz, landscape); 
		
		// Need references to these parts to create a slide
		// Please note that these parts *already exist* - they are
		// created by createPackage() above.  See that method
		// for instruction on how to create and add a part.
		MainPresentationPart pp = (MainPresentationPart)pMLPackage.getParts().getParts().get(
				new PartName("/ppt/presentation.xml"));		
		SlideLayoutPart layoutPart = (SlideLayoutPart)pMLPackage.getParts().getParts().get(
				new PartName("/ppt/slideLayouts/slideLayout1.xml"));
		
		// OK, now we can create a slide
		SlidePart slidePart = pMLPackage.createSlidePart(pp, layoutPart, 
				new PartName("/ppt/slides/slide1.xml"));
								
		// Layout part
		DiagramLayoutPart layout = new DiagramLayoutPart(new PartName("/ppt/diagrams.layout1.xml"));
		layout.setJaxbElement(diagramLayoutObj);

		DiagramColorsPart colors = new DiagramColorsPart(new PartName("/ppt/diagrams.colors1.xml"));
		colors.unmarshal("colorsDef-accent1_2.xml");
		//colors.CreateMinimalContent("mycolors");
		
		DiagramStylePart style = new DiagramStylePart(new PartName("/ppt/diagrams.quickStyle1.xml"));
		style.unmarshal("quickStyle-simple1.xml");
		//style.CreateMinimalContent("mystyle");
		
		// DiagramDataPart
		DiagramDataPart data = new DiagramDataPart(new PartName("/ppt/diagrams.data1.xml"));
		data.setPackage(pMLPackage); // otherwise we need to pass pkg around
		data.setJaxbElement( createDiagramData(data, xml) );
		
		String layoutRelId = slidePart.addTargetPart(layout).getId();
		String dataRelId = slidePart.addTargetPart(data).getId();
		String colorsRelId = slidePart.addTargetPart(colors).getId();
		String styleRelId = slidePart.addTargetPart(style).getId();
		
		// Create and add graphicFrame for SmartArt
		Presentation.SldSz tmpSldSz = pMLPackage.getMainPresentationPart().getJaxbElement().getSldSz();
		
		CTGraphicalObjectFrame graphicFrame = createSmartArt( layoutRelId,  dataRelId, colorsRelId,  styleRelId,
				""+(tmpSldSz.getCx()-200000), ""+(tmpSldSz.getCy()-1000000) );
				// A bit smaller, so we can have a margin around the edge

		slidePart.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(
				graphicFrame);
						
		return pMLPackage;
	}
		
	
	public static CTGraphicalObjectFrame createSmartArt(String layoutRelId, String dataRelId, 
			String colorsRelId, String styleRelId, String cx, String cy) throws Exception {
				
        String ml =
        	 "<p:graphicFrame   xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">"
            + "<p:nvGraphicFramePr>"
            + "<p:cNvPr id=\"4\" name=\"Diagram 3\"/>"
            + "<p:cNvGraphicFramePr/>"
            + "<p:nvPr/>"
          + "</p:nvGraphicFramePr>"
          + "<p:xfrm>"
          + "<a:off x=\"100000\" y=\"900000\"/>" // Room at the top for chart title
	        + "<a:ext cx=\"${cx}\" cy=\"${cy}\"/>"
          + "</p:xfrm>"
          + "<a:graphic>"
            + "<a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/diagram\">"
              + "<dgm:relIds  r:dm=\"${dataRelId}\" r:lo=\"${layoutRelId}\" r:qs=\"${styleRelId}\" r:cs=\"${colorsRelId}\" xmlns:dgm=\"http://schemas.openxmlformats.org/drawingml/2006/diagram\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" />"
            + "</a:graphicData>"
          + "</a:graphic>"
        + "</p:graphicFrame>"; 
        	
        java.util.HashMap<String, String>mappings = new java.util.HashMap<String, String>();
        
        mappings.put("layoutRelId", layoutRelId);
        mappings.put("dataRelId", dataRelId);
        mappings.put("colorsRelId", colorsRelId);
        mappings.put("styleRelId", styleRelId);
        mappings.put("cx", cx);
        mappings.put("cy", cy);        

        return (CTGraphicalObjectFrame)org.docx4j.XmlUtils.unmarshallFromTemplate(ml, mappings, Context.jcPML,
        		CTGraphicalObjectFrame.class) ;        
	}

	public static void main(String[] args) throws Exception {
		
		// Need the source doc as a DOM for later, and also
		// as XSLT input
		Document doc = XmlUtils.getNewDocumentBuilder().parse(
				new File(System.getProperty("user.dir")+ "/sample-docs/glox/extracted/data-sample.xml"  ) );		
						
		GloxPackage gloxPackage = (GloxPackage)OpcPackage.load(
				new File(System.getProperty("user.dir")+ "/sample-docs/glox/extracted/CirclePictureHierarchy.glox"  ) );
	
		CTDiagramDefinition diagramLayoutObj = gloxPackage.getDiagramLayoutPart().getJaxbElement();
	
		Templates layoutTreeCreatorXslt =
				DiagramLayoutPart.generateLayoutTreeXSLT(
						diagramLayoutObj);
	
		Templates layoutTree2DiagramDataXslt = XmlUtils.getTransformerTemplate(
				new StreamSource(
					org.docx4j.utils.ResourceUtils.getResource(
						"org/docx4j/openpackaging/parts/DrawingML/DiagramLayoutTree4AlgHier.xslt")));
		
		CreatePptxWithSmartArt creatorPptx = new CreatePptxWithSmartArt(diagramLayoutObj, layoutTreeCreatorXslt, layoutTree2DiagramDataXslt);
		

		PresentationMLPackage pkg = creatorPptx.createSmartArtPkg(SlideSizesWellKnown.A3, true, doc);
		
		SaveToZipFile saver = new SaveToZipFile(pkg);
		saver.save(new File(System.getProperty("user.dir")+ "/OUT.pptx"  ) );
		System.out.println("Done!");
	}
}
