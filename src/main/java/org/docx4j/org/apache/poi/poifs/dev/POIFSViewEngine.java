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
        

package org.docx4j.org.apache.poi.poifs.dev;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class contains methods used to inspect POIFSViewable objects
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class POIFSViewEngine
{
    private static final String _EOL = System.getProperty("line.separator");

    /**
     * Inspect an object that may be viewable, and drill down if told
     * to
     *
     * @param viewable the object to be viewed
     * @param drilldown if true, and the object implements
     *                  POIFSViewable, inspect the objects' contents
     *                  (recursively)
     * @param indentLevel how far in to indent each string
     * @param indentString string to use for indenting
     *
     * @return a List of Strings holding the content
     */

    public static List<String> inspectViewable(final Object viewable,
                                       final boolean drilldown,
                                       final int indentLevel,
                                       final String indentString)
    {
        List<String> objects = new ArrayList<String>();

        if (viewable instanceof POIFSViewable)
        {
            POIFSViewable inspected = ( POIFSViewable ) viewable;

            objects.add(indent(indentLevel, indentString,
                               inspected.getShortDescription()));
            if (drilldown)
            {
                if (inspected.preferArray())
                {
                    Object[] data = inspected.getViewableArray();

                    for (int j = 0; j < data.length; j++)
                    {
                        objects.addAll(inspectViewable(data[ j ], drilldown,
                                                       indentLevel + 1,
                                                       indentString));
                    }
                }
                else
                {
                    Iterator<Object> iter = inspected.getViewableIterator();

                    while (iter.hasNext())
                    {
                        objects.addAll(inspectViewable(iter.next(),
                                                       drilldown,
                                                       indentLevel + 1,
                                                       indentString));
                    }
                }
            }
        }
        else if (viewable==null) {
            objects.add(indent(indentLevel, indentString, "[null]"));        	
        } else 
        {
            objects.add(indent(indentLevel, indentString,
                               viewable.toString()));
        }
        return objects;
    }

    private static String indent(final int indentLevel,
                                 final String indentString, final String data)
    {
        StringBuffer finalBuffer  = new StringBuffer();
        StringBuffer indentPrefix = new StringBuffer();

        for (int j = 0; j < indentLevel; j++)
        {
            indentPrefix.append(indentString);
        }
        LineNumberReader reader =
            new LineNumberReader(new StringReader(data));

        try
        {
            String line = reader.readLine();

            while (line != null)
            {
                finalBuffer.append(indentPrefix).append(line).append(_EOL);
                line = reader.readLine();
            }
        }
        catch (IOException e)
        {
            finalBuffer.append(indentPrefix).append(e.getMessage())
                .append(_EOL);
        }
        return finalBuffer.toString();
    }
}   // end public class POIFSViewEngine

