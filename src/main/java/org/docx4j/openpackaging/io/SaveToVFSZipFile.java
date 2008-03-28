/*
 *  Copyright 2007, Plutext Pty Ltd.
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

package org.docx4j.openpackaging.io;


import java.io.OutputStream;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.Package;


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
	private static Logger log = Logger.getLogger(SaveToVFSZipFile.class);				
	
	private SaveToZipFile _saveToZipFile;
	
	public SaveToVFSZipFile(Package p) {
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























