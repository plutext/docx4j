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

/**
 * <p>This exception is thrown if HPSF encounters a problem with a variant type.
 * Concrete subclasses specifiy the problem further.</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 */
public abstract class VariantTypeException extends HPSFException
{

    private Object value;

    private long variantType;



    /**
     * <p>Constructor.</p>
     *
     * @param variantType The variant type causing the problem
     * @param value The value who's variant type causes the problem
     * @param msg A message text describing the problem
     */
    public VariantTypeException(final long variantType, final Object value,
                                final String msg)
    {
        super(msg);
        this.variantType = variantType;
        this.value = value;
    }



    /**
     * <p>Returns the offending variant type.</p>
     *
     * @return the offending variant type.
     */
    public long getVariantType()
    {
        return variantType;
    }



    /**
     * <p>Returns the value who's variant type caused the problem.</p>
     *
     * @return the value who's variant type caused the problem
     */
    public Object getValue()
    {
        return value;
    }

}
