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

import java.util.Random;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;

import org.docx4j.XmlUtils;
import org.docx4j.vml.CTImageData;
import org.docx4j.vml.CTShape;
import org.docx4j.wml.Pict;
import org.docx4j.vml.officedrawing.CTLock;
import org.docx4j.vml.officedrawing.CTSignatureLine;
/**
 * 

 * 
 * Create something like:
 * 
         <w:pict>
          <v:shapetype id="_x0000_t75" coordsize="21600,21600" path="m@4@5l@4@11@9@11@9@5xe" o:preferrelative="t" o:spt="75.0" filled="f" stroked="f">
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
            <v:path o:extrusionok="f" o:connecttype="rect" gradientshapeok="t"/>
            <o:lock v:ext="edit" aspectratio="t"/>
          </v:shapetype>
          
          (that much is the same as an OLE anchor)
          
          (shape has more content)
          
          
          <v:shape id="_x0000_i1025" type="#_x0000_t75" alt="Microsoft Office Signature Line..." style="width:191.7pt;height:96.3pt">
            <v:imagedata r:id="rId5" o:title=""/>
            <o:lock v:ext="edit" ungrouping="t" rotation="t" cropping="t" verticies="t" text="t" grouping="t"/>
            <o:signatureline v:ext="edit" id="{95FBD063-BB50-47B0-B1D4-11BAE2C2AF54}" provid="{00000000-0000-0000-0000-000000000000}" o:suggestedsigner="me" issignatureline="t"/>
          </v:shape>
        </w:pict>
        
        
        
    Minimal working signature:
    
       <w:pict>
          <v:shape id="_x0000_i1025" style="width:96pt;height:120pt">
            <v:imagedata r:id="rId5" o:title=""/>
            <o:signatureline v:ext="edit" id="{6CFF3125-8270-4B50-946A-1ACCC04D335F}" provid="{000CD6A4-0000-0000-C000-000000000046}" 
                             o:suggestedsigner="JOhn Doe" o:suggestedsigner2="Manager" o:suggestedsigneremail="a@b.com" showsigndate="f" issignatureline="t"/>
          </v:shape>
        </w:pict>

 * 
 * @author jharrop
 */
public class WordMLPictHelper {
	
	public final static org.docx4j.vml.ObjectFactory vmlObjectFactory 
		= new org.docx4j.vml.ObjectFactory();	
	
	public final static org.docx4j.vml.officedrawing.ObjectFactory vmlofficedrawingObjectFactory 
		= new org.docx4j.vml.officedrawing.ObjectFactory();
	
    final static Random rand = new Random();

	
	
	/**
	 * Return a w:object suitable for anchoring this 
	 * @param progId  can get from embeddingType.getProgID()
	 * @param relId
	 * @param imageRelID
	 * @param style attribute on v:shape eg "width:446.4pt;height:631.7pt" (usually null is best)
	 * @return
	 * @throws OLEException 
	 * @throws JAXBException
	 */
	public static JAXBElement<org.docx4j.wml.Pict> getPict(CTSignatureLine signatureline,  
			String imageRelID, String alt,  boolean isFirstEmbedding) throws Exception  {
		
		org.docx4j.wml.ObjectFactory wmlObjectFactory = new org.docx4j.wml.ObjectFactory();
		
		Pict pict = wmlObjectFactory.createPict();
		
		JAXBElement<org.docx4j.wml.Pict> pictWrapped = wmlObjectFactory.createRPict(pict); 
		org.docx4j.vml.ObjectFactory vmlObjectFactory = new org.docx4j.vml.ObjectFactory();
		
		    
		if (isFirstEmbedding) {
			/*
			 * Powerpoint 2010 x64 doesn't seem to care whether v:sharetype definition for @type="#_x0000_t75
			 * is present or absent, but other Powerpoints might, so we add it.
			 * 
			 * Powerpoint 2010 x64 doesn't seem to care whether v:sharetype definition for @type="#_x0000_t75
			 * is present on the first w:object, or some subsequent one; but on save it moves it to the first
			 * one.
			 */
		    try {
		    	pict.getAnyAndAny().add( createShapeType() );
			} catch (JAXBException e) {
				throw new Exception(e);
			} 		    
		}
		
		CTShape ctShape = createShape( alt,  imageRelID,   signatureline);
		
	    JAXBElement<org.docx4j.vml.CTShape> shapeWrapped
	    	= vmlObjectFactory.createShape(ctShape);
	    
	    pict.getAnyAndAny().add( shapeWrapped); 
	    
	    return pictWrapped;
	}
	


	
	/**
	 * This fixed v:shapetype is always used (by Office 2010, 2011 Mac, and 2013).
	 * 
	 * @return
	 * @throws JAXBException
	 */
	private static JAXBElement<org.docx4j.vml.CTShapetype> createShapeType() throws JAXBException {
		
	    // Create object for shapetype (wrapped in JAXBElement) 
		// ID is fixed here. It assumes this is the first/only declaration of this
		// object in the docx.  That's fine, since we have isFirstEmbedding to control that.
		String openXML = "<v:shapetype coordsize=\"21600,21600\" filled=\"f\" id=\"_x0000_t75\" o:preferrelative=\"t\" o:spt=\"75\" path=\"m@4@5l@4@11@9@11@9@5xe\" stroked=\"f\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\">"
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

		return (JAXBElement<org.docx4j.vml.CTShapetype>)XmlUtils.unmarshalString(openXML);
		
	}
	
