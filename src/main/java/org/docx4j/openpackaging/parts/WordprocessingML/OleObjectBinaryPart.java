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
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.Entry;
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
    	viewFile(System.out, verbose);
    	
    }

    /**
     * @param os
     * @param verbose
     * @throws IOException
     * @since 3.0.0
     */
    public void viewFile(OutputStream os, boolean verbose) throws IOException
    {
    	String indent="";
    	boolean withSizes = true;    	
    	displayDirectory(fs.getRoot(), os, indent, withSizes);
    	
    	if (verbose) {
	        List strings = POIFSViewEngine.inspectViewable(fs, true, 0, "  ");
			Iterator iter = strings.iterator();
	
			while (iter.hasNext()) {
				os.write( ((String)iter.next()).getBytes());
			}
    	}
    	
    }
    
    
    /**
     * Adapted from org.apache.poi.poifs.dev.POIFSLister
     * @param dir
     * @param indent
     * @param withSizes
     * @throws IOException 
     * 
     */
    private void displayDirectory(DirectoryNode dir, OutputStream os, String indent, boolean withSizes) throws IOException {
        System.out.println(indent + dir.getName() + " -");
        String newIndent = indent + "  ";

        boolean hadChildren = false;
        for(Iterator<Entry> it = dir.getEntries(); it.hasNext();) {
           hadChildren = true;
           Entry entry = it.next();
           if (entry instanceof DirectoryNode) {
              displayDirectory((DirectoryNode) entry, os, newIndent, withSizes);
           } else {
              DocumentNode doc = (DocumentNode) entry;
              String name = doc.getName();
              String size = "";
              if (name.charAt(0) < 10) {
                 String altname = "(0x0" + (int) name.charAt(0) + ")" + name.substring(1);
                 name = name.substring(1) + " <" + altname + ">";
              }
              if (withSizes) {
                 size = " [" + doc.getSize() + " / 0x" + 
                        Integer.toHexString(doc.getSize()) + "]";
              }
              os.write((newIndent + name + size).getBytes());
           }
        }
        if (!hadChildren) {
        	os.write((newIndent + "(no children)").getBytes());
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
