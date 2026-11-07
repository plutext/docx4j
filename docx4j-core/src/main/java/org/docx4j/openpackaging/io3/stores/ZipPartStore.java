/*
 *  Copyright 2012, Plutext Pty Ltd.
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
package org.docx4j.openpackaging.io3.stores;

import static java.lang.Math.toIntExact;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.commons.io.IOUtils;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.Docx4JRuntimeException;
import org.docx4j.openpackaging.exceptions.FileTooLargeException;
import org.docx4j.openpackaging.exceptions.PartTooLargeException;
import org.docx4j.openpackaging.io3.RepairMode;
import org.docx4j.openpackaging.packages.LoadFinding;
import org.docx4j.openpackaging.parts.*;
import org.docx4j.openpackaging.parts.WordprocessingML.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Load a zipped up package from a file or input stream;
 * save it to some output stream.
 *
 * @author jharrop
 * @since 3.0
 */
public class ZipPartStore implements PartStore {

	private static Logger log = LoggerFactory.getLogger(ZipPartStore.class);


	HashMap<String, ByteArray> partByteArrays;
	
	private static final long MAX_BYTES_Unzip_Error;
    private static final long MAX_UNCOMPRESSED_SIZE; 
    private static final double MAX_RATIO; 

    static {
		MAX_BYTES_Unzip_Error = Docx4jProperties.getPropertyLong("docx4j.openpackaging.parts.MAX_BYTES.unzip.error", 52428800);
		MAX_RATIO = Docx4jProperties.getPropertyLong("docx4j.openpackaging.parts.MAX_RATIO.unzip.error", 500);
		MAX_UNCOMPRESSED_SIZE = Docx4jProperties.getPropertyLong("docx4j.openpackaging.package.MAX_UNCOMPRESSED_SIZE.unzip.error", 52428800);   
		if (log.isInfoEnabled()) {
			if (MAX_BYTES_Unzip_Error>-1) {
				log.info("MAX_BYTES.unzip.error: " + (MAX_BYTES_Unzip_Error/(1024*1024)) + " MB");
			} else {
				log.info("MAX_BYTES.unzip.error: check disabled" );				
			}
			if (MAX_UNCOMPRESSED_SIZE > -1) {
				log.info("MAX_UNCOMPRESSED_SIZE.unzip.error: " + (MAX_UNCOMPRESSED_SIZE/(1024*1024)) + " MB");
			} else {
				log.info("MAX_UNCOMPRESSED_SIZE.unzip.error: check disabled" );				
			}
			if (MAX_RATIO > -1) {
				log.info("MAX_RATIO.unzip.error: " + MAX_RATIO);
			} else {
				log.info("MAX_RATIO.unzip.error: check disabled" );				
			}
		}
		/*  Potential TODOs:
				•	Enforce maxEntries
				•	Reject or limit handling of nested archives (e.g., zip-in-zip). If you allow them, enforce maxNestingDepth.
		*/
    }
	
	public ZipPartStore() {}    

	/** What this store's load read around (CR-075 D2); {@link org.docx4j.openpackaging.io3.Load3} hands it to the package. */
	private final List<LoadFinding> loadFindings = new ArrayList<LoadFinding>();

	/** What this store's load read around, in the order found: empty for a package with no duplicated entry. @since 17.3.6 */
	public List<LoadFinding> getLoadFindings() {
		return Collections.unmodifiableList(loadFindings);
	}

	public ZipPartStore(File f) throws Docx4JException {
		this(f, RepairMode.fromProperty());
	}

