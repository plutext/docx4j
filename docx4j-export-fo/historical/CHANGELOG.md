CHANGELOG
=========


Version 6.1.0
=============

Release date
------------

17 December 2018


Changes in Version 6.1.0
------------------------

Refactor text box code (since FOP 2 supports fo:float)

mc alternate content: select fallback

Null check invoking tcPr.getTextDirection()




Version 6.0.1
=============

Release date
------------

3 August 2018


Changes in Version 6.0.1
------------------------

Depends on docx4j 6.0.1 (since docx4j 6.0.0 should be avoided).



Version 6.0.0
=============

Release date
------------

22 July 2018


Changes in Version 6.0.0
------------------------

Docx4j 6.x will be the last series supporting Java 6.  
(docx4j 7.x, when released, will require Java 7+) 

This itself is a minor release (it is numbered 6.0.0 to align with docx4j,
which is a more significant release).  You can use export-fo 6.0.0 with
docx4j 6.0.x (and 3.3.x, though 6.0.x is recommended) 

Use FOP and XmlGraphics 2.3 

Take paragraph mark font size into account when setting font size on block. This is important, since it affects line spacing.

Support tcPr/textDirection (rotated text in table cell), though more work is 
required for FOP to position the text in the cell!


Version 3.3.6 (minor release)
===============

Release date
------------

7 October 2017


Notable Changes in Version 3.3.6
---------------------------------

Use FOP 2.2

Expose FOUserAgent, so user can set accessibility



Version 3.3.4 (minor release)
===============

Release date
------------

15 June 2017


Notable Changes in Version 3.3.4
---------------------------------

No changes, except pom uses docx4j 3.3.4


Compatibility notes
-------------------

Version number 3.3.4 assigned, to align with docx4j release number
of same date.

docx4j 3.3.4 or later recommended (though 3.3.1 or later ought to work)


Version 3.3.1
=============

Release date
------------

16 October 2016


Notable Changes in Version 3.3.1
---------------------------------

convert.out.fo.TableWriter match new overridden method sig: pass TableModelCell  instead of AbstractTableWriterModelCell into applyTableCellCustomAttributes

workaround for "The column-number or number of cells in the row overflows the number of fo:table-columns specified for the table.




Version 3.3.0
=============

Release date
------------

21 April 2016


Notable Changes in Version 3.3.0
---------------------------------

First release of export-FO as a separate GitHub project from docx4j

Uses FOP 2.1


