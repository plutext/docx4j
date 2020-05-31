package org.docx4j.convert.out.documents4j.remote;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.convert.out.Documents4jConversionSettings;
import org.docx4j.convert.out.common.Exporter;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.Docx4JRuntimeException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.RemoteConverter;

/**
 * Convert docx/xlsx to PDF using Documents4j, with Microsoft Word running on a 
 * remote server. 
 * 
 * @author jharrop
 * @since 8.2.0
 */
public class Documents4jRemoteExporter implements Exporter<Documents4jConversionSettings> {

	private static Logger log = LoggerFactory.getLogger(Documents4jRemoteExporter.class);		
	
	protected static Documents4jRemoteExporter instance = null;
	
	public static Exporter<Documents4jConversionSettings> getInstance() {
		if (instance == null) {
			synchronized(Documents4jRemoteExporter.class) {
				if (instance == null) {
					instance = new Documents4jRemoteExporter();
				}
			}
		}
		return instance;
	}
	
	
	private IConverter converter;
	
	/**
	 * Configure the converter with default settings.
	 */
	public Documents4jRemoteExporter() {

		/* a RemoteConverter will always perform better when handed instances of 
		 * InputStream and OutputStream as source and target compared to files. 
		 * */
       converter = RemoteConverter.builder()
               .baseFolder(tmpDir)
               .workerPool(20, 25, 2, TimeUnit.SECONDS)
               .requestTimeout(10, TimeUnit.SECONDS)
               .baseUri(getConverterUri())
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
	public Documents4jRemoteExporter(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, long timeout) {

       converter = RemoteConverter.builder()
               .baseFolder(tmpDir)
               .workerPool(20, 25, 2, TimeUnit.SECONDS)
               .requestTimeout(timeout, TimeUnit.MILLISECONDS)
               .baseUri(getConverterUri())
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

		if (pkg instanceof WordprocessingMLPackage) {
			
			// Avoid creating a temp file; documents4j wants an inputstream
			ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
			Docx4J.save( (WordprocessingMLPackage)pkg, baos);
			
			try {
				export(outputStreamToInputStream(baos), outputStream, DocumentType.MS_WORD);
			} catch (IOException e) {
				throw new Docx4JException(e.getMessage(), e);
			} 
			
//			try {
//				file = File.createTempFile("", ".docx", tmpDir);
//			} catch (IOException e) {
//				throw new Docx4JException(e.getMessage(), e);
//			}
//			Docx4J.save( (WordprocessingMLPackage)pkg, file);
//			export(file, outputStream, DocumentType.MS_WORD);
			
		} else if (pkg instanceof SpreadsheetMLPackage) {

			ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
			pkg.save( baos);
			
			try {
				export(outputStreamToInputStream(baos), outputStream, DocumentType.MS_EXCEL);
			} catch (IOException e) {
				throw new Docx4JException(e.getMessage(), e);
			} 
			
		} else if (pkg instanceof PresentationMLPackage) {
			// https://github.com/documents4j/documents4j/pull/29
			throw new Docx4JException("pptx export is not available via documents4j"); 
		}
		
		
		
	}
	
	private PipedInputStream outputStreamToInputStream(ByteArrayOutputStream baos) throws IOException {

		// https://stackoverflow.com/a/23874232/1031689
		PipedInputStream in = new PipedInputStream();
		final PipedOutputStream out = new PipedOutputStream(in);
		new Thread(
		  new Runnable(){
			  public void run(){
				try {
					baos.writeTo(out);
				} catch (IOException e) {
					// TODO
					e.printStackTrace();
				} finally {
					// close the PipedOutputStream here because we're done writing data
					// once this thread has completed its run
					IOUtils.closeQuietly(out);
				}
			  }
		}).start(); 
		return in;
		
	}

	/** A RemoteConverter should perform better when handed instances of 
	 * InputStream and OutputStream as source and target compared to files,
	 * so this method is to be preferred. 
	 * @param src
	 * @param target
	 * @param documentType
	 * @throws Docx4JException
	 */
	public void export(InputStream src, OutputStream target, DocumentType documentType)
			throws Docx4JException {
	
       converter.convert(src).as(documentType)
               .to(target).as(DocumentType.PDF).execute();
		
	}
	
	public void export(File wordFile , OutputStream target, DocumentType documentType)
			throws Docx4JException {
	

       converter.convert(wordFile).as(documentType)
               .to(target).as(DocumentType.PDF).execute();
		
	}
	
	private String getConverterUri() {
		
    	String converterUri = Docx4jProperties.getProperty("docx4j.convert.out.documents4j.remote.Uri");
    	if (converterUri==null) {
    		throw new Docx4JRuntimeException("Converter URI not specified; set property 'docx4j.convert.out.documents4j.remote.Uri'");
    	}
		return converterUri;
	}

	
    private static final String TEMP_DIR_DEFAULT = "temp_documents4j_remote";
	
    private static File tmpDir = null; 
    
    static {
    	
    	String tmpDirPath = Docx4jProperties.getProperty("docx4j.convert.out.documents4j.remote.tmpDir");
    	
    	if (tmpDirPath==null) {
    		
	        File userHome = getUserHome();
	        if (userHome == null) {
	        	log.warn("No home dir found; consider setting property 'docx4j.convert.out.documents4j.remote.tmpDir'");
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
    
//    public static String getTmpDirAsString() {
//    	
//    	if (getTmpDir()==null) {
//    		throw new RuntimeException("No dir configured for documents4j!  Either set system property user.home to a writable dir, "
//    				+ "or configure docx4j property 'docx4j.convert.out.documents4j.remote.tmpDir'");
//    	}
//    	try {
//			return getTmpDir().getCanonicalPath();
//		} catch (IOException e) {
//			log.error(e.getMessage(),e);
//			return null;
//		}
//    }
    
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
