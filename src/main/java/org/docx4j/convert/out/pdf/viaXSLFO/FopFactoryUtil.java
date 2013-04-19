package org.docx4j.convert.out.pdf.viaXSLFO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.log4j.Logger;

/* For FOP 1.1 and earlier,
 * 
 *    FopFactory fopFactory = FopFactory.newInstance();
 *    fopFactory.setUserConfig(Configuration userConfig);
 *    
 * For FOP >1.1, it is:
 * 
 *    FopFactoryBuilder fopFactoryBuilder = new FopConfParser(InputStream fopConfStream, URI defaultBaseURI).getFopFactoryBuilder()
 *    FopFactory fopFactory = fopFactoryBuilder.build();
 *    
 * The objective of this class is to be able to configure any version of FOP
 */
public class FopFactoryUtil {
	
	protected static Logger log = Logger.getLogger(FopFactoryUtil.class);
	//used as a ThreadLocal
	protected static Map<Long, FopFactory> fopFactories = new TreeMap<Long, FopFactory>();
	// Any reason for many FopFactory?  See http://wiki.apache.org/xmlgraphics-fop/FopFactoryConfiguration
	// Answer: A reuse of the FopFactory isn't possible as long as the userConfiguration
	// contains a per document font mapping


//	protected static Fop createFop(Configuration userConfiguration, String outputFormat, OutputStream outputStream) throws FOPException {
//		
//		FopFactory fopFactory = getFopFactory(userConfiguration);
//		return fopFactory.newFop(outputFormat != null ? outputFormat : MimeConstants.MIME_PDF, outputStream);
//	}
	public static Fop createFop(String userConfiguration, String outputFormat, OutputStream outputStream) throws FOPException {
		
		FopFactory fopFactory = getFopFactory(userConfiguration);
		return fopFactory.newFop(outputFormat != null ? outputFormat : MimeConstants.MIME_PDF, outputStream);
	}

	
	protected static FopFactory getFopFactory(String userConfig) throws FOPException {
//	protected static FopFactory getFopFactory(Configuration userConfig) throws FOPException {
		
		FopFactory fopFactory = fopFactories.get(Thread.currentThread().getId());
		if (fopFactory == null) {
			synchronized(fopFactories) {
				fopFactory = createFopFactory(userConfig);
				fopFactories.put(Thread.currentThread().getId(), fopFactory);
			}
		}
//		fopFactory.setUserConfig(userConfig);
		return fopFactory;
	}

	//Tomcat likes empty maps at shutdown...
	public static void releaseFactories() {
		if (!fopFactories.isEmpty()) {
			synchronized(fopFactories) {
				fopFactories.clear();
			}
		}
	}
	
	
	/**
	 * Create and configure FopFactory.
	 * 
	 * @param userConfig
	 * @param defaultBaseURI
	 * @return
	 * @throws FOPException
	 */
	protected static FopFactory createFopFactory(String userConfig) throws FOPException {
		// This class won't compile unless you have some version of FOP on your path. 

		/* 
		 * Unfortunately, <=1.1 requires Configuration object,
		 * and >1.1 requires InputStream.
		 * 
		 * So either we pass Configuration object into this method, 
		 * and use DefaultConfigurationSerializer to get an input stream from it,
		 * or we pass a string, and make it into a Configuration object in the case
		 * where it is required. 
		 * 
		 * I've submitted a patch to FOP allowing configuration
		 * using a Configuration object.  Let's see whether it is applied.
		 * 
		 * In the absence of that, passing a String (or an InputStream) seems better going forward.
		 * So that's what the code accepts for now.
		 */
		
		InputStream is=null;
		try {
			is = IOUtils.toInputStream(userConfig, "UTF-8");
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		FopFactory fopFactory = null;
		
		// If FopConfParser is on path, it is post 1.1
		try {
			Class fopConfParserClass = Class.forName("org.apache.fop.apps.FopConfParser");
			
			URI defaultBaseURI = new URI("http://dummy.domain");
				// Default base URI must not be null, but we don't need it.
				// at org.apache.fop.apps.EnvironmentalProfileFactory$Profile (EnvironmentalProfileFactory.java:92)

			Object o = fopConfParserClass.getConstructor(InputStream.class, URI.class).newInstance(is, defaultBaseURI);
			
			Method method = fopConfParserClass.getDeclaredMethod("getFopFactoryBuilder", new Class[0] );
			Object fopFactoryBuilder = method.invoke(o);
			
			Class fopFactoryBuilderClass = Class.forName("org.apache.fop.apps.FopFactoryBuilder");
			method = fopFactoryBuilderClass.getDeclaredMethod("build", new Class[0] );
			
			fopFactory = (FopFactory)method.invoke(fopFactoryBuilder);
			
		} catch (Exception e) {
			log.warn("Can't set up FOP svn.");
			e.printStackTrace();
			// eg java.lang.ClassNotFoundException: org.apache.fop.apps.FopConfParser
			
			// legacy FOP 1.0 or 1.1 config.
			try {
				Method method;
				Class[] params = new Class[1];

				// FopFactory fopFactory = FopFactory.newInstance();
				method = FopFactory.class.getDeclaredMethod("newInstance", new Class[0] );
				fopFactory = (FopFactory)method.invoke(null);
				
				// fopFactory.setUserConfig(userConfig);
				params[0] = Configuration.class;				
				method = FopFactory.class.getDeclaredMethod("setUserConfig", params );
				
				// There isn't a method which takes it as a string :-(
				DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
				
				method.invoke(fopFactory, cfgBuilder.build(is) );
				
			} catch (Exception e1) {
				e1.printStackTrace();
				
				// java.lang.IllegalAccessException: Class org.docx4j.fonts.fop.util.FopFactoryUtil 
				// can not access a member of class org.apache.fop.apps.FopFactory 
				// with modifiers "protected"
				
			} 
		}
		return fopFactory;
	}

		
	
}
