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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.docx4j.org.apache.poi.EmptyFileException;
import org.docx4j.org.apache.poi.poifs.common.POIFSBigBlockSize;
import org.docx4j.org.apache.poi.poifs.common.POIFSConstants;
import org.docx4j.org.apache.poi.poifs.dev.POIFSViewable;
import org.docx4j.org.apache.poi.poifs.nio.ByteArrayBackedDataSource;
import org.docx4j.org.apache.poi.poifs.nio.DataSource;
import org.docx4j.org.apache.poi.poifs.nio.FileBackedDataSource;
import org.docx4j.org.apache.poi.poifs.property.DirectoryProperty;
import org.docx4j.org.apache.poi.poifs.property.DocumentProperty;
import org.docx4j.org.apache.poi.poifs.property.PropertyTable;
import org.docx4j.org.apache.poi.poifs.storage.BATBlock;
import org.docx4j.org.apache.poi.poifs.storage.BATBlock.BATBlockAndIndex;
import org.docx4j.org.apache.poi.poifs.storage.HeaderBlock;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.Internal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>This is the main class of the POIFS system; it manages the entire
 * life cycle of the filesystem.</p>
 * <p>This is the new NIO version, which uses less memory</p>
 */

public class POIFSFileSystem extends BlockStore
        implements POIFSViewable, Closeable {
    //arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_RECORD_LENGTH = 100_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    private static final int MAX_ALLOCATION_SIZE = 250_000_000;

	protected static Logger log = LoggerFactory.getLogger(POIFSFileSystem.class);	

    /**
     * Maximum number size (in blocks) of the allocation table as supported by
     * POI.
     * <p>
     * This constant has been chosen to help POI identify corrupted data in the
     * header block (rather than crash immediately with {@link OutOfMemoryError}
     * ). It's not clear if the compound document format actually specifies any
     * upper limits. For files with 512 byte blocks, having an allocation table
     * of 65,335 blocks would correspond to a total file size of 4GB. Needless
     * to say, POI probably cannot handle files anywhere near that size.
     */
    private static final int MAX_BLOCK_COUNT = 65535;

    private POIFSMiniStore _mini_store;
    private PropertyTable _property_table;
    private final List<BATBlock> _xbat_blocks;
    private final List<BATBlock> _bat_blocks;
    private HeaderBlock _header;
    private DirectoryNode _root;

    protected DataSource _data;

    /**
     * What big block size the file uses. Most files
     * use 512 bytes, but a few use 4096
     */
    private POIFSBigBlockSize bigBlockSize =
            POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS;

    /**
     * @param length the max record length allowed for POIFSFileSystem
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for POIFSFileSystem
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    private POIFSFileSystem(boolean newFS) {
        _header = new HeaderBlock(bigBlockSize);
        _property_table = new PropertyTable(_header);
        _mini_store = new POIFSMiniStore(this, _property_table.getRoot(), new ArrayList<>(), _header);
        _xbat_blocks = new ArrayList<>();
        _bat_blocks = new ArrayList<>();
        _root = null;

        if (newFS) {
            createNewDataSource();
        }
    }

    protected void createNewDataSource() {
        // Data needs to initially hold just the header block,
        //  a single bat block, and an empty properties section
        long blockSize = Math.multiplyExact(bigBlockSize.getBigBlockSize(), 3L);
        _data = new ByteArrayBackedDataSource(IOUtils.safelyAllocate(blockSize, MAX_RECORD_LENGTH));
    }

    /**
     * Constructor, intended for writing
     */
    public POIFSFileSystem() {
        this(true);

        // Reserve block 0 for the start of the Properties Table
        // Create a single empty BAT, at pop that at offset 1
        _header.setBATCount(1);
        _header.setBATArray(new int[]{1});
        BATBlock bb = BATBlock.createEmptyBATBlock(bigBlockSize, false);
        bb.setOurBlockIndex(1);
        _bat_blocks.add(bb);

        setNextBlock(0, POIFSConstants.END_OF_CHAIN);
        setNextBlock(1, POIFSConstants.FAT_SECTOR_BLOCK);

        _property_table.setStartBlock(0);
    }

    /**
     * <p>Creates a POIFSFileSystem from a {@code File}. This uses less memory than
     * creating from an {@code InputStream}. The File will be opened read-only</p>
     *
     * <p>Note that with this constructor, you will need to call {@link #close()}
     * when you're done to have the underlying file closed, as the file is
     * kept open during normal operation to read the data out.</p>
     *
     * @param file the File from which to read the data
     * @throws IOException on errors reading, or on invalid data
     */
    public POIFSFileSystem(File file)
            throws IOException {
        this(file, true);
    }

    /**
     * <p>Creates a POIFSFileSystem from a {@code File}. This uses less memory than
     * creating from an {@code InputStream}.</p>
     *
     * <p>Note that with this constructor, you will need to call {@link #close()}
     * when you're done to have the underlying file closed, as the file is
     * kept open during normal operation to read the data out.</p>
     *
     * @param file     the File from which to read or read/write the data
     * @param readOnly whether the POIFileSystem will only be used in read-only mode
     * @throws IOException on errors reading, or on invalid data
     */
    public POIFSFileSystem(File file, boolean readOnly)
            throws IOException {
        this(null, file, readOnly, true, true);
    }

    /**
     * <p>Creates a POIFSFileSystem from an open {@code FileChannel}. This uses
     * less memory than creating from an {@code InputStream}. The stream will
     * be used in read-only mode.</p>
     *
     * <p>Note that with this constructor, you will need to call {@link #close()}
     * when you're done to have the underlying Channel closed, as the channel is
     * kept open during normal operation to read the data out. For legacy reasons,
     * the channel is not closed if there is an error creating the POIFSFileSystem.</p>
     *
     * @param channel the FileChannel from which to read the data
     * @throws IOException on errors reading, or on invalid data
     * @see #POIFSFileSystem(FileChannel, boolean, boolean) this constructor gives more control over whether to
     * close the provided channel
     */
    public POIFSFileSystem(FileChannel channel)
            throws IOException {
        this(channel, true);
    }

    /**
     * <p>Creates a POIFSFileSystem from an open {@code FileChannel}. This uses
     * less memory than creating from an {@code InputStream}.</p>
     *
     * <p>Note that with this constructor, you will need to call {@link #close()}
     * when you're done to have the underlying Channel closed, as the channel is
     * kept open during normal operation to read the data out. For legacy reasons,
     * the channel is not closed if there is an error creating the POIFSFileSystem.</p>
     *
     * @param channel  the FileChannel from which to read or read/write the data
     * @param readOnly whether the POIFileSystem will only be used in read-only mode
     * @throws IOException on errors reading, or on invalid data
     * @see #POIFSFileSystem(FileChannel, boolean, boolean) this constructor gives more control over whether to
     * close the provided channel
     */
    public POIFSFileSystem(FileChannel channel, boolean readOnly)
            throws IOException {
        this(channel, null, readOnly, false, true);
    }

    /**
     * <p>Creates a POIFSFileSystem from an open {@code FileChannel}. This uses
     * less memory than creating from an {@code InputStream}.</p>
     *
     * <p>Note that with this constructor, you will need to call {@link #close()}
     * when you're done to have the underlying resources closed. The <code>closeChannel</code>
     * parameter controls whether the provided channel is closed.</p>
     *
     * @param channel      the FileChannel from which to read or read/write the data
     * @param readOnly     whether the POIFileSystem will only be used in read-only mode
     * @param closeChannel whether the provided FileChannel should be closed when
     *                     {@link #close()} is called, or when this constructor throws
     *                     an exception
     * @throws IOException on errors reading, or on invalid data
     * @since 5.1.0
     */
    public POIFSFileSystem(FileChannel channel, boolean readOnly, boolean closeChannel)
            throws IOException {
        this(channel, null, readOnly, closeChannel, closeChannel);
    }

    @SuppressWarnings("java:S2095")
    private POIFSFileSystem(FileChannel channel, File srcFile, boolean readOnly, boolean closeChannelOnError,
                            boolean closeChannelOnClose) throws IOException {
        this(false);

        try {
            // Initialize the datasource
            if (srcFile != null) {
                if (srcFile.length() == 0)
                    throw new EmptyFileException(srcFile);

                FileBackedDataSource d = new FileBackedDataSource(srcFile, readOnly);
                channel = d.getChannel();
                _data = d;
            } else {
                _data = new FileBackedDataSource(channel, readOnly, closeChannelOnClose);
            }

            // Get the header
            ByteBuffer headerBuffer = ByteBuffer.allocate(POIFSConstants.SMALLER_BIG_BLOCK_SIZE);
            IOUtils.readFully(channel, headerBuffer);

            // Have the header processed
            _header = new HeaderBlock(headerBuffer);

            // Now process the various entries
            readCoreContents();
        } catch (IOException | RuntimeException e) {
            // Comes from Iterators etc.
            // TODO Decide if we can handle these better whilst
            //  still sticking to the iterator contract
            if (closeChannelOnError && channel != null) {
                channel.close();
            }
            throw e;
        }
    }

    /**
     * Create a POIFSFileSystem from an {@code InputStream}.  Normally the stream is read until
     * EOF.  The stream is always closed.
     * <p>
     * Some streams are usable after reaching EOF (typically those that return {@code true}
     * for {@code markSupported()}).  In the unlikely case that the caller has such a stream
     * <i>and</i> needs to use it after this constructor completes, a work around is to wrap the
     * stream in order to trap the {@code close()} call.  A convenience method (
     * {@code createNonClosingInputStream()}) has been provided for this purpose:
     * <pre>
     * InputStream wrappedStream = POIFSFileSystem.createNonClosingInputStream(is);
     * HSSFWorkbook wb = new HSSFWorkbook(wrappedStream);
     * is.reset();
     * doSomethingElse(is);
     * </pre>
     * Note also the special case of {@code ByteArrayInputStream} for which the {@code close()}
     * method does nothing.
     * <pre>
     * ByteArrayInputStream bais = ...
     * HSSFWorkbook wb = new HSSFWorkbook(bais); // calls bais.close() !
     * bais.reset(); // no problem
     * doSomethingElse(bais);
     * </pre>
     *
     * @param stream the InputStream from which to read the data
     * @throws IOException on errors reading, or on invalid data
     */

    public POIFSFileSystem(InputStream stream)
            throws IOException {
        this(false);

        boolean success = false;
        try (ReadableByteChannel channel = Channels.newChannel(stream)) {
            // Turn our InputStream into something NIO based

            // Get the header
            ByteBuffer headerBuffer = ByteBuffer.allocate(POIFSConstants.SMALLER_BIG_BLOCK_SIZE);
            IOUtils.readFully(channel, headerBuffer);

            // Have the header processed
            _header = new HeaderBlock(headerBuffer);

            // Sanity check the block count
            sanityCheckBlockCount(_header.getBATCount());

            // We need to buffer the whole file into memory when
            //  working with an InputStream.
            // The max possible size is when each BAT block entry is used
            long maxSize = BATBlock.calculateMaximumSize(_header);
            if (maxSize > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Unable read a >2gb file via an InputStream");
            }

            // don't allow huge allocations with invalid header-values
            IOUtils.safelyAllocateCheck(maxSize, MAX_ALLOCATION_SIZE);

            ByteBuffer data = ByteBuffer.allocate((int) maxSize);

            // Copy in the header
            headerBuffer.position(0);
            data.put(headerBuffer);
            data.position(headerBuffer.capacity());

            // Now read the rest of the stream
            IOUtils.readFully(channel, data);
            success = true;

            // Turn it into a DataSource
            _data = new ByteArrayBackedDataSource(data.array(), data.position());
        } finally {
            // As per the constructor contract, always close the stream
            closeInputStream(stream, success);
        }

        // Now process the various entries
        readCoreContents();
    }

    /**
     * @param stream  the stream to be closed
     * @param success {@code false} if an exception is currently being thrown in the calling method
     */
    private void closeInputStream(InputStream stream, boolean success) {
        try {
            stream.close();
        } catch (IOException e) {
            if (success) {
                throw new IllegalStateException(e);
            }
            // else not success? Try block did not complete normally
            // just print stack trace and leave original ex to be thrown
            log.error("can't close input stream", e);
        }
    }

    /**
     * Read and process the PropertiesTable and the
     * FAT / XFAT blocks, so that we're ready to
     * work with the file
     */
    private void readCoreContents() throws IOException {
        // Grab the block size
        bigBlockSize = _header.getBigBlockSize();

        // Each block should only ever be used by one of the
        //  FAT, XFAT or Property Table. Ensure it does
        ChainLoopDetector loopDetector = getChainLoopDetector();

        // Read the FAT blocks
        for (int fatAt : _header.getBATArray()) {
            readBAT(fatAt, loopDetector);
        }

        // Work out how many FAT blocks remain in the XFATs
        int remainingFATs = _header.getBATCount() - _header.getBATArray().length;

        // Now read the XFAT blocks, and the FATs within them
        BATBlock xfat;
        int nextAt = _header.getXBATIndex();
        for (int i = 0; i < _header.getXBATCount(); i++) {
            loopDetector.claim(nextAt);
            ByteBuffer fatData = getBlockAt(nextAt);
            xfat = BATBlock.createBATBlock(bigBlockSize, fatData);
            xfat.setOurBlockIndex(nextAt);
            nextAt = xfat.getValueAt(bigBlockSize.getXBATEntriesPerBlock());
            _xbat_blocks.add(xfat);

            // Process all the (used) FATs from this XFAT
            int xbatFATs = Math.min(remainingFATs, bigBlockSize.getXBATEntriesPerBlock());
            for (int j = 0; j < xbatFATs; j++) {
                int fatAt = xfat.getValueAt(j);
                if (fatAt == POIFSConstants.UNUSED_BLOCK || fatAt == POIFSConstants.END_OF_CHAIN) break;
                readBAT(fatAt, loopDetector);
            }
            remainingFATs -= xbatFATs;
        }

        // We're now able to load steams
        // Use this to read in the properties
        _property_table = new PropertyTable(_header, this);

        // Finally read the Small Stream FAT (SBAT) blocks
        BATBlock sfat;
        List<BATBlock> sbats = new ArrayList<>();
        _mini_store = new POIFSMiniStore(this, _property_table.getRoot(), sbats, _header);
        nextAt = _header.getSBATStart();
        for (int i = 0; i < _header.getSBATCount() && nextAt != POIFSConstants.END_OF_CHAIN; i++) {
            loopDetector.claim(nextAt);
            ByteBuffer fatData = getBlockAt(nextAt);
            sfat = BATBlock.createBATBlock(bigBlockSize, fatData);
            sfat.setOurBlockIndex(nextAt);
            sbats.add(sfat);
            nextAt = getNextBlock(nextAt);
        }
    }

    private void readBAT(int batAt, ChainLoopDetector loopDetector) throws IOException {
        loopDetector.claim(batAt);
        ByteBuffer fatData = getBlockAt(batAt);
        BATBlock bat = BATBlock.createBATBlock(bigBlockSize, fatData);
        bat.setOurBlockIndex(batAt);
        _bat_blocks.add(bat);
    }

    private BATBlock createBAT(int offset, boolean isBAT) throws IOException {
        // Create a new BATBlock
        BATBlock newBAT = BATBlock.createEmptyBATBlock(bigBlockSize, !isBAT);
        newBAT.setOurBlockIndex(offset);
        // Ensure there's a spot in the file for it
        ByteBuffer buffer = ByteBuffer.allocate(bigBlockSize.getBigBlockSize());
        // Header isn't in BATs
        long writeTo = Math.multiplyExact(1L + offset, bigBlockSize.getBigBlockSize());
        _data.write(buffer, writeTo);
        // All done
        return newBAT;
    }

    /**
     * Load the block at the given offset.
     */
    @Override
    protected ByteBuffer getBlockAt(final int offset) throws IOException {
        // The header block doesn't count, so add one
        long blockWanted = offset + 1L;
        long startAt = blockWanted * bigBlockSize.getBigBlockSize();
        try {
            return _data.read(bigBlockSize.getBigBlockSize(), startAt);
        } catch (IndexOutOfBoundsException e) {
            IndexOutOfBoundsException wrapped = new IndexOutOfBoundsException("Block " + offset + " not found");
            wrapped.initCause(e);
            throw wrapped;
        }
    }

    /**
     * Load the block at the given offset,
     * extending the file if needed
     */
    @Override
    protected ByteBuffer createBlockIfNeeded(final int offset) throws IOException {
        try {
            return getBlockAt(offset);
        } catch (IndexOutOfBoundsException e) {
            // The header block doesn't count, so add one
            long startAt = (offset + 1L) * bigBlockSize.getBigBlockSize();
            // Allocate and write
            ByteBuffer buffer = ByteBuffer.allocate(getBigBlockSize());
            _data.write(buffer, startAt);
            // Retrieve the properly backed block
            return getBlockAt(offset);
        }
    }

    /**
     * Returns the BATBlock that handles the specified offset,
     * and the relative index within it
     */
    @Override
    protected BATBlockAndIndex getBATBlockAndIndex(final int offset) {
        return BATBlock.getBATBlockAndIndex(
                offset, _header, _bat_blocks
        );
    }

    /**
     * Works out what block follows the specified one.
     */
    @Override
    protected int getNextBlock(final int offset) {
        BATBlockAndIndex bai = getBATBlockAndIndex(offset);
        return bai.getBlock().getValueAt(bai.getIndex());
    }

    /**
     * Changes the record of what block follows the specified one.
     */
    @Override
    protected void setNextBlock(final int offset, final int nextBlock) {
        BATBlockAndIndex bai = getBATBlockAndIndex(offset);
        bai.getBlock().setValueAt(
                bai.getIndex(), nextBlock
        );
    }

    /**
     * Finds a free block, and returns its offset.
     * This method will extend the file if needed, and if doing
     * so, allocate new FAT blocks to address the extra space.
     */
    @Override
    protected int getFreeBlock() throws IOException {
        int numSectors = bigBlockSize.getBATEntriesPerBlock();

        // First up, do we have any spare ones?
        int offset = 0;
        for (BATBlock bat : _bat_blocks) {
            if (bat.hasFreeSectors()) {
                // Claim one of them and return it
                for (int j = 0; j < numSectors; j++) {
                    int batValue = bat.getValueAt(j);
                    if (batValue == POIFSConstants.UNUSED_BLOCK) {
                        // Bingo
                        return offset + j;
                    }
                }
            }

            // Move onto the next BAT
            offset += numSectors;
        }

        // If we get here, then there aren't any free sectors
        //  in any of the BATs, so we need another BAT
        BATBlock bat = createBAT(offset, true);
        bat.setValueAt(0, POIFSConstants.FAT_SECTOR_BLOCK);
        _bat_blocks.add(bat);

        // Now store a reference to the BAT in the required place
        if (_header.getBATCount() >= 109) {
            // Needs to come from an XBAT
            BATBlock xbat = null;
            for (BATBlock x : _xbat_blocks) {
                if (x.hasFreeSectors()) {
                    xbat = x;
                    break;
                }
            }
            if (xbat == null) {
                // Oh joy, we need a new XBAT too...
                xbat = createBAT(offset + 1, false);
                // Allocate our new BAT as the first block in the XBAT
                xbat.setValueAt(0, offset);
                // And allocate the XBAT in the BAT
                bat.setValueAt(1, POIFSConstants.DIFAT_SECTOR_BLOCK);

                // Will go one place higher as XBAT added in
                offset++;

                // Chain it
                if (_xbat_blocks.isEmpty()) {
                    _header.setXBATStart(offset);
                } else {
                    _xbat_blocks.get(_xbat_blocks.size() - 1).setValueAt(
                            bigBlockSize.getXBATEntriesPerBlock(), offset
                    );
                }
                _xbat_blocks.add(xbat);
                _header.setXBATCount(_xbat_blocks.size());
            } else {
                // Allocate our BAT in the existing XBAT with space
                for (int i = 0; i < bigBlockSize.getXBATEntriesPerBlock(); i++) {
                    if (xbat.getValueAt(i) == POIFSConstants.UNUSED_BLOCK) {
                        xbat.setValueAt(i, offset);
                        break;
                    }
                }
            }
        } else {
            // Store us in the header
            int[] newBATs = new int[_header.getBATCount() + 1];
            System.arraycopy(_header.getBATArray(), 0, newBATs, 0, newBATs.length - 1);
            newBATs[newBATs.length - 1] = offset;
            _header.setBATArray(newBATs);
        }
        _header.setBATCount(_bat_blocks.size());

        // The current offset stores us, but the next one is free
        return offset + 1;
    }

    protected long size() throws IOException {
        return _data.size();
    }

    @Override
    protected ChainLoopDetector getChainLoopDetector() throws IOException {
        return new ChainLoopDetector(_data.size());
    }

    /**
     * For unit testing only! Returns the underlying
     * properties table
     */
    PropertyTable _get_property_table() {
        return _property_table;
    }

    /**
     * Returns the MiniStore, which performs a similar low
     * level function to this, except for the small blocks.
     */
    POIFSMiniStore getMiniStore() {
        return _mini_store;
    }

    /**
     * add a new POIFSDocument to the FileSytem
     *
     * @param document the POIFSDocument being added
     */
    void addDocument(final POIFSDocument document) {
        _property_table.addProperty(document.getDocumentProperty());
    }

    /**
     * add a new DirectoryProperty to the FileSystem
     *
     * @param directory the DirectoryProperty being added
     */
    void addDirectory(final DirectoryProperty directory) {
        _property_table.addProperty(directory);
    }

    /**
     * Create a new document to be added to the root directory
     *
     * @param stream the InputStream from which the document's data
     *               will be obtained
     * @param name   the name of the new POIFSDocument
     * @return the new DocumentEntry
     * @throws IOException on error creating the new POIFSDocument
     */

    public DocumentEntry createDocument(final InputStream stream,
                                        final String name)
            throws IOException {
        return getRoot().createDocument(name, stream);
    }

    /**
     * create a new DocumentEntry in the root entry; the data will be
     * provided later
     *
     * @param name   the name of the new DocumentEntry
     * @param size   the size of the new DocumentEntry
     * @param writer the writer of the new DocumentEntry
     * @return the new DocumentEntry
     * @throws IOException if the writer exceeds the given size
     */
    public DocumentEntry createDocument(final String name, final int size, final POIFSWriterListener writer)
            throws IOException {
        return getRoot().createDocument(name, size, writer);
    }

    /**
     * create a new DirectoryEntry in the root directory
     *
     * @param name the name of the new DirectoryEntry
     * @return the new DirectoryEntry
     * @throws IOException on name duplication
     */

    public DirectoryEntry createDirectory(final String name)
            throws IOException {
        return getRoot().createDirectory(name);
    }

    /**
     * Set the contents of a document in the root directory,
     * creating if needed, otherwise updating
     *
     * @param stream the InputStream from which the document's data
     *               will be obtained
     * @param name   the name of the new or existing POIFSDocument
     * @return the new or updated DocumentEntry
     * @throws IOException on error populating the POIFSDocument
     */
    @SuppressWarnings("UnusedReturnValue")
    public DocumentEntry createOrUpdateDocument(final InputStream stream, final String name)
            throws IOException {
        return getRoot().createOrUpdateDocument(name, stream);
    }

    /**
     * Does the filesystem support an in-place write via
     * {@link #writeFilesystem()} ? If false, only writing out to
     * a brand new file via {@link #writeFilesystem(OutputStream)}
     * is supported.
     */
    public boolean isInPlaceWriteable() {
        return (_data instanceof FileBackedDataSource) && ((FileBackedDataSource) _data).isWriteable();
    }

    /**
     * Write the filesystem out to the open file. Will thrown an
     * {@link IllegalArgumentException} if opened from an
     * {@link InputStream}.
     *
     * @throws IOException thrown on errors writing to the stream
     */
    public void writeFilesystem() throws IOException {
        if (!(_data instanceof FileBackedDataSource)) {
            throw new IllegalArgumentException(
                    "POIFS opened from an inputstream, so writeFilesystem() may " +
                            "not be called. Use writeFilesystem(OutputStream) instead"
            );
        }
        if (!((FileBackedDataSource) _data).isWriteable()) {
            throw new IllegalArgumentException(
                    "POIFS opened in read only mode, so writeFilesystem() may " +
                            "not be called. Open the FileSystem in read-write mode first"
            );
        }
        syncWithDataSource();
    }

    /**
     * Write the filesystem out
     *
     * @param stream the OutputStream to which the filesystem will be
     *               written
     * @throws IOException thrown on errors writing to the stream
     */
    public void writeFilesystem(final OutputStream stream) throws IOException {
        // Have the datasource updated
        syncWithDataSource();

        // Now copy the contents to the stream
        _data.copyTo(stream);
    }

    /**
     * Has our in-memory objects write their state
     * to their backing blocks
     */
    private void syncWithDataSource() throws IOException {
        // Mini Stream + SBATs first, as mini-stream details have
        //  to be stored in the Root Property
        _mini_store.syncWithDataSource();

        // Properties
        POIFSStream propStream = new POIFSStream(this, _header.getPropertyStart());
        _property_table.preWrite();
        _property_table.write(propStream);
        // _header.setPropertyStart has been updated on write ...

        // HeaderBlock
        UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().setBufferSize(
                _header.getBigBlockSize().getBigBlockSize()
        ).get();
        _header.writeData(baos);
        getBlockAt(-1).put(baos.toByteArray());


        // BATs
        for (BATBlock bat : _bat_blocks) {
            ByteBuffer block = getBlockAt(bat.getOurBlockIndex());
            bat.writeData(block);
        }
        // XBats
        for (BATBlock bat : _xbat_blocks) {
            ByteBuffer block = getBlockAt(bat.getOurBlockIndex());
            bat.writeData(block);
        }
    }

    /**
     * Closes the FileSystem, freeing any underlying files, streams
     * and buffers. After this, you will be unable to read or
     * write from the FileSystem.
     */
    @Override
    public void close() throws IOException {
        _data.close();
    }

    /**
     * read in a file and write it back out again
     *
     * @param args names of the files; arg[ 0 ] is the input file,
     *             arg[ 1 ] is the output file
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println(
                    "two arguments required: input filename and output filename");
            System.exit(1);
        }

        try (InputStream istream = Files.newInputStream(Path.of(args[0]))) {
            try (OutputStream ostream = Files.newOutputStream(Path.of(args[1]))) {
                try (POIFSFileSystem fs = new POIFSFileSystem(istream)) {
                    fs.writeFilesystem(ostream);
                }
            }
        }
    }

    /**
     * Get the root entry
     *
     * @return the root entry
     */
    public DirectoryNode getRoot() {
        if (_root == null) {
            _root = new DirectoryNode(_property_table.getRoot(), this, null);
        }
        return _root;
    }

    /**
     * open a document in the root entry's list of entries
     *
     * @param documentName the name of the document to be opened
     * @return a newly opened DocumentInputStream
     * @throws IOException if the document does not exist or the
     *                     name is that of a DirectoryEntry
     */
    public DocumentInputStream createDocumentInputStream(
            final String documentName) throws IOException {
        return getRoot().createDocumentInputStream(documentName);
    }

    /**
     * remove an entry
     *
     * @param entry to be removed
     */
    void remove(EntryNode entry) throws IOException {
        // If it's a document, free the blocks
        if (entry instanceof DocumentEntry) {
            POIFSDocument doc = new POIFSDocument((DocumentProperty) entry.getProperty(), this);
            doc.free();
        }

        // Now zap it from the properties list
        _property_table.removeProperty(entry.getProperty());
    }

    /* ********** START begin implementation of POIFSViewable ********** */

    /**
     * Get an array of objects, some of which may implement
     * POIFSViewable
     *
     * @return an array of Object; may not be null, but may be empty
     */
    @Override
    public Object[] getViewableArray() {
        if (preferArray()) {
            return getRoot().getViewableArray();
        }

        return new Object[0];
    }

    /**
     * Get an Iterator of objects, some of which may implement
     * POIFSViewable
     *
     * @return an Iterator; may not be null, but may have an empty
     * back end store
     */

    @Override
    public Iterator<Object> getViewableIterator() {
        if (!preferArray()) {
            return getRoot().getViewableIterator();
        }

        return Collections.emptyIterator();
    }

    /**
     * Give viewers a hint as to whether to call getViewableArray or
     * getViewableIterator
     *
     * @return true if a viewer should call getViewableArray, false if
     * a viewer should call getViewableIterator
     */

    @Override
    public boolean preferArray() {
        return getRoot().preferArray();
    }

    /**
     * Provides a short description of the object, to be used when a
     * POIFSViewable object has not provided its contents.
     *
     * @return short description
     */

    @Override
    public String getShortDescription() {
        return "POIFS FileSystem";
    }

    /* **********  END  begin implementation of POIFSViewable ********** */

    /**
     * @return The Big Block size, normally 512 bytes, sometimes 4096 bytes
     */
    public int getBigBlockSize() {
        return bigBlockSize.getBigBlockSize();
    }

    /**
     * @return The Big Block size, normally 512 bytes, sometimes 4096 bytes
     */
    @SuppressWarnings("WeakerAccess")
    public POIFSBigBlockSize getBigBlockSizeDetails() {
        return bigBlockSize;
    }

    /**
     * Creates a new POIFSFileSystem in a new {@link File}.
     * Use {@link #POIFSFileSystem(File)} to open an existing File,
     * this should only be used to create a new empty filesystem.
     *
     * @param file The file to create and open
     * @return The created and opened POIFSFileSystem
     */
    public static POIFSFileSystem create(File file) throws IOException {
        // Create a new empty POIFS in the file
        try (POIFSFileSystem tmp = new POIFSFileSystem();
             OutputStream out = Files.newOutputStream(file.toPath())) {
            tmp.writeFilesystem(out);
        }

        // Open it up again backed by the file
        return new POIFSFileSystem(file, false);
    }

    @Override
    protected int getBlockStoreBlockSize() {
        return getBigBlockSize();
    }

    @Internal
    public PropertyTable getPropertyTable() {
        return _property_table;
    }

    @Internal
    public HeaderBlock getHeaderBlock() {
        return _header;
    }

    @Override
    protected void releaseBuffer(ByteBuffer buffer) {
        if (_data instanceof FileBackedDataSource) {
            ((FileBackedDataSource)_data).releaseBuffer(buffer);
        }
    }

    private static void sanityCheckBlockCount(int block_count) throws IOException {
        if (block_count <= 0) {
            throw new IOException(
                    "Illegal block count; minimum count is 1, got " +
                            block_count + " instead"
            );
        }
        if (block_count > MAX_BLOCK_COUNT) {
            throw new IOException(
                    "Block count " + block_count +
                            " is too high. POI maximum is " + MAX_BLOCK_COUNT + "."
            );
        }
    }

}
