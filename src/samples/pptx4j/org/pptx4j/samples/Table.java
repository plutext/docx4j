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

package org.pptx4j.samples;


import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.XmlUtils;
import org.docx4j.dml.CTPoint2D;
import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.dml.CTTable;
import org.docx4j.dml.CTTableCell;
import org.docx4j.dml.CTTableCol;
import org.docx4j.dml.CTTableGrid;
import org.docx4j.dml.CTTableRow;
import org.docx4j.dml.Graphic;
import org.docx4j.dml.GraphicData;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.pptx4j.jaxb.Context;
import org.pptx4j.pml.CTGraphicalObjectFrame;



/**
 * Demonstrates 2 methods of adding a table
 * to a slide.
 *
 */
public class Table  {
	
	protected static Logger log = LoggerFactory.getLogger(Table.class);
		
	public static void main(String[] args) throws Exception {

		// Where will we save our new .ppxt?
		String outputfilepath = System.getProperty("user.dir") + "/OUT_Table.pptx";
		
		// Create skeletal package, including a MainPresentationPart and a SlideLayoutPart
		PresentationMLPackage presentationMLPackage = PresentationMLPackage.createPackage(); 
		
		// Need references to these parts to create a slide
		// Please note that these parts *already exist* - they are
		// created by createPackage() above.  See that method
		// for instruction on how to create and add a part.
		MainPresentationPart pp = (MainPresentationPart)presentationMLPackage.getParts().getParts().get(
				new PartName("/ppt/presentation.xml"));		
		SlideLayoutPart layoutPart = (SlideLayoutPart)presentationMLPackage.getParts().getParts().get(
				new PartName("/ppt/slideLayouts/slideLayout1.xml"));
		
		// OK, now we can create a slide
		SlidePart slidePart = presentationMLPackage.createSlidePart(pp, layoutPart, 
				new PartName("/ppt/slides/slide1.xml"));
				
		// Method 1 - programmatic
		slidePart.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add( getTable() );
		
		// Method 2 - from string - on slide 2
		SlidePart slide2 = presentationMLPackage.createSlidePart(pp, layoutPart, 
				new PartName("/ppt/slides/slide2.xml"));
		slide2.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add( createGraphicFrameFromString() );
		
		// All done: save it
		presentationMLPackage.save(new java.io.File(outputfilepath));

		System.out.println("\n\n done .. saved " + outputfilepath);
		
	}	
	
