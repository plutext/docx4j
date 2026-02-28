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

package org.docx4j.org.apache.poi.poifs.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.docx4j.org.apache.poi.hssf.OldExcelFormatException;
import org.docx4j.org.apache.poi.poifs.common.POIFSBigBlockSize;
import org.docx4j.org.apache.poi.poifs.common.POIFSConstants;
import org.docx4j.org.apache.poi.poifs.filesystem.FileMagic;
import org.docx4j.org.apache.poi.poifs.filesystem.NotOLE2FileException;
import org.docx4j.org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.docx4j.org.apache.poi.util.HexDump;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.IntegerField;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LongField;
import org.docx4j.org.apache.poi.util.ShortField;

/**
 * The block containing the archive header
 */
public final class HeaderBlock implements HeaderBlockConstants {

    private static final byte _default_value = ( byte ) 0xFF;

    /**
     * What big block size the file uses. Most files
     *  use 512 bytes, but a few use 4096
     */
    private final POIFSBigBlockSize bigBlockSize;

    /** 
     * Number of big block allocation table blocks (int).
     * (Number of FAT Sectors in Microsoft parlance).
     */
    private int _bat_count;

    /**
     * Number of property blocks (int).
     */
    private int _property_count;

    /** 
     * Start of the property set block (int index of the property set
     * chain's first big block).
     */
    private int _property_start;

    /** 
     * start of the small block allocation table (int index of small
     * block allocation table's first big block)
     */
    private int _sbat_start;
    /**
     * Number of small block allocation table blocks (int)
     * (Number of MiniFAT Sectors in Microsoft parlance)
     */
    private int _sbat_count;

    /** 
     * Big block index for extension to the big block allocation table
     */
    private int _xbat_start;
    /**
     * Number of big block allocation table blocks (int)
     * (Number of DIFAT Sectors in Microsoft parlance)
     */
    private int _xbat_count;
    
    /**
     * The data. Only ever 512 bytes, because 4096 byte
     *  files use zeros for the extra header space.
     */
    private final byte[] _data;
    
    /**
     * create a new HeaderBlockReader from an InputStream
     *
     * @param stream the source InputStream
     *
     * @throws IOException on errors or bad data
     */
    public HeaderBlock(InputStream stream) throws IOException {
        // Grab the first 512 bytes
        // (For 4096 sized blocks, the remaining 3584 bytes are zero)
        // Then, process the contents
        this(readFirst512(stream));
        
        // Fetch the rest of the block if needed
        if(bigBlockSize.getBigBlockSize() != 512) {
           int rest = bigBlockSize.getBigBlockSize() - 512;
           byte[] tmp = IOUtils.safelyAllocate(rest, POIFSFileSystem.getMaxRecordLength());
           IOUtils.readFully(stream, tmp);
        }
    }
    
    public HeaderBlock(ByteBuffer buffer) throws IOException {
       this(IOUtils.toByteArray(buffer, POIFSConstants.SMALLER_BIG_BLOCK_SIZE));
    }
    
