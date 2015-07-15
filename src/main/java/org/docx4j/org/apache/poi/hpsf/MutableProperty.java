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

import java.io.IOException;
import java.io.OutputStream;

import org.docx4j.org.apache.poi.util.CodePageUtil;

/**
 * <p>Adds writing capability to the {@link Property} class.</p>
 *
 * <p>Please be aware that this class' functionality will be merged into the
 * {@link Property} class at a later time, so the API will change.</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 */
public class MutableProperty extends Property
{

    /**
     * <p>Creates an empty property. It must be filled using the set method to
     * be usable.</p>
     */
    public MutableProperty()
    { }



    /**
     * <p>Creates a <code>MutableProperty</code> as a copy of an existing
     * <code>Property</code>.</p>
     *
     * @param p The property to copy.
     */
    public MutableProperty(final Property p)
    {
        setID(p.getID());
        setType(p.getType());
        setValue(p.getValue());
    }


    /**
     * <p>Sets the property's ID.</p>
     *
     * @param id the ID
     */
    public void setID(final long id)
    {
        this.id = id;
    }



    /**
     * <p>Sets the property's type.</p>
     *
     * @param type the property's type
     */
    public void setType(final long type)
    {
        this.type = type;
    }



    /**
     * <p>Sets the property's value.</p>
     *
     * @param value the property's value
     */
    public void setValue(final Object value)
    {
        this.value = value;
    }



    /**
     * <p>Writes the property to an output stream.</p>
     *
     * @param out The output stream to write to.
     * @param codepage The codepage to use for writing non-wide strings
     * @return the number of bytes written to the stream
     *
     * @exception IOException if an I/O error occurs
     * @exception WritingNotSupportedException if a variant type is to be
     * written that is not yet supported
     */
    public int write(final OutputStream out, final int codepage)
        throws IOException, WritingNotSupportedException
    {
        int length = 0;
        long variantType = getType();

        /* Ensure that wide strings are written if the codepage is Unicode. */
        if (codepage == CodePageUtil.CP_UNICODE && variantType == Variant.VT_LPSTR)
            variantType = Variant.VT_LPWSTR;

        length += TypeWriter.writeUIntToStream(out, variantType);
        length += VariantSupport.write(out, variantType, getValue(), codepage);
        return length;
    }

}