	public static CTGraphicalObjectFrame getTable() throws JAXBException {

		// instatiation the factory for later object creation.
		org.docx4j.dml.ObjectFactory dmlFactory = new org.docx4j.dml.ObjectFactory();
		org.pptx4j.pml.ObjectFactory pmlFactory = new org.pptx4j.pml.ObjectFactory();

		// Node Creation
		CTGraphicalObjectFrame graphicFrame = pmlFactory
				.createCTGraphicalObjectFrame();
		org.pptx4j.pml.CTGraphicalObjectFrameNonVisual nvGraphicFramePr = pmlFactory
				.createCTGraphicalObjectFrameNonVisual();
		org.docx4j.dml.CTNonVisualDrawingProps cNvPr = dmlFactory
				.createCTNonVisualDrawingProps();
		org.docx4j.dml.CTNonVisualGraphicFrameProperties cNvGraphicFramePr = dmlFactory
				.createCTNonVisualGraphicFrameProperties();
		org.docx4j.dml.CTGraphicalObjectFrameLocking graphicFrameLocks = new org.docx4j.dml.CTGraphicalObjectFrameLocking();
		org.docx4j.dml.CTTransform2D xfrm = dmlFactory.createCTTransform2D();
		Graphic graphic = dmlFactory.createGraphic();
		GraphicData graphicData = dmlFactory.createGraphicData();
		
		// Build the parent-child relationship of this slides.xml
		graphicFrame.setNvGraphicFramePr(nvGraphicFramePr);
		nvGraphicFramePr.setCNvPr(cNvPr);
		cNvPr.setName("1");
		nvGraphicFramePr.setCNvGraphicFramePr(cNvGraphicFramePr);
		cNvGraphicFramePr.setGraphicFrameLocks(graphicFrameLocks);
		graphicFrameLocks.setNoGrp(true);
		nvGraphicFramePr.setNvPr(pmlFactory.createNvPr());
		
//        <p:xfrm>
//        <a:off x="1524000" y="1397000"/>
//        <a:ext cx="6096000" cy="741680"/>
//      </p:xfrm>
		graphicFrame.setXfrm(xfrm);
		
		CTPositiveSize2D ext = dmlFactory.createCTPositiveSize2D();
		ext.setCx(6096000);
		ext.setCy(741680);
		
		xfrm.setExt(ext);
		
		CTPoint2D off = dmlFactory.createCTPoint2D();
		xfrm.setOff(off);
		off.setX(1524000);
		off.setY(1397000);
		
		graphicFrame.setGraphic(graphic);
		
		graphic.setGraphicData(graphicData);
		graphicData
				.setUri("http://schemas.openxmlformats.org/drawingml/2006/table");
				
		CTTable ctTable = dmlFactory.createCTTable();
		JAXBElement<CTTable> tbl = dmlFactory.createTbl(ctTable);
		graphicData.getAny().add(tbl);
		
		CTTableGrid ctTableGrid = dmlFactory.createCTTableGrid();		
		CTTableCol gridCol = dmlFactory.createCTTableCol();
		ctTable.setTblGrid(ctTableGrid);
		ctTableGrid.getGridCol().add(gridCol);
		ctTableGrid.getGridCol().add(gridCol);
		gridCol.setW(300000);

		CTTableRow ctTableRow = dmlFactory.createCTTableRow();
		ctTableRow.setH(370840);
		
		
		ctTableRow.getTc().add(createTableCell());
		ctTableRow.getTc().add(createTableCell());
		
		for (int i = 0; i < 4; i++) {
			ctTable.getTr().add(ctTableRow);
		}
		
		return graphicFrame;
	}	

	public static CTTableCell createTableCell() throws JAXBException {
	   String contents =
		"<a:tc  xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">" +
		"<a:txBody>"
	        +"<a:bodyPr/>"
	        +"<a:lstStyle/>"
	        +"<a:p>"
	          +"<a:r>"
	            +"<a:rPr lang=\"en-AU\" dirty=\"0\" smtClean=\"0\"/>"
	            +"<a:t>11</a:t>"
	          +"</a:r>"
	          +"<a:endParaRPr lang=\"en-AU\" dirty=\"0\"/>"
	          +"</a:p>"
	      +"</a:txBody>" +
	      "</a:tc>";
	      //+"<a:tcPr/>
	   return ((CTTableCell)XmlUtils.unmarshalString(contents,org.docx4j.jaxb.Context.jc, CTTableCell.class));
		
	}
	