	/**
	 * Create something like:
	 * 
          <v:shape id="_x0000_i1025" type="#_x0000_t75" alt="Microsoft Office Signature Line..." style="width:191.7pt;height:96.3pt">
            <v:imagedata r:id="rId5" o:title=""/>
            <o:lock v:ext="edit" ungrouping="t" rotation="t" cropping="t" verticies="t" text="t" grouping="t"/>
            
            <o:signatureline v:ext="edit" id="{95FBD063-BB50-47B0-B1D4-11BAE2C2AF54}" 
            	provid="{00000000-0000-0000-0000-000000000000}" o:suggestedsigner="me" issignatureline="t"/>
          </v:shape>
          
          In Word, the user can resize the signature line, add a border etc but we just do the defaults here
	 */	
	private static CTShape createShape(String alt, String imageRelID,  CTSignatureLine signatureline) {

		CTShape shape = vmlObjectFactory.createCTShape(); 
		JAXBElement<org.docx4j.vml.CTShape> shapeWrapped = vmlObjectFactory.createShape(shape); 
		    shape.setStyle( "width:191.7pt;height:96.3pt"); 
		    shape.setInsetmode(org.docx4j.vml.officedrawing.STInsetMode.CUSTOM);
		    shape.setConnectortype(org.docx4j.vml.officedrawing.STConnectorType.STRAIGHT);
		    // Create object for imagedata (wrapped in JAXBElement) 
		    CTImageData imagedata = vmlObjectFactory.createCTImageData(); 
		    JAXBElement<org.docx4j.vml.CTImageData> imagedataWrapped = vmlObjectFactory.createImagedata(imagedata); 
		    shape.getEGShapeElements().add( imagedataWrapped); 
		        imagedata.setTitle( ""); 
		        imagedata.setId( imageRelID); 
		        
		    // Create object for lock (wrapped in JAXBElement) 
		    CTLock lock = vmlofficedrawingObjectFactory.createCTLock(); 
		    JAXBElement<org.docx4j.vml.officedrawing.CTLock> lockWrapped = vmlofficedrawingObjectFactory.createLock(lock); 
		    shape.getEGShapeElements().add( lockWrapped); 
		        lock.setText(org.docx4j.vml.officedrawing.STTrueFalse.T);
		        lock.setExt(org.docx4j.vml.STExt.EDIT);
		        lock.setGrouping(org.docx4j.vml.officedrawing.STTrueFalse.T);
		        lock.setUngrouping(org.docx4j.vml.officedrawing.STTrueFalse.T);
		        lock.setRotation(org.docx4j.vml.officedrawing.STTrueFalse.T);
		        lock.setCropping(org.docx4j.vml.officedrawing.STTrueFalse.T);
		        lock.setVerticies(org.docx4j.vml.officedrawing.STTrueFalse.T);

			    // Create object for signatureline (wrapped in JAXBElement) 
			    JAXBElement<org.docx4j.vml.officedrawing.CTSignatureLine> signaturelineWrapped = vmlofficedrawingObjectFactory.createSignatureline(signatureline); 
			    shape.getEGShapeElements().add( signaturelineWrapped);
		        
		    if (alt==null) {
		    	shape.setAlt( "Microsoft Office Signature Line...");
		    } else {
		    	shape.setAlt( alt );		    	
		    }
		    shape.setVmlId( "_x0000_i1025"); 
		    shape.setHralign(org.docx4j.vml.officedrawing.STHrAlign.LEFT);
		    shape.setType( "#_x0000_t75"); 

		return shape;
	}	
}
