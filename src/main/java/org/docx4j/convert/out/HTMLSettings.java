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
package org.docx4j.convert.out;

import org.docx4j.fonts.Mapper;

public class HTMLSettings extends AbstractConversionSettings {
	
	public static final String IMAGE_TARGET_URI = "imageTargetUri";
	public static final String CONDITIONAL_COMMENTS = "conditionalComments";
	public static final String FONT_FAMILY_STACK = "fontFamilyStack";
	public static final String STYLE_ELEMENT_HANDLER = "styleElementHandler";
	public static final String SCRIPT_ELEMENT_HANDLER = "scriptElementHandler";
	public static final String USER_CSS = "userCSS";
	public static final String USER_SCRIPT = "userScript";
	public static final String USER_BODY_TOP = "userBodyTop";
	public static final String USER_BODY_TAIL = "userBodyTail";
	
	public HTMLSettings() {
		settings.put(CONDITIONAL_COMMENTS, Boolean.FALSE);
		settings.put(FONT_FAMILY_STACK,     Boolean.FALSE);
		
		settings.put(USER_CSS, "");
		settings.put(USER_SCRIPT, "");
		settings.put(USER_BODY_TOP, "<!-- userBodyTop goes here -->");
		settings.put(USER_BODY_TAIL, "<!-- userBodyTail goes here -->");

		addFeatures(ConversionFeatures.DEFAULT_HTML_FEATURES);
	}
	
	public void setConditionalComments(Boolean conditionalComments) {
		settings.put(CONDITIONAL_COMMENTS, conditionalComments);
	}
	
	public void setFontFamilyStack(boolean val) {
		settings.put(FONT_FAMILY_STACK, new Boolean(val));
	}

	public void setFontMapper(Mapper fontMapper) {
		settings.put("fontMapper", fontMapper);
	}
	public Mapper getFontMapper() {
		return (Mapper)settings.get("fontMapper");
	}
	
	/**
	 * @see get/setStyleElementHandler
	 */
	@Deprecated
	public String getUserCSS() {
		return (String)settings.get(USER_CSS);
	}
			
	/**
	 * @see get/setStyleElementHandler
	 */
	@Deprecated
	public void setUserCSS(String val) {
		settings.put(USER_CSS, val);
	}

	/**
	 * @see get/setScriptElementHandler
	 */
	@Deprecated
	public String getUserScript() {
		return (String)settings.get(USER_SCRIPT);
	}

	/**
	 * @see get/setScriptElementHandler
	 */
	@Deprecated
	public void setUserScript(String val) {
		settings.put(USER_SCRIPT, val);
	}

	public String getUserBodyTop() {
		return (String)settings.get(USER_BODY_TOP);
	}

	public void setUserBodyTop(String val) {
		settings.put(USER_BODY_TOP, val);
	}

	public String getUserBodyTail() {
		return (String)settings.get(USER_BODY_TAIL);
	}		

	public void setUserBodyTail(String val) {
		settings.put(USER_BODY_TAIL, val);
	}		
	
	public void setImageTargetUri(String imageTargetUri) {
		settings.put(IMAGE_TARGET_URI, imageTargetUri);
	}
	
	public String getImageTargetUri() {
		return (String)settings.get(IMAGE_TARGET_URI);
	}
	
	/**
	 * How to generate the style element. The default implementation is inline.
	 */
	public void setStyleElementHandler(ConversionHTMLStyleElementHandler styleElementHandler) {
		settings.put(STYLE_ELEMENT_HANDLER, styleElementHandler);
	}
	
	public ConversionHTMLStyleElementHandler getStyleElementHandler() {
		return (ConversionHTMLStyleElementHandler)settings.get(STYLE_ELEMENT_HANDLER);
	}
	
	/**
	 * How to generate the script element. The default implementation is inline.
	 */
	public void setScriptElementHandler(ConversionHTMLScriptElementHandler scriptElementHandler) {
		settings.put(SCRIPT_ELEMENT_HANDLER, scriptElementHandler);
	}
	
	public ConversionHTMLScriptElementHandler getScriptElementHandler() {
		return (ConversionHTMLScriptElementHandler)settings.get(SCRIPT_ELEMENT_HANDLER);
	}
}