	public static CTGraphicalObjectFrame createGraphicFrameFromString() throws JAXBException {
		
	        String tableau = 
	        	     "<p:graphicFrame xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">" + 
	        	     "        <p:nvGraphicFramePr>" + 
	        	     "          <p:cNvPr id=\"4\" name=\"Table 3\"/>" + 
	        	     "          <p:cNvGraphicFramePr>" + 
	        	     "            <a:graphicFrameLocks noGrp=\"1\"/>" + 
	        	     "          </p:cNvGraphicFramePr>" + 
	        	     "          <p:nvPr/>" + 
	        	     "        </p:nvGraphicFramePr>" + 
	        	     "        <p:xfrm>" + 
	        	     "          <a:off x=\"1524000\" y=\"1397000\"/>" + 
	        	     "          <a:ext cx=\"6096000\" cy=\"741680\"/>" + 
	        	     "        </p:xfrm>" + 
	        	     "        <a:graphic>" + 
	        	     "          <a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/table\">" + 
	        	     "            <a:tbl>" + 
	        	     "              <a:tblPr firstRow=\"1\" bandRow=\"1\">" + 
	        	     "                <a:tableStyleId>{5C22544A-7EE6-4342-B048-85BDC9FD1C3A}</a:tableStyleId>" + 
	        	     "              </a:tblPr>" + 
	        	     "              <a:tblGrid>" + 
	        	     "                <a:gridCol w=\"3048000\"/>" + 
	        	     "                <a:gridCol w=\"3048000\"/>" + 
	        	     "              </a:tblGrid>" + 
	        	     "              <a:tr h=\"370840\">" + 
	        	     "                <a:tc>" + 
	        	     "                  <a:txBody>" + 
	        	     "                    <a:bodyPr/>" + 
	        	     "                    <a:lstStyle/>" + 
	        	     "                    <a:p>" + 
	        	     "                      <a:r>" + 
	        	     "                        <a:rPr lang=\"en-AU\" dirty=\"0\" smtClean=\"0\"/>" + 
	        	     "                        <a:t>11</a:t>" + 
	        	     "                      </a:r>" + 
	        	     "                      <a:endParaRPr lang=\"en-AU\" dirty=\"0\"/>" + 
	        	     "                    </a:p>" + 
	        	     "                  </a:txBody>" + 
	        	     "                  <a:tcPr/>" + 
	        	     "                </a:tc>" + 
	        	     "                <a:tc>" + 
	        	     "                  <a:txBody>" + 
	        	     "                    <a:bodyPr/>" + 
	        	     "                    <a:lstStyle/>" + 
	        	     "                    <a:p>" + 
	        	     "                      <a:r>" + 
	        	     "                        <a:rPr lang=\"en-AU\" dirty=\"0\" smtClean=\"0\"/>" + 
	        	     "                        <a:t>12</a:t>" + 
	        	     "                      </a:r>" + 
	        	     "                      <a:endParaRPr lang=\"en-AU\" dirty=\"0\"/>" + 
	        	     "                    </a:p>" + 
	        	     "                  </a:txBody>" + 
	        	     "                  <a:tcPr/>" + 
	        	     "                </a:tc>" + 
	        	     "              </a:tr>" + 
	        	     "              <a:tr h=\"370840\">" + 
	        	     "                <a:tc>" + 
	        	     "                  <a:txBody>" + 
	        	     "                    <a:bodyPr/>" + 
	        	     "                    <a:lstStyle/>" + 
	        	     "                    <a:p>" + 
	        	     "                      <a:r>" + 
	        	     "                        <a:rPr lang=\"en-AU\" dirty=\"0\" smtClean=\"0\"/>" + 
	        	     "                        <a:t>21</a:t>" + 
	        	     "                      </a:r>" + 
	        	     "                      <a:endParaRPr lang=\"en-AU\" dirty=\"0\"/>" + 
	        	     "                    </a:p>" + 
	        	     "                  </a:txBody>" + 
	        	     "                  <a:tcPr/>" + 
	        	     "                </a:tc>" + 
	        	     "                <a:tc>" + 
	        	     "                  <a:txBody>" + 
	        	     "                    <a:bodyPr/>" + 
	        	     "                    <a:lstStyle/>" + 
	        	     "                    <a:p>" + 
	        	     "                      <a:r>" + 
	        	     "                        <a:rPr lang=\"en-AU\" dirty=\"0\" smtClean=\"0\"/>" + 
	        	     "                        <a:t>22</a:t>" + 
	        	     "                      </a:r>" + 
	        	     "                      <a:endParaRPr lang=\"en-AU\" dirty=\"0\"/>" + 
	        	     "                    </a:p>" + 
	        	     "                  </a:txBody>" + 
	        	     "                  <a:tcPr/>" + 
	        	     "                </a:tc>" + 
	        	     "              </a:tr>" + 
	        	     "            </a:tbl>" + 
	        	     "          </a:graphicData>" + 
	        	     "        </a:graphic>" + 
	        	     "      </p:graphicFrame>";

	        
	        return (CTGraphicalObjectFrame) XmlUtils.unmarshalString(tableau, Context.jcPML,
	        		CTGraphicalObjectFrame.class);
		
	}
	
	
}
