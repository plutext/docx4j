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

package org.docx4j.org.apache.poi.poifs.macros;

import static org.docx4j.org.apache.poi.util.StringUtil.endsWithIgnoreCase;
import static org.docx4j.org.apache.poi.util.StringUtil.startsWithIgnoreCase;

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode;
import org.docx4j.org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.docx4j.org.apache.poi.poifs.filesystem.DocumentNode;
import org.docx4j.org.apache.poi.poifs.filesystem.Entry;
import org.docx4j.org.apache.poi.poifs.filesystem.FileMagic;
import org.docx4j.org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.docx4j.org.apache.poi.poifs.macros.Module.ModuleType;
import org.docx4j.org.apache.poi.util.CodePageUtil;
import org.docx4j.org.apache.poi.util.HexDump;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.docx4j.org.apache.poi.util.RLEDecompressingInputStream;
import org.docx4j.org.apache.poi.util.StringUtil;
import org.docx4j.utils.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Finds all VBA Macros in an office file (OLE2/POIFS and OOXML/OPC),
 *  and returns them.
 * </p>
 * <p>
 * <b>NOTE:</b> This does not read macros from .ppt files.
 * See org.apache.poi.hslf.usermodel.TestBugs.getMacrosFromHSLF() in the scratchpad
 * module for an example of how to do this. Patches that make macro
 * extraction from .ppt more elegant are welcomed!
 * </p>
 *
 * @since 3.15-beta2
 */
@SuppressWarnings("unused")
public class VBAMacroReader implements Closeable {
	
	protected static Logger log = LoggerFactory.getLogger(VBAMacroReader.class);	

    //arbitrary limit on size of strings to read, etc.
    private static final int MAX_STRING_LENGTH = 20000;
    protected static final String VBA_PROJECT_OOXML = "vbaProject.bin";
    protected static final String VBA_PROJECT_POIFS = "VBA";

    private POIFSFileSystem fs;

    public VBAMacroReader(InputStream rstream) throws IOException {
        InputStream is = FileMagic.prepareToCheckMagic(rstream);
        FileMagic fm = FileMagic.valueOf(is);
        if (fm == FileMagic.OLE2) {
            fs = new POIFSFileSystem(is);
        } else {
            openOOXML(is);
        }
    }

    public VBAMacroReader(File file) throws IOException {
        try {
            this.fs = new POIFSFileSystem(file);
        } catch (OfficeXmlFileException e) {
            openOOXML(Files.newInputStream(file.toPath()));
        }
    }
    public VBAMacroReader(POIFSFileSystem fs) {
        this.fs = fs;
    }