    private HeaderBlock(byte[] data) throws IOException {
       this._data = data.clone();
       
        // verify signature
       FileMagic fm = FileMagic.valueOf(data);
       
       switch (fm) {
       case OLE2:
           break;
       case OOXML:
           throw new OfficeXmlFileException("The supplied data appears to be in the Office 2007+ XML. "
               + "You are calling the part of POI that deals with OLE2 Office Documents. "
               + "You need to call a different part of POI to process this data (eg XSSF instead of HSSF)");
       case XML:
           throw new NotOLE2FileException("The supplied data appears to be a raw XML file. "
               + "Formats such as Office 2003 XML are not supported");
       case MSWRITE:
           throw new NotOLE2FileException("The supplied data appears to be in the old MS Write format. "
               + "Apache POI doesn't currently support this format");
       case WORD2:
           throw new NotOLE2FileException("The supplied data appears to be an old Word version 2 file. "
               + "Apache POI doesn't currently support this format");
       case BIFF2:
       case BIFF3:
       case BIFF4:
           throw new OldExcelFormatException("The supplied data appears to be in "+fm+" format. "
               + "HSSF only supports the BIFF8 format, try OldExcelExtractor");
       default:
           // Give a generic error if the OLE2 signature isn't found
           String exp = HexDump.longToHex(_signature);
           String act = HexDump.longToHex(LittleEndian.getLong(data, 0));
           throw new NotOLE2FileException(
               "Invalid header signature; read " + act + ", expected " + exp +
               " - Your file appears not to be a valid OLE2 document");
       }
       
        // Figure out our block size
        if (_data[30] == 12) {
            this.bigBlockSize = POIFSConstants.LARGER_BIG_BLOCK_SIZE_DETAILS;
        } else if(_data[30] == 9) {
            this.bigBlockSize = POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS;
        } else {
           throw new IOException("Unsupported blocksize  (2^"+ _data[30] + "). Expected 2^9 or 2^12.");
        }

       // Setup the fields to read and write the counts and starts
      _bat_count  = new IntegerField(_bat_count_offset, data).get();
      _property_count = new IntegerField(_property_count_offset,_data).get();
      _property_start = new IntegerField(_property_start_offset,_data).get();
      _sbat_start = new IntegerField(_sbat_start_offset, _data).get();
      _sbat_count = new IntegerField(_sbat_block_count_offset, _data).get();
      _xbat_start = new IntegerField(_xbat_start_offset, _data).get();
      _xbat_count = new IntegerField(_xbat_count_offset, _data).get();
    }
    
   /**
    * Create a single instance initialized with default values
    */
   public HeaderBlock(POIFSBigBlockSize bigBlockSize)
   {
      this.bigBlockSize = bigBlockSize;

      // Our data is always 512 big no matter what
      _data = new byte[ POIFSConstants.SMALLER_BIG_BLOCK_SIZE ];
      Arrays.fill(_data, _default_value);
      
      // Set all the default values
      new LongField(_signature_offset, _signature, _data);
      new IntegerField(0x08, 0, _data);
      new IntegerField(0x0c, 0, _data);
      new IntegerField(0x10, 0, _data);
      new IntegerField(0x14, 0, _data);
      new ShortField(0x18, ( short ) 0x3b, _data);
      new ShortField(0x1a, ( short ) 0x3, _data);
      new ShortField(0x1c, ( short ) -2, _data);
       
      new ShortField(0x1e, bigBlockSize.getHeaderValue(), _data);
      new IntegerField(0x20, 0x6, _data);
      new IntegerField(0x24, 0, _data);
      new IntegerField(0x28, 0, _data);
      new IntegerField(0x34, 0, _data);
      new IntegerField(0x38, 0x1000, _data);
      
      // Initialize the variables
      _bat_count = 0;
      _sbat_count = 0;
      _xbat_count = 0;
      _property_start = POIFSConstants.END_OF_CHAIN;
      _sbat_start = POIFSConstants.END_OF_CHAIN;
      _xbat_start = POIFSConstants.END_OF_CHAIN;
   }
   
    private static byte[] readFirst512(InputStream stream) throws IOException {
      // Grab the first 512 bytes
      // (For 4096 sized blocks, the remaining 3584 bytes are zero)
      byte[] data = new byte[512];
      int bsCount = IOUtils.readFully(stream, data);
      if(bsCount != 512) {
         throw alertShortRead(bsCount);
      }
      return data;
    }

    private static IOException alertShortRead(int pRead) {
        int read = Math.max(pRead, 0);
        //Can't have -1 bytes read in the error message!
        String type = " byte" + (read == 1 ? (""): ("s"));

        return new IOException("Unable to read entire header; "
                + read + type + " read; expected 512 bytes");
    }

    /**
     * @return the number of property blocks
     * @since 5.4.0
     */
    public int getPropertyCount() {
        return _property_count;
    }

    /**
     * Set the number of property blocks
     *
     * @param property_count number of property blocks
     * @since 5.4.0
     */
    public void setPropertyCount(final int property_count) {
        this._property_count = property_count;
    }

    /**
     * get start of Property Table
     *
     * @return the index of the first block of the Property Table
     */
    public int getPropertyStart() {
        return _property_start;
    }

    /**
     * Set start of Property Table
     *
     * @param startBlock the index of the first block of the Property Table
     */
    public void setPropertyStart(final int startBlock) {
        _property_start = startBlock;
    }

