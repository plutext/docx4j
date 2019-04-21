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

package org.docx4j.org.apache.poi.poifs.crypt;

import java.io.IOException;
import java.nio.charset.Charset;



//import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.crypt.standard.EncryptionRecord;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.docx4j.org.apache.poi.poifs.filesystem.DocumentEntry;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSWriterEvent;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSWriterListener;
import org.docx4j.org.apache.poi.util.LittleEndianByteArrayOutputStream;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInput;
import org.docx4j.org.apache.poi.util.LittleEndianOutput;
import org.docx4j.org.apache.poi.util.StringUtil;

public class DataSpaceMapUtils {
    public static void addDefaultDataSpace(DirectoryEntry dir) throws IOException {
        DataSpaceMapEntry dsme = new DataSpaceMapEntry(
                new int[]{ 0 }
              , new String[]{ Decryptor.DEFAULT_POIFS_ENTRY }
              , "StrongEncryptionDataSpace"
          );
          DataSpaceMap dsm = new DataSpaceMap(new DataSpaceMapEntry[]{dsme});
          createEncryptionEntry(dir, "\u0006DataSpaces/DataSpaceMap", dsm);

          DataSpaceDefinition dsd = new DataSpaceDefinition(new String[]{ "StrongEncryptionTransform" });
          createEncryptionEntry(dir, "\u0006DataSpaces/DataSpaceInfo/StrongEncryptionDataSpace", dsd);

          TransformInfoHeader tih = new TransformInfoHeader(
                1
              , "{FF9A3F03-56EF-4613-BDD5-5A41C1D07246}"
              , "Microsoft.Container.EncryptionTransform"
              , 1, 0, 1, 0, 1, 0
          );
          IRMDSTransformInfo irm = new IRMDSTransformInfo(tih, 0, null);
          createEncryptionEntry(dir, "\u0006DataSpaces/TransformInfo/StrongEncryptionTransform/\u0006Primary", irm);
          
          DataSpaceVersionInfo dsvi = new DataSpaceVersionInfo("Microsoft.Container.DataSpaces", 1, 0, 1, 0, 1, 0);
          createEncryptionEntry(dir, "\u0006DataSpaces/Version", dsvi);
    }
    
    public static DocumentEntry createEncryptionEntry(DirectoryEntry dir, String path, EncryptionRecord out) throws IOException {
        String parts[] = path.split("/");
        for (int i=0; i<parts.length-1; i++) {
            dir = dir.hasEntry(parts[i])
                ? (DirectoryEntry)dir.getEntry(parts[i])
                : dir.createDirectory(parts[i]);
        }
        
        final byte buf[] = new byte[5000];        
        LittleEndianByteArrayOutputStream bos = new LittleEndianByteArrayOutputStream(buf, 0);
        out.write(bos);
        
        String fileName = parts[parts.length-1];
        
        if (dir.hasEntry(fileName)) {
            dir.getEntry(fileName).delete();
        }
        
        return dir.createDocument(fileName, bos.getWriteIndex(), new POIFSWriterListener(){
            public void processPOIFSWriterEvent(POIFSWriterEvent event) {
                try {
                    event.getStream().write(buf, 0, event.getLimit());
                } catch (IOException e) {
                    throw new EncryptedDocumentException(e);
                }
            }
        });
    }   
    
    public static class DataSpaceMap implements EncryptionRecord {
        DataSpaceMapEntry entries[];
        
        public DataSpaceMap(DataSpaceMapEntry entries[]) {
            this.entries = entries;
        }
        
        public DataSpaceMap(LittleEndianInput is) {
            @SuppressWarnings("unused")
            int length = is.readInt();
            int entryCount = is.readInt();
            entries = new DataSpaceMapEntry[entryCount];
            for (int i=0; i<entryCount; i++) {
                entries[i] = new DataSpaceMapEntry(is);
            }
        }
    
        public void write(LittleEndianByteArrayOutputStream os) {
            os.writeInt(8);
            os.writeInt(entries.length);
            for (DataSpaceMapEntry dsme : entries) {
                dsme.write(os);
            }
        }
    }
    
    public static class DataSpaceMapEntry implements EncryptionRecord {
        int referenceComponentType[];
        String referenceComponent[];
        String dataSpaceName;
        
        public DataSpaceMapEntry(int referenceComponentType[], String referenceComponent[], String dataSpaceName) {
            this.referenceComponentType = referenceComponentType;
            this.referenceComponent = referenceComponent;
            this.dataSpaceName = dataSpaceName;
        }
        
