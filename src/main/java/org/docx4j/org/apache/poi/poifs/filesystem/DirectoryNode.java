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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.docx4j.org.apache.poi.hpsf.ClassID;
import org.docx4j.org.apache.poi.poifs.dev.POIFSViewable;
import org.docx4j.org.apache.poi.poifs.property.DirectoryProperty;
import org.docx4j.org.apache.poi.poifs.property.DocumentProperty;
import org.docx4j.org.apache.poi.poifs.property.Property;

/**
 * Simple implementation of DirectoryEntry
 */
public class DirectoryNode
    extends EntryNode
    implements DirectoryEntry, POIFSViewable, Iterable<Entry>
{

    // Map of Entry instances, keyed by their names
    private Map<String,Entry> _byname;
    // Our list of entries, kept sorted to preserve order
    private ArrayList<Entry> _entries;

    // Only one of these two will exist
    // the OPOIFSFileSystem we belong to
    private OPOIFSFileSystem   _ofilesystem;
    // the NPOIFSFileSytem we belong to
    private NPOIFSFileSystem  _nfilesystem;

    // the path described by this document
    private POIFSDocumentPath _path;

    /**
     * create a DirectoryNode. This method is not public by design; it
     * is intended strictly for the internal use of this package
     *
     * @param property the DirectoryProperty for this DirectoryEntry
     * @param filesystem the OPOIFSFileSystem we belong to
     * @param parent the parent of this entry
     */
    DirectoryNode(final DirectoryProperty property,
                  final OPOIFSFileSystem filesystem,
                  final DirectoryNode parent)
    {
       this(property, parent, filesystem, (NPOIFSFileSystem)null);
    }

    /**
     * create a DirectoryNode. This method is not public by design; it
     * is intended strictly for the internal use of this package
     *
     * @param property the DirectoryProperty for this DirectoryEntry
     * @param nfilesystem the NPOIFSFileSystem we belong to
     * @param parent the parent of this entry
     */
    DirectoryNode(final DirectoryProperty property,
                  final NPOIFSFileSystem nfilesystem,
                  final DirectoryNode parent)
    {
       this(property, parent, (OPOIFSFileSystem)null, nfilesystem);
    }

    private DirectoryNode(final DirectoryProperty property,
                          final DirectoryNode parent,
                          final OPOIFSFileSystem ofilesystem,
                          final NPOIFSFileSystem nfilesystem)
    {
        super(property, parent);
        this._ofilesystem = ofilesystem;
        this._nfilesystem = nfilesystem;

        if (parent == null)
        {
            _path = new POIFSDocumentPath();
        }
        else
        {
            _path = new POIFSDocumentPath(parent._path, new String[]
            {
                property.getName()
            });
        }
        _byname     = new HashMap<String, Entry>();
        _entries    = new ArrayList<Entry>();
        Iterator<Property> iter = property.getChildren();

        while (iter.hasNext())
        {
            Property child     = iter.next();
            Entry    childNode = null;

            if (child.isDirectory())
            {
                DirectoryProperty childDir = (DirectoryProperty) child;
                if(_ofilesystem != null) {
                   childNode = new DirectoryNode(childDir, _ofilesystem, this);
                } else {
                   childNode = new DirectoryNode(childDir, _nfilesystem, this);
                }
            }
            else
            {
                childNode = new DocumentNode((DocumentProperty) child, this);
            }
            _entries.add(childNode);
            _byname.put(childNode.getName(), childNode);
        }
    }

    /**
     * @return this directory's path representation
     */

    public POIFSDocumentPath getPath()
    {
        return _path;
    }

    /**
     * @return the filesystem that this belongs to
     */
    public NPOIFSFileSystem getFileSystem()
    {
        return _nfilesystem;
    }

    /**
     * If this is OPOIFS based, return the NPOIFSFileSystem
     *  that this belong to, otherwise Null if NPOIFS based
     * @return the filesystem that this belongs to
     */
    public OPOIFSFileSystem getOFileSystem()
    {
        return _ofilesystem;
    }

    /**
     * If this is NPOIFS based, return the NPOIFSFileSystem
     *  that this belong to, otherwise Null if OPOIFS based
     * @return the filesystem that this belongs to
     */
    public NPOIFSFileSystem getNFileSystem()
    {
        return _nfilesystem;
    }

    /**
     * open a document in the directory's entry's list of entries
     *
     * @param documentName the name of the document to be opened
     *
     * @return a newly opened DocumentInputStream
     *
     * @exception IOException if the document does not exist or the
     *            name is that of a DirectoryEntry
     */
    public DocumentInputStream createDocumentInputStream(
            final String documentName)
        throws IOException
    {
        return createDocumentInputStream(getEntry(documentName));
    }

    /**
     * open a document in the directory's entry's list of entries
     *
     * @param document the document to be opened
     *
     * @return a newly opened DocumentInputStream or NDocumentInputStream
     *
     * @exception IOException if the document does not exist or the
     *            name is that of a DirectoryEntry
     */
    public DocumentInputStream createDocumentInputStream(
            final Entry document)
        throws IOException
    {
        if (!document.isDocumentEntry()) {
            throw new IOException("Entry '" + document.getName()
                                  + "' is not a DocumentEntry");
        }

        DocumentEntry entry = (DocumentEntry)document;
        return new DocumentInputStream(entry);
    }

    /**
     * create a new DocumentEntry
     *
     * @param document the new document
     *
     * @return the new DocumentEntry
     *
     * @exception IOException
     */
    DocumentEntry createDocument(final OPOIFSDocument document)
        throws IOException
    {
        DocumentProperty property = document.getDocumentProperty();
        DocumentNode     rval     = new DocumentNode(property, this);

        (( DirectoryProperty ) getProperty()).addChild(property);
        _ofilesystem.addDocument(document);

        _entries.add(rval);
        _byname.put(property.getName(), rval);
        return rval;
    }

    /**
     * create a new DocumentEntry
     *
     * @param document the new document
     *
     * @return the new DocumentEntry
     *
     * @exception IOException
     */
    DocumentEntry createDocument(final NPOIFSDocument document)
        throws IOException
    {
        DocumentProperty property = document.getDocumentProperty();
        DocumentNode     rval     = new DocumentNode(property, this);

        (( DirectoryProperty ) getProperty()).addChild(property);
        _nfilesystem.addDocument(document);

        _entries.add(rval);
        _byname.put(property.getName(), rval);
        return rval;
    }

    /**
     * Change a contained Entry's name
     *
     * @param oldName the original name
     * @param newName the new name
     *
     * @return true if the operation succeeded, else false
     */
    boolean changeName(final String oldName, final String newName)
    {
        boolean   rval  = false;
        EntryNode child = ( EntryNode ) _byname.get(oldName);

        if (child != null)
        {
            rval = (( DirectoryProperty ) getProperty())
                .changeName(child.getProperty(), newName);
            if (rval)
            {
                _byname.remove(oldName);
                _byname.put(child.getProperty().getName(), child);
            }
        }
        return rval;
    }

    /**
     * Delete an entry
     *
     * @param entry the EntryNode to be deleted
     *
     * @return true if the entry was deleted, else false
     */

    boolean deleteEntry(final EntryNode entry)
    {
        boolean rval =
            (( DirectoryProperty ) getProperty())
                .deleteChild(entry.getProperty());

        if (rval)
        {
            _entries.remove(entry);
            _byname.remove(entry.getName());

            if(_ofilesystem != null) {
                _ofilesystem.remove(entry);
            } else {
                try {
                    _nfilesystem.remove(entry);
                } catch (IOException e) {
                    // TODO Work out how to report this, given we can't change the method signature...
                }
            }
        }
        return rval;
    }

    /* ********** START implementation of DirectoryEntry ********** */

    /**
     * get an iterator of the Entry instances contained directly in
     * this instance (in other words, children only; no grandchildren
     * etc.)
     *
     * @return iterator; never null, but hasNext() may return false
     *         immediately (i.e., this DirectoryEntry is empty). All
     *         objects retrieved by next() are guaranteed to be
     *         implementations of Entry.
     */

    public Iterator<Entry> getEntries()
    {
        return _entries.iterator();
    }
    
    /**
     * get the names of all the Entries contained directly in this
     * instance (in other words, names of children only; no grandchildren
     * etc).
     *
     * @return the names of all the entries that may be retrieved with
     *         getEntry(String), which may be empty (if this 
     *         DirectoryEntry is empty)
     */
    public Set<String> getEntryNames()
    {
        return _byname.keySet();
    }

    /**
     * is this DirectoryEntry empty?
     *
     * @return true if this instance contains no Entry instances
     */

    public boolean isEmpty()
    {
        return _entries.isEmpty();
    }

    /**
     * find out how many Entry instances are contained directly within
     * this DirectoryEntry
     *
     * @return number of immediately (no grandchildren etc.) contained
     *         Entry instances
     */

    public int getEntryCount()
    {
        return _entries.size();
    }

    public boolean hasEntry( String name )
    {
        return name != null && _byname.containsKey( name );
    }

    /**
     * get a specified Entry by name
     *
     * @param name the name of the Entry to obtain.
     *
     * @return the specified Entry, if it is directly contained in
     *         this DirectoryEntry
     *
     * @exception FileNotFoundException if no Entry with the specified
     *            name exists in this DirectoryEntry
     */

    public Entry getEntry(final String name) throws FileNotFoundException {
        Entry rval = null;

        if (name != null) {
            rval = _byname.get(name);
        }
        if (rval == null) {
            // either a null name was given, or there is no such name
            throw new FileNotFoundException("no such entry: \"" + name
                    + "\", had: " + _byname.keySet());
        }
        return rval;
    }

    /**
     * create a new DocumentEntry
     *
     * @param name the name of the new DocumentEntry
     * @param stream the InputStream from which to create the new
     *               DocumentEntry
     *
     * @return the new DocumentEntry
     *
     * @exception IOException
     */

    public DocumentEntry createDocument(final String name,
                                        final InputStream stream)
        throws IOException
    {
        if(_nfilesystem != null) {
           return createDocument(new NPOIFSDocument(name, _nfilesystem, stream));
        } else {
           return createDocument(new OPOIFSDocument(name, stream));
        }
    }

    /**
     * create a new DocumentEntry; the data will be provided later
     *
     * @param name the name of the new DocumentEntry
     * @param size the size of the new DocumentEntry
     * @param writer the writer of the new DocumentEntry
     *
     * @return the new DocumentEntry
     *
     * @exception IOException
     */

    public DocumentEntry createDocument(final String name, final int size,
                                        final POIFSWriterListener writer)
        throws IOException
    {
        if(_nfilesystem != null) {
            return createDocument(new NPOIFSDocument(name, size, _nfilesystem, writer));
         } else {
            return createDocument(new OPOIFSDocument(name, size, _path, writer));
         }
    }

    /**
     * create a new DirectoryEntry
     *
     * @param name the name of the new DirectoryEntry
     *
     * @return the new DirectoryEntry
     *
     * @exception IOException
     */

    public DirectoryEntry createDirectory(final String name)
        throws IOException
    {
        DirectoryNode rval;
        DirectoryProperty property = new DirectoryProperty(name);

        if(_ofilesystem != null) {
           rval = new DirectoryNode(property, _ofilesystem, this);
           _ofilesystem.addDirectory(property);
        } else {
           rval = new DirectoryNode(property, _nfilesystem, this);
           _nfilesystem.addDirectory(property);
        }

        (( DirectoryProperty ) getProperty()).addChild(property);
        _entries.add(rval);
        _byname.put(name, rval);
        return rval;
    }

    /**
     * Gets the storage clsid of the directory entry
     *
     * @return storage Class ID
     */
    public ClassID getStorageClsid()
    {
        return getProperty().getStorageClsid();
    }

    /**
     * Sets the storage clsid for the directory entry
     *
     * @param clsidStorage storage Class ID
     */
    public void setStorageClsid(ClassID clsidStorage)
    {
        getProperty().setStorageClsid(clsidStorage);
    }

    /* **********  END  implementation of DirectoryEntry ********** */
    /* ********** START implementation of Entry ********** */

    /**
     * is this a DirectoryEntry?
     *
     * @return true if the Entry is a DirectoryEntry, else false
     */

    @Override
    public boolean isDirectoryEntry()
    {
        return true;
    }

    /* **********  END  implementation of Entry ********** */
    /* ********** START extension of Entry ********** */

    /**
     * extensions use this method to verify internal rules regarding
     * deletion of the underlying store.
     *
     * @return true if it's ok to delete the underlying store, else
     *         false
     */

    @Override
    protected boolean isDeleteOK()
    {

        // if this directory is empty, we can delete it
        return isEmpty();
    }

    /* **********  END  extension of Entry ********** */
    /* ********** START begin implementation of POIFSViewable ********** */

    /**
     * Get an array of objects, some of which may implement
     * POIFSViewable
     *
     * @return an array of Object; may not be null, but may be empty
     */

    public Object [] getViewableArray()
    {
        return new Object[ 0 ];
    }

    /**
     * Get an Iterator of objects, some of which may implement
     * POIFSViewable
     *
     * @return an Iterator; may not be null, but may have an empty
     * back end store
     */
    public Iterator<Object> getViewableIterator()
    {
        List<Object> components = new ArrayList<Object>();

        components.add(getProperty());
        Iterator<Entry> iter = _entries.iterator();
        while (iter.hasNext())
        {
            components.add(iter.next());
        }
        return components.iterator();
    }

    /**
     * Give viewers a hint as to whether to call getViewableArray or
     * getViewableIterator
     *
     * @return true if a viewer should call getViewableArray, false if
     *         a viewer should call getViewableIterator
     */

    public boolean preferArray()
    {
        return false;
    }

    /**
     * Provides a short description of the object, to be used when a
     * POIFSViewable object has not provided its contents.
     *
     * @return short description
     */

    public String getShortDescription()
    {
        return getName();
    }

    /**
     * Returns an Iterator over all the entries
     */
    public Iterator<Entry> iterator() {
        return getEntries();
    }

    /* **********  END  begin implementation of POIFSViewable ********** */
}   // end public class DirectoryNode

