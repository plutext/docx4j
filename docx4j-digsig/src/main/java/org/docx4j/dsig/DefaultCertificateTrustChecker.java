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
package org.docx4j.dsig;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorException.BasicReason;
import java.security.cert.CertStore;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXParameters;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.docx4j.openpackaging.parts.digitalsignature.CertificateTrustCheckResult;
import org.docx4j.openpackaging.parts.digitalsignature.CertificateTrustChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decides whether a signer's certificate is trusted, by building and validating a PKIX
 * certification path from it to one of a set of trust anchors, as Office does against the
 * Windows certificate store.
 *
 * <p>The trust anchors are the caller's choice: the JDK's cacerts (no-argument constructor),
 * any KeyStore's trusted certificates (on Windows, <code>KeyStore.getInstance("Windows-ROOT")</code>
 * is what Office trusts), a set of TrustAnchors, and {@link #addTrustAnchor(X509Certificate)}
 * for an extra root or an individual certificate.
 *
 * <p>The certificate's validity period, and the path, are judged at the time the document
 * was signed (which is what the signature claims, not proof), unless
 * {@link #setValidityCheckedAt(ValidityCheckedAt)} says otherwise.
 *
 * <p>Revocation is checked through the JDK's PKIXRevocationChecker (OCSP and CRLs, over the
 * network) and a certificate whose status can't be obtained is a soft fail; see
 * {@link #setRevocation(Revocation)}.
 *
 * <p>Results: {@link CertificateTrustCheckResult#TRUSTED} when a path to an anchor validates
 * (or the signer is itself an anchor); {@link CertificateTrustCheckResult#HARD_FAIL} when the
 * signer was not valid at the time, or is revoked, or a certificate in the path is badly signed;
 * {@link CertificateTrustCheckResult#SOFT_FAIL} when the path validates but revocation status
 * could not be obtained; {@link CertificateTrustCheckResult#SELF_SIGNED} for a self-signed
 * certificate which is not an anchor; {@link CertificateTrustCheckResult#UNTRUSTED} when no
 * path to an anchor can be built.  An exception means the checker itself is misconfigured.
 *
 * @since 17.2.1 (it was in the source distribution from 3.3.0 but not built)
 */
public class DefaultCertificateTrustChecker implements CertificateTrustChecker {

	private static Logger log = LoggerFactory.getLogger(DefaultCertificateTrustChecker.class);

	/** How the revocation status of the certificates in the path is checked */
	public enum Revocation {
		/** Not checked.  No network access is needed. */
		NONE,
		/** Checked; a status which can't be obtained is a {@link CertificateTrustCheckResult#SOFT_FAIL}.  The default. */
		SOFT_FAIL,
		/** Checked; a status which can't be obtained is an {@link CertificateTrustCheckResult#UNTRUSTED} path. */
		REQUIRED
	}

	/** The time at which the certificate's validity period, and the path, are judged */
	public enum ValidityCheckedAt {
		/** The time the signature says it was made; the present if the signature doesn't say.  The default. */
		SIGNING_TIME,
		/** The present */
		NOW
	}

	/** Set globally by {@link #setFetchIssuersByAIA(boolean)}; the JDK has no per-call setting */
	private static final String AIA_PROPERTY = "com.sun.security.enableAIAcaIssuers";

	private final Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();

	private Revocation revocation = Revocation.SOFT_FAIL;
	private ValidityCheckedAt validityCheckedAt = ValidityCheckedAt.SIGNING_TIME;
	private boolean fetchIssuersByAIA = false;

	/**
	 * Trust what the JDK trusts: the certificates in its cacerts file.
	 *
	 * @throws DigitalSignatureException if cacerts can't be read
	 */
	public DefaultCertificateTrustChecker() throws DigitalSignatureException {

		File cacerts = new File(System.getProperty("java.home"), "lib" + File.separator + "security" + File.separator + "cacerts");
		try (InputStream is = new FileInputStream(cacerts)) {
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(is, null); // the password is only needed to check integrity
			addTrustAnchors(keystore);
		} catch (Exception e) {
			throw new DigitalSignatureException("Can't read the JDK's trusted certificates from " + cacerts + ": " + e.getMessage(), e);
		}
		if (trustAnchors.isEmpty()) {
			throw new DigitalSignatureException("No trusted certificates in " + cacerts);
		}
	}

	/**
	 * Trust the certificates in a key store: its trusted-certificate entries, and the certificate
	 * of each key entry.  The store must be loaded.  On Windows,
	 * <code>KeyStore.getInstance("Windows-ROOT")</code> (loaded with nulls) gives the roots the
	 * computer trusts, which is what Office checks against.
	 *
	 * @throws DigitalSignatureException if the store can't be read
	 */
	public DefaultCertificateTrustChecker(KeyStore keystore) throws DigitalSignatureException {
		try {
			addTrustAnchors(keystore);
		} catch (GeneralSecurityException e) {
			throw new DigitalSignatureException("Can't read the key store: " + e.getMessage(), e);
		}
	}

	/**
	 * Trust these anchors.  An empty set is allowed: every signer is then self-signed or untrusted.
	 */
	public DefaultCertificateTrustChecker(Set<TrustAnchor> trustAnchors) {
		this.trustAnchors.addAll(trustAnchors);
	}

	private void addTrustAnchors(KeyStore keystore) throws GeneralSecurityException {
		for (Enumeration<String> aliases = keystore.aliases(); aliases.hasMoreElements();) {
			String alias = aliases.nextElement();
			java.security.cert.Certificate c = keystore.getCertificate(alias);
			if (c instanceof X509Certificate) {
				trustAnchors.add(new TrustAnchor((X509Certificate) c, null));
			}
		}
	}

	/** Also trust this certificate, whether a root or an individual (eg self-signed) one. */
	public DefaultCertificateTrustChecker addTrustAnchor(X509Certificate certificate) {
		trustAnchors.add(new TrustAnchor(certificate, null));
		return this;
	}

	public Set<TrustAnchor> getTrustAnchors() {
		return Collections.unmodifiableSet(trustAnchors);
	}

	public Revocation getRevocation() {
		return revocation;
	}

	/** Default {@link Revocation#SOFT_FAIL} */
	public DefaultCertificateTrustChecker setRevocation(Revocation revocation) {
		this.revocation = revocation;
		return this;
	}

	public ValidityCheckedAt getValidityCheckedAt() {
		return validityCheckedAt;
	}

	/** Default {@link ValidityCheckedAt#SIGNING_TIME} */
	public DefaultCertificateTrustChecker setValidityCheckedAt(ValidityCheckedAt validityCheckedAt) {
		this.validityCheckedAt = validityCheckedAt;
		return this;
	}

	public boolean isFetchIssuersByAIA() {
		return fetchIssuersByAIA;
	}

	/**
	 * Whether the path builder may fetch an issuer's certificate from the URL in a
	 * certificate's Authority Information Access extension (a network call).  Default false;
	 * an intermediate carried in the signature is used without it.  NB this is a JDK-wide
	 * setting (the system property com.sun.security.enableAIAcaIssuers), so it affects other
	 * code in the JVM too.
	 */
	public DefaultCertificateTrustChecker setFetchIssuersByAIA(boolean fetchIssuersByAIA) {
		this.fetchIssuersByAIA = fetchIssuersByAIA;
		System.setProperty(AIA_PROPERTY, Boolean.toString(fetchIssuersByAIA));
		return this;
	}

	/** The signer alone, judged at the present: prefer {@link #isTrusted(List, Date)}. */
	@Override
	public CertificateTrustCheckResult isTrusted(X509Certificate signer) throws DigitalSignatureException {
		return isTrusted(Collections.singletonList(signer), null);
	}

	@Override
	public CertificateTrustCheckResult isTrusted(List<X509Certificate> chain, Date signingTime) throws DigitalSignatureException {

		if (chain == null || chain.isEmpty()) {
			throw new DigitalSignatureException("No signer certificate");
		}
		X509Certificate signer = chain.get(0);

		Date at = (validityCheckedAt == ValidityCheckedAt.SIGNING_TIME && signingTime != null) ? signingTime : new Date();
		log.debug("checking {} at {}", signer.getSubjectX500Principal(), at);

		// Was it valid then?
		try {
			signer.checkValidity(at);
		} catch (CertificateExpiredException e) {
			log.info("{} had expired at {} ({})", signer.getSubjectX500Principal(), at, e.getMessage());
			return CertificateTrustCheckResult.HARD_FAIL;
		} catch (CertificateNotYetValidException e) {
			log.info("{} was not yet valid at {} ({})", signer.getSubjectX500Principal(), at, e.getMessage());
			return CertificateTrustCheckResult.HARD_FAIL;
		}

		// Trusted as such?  (eg a self-signed certificate the caller has added)
		for (TrustAnchor anchor : trustAnchors) {
			if (signer.equals(anchor.getTrustedCert())) {
				return CertificateTrustCheckResult.TRUSTED;
			}
		}

		if (isSelfSigned(signer)) {
			return CertificateTrustCheckResult.SELF_SIGNED;
		}

		if (trustAnchors.isEmpty()) {
			return CertificateTrustCheckResult.UNTRUSTED;
		}

		// Build a path to an anchor, then validate it
		CertPath certPath;
		try {
			certPath = buildPath(chain, at);
		} catch (CertPathBuilderException e) {
			log.info("no path from {} to a trust anchor: {}", signer.getSubjectX500Principal(), e.getMessage());
			return CertificateTrustCheckResult.UNTRUSTED;
		} catch (GeneralSecurityException e) {
			throw new DigitalSignatureException("Can't build a certification path: " + e.getMessage(), e);
		}

		try {
			return validatePath(certPath, at);
		} catch (CertPathValidatorException e) {
			return resultOf(e, signer);
		} catch (GeneralSecurityException e) {
			throw new DigitalSignatureException("Can't validate the certification path: " + e.getMessage(), e);
		}
	}

	private CertPath buildPath(List<X509Certificate> chain, Date at) throws GeneralSecurityException {

		X509CertSelector target = new X509CertSelector();
		target.setCertificate(chain.get(0));

		PKIXBuilderParameters params = new PKIXBuilderParameters(trustAnchors, target);
		params.setDate(at);
		params.setRevocationEnabled(false); // that is the validator's job
		// the intermediates the signature carries
		params.addCertStore(CertStore.getInstance("Collection", new CollectionCertStoreParameters(new ArrayList<X509Certificate>(chain))));

		PKIXCertPathBuilderResult result = (PKIXCertPathBuilderResult) CertPathBuilder.getInstance("PKIX").build(params);
		if (log.isDebugEnabled()) {
			for (java.security.cert.Certificate c : result.getCertPath().getCertificates()) {
				log.debug("  path: {}", ((X509Certificate) c).getSubjectX500Principal());
			}
			log.debug("  anchor: {}", result.getTrustAnchor().getTrustedCert().getSubjectX500Principal());
		}
		return result.getCertPath();
	}

	private CertificateTrustCheckResult validatePath(CertPath certPath, Date at) throws GeneralSecurityException {

		CertPathValidator validator = CertPathValidator.getInstance("PKIX");
		PKIXParameters params = new PKIXParameters(trustAnchors);
		params.setDate(at);

		PKIXRevocationChecker revocationChecker = null;
		if (revocation == Revocation.NONE) {
			params.setRevocationEnabled(false);
		} else {
			revocationChecker = (PKIXRevocationChecker) validator.getRevocationChecker();
			if (revocation == Revocation.SOFT_FAIL) {
				revocationChecker.setOptions(EnumSet.of(PKIXRevocationChecker.Option.SOFT_FAIL));
			}
			params.addCertPathChecker(revocationChecker);
		}

		validator.validate(certPath, params);

		if (revocationChecker != null && !revocationChecker.getSoftFailExceptions().isEmpty()) {
			for (CertPathValidatorException e : revocationChecker.getSoftFailExceptions()) {
				log.info("revocation status not obtained: {}", e.getMessage());
			}
			return CertificateTrustCheckResult.SOFT_FAIL;
		}
		return CertificateTrustCheckResult.TRUSTED;
	}

	private CertificateTrustCheckResult resultOf(CertPathValidatorException e, X509Certificate signer) {

		log.info("path from {} did not validate: {} ({})", signer.getSubjectX500Principal(), e.getMessage(), e.getReason());

		if (e.getReason() instanceof BasicReason) {
			switch ((BasicReason) e.getReason()) {
			case REVOKED:
			case EXPIRED:
			case NOT_YET_VALID:
			case INVALID_SIGNATURE:
				return CertificateTrustCheckResult.HARD_FAIL;
			case UNDETERMINED_REVOCATION_STATUS:
				// Revocation.REQUIRED (with SOFT_FAIL this is a soft fail, not an exception)
				return CertificateTrustCheckResult.UNTRUSTED;
			default:
				return CertificateTrustCheckResult.UNTRUSTED;
			}
		}
		return CertificateTrustCheckResult.UNTRUSTED;
	}

	/** Whether the certificate is signed with its own key */
	public static boolean isSelfSigned(X509Certificate cert) throws DigitalSignatureException {
		try {
			cert.verify(cert.getPublicKey());
			return true;
		} catch (SignatureException | InvalidKeyException e) {
			return false;
		} catch (GeneralSecurityException e) {
			throw new DigitalSignatureException("Can't verify " + cert.getSubjectX500Principal() + ": " + e.getMessage(), e);
		}
	}

}
