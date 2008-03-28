/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of Docx4all.

    Docx4all is free software: you can redistribute it and/or modify
    it under the terms of version 3 of the GNU General Public License 
    as published by the Free Software Foundation.

    Docx4all is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License   
    along with Docx4all.  If not, see <http://www.gnu.org/licenses/>.
    
 */

package org.docx4j.utils;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.UriParser;

/**
 *	@author Jojada Tirtowidjojo - 28/03/2008
 */
public class VFSUtils {
	
	/**
	 * Returns the absolute path of Apache Commons-VFS FileObject if its uri scheme is 'file://'
	 * This absolute path then can be used to construct java.io.File object.
	 * 
	 * @param fo 
	 * @return local absolute path if any;
	 *         null, otherwise
	 */
	public final static String getLocalFilePath(org.apache.commons.vfs.FileObject fo) {
		String thePath = fo.getName().getURI();

		String localScheme = "file://";
		if (thePath.startsWith(localScheme)) {
			//uri syntax of local file system has this format "file:///[absoultePath]"
			try {
				int idx = localScheme.length();
				if (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") > -1) {
					//In Windows we're going to chop the leading "file:///" off.
					idx++;
				}
				thePath = thePath.substring(idx);
				thePath = UriParser.decode(thePath);
			} catch (FileSystemException exc) {
				exc.printStackTrace();
				thePath = null;
			}
		} else {
			thePath = null;
		}
		
		return thePath;
	}
	
	private VFSUtils() {
		;//uninstantiable
	}

} // VFSUtils class



















