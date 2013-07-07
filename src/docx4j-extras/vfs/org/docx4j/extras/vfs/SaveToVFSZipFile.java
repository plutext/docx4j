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

package org.docx4j.extras.vfs;


import java.io.OutputStream;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.OpcPackage;


/**
 * Save a Package object to a Zip file.
 * 
 * 28/03/08 - The current Commons-VFS library does not support writing to 
 * zip file. Therefore we do not use "zip://" uri scheme to write.
 * 
 * @author Jojada Tirtowidjojo - 28/03/2008
 *
 */
public class SaveToVFSZipFile {
	private static Logger log = LoggerFactory.getLogger(SaveToVFSZipFile.class);				
	
	private SaveToZipFile _saveToZipFile;
	
	public SaveToVFSZipFile(OpcPackage p) {
		_saveToZipFile = new SaveToZipFile(p);
	}
		
	/**
	 * Save the contained Package as a Zip file in the file system 
	 * 
	 * @param filepath The destination file path whose syntax has to 
	 * follow Apache Commons-VFS uri file system syntax.
	 * @return true if successful;
	 *         false, otherwise.
	 * @throws Docx4JException if there is an error
	 */
	public boolean save(String filepath) throws Docx4JException  {
		log.info("Saving to" + filepath );
		
		boolean success = true;
		
		try {
			FileObject fo = VFS.getManager().resolveFile(filepath);
			success = save(fo);
		} catch (FileSystemException exc) {
			exc.printStackTrace();
			throw new Docx4JException("Couldn't get FileObject", exc);
		}
		
		return success;
	}

	/**
	 * Save the contained Package as a Zip file in the file system 
	 * 
	 * @param docxFile A destination FileObject
	 * @return true if successful;
	 *         false, otherwise.
	 * @throws Docx4JException if there is an error
	 */
	public boolean save(FileObject docxFile) throws Docx4JException  {
		log.info("Saving to" +  docxFile );	
		
		boolean success = false;
		
		try {
			OutputStream docxOut = docxFile.getContent().getOutputStream();
			success = _saveToZipFile.save(docxOut);
		} catch (FileSystemException exc) {
			exc.printStackTrace();
			throw new Docx4JException("Failed to save package", exc);
		}
		
		try {
			docxFile.close();
		} catch (FileSystemException exc) {
			;//ignore
		}
		
		return success;
	}
} //SaveToVFSZipFile class























