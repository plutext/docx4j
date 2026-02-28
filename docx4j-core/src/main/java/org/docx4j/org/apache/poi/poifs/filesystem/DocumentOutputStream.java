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
import java.io.OutputStream;

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.docx4j.org.apache.poi.poifs.common.POIFSConstants;
import org.docx4j.org.apache.poi.poifs.property.DocumentProperty;

/**
 * This class provides methods to write a DocumentEntry managed by a
 * {@link POIFSFileSystem} instance.
 */
public final class DocumentOutputStream extends OutputStream {
    /** the Document's size, i.e. the size of the big block data - mini block data is cached and not counted */
    private int _document_size = 0;

    /** have we been closed? */
    private boolean _closed = false;

    /** the actual Document */
    private final POIFSDocument _document;
    /** and its Property */
    private final DocumentProperty _property;

    /** our buffer, when null we're into normal blocks */
    private UnsynchronizedByteArrayOutputStream _buffer =
            UnsynchronizedByteArrayOutputStream.builder().setBufferSize(POIFSConstants.BIG_BLOCK_MINIMUM_DOCUMENT_SIZE).get();

    /** our main block stream, when we're into normal blocks */
    private POIFSStream _stream;
    private OutputStream _stream_output;

    /** a write limit or -1 if unlimited */
    private final long _limit;


    /**
     * Create an OutputStream from the specified DocumentEntry.
     * The specified entry will be emptied.
     *
     * @param document the DocumentEntry to be written
     */
    public DocumentOutputStream(DocumentEntry document) throws IOException {
        this(document, -1);
    }

    /**
     * Create an OutputStream to create the specified new Entry
     *
     * @param parent Where to create the Entry
     * @param name Name of the new entry
     */
    public DocumentOutputStream(DirectoryEntry parent, String name) throws IOException {
        this(createDocument(parent, name), -1);
    }

    /**
     * Create a DocumentOutputStream
     *
     * @param document the DocumentEntry to which the data is actually written
     * @param limit the maximum number of bytes that can be written
     */
    DocumentOutputStream(DocumentEntry document, long limit) throws IOException {
        this(getDocument(document), limit);
    }

    DocumentOutputStream(POIFSDocument document, long limit) throws IOException {
        _document = document;
        _document.free();

        _property = document.getDocumentProperty();

        _limit   = limit;
    }

    private static POIFSDocument getDocument(DocumentEntry document) throws IOException {
        if (!(document instanceof DocumentNode)) {
            throw new IOException("Cannot open internal document storage, " + document + " not a Document Node");
        }
        return new POIFSDocument((DocumentNode)document);
    }

    private static DocumentEntry createDocument(DirectoryEntry parent, String name) throws IOException {
        if (!(parent instanceof DirectoryNode)) {
            throw new IOException("Cannot open internal directory storage, " + parent + " not a Directory Node");
        }

        // Have an empty one created for now
        return parent.createDocument(name, UnsynchronizedByteArrayInputStream.builder().setByteArray(new byte[0]).get());
    }

    private void checkBufferSize() throws IOException {
        // Have we gone over the mini stream limit yet?
        if (_buffer.size() > POIFSConstants.BIG_BLOCK_MINIMUM_DOCUMENT_SIZE) {
            // Will need to be in the main stream
            byte[] data = _buffer.toByteArray();
            _buffer = null;
            write(data, 0, data.length);
        }
        // otherwise mini stream will work, keep going
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[] { (byte)b }, 0, 1);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (_closed) {
            throw new IOException("cannot perform requested operation on a closed stream");
        }
        if (_limit > -1 && (size() + len) > _limit) {
            throw new IOException("tried to write too much data");
        }

        if (_buffer != null) {
            _buffer.write(b, off, len);
            checkBufferSize();
        } else {
            if (_stream == null) {
                _stream = new POIFSStream(_document.getFileSystem());
                _stream_output = _stream.getOutputStream();
            }
            _stream_output.write(b, off, len);
            _document_size += len;
        }
    }

    @Override
    public void close() throws IOException {
        // Do we have a pending buffer for the mini stream?
        if (_buffer != null) {
            // It's not much data, so ask POIFSDocument to do it for us
            _document.replaceContents(_buffer.toInputStream());
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

    /**
     * @return the amount of written bytes
     */
    public long size() {
        return _document_size + (_buffer == null ? 0L : _buffer.size());
    }
}