	/**
	 * As {@link #ZipPartStore(File)}, with what to do with entries of one name (WD REQ-206; CR-075 D3):
	 * {@link RepairMode#NONE} refuses them, naming the entry; {@link RepairMode#WORD} reads the first of
	 * each name in the central directory and records {@link LoadFinding#DUPLICATE_ENTRY}.
	 *
	 * @since 17.3.6 (CR-075 phase 1)
	 */
	public ZipPartStore(File f, RepairMode repair) throws Docx4JException {
				
		log.info("Filepath = " + f.getPath() );

		ZipFile zf = null;
		try {
			if (!f.exists()) {
				log.info( "Couldn't find " + f.getPath() );
			}
			zf = new ZipFile(f);
		} catch (IOException ioe) {
			ioe.printStackTrace() ;
			throw new Docx4JException("Couldn't get ZipFile", ioe);
		}
		warnIfBytesAfterCentralDirectory(f);

		partByteArrays = new HashMap<String, ByteArray>();
		try {
			Map<String, List<String>> duplicated = duplicated(entryNames(zf));
			refuseOrRecord(duplicated, repair);
			readEntries(zf, f);
			alias(duplicated);
		} finally {
			// At this point, we've finished with the zip file
			try {
				zf.close();
			} catch (IOException exc) {
				log.warn("Couldn't close " + f.getName() + ": " + exc.getMessage());
			}
		}
	}

	/** Each entry's name, in the central directory's order. */
	private static List<String> entryNames(ZipFile zf) {
		List<String> names = new ArrayList<String>();
		Enumeration<ZipArchiveEntry> entries = zf.getEntries();
		while (entries.hasMoreElements()) names.add(entries.nextElement().getName());
		return names;
	}

	/** A name as OPC compares part names, case-insensitively (WD REQ-206). */
	private static String folded(String name) {
		return name.toLowerCase(Locale.ROOT);
	}

	/**
	 * The names given more than once under OPC equivalence, each with its spellings in the order given,
	 * one for each entry: keyed by the folded name, in the order first met. Empty for an ordinary package.
	 * A directory entry is no part, so it doesn't count: archivers write them, and Word hasn't been asked.
	 */
	private static Map<String, List<String>> duplicated(List<String> names) {
		Set<String> seen = new HashSet<String>(names.size() * 2);
		Set<String> repeated = null;
		for (String name : names) {
			if (!name.endsWith("/") && !seen.add(folded(name))) {
				if (repeated == null) repeated = new HashSet<String>();
				repeated.add(folded(name));
			}
		}
		if (repeated == null) return Collections.emptyMap();
		Map<String, List<String>> out = new LinkedHashMap<String, List<String>>();
		for (String name : names) {
			if (repeated.contains(folded(name))) out.computeIfAbsent(folded(name), k -> new ArrayList<String>()).add(name);
		}
		return out;
	}

	/** REQ-206: refuses a package with entries of one name, naming them; under {@link RepairMode#WORD} records each name once. */
	private void refuseOrRecord(Map<String, List<String>> duplicated, RepairMode repair) throws Docx4JException {
		if (duplicated.isEmpty()) return;
		if (repair != RepairMode.WORD) {
			StringBuilder named = new StringBuilder();
			for (List<String> spellings : duplicated.values()) {
				if (named.length() > 0) named.append(", ");
				named.append(spellings.get(0)).append(" (").append(spellings.size()).append(" entries)");
			}
			throw new Docx4JException("Refused: the zip holds more than one entry of a name, compared as OPC part names: " + named
					+ ". Word reads such a package only through a repair; load with RepairMode.WORD to read the first of each ("
					+ RepairMode.PROPERTY + "=word).");
		}
		for (List<String> spellings : duplicated.values()) {
			LoadFinding finding = new LoadFinding(LoadFinding.DUPLICATE_ENTRY, spellings.get(0), String.valueOf(spellings.size()));
			log.warn(finding.toString());
			loadFindings.add(finding);
		}
	}

	/** Under {@link RepairMode#WORD}: each other spelling of a duplicated name finds the entry kept, as the names are one to Word. */
	private void alias(Map<String, List<String>> duplicated) {
		for (List<String> spellings : duplicated.values()) {
			ByteArray kept = partByteArrays.get(spellings.get(0));
			for (String spelling : spellings) partByteArrays.put(spelling, kept);
		}
	}

