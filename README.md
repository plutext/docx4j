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
- Document generation via variable, content control data binding, or MERGEFIELD
- CustomXML binding (with support for pictures, rich text, checkboxes, and OpenDoPE extensions for repeats & conditionals, and importing XHTML) 
- Export as HTML
- Export as PDF, choice of 3 strategies, see https://www.docx4java.org/blog/2020/09/office-pptxxlsxdocx-to-pdf-to-in-docx4j-8-2-3/ 
- Produce/consume Word 2007's xmlPackage (pkg) format
- Apply transforms, including common filters
- Font support (font substitution, and use of any fonts embedded in the document) 

docx4j-8
--------

This is docx4j for Java 8. Although in principle it would compile and run under Java 6, some of its
dependencies are Java 8 only.  So to run it under Java 6, you'd need to use the same version of the deps
which docx4j 6.x uses.

docx4j v8 is a multi-module Maven project.

To use docx4j v8, add the dep corresponding to the JAXB implementation you wish to use

* docx4j-JAXB-Internal (shipped in Oracle and OpenJDK v8)
* docx4j-JAXB-ReferenceImpl (you may need to respect the endorsed dir mechanism for the RI jars)
* docx4j-JAXB-MOXy

You should use one and only one of docx4j-JAXB-* 


docx4j for Java 11
------------------

See https://github.com/plutext/docx4j/tree/docx4j-parent-11.1.8

docx4j 11.1.x - for use with Java 11 or later - is available in Maven Central.

Being a JPMS modularised release, the jars contain module-info.class entries.

11.1.0 is compiled with Java 12, targeting Java 11. If you are not using Java 11, you should stick with docx4j 8.1.0.

Aside from the use of named modules / module path, the releases pretty much track v8. However, docx4j-export-fo is temporarily omitted since FOP 2.3's avalon dependencies are not JPMS-friendly. 


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
