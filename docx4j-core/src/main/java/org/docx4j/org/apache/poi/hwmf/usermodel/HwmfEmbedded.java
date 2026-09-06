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

package org.docx4j.org.apache.poi.hwmf.usermodel;

import org.docx4j.org.apache.poi.util.Beta;

/**
 * An embedded resource - this class hides the logic of chained emf+ object records and other internals.
 * Consider its API as unstable for now, i.e. there's no guarantee for backward compatibility
 */
@Beta
public class HwmfEmbedded {

    private HwmfEmbeddedType embeddedType;
    private byte[] data;

    public HwmfEmbeddedType getEmbeddedType() {
        return embeddedType;
    }

    public byte[] getRawData() {
        return data;
    }

    public void setEmbeddedType(HwmfEmbeddedType embeddedType) {
        this.embeddedType = embeddedType;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}


