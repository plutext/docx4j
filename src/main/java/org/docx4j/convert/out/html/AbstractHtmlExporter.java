package org.docx4j.convert.out.html;

import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.convert.out.Output;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/** The AbstractHtmlExporter is the superclass for the HTML docx4j conversions.<br>
 *  This class has been depreciated and replaced with the <code>Docx4j.getExporter()</code> functions.
 * 
 * @deprecated
 */
public abstract class AbstractHtmlExporter implements Output {

	//This stub is kept to be compatible with previous code
	
	public abstract void html(WordprocessingMLPackage wmlPackage,
			javax.xml.transform.Result result, HTMLSettings htmlSettings)
			throws Exception;		
	
  /**
   * @deprecated
   */  
	public static class HtmlSettings extends HTMLSettings {
		
		
	}
	
}
