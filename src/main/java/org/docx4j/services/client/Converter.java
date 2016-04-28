/*
 *  Copyright 2015-2016, Plutext Pty Ltd.
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
package org.docx4j.services.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Java client library interface for Plutext PDF Converter
 * 
 * @since 3.3.0
 */
public interface Converter {
	
	/** There is no signature here for converting from a WordMLPackage, since
	 *  then docx4j would be a dependency.
	 *  
	 *  Better for docx4j 3 to have converter as a dependency, and extend to
	 *  implement that.
	 */

	/**
	 * Convert File fromFormat to toFormat, streaming result to OutputStream os.
	 * 
	 * fromFormat supported: DOC, DOCX
	 * 
	 * toFormat supported: PDF
	 * 
	 * @param f
	 * @param fromFormat
	 * @param toFormat
	 * @param os
	 * @throws IOException
	 * @throws ConversionException
	 */
	public void convert(File f, Format fromFormat, Format toFormat, OutputStream os) throws IOException, ConversionException;



	/**
	 * Convert InputStream fromFormat to toFormat, streaming result to OutputStream os.
	 * 
	 * fromFormat supported: DOC, DOCX
	 * 
	 * toFormat supported: PDF
	 * 
	 * Note this uses a non-repeatable request entity, so it may not be suitable
	 * (depending on the endpoint config).
	 * 
	 * @param instream
	 * @param fromFormat
	 * @param toFormat
	 * @param os
	 * @throws IOException
	 * @throws ConversionException
	 */
	public void convert(InputStream instream, Format fromFormat, Format toFormat, OutputStream os) throws IOException, ConversionException;


	/**
	 * Convert byte array fromFormat to toFormat, streaming result to OutputStream os.
	 * 
	 * fromFormat supported: DOC, DOCX
	 * 
	 * toFormat supported: PDF
	 * 
	 * @param bytesIn
	 * @param fromFormat
	 * @param toFormat
	 * @param os
	 * @throws IOException
	 * @throws ConversionException
	 */
	public void convert(byte[] bytesIn, Format fromFormat, Format toFormat, OutputStream os) throws IOException, ConversionException;
	
}