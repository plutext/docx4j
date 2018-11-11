package org.docx4j.model.datastorage;

import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.docx4j.XmlUtils;
import org.docx4j.utils.ResourceUtils;

public class XsltProviderImpl implements XsltProvider {


	@Override
	public Templates getFinisherXslt(String filename) throws TransformerConfigurationException  {
		
		Source xsltSource;
		try {
			xsltSource = new StreamSource(
						ResourceUtils.getResourceViaProperty(
								"docx4j.model.datastorage.XsltFinisher.xslt",
								"XsltFinisherCustom.xslt"));
		} catch (IOException e) {
			throw new TransformerConfigurationException(e.getMessage(), e);
		}
		return XmlUtils.getTransformerTemplate(xsltSource);
		
	}

}
