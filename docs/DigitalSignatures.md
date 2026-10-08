# Digital signatures: docx4j-digsig

docx4j signs and validates OPC package signatures, the kind Word, Excel and PowerPoint make and
check under *File > Info > Protect > Add a Digital Signature*. The module is `docx4j-digsig`
(since 17.3.2; before that, Plutext DigSig in the Enterprise edition). Change request:
`docs/developer/change-requests/CR-033-digsig-module.md`; the pptx and xlsx work it rests on
is the Enterprise repository's CR-002.

## Supported

| | docx | pptx | xlsx |
|---|---|---|---|
| sign, so that Office reports the signature valid and covering the whole file | yes | yes | yes |
| validate a signature made by Office or by docx4j | yes | yes | yes |
| macro-enabled (`docm`, `pptm`, `xlsm`), template (`dotx`, `potx`, `xltx`) and strict OOXML | yes | yes | yes |
| more than one signature | yes | yes | yes |
| list and remove signatures | yes | yes | yes |
| visible signature line, signed | yes | (PowerPoint has none) | yes |

Signature levels: XML-DSig, XAdES-EPES (the default, and what Office writes) and XAdES-T (needs
an RFC 3161 timestamp server). Digests SHA-256 by default (what current Office writes), SHA-1
and SHA-512 on request. Keys: RSA, from a PKCS#12 file or a `java.security.KeyStore` entry.

Not supported: ECDSA; HSM or cloud KMS keys; countersignatures; XAdES-X-L and -A (the code for
XAdES-XL is present but untested); `.xlsb` and the binary formats; signing a VBA project (the
`vbaProjectSignature` part is a different mechanism).

## Signing

```java
OpcPackage pkg = OpcPackage.load(new File("in.xlsx"));
SignatureHelper helper = new SignatureHelper(pkg);
SignatureDetail detail = helper.configureSignature(new FileInputStream("me.pfx"), password);
// optional: detail.setSignerName(..), setSignerRole(..) etc for the XAdES signed properties
helper.sign();
pkg.save(new File("signed.xlsx"));
```

`helper.getSignatureConfig()` is the `SignatureConfig` (adapted from Apache POI's): the digest
(`setDigestAlgo`), the XAdES level (property `docx4j.dsig.XAdES.Level`, or `setSignatureFacets`),
the timestamp server for XAdES-T (`setTspUrl`, `setTspUser`/`setTspPass`, `setProxyUrl`),
what goes into KeyInfo (`setIncludeEntireCertificateChain`, `setIncludeIssuerSerial`,
`setIncludeKeyValue`), and the signing time (`setExecutionTime`; now by default, written in UTC
to both the OPC and the XAdES signing time, as Office does).

What is signed is what Office signs: every part and every relationship, external relationships
included, except the document properties (`docProps/core.xml`, `app.xml`), the thumbnail and the
signature parts themselves. Custom XML data parts are signed. Excel treats anything less as
invalid, not partial, so this is not configurable beyond `setPartnameBlackList`.

The signature is over what docx4j marshals, not over the bytes that were loaded, so:

- **signing a file Office signed invalidates Office's signature** (docx4j re-marshals parts
  on save, and a few of them differ byte for byte from Office's). Your own signature is valid;
  the earlier one reports INVALID;
- a file docx4j signed, loaded and signed again keeps both signatures valid.

### Signature lines

Word: `helper.createCTSignatureLine(suggestedSigner, showSignDate)` makes the line,
`helper.createDocxSignatureLineP(...)` the paragraph holding it; put the paragraph in the
document, then `helper.setVisualSignature(detail, line, signedImage, unsignedImage)` before
`sign()`. Excel: `helper.getXlsxSignatureLines(worksheetPart)` finds lines Excel drew, and
`helper.createXlsxSignatureLine(worksheetPart, line, anchor)` makes one; either goes to
`setVisualSignature` as for Word. Images are PNG (EMF also accepted by Word). Excel changes
nothing in the sheet when it signs a line: the valid and invalid images live in the signature.

## Validating

```java
List<XmlSignaturePart> signatures = SignatureHelper.getSignaturesStatus(
        new File("signed.docx"), new DefaultCertificateTrustChecker());
for (XmlSignaturePart s : signatures) {
    SignatureStatus status = s.getSignatureStatus(null);
    X509Certificate signer = s.getSigner();
    Date when = s.getSigningTime();
    List<String> unsigned = s.getUnsignedRequiredParts();   // non-empty for *_PARTIAL
}
```

`SignaturesNotPresentException` if the file has no signature (`SignatureHelper.isPackageSigned`
to ask first). The status per signature, as Office classifies them:

| Status | Meaning |
|---|---|
| `VALID` | every reference verifies, the signature value verifies, the certificate is trusted |
| `VALID_PARTIAL` | as above, but a part Office would sign is not covered (`getUnsignedRequiredParts`) |
| `RECOVERABLE` | the signature verifies, but the certificate is not trusted (self-signed, unknown root, expired at the time checked, revocation unknown). Office shows "recoverable signature" and offers to trust the certificate |
| `RECOVERABLE_PARTIAL` | both of the above |
| `INVALID` | a digest or the signature value does not verify: the file changed after signing, or the signature is malformed |

**Trust** is the caller's. `DefaultCertificateTrustChecker` builds a PKIX path from the signer
to trust anchors you choose:

- `new DefaultCertificateTrustChecker()`: the JDK's `cacerts`;
- `new DefaultCertificateTrustChecker(keyStore)`: for example `KeyStore.getInstance("Windows-ROOT")`
  on Windows, which is what Office trusts;
- `new DefaultCertificateTrustChecker(trustAnchors)`, plus `addTrustAnchor(cert)` for a root or
  a self-signed certificate;
- `setValidityCheckedAt(SIGNING_TIME | NOW)`: by default validity is judged at the signing
  time the signature claims, which is what Office does (a certificate that expired after
  signing still gives VALID);
- `setRevocation(NONE | SOFT_FAIL | REQUIRED)`, SOFT_FAIL by default (OCSP/CRL consulted if
  reachable); `setFetchIssuersByAIA(true)` to fetch missing intermediates (a JVM-wide setting).

`TrustCertificateUnconditionally` accepts any certificate (`getSignaturesStatusTrusted`); use
it to ask only whether the file changed. Implement `CertificateTrustChecker` for anything else.

**Hostile input.** Validation checks the signature's structure before Santuario sees it: only
the transforms, digest and signature methods an OOXML signature uses (no MD5, HMAC or XSLT), at
most two transforms per reference, no `RetrievalMethod`, no duplicate Id, reference URIs that are
either same-document or part names, and at most `docx4j.dsig.validation.maxReferences` (10000)
references. Santuario's own secure validation is then on while validating. (Santuario's
unmarshal-time limit of 30 references per Manifest would refuse every pptx, so it is off for
that step only.)

