# docx4j-export-FO

Export docx to PDF via XSL FO, using FOP

With docx4j 8, this maven module builds as an optional jar. (From 3.3.0 to 6.1.x, this was an optional separate project at https://github.com/plutext/docx4j-export-fo )

docx4j will use export-FO automatically if it finds it in your classpath.

## Alternatives

* documents4j: since 8.2.0, use Microsoft Word to do the conversion

* via-Microsoft-Graph: new in 8.2.3, use java-docx-to-pdf-using-Microsoft-Graph to do the conversion

For comparison of approaches, please see https://www.docx4java.org/blog/2020/09/office-pptxxlsxdocx-to-pdf-to-in-docx4j-8-2-3/