    private void openOOXML(InputStream zipFile) throws IOException {
        try(ZipInputStream zis = new ZipInputStream(zipFile)) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (endsWithIgnoreCase(zipEntry.getName(), VBA_PROJECT_OOXML)) {
                    try {
                        // Make a POIFSFileSystem from the contents, and close the stream
                        this.fs = new POIFSFileSystem(zis);
                        return;
                    } catch (IOException e) {
                        // Tidy up
                        zis.close();

                        // Pass on
                        throw e;
                    }
                }
            }
        }
        throw new IllegalArgumentException("No VBA project found");
    }

    @Override
    public void close() throws IOException {
        fs.close();
        fs = null;
    }

    public Map<String, Module> readMacroModules() throws IOException {
        final ModuleMap modules = new ModuleMap();
        //ascii -> unicode mapping for module names
        //preserve insertion order
        final Map<String, String> moduleNameMap = new LinkedHashMap<>();

        findMacros(fs.getRoot(), modules);
        findModuleNameMap(fs.getRoot(), moduleNameMap, modules);
        findProjectProperties(fs.getRoot(), moduleNameMap, modules);

        Map<String, Module> moduleSources = new HashMap<>();
        for (Map.Entry<String, ModuleImpl> entry : modules.entrySet()) {
            ModuleImpl module = entry.getValue();
            module.charset = modules.charset;
            moduleSources.put(entry.getKey(), module);
        }
        return moduleSources;
    }

    /**
     * Reads all macros from all modules of the opened office file.
     * @return All the macros and their contents
     *
     * @since 3.15-beta2
     */
    public Map<String, String> readMacros() throws IOException {
        Map<String, Module> modules = readMacroModules();
        Map<String, String> moduleSources = new HashMap<>();
        for (Map.Entry<String, Module> entry : modules.entrySet()) {
            moduleSources.put(entry.getKey(), entry.getValue().getContent());
        }
        return moduleSources;
    }

    protected static class ModuleImpl implements Module {
        Integer offset;
        byte[] buf;
        ModuleType moduleType;
        Charset charset;
        void read(InputStream in) throws IOException {
            buf = IOUtils.toByteArray(in);
        }
        @Override
        public String getContent() {
            return new String(buf, charset);
        }
        @Override
        public ModuleType geModuleType() {
            return moduleType;
        }
    }
    protected static class ModuleMap extends HashMap<String, ModuleImpl> {
        Charset charset = StringUtil.WIN_1252; // default charset
    }

    /**
     * Recursively traverses directory structure rooted at {@code dir}.
     * For each macro module that is found, the module's name and code are
     * added to {@code modules}.
     *
     * @param dir The directory of entries to look at
     * @param modules The resulting map of modules
     * @throws IOException If reading the VBA module fails
     * @since 3.15-beta2
     */
    protected void findMacros(DirectoryNode dir, ModuleMap modules) throws IOException {
        if (VBA_PROJECT_POIFS.equalsIgnoreCase(dir.getName())) {
            // VBA project directory, process
            readMacros(dir, modules);
        } else {
            // Check children
            for (Entry child : dir) {
                if (child instanceof DirectoryNode) {
                    findMacros((DirectoryNode)child, modules);
                }
            }
        }
    }



    /**
     * reads module from DIR node in input stream and adds it to the modules map for decompression later
     * on the second pass through this function, the module will be decompressed
     *
     * Side-effects: adds a new module to the module map or sets the buf field on the module
     * to the decompressed stream contents (the VBA code for one module)
     *
     * @param in the run-length encoded input stream to read from
     * @param streamName the stream name of the module
     * @param modules a map to store the modules
     * @throws IOException If reading data from the stream or from modules fails
     */
    private static void readModuleMetadataFromDirStream(RLEDecompressingInputStream in, String streamName, ModuleMap modules) throws IOException {
        int moduleOffset = in.readInt();
        ModuleImpl module = modules.get(streamName);
        if (module == null) {
            // First time we've seen the module. Add it to the ModuleMap and decompress it later
            module = new ModuleImpl();
            module.offset = moduleOffset;
            modules.put(streamName, module);
            // Would adding module.read(in) here be correct?
        } else {
            // Decompress a previously found module and store the decompressed result into module.buf
            InputStream stream = new RLEDecompressingInputStream(
                    UnsynchronizedByteArrayInputStream.builder().
                            setByteArray(module.buf).
                            setOffset(moduleOffset).
                            setLength(module.buf.length - moduleOffset).
                            get()
            );
            module.read(stream);
            stream.close();
        }
    }

    private static void readModuleFromDocumentStream(DocumentNode documentNode, String name, ModuleMap modules) throws IOException {
        ModuleImpl module = modules.get(name);
        // TODO Refactor this to fetch dir then do the rest
        if (module == null) {
            // no DIR stream with offsets yet, so store the compressed bytes for later
            module = new ModuleImpl();
            modules.put(name, module);
            try (InputStream dis = new DocumentInputStream(documentNode)) {
                module.read(dis);
            }
        } else if (module.buf == null) { //if we haven't already read the bytes for the module keyed off this name...

            if (module.offset == null) {
                //This should not happen. bug 59858
                throw new IOException("Module offset for '" + name + "' was never read.");
            }

            //try the general case, where module.offset is accurate
            try (InputStream compressed = new DocumentInputStream(documentNode)) {
                // we know the offset already, so decompress immediately on-the-fly
                trySkip(compressed, module.offset);
                try (InputStream decompressed = new RLEDecompressingInputStream(compressed)) {
                    module.read(decompressed);
                }
                return;
            } catch (IllegalArgumentException | IllegalStateException ignored) {
            }

            //bad module.offset, try brute force
            byte[] decompressedBytes;
            try (InputStream compressed = new DocumentInputStream(documentNode)) {
                decompressedBytes = findCompressedStreamWBruteForce(compressed);
            }

            if (decompressedBytes != null) {
                module.read(UnsynchronizedByteArrayInputStream.builder().setByteArray(decompressedBytes).get());
            }
        }

    }

    /**
      * Skips {@code n} bytes in an input stream, throwing IOException if the
      * number of bytes skipped is different than requested.
      * @throws IOException If skipping would exceed the available data or skipping did not work.
      */
    private static void trySkip(InputStream in, long n) throws IOException {
        long skippedBytes = IOUtils.skipFully(in, n);
        if (skippedBytes != n) {
            if (skippedBytes < 0) {
                throw new IOException(
                    "Tried skipping " + n + " bytes, but no bytes were skipped. "
                    + "The end of the stream has been reached or the stream is closed.");
            } else {
                throw new IOException(
                    "Tried skipping " + n + " bytes, but only " + skippedBytes + " bytes were skipped. "
                    + "This should never happen with a non-corrupt file.");
            }
        }
    }

    // Constants from MS-OVBA: https://msdn.microsoft.com/en-us/library/office/cc313094(v=office.12).aspx
    private static final int STREAMNAME_RESERVED = 0x0032;
    private static final int PROJECT_CONSTANTS_RESERVED = 0x003C;
    private static final int HELP_FILE_PATH_RESERVED = 0x003D;
    private static final int REFERENCE_NAME_RESERVED = 0x003E;
    private static final int DOC_STRING_RESERVED = 0x0040;
    private static final int MODULE_DOCSTRING_RESERVED = 0x0048;

    /**
     * Reads VBA Project modules from a VBA Project directory located at
     * {@code macroDir} into {@code modules}.
     *
     * @since 3.15-beta2
     */
    protected void readMacros(DirectoryNode macroDir, ModuleMap modules) throws IOException {
        //bug59858 shows that dirstream may not be in this directory (\MBD00082648\_VBA_PROJECT_CUR\VBA ENTRY NAME)
        //but may be in another directory (\_VBA_PROJECT_CUR\VBA ENTRY NAME)
        //process the dirstream first -- "dir" is case insensitive
        for (String entryName : macroDir.getEntryNames()) {
            if ("dir".equalsIgnoreCase(entryName)) {
                processDirStream(macroDir.getEntryCaseInsensitive(entryName), modules);
                break;
            }
        }

        for (Entry entry : macroDir) {
            if (! (entry instanceof DocumentNode)) { continue; }

            String name = entry.getName();
            DocumentNode document = (DocumentNode)entry;

            if (! "dir".equalsIgnoreCase(name) && !startsWithIgnoreCase(name, "__SRP")
                        && !startsWithIgnoreCase(name, "_VBA_PROJECT")) {
                    // process module, skip __SRP and _VBA_PROJECT since these do not contain macros
                    readModuleFromDocumentStream(document, name, modules);
            }
        }
    }

    protected void findProjectProperties(DirectoryNode node, Map<String, String> moduleNameMap, ModuleMap modules) throws IOException {
        for (Entry entry : node) {
            if ("project".equalsIgnoreCase(entry.getName())) {
                DocumentNode document = (DocumentNode)entry;
                try(DocumentInputStream dis = new DocumentInputStream(document)) {
                    readProjectProperties(dis, moduleNameMap, modules);
                    return;
                }
            } else if (entry instanceof DirectoryNode) {
                findProjectProperties((DirectoryNode)entry, moduleNameMap, modules);
            }
        }
    }

    protected void findModuleNameMap(DirectoryNode node, Map<String, String> moduleNameMap, ModuleMap modules) throws IOException {
        for (Entry entry : node) {
            if ("projectwm".equalsIgnoreCase(entry.getName())) {
                DocumentNode document = (DocumentNode)entry;
                try(DocumentInputStream dis = new DocumentInputStream(document)) {
                    readNameMapRecords(dis, moduleNameMap, modules.charset);
                    return;
                }
            } else if (entry.isDirectoryEntry()) {
                findModuleNameMap((DirectoryNode)entry, moduleNameMap, modules);
            }
        }
    }

    private enum RecordType {
        // Constants from MS-OVBA: https://msdn.microsoft.com/en-us/library/office/cc313094(v=office.12).aspx
        MODULE_OFFSET(0x0031),
        PROJECT_SYS_KIND(0x01),
        PROJECT_LCID(0x0002),
        PROJECT_LCID_INVOKE(0x14),
        PROJECT_CODEPAGE(0x0003),
        PROJECT_NAME(0x04),
        PROJECT_DOC_STRING(0x05),
        PROJECT_HELP_FILE_PATH(0x06),
        PROJECT_HELP_CONTEXT(0x07, 8),
        PROJECT_LIB_FLAGS(0x08),
        PROJECT_VERSION(0x09, 10),
        PROJECT_CONSTANTS(0x0C),
        PROJECT_MODULES(0x0F),
        DIR_STREAM_TERMINATOR(0x10),
        PROJECT_COOKIE(0x13),
        MODULE_NAME(0x19),
        MODULE_NAME_UNICODE(0x47),
        MODULE_STREAM_NAME(0x1A),
        MODULE_DOC_STRING(0x1C),
        MODULE_HELP_CONTEXT(0x1E),
        MODULE_COOKIE(0x2c),
        MODULE_TYPE_PROCEDURAL(0x21, 4),
        MODULE_TYPE_OTHER(0x22, 4),
        MODULE_PRIVATE(0x28, 4),
        REFERENCE_NAME(0x16),
        REFERENCE_REGISTERED(0x0D),
        REFERENCE_PROJECT(0x0E),
        REFERENCE_CONTROL_A(0x2F),

        //according to the spec, REFERENCE_CONTROL_B(0x33) should have the
        //same structure as REFERENCE_CONTROL_A(0x2F).
        //However, it seems to have the int(length) record structure that most others do.
        //See 59830.xls for this record.
        REFERENCE_CONTROL_B(0x33),
        //REFERENCE_ORIGINAL(0x33),


        MODULE_TERMINATOR(0x002B),
        EOF(-1),
        UNKNOWN(-2);


        private final int id;
        private final int constantLength;

        RecordType(int id) {
            this.id = id;
            this.constantLength = -1;
        }

        RecordType(int id, int constantLength) {
            this.id = id;
            this.constantLength = constantLength;
        }

        int getConstantLength() {
            return constantLength;
        }

        static RecordType lookup(int id) {
            for (RecordType type : RecordType.values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }


    private enum DIR_STATE {
        INFORMATION_RECORD,
        REFERENCES_RECORD,
        MODULES_RECORD
    }

    private static class ASCIIUnicodeStringPair {
        private final String ascii;
        private final String unicode;
        private final int pushbackRecordId;

        ASCIIUnicodeStringPair(String ascii, int pushbackRecordId) {
            this.ascii = ascii;
            this.unicode = "";
            this.pushbackRecordId = pushbackRecordId;
        }

        ASCIIUnicodeStringPair(String ascii, String unicode) {
            this.ascii = ascii;
            this.unicode = unicode;
            pushbackRecordId = -1;
        }

        private String getAscii() {
            return ascii;
        }

        private String getUnicode() {
            return unicode;
        }

        private int getPushbackRecordId() {
            return pushbackRecordId;
        }
    }

    private void processDirStream(Entry dir, ModuleMap modules) throws IOException {
        DocumentNode dirDocumentNode = (DocumentNode)dir;
        DIR_STATE dirState = DIR_STATE.INFORMATION_RECORD;
        try (DocumentInputStream dis = new DocumentInputStream(dirDocumentNode)) {
            String streamName = null;
            int recordId = 0;

            try (RLEDecompressingInputStream in = new RLEDecompressingInputStream(dis)) {
                while (true) {
                    recordId = in.readShort();
                    if (recordId == -1) {
                        break;
                    }
                    RecordType type = RecordType.lookup(recordId);

                    if (type.equals(RecordType.EOF) || type.equals(RecordType.DIR_STREAM_TERMINATOR)) {
                        break;
                    }
                    switch (type) {
                        case PROJECT_VERSION:
                            trySkip(in, RecordType.PROJECT_VERSION.getConstantLength());
                            break;
                        case PROJECT_CODEPAGE:
                            in.readInt();//record size must == 4
                            int codepage = in.readShort();
                            modules.charset = Charset.forName(CodePageUtil.codepageToEncoding(codepage, true));
                            break;
                        case MODULE_STREAM_NAME:
                            ASCIIUnicodeStringPair pair = readStringPair(in, modules.charset, STREAMNAME_RESERVED);
                            streamName = pair.getAscii();
                            break;
                        case PROJECT_DOC_STRING:
                            readStringPair(in, modules.charset, DOC_STRING_RESERVED);
                            break;
                        case PROJECT_HELP_FILE_PATH:
                            readStringPair(in, modules.charset, HELP_FILE_PATH_RESERVED);
                            break;
                        case PROJECT_CONSTANTS:
                            readStringPair(in, modules.charset, PROJECT_CONSTANTS_RESERVED);
                            break;
                        case REFERENCE_NAME:
                            if (dirState.equals(DIR_STATE.INFORMATION_RECORD)) {
                                dirState = DIR_STATE.REFERENCES_RECORD;
                            }
                            ASCIIUnicodeStringPair stringPair = readStringPair(in,
                                    modules.charset, REFERENCE_NAME_RESERVED, false);
                            if (stringPair.getPushbackRecordId() == -1) {
                                break;
                            }
                            //Special handling for when there's only an ascii string and a REFERENCED_REGISTERED
                            //record that follows.
                            //See https://github.com/decalage2/oletools/blob/master/oletools/olevba.py#L1516
                            //and https://github.com/decalage2/oletools/pull/135 from (@c1fe)
                            if (stringPair.getPushbackRecordId() != RecordType.REFERENCE_REGISTERED.id) {
                                throw new IllegalArgumentException("Unexpected reserved character. "+
                                        "Expected "+Integer.toHexString(REFERENCE_NAME_RESERVED)
                                        + " or "+Integer.toHexString(RecordType.REFERENCE_REGISTERED.id)+
                                        " not: "+Integer.toHexString(stringPair.getPushbackRecordId()));
                            }
                            //fall through!
                        case REFERENCE_REGISTERED:
                            //REFERENCE_REGISTERED must come immediately after
                            //REFERENCE_NAME to allow for fall through in special case of bug 62625
                            int recLength = in.readInt();
                            trySkip(in, recLength);
                            break;
                        case MODULE_DOC_STRING:
                            int modDocStringLength = in.readInt();
                            readString(in, modDocStringLength, modules.charset);
                            int modDocStringReserved = in.readShort();
                            if (modDocStringReserved != MODULE_DOCSTRING_RESERVED) {
                                throw new IOException("Expected x003C after stream name before Unicode stream name, but found: " +
                                        Integer.toHexString(modDocStringReserved));
                            }
                            int unicodeModDocStringLength = in.readInt();
                            readUnicodeString(in, unicodeModDocStringLength);
                            // do something with this at some point
                            break;
                        case MODULE_OFFSET:
                            int modOffsetSz = in.readInt();
                            //should be 4
                            readModuleMetadataFromDirStream(in, streamName, modules);
                            break;
                        case PROJECT_MODULES:
                            dirState = DIR_STATE.MODULES_RECORD;
                            in.readInt();//size must == 2
                            in.readShort();//number of modules
                            break;
                        case REFERENCE_CONTROL_A:
                            int szTwiddled = in.readInt();
                            trySkip(in, szTwiddled);
                            int nextRecord = in.readShort();
                            //reference name is optional!
                            if (nextRecord == RecordType.REFERENCE_NAME.id) {
                                readStringPair(in, modules.charset, REFERENCE_NAME_RESERVED);
                                nextRecord = in.readShort();
                            }
                            if (nextRecord != 0x30) {
                                throw new IOException("Expected 0x30 as Reserved3 in a ReferenceControl record");
                            }
                            int szExtended = in.readInt();
                            trySkip(in, szExtended);
                            break;
                        case MODULE_TERMINATOR:
                            int endOfModulesReserved = in.readInt();
                            //must be 0;
                            break;
                        default:
                            if (type.getConstantLength() > -1) {
                                trySkip(in, type.getConstantLength());
                            } else {
                                int recordLength = in.readInt();
                                trySkip(in, recordLength);
                            }
                            break;
                    }
                }
            } catch (final IOException e) {
                throw new IOException(
                        "Error occurred while reading macros at section id "
                                + recordId + " (" + HexDump.shortToHex(recordId) + ")", e);
            }
        }
    }



    private ASCIIUnicodeStringPair readStringPair(RLEDecompressingInputStream in,
                                                  Charset charset, int reservedByte) throws IOException {
        return readStringPair(in, charset, reservedByte, true);
    }

    private ASCIIUnicodeStringPair readStringPair(RLEDecompressingInputStream in,
                                                  Charset charset, int reservedByte,
                                                  boolean throwOnUnexpectedReservedByte) throws IOException {
        int nameLength = in.readInt();
        String ascii = readString(in, nameLength, charset);
        int reserved = in.readShort();

        if (reserved != reservedByte) {
            if (throwOnUnexpectedReservedByte) {
                throw new IOException("Expected " + Integer.toHexString(reservedByte) +
                        "after name before Unicode name, but found: " +
                        Integer.toHexString(reserved));
            } else {
                return new ASCIIUnicodeStringPair(ascii, reserved);
            }
        }
        int unicodeNameRecordLength = in.readInt();
        String unicode = readUnicodeString(in, unicodeNameRecordLength);
        return new ASCIIUnicodeStringPair(ascii, unicode);
    }

    protected void readNameMapRecords(InputStream is,
                                           Map<String, String> moduleNames, Charset charset) throws IOException {
        //see 2.3.3 PROJECTwm Stream: Module Name Information
        //multibytecharstring
        String mbcs;
        String unicode;
        //arbitrary sanity threshold
        final int maxNameRecords = 10000;
        int records = 0;
        while (++records < maxNameRecords) {
            try {
                int b = IOUtils.readByte(is);
                //check for two 0x00 that mark end of record
                if (b == 0) {
                    b = IOUtils.readByte(is);
                    if (b == 0) {
                        return;
                    }
                }
                mbcs = readMBCS(b, is, charset);
            } catch (EOFException e) {
                return;
            }

            try {
                unicode = readUnicode(is);
            } catch (EOFException e) {
                return;
            }
            if (StringUtil.isNotBlank(mbcs) && StringUtil.isNotBlank(unicode)) {
                moduleNames.put(mbcs, unicode);
            }

        }
        log.warn("Hit max name records to read (" + maxNameRecords + "). Stopped early.");
    }

    private static String readUnicode(InputStream is) throws IOException {
        //reads null-terminated unicode string
        try (UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()) {
            int b0 = IOUtils.readByte(is);
            int b1 = IOUtils.readByte(is);

            int read = 2;
            while ((b0 + b1) != 0 && read < MAX_STRING_LENGTH) {
                bos.write(b0);
                bos.write(b1);
                b0 = IOUtils.readByte(is);
                b1 = IOUtils.readByte(is);
                read += 2;
            }
            if (read >= MAX_STRING_LENGTH) {
                log.warn("stopped reading unicode name after {} bytes", read);
            }
            return bos.toString(StandardCharsets.UTF_16LE);
        }
    }

    private static String readMBCS(int firstByte, InputStream is, Charset charset) throws IOException {
        try (UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()) {
            int len = 0;
            int b = firstByte;
            while (b > 0 && len < MAX_STRING_LENGTH) {
                ++len;
                bos.write(b);
                b = IOUtils.readByte(is);
            }
            return bos.toString(charset);
        }
    }

    /**
     * Read {@code length} bytes of MBCS (multi-byte character set) characters from the stream
     *
     * @param stream the inputstream to read from
     * @param length number of bytes to read from stream
     * @param charset the character set encoding of the bytes in the stream
     * @return a java String in the supplied character set
     * @throws IOException If reading from the stream fails
     */
    private static String readString(InputStream stream, int length, Charset charset) throws IOException {
        byte[] buffer = IOUtils.safelyAllocate(length, MAX_STRING_LENGTH);
        int bytesRead = IOUtils.readFully(stream, buffer);
        if (bytesRead != length) {
            throw new IOException("Tried to read: "+length +
                    ", but could only read: "+bytesRead);
        }
        return new String(buffer, 0, length, charset);
    }

    protected void readProjectProperties(DocumentInputStream dis,
                                         Map<String, String> moduleNameMap, ModuleMap modules) throws IOException {
        InputStreamReader reader = new InputStreamReader(dis, modules.charset);
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[512];
        int read;
        while ((read = reader.read(buffer)) >= 0) {
            builder.append(buffer, 0, read);
        }
        String properties = builder.toString();
        //the module name map names should be in exactly the same order
        //as the module names here. See 2.3.3 PROJECTwm Stream.
        //At some point, we might want to enforce that.
        for (String line : properties.split("\r\n|\n\r")) {
            if (!line.startsWith("[")) {
                String[] tokens = line.split("=");
                if (tokens.length > 1 && tokens[1].length() > 1
                        && tokens[1].startsWith("\"") && tokens[1].endsWith("\"")) {
                    // Remove any double quotes
                    tokens[1] = tokens[1].substring(1, tokens[1].length() - 1);
                }
                if ("Document".equals(tokens[0]) && tokens.length > 1) {
                    String mn = tokens[1].substring(0, tokens[1].indexOf("/&H"));
                    ModuleImpl module = getModule(mn, moduleNameMap, modules);
                    if (module != null) {
                        module.moduleType = ModuleType.Document;
                    } else {
                        log.warn("couldn't find module with name: {}", mn);
                    }
                } else if ("Module".equals(tokens[0]) && tokens.length > 1) {
                    ModuleImpl module = getModule(tokens[1], moduleNameMap, modules);
                    if (module != null) {
                        module.moduleType = ModuleType.Module;
                    } else {
                        log.warn("couldn't find module with name: {}", tokens[1]);
                    }
                } else if ("Class".equals(tokens[0]) && tokens.length > 1) {
                    ModuleImpl module = getModule(tokens[1], moduleNameMap, modules);
                    if (module != null) {
                        module.moduleType = ModuleType.Class;
                    } else {
                        log.warn("couldn't find module with name: {}", tokens[1]);
                    }
                }
            }
        }
    }
    //can return null!
    private ModuleImpl getModule(String moduleName, Map<String, String> moduleNameMap, ModuleMap moduleMap) {
        if (moduleNameMap.containsKey(moduleName)) {
            return moduleMap.get(moduleNameMap.get(moduleName));
        }
        return moduleMap.get(moduleName);
    }

    private String readUnicodeString(RLEDecompressingInputStream in, int unicodeNameRecordLength) throws IOException {
        byte[] buffer = IOUtils.safelyAllocate(unicodeNameRecordLength, MAX_STRING_LENGTH);
        int bytesRead = IOUtils.readFully(in, buffer);
        if (bytesRead != unicodeNameRecordLength) {
            throw new EOFException();
        }
        return new String(buffer, StringUtil.UTF16LE);
    }

    /**
     * Sometimes the offset record in the dirstream is incorrect, but the macro can still be found.
     * This will try to find the first RLEDecompressing stream that starts with "Attribute".
     * This relies on some, er, heuristics, admittedly.
     *
     * @param is full module inputstream to read
     * @return uncompressed bytes if found, {@code null} otherwise
     * @throws IOException for a true IOException copying the is to a byte array
     */
    private static byte[] findCompressedStreamWBruteForce(InputStream is) throws IOException {
        //buffer to memory for multiple tries
        byte[] compressed = IOUtils.toByteArray(is);
        byte[] decompressed = null;
        for (int i = 0; i < compressed.length; i++) {
            if (compressed[i] == 0x01 && i < compressed.length-1) {
                int w = LittleEndian.getUShort(compressed, i+1);
                if (w <= 0 || (w & 0x7000) != 0x3000) {
                    continue;
                }
                decompressed = tryToDecompress(UnsynchronizedByteArrayInputStream.builder().
                        setByteArray(compressed).
                        setOffset(i).
                        setLength(compressed.length - i).
                        get());
                if (decompressed != null) {
                    if (decompressed.length > 9) {
                        //this is a complete hack.  The challenge is that there
                        //can be many 0 length or junk streams that are uncompressed
                        //look in the first 20 characters for "Attribute"
                        int firstX = Math.min(20, decompressed.length);
                        String start = new String(decompressed, 0, firstX, StringUtil.WIN_1252);
                        if (start.contains("Attribute")) {
                            return decompressed;
                        }
                    }
                }
            }
        }
        return decompressed;
    }

    private static byte[] tryToDecompress(InputStream is) {
        try (RLEDecompressingInputStream ris = new RLEDecompressingInputStream(is)) {
            return IOUtils.toByteArray(ris);
        } catch (IllegalArgumentException | IOException | IllegalStateException e){
            return null;
        }
    }
}
