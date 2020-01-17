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

import java.io.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Utilities to read hex from files.
 * TODO - move to test packages
 *
 * @author Marc Johnson
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class HexRead
{
    /**
     * This method reads hex data from a filename and returns a byte array.
     * The file may contain line comments that are preceeded with a # symbol.
     *
     * @param filename  The filename to read
     * @return The bytes read from the file.
     * @throws IOException If there was a problem while reading the file.
     */
    public static byte[] readData( String filename ) throws IOException
    {
        File file = new File( filename );
        FileInputStream stream = new FileInputStream( file );
        try
        {
            return readData( stream, -1 );
        }
        finally
        {
            stream.close();
        }
    }

    /**
     * Same as readData(String) except that this method allows you to specify sections within
     * a file.  Sections are referenced using section headers in the form:
     * <pre>
     *  [sectioname]
     * </pre>
     *
     * @see #readData(String)
     */
    public static byte[] readData(InputStream stream, String section ) throws IOException {

        try
        {
            StringBuffer sectionText = new StringBuffer();
            boolean inSection = false;
            int c = stream.read();
            while ( c != -1 )
            {
                switch ( c )
                {
                    case '[':
                        inSection = true;
                        break;
                    case '\n':
                    case '\r':
                        inSection = false;
                        sectionText = new StringBuffer();
                        break;
                    case ']':
                        inSection = false;
                        if ( sectionText.toString().equals( section ) ) return readData( stream, '[' );
                        sectionText = new StringBuffer();
                        break;
                    default:
                        if ( inSection ) sectionText.append( (char) c );
                }
                c = stream.read();
            }
        }
        finally
        {
            stream.close();
        }
        throw new IOException( "Section '" + section + "' not found" );
    }
    public static byte[] readData( String filename, String section ) throws IOException
    {
        File file = new File( filename );
        FileInputStream stream = new FileInputStream( file );
        return readData(stream, section);
    }

    static public byte[] readData( InputStream stream, int eofChar )
            throws IOException
    {
        int characterCount = 0;
        byte b = (byte) 0;
        List<Byte> bytes = new ArrayList<Byte>();
        boolean done = false;
        while ( !done )
        {
            int count = stream.read();
            char baseChar = 'a';
            if ( count == eofChar ) break;
            switch ( count )
            {
                case '#':
                    readToEOL( stream );
                    break;
                case '0': case '1': case '2': case '3': case '4': case '5':
                case '6': case '7': case '8': case '9':
                    b <<= 4;
                    b += (byte) ( count - '0' );
                    characterCount++;
                    if ( characterCount == 2 )
                    {
                        bytes.add( Byte.valueOf( b ) );
                        characterCount = 0;
                        b = (byte) 0;
                    }
                    break;
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                    baseChar = 'A';
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                    b <<= 4;
                    b += (byte) ( count + 10 - baseChar );
                    characterCount++;
                    if ( characterCount == 2 )
                    {
                        bytes.add( Byte.valueOf( b ) );
                        characterCount = 0;
                        b = (byte) 0;
                    }
                    break;
                case -1:
                    done = true;
                    break;
                default :
                    break;
            }
        }
        Byte[] polished = bytes.toArray( new Byte[0] );
        byte[] rval = new byte[polished.length];
        for ( int j = 0; j < polished.length; j++ )
        {
            rval[j] = polished[j].byteValue();
        }
        return rval;
    }

    static public byte[] readFromString(String data) {
        try {
            return readData(new ByteArrayInputStream( data.getBytes() ), -1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static private void readToEOL( InputStream stream ) throws IOException
    {
        int c = stream.read();
        while ( c != -1 && c != '\n' && c != '\r' )
        {
            c = stream.read();
        }
    }
}
