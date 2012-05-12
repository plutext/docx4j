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

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.vfs.CacheStrategy;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.commons.vfs.provider.UriParser;

/**
 *	@author Jojada Tirtowidjojo - 28/03/2008
 */
public class VFSUtils {
	private static FileSystemManager fileSystemManager;
	private static ReadWriteLock aLock = new ReentrantReadWriteLock(true);
	
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

	public static FileSystemManager getFileSystemManager() {
		aLock.readLock().lock();

		try {
			if (fileSystemManager == null) {
				try {
					StandardFileSystemManager fm = new StandardFileSystemManager();
					fm.setCacheStrategy(CacheStrategy.MANUAL);
					fm.init();
					fileSystemManager = fm;
				} catch (Exception exc) {
					throw new RuntimeException(exc);
				}
			}

			return fileSystemManager;
		}
        finally
        {
            aLock.readLock().unlock();
        }
    }
	
} // VFSUtils class



