	/** Reads each entry in the central directory's order, keeping the first of a name; a null file for a buffered stream. */
	private void readEntries(ZipFile zf, File f) throws Docx4JException {
		long totalUncompressedSize = 0;
		Set<String> read = new HashSet<String>();
		Enumeration entries = zf.getEntries();
		while (entries.hasMoreElements()) {
			ZipArchiveEntry entry = (ZipArchiveEntry) entries.nextElement();
			if (!read.add(folded(entry.getName()))) continue; // REQ-206's first, under RepairMode.WORD
			policePartSize(f, entry.getSize(), entry.getName());
			
            // Get uncompressed and compressed sizes from the entry metadata
            long uncompressedSize = entry.getSize();
            long compressedSize = entry.getCompressedSize();
			
            // Perform zip bomb check: excessive compression ratio
            if (MAX_RATIO>-1) {
    			if (uncompressedSize != -1 && compressedSize != -1) {
	                double compressionRatio = (double) uncompressedSize / compressedSize;
	                if (compressionRatio > MAX_RATIO) {
	                    throw new PartTooLargeException("Zip bomb detected: Excessive compression ratio for entry " + entry.getName());
	                }
    			} else {
    				log.warn("Size (compressed or uncompressed) missing for entry: " + entry.getName());
    			}
            }
                        
			InputStream in = null;
			try {
				byte[] bytes =  getBytesFromInputStream( zf.getInputStream(entry), entry.getSize() );
				policePartSize(f, bytes.length, entry.getName()); // in case earlier check ineffective
				
	            // Add the current entry's size to the total uncompressed size
	            totalUncompressedSize += bytes.length;            

	            // Check against the maximum total uncompressed size
	            if (MAX_UNCOMPRESSED_SIZE>-1
	    				&& totalUncompressedSize > MAX_UNCOMPRESSED_SIZE) {
	                throw new FileTooLargeException("Zip bomb detected: Total uncompressed size exceeds limit in Docx4j.properties.");
	            }
				
				partByteArrays.put(entry.getName(), new ByteArray(bytes) );
			} catch (PartTooLargeException e) {
				throw e;
			} catch (FileTooLargeException e) {
				// the zip-bomb guard's own exception: a real document with large
				// uncompressible-looking parts (EMF images compress 200:1) trips it,
				// and the "is it a zip file?" wrapper hid the cause and the property
				throw e;
			} catch (Exception e) {
	            throw new Docx4JException("Error processing zip file (is it a zip file?)", e);
			}
		}
	}
	
	/**
	 * Warn if the file has any bytes after the ZIP end of central directory record.
	 *
	 * <p>Word will not open such a package: it reports "Word found unreadable content"
	 * and offers to recover it.  docx4j will, and so will most zip libraries, because
	 * they find the end of central directory by scanning backwards from the end of the
	 * file and simply ignore whatever follows it - which means the damage is invisible
	 * until someone tries to open the file in Word.  Measured on a corpus of real
	 * documents: 137 of 454 had six bytes appended reading "Upload" (one, from a
	 * Spanish-language site, read "Subir"), an upload form's button label concatenated
	 * onto the body of the file it posted.  Every one of them loaded in docx4j, rendered
	 * correctly, and was refused by Word.
	 *
	 * <p>Truncating the file at the end of the record repairs it exactly: no zip record
	 * is touched and every part is byte-identical afterwards.
	 *
	 * <p>Only a diagnostic - nothing is thrown, and a file which cannot be inspected is
	 * passed over in silence, since by this point the package has already opened.
	 *
	 * @since 17.2.0
	 */
	private static void warnIfBytesAfterCentralDirectory(File f) {
		long trailing = bytesAfterCentralDirectory(f);
		if (trailing > 0) {
			log.warn(f.getName() + " has " + trailing + " byte(s) after the zip "
					+ "end of central directory record. docx4j ignores them, but "
					+ "Word will refuse to open this file (\"Word found unreadable "
					+ "content\"). Truncating the file to remove them repairs it "
					+ "without altering any part.");
		}
	}

