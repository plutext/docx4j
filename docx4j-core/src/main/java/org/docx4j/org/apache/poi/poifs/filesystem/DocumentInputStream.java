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

package org.docx4j.org.apache.poi.poifs.filesystem;

import static org.docx4j.org.apache.poi.util.LittleEndianConsts.INT_SIZE;
import static org.docx4j.org.apache.poi.util.LittleEndianConsts.LONG_SIZE;
import static org.docx4j.org.apache.poi.util.LittleEndianConsts.SHORT_SIZE;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

import org.docx4j.org.apache.poi.poifs.property.DocumentProperty;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.docx4j.org.apache.poi.util.LittleEndianInput;

/**
 * This class provides methods to read a DocumentEntry managed by a
 * {@link POIFSFileSystem} instance.
 */
public final class DocumentInputStream extends InputStream implements LittleEndianInput {
    /** returned by read operations if we're at end of document */
    private static final int EOF = -1;

    /** current offset into the Document */
    private int _current_offset;
    /** current block count */
    private int _current_block_count;

    /** current marked offset into the Document (used by mark and reset) */
    private int _marked_offset;
    /** and the block count for it */
    private int _marked_offset_count;

    /** the Document's size */
    private final int _document_size;

    /** have we been closed? */
    private boolean _closed;

    /** the actual Document */
    private final POIFSDocument _document;

    private Iterator<ByteBuffer> _data;
    private ByteBuffer _buffer;

    /**
     * Create an InputStream from the specified DocumentEntry
     *
     * @param document the DocumentEntry to be read
     *
     * @throws IOException if the DocumentEntry cannot be opened (like, maybe it has
     *                been deleted?)
     */
    public DocumentInputStream(DocumentEntry document) throws IOException {
        if (!(document instanceof DocumentNode)) {
            throw new IOException("Cannot open internal document storage, " + document + " not a Document Node");
        }
        _current_offset = 0;
        _current_block_count = 0;
        _marked_offset = 0;
        _marked_offset_count = 0;
        _document_size = document.getSize();
        _closed = false;

        // can't be asserted ... see bug 61300
        // assert (_document_size >= 0) : "Document size can't be < 0";

        DocumentNode doc = (DocumentNode)document;
        DocumentProperty property = (DocumentProperty)doc.getProperty();
        _document = new POIFSDocument(
                property,
                ((DirectoryNode)doc.getParent()).getFileSystem()
        );
        _data = _document.getBlockIterator();
    }

    /**
     * Create an InputStream from the specified Document
     *
     * @param document the Document to be read
     */
    public DocumentInputStream(POIFSDocument document) {
        _current_offset = 0;
        _current_block_count = 0;
        _marked_offset = 0;
        _marked_offset_count = 0;
        _document_size = document.getSize();
        _closed = false;
        _document = document;
        _data = _document.getBlockIterator();
    }

    @Override
    public int available() {
        return remainingBytes();
    }

    /**
     * Helper methods for forbidden api calls
     *
     * @return the bytes remaining until the end of the stream
     */
    private int remainingBytes() {
        if (_closed) {
            throw new IllegalStateException("cannot perform requested operation on a closed stream");
        }
        return _document_size - _current_offset;
    }

    @Override
    public void close() {
        _closed = true;
    }

    /**
     * Tests if this input stream supports the mark and reset methods.
     *
     * @return {@code true} always
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public synchronized void mark(int ignoredReadlimit) {
        _marked_offset = _current_offset;
        _marked_offset_count = Math.max(0, _current_block_count - 1);
    }

    @Override
    public int read() throws IOException {
        dieIfClosed();
        if (atEOD()) {
            return EOF;
        }
        byte[] b = new byte[1];
        int result = read(b, 0, 1);
        return (result == EOF) ? EOF : (b[0] & 0xFF);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        dieIfClosed();
        if (b == null) {
            throw new IllegalArgumentException("buffer must not be null");
        }
        if (off < 0 || len < 0 || b.length < off + len) {
            throw new IndexOutOfBoundsException("can't read past buffer boundaries with off: " + off +
                    ", len: " + len + ", b.length: " + b.length);
        }
        if (len == 0) {
            return 0;
        }
        if (atEOD()) {
            return EOF;
        }
        int limit = Math.min(remainingBytes(), len);
        readFully(b, off, limit);
        return limit;
    }

    /**
     * Repositions this stream to the position at the time the mark() method was
     * last called on this input stream. If mark() has not been called this
     * method repositions the stream to its beginning.
     */
    @Override
    public synchronized void reset() {
        // Special case for reset to the start
        if(_marked_offset == 0 && _marked_offset_count == 0) {
            _current_block_count = _marked_offset_count;
            _current_offset = _marked_offset;
            _data = _document.getBlockIterator();
            _buffer = null;
            return;
        }

        // Start again, then wind on to the required block
        _data = _document.getBlockIterator();
        _current_offset = 0;
        for(int i=0; i<_marked_offset_count; i++) {
           _buffer = _data.next();
           _current_offset += _buffer.remaining();
        }

      _current_block_count = _marked_offset_count;

      // Do we need to position within it?
      if(_current_offset != _marked_offset) {
        // Grab the right block
         _buffer = _data.next();
         _current_block_count++;

        // Skip to the right place in it
         // (It should be positioned already at the start of the block,
         //  we need to move further inside the block)
         int skipBy = _marked_offset - _current_offset;
        _buffer.position(_buffer.position() + skipBy);
      }

      // All done
      _current_offset = _marked_offset;
    }

