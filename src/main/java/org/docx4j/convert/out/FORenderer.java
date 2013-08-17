package org.docx4j.convert.out;

import java.io.OutputStream;
import java.util.List;

import org.docx4j.openpackaging.exceptions.Docx4JException;

/** The pdf conversion process needs a fo renderer to convert the 
 *  fo document into the requested format. The fo renderer is encapsulated in 
 *  this interface.<br> 
 *  
 *  The conversion process might need a two pass conversion, if this is the case, 
 *  then the body of some elements might contain a placeholder that needs to be 
 *  replaced with the page count in the document or the page count in the section. 
 *  A placeholder for the page count of the document is defined as <br>
 *  <code>PLACEHOLDER_PREFIX + getDocumentPageCountID() + PLACEHOLDER_SUFFIX</code><br>
 *  A placeholder for the page count of a section is defined as <br>
 *  <code>PLACEHOLDER_PREFIX + getSectionPageCountID() + PLACEHOLDER_SUFFIX</code><br>
 *  The values need to be calculated by the renderer and the placeholder replaced. The format 
 *  of the numbers is defined for the page count of the document by getDocumentPageCountFoFormat() and
 *  the one for the section by getSectionPageCountFoFormat(); 
 *  
 */
public interface FORenderer {
	public static final String PLACEHOLDER_PREFIX = "${";
	public static final String PLACEHOLDER_SUFFIX = "}";
	
	/** The definition for the IDs and formats for the page count of one Section.
	 * 
	 */
	public interface SectionPageInformation {
		public String getDocumentPageCountID();
		public String getDocumentPageCountFoFormat();
		public String getSectionPageCountID();
		public String getSectionPageCountFoFormat();
	}
	
	/** Render the foDocument to the requested format, if needed do a 2 pass conversion.
	 * 
	 * @param foDocument the fo document to be rendered. 
	 * @param settings the conversion settings passed to the conversion process
	 * @param twoPass a two pass rendering might be needed, i.e. the body of some tags contain placeholder 
	 *        How the renderer calculates the corresponding values and replaces them is its responsability 
	 * @param pageNumberInformation This list contains an entry for each section in the document. It defines
	 *        the IDs of the corresponding placeholders and the number format (as a fo format).<br>
	 *        This List is only available if twoPass = true.  
	 * @param outputStream The output stream on which the output should be rendered
	 * @throws Docx4JException 
	 */
	public void render(String foDocument, FOSettings settings, boolean twoPass, List<SectionPageInformation> pageNumberInformation, OutputStream outputStream) throws Docx4JException;

}
