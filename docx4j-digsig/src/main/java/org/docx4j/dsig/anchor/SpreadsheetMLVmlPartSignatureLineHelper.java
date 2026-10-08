/*
 *  Copyright 2016, Plutext Pty Ltd.
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
package org.docx4j.dsig.anchor;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.VMLPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;
import org.docx4j.relationships.Relationship;
import org.docx4j.vml.CTShape;
import org.docx4j.vml.CTShapetype;
import org.docx4j.vml.officedrawing.CTShapeLayout;
import org.docx4j.vml.officedrawing.CTSignatureLine;
import org.docx4j.vml.root.Xml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlsx4j.exceptions.Xlsx4jException;
import org.xlsx4j.sml.CTLegacyDrawing;
import org.xlsx4j.sml.Worksheet;


public class SpreadsheetMLVmlPartSignatureLineHelper {
	
	private static Logger log = LoggerFactory.getLogger(SpreadsheetMLVmlPartSignatureLineHelper.class);	
	
	
//	final static org.xlsx4j.sml.ObjectFactory smlObjectFactory = new org.xlsx4j.sml.ObjectFactory();
	
	/* The worksheet itself contains:
	 * 
		  <legacyDrawing r:id="rId1"/>
		</worksheet>

	 * that's a link to a vml drawing part, which contains:
	 * 
		<xml xmlns:v="urn:schemas-microsoft-com:vml"
		 xmlns:o="urn:schemas-microsoft-com:office:office"
		 xmlns:x="urn:schemas-microsoft-com:office:excel">
		  <o:shapelayout v:ext="edit">
		    <o:idmap v:ext="edit" data="1"/>
		  </o:shapelayout>
		  <v:shapetype id="_x0000_t75" coordsize="21600,21600" o:spt="75"
		  o:preferrelative="t" path="m@4@5l@4@11@9@11@9@5xe" filled="f" stroked="f">
		    <v:stroke joinstyle="miter"/>
		    <v:formulas>
		      <v:f eqn="if lineDrawn pixelLineWidth 0"/>
		      <v:f eqn="sum @0 1 0"/>
		      <v:f eqn="sum 0 0 @1"/>
		      <v:f eqn="prod @2 1 2"/>
		      <v:f eqn="prod @3 21600 pixelWidth"/>
		      <v:f eqn="prod @3 21600 pixelHeight"/>
		      <v:f eqn="sum @0 0 1"/>
		      <v:f eqn="prod @6 1 2"/>
		      <v:f eqn="prod @7 21600 pixelWidth"/>
		      <v:f eqn="sum @8 21600 0"/>
		      <v:f eqn="prod @7 21600 pixelHeight"/>
		      <v:f eqn="sum @10 21600 0"/>
		    </v:formulas>
		    <v:path o:extrusionok="f" gradientshapeok="t" o:connecttype="rect"/>
		    <o:lock v:ext="edit" aspectratio="t"/>
		  </v:shapetype>
		  
		  <v:shape id="_x0000_s1025" type="#_x0000_t75" alt="Microsoft Office Signature Line..."
		  style='position:absolute;margin-left:48pt;margin-top:15pt;width:192pt;
		  height:96pt;z-index:1'>
		    <v:imagedata o:relid="rId1" o:title=""/>
		    <o:lock v:ext="edit" ungrouping="t" rotation="t" cropping="t" verticies="t"
		     text="t" grouping="t"/>
		    <o:signatureline v:ext="edit" id="{27DA2118-D46A-4716-AFA5-7416CDE61D9C}"
		     provid="{00000000-0000-0000-0000-000000000000}" o:suggestedsigner="Jason"
		     o:suggestedsigner2="[Title]" o:suggestedsigneremail="jharrop@gmail.comix"
		     showsigndate="f" allowcomments="t" issignatureline="t"/>
		    <x:ClientData ObjectType="Pict">
		      <x:SizeWithCells/>
		      <x:Anchor>
		        1, 0, 1, 0, 5, 0, 7, 8
		      </x:Anchor>
		      <x:CF>Pict</x:CF>
		      <x:AutoPict/>
		    </x:ClientData>
		  </v:shape>
		  
		</xml>

	 *  The VML drawing part in turn links to an image. 
	 * 
	 *  If you have multiple signatures on the one sheet, they all use this single VML part;
	 *  there's a v:shape for each signature line.
	 *  
	 *   
	 * 
	 
	 */
	

    private final static QName _Signatureline_QNAME = new QName("urn:schemas-microsoft-com:office:office", "signatureline");
	
	/**
	 * Add a signature line to the sheet.  It goes in the sheet's legacy (VML) drawing part, 
	 * which is created if the sheet doesn't have one (it will have, if it has comments).
	 * 
	 * @param anchorLocation 8 numbers: from column, offset, row, offset, to column, offset, row, offset;
	 * eg "1, 0, 1, 0, 5, 0, 7, 8"
	 */
	public void addSignatureLine(WorksheetPart sheet, CTSignatureLine signatureline,  
			byte[] imageBytes, String anchorLocation,
			String alt) throws Xlsx4jException  {
		
		VMLPart vmlPart = getLegacyDrawingPart(sheet, true);
		
		Xml xml = ensureInitialised( vmlPart);
		
		// OK, now we can create shape and add a signature line to it
		String imageRelID;
		try {
			imageRelID = addImage(sheet.getPackage(), vmlPart, imageBytes);
		} catch (Exception e) {
			throw new Xlsx4jException(e.getMessage(), e);
		}
		Object shapeEl = getVShapeXML(  alt, imageRelID,  anchorLocation, getNextShapeId(xml));	
		xml.getAny().add(shapeEl);
		CTShape shape = (CTShape)XmlUtils.unwrap(shapeEl);
		
		// before x:ClientData, as Excel has it
		shape.getEGShapeElements().add(shape.getEGShapeElements().size()-1,
				new JAXBElement<CTSignatureLine>(_Signatureline_QNAME, CTSignatureLine.class, null, signatureline)
				);
	}
	
	/**
	 * The signature lines on this sheet, signed or not.
	 */
	public List<CTSignatureLine> getSignatureLines(WorksheetPart sheet) throws Xlsx4jException {
		
		List<CTSignatureLine> lines = new ArrayList<CTSignatureLine>();
		
		VMLPart vmlPart = getLegacyDrawingPart(sheet, false);
		if (vmlPart==null) return lines;
		
		Xml xml;
		try {
			xml = vmlPart.getContents();
		} catch (Docx4JException e) {
			throw new Xlsx4jException(e.getMessage(), e);
		}
		for (Object o : xml.getAny() ) {
			o = XmlUtils.unwrap(o);
			if (o instanceof CTShape) {
				for (JAXBElement<?> el : ((CTShape)o).getEGShapeElements()) {
					if (el.getValue() instanceof CTSignatureLine) {
						lines.add((CTSignatureLine)el.getValue());
					}
				}
			}
		}
		return lines;
	}
	
