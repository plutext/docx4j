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
        

package org.docx4j.org.apache.poi.poifs.eventfilesystem;

import java.util.*;

import org.docx4j.org.apache.poi.poifs.filesystem.DocumentDescriptor;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSDocumentPath;

/**
 * A registry for POIFSReaderListeners and the DocumentDescriptors of
 * the documents those listeners are interested in
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 * @version %I%, %G%
 */

class POIFSReaderRegistry
{

    // the POIFSReaderListeners who listen to all POIFSReaderEvents
    private Set omnivorousListeners;

    // Each mapping in this Map has a key consisting of a
    // POIFSReaderListener and a value cosisting of a Set of
    // DocumentDescriptors for the documents that POIFSReaderListener
    // is interested in; used to efficiently manage the registry
    private Map selectiveListeners;

    // Each mapping in this Map has a key consisting of a
    // DocumentDescriptor and a value consisting of a Set of
    // POIFSReaderListeners for the document matching that
    // DocumentDescriptor; used when a document is found, to quickly
    // get the listeners interested in that document
    private Map chosenDocumentDescriptors;

    /**
     * Construct the registry
     */

    POIFSReaderRegistry()
    {
        omnivorousListeners       = new HashSet();
        selectiveListeners        = new HashMap();
        chosenDocumentDescriptors = new HashMap();
    }

    /**
     * register a POIFSReaderListener for a particular document
     *
     * @param listener the listener
     * @param path the path of the document of interest
     * @param documentName the name of the document of interest
     */

    void registerListener(final POIFSReaderListener listener,
                          final POIFSDocumentPath path,
                          final String documentName)
    {
        if (!omnivorousListeners.contains(listener))
        {

            // not an omnivorous listener (if it was, this method is a
            // no-op)
            Set descriptors = ( Set ) selectiveListeners.get(listener);

            if (descriptors == null)
            {

                // this listener has not registered before
                descriptors = new HashSet();
                selectiveListeners.put(listener, descriptors);
            }
            DocumentDescriptor descriptor = new DocumentDescriptor(path,
                                                documentName);

            if (descriptors.add(descriptor))
            {

                // this listener wasn't already listening for this
                // document -- add the listener to the set of
                // listeners for this document
                Set listeners =
                    ( Set ) chosenDocumentDescriptors.get(descriptor);

                if (listeners == null)
                {

                    // nobody was listening for this document before
                    listeners = new HashSet();
                    chosenDocumentDescriptors.put(descriptor, listeners);
                }
                listeners.add(listener);
            }
        }
    }

    /**
     * register for all documents
     *
     * @param listener the listener who wants to get all documents
     */

    void registerListener(final POIFSReaderListener listener)
    {
        if (!omnivorousListeners.contains(listener))
        {

            // wasn't already listening for everything, so drop
            // anything listener might have been listening for and
            // then add the listener to the set of omnivorous
            // listeners
            removeSelectiveListener(listener);
            omnivorousListeners.add(listener);
        }
    }

    /**
     * get am iterator of listeners for a particular document
     *
     * @param path the document path
     * @param name the name of the document
     *
     * @return an Iterator POIFSReaderListeners; may be empty
     */

    Iterator getListeners(final POIFSDocumentPath path, final String name)
    {
        Set rval               = new HashSet(omnivorousListeners);
        Set selectiveListeners =
            ( Set ) chosenDocumentDescriptors.get(new DocumentDescriptor(path,
                name));

        if (selectiveListeners != null)
        {
            rval.addAll(selectiveListeners);
        }
        return rval.iterator();
    }

    private void removeSelectiveListener(final POIFSReaderListener listener)
    {
        Set selectedDescriptors = ( Set ) selectiveListeners.remove(listener);

        if (selectedDescriptors != null)
        {
            Iterator iter = selectedDescriptors.iterator();

            while (iter.hasNext())
            {
                dropDocument(listener, ( DocumentDescriptor ) iter.next());
            }
        }
    }

    private void dropDocument(final POIFSReaderListener listener,
                              final DocumentDescriptor descriptor)
    {
        Set listeners = ( Set ) chosenDocumentDescriptors.get(descriptor);

        listeners.remove(listener);
        if (listeners.size() == 0)
        {
            chosenDocumentDescriptors.remove(descriptor);
        }
    }
}   // end package scope class POIFSReaderRegistry

