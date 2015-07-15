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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.docx4j.org.apache.poi.poifs.common.POIFSBigBlockSize;
import org.docx4j.org.apache.poi.poifs.common.POIFSConstants;
import org.docx4j.org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.docx4j.org.apache.poi.poifs.filesystem.NPOIFSStream;
import org.docx4j.org.apache.poi.poifs.storage.HeaderBlock;
//import org.docx4j.org.apache.poi.util.POILogFactory;
//import org.docx4j.org.apache.poi.util.POILogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class embodies the Property Table for a {@link NPOIFSFileSystem}; 
 *  this is basically the directory for all of the documents in the
 * filesystem.
 */
public final class NPropertyTable extends PropertyTableBase {
//    private static final POILogger _logger =
//       POILogFactory.getLogger(NPropertyTable.class);
	private static Logger _logger = LoggerFactory.getLogger(NPropertyTable.class);
	
    private POIFSBigBlockSize _bigBigBlockSize;

    public NPropertyTable(HeaderBlock headerBlock)
    {
        super(headerBlock);
        _bigBigBlockSize = headerBlock.getBigBlockSize();
    }

    /**
     * reading constructor (used when we've read in a file and we want
     * to extract the property table from it). Populates the
     * properties thoroughly
     *
     * @param headerBlock the header block of the file
     * @param filesystem the filesystem to read from
     *
     * @exception IOException if anything goes wrong (which should be
     *            a result of the input being NFG)
     */
    public NPropertyTable(final HeaderBlock headerBlock,
                          final NPOIFSFileSystem filesystem)
        throws IOException
    {
        super(
              headerBlock,
              buildProperties(
                    (new NPOIFSStream(filesystem, headerBlock.getPropertyStart())).iterator(),
                    headerBlock.getBigBlockSize()
              )
        );
        _bigBigBlockSize = headerBlock.getBigBlockSize();
    }
    
    /**
     * Builds
     * @param startAt
     * @param filesystem
     * @throws IOException
     */
    private static List<Property> buildProperties(final Iterator<ByteBuffer> dataSource,
          final POIFSBigBlockSize bigBlockSize) throws IOException
    {
       List<Property> properties = new ArrayList<Property>();
       while(dataSource.hasNext()) {
          ByteBuffer bb = dataSource.next();
          
          // Turn it into an array
          byte[] data;
          if(bb.hasArray() && bb.arrayOffset() == 0 && 
                bb.array().length == bigBlockSize.getBigBlockSize()) {
             data = bb.array();
          } else {
             data = new byte[bigBlockSize.getBigBlockSize()];
             
             int toRead = data.length;
             if (bb.remaining() < bigBlockSize.getBigBlockSize()) {
                // Looks to be a truncated block
                // This isn't allowed, but some third party created files
                //  sometimes do this, and we can normally read anyway
                _logger.warn( "Short Property Block, ", bb.remaining(),
                            " bytes instead of the expected " + bigBlockSize.getBigBlockSize());
                toRead = bb.remaining();
             }
             
             bb.get(data, 0, toRead);
          }
          
          PropertyFactory.convertToProperties(data, properties);
       }
       return properties;
    }

    /**
     * Return the number of BigBlock's this instance uses
     *
     * @return count of BigBlock instances
     */
    public int countBlocks()
    {
       int size = _properties.size() * POIFSConstants.PROPERTY_SIZE;
       return (int)Math.ceil(size / _bigBigBlockSize.getBigBlockSize());
    }
 
    /**
     * Prepare to be written
     */
    public void preWrite() {
        List<Property> pList = new ArrayList<Property>();
        // give each property its index
        int i=0;
        for (Property p : _properties) {
            // only handle non-null properties 
            if (p == null) continue;
            p.setIndex(i++);
            pList.add(p);
        }

        // prepare each property for writing
        for (Property p : pList) p.preWrite();
    }    
    
    /**
     * Writes the properties out into the given low-level stream
     */
    public void write(NPOIFSStream stream) throws IOException {
       OutputStream os = stream.getOutputStream();
       for(Property property : _properties) {
          if(property != null) {
             property.writeData(os);
          }
       }
       os.close();
       
       // Update the start position if needed
       if(getStartBlock() != stream.getStartBlock()) {
          setStartBlock(stream.getStartBlock());
       }
    }
}
