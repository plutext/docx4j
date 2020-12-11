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

import org.docx4j.UnitsOfMeasurement;
import org.docx4j.XmlUtils;
import org.docx4j.dml.diagram.CTDiagramDefinition;
import org.docx4j.model.structure.MarginsWellKnown;
import org.docx4j.model.structure.PageDimensions;
import org.docx4j.model.structure.PageSizePaper;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.SectPr.PgSz;
import org.glox4j.openpackaging.packages.GloxPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class CreateDocxWithSmartArt extends CreateWithSmartArtAbstract {
	
	private static Logger log = LoggerFactory.getLogger(CreateDocxWithSmartArt.class);		
	

	public CreateDocxWithSmartArt(CTDiagramDefinition diagramLayoutObj,
			Templates layoutTreeCreatorXslt,
			Templates layoutTree2DiagramDataXslt) {

		super( diagramLayoutObj,
				 layoutTreeCreatorXslt,
				 layoutTree2DiagramDataXslt);
	}

	/**createSmartArtDocx from the provided XML.  
	 * 
	 * @param sz
	 * @param landscape
	 * @param margins
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	public WordprocessingMLPackage createSmartArtDocx(
			PageSizePaper sz, boolean landscape,
			MarginsWellKnown margins, 
			Document xml) throws Exception {
						
		// Make a basic docx
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage(sz, landscape);		
				
		// Layout part
		DiagramLayoutPart layout = new DiagramLayoutPart();
		layout.setJaxbElement(diagramLayoutObj);

		DiagramColorsPart colors = new DiagramColorsPart();
		colors.unmarshal("colorsDef-accent1_2.xml");
		//colors.CreateMinimalContent("mycolors");
		
		DiagramStylePart style = new DiagramStylePart();
		style.unmarshal("quickStyle-simple1.xml");
		//style.CreateMinimalContent("mystyle");
		
		// DiagramDataPart
		DiagramDataPart data = new DiagramDataPart();
		data.setPackage(wordMLPackage); // otherwise we need to pass pkg around
		data.setJaxbElement( createDiagramData(data, xml) );
		
		String layoutRelId = wordMLPackage.getMainDocumentPart().addTargetPart(layout).getId();
		String dataRelId = wordMLPackage.getMainDocumentPart().addTargetPart(data).getId();
		String colorsRelId = wordMLPackage.getMainDocumentPart().addTargetPart(colors).getId();
		String styleRelId = wordMLPackage.getMainDocumentPart().addTargetPart(style).getId();

		// Occupy entire page, less margins
		PageDimensions pd = new PageDimensions();
		pd.setPgSize(sz, landscape );
		PgSz pgSz = pd.getPgSz(); 
		pd.setMargins(margins);
		String cx =  ""+UnitsOfMeasurement.twipToEMU(pgSz.getW().intValue() 
				- (pd.getPgMar().getLeft().intValue()+pd.getPgMar().getRight().intValue() ) );  //"5486400";
		String cy = ""+UnitsOfMeasurement.twipToEMU(pgSz.getH().intValue() 
				- (pd.getPgMar().getTop().intValue()+pd.getPgMar().getBottom().intValue() ));   //"3200400";
		
		// Now use it in the docx
		wordMLPackage.getMainDocumentPart().addObject(
				createSmartArt( layoutRelId,  dataRelId, colorsRelId,  styleRelId, cx, cy)); 
		
		return wordMLPackage;
	}
		
	public static P createSmartArt(String layoutRelId, String dataRelId, 
			String colorsRelId, String styleRelId, String cx, String cy) throws Exception {
				
        String ml = "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
        	  + "<w:r>"
        	    + "<w:rPr>"
        	      + "<w:noProof/>"
        	      + "<w:lang w:eastAsia=\"en-AU\"/>"
        	    + "</w:rPr>"
        	    + "<w:drawing>"
        	      + "<wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\" xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" >"
        	        + "<wp:extent cx=\"${cx}\" cy=\"${cy}\"/>"
        	        + "<wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\"/>"
        	        + "<wp:docPr id=\"1\" name=\"Diagram 1\"/>"
        	        + "<wp:cNvGraphicFramePr/>"
        	        + "<a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
        	          + "<a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/diagram\">"
        	            + "<dgm:relIds r:dm=\"${dataRelId}\" r:lo=\"${layoutRelId}\" r:qs=\"${styleRelId}\" r:cs=\"${colorsRelId}\" xmlns:dgm=\"http://schemas.openxmlformats.org/drawingml/2006/diagram\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"/>"
        	          + "</a:graphicData>"
        	        + "</a:graphic>"
        	      + "</wp:inline>"
        	    + "</w:drawing>"
        	  + "</w:r>"
        	+ "</w:p>";

        java.util.HashMap<String, String>mappings = new java.util.HashMap<String, String>();
        
        mappings.put("layoutRelId", layoutRelId);
        mappings.put("dataRelId", dataRelId);
        mappings.put("colorsRelId", colorsRelId);
        mappings.put("styleRelId", styleRelId);
        mappings.put("cx", cx);
        mappings.put("cy", cy);

        return (P)org.docx4j.XmlUtils.unmarshallFromTemplate(ml, mappings ) ;        
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
		
		CreateDocxWithSmartArt creatorDocx = new CreateDocxWithSmartArt(diagramLayoutObj, layoutTreeCreatorXslt, layoutTree2DiagramDataXslt);
		WordprocessingMLPackage pkg = creatorDocx.createSmartArtDocx(PageSizePaper.A3, true, MarginsWellKnown.NORMAL, doc);
		
		SaveToZipFile saver = new SaveToZipFile(pkg);
		saver.save(new File(System.getProperty("user.dir")+ "/OUT.docx"  ) );
		System.out.println("Done!");
	}
}
