CHANGELOG
=========


Version 8.2.9  namespaces required in documents created using recent Word updates
==============

Release date
------------

15 April 2021


Contributors to this release
----------------------------

Dingfu.Ye (jdkcn)
Jason Harrop


Changes in Version 8.2.9 
-------------------------

http://schemas.microsoft.com/office/word/2020/wordml/sdtdatahash is new in [MS-DOCX] v15.0 of 22 March. Word writes this namespace prefix to @mc:Ignorable in the MDP, for example, "w14 w15 w16se w16cid w16 w16cex w16sdtdh wp14", so Word requires the namespace to also be declared.



Version 8.2.8   minor release
==============

Release date
------------

5 January 2021

Contributors to this release
----------------------------

Jason Harrop

Changes in Version 8.2.8
-------------------------

Microsoft Word: Dedicated part for template command group (wne:tcg), which contains customizations for keyboard shortcuts and toolbars



Version 8.2.7  for pptx4j users
==============

Release date
------------

14 December 2020

Contributors to this release
----------------------------

Jason Harrop

Guillermo

Yavuz Tas


Changes in Version 8.2.7
-------------------------

Zip loading optimisation: Avoid using streams where entry size is known

Fix issue in unmarshalString(String str, JAXBContext jc, Class declaredType) introduced by XXE fixes in 8.2.1 mostly affecting pptx

Emojis: specify fallback font in docx4j.properties property docx4j.fonts.RunFontSelector.EmojiFont 



Version 8.2.6  minor release
==============

Release date
------------

7 December 2020

Contributors to this release
----------------------------

Jason Harrop

Changes in Version 8.2.6
-------------------------

PDF via FOP with docx embedded fonts: bug fix for case where temp extracted fonts were deleted too early.

XHTML to docx: CSS font-size bug fix

OpenDoPE image injection: tag width=n|auto to specify max width (eg 10500) or let BPAI work out the scaling (ported from 11.1.8)




Version 11.2.5  
==============

Release date
------------

18 Nov 2020

Changes in Version 11.2.5
-------------------------

Cumulative update for parity 8.2.4

Note: this release is labelled 11.2.5, since 11.x
has since June 2020 contained a feature OpenDoPE image injection: tag width=n|auto 
which isn't in 8.2.4 (but has now been ported and will be in an 8.2.5 or 8.2.6)

Limitations:
- The docx4j-documents4j-* sub-projects don't have a module-info.java until https://github.com/documents4j/documents4j/issues/108 is addressed
- The docx4j-conversion-via-microsoft-graph sub-project doesn't have a module-info.java



Version 8.2.4  minor release
==============

Release date
------------

13 November 2020

Contributors to this release
----------------------------

Jason Harrop

Significant Changes in Version 8.2.4
-------------------------

docx4j toc generation: p-level rPr rFont or sz should take priority over doc default setting

xlsx4j: Worksheet retain  xr:uid, so MOXy writes the xr namespace, 
works around https://stackoverflow.com/questions/64778237/moxy-missing-namespace-for-content-in-xmlanyelementlax-true-object



Version 8.2.3  
==============

Release date
------------

4 September 2020

Contributors to this release
----------------------------

Jason Harrop


Significant Changes in Version 8.2.3
-------------------------

Support for docx/pptx/slsx conversion to PDF via Microsoft's cloud (Microsoft Graph)
 


Version 8.2.2  minor release
==============

Release date
------------

25 August 2020


Contributors to this release
----------------------------

Jason Harrop


Significant Changes in Version 8.2.2
-------------------------

FieldUpdater support for docvars (docprops are already supported)

Update OSGI stuff for current release. Confirmed working in karaf 4.2.6 with docx4j-JAXB-Internal on Java 8.



Version 8.2.1  
==============

Release date
------------

16 July 2020


Contributors to this release
----------------------------

Jason Harrop


Significant Changes in Version 8.2.1
-------------------------

Traverse into 2010/wordprocessingShape txbxContent
XXE avoid StreamSource in various places
OpenDoPE XHTML support: where Import XHTML is not on classpath, insert an altChunk (ie rely on Word to process the XHTML)


Version 11.1.8  
==============

Release date
------------

10 June 2020

Changes in Version 11.1.8 
-------------------------

OpenDoPE image injection: tag width=n|auto to specify max width (eg 10500) or let BPAI work out the scaling


Version 8.2.0  
==============

Release date
------------

2 June 2020

Contributors to this release
----------------------------

Jason Harrop

Significant Changes in Version 8.2.0
-------------------------

New sub-projects to facilitate the use of documents4j
to drive Microsoft Word (running locally or remotely),
for PDF export, TOC update, and import of RTF, binary .doc
and HTML.

For examples, see docx4j-samples-documents4j-local 
(or -remote).


Version 8.1.7  minor release (to align with 11.1.7)
==============

Release date
------------

25 May 2020


Contributors to this release
----------------------------

Jason Harrop

Significant Changes in Version 8.1.7 
-------------------------

OpenDoPE: fix handling of count>0 when OpenDoPEHandler.ENABLE_XPATH_CACHE

docx4j-export-fo
------------

Move to FOP 2.4

Support VertAlign direct rPr formatting 



Version 8.1.6  minor release
==============

Release date
------------

7 April 2020


Contributors to this release
----------------------------

Jason Harrop

Changes in Version 8.1.6 
-------------------------

Improve efficiency of package clone

pptx4j
------

SaveSlides provides the ability to save selected slides only; partialClone can use this to make a clone of a PresentationMLPackage, but with just those slides

xlsx4j
------

xlsx revision namespaces; fixes #389

Support importing flat OPC XML representing a spreadsheet



Version 8.1.5  namespaces required in documents created using recent Word updates
==============

Release date
------------

26 February 2020


Contributors to this release
----------------------------

Jason Harrop

R Cambell

Wussup

Changes in Version 8.1.5 
-------------------------

Add mappings for w16 and w16cid.  Fixes 381.

JAXB: Remove dep on java-api 2.3.1, since that depends on javax.activation:javax.activation-api.  The 2.3.2 version which we want is jakarta.xml.bind:jakarta.xml.bind-api which depends on jakarta.activation

NamespacePrefixMapperUtils.tryRIforRelationshipsPart: Fix typo

MergeField: support multiple newline characters

For more, please see https://github.com/plutext/docx4j/commits/master

 

Version 8.1.4 minor release
==============

Release date
------------

23 December 2019


Contributors to this release
----------------------------

Jason Harrop


Changes in Version 8.1.4 
-------------------------

Dedicated part CommentsIdsPart for w16cid:commentsIds

Enhance ArrayListWml to handle parent in other methods; make JAXBElements transparent when matching objects 

export-FO: revert white-space-treatment="preserve"; white-space-collapse="false" because it causes unwanted formatting issues

xlsx4j
------

Enhance ArrayListSml to handle parent in other methods; make JAXBElements transparent when matching objects 



Version 8.1.3 minor release
==============

Release date
------------

29 August 2019


Contributors to this release
----------------------------

Jason Harrop


Changes in Version 8.1.3 
-------------------------

Port changes in 11.1.3 (as to which see below) back to 8.1.x 



Version 11.1.3 minor release
==============

Release date
------------

28 August 2019

Overview 
--------

11.1.3 is a JPMS modularised release.  


Contributors to this release
----------------------------

Jason Harrop


Changes in Version 11.1.3 
-------------------------

improved diagnostics for XML factories

handle newline chars in createParagraphOfText

OpenDoPE: access resources in org.docx4j.model.datastorage (Widen opens in module-info)

