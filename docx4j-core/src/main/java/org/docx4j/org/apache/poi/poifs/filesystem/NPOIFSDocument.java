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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.docx4j.org.apache.poi.poifs.common.POIFSConstants;
import org.docx4j.org.apache.poi.poifs.dev.POIFSViewable;
import org.docx4j.org.apache.poi.poifs.property.DocumentProperty;
import org.docx4j.org.apache.poi.util.HexDump;

/**
 * This class manages a document in the NIO POIFS filesystem.
 * This is the {@link NPOIFSFileSystem} version.
 */
public final class NPOIFSDocument implements POIFSViewable {
   private DocumentProperty _property;

   private NPOIFSFileSystem _filesystem;
   private NPOIFSStream _stream;
   private int _block_size;
	
   /**
    * Constructor for an existing Document 
    */
   public NPOIFSDocument(DocumentNode document) throws IOException {
       this((DocumentProperty)document.getProperty(), 
            ((DirectoryNode)document.getParent()).getNFileSystem());
   }
   
   /**
    * Constructor for an existing Document 
    */
   public NPOIFSDocument(DocumentProperty property, NPOIFSFileSystem filesystem) 
      throws IOException
   {
      this._property = property;
      this._filesystem = filesystem;

      if(property.getSize() < POIFSConstants.BIG_BLOCK_MINIMUM_DOCUMENT_SIZE) {
         _stream = new NPOIFSStream(_filesystem.getMiniStore(), property.getStartBlock());
         _block_size = _filesystem.getMiniStore().getBlockStoreBlockSize();
      } else {
         _stream = new NPOIFSStream(_filesystem, property.getStartBlock());
         _block_size = _filesystem.getBlockStoreBlockSize();
      }
   }

   /**
    * Constructor for a new Document
    *
    * @param name the name of the POIFSDocument
    * @param stream the InputStream we read data from
    */
   public NPOIFSDocument(String name, NPOIFSFileSystem filesystem, InputStream stream) 
      throws IOException 
   {
      this._filesystem = filesystem;

      // Store it
      int length = store(stream);

      // Build the property for it
      this._property = new DocumentProperty(name, length);
      _property.setStartBlock(_stream.getStartBlock());     
   }
   
   public NPOIFSDocument(String name, int size, NPOIFSFileSystem filesystem, POIFSWriterListener writer) 
      throws IOException 
   {
       this._filesystem = filesystem;

       if (size < POIFSConstants.BIG_BLOCK_MINIMUM_DOCUMENT_SIZE) {
           _stream = new NPOIFSStream(filesystem.getMiniStore());
           _block_size = _filesystem.getMiniStore().getBlockStoreBlockSize();
       } else {
           _stream = new NPOIFSStream(filesystem);
           _block_size = _filesystem.getBlockStoreBlockSize();
       }
       
       OutputStream innerOs = _stream.getOutputStream();
       DocumentOutputStream os = new DocumentOutputStream(innerOs, size);
       POIFSDocumentPath path = new POIFSDocumentPath(name.split("\\\\"));
       String docName = path.getComponent(path.length()-1);
       POIFSWriterEvent event = new POIFSWriterEvent(os, path, docName, size);
       writer.processPOIFSWriterEvent(event);
       innerOs.close();

       // And build the property for it
       this._property = new DocumentProperty(name, size);
       _property.setStartBlock(_stream.getStartBlock());     
   }
   
   /**
    * Stores the given data for this Document
    */
   private int store(InputStream stream) throws IOException {
       final int bigBlockSize = POIFSConstants.BIG_BLOCK_MINIMUM_DOCUMENT_SIZE;
       BufferedInputStream bis = new BufferedInputStream(stream, bigBlockSize+1);
       bis.mark(bigBlockSize);

       // Do we need to store as a mini stream or a full one?
       if(bis.skip(bigBlockSize) < bigBlockSize) {
          _stream = new NPOIFSStream(_filesystem.getMiniStore());
          _block_size = _filesystem.getMiniStore().getBlockStoreBlockSize();
       } else {
          _stream = new NPOIFSStream(_filesystem);
          _block_size = _filesystem.getBlockStoreBlockSize();
       }

       // start from the beginning 
       bis.reset();
       
       // Store it
       OutputStream os = _stream.getOutputStream();
       byte buf[] = new byte[1024];
       int length = 0;
       
       for (int readBytes; (readBytes = bis.read(buf)) != -1; length += readBytes) {
           os.write(buf, 0, readBytes);
       }
       
       // Pad to the end of the block with -1s
       int usedInBlock = length % _block_size;
       if (usedInBlock != 0 && usedInBlock != _block_size) {
           int toBlockEnd = _block_size - usedInBlock;
           byte[] padding = new byte[toBlockEnd];
           Arrays.fill(padding, (byte)0xFF);
           os.write(padding);
       }
       
       // Tidy and return the length
       os.close();
       return length;
   }
   
   /**
    * Frees the underlying stream and property
    */
   void free() throws IOException {
       _stream.free();
       _property.setStartBlock(POIFSConstants.END_OF_CHAIN);
   }
   
   NPOIFSFileSystem getFileSystem()
   {
       return _filesystem;
   }
   
   int getDocumentBlockSize() {
      return _block_size;
   }
   
   Iterator<ByteBuffer> getBlockIterator() {
      if(getSize() > 0) {
         return _stream.getBlockIterator();
      } else {
         List<ByteBuffer> empty = Collections.emptyList();
         return empty.iterator();
      }
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
      Object[] results = new Object[1];
      String result;

      try {
         if(getSize() > 0) {
            // Get all the data into a single array
            byte[] data = new byte[getSize()];
            int offset = 0;
            for(ByteBuffer buffer : _stream) {
               int length = Math.min(_block_size, data.length-offset); 
               buffer.get(data, offset, length);
               offset += length;
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            HexDump.dump(data, 0, output, 0);
            result = output.toString();
         } else {
            result = "<NO DATA>";
         }
      } catch (IOException e) {
         result = e.getMessage();
      }
      results[0] = result;
      return results;
   }

   /**
    * Get an Iterator of objects, some of which may implement POIFSViewable
    *
    * @return an Iterator; may not be null, but may have an empty back end
    *		 store
    */
   public Iterator<Object> getViewableIterator() {
      return Collections.emptyList().iterator();
   }

   /**
    * Give viewers a hint as to whether to call getViewableArray or
    * getViewableIterator
    *
    * @return <code>true</code> if a viewer should call getViewableArray,
    *		 <code>false</code> if a viewer should call getViewableIterator
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
      StringBuffer buffer = new StringBuffer();

      buffer.append("Document: \"").append(_property.getName()).append("\"");
      buffer.append(" size = ").append(getSize());
      return buffer.toString();
   }
}