    /**
     * @return start of small block (MiniFAT) allocation table
     */
    public int getSBATStart() {
        return _sbat_start;
    }
    public int getSBATCount() {
       return _sbat_count;
    }
    
   /**
    * Set start of small block allocation table
    *
    * @param startBlock the index of the first big block of the small
    *                   block allocation table
    */
   public void setSBATStart(final int startBlock) {
       _sbat_start = startBlock;
   }
   /**
    * Set count of SBAT blocks
    *
    * @param count the number of SBAT blocks
    */
   public void setSBATBlockCount(final int count)
   {
      _sbat_count = count;
   }

    /**
     * @return number of BAT blocks
     */
    public int getBATCount() {
        return _bat_count;
    }
   /**
    * Sets the number of BAT blocks that are used.
    * This is the number used in both the BAT and XBAT. 
    */
   public void setBATCount(final int count) {
      _bat_count = count;
   }

    /**
     * Returns the offsets to the first (up to) 109
     *  BAT sectors.
     * Any additional BAT sectors are held in the XBAT (DIFAT)
     *  sectors in a chain.
     * @return BAT offset array
     */
    public int[] getBATArray() {
      // Read them in
        int[] result = new int[ Math.min(_bat_count,_max_bats_in_header) ];
        int offset = _bat_array_offset;
        for (int j = 0; j < result.length; j++) {
            result[ j ] = LittleEndian.getInt(_data, offset);
            offset     += LittleEndianConsts.INT_SIZE;
        }
        return result;
    }
    /**
     * Sets the offsets of the first (up to) 109
     *  BAT sectors.
     */
    public void setBATArray(int[] bat_array) {
       int count = Math.min(bat_array.length, _max_bats_in_header);
       int blank = _max_bats_in_header - count;
       
      int offset = _bat_array_offset;
       for(int i=0; i<count; i++) {
          LittleEndian.putInt(_data, offset, bat_array[i]);
         offset += LittleEndianConsts.INT_SIZE;
       }
       for(int i=0; i<blank; i++) {
         LittleEndian.putInt(_data, offset, POIFSConstants.UNUSED_BLOCK);
         offset += LittleEndianConsts.INT_SIZE;
       }
    }

    /**
     * @return XBAT (DIFAT) count
     */
    public int getXBATCount() {
        return _xbat_count;
    }
    /**
     * Sets the number of XBAT (DIFAT) blocks used
     */
    public void setXBATCount(final int count) {
       _xbat_count = count;
    }

    /**
     * @return XBAT (DIFAT) index
     */
    public int getXBATIndex() {
        return _xbat_start;
    }
    /**
     * Sets the first XBAT (DIFAT) block location
     */
   public void setXBATStart(final int startBlock) {
      _xbat_start = startBlock;
  }

    /**
     * @return The Big Block size, normally 512 bytes, sometimes 4096 bytes
     */
    public POIFSBigBlockSize getBigBlockSize() {
        return bigBlockSize;
    }
    
   /**
    * Write the block's data to an OutputStream
    *
    * @param stream the OutputStream to which the stored data should
    *               be written
    *
    * @throws IOException on problems writing to the specified
    *            stream
    */
   public void writeData(final OutputStream stream) throws IOException {
      // Update the counts and start positions 
      new IntegerField(_bat_count_offset,      _bat_count, _data);
      new IntegerField(_property_count_offset, bigBlockSize.getBigBlockSize() == 512 ? 0 : _property_count, _data);
      new IntegerField(_property_start_offset, _property_start, _data);
      new IntegerField(_sbat_start_offset,     _sbat_start, _data);
      new IntegerField(_sbat_block_count_offset, _sbat_count, _data);
      new IntegerField(_xbat_start_offset,      _xbat_start, _data);
      new IntegerField(_xbat_count_offset,      _xbat_count, _data);
      
      // Write the main data out
      stream.write(_data, 0, 512);
      
      // Now do the padding if needed
      for(int i=POIFSConstants.SMALLER_BIG_BLOCK_SIZE; i<bigBlockSize.getBigBlockSize(); i++) {
         stream.write(0);
      }
   }
}
