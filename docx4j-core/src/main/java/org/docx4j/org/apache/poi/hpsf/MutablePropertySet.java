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

package org.docx4j.org.apache.poi.hpsf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.docx4j.org.apache.poi.poifs.filesystem.Entry;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;

/**
 * <p>Adds writing support to the {@link PropertySet} class.</p>
 *
 * <p>Please be aware that this class' functionality will be merged into the
 * {@link PropertySet} class at a later time, so the API will change.</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 */
public class MutablePropertySet extends PropertySet
{

    /**
     * <p>Constructs a <code>MutablePropertySet</code> instance. Its
     * primary task is to initialize the immutable field with their proper
     * values. It also sets fields that might change to reasonable defaults.</p>
     */
    public MutablePropertySet()
    {
        /* Initialize the "byteOrder" field. */
        byteOrder = LittleEndian.getUShort(BYTE_ORDER_ASSERTION);

        /* Initialize the "format" field. */
        format = LittleEndian.getUShort(FORMAT_ASSERTION);

        /* Initialize "osVersion" field as if the property has been created on
         * a Win32 platform, whether this is the case or not. */
        osVersion = (OS_WIN32 << 16) | 0x0A04;

        /* Initailize the "classID" field. */
        classID = new ClassID();

        /* Initialize the sections. Since property set must have at least
         * one section it is added right here. */
        sections = new LinkedList<Section>();
        sections.add(new MutableSection());
    }



    /**
     * <p>Constructs a <code>MutablePropertySet</code> by doing a deep copy of
     * an existing <code>PropertySet</code>. All nested elements, i.e.
     * <code>Section</code>s and <code>Property</code> instances, will be their
     * mutable counterparts in the new <code>MutablePropertySet</code>.</p>
     *
     * @param ps The property set to copy
     */
    public MutablePropertySet(final PropertySet ps)
    {
        byteOrder = ps.getByteOrder();
        format = ps.getFormat();
        osVersion = ps.getOSVersion();
        setClassID(ps.getClassID());
        clearSections();
        if (sections == null)
            sections = new LinkedList<Section>();
        for (final Section section : ps.getSections())
        {
            final MutableSection s = new MutableSection(section);
            addSection(s);
        }
    }



    /**
     * <p>The length of the property set stream header.</p>
     */
    private final int OFFSET_HEADER =
        BYTE_ORDER_ASSERTION.length + /* Byte order    */
        FORMAT_ASSERTION.length +     /* Format        */
        LittleEndianConsts.INT_SIZE + /* OS version    */
        ClassID.LENGTH +              /* Class ID      */
        LittleEndianConsts.INT_SIZE;  /* Section count */



    /**
     * <p>Sets the "byteOrder" property.</p>
     *
     * @param byteOrder the byteOrder value to set
     */
    public void setByteOrder(final int byteOrder)
    {
        this.byteOrder = byteOrder;
    }



    /**
     * <p>Sets the "format" property.</p>
     *
     * @param format the format value to set
     */
    public void setFormat(final int format)
    {
        this.format = format;
    }



    /**
     * <p>Sets the "osVersion" property.</p>
     *
     * @param osVersion the osVersion value to set
     */
    public void setOSVersion(final int osVersion)
    {
        this.osVersion = osVersion;
    }



    /**
     * <p>Sets the property set stream's low-level "class ID"
     * field.</p>
     *
     * @param classID The property set stream's low-level "class ID" field.
     *
     * @see PropertySet#getClassID()
     */
    public void setClassID(final ClassID classID)
    {
        this.classID = classID;
    }



    /**
     * <p>Removes all sections from this property set.</p>
     */
    public void clearSections()
    {
        sections = null;
    }



    /**
     * <p>Adds a section to this property set.</p>
     *
     * @param section The {@link Section} to add. It will be appended
     * after any sections that are already present in the property set
     * and thus become the last section.
     */
    public void addSection(final Section section)
    {
        if (sections == null)
            sections = new LinkedList<Section>();
        sections.add(section);
    }



    /**
     * <p>Writes the property set to an output stream.</p>
     *
     * @param out the output stream to write the section to
     * @exception IOException if an error when writing to the output stream
     * occurs
     * @exception WritingNotSupportedException if HPSF does not yet support
     * writing a property's variant type.
     */
    public void write(final OutputStream out)
        throws WritingNotSupportedException, IOException
    {
        /* Write the number of sections in this property set stream. */
        final int nrSections = sections.size();
        int length = 0;

        /* Write the property set's header. */
        length += TypeWriter.writeToStream(out, (short) getByteOrder());
        length += TypeWriter.writeToStream(out, (short) getFormat());
        length += TypeWriter.writeToStream(out, getOSVersion());
        length += TypeWriter.writeToStream(out, getClassID());
        length += TypeWriter.writeToStream(out, nrSections);
        int offset = OFFSET_HEADER;

        /* Write the section list, i.e. the references to the sections. Each
         * entry in the section list consist of the section's class ID and the
         * section's offset relative to the beginning of the stream. */
        offset += nrSections * (ClassID.LENGTH + LittleEndian.INT_SIZE);
        final int sectionsBegin = offset;
        for (final Section section : sections)
        {
            final MutableSection s = (MutableSection)section;
            final ClassID formatID = s.getFormatID();
            if (formatID == null)
                throw new NoFormatIDException();
            length += TypeWriter.writeToStream(out, s.getFormatID());
            length += TypeWriter.writeUIntToStream(out, offset);
            try
            {
                offset += s.getSize();
            }
            catch (HPSFRuntimeException ex)
            {
                final Throwable cause = ex.getReason();
                if (cause instanceof UnsupportedEncodingException) {
                    throw new IllegalPropertySetDataException(cause);
                }
                throw ex;
            }
        }

        /* Write the sections themselves. */
        offset = sectionsBegin;
        for (final Section section : sections)
        {
            final MutableSection s = (MutableSection)section;
            offset += s.write(out);
        }
        
        /* Indicate that we're done */
        out.close();
    }



    /**
     * <p>Returns the contents of this property set stream as an input stream.
     * The latter can be used for example to write the property set into a POIFS
     * document. The input stream represents a snapshot of the property set.
     * If the latter is modified while the input stream is still being
     * read, the modifications will not be reflected in the input stream but in
     * the {@link MutablePropertySet} only.</p>
     *
     * @return the contents of this property set stream
     *
     * @throws WritingNotSupportedException if HPSF does not yet support writing
     * of a property's variant type.
     * @throws IOException if an I/O exception occurs.
     */
    public InputStream toInputStream()
        throws IOException, WritingNotSupportedException
    {
        final ByteArrayOutputStream psStream = new ByteArrayOutputStream();
        try {
            write(psStream);
        } finally {
            psStream.close();
        }
        final byte[] streamData = psStream.toByteArray();
        return new ByteArrayInputStream(streamData);
    }

    /**
     * <p>Writes a property set to a document in a POI filesystem directory.</p>
     *
     * @param dir The directory in the POI filesystem to write the document to.
     * @param name The document's name. If there is already a document with the
     * same name in the directory the latter will be overwritten.
     *
     * @throws WritingNotSupportedException
     * @throws IOException
     */
    public void write(final DirectoryEntry dir, final String name)
    throws WritingNotSupportedException, IOException
    {
        /* If there is already an entry with the same name, remove it. */
        try
        {
            final Entry e = dir.getEntry(name);
            e.delete();
        }
        catch (FileNotFoundException ex)
        {
            /* Entry not found, no need to remove it. */
        }
        /* Create the new entry. */
        dir.createDocument(name, toInputStream());
    }

}
