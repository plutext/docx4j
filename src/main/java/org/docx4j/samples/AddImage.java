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


package org.docx4j.samples;

import java.io.File;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.SaveToZipFile;

import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.relationships.Relationship;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

/**
 * Creates a WordprocessingML document from scratch. 
 * 
 * @author Jason Harrop
 * @version 1.0
 */
public class AddImage {

	public static void main(String[] args) throws Exception {
		
		System.out.println( "Creating package..");
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		createImagePart(wordMLPackage);
		
		// Now save it 
		wordMLPackage.save(new java.io.File(System.getProperty("user.dir") + "/result.docx") );
		
		System.out.println("Done.");
				
	}
	
	public static void createImagePart(WordprocessingMLPackage wordMLPackage) {
		
		try {
			org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart imagePart 
				= new org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart( new PartName("/word/media/image1.jpeg") );
			
			java.io.InputStream is = new java.io.FileInputStream("/tmp/image.jpeg" );
			
			imagePart.setBinaryData(is);
			imagePart.setContentType( new ContentType(ContentTypes.IMAGE_JPEG) );  

			//imagePart.setContentType( new ContentType(ContentTypes.IMAGE_PNG) );  

			
			imagePart.setRelationshipType( Namespaces.IMAGE );
						
			Relationship rel = wordMLPackage.getMainDocumentPart().addTargetPart(imagePart);
			
			// Contains ${docPrId}, ${docPrName}, ${docPrDesc}, ${picName}, ${rEmbedId}
            String ml ="<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\"><w:r><w:rPr><w:noProof/></w:rPr><w:drawing><wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\"><wp:extent cx=\"3238500\" cy=\"2362200\"/><wp:effectExtent l=\"19050\" t=\"0\" r=\"0\" b=\"0\"/><wp:docPr id=\"${docPrId}\" name=\"${docPrName}\" descr=\"${docPrDesc}\"/><wp:cNvGraphicFramePr><a:graphicFrameLocks xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" noChangeAspect=\"1\"/></wp:cNvGraphicFramePr><a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\"><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"><pic:pic xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"><pic:nvPicPr><pic:cNvPr id=\"0\" name=\"${picName}\"/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed=\"${rEmbedId}\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"3238500\" cy=\"2362200\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>";
            java.util.HashMap<String, String>mappings = new java.util.HashMap<String, String>();
            
            mappings.put("docPrId", "1");
            mappings.put("docPrName", "Picture 1");
            mappings.put("docPrDesc", "some.jpeg");
            mappings.put("picName", "some.jpeg");
            mappings.put("rEmbedId", rel.getId()  );

            wordMLPackage.getMainDocumentPart().addObject(
                  org.docx4j.XmlUtils.unmarshallFromTemplate(ml, mappings ) );
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
		
}
