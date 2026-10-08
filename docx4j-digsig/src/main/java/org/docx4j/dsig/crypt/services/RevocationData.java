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

package org.docx4j.dsig.crypt.services;

import java.security.cert.CRLException;
import java.security.cert.X509CRL;
import java.util.ArrayList;
import java.util.List;

/**
 * Container class for PKI revocation data.
 * 
 * @author Frank Cornelis
 * 
 */
public class RevocationData {

    private final List<byte[]> crls;

    private final List<byte[]> ocsps;

    /**
     * Default constructor.
     */
    public RevocationData() {
        this.crls = new ArrayList<byte[]>();
        this.ocsps = new ArrayList<byte[]>();
    }

    /**
     * Adds a CRL to this revocation data set.
     * 
     * @param encodedCrl
     */
    public void addCRL(byte[] encodedCrl) {
        this.crls.add(encodedCrl);
    }

    /**
     * Adds a CRL to this revocation data set.
     * 
     * @param crl
     */
    public void addCRL(X509CRL crl) {
        byte[] encodedCrl;
        try {
            encodedCrl = crl.getEncoded();
        } catch (CRLException e) {
            throw new IllegalArgumentException("CRL coding error: "
                    + e.getMessage(), e);
        }
        addCRL(encodedCrl);
    }

    /**
     * Adds an OCSP response to this revocation data set.
     * 
     * @param encodedOcsp
     */
    public void addOCSP(byte[] encodedOcsp) {
        this.ocsps.add(encodedOcsp);
    }

    /**
     * Gives back a list of all CRLs.
     * 
     * @return a list of all CRLs
     */
    public List<byte[]> getCRLs() {
        return this.crls;
    }

    /**
     * Gives back a list of all OCSP responses.
     * 
     * @return a list of all OCSP response
     */
    public List<byte[]> getOCSPs() {
        return this.ocsps;
    }

    /**
     * Returns <code>true</code> if this revocation data set holds OCSP
     * responses.
     * 
     * @return <code>true</code> if this revocation data set holds OCSP
     * responses.
     */
    public boolean hasOCSPs() {
        return false == this.ocsps.isEmpty();
    }

    /**
     * Returns <code>true</code> if this revocation data set holds CRLs.
     * 
     * @return <code>true</code> if this revocation data set holds CRLs.
     */
    public boolean hasCRLs() {
        return false == this.crls.isEmpty();
    }

    /**
     * Returns <code>true</code> if this revocation data is not empty.
     * 
     * @return <code>true</code> if this revocation data is not empty.
     */
    public boolean hasRevocationDataEntries() {
        return hasOCSPs() || hasCRLs();
    }
}