## Removing

`helper.removeSignatureParts(pkg)` removes every signature part and the origin part. Save the
package afterwards.

## Properties

In `docx4j.properties`:

| Key | Values | Default |
|---|---|---|
| `docx4j.dsig.XAdES.Level` | 0 XML-DSig, 1 XAdES-EPES, 2 XAdES-T | 1 |
| `docx4j.dsig.validation.maxReferences` | integer | 10000 |

(Before 17.3.2 these were `com.plutext.dsig.*`; the old names are not read.)

## Certificates for testing

Office accepts a self-signed certificate (as "recoverable" until it is trusted on the
machine) if it has the e-mail protection and Microsoft document-signing extended key usages:

```bash
openssl req -x509 -newkey rsa:2048 -sha256 -days 1825 -nodes \
   -keyout key.pem -out test.cer -subj "/CN=docx4j test" \
   -addext "basicConstraints=critical,CA:FALSE" \
   -addext "keyUsage=critical,digitalSignature,nonRepudiation" \
   -addext "extendedKeyUsage=emailProtection,1.3.6.1.4.1.311.10.3.12"
openssl pkcs12 -export -inkey key.pem -in test.cer -out test.pfx
```

For a signature Office trusts without that step you need a certificate from a CA Windows
trusts, with those key usages (an S/MIME certificate does).

## What has been checked, and where

Automated (`mvn install` in `docx4j-digsig`): each corpus file signed, saved, VALID, then
altered and INVALID; each Office-signed golden validates, the tampered one fails, and the parts
and relationships docx4j signs equal what Office signed; signature structure checks against
hostile input; two signatures; removal; the trust checker against a chain built per run.

In Office, Microsoft 365 build 16.0.20430 (2026-10): docx, pptx, xlsx, their macro-enabled,
template and strict variants, signed by docx4j, are reported valid and complete by Word,
PowerPoint and Excel; Excel signature lines both ways; SHA-256 and SHA-512; XAdES-BES/EPES.
Word 2010, 2013 and 2016 signatures validate here. Not checked: Office for Mac, Online or
mobile; LibreOffice; XAdES-T against a timestamp server; revocation checking against the
network. Validity in Office is the test that matters: a signature that verifies arithmetically
can still be one Office rejects, so a change to what is signed needs that check.

## Dependencies

`docx4j-digsig` brings Apache Santuario (`xmlsec`, Apache 2; pinned, because a version change
is a signing change), BouncyCastle (`bcpkix-jdk18on`, MIT-style; timestamps, OCSP, CRL) and
TextImageGen (Apache 2; the signature-line image). docx4j-core has none of them: without the
module a signed package still loads and saves, with its signature parts as plain XML and binary
parts, and docx4j-core finds the typed parts through the `PartProvider` service when the module
is present.
