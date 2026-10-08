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

public enum CertificateTrustCheckResult {
	UNKNOWN, 
	/** A path from the signer to a trust anchor validates (or the signer is an anchor); the signature is VALID */
	TRUSTED, 
	/** The signer was not valid at the time, or is revoked, or the path is badly signed; the signature is INVALID */
	HARD_FAIL, 
	/** The path validates but the revocation status of a certificate in it could not be obtained; RECOVERABLE */
	SOFT_FAIL, 
	/** The signer is self-signed and not a trust anchor; RECOVERABLE */
	SELF_SIGNED,
	/** The signer is not self-signed, but no path from it to a trust anchor can be built; RECOVERABLE 
	 * @since 17.2.1 */
	UNTRUSTED

}
