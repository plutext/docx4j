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

See the sample code at http://dev.plutext.org/trac/docx4j/browser/trunk/docx4j/src/main/java/org/docx4j/samples


Where to get help?
------------------

http://dev.plutext.org/forums


How do I build docx4j?
----------------------

http://dev.plutext.org/trac/docx4j/wiki/DevEnv


Legal Information
-----------------

docx4j is published under the Apache License version 2.0. For the license
text, please see the following files:
- LICENSE
- NOTICE

Legal information on libraries used by docx4j can be found in the 
"lib/README.txt" file.

Here is a (TODO: non exhaustive?) list of files included in docx4j but not published under Apache
License version 2.0:

- DocX2Html.xslt
- src/diffx (ARTISTIC LICENCE)
- xsd/** 



==============================================================================
  RELEASE NOTES
==============================================================================




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



