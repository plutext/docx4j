package org.xlsx4j.samples;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.docx4j.XmlUtils;
import org.docx4j.dml.CTBlip;
import org.docx4j.dml.CTBlipFillProperties;
import org.docx4j.dml.CTGeomGuideList;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTNonVisualPictureProperties;
import org.docx4j.dml.CTOfficeArtExtension;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.docx4j.dml.CTPictureLocking;
import org.docx4j.dml.CTPoint2D;
import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.dml.CTPresetGeometry2D;
import org.docx4j.dml.CTRelativeRect;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTStretchInfoProperties;
import org.docx4j.dml.CTTransform2D;
import org.docx4j.dml.spreadsheetdrawing.CTAnchorClientData;
import org.docx4j.dml.spreadsheetdrawing.CTDrawing;
import org.docx4j.dml.spreadsheetdrawing.CTMarker;
import org.docx4j.dml.spreadsheetdrawing.CTPicture;
import org.docx4j.dml.spreadsheetdrawing.CTPictureNonVisual;
import org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.DrawingML.Drawing;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.relationships.Relationship;

/**
 * Example showing how to add an image to a worksheet
 * 
 * @author jharrop
 *
 */
public class Xlsx4JAddImage {
	
	public static void main(String[] args) throws Exception {
	
		String outputfilepath = System.getProperty("user.dir") + "/OUT_Xlsx4JAddImage.xlsx";
		String imagefilePath = System.getProperty("user.dir")+"/src/test/resources/images/greentick.png" ;
		
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.createPackage();
		WorksheetPart worksheet = pkg.createWorksheetPart(new PartName("/xl/worksheets/sheet1.xml"), "Sheet1", 1);

		// Create Drawing part and add to sheet 
		Drawing drawingPart = new Drawing();
		Relationship drawingRel = worksheet.addTargetPart(drawingPart);

		// Add anchor XML to worksheet
		org.xlsx4j.sml.CTDrawing drawing = org.xlsx4j.jaxb.Context.getsmlObjectFactory().createCTDrawing(); 
		    worksheet.getJaxbElement().setDrawing(drawing); 
		        drawing.setId( drawingRel.getId() ); 		
		
		// Create image part and add to Drawing part
        BinaryPartAbstractImage imagePart 
        	= BinaryPartAbstractImage.createImagePart(pkg, drawingPart, 
        			FileUtils.readFileToByteArray(new File(imagefilePath) ));
        String imageRelID = imagePart.getSourceRelationship().getId();
		
		// Create and set drawing part content
        // Take your pick ..
        // .. build it using code
//        drawingPart.setJaxbElement(
//        		buildDrawingPartContentUsingCode(imageRelID));
        // .. or build it from an XML string
        drawingPart.setJaxbElement(
        		buildDrawingPartContentFromXmlString(imageRelID));
		
		
        // Save the xlsx
		SaveToZipFile saver = new SaveToZipFile(pkg);
		saver.save(outputfilepath);
		System.out.println("\n\n done .. " + outputfilepath);	
	}
	
	
	/**
	 * This code generated using http://webapp.docx4java.org/OnlineDemo/PartsList.html
	 * "Method 1"
	 */
	public static CTDrawing buildDrawingPartContentUsingCode(String imageRelID) {

		org.docx4j.dml.spreadsheetdrawing.ObjectFactory dmlspreadsheetdrawingObjectFactory = new org.docx4j.dml.spreadsheetdrawing.ObjectFactory();
		
		CTDrawing drawing = dmlspreadsheetdrawingObjectFactory.createCTDrawing(); 
//		JAXBElement<org.docx4j.dml.spreadsheetdrawing.CTDrawing> drawingWrapped = dmlspreadsheetdrawingObjectFactory.createWsDr(drawing); 
		    // Create object for twoCellAnchor
		    CTTwoCellAnchor twocellanchor = dmlspreadsheetdrawingObjectFactory.createCTTwoCellAnchor(); 
		    drawing.getEGAnchor().add( twocellanchor); 
		        // Create object for clientData
		        CTAnchorClientData anchorclientdata = dmlspreadsheetdrawingObjectFactory.createCTAnchorClientData(); 
		        twocellanchor.setClientData(anchorclientdata); 
		        // Create object for pic
		        CTPicture picture = dmlspreadsheetdrawingObjectFactory.createCTPicture(); 
		        twocellanchor.setPic(picture); 
		org.docx4j.dml.ObjectFactory dmlObjectFactory = new org.docx4j.dml.ObjectFactory();
		            // Create object for blipFill
		            CTBlipFillProperties blipfillproperties = dmlObjectFactory.createCTBlipFillProperties(); 
		            picture.setBlipFill(blipfillproperties); 
		                // Create object for blip
		                CTBlip blip = dmlObjectFactory.createCTBlip(); 
		                blipfillproperties.setBlip(blip); 
		                    blip.setCstate(org.docx4j.dml.STBlipCompression.NONE);
		                    blip.setEmbed( imageRelID ); 
		                    // Create object for extLst
		                    CTOfficeArtExtensionList officeartextensionlist = dmlObjectFactory.createCTOfficeArtExtensionList(); 
		                    blip.setExtLst(officeartextensionlist); 
		                        // Create object for ext
		                        CTOfficeArtExtension officeartextension = dmlObjectFactory.createCTOfficeArtExtension(); 
		                        officeartextensionlist.getExt().add( officeartextension); 
		                            officeartextension.setUri( "{28A0092B-C50C-407E-A947-70E740481C1C}"); 
		                    blip.setLink( ""); 
		                // Create object for stretch
		                CTStretchInfoProperties stretchinfoproperties = dmlObjectFactory.createCTStretchInfoProperties(); 
		                blipfillproperties.setStretch(stretchinfoproperties); 
		                    // Create object for fillRect
		                    CTRelativeRect relativerect = dmlObjectFactory.createCTRelativeRect(); 
		                    stretchinfoproperties.setFillRect(relativerect); 
		                        relativerect.setR( new Integer(0) );
		                        relativerect.setT( new Integer(0) );
		                        relativerect.setL( new Integer(0) );
		                        relativerect.setB( new Integer(0) );
		            // Create object for spPr
		            CTShapeProperties shapeproperties = dmlObjectFactory.createCTShapeProperties(); 
		            picture.setSpPr(shapeproperties); 
		                // Create object for xfrm
		                CTTransform2D transform2d = dmlObjectFactory.createCTTransform2D(); 
		                shapeproperties.setXfrm(transform2d); 
		                    transform2d.setRot( new Integer(0) );
		                    // Create object for off
		                    CTPoint2D point2d = dmlObjectFactory.createCTPoint2D(); 
		                    transform2d.setOff(point2d); 
		                        point2d.setY( 0 );
		                        point2d.setX( 0 );
		                    // Create object for ext
		                    CTPositiveSize2D positivesize2d = dmlObjectFactory.createCTPositiveSize2D(); 
		                    transform2d.setExt(positivesize2d); 
		                        positivesize2d.setCx( 714375 );
		                        positivesize2d.setCy( 714375 );
		                // Create object for prstGeom
		                CTPresetGeometry2D presetgeometry2d = dmlObjectFactory.createCTPresetGeometry2D(); 
		                shapeproperties.setPrstGeom(presetgeometry2d); 
		                    // Create object for avLst
		                    CTGeomGuideList geomguidelist = dmlObjectFactory.createCTGeomGuideList(); 
		                    presetgeometry2d.setAvLst(geomguidelist); 
		                    presetgeometry2d.setPrst(org.docx4j.dml.STShapeType.RECT);
		            // Create object for nvPicPr
		            CTPictureNonVisual picturenonvisual = dmlspreadsheetdrawingObjectFactory.createCTPictureNonVisual(); 
		            picture.setNvPicPr(picturenonvisual); 
		                // Create object for cNvPr
		                CTNonVisualDrawingProps nonvisualdrawingprops = dmlObjectFactory.createCTNonVisualDrawingProps(); 
		                picturenonvisual.setCNvPr(nonvisualdrawingprops); 
		                    nonvisualdrawingprops.setDescr( ""); 
		                    nonvisualdrawingprops.setName( "Picture 1"); 
		                    nonvisualdrawingprops.setId( 2 );
		                // Create object for cNvPicPr
		                CTNonVisualPictureProperties nonvisualpictureproperties = dmlObjectFactory.createCTNonVisualPictureProperties(); 
		                picturenonvisual.setCNvPicPr(nonvisualpictureproperties); 
		                    // Create object for picLocks
		                    CTPictureLocking picturelocking = dmlObjectFactory.createCTPictureLocking(); 
		                    nonvisualpictureproperties.setPicLocks(picturelocking); 
		            picture.setMacro( ""); 
		        // Create object for to
		        CTMarker marker = dmlspreadsheetdrawingObjectFactory.createCTMarker(); 
		        twocellanchor.setTo(marker); 
		        	marker.setCol(1);
		            marker.setColOff( 104775 );
		        	marker.setRow(3);
		            marker.setRowOff( 142875 );
		        // Create object for from
		        CTMarker marker2 = dmlspreadsheetdrawingObjectFactory.createCTMarker(); 
		        twocellanchor.setFrom(marker2); 
		        	marker2.setCol(0);
		            marker2.setColOff( 0 );
		        	marker2.setRow(0);
		            marker2.setRowOff( 0 );
		        twocellanchor.setEditAs(org.docx4j.dml.spreadsheetdrawing.STEditAs.ONE_CELL);

//		return drawingWrapped;
		        return drawing;
		}	

