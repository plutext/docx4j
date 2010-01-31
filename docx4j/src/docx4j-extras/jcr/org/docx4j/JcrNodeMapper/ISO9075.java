package org.docx4j.JcrNodeMapper;

/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

// We need this until http://issues.alfresco.com/browse/AR-1979 is resolved

import java.util.Collection;


//import org.apache.xerces.util.XMLChar;
import com.sun.org.apache.xerces.internal.util.XMLChar;

/**
 * Support for the ISO 9075 encoding of XML element names.
 * 
 * @author Andy Hind
 */
public class ISO9075
{
	
    /*
     * Mask for hex encoding
     */
    private static final int MASK = (1 << 4) - 1;

    /*
     * Digits used string encoding
     */
    private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
            'f' };

    /**
     * Private constructor
     * 
     */
    private ISO9075()
    {
        super();
    }

    /**
     * Encode a string according to ISO 9075
     * 
     * @param toEncode
     * @return
     */
    public static String encode(String toEncode)
    {
        if ((toEncode == null) || (toEncode.length() == 0))
        {
            return toEncode;
        }
        else if (XMLChar.isValidName(toEncode) && (toEncode.indexOf("_x") == -1) && (toEncode.indexOf(':') == -1))
        {
            return toEncode;
        }
        else
        {
            StringBuilder builder = new StringBuilder(toEncode.length());
            for (int i = 0; i < toEncode.length(); i++)
            {
                char c = toEncode.charAt(i);
                // First requires special test
                if (i == 0)
                {
                    if (XMLChar.isNCNameStart(c))
                    {
                        // The first character may be the _ at the start of an
                        // encoding pattern
                        if (matchesEncodedPattern(toEncode, i))
                        {
                            // Encode the first _
                            encode('_', builder);
                        }
                        else
                        {
                            // Just append
                            builder.append(c);
                        }
                    }
                    else
                    {
                        // Encode an invalid start character for an XML element
                        // name.
                        encode(c, builder);
                    }
                }
                else if (!XMLChar.isNCName(c))
                {
                    encode(c, builder);
                }
                else
                {
                    if (matchesEncodedPattern(toEncode, i))
                    {
                        // '_' must be encoded
                        encode('_', builder);
                    }
                    else
                    {
                        builder.append(c);
                    }
                }
            }
            return builder.toString();
        }

    }

    private static boolean matchesEncodedPattern(String string, int position)
    {
        return (string.length() >= position + 6)
                && (string.charAt(position) == '_') && (string.charAt(position + 1) == 'x')
                && isHexChar(string.charAt(position + 2)) && isHexChar(string.charAt(position + 3))
                && isHexChar(string.charAt(position + 4)) && isHexChar(string.charAt(position + 5))
                && (string.charAt(position + 6) == '_');
    }

    private static boolean isHexChar(char c)
    {
        switch (c)
        {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
        case 'a':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
        case 'f':
        case 'A':
        case 'B':
        case 'C':
        case 'D':
        case 'E':
        case 'F':
            return true;
        default:
            return false;
        }
    }

    public static String decode(String toDecode)
    {
        if ((toDecode == null) || (toDecode.length() < 7) || (toDecode.indexOf("_x") < 0))
        {
            return toDecode;
        }
        StringBuffer decoded = new StringBuffer();
        for (int i = 0, l = toDecode.length(); i < l; i++)
        {
            if (matchesEncodedPattern(toDecode, i))
            {
                decoded.append(((char) Integer.parseInt(toDecode.substring(i + 2, i + 6), 16)));
                i += 6;// then one added for the loop to mkae the length of 7
            }
            else
            {
                decoded.append(toDecode.charAt(i));
            }
        }
        return decoded.toString();
    }

    private static void encode(char c, StringBuilder builder)
    {
        char[] buf = new char[] { '_', 'x', '0', '0', '0', '0', '_' };
        int charPos = 6;
        do
        {
            buf[--charPos] = DIGITS[c & MASK];
            c >>>= 4;
        }
        while (c != 0);
        builder.append(buf);
    }

//    public static String getXPathName(QName qName, NamespacePrefixResolver nspr)
//    {
//
//        Collection<String> prefixes = nspr.getPrefixes(qName.getNamespaceURI());
//        if (prefixes.size() == 0)
//        {
//            throw new NamespaceException("A namespace prefix is not registered for uri " + qName.getNamespaceURI());
//        }
//        String prefix = prefixes.iterator().next();
//        if (prefix.equals(NamespaceService.DEFAULT_PREFIX))
//        {
//            return ISO9075.encode(qName.getLocalName());
//        }
//        else
//        {
//            return prefix + ":" + ISO9075.encode(qName.getLocalName());
//        }
//
//    }
//
//    public static String getXPathName(QName qName)
//    {
//
//        return "{" + qName.getNamespaceURI() + "}" + ISO9075.encode(qName.getLocalName());
//
//    }
}
