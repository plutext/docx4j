/**
 *  Copyright 2026, Plutext Pty Ltd.
 *   
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

 **/
package org.docx4j.model.fields.merge;

import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * If we're converting MERGEFIELD to FORMTEXT, it is
 * desirable to make the w:fldChar/w:ffData/w:name
 * unique within the docx (though Word 2010 can still open the docx if they
 * aren't), and to remove spaces 
 * 
 * @author jharrop
 * @since 3.0.0
 */
class FormTextFieldNames {
	
	// MS-OE376 says Word only allows strings of length at most 40 for the name attribute.
	
	// Also, by experiment (in Word 2010), only alphanumberic characters and "_" are allowed 
	// (no spaces, other symbols/punctuation etc),
	// and it can't start with a numeral.  Comparison is case insensitive.
	// Some UTF-8 characters are allowed eg ϣ
	Pattern pattern = java.util.regex.Pattern.compile("[^a-zA-Z0-9]");
	
	// http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/bookmarkStart.html
	// says:
	//    If multiple bookmarks in a document share the same name, then the first bookmark 
	//    (defined by the location of the bookmarkStart element in document order) shall 
	//    be maintained, and all subsequent bookmarks should be ignored.
	
	// By experiment (inserting a cross ref) it looks like Word 2010 actually ignores the first!
	// Not that that matters here, since we make the field names unique
	
	private FormTextFieldNameSet names = new FormTextFieldNameSet(); 
	
	public String generateName(String input) {
		
		// Strip characters
		String unpunctuated = pattern.matcher(input).replaceAll("_");			
		
		// Ensure it starts with a letter
		char c = unpunctuated.charAt(0);
		if('0'<=c && c<='9') {
			unpunctuated = "z" + unpunctuated;
		} else if(c=='_') {
			unpunctuated = "z" + unpunctuated;
		}
		
		if (names.contains(unpunctuated)) {
			// Then make unique
			int i = 2;
			String newName = null;
	    	do {
	    		newName = unpunctuated + i;
	    		i++;
	    		
	    	} while (names.contains(newName));
	    	unpunctuated = newName;
		}
		
		// Add to FormTextFieldNameSet
    	names.add(unpunctuated);
		
    	return unpunctuated;
	}
	
	/**
	 * Case insensitive key
	 * (matching http://www.w3.org/TR/css3-fonts/#font-family-casing
	 */
	private static class FormTextFieldNameSet extends HashSet<String> {

	    @Override
	    public boolean add(String key) {
	       MailMerger.log.debug("Added '" + key.toLowerCase() + "'");	
	       return super.add(key.toLowerCase());
	    }

	    // not @Override because that would require the key parameter to be of type Object
	    public boolean contains(String key) {
	       return super.contains(key.toLowerCase());
	    }
	}	
	
}