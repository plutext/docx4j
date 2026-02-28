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
import java.util.List;
import java.util.Stack;

import org.docx4j.org.apache.poi.poifs.common.POIFSBigBlockSize;
import org.docx4j.org.apache.poi.poifs.common.POIFSConstants;
import org.docx4j.org.apache.poi.poifs.filesystem.BATManaged;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSStream;
import org.docx4j.org.apache.poi.poifs.storage.HeaderBlock;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class embodies the Property Table for a {@link POIFSFileSystem};
 * this is basically the directory for all of the documents in the
 * filesystem and looks up entries in the filesystem to their
 * chain of blocks.
 */
public final class PropertyTable implements BATManaged {
	
	protected static Logger log = LoggerFactory.getLogger(PropertyTable.class);	

    private final HeaderBlock    _header_block;
    private final List<Property> _properties = new ArrayList<>();
    private final POIFSBigBlockSize _bigBigBlockSize;

    public PropertyTable(HeaderBlock headerBlock)
    {
        _header_block = headerBlock;
        _bigBigBlockSize = headerBlock.getBigBlockSize();
        addProperty(new RootProperty());
    }

    /**
     * reading constructor (used when we've read in a file and we want
     * to extract the property table from it). Populates the
     * properties thoroughly
     *
     * @param headerBlock the header block of the file
     * @param filesystem the filesystem to read from
     *
     * @throws IOException if anything goes wrong (which should be
     *            a result of the input being NFG)
     */
    public PropertyTable(final HeaderBlock headerBlock, final POIFSFileSystem filesystem)
    throws IOException {
        this(
              headerBlock,
              new POIFSStream(filesystem, headerBlock.getPropertyStart())
        );
    }

    /* only invoked locally and from the junit tests */
    PropertyTable(final HeaderBlock headerBlock, final Iterable<ByteBuffer> dataSource)
    throws IOException {
        _header_block = headerBlock;
        _bigBigBlockSize = headerBlock.getBigBlockSize();

        for (ByteBuffer bb : dataSource) {
            // Turn it into an array
            byte[] data = null;
            if (bb.hasArray() && bb.arrayOffset() == 0) {
                final byte[] array = bb.array();
                if (array.length == _bigBigBlockSize.getBigBlockSize()) {
                    data = array;
                }
            }
            if (data == null) {
                data = IOUtils.safelyAllocate(_bigBigBlockSize.getBigBlockSize(), POIFSFileSystem.getMaxRecordLength());

                int toRead = data.length;
                if (bb.remaining() < _bigBigBlockSize.getBigBlockSize()) {
                    // Looks to be a truncated block
                    // This isn't allowed, but some third party created files
                    //  sometimes do this, and we can normally read anyway
                    log.warn("Short Property Block, {} bytes instead of the expected {}", bb.remaining(),_bigBigBlockSize.getBigBlockSize());
                    toRead = bb.remaining();
                }

                bb.get(data, 0, toRead);
            }

            PropertyFactory.convertToProperties(data, _properties);
        }

        Property property = _properties.get(0);
        if (property != null) {
            if (property instanceof DirectoryProperty) {
                populatePropertyTree((DirectoryProperty) property, 0);
            } else {
                throw new IOException("Invalid format, cannot convert property " + property + " to DirectoryProperty");
            }
        }
    }

    /**
     * Add a property to the list of properties we manage
     *
     * @param property the new Property to manage
     */
    public void addProperty(Property property) {
        _properties.add(property);
    }

    /**
     * Remove a property from the list of properties we manage
     *
     * @param property the Property to be removed
     */
    public void removeProperty(final Property property) {
        _properties.remove(property);
    }

    /**
     * Get the root property
     *
     * @return the root property
     */
    public RootProperty getRoot() {
        // it's always the first element in the List
        Property property = _properties.get(0);
        if (property instanceof RootProperty) {
            return (RootProperty) property;
        } else {
            throw new IllegalStateException("Invalid format, cannot convert property " +
                    property + " to RootProperty");
        }
    }

    /**
     * Get the start block for the property table
     *
     * @return start block index
     */
    public int getStartBlock() {
        return _header_block.getPropertyStart();
    }

    /**
     * Set the start block for this instance
     *
     * @param index index into the array of BigBlock instances making
     *              up the filesystem
     */
    public void setStartBlock(final int index) {
        _header_block.setPropertyStart(index);
    }



    /**
     * Return the number of BigBlock's this instance uses
     *
     * @return count of BigBlock instances
     */
    public int countBlocks() {
       long rawSize = _properties.size() * (long)POIFSConstants.PROPERTY_SIZE;
       int blkSize = _bigBigBlockSize.getBigBlockSize();
       int numBlocks = (int)(rawSize / blkSize);
       if ((rawSize % blkSize) != 0) {
           numBlocks++;
       }
       return numBlocks;
    }

    /**
     * Prepare to be written
     */
    public void preWrite() {
        List<Property> pList = new ArrayList<>();
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
    public void write(POIFSStream stream) throws IOException {
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

       // Update the number of property blocks in the header
       _header_block.setPropertyCount(countBlocks());
    }

    // Maximum depth of the property tree to prevent stackoverflow errors
    private static final int MAX_PROPERTY_DEPTH = 1000;

    private void populatePropertyTree(final DirectoryProperty root, final int depth) throws IOException {
        if (depth > MAX_PROPERTY_DEPTH) {
            throw new IOException("Property tree too deep, likely a corrupt file");
        }

        int index = root.getChildIndex();

        if (!Property.isValidIndex(index)) {
            // property has no children
            return;
        }

        final Stack<Property> children = new Stack<>();
        children.push(_properties.get(index));
        while (!children.empty()) {
            Property property = children.pop();
            if (property == null) {
                // unknown / unsupported / corrupted property, skip
                continue;
            }

            root.addChild(property);
            if (property.isDirectory()) {
                populatePropertyTree((DirectoryProperty) property, depth + 1);
            }
            index = property.getPreviousChildIndex();
            if (isValidIndex(index)) {
                children.push(_properties.get(index));
            }
            index = property.getNextChildIndex();
            if (isValidIndex(index)) {
                children.push(_properties.get(index));
            }
        }
    }

    private boolean isValidIndex(int index) {
        if (! Property.isValidIndex(index))
            return false;
        if (index < 0 || index >= _properties.size()) {
            log.warn("Property index {} outside the valid range 0..{}", index,_properties.size());
            return false;
        }
        return true;
    }
}
