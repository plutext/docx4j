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

//File   : gui/componenents/calculators/Roman.java
//Description: A static method for converting binary integers to Roman numbers.
//Illustrates: Static inner value class, StringBuffer, throw exceptions.
//Author : Fred Swartz - 2006-12-29 - Placed in public domain

//JH comment - under the MIT license, according to the footer at
// http://leepoint.net/notes-java/examples/components/romanNumerals/romanNumeral.html

/////////////////////////////////////////////////////////////////// class Roman
public abstract class NumberFormatRomanAbstract extends NumberFormat {
 //================================================================ constant
 // This could be alternatively be done with parallel arrays.
 // Another alternative would be Pair<Integer, String>
  RomanValue[] ROMAN_VALUE_TABLE; 
 
 //============================================================== int2roman
 public String format(int n) {
     if (n >= 4000  || n < 1) {
         throw new NumberFormatException("Numbers must be in range 1-3999");
     }
     StringBuffer result = new StringBuffer(10);
     
     //... Start with largest value, and work toward smallest.
     for (RomanValue equiv : ROMAN_VALUE_TABLE) {
         //... Remove as many of this value as possible (maybe none).
         while (n >= equiv.intVal) {
             n -= equiv.intVal;            // Subtract value.
             result.append(equiv.romVal);  // Add roman equivalent.
         }
     }
     return result.toString();
 }
 
 ///////////////////////////////////////////////////////// inner value class
 protected static class RomanValue {
     //============================================================== fields
     //... No need to make this fields private because they are
     //    used only in this private value class.
     int    intVal;     // Integer value.
     String romVal;     // Equivalent roman numeral.
     
     //========================================================= constructor
     RomanValue(int dec, String rom) {
         this.intVal = dec;
         this.romVal = rom;
     }
 }
}