        public DataSpaceMapEntry(LittleEndianInput is) {
            @SuppressWarnings("unused")
            int length = is.readInt();
            int referenceComponentCount = is.readInt();
            referenceComponentType = new int[referenceComponentCount];
            referenceComponent = new String[referenceComponentCount];
            for (int i=0; i<referenceComponentCount; i++) {
                referenceComponentType[i] = is.readInt();
                referenceComponent[i] = readUnicodeLPP4(is);
            }
            dataSpaceName = readUnicodeLPP4(is);
        }
        
        public void write(LittleEndianByteArrayOutputStream os) {
            int start = os.getWriteIndex();
            LittleEndianOutput sizeOut = os.createDelayedOutput(LittleEndianConsts.INT_SIZE);
            os.writeInt(referenceComponent.length);
            for (int i=0; i<referenceComponent.length; i++) {
                os.writeInt(referenceComponentType[i]);
                writeUnicodeLPP4(os, referenceComponent[i]);
            }
            writeUnicodeLPP4(os, dataSpaceName);
            sizeOut.writeInt(os.getWriteIndex()-start);
        }
    }
    
    public static class DataSpaceDefinition implements EncryptionRecord {
        String transformer[];
        
        public DataSpaceDefinition(String transformer[]) {
            this.transformer = transformer;
        }
        
        public DataSpaceDefinition(LittleEndianInput is) {
            @SuppressWarnings("unused")
            int headerLength = is.readInt();
            int transformReferenceCount = is.readInt();
            transformer = new String[transformReferenceCount];
            for (int i=0; i<transformReferenceCount; i++) {
                transformer[i] = readUnicodeLPP4(is);
            }
        }
        
        public void write(LittleEndianByteArrayOutputStream bos) {
            bos.writeInt(8);
            bos.writeInt(transformer.length);
            for (String str : transformer) {
                writeUnicodeLPP4(bos, str);
            }
        }
    }
    
    public static class IRMDSTransformInfo implements EncryptionRecord {
        TransformInfoHeader transformInfoHeader;
        int extensibilityHeader;
        String xrMLLicense;
        
        public IRMDSTransformInfo(TransformInfoHeader transformInfoHeader, int extensibilityHeader, String xrMLLicense) {
            this.transformInfoHeader = transformInfoHeader;
            this.extensibilityHeader = extensibilityHeader;
            this.xrMLLicense = xrMLLicense;
        }
        
        public IRMDSTransformInfo(LittleEndianInput is) {
            transformInfoHeader = new TransformInfoHeader(is);
            extensibilityHeader = is.readInt();
            xrMLLicense = readUtf8LPP4(is);
            // finish with 0x04 (int) ???
        }
        
        public void write(LittleEndianByteArrayOutputStream bos) {
            transformInfoHeader.write(bos);
            bos.writeInt(extensibilityHeader);
            writeUtf8LPP4(bos, xrMLLicense);
            bos.writeInt(4); // where does this 4 come from???
        }
    }
    
    public static class TransformInfoHeader implements EncryptionRecord {
        int transformType;
        String transformerId;
        String transformerName;
        int readerVersionMajor = 1, readerVersionMinor = 0;
        int updaterVersionMajor = 1, updaterVersionMinor = 0;
        int writerVersionMajor = 1, writerVersionMinor = 0;

        public TransformInfoHeader(
            int transformType,
            String transformerId,
            String transformerName,
            int readerVersionMajor, int readerVersionMinor,
            int updaterVersionMajor, int updaterVersionMinor,
            int writerVersionMajor, int writerVersionMinor                
        ){
            this.transformType = transformType;
            this.transformerId = transformerId;
            this.transformerName = transformerName;
            this.readerVersionMajor = readerVersionMajor;
            this.readerVersionMinor = readerVersionMinor;
            this.updaterVersionMajor = updaterVersionMajor;
            this.updaterVersionMinor = updaterVersionMinor;
            this.writerVersionMajor = writerVersionMajor;
            this.writerVersionMinor = writerVersionMinor;
        }
        
        public TransformInfoHeader(LittleEndianInput is) {
            @SuppressWarnings("unused")
            int length = is.readInt();
            transformType = is.readInt();
            transformerId = readUnicodeLPP4(is);
            transformerName = readUnicodeLPP4(is);
            readerVersionMajor = is.readShort();
            readerVersionMinor = is.readShort();
            updaterVersionMajor = is.readShort();
            updaterVersionMinor = is.readShort();
            writerVersionMajor = is.readShort();
            writerVersionMinor = is.readShort();
        }
        
        public void write(LittleEndianByteArrayOutputStream bos) {
            int start = bos.getWriteIndex();
            LittleEndianOutput sizeOut = bos.createDelayedOutput(LittleEndianConsts.INT_SIZE);
            bos.writeInt(transformType);
            writeUnicodeLPP4(bos, transformerId);
            sizeOut.writeInt(bos.getWriteIndex()-start);
            writeUnicodeLPP4(bos, transformerName);
            bos.writeShort(readerVersionMajor);
            bos.writeShort(readerVersionMinor); 
            bos.writeShort(updaterVersionMajor);
            bos.writeShort(updaterVersionMinor);
            bos.writeShort(writerVersionMajor); 
            bos.writeShort(writerVersionMinor); 
        }
    }
    
