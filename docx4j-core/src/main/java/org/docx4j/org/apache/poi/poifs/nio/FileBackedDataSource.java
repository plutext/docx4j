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

package org.docx4j.org.apache.poi.poifs.nio;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.IdentityHashMap;

import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.utils.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A POIFS {@link DataSource} backed by a File
 */
public class FileBackedDataSource extends DataSource implements Closeable {

	protected static Logger log = LoggerFactory.getLogger(FileBackedDataSource.class);	
	
    private final FileChannel channel;
    private Long channelSize;

    private final boolean writable;
    private final boolean closeChannelOnClose;
    // remember file base, which needs to be closed too
    private final RandomAccessFile srcFile;

    // Buffers which map to a file-portion are not closed automatically when the Channel is closed
    // therefore we need to keep the list of mapped buffers and do some ugly reflection to try to
    // clean the buffer during close().
    // See https://bz.apache.org/bugzilla/show_bug.cgi?id=58480,
    private final IdentityHashMap<ByteBuffer,ByteBuffer> buffersToClean = new IdentityHashMap<>();

    public FileBackedDataSource(File file) throws FileNotFoundException {
        this(newSrcFile(file, "r"), true);
    }

    public FileBackedDataSource(File file, boolean readOnly) throws FileNotFoundException {
        this(newSrcFile(file, readOnly ? "r" : "rw"), readOnly);
    }

    public FileBackedDataSource(RandomAccessFile srcFile, boolean readOnly) {
        this(srcFile, srcFile.getChannel(), readOnly, false);
    }

    public FileBackedDataSource(FileChannel channel, boolean readOnly) {
        this(channel, readOnly, true);
    }

    /**
     * @since 5.1.0
     */
    public FileBackedDataSource(FileChannel channel, boolean readOnly, boolean closeChannelOnClose) {
        this(null, channel, readOnly, closeChannelOnClose);
    }

    private FileBackedDataSource(RandomAccessFile srcFile, FileChannel channel, boolean readOnly, boolean closeChannelOnClose) {
        this.srcFile = srcFile;
        this.channel = channel;
        this.writable = !readOnly;
        this.closeChannelOnClose = closeChannelOnClose;
    }


    public boolean isWriteable() {
        return this.writable;
    }

    public FileChannel getChannel() {
        return this.channel;
    }

    @Override
    public ByteBuffer read(int length, long position) throws IOException {
        if (position >= size()) {
            throw new IndexOutOfBoundsException("Position " + position + " past the end of the file");
        }

        // TODO Could we do the read-only case with MapMode.PRIVATE instead?
        // See https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/channels/FileChannel.MapMode.html
        // Or should we have 3 modes instead of the current boolean -
        //  read-write, read-only, read-to-write-elsewhere?

        // Do we read or map (for read/write)?
        ByteBuffer dst;
        if (writable) {
            dst = channel.map(FileChannel.MapMode.READ_WRITE, position, length);

            // remember this buffer for cleanup
            buffersToClean.put(dst,dst);
        } else {
            channel.position(position);

            // allocate the buffer on the heap if we cannot map the data in directly
            dst = ByteBuffer.allocate(length);

            // Read the contents and check that we could read some data
            int worked = IOUtils.readFully(channel, dst);
            if (worked == -1) {
                throw new IndexOutOfBoundsException("Position " + position + " past the end of the file");
            }
        }

        // make it ready for reading
        dst.position(0);

        // All done
        return dst;
    }

    @Override
    public void write(ByteBuffer src, long position) throws IOException {
        channel.write(src, position);

        // we have to re-read size if we write "after" the recorded one
        if(channelSize != null && position >= channelSize) {
            channelSize = null;
        }
    }

    @Override
    public void copyTo(OutputStream stream) throws IOException {
        // Wrap the OutputSteam as a channel
        try (WritableByteChannel out = Channels.newChannel(stream)) {
            // Now do the transfer
            channel.transferTo(0, channel.size(), out);
        }
    }

    @Override
    public long size() throws IOException {
        // this is called often and profiling showed that channel.size()
        // was taking a large part of processing-time, so we only read it
        // once
        if(channelSize == null) {
            channelSize = channel.size();
        }
        return channelSize;
    }

    public void releaseBuffer(ByteBuffer buffer) {
        ByteBuffer previous = buffersToClean.remove(buffer);
        if (previous != null) {
            unmap(previous);
        }
    }

    @Override
    public void close() throws IOException {
        // also ensure that all buffers are unmapped so we do not keep files locked on Windows
        // We consider it a bug if a Buffer is still in use now!
        buffersToClean.forEach((k,v) -> unmap(v));
        buffersToClean.clear();

        if (srcFile != null) {
            // see http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4796385
            srcFile.close();
        } else if (closeChannelOnClose) {
            channel.close();
        }
    }

    private static RandomAccessFile newSrcFile(File file, String mode) throws FileNotFoundException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.toString());
        }
        return new RandomAccessFile(file, mode);
    }

    // need to use reflection to avoid depending on the sun.nio internal API
    // unfortunately this might break silently with newer/other Java implementations,
    // but we at least have unit-tests which will indicate this when run on Windows
    private static void unmap(final ByteBuffer buffer) {
        // not necessary for HeapByteBuffer, avoid lots of log-output on this class
        if (buffer.getClass().getName().endsWith("HeapByteBuffer")) {
            return;
        }

        if (CleanerUtil.UNMAP_SUPPORTED) {
            try {
                CleanerUtil.getCleaner().freeBuffer(buffer);
            } catch (IOException e) {
                log.warn("Failed to unmap the buffer", e);
            }
        } else {
            log.debug(CleanerUtil.UNMAP_NOT_SUPPORTED_REASON);
        }
    }
}
