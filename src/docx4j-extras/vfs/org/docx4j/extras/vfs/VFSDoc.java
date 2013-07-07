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


import java.io.FileInputStream;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileTypeSelector;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.provider.local.LocalFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.convert.in.Doc;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * @author jason
 *
 */
public class VFSDoc {
	private static Logger log = LoggerFactory.getLogger(VFSDoc.class);
	
//	public static boolean convert(org.apache.commons.vfs.FileObject in,
//	WordprocessingMLPackage out) throws Exception {
	
	/**
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public static WordprocessingMLPackage convert(
			org.apache.commons.vfs.FileObject in) throws Exception {

		LocalFile localCopy = null;
		if (!(in instanceof LocalFile)) {

			StringBuffer sb = new StringBuffer("file:///");
			sb.append(System.getProperty("user.home"));
			sb.append("/.docx4j/tmp/");
			sb.append(in.getName().getBaseName());
			String tmpPath = sb.toString().replace('\\', '/');

			try {
				localCopy = (LocalFile) VFS.getManager().resolveFile(tmpPath);
				localCopy.copyFrom(in, new FileTypeSelector(FileType.FILE));
				localCopy.close();
			} catch (FileSystemException exc) {
				exc.printStackTrace();
				throw new Docx4JException(
						"Could not create a temporary local copy", exc);
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
			localCopy = (LocalFile) in;
		}

		String localPath = VFSUtils.getLocalFilePath(in);
		if (localPath == null) {
			throw new Docx4JException("Couldn't get local path");
		}
		
		return Doc.convert(new FileInputStream(localPath));
	}
}
