# CR-033: Digital signatures in docx4j — the `docx4j-digsig` module

Status: ACCEPTED 2026-10-08 (Jason: "i accept the recommendations in CR-033"; proposed the same
day: "prepare a CR for moving this into docx4j proper"; no additional dependencies in
docx4j-core; the `com.plutext` packages become `org.docx4j` packages). Decisions D1 to D7 taken
(§8; D7 with `template.potx` held back). Implementation from the DigSig session, with the docx4j
session reviewing phase 2 (§8). Phase 0 done 2026-10-08 (private repo 8bceacd); phase 1 (bacdc3044), phase 2 (the core
service, d21a2ddd0, review by the docx4j session requested) and phase 3's documentation done 2026-10-08 on branch `CR-033-digsig`; phase 3's Office
check awaits Jason; phase 4 after it (§10). Drafted with Claude Fable 5.1 from the DigSig
session, with the inventory of §2 measured in `../docx4j-digsig_11_4` at commit d7ea9a4 plus its
uncommitted phase 5 samples. Owner: Jason Harrop.

Scope: move Plutext DigSig (OPC package signatures: XML-DSig, XAdES-EPES and XAdES-T, for docx,
pptx and xlsx; validation; signature lines) from its private repository into this reactor as a
new module, `docx4j-digsig`, with a samples module beside it, and retire the private repository
as the place where the code lives. The code moves as it is: a rename and a re-homing, with
identical signing output, verified by its own tests and by Office. Not in scope: any change to
what is signed or how (that is the Enterprise CR-002 and its successors); ECDSA, HSM or KMS
keys, countersignatures, XAdES-X-L/A; the instance-method validation API the 8.x manual showed
(Enterprise CR-002 §5, phase 5); signing the bytes on disk (CR-002 phase 6).

## 1. Goal

A docx4j user adds one dependency and can sign and validate a docx, pptx or xlsx:

```xml
<dependency>
  <groupId>org.docx4j</groupId>
  <artifactId>docx4j-digsig</artifactId>
  <version>${docx4j.version}</version>
</dependency>
```

```java
SignatureHelper helper = new SignatureHelper(pkg);          // org.docx4j.dsig
helper.configureSignature(pkcs12Stream, password);
helper.sign();
pkg.save(out);

List<XmlSignaturePart> sigs = SignatureHelper.getSignaturesStatus(file,
        new DefaultCertificateTrustChecker());               // VALID, VALID_PARTIAL, RECOVERABLE, ... 
```

with docx4j-core unchanged in its dependencies: Santuario and BouncyCastle are dependencies of
`docx4j-digsig` only. `docx4j-core` keeps working, as today, when the module is absent: a signed
package loads, its signature parts are generic XML and binary parts, and a save preserves them.

