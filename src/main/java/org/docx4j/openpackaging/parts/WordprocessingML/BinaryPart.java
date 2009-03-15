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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.ExternalTarget;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;


public class BinaryPart extends Part {
	
	public BinaryPart(PartName partName) throws InvalidFormatException {
		super(partName);
		
		// Can't setContentType or setRelationshipType, since 
		// these will differ depending on the nature of the data.
		// Common binary parts should extend this class to 
		// provide that information.
		
	}
	
	
	ExternalTarget externalTarget = null;	
	public BinaryPart(ExternalTarget externalTarget) {
		
		this.externalTarget = externalTarget;
		
	}
	public ExternalTarget getExternalTarget() {
		return externalTarget;
	}
	
//	private InputStream binaryData;
//
//	public InputStream getBinaryData() {
//		return binaryData;
//	}

	java.nio.ByteBuffer bb;
	public void setBinaryData(InputStream binaryData) {
		
		log.debug("reading input stream");
		try {
			bb = org.docx4j.utils.BufferUtil.readInputStream(binaryData);
			log.debug(".. done" );
		} catch (IOException e) {
			//e.printStackTrace();
			log.error(e);
		} finally {
			
			try {
				log.debug("closing binary input stream");
				binaryData.close();
				log.info(".. closed.");
			} catch (Exception nested) {}
			
		}
	}	

	public void setBinaryData(byte[] bytes) {
		
		bb = java.nio.ByteBuffer.wrap(bytes);
		
	}
	
	
	public java.nio.ByteBuffer getBuffer() {
		
		bb.rewind(); // Don't forget this!
		
		return bb;
	}
	
	/**
	 * Copy the ByteBuffer containing this part's binary data
	 * to an output stream.
	 * 
	 * @param out
	 * @throws IOException
	 */
	public void writeDataToOutputStream(OutputStream out) throws IOException {
		
        bb.clear();
        byte[] bytes = new byte[bb.capacity()];
        bb.get(bytes, 0, bytes.length);
        	        
        out.write( bytes );	    
		
	}
	
		
}
