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
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.zip.ZipFile;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io3.stores.PartStore;
import org.docx4j.openpackaging.parts.ExternalTarget;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.utils.BufferUtil;


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
		return this.externalTarget;
	}
	
	private java.nio.ByteBuffer bb;
	public void setBinaryData(InputStream binaryData) {
		log.debug("reading input stream");
		try {
			this.bb = org.docx4j.utils.BufferUtil.readInputStream(binaryData);
			log.debug(".. done" );
		} catch (IOException e) {
			//e.printStackTrace();
			log.error(e.getMessage(), e);
		} finally {
			try {
				log.debug("closing binary input stream");
				binaryData.close();
				log.info(".. closed.");
			} catch (Exception nested) {
				// ignored
				log.error(nested.getMessage(), nested);				
			}
		}
	}	

	public void setBinaryData(byte[] bytes) {
		this.bb = java.nio.ByteBuffer.wrap(bytes);
	}

	public void setBinaryData(ByteBuffer bb) {
		this.bb = bb;
	}
	
	
	/**
	 * Store buffer thru soft reference so it could be 
	 * unloaded by the java vm if free memory is low.
	 */
	private Reference<ByteBuffer> bbRef = null;
	
	/**
	 * @since 3.0
	 */
	public boolean isLoaded() {
		
		if (this.bb != null) {
			return true;
		}
		
		return (this.bbRef != null);
	}
	
	public ByteBuffer getBuffer() {
		ByteBuffer res = null;
		
		if (this.bb != null) {
			// use buffer loaded during package load
			// (if not using Load3)
			res = this.bb;
			
		} else {

			res = (this.bbRef != null) ? this.bbRef.get() : null;
			if (this.getPackage()==null) {
				log.warn("No package owns this part.");
				return null;				
			}
			// no cached buffer, try to load part data now			
			PartStore partStore = this.getPackage().getPartStore();
			if (partStore==null) {
				log.warn("No PartStore configured for this package");
				return null;
			} if (res == null) {
				InputStream is=null;
				try {
					String name = this.partName.getName();
					
					try {
						this.setContentLengthAsLoaded(
								partStore.getPartSize( name.substring(1)));
					} catch (UnsupportedOperationException uoe) {}
					
					is = partStore.loadPart( name.substring(1));
					if (is==null) {
						log.warn(name + " missing from part store");
					} else {
						res = BufferUtil.readInputStream(is);
						// Store buffer thru soft reference so it could be
						// unloaded by the java vm if free memory is low.
						this.bbRef = new SoftReference<ByteBuffer>(res);
					}
				} catch (Docx4JException e) {
					log.error(e.getMessage(), e);
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				} finally {
					IOUtils.closeQuietly(is);
				}
			
			}			
			
		}
		
		res.rewind(); // Don't forget this!
		return res;
	}
	
	/**
	 * Copy the ByteBuffer containing this part's binary data
	 * to an output stream.
	 * 
	 * @param out
	 * @throws IOException
	 */
	public void writeDataToOutputStream(OutputStream out) throws IOException {
		ByteBuffer buf = this.getBuffer();
		
        buf.clear();
        byte[] bytes = new byte[buf.capacity()];
        buf.get(bytes, 0, bytes.length);
        	        
        out.write( bytes );	    
	}
	
    public boolean isContentEqual(Part other) throws Docx4JException {
    	
    	if (!(other instanceof BinaryPart))
    		return false;
    	
    	ByteBuffer thisBB = getBuffer();
    	ByteBuffer thatBB = ((BinaryPart)other).getBuffer();
    	
    	return thisBB.equals(thatBB);
    	
//        if (m_ContentType != arg.m_ContentType)
//            return false;
        
//        if (m_Image.GetLongLength(0) != arg.m_Image.GetLongLength(0))
//            return false;
//        
//        // Compare the arrays byte by byte
//        long length = m_Image.GetLongLength(0);
//        for (long n = 0; n < length; n++)
//            if (m_Image[n] != arg.m_Image[n])
//                return false;
//        return true;
    	
    }
	
}
