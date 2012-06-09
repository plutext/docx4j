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
 
package org.docx4j.model.listnumbering;

import org.apache.log4j.Logger;

public class NumberFormatLowerLetter extends NumberFormat {
	
	protected static Logger log = Logger.getLogger(NumberFormatLowerLetter.class);
	
	public String format( int in ) {
		
		String str = Integer.toString(in);
		StringBuilder out = new StringBuilder();
		
		for (int i = 0; i < str.length(); i++) {
			int dig = Character.digit(str.charAt(i), 36);
			char cdig = Character.forDigit(dig + 9, 36);
			log.debug(str.charAt(i) + " --> " + cdig);
			out.append(cdig);
		}		
		return out.toString();		
	}
	
	  public static void main (String [] args)
	   {
		   
		  NumberFormatLowerLetter format = new NumberFormatLowerLetter();
		  
		  format.format(1);
		  format.format(2);
		  log.debug(format.format(3));
		  
		  
		  
	   }

}
