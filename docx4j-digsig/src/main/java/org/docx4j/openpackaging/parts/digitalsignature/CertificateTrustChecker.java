/*
 *  Copyright 2016, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.openpackaging.parts.digitalsignature;

import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

import org.docx4j.dsig.DigitalSignatureException;

public interface CertificateTrustChecker {
	
	// TODO: is there some indeterminate state we need to 
	// allow for, like "no network connection - can't be checked right now"?
	// What does Word do in this case?
	
	CertificateTrustCheckResult isTrusted(X509Certificate signer) throws DigitalSignatureException, CertPathValidatorException, CertPathBuilderException;

	/**
	 * Is the signer trusted, given the certificates the signature carries and the time it says
	 * it was made?  This is what XmlSignaturePart calls.  The default implementation ignores 
	 * both and asks {@link #isTrusted(X509Certificate)} about the signer alone, so an 
	 * implementation of the old method keeps working.
	 * 
	 * @param chain the certificates in the signature's KeyInfo, the signer first
	 * @param signingTime the time the signature says it was made (a claim by the signer, not proof); null if it doesn't say
	 * @since 17.2.1
	 */
	default CertificateTrustCheckResult isTrusted(List<X509Certificate> chain, Date signingTime) 
			throws DigitalSignatureException, CertPathValidatorException, CertPathBuilderException {
		return isTrusted(chain.get(0));
	}

}
