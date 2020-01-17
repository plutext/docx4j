package org.glox4j.samples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.XmlUtils;
import org.docx4j.dml.diagram.CTDataModel;
import org.docx4j.dml.diagram.CTElemPropSet;
import org.docx4j.dml.diagram.ObjectFactory;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.DrawingML.DiagramColorsPart;
import org.docx4j.openpackaging.parts.DrawingML.DiagramDataPart;
import org.docx4j.openpackaging.parts.DrawingML.DiagramLayoutPart;
import org.docx4j.openpackaging.parts.DrawingML.DiagramStylePart;
import org.docx4j.wml.P;
import org.glox4j.openpackaging.packages.GloxPackage;

/**
 * Create a docx containing SmartArt
 * based on the sample data in the
 * specified glox file.
 *
 * @author jharrop
 *
 */
public class AddGloxToDocx {

	private static Logger log = LoggerFactory.getLogger(AddGloxToDocx.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/Hier2Level.glox";
//			inputfilepath = System.getProperty("user.dir") + "/sample-docs/extracted/SmartArt-BasicChevronProcess.pptx.glox";
//		String inputfilepath = System.getProperty("user.dir") + "/RectTimeline2.glox";


		// Make a basic docx
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		wordMLPackage.getMainDocumentPart()
			.addStyledParagraphOfText("Title", "Hello world");
		wordMLPackage.getMainDocumentPart().addParagraphOfText("from docx4j!");

		// Now add the SmartArt parts from the glox
		GloxPackage gloxPackage = GloxPackage.load(new java.io.File(inputfilepath));
		ObjectFactory factory = new ObjectFactory();


		// Layout part
		DiagramLayoutPart layout = new DiagramLayoutPart();
		layout.setJaxbElement(gloxPackage.getDiagramLayoutPart().getJaxbElement());
		gloxPackage.getDiagramLayoutPart().getJaxbElement().setUniqueId("mylayout");

		DiagramColorsPart colors = new DiagramColorsPart();
		colors.unmarshal("colorsDef-accent1_2.xml");
		//colors.CreateMinimalContent("mycolors");

		DiagramStylePart style = new DiagramStylePart();
		style.unmarshal("quickStyle-simple1.xml");
		//style.CreateMinimalContent("mystyle");

		// DiagramDataPart
		DiagramDataPart data = new DiagramDataPart();

		// Get the sample data from dgm:sampData
		if (gloxPackage.getDiagramLayoutPart().getJaxbElement().getSampData()==null) {
			log.error("Sample data missing!");
			return;
		}
		CTDataModel sampleDataModel = gloxPackage.getDiagramLayoutPart().getJaxbElement().getSampData().getDataModel();

		// If there is none, this sample won't work
		if (sampleDataModel==null
				|| sampleDataModel.getPtLst()==null
				|| sampleDataModel.getPtLst().getPt().size()==0) {
			System.out.println("No sample data in this glox, so can't create demo docx");
			return;
			// TODO: in this case, try generating our own sample data?
		}

		CTDataModel clonedDataModel = XmlUtils.deepCopy((CTDataModel)sampleDataModel);
		data.setJaxbElement( clonedDataModel );

        /* <dgm:pt modelId="1" type="doc">
		        <dgm:prSet
		            loTypeId="mylayout"
		            qsTypeId="mystyle"
		            csTypeId="mycolors" />
		    </dgm:pt> */
		CTElemPropSet prSet = factory.createCTElemPropSet();
		prSet.setLoTypeId("mylayout");
		prSet.setQsTypeId(style.getJaxbElement().getUniqueId());
		prSet.setCsTypeId(colors.getJaxbElement().getUniqueId());

		clonedDataModel.getPtLst().getPt().get(0).setPrSet(prSet);

		String layoutRelId = wordMLPackage.getMainDocumentPart().addTargetPart(layout).getId();
		String dataRelId = wordMLPackage.getMainDocumentPart().addTargetPart(data).getId();
		String colorsRelId = wordMLPackage.getMainDocumentPart().addTargetPart(colors).getId();
		String styleRelId = wordMLPackage.getMainDocumentPart().addTargetPart(style).getId();

		// Now use it in the docx
		wordMLPackage.getMainDocumentPart().addObject(
				createSmartArt( layoutRelId,  dataRelId, colorsRelId,  styleRelId));


		wordMLPackage.save(new java.io.File(
				System.getProperty("user.dir") + "/glox-p1.docx" ) );

		System.out.println("Done!");

	}

	public static P createSmartArt(String layoutRelId, String dataRelId,
			String colorsRelId, String styleRelId) throws Exception {


		// \"${filenameHint}\"

        String ml = "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
        	  + "<w:r>"
        	    + "<w:rPr>"
        	      + "<w:noProof/>"
        	      + "<w:lang w:eastAsia=\"en-AU\"/>"
        	    + "</w:rPr>"
        	    + "<w:drawing>"
        	      + "<wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\" xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" >"
        	        + "<wp:extent cx=\"5486400\" cy=\"3200400\"/>"
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

        return (P)org.docx4j.XmlUtils.unmarshallFromTemplate(ml, mappings ) ;
	}

}