	/**
	 * This code generated using http://webapp.docx4java.org/OnlineDemo/PartsList.html
	 * "Method 2"
	 */
	public static CTDrawing buildDrawingPartContentFromXmlString(String imageRelID) throws JAXBException {
		
		String openXML = "<xdr:wsDr xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:a14=\"http://schemas.microsoft.com/office/drawing/2010/main\" xmlns:xdr=\"http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
	            + "<xdr:twoCellAnchor editAs=\"oneCell\">"
	                + "<xdr:from>"
	                    + "<xdr:col>0</xdr:col>"
	                    + "<xdr:colOff>0</xdr:colOff>"
	                    + "<xdr:row>0</xdr:row>"
	                    + "<xdr:rowOff>0</xdr:rowOff>"
	                +"</xdr:from>"
	                + "<xdr:to>"
	                    + "<xdr:col>1</xdr:col>"
	                    + "<xdr:colOff>104775</xdr:colOff>"
	                    + "<xdr:row>3</xdr:row>"
	                    + "<xdr:rowOff>142875</xdr:rowOff>"
	                +"</xdr:to>"
	                + "<xdr:pic>"
	                    + "<xdr:nvPicPr>"
	                        + "<xdr:cNvPr id=\"2\" name=\"Picture 1\"/>"
	                        + "<xdr:cNvPicPr>"
	                            + "<a:picLocks noChangeAspect=\"1\"/>"
	                        +"</xdr:cNvPicPr>"
	                    +"</xdr:nvPicPr>"
	                    + "<xdr:blipFill>"
	                        + "<a:blip r:embed=\"" + imageRelID + "\">"
	                            + "<a:extLst>"
	                                + "<a:ext uri=\"{28A0092B-C50C-407E-A947-70E740481C1C}\">"
	                                    + "<a14:useLocalDpi val=\"0\"/>"
	                                +"</a:ext>"
	                            +"</a:extLst>"
	                        +"</a:blip>"
	                        + "<a:stretch>"
	                            + "<a:fillRect/>"
	                        +"</a:stretch>"
	                    +"</xdr:blipFill>"
	                    + "<xdr:spPr>"
	                        + "<a:xfrm>"
	                            + "<a:off x=\"0\" y=\"0\"/>"
	                            + "<a:ext cx=\"714375\" cy=\"714375\"/>"
	                        +"</a:xfrm>"
	                        + "<a:prstGeom prst=\"rect\">"
	                            + "<a:avLst/>"
	                        +"</a:prstGeom>"
	                    +"</xdr:spPr>"
	                +"</xdr:pic>"
	                + "<xdr:clientData/>"
	            +"</xdr:twoCellAnchor>"
	        +"</xdr:wsDr>";
		
		return (CTDrawing)XmlUtils.unwrap(
				XmlUtils.unmarshalString(openXML));		
		
	}
	
}
