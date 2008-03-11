/*
 *  Copyright 2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.openpackaging.parts.WordprocessingML;

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

public class ObfuscatedFontPart extends BinaryPart {

	private static Logger log = Logger.getLogger(ObfuscatedFontPart.class);		
	
	public ObfuscatedFontPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();				
	}
	
	public void init() {
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.OFFICEDOCUMENT_FONT));

		// Used when this Part is added to a rels
		// TODO
		//setRelationshipType(Namespaces.);
	}
	

	static final int BUFF_SIZE = 100000;
	static final byte[] buffer = new byte[BUFF_SIZE];

	public byte[] createFontDataByteArray() throws IOException{
	   InputStream in = getBinaryData();
	   ByteArrayOutputStream out = null;  
	   try {
		   out = new ByteArrayOutputStream();
	      while (true) {
	         synchronized (buffer) {
	            int amountRead = in.read(buffer);
	            if (amountRead == -1) {
	               break;
	            }
	            out.write(buffer, 0, amountRead); 
	         }
	      } 
	      return out.toByteArray();
	   } finally {
	      if (in != null) {
	         in.close();
	      }
	      if (out != null) {
	         out.close();
	      }
	   }
	}	
	
	public void deObfuscate(String fontKey) {
		
		// Convert the input stream into a byte array
		byte[] fontData = null;
		try {
			fontData = createFontDataByteArray();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		log.debug("bytes: " + fontData.length);
		
		log.info("deObfuscating with fontkey: " + fontKey);			
		// INPUT: {1DF903E3-2F14-4575-8028-881FEBABF2AB}

		// See http://openiso.org/Ecma/376/Part4/2.8.1
		// for how to do this.
		
		
		// First, strip {,}
		String tmpString = fontKey.substring(1, fontKey.length()-1);
		log.debug(tmpString);
		
		// Now strip -
		String guidString = tmpString.replace(target, replacement);
		log.debug(guidString);

		
		// Make the font key into a byte array
		byte[] guidByteArray = new byte[16];
		for (int i = 0; i < guidByteArray.length; i++) {
			guidByteArray[i] = fromHexString(guidString.substring(i * 2,
					(i * 2) + 2));
		}
		
		// XOR the reverse of the guidByteArray with 
		// the first and second 16 bytes 
		for (int j = 0; j < 2; j++) {
			for (int i = 0; i < 16; i++) {
				fontData[(j * 16) + i] ^= guidByteArray[15 - i];  // Reverse happens here
			}
		}
		
		// Save the result
		java.io.File f = new java.io.File("/tmp/fontA" + fontData.length +".ttf"); 
		java.io.FileOutputStream fos = null; 
		try {
			fos = new java.io.FileOutputStream(f);
			fos.write(fontData);
			log.debug("wrote: " + fontData.length);
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		log.info("Done!");
		
	}
	
	
	public static byte fromHexString( String hexStr ){
    	byte firstNibble  = Byte.parseByte(hexStr.substring(0,1),16); 
    	byte secondNibble = Byte.parseByte(hexStr.substring(1,2),16);
    	int finalByte = (secondNibble) | (firstNibble << 4 ); 
    	return (byte) finalByte;
	}
	
	
	static java.lang.CharSequence target = (new String("-")).subSequence(0, 1);
    static java.lang.CharSequence replacement = (new String("")).subSequence(0, 0);
	
	
	
}
