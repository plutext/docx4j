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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.docx4j.org.apache.poi.util.Internal;

@Internal
public class EntryUtils
{

    /**
     * Copies an Entry into a target POIFS directory, recursively
     */
    @Internal
    public static void copyNodeRecursively( Entry entry, DirectoryEntry target )
            throws IOException
    {
        // logger.log( POILogger.ERROR, "copyNodeRecursively called with "+entry.getName()+
        // ","+target.getName());
        DirectoryEntry newTarget = null;
        if ( entry.isDirectoryEntry() )
        {
        	DirectoryEntry dirEntry = (DirectoryEntry)entry;
            newTarget = target.createDirectory( entry.getName() );
            newTarget.setStorageClsid( dirEntry.getStorageClsid() );
            Iterator<Entry> entries = dirEntry.getEntries();

            while ( entries.hasNext() )
            {
                copyNodeRecursively( entries.next(), newTarget );
            }
        }
        else
        {
            DocumentEntry dentry = (DocumentEntry) entry;
            DocumentInputStream dstream = new DocumentInputStream( dentry );
            target.createDocument( dentry.getName(), dstream );
            dstream.close();
        }
    }

    /**
     * Copies all the nodes from one POIFS Directory to another
     * 
     * @param sourceRoot
     *            is the source Directory to copy from
     * @param targetRoot
     *            is the target Directory to copy to
     */
    public static void copyNodes(DirectoryEntry sourceRoot,
            DirectoryEntry targetRoot) throws IOException
    {
        for (Entry entry : sourceRoot) {
            copyNodeRecursively( entry, targetRoot );
        }
    }

    /**
     * Copies nodes from one Directory to the other minus the excepts
     * 
     * @param filteredSource The filtering source Directory to copy from
     * @param filteredTarget The filtering target Directory to copy to
     */
    public static void copyNodes( FilteringDirectoryNode filteredSource,
            FilteringDirectoryNode filteredTarget ) throws IOException
    {
        // Nothing special here, just overloaded types to make the
        //  recommended new way to handle this clearer
        copyNodes( (DirectoryEntry)filteredSource, (DirectoryEntry)filteredTarget );
    }

    /**
     * Copies nodes from one Directory to the other minus the excepts
     * 
     * @param sourceRoot
     *            is the source Directory to copy from
     * @param targetRoot
     *            is the target Directory to copy to
     * @param excepts
     *            is a list of Strings specifying what nodes NOT to copy
     * @deprecated use {@link FilteringDirectoryNode} instead
     */
    public static void copyNodes( DirectoryEntry sourceRoot,
            DirectoryEntry targetRoot, List<String> excepts )
            throws IOException
    {
        Iterator<Entry> entries = sourceRoot.getEntries();
        while ( entries.hasNext() )
        {
            Entry entry = entries.next();
            if ( !excepts.contains( entry.getName() ) )
            {
                copyNodeRecursively( entry, targetRoot );
            }
        }
    }

    /**
     * Copies all nodes from one POIFS to the other
     * 
     * @param source
     *            is the source POIFS to copy from
     * @param target
     *            is the target POIFS to copy to
     */
    public static void copyNodes( OPOIFSFileSystem source,
            OPOIFSFileSystem target ) throws IOException
    {
        copyNodes( source.getRoot(), target.getRoot() );
    }
    /**
     * Copies all nodes from one POIFS to the other
     * 
     * @param source
     *            is the source POIFS to copy from
     * @param target
     *            is the target POIFS to copy to
     */
    public static void copyNodes( NPOIFSFileSystem source,
            NPOIFSFileSystem target ) throws IOException
    {
        copyNodes( source.getRoot(), target.getRoot() );
    }
    
