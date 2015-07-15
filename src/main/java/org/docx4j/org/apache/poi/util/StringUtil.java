/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */
 
 /* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.docx4j.org.apache.poi.util;

import java.nio.charset.Charset;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

//import org.docx4j.org.apache.poi.hssf.record.RecordInputStream;
/**
 *  Title: String Utility Description: Collection of string handling utilities<p/>
 *
 * Note - none of the methods in this class deals with {@link org.docx4j.org.apache.poi.hssf.record.ContinueRecord}s.
 * For such functionality, consider using {@link RecordInputStream}
 * 
 *
 *@author     Andrew C. Oliver
 *@author     Sergei Kozello (sergeikozello at mail.ru)
 *@author     Toshiaki Kamoshida (kamoshida.toshiaki at future dot co dot jp)
 */
public class StringUtil {
	private static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
	private static final Charset UTF16LE = Charset.forName("UTF-16LE");
    private static Map<Integer,Integer> msCodepointToUnicode;

	private StringUtil() {
		// no instances of this class
	}

	/**
	 *  Given a byte array of 16-bit unicode characters in Little Endian
	 *  format (most important byte last), return a Java String representation
	 *  of it.
	 *
	 * { 0x16, 0x00 } -0x16
	 *
	 * @param  string  the byte array to be converted
	 * @param  offset  the initial offset into the
	 *                 byte array. it is assumed that string[ offset ] and string[ offset +
	 *                 1 ] contain the first 16-bit unicode character
     * @param len the length of the final string
	 * @return the converted string, never <code>null</code>.
	 * @exception  ArrayIndexOutOfBoundsException  if offset is out of bounds for
	 *      the byte array (i.e., is negative or is greater than or equal to
	 *      string.length)
	 * @exception  IllegalArgumentException        if len is too large (i.e.,
	 *      there is not enough data in string to create a String of that
	 *      length)
	 */
	public static String getFromUnicodeLE(
		final byte[] string,
		final int offset,
		final int len)
		throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
		if ((offset < 0) || (offset >= string.length)) {
			throw new ArrayIndexOutOfBoundsException("Illegal offset " + offset + " (String data is of length " + string.length + ")");
		}
		if ((len < 0) || (((string.length - offset) / 2) < len)) {
			throw new IllegalArgumentException("Illegal length " + len);
		}