Why now: CR-002 in the Enterprise repository (`../Plutext-Enterprise-Java-11/docs/developer/
change-requests/CR-002-digsig-pptx-xlsx.md`) has brought DigSig to docx4j 17.2.1 with pptx and
xlsx signatures that Office accepts as complete, a certificate trust checker, and a test suite,
all on branch `VERSION_17_2_1` of the private repository. Shipping it as an Enterprise product
again (`enterprise/digsig-release` in the portfolio registry) still needs a licence Product,
enforcement, an obfuscation script and the Enterprise build steps, none of which exist on the
docx4j 17 line. The portfolio's competition analysis (`../docx4j-portfolio/docs/
commercial_competition.md` §7 and §11.3) names digital signatures as docx4j's most conspicuous
capability gap: five of eight vendors have it, Apache POI (from which DigSig's core was adapted)
signs OOXML for free, and Aspose includes it in Words. Putting it in docx4j closes the gap for
every user and removes the release work.

## 2. What moves (measured 2026-10-08)

The private repository `plutext/docx4j-digsig` (GitHub, private), checkout
`../docx4j-digsig_11_4`, branch `VERSION_17_2_1` (jakarta, docx4j 17.2.1, Santuario 3.0.6;
`com.plutext:digsig:17.2.1.0`). Tracked: 178 files. What matters:

| | Count | Notes |
|---|---|---|
| Java sources, `src/main/java` | 108 | 66 are generated JAXB classes (2016, from the xsd below), checked in |
| `com.plutext.dsig` (+ `anchor`) | 6 | the public API: `SignatureHelper`, `DefaultCertificateTrustChecker`, two exceptions, two signature-line helpers |
| `com.plutext.crypt.dsig` (+ `facets`, `services`) | 28 | the core, adapted from POI `poifs.crypt.dsig`: `SignatureInfo`, `SignatureConfig`, `SignatureDetail`, `OOXMLSignatureFacet`, `XAdESSignatureFacet`, `RelationshipTransformService`, `SignatureMarshalListener`, `DSigJAXBContext`, ... |
| `org.docx4j.openpackaging.parts.digitalsignature` | 7 | `XmlSignaturePart`, `SignatureOriginPart`, `SignatureStatus`, `CertificateTrustChecker`, `CertificateTrustCheckResult`, `TrustCertificateUnconditionally`, `SignatureStructureCheck`. Already in docx4j's namespace; not present in docx4j-core |
| `org.apache.xml.security.binding.xmldsig` | 1 | a `package-info.java` only (§4.4: a split package with Santuario's jar) |
| Generated JAXB: `com.plutext.org.etsi.uri.x01903.v13` / `.v141`, `com.plutext.com.microsoft.schemas.office.x2006.digsig`, `com.plutext.org.openxmlformats.schemas.xpackage.x2006.digitalSignature` | 53 + 3 + 4 + 6 | ETSI XAdES 1.3.2 and 1.4.1, Office 2006 digsig, OPC digital-signature |
| Schemas, `xsd/digSig` | 7 | `xmldsig-core-schema.xsd`, `XAdES.xsd`, `XAdESv141.xsd`, `office-2006-digital-signature.xsd`, `opc-digSig.xsd`, `office-encryptionData.xsd`, a stray "Copy of XAdES.xsd" |
| Tests, `src/test/java/com/plutext/dsig` | 9 classes, 46 `@Test` | JUnit 4; parameterised over the fixtures; a key pair and certificate generated per run with BouncyCastle |
| Fixtures | 14 corpus files (1.4 MB), 12 goldens + 9 originals (0.8 MB) | goldens are Office-signed (Microsoft 365 build 16.0.20430 and Word 2010/2013/2016); `TAMPERED` in a name means it must fail |
| Samples, `src/samples/java/signaturesamples` | 7 | `SignDocx`, `SignPptx`, `SignXlsx`, `SignVisible`, `SignVisibleXlsx`, `ValidateSignature`, `IsSignerTrusted`; not built by Maven |
| Non-Java resources in `src/main` | 0 | |
| Docs | `notes_2026.txt`, `CLAUDE.md`, `README.md` (one line) | the 2016 `build.xml`, `ZKMScript.txt`; `TODO.txt` and `ChangeLog.txt` untracked |

Dependencies of the private pom, and what each is for:

| Dependency | Version there | Used by | Licence | In the module |
|---|---|---|---|---|
| `org.apache.santuario:xmlsec` | 3.0.6 | everything: `javax.xml.crypto` provider, c14n, `TransformService` base, the `binding.xmldsig` JAXB classes | Apache 2 | yes, 3.0.6 (§4.5) |
| `org.bouncycastle:bcpkix-jdk15on` | 1.54 (2016) | `TSPTimeStampService` (XAdES-T), `XAdESXLSignatureFacet` (OCSP/CRL values); the tests (certificates per run) | MIT-style | yes, `bcpkix-jdk18on` current (§4.5) |
| `org.bouncycastle:bcprov-ext-jdk15on` | 1.54 | nothing directly (`-ext` adds algorithms DigSig does not use) | MIT-style | `bcprov-jdk18on`, transitively from bcpkix |
| `org.docx4j.org.capaxit.textimage:TextImageGen` | 1.9 | `SignatureDetail`: renders the signer's name as the signature-line image | Apache 2 (its pom); docx4j-published, on Central; no `Automatic-Module-Name` | yes |
| `xerces:xercesImpl` | 2.12.0 | no import anywhere in main, test or samples | Apache 2 | no, unless phase 0 finds a behavioural reason (§5) |
| `ch.qos.logback:logback-classic` | 1.5.19, compile scope | logging in tests and samples | EPL/LGPL | test scope |
| `org.slf4j:slf4j-api` | 2.0.18 | logging | MIT | from the parent |
| `org.docx4j:docx4j-JAXB-ReferenceImpl` | 17.2.1, compile scope | the JAXB runtime for tests | | test scope (the consumer picks one, as for every module) |
| `junit:junit` | 4.13.2 | tests | | from the parent |

Properties read (`Docx4jProperties`): `com.plutext.dsig.XAdES.Level`, `com.plutext.dsig.
validation.maxReferences`; `docx4j.properties` in `docx4j-samples-resources` (line 750) and
`docx4j-core-tests` (line 500) already carry a 2016 "XML Dig Signature setup" block with
`com.plutext.dsig.XAdES.Level=1` and two commented keys.

Provenance and licence: the pom already declares Apache 2. 26 sources carry the Apache header
(the POI-derived core); 82 carry none, of which 66 are the generated classes and 16 are Plutext's
own (`SignatureHelper`, the trust checker, the parts package, the helpers). `SignatureHelper.sign`
inserts a "Created with a trial/evaluation version of Docx4j Enterprise" paragraph into every
docx (its trial date is May 2016, so it fires always); that is the only licensing code and it goes.

## 3. What docx4j-core already has, and what it must not gain

docx4j-core has been built for this module since the Enterprise years:

- `ContentTypes.DIGITAL_SIGNATURE_XML_SIGNATURE_PART` and `DIGITAL_SIGNATURE_ORIGIN_PART`;
  `Namespaces.DIGITAL_SIGNATURE`, `_CERTIFICATE`, `_ORIGIN`.
- `ContentTypeManager` creates `org.docx4j.openpackaging.parts.digitalsignature.XmlSignaturePart`
  and `SignatureOriginPart` by `Class.forName`, falling back to a default XML part and a
  `BinaryPart` when the class is absent ("the dig sig functionality is in Enterprise only").
  `Load3`, `LoadFromZipNG` and `FlatOpcXmlImporter` compare the part's class name as a string.
- `NamespacePrefixMappings` maps `mdssi` and the Office 2006 `digsig` namespace both ways.
- `org.docx4j.org.apache.xml.security` (64 classes): docx4j's own copy of Santuario's
  canonicaliser, used by `XmlUtils` and `JaxbXmlPart` for c14n of parts. It is not a signing
  library and the module does not use it; the two coexist today (different packages).
- `module-info` already `requires java.xml.crypto`.
- `xsd/digSig` holds the three Office 2006 encryption schemas, generated into
  `docx4j-generated-objects` (`org.docx4j.com.microsoft.schemas.office.x2006.encryption` and the
  two `keyEncryptor` packages; `DSigJAXBContext` lists them, though no DigSig class uses them).
- `org.docx4j.vml.officedrawing.CTSignatureLine` (the signature-line VML) is in the generated
  objects.

So the module slots into hooks that exist. The constraint (Jason, 2026-10-08) is that docx4j-core
gains **no dependency**: Santuario, BouncyCastle and TextImageGen are declared by `docx4j-digsig`
alone, and the parent pom gains at most version properties for them. Any core change in this CR
is code, not a dependency, and §4.3 keeps even that small.

## 4. Design

### 4.1 Modules

Two new reactor modules, in the parent pom after `docx4j-markdown` and in the samples block:

| Module | Artifact | Content | Published |
|---|---|---|---|
| `docx4j-digsig` | `org.docx4j:docx4j-digsig` | the API, the core, the parts package, the generated JAXB classes; tests and fixtures in-module (`src/test`), as `docx4j-markdown` does and as the peer docx4j session advised for a crypto module with heavy fixtures | yes, to Central with the release |
| `docx4j-samples-digsig` | `org.docx4j:docx4j-samples-digsig` | the seven samples, compiled by the build (today they are not), `maven.deploy.skip` like the other samples modules | no |

Layout of `docx4j-digsig` as the other modules: `src/main/java`, `src/test/java`,
`src/test/resources/{corpus,goldens}`, output to `bin`, version `${revision}`, flatten as the
parent arranges. Its `module-info.java`:

```java
module org.docx4j.digsig {
    requires transitive org.docx4j.core;
    requires org.docx4j.generated_objects;
    requires jakarta.xml.bind;
    requires org.slf4j;
    requires java.xml.crypto;
    requires org.apache.santuario.xmlsec;          // automatic module (Automatic-Module-Name), as docx4j_xalan_* are today
    requires org.bouncycastle.provider;
    requires org.bouncycastle.pkix;
    requires org.bouncycastle.util;
    requires TextImageGen;                          // automatic; its exact name from the jar in phase 1

