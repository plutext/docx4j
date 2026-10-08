/* NOTICE: This file has been changed by Plutext Pty Ltd for use with docx4j.
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

/* ====================================================================
   This product contains an ASLv2 licensed version of the OOXML signer
   package from the eID Applet project
   http://code.google.com/p/eid-applet/source/browse/trunk/README.txt  
   Copyright (C) 2008-2014 FedICT.
   ================================================================= */ 

package org.docx4j.dsig.crypt;

import java.io.Serializable;

import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;

/**
 * Digest Information data transfer class.
 */
public class DigestInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Main constructor.
     * 
     * @param digestValue
     * @param hashAlgo
     * @param description
     */
    public DigestInfo(byte[] digestValue, HashAlgorithm hashAlgo, String description) {
        this.digestValue = digestValue;
        this.hashAlgo = hashAlgo;
        this.description = description;
    }

    public final byte[] digestValue;

    public final String description;

    public final HashAlgorithm hashAlgo;
}
