README
======

What is docx4j?
---------------

docx4j is an open source (Apache v2) library for creating, editing, and saving OpenXML "packages", including docx, pptx, and xslx. 

It uses JAXB to create the Java representation.

- Open existing docx/pptx/xlsx (from filesystem, SMB/CIFS, WebDAV using VFS)
- Create new docx
- Programmatically manipulate the docx document (of course)
- CustomXML binding (with OpenDoPE extensions for repeats & conditionals) 
- Export as HTML or PDF
- Diff/compare documents, paragraphs or sdt (content controls)
- Import a binary doc (uses Apache POI's HWPF)
- Produce/consume Word 2007's xmlPackage (pkg) format
- Save docx to filesystem as a docx (ie zipped), or to JCR (unzipped)
- Apply transforms, including common filters
- Font support (font substitution, and use of any fonts embedded in the document) 

How do I build docx4j?
----------------------

Get it from GitHub, at https://github.com/plutext/docx4j
For more details, see http://www.docx4java.org/blog/2012/05/docx4j-from-github-in-eclipse/

If you are working with the source code, please join the developer
mailing list:

        docx4j-dev-subscribe@docx4java.org

Where do I get a binary?
------------------------

http://www.docx4java.org/downloads.html

How do I get started?
------------------

See the Getting Started guide.

Where to get help?
------------------

http://www.docx4java.org/forums or StackOverflow (use tag 'docx4j')

To subscribe, send an email to docx4j-user-subscribe@docx4java.org

The forum is gatewayed to a mailing list, so you can interact via your email
client if you prefer.  To subscribe, send an email to docx4j-user-subscribe@docx4java.org


Legal Information
-----------------

docx4j is published under the Apache License version 2.0. For the license
text, please see the following files in the legals directory:
- LICENSE
- NOTICE
Legal information on libraries used by docx4j can be found in the 
"legals/NOTICE" file.
Here is a (TODO: non exhaustive?) list of files included in docx4j but not published under Apache
License version 2.0:

- DocX2Html.xslt (though docx2xhtmlNG2.xslt is our supported transform, not that) 
- src/diffx (ARTISTIC LICENCE)
- xsd/**