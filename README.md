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

Where do I get it?
------------------

http://www.docx4java.org/downloads.html

How do I get started?
------------------

See the Getting Started guide.

Where to get help?
------------------

http://www.docx4java.org/forums or StackOverflow (use tag 'docx4j')

How do I build docx4j?
----------------------

Get it from GitHub, at https://github.com/plutext/docx4j

For more details, see http://www.docx4java.org/blog/2012/05/docx4j-from-github-in-eclipse/

Social contract
---------------

docx4j is currently available under the Apache Software license (v2).
This gives you freedom to do pretty much anything you like with it.
It also means you don't have to pay for it (there is no incentive to take up a commercial license, so we don't offer one).

The quid pro quo is that if docx4j helps you out, you should "give something back",
by way of code, community support, by "spreading the word" (promotion), or by buying commerical support. Your choice.

If you choose promotion, your options include emailing a testimonial we can put on our website
(preferably with your company name, but without is worthwhile as well),
a blog post, a tweet, or a helpful (non-spammy) comment in an online forum.

Your promotion/support will help grow the docx4j community and thus its strength, to the benefit of all. 

Legal Information
-----------------

docx4j is published under the Apache License version 2.0.
Please see the LICENSE file for the license text.

docx4j includes a number of subcomponents with separate copyright notices and license terms.
Your use of the code for the these subcomponents is subject to their copyright notices and license terms.
See the NOTICE file in this directory for further information.

Here is a (TODO: non exhaustive?) list of files included in docx4j but not published under Apache License version 2.0:

- DocX2Html.xslt (though docx2xhtmlNG2.xslt is our supported transform, not that) 
- src/diffx (ARTISTIC LICENCE)
- xsd/**