	/**
	 * How many bytes follow the ZIP end of central directory record (and its
	 * comment): 0 for a well-formed file, -1 if the record was not found or the
	 * file could not be inspected.
	 * <p>
	 * Until 17.2.1 the count was wrong for a file longer than the 64K tail this
	 * reads: the tail's offset in the file was added to the answer, so every
	 * such package - most real documents - was reported as having trailing bytes.
	 *
	 * @since 17.2.1
	 */
	static long bytesAfterCentralDirectory(File f) {

		final byte[] EOCD = { 0x50, 0x4b, 0x05, 0x06 };  // "PK\005\006"
		try {
			long length = f.length();
			if (length < 22) return -1;
			// the record is 22 bytes plus a comment of at most 64K, so it is in the tail
			int tail = (int) Math.min(length, 22 + 0xFFFF);
			byte[] buf = new byte[tail];
			try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(f, "r")) {
				raf.seek(length - tail);
				raf.readFully(buf);
			}
			for (int i = buf.length - 22; i >= 0; i--) {
				if (buf[i] != EOCD[0] || buf[i + 1] != EOCD[1]
						|| buf[i + 2] != EOCD[2] || buf[i + 3] != EOCD[3]) continue;
				int commentLength = (buf[i + 20] & 0xFF) | ((buf[i + 21] & 0xFF) << 8);
				// i is an offset within the tail, so the bytes after the record are those of
				// the tail beyond it
				return buf.length - ((long) i + 22 + commentLength);
			}
		} catch (Exception e) {
			log.debug("Could not check " + f.getName() + " for trailing bytes: " + e.getMessage());
		}
		return -1;
	}

	private void policePartSize(File f, long length, String entryName) throws PartTooLargeException {

		if (MAX_BYTES_Unzip_Error>-1
				&& length>MAX_BYTES_Unzip_Error) {
			if (f==null) {
				throw new PartTooLargeException(entryName + ", length " + length 
						+ " exceeds your configured maximum allowed size for unzip.");
			} else {
				throw new PartTooLargeException(f.getName() + ", " + entryName + ", length " + length 
						+ " exceeds your configured maximum allowed size for unzip.");				
			}
		}
		
	}

	/**
	 * Note this uses a streaming parser which doesn't have access 
	 * to entry.getCompressedSize, making it more susceptible to a zip bomb attack.
	 * If you are concerned, use ZipPartStore(File f).
	 * @param is
	 * @throws Docx4JException
	 */
	public ZipPartStore(InputStream is) throws Docx4JException {
		this(is, RepairMode.fromProperty());
	}

	/**
	 * As {@link #ZipPartStore(InputStream)}, with what to do with entries of one name, as
	 * {@link #ZipPartStore(File, RepairMode)}.  So that a stream chooses as a file does, the stream is
	 * buffered and its central directory read first.  A package with no duplicated entry is then read
	 * through its local headers, as ever, so a zip with no usable central directory still loads.
	 *
	 * @since 17.3.6 (CR-075 phase 1)
	 */
	public ZipPartStore(InputStream is, RepairMode repair) throws Docx4JException {

		partByteArrays = new HashMap<String, ByteArray>();
		byte[] zip = buffered(is);
		List<String> central = centralDirectoryNames(zip);
		Map<String, List<String>> duplicated = central == null ? Collections.<String, List<String>>emptyMap() : duplicated(central);
		refuseOrRecord(duplicated, repair);
		if (!duplicated.isEmpty()) {
			readThroughCentralDirectory(zip);
			alias(duplicated);
			return;
		}

		long totalUncompressedSize = 0;
		List<String> names = new ArrayList<String>();
		Set<String> read = new HashSet<String>();
       try {
            ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip));
            ArchiveEntry entry = null;
            while ((entry = zis.getNextEntry()) != null) {
            	names.add(entry.getName());
            	if (!read.add(folded(entry.getName()))) continue; // a local header the central directory doesn't list: checked below
            	// How to read the data descriptor for length? ie before reading?
            	// https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4079029
    			policePartSize(null, entry.getSize(), entry.getName());

				byte[] bytes =  getBytesFromInputStream( zis, entry.getSize() );
				//log.debug("Extracting " + entry.getName());
				policePartSize(null, bytes.length, entry.getName()); // in case earlier check ineffective
				
                // Add the current entry's size to the total uncompressed size
                totalUncompressedSize += bytes.length;            

                // Check against the maximum total uncompressed size
                if (MAX_UNCOMPRESSED_SIZE>-1
        				&& totalUncompressedSize > MAX_UNCOMPRESSED_SIZE) {
                    throw new FileTooLargeException("Zip bomb detected: Total uncompressed size exceeds limit in Docx4j.properties.");
                }
								
				partByteArrays.put(entry.getName(), new ByteArray(bytes) );
            }
            zis.close();
		} catch (PartTooLargeException e) {
			throw e;
        } catch (Exception e) {
            throw new Docx4JException("Error processing zip file (is it a zip file?)", e);
        }
       duplicated = duplicated(names);
       if (duplicated.isEmpty()) return;
       if (central != null) {
    	   // local headers the central directory doesn't list: it is the authority, as for a file
    	   partByteArrays.clear();
    	   readThroughCentralDirectory(zip);
       } else {
    	   refuseOrRecord(duplicated, repair);
    	   alias(duplicated);
       }
	}

	/**
	 * The stream's bytes, within the package limit: a zip's compressed size doesn't pass its uncompressed size by
	 * much, so a stream twice over the limit is refused before it is held in memory.
	 */
	private static byte[] buffered(InputStream is) throws Docx4JException {
		long limit = MAX_UNCOMPRESSED_SIZE > -1 ? Math.min(MAX_UNCOMPRESSED_SIZE * 2 + (1 << 20), Integer.MAX_VALUE - 8) : Integer.MAX_VALUE - 8;
		try {
			byte[] zip = is.readNBytes((int) limit);
			if (zip.length == limit && is.read() != -1) {
				throw new FileTooLargeException("Zip bomb detected: the stream is over " + limit + " bytes");
			}
			return zip;
		} catch (IOException e) {
			throw new Docx4JException("Error processing zip file (is it a zip file?)", e);
		}
	}

	private void readThroughCentralDirectory(byte[] zip) throws Docx4JException {
		try (ZipFile zf = ZipFile.builder().setSeekableByteChannel(new SeekableInMemoryByteChannel(zip)).get()) {
			readEntries(zf, null);
		} catch (IOException e) {
			throw new Docx4JException("Error processing zip file (is it a zip file?)", e);
		}
	}

	/** The names in a zip's central directory, in its order; null where it has none to read, as the local headers are then all there is. */
	private static List<String> centralDirectoryNames(byte[] zip) {
		try (ZipFile zf = ZipFile.builder().setSeekableByteChannel(new SeekableInMemoryByteChannel(zip)).setIgnoreLocalFileHeader(true).get()) {
			return entryNames(zf);
		} catch (IOException e) {
			log.debug("No central directory to read: " + e.getMessage());
			return null;
		}
	}

	private PartStore sourcePartStore;

	/**
	 * Set this if its different to the target part store
	 * (ie this object)
	 */
	public void setSourcePartStore(PartStore partStore) {
		this.sourcePartStore = partStore;
	}

	/////// Load methods

	public boolean partExists(String partName) {
		return (partByteArrays.get(partName) !=null );
	}

    private byte[] getBytesFromInputStream(InputStream is, long size) throws Exception {
		if (size == -1) {
            log.debug("entry.getSize() -1");
            return is.readAllBytes();
        } else {
            log.debug("entry.getSize()={}", +size);
            return is.readNBytes(toIntExact(size));
        }
    }

	public InputStream loadPart(String partName) throws Docx4JException {

        ByteArray bytes = partByteArrays.get(partName);
        if (bytes == null) {
        	if (partName.endsWith(".rels")) {
        		log.debug("part '" + partName + "' not present in part store");
        	} else {
        		log.warn("part '" + partName + "' not present in part store");        		
        	}
        	return null;
        	//throw new Docx4JException("part '" + partName + "' not found");
        }
        
		return bytes.getInputStream();
	}
	
	@Override
	public long getPartSize(String partName) throws Docx4JException {
		
        ByteArray bytes = partByteArrays.get(partName);
        if (bytes == null) {
        	return -1;
        }
		return bytes.getLength();
	}

	/**
	 * This method is to facilitate updating the part without
	 * JAXB unmarshalling then marshalling.  Not intended for direct
	 * use by user code.
	 * 
	 * @param partName
	 * @return
	 * @throws Docx4JException
	 * @since 3.3.2
	 */
	public ByteArray getByteArray(String partName) throws Docx4JException {

		return partByteArrays.get(partName);
	}

	
	/**
	 * @param oldName
	 * @param newName
	 * @since 8.1.4
	 */
	public void rename(PartName oldName, PartName newName) {
		
		log.info("Renaming part " + oldName.getName() + " to " + newName.getName() );
		
		ByteArray partByteArray = partByteArrays.remove(oldName.getName().substring(1)); 
		if (partByteArray==null) throw new Docx4JRuntimeException(oldName.getName().substring(1) + " not present in store");
		partByteArrays.put( newName.getName().substring(1), partByteArray);
		
	}
		
	
	///// Save methods

	private ZipArchiveOutputStream zos;

	/**
	 * @param zipOutputStream the zipOutputStream to set
	 */
	public void setOutputStream(OutputStream os) {
		this.zos = new ZipArchiveOutputStream(os);
	}

	public void saveContentTypes(ContentTypeManager ctm) throws Docx4JException {

		try {

	        zos.putArchiveEntry(new ZipArchiveEntry("[Content_Types].xml"));
	        ctm.marshal(zos);
	        zos.closeArchiveEntry();

		} catch (Exception e) {
			throw new Docx4JException("Error marshalling Content_Types ", e);
		}

	}

	public void saveJaxbXmlPart(JaxbXmlPart part) throws Docx4JException {

		String targetName;
		if (part.getPartName().getName().equals("_rels/.rels")) {
			targetName = part.getPartName().getName();
		} else {
			targetName = part.getPartName().getName().substring(1);
		}

		try {
	        // Add ZIP entry to output stream.
	        zos.putArchiveEntry(new ZipArchiveEntry(targetName));

	        if (part.isUnmarshalled() ) {
	        	log.debug("marshalling " + part.getPartName() );
	        	part.marshal( zos );	        	
	        } else if (part.getPackage() != null 
	        		&& part.getPackage().isWasStrict() ) {
	        	// import strict, save as transitional: a part nobody read is still strict,
	        	// so it has to be read now rather than copied, or the output would mix the
	        	// two dialects. A part which cannot be read names itself in the exception.
	        	log.debug("unmarshalling " + part.getPartName() );
	        	PartStore.readSoTheSaveIsTransitional(part);
	        	log.debug("marshalling " + part.getPartName() );
	        	part.marshal( zos );
	        } else {

	        	if (this.sourcePartStore==null) {

	        		throw new Docx4JException("part store has changed, and sourcePartStore not set");
	        		
	        		/* If you are seeing this exception:
	    			
		    			For processing efficiency, docx4j doesn't actually fully load a part until it is needed.
		    			
		    			To load it, it needs to know where to find it (ie its SourcePartStore).
		    			
		    			This info is stored at the package level, so if you move a part to a new package, that info is lost.
		    			
		    			3 ways to work around this:
		    			
		    			1. wordprocessingMLPackage.setSourcePartStore(jaxbXmlPart.getPackage().getSourcePartStore());
		    			
		    				but not so good, since in the general case, you might want to use several source packages,
		    				and, in any case, you can't retrieve the part if you've changed its name
		    				
		    			2. unmarshall it, so it is fully loaded and docx4j doesn't try to get it from the source part store
		    		
		    					jaxbXmlPart.getContents();
		    			
		    			   this works fine, but it is a bit opaque since it relies on a side effect / underlying knowledge of docx4j 
		    			   
		    			3. best: create a new part, and set its contents (eg via deepcopy) 
	    			*/	        		

	        	} else if (this.sourcePartStore==this) {

		        	// Just use the ByteArray
		        	log.debug(part.getPartName() + " is clean" );
		            ByteArray bytes = partByteArrays.get(
		            		part.getPartName().getName().substring(1) );
		            if (bytes == null) throw new IOException("part '" + part.getPartName() + "' not found");
			        zos.write( bytes.getBytes() );

	        	} else {
	        		InputStream is = sourcePartStore.loadPart(part.getPartName().getName().substring(1));
	        		int read = 0;
	        		byte[] bytes = new byte[1024];

	        		while ((read = is.read(bytes)) != -1) {
	        			zos.write(bytes, 0, read);
	        		}
	        		is.close();
	        	}
	        }


	        // Complete the entry
	        zos.closeArchiveEntry();

		} catch (Exception e) {
			throw new Docx4JException("Error marshalling JaxbXmlPart " + part.getPartName(), e);
		}
	}

	public void saveCustomXmlDataStoragePart(CustomXmlDataStoragePart part) throws Docx4JException {

		String targetName = part.getPartName().getName().substring(1);

		try {

	        // Add ZIP entry to output stream.
	        zos.putArchiveEntry(new ZipArchiveEntry(targetName));

	        part.getData().writeDocument( zos );

	        // Complete the entry
	        zos.closeArchiveEntry();

		} catch (Exception e) {
			throw new Docx4JException("Error marshalling CustomXmlDataStoragePart " + part.getPartName(), e);
		}

	}

	public void saveXmlPart(XmlPart part) throws Docx4JException {

		String targetName = part.getPartName().getName().substring(1);

		try {

		    // Add ZIP entry to output stream.
		    zos.putArchiveEntry(new ZipArchiveEntry(targetName));

		   Document doc =  part.getDocument();

			/*
			 * With Crimson, this gives:
			 *
				Exception in thread "main" java.lang.AbstractMethodError: org.apache.crimson.tree.XmlDocument.getXmlStandalone()Z
					at com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO.setDocumentInfo(DOM2TO.java:373)
					at com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO.parse(DOM2TO.java:127)
					at com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO.parse(DOM2TO.java:94)
					at com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl.transformIdentity(TransformerImpl.java:662)
					at com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl.transform(TransformerImpl.java:708)
					at com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl.transform(TransformerImpl.java:313)
					at org.docx4j.model.datastorage.CustomXmlDataStorageImpl.writeDocument(CustomXmlDataStorageImpl.java:174)
			 *
			 */
			DOMSource source = new DOMSource(doc);
			 XmlUtils.getTransformerFactory().newTransformer().transform(source,
					 new StreamResult(zos) );


		    // Complete the entry
		    zos.closeArchiveEntry();

		} catch (Exception e) {
			throw new Docx4JException("Error marshalling XmlPart " + part.getPartName(), e);
		}
	}

	public void saveBinaryPart(Part part) throws Docx4JException {
		
		if (log.isDebugEnabled()) {
			log.debug("saving " + part.getPartName() + " class " + part.getClass().getName() );	
		}
		// Drop the leading '/'
		String resolvedPartUri = part.getPartName().getName().substring(1);

		try {
			
			byte[] bytes = null;
			
	        if (((BinaryPart)part).isLoaded() ) {

	            bytes = ((BinaryPart)part).getBytes();

	        } else {

	        	if (this.sourcePartStore==null) {

	        		throw new Docx4JException("part store has changed, and sourcePartStore not set");

	        	} else if (this.sourcePartStore==this) {

		        	// Just use the ByteArray
		        	log.debug(part.getPartName() + " is clean" );
		            ByteArray byteArray = partByteArrays.get(
		            		part.getPartName().getName().substring(1) );
		            if (byteArray == null) throw new IOException("part '" + part.getPartName() + "' contents not found");
		            bytes = byteArray.getBytes();

	        	} else {

	        		InputStream is = sourcePartStore.loadPart(part.getPartName().getName().substring(1));
	        		bytes = IOUtils.toByteArray(is);
	        	}
	        }
			
	        // Add ZIP entry to output stream.
			if (! shouldCompress(part)) {
				ZipArchiveEntry ze = new ZipArchiveEntry(resolvedPartUri);
				ze.setMethod(ZipArchiveOutputStream.STORED);
				
				// must set size, compressed size, and crc-32
				ze.setSize(bytes.length);
				ze.setCompressedSize(bytes.length);
				
			    CRC32 crc = new CRC32();
			    crc.update(bytes);	
			    ze.setCrc(crc.getValue());
				
				zos.putArchiveEntry(ze);				
			} else {
				zos.putArchiveEntry(new ZipArchiveEntry(resolvedPartUri));
			}

	        zos.write( bytes );

			// Complete the entry
	        zos.closeArchiveEntry();

		} catch (Exception e ) {
			throw new Docx4JException("Failed to put binary part", e);
		}

		log.debug("success writing part: " + resolvedPartUri);
	}

    private static boolean shouldCompress(Part part) {
    	// Don't compress images or EmbeddedPackagePart (since these should already be compressed)
        if (part instanceof EmbeddedPackagePart || part instanceof ImagePngPart || part instanceof ImageJpegPart || part instanceof ImageGifPart)
            return false;
        if (part instanceof AlternativeFormatInputPart) {
            AltChunkType type = ((AlternativeFormatInputPart) part).getAltChunkType();
            // Don't compress if its one of the following AlternativeFormatInputPart
            return type != AltChunkType.WordprocessingML && type != AltChunkType.OfficeWordTemplate &&
                   type != AltChunkType.OfficeWordMacroEnabled && type != AltChunkType.OfficeWordMacroEnabledTemplate;
        }

        if (part instanceof OleObjectBinaryPart) {
        	// since 11.5.10 (2026) defaults to true
        	return Docx4jProperties.getProperty("docx4j.openpackaging.io3.stores.ZipPartStore.shouldCompress.OleObjectBinaryPart", true);
			// Note: Powerpoint 2010 (32-bit) can't play eg WMV if it is compressed (though 64-bit version is fine),
        	// so prior to 11.5.10 these weren't compressed
        }
        return true;
    }

	public void finishSave() throws Docx4JException {

		try {
			// Complete the ZIP file
			// Don't forget to do this or everything will appear
			// to work, but when you open the zip file you'll get an error
			// "End-of-central-directory signature not found."
	        zos.close();
		} catch (Exception e ) {
			throw new Docx4JException("Failed to put binary part", e);
		}

	}




	public static class ByteArray implements Serializable {

		private static final long serialVersionUID = -784146312250361899L;
		// 4469266984448028582L;

		private byte[] bytes;
		public byte[] getBytes() {
			return bytes;
		}
		
		
		/**
		 * Replace the contents. Not intended for direct
		 * use by user code.
		 * 
		 * @param bytes
		 * @since 3.3.2
		 */  
		public void setBytes(byte[] bytes) {
			this.bytes = bytes;
		}

		private String mimetype;
		public String getMimetype() {
			return mimetype;
		}

		public ByteArray(byte[] bytes) {
			this.bytes = bytes;
			//log.info("Added " + bytes.length  );
		}


		public ByteArray(ByteBuffer bb, String mimetype ) {

			bb.rewind();
			bytes = new byte[bb.limit()];
			bb.get(bytes, 0, bytes.length);

			this.mimetype = mimetype;
		}


		public InputStream getInputStream() {
			return new ByteArrayInputStream(bytes);
		}

		public int getLength() {
			return bytes.length;
		}

	}




	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
