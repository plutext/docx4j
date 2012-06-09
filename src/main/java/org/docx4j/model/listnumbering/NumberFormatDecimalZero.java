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

/**
 * This format is 01, 02, 03 etc.
 * Not sure what Word does after 99?
 *
 */
public class NumberFormatDecimalZero extends NumberFormat {
	
	protected static Logger log = Logger.getLogger(NumberFormatDecimalZero.class);
	
	public String format( int in ) {

		if (in<10) {
			return "0" + in;
		} else {
			return "" + in;
		}
	}
	
}
