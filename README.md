README
======


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


docx4j for JAXB 3.0 and Java 11+
--------------------------------

docx4j v11.4.5 uses Jakarta XML Binding API 3.0, as opposed to JAXB 2.x used in earlier versions (which import javax.xml.bind.*).  Since this release uses jakarta.xml.bind, rather than javax.xml.bind, if you have existing code which imports javax.xml.bind, you'll need to search/replace across your code base, replacing javax.xml.bind with jakarta.xml.bind. You'll also need to replace your JAXB jars (which Maven will do for you automatically; otherwise get them from the relevant zip file).

Being a JPMS modularised release, the jars also contain module-info.class entries.

To use it, add the dep corresponding to the JAXB implementation you wish to use

* [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.docx4j/docx4j-JAXB-ReferenceImpl/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.docx4j/docx4j-JAXB-ReferenceImpl)
 docx4j-JAXB-ReferenceImpl
* [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.docx4j/docx4j-JAXB-MOXy/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.docx4j/docx4j-JAXB-MOXy)
 docx4j-JAXB-MOXy
 

docx4j-8
--------

This is docx4j for Java 8. Although in principle it would compile and run under Java 6, some of its
dependencies are Java 8 only.  So to run it under Java 6, you'd need to use the same version of the deps
which docx4j 6.x uses.

docx4j v8 is a multi-module Maven project.

To use docx4j v8, add the dep corresponding to the JAXB implementation you wish to use

* [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.docx4j/docx4j-JAXB-Internal/badge.svg?gav=true)](https://maven-badges.herokuapp.com/maven-central/org.docx4j/docx4j-JAXB-Internal?gav=true)
 docx4j-JAXB-Internal (shipped in Oracle and OpenJDK v8)
* [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.docx4j/docx4j-JAXB-ReferenceImpl/badge.svg?gav=true)](https://maven-badges.herokuapp.com/maven-central/org.docx4j/docx4j-JAXB-ReferenceImpl?gav=true)
 docx4j-JAXB-ReferenceImpl (you may need to respect the endorsed dir mechanism for the RI jars)
* [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.docx4j/docx4j-JAXB-MOXy/badge.svg?gav=true)](https://maven-badges.herokuapp.com/maven-central/org.docx4j/docx4j-JAXB-MOXy?gav=true)
 docx4j-JAXB-MOXy

You should use one and only one of docx4j-JAXB-* 

 
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
