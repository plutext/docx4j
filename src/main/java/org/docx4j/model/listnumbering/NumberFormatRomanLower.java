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

import org.docx4j.model.listnumbering.NumberFormatRomanAbstract.RomanValue;

//File   : gui/componenents/calculators/Roman.java
//Description: A static method for converting binary integers to Roman numbers.
//Illustrates: Static inner value class, StringBuffer, throw exceptions.
//Author : Fred Swartz - 2006-12-29 - Placed in public domain

//JH comment - under the MIT license, according to the footer at
// http://leepoint.net/notes-java/examples/components/romanNumerals/romanNumeral.html

/////////////////////////////////////////////////////////////////// class Roman
public class NumberFormatRomanLower extends NumberFormatRomanAbstract {
	
	
	NumberFormatRomanLower() {
		
		ROMAN_VALUE_TABLE = ROMAN_VALUE_TABLE_LOWER;
	}
	
	 //================================================================ constant
	 // This could be alternatively be done with parallel arrays.
	 // Another alternative would be Pair<Integer, String>
 static RomanValue[] ROMAN_VALUE_TABLE_LOWER = {
     new RomanValue(1000, "m"),
     new RomanValue( 900, "cm"),
     new RomanValue( 500, "d"),
     new RomanValue( 400, "cd"),
     new RomanValue( 100, "c"),
     new RomanValue(  90, "xc"),
     new RomanValue(  50, "l"),
     new RomanValue(  40, "xl"),
     new RomanValue(  10, "x"),
     new RomanValue(   9, "ix"),
     new RomanValue(   5, "v"),
     new RomanValue(   4, "iv"),
     new RomanValue(   1, "i")
 };
 
}