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

import static java.util.Collections.emptyIterator;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.docx4j.org.apache.poi.poifs.common.POIFSConstants;
import org.docx4j.org.apache.poi.poifs.dev.POIFSViewable;
import org.docx4j.org.apache.poi.poifs.property.DocumentProperty;
import org.docx4j.org.apache.poi.util.HexDump;
import org.docx4j.org.apache.poi.util.IOUtils;

/**
 * This class manages a document in the NIO POIFS filesystem.
 * This is the {@link POIFSFileSystem} version.
 */
public final class POIFSDocument implements POIFSViewable, Iterable<ByteBuffer> {

    private DocumentProperty _property;

    private POIFSFileSystem _filesystem;
    private POIFSStream _stream;
    private int _block_size;

    /**
     * Constructor for an existing Document
     */
    public POIFSDocument(DocumentNode document) {
        this((DocumentProperty) document.getProperty(),
                ((DirectoryNode) document.getParent()).getFileSystem());
    }

    /**
     * Constructor for an existing Document
     */
    public POIFSDocument(DocumentProperty property, POIFSFileSystem filesystem) {
        this._property = property;
        this._filesystem = filesystem;

        if (property.getSize() < POIFSConstants.BIG_BLOCK_MINIMUM_DOCUMENT_SIZE) {
            _stream = new POIFSStream(_filesystem.getMiniStore(), property.getStartBlock());
            _block_size = _filesystem.getMiniStore().getBlockStoreBlockSize();
        } else {
            _stream = new POIFSStream(_filesystem, property.getStartBlock());
            _block_size = _filesystem.getBlockStoreBlockSize();
        }
    }

    /**
     * Constructor for a new Document
     *
     * @param name   the name of the POIFSDocument
     * @param stream the InputStream we read data from
     */
    public POIFSDocument(String name, POIFSFileSystem filesystem, InputStream stream)
            throws IOException {
        this._filesystem = filesystem;

        // Store it
        int length = store(stream);

        // Build the property for it
        this._property = new DocumentProperty(name, length);
        _property.setStartBlock(_stream.getStartBlock());
        _property.setDocument(this);
    }

    public POIFSDocument(String name, final int size, POIFSFileSystem filesystem, POIFSWriterListener writer)
            throws IOException {
        this._filesystem = filesystem;

        if (size < POIFSConstants.BIG_BLOCK_MINIMUM_DOCUMENT_SIZE) {
            _stream = new POIFSStream(filesystem.getMiniStore());
            _block_size = _filesystem.getMiniStore().getBlockStoreBlockSize();
        } else {
            _stream = new POIFSStream(filesystem);
            _block_size = _filesystem.getBlockStoreBlockSize();
        }

        this._property = new DocumentProperty(name, size);
        _property.setStartBlock(_stream.getStartBlock());
        _property.setDocument(this);

        try (DocumentOutputStream os = new DocumentOutputStream(this, size)) {
            POIFSDocumentPath path = new POIFSDocumentPath(name.split("\\\\"));
            String docName = path.getComponent(path.length() - 1);
            POIFSWriterEvent event = new POIFSWriterEvent(os, path, docName, size);
            writer.processPOIFSWriterEvent(event);
        }
    }

    /**
     * Stores the given data for this Document
     */
    private int store(InputStream stream) throws IOException {
        final int bigBlockSize = POIFSConstants.BIG_BLOCK_MINIMUM_DOCUMENT_SIZE;
        BufferedInputStream bis = new BufferedInputStream(stream, bigBlockSize + 1);
        bis.mark(bigBlockSize);

        // Do we need to store as a mini stream or a full one?
        long streamBlockSize = IOUtils.skipFully(bis, bigBlockSize);
        if (streamBlockSize < bigBlockSize) {
            _stream = new POIFSStream(_filesystem.getMiniStore());
            _block_size = _filesystem.getMiniStore().getBlockStoreBlockSize();
        } else {
            _stream = new POIFSStream(_filesystem);
            _block_size = _filesystem.getBlockStoreBlockSize();
        }

        // start from the beginning
        bis.reset();

        // Store it
        final long length;
        try (OutputStream os = _stream.getOutputStream()) {
            length = IOUtils.copy(bis, os);

            // Pad to the end of the block with -1s
            int usedInBlock = (int) (length % _block_size);
            if (usedInBlock != 0 && usedInBlock != _block_size) {
                int toBlockEnd = _block_size - usedInBlock;
                byte[] padding = IOUtils.safelyAllocate(toBlockEnd, POIFSFileSystem.getMaxRecordLength());
                Arrays.fill(padding, (byte) 0xFF);
                os.write(padding);
            }
        }

        return Math.toIntExact(length);
    }

    /**
     * Frees the underlying stream and property
     */
    void free() throws IOException {
        _stream.free();
        _property.setStartBlock(POIFSConstants.END_OF_CHAIN);
    }

    POIFSFileSystem getFileSystem() {
        return _filesystem;
    }

    int getDocumentBlockSize() {
        return _block_size;
    }

    @Override
    public Iterator<ByteBuffer> iterator() {
        return getBlockIterator();
    }

    Iterator<ByteBuffer> getBlockIterator() {
        return (getSize() > 0 ? _stream : Collections.<ByteBuffer>emptyList()).iterator();
    }

    /**
     * @return size of the document
     */
    public int getSize() {
        return _property.getSize();
    }

    public void replaceContents(InputStream stream) throws IOException {
        free();
        int size = store(stream);
        _property.setStartBlock(_stream.getStartBlock());
        _property.updateSize(size);
    }

    /**
     * @return the instance's DocumentProperty
     */
    DocumentProperty getDocumentProperty() {
        return _property;
    }

    /**
     * Get an array of objects, some of which may implement POIFSViewable
     *
     * @return an array of Object; may not be null, but may be empty
     */
    public Object[] getViewableArray() {
        String result = "<NO DATA>";

        if (getSize() > 0) {
            // Get all the data into a single array
            byte[] data = IOUtils.safelyAllocate(getSize(), POIFSFileSystem.getMaxRecordLength());
            int offset = 0;
            for (ByteBuffer buffer : _stream) {
                int length = Math.min(_block_size, data.length - offset);
                buffer.get(data, offset, length);
                offset += length;
            }

            result = HexDump.dump(data, 0, 0);
        }

        return new String[]{result};
    }

    /**
     * Get an Iterator of objects, some of which may implement POIFSViewable
     *
     * @return an Iterator; may not be null, but may have an empty back end
     * store
     */
    public Iterator<Object> getViewableIterator() {
        return emptyIterator();
    }

    /**
     * Give viewers a hint as to whether to call getViewableArray or
     * getViewableIterator
     *
     * @return <code>true</code> if a viewer should call getViewableArray,
     * <code>false</code> if a viewer should call getViewableIterator
     */
    public boolean preferArray() {
        return true;
    }

    /**
     * Provides a short description of the object, to be used when a
     * POIFSViewable object has not provided its contents.
     *
     * @return short description
     */
    public String getShortDescription() {

        return "Document: \"" + _property.getName() + "\" size = " + getSize();
    }
}
