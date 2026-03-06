/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */

package org.docx4j.openpackaging.parts.WordprocessingML;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static java.util.Map.entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.org.apache.poi.hpsf.ClassID;
import org.docx4j.org.apache.poi.hpsf.ClassIDPredefined;
import org.docx4j.org.apache.poi.poifs.dev.POIFSViewEngine;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode;
import org.docx4j.org.apache.poi.poifs.filesystem.DocumentEntry;
import org.docx4j.org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.docx4j.org.apache.poi.poifs.filesystem.DocumentNode;
import org.docx4j.org.apache.poi.poifs.filesystem.Entry;
import org.docx4j.org.apache.poi.poifs.filesystem.FileMagic;
import org.docx4j.org.apache.poi.poifs.filesystem.Ole10Native;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.docx4j.org.apache.poi.poifs.filesystem.Ole10Native.EncodingMode;
import org.docx4j.org.apache.poi.util.IOUtils;


/**
 * You can use oleObjectBinaryPart.setBinaryData( ByteBuffer.wrap(bytes) ); to
 * populate this from a byte[]
 * 
 * An OleObjectBinaryPart is a binary container for Object Linking and
 * Embedding (OLE) data. It essentially stores a "snapshot" of a foreign
 * application's data so that Word can hand it back to that application when you
 * double-click it.
 * 
 * Here are the most common things you’ll find stored in this part:
 * 
 * 1. Embedded Excel Spreadsheets This is the most frequent resident of an OLE
 * binary. When you go to Insert > Object > Excel Worksheet, Word doesn't just
 * copy the text; it embeds the entire logic of an Excel file.
 * 
 * What's inside: The cells, formulas, formatting, and even hidden sheets from
 * that specific Excel object.
 * 
 * 2. Legacy Equations (Microsoft Equation 3.0) While modern Word uses Office
 * Math (MathML), older documents or those using the "Equation 3.0" editor store
 * math formulas as OLE objects.
 * 
 * Note: Because of security vulnerabilities, Microsoft has disabled the
 * execution of many of these older OLE objects in recent years, but the .bin
 * files often remain in older documents.
 * 
 * 3. Adobe Acrobat Documents (PDFs) If you drag and drop a PDF into a Word
 * document as an icon, Word creates an oleObject.bin.
 * 
 * The Catch: The .bin file contains the actual PDF data. When you double-click
 * the icon in Word, it extracts that binary data and asks your PDF reader to
 * open it.
 * 
 * 4. Third-Party Specialized Data Any program that supports the OLE standard
 * can "park" its data here. 
 * 
 * Why is it a .bin part? Microsoft uses a format called Compound File Binary
 * Format (CFBF)—informally known as "a file system within a file."
 * 
 * Because Word doesn't know how to read a CFBF natively, it treats the data 
 * as a "black box." It stores the binary
 * "blob" and keeps a record of which program (the Class ID or CLSID) is
 * responsible for opening it.
 * 
 * Security Warning Because .bin files can execute code via their host
 * applications (like Excel macros or old Equation Editor exploits), they are a
 * common vector for malware. If you find an oleObject1.bin in a document from
 * an untrusted source, it is the most likely place for a "malicious payload" to
 * be hiding. 
 * 
 * See further Microsoft specs [MS-OLEDS] and [MS-CFB]
 * 
 * 
 * @author jharrop
 *
 */
public class OleObjectBinaryPart extends BinaryPart {

	private static Logger log = LoggerFactory.getLogger(OleObjectBinaryPart.class);		
	
