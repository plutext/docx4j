/*
 *  Copyright 2009, Plutext Pty Ltd.
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
package org.docx4j.convert.out.common.writer;

public class SymbolUtils  {
	
	public final static int UNICODE_PRIV_USE_START = 0xF000;
	public final static int UNICODE_PRIV_USE_END = 0xFFFF;

	public static int short2Int(byte[] val) {
		
		assert(val.length<=2);
		
		if (val.length==1) {
			return (val[0] & 0xFF);
		} else {
			return (((val[0] & 0xFF) << 8) | ((val[1] & 0xFF) << 0));
		}
	}
	
	public static byte[] hexStringToByteArray(String s) {
		// From http://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
  
}
