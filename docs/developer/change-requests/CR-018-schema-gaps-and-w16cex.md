# CR-018: Five schema gaps the content API found, and a w16cex schema for `commentsExtensible`

Status: PROPOSED (2026-09-16). Requested from `../docx4j-generated-objects-ts`
(2026-09-16): core-ts's content API found five gaps in `xsd/`, docx4j Java has the
same gaps, and the TypeScript objects package regenerates from `xsd/ROOT.xsd`
once this lands. Every claim below was re-checked against the current tree
(b5c2f417f) and the current XJC output (`docx4j-generated-objects/target/
generated-sources/xjc`, 2026-09-09), not the 2019 classes in `bin/`.
Owner: Jason Harrop. Drafted with Claude Fable 5.1.

Scope: `xsd/` only for items 1 to 4 (additive), a decision for item 5 (a Java
type change), and a new schema plus part class for `w16cex`. The generated
model changes shape, so `docx4j-generated-objects` is rebuilt and the
TypeScript objects package regenerates after it.

## Background

### The request (from the TypeScript objects repository, 2026-09-16)

> Please consider a schema CR ... for five small xsd/ changes. core-ts's
> content API found them, and docx4j Java has the same gaps. I checked
> bin/org/docx4j with javap, but those classes are from 2019, so please
> confirm against a current build. After it lands, the TypeScript objects
> package regenerates from xsd/ROOT.xsd.
>
> 1. mc:Ignorable on CT_Comments (xsd/wml/wml.xsd). Settings, footnotes,
>    endnotes and headers/footers carry `<xsd:attribute ref="mc:Ignorable"
>    use="optional"/>`, and so do w:styles and w16cid's CT_CommentsIds, but
>    CT_Comments doesn't. A re-marshalled word/comments.xml loses Word's
>    mc:Ignorable="w14 w15 ..." and keeps the w14:paraId attributes it
>    covers. Reproduced through the facade on 2026-09-16. This one changes
>    bytes Word sees. CT_CommentsEx and w15 CT_People lack it too, so worth
>    adding while you're there.
> 2. title on CT_NonVisualDrawingProps (xsd/dml/dml-documentProperties.xsd:
>    id, name, descr, hidden today). Later editions of ECMA-376 add
>    `<xsd:attribute name="title" type="xsd:string" use="optional"
>    default=""/>`. Without it wp:docPr/@title (Office JS
>    InlinePicture.altTextTitle) doesn't survive a round trip.
> 3. w15 CT_Person/@contact optional (xsd/wml/w15_word_2012_wordml.xsd line
>    89 has use="required"). Word omits it in every w15:people part seen, so
>    the required property in the model claims a value that isn't there.
> 4. xsd:anyAttribute on ds:datastoreItem
>    (xsd/customXmlProperties/shared-customxmlprops.xsd, the anonymous type
>    with schemaRefs and itemID). Anything Word adds there is dropped on
>    re-marshal. Lower priority.
> 5. The w14 checkbox's checked is w14:CT_OnOff, whose val is w14:ST_OnOff
>    (strings true, false, 0, 1), so every reader handles strings.
>    w:BooleanDefaultTrue accepts the same lexical values as a boolean.
>    Retyping w14:CT_SdtCheckbox/checked (or w14's CT_OnOff) would help, but
>    it changes a Java type (CTOnOff -> BooleanDefaultTrue), so it's your
>    call; skip it if the break isn't worth it.
>
> Separately, and bigger: a w16cex schema. w16cex:commentsExtensible (Word
> 2018: durable id and UTC date per comment, the anchor for comment
> reactions) has no schema in xsd/, so the part stays untyped in docx4j Java
> and in the TypeScript packages. The w16cid precedent: xsd/wml/w16cid.xsd
> (from the [MS-DOCX] schema, with its own jaxb:schemaBindings package
> annotation) plus an xsd:import in ROOT.xsd. A w16cex.xsd from [MS-DOCX]
> the same way, e.g. package org.docx4j.w16cex, would give both languages
> the types.

### Verification against the current tree (2026-09-16)

| # | claim | current `xsd/` | current XJC output | verdict |
|---|---|---|---|---|
| 1 | `CT_Comments` lacks `mc:Ignorable` | `wml.xsd`: the ref is on `CT_Settings` (12646), `CT_Footnotes` (13768), `CT_Endnotes` and the header/footer types (13788-13820), `CT_DocParts`, `CT_Captions`; **not** on `CT_Comments`. `w15_word_2012_wordml.xsd` has no `mc:Ignorable` at all (`CT_CommentsEx` line 52, `CT_People` line 70). `w16cid.xsd` line 41 has it. | `org.docx4j.wml.Comments` has no `ignorable` field; `CTSettings` has (line 963) | **confirmed**. The repository's own `docx4j-core-tests/src/test/resources/loadAndSave.docx` shows the shape: `w:comments mc:Ignorable="w14 w15 w16se w16cid w16 w16cex w16sdtdh w16sdtfl w16du wp14"` over paragraphs carrying `w14:paraId` |
| 2 | `CT_NonVisualDrawingProps` lacks `title` | `dml-documentProperties.xsd` 160-187: `id`, `name`, `descr`, `hidden`, nothing else | as the schema | **confirmed** |
| 3 | `CT_Person/@contact` is required | `w15_word_2012_wordml.xsd` line 89: `use="required"` | `CTPerson.getContact()` is generated as required | **confirmed** |
| 4 | `ds:datastoreItem` has no `xsd:anyAttribute` | `shared-customxmlprops.xsd` 44-70: `schemaRefs`, `itemID`, no `anyAttribute` | as the schema | **confirmed** |
| 5 | `w14:CT_SdtCheckbox/checked` is `w14:CT_OnOff` with a string `val` | `w14_word_2010_wordml.xsd` 49-59 (`ST_OnOff` = true/false/0/1 as strings), 632 | `org.docx4j.w14.CTSdtCheckbox.checked` is `CTOnOff` | **confirmed**; a type change |
| w16cex | no schema, part untyped | no `xsd/` file targets `.../2018/wordml/cex`; `NamespacePrefixMappings` knows the prefix (`w16cex`, lines 240 and 692) and `w16` (2018/wordml); `ContentTypeManager` has no case for it, so the part loads as a generic XML part | no package | **confirmed** |

The `w16cex` shape, from the repository's own `loadAndSave.docx` (Word 365, 2026):

```
[Content_Types].xml: <Override PartName="/word/commentsExtensible.xml"
    ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.commentsExtensible+xml"/>
document.xml.rels:   <Relationship Type="http://schemas.microsoft.com/office/2018/08/relationships/commentsExtensible"
    Target="commentsExtensible.xml"/>
<w16cex:commentsExtensible mc:Ignorable="w14 w15 w16se w16cid w16 w16cex w16sdtdh w16sdtfl cr w16du wp14">
  <w16cex:commentExtensible w16cex:durableId="05546856" w16cex:dateUtc="2026-05-18T23:21:00Z"/>
</w16cex:commentsExtensible>
```

The schema, from [MS-DOCX] section 5.6 (fetched 2026-09-16 from
https://learn.microsoft.com/en-us/openspecs/office_standards/ms-docx/0df7c115-b22a-4e09-bab7-4f24fbb8e6f5):

```xml
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:w12="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
    xmlns:w16="http://schemas.microsoft.com/office/word/2018/wordml"
    elementFormDefault="qualified" attributeFormDefault="qualified" blockDefault="#all"
    xmlns="http://schemas.microsoft.com/office/word/2018/wordml/cex"
    targetNamespace="http://schemas.microsoft.com/office/word/2018/wordml/cex">
  <xsd:import id="w16" namespace="http://schemas.microsoft.com/office/word/2018/wordml" schemaLocation="word16.xsd"/>
  <xsd:import id="w12" namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" schemaLocation="word12.xsd"/>
  <xsd:complexType name="CT_CommentsExtensible">
    <xsd:sequence>
      <xsd:element name="commentExtensible" type="CT_CommentExtensible" minOccurs="0" maxOccurs="unbounded"/>
      <xsd:element name="extLst" type="w16:CT_ExtensionList" minOccurs="0" maxOccurs="1"/>
    </xsd:sequence>
  </xsd:complexType>
  <xsd:complexType name="CT_CommentExtensible">
    <xsd:sequence>
      <xsd:element name="extLst" type="w16:CT_ExtensionList" minOccurs="0" maxOccurs="1"/>
    </xsd:sequence>
    <xsd:attribute name="durableId" type="w12:ST_LongHexNumber" use="required"/>
    <xsd:attribute name="dateUtc" type="w12:ST_DateTime" use="optional"/>
    <xsd:attribute name="intelligentPlaceholder" type="w12:ST_OnOff" use="optional"/>
  </xsd:complexType>
  <xsd:element name="commentsExtensible" type="CT_CommentsExtensible"/>
</xsd:schema>
```

Two things the precedent (`w16cid.xsd`, package `org.docx4j.w16cid`,
`CommentsIdsPart`) shows the schema needs beyond [MS-DOCX]'s text: the
`mc:Ignorable` attribute on the root type (Word writes it, above), and an
`annox` `@XmlRootElement` on the root type so the part's JAXB class is a root.
[MS-DOCX]'s schema imports `w16:CT_ExtensionList` from the 2018/wordml
namespace, which `xsd/` does not have either; the minimal-import precedent is
`xsd/mce/markup-compatibility-2006-MINIMAL.xsd`.

### How the docx4j model treats `mc:Ignorable` today

`XmlUtils.setMcIgnorable` (line 810: "Knows how to setMcIgnorable on
wml.Document only") and the `McIgnorableNamespaceDeclarator` prefix mapper
declare the namespaces the `mc:Ignorable` value names on marshal. A part whose
root type has no `ignorable` property cannot carry the value through, so the
re-marshalled `comments.xml` writes `w14:paraId` without declaring `w14` as
ignorable. Word 2007-2010 readers then see an unknown attribute in a
non-ignorable namespace; item 1's "changes bytes Word sees" is that.

## Gaps

1. **`mc:Ignorable` lost on three parts** (`w:comments`, `w15:commentsEx`,
   `w15:people`), with `w14:paraId`/`w14:textId` still present on the
   paragraphs they cover. Reproduced by the requester through the facade;
   `loadAndSave.docx` in this repository is a test vector.
2. **`wp:docPr/@title` dropped** on round trip: the attribute is in ECMA-376
   4th edition and later (Transitional), and Office JS writes it
   (`InlinePicture.altTextTitle`).
3. **`w15:person/@contact` modelled as required** where Word omits it; a
   generated model that claims a value that is not there misleads every
   consumer and every port.
4. **`ds:datastoreItem` drops unknown attributes** on re-marshal (no
   `anyAttribute`).
5. **`w14:checked/@val` is a string** in the model (`CTOnOff`, `getVal()`
   returning the `ST_OnOff` enum) where `w:BooleanDefaultTrue` gives readers
   a boolean with the same lexical space. A type change: `CTSdtCheckbox
   .getChecked()` would return `BooleanDefaultTrue`, and any code calling
   `getVal()` on the old type breaks.
6. **`w16cex:commentsExtensible` is untyped**: loads and saves as a generic
   XML part (bytes preserved), but nothing can read a comment's `durableId`
   or `dateUtc` through the model, in Java or in the ports, and the comment
   reactions part ([MS-OREACTXML], `cr:` in the `mc:Ignorable` above) anchors
   on it.

## Design

Items 1 to 4 are additive attribute changes to `xsd/` and change no Java
signature except by adding getters/setters:

- **Item 1:** `<xsd:attribute ref="mc:Ignorable" use="optional"/>` on
  `CT_Comments` (`wml.xsd`), on `CT_CommentsEx` and `CT_People`
  (`w15_word_2012_wordml.xsd`, which must then import the minimal `mce`
  schema as `w16cid.xsd` does). `XmlUtils.setMcIgnorable`'s "wml.Document
  only" note is checked: if the declarator needs the value set per part
  root, `CommentsPart`, `CommentsExtendedPart` and `PeoplePart` marshal the
  way `MainDocumentPart` does.
- **Item 2:** `<xsd:attribute name="title" type="xsd:string" use="optional"
  default=""/>` on `CT_NonVisualDrawingProps`, after `descr`, per ECMA-376
  4th edition Part 1 §20.1.2.2.8 (`title (Title)`).
- **Item 3:** `use="optional"` on `CT_Person/@contact`.
- **Item 4:** `<xsd:anyAttribute processContents="lax"/>` on
  `ds:datastoreItem`'s anonymous type (JAXB generates
  `getOtherAttributes()`).
- **Item 5 (decided by Jason 2026-09-16: change it):** w14's `CT_OnOff`
  takes the formulation wml's `BooleanDefaultTrue` uses (`wml.xsd` 326-358):
  `<xsd:attribute name="val" type="xsd:boolean" default="true"/>` in place
  of `type="ST_OnOff"`. The attribute stays `w14:val` (the w14 schema is
  `attributeFormDefault="qualified"`, so retyping the element to
  `w:CT_OnOff` would have moved it into the `w:` namespace, which is why
  the type is changed where it is rather than swapped); `xsd:boolean`'s
  lexical space is exactly w14 `ST_OnOff`'s four values. The class stays
  `org.docx4j.w14.CTOnOff`; its `val` becomes `Boolean` with `isVal()`
  (default true) and `setVal(Boolean)`, as `BooleanDefaultTrue` has. The
  same type serves `w14:conflictMode`, `w14:cntxtAlts` and
  `w14:discardImageEditingData`, which change with it. Callers in this
  repository: `BindingTraverserCommonImpl` line 588 (`setVal("1"/"0")`
  becomes `setVal(true/false)`) and `TextBindParityTest` line 67. JAXB
  marshals `true`/`false` where Word writes `1`/`0`; both are legal, and
  the round-trip test asserts Word's reading, not the bytes. The
  unused `w14:ST_OnOff` simple type stays for the `noSpellErr`-style
  attributes that still name it.
- **w16cex:** `xsd/wml/w16cex.xsd` from the [MS-DOCX] text above, with the
  `w16cid.xsd` additions (jaxb `schemaBindings` package `org.docx4j.w16cex`,
  `annox` `@XmlRootElement(name="commentsExtensible")` on
  `CT_CommentsExtensible`, `mc:Ignorable` on it, the `mce` minimal import);
  a minimal `xsd/wml/w16.xsd` for the 2018/wordml namespace carrying
  `CT_ExtensionList`/`CT_Extension` only (package `org.docx4j.w16`), from
  [MS-DOCX]'s 2018/wordml schema, with a header saying it is minimal and
  why; `xsd:import` of both in `ROOT.xsd` beside `w16cid`; `exports` and
  `opens` in `docx4j-generated-objects/module-info.java`; the packages added
  to the context path in `org.docx4j.jaxb.Context` (line 181) and the
  pptx4j context if it lists w16cid; `ContentTypes.WORDPROCESSINGML_COMMENTS_EXTENSIBLE`,
  `Namespaces.COMMENTS_EXTENSIBLE`, a `CommentsExtensiblePart extends
  JaxbXmlPart<org.docx4j.w16cex.CTCommentsExtensible>` on the
  `CommentsIdsPart` pattern (default part name `/word/commentsExtensible.xml`),
  and the `ContentTypeManager` case that creates it.

## Plan

1. **Items 1 to 4** in `xsd/`, `docx4j-generated-objects` rebuilt (`mvn clean
   install -pl docx4j-generated-objects -am`; the XJC plugin only checks
   `ROOT.xsd` for staleness, so `clean`), the `mce` import for w15, and a
   round-trip test per item in `docx4j-core-tests` (load `loadAndSave.docx`,
   save, assert `mc:Ignorable` present on `comments.xml`; a `docPr` with a
   `title`; a `w15:person` without `contact`; a `datastoreItem` with a foreign
   attribute). `LoadAndSaveTests` and the whole of `docx4j-core-tests` green.
2. **w16cex**: the two schemas, the import, the module exports, the context
   path, the part class and its content-type case; a test that
   `loadAndSave.docx`'s part loads as `CommentsExtensiblePart` with one
   `commentExtensible` carrying `durableId` 05546856 and the `dateUtc`, and
   that it round-trips byte-equivalent. `docx4j-core-tests` green; the
   `docx4j-bundle` and OSGi manifests checked for the new packages
   (`docx4j-generated-objects`'s `Export-Package` if the OSGi profile lists
   packages by name).
3. **Item 5**: the `xsd:boolean` retype, the two callers, and a test that
   the four lexical values (`true`, `false`, `1`, `0`) unmarshal to the right
   boolean on `w14:checked` and that a checkbox bound through
   `BindingTraverserCommonImpl` round-trips.
4. CHANGELOG (17.1.1: a "Schema" heading), the CR marked DONE with the
   commit hashes, the registry updated, and a message back to the TypeScript
   objects repository with the CR number and commits so it regenerates.

Each phase is its own commit. The generated-objects rebuild is the one
expensive step and runs once per phase; nothing here touches the fidelity
corpus (no FO change), so the gate is the test modules, not the corpora.

## Decisions (for Jason)

1. **Item 5, the checkbox type.** Recommended keeping `CTOnOff` with a
   boolean convenience; **Jason decided 2026-09-16: change it.** Done as in
   the Design: `val` becomes `xsd:boolean` on w14's own `CT_OnOff`, the class
   name stays, `getVal()`/`setVal(String)` become `isVal()`/`setVal(Boolean)`.
   An API change for 17.1.1, named in the CHANGELOG.
2. **The minimal `w16.xsd`** (only `CT_ExtensionList`/`CT_Extension`) rather
   than the whole 2018/wordml schema. Recommended: the whole schema brings
   types nothing in `xsd/` references, and the precedent for a minimal
   import exists (`mce`).
3. **Package name `org.docx4j.w16cex`**, as requested, matching `w16cid`.

## Risks

- **Regenerating changes many files' shape** for every consumer of
  `docx4j-generated-objects` (the XJC output is not checked in, so the
  visible change is the xsd diff plus the new part class; the MOXy and RI
  selector modules need no change unless a new package must be listed).
- **`mc:Ignorable` on `w:comments` changes bytes Word sees**, in the
  direction of what Word itself writes; the round-trip test on
  `loadAndSave.docx` is the guard, and Word on the VM should open the saved
  file once (the resave harness exists).
- **`w15` importing `mce`**: the import must not disturb XJC's episode /
  package bindings for `w15` (`org.docx4j.w15`); `w16cid.xsd` already
  imports it, so the pattern is known to work.
- **OSGi `Export-Package`** lists may need the two new packages; the
  `docx4j-bundle` build (outside the reactor) is the check.
