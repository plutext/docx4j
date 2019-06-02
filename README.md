README
======

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.docx4j/docx4j/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.docx4j/docx4j)

What is docx4j?
---------------

docx4j is an open source (Apache v2) library for creating, editing, and saving OpenXML "packages", including docx, pptx, and xslx. 

It uses JAXB to create the Java representation.

- Open existing docx/pptx/xlsx 
- Create new docx/pptx/xlsx 
- Programmatically manipulate docx/pptx/xlsx (anything the file format allows)
- CustomXML binding (with support for pictures, rich text, checkboxes, and OpenDoPE extensions for repeats & conditionals, and importing XHTML) 
- Export as HTML
- Export as PDF (using Plutext's PDF Converter, or use docx4j-export-FO project)
- Produce/consume Word 2007's xmlPackage (pkg) format
- Apply transforms, including common filters
- Font support (font substitution, and use of any fonts embedded in the document) 

docx4j-11.1.0
------------

11.1.0 is our first JPMS modularised release (ie the jars contain module-info.class entries).

To use docx4j v11., add the dep corresponding to the JAXB implementation you wish to use

* docx4j-JAXB-ReferenceImpl (you may need to respect the endorsed dir mechanism for the RI jars)
* docx4j-JAXB-MOXy

You should use one and only one of docx4j-JAXB-* 

See CHANGELOG.md for our module names.


How do I build docx4j?
----------------------

Get it from GitHub, at https://github.com/plutext/docx4j

```
mvn clean
mvn install
```

Some of the tests might fail on Windows.  For now, you could skip them: `mvn install -DskipTests`  

For more details, see http://www.docx4java.org/blog/2015/06/docx4j-from-github-in-eclipse-5-years-on/

If you are working with the source code, please join the developer
mailing list:

        docx4j-dev-subscribe@docx4java.org


Where do I get a binary?
------------------------

http://www.docx4java.org/downloads.html

How do I get started?
------------------

See the Getting Started guide:  https://github.com/plutext/docx4j/tree/master/docs

and the Cheat Sheet:  http://www.docx4java.org/blog/2013/05/docx4j-in-a-single-page/

And see the sample code:  https://github.com/plutext/docx4j/tree/master/src/samples

You'll probably want the Helper AddIn to generate code:  http://www.docx4java.org/blog/2016/05/docx4j-helper-word-addin-new-version-v3-3-0/



Where to get help?
------------------

http://www.docx4java.org/forums or StackOverflow (use tag 'docx4j')

Please post to one or the other, not both


Legal Information
-----------------

docx4j is published under the Apache License version 2.0. For the license
text, please see the following files in the legals directory:
- LICENSE
- NOTICE
Legal information on libraries used by docx4j can be found in the 
"legals/NOTICE" file.
