# docx4j-digsig

Digital signatures for docx, pptx and xlsx: sign an OPC package the way Office does
(XML-DSig, XAdES-EPES or XAdES-T; SHA-256), validate signatures made by Office or by docx4j,
list and remove them, and sign Word and Excel signature lines. Formerly Plutext DigSig
(CR-033, `docs/developer/change-requests/CR-033-digsig-module.md`).

```xml
<dependency>
  <groupId>org.docx4j</groupId>
  <artifactId>docx4j-digsig</artifactId>
  <version>${docx4j.version}</version>
</dependency>
```

plus one of `docx4j-JAXB-ReferenceImpl` or `docx4j-JAXB-MOXy`, as for any docx4j module.
Santuario, BouncyCastle and TextImageGen come with this module; docx4j-core gains nothing.

## Use

```java
OpcPackage pkg = OpcPackage.load(new File("in.docx"));       // or pptx, xlsx, and their
SignatureHelper helper = new SignatureHelper(pkg);           // macro-enabled and template kin
helper.configureSignature(new FileInputStream("me.pfx"), "password".toCharArray());
helper.sign();
pkg.save(new File("signed.docx"));

// validate: VALID, VALID_PARTIAL, RECOVERABLE, RECOVERABLE_PARTIAL or INVALID per signature
for (XmlSignaturePart sig : SignatureHelper.getSignaturesStatus(new File("signed.docx"),
        new DefaultCertificateTrustChecker())) {
    System.out.println(sig.getSignatureStatus(null) + " " + sig.getSignerChain().get(0).getSubjectX500Principal());
}
```

`DefaultCertificateTrustChecker` builds a PKIX path to trust anchors you choose: the JDK's
cacerts (no-arg constructor), a `KeyStore` (`Windows-ROOT` is what Office trusts), or anchors
you add; validity is judged at the signing time the signature claims, as Office does.
`TrustCertificateUnconditionally` skips the check. `SignatureConfig` (from
`helper.getSignatureConfig()`) sets the digest, the XAdES level, a timestamp server, the
signer's XAdES details, and what goes into KeyInfo.

Properties (`docx4j.properties`): `docx4j.dsig.XAdES.Level` (0 XML-DSig, 1 XAdES-EPES, the
default, 2 XAdES-T) and `docx4j.dsig.validation.maxReferences` (10000).

Samples: `docx4j-samples-digsig` (`SignDocx`, `SignPptx`, `SignXlsx`, `SignVisible`,
`SignVisibleXlsx`, `ValidateSignature`, `IsSignerTrusted`). The signing samples take the
PKCS#12 file and its password as system properties: `-Dpfx=test.pfx -Dpfx.password=...`.

## A test certificate

No key is in this repository. For Office to accept a signature the certificate needs the
e-mail protection and Microsoft document-signing extended key usages; this makes one Word,
Excel and PowerPoint accept ("recoverable signature" until you trust the certificate on the
machine, then valid):

```bash
openssl req -x509 -newkey rsa:2048 -sha256 -days 1825 -nodes \
   -keyout key.pem -out test.cer -subj "/CN=docx4j test" \
   -addext "basicConstraints=critical,CA:FALSE" \
   -addext "keyUsage=critical,digitalSignature,nonRepudiation" \
   -addext "extendedKeyUsage=emailProtection,1.3.6.1.4.1.311.10.3.12"
openssl pkcs12 -export -inkey key.pem -in test.cer -out test.pfx
```

## What is tested, and what is not

`mvn install` runs the tests in this module: every file in `src/test/resources/corpus` is
signed, saved and validated, then altered and found INVALID; every Office-signed file in
`goldens` validates (a name containing `TAMPERED` must fail), and the parts and relationships
we sign are compared with what Office signed. Drop a file into either directory to add it.
Tests sign with a key generated per run.

Office acceptance is the real test and cannot be automated: a signature that verifies
arithmetically can still be one Office rejects, or calls partial. Checked in Microsoft 365
(build 16.0.20430) for docx, xlsx and pptx, their macro-enabled, strict and template variants,
and Excel signature lines; the Word 2010, 2013 and 2016 goldens validate. Not checked: Mac,
Online or mobile Office, LibreOffice, XAdES-T against a real timestamp server, revocation
checking with the network.

Known limits: RSA only; keys from PKCS#12 or a `KeyStore` (no HSM or KMS); signing a file
Office has signed invalidates Office's signature, because docx4j re-marshals parts on save;
Santuario is pinned at 3.0.6 because a version change is a signing change (see the CR).