    public static class DataSpaceVersionInfo implements EncryptionRecord {
        String featureIdentifier;
        int readerVersionMajor = 1, readerVersionMinor = 0;
        int updaterVersionMajor = 1, updaterVersionMinor = 0;
        int writerVersionMajor = 1, writerVersionMinor = 0;
        
        public DataSpaceVersionInfo(LittleEndianInput is) {
            featureIdentifier = readUnicodeLPP4(is);
            readerVersionMajor = is.readShort();
            readerVersionMinor = is.readShort();
            updaterVersionMajor = is.readShort();
            updaterVersionMinor = is.readShort();
            writerVersionMajor = is.readShort();
            writerVersionMinor = is.readShort();
        }
        
        public DataSpaceVersionInfo(
            String featureIdentifier,
            int readerVersionMajor, int readerVersionMinor,
            int updaterVersionMajor, int updaterVersionMinor,
            int writerVersionMajor, int writerVersionMinor                
        ){
            this.featureIdentifier = featureIdentifier;
            this.readerVersionMajor = readerVersionMajor;
            this.readerVersionMinor = readerVersionMinor;
            this.updaterVersionMajor = updaterVersionMajor;
            this.updaterVersionMinor = updaterVersionMinor;
            this.writerVersionMajor = writerVersionMajor;
            this.writerVersionMinor = writerVersionMinor;
        }
        
        public void write(LittleEndianByteArrayOutputStream bos) {
            writeUnicodeLPP4(bos, featureIdentifier);
            bos.writeShort(readerVersionMajor);
            bos.writeShort(readerVersionMinor); 
            bos.writeShort(updaterVersionMajor);
            bos.writeShort(updaterVersionMinor);
            bos.writeShort(writerVersionMajor); 
            bos.writeShort(writerVersionMinor); 
        }
    }
    
    public static String readUnicodeLPP4(LittleEndianInput is) {
        int length = is.readInt();
        if (length%2 != 0) {
            throw new EncryptedDocumentException(
                "UNICODE-LP-P4 structure is a multiple of 4 bytes. "
                + "If Padding is present, it MUST be exactly 2 bytes long");
        }
        
        String result = StringUtil.readUnicodeLE(is, length/2);
        if (length%4==2) {
            // Padding (variable): A set of bytes that MUST be of the correct size such that the size of the 
            // UNICODE-LP-P4 structure is a multiple of 4 bytes. If Padding is present, it MUST be exactly 
            // 2 bytes long, and each byte MUST be 0x00.            
            is.readShort();
        }
        
        return result;
    }
    
    public static void writeUnicodeLPP4(LittleEndianOutput os, String string) {
        byte buf[] = StringUtil.getToUnicodeLE(string);
        os.writeInt(buf.length);
        os.write(buf);
        if (buf.length%4==2) {
            os.writeShort(0);
        }
    }

    public static String readUtf8LPP4(LittleEndianInput is) {
        int length = is.readInt();
        if (length == 0 || length == 4) {
            @SuppressWarnings("unused")
            int skip = is.readInt(); // ignore
            return length == 0 ? null : "";
        }
        
        byte data[] = new byte[length];
        is.readFully(data);

        // Padding (variable): A set of bytes that MUST be of correct size such that the size of the UTF-8-LP-P4
        // structure is a multiple of 4 bytes. If Padding is present, each byte MUST be 0x00. If 
        // the length is exactly 0x00000000, this specifies a null string, and the entire structure uses 
        // exactly 4 bytes. If the length is exactly 0x00000004, this specifies an empty string, and the 
        // entire structure also uses exactly 4 bytes
        int scratchedBytes = length%4;
        if (scratchedBytes > 0) {
            for (int i=0; i<(4-scratchedBytes); i++) {
                is.readByte();
            }
        }

        return new String(data, 0, data.length, Charset.forName("UTF-8"));
    }
    
    public static void writeUtf8LPP4(LittleEndianOutput os, String str) {
        if (str == null || "".equals(str)) {
            os.writeInt(str == null ? 0 : 4);
            os.writeInt(0);
        } else {
            byte buf[] = str.getBytes(Charset.forName("UTF-8"));
            os.writeInt(buf.length);
            os.write(buf);
            int scratchBytes = buf.length%4;
            if (scratchBytes > 0) {
                for (int i=0; i<(4-scratchBytes); i++) {
                    os.writeByte(0);
                }
            }
        }        
    }

}