    /**
     * Copies nodes from one POIFS to the other, minus the excepts.
     * This delegates the filtering work to {@link FilteringDirectoryNode},
     *  so excepts can be of the form "NodeToExclude" or
     *  "FilteringDirectory/ExcludedChildNode"
     * 
     * @param source is the source POIFS to copy from
     * @param target is the target POIFS to copy to
     * @param excepts is a list of Entry Names to be excluded from the copy
     */
    public static void copyNodes( OPOIFSFileSystem source,
            OPOIFSFileSystem target, List<String> excepts ) throws IOException
    {
        copyNodes(
              new FilteringDirectoryNode(source.getRoot(), excepts),
              new FilteringDirectoryNode(target.getRoot(), excepts)
        );
    }
    /**
     * Copies nodes from one POIFS to the other, minus the excepts.
     * This delegates the filtering work to {@link FilteringDirectoryNode},
     *  so excepts can be of the form "NodeToExclude" or
     *  "FilteringDirectory/ExcludedChildNode"
     * 
     * @param source is the source POIFS to copy from
     * @param target is the target POIFS to copy to
     * @param excepts is a list of Entry Names to be excluded from the copy
     */
    public static void copyNodes( NPOIFSFileSystem source,
            NPOIFSFileSystem target, List<String> excepts ) throws IOException
    {
        copyNodes(
              new FilteringDirectoryNode(source.getRoot(), excepts),
              new FilteringDirectoryNode(target.getRoot(), excepts)
        );
    }
    
    /**
     * Checks to see if the two Directories hold the same contents.
     * For this to be true, they must have entries with the same names,
     *  no entries in one but not the other, and the size+contents
     *  of each entry must match, and they must share names.
     * To exclude certain parts of the Directory from being checked,
     *  use a {@link FilteringDirectoryNode}
     */
    public static boolean areDirectoriesIdentical(DirectoryEntry dirA, DirectoryEntry dirB) {
       // First, check names
       if (! dirA.getName().equals(dirB.getName())) {
          return false;
       }
       
       // Next up, check they have the same number of children
       if (dirA.getEntryCount() != dirB.getEntryCount()) {
          return false;
       }
       
       // Next, check entries and their types/sizes
       Map<String,Integer> aSizes = new HashMap<String, Integer>();
       final int isDirectory = -12345; 
       for (Entry a : dirA) {
          String aName = a.getName();
          if (a.isDirectoryEntry()) {
             aSizes.put(aName, isDirectory);
          } else {
             aSizes.put(aName, ((DocumentNode)a).getSize());
          }
       }
       for (Entry b : dirB) {
          String bName = b.getName();
          if (! aSizes.containsKey(bName)) {
             // In B but not A
             return false;
          }
          
          int size;
          if (b.isDirectoryEntry()) {
             size = isDirectory;
          } else {
             size = ((DocumentNode)b).getSize();
          }
          if (size != aSizes.get(bName)) {
             // Either the wrong type, or they're different sizes
             return false;
          }
          
          // Track it as checked
          aSizes.remove(bName);
       }
       if (!aSizes.isEmpty()) {
          // Nodes were in A but not B
          return false;
       }
       
       // If that passed, check entry contents
       for (Entry a : dirA) {
          try {
             Entry b = dirB.getEntry(a.getName());
             boolean match;
             if (a.isDirectoryEntry()) {
                match = areDirectoriesIdentical(
                      (DirectoryEntry)a, (DirectoryEntry)b);
             } else {
                match = areDocumentsIdentical(
                      (DocumentEntry)a, (DocumentEntry)b);
             }
             if (!match) return false;
          } catch(FileNotFoundException e) {
             // Shouldn't really happen...
             return false;
          } catch(IOException e) {
             // Something's messed up with one document, not a match
             return false;
          }
       }
       
       // If we get here, they match!
       return true;
    }
    
    /**
     * Checks to see if two Documents have the same name
     *  and the same contents. (Their parent directories are
     *  not checked)
     */
    public static boolean areDocumentsIdentical(DocumentEntry docA, DocumentEntry docB) throws IOException {
       if (! docA.getName().equals(docB.getName())) {
          // Names don't match, not the same
          return false;
       }
       if (docA.getSize() != docB.getSize()) {
          // Wrong sizes, can't have the same contents
          return false;
       }

       boolean matches = true;
       DocumentInputStream inpA = null, inpB = null;
       try {
          inpA = new DocumentInputStream(docA);
          inpB = new DocumentInputStream(docB);
          
          int readA, readB;
          do {
             readA = inpA.read();
             readB = inpB.read();
             if (readA != readB) {
                matches = false;
                break;
             }
          } while(readA != -1 && readB != -1);
       } finally {
          if (inpA != null) inpA.close();
          if (inpB != null) inpB.close();
       }
       
       return matches;
    }
}
