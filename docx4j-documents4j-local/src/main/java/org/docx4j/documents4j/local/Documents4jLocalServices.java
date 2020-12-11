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
package org.docx4j.documents4j.local;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
 * Import/update/export docx/xlsx using Documents4j, with Microsoft Word running locally.
 * 
 * You can use this to export to PDF, or to update your ToC.  It can also import binary 
 * .doc or RTF.   
 * 
 * You can customise your conversion script by specifying one in docx4j.properties,
 * under key: com.documents4j.conversion.msoffice.word_convert.vbs
 * 
 * If you do not do that, the document4j's default script will be used
 * (which doesn't update ToC).  A sample override script (which updates ToC) is in 
 * docx4j-samples-resources. You'll need that script (or one like it) to update ToC.
 * 
 * @author jharrop
 * @since 8.2.0
 */
public class Documents4jLocalServices implements Exporter<Documents4jConversionSettings> {
	
	private static Logger log = LoggerFactory.getLogger(Documents4jLocalServices.class);		
	
	protected static Documents4jLocalServices instance = null;
	
	public static Exporter<Documents4jConversionSettings> getInstance() {
		if (instance == null) {
			synchronized(Documents4jLocalServices.class) {
				if (instance == null) {
					instance = new Documents4jLocalServices();
				}
			}
		}
		return instance;
	}
	
	
	private IConverter converter;

	/**
	 * Configure the converter with default settings.
	 */
	public Documents4jLocalServices() {
		
		setWordConversionScript();	       
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
	public Documents4jLocalServices(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, long timeout) {

		setWordConversionScript();	       
		converter = LocalConverter.builder()
               .baseFolder(tmpDir)
               .workerPool(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS)
               .processTimeout(30, TimeUnit.SECONDS)
               .build();
	       
	}

	private void setWordConversionScript() {

		String script = System.getProperty("com.documents4j.conversion.msoffice.word_convert.vbs");
		if (script==null) {
			script = Docx4jProperties.getProperty("com.documents4j.conversion.msoffice.word_convert.vbs");
			if (script==null) {
				log.info("documents4j: using default Word conversion script" );
			} else {
				log.info("documents4j: using script: " + script + " from docx4j.properties");
				// We need the property as a System property
		        System.setProperty("com.documents4j.conversion.msoffice.word_convert.vbs", script);
			}
		} else {
			log.info("documents4j: using script: " + script );							
			// We need the property as a System property
	        System.setProperty("com.documents4j.conversion.msoffice.word_convert.vbs", script); 
		}		
	}
	
	
	/**
	 * Export to outputStream as PDF.  Make sure you have setOpcPackage in conversionSettings.
	 * That's the only value which matters to documents4j.
	 */
	@Override
	public void export(Documents4jConversionSettings conversionSettings, OutputStream outputStream)
			throws Docx4JException {

		OpcPackage pkg = conversionSettings.getOpcPackage();
		
		export(pkg, outputStream);
	}

	/**
	 * Export WordprocessingMLPackage or SpreadsheetMLPackage as PDF
	 * 
	 * @param pkg
	 * @param outputStream
	 * @throws Docx4JException
	 */
	public void export(OpcPackage pkg , OutputStream outputStream)
			throws Docx4JException {
		
		export(pkg, outputStream, DocumentType.PDF);
	}
	
	/**
	 * Export WordprocessingMLPackage or SpreadsheetMLPackage as specified com.documents4j.api.DocumentType
	 * 
	 * @param pkg
	 * @param outputStream
	 * @throws Docx4JException
	 */
	public void export(OpcPackage pkg , OutputStream outputStream, DocumentType asDocumentType)
			throws Docx4JException {

		/* The LocalConverter communicates with a backing conversion software 
		 * such as MS Word by using the file system. Therefore, instances of 
		 * File as source and target input are better.
		 * */
		
		File file = null;
		if (pkg instanceof WordprocessingMLPackage) {

			try {
				file = File.createTempFile("docx_", ".docx", tmpDir); //Prefix string "" too short: length must be at least 3
			} catch (IOException e) {
				throw new Docx4JException(e.getMessage(), e);
			}
			Docx4J.save( (WordprocessingMLPackage)pkg, file);
			export(file, outputStream, DocumentType.MS_WORD, asDocumentType);
			
		} else if (pkg instanceof SpreadsheetMLPackage) {

			try {
				file = File.createTempFile("xlsx_", ".xlsx", tmpDir);
			} catch (IOException e) {
				throw new Docx4JException(e.getMessage(), e);
			}
			Docx4J.save( (WordprocessingMLPackage)pkg, file);
			export(file, outputStream, DocumentType.MS_EXCEL, asDocumentType);
			
		} else if (pkg instanceof PresentationMLPackage) {
			// https://github.com/documents4j/documents4j/pull/29
			throw new Docx4JException("pptx export is not available via documents4j"); 
		}
	}

	/**
	 * Export docx or xlsx file as PDF
	 * 
	 * @param officeFile
	 * @param target
	 * @param documentType
	 * @throws Docx4JException
	 */
	public void export(File officeFile , OutputStream target, DocumentType documentType)
			throws Docx4JException {
		
		export(officeFile, target, documentType, DocumentType.PDF);
	}
	
	/**
	 * Export docx or xlsx file as specified com.documents4j.api.DocumentType
	 * 
	 * @param officeFile
	 * @param target
	 * @param documentType
	 * @param asDocumentType
	 * @throws Docx4JException
	 */
	public void export(File officeFile , OutputStream target, DocumentType documentType, DocumentType asDocumentType)
			throws Docx4JException {

       converter.convert(officeFile).as(documentType)
               .to(target).as(asDocumentType).execute();		
	}

	
	/**
	 * Run the script to update the docx (eg ToC). See class Javadoc for how to configure your script.
	 * Documents4j writes a new docx file to outputStream.
	 * 
	 * @param pkg
	 * @param outputStream
	 * @throws Docx4JException
	 */
	public void updateDocx(WordprocessingMLPackage pkg , OutputStream outputStream) 
			throws Docx4JException {

		File file = null;
		try {
			file = File.createTempFile("docx_", ".docx", tmpDir); //Prefix string "" too short: length must be at least 3
		} catch (IOException e) {
			throw new Docx4JException(e.getMessage(), e);
		}
		Docx4J.save( (WordprocessingMLPackage)pkg, file);
		updateDocx(file, outputStream);
	}
	
	/**
	 * Run the script to update the docx (eg ToC). See class Javadoc for how to configure your script.
	 * Returns a new WordprocessingMLPackage.
	 * 
	 * @param pkg
	 * @return
	 * @throws Docx4JException
	 */
	public WordprocessingMLPackage updateDocx(WordprocessingMLPackage pkg) 
			throws Docx4JException {

		File file = null;
		try {
			file = File.createTempFile("docx_", ".docx", tmpDir); //Prefix string "" too short: length must be at least 3
		} catch (IOException e) {
			throw new Docx4JException(e.getMessage(), e);
		}
		Docx4J.save( (WordprocessingMLPackage)pkg, file);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		
		updateDocx(file, baos);
		
		return Docx4J.load(new ByteArrayInputStream(baos.toByteArray()));
	}
	
	
	/**
	 * Run the script to update the docx (eg ToC). See class Javadoc for how to configure your script.
	 * 
	 * @param officeFile
	 * @param target
	 * @param documentType
	 * @throws Docx4JException
	 */
	public void updateDocx(File officeFile , OutputStream target)
			throws Docx4JException {
		
		export(officeFile, target, DocumentType.MS_WORD, DocumentType.DOCX);
	}
	
	/**
	 * Import as a docx from one of the other file types supported by Word.
	 * 
	 * @param officeFile
	 * @param target
	 * @param documentType
	 * @throws Docx4JException
	 */
	public void importAsDocx(File officeFile , OutputStream target)
			throws Docx4JException {
		
		export(officeFile, target, DocumentType.MS_WORD, DocumentType.DOCX);
	}

	/**
	 * Import as a docx from one of the other file types supported by Word.
	 * 
	 * @param officeFile
	 * @throws Docx4JException
	 */
	public WordprocessingMLPackage importAsDocx(File officeFile)
			throws Docx4JException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		importAsDocx(officeFile, baos);
		
		return Docx4J.load(new ByteArrayInputStream(baos.toByteArray()));
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
