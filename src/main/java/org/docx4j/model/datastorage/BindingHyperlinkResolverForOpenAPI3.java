package org.docx4j.model.datastorage;

import javax.xml.bind.JAXBException;

import org.docx4j.XmlUtils;
import org.docx4j.wml.P.Hyperlink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindingHyperlinkResolverForOpenAPI3 extends BindingHyperlinkResolver {

	private static Logger log = LoggerFactory.getLogger(BindingHyperlinkResolver.class);	
	
	private static final String COMPONENT_REF = "#/components"; 
	
	/**
	 * Return the index of the position you wish to treat as a hyperlink.
	 * The characters from there to the first whitespace following will be treated as 
	 * the URL
	 * 
	 * @param text
	 * @return
	 */
	public int getIndexOfURL(String text) {
		
		/* For OpenAPI, treat a string starting with:
		 * 
		 *    #/components
		 *    
		 * as a hyperlink to a bookmark.
		 */

		if (text.startsWith(COMPONENT_REF)) return 0;
		return super.getIndexOfURL(text);
		
	}
	
	/**
	 * @param bookmarkName
	 * @param url
	 * @return
	 * @throws JAXBException
	 */
	public Hyperlink generateHyperlink(String bookmarkName, String url) throws JAXBException {
		
		if (url.startsWith(COMPONENT_REF)) {

			String hpl = "<w:hyperlink w:anchor=\"" + bookmarkName + "\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" >" +
			        "<w:r>" +
			        "<w:rPr>" +
			        "<w:rStyle w:val=\"" + getHyperlinkStyleId() + "\" />" +  
			        "</w:rPr>" +
			        "<w:t>" + url + "</w:t>" +
			        "</w:r>" +
			        "</w:hyperlink>";

			return (Hyperlink)XmlUtils.unmarshalString(hpl);		
			
		} else {
			return super.generateHyperlink(bookmarkName /* actually, relId here */, url);
		}
	}
	
}
