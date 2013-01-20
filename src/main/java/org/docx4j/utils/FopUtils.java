package org.docx4j.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.docx4j.XmlUtils;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.fonts.fop.fonts.FontTriplet;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.apps.MimeConstants;
import org.apache.log4j.Logger;

public class FopUtils {
	protected static Logger log = Logger.getLogger(FopUtils.class);
	//used as a ThreadLocal
	protected static Map<Long, FopFactory> fopFactories = new TreeMap<Long, FopFactory>();

	public static Configuration createDefaultConfiguration(Mapper fontMapper, Map<String, String> fontsInUse) throws Docx4JException {
	DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
	StringBuilder buffer = new StringBuilder(10240);
	Configuration ret = null;
	byte[] bufferBytes = null;
		buffer.append("<fop version=\"1.0\"><strict-configuration>true</strict-configuration>");
		buffer.append("<renderers><renderer mime=\"application/pdf\">");
		buffer.append("<fonts>");
		declareFonts(fontMapper, fontsInUse, buffer);
		buffer.append("</fonts></renderer></renderers></fop>");

		if (log.isDebugEnabled()) {
			log.debug("\nUsing fop config:\n " + buffer.toString() + "\n");
		}

		// See FOP's PrintRendererConfigurator
		// String myConfig = "<fop
		// version=\"1.0\"><strict-configuration>true</strict-configuration>"
		// +
		// "<renderers><renderer mime=\"application/pdf\">" +
		// "<fonts><directory
		// recursive=\"true\">C:\\WINDOWS\\Fonts</directory>" +
		// "<auto-detect/>" +
		// "</fonts></renderer></renderers></fop>";

		try {
			bufferBytes = buffer.toString().getBytes("UTF-8");
		}
		catch (UnsupportedEncodingException uee) {
			bufferBytes = buffer.toString().getBytes();
		}
		
		try {
			ret = cfgBuilder.build(new ByteArrayInputStream(bufferBytes));
		}
		catch (SAXException se) {
			throw new Docx4JException("Exception creating default fop configuration: " + se.getMessage(), se);
		} catch (IOException ioe) {
			throw new Docx4JException("Exception creating default fop configuration: " + ioe.getMessage(), ioe);
		} catch (ConfigurationException ce) {
			throw new Docx4JException("Exception creating default fop configuration: " + ce.getMessage(), ce);
		}
		return ret;
	}

	/**
	 * Create a FOP font configuration for each font used in the
	 * document.
	 * 
	 * @return
	 */
	protected static void declareFonts(Mapper fontMapper, Map<String, String> fontsInUse, StringBuilder result) {
		Iterator fontMappingsIterator = fontsInUse.entrySet().iterator();
		while (fontMappingsIterator.hasNext()) {
		    Map.Entry pairs = (Map.Entry)fontMappingsIterator.next();
		    if(pairs.getKey()==null) {
		    	log.info("Skipped null key");
//		    	pairs = (Map.Entry)fontMappingsIterator.next();
		    	continue;
		    }
		    
		    String fontName = (String)pairs.getKey();		    
		    
		    PhysicalFont pf = fontMapper.getFontMappings().get(fontName);
		    
		    if (pf==null) {
		    	log.error("Document font " + fontName + " is not mapped to a physical font!");
		    	continue;
		    }
		    
		    String subFontAtt = "";
		    if (pf.getEmbedFontInfo().getSubFontName()!=null)
		    	subFontAtt= " sub-font=\"" + pf.getEmbedFontInfo().getSubFontName() + "\"";
		    
		    result.append("<font embed-url=\"" +pf.getEmbeddedFile() + "\""+ subFontAtt +">" );
		    	// now add the first font triplet
			    FontTriplet fontTriplet = (FontTriplet)pf.getEmbedFontInfo().getFontTriplets().get(0);
			    addFontTriplet(result, fontTriplet);
		    result.append("</font>" );
		    
		    // bold, italic etc
		    PhysicalFont pfVariation = PhysicalFonts.getBoldForm(pf);
		    if (pfVariation!=null) {
			    result.append("<font embed-url=\"" +pfVariation.getEmbeddedFile() + "\""+ subFontAtt +">" );
		    	addFontTriplet(result, pf.getName(), "normal", "bold");
			    result.append("</font>" );
		    }
		    pfVariation = PhysicalFonts.getBoldItalicForm(pf);
		    if (pfVariation!=null) {
			    result.append("<font embed-url=\"" +pfVariation.getEmbeddedFile() + "\""+ subFontAtt +">" );
		    	addFontTriplet(result, pf.getName(), "italic", "bold");
			    result.append("</font>" );
		    }
		    pfVariation = PhysicalFonts.getItalicForm(pf);
		    if (pfVariation!=null) {
			    result.append("<font embed-url=\"" +pfVariation.getEmbeddedFile() + "\""+ subFontAtt +">" );
		    	addFontTriplet(result, pf.getName(), "italic", "normal");
			    result.append("</font>" );
		    }
			    
		}
	}
		