//	private static String anchorLocationAsString(int[] anchorLocation) throws OLEException {
//	
//	// anchor must contain 8 numbers; see http://webapp.docx4java.org/OnlineDemo/ecma376/VML/Anchor.html
//	// eg 0, 0, 0, 0, 9, 19, 42, 2
//	if (anchorLocation==null
//			|| anchorLocation.length!=8) {
//		throw new OLEException("Invalid anchor location specified.  Should be 8 numbers.");			
//	}
//	StringBuilder anchorSB = new StringBuilder();
//	for (int i=0 ; i<anchorLocation.length; i++) {
//		if (i<=6) {
//			anchorSB.append(anchorLocation[i] + ", ");
//		} else {
//			anchorSB.append(anchorLocation[i] );				
//		}
//	}
//	
//	return anchorSB.toString();
//}	
	private String addImage(OpcPackage opcPackage, VMLPart vmlPart, byte[] imageBytes) throws Exception {
				
        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(opcPackage, vmlPart, imageBytes);

        return imagePart.getRelLast().getId(); 
}
	
	public VMLPart getLegacyDrawingPart(WorksheetPart sheet, boolean create) {
		
		// This is here, rather than in WorksheetPart, so we have a hope of working with 3.2.x
		// The create arg is to make it general purpose.
		
		Worksheet worksheet;
		try {
			worksheet = sheet.getContents();
		} catch (Docx4JException e) {
			throw new RuntimeException(e);
		}
		
		CTLegacyDrawing legacyDrawing = worksheet.getLegacyDrawing();
		VMLPart vmlPart = null;
		if (legacyDrawing==null
				&& create) {
			
			legacyDrawing = new CTLegacyDrawing();
			worksheet.setLegacyDrawing(legacyDrawing);
			
			try {
				vmlPart = new VMLPart();
			} catch (InvalidFormatException e) {
				// shouldn't happen
				e.printStackTrace();
			}
			Relationship rel=null;
			try {
				rel = sheet.addTargetPart(vmlPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS);
			} catch (InvalidFormatException e) {
				// shouldn't happen
				log.error(e.getMessage(), e);
			}
			
			legacyDrawing.setId(rel.getId());
			
		} else if (legacyDrawing!=null) {
			
			vmlPart = (VMLPart)sheet.getRelationshipsPart().getPart(legacyDrawing.getId());
		}
		return vmlPart;
	}
		
		
	
	/**
	 * Generate the VML part which the legacyDrawing in the worksheet points to,
	 * complete with defintion of shape t75.
	 * 
	 * Once this is done, signature lines can be added
	 */
	private Xml ensureInitialised(VMLPart vmlPart)   {
		
		Xml xml = null;
		if (vmlPart.getPackage().getSourcePartStore()!=null || vmlPart.getJaxbElement()!=null) {
			try {
				xml = vmlPart.getContents();
			} catch (Docx4JException e) {
				// a part we just created has nothing to load
				log.debug(e.getMessage());
			}
		}
		if (xml==null) {
			xml = createVmlPartXmlContent(getNextIdMap(vmlPart));
			vmlPart.setJaxbElement(xml);			
		}
		
		if (findShapeTypeByID(xml, "_x0000_t75")==null) {
			xml.getAny().add(createShapetypeT75());
		}

		return xml;
				
	}
	
	private CTShapetype findShapeTypeByID(Xml xml, String id) {
		
		for (Object o : xml.getAny() ) {
			
			Object o2 = XmlUtils.unwrap(o);
			if (o2 instanceof CTShapetype) {
				if ( id.equals(((CTShapetype)o2).getVmlId())) {
					return (CTShapetype)o2;
				}
			}
		}
		return null;
	}
	



	
	/**
	 * Excel numbers the shapes in a VML part from 1024 times the part's o:idmap/@data;
	 * eg _x0000_s1025 is the first shape where that is 1.
	 */
	private String getNextShapeId(Xml xml) {
		
		int idmap = 1;
		for (Object o : xml.getAny() ) {
			o = XmlUtils.unwrap(o);
			if (o instanceof CTShapeLayout 
					&& ((CTShapeLayout)o).getIdmap()!=null) {
				try {
					// can be a list; the first block will do
					idmap = Integer.parseInt(((CTShapeLayout)o).getIdmap().getData().split(",")[0].trim());
				} catch (RuntimeException e) {
					log.warn("o:idmap/@data " + ((CTShapeLayout)o).getIdmap().getData());
				}
			}
		}
		
		int n = 0;
		String shapeId;
    	do {
    		n++;
    		shapeId = "_x0000_s" + (1024*idmap + n);
    	} while (isShapeIdOccupied(xml.getAny(), shapeId));
		
		return shapeId;
	}
	
	/** A new VML part needs an o:idmap/@data no other VML part in the workbook has */
	private int getNextIdMap(VMLPart newPart) {
		
		int max = 0;
		for (Part p : newPart.getPackage().getParts().getParts().values()) {
			if (p==newPart || !(p instanceof VMLPart)) continue;
			try {
				for (Object o : ((VMLPart)p).getContents().getAny() ) {
					o = XmlUtils.unwrap(o);
					if (o instanceof CTShapeLayout 
							&& ((CTShapeLayout)o).getIdmap()!=null) {
						for (String block : ((CTShapeLayout)o).getIdmap().getData().split(",")) {
							max = Math.max(max, Integer.parseInt(block.trim()));
						}
					}
				}
			} catch (Exception e) {
				log.warn(p.getPartName().getName() + ": " + e.getMessage());
			}
		}
		return max+1;
	}
	
	private static boolean isShapeIdOccupied(List<Object> vmlContents, String shapeId) {
		
		for (Object o  : vmlContents ) {	
			o = XmlUtils.unwrap(o);
			if (o instanceof org.docx4j.vml.CTShape) {
				org.docx4j.vml.CTShape shape = (org.docx4j.vml.CTShape)o;
				if (shape.getVmlId()!=null // this is what it is called
						&& shape.getVmlId().equals(shapeId)) {
					return true;
				}
			}
		}
		return false;
	}	
	
	private  org.docx4j.vml.root.Xml createVmlPartXmlContent(int idmap)  {
		
		String openXML = 
				"<xml xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:ns13=\"urn:schemas-microsoft-com:office:excel\">"
				// TODO: use http://webapp.docx4java.org/OnlineDemo/ecma376/VML/idmap.html properly!
	            + "<o:shapelayout v:ext=\"edit\">"
	                + "<o:idmap data=\"" + idmap + "\" v:ext=\"edit\"/>"
	            +"</o:shapelayout>"
	        +"</xml>";
		
		try {
			return (org.docx4j.vml.root.Xml)XmlUtils.unmarshalString(openXML);
		} catch (JAXBException e) {
			// shouldn't happen
			log.error(e.getMessage(), e);
			return null;
		}		
		
	}
	
	/** @return the shapetype, as the JAXBElement the part's content list wants */
	private  Object  createShapetypeT75()  {
		
        // shapetype id="_x0000_t75", same as for OLE

		String openXML = 
	            "<v:shapetype   xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\"    coordsize=\"21600,21600\" filled=\"f\" id=\"_x0000_t75\" o:preferrelative=\"t\" o:spt=\"75\" path=\"m@4@5l@4@11@9@11@9@5xe\" stroked=\"f\">"
	                + "<v:stroke joinstyle=\"miter\"/>"
	                + "<v:formulas>"
	                    + "<v:f eqn=\"if lineDrawn pixelLineWidth 0\"/>"
	                    + "<v:f eqn=\"sum @0 1 0\"/>"
	                    + "<v:f eqn=\"sum 0 0 @1\"/>"
	                    + "<v:f eqn=\"prod @2 1 2\"/>"
	                    + "<v:f eqn=\"prod @3 21600 pixelWidth\"/>"
	                    + "<v:f eqn=\"prod @3 21600 pixelHeight\"/>"
	                    + "<v:f eqn=\"sum @0 0 1\"/>"
	                    + "<v:f eqn=\"prod @6 1 2\"/>"
	                    + "<v:f eqn=\"prod @7 21600 pixelWidth\"/>"
	                    + "<v:f eqn=\"sum @8 21600 0\"/>"
	                    + "<v:f eqn=\"prod @7 21600 pixelHeight\"/>"
	                    + "<v:f eqn=\"sum @10 21600 0\"/>"
	                +"</v:formulas>"
	                + "<v:path gradientshapeok=\"t\" o:connecttype=\"rect\" o:extrusionok=\"f\"/>"
	                + "<o:lock aspectratio=\"t\" v:ext=\"edit\"/>"
	            +"</v:shapetype>";
		
		try {
			return XmlUtils.unmarshalString(openXML);
		} catch (JAXBException e) {
			// shouldn't happen
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	
	/** @return the shape, as the JAXBElement the part's content list wants */
	private  Object getVShapeXML( String alt, String imageRelID, String anchorLocation, String shapeId) {
		
		if (alt==null) {
			alt = "Microsoft Office Signature Line...";
		}

		String xml = 
			"<v:shape alt=\"" + alt + "\" id=\"" + shapeId + "\" style=\"position:absolute;margin-left:0;margin-top:0;width:192pt;height:96pt;   z-index:1\" type=\"#_x0000_t75\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\">"
			    + "<v:imagedata o:relid=\"" + imageRelID + "\" o:title=\"\"/>"
				+ "<o:lock cropping=\"t\" grouping=\"t\" rotation=\"t\" text=\"t\" ungrouping=\"t\" v:ext=\"edit\" verticies=\"t\"/>"
//				+ "<o:signatureline id=\"{B0805832-C076-40E6-A6AB-5F71EFF019FB}\" issignatureline=\"t\" o:suggestedsigner=\"bbb\" o:suggestedsigner2=\"bbb\" o:suggestedsigneremail=\"bbb\" provid=\"{00000000-0000-0000-0000-000000000000}\" v:ext=\"edit\"/>"
				+ "<x:ClientData ObjectType=\"Pict\">"
				     + "<x:SizeWithCells/>"
				     + "<x:Anchor>" + anchorLocation + "</x:Anchor>"
				     + "<x:CF>Pict</x:CF>"
				     + "<x:AutoPict/>"
				+"</x:ClientData>"
			+"</v:shape>";
		
		try {
			return XmlUtils.unmarshalString(xml);
		} catch (JAXBException e) {
			// shouldn't happen
			log.error(e.getMessage(), e);
			return null;
		}
	}
	

	
}
