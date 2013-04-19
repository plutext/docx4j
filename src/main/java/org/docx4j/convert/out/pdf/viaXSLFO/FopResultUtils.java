package org.docx4j.convert.out.pdf.viaXSLFO;

import java.io.OutputStream;
import java.io.StringReader;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FormattingResults;
import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.w3c.dom.Document;

public class FopResultUtils {
	
	protected static Logger log = Logger.getLogger(FopResultUtils.class);

	
	protected static Result createResult(String userConfiguration, String outputFormat, OutputStream outputStream) throws FOPException {
		
		Fop fop = FopFactoryUtil.createFop(userConfiguration, outputFormat, outputStream);
		return new SAXResult(fop.getDefaultHandler());
	}

	public static void render(String userConfiguration, String outputFormat, Document document, Templates xslt, Map parameters, OutputStream outputStream) throws Docx4JException {
		
		Result result = null;
		try {
			result = createResult(userConfiguration, outputFormat, outputStream);
		} catch (FOPException e) {
			throw new Docx4JException("Exception setting up result for fo transformation: " + e.getMessage(), e);
		}
		XmlUtils.transform(document, xslt, parameters, result);		
	}
	
	public static void render(String userConfiguration, String outputFormat, String foDocument, Map<String, Object> parameters, OutputStream outputStream) throws Docx4JException {
		render(userConfiguration, outputFormat, new StreamSource(new StringReader(foDocument)), parameters, outputStream);
	}
	
	public static void render(String userConfiguration, String outputFormat, Document foDocument, Map<String, Object> parameters, OutputStream outputStream) throws Docx4JException {
		render(userConfiguration, outputFormat, new DOMSource(foDocument), parameters, outputStream);
	}
	
	public static void render(String userConfiguration, String outputFormat, Source foDocumentSrc, Map<String, Object> parameters, OutputStream outputStream) throws Docx4JException {
		
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
	
	public static FormattingResults calcResults(String userConfiguration, String outputFormat, Document document, Templates xslt, Map parameters) throws Docx4JException {
		
		Fop fop = null;
		Result result = null;
		try {
			fop = FopFactoryUtil.createFop(userConfiguration, outputFormat, new NullOutputStream());
			result = new SAXResult(fop.getDefaultHandler());
		} catch (FOPException e) {
			throw new Docx4JException("Exception setting up result for fo transformation: " + e.getMessage(), e);
		}
		XmlUtils.transform(document, xslt, parameters, result);
		return fop.getResults();
	}
	
	
	public static FormattingResults calcResults(String userConfiguration, String outputFormat, String foDocument, Map<String, Object> parameters) throws Docx4JException {
		return calcResults(userConfiguration, outputFormat, new StreamSource(new StringReader(foDocument)), parameters);
	}
	
	public static FormattingResults calcResults(String userConfiguration, String outputFormat, Document foDocument, Map<String, Object> parameters) throws Docx4JException {
		return calcResults(userConfiguration, outputFormat, new DOMSource(foDocument), parameters);
	}
	
	public static FormattingResults calcResults(String userConfiguration, String outputFormat, Source foDocumentSrc, Map<String, Object> parameters) throws Docx4JException {
		
		Fop fop = null;
		Result result = null;
		try {
			fop = FopFactoryUtil.createFop(userConfiguration, outputFormat, new NullOutputStream());
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
