/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.fonts;

public class FontUtils {
	
	public final static java.lang.CharSequence target = (new String("%20")).subSequence(0, 3);
	public final static java.lang.CharSequence replacement = (new String(" ")).subSequence(0, 1);
	
	public static String pathFromURL(String path) {
		
		if (path.startsWith("file:/")) {
			if (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") > -1) {
				path = path.substring(6);
			} else {
				path = path.substring(5);
			}
		}
		
		// Convert %20 to spaces
		if (path.indexOf("%20")>-1) {
					               
               path = path.toString().replace(target, replacement);					
		}
		
		return path;
		
	}

}