	public OleObjectBinaryPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();				
	}

	
	public OleObjectBinaryPart() throws InvalidFormatException {
		super( new PartName("/word/embeddings/oleObject1.bin") );
		init();				
	}
	
	public void init() {
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.OFFICEDOCUMENT_OLE_OBJECT));
			// should be this, unless it contains eg a doc stored directly (ie a non-generic OLE object)

		// Used when this Part is added to a rels
		setRelationshipType(Namespaces.OLE_OBJECT);
		
		
	}
	
	private static final String MIME_OCTET = "application/octet-stream";

	POIFSFileSystem fs;
	public POIFSFileSystem getFs() throws IOException {
		if (fs==null) {
			initPOIFSFileSystem();
		}
		return fs;
	}
	
	public void initPOIFSFileSystem() throws IOException {
		
		if (getBuffer()!=null) {

			//fs = new POIFSFileSystem( org.docx4j.utils.BufferUtil.newInputStream(bb) );
			// the above seems to be calling methods which aren't implemented,
			// so, for now, brute force..

			log.info("initing POIFSFileSystem from existing data");
			ByteArrayInputStream bais = new ByteArrayInputStream(this.getBytes());
			fs = new POIFSFileSystem(bais);
			
		} else {

			log.info("creating new empty POIFSFileSystem");
			fs = new POIFSFileSystem();
			writePOIFSFileSystem();
		}
	}
	
	/**
	 * Write any changes which have been made to POIFSFileSystem,
	 * to the underlying ByteBuffer.  This is necessary if the changes
	 * are to be persisted.
	 * 
	 * @throws IOException
	 */
	public void writePOIFSFileSystem() throws IOException {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 

		getFs().writeFilesystem(baos);
		
		// Need to put this is bb
		byte[] bytes = baos.toByteArray();
		
		// java.nio.ByteBuffer bb contains the data
		setBinaryData( ByteBuffer.wrap(bytes) );
		
	}
	
	
    
    
    public void viewFile(boolean verbose) throws IOException
    {
    	viewFile(System.out, verbose);
    }

    /**
     * @param os
     * @param verbose
     * @throws IOException
     * @since 3.0.0
     */
    public void viewFile(OutputStream os, boolean verbose) throws IOException
    {
    	String indent="";
    	boolean withSizes = true;    	
    	displayDirectory(getFs().getRoot(), os, indent, withSizes);
    	
    	if (verbose) {
	        List strings = POIFSViewEngine.inspectViewable(fs, true, 0, "  ");
			Iterator iter = strings.iterator();
	
			while (iter.hasNext()) {
				os.write( ((String)iter.next()).getBytes());
			}
    	}
    }
    
    /**
     * Adapted from org.docx4j.org.apache.poi.poifs.dev.POIFSLister
     * @param dir
     * @param indent
     * @param withSizes
     * @throws IOException 
     * 
     */
    private void displayDirectory(DirectoryNode dir, OutputStream os, String indent, boolean withSizes) throws IOException {
        os.write((indent + dir.getName() + " -").getBytes());
        String newIndent = indent + "  ";

        boolean hadChildren = false;
        for(Iterator<Entry> it = dir.getEntries(); it.hasNext();) {
           hadChildren = true;
           Entry entry = it.next();
           if (entry instanceof DirectoryNode) {
              displayDirectory((DirectoryNode) entry, os, newIndent, withSizes);
           } else {
              DocumentNode doc = (DocumentNode) entry;
              String name = doc.getName();
              String size = "";
              if (name.charAt(0) < 10) {
                 String altname = "(0x0" + (int) name.charAt(0) + ")" + name.substring(1);
                 name = name.substring(1) + " <" + altname + ">";
              }
              if (withSizes) {
                 size = " [" + doc.getSize() + " / 0x" + 
                        Integer.toHexString(doc.getSize()) + "]";
              }
              os.write((newIndent + name + size + "\n").getBytes() );
           }
        }
        if (!hadChildren) {
        	os.write((newIndent + "(no children)").getBytes());
        }
     }
          
    
    private static boolean USE_TIKA_IF_AVAILABLE = true;
    
    /**
     * Primarily for test purposes, you'd use it if you had it.
     */
    public static void setUSE_TIKA_IF_AVAILABLE(boolean val) {
		USE_TIKA_IF_AVAILABLE = val;
	}

    private static boolean TRUST_CLSID = true;

    /**
     * Primarily for test purposes, trust unless proven otherwise.
     */
    public static void setTRUST_CLSID(boolean val) {
		TRUST_CLSID = val;
	}


	/**
     * Returns a mime type
     * @return
     * @since 11.5.10
     */
    public String detectMimeType() {
    	
        try (POIFSFileSystem fs = getFs()) {
        	
            DirectoryNode root = fs.getRoot();
            
            if (TRUST_CLSID) {
	            ClassID clsid = root.getStorageClsid();
	            log.info("clsid: " + clsid.toString());
				// Note, it could be malformed in legacy documents—usually
				// due to 16-bit to 32-bit conversion errors or "Quick Save" glitches in older
				// versions of Office, but in this case ClassIDPredefined.lookup will return null.
	            ClassIDPredefined predefined = ClassIDPredefined.lookup(clsid);
	            if ( predefined==null) {
	                log.info(".. not predefined");            	
	            } else if (predefined.equals(ClassIDPredefined.OLE_V1_PACKAGE) ) {
	                log.info("OLE_V1_PACKAGE, need to sniff");            	            	
	            } else if (predefined.getContentType()!=null && !predefined.getContentType().equals(MIME_OCTET)) {
	        		return predefined.getContentType();            		
	        	} else {
	        		log.info("No mime type for " + predefined.name());
	        	}
            }
            
            CompObjInfo compObjInfo = getCompObj(root);
            
			// If the ProgID is exactly Packager, this is a signal to stop looking at the
			// CompObj and immediately move to the Ole10Native stream. Packager is the
			// wrapper used when a user drags any random file (like a .zip or .exe) into a
			// document.
            if (compObjInfo !=null && compObjInfo.getProgId() != null) {
            	// We don't have to be comprehensive here, since the sniffing whilst slower
            	// is more likely to be accurate
                if (compObjInfo.getProgId() != null
                		&&  (compObjInfo.getProgId().contains("AcroExch") 
                		  || compObjInfo.getProgId().contains("PDF")))
                {
                	return "application/pdf";
                }
                if (compObjInfo.getUserType() != null
                		&&  /* pdf dragged onto recent Word */ compObjInfo.getUserType().contains("Acrobat") )
                {
                	return "application/pdf";
                }
                // would need to distinguish between for example Word.Document.8 and Word.Document.12                
            }

            // FAST PATH: Check Signatures (No byte-reading required, very fast)
            for (String sig : OFFICE_SIGNATURES) {
                if (root.hasEntry(sig)) {
                    return mapSignatureToMime(sig);
                }
            }
            
            if (root.hasEntry("\u0001Ole10Native")) {
	            try {
		            Ole10Native ole10Native = Ole10Native.createFromEmbeddedOleObject(root);
		            log.info("EncodingMode: " + ole10Native.getMode().name() ); 
		            
		            if (ole10Native.getMode().equals(EncodingMode.parsed)) {
		            	// the data is stored in parsed format - including label, command, etc.
		                log.info("filename: " + ole10Native.getFileName() ); 
		                log.info("label: " + ole10Native.getLabel()); 
		                log.info("command: " + ole10Native.getCommand() );             	
		            }
		            
		            // No input stream in this case :-(
		            byte[] dataBuffer = ole10Native.getDataBuffer();
		            
		            // Will use tika-core if present
		            String mime = detectMimeType(dataBuffer);
	            	log.info(mime);
		            
	            	// prefer sniffing result over compObj unless its generic
	            	// for example, for paintbrush image/bmp over image/x-wmf
	            	if (!mime.equals(MIME_OCTET)) {
	            		return mime;
	            	}
            		// CompObj could potentially provide us something more useful,
            		// so fall through
	            
	            } catch (Exception e) {
	            	log.info("Couldn't extract OLE Native", e);
	            }
            }

            // If it has Ole10Native (but this failed above) OR CONTENTS, sniff the raw bytes
            // using Tika if present, or FileMagic
            String[] fallbackStreams = {"\u0001Ole10Native", "CONTENTS", "Contents"};
            for (String streamName : fallbackStreams) {
                if (root.hasEntry(streamName)) {
                    try (DocumentInputStream dis = root.createDocumentInputStream(streamName)) {
                    	log.info("Sniffing " + streamName);
                    	String mime = detectMimeType(dis);
    	            	if (!mime.equals(MIME_OCTET)) {
    	            		return mime;
    	            	}
    	            	// TODO if the result is text/plain, look at filename or command,
    	            	// for eg .csv
                    } catch (Exception ignored) {}
                }
            }                
            return MIME_OCTET; 
			// TODO compObjInfo contains info in userType (e.g., "Microsoft Word Document")
			// and progId (e.g., "Word.Document.8"), which we could potentially use here. 
            
        } catch (Exception e) {
            System.err.println("Error reading OLE file: " + e.getMessage());
        }        
        return null;  // Couldn't read it
    }
    
    /**
     * Currently this only detects "application/pdf".  Everything else falls through to sniffing
     * 
     * @param root
     * @return
     * @throws Exception
     */
    private CompObjInfo getCompObj(DirectoryNode root) throws Exception /* IO, File */ {
    	
        if (!root.hasEntry("\u0001CompObj")) {
            log.warn("No CompObj stream found.");
            return null;
        }

        DocumentEntry compObjEntry = (DocumentEntry) root.getEntry("\u0001CompObj");
        try (DocumentInputStream dis = new DocumentInputStream(compObjEntry)) {
            byte[] data = IOUtils.toByteArray(dis);
            
            // CompObj format: 
            // Header (28-32 bytes) -> Length of Type Name -> Type Name -> Length of ProgID -> ProgID

            ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

            return new CompObjInfo(buffer);
            
            
        } catch (Exception e) {
            log.debug("Failed to parse CompObj structure: {}", e.getMessage());
        }
        return null;
    }
    
    private static class CompObjInfo {
    	
        private final String userType;
        private final String progId;

        public CompObjInfo(ByteBuffer buffer) {
        	
            // 1. the Header (usually 28 bytes)
            // 4 bytes: byte order, 4 bytes: format, 4 bytes: OS version, 16 bytes: Reserved/CLSID
                        
            if (buffer.remaining() < 28) {
            	this.userType = null;
            	this.progId = null;
            	return;
            }
            buffer.position(28);

            // 2. Read 'User Type' String (e.g., "Acrobat Document")
            this.userType = readLengthPrefixedString(buffer);
            
            // 3. Read 'ProgID' String (e.g., "AcroExch.Document.DC")
            this.progId = readLengthPrefixedString(buffer);

            log.info("CompObj - UserType: {}, ProgID: {}", userType, progId);
        	
        }
        
//        public CompObjInfo(ClassID clsid, String userType, String progId) {
//            this.clsid = clsid;
//            this.userType = userType;
//            this.progId = progId;
//        }

        public String getUserType() { return userType; }
        public String getProgId() { return progId; }
        
        private String readLengthPrefixedString(ByteBuffer buffer) {
        	
            if (buffer.remaining() < 4) return null;
            int length = buffer.getInt(); // OLE strings in CompObj often have a 4-byte length prefix
            
            if (length <= 0 || length > buffer.remaining()) return null;

            byte[] strBytes = new byte[length];
            buffer.get(strBytes);
            
            // OLE strings are null-terminated; strip the \0
            return new String(strBytes, StandardCharsets.US_ASCII).trim();
        }    
        
    }

    private static final Set<String> OFFICE_SIGNATURES = Set.of(
    	    "Workbook", "Book",          // Excel
    	    "WordDocument",               // Word
    	    "PowerPoint Document",        // PowerPoint
    	    "VisioDocument",              // Visio
    	    "__properties_version1.0",    // Outlook MSG
    	    "MSProject"                   // Project
    	);   
    
 // Define the signatures as a static constant map
    private static final Map<String, String> SIGNATURE_TO_MIME;

    static {
        Map<String, String> map = new java.util.HashMap<>();
        
        // Excel
        map.put("Workbook", "application/vnd.ms-excel");
        map.put("Book", "application/vnd.ms-excel");
        
        // Word
        map.put("WordDocument", "application/msword");
        
        // PowerPoint
        map.put("PowerPoint Document", "application/vnd.ms-powerpoint");
//        map.put("Current User", "application/vnd.ms-powerpoint");
        
        // Visio
        map.put("VisioDocument", "application/vnd.visio");
        
        // Outlook (Email MSG)
        map.put("__properties_version1.0", "application/vnd.ms-outlook");
        
        // Project
        map.put("MSProject", "application/vnd.ms-project");
        
//        // Publisher
//        map.put("Contents", "application/x-mspublisher");
        
        SIGNATURE_TO_MIME = Collections.unmodifiableMap(map);
    }

    /**
     * Maps a known OLE stream signature to its corresponding MIME type.
     * @param signature The name of the stream found in the OLE directory.
     * @return The MIME type string, or a generic fallback if unknown.
     */
    public static String mapSignatureToMime(String signature) {
        if (signature == null) {
            return MIME_OCTET;
        }
        
        // Use getOrDefault for a clean Java 11 fallback
        return SIGNATURE_TO_MIME.getOrDefault(signature, MIME_OCTET);
    } 
    

	/**
     * Attempts to use Tika via reflection. 
     * Falls back to FileMagic if Tika is missing.
     */
    public String detectMimeType(byte[] payload) {
    	
    	if (USE_TIKA_IF_AVAILABLE) {
	        try {
	            // 1. Try to load the Tika class
	            Class<?> tikaClass = Class.forName("org.apache.tika.Tika");
	            Object tikaInstance = tikaClass.getDeclaredConstructor().newInstance();
	            
	            // 2. Locate the detect(byte[]) method
	            Method detectMethod = tikaClass.getMethod("detect", byte[].class);
	            
	            // 3. Invoke and return result
	            return (String) detectMethod.invoke(tikaInstance, (Object) payload);
	            
	        } catch (ClassNotFoundException e) {
	            log.info("Tika not found on classpath. Using fallback identification.");
	        } catch (Exception e) {
	        	log.error("Error invoking Tika via reflection", e);
	        }
    	}
        
        // Fallback identification (Manual "Magic Byte" sniffing)
        try {
			return fileMagicMimeType(new ByteArrayInputStream(payload));
		} catch (IOException e) {
			log.error("FileMagic failure", e);
            return MIME_OCTET;
		}
    }    
    
    /**
     * Attempts to use Tika via reflection. 
     * Falls back to FileMagic if Tika is missing.
     */
    public String detectMimeType(InputStream stream) {
    	
    	if (USE_TIKA_IF_AVAILABLE) {
	        try {
	            // 1. Load Classes
	            Class<?> tikaClass = Class.forName("org.apache.tika.Tika");
	            Class<?> metadataClass = Class.forName("org.apache.tika.metadata.Metadata");
	            
	            // 2. Instantiate Tika and Metadata
	            Object tikaInstance = tikaClass.getDeclaredConstructor().newInstance();
	            Object metadataInstance = metadataClass.getDeclaredConstructor().newInstance();
	            
	            // 3. Locate the detect(InputStream, Metadata) method
	            Method detectMethod = tikaClass.getMethod("detect", InputStream.class, metadataClass);
	            
	            // 4. Invoke
	            return (String) detectMethod.invoke(tikaInstance, stream, metadataInstance);
	            
	        } catch (ClassNotFoundException e) {
	            log.info("Tika not found on classpath. Using fallback identification.");
	        } catch (Exception e) {
	            log.error("Reflection error invoking Tika", e);
	        }
    	}
        
        // Fallback identification (Manual "Magic Byte" sniffing)
        try {
			return fileMagicMimeType(stream);
		} catch (IOException e) {
			log.error("FileMagic failure", e);
            return MIME_OCTET;
		}
        
    }    
	private  String fileMagicMimeType(InputStream is) throws IOException {
		
	    // FileMagic.valueOf() needs an InputStream that supports mark/reset
    	log.info("Using FileMagic");
	     return getMimeType(FileMagic.valueOf(is));
	}
	
	private static final Map<FileMagic, String> MAGIC_MIME_MAP = Map.ofEntries(
	        entry(FileMagic.OLE2,    "application/x-ole-storage"), // Legacy Office Container
	        entry(FileMagic.OOXML,   "application/x-tika-ooxml"),    // application/vnd.openxmlformats-officedocument isn't common
	        entry(FileMagic.XML,     "application/xml"),
	        entry(FileMagic.BIFF2,   "application/vnd.ms-excel"),
	        entry(FileMagic.BIFF3,   "application/vnd.ms-excel"),
	        entry(FileMagic.BIFF4,   "application/vnd.ms-excel"),
	        entry(FileMagic.MSWRITE, "application/x-mswrite"),
	        entry(FileMagic.RTF,     "application/rtf"),
	        entry(FileMagic.PDF,     "application/pdf"),
	        entry(FileMagic.HTML,    "text/html"),
	        entry(FileMagic.WORD2,   "application/msword"),
	        entry(FileMagic.JPEG,    "image/jpeg"),
	        entry(FileMagic.GIF,     "image/gif"),
	        entry(FileMagic.PNG,     "image/png"),
	        entry(FileMagic.TIFF,    "image/tiff"),
	        entry(FileMagic.WMF,     "image/x-wmf"),
	        entry(FileMagic.EMF,     "image/x-emf"),
	        entry(FileMagic.BMP,     "image/bmp"),
	        entry(FileMagic.UNKNOWN, MIME_OCTET)
	    );

	    /**
	     * Converts a FileMagic entry to its primary MIME type.
	     */
	    public static String getMimeType(FileMagic magic) {
	        return MAGIC_MIME_MAP.getOrDefault(magic, MIME_OCTET);
	    }	
}
