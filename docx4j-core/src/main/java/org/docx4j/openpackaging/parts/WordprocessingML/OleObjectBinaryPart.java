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
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.org.apache.poi.poifs.dev.POIFSViewEngine;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode;
import org.docx4j.org.apache.poi.poifs.filesystem.DocumentNode;
import org.docx4j.org.apache.poi.poifs.filesystem.Entry;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSFileSystem;


/**
 * You can use oleObjectBinaryPart.setBinaryData( ByteBuffer.wrap(bytes) );
 * to populate this from a byte[]
 * 
 * @author jharrop
 *
 */
public class OleObjectBinaryPart extends BinaryPart {

	private static Logger log = LoggerFactory.getLogger(OleObjectBinaryPart.class);		
	
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
			// should be this, unless it contains eg a doc stored directly (ie a non-generic OLE object)

		// Used when this Part is added to a rels
		setRelationshipType(Namespaces.OLE_OBJECT);
		
		
	}

	POIFSFileSystem fs;
	public POIFSFileSystem getFs() throws IOException {
		if (fs==null) {
			initPOIFSFileSystem();
		}
		return fs;
	}
	
	public void initPOIFSFileSystem() throws IOException {
		
		if (getBuffer()!=null) {

			//fs = new POIFSFileSystem( org.docx4j.utils.BufferUtil.newInputStream(bb) );
			// the above seems to be calling methods which aren't implemented,
			// so, for now, brute force..

			log.info("initing POIFSFileSystem from existing data");
			ByteArrayInputStream bais = new ByteArrayInputStream(this.getBytes());
			fs = new POIFSFileSystem(bais);
			
		} else {

			log.info("creating new empty POIFSFileSystem");
			fs = new POIFSFileSystem();
			writePOIFSFileSystem();
		}
	}
	
	/**
	 * Write any changes which have been made to POIFSFileSystem,
	 * to the underlying ByteBuffer.  This is necessary if the changes
	 * are to be persisted.
	 * 
	 * @throws IOException
	 */
	public void writePOIFSFileSystem() throws IOException {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 

		getFs().writeFilesystem(baos);
		
		// Need to put this is bb
		byte[] bytes = baos.toByteArray();
		
		// java.nio.ByteBuffer bb contains the data
		setBinaryData( ByteBuffer.wrap(bytes) );
		
	}
	
	
    
    
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
    	displayDirectory(getFs().getRoot(), os, indent, withSizes);
    	
    	if (verbose) {
	        List strings = POIFSViewEngine.inspectViewable(fs, true, 0, "  ");
			Iterator iter = strings.iterator();
	
			while (iter.hasNext()) {
				os.write( ((String)iter.next()).getBytes());
			}
    	}
    }
    
    /**
     * Adapted from org.docx4j.org.apache.poi.poifs.dev.POIFSLister
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
              os.write((newIndent + name + size + "\n").getBytes() );
           }
        }
        if (!hadChildren) {
        	os.write((newIndent + "(no children)").getBytes());
        }
     }
	
}
