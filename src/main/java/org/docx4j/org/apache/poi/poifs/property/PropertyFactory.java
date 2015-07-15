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
        

package org.docx4j.org.apache.poi.poifs.property;

import java.io.IOException;
import java.util.*;

import org.docx4j.org.apache.poi.poifs.common.POIFSConstants;
import org.docx4j.org.apache.poi.poifs.storage.ListManagedBlock;

/**
 * Factory for turning an array of RawDataBlock instances containing
 * Property data into an array of proper Property objects.
 *
 * The array produced may be sparse, in that any portion of data that
 * should correspond to a Property, but which does not map to a proper
 * Property (i.e., a DirectoryProperty, DocumentProperty, or
 * RootProperty) will get mapped to a null Property in the array.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

class PropertyFactory
{
    // no need for an accessible constructor
    private PropertyFactory()
    {
    }

    /**
     * Convert raw data blocks to an array of Property's
     *
     * @param blocks to be converted
     *
     * @return the converted List of Property objects. May contain
     *         nulls, but will not be null
     *
     * @exception IOException if any of the blocks are empty
     */
    static List<Property> convertToProperties(ListManagedBlock [] blocks)
        throws IOException
    {
        List<Property> properties = new ArrayList<Property>();

        for (int j = 0; j < blocks.length; j++) {
            byte[] data = blocks[ j ].getData();
            convertToProperties(data, properties);
        }
        return properties;
    }
    
    static void convertToProperties(byte[] data, List<Property> properties)
        throws IOException
    {
       int property_count = data.length / POIFSConstants.PROPERTY_SIZE;
       int offset         = 0;

       for (int k = 0; k < property_count; k++) {
          switch (data[ offset + PropertyConstants.PROPERTY_TYPE_OFFSET ]) {
          case PropertyConstants.DIRECTORY_TYPE :
             properties.add(
                   new DirectoryProperty(properties.size(), data, offset)
             );
             break;

          case PropertyConstants.DOCUMENT_TYPE :
             properties.add(
                   new DocumentProperty(properties.size(), data, offset)
             );
             break;

          case PropertyConstants.ROOT_TYPE :
             properties.add(
                   new RootProperty(properties.size(), data, offset)
             );
             break;

          default :
             properties.add(null);
             break;
          }
          
          offset += POIFSConstants.PROPERTY_SIZE;
       }
    }
    
}   // end package scope class PropertyFactory

