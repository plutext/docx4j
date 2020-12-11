/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
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

 */
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
