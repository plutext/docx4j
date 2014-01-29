/*
 *  Copyright 2012, Plutext Pty Ltd.
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

 */
package org.docx4j.model.listnumbering;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NumberFormatLowerLetterTest
{
	@Test
	public void testLowerLetterNumbering()
	{
		NumberFormatLowerLetter format = new NumberFormatLowerLetter();
		
		assertEquals(format.format(1),  "a");
		assertEquals(format.format(2),  "b");
		assertEquals(format.format(10), "j");
		assertEquals(format.format(11), "k");
		assertEquals(format.format(26), "z");
	}
}
