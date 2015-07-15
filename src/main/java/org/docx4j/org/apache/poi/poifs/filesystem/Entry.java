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

/**
 * This interface provides access to an object managed by a Filesystem
 * instance. Entry objects are further divided into DocumentEntry and
 * DirectoryEntry instances.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public interface Entry
{

    /**
     * get the name of the Entry
     *
     * @return name
     */

    public String getName();

    /**
     * is this a DirectoryEntry?
     *
     * @return true if the Entry is a DirectoryEntry, else false
     */

    public boolean isDirectoryEntry();

    /**
     * is this a DocumentEntry?
     *
     * @return true if the Entry is a DocumentEntry, else false
     */

    public boolean isDocumentEntry();

    /**
     * get this Entry's parent (the DirectoryEntry that owns this
     * Entry). All Entry objects, except the root Entry, has a parent.
     *
     * @return this Entry's parent; null iff this is the root Entry
     */

    public DirectoryEntry getParent();

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

    public boolean delete();

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

    public boolean renameTo(final String newName);
}   // end public interface Entry

