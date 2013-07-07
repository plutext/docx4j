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



import java.io.File;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileTypeSelector;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.provider.local.LocalFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.extras.vfs.VFSUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.Load;
import org.docx4j.openpackaging.io.LoadFromZipFile;
import org.docx4j.openpackaging.packages.OpcPackage;


/**
 * Create a Package object from a Zip file.
 * 
 *	@author Jojada Tirtowidjojo - 28/03/2008
 *
 */
public class LoadFromVFSZipFile extends Load {
	
	private static Logger log = LoggerFactory.getLogger(LoadFromVFSZipFile.class);

	// Testing
	public static void main(String[] args) throws Exception {
		String filepath = "file:///C:/Users/jojada/Documents/This%20is%20Heading1.docx";
		//String filepath = 
		//	"webdav://jojada:password@192.168.23.129:8080/alfresco/webdav/User%20Homes/jojada/This%20is%20Heading1.docx";
		log.info("Path: " + filepath );
		LoadFromVFSZipFile loader = new LoadFromVFSZipFile(true);
		loader.get(filepath);		
	}

	private LoadFromZipFile _loadFromZipFile;

	public LoadFromVFSZipFile() {
		this(true);
	}
	
	public LoadFromVFSZipFile(boolean loadExternalTargets) {
		_loadFromZipFile = new LoadFromZipFile();
		_loadFromZipFile.loadExternalTargets(loadExternalTargets);
	}

//	public LoadFromVFSZipFile(ContentTypeManager ctm, boolean loadExternalTargets) {
//		_loadFromZipFile = new LoadFromZipFile(ctm);
//		_loadFromZipFile.loadExternalTargets(loadExternalTargets);
//	}
	
	public OpcPackage get(String filepath) throws Docx4JException {
		OpcPackage thePackage = null;
		try {
			FileObject fo = VFS.getManager().resolveFile(filepath);
			thePackage = getPackageFromFileObject(fo);
		} catch (FileSystemException exc) {
			exc.printStackTrace();
			throw new Docx4JException("Couldn't get FileObject", exc);
		}
		return thePackage;
	}
	
	public OpcPackage getPackageFromFileObject(FileObject fo) throws Docx4JException {
		OpcPackage thePackage = null;
		
		if (!(fo instanceof LocalFile)) {
			//Non-Local file such as webdav file.
			//We make a temporary local copy of the file.
			//TODO: 28/03/08 - This is a second approach of dealing with non-local file.
			//The first approach which is more preferable and done in SVN version 233
			//does not create a temporary local copy of the file. 
			//It uses Commons-VFS "zip://" uri scheme to directly access the non-local file.
			//This second approach is being carried out because the writing operation cannot
			//use Commons-VFS "zip://" scheme as the scheme does not support writing to 
			//zip file yet.
			//TODO: 28/03/08 - DO NOT use "tmp://" scheme to make a temporary file. 
			//as it is going to fail. The current Commons-VFS library is still buggy. 
			StringBuffer sb = new StringBuffer("file:///");
			sb.append(System.getProperty("user.home"));
			sb.append("/.docx4j/tmp/");
			sb.append(fo.getName().getBaseName());
			String tmpPath = sb.toString().replace('\\', '/');
			
			LocalFile localCopy = null;
			try {
				localCopy = (LocalFile) VFS.getManager().resolveFile(tmpPath);
				localCopy.copyFrom(fo, new FileTypeSelector(FileType.FILE));
				localCopy.close();
				
				thePackage = getPackageFromLocalFile(localCopy);
			} catch (FileSystemException exc) {
				exc.printStackTrace();
				throw new Docx4JException("Could not create a temporary local copy", exc);
			} finally {
				if (localCopy != null) {
					try {
						localCopy.delete();
					} catch (FileSystemException exc) {
						exc.printStackTrace();
						log.warn("Couldn't delete temporary file " + tmpPath);
					}
				}
			}
		} else {
			thePackage = getPackageFromLocalFile((LocalFile) fo);
		}
		
		return thePackage;
	}
	
	public OpcPackage getPackageFromLocalFile(LocalFile fo) throws Docx4JException {
		OpcPackage thePackage = null;
		
		String localPath = VFSUtils.getLocalFilePath(fo);
		if (localPath == null) {
			throw new Docx4JException("Couldn't get local path");
		}
		
		thePackage = _loadFromZipFile.get(new File(localPath));
		return thePackage;
	}
	
} // LoadFromVFSZipFile class


