    exports org.docx4j.dsig;
    exports org.docx4j.dsig.anchor;
    exports org.docx4j.dsig.crypt;
    exports org.docx4j.dsig.crypt.facets;
    exports org.docx4j.dsig.crypt.services;
    exports org.docx4j.openpackaging.parts.digitalsignature;
    exports org.docx4j.org.etsi.uri.x01903.v13;      // and v141, the digsig and digitalSignature packages
    ...
    opens org.docx4j.org.etsi.uri.x01903.v13 to jakarta.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    ... // the four generated packages
}
```

The schemas go to `xsd/digSig` beside the encryption ones (the stray copy dropped), **not** into
`ROOT.xsd`: they are the record of what the checked-in classes were generated from, not an input
to `docx4j-generated-objects` (D6).

`docx4j-bundle` does not include the module (it shades docx4j-core, the JAXB RI and the samples
resources only; `docx4j-markdown` and `docx4j-export-fo` are not in it either). Recorded, not
changed.

### 4.2 Package renames

| From | To | Why this name |
|---|---|---|
| `com.plutext.dsig` | `org.docx4j.dsig` | the API package; short, and `dsig` is what the property keys and the Office namespace call it |
| `com.plutext.dsig.anchor` | `org.docx4j.dsig.anchor` | |
| `com.plutext.crypt.dsig` | `org.docx4j.dsig.crypt` | one tree under `org.docx4j.dsig`; `crypt` kept so the POI lineage (`poifs.crypt.dsig`) stays visible |
| `com.plutext.crypt.dsig.facets` | `org.docx4j.dsig.crypt.facets` | |
| `com.plutext.crypt.dsig.services` | `org.docx4j.dsig.crypt.services` | |
| `com.plutext.org.etsi.uri.x01903.v13` | `org.docx4j.org.etsi.uri.x01903.v13` | docx4j's convention for generated classes: `org.docx4j.` + the reversed namespace, as `org.docx4j.com.microsoft.schemas.office.x2006.encryption` |
| `com.plutext.org.etsi.uri.x01903.v141` | `org.docx4j.org.etsi.uri.x01903.v141` | |
| `com.plutext.com.microsoft.schemas.office.x2006.digsig` | `org.docx4j.com.microsoft.schemas.office.x2006.digsig` | |
| `com.plutext.org.openxmlformats.schemas.xpackage.x2006.digitalSignature` | `org.docx4j.org.openxmlformats.schemas.xpackage.x2006.digitalSignature` | |
| `org.docx4j.openpackaging.parts.digitalsignature` | unchanged | already the right place; docx4j-core's reflective hooks name it |
| `org.apache.xml.security.binding.xmldsig` (package-info) | deleted (§4.4) | |
| `signaturesamples` (samples) | `org.docx4j.samples.digsig` | as the other samples modules |

The rename is mechanical (`sed` over imports and the `DSigJAXBContext` context path, directories
moved), but two places carry package names as strings and must be checked by grep, not by the
compiler: `DSigJAXBContext.newInstance("...")` and the `jaxb.index`/`ObjectFactory` discovery of
the generated packages. The property keys are renamed separately (§4.6).

Alternative considered: `org.docx4j.openpackaging.digsig` or `org.docx4j.crypt.dsig`. Rejected
for a single, discoverable tree; D2 if Jason prefers otherwise.

### 4.3 How docx4j-core finds the parts

Today's mechanism, `Class.forName` in `ContentTypeManager`, keeps working: on the classpath
trivially, and on the module path provided `org.docx4j.digsig` is in the module graph (it is, in
any application that uses `SignatureHelper`, because that application `requires` it) and exports
the parts package (it does). Core reflection does not need `org.docx4j.core` to read the module.
Phase 1 therefore changes nothing in core, and the module is usable from phase 1.

The docx4j session (2026-10-08) pointed to the reactor's own pattern for an optional module core
must find without a `requires`: `META-INF/services` with `uses` in core and `provides` in the
module, as `docx4j-JAXB-ReferenceImpl` / `-MOXy` do. Phase 2 does that, small and dependency-free:

- a `PartFactory`-style service interface in core (`org.docx4j.openpackaging.parts.
  PartProvider`, say: given a content type and a `PartName`, return a `Part` or null), `uses` in
  core's `module-info`;
- `ContentTypeManager` asks the loaded providers for the two signature content types before the
  default; the `Class.forName` branch stays as the fallback for one release and then goes;
- the module `provides` it and declares it in `META-INF/services`;
- `Load3`, `LoadFromZipNG` and `FlatOpcXmlImporter` drop the class-name string compare for
  `instanceof XmlPart` / `JaxbXmlPart` (the `Load3` line is already commented out and the
  `JaxbXmlPart` case covers it; the other two need the same reading).

D3 is whether phase 2 is in this CR or deferred. Recommendation: in, because it is small, it is
the mechanism the reactor already uses, and the string compares are the only thing in core that
knows DigSig's class names.

### 4.4 JPMS: the split package and the provider

- **Split package.** DigSig declares `org.apache.xml.security.binding.xmldsig/package-info.java`
  (an `@XmlSchema` with the XML-DSig namespace as the default prefix) in a package that
  Santuario's jar owns (24 classes; it ships no `package-info` and no `module-info`, so it is an
  automatic module). Two modules exporting one package is a resolution error on the module path.
  The file is deleted. What it did, if anything, is measured first: sign a docx with and without
  it and diff `sig1.xml`. The expectation is no difference, since the signature DOM is built by
  Santuario's `DOMXMLSignature.marshal`, not by JAXB, and the JAXB classes are used for reading
  (`XmlSignaturePart`) and for the XAdES `QualifyingProperties` object, whose prefixes
  `SignatureMarshalListener` already fixes. If `sig1.xml` changes, the prefix goes into the JAXB
  `NamespacePrefixMapper` the module already relies on (`NamespacePrefixMappings` in core knows
  `mdssi` and `digsig`; `ds` would join it, a one-line core change with no dependency).
- **Automatic modules.** `requires org.apache.santuario.xmlsec` and `requires TextImageGen` are
  automatic modules in a published `module-info`. The reactor accepts this already
  (`docx4j_xalan_serializer`, `docx4j_xalan_interpretive`, `mbassador` in core). Noted for the
  release notes; Santuario 4.x has a real `module-info` (§4.5) and would remove one of them.
- **The `TransformService` provider.** `RelationshipTransformService.registerDsigProvider()`
  registers a `java.security.Provider` programmatically before signing (`SignatureInfo` line
  320). That is classloader-based and works on both paths; nothing to change.
- **`opens`.** The four generated packages open to the JAXB runtimes as the generated objects
  module does. `XmlSignaturePart` unmarshals with `DSigJAXBContext.jcXmlDSig`; the module's
  context lists its own four packages, Santuario's `binding.xmldsig`, and (today) the three
  encryption packages from generated objects that nothing uses: those are dropped from the context
  path, after the tests confirm it (they bind no element DigSig reads).

### 4.5 Dependency versions

- **Santuario 3.0.6**, pinned. It is what CR-002 measured against Office; 2.3.0 signed invalidly
  on `master` (`notes_2026.txt`), so a version change is a signing change and gets the Office
  check. Santuario 4.0.x (latest 4.0.4) exists, is Jakarta and Java 11+, and ships a
  `module-info`; moving to it is a separate, later item, not part of a move.
- **BouncyCastle** from `bcpkix-jdk15on` / `bcprov-ext-jdk15on` 1.54 (2016; the `jdk15on` line is
  discontinued) to `bcpkix-jdk18on` current (1.86 on Central; 1.78.1 in the local repository),
  which brings `bcprov-jdk18on` and `bcutil-jdk18on`. Java 8+ bytecode, fine for the Java 11
  baseline, real `module-info`s (`org.bouncycastle.pkix`, `.provider`, `.util`). The API DigSig
  uses (`tsp`, `cms`, `cert.ocsp`, `operator`, `JcaX509CertificateConverter`) has been stable
  since 1.5x; compile errors, if any, are in `TSPTimeStampService` and `XAdESXLSignatureFacet`,
  neither of which the tests exercise (XAdES-T needs a timestamp server), so phase 1 compiles them
  and phase 3's Office check includes one XAdES-T signature if a free TSA is reachable
  (`notes_2026.txt` TODO).
- **`xercesImpl` dropped** unless phase 0 shows a difference. `notes_2026.txt` notes a Xerces
  behaviour (`getDocumentElement()` unset while an event is dispatched) that the JDK's internal
  Xerces shares; the tests tell.
- **`TextImageGen` 1.9**, as is (`org.docx4j.org.capaxit.textimage`, on Central).
- `slf4j`, `junit`, `logback` (test scope) from the parent's properties.

Third-party licences to record where the portfolio lists them (`../docx4j-portfolio/docs/
dependencies.md`, per the docx4j session): Santuario Apache 2, BouncyCastle MIT-style,
TextImageGen Apache 2. The bundle's NOTICE is unaffected (§4.1).

`requires TextImageGen` in §4.1 is the name the module system derives from the jar's file name
(the jar has no `Automatic-Module-Name`), so it is fragile: a renamed jar breaks it. A later
TextImageGen release with the manifest entry fixes that; noted, not in this CR.

### 4.6 Properties and configuration

Keys renamed to docx4j's style. The old keys are not read (Jason, 2026-10-08: "no need to keep
the old keys as a fallback"); the CHANGELOG names the rename so a user with the 2016 block in a
`docx4j.properties` knows to update it:

| Old | New |
|---|---|
| `com.plutext.dsig.XAdES.Level` | `docx4j.dsig.XAdES.Level` |
| `com.plutext.dsig.validation.maxReferences` | `docx4j.dsig.validation.maxReferences` |
| `com.plutext.dsig.XAdES.ExcludeFromManifest` (commented in the properties files; check whether code reads it) | `docx4j.dsig.XAdES.ExcludeFromManifest` or removed |

The 2016 block in `docx4j-samples-resources/.../docx4j.properties` and `docx4j-core-tests/.../
docx4j.properties` is rewritten for the new keys and the current defaults (XAdES-EPES, SHA-256).
`DefaultCertificateTrustChecker.setFetchIssuersByAIA` sets a JVM-wide security property; that is
documented, not changed.

### 4.7 Licence, headers, and the trial notice

- The trial paragraph (`SignatureHelper.TRIAL_BLURB`, `trialBlurb`, `now`/`later`) is removed.
  Side effect worth stating: re-signing a docx DigSig signed no longer invalidates the first
  signature for that reason (Enterprise CR-002 §6.3 found the notice was the only cause for docx).
  `MultipleSignaturesTest` records the new behaviour.
- New or Plutext-authored files get the docx4j header: Apache 2, "Copyright <year>, Plutext Pty
  Ltd. / This file is part of docx4j." (never the old CLA wording). POI-derived files keep the
  ASF header and the "adapted from Apache POI" note. Generated classes get the header docx4j's
  generated code carries, or none, as `docx4j-generated-objects` does.
- LF line endings throughout (the private repo's Java is LF; its pom is CRLF and is not copied).
- The GitHub repository `plutext/docx4j-digsig` stays private as the archive (its history holds
  the expired Comodo/GlobalSign/StartSSL `.pfx` files, untracked but on disk, and 2016 customer
  samples); the move brings the tracked sources, tests and fixtures only, and the git history is
  not imported (D5 if Jason wants it preserved by a subtree merge instead).

### 4.8 Tests and fixtures

The nine test classes move as they are (package `org.docx4j.dsig`), run by surefire in-module
under `mvn install`, one JVM per class as `docx4j-core-tests` does if the Santuario provider
registration or `Docx4jProperties` state needs it (phase 1 measures; the private repo runs them
in one JVM today). `TestSupport.newSigner` generates the key per run with BouncyCastle, so no key
is committed.

Fixtures (2.2 MB) move to `src/test/resources/corpus` and `goldens`. Before they land in a public
repository Jason confirms each may be published; the list for that check:

| | Files | Origin |
|---|---|---|
| corpus | `sections.pptx` | Enterprise repository sample |
| | `table.pptx`, `AutoShapes.pptx`, `pptx-chart.pptx`, `comments.xlsx`, `simple.xlsx`, `docx4j_jaxb_packages.xlsx`, `cr022-data-model.xlsx`, `strict.pptx`, `strict-chart.xlsx` | docx4j samples and test resources (already public) |
| | `sign-me-please.docx`, `pivot.xlsm`, `template.potx` (617 KB), `template.xltx` | DigSig repository root / made for CR-002 |
| goldens | `word2010.docx`, `word2010.TAMPERED.docx`, `word2013.docx`, `word2016.docx` | Word-signed, 2010 to 2016, with the Comodo and GlobalSign certificates of the time (public certificates, no private material) |
| | `minimal.pptx`, `minimal.xlsx`, `notes.pptx`, `rich.pptx`, `rich.xlsx`, `macro.pptm`, `macro.xlsm`, `sigline.xlsx` + `originals/` | Office-signed 2026-10-02 with the self-signed `plutext-digsig-test` certificate |

The Office goldens embed only certificates, never keys. `template.potx` is the one file whose
size (617 KB) argues for a smaller replacement if a potx with a signature-relevant feature set can
be made.

New tests the move itself needs: a `ModulePathTest` or an equivalent check that the module
resolves on the module path with core (the split package of §4.4 is exactly what such a test
catches; the reactor's build runs on the classpath, so it would not); a test that the new
`docx4j.dsig.*` keys are the ones read (`DefaultsTest` is the natural home).

### 4.9 Samples

`docx4j-samples-digsig` compiles the seven samples. They take the PKCS#12 path and password as
arguments or from `docx4j.properties`; **no `.pfx` is committed.** The module's `README.md`
carries the one-command OpenSSL recipe for a self-signed test certificate from `notes_2026.txt`
(the `emailProtection` and Microsoft document-signing EKUs Office wants), and says what Word does
with it (recoverable until the certificate is trusted). `IsSignerTrusted` shows
`DefaultCertificateTrustChecker` against the JDK's cacerts and against a Windows `ROOT` store.

### 4.10 Documentation

- `CHANGELOG.md`, in the open section: "New module docx4j-digsig: digital signatures (XML-DSig,
  XAdES) for docx, pptx and xlsx, formerly Plutext DigSig (CR-033)", and the property-key rename.
- `docs/DigitalSignatures.md`: what is supported per format, the API, trust checking, Office
  versions actually tested (Microsoft 365 build 16.0.20430 and Word 2010/2013/2016 goldens; not
  Mac, Online or Android), the limitations CR-002 records (re-signing an Office-signed docx
  invalidates Office's signature; RSA only; PKCS#12 or `KeyStore` keys; the 30-reference
  workaround and `SignatureStructureCheck`), and the test-certificate recipe. Source: the 8.x
  Enterprise manual's chapter corrected by CR-002 §2.1 and §6.
- `CLAUDE.md` module map: one line for the module. `notes_2026.txt` stays in the private
  repository (Jason, 2026-10-08: "do not add notes_2026.txt to the target"); the Santuario 3
  analysis it holds, the reason `SignatureMarshalListener` does what it does, is summarised in
  that class's Javadoc and in `docs/DigitalSignatures.md` instead.

### 4.11 What happens on the Enterprise side

Jason's call (D5), recorded here because the registry entries change:

- `enterprise/digsig-release` (planned, no CR: licence Product, enforcement, ZKM, build steps) is
  **superseded** by this CR. Its remaining content, the BouncyCastle upgrade and the docx4j 17
  port, is here or done.
- `enterprise/CR-002.5` (samples, manual chapter, release): the samples are done and move with
  the code; the manual chapter becomes `docs/DigitalSignatures.md` here; "release" becomes a
  docx4j release. The Enterprise manual's "we expect to re-introduce it" gets a pointer to docx4j.
- `enterprise/CR-002` stays the record of the pptx/xlsx work; its Status gains a line saying the
  code's home moved, and its §9 the new path. Future signing work is docx4j CRs.
- `commercial_competition.md` §11.3 recommended "bring DigSig back, included in Enterprise at no
  extra charge"; this CR takes the other branch of §10's open decision ("Build digital
  signatures (open docx4j, or Enterprise)"). The competition table's "none" for docx4j becomes a
  capability once this ships. The price list does not list signatures, so nothing to retract.

## 5. Phases

| Phase | What | Size | Needs |
|---|---|---|---|
| 0 | Preparation in the private repository: commit the phase 5 samples and CR §6.8 changes (uncommitted today); run the 46 tests as the baseline; run them again without `xercesImpl` and without the `binding.xmldsig` `package-info` (diffing a `sig1.xml` each time); fixture provenance list to Jason (§4.8); record the output of a reference signing to compare after the move | S | nothing |
| 1 | The module: copy, rename (§4.2), `module-info`, pom and parent module list, dependency versions (§4.5), property keys (§4.6), trial notice out, headers (§4.7), `notes_2026.txt` in; `mvn install` green with the tests in-module; the reference signing from phase 0 reproduced byte-for-byte apart from time and random content; `master` (Santuario 2.0.6) still validates it; CHANGELOG | M | phase 0; D1, D2, D6 |
| 2 | Core: the part provider service and the removal of the class-name strings (§4.3); the module-path test (§4.8) | S | phase 1; D3 |
| 3 | Samples module, `docs/DigitalSignatures.md`, properties files, portfolio licence list; **Office check on the VM**: Word, Excel and PowerPoint open a file signed by the module from each of docx, xlsx, pptx and report it valid, and the Excel signature line; one XAdES-T if a TSA is reachable | M | phase 1; Jason and the Office VM |
| 4 | Decommission and registry: private repo `README` points here; Enterprise CR-002 Status and §9, `enterprise/digsig-release` and `CR-002.5` entries, manual pointer, competition note (§4.11); `tasks.yaml` updated and checked | S | phase 3; D5 |

Sizes as the Enterprise CRs use them (S about a day, M several). Phase 1 is the move; phases 2
and 3 can run in parallel once it lands. The release that carries it is D4.

## 6. Risks

- **A behaviour change hidden in the move.** The rename touches every file; the JAXB context
  path and the `package-info` deletion are the two places a change can alter the signature XML.
  Mitigations: the phase 0 reference signing diffed after phase 1; the goldens test (we sign what
  Office signed) and the round-trip tests; `master`'s Santuario 2.0.6 validating the output; and
  the Office check as the final gate, since a signature that verifies arithmetically can still be
  one Office rejects (CR-002's phase 2 found exactly that).
- **JPMS.** Three automatic modules in the graph, a provider registered programmatically, JAXB
  packages that must `open`. The reactor's own build is classpath-based, so the module-path test
  in §4.8 is the only thing that would catch a regression here. Santuario 4.x and a
  `module-info` for TextImageGen would reduce the exposure, both later.
- **MOXy.** `DSigJAXBContext` checks for MOXy and logs; whether the module works under
  `docx4j-JAXB-MOXy` is unknown (the private repository tests only the RI). The tests run with the
  RI; a MOXy run is attempted in phase 1 and the result documented, not gated on.
- **BouncyCastle 1.54 to 1.8x** compiles differently in the two untested classes (XAdES-T, XAdES-XL).
  Compile is the gate; function is only testable against a timestamp server.
- **Fixture provenance.** One Enterprise sample and one 617 KB template; §4.8's list is for Jason
  before phase 1 pushes anything.
- **Expectations.** Publishing a signing API in docx4j invites requests the move does not
  answer (ECDSA, HSM/KMS, countersignatures, LibreOffice validation). `docs/DigitalSignatures.md`
  says what is supported and tested, and the rest are future CRs.

## 7. Open questions

- Should the generated JAXB classes be regenerated with the reactor's XJC (consistent with the
  rest of docx4j, picks up `Copyable` and parent pointers, but adds an XJC execution and the risk
  of a changed binding) or stay checked in as they have been since 2016 (D6)?
- Does anything read `com.plutext.dsig.XAdES.ExcludeFromManifest`, or is it a documented key
  with no code? Phase 1 greps.
- One test JVM or one per class (§4.8)? Phase 1 measures.

## 8. Decisions

**Decided 2026-10-08 (Jason): the renamed property keys (§4.6) have no fallback to the
`com.plutext.dsig.*` names.** The old keys stop being read; the rename is a CHANGELOG item.

**Accepted 2026-10-08 (Jason): the recommendations below, D1 to D6.** That is: D1 the module is
`docx4j-digsig`; D2 the package table of §4.2; D3 the core service lookup is phase 2 of this CR;
D4 the release is whichever is open when phase 3's Office check passes; D5 §4.11 as written
(DigSig becomes an open-source docx4j module, the private repository is the archive, history not
imported); D6 the generated classes stay checked in (a named departure from the adding-a-schema
recipe). D7 decided the same day: the fixtures of §4.8 go in as listed except `template.potx`, which is
not committed until Jason has made a smaller potx to replace it (`RoundTripTest` runs over
whatever is in `corpus`, so the module's tests are unaffected by its absence).

**Also accepted 2026-10-08 (Jason): who implements.** The DigSig session (the one that wrote this
CR and holds the Santuario and Office-check context) implements phases 0, 1, 3 and 4, working in
a git worktree of `../docx4j` on a branch `CR-033-digsig` from `VERSION_17_3_2`, so that the
docx4j session's uncommitted CR-032 work is never in its diff. Phase 2, the core change, is
written there too but reviewed by the docx4j session before it is pushed. Once phase 1 lands the
code's home is docx4j and, by the portfolio's one-agent-per-repository rule, the docx4j session
owns it from then on; the private repository is the archive.

As proposed:

- **D1. Module name.** `docx4j-digsig` (recommended: it is what the repository and the registry
  call it) or `docx4j-signatures`.
- **D2. Package names.** The table in §4.2 (recommended) or another root.
- **D3. Core's lookup.** Phase 2's service in this CR (recommended) or keep `Class.forName` and
  defer.
- **D4. Release.** 17.3.2 (open, release date blank) if phase 1 lands before it ships, else the
  next. A new module is a minor-version event by most conventions; the reactor has added modules
  in patch releases before (`docx4j-markdown` arrived in 17.0.4). Recommended: whichever release
  is open when phase 3's Office check passes.
- **D5. Enterprise consequences.** §4.11 as written: DigSig becomes an open-source docx4j module,
  the private repository is the archive, history not imported. Alternatives: import the history
  by subtree merge; keep a licensed Enterprise feature on top (there is none left once the trial
  notice goes).
- **D6. Generated classes.** Checked in as today (recommended; named departure from the
  adding-a-schema recipe, whose step 0 asks for exactly this naming) or regenerated.
- **D7. Fixtures.** Publish the list in §4.8 as is, or replace `sections.pptx` and
  `template.potx`. Decided 2026-10-08 (Jason): as listed, `template.potx` held back for a smaller
  replacement.

## 9. References

- Private repository: `../docx4j-digsig_11_4` (branch `VERSION_17_2_1`, commit d7ea9a4 plus
  uncommitted phase 5 samples); `notes_2026.txt` and `CLAUDE.md` there. `../docx4j-digsig` is the
  `master` checkout (javax, docx4j 8.x, Santuario 2.0.6) used for the cross-check.
- Enterprise CR-002, `../Plutext-Enterprise-Java-11/docs/developer/change-requests/
  CR-002-digsig-pptx-xlsx.md`: §2 (what DigSig is), §4.1 (secure validation and the 30-reference
  cap), §4.5 (`DefaultCertificateTrustChecker`), §6 (findings, Office measurements), §8
  (decisions, including SHA-256 as the default).
- Portfolio: `../docx4j-portfolio/tasks.yaml` entries `enterprise/CR-002`, `enterprise/CR-002.5`,
  `enterprise/digsig-release`; `docs/commercial_competition.md` §7, §10, §11.3.
- docx4j-core: `ContentTypeManager` (lines 517 to 541), `Load3` (line 499), `LoadFromZipNG`
  (line 601), `FlatOpcXmlImporter` (line 518), `NamespacePrefixMappings` (lines 133, 396, 650,
  872), `module-info.java`.
- The reactor's pick-one service pattern: `docx4j-JAXB-ReferenceImpl` / `docx4j-JAXB-MOXy`.
- Apache Santuario 3.0.6 (`Automatic-Module-Name: org.apache.santuario.xmlsec`); 4.0.4 current.
  BouncyCastle `bcpkix-jdk18on` 1.86 current. Apache POI `poifs.crypt.dsig`.
- ECMA-376 Part 2 (Open Packaging Conventions), digital signatures; ETSI TS 101 903 (XAdES).

## 10. Implementation record

### Phase 0 (2026-10-08), in the private repository

Commit 1bcc4bd there commits the phase 5 samples and the CR-033 pointers (the tree the move
starts from); c8f95d9 in the Enterprise repository commits CR-002 §6.8. Baseline: 136 tests,
0 failures, 1 skipped by design (46 `@Test` methods, parameterised over the fixtures).

The two experiments, signing `sign me please.docx` with `plutext-digsig-test.pfx` four ways
(as is; without `xercesImpl`; without the `binding.xmldsig` `package-info`; without both):
the four `sig1.xml` are identical after normalising base64 content and times, except for the
wall-clock time inside the `SignatureProperties` Id, and all four validate VALID. The suite
passes without both. Commit 8bceacd there removes them. So the split package of §4.4 was
carrying nothing: the signature DOM is Santuario's, and the prefixes are the marshal
listener's.

### Phase 1 (2026-10-08), branch `CR-033-digsig` in a worktree of this repository

The move, by a script (`PKG_MAP` of §4.2 applied textually, property keys first; headers of
§4.7 substituted where the Plutext Component Agreement header was; LF throughout) plus the
hand edits: the trial notice and its five imports out of `SignatureHelper`;
`MultipleSignaturesTest.signedAgain_docx` now expects both signatures VALID, and does;
`DSigJAXBContext` without the three encryption packages (nothing bound them);
`SignatureDetail` on `java.util.Base64` instead of commons-codec (which the module would
otherwise have had to `requires`); and one BouncyCastle 1.54 to 1.78.1 API change,
`DERTaggedObject.getObject()` to `getExplicitBaseObject()` in `XAdESXLSignatureFacet`
(`ResponderID`'s alternatives are explicit tags). `template.potx` not copied (D7);
`office-encryptionData.xsd` not copied (docx4j's `xsd/digSig` already has that schema under
its ASF header). `xercesImpl` gone; `logback` and the JAXB RI at test scope.

Results: `mvn install -pl docx4j-digsig`: 133 tests, 0 failures, 1 skipped (the three fewer
are `template.potx`'s). The reference docx signed by the module is identical to the phase 0
baseline after normalising (the trial paragraph's absence changes only a digest value). The
`master` checkout on Santuario 2.0.6 validates it VALID. On the module path
(`java -p` with every dependency jar, `--add-modules org.docx4j.digsig`), the module resolves,
signs and validates VALID: no split package, the automatic modules resolve as named in
`module-info` (`org.apache.santuario.xmlsec`, `TextImageGen`), BouncyCastle's real modules
read. The module-path test of §4.8 is therefore this measurement plus the compiler, which
resolves the module graph when it compiles `module-info.java`; no subprocess test was added.
`docx4j-samples-digsig` compiles; the signing samples take `-Dpfx` and `-Dpfx.password`.

Found on the way: TextImageGen 1.9 depends on `tagsoup` (compile scope, Apache 2), which thus
comes with the module; noted for the licence list. `commons-codec` is on docx4j-core's
classpath but not in its `module-info`, hence the `Base64` swap.

Not done in phase 1: the Office check (phase 3); `docs/DigitalSignatures.md` (phase 3).

### Phase 2 (2026-10-08), the core change: commit d21a2ddd0 (with phase 3's documentation)

`org.docx4j.openpackaging.parts.PartProvider` in docx4j-core: `Part createPart(String
contentType, PartName)` returning null when not handled, with the providers resolved once by
`ServiceLoader` (TCCL first, then core's own loader, as `MetafileSvgProvider` does), and `uses`
in core's `module-info`. `ContentTypeManager` asks the providers for the two signature content
types and falls back to its generic XML and binary parts; the `Class.forName` branches are gone
(D3 said one release of fallback; there is no caller left to protect, since the only class
those names ever found is in this module, which provides). `LoadFromZipNG` and
`FlatOpcXmlImporter` lose the class-name string compare, which could never have worked for
the current `XmlSignaturePart` (it extends `XmlPart`, and the branch cast to `JaxbXmlPart`);
their `XmlPart` branch handles it. `Load3`'s commented-out line goes. No dependency added.

The module: `DigitalSignaturePartProvider` (`provides` in `module-info`, and
`META-INF/services`), and `PartProviderTest` (the provider is found; an Office-signed golden
loads with `XmlSignaturePart` and `SignatureOriginPart`). 135 tests pass.

Measured with a probe loading `word2016.docx`: on the classpath, one provider, the typed
parts; on the module path (`java -p`, `--add-modules org.docx4j.digsig`), one provider, the
typed parts, the service resolved through `uses`/`provides`; with docx4j-core alone, no
provider, `DefaultXmlPart` and `BinaryPart`, and the package loads.

Found on the way, for the docx4j session: core's `module-info` has no `uses
org.docx4j.model.images.MetafileSvgProvider`, so on the module path that lookup throws
`ServiceConfigurationError`, which its catch turns into the no-provider fallback. Not changed
here (not this CR's code); one line to add.

`docs/DigitalSignatures.md` written (phase 3's documentation item).

**Reviewed by the docx4j session (2026-10-08): approved**, with two things folded in (the
follow-up commit): the resolved provider list moved out of a nested `Holder` (implicitly
public in an interface, so API) into a package-private top-level `PartProviders`, so only the
interface and `providers()` are public; and `uses org.docx4j.model.images.MetafileSvgProvider`
added to core's `module-info` in the same change, the gap noted above. It confirmed the removed
loader branches could never have run. The branch is off the current `VERSION_17_3_2` HEAD, so
the merge is a fast-forward if taken before its next core commit.

### Phase 3 (2026-10-08), documentation done; the Office check awaits Jason

`docs/DigitalSignatures.md` and the module's `README.md` written; `CLAUDE.md`'s module map,
the CHANGELOG entry and the two `docx4j.properties` blocks were in phase 1. The samples module
compiles.

The Office-check set is in `docx4j-digsig/target/office-check-1/` (not committed; made with
`plutext-digsig-test.pfx`, which Office on the VM already trusts from CR-002):

| File | Made from | Our validator | master (2.0.6) |
|---|---|---|---|
| `docx4j-signed.docx` | `sign me please.docx` | VALID | VALID |
| `docx4j-signed.xlsx` | `corpus/comments.xlsx` | VALID | n/a (master's static validator is docx-only) |
| `docx4j-signed.pptx` | `corpus/table.pptx` | VALID | n/a |
| `docx4j-signed.xlsm` | `goldens/originals/macro.xlsm` | VALID | n/a |
| `docx4j-signed-strict.pptx` | `corpus/strict.pptx` | VALID | n/a |
| `docx4j-signed-sigline.xlsx` | `goldens/originals/sigline.xlsx`, Excel's line signed with a PNG | VALID | n/a |
| `docx4j-signed-xades-t.docx` | `sign me please.docx`, XAdES-T, TSA `http://timestamp.digicert.com` | VALID | VALID |

So XAdES-T works end to end against a public RFC 3161 server after the BouncyCastle upgrade
(`xd:SignatureTimeStamp` with an `EncapsulatedTimeStamp` is in the signature); that was the one
untested path of §4.5. The gate: Word, Excel and PowerPoint report each file's signature valid
and complete, the Excel line shows the signed image, and Word shows the XAdES-T one as valid
(Office labels any XAdES level "XAdES-EPES" in its details, per CR-002 §6.6).
