==============================================================================
  DOCX4J  -  README
==============================================================================

Contents of this file:
 - What is docx4j?
 - Where do I get it?
 - How do I get started?
 - Where to get help?
 - How do I build docx4j?
 - Legal Information
 - Release Notes

==============================================================================


What is docx4j?
---------------

docx4j is an open source (Apache v2) library for creating, editing, and saving docx "packages". 

It uses JAXB to create the Java representation.

- Open existing docx (from filesystem, SMB/CIFS, WebDAV using VFS)
- Create new docx
- Programmatically manipulate the docx document (of course)
- Template substitution; CustomXML binding
- Import a binary doc (uses Apache POI's HWPF)
- Produce/consume Word 2007's xmlPackage (pkg) format
- Save docx to filesystem as a docx (ie zipped), or to JCR (unzipped)
- Apply transforms, including common filters
- Export as HTML or PDF
- Diff/compare documents, paragraphs or sdt (content controls)
- Font support (font substitution, and use of any fonts embedded in the document) 


Where do I get it?
------------------

http://dev.plutext.org/downloads.html


How do I get started?
------------------

See the Getting Started guide.


Where to get help?
------------------

http://dev.plutext.org/forums


How do I build docx4j?
----------------------

http://dev.plutext.org/trac/docx4j/wiki/DevEnv


Legal Information
-----------------

docx4j is published under the Apache License version 2.0. For the license
text, please see the following files in the legals directory:
- LICENSE
- NOTICE

Legal information on libraries used by docx4j can be found in the 
"legals/NOTCIE" file.

Here is a (TODO: non exhaustive?) list of files included in docx4j but not published under Apache
License version 2.0:

- DocX2Html.xslt (though docx2xhtmlNG2.xslt is our supported transform, not that) 
- src/diffx (ARTISTIC LICENCE)
- xsd/** 



==============================================================================
  RELEASE NOTES
==============================================================================

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