	protected static void addFontTriplet(StringBuilder result, FontTriplet fontTriplet) {
		addFontTriplet(result, fontTriplet.getName(), 
							   fontTriplet.getStyle(), 
							   weightToCSS2FontWeight(fontTriplet.getWeight()));
	}
	
	protected static void addFontTriplet(StringBuilder result, String familyName, String style, String weight) {
	    result.append("<font-triplet name=\""); 
	    result.append(familyName);
	    result.append('"');
	    result.append(" style=\"");
	    result.append(style);
	    result.append('"');
	    result.append(" weight=\"");
	    result.append(weight); 
	    result.append("\"/>");
	}
	
	protected static String weightToCSS2FontWeight(int i) {
		return (i >= 700 ? "bold" : "normal");
	}
	
	protected static FopFactory getFopFactory(Configuration userConfig) throws FOPException {
	FopFactory fopFactory = fopFactories.get(Thread.currentThread().getId());
		if (fopFactory == null) {
			synchronized(fopFactories) {
				fopFactory = createFopFactory();
				fopFactories.put(Thread.currentThread().getId(), fopFactory);
			}
		}
		fopFactory.setUserConfig(userConfig);
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
	
	protected static FopFactory createFopFactory() throws FOPException {
		//A reuse of the FopFactory isn't possible as long as the userConfiguration
		//contains a per document font mapping
		
		
		// See http://xmlgraphics.apache.org/fop/0.95/embedding.html
		// (reuse if you plan to render multiple documents!)
		FopFactory fopFactory = FopFactory.newInstance();

//		FopFactory fopFactory = null;
//		// in FOP r1356646 (after FOP 1.1),
//		// FopFactory.newInstance() was replaced with FopFactory.newInstance(URI) 
//		Method[] methods = FopFactory.class.getDeclaredMethods();		
//		Method method;
//		try {
//			method = FopFactory.class.getDeclaredMethod("newInstance", new Class[0] );
//			if (method==null) {
//				Class[] params = new Class[1];
//				params[0] = URI.class;
//				method = FopFactory.class.getDeclaredMethod("newInstance", params );
//				fopFactory = (FopFactory)method.invoke(null, new URI("http://") );
//				// Also requires xmlgraphics-commons to be built from nightly 
//				// for org/apache/xmlgraphics/image/loader/impl/AbstractImageSessionContext$FallbackResolver
//				// which was introduced in r1391005 
//			} else {
//				fopFactory = (FopFactory)method.invoke(null);
//			}
//		} catch (Exception e1) {
//			log.error(e1);
//		} 
		
		return fopFactory;
	}
	
	protected static Fop createFop(Configuration userConfiguration, String outputFormat, OutputStream outputStream) throws FOPException {
	FopFactory fopFactory = getFopFactory(userConfiguration);
		return fopFactory.newFop(outputFormat != null ? outputFormat : MimeConstants.MIME_PDF, outputStream);
	}
	
	protected static Result createResult(Configuration userConfiguration, String outputFormat, OutputStream outputStream) throws FOPException {
	Fop fop = createFop(userConfiguration, outputFormat, outputStream);
		return new SAXResult(fop.getDefaultHandler());
	}
	
	public static void render(Configuration userConfiguration, String outputFormat, Document document, Templates xslt, Map parameters, OutputStream outputStream) throws Docx4JException {
	Result result = null;
		try {
			result = createResult(userConfiguration, outputFormat, outputStream);
		} catch (FOPException e) {
			throw new Docx4JException("Exception setting up result for fo transformation: " + e.getMessage(), e);
		}
		XmlUtils.transform(document, xslt, parameters, result);		
	}
	
	public static void render(Configuration userConfiguration, String outputFormat, String foDocument, Map<String, Object> parameters, OutputStream outputStream) throws Docx4JException {
		render(userConfiguration, outputFormat, new StreamSource(new StringReader(foDocument)), parameters, outputStream);
	}
	
	public static void render(Configuration userConfiguration, String outputFormat, Document foDocument, Map<String, Object> parameters, OutputStream outputStream) throws Docx4JException {
		render(userConfiguration, outputFormat, new DOMSource(foDocument), parameters, outputStream);
	}
	
	public static void render(Configuration userConfiguration, String outputFormat, Source foDocumentSrc, Map<String, Object> parameters, OutputStream outputStream) throws Docx4JException {
	Result result = null;
		try {
			result = createResult(userConfiguration, outputFormat, outputStream);
		} catch (FOPException e) {
			throw new Docx4JException("Exception setting up result for fo transformation: " + e.getMessage(), e);
		}

		Transformer transformer;
		try {
			transformer = XmlUtils.getTransformerFactory().newTransformer();
			setupParameters(transformer, parameters);
			transformer.transform(foDocumentSrc, result);
		} catch (TransformerConfigurationException e) {
			throw new Docx4JException("Exception setting up transformer: " + e.getMessage(), e);
		} catch (TransformerException e) {
			throw new Docx4JException("Exception executing transformer: " + e.getMessage(), e);
		}
	}
	
	public static FormattingResults calcResults(Configuration userConfiguration, String outputFormat, Document document, Templates xslt, Map parameters) throws Docx4JException {
	Fop fop = null;
	Result result = null;
		try {
			fop = createFop(userConfiguration, outputFormat, new NullOutputStream());
			result = new SAXResult(fop.getDefaultHandler());
		} catch (FOPException e) {
			throw new Docx4JException("Exception setting up result for fo transformation: " + e.getMessage(), e);
		}
		XmlUtils.transform(document, xslt, parameters, result);
		return fop.getResults();
	}
	
	
	public static FormattingResults calcResults(Configuration userConfiguration, String outputFormat, String foDocument, Map<String, Object> parameters) throws Docx4JException {
		return calcResults(userConfiguration, outputFormat, new StreamSource(new StringReader(foDocument)), parameters);
	}
	
	public static FormattingResults calcResults(Configuration userConfiguration, String outputFormat, Document foDocument, Map<String, Object> parameters) throws Docx4JException {
		return calcResults(userConfiguration, outputFormat, new DOMSource(foDocument), parameters);
	}
	
	public static FormattingResults calcResults(Configuration userConfiguration, String outputFormat, Source foDocumentSrc, Map<String, Object> parameters) throws Docx4JException {
	Fop fop = null;
	Result result = null;
		try {
			fop = createFop(userConfiguration, outputFormat, new NullOutputStream());
			result = new SAXResult(fop.getDefaultHandler());
		} catch (FOPException e) {
			throw new Docx4JException("Exception setting up result for fo transformation: " + e.getMessage(), e);
		}

		Transformer transformer;
		try {
			transformer = XmlUtils.getTransformerFactory().newTransformer();
			setupParameters(transformer, parameters);
			transformer.transform(foDocumentSrc, result);
		} catch (TransformerConfigurationException e) {
			throw new Docx4JException("Exception setting up transformer: " + e.getMessage(), e);
		} catch (TransformerException e) {
			throw new Docx4JException("Exception executing transformer: " + e.getMessage(), e);
		}
		return fop.getResults();
	}
	
	protected static void setupParameters(Transformer transformer, Map<String, Object> parameters) {
		if ((parameters != null) && (!parameters.isEmpty())) {
			for (Map.Entry<String, Object> item : parameters.entrySet()) {
				if (item.getKey() != null) {
					transformer.setParameter(item.getKey(), item.getValue());
				}
			}
		}
	}
}