FieldUpdater patch (#343)

pptx4j
------

Improve API for adding slide notes

Bugfix: HandoutMasterPart part name must start with '/' 



Version 11.1.2 
==============

Release date
------------

5 August 2019

Overview 
--------

11.1.2 is a JPMS modularised release.  It incorporates changes made in 8.1.1 and 8.1.2 (ie changes since 11.1.0 was released)


Contributors to this release
----------------------------

Jason Harrop

ChilamYan


Notable Changes in Version 11.1.2 
---------------------------------

Widen opens in pptx4j/xlsx4j module-info

Bugfix: synchronize access to namespacePrefixMapper to avoid "namespacePrefixMapper is null" in multithreaded scenarios

Bugfix: specify UTF-8 in MainDocumentPartFilterOutputStream

Support for embedded True Type Fonts. (Up until now, we only supported obfuscated embeddings)

xlsx4j
------

Bugfix: Correct jcSML spreadsheetDrawing package name

Support mc:alternateContent in Workbook part (ie instead of dropping it)



Version 8.1.2 minor release 
=============

Release date
------------

31 July 2019


Contributors to this release
----------------------------
	
Jason Harrop


Notable Changes in Version 8.1.2 
---------------------------------

Bugfix: synchronize access to namespacePrefixMapper to avoid "namespacePrefixMapper is null" in multithreaded scenarios



Version 8.1.1 minor release 
=============

Release date
------------

29 June 2019


Contributors to this release
----------------------------

ChilamYan 
	
Jason Harrop


Notable Changes in Version 8.1.1 
---------------------------------

Bugfix: specify UTF-8 in MainDocumentPartFilterOutputStream

Support for embedded True Type Fonts. (Up until now, we only supported obfuscated embeddings)


xlsx4j
------

Bugfix: Correct jcSML spreadsheetDrawing package name

Support mc:alternateContent in Workbook part (ie instead of dropping it)



Version 11.1.0 
==============

Release date
------------

2 June 2019


Contributors to this release
----------------------------

Jason Harrop


Notable Changes in Version 11.1.0 
---------------------------------

11.1.0 is our first JPMS modularised release, so the jars contain module-info.class entries.

The module names correspond to our Maven modules: 

	org.docx4j.openxml_objects 
	org.docx4j.openxml_objects_pml 
	org.docx4j.openxml_objects_sml 
	
	org.docx4j.core 
	
	org.docx4j.JAXB_MOXy 
	org.docx4j.JAXB_ReferenceImpl 

We require named modules:

    docx4j_xalan_serializer
    docx4j_xalan_interpretive
	org.slf4j
	
(Other dependencies generally aren't JPMS-ready, so they go on the classpath in the "unnamed" module.)	

11.1.0 is compiled with Java 12, targeting Java 11.

jackson-databind dep bumped to 2.9.9.

In other respects this release is the same as v8.1.0, except that docx4j-export-fo 
is temporarily omitted since Maven compiler doesn't like FOP 2.3's avalon dependency.  
When FOP 2.4 is released, we should be able to include it again.



Version 8.1.0 
=============

Release date
------------

21 May 2019


Contributors to this release
----------------------------

Jason Harrop


Notable Changes in Version 8.1.0 
---------------------------------

Update [MS-PPTX] and [MS-ODRAWXML] content models with latest schemas.

Implement child in dml

Re-packaged subset of Guava to shave 2.4MB off deps size 

StyleTree handles default table style (used by MergeDocx)

docx4j
------

Support adding altChunk at specified index

VariablePrepare: make settings configurable

Move anon out of core to a dedicated Maven module

pptx4j
------

support mc:AlternateContent in slide shape tree

Implement child in pml



Version 8.0.0 
=============

Release date
------------

21 April 2019


Contributors to this release
----------------------------

Jason Harrop


Notable Changes in Version 8.0.0 
---------------------------------

Minimum supported Java version is 8.0

docx4j 8 should be easier to deploy in complex environments such as OSGi, JBoss EAP etc.

docx4j is now a Maven multi-module project.  To use it via Maven, add just one of docx4j-JAXB-Internal, docx4j-JAXB-MOXy,
or docx4j-JAXB-ReferenceImpl dependencies. 

Various dependencies were updated (some of which require Java 1.8)

OpenDoPE support for XPath 2.0, 3.0 (requires Saxon)

Uses repackaged and mavenized Xalan; see https://github.com/plutext/xalan-j/tree/Plutext_Java8_Repackaged_Docx4j



Version 6.1.2 minor release 
=============

Release date
------------

27 February 2019


Contributors to this release
----------------------------

Jason Harrop

rmanitz


Notable Changes in Version 6.1.2 
---------------------------------

Fix for #340 Field handling NumberExtractor, handle group separator '.' (eg German) correctly.

New property docx4j.Fields.Numbers.JavaStylePercentHandling which if set to true multiplies the value by hundred.  
For example, formatting 0.33 with "\\# ##%" would yield 33%.

Fix for #344 (NPE in TraversalUtil in graphicData)
Fix for #346 (relating to Fields)

xlsx4j
------

Convenience method to access ThemePart


Version 6.1.1 
=============

Release date
------------

21 January 2019


Contributors to this release
----------------------------

Jason Harrop


Notable Changes in Version 6.1.1 
---------------------------------

vml and mc lists are now of type ArrayListVml|Mce respecitvely, in which parent object is automatically set correctly
(as is done for wml objects)

TraversalUtil replaceChildren: support replacing in a bare List object.

OpenDoPE removal handler: new option ALL_BUT_PLACEHOLDERS_CONTENT, 
Ordinarily, if an SDT contains XML but no real content, that XML is also removed.  
Choose this option if you want to keep placeholder XML (but remove the SDT).



Version 6.1.0 
=============

Release date
------------

15 December 2018

Contributors to this release
----------------------------

https://github.com/adolbin

Jason Harrop

https://github.com/Lood

Mioara

https://github.com/P2EQgf

https://github.com/verkhovin



Notable Changes in Version 6.1.0 
---------------------------------

IMPORTANT: Force namespaces used in mc:choice to be declared top-level. If 
they aren't there, Word can't open the docx.

mc:Ignorable: support attribute in footnotes/endnotes, numbering, styles 
(already supported in main and headers/footers) 

w14 namespace handling improvements: Don't drop these attributes in mc-preprocessor.xslt; 
Support w14:EG_RPrOpenType elements

PDF Converter now defaults to localhost endpoint to avoid information leakage.
(controlled by docx4j property com.plutext.converter.URL)
The localhost endpoint will only work if you download and install the converter there! 
If you choose to use converter-eval.plutext.com, please only use it for light testing purposes.

BookmarksIntegrity checking now part of docx4j proper; used before ToC updating

AttachTemplate now part of docx4j proper; method cloneAs

OpenDoPE finisher: optional step in which formatting can be applied
(this is often neater than using conditional content controls just for formatting)

OpenDoPE RemovalHandler: new Quantifier.ALL_BUT_PLACEHOLDERS (ie remove content controls, 
but keep any placeholders inserted where there is empty content), ability to specify 
Quantifier via docx4j.properties; also ability to substitute your own RemovalHandler.xslt.

OpenDoPE: simplified component processing model [requires Enterprise]:
 1. components don't have to be at the top paragraph level of the content tree,
 BUT:
 2. component processing is now done before condition/repeat processing
 3. component processing is not recursive anymore
 4. components typically use the "main" answer file
component processing is OFF by default
Enable it with property "docx4j.model.datastorage.OpenDoPEHandlerComponents.enabled"


Version 6.0.1 
=============

Release date
------------

3 August 2018

Contributors to this release
----------------------------

Jason Harrop

Notable Changes in Version 6.0.1 
---------------------------------

In many cases v6.0.0 release writes <?xml version="1.0" ?> omitting encoding "UTF-8".
This release corrects that, and is recommended for this reason.


Version 6.0.0 (NOT RECOMMENDED)
=============

Release date
------------

22 July 2018

Contributors to this release
----------------------------

ewanmellor (NationalBI)

Jason Harrop


Notable Changes in Version 6.0.0 
---------------------------------

Docx4j 6.x will be the last series supporting Java 6.  
(docx4j 7.x, when released, will require Java 7+) 

New dependency commons-compress used for zip/unzip.  Most other
deps updated to last version supporting Java 6.

Support for Java 9 and 10 (see notes in README for use in Eclipse)
- Maven profiles for java 9 and 10 
- Maven pom now specifies source & target 1.6 (previously 1.5)

Preserve mc:AlternateContent in a run. Previously, this would have caused mc-preprocessor 
to be invoked, selecting one of the pieces of content.

Object model for wps http://schemas.microsoft.com/office/word/2010/wordprocessingShape

Content Control databinding (and OpenDoPE)
- UpdateXmlFromDocumentSurface: Copy content control content back to the custom XML part, like Microsoft Word does for a content control
 which has a w:databinding element
- improvements to migration (from eg VariableReplace)
- better support for binding standardised Answers format
- various other improvement

Write docx4j version as XML comment in MainDocument part.

New properties docx4j.openpackaging.parts.MAX_BYTES.unzip.error and docx4j.openpackaging.parts.MAX_BYTES.unmarshal.error which can be 
set to positive values to have docx4j throw a PartTooLarge exception 
if a part is larger than the set value. 



Version 3.3.7  minor release
=============

Release date
------------

25 March 2018

Contributors to this release
----------------------------

Jason Harrop


Notable Changes in Version 3.3.7 
---------------------------------

OpenDoPE: handle w15:databinding; FlatOPC processing fixes; component handling fixes

PDF Converter (commercial, not export-FO) defaults to https (port 443)

IBM JAXB detection improvements

various other fixes



Version 3.3.6  minor release
=============

Release date
------------

7 October 2017

Contributors to this release
----------------------------

ai-github

Exandra

Jason Harrop

whisper2shade


Notable Changes in Version 3.3.6 
---------------------------------

@XmlRootElement on webextension and taskpanes

OpenDoPE performance enhancements:  pre-calculate repeat counts and simple conditions for substantial time saving in ODH step

JAXB representation of c14 (http://schemas.microsoft.com/office/drawing/2007/8/2/chart) from odrawxml/chart20070802.xsd

OpenDoPE: bind date sdt

JaxbXmlPart new method transform(Templates xslt, Map<String, Object> transformParameters, Result result)

xlsx4j
------

Chartsheetpart



Version 3.3.5  minor release
=============

Release date
------------

30 June 2017


Notable Changes in Version 3.3.5 
---------------------------------

Support w16cid (commentsIds); see https://msdn.microsoft.com/en-us/library/mt791826(v=office.12).aspx
(without this, document created in Word 2016 with recent updates, then saved by docx4j, couldn't be re-opened in Word) 

Support w15symex (long hex replacement for symbol char); see https://msdn.microsoft.com/en-us/library/mt459331(v=office.12).aspx

IBM Unmarshaller (usually used in WebSphere v7+) workaround for "errors limit exceeded" (same as Oracle JAXB)



Version 3.3.4  minor release
=============

Release date
------------

15 June 2017

Contributors to this release
----------------------------

Danilo Zagatto

Greg Kinman

Jason Harrop


Notable Changes in Version 3.3.4 
---------------------------------

BinaryPartAbstractImage: ImageMagick executable name fetched from Docx4jProperties.getProperty("org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage.ImageMagickExecutable", "imconvert")
and workaround for file URL issue on Windows

Parent handling: convenience for user SdtPr set parent (same for SdtContent, now an interface); 

mc-preprocessor.xslt: more Google Docs fixes

samples/BookmarksDuplicateCheck: Check also the ends; option to remediate (ie remove broken ones)

OpenDoPE fixes/enhancements:
#234  convert #STRING to NodeList
#235  complex XPaths
count-type conditions in repeats: perform enhancement in non-oda case

other bugfixes:
#175 HTML output: conversion to nested <ul> or <ol>
#164 Reset error counter, this time for IBM's JAXB (in Websphere inc 8.5.5.11)

xlsx4j
------

fix to support Apple iCloud Numbers (set reference attribute on cell)

loading .pptx and .xlsx files from InputStream


Version 3.3.3  minor release
=============

Release date
------------

7 February 2017

Contributors to this release
----------------------------

Ashton Batty

Jason Harrop

Notable Changes in Version 3.3.3 
---------------------------------

Minor modifications to support use on Android; see https://github.com/plutext/AndroidDocxToHtml

Several bugfixes, including:
- Handle non-English speaking locales in XmlPart.cachedXPathGetString (used in OpenDoPE content control data binding)
- Regression in xpathtracker histgram namespace handling (issue #230)
 

xlsx4j
------

Convenience method for cell to access its WorksheetPart; WorksheetPart to get WorkbookPart.

Cell content extraction - formatted (using modified Apache POI code)



Version 3.3.2  minor release
=============

Release date
------------

20 December 2016


Contributors to this release
----------------------------

Jason Harrop


Notable Changes in Version 3.3.2 
---------------------------------

JAXBXmlPart pipe methods, which enable you to process using StAX or SAX
(avoiding JAXB overhead);  see VariableReplaceStAX for an example

JAXB content model for web extensions

Various bugfixes, including:
- fix for OpenDoPE complex conditions (broken by 3.3.1)
- fix for IllegalArgumentException: local part cannot be "null" when creating a QName


Version 3.3.1  minor release
=============

Release date
------------

16 August 2016


Contributors to this release
----------------------------

https://github.com/ai-github

Jason Harrop


Notable Changes in Version 3.3.1
---------------------------------

Contents lists are now of type ArrayListWml, in which parent is automatically set correctly

org.docx4j.anon: new package which converts a docx to lorem ipsum latin, and removes
other sensitive info, so the docx can safely be shared eg for tech support.  
For this to work, you'll need to add a new dependency: http://search.maven.org/#artifactdetails%7Ccom.thedeanda%7Clorem%7C2.0%7Cjar
 
OpenDoPE: Two separate performance optimizations which substantially speed up large XML binding jobs.  By traversing the XML data, manually calculate and cache XPaths (tested with 500,000 entries).  Our strategy is to try the cache first (if enabled), then if there is a cache miss, use org.apache.xpath.CachedXPathAPI (the second optimization - CachedXPathAPI  is quicker than default javax.xml.xpath.XPath implementations)

Dedicated parts for w15 CommentsExtended and People

Support anchor attributes wp14:anchorId and wp14:editId; children wp14:sizeRelH, wp14:sizeRelV

Support w:bdo and w:dir from ECMA 376 4ed

workaround for MOXy issue triggering pre-processing from binder: http://stackoverflow.com/questions/37225221/moxy-validationevents-triggered-by-unmarshaller-but-not-binder

JAXB event handler: make shouldContinue false by default, mc Content pre-processing works in MOXy (for unmarshalling anyway, as distinct from binder)

unmarshall input stream: do mc pre-processing if necessary

New property docx4j.jaxb.marshal.canonicalize:
Whether to canonicalize during marshalling (defaults to false);
this is for aesthetic purposes: the Sun/Oracle JAXB reference implementation
writes a lot of unnecessary namespaces (each one in the JAXB context?).


Dependency Changes
------------------

Added com.thedeanda:lorem:jar:2.0; only required if you want to use org.docx4j.anon (see above)
 


Version 3.3.0
=============

Release date
------------

21 April 2016


Contributors to this release
----------------------------

https://github.com/crherman7

https://github.com/increatum

https://github.com/jerryorr

https://github.com/MaciejDobrowolski

https://github.com/pwarncke

https://github.com/rasmusfaber

https://github.com/tfcporciuncula

Jason Harrop

Notable Changes - PDF
---------------------

XSL FO based PDF output moved to new/separate project docx4j-export-fo; default PDF converter 
changed from XSL FO + Apache FOP to Plutext's PDF Converter. The instance at
converter-eval.plutext.com is used by default, but you can (and should!) alter it to your own
instance, by setting docx4j property, eg:

com.plutext.converter.URL=http://converter-eval.plutext.com:80/v1/00000000-0000-0000-0000-000000000000/convert

See further http://www.plutext.com/m/index.php/products-docx-to-pdf.html

If you want to use the existing XSL FO + Apache FOP PDF Conversion, just add docx4j-export-fo (+ deps) to your classpath.  These jars are in the zip file, in dir optional/export-fo  docx4j will detect that they are present, and revert to the FO based conversion.

Notable Changes - Other
-----------------------

Document protection (read only, track revisions etc) for docx/pptx/xlsx, with or without password. 
NB: Digital signature support is in Enterprise Ed. 
See further https://github.com/plutext/docx4j/blob/VERSION_3_3_0/src/main/java/org/docx4j/openpackaging/packages/ProtectionSettings.java and ProtectDocument.java

docx table of contents (TOC) generation and update migrated from Enterpise Ed.  See org.docx4j.toc and the Toc* samples. The layout model in Plutext's PDF Converter (see above) will be used to calculate page numbers, unless docx4j-export-fo (+ deps) is on your path

Workaround for JAXB behaviour change (for recent releases of JAXB, mcPreprocessor only worked the first 10 times);
see/subscribe to https://github.com/gf-metro/jaxb/issues/22

Xalan <= 2.7.2 can't handle astral characters: see/vote for https://issues.apache.org/jira/browse/XALANJ-2419
So:
(i)  workaround this when serializing XML
(ii) optionally workaround this when we use Xalan for XSLT, controlled by docx4j.properties docx4j.xalan.XALANJ-2419.workaround=true|false
[Comment: When will Xalan make a release which fixes this issue? This code will be removed if they do.]

RunFontSelector: basic support for Unicode astral characters

Performance improvement: property docx4j.openpackaging.parts.JaxbXmlPartXPathAware.binder.eager.* allows us to avoid unmarshalling via DOM

Handle McIgnorableNamespaces within NamespacePrefixMapper classes

XJC generated code for word/2010/wordprocessingDrawing

Repackage portions of org.apache.poi (mainly poifs) as org.docx4j.org.apache.poi
(and remove dependency on poi-scratchpad; remove Doc import since poi.hwpf dep is now absent)

Repackage santuario c14n (org.apache.xml.security) as org.docx4j.org.apache.xml.security. 
[Comment: Useful for c14n, but not essential to docx4j, so may yet be removed. Opinions welcome]

Avoid adding virtual styles to docx representing DocDefaults; this is now internal to StyleTree



Dependency Changes (relative to 3.2.x)
------------------

Removed: jaxb-xmldsig-core, poi-scratchpad, commons-lang

Added: 

		<dependency>
			<groupId>org.apache.commons</groupId>
			<artifactId>commons-lang3</artifactId>
			<version>3.4</version>
		</dependency>

Moved FOP/batik jars to separate project docx4j-export-fo

Bumped most deps to most recent available (except MOXy, where bumped to most recent compiled for Java 6)



Version 3.2.2  minor release
=============

Release date
------------

4 Sept 2015


Contributors to this release
----------------------------

https://github.com/sgrachov

Jason Harrop


Notable Changes in Version 3.2.2
---------------------------------

guard against XML Enternal Entity Injection attacks for some additional cases (the main cases were addressed in v3.2.0)

tune down certain logging

temp embedded font handling improvements (sgrachov, jharrop)

PDF (FO via XSL): insert blank page to honour sectPr/w:type oddPage|evenPage set on the following section

databinding/OpenDoPE: 

- support w14 checkbox, w15:repeatingSection

- property docx4j.model.datastorage.BindingHandler.Implementation (which defaults to BindingTraverserXSLT)
 
- ODH minor performance optimization

(X)HTML output: use ul|ol list items, if feature PP_HTML_COLLECT_LISTS is set and SdtToListSdtTagHandler is registered.

xlsx4j
------

Variable replacement in xlsx4j

pptx4j
------

Ensure VML namespace is declared on key parts



Version 3.2.1  minor release
=============

Release date
------------

3 October 2014


Contributors to this release
----------------------------

https://github.com/baudinseb/docx4j

Jason Harrop


Notable Changes in Version 3.2.1
---------------------------------

MailMergerWithNext (baudinseb): supports NEXT, useful for mailing labels

OpenDoPE: bookmark ID management

pkg Load classes: getSourceRelationships().add(r) for additional source rel 

Improvements to diffx (still not production ready though)

PresentationML/FontDataPart

Expose Base.partName, so MergeDocx v1.6.6 can use this docx4j





Version 3.2.0 
=============


Release date
------------

26 August 2014


Contributors to this release
----------------------------

BobFleischman
apixandru
Jason Harrop
Sven Jacobi 


Dependency changes
------------------

31mb1c1646 Add new dependency com.google.guava

Minimum Java version is Java 6 (since guava and ambassador are compiled for that)


Notable Changes in Version 3.2.0
---------------------------------

d150d9c7f6 *Security fix*  Configure DocumentBuilderFactory to disallow doctype declaration etc. (reported by Sven Jacobi)

FO/PDF output: miscellaneous improvements, including:

   header/footer height calculation
   Support for table row w:cantsplit property   
   ParagraphStylesInTableFix enhancements
   support FOP config font substitutions
   formatting of list item label   
   generally use per class logging, since this makes it easier to see where the message is generated 
   Add GlyphCheck; improve support for Arial Unicode MS 
   For Wingding etc symbols, use hAnsi font ; https://github.com/plutext/docx4j/issues/118 
   PDF events
   PDF (non XLST): support nested tables
   Workaround for missing space before fo:page-number-citation-last in FOP 1.1 output
   FO support for ptab align right 
   Create a suitable ConversionSectionWrapper, when continuous sectPr encountered, by using header/footer details from the previous sectPr
   Where appropriate (ie different page size), insert a page break
   Support css line-height 
   In HTML|FO\PDF output, ensure tblPr is not null - Try to behave gracefully if broken style is encountered (ie missing @w:type or @w:styleId) 
   w:br in FO output: Handling of vertical space 

31m0c0e45d *API change*  getContents now throws Docx4JException, instead of returning null in the case of error. 


New docx4j.properties
---------------------

	# PDF output; ability to specify font substitutions.  See src/samples/_resources
	# Avoid using both this and fontMapper.getFontMappings() for the same fonts!
	#docx4j.fonts.fop.util.FopConfigUtil.substitutions=fop-substitutions.xml
	
	# Defaults to org/docx4j/fonts/microsoft/MicrosoftFonts.xml
	# which is contained in the docx4j jar
	# If you need to override it in order to provide different file names for
	# one or more fonts, start by extracting and editing a copy of the existing file
	docx4j.fonts.microsoft.MicrosoftFonts=org/docx4j/fonts/microsoft/MicrosoftFonts.xml
	
	# In XHTML import, span/@style='background-color:red;' would usually become w:rPr/w:shd/@w:fill="ff0000"
	# Set this to true to use w:highlight instead 
	#docx4j.model.properties.PropertyFactory.createPropertyFromCssName.background-color.useHighlightInRPr=false
		
	# Defaults to org/docx4j/jaxb/mc-preprocessor.xslt
	docx4j.jaxb.JaxbValidationEventHandler=custom-preprocessor.xslt
	
	# The styles part content used by WordprocessingMLPackage createPackage
	# and by getStyleDefinitionsPart(true) where the styles part is null
	docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart.DefaultStyles=org/docx4j/openpackaging/parts/WordprocessingML/styles.xml
	
	# Used to try to activate a style (PropertyResolver.activateStyle) which isn't defined in the styles part
	docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart.KnownStyles=org/docx4j/openpackaging/parts/WordprocessingML/KnownStyles.xml
	
	docx4j.openpackaging.parts.WordprocessingML.FontTablePart.DefaultFonts=org/docx4j/openpackaging/parts/WordprocessingML/fontTable.xml
	
	docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart.DefaultNumbering=org/docx4j/openpackaging/parts/WordprocessingML/numbering.xml
	
	# Embedded Fonts - extract to dir
	# By default, docx4j will extract embedded fonts to dir 
	# ~/.docx4all/temporary embedded fonts
	# (creating it if necessary).
	#docx4j.openpackaging.parts.WordprocessingML.ObfuscatedFontPart.tmpFontDir=c:\\temp
	
	# .. placeholder to use instead of "Click here to enter text" 
	# Defaults to OpenDoPE/placeholder.xml, and if nothing is there,
	# will fallback to org/docx4j/model/datastorage/placeholder.xml (which is in the docx4j jar)
	#docx4j.model.datastorage.placeholder=OpenDoPE/placeholder.xml	
	# What is new is that you can override that location (ie so you don't have to create an OpenDoPE folder)
   
Pptx4j
------

d1b60a96e7 pptx4j: dedicated parts for slide comments 

b452e79852 Support for opening pptm/potx/potm files

Other Changes (non-exhaustive)
------------------------------

8e640dafb7 dedicated parts for VbaProjectSignature

9d714432db partName is now private; has getter/setter

e66884b830 Create rels part name dynamically
   
31mff84cf0  DocumentModel: use SectPrFinder to find the sectPr (which means it will now find sectPr inside content controls) 

31m0700196  preset Shape definitions

31mfbef8b9 Map font names case-insensitive, since Word treats w:rFonts attribute values case-insensitive.

31mfa767f6 RPr and ParaRPr inherit from RPrAbstract

31m7c9dd58 signature line attributes: add namespace; fixes https://github.com/plutext/docx4j/issues/121 

31m543713f Support for part representing userShapes added to a chart

31m265ee8b fixed code that generates textId (BobFleischman)

31m9a8b75c BinaryPart: Tidy up code and remove soft reference ByteBuffer. 

Import XHTML

    31m85b7a4d  In XHTML import, span/@style='background-color:red;' would usually become w:rPr/w:shd/@w:fill="ff0000" Now you can configure it to use w:highlight instead

    31m810ed9b LineSpacing: handle the CSS default 'normal' sensibly when importing 

31mdbf7dee StyleUtil: changes to isEmpty and apply semantics for Style objects
 
31m3845825 StyleUtil changes: - change to isEmpty semantics for Integer, BigInteger so that a value of 0 is not treated as empty (since otherwise eg spaceAfter 0 isn't applied) - pPr includes sectPr, so add PARTIAL implementation of areEqual and apply for sectPr

31mea22fb6  make jc final. it's used in a lot of places but only assigned once. (apixandru)

31m8edaddf For Oracle Java 8, use com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl and com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl

31mdf84571 Workaround for Microsoft SQLServer Reporting Service (SSRS) 2012, which generates invalid docx 

31mf0fbd9f User can override org/docx4j/jaxb/mc-preprocessor.xslt with an xslt on their classpath named custom-preprocessor.xslt




Version 3.1.0 
=============


Release date
------------

23 April 2014


Dependency changes
------------------

mbassador 1.1.10 jar; see http://www.docx4java.org/forums/docx-java-f6/nightly-20140403-requires-mbassador-jar-t1852.html



Notable Changes in Version 3.1.0
---------------------------------

Events infrastructure, allowing a listener to subscribe to track progress of time consuming tasks (eg PDF output).
See for example https://github.com/plutext/docx4j/blob/master/src/samples/docx4j/org/docx4j/samples/EventMonitoringDemo.java

FO/PDF output: suitable room for header/footer


Other Changes (non-exhaustive)
------------------------------

Part remove - new method
VbaDataPart - namespace qualify attributes  

Fonts on Mac OSX - avoid NPE

docx Binding fixes for case where OpenDoPE parts are not present

docx fix for non-conformant Google Docs docx

docx PDF (and XSL FO) output 
- TOC dot leader tabs
- soft return reduce vertical space 
- computed style improvements for p in table
- Arabic numbering
- Arabic/Hebrew output: w:pPr/w:bidi and w:rPr/w:rtl handling
- Support bidiVisual property, to layout columns in rtl order for eg Hebrew
- Chinese improvements

pptx
- slides: use UTF-8, irrespective of default encoding

xlsx
- add bookViews/workbookView to workbook to ensure Excel 2010 doesn't crash when printing



Version 3.0.1
=============


Release date
------------

8 Feb 2014

Contributors to this release
----------------------------

David Becker
Jason Harrop
Anton Reshetnikov (batonez)
Dirk Rewoo


Notable Changes in Version 3.0.1
---------------------------------

OpenDoPE - Handle escaped Flat OPC ie bind rich text content (tag contains 'od:progid=Word.Document') 

Other Changes (non-exhaustive)
------------------------------

PDF/FO output - basic implementation of textbox handling (dependent on support in FO renderer)

61ea5a2 - FieldsPreprocessor ignore empty w:instrText elements
5a21b10 - Fields: handle nested p, eg w:p//v:textbox//w:p
e726d4d - MailMerger: handle quoted field name (containing spaces)
0057b7b - text box handling in MailMerge (David Becker)

56227b3 - OpenDoPE.  New  CustomXmlDataStoragePartSelector to identify the XML part being replaced
c672b1f - Renumber bookmarks in repeats
01203b6 - placeholder.xml - user can override with OpenDoPE/placeholder.xml
06a8f02 - od:Handler=Picture processes a rich text control containing an image
1525482 - OpenDoPE org.docx4j.model.datastorage.BindingTraverser.XHTML.Block.rStyle.Adopt option to automatcially apply linked paragraph style to bound escaped XHTML

cf9a517 - VML shape interfaces
87ac08f - use default font size of 10pt (same as Word)

5c297e1 - List numbering emulator - fix for lower lettering (batonez)

ba59b54 - xlsx4j getWorksheet(int index)
ab59e6c - xlsx4j ExternalLinkPart

7ee0ab5 - pptx4j getSlide(int index)
86f5ebb - pptx4j SlidePart marshal mc:AlternateContent 

1491394 - mc:AlternateContent JAXB model

ba560ca - Support Google AppEngine (GAE)

dd51255 - RunFontSelector bug fixes




Version 3.0.0
=============


Release date
------------

26 Nov 2013

Contributors to this release
----------------------------

ai
azerolo
bezda
EthanTsui
hpeng
Jason Harrop
jlesquembre
jeffbeard
fmmfonseca
meletis
pnml
Tinne
tj09
tstirrat
vollewijn
zluspai


Notable Changes in Version 3.0.0
---------------------------------

Logging: Use slf4j instead of log4j
 Move XHTML Import stuff into a separate project, to get rid of dependency on LGPL library.
Sample code has moved to src/samples
Docx4j facade (contributed by Alberto)
Extensive rearchitecting of PDF and HTML export (contributed by Alberto)
PDF output enhancements, especially tables (contributed by Alberto)
PDF and HTML output, without using XSLT (proof of concept)
PDF and HTML output use rFont font selection for different unicode ranges
Support for switching to/from MOXy as your JAXB implementation
PartStore interface: save a docx/pptx unzipped 
Use FOP 1.1 for PDF output, or optionally something more recent
Generate org.docx4j.math from ECMA 376 2ed
Update SML to ECMA 376 4ed transitional 
MERGEFIELD and DOCPROPERTY field improvements, including formatting switches
Support XPath operations on all docx, pptx and xlsx xml parts
Support for portions of [MS-DOCX] (w14,w15 namespace support)
Update pml to ECMA 376 2ed
 

Other Changes (non-exhaustive)
------------------------------

1045e36 - OpenDoPE pictures: reuse template picture (if present), for size, positioning etc

6724f06 - Make build compatible with Java 7 (alberto) 

c018431 - Images: Make configurable whether GC is performed prior to temp file deletion

d0e0bfe - Data binding: where XPath returned is empty, add placeholder content (workaround for some patch levels of Word 2010 x86 where multiline is true)

0bf7610 - BinaryPart getBytes() method 

1ca228f - a table without rows or a row without cells produces invalid fo (alberto)

59b03a9 - OpenDoPE: Take Word styles into account in importing XHTML 
e276493 - Support using a customised WriterRegistry 

98e88d7 - Binding: if FLAG_BIND_INSERT_XML is not set, allow null XML data

eddec90 - Convenience methods to add and remove slides 
23c42e5 - User friendly get/setContents methods (more intuitive than get/setJaxbElement) 
4f9ed48 - userData is Map<String, Object>
0485568 - Support custom ID on <img element 
2e1ec6c - HTML via XSLT: enable use of user defined ConversionHTMLStyleElementHandler (and script handler) 

26ca4f1 - New method NumberingDefinitionsPart resolveLinkedAbstractNum which updates abstract list definitions which have w:numStyleLink 

aa3ff88 - doc defaults style: - setup once only - make sure this is done before fontsInUse() 
          - fix regression in PropertyResolver (documentDefaultPPr, documentDefaultRPr) 

68ddc14 - improvements to finding sdt context (block, inline etc)

f8d3303 - differencing: add some common required namespaces 

afa87e3 - ThemeOverridePart

090b367 - Bugfix: retain picture control 

aa52ce2 - getBinder() create binder in case where user has manually set jaxbElement

b56ccb6 - new method getDefaultTableStyle() 

5e08f16 - fix for duplicate FORMTEXT results 

5f0a4aa - Package for classes which find specific objects via traversal 

f2bb6b4 - 2.8.1.8 remove <w:highlight w:val="lightGray"/>, if present

641d032 - Ensure table integrity in face of sdt removal 

4401138 - Convenient getGlossaryDocumentPart() 

8d14ba3 - Ability to specify what CSS font-size 'medium' is in points 

763aa09 - add extra processing option to remove SDTs bound to an empty/non-existing/void XML location which would otherwise prompt the user for data upon opening the file in Word. 

df64bd5 - MERGEFIELD multiline output (tokenises on the newline character, the carriage-return character, and the form-feed character)
696dbe3 - Ability to convert MERGEFIELD to FORMTEXT, so output can be edited when editing is restricted to fields (from 2.8.1.7)

f70a80e - HTML output: Basic support for images in header Fixes Issue 10 

63489de - Simple support for \* MERGEFORMAT 

ee744d6 - RemovalHandler: For od:resultRepeatZero or od:resultConditionFalse, remove the content as well. 

07730b9 - table cell shading from background color

6fc31ea - Workaround for OpenXML SDK 2.0, which writes Target="/word/document.xml" 

863f31a - w:tc integrity, nested table case
99fe79b - Move minimiseContentOfSdtCell (previously eventuallyEmptyList) to OpenDoPEIntegrity, and handle case where there is no nested table. 

7a69cd9 - Fix for xml:space being dropped in return from BindingTraverserXSLT 
5509360 - A content control with SdtPr w:dataBinding and w:text which contains a w:hyperlink will prevent Word 2007 from opening the docx, 
          so if there is a w:hyperlink, remove w:dataBinding and w:text (if present).

6cd99a7 - Update RemovalHandler to handle BINDING_RESULT_CONDITION_FALSE and BINDING_RESULT_RPTD_ZERO 
e1f1dca - OpenDoPEReverter - initial implementation 

e3cc9e4 - Get rid of BINDING_ROLE_RPT_INSTANCE = "od:RptInst"  entirely, because it became redundant after introducing 
          "od:RptPosCon" processing (conditional inclusion dependant on position in repeat), which requires each entry 
          (ie including the original) to have the same tag (od:rptd).

c3d8c66 - Improved diagnostics for case where XPath returns empty result set 
5f7af4b - BindingHyperlinkResolver, which can be overridden if you want to customise hyperlink handling. 
          You can override what is recognised as a URL, and how that is converted into a hyperlink. 
11f8607 - Fix NPE in conjunction with almost empty documents (tinne)
7b26726 - Added conversion support for some css properties (ai-github)

6180c8f - throw InputIntegrityException if not found 

6be807b - Workaround for Word 2010 not displaying all spaces where you have multiple FORMTEXT in a single run 

7eb5b5e - Only canonicalise the fields we're able to update: namely MAILMERGE and DOCPROPERTY 

c78d336 - Field canonicalisaton: better support for nested fields 

ca573ae - field canonicalisation: missing separator case - bug fix 

ea1d741 - There can be more than one style which isn't basedOn another, so don't make such a style the root of the hierarchy.  
          Instead, a child of root. 

9e806af - Use type of main part to create pkg

793fdd7 - Fix for ClassCastException in eventuallyEmptyList, where SdtCell contains another SdtCell 

6c9870f - Option to retain instrText in output of MERGEFIELD processing. 

130ff05 - FieldsPreprocessor - minor refactor (extract method) 

6f442cf - VBADataPart now has JAXB representation; VbaProjectBinaryPart now extends OleObjectBinaryPart. 
          VBADataPart works for a docm; I haven't seen an xlsm with a VBADataPart, so will have to wait til one turns up to get this right. 

e49e436 - XSDs from S-OFFMACRO2] 

ec53dab - Use Hyperlink model/writer in HTML output. 

0866667 - Ensure tables meet XSL FO requirements (table body present, and all header rows precede body rows). 
79ad93f - Check for "continue" as a vmerge value in cells, otherwise rowspan might not be calculated properly. (TJ09)

5eb0d35 - update to diffx-0.7.4 (BSI)

d72e3e4 - use decimalSymbol specified in document settings part 

a584bbd - Ensure there is just a single run between SEPARATE and END

df9afd6 - Import Word 2003 XML. Proof of concept. 

88e2134 - new Docx4jProperties: docx4j.javax.xml.parsers.DocumentBuilderFactory.donotset,
           docx4j.javax.xml.parsers.SAXParserFactory.donotset 

0c43b5b - EmbeddedPackagePart new method extractPackage() 

193165b - CTM: Recognise OleObjectBinaryPart 
          OleObjectBinaryPart: display OLE Directory to output stream 

120183f - Cambria in headings is by virtue of theme major font; follow that logic.

63c61bf - simplifying AlternativeFormatInputPart with better use of enum (zluspai) 

a947e6e - Header|Footer part, Styles, Numbering now extends JaxbXmlPartXPathAware 
 
8908a82 - VerticalAlignment css value "super" 

ec6c723 - Bug fix: Change content type for mht to "message/rfc822" (Zoltan Luspai) 

7c258e0 - Support XPath operations on all pptx JaxbPmlParts

38340ac - new method List<JAXBAssociation> getJAXBAssociationsForXPath 
          existing getJAXBNodesViaXPath now throws XPathBinderAssociationIsPartialException

3b88e64 - xlsx getJAXBNodesViaXPath

83f2b4f - Workaround for Powerpoint 2010 bug reading AlternateContent element.
7127794 - bug fix: XSL FO doesn't like font color #auto 
0d1feb4 - FieldUpdater: new class which can update field values (currently DOCPROPERTY) only independent of PDF output. 

4071a24 - HTML output: honour indentation specified in numbering. 

2bfe95a - PDF numbering first line indentation now works  

96fb0d2 - Numbering first line indent: basic support 

8993259 - BigInteger apply: still use source if its zero. We need to honour things like <w:ind w:firstLine="0" /> 

3f34e12 - Numbering in PDF output: get hanging indents right. 

93e87a1 - page-numbering: only specify initial-page-number on fo:page-sequence if start number is specified in sectPr.

0de7c01 - Inherit default header or footer from previous section if this section doesn't have one, 
          irrespective of whether this section has a first page header/footer. 
1876991 - Fix for org.apache.fop.fo.pagination.PageProductionException: Subsequences exhausted in page-sequence-master 
          which occurred where a sectPr contained a first page header or footer only. 

49bdfdb - FOP configuration changed after FOP 1.1.  Our FOP configuration now supports FOP before or after 1.1. 
          This means you can use FOP 1.1 to get around FOP's page-break-before bug. 

3a2128e - Workaround for org.apache.fop.fo.ValidationException: "fo:flow" is missing child elements. 
          Required content model: marker* (%block;)+ which occurs if a docx contains a single p containing a sectPr, 
          or consecutive p each containing sectPr.

aa16a2c - Implement java:org.docx4j.convert.out.Converter.getFootnote 
4d5039d - Sketch of line spacing conversion 

545f3f0 - field formatting unit tests. 
1e5cf3f - Specify character style on each run 

d279d80 - Set spacing in documentDefaultPPr
           - in HTML via XSL output, use that; also get default p style ID properly. 
          - propertyresolver: call createVirtualStylesForDocDefaults() instead of duplicating code 
c2129ec - Follow spec rules for when formatting switches apply (outline). 
b67a875 - numbering formatting switch 
81d3952 - DOCPROPERTY processing in PDF output. 
71f7f94 - JAXB model for coverpage properties 
d12aaa1 - Fix getJAXBNodesViaXPath v3 NPE for v3 

206399d - Fields - bug fix in canonicalisation of instrText (merge multiple); and add FieldDiagnostics sample. 

d777360 - CT_TblLook from transitional 4ed; fixes https://github.com/plutext/docx4j/issues/53

1b49869 - XHTML content controls - use cc rPr + rPr derived from CSS EXCEPT default font color and default 11pt. 

6712f6f - Honour rPr provided in escaped XHTML's SdtContent, unless overridden in the CSS. 

7c65db6 - Experimental VariableReplace method (avoid unnecessary unmarshal/marshal steps) 

6eeb0ad - w3CDomNodeToString: emit XML! 

4aa0860 - traverse w:object/w:control 

16680f9 - Find Calibri etc on Mac OSX 

be607ea - Bugfix: preserve PPr in MERGEFIELD processing

003e5c5 - Correct semantics for parent/child 

1c1190f - Fix for issue 46 (POI uses default .xml content type to identify pkg) 
ded7bbd - NotesSlidePart support 
5f354f3 - CxCy support for SldSz 

054090e - Handle sectPr within content control 

db062c6 - Handle continuous sections properly 

db4a839 - Alberto Zerolo patch of 22 January, including support for evenAndOddHeaders flag. 

68d88b4 - When there is no mc:Fallback, remove the mc:AlternateContent entirely (rather than hoping for the best with mc:Choice[1]).  
          See http://www.docx4java.org/forums/topic1326.html 

6a9b61a - Apply patch contributed at http://www.docx4java.org/forums/docx-java-f6/xhtml-docx-npe-during-loading-image-with-wrong-url-t1333.html 

3f3bf5c - (X)HTML output - whether this is well formed XML or not is now configurable in docx4j.properties 

8422e3c - Alberto Zerolo's header/footer contrib 

3a48581 - Update SML to ECMA 376 4ed transitional (with support for xml:space in Shared Strings part) 

53d785d - Support xml:space in shared strings table. This was missing from the XSD; 
          but its absence causes files Excel can't open! 

8cc41a2 - xpathDate - support StandardisedAnswersPart. 

c31cdb2 - NonXSLT: support for hyperlinks

9c04408 - Support for nested tables

565403a - PDF output: ignore bookmark between table rows, so table processing works. 

4e442a0 - Remove old LoadFromZipFile.

1717906 - Lazy unmarshalling

392821e - Separate the loading of a package, from its physical store 

2088538 - Context: display manifest from JAXB jar mc-pre: ignore attributes 
          ConvertOutPDF: workaround for FOP bug where first P has page break before.

0a871e4 - bug in public static boolean isEmpty(BooleanDefaultTrue booleanDefaultTrue), pointed out by hpeng 

0cb3c68 - XSLFOExporterNonXSLT is a Proof of concept of generating XSL FO without using Xalan/XSLT.  
          This approach is faster and less memory intensive.

305b3b8 - interface CustomXmlPart implemented by CustomXmlDataStoragePart and JaxbCustomXmlDataStoragePart.  
          customXmlDataStorageParts collection is now Map<String, CustomXmlPart> 

1948858 - Preserve mc:AlternateContent in a pptx slide's p:controls 

5f52ecd - table handling improvements, contributed by Alberto Zerolo

d480a83 - Notes regarding FOP r1356646 (after FOP 1.1), in which FopFactory.newInstance() was replaced with FopFactory.newInstance(URI) 

72d061b - Fix for cases where getParent is broken, so it can be used in traverse

daf2570 - Mechanism to allow a dev to associate arbitrary data with a package or part. 

7e62425 - Call setPartShortcut, even if this part has been encountered already 
          (since for pptx, more than 1 slide typically use the same layout part) 

9e0590a - For break-before false, set value to auto in XSL FO output. 

01d31ba - Support for converting MERGEFIELD to data bound content controls.
2b1b408 - Convert a docx containing variables ${--} to bound content controls. 
c7384bd - Convert variable replacement templates to OpenDoPE content control databound template. 

5f587bd - Bug fix: mc-preprocessing for XLSX (ie other than Context.jc) 

afd9610 - Helpful error message if your JAXBElement is null 

d963e2a - OpenDoPE optional standardised answer file format. 
14d7df9 - Support for switching to/from MOXy as your JAXB implementation

9e5cf5f - Generate org.docx4j.math from ECMA 376 2ed, to address ST_OnOff issue. 
          (see http://stackoverflow.com/questions/10811078/xmldirectmapping-no-conversion-value-provided ) 

5a43e01 - bookmarkEnd, moveFromRangeEnd, moveToRangeEnd  all use CT_MarkupRange. wml.xsd contained a proposed change, 
          so that only bookmarkEnd does, but corresponding org.docx4j.wml was never generated. So comment out this change  
          until such time as we're ready to run XJC. 

be02784 - data constraints
6e79bf0 - FOP 1.1 
f57d41e - Remove od:rptd 
5c0c945 - Handle case where pPr.getNumPr().getNumId() is null 
e393fc5 - Added sample program to edit an embedded chart in a PowerPoint presentation (jeffbeard)
a5e7ca8 - Added XmlRootElement annotation that was causing an Exception when attempting to write an Excel spreadsheet
          to a disk file. In this use case, the spreadsheet was embedded in a PowerPoint slide as the backing store for a chart. 
5997616 - Update pml to ECMA 376 2ed. Want this for its revised CT_Control. Closing https://github.com/plutext/docx4j/issues/13 
0ea48f4 - expose getStyleById method 
9ae0190 - Improvements to numId/ilvl identification 
389af95 - VML text box: avoid possible NPE 
dff682d - header/footer documentation & test cases 
e8eecf8 - Code refactored Handle top position with and without header
9121390 - Header and footer keep the layout as applied after PDF conversion
da8f9c5 - Merge pull request #33 from jlesquembre/upstream #[32m(12 months ago)#
fa71a43 - First approach to improve margin transformation from docx to pdf 
85455e6 - Preserve the formatting of the rest of the paragraph content. 
1868a04 - Bug (in 2.8.1) fix: tr/tc finder, don't look in nested table.
30ebd56 - refresh the document model, so it can know about headers/footers added programmatically #[32m(12 months ago)#
c497c45 - skip external relationships in RelationshipsPart.getRel() (fmmfonseca)
c16344f - TraversalUtil: traverse into hyperlink in Anchor and Inline
417c377 - Prevent malformed uri exception when supplying a file path with no protocol. Fixes #26 (tstirrat)
61af02f - support table row height in PDF output



Version 2.8.1
=============

Release date
------------

30 September 2012

Contributors to this release
----------------------------

Jason Harrop
Olivier 'olabrosse'  
sgrachov
bezda
tstirrat
Nanocom
dmole

Notable Changes in Version 2.8.1
---------------------------------

improvements to XHTML import
support for OpenDoPE complex conditions 


pptx4j
------

b6c12c8 - make getSlideLayoutOrMasterId public  
c8f6b33 - addSlideIdListEntry(SlidePart slidePart, AddPartBehaviour mode) 


content control databinding (OpenDoPE) changes
----------------------------------------------

128853a - support for OpenDoPE complex conditions 
8675678 - OpenDoPE od:RptPosCon bugfix 
68fb8bc - Databinding for date content controls  
dd3ed9a - Distinguish repeat instances, so "and" between repeat items still works when a repeat is re-used. 
049ebe9 - Support for od:RptPosCon (retain/delete a content control sensitive to position in a repeat)  
6a19661 - Make merge sample work where OpenDoPE parts aren't present; add sample data  
 

Other Changes (non-exhaustive)
------------------------------

13070d5 - XSL FO PDF output Support for w:customXml
7148124 - HTML non XSLT Use \u2022 for bullet  
d68261a - Get the part from whatever package its rp thinks owns it (useful for MergePPtx) 
d0deb52 – HTML table output: find tc nested in other content  
8bdc83f - HTML table output: Improved approach to finding tr, tc\
4872d98 – XHTML import: rudimentary support for table borders off  
c9a1a35 - Bugfix in picture content control binding, contributed by Olivier 'olabrosse' 
c7acf15 - New method performMerge(WordprocessingMLPackage input, Map<DataFieldName, String> data, boolean processHeadersAndFooters 
80ea26f - Fix for https://github.com/plutext/docx4j/issues/11 Don't attempt to normalise image extension. 
8009929 - Support for MailMerge into headers/footers  
08ec3f1 - Basic support for EastAsian (eg Chinese) fonts (eg SimSun) in PDF output.  
0b943b7 - Fix for class cast exception in JAXB which resulted from implementing ContentAccessor interface on EndnotesPart and FootnotesPart.  Those parts no longer implement that interface.  
c889549 - Possibility of doing the binding step, without using XSLT
59abc02 - Sketch of TextDirection (WIP).  
eac9a9f - Prefix mappings for OpenDoPE answers, XForms, XML events, and XSD  
a0c898a – HTML non XSLT Add a method for exporting a specific block level content object; and getCss() 
19086e4 - workaround to support color:# as in <span style="color:#ff000"> 
cc29ed0 - docx4jexception instead of npe when attempting to load bad zip file  
cb01aa3 - diff doesn't work for 5000+ elements 
afd9a70 - utf-8 in font names 
031d9ec - Made CTTxbxContent implement ContentAccessor 
940cfba - FldChar support for TraversalUtils  
20bdafd - better cell margins for pdf  
932843c - first-line indent support for pdf  
ebaadc7 - Fixed NullPointerException when (invalid) numbering overrides are set to override a non-existent numbering definition. 
83d6e8a - Handle paragraph space before and after in HtmlExporterNG2 output 
eb2b366 - Save existing System.getProperty("javax.xml.transform.TransformerFactory") and restore.  
57404de - Move choice of DocumentBuilderFactory from XmlPart, to XmlUtils. This and SAXParserFactory is now configurable via docx4j.properties. 
b91e4f1 - SAXParserFactory can be read from docx4j.properties 
1066a9e – XHTML import fixes in course of importing several pages from Wikipedia: - nested tables (insert empty p; hack to fix hierarchy) - hyperlink inAlreadyProcessed - lax table next cell null 
9c3ffd0 - Fixed handling of special characters (bullet, non-breaking space) by avoiding use of character entities.  Instead, use UTF-8 characters in Java extensions (ie \u...) 
523823c - Use DOMResult, so we can use part.unmarshal, which should create a binder where possible  
d4fa494 - Support base 64 encoded image in data URI 
18fdf42 - Word for Mac 2010 doesn't set title  
b29b9cd - Configurable regex, to limit to common fonts in order to avoid java.lang.OutOfMemoryError: Java heap space  
3ae1448 - Footnote/Endnote parts implement ContentAccessor  
c2dc9cb - Improvements to XHTML import (tables)  



Version 2.8.0
=============

** docx4j source code is now on GitHub. Our SVN repository is obsolete. **

Release date
------------

24 May 2012

Contributors to this release
----------------------------

Jeromy Evans
Jason Harrop
Arnaud Kleinpeter
Pierre
Tinne

Notable Changes in Version 2.8.0
---------------------------------

XHTML import

org.docx4j.samples reviewed

svg JAXB content model moved to its own project on Github, and uploaded to Maven Central 
svg JAXB content model moved to its own project on Github, and uploaded to Maven Central 

[1727] Field processing infrastructure, and [1732] MERGEFIELD processing

HTML export option without using Xalan/XSLT

xlsx4j
------

40056bc - Convenience method getSharedStrings() 

pptx4j
------

89be7a1 - Add XPath support to SlidePart 

OpenDoPE changes
----------------

Support binding/import of escaped XHTML content

[1763] Support pictures in repeats (for this, a picture content control must have an od:xpath tag)
469e126 - Make XHTML work in a repeat 

Other Changes (non-exhaustive)
------------------------------

[1729] Improved approach to determining appropriate NamespacePrefixMapper
[1736] Headers/footers: Work around for class cast exception in BinderImpl.unmarshal
[1741] Add element name="compatSetting" and type="CT_CompatSetting" from ECMA-376 2ed to wml, so Word 2010 can detect docx as native
[1745] Numbering: Support w:startOverride
[1746] Numbering: Support for DECIMAL_ZERO (01, 02, 03 ..) and basic support for "Legal" numbering
[1747] Numbering: w:isLgl
[1751] XPath-aware deep copy
[1754] Improvements to indentation (direct indent values are given priority over values in numbering, which in turn is given priority over values in styles), plus associated unit tests.
[1758] For Oracle's Java 1.7, System.setProperty("javax.xml.parsers.SAXParserFactory", "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
[1760] Convert first line and hanging indent to CSS (using text-indent). 
[1762] For superscript, FO property corrected to "super"
[1764] Traverse to DML pictures
[1767] Improvements to bestmatching font mapper
[1774] Open a .potx file (powerpoint template)
[1775] Interfaces for dml.chart
[1776] Internal hyperlinks sample
[1777] Convenience method to create hyperlink to bookmark, plus example of use
[1778] Read docx4j.properties for page size & margins
[1779] Fix for getAllAppenders issues (slf4j)
[1784] XmlSignature part, which uses JAXB representation of xmldsig-core, contained in separate project
33b2ab1 - Support reading a password protected docx (which is a compound file)
4a6604a - Add Eclipse project configuration 
887b5ac - Introduce abstract class JaxbXmlPartXPathAware, which MDP, header/footer etc extend
1b75487 - XmlPart: Use a document builder, even if Xerces (Apache's or Sun's) is not available
e1b616e - Create VbaDataPart and VbaProjectBinaryPart
54f2fad - AltChunk handling improvements 

Version 2.7.1 
=============

r1601-

Release date
------------

14 October 2011

Contributors to this release
----------------------------

Albert Aymerich
alberto
Antoine
Jason Harrop
y.rolland

Notable Changes in Version 2.7.1
---------------------------------

Preparation for including docx4j in Maven Central

[1605-1610] mc:AlternateContent preprocessor, allowing graceful degradation of Word 2010 specific content

[1604] docx4j.properties, supports configuration of default page size, margins, orientation; also ability to set some of the doc props metadata (Application & AppVersion; dc.creator & dc.lastModifiedBy).

[1631, 1637] HtmlExporterNG2,(Pdf)Conversion, SvgExporter: storing any images is delegated to a
ConversionImageHandler that may be passed as a conversion parameter. Default implementation: DefaultConversionImageHandler

VFS stuff moved to docx4j-extras

OpenDoPE changes
----------------

[1639] Change static OpenDoPEHandler design to instance-based design, with objective of making it thread-safe.

[1645] When binding, create hyperlinks out of text containing http://

[1653] Handle unwrapping correctly in ShallowTraversor, so JAXBElements stay wrapped, and we don't risk a marshalling exception for any which don't have an @XmlRootElement annotation.

[1658] Word can only resolve an XPath binding which results in an element (as opposed to a boolean, integer, string or node-set). OpenDoPE processing can handle these other results types (some of them anyway).
Up until now, that processing was done in OpenDoPEHandler. Now it is done in BindingHandler and bind.xslt, for consistency with how normal Word XPath bindings are handled by docx4j.

[1662] Bind picture correctly where parent is another content control.

Other Changes (non-exhaustive)
------------------------------

[1613] Header and footer parts use XPath binder

[1679-80] create image part directly from file

Version 2.7.0 
=============

Release date
------------

8 July 2011

Contributors to this release
----------------------------

alberto
amdonov
azeloro
Dave Brown
Jason Harrop
Marcel
Patrick Linskey
ppa_waw
Richard
Tinne

Notable Changes in Version 2.7.0
---------------------------------

Improvements to Maven build

ContentAccessor interface

AlteredParts: identify parts in this pkg which are new or altered; Patcher 
which adds new or altered parts.

Support for .glox SmartArt package (/src/glox/) 

JAXB RI 2.2.3 compatibilty

OpenDoPE improvements (see below) 

xlsx4j
------

[1455] Support for Spreadsheet Comments.

[1494] Detect /xl/workbook.xml as WorkbookPart, rather than DefaultXmlPart.  
Add convenience method getWorkbookPart

pptx4j
------

[1539] Better support for slide size.

[1549] Convenience method to get MainPresentationPart

OpenDoPE changes
----------------

[1339] OpenDoPEHandler: Pre-processing step evaluates an od:xpath which doesn't have a corresponding w:databinding.  
This is designed to handle an XPath expression which evaluates to a boolean or number, rather than a node.

[1389] Generalise applyBindings, so that it should work on not just a DocumentPart, but also a header or footer part.

[1390] Act on databinding for content controls of type picture.

[1423] Support w:multiLine data binding.

[1441] scale image based on content control size

[1449] Handle a databinding which points to Core or Extended Properties, or CoverPage props.

[1453] Traverse into text box.  Handle content control in text box.  

[1506] Process Header and Footer parts as well.

[1547] Tinne's patch of 20 June, which takes the Jan-Willem van den Broeks XPath grammar and builds it into a rewriting parser that enhances xpath expressions just the way that is needed. Thus, all xpath 1.0 expressions can be used.

Other Changes (non-exhaustive)
------------------------------

Various PDF & HTML output improvements

Tuning of log levels; removal of some System.out.println

[1333] Fix for image part naming.

[1344] UnitsOfMeasurement, fix for Germany, where they use a comma as decimal separator instead of a point.  Solves issue in FOP

[1352] Rework header/footer model to take account of "same as previous" and whether first page header/footer is active or not.

[1356] Code cleanup: remove old approaches to HTML generation.

[1358] Allow for user-defined handlers to prepare HTML output depending on the value of an sdt tag.

[1396] docx4j is not dependent on Xerces (other than in XmlPart), but Websphere (presumably using IBM JDK) doesn't have Sun's Xerces implementation, so use real Xerces if it is on the class path

[1412] Add XPath support to header part.

[1416] coarser grained ways to tokenize text when
diffx turns XML into a stream of events. The current diffx stuff creates
a token for every word, and on large documents, the diff algorithms become
unwieldy in terms of memory usage/time. Coarser text splitting makes fewer
events.

[1432] interface to getSdtPr
[1437] SdtElement interface; CTCustomXmlElement interface

[1461] VML generated classes, based on ECMA 376 1ed (rather than earlier draft).  

[1470] Make docx4j compatible with JAXB RI 2.2.3 unmarshalling

[1479] extension to TraversalUtil, which allows you to define the tag you are interested in as a generic of the visitor class.

[1480] StyleUtil: styles areEqual, isEmpty, apply

[1481] Bugfix: Handle internal HYPERLINK 

[1487] MetafilePart to extend BPAI, so WMF images can be added.

[1492] Support for http://schemas.openxmlformats.org/officeDocument/2006/bibliography

[1536] Support for common paper sizes.

[1537] Knowledge of "well known" margin settings.

[1556] Native support for bitmap (bmp) images

[1569] Configure log4j automatically if necessary; paves the way for all System.out.println to be removed.

Version 2.6.0
=============

Release date
------------

18 Nov 2010

Contributors to this release
----------------------------

Jason Harrop

Major Changes in Version 2.6.0
------------------------------

OpenDoPE ("Open Document Processing Ecosystem") v2.2 implementation for generating documents using repeats, conditionals and component inclusion.
Implementation now lives in model/datastorage package.

TraversalUtil class, which makes it easy to find things in the main document part (an alternative to XPath), and optionally, do something to them

Dependency cleanup, now uses FOP 1.0, and standard Xalan 2.7.1

Other Changes (non-exhaustive)
------------------------------

[1177] PDF output: set margins in layout masters; make room in region body margins for header & footer; set header & footer extents manually
[1182] Support for page number field <w:fldSimple w:instr=" PAGE   \* MERGEFORMAT ">
[1196] Ensure docx4j can be built using either ant or maven, with only one of the JAXB implementations (Java 6 or RI) required
[1212] Basic support for paragraph shading and borders in PDF and HTML output.
[1217] Bug fix: image part naming
[1220] If there is w:pPr/w:pStyle, we must honour any rPr in the pStyle (reinstate code commented out months ago)
[1231] Use official xmlgraphics-commons-1.4
[1232] Make it possible to run certain samples from the command line.
[1234] Use FOP 1.0.; Include source code for fop-fonts, as org.docx4j.fonts.fop. Move panose to org.docx4j.fonts.foray
[1235] Include @sub-font in FOP config; this is required for TTC
[1238] Use standard Xalan 2.7.1 instead of our patched version; remove references to DTMNodeProxy
[1262] Use style0 as default para style for docx from OO
[1270] Support full justification in XSL FO
[1273] HTML output: when test="contains(./w:sdtPr/w:tag/@w:val, '@class=collapse')" allow the sdt to Collapse.
[1295] Methods to check whether partname is already in use.
[1302] Pass Relationship to newPartForContentType so AlternativeFormatInputPart can be detected.
[1306] EmbeddedPackagePart
[1307] OpcPackage: don't create props parts, merely because user has asked for one. CorePart: set JAXB context correctly. Rels part: relId generation altered

pptx4j changes
--------------

[1179] Basic support for images in pptx svg in html output
[1180] Alter slide to html/svg api, to make it more obvious you are processing a slide, and doing so one at a time.
Only show text box with a red dash border if debug level logging is enabled.
[1185] Support Word 2003 page numbers in PDF output. ie <w:fldChar w:fldCharType="begin"/> <w:instrText xml:space="preserve">PAGE  </w:instrText>
<w:fldChar w:fldCharType="end"/>
[1198] Method for creating a slide; don't do that when creating package.

Version 2.5.0
=============

Release date
------------

15 July 2010

Contributors to this release
----------------------------

Jason Harrop

Major Changes in Version 2.5.0
------------------------------

[1152] XPath query which returns live JAXB objects
[1158] Content control pre-processing for conditionals, repeats.
[1167/8] PDF conversion via HTML or iText moved from main source tree;
       iText, xhtmlrenderer and pdf-renderer dependencies removed.

Other Changes
-------------

[1152] Content control data binding xpath namespace stuff integrated into NamespacePrefixMappings.
[1164] SaveToZip: .xml extension implies save as Flat OPC instead
[1164] XmlUtils.unwrap

Version 2.4.0
=============

Release date
------------

9 July 2010

Contributors to this release
----------------------------

Jason Harrop

Major Changes in Version 2.4.0
------------------------------

[1135] PDF via XSL FO: header/footer support for more than 1 section
[1134] JAXB representation of XSL FO

Other Changes
-------------

[1140] Try harder to delete add image temp file
[1139] HTML, PDF: highlight wholly unimplemented features, only if debug-level logging is enabled.
[1131] Enhancements to XSL FO output
[1130] Support for .dotx and .dotm
[1129] Fix instances of "Two classes have the same XML type name -- Use .. @XmlType namespace to assign different names to them."
[1127] PDF output: Handle images in headers/footers
[1126] MetafileEmfPart now extends BinaryPartAbstractImage, so EMF images can be added to the docx.
[1125] Add @XmlRootElement to CT_MarkupRange, indicating it is bookmarkEnd.
XSD: create new types CT_MoveFrom|ToRangeEnd, so elements moveFrom|ToRangeEnd don't get confused with bookmarkEnd. TODO: havn't run xjc on this new xsd.
[1121] Support for 4 SmartArt parts.
[1120] XML parts which we don't specifically know how to handle: load these as xml parts (previously they were loaded as binary parts).
[1107] When creating image part names in BPAI, use the generated relId as the image name.
[1102] Support for ActiveX parts.  Previously the Xml part was being represented as binary, and hence encoded in output.
[1098] Support image of type anchor, not just inline.
[1078] Support for WMF (but not EMF, yet) as SVG in HTML output.

pptx4j changes
--------------

[1088] SVG output: Paragraphs of large text in a box with a border, need a reduced top-margin.
[1087] Basic character formatting in SVG output
[1085] Convert line to SVG
[1083] JAXB representation of SVG 1.1

Version 2.3.0
=============

Release date
------------

17 Feb 2010

Contributors to this release
----------------------------

Jason Harrop
Holger Schlegel

Major Changes in Version 2.3.0
------------------------------

[1044] pptx4j
[1041] More complete DML, generated from TC45 1.0 final, using dml__ROOT.xsd 
[ 956] Basic implementation of styled tables in xsl fo.  More work needed on border conflict resolution.
[ 949] Table styles in HTML NG2 output; borders, shading, vertical alignment
[ 943] Added DocumentModel. DocumentModel is a list of SectionWrappers; a SectionWrapper has a HeaderFooterPolicy, PageDimensions and sectPr.
HeaderFooterPolicy moved to new package, as there will be 1 per SectionWrapper.
[ 923] introduce model/Property, to handle property conversion to CSS, and to XSL FO, more cleanly.
Adds conversion from CSS.      
[ 912] HtmlExporterNG2, which uses new StyleTree to take advantage of CSS cascade/priority rules to apply effective styles.

Other Changes
-------------

[1050] Renamed Package -> OpcPackage
[1039] Original dml-* from EcmaTC45 OOXML v1.0 final
[1036] Original pml-* from EcmaTC45 OOXML v1.0 final 
[1024] Footnotes in PDF via XSL FO.
[1015] Support for footnotes and endnotes in HTML.
[1008] added docs/Docx4j_GettingStarted
[1003] Remove dom4j stuff
[ 997] Basic support for list indentation in PDF via XSL FO
[ 990] Updated fop jar to include support for wingdings and other TrueType fonts with symbol character maps (patched with fop r891181 of 20091216)
[ 983] Support for adding linked (as opposed to embedded) images.
[ 979] Basic support in pdf via XSL FO, and HTML NG2, for bookmarks, hyperlink, symbols, w:pict.
[ 977] PDF via XSL FO: basic support for paragraph numbering
[ 975] JCR: Methods to get content as string (workaround for ALFCOM-3049)
[ 974] Handle w:t[@xml:space='preserve'] in NG2
[ 962] Example: CopyPart.
[ 962] New method setPartName(PartName newName), which is useful if you want to rename a part.
[ 960] Mechanism for passing state during the conversion process
[ 955] altChunk
[ 932] DocPropsCustomPart: When setting property, overwrite existing property with same name.
[ 930] Converter infrastructure can be used for incoming conversions (eg HTML table to w:tbl)
[ 928] Model interface: remove Converter arg from build method 
[ 925] Regenerated classes from wml.xsd, having added EG_MathContent back in to EG_RunLevelElements
[ 924] New method Context.getWmlObjectFactory(); we only need one instance of the ObjectFactory..
[ 922] new UnitsOfMeasurement class
[ 909] LoadFromZipFile can conserve memory by not loading the contents of binary parts
[ 905] Modify load method to also support loading a Flat OPC .xml file
[ 903] Bug fix in revised deepCopy method: use JAXBContext parameter properly

Version 2.2.2
=============

Release date
------------

17 Sept 2009

Contributors to this release
----------------------------

Jason Harrop
Holger Schlegel

Major Changes in Version 2.2.2
------------------------------

[888] Generate classes from shared-math.xsd

[885] JAXB representation for VML (eg as used when a document containing embedded images is 
      saved as docx from Word 2003).      

Other Changes
-------------

[895] There are no dom4j parts anymore.  Parts which aren't JAXB XML parts now extend new XmlPart, 
      which uses JAXP instead of dom4j.  The use of dom4j is deprecated, and all references to it
      will be removed in docx4j v3.

[894] Explicitly specify class loader when loading JAXBContext. Prevents versions of JBOSS from
      trying to use a different class loader.
      
[893] Replace deepCopy methods with Holger's contribution of 9 Sept.

[887] Apply Holger Schlegel's patch adding a generic parameter for the JaxbElement property.      

[886] SaveToJCR will create folders from path segments as required (at least for Alfresco; 
      for other implementations, TODO ensure '/' is not encoded!)  
      
[883] NamespacePrefixMappings stores the mappings in a single location, and is sufficient for xpath.      

Version 2.2.1
=============

Release date
------------

24 Aug 2009

Contributors to this release
----------------------------

Jason Harrop
Adam Schmideg

Major Changes
-------------

[869] NamespacePrefixMappers which work with Java 6 (ie if you don't have JAXB in your endorsed dir, 
      or can't (eg Java Web Start)).  

Other Changes (not exhaustive)
-------------

[871] Get rid of System.out.println (mostly).

[870] Avoid returning null DocumentFragment from getNumberXmlNode extension, since this causes 
      Xalan to produce a stack trace
      
[867] Use Java's xerces.internal instead of Crimson in CustomXmlDataStorageImpl  

[865] Don't get value of attributes when passing table contents to Converter.toNode; 
      Attributes on <w:tr w:rsidR="00E54D1F" w:rsidRPr="00A84EA1"> screws output.

[864] ImmutablePropertyResolver, contributed by Adam Schmideg.    

Version 2.2.0
=============

Release date
------------

28 July 2009

Contributors to this release
----------------------------

Serge Grachov
Jason Harrop
Adam Schmideg
Leigh

Major Changes
-------------

CustomXml applyBindings works (to proof of concept level)

Differencing improvements

Table model and Converter interface.  Use of this table model in HtmlExporterNG, 
to support merged cells. Contributed by Adam Schmideg. 

New class PropertyResolver [757], which works out the actual properties which apply to a paragraph or a run.

Header/footer support 

PDF via XSL FO or iText (in addition to existing support for via HTML)

"Next Generation" HTML Exporter, which only needs the main document part as input, and 
which takes advantage of docx4j's knowledge of the document (via extension functions) 
so that most of the logic is done in Java (as opposed to xslt).

Improvements to font handling/substitution (inc auto-detect option)

Image insertion convenience methods

Other Changes (not exhaustive)
-------------

[856] Start of work on NamespacePrefixMappers which work with Java 6 (ie if you don't have JAXB 
      in your endorsed dir, or can't (eg Java Web Start)). [Not finished until v2.2.1!]

[854] Remove ContentTypeManager interface; replace it with implementation.
      ContentTypeManager: change semantics of isContentTypeRegistered, so that it means 
      'is *default* content type registered'.
      
[847] Relativise file URLs for images, based on contribution by Leigh;
      Only relativise path if its not the tmpdir, because the tmp dir is used for pdf output      

[841] Updated createImgE10 extension function to point to class org.docx4j.model.images.WordXmlPicture.  
      Reported and fix suggested by Leigh
      
[816] Bug fix: Close image files properly; patch contributed by Serge Grachov

[815] Store/retrieve key/value pairs in sdtPr/tag

[808] Text extraction

[799] If no directory for saving images is specified, embed the image using a data: URI

[784] LoadFromZipFileNG, suports reading from an input stream

[776] Renamed package out.xmlPackage to out.FlatOpcXml
      Renamed XmlPackageCreator to FlatOpcXmlCreator
      
[771] Renamed XmlPackageImporter to FlatOpcXmlImporter

[697] Remove fop-fonts; to be replaced with a complete fop jar

[650] Convenience method to restart numbering

[633] Add a createImagePart signature which allows you to specify the source part of the image part's rel, 
      so that the image can be added to eg a header.
      
[625] Add an HtmlSetting 'conditionalComments' which defaults to turning these off in the style sheet, 
      since FlyingSaucer PDF Renderer renders <![if!supportMisalignedColumns]><![endif]> verbatim, and there 
      have been reports of Xerces SAX parser not liking these:

[607] Make it easy for a part to have its own NamespacePrefixMapper. (eg for relationships part, 
      we want the rels namespace to be the default namespace)

Version 2.1.0
=============

Release date
------------

11 Nov 2008

Contributors to this release
----------------------------

Jason Harrop
Manimala Kumar

Major Changes
-------------

Use docx 2 html XSLT from OpenXMLViewer (OpenXMLViewer XSLT 11089, as downloaded 9 Oct 2008), 
with support for numbering, image handling, hyperlinks  

Other Changes (not exhaustive)
-------------

[563] Specialised parts for some image types (rather than just treating them as BinaryPart).

[560] Create <img> element for E2.0 images

[559] Basic support for resolving a hyperlink by reference to the rels part, using an XSLT 
      extension function.
      
[558] Use docx 2 html XSLT from OpenXMLViewer.     

[547] getParts() method contributed by Manimala Kumar

[539] VBA parts

[532] RelationshipsPart is now a JAXB part.  

[529] Differencing improvements 

Version 2.0
=============

Release date
------------

21 July 2008

Major Changes
-------------

Support for Flat OPC XML file format

Binary doc import proof of concept (using POI)

Support for <w:drawing> element

Differencing
