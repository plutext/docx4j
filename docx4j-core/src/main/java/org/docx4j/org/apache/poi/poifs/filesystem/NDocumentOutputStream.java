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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.docx4j.org.apache.poi.poifs.common.POIFSConstants;
import org.docx4j.org.apache.poi.poifs.property.DocumentProperty;

/**
 * This class provides methods to write a DocumentEntry managed by a
 * {@link NPOIFSFileSystem} instance.
 */
public final class NDocumentOutputStream extends OutputStream {
	/** the Document's size */
	private int _document_size;

	/** have we been closed? */
	private boolean _closed;

	/** the actual Document */
	private NPOIFSDocument _document;
	/** and its Property */
	private DocumentProperty _property;
	
	/** our buffer, when null we're into normal blocks */
	private ByteArrayOutputStream _buffer = 
	        new ByteArrayOutputStream(POIFSConstants.BIG_BLOCK_MINIMUM_DOCUMENT_SIZE);
	
	/** our main block stream, when we're into normal blocks */
	private NPOIFSStream _stream;
	private OutputStream _stream_output;
	
	/**
	 * Create an OutputStream from the specified DocumentEntry.
	 * The specified entry will be emptied.
	 * 
	 * @param document the DocumentEntry to be written
	 */
	public NDocumentOutputStream(DocumentEntry document) throws IOException {
		if (!(document instanceof DocumentNode)) {
			throw new IOException("Cannot open internal document storage, " + document + " not a Document Node");
		}
		_document_size = 0;
		_closed = false;
		
		_property = (DocumentProperty)((DocumentNode)document).getProperty();
		
		_document = new NPOIFSDocument((DocumentNode)document);
		_document.free();
	}
	
	/**
	 * Create an OutputStream to create the specified new Entry
	 * 
	 * @param parent Where to create the Entry
	 * @param name Name of the new entry
	 */
	public NDocumentOutputStream(DirectoryEntry parent, String name) throws IOException {
        if (!(parent instanceof DirectoryNode)) {
            throw new IOException("Cannot open internal directory storage, " + parent + " not a Directory Node");
        }
        _document_size = 0;
        _closed = false;

        // Have an empty one created for now
        DocumentEntry doc = parent.createDocument(name, new ByteArrayInputStream(new byte[0]));
        _property = (DocumentProperty)((DocumentNode)doc).getProperty();
        _document = new NPOIFSDocument((DocumentNode)doc);
	}
	
    private void dieIfClosed() throws IOException {
        if (_closed) {
            throw new IOException("cannot perform requested operation on a closed stream");
        }
    }
    
    private void checkBufferSize() throws IOException {
        // Have we gone over the mini stream limit yet?
        if (_buffer.size() > POIFSConstants.BIG_BLOCK_MINIMUM_DOCUMENT_SIZE) {
            // Will need to be in the main stream
            byte[] data = _buffer.toByteArray();
            _buffer = null;
            write(data, 0, data.length);
        } else {
            // So far, mini stream will work, keep going
        }
    }

    public void write(int b) throws IOException {
        dieIfClosed();
        
        if (_buffer != null) {
            _buffer.write(b);
            checkBufferSize();
        } else {
            write(new byte[] { (byte)b });
        }
    }

    public void write(byte[] b) throws IOException {
        dieIfClosed();
        
        if (_buffer != null) {
            _buffer.write(b);
            checkBufferSize();
        } else {
            write(b, 0, b.length);
        }
    }

    public void write(byte[] b, int off, int len) throws IOException {
        dieIfClosed();
        
        if (_buffer != null) {
            _buffer.write(b, off, len);
            checkBufferSize();
        } else {
            if (_stream == null) {
                _stream = new NPOIFSStream(_document.getFileSystem());
                _stream_output = _stream.getOutputStream();
            }
            _stream_output.write(b, off, len);
            _document_size += len;
        }
    }

    public void close() throws IOException {
        // Do we have a pending buffer for the mini stream?
        if (_buffer != null) {
            // It's not much data, so ask NPOIFSDocument to do it for us
            _document.replaceContents(new ByteArrayInputStream(_buffer.toByteArray()));
        }
        else {
            // We've been writing to the stream as we've gone along
            // Update the details on the property now
            _stream_output.close();
            _property.updateSize(_document_size);
            _property.setStartBlock(_stream.getStartBlock());
        }
        
        // No more!
        _closed = true;
    }
}
