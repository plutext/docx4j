docx4j has a three layered model of a docx file.

The lowest level is org.docx4j.openpackaging, which represents the docx package, 
and each of its constituent parts. 

Parts are generally subclasses of org.docx4j.openpackaging.parts.JaxbXmlPart

A JaxbXmlPart has a content tree:

	public Object getJaxbElement() {
		return jaxbElement;
	}

	public void setJaxbElement(Object jaxbElement) {
		this.jaxbElement = jaxbElement;
	}

The jaxb content tree is the second level of the three layered model.

Most parts (including MainDocumentPart, styles, headers/footers, comments, endnotes/footnotes) 
use org.docx4j.wml (WordprocessingML); wml references org.docx4j.dml (DrawingML) as necessary.

The properties parts use org.docx4j.docProps.

The highest level layer is this package (ie org.docx4j.model). 
This package builds on the lower two layers and does/should include functionality relating to:

- headers/footers
- list numbering
- images (TODO: refactor)

Often the question is whether to put functionality into this package or into one of the 
other two layers.

Placing it in the jaxb layer (ie in org.docx4j.wml, org.docx4j.dml, or org.docx4j.docProps)
is to be avoided, since these classes are (largely) generated automatically from the
schemas.

You can place the functionality in a particular part (ie in org.docx4j.openpackaging.parts.WordprocessingML)
if the functionality relates specifically to that part.  So for example, the list numbering stuff could
have gone into the ListNumberingPart (and probably would have, but for the fact that it comprises
several different classes).

However, functionality which applies to several parts (eg the header/footer stuff), is clearly better
placed here.

In general, classes in the highest level will hold references to objects in the lower 2 levels, but
not vice versa. (Although since there is no central/core object on the highest level, it may be
convenient to store references to some of these in the package or MainDocumentPart). 

Aside: typically, when working with docx4j, you use the lowest level to create/open/save a package,
to add/remove parts, and to access the jaxb content tree for a particular part.  You use the second and highest levels
to manipulate the document content.




  