		return new String(string, offset, len * 2, UTF16LE);
	}

	/**
	 *  Given a byte array of 16-bit unicode characters in little endian
	 *  format (most important byte last), return a Java String representation
	 *  of it.
	 *
	 * { 0x16, 0x00 } -0x16
	 *
	 * @param  string  the byte array to be converted
	 * @return the converted string, never <code>null</code>
	 */
	public static String getFromUnicodeLE(byte[] string) {
        if(string.length == 0) { return ""; }
        return getFromUnicodeLE(string, 0, string.length / 2);
	}
	
	/**
	 * Convert String to 16-bit unicode characters in little endian format
	 *
	 * @param string the string
	 * @return the byte array of 16-bit unicode characters
	 */
	public static byte[] getToUnicodeLE(String string) {
	    return string.getBytes(UTF16LE);
	}

	/**
	 * Read 8 bit data (in ISO-8859-1 codepage) into a (unicode) Java
	 * String and return.
	 * (In Excel terms, read compressed 8 bit unicode as a string)
	 *
	 * @param string byte array to read
	 * @param offset offset to read byte array
	 * @param len    length to read byte array
	 * @return String generated String instance by reading byte array
	 */
	public static String getFromCompressedUnicode(
		final byte[] string,
		final int offset,
		final int len) {
		int len_to_use = Math.min(len, string.length - offset);
		return new String(string, offset, len_to_use, ISO_8859_1);
	}
	
	public static String readCompressedUnicode(LittleEndianInput in, int nChars) {
		byte[] buf = new byte[nChars];
		in.readFully(buf);
		return new String(buf, ISO_8859_1);
	}
	
	/**
	 * InputStream <tt>in</tt> is expected to contain:
	 * <ol>
	 * <li>ushort nChars</li>
	 * <li>byte is16BitFlag</li>
	 * <li>byte[]/char[] characterData</li>
	 * </ol>
	 * For this encoding, the is16BitFlag is always present even if nChars==0.
	 * 
	 * This structure is also known as a XLUnicodeString.
	 */
	public static String readUnicodeString(LittleEndianInput in) {

		int nChars = in.readUShort();
		byte flag = in.readByte();
		if ((flag & 0x01) == 0) {
			return readCompressedUnicode(in, nChars);
		}
		return readUnicodeLE(in, nChars);
	}
	/**
	 * InputStream <tt>in</tt> is expected to contain:
	 * <ol>
	 * <li>byte is16BitFlag</li>
	 * <li>byte[]/char[] characterData</li>
	 * </ol>
	 * For this encoding, the is16BitFlag is always present even if nChars==0.
	 * <br/>
	 * This method should be used when the nChars field is <em>not</em> stored
	 * as a ushort immediately before the is16BitFlag. Otherwise, {@link
	 * #readUnicodeString(LittleEndianInput)} can be used.
	 */
	public static String readUnicodeString(LittleEndianInput in, int nChars) {
		byte is16Bit = in.readByte();
		if ((is16Bit & 0x01) == 0) {
			return readCompressedUnicode(in, nChars);
		}
		return readUnicodeLE(in, nChars);
	}
	/**
	 * OutputStream <tt>out</tt> will get:
	 * <ol>
	 * <li>ushort nChars</li>
	 * <li>byte is16BitFlag</li>
	 * <li>byte[]/char[] characterData</li>
	 * </ol>
	 * For this encoding, the is16BitFlag is always present even if nChars==0.
	 */
	public static void writeUnicodeString(LittleEndianOutput out, String value) {

		int nChars = value.length();
		out.writeShort(nChars);
		boolean is16Bit = hasMultibyte(value);
		out.writeByte(is16Bit ? 0x01 : 0x00);
		if (is16Bit) {
			putUnicodeLE(value, out);
		} else {
			putCompressedUnicode(value, out);
		}
	}
	/**
	 * OutputStream <tt>out</tt> will get:
	 * <ol>
	 * <li>byte is16BitFlag</li>
	 * <li>byte[]/char[] characterData</li>
	 * </ol>
	 * For this encoding, the is16BitFlag is always present even if nChars==0.
	 * <br/>
	 * This method should be used when the nChars field is <em>not</em> stored
	 * as a ushort immediately before the is16BitFlag. Otherwise, {@link
	 * #writeUnicodeString(LittleEndianOutput, String)} can be used.
	 */
	public static void writeUnicodeStringFlagAndData(LittleEndianOutput out, String value) {
		boolean is16Bit = hasMultibyte(value);
		out.writeByte(is16Bit ? 0x01 : 0x00);
		if (is16Bit) {
			putUnicodeLE(value, out);
		} else {
			putCompressedUnicode(value, out);
		}
	}

	/**
	 * @return the number of bytes that would be written by {@link #writeUnicodeString(LittleEndianOutput, String)}
	 */
	public static int getEncodedSize(String value) {
		int result = 2 + 1;
		result += value.length() * (StringUtil.hasMultibyte(value) ? 2 : 1);
		return result;
	}

	/**
	 * Takes a unicode (java) string, and returns it as 8 bit data (in ISO-8859-1
	 * codepage).
	 * (In Excel terms, write compressed 8 bit unicode)
	 *
	 * @param  input   the String containing the data to be written
	 * @param  output  the byte array to which the data is to be written
	 * @param  offset  an offset into the byte arrat at which the data is start
	 *      when written
	 */
	public static void putCompressedUnicode(String input, byte[] output, int offset) {
		byte[] bytes = input.getBytes(ISO_8859_1);
		System.arraycopy(bytes, 0, output, offset, bytes.length);
	}

	public static void putCompressedUnicode(String input, LittleEndianOutput out) {
		byte[] bytes = input.getBytes(ISO_8859_1);
		out.write(bytes);
	}

	/**
	 * Takes a unicode string, and returns it as little endian (most
	 * important byte last) bytes in the supplied byte array.
	 * (In Excel terms, write uncompressed unicode)
	 *
	 * @param  input   the String containing the unicode data to be written
	 * @param  output  the byte array to hold the uncompressed unicode, should be twice the length of the String
	 * @param  offset  the offset to start writing into the byte array
	 */
	public static void putUnicodeLE(String input, byte[] output, int offset) {
		byte[] bytes = input.getBytes(UTF16LE);
		System.arraycopy(bytes, 0, output, offset, bytes.length);
	}
	public static void putUnicodeLE(String input, LittleEndianOutput out) {
		byte[] bytes = input.getBytes(UTF16LE);
		out.write(bytes);
	}

	public static String readUnicodeLE(LittleEndianInput in, int nChars) {
        byte[] bytes = new byte[nChars*2];
        in.readFully(bytes);
        return new String(bytes, UTF16LE);
	}

	/**
	 *  Apply printf() like formatting to a string.
	 *  Primarily used for logging.
	 * @param  message  the string with embedded formatting info
	 *                 eg. "This is a test %2.2"
	 * @param  params   array of values to format into the string
	 * @return          The formatted string
	 */
	public static String format(String message, Object[] params) {
		int currentParamNumber = 0;
		StringBuffer formattedMessage = new StringBuffer();
		for (int i = 0; i < message.length(); i++) {
			if (message.charAt(i) == '%') {
				if (currentParamNumber >= params.length) {
					formattedMessage.append("?missing data?");
				} else if (
					(params[currentParamNumber] instanceof Number)
						&& (i + 1 < message.length())) {
					i
						+= matchOptionalFormatting(
							(Number) params[currentParamNumber++],
							message.substring(i + 1),
							formattedMessage);
				} else {
					formattedMessage.append(
						params[currentParamNumber++].toString());
				}
			} else {
				if ((message.charAt(i) == '\\')
					&& (i + 1 < message.length())
					&& (message.charAt(i + 1) == '%')) {
					formattedMessage.append('%');
					i++;
				} else {
					formattedMessage.append(message.charAt(i));
				}
			}
		}
		return formattedMessage.toString();
	}


	private static int matchOptionalFormatting(
		Number number,
		String formatting,
		StringBuffer outputTo) {
		NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
		if ((0 < formatting.length())
			&& Character.isDigit(formatting.charAt(0))) {
			numberFormat.setMinimumIntegerDigits(
				Integer.parseInt(formatting.charAt(0) + ""));
			if ((2 < formatting.length())
				&& (formatting.charAt(1) == '.')
				&& Character.isDigit(formatting.charAt(2))) {
				numberFormat.setMaximumFractionDigits(
					Integer.parseInt(formatting.charAt(2) + ""));
				numberFormat.format(number, outputTo, new FieldPosition(0));
				return 3;
			}
			numberFormat.format(number, outputTo, new FieldPosition(0));
			return 1;
		} else if (
			(0 < formatting.length()) && (formatting.charAt(0) == '.')) {
			if ((1 < formatting.length())
				&& Character.isDigit(formatting.charAt(1))) {
				numberFormat.setMaximumFractionDigits(
					Integer.parseInt(formatting.charAt(1) + ""));
				numberFormat.format(number, outputTo, new FieldPosition(0));
				return 2;
			}
		}
		numberFormat.format(number, outputTo, new FieldPosition(0));
		return 1;
	}

	/**
	 * @return the encoding we want to use, currently hardcoded to ISO-8859-1
	 */
	public static String getPreferredEncoding() {
		return ISO_8859_1.name();
	}

	/**
	 * check the parameter has multibyte character
	 *
	 * @param value string to check
	 * @return boolean result true:string has at least one multibyte character
	 */
	public static boolean hasMultibyte(String value) {
		if (value == null)
			return false;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c > 0xFF) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks to see if a given String needs to be represented as Unicode
	 *
	 * @param value
	 * @return true if string needs Unicode to be represented.
	 */
	public static boolean isUnicodeString(final String value) {
        return !value.equals(new String(value.getBytes(ISO_8859_1), ISO_8859_1));
	}
	
   /**
    * An Iterator over an array of Strings.
    */
   public static class StringsIterator implements Iterator<String> {
      private String[] strings;
      private int position = 0;
      public StringsIterator(String[] strings) {
         if(strings != null) {
            this.strings = strings;
         } else {
            this.strings = new String[0];
         }
      }

      public boolean hasNext() {
         return position < strings.length;
      }
      public String next() {
         int ourPos = position++;
         if(ourPos >= strings.length)
            throw new ArrayIndexOutOfBoundsException(ourPos);
         return strings[ourPos];
      }
      public void remove() {}
   }


   /**
    * Some strings may contain encoded characters of the unicode private use area.
    * Currently the characters of the symbol fonts are mapped to the corresponding
    * characters in the normal unicode range. 
    *
    * @param string the original string 
    * @return the string with mapped characters
    * 
    * @see <a href="http://www.alanwood.net/unicode/private_use_area.html#symbol">Private Use Area (symbol)</a>
    * @see <a href="http://www.alanwood.net/demos/symbol.html">Symbol font - Unicode alternatives for Greek and special characters in HTML</a>
    */
   public static String mapMsCodepointString(String string) {
       if (string == null || "".equals(string)) return string;
       initMsCodepointMap();
       
       StringBuilder sb = new StringBuilder();
       final int length = string.length();
       for (int offset = 0; offset < length; ) {
          Integer msCodepoint = string.codePointAt(offset);
          Integer uniCodepoint = msCodepointToUnicode.get(msCodepoint);
          sb.appendCodePoint(uniCodepoint == null ? msCodepoint : uniCodepoint);
          offset += Character.charCount(msCodepoint);
       }
       
       return sb.toString();
   }
   
   public static synchronized void mapMsCodepoint(int msCodepoint, int unicodeCodepoint) {
       initMsCodepointMap();
       msCodepointToUnicode.put(msCodepoint, unicodeCodepoint);
   }
   
   private static synchronized void initMsCodepointMap() {
       if (msCodepointToUnicode != null) return;
       msCodepointToUnicode = new HashMap<Integer,Integer>();
       int i=0xF020;
       for (int ch : symbolMap_f020) {
           msCodepointToUnicode.put(i++, ch);
       }
       i = 0xf0a0;
       for (int ch : symbolMap_f0a0) {
           msCodepointToUnicode.put(i++, ch);
       }       
   }
   
   private static final int symbolMap_f020[] = {
       ' ', // 0xf020 space
       '!', // 0xf021 exclam
       8704, // 0xf022 universal
       '#', // 0xf023 numbersign
       8707, // 0xf024 existential
       '%', // 0xf025 percent
       '&', // 0xf026 ampersand
       8717, // 0xf027 suchthat
       '(', // 0xf028 parenleft
       ')', // 0xf029 parentright
       8727, // 0xf02a asteriskmath
       '+', // 0xf02b plus
       ',', // 0xf02c comma
       8722, // 0xf02d minus sign (long -)
       '.', // 0xf02e period
       '/', // 0xf02f slash
       '0', // 0xf030 0
       '1', // 0xf031 1
       '2', // 0xf032 2
       '3', // 0xf033 3
       '4', // 0xf034 4
       '5', // 0xf035 5
       '6', // 0xf036 6
       '7', // 0xf037 7
       '8', // 0xf038 8
       '9', // 0xf039 9
       ':', // 0xf03a colon
       ';', // 0xf03b semicolon
       '<', // 0xf03c less
       '=', // 0xf03d equal
       '>', // 0xf03e greater
       '?', // 0xf03f question
       8773, // 0xf040 congruent
       913, // 0xf041 alpha (upper)
       914, // 0xf042 beta (upper)
       935, // 0xf043 chi (upper)
       916, // 0xf044 delta (upper)
       917, // 0xf045 epsilon (upper)
       934, // 0xf046 phi (upper)
       915, // 0xf047 gamma (upper)
       919, // 0xf048 eta (upper)
       921, // 0xf049 iota (upper)
       977, // 0xf04a theta1 (lower)
       922, // 0xf04b kappa (upper)
       923, // 0xf04c lambda (upper)
       924, // 0xf04d mu (upper)
       925, // 0xf04e nu (upper)
       927, // 0xf04f omicron (upper)
       928, // 0xf050 pi (upper)
       920, // 0xf051 theta (upper)
       929, // 0xf052 rho (upper)
       931, // 0xf053 sigma (upper)
       932, // 0xf054 tau (upper)
       933, // 0xf055 upsilon (upper)
       962, // 0xf056 simga1 (lower)
       937, // 0xf057 omega (upper)
       926, // 0xf058 xi (upper)
       936, // 0xf059 psi (upper)
       918, // 0xf05a zeta (upper)
       '[', // 0xf05b bracketleft
       8765, // 0xf05c therefore
       ']', // 0xf05d bracketright
       8869, // 0xf05e perpendicular
       '_', // 0xf05f underscore
       ' ', // 0xf060 radicalex (doesn't exist in unicode)
       945, // 0xf061 alpha (lower)
       946, // 0xf062 beta (lower)
       967, // 0xf063 chi (lower)
       948, // 0xf064 delta (lower)
       949, // 0xf065 epsilon (lower)
       966, // 0xf066 phi (lower)
       947, // 0xf067 gamma (lower)
       951, // 0xf068 eta (lower)
       953, // 0xf069 iota (lower)
       981, // 0xf06a phi1 (lower)
       954, // 0xf06b kappa (lower)
       955, // 0xf06c lambda (lower)
       956, // 0xf06d mu (lower)
       957, // 0xf06e nu (lower)
       959, // 0xf06f omnicron (lower)
       960, // 0xf070 pi (lower)
       952, // 0xf071 theta (lower)
       961, // 0xf072 rho (lower)
       963, // 0xf073 sigma (lower)
       964, // 0xf074 tau (lower)
       965, // 0xf075 upsilon (lower)
       982, // 0xf076 piv (lower)
       969, // 0xf077 omega (lower)
       958, // 0xf078 xi (lower)
       968, // 0xf079 psi (lower)
       950, // 0xf07a zeta (lower)
       '{', // 0xf07b braceleft
       '|', // 0xf07c bar
       '}', // 0xf07d braceright
       8764, // 0xf07e similar '~'
       ' ', // 0xf07f not defined
   };

   private static final int symbolMap_f0a0[] = {
       8364, // 0xf0a0 not defined / euro symbol
       978, // 0xf0a1 upsilon1 (upper)
       8242, // 0xf0a2 minute
       8804, // 0xf0a3 lessequal
       8260, // 0xf0a4 fraction
       8734, // 0xf0a5 infinity
       402, // 0xf0a6 florin
       9827, // 0xf0a7 club
       9830, // 0xf0a8 diamond
       9829, // 0xf0a9 heart
       9824, // 0xf0aa spade
       8596, // 0xf0ab arrowboth
       8591, // 0xf0ac arrowleft
       8593, // 0xf0ad arrowup
       8594, // 0xf0ae arrowright
       8595, // 0xf0af arrowdown
       176, // 0xf0b0 degree
       177, // 0xf0b1 plusminus
       8243, // 0xf0b2 second
       8805, // 0xf0b3 greaterequal
       215, // 0xf0b4 multiply
       181, // 0xf0b5 proportional
       8706, // 0xf0b6 partialdiff
       8729, // 0xf0b7 bullet
       247, // 0xf0b8 divide
       8800, // 0xf0b9 notequal
       8801, // 0xf0ba equivalence
       8776, // 0xf0bb approxequal
       8230, // 0xf0bc ellipsis
       9168, // 0xf0bd arrowvertex
       9135, // 0xf0be arrowhorizex
       8629, // 0xf0bf carriagereturn
       8501, // 0xf0c0 aleph
       8475, // 0xf0c1 Ifraktur
       8476, // 0xf0c2 Rfraktur
       8472, // 0xf0c3 weierstrass
       8855, // 0xf0c4 circlemultiply
       8853, // 0xf0c5 circleplus
       8709, // 0xf0c6 emptyset
       8745, // 0xf0c7 intersection
       8746, // 0xf0c8 union
       8835, // 0xf0c9 propersuperset
       8839, // 0xf0ca reflexsuperset
       8836, // 0xf0cb notsubset
       8834, // 0xf0cc propersubset
       8838, // 0xf0cd reflexsubset
       8712, // 0xf0ce element
       8713, // 0xf0cf notelement
       8736, // 0xf0d0 angle
       8711, // 0xf0d1 gradient
       174, // 0xf0d2 registerserif
       169, // 0xf0d3 copyrightserif
       8482, // 0xf0d4 trademarkserif
       8719, // 0xf0d5 product
       8730, // 0xf0d6 radical
       8901, // 0xf0d7 dotmath
       172, // 0xf0d8 logicalnot
       8743, // 0xf0d9 logicaland
       8744, // 0xf0da logicalor
       8660, // 0xf0db arrowdblboth
       8656, // 0xf0dc arrowdblleft
       8657, // 0xf0dd arrowdblup
       8658, // 0xf0de arrowdblright
       8659, // 0xf0df arrowdbldown
       9674, // 0xf0e0 lozenge
       9001, // 0xf0e1 angleleft
       174, // 0xf0e2 registersans
       169, // 0xf0e3 copyrightsans
       8482, // 0xf0e4 trademarksans
       8721, // 0xf0e5 summation
       9115, // 0xf0e6 parenlefttp
       9116, // 0xf0e7 parenleftex
       9117, // 0xf0e8 parenleftbt
       9121, // 0xf0e9 bracketlefttp
       9122, // 0xf0ea bracketleftex
       9123, // 0xf0eb bracketleftbt
       9127, // 0xf0ec bracelefttp
       9128, // 0xf0ed braceleftmid
       9129, // 0xf0ee braceleftbt
       9130, // 0xf0ef braceex
       ' ', // 0xf0f0 not defined
       9002, // 0xf0f1 angleright
       8747, // 0xf0f2 integral
       8992, // 0xf0f3 integraltp
       9134, // 0xf0f4 integralex
       8993, // 0xf0f5 integralbt
       9118, // 0xf0f6 parenrighttp
       9119, // 0xf0f7 parenrightex
       9120, // 0xf0f8 parenrightbt
       9124, // 0xf0f9 bracketrighttp
       9125, // 0xf0fa bracketrightex
       9126, // 0xf0fb bracketrightbt
       9131, // 0xf0fc bracerighttp
       9132, // 0xf0fd bracerightmid
       9133, // 0xf0fe bracerightbt
       ' ', // 0xf0ff not defined
   };
}