    @Override
    public long skip(long n) throws IOException {
        dieIfClosed();
        if (n < 0) {
            return 0;
        }
        long new_offset = _current_offset + n;

        if (new_offset < _current_offset) {
            // wrap around in converting a VERY large long to an int
            new_offset = _document_size;
        } else if (new_offset > _document_size) {
            new_offset = _document_size;
        }

        long rval = new_offset - _current_offset;

        // TODO Do this better
        byte[] skip = IOUtils.safelyAllocate(rval, Integer.MAX_VALUE);
        readFully(skip);
        return rval;
    }

    private void dieIfClosed() throws IOException {
        if (_closed) {
            throw new IOException("cannot perform requested operation on a closed stream");
        }
    }

    private boolean atEOD() {
        return _current_offset == _document_size;
    }

    private void checkAvailable(int requestedSize) {
        if (_closed) {
            throw new IllegalStateException("cannot perform requested operation on a closed stream");
        }
        if (requestedSize > _document_size - _current_offset) {
            throw new IllegalStateException("Buffer underrun - requested " + requestedSize
                    + " bytes but " + (_document_size - _current_offset) + " was available");
        }
    }

    @Override
    public void readFully(byte[] buf) {
        readFully(buf, 0, buf.length);
    }

    @Override
    public void readFully(byte[] buf, int off, int len) {
        if (len < 0) {
           throw new IllegalArgumentException("Can't read negative number of bytes, but had: " + len);
        }

        checkAvailable(len);

        int read = 0;
        while(read < len) {
           if(_buffer == null || _buffer.remaining() == 0) {
              _current_block_count++;
              _buffer = _data.next();
           }

           int limit = Math.min(len-read, _buffer.remaining());
           _buffer.get(buf, off+read, limit);
         _current_offset += limit;
           read += limit;
        }
    }

    @Override
    public void readPlain(byte[] buf, int off, int len) {
        readFully(buf, off, len);
    }


    @Override
   public byte readByte() {
      return (byte) readUByte();
   }

   @Override
   public double readDouble() {
      return Double.longBitsToDouble(readLong());
   }

   @Override
    public long readLong() {
        checkAvailable(LONG_SIZE);
        byte[] data = new byte[LONG_SIZE];
        readFully(data, 0, LONG_SIZE);
        return LittleEndian.getLong(data, 0);
    }

   @Override
   public short readShort() {
      checkAvailable(SHORT_SIZE);
      byte[] data = new byte[SHORT_SIZE];
      readFully(data, 0, SHORT_SIZE);
      return LittleEndian.getShort(data);
   }

   @Override
    public int readInt() {
        checkAvailable(INT_SIZE);
      byte[] data = new byte[INT_SIZE];
      readFully(data, 0, INT_SIZE);
      return LittleEndian.getInt(data);
    }

    public long readUInt() {
        int i = readInt();
        return i & 0xFFFFFFFFL;
    }

    @Override
    public int readUShort() {
        checkAvailable(SHORT_SIZE);
      byte[] data = new byte[SHORT_SIZE];
      readFully(data, 0, SHORT_SIZE);
      return LittleEndian.getUShort(data);
    }

    @Override
    public int readUByte() {
        checkAvailable(1);
        byte[] data = new byte[1];
        readFully(data, 0, 1);
        if (data[0] >= 0)
            return data[0];
        return data[0] + 256;
    }
}
