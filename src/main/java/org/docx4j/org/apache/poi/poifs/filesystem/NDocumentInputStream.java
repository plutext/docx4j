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

package org.docx4j.org.apache.poi.poifs.filesystem;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

import org.docx4j.org.apache.poi.poifs.property.DocumentProperty;
import org.docx4j.org.apache.poi.util.LittleEndian;

/**
 * This class provides methods to read a DocumentEntry managed by a
 * {@link NPOIFSFileSystem} instance.
 */
public final class NDocumentInputStream extends DocumentInputStream {
    /** current offset into the Document */
    private int _current_offset;
    /** current block count */
    private int _current_block_count;

    /** current marked offset into the Document (used by mark and reset) */
    private int _marked_offset;
    /** and the block count for it */
    private int _marked_offset_count;

    /** the Document's size */
    private int _document_size;

    /** have we been closed? */
    private boolean _closed;

    /** the actual Document */
    private NPOIFSDocument _document;

    private Iterator<ByteBuffer> _data;
    private ByteBuffer _buffer;

    /**
     * Create an InputStream from the specified DocumentEntry
     * 
     * @param document the DocumentEntry to be read
     * 
     * @exception IOException if the DocumentEntry cannot be opened (like, maybe it has
     *                been deleted?)
     */
    public NDocumentInputStream(DocumentEntry document) throws IOException {
        if (!(document instanceof DocumentNode)) {
            throw new IOException("Cannot open internal document storage, " + document + " not a Document Node");
        }
        _current_offset = 0;
        _current_block_count = 0;
        _marked_offset = 0;
        _marked_offset_count = 0;
        _document_size = document.getSize();
        _closed = false;

        DocumentNode doc = (DocumentNode)document;
        DocumentProperty property = (DocumentProperty)doc.getProperty();
        _document = new NPOIFSDocument(
                property, 
                ((DirectoryNode)doc.getParent()).getNFileSystem()
        );
        _data = _document.getBlockIterator();
    }

    /**
     * Create an InputStream from the specified Document
     * 
     * @param document the Document to be read
     */
    public NDocumentInputStream(NPOIFSDocument document) {
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
        if (_closed) {
            throw new IllegalStateException("cannot perform requested operation on a closed stream");
        }
        return _document_size - _current_offset;
    }

    @Override
    public void close() {
        _closed = true;
    }

    @Override
    public void mark(int ignoredReadlimit) {
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
        if(result >= 0) {
            if(b[0] < 0) {
                return b[0]+256;
            }
            return b[0];
        }
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        dieIfClosed();
        if (b == null) {
            throw new IllegalArgumentException("buffer must not be null");
        }
        if (off < 0 || len < 0 || b.length < off + len) {
            throw new IndexOutOfBoundsException("can't read past buffer boundaries");
        }
        if (len == 0) {
            return 0;
        }
        if (atEOD()) {
            return EOF;
        }
        int limit = Math.min(available(), len);
        readFully(b, off, limit);
        return limit;
    }

    /**
     * Repositions this stream to the position at the time the mark() method was
     * last called on this input stream. If mark() has not been called this
     * method repositions the stream to its beginning.
     */
    @Override
    public void reset() {
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
		int new_offset = _current_offset + (int) n;

		if (new_offset < _current_offset) {
			// wrap around in converting a VERY large long to an int
			new_offset = _document_size;
		} else if (new_offset > _document_size) {
			new_offset = _document_size;
		}
		
		long rval = new_offset - _current_offset;
		
		// TODO Do this better
		byte[] skip = new byte[(int)rval];
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

	private void checkAvaliable(int requestedSize) {
		if (_closed) {
			throw new IllegalStateException("cannot perform requested operation on a closed stream");
		}
		if (requestedSize > _document_size - _current_offset) {
			throw new RuntimeException("Buffer underrun - requested " + requestedSize
					+ " bytes but " + (_document_size - _current_offset) + " was available");
		}
	}

   @Override
	public void readFully(byte[] buf, int off, int len) {
		checkAvaliable(len);

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
   public byte readByte() {
      return (byte) readUByte();
   }

   @Override
   public double readDouble() {
      return Double.longBitsToDouble(readLong());
   }

   @Override
	public long readLong() {
		checkAvaliable(SIZE_LONG);
		byte[] data = new byte[SIZE_LONG];
		readFully(data, 0, SIZE_LONG);
		return LittleEndian.getLong(data, 0);
	}

   @Override
   public short readShort() {
      checkAvaliable(SIZE_SHORT);
      byte[] data = new byte[SIZE_SHORT];
      readFully(data, 0, SIZE_SHORT);
      return LittleEndian.getShort(data);
   }

   @Override
	public int readInt() {
		checkAvaliable(SIZE_INT);
      byte[] data = new byte[SIZE_INT];
      readFully(data, 0, SIZE_INT);
      return LittleEndian.getInt(data);
	}

   @Override
	public int readUShort() {
		checkAvaliable(SIZE_SHORT);
      byte[] data = new byte[SIZE_SHORT];
      readFully(data, 0, SIZE_SHORT);
      return LittleEndian.getUShort(data);
	}

   @Override
	public int readUByte() {
		checkAvaliable(1);
      byte[] data = new byte[1];
      readFully(data, 0, 1);
      if(data[0] >= 0)
         return data[0];
      return data[0] + 256;
	}
}
