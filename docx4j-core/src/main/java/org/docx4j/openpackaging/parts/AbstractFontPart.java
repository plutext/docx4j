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

package org.docx4j.openpackaging.parts;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.docx4j.Docx4jProperties;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractFontPart extends BinaryPart {

	private static Logger log = LoggerFactory.getLogger(AbstractFontPart.class);		

	public abstract PhysicalFont extract(String fontNameAsInTablePart, String fontFileName, String fontKey, Set<File> embeddedFontTempFiles);
	
	
	private java.io.File f; // the temp embedded font file
	
	
    /** docx4j's user directory name */
    private static final String DOCX4J_USER_DIR = ".docx4all"; 
    	// docx4all already creates a dir; no point creating a second 

    /** font cache file path */
    private static final String TEMPORARY_FONT_DIR = "temporary embedded fonts";
	
    private static File tmpFontDir = null; 
	public static File getTmpFontDir() {
		return tmpFontDir;
	}
    
    static {
    	
    	String tmpFontDirPath = Docx4jProperties.getProperty("docx4j.openpackaging.parts.WordprocessingML.ObfuscatedFontPart.tmpFontDir");
    	
    	if (tmpFontDirPath==null) {
    		
	        File userHome = getUserHome();
	        if (userHome == null) {
	        	log.warn("No home dir found; consider setting property 'docx4j.openpackaging.parts.WordprocessingML.ObfuscatedFontPart.tmpFontDir'");
	        } else {
	        	
	            File docx4jUserDir = new File(userHome, DOCX4J_USER_DIR);
	            docx4jUserDir.mkdir();
	            tmpFontDir = new File(docx4jUserDir, TEMPORARY_FONT_DIR);
	            getTmpFontDir().mkdir();
	        }    	
    		
    	} else {

    		tmpFontDir = new File(tmpFontDirPath);
            if (!getTmpFontDir().exists() ) {
            	log.info(tmpFontDirPath + " does not exist. Attempting to create..");
	            getTmpFontDir().mkdir();
            } else if (!getTmpFontDir().isDirectory()) {
            	log.info(tmpFontDirPath + " exists, but is not a directory!") ;          	
            }
    	
    	}

	}
    
    public static String getTemporaryEmbeddedFontsDir() {
    	
    	if (getTmpFontDir()==null) {
    		throw new RuntimeException("No dir configured for temp fonts!  Either set system property user.home to a writable dir, or configure docx4j property 'docx4j.openpackaging.parts.WordprocessingML.ObfuscatedFontPart.tmpFontDir'");
    	}
    	try {
			return getTmpFontDir().getCanonicalPath();
		} catch (IOException e) {
			log.error(e.getMessage(),e);
			return null;
		}
    }
    
    private static File getUserHome() {
        String s = System.getProperty("user.home");
        if (s != null) {
            File userDir = new File(s);
            if (userDir.exists()) {
                return userDir;
            }
        }
        return null;
    }
    

	public AbstractFontPart(PartName partName) throws InvalidFormatException {
		super(partName);
	}
			
	public java.io.File getF() {
		return f;
	}

	public void setF(java.io.File f) {
		this.f = f;
	}

	protected File createFile(String fontFileName) throws IOException {
		return File.createTempFile(fontFileName, ".ttf", getTmpFontDir());
		
	}
	
}
