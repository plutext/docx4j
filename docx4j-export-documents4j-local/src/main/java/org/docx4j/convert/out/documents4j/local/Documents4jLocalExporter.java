/**
 *  Copyright 2020, Plutext Pty Ltd.
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

 **/
package org.docx4j.convert.out.documents4j.local;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.convert.out.Documents4jConversionSettings;
import org.docx4j.convert.out.common.Exporter;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;

/**
 * Convert docx/xlsx to PDF using Documents4j, with Microsoft Word running locally. 
 * 
 * @author jharrop
 * @since 8.2.0
 */
public class Documents4jLocalExporter implements Exporter<Documents4jConversionSettings> {
	
	private static Logger log = LoggerFactory.getLogger(Documents4jLocalExporter.class);		
	
	protected static Documents4jLocalExporter instance = null;
	
	public static Exporter<Documents4jConversionSettings> getInstance() {
		if (instance == null) {
			synchronized(Documents4jLocalExporter.class) {
				if (instance == null) {
					instance = new Documents4jLocalExporter();
				}
			}
		}
		return instance;
	}
	
	
	private IConverter converter;

	/**
	 * Configure the converter with default settings.
	 */
	public Documents4jLocalExporter() {

       converter = LocalConverter.builder()
               .baseFolder(tmpDir)
               .workerPool(20, 25, 2, TimeUnit.SECONDS)
               .processTimeout(30, TimeUnit.SECONDS)
               .build();		
	}
	
    /**
     * Configures a worker pool for the converter. This worker pool implicitly sets a maximum
     * number of conversions that are concurrently undertaken by the resulting converter. When a
     * converter is requested to concurrently execute more conversions than {@code maximumPoolSize},
     * it will queue excess conversions until capacities are available again.
     * <p>&nbsp;</p>
     * If this number is set too low, the concurrent performance of the resulting converter will be weak
     * compared to a higher number. If this number is set too high, the converter might <i>overheat</i>
     * when accessing the underlying external resource (such as for example an external process or a
     * HTTP connection). A remote converter that shares a conversion server with another converter might
     * also starve these other remote converters.
     *
     * @param corePoolSize    The core pool size of the worker pool.
     * @param maximumPoolSize The maximum pool size of the worker pool.
     * @param keepAliveTime   The keep alive time of the worker pool.
     * @param unit            The time unit of the specified keep alive time.
     * @param timeout The timeout for a network request (in ms).
     * 
     */
	public Documents4jLocalExporter(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, long timeout) {

	       converter = LocalConverter.builder()
	               .baseFolder(tmpDir)
	               .workerPool(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS)
	               .processTimeout(30, TimeUnit.SECONDS)
	               .build();		
	}
	
	
	@Override
	public void export(Documents4jConversionSettings conversionSettings, OutputStream outputStream)
			throws Docx4JException {

		OpcPackage pkg = conversionSettings.getOpcPackage();
		
		export(pkg, outputStream);
	}

	public void export(OpcPackage pkg , OutputStream outputStream)
			throws Docx4JException {

		/* The LocalConverter communicates with a backing conversion software 
		 * such as MS Word by using the file system. Therefore, instances of 
		 * File as source and target input are better.
		 * */
		
		File file = null;
		if (pkg instanceof WordprocessingMLPackage) {

			try {
				file = File.createTempFile("", ".docx", tmpDir);
			} catch (IOException e) {
				throw new Docx4JException(e.getMessage(), e);
			}
			Docx4J.save( (WordprocessingMLPackage)pkg, file);
			export(file, outputStream, DocumentType.MS_WORD);
			
		} else if (pkg instanceof SpreadsheetMLPackage) {

			try {
				file = File.createTempFile("", ".xlsx", tmpDir);
			} catch (IOException e) {
				throw new Docx4JException(e.getMessage(), e);
			}
			Docx4J.save( (WordprocessingMLPackage)pkg, file);
			export(file, outputStream, DocumentType.MS_EXCEL);
			
		} else if (pkg instanceof PresentationMLPackage) {
			// https://github.com/documents4j/documents4j/pull/29
			throw new Docx4JException("pptx export is not available via documents4j"); 
		}
	}

	public void export(File officeFile , OutputStream target, DocumentType documentType)
			throws Docx4JException {
	
       converter.convert(officeFile).as(documentType)
               .to(target).as(DocumentType.PDF).execute();		
	}
	
    private static final String TEMP_DIR_DEFAULT = "temp_documents4j_local";
	
    private static File tmpDir = null; 
    
    static {
    	
    	String tmpDirPath = Docx4jProperties.getProperty("docx4j.convert.out.documents4j.local.tmpDir");
    	
    	if (tmpDirPath==null) {
    		
	        File userHome = getUserHome();
	        if (userHome == null) {
	        	log.warn("No home dir found; consider setting property 'docx4j.convert.out.documents4j.local.tmpDir'");
	        } else {
	        	
	            tmpDir = new File(userHome, TEMP_DIR_DEFAULT);
	            tmpDir.mkdir();
	        }    	
    		
    	} else {

    		tmpDir = new File(tmpDirPath);
            if (!tmpDir.exists() ) {
            	log.info(tmpDirPath + " does not exist. Attempting to create..");
            	tmpDir.mkdir();
            } else if (!tmpDir.isDirectory()) {
            	log.info(tmpDirPath + " exists, but is not a directory!") ;          	
            }
    	
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
	
	
}	
