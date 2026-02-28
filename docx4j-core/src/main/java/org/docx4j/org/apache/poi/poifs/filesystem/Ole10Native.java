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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInput;
import org.docx4j.org.apache.poi.util.LittleEndianOutputStream;
import org.docx4j.org.apache.poi.util.StringUtil;

/**
 * Represents an Ole10Native record which is wrapped around certain binary
 * files being embedded in OLE2 documents.<p>
 *
 * Ole10Native objects come in different shapes:
 * <ul>
 *     <li>unparsed: we can't identify its structure</li>
 *     <li>compact: same as unparsed but with a leading flag</li>
 *     <li>parsed - Ole-Class "Package": data + ASCII label,command,filename</li>
 *     <li>parsed - Ole-Class "Package2": as above plus UTF16 label,command,filename</li>
 * </ul>
 */
@SuppressWarnings("unused")
public class Ole10Native {


    public static final String OLE10_NATIVE = "\u0001Ole10Native";
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    // arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_RECORD_LENGTH = 100_000_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;
    // arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_STRING_LENGTH = 1024;
    private static int MAX_STRING_LENGTH = DEFAULT_MAX_STRING_LENGTH;

    /**
     * Default content of the \u0001Ole entry
     */
    private static final byte[] OLE_MARKER_BYTES =
            {1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final String OLE_MARKER_NAME = "\u0001Ole";


    // 4 bytes, total size of record not including this field
    private int totalSize;
    // 2 bytes, unknown, mostly [02 00]
    private short flags1 = 2;
    // ASCIIZ, stored in this field without the terminating zero
    private String label;
    // ASCIIZ, stored in this field without the terminating zero
    private String fileName;
    // 2 bytes, unknown, mostly [00 00]
    private short flags2;
    // see below
    private short unknown1 = 3;
    // ASCIIZ, stored in this field without the terminating zero
    private String command;
    // varying size, the actual native data
    private byte[] dataBuffer;
    // UTF16-LE String with leading length
    private String command2;
    // UTF16-LE String with leading length
    private String label2;
    // UTF16-LE String with leading length
    private String fileName2;

    /**
     * the field encoding mode - merely a try-and-error guess ...
     **/
    public enum EncodingMode {
        /**
         * the data is stored in parsed format - including label, command, etc.
         */
        parsed,
        /**
         * the data is stored raw after the length field
         */
        unparsed,
        /**
         * the data is stored raw after the length field and the flags1 field
         */
        compact
    }

    private EncodingMode mode;


    public EncodingMode getMode() {
		return mode;
	}

	/**
     * Creates an instance of this class from an embedded OLE Object. The OLE Object is expected
     * to include a stream &quot;{01}Ole10Native&quot; which contains the actual
     * data relevant for this class.
     *
     * @param poifs POI Filesystem object
     * @return Returns an instance of this class
     * @throws IOException          on IO error
     * @throws Ole10NativeException on invalid or unexcepted data format
     */
    public static Ole10Native createFromEmbeddedOleObject(POIFSFileSystem poifs) throws IOException, Ole10NativeException {
        return createFromEmbeddedOleObject(poifs.getRoot());
    }

    /**
     * Creates an instance of this class from an embedded OLE Object. The OLE Object is expected
     * to include a stream &quot;{01}Ole10Native&quot; which contains the actual
     * data relevant for this class.
     *
     * @param directory POI Filesystem object
     * @return Returns an instance of this class
     * @throws IOException          on IO error
     * @throws Ole10NativeException on invalid or unexcepted data format
     */
    public static Ole10Native createFromEmbeddedOleObject(DirectoryNode directory) throws IOException, Ole10NativeException {
        DocumentEntry nativeEntry = (DocumentEntry) directory.getEntryCaseInsensitive(OLE10_NATIVE);
        try (DocumentInputStream dis = directory.createDocumentInputStream(nativeEntry)) {
            byte[] data = IOUtils.toByteArray(dis, nativeEntry.getSize(), MAX_RECORD_LENGTH);
            return new Ole10Native(data, 0);
        }
    }

    /**
     * @param length the max record length allowed for Ole10Native
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for Ole10Native
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    /**
     * @param length the max string length allowed for Ole10Native
     */
    public static void setMaxStringLength(int length) {
        MAX_STRING_LENGTH = length;
    }

    /**
     * @return the max string length allowed for Ole10Native
     */
    public static int getMaxStringLength() {
        return MAX_STRING_LENGTH;
    }

    /**
     * Creates an instance and fills the fields based on ... the fields
     */
    public Ole10Native(String label, String filename, String command, byte[] data) {
        setLabel(label);
        setFileName(filename);
        setCommand(command);
        command2 = command;
        setDataBuffer(data);
        mode = EncodingMode.parsed;
    }

    /**
     * Creates an instance and fills the fields based on the data in the given buffer.
     *
     * @param data   The buffer containing the Ole10Native record
     * @param offset The start offset of the record in the buffer
     * @throws Ole10NativeException on invalid or unexcepted data format
     */
    public Ole10Native(final byte[] data, final int offset) throws Ole10NativeException {
        LittleEndianByteArrayInputStream leis = new LittleEndianByteArrayInputStream(data, offset);

        totalSize = leis.readInt();
        leis.limit(totalSize + LittleEndianConsts.INT_SIZE);

        leis.mark(0);

        try {
            flags1 = leis.readShort();
            if (flags1 == 2) {
                leis.mark(0);
                // some files like equations don't have a valid filename,
                // but somehow encode the formula right away in the ole10 header
                boolean validFileName = !Character.isISOControl(leis.readByte());
                leis.reset();

                if (validFileName) {
                    readParsed(leis);
                } else {
                    readCompact(leis);
                }
            } else {
                leis.reset();
                readUnparsed(leis);
            }
        } catch (IOException e) {
            throw new Ole10NativeException("Invalid Ole10Native", e);
        }
    }

    private void readParsed(LittleEndianByteArrayInputStream leis) throws Ole10NativeException, IOException {
        mode = EncodingMode.parsed;
        label = readAsciiZ(leis);
        fileName = readAsciiZ(leis);
        flags2 = leis.readShort();
        unknown1 = leis.readShort();
        command = readAsciiLen(leis);
        dataBuffer = IOUtils.toByteArray(leis, leis.readInt(), MAX_RECORD_LENGTH);

        leis.mark(0);
        short lowSize = leis.readShort();
        if (lowSize != 0) {
            leis.reset();
            command2 = readUtf16(leis);
            label2 = readUtf16(leis);
            fileName2 = readUtf16(leis);
        }
    }

    private void readCompact(LittleEndianByteArrayInputStream leis) throws IOException {
        mode = EncodingMode.compact;
        dataBuffer = IOUtils.toByteArray(leis, totalSize - LittleEndianConsts.SHORT_SIZE, MAX_RECORD_LENGTH);
    }

    private void readUnparsed(LittleEndianByteArrayInputStream leis) throws IOException {
        mode = EncodingMode.unparsed;
        dataBuffer = IOUtils.toByteArray(leis, totalSize, MAX_RECORD_LENGTH);
    }

    /**
     * Add the \1OLE marker entry, which is not the Ole10Native entry.
     * Beside this "\u0001Ole" record there were several other records, e.g. CompObj,
     * OlePresXXX, but it seems, that they aren't necessary
     */
    public static void createOleMarkerEntry(final DirectoryEntry parent) throws IOException {
        if (!parent.hasEntryCaseInsensitive(OLE_MARKER_NAME)) {
            parent.createDocument(OLE_MARKER_NAME, UnsynchronizedByteArrayInputStream.builder().setByteArray(OLE_MARKER_BYTES).get());
        }
    }

    /**
     * Add the \1OLE marker entry, which is not the Ole10Native entry.
     * Beside this "\u0001Ole" record there were several other records, e.g. CompObj,
     * OlePresXXX, but it seems, that they aren't necessary
     */
    public static void createOleMarkerEntry(final POIFSFileSystem poifs) throws IOException {
        createOleMarkerEntry(poifs.getRoot());
    }


    /**
     * Read zero terminated string (ASCIIZ).
     */
    private static String readAsciiZ(LittleEndianInput is) throws Ole10NativeException {
        // arbitrary sized buffer - not sure how big strings can get in an Ole10 record
        byte[] buf = new byte[MAX_STRING_LENGTH];
        for (int i=0; i<buf.length; i++) {
            if ((buf[i] = is.readByte()) == 0) {
                return StringUtil.getFromCompressedUTF8(buf, 0, i);
            }
        }
        throw new Ole10NativeException("AsciiZ string was not null terminated after " + MAX_STRING_LENGTH + " bytes - Exiting.");
    }

    private static String readAsciiLen(LittleEndianByteArrayInputStream leis) throws IOException {
        int size = leis.readInt();
        byte[] buf = IOUtils.toByteArray(leis, size, MAX_STRING_LENGTH);
        return (buf.length == 0) ? "" : StringUtil.getFromCompressedUnicode(buf, 0, size - 1);
    }

    private static String readUtf16(LittleEndianByteArrayInputStream leis) throws IOException {
        int size = leis.readInt();
        byte[] buf = IOUtils.toByteArray(leis, size * 2, MAX_STRING_LENGTH);
        return StringUtil.getFromUnicodeLE(buf, 0, size);
    }

    /**
     * Returns the value of the totalSize field - the total length of the
     * structure is totalSize + 4 (value of this field + size of this field).
     *
     * @return the totalSize
     */
    public int getTotalSize() {
        return totalSize;
    }

    /**
     * Returns flags1 - currently unknown - usually 0x0002.
     *
     * @return the flags1
     */
    public short getFlags1() {
        return flags1;
    }

    /**
     * Returns the label field - usually the name of the file (without
     * directory) but probably may be any name specified during
     * packaging/embedding the data.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the fileName field - usually the name of the file being embedded
     * including the full path.
     *
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns flags2 - currently unknown - mostly 0x0000.
     *
     * @return the flags2
     */
    public short getFlags2() {
        return flags2;
    }

    /**
     * Returns unknown1 field - currently unknown.
     *
     * @return the unknown1
     */
    public short getUnknown1() {
        return unknown1;
    }

    /**
     * Returns the command field - usually the name of the file being embedded
     * including the full path, may be a command specified during embedding the
     * file.
     *
     * @return the command
     */
    public String getCommand() {
        return command;
    }

    /**
     * Returns the size of the embedded file. If the size is 0 (zero), no data
     * has been embedded. To be sure, that no data has been embedded, check
     * whether {@link #getDataBuffer()} returns {@code null}.
     *
     * @return the dataSize
     */
    public int getDataSize() {
        return dataBuffer.length;
    }

    /**
     * Returns the buffer containing the embedded file's data, or
     * {@code null} if no data was embedded. Note that an embedding may
     * provide information about the data, but the actual data is not included.
     * (So label, filename etc. are available, but this method returns
     * {@code null}.)
     *
     * @return the dataBuffer
     */
    public byte[] getDataBuffer() {
        return dataBuffer;
    }

    /**
     * Have the contents printer out into an OutputStream, used when writing a
     * file back out to disk (Normally, atom classes will keep their bytes
     * around, but non atom classes will just request the bytes from their
     * children, then chuck on their header and return)
     */
    public void writeOut(OutputStream out) throws IOException {
        // byte intbuf[] = new byte[LittleEndianConsts.INT_SIZE];
        // byte shortbuf[] = new byte[LittleEndianConsts.SHORT_SIZE];

        @SuppressWarnings("resource")
        LittleEndianOutputStream leosOut = new LittleEndianOutputStream(out);

        switch (mode) {
            case parsed: {
                UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get();
                try (LittleEndianOutputStream leos = new LittleEndianOutputStream(bos)) {
                    // total size, will be determined later ..

                    leos.writeShort(getFlags1());
                    leos.write(getLabel().getBytes(UTF8));
                    leos.write(0);
                    leos.write(getFileName().getBytes(UTF8));
                    leos.write(0);
                    leos.writeShort(getFlags2());
                    leos.writeShort(getUnknown1());
                    leos.writeInt(getCommand().length() + 1);
                    leos.write(getCommand().getBytes(UTF8));
                    leos.write(0);
                    leos.writeInt(getDataSize());
                    leos.write(getDataBuffer());

                    if (command2 == null || label2 == null || fileName2 == null) {
                        leos.writeShort(0);
                    } else {
                        leos.writeUInt(command2.length());
                        leos.write(StringUtil.getToUnicodeLE(command2));
                        leos.writeUInt(label2.length());
                        leos.write(StringUtil.getToUnicodeLE(label2));
                        leos.writeUInt(fileName2.length());
                        leos.write(StringUtil.getToUnicodeLE(fileName2));
                    }
                }

                // total size
                leosOut.writeInt(bos.size());
                bos.writeTo(out);
                break;
            }

            case compact:
                leosOut.writeInt(getDataSize() + LittleEndianConsts.SHORT_SIZE);
                leosOut.writeShort(getFlags1());
                out.write(getDataBuffer());
                break;

            default:
            case unparsed:
                leosOut.writeInt(getDataSize());
                out.write(getDataBuffer());
                break;
        }

    }

    public void setFlags1(short flags1) {
        this.flags1 = flags1;
    }

    public void setFlags2(short flags2) {
        this.flags2 = flags2;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setUnknown1(short unknown1) {
        this.unknown1 = unknown1;
    }

    public void setDataBuffer(byte[] dataBuffer) {
        this.dataBuffer = dataBuffer.clone();
    }

    /**
     * Get Command string of UTF16 extended OLE packages or {@code null} if not set or not UTF16 extended
     */
    public String getCommand2() {
        return command2;
    }

    /**
     * Set Command string for UTF16 extended OLE packages or {@code null} if not set or not UTF16 extended
     */
    public void setCommand2(String command2) {
        this.command2 = command2;
    }

    /**
     * Get Label string for UTF16 extended OLE packages or {@code null} if not set or not UTF16 extended
     */
    public String getLabel2() {
        return label2;
    }

    /**
     * Set Label string for UTF16 extended OLE packages or {@code null} if not set or not UTF16 extended
     */
    public void setLabel2(String label2) {
        this.label2 = label2;
    }

    /**
     * Get filename string for UTF16 extended OLE packages or {@code null} if not set or not UTF16 extended
     */
    public String getFileName2() {
        return fileName2;
    }

    /**
     * Set filename string for UTF16 extended OLE packages or {@code null} if not set or not UTF16 extended
     */
    public void setFileName2(String fileName2) {
        this.fileName2 = fileName2;
    }
}
