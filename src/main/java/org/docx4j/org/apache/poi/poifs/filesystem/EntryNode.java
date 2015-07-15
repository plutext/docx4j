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

import org.docx4j.org.apache.poi.poifs.property.Property;

/**
 * Abstract implementation of Entry
 *
 * Extending classes should override isDocument() or isDirectory(), as
 * appropriate
 *
 * Extending classes must override isDeleteOK()
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public abstract class EntryNode
    implements Entry
{

    // the DocumentProperty backing this object
    private Property      _property;

    // this object's parent Entry
    private DirectoryNode _parent;

    /**
     * create a DocumentNode. This method is not public by design; it
     * is intended strictly for the internal use of extending classes
     *
     * @param property the Property for this Entry
     * @param parent the parent of this entry
     */

    protected EntryNode(final Property property, final DirectoryNode parent)
    {
        _property = property;
        _parent   = parent;
    }

    /**
     * grant access to the property
     *
     * @return the property backing this entry
     */

    protected Property getProperty()
    {
        return _property;
    }

    /**
     * is this the root of the tree?
     *
     * @return true if so, else false
     */

    protected boolean isRoot()
    {

        // only the root Entry has no parent ...
        return (_parent == null);
    }

    /**
     * extensions use this method to verify internal rules regarding
     * deletion of the underlying store.
     *
     * @return true if it's ok to delete the underlying store, else
     *         false
     */

    protected abstract boolean isDeleteOK();

    /* ********** START implementation of Entry ********** */

    /**
     * get the name of the Entry
     *
     * @return name
     */

    public String getName()
    {
        return _property.getName();
    }

    /**
     * is this a DirectoryEntry?
     *
     * @return true if the Entry is a DirectoryEntry, else false
     */

    public boolean isDirectoryEntry()
    {
        return false;
    }

    /**
     * is this a DocumentEntry?
     *
     * @return true if the Entry is a DocumentEntry, else false
     */

    public boolean isDocumentEntry()
    {
        return false;
    }

    /**
     * get this Entry's parent (the DocumentEntry that owns this
     * Entry). All Entry objects, except the root Entry, has a parent.
     *
     * @return this Entry's parent; null iff this is the root Entry
     */

    public DirectoryEntry getParent()
    {
        return _parent;
    }

    /**
     * Delete this Entry. This operation should succeed, but there are
     * special circumstances when it will not:
     *
     * If this Entry is the root of the Entry tree, it cannot be
     * deleted, as there is no way to create another one.
     *
     * If this Entry is a directory, it cannot be deleted unless it is
     * empty.
     *
     * @return true if the Entry was successfully deleted, else false
     */

    public boolean delete()
    {
        boolean rval = false;

        if ((!isRoot()) && isDeleteOK())
        {
            rval = _parent.deleteEntry(this);
        }
        return rval;
    }

    /**
     * Rename this Entry. This operation will fail if:
     *
     * There is a sibling Entry (i.e., an Entry whose parent is the
     * same as this Entry's parent) with the same name.
     *
     * This Entry is the root of the Entry tree. Its name is dictated
     * by the Filesystem and many not be changed.
     *
     * @param newName the new name for this Entry
     *
     * @return true if the operation succeeded, else false
     */

    public boolean renameTo(final String newName)
    {
        boolean rval = false;

        if (!isRoot())
        {
            rval = _parent.changeName(getName(), newName);
        }
        return rval;
    }

    /* **********  END  implementation of Entry ********** */
}   // end public class EntryNode

