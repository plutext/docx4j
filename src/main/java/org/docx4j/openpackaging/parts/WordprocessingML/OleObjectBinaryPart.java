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

package org.docx4j.openpackaging.parts.WordprocessingML;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.poifs.dev.POIFSViewEngine;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;


public class OleObjectBinaryPart extends BinaryPart {

	private static Logger log = Logger.getLogger(OleObjectBinaryPart.class);		
	
	public OleObjectBinaryPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();				
	}

	
	public OleObjectBinaryPart() throws InvalidFormatException {
		super( new PartName("/word/embeddings/oleObject1.bin") );
		init();				
	}
	
	public void init() {
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.OFFICEDOCUMENT_OLE_OBJECT));

		// Used when this Part is added to a rels
		setRelationshipType(Namespaces.OLE_OBJECT);
		
		
	}

	POIFSFileSystem fs;
	public POIFSFileSystem getFs() {
		return fs;
	}
	
	public void initPOIFSFileSystem() throws IOException {
		
		
		//fs = new POIFSFileSystem( org.docx4j.utils.BufferUtil.newInputStream(bb) );
		
		// the above seems to be calling methods which aren't implemented,
		// so, for now, brute force..
		
		getBuffer().clear();
        byte[] bytes = new byte[getBuffer().capacity()];
        getBuffer().get(bytes, 0, bytes.length);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		fs = new POIFSFileSystem(bais);
		
	}
	
	public void writePOIFSFileSystem() throws IOException {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 

		fs.writeFilesystem(baos);
		
		// Need to put this is bb
		byte[] bytes = baos.toByteArray();
		
		setBinaryData( ByteBuffer.wrap(bytes) );
		
	}
	
	// java.nio.ByteBuffer bb contains the data
	
    public void viewFile(boolean verbose) throws IOException
    {
    	String indent="";
    	boolean withSizes = true;    	
    	org.apache.poi.poifs.dev.POIFSLister.displayDirectory(fs.getRoot(), indent, withSizes);
    	
    	if (verbose) {
	        List strings = POIFSViewEngine.inspectViewable(fs, true, 0, "  ");
			Iterator iter = strings.iterator();
	
			while (iter.hasNext()) {
				System.out.print(iter.next());
			}
    	}
    	
    }

//	public void extractPdf(OutputStream out) throws IOException {
//		
//		DocumentInputStream inputStream = fs.createDocumentInputStream("CONTENTS");
//	    
//	    byte buf[]=new byte[1024];
//	    int len;
//	    while ((len=inputStream.read(buf))>0) {
//	    	out.write(buf,0,len);
//	    }
//	    out.close();
//	    inputStream.close();
//		
//	}

	
	
	
	
}
