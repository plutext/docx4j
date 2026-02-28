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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;

import org.docx4j.org.apache.poi.hpsf.ClassID;

/**
 * A DirectoryEntry filter, which exposes another
 *  DirectoryEntry less certain parts.
 * This is typically used when copying or comparing
 *  Filesystems.
 */
public class FilteringDirectoryNode implements DirectoryEntry
{
   /**
    * The names of our entries to exclude
    */
   private final Set<String> excludes;
   /**
    * Excludes of our child directories
    */
   private final Map<String,List<String>> childExcludes;

   private final DirectoryEntry directory;

   /**
    * Creates a filter round the specified directory, which
    *  will exclude entries such as "MyNode" and "MyDir/IgnoreNode".
    * The excludes can stretch into children, if they contain a /.
    *
    * @param directory The Directory to filter
    * @param excludes The Entries to exclude
    * @throws IllegalArgumentException if directory is null
    */
   public FilteringDirectoryNode(DirectoryEntry directory, Collection<String> excludes) {
      if (directory == null) {
         throw new IllegalArgumentException("directory cannot be null");
      }
      this.directory = directory;

      // Process the excludes
      this.excludes = new HashSet<>();
      this.childExcludes = new HashMap<>();
      for (String excl : excludes) {
         int splitAt = excl.indexOf('/');
         if (splitAt == -1) {
            // Applies to us
            this.excludes.add(excl);
         } else {
            // Applies to a child
            String child = excl.substring(0, splitAt);
            String childExcl = excl.substring(splitAt+1);
            if (! this.childExcludes.containsKey(child)) {
               this.childExcludes.put(child, new ArrayList<>());
            }
            this.childExcludes.get(child).add(childExcl);
         }
      }
   }

   @Override
   public DirectoryEntry createDirectory(String name) throws IOException {
      return directory.createDirectory(name);
   }

   @Override
   public DocumentEntry createDocument(String name, InputStream stream)
         throws IOException {
      return directory.createDocument(name, stream);
   }

   @Override
   public DocumentEntry createDocument(String name, int size,
         POIFSWriterListener writer) throws IOException {
      return directory.createDocument(name, size, writer);
   }

   @Override
   public Iterator<Entry> getEntries() {
      return new FilteringIterator();
   }

   @Override
   public Iterator<Entry> iterator() {
      return getEntries();
   }

   /**
    * @since 5.2.0
    */
   @Override
   public Spliterator<Entry> spliterator() {
      return Spliterators.spliterator(iterator(), getEntryCount(), 0);
   }

   @Override
   public int getEntryCount() {
      int size = directory.getEntryCount();
      for (String excl : excludes) {
         if (directory.hasEntryCaseInsensitive(excl)) {
            size--;
         }
      }
      return size;
   }

   @Override
   public Set<String> getEntryNames() {
       Set<String> names = new HashSet<>();
       for (String name : directory.getEntryNames()) {
           if (!excludes.contains(name)) {
               names.add(name);
           }
       }
       return names;
   }

   @Override
   public boolean isEmpty() {
      return (getEntryCount() == 0);
   }

   @Override
   public boolean hasEntry(String name) {
      if (excludes.contains(name)) {
         return false;
      }
      return directory.hasEntry(name);
   }

   @Override
   public boolean hasEntryCaseInsensitive(String name) {
      if (excludes.contains(name)) {
         return false;
      }
      return directory.hasEntryCaseInsensitive(name);
   }


   @Override
   public Entry getEntry(String name) throws FileNotFoundException {
      if (excludes.contains(name)) {
         throw new FileNotFoundException(name);
      }

      Entry entry = directory.getEntry(name);
      return wrapEntry(entry);
   }

   @Override
   public Entry getEntryCaseInsensitive(String name) throws FileNotFoundException {
      if (excludes.contains(name)) {
         throw new FileNotFoundException(name);
      }

      Entry entry = directory.getEntryCaseInsensitive(name);
      return wrapEntry(entry);
   }

   private Entry wrapEntry(Entry entry) {
      String name = entry.getName();
      if (childExcludes.containsKey(name) && entry instanceof DirectoryEntry) {
         return new FilteringDirectoryNode(
               (DirectoryEntry)entry, childExcludes.get(name));
      }
      return entry;
   }

   @Override
   public ClassID getStorageClsid() {
      return directory.getStorageClsid();
   }

   @Override
   public void setStorageClsid(ClassID clsidStorage) {
      directory.setStorageClsid(clsidStorage);
   }

   @Override
   public boolean delete() {
      return directory.delete();
   }

   @Override
   public boolean renameTo(String newName) {
      return directory.renameTo(newName);
   }

   @Override
   public String getName() {
      return directory.getName();
   }

   @Override
   public DirectoryEntry getParent() {
      return directory.getParent();
   }

   @Override
   public boolean isDirectoryEntry() {
      return true;
   }

   @Override
   public boolean isDocumentEntry() {
      return false;
   }

   private class FilteringIterator implements Iterator<Entry> {
      private final Iterator<Entry> parent;
      private Entry next;

      private FilteringIterator() {
         parent = directory.getEntries();
         locateNext();
      }
      private void locateNext() {
         next = null;
         Entry e;
         while (parent.hasNext() && next == null) {
            e = parent.next();
            if (! excludes.contains(e.getName())) {
               next = wrapEntry(e);
            }
         }
      }

      @Override
      public boolean hasNext() {
         return (next != null);
      }

      @Override
      public Entry next() {
         if (!hasNext()) {
            throw new NoSuchElementException();
         }

         Entry e = next;
         locateNext();
         return e;
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException("Remove not supported");
      }
   }
}
