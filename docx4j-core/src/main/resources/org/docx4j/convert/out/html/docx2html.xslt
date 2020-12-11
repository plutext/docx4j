
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">	

   <!-- Use this method="html" if you need IE9 to display quotes properly (it
   		still doesn't use UTF 8 though!).  -->

<xsl:output method="html" encoding="utf-8" omit-xml-declaration="no" indent="no" 
doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
      doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>
        
<!--  Note, this output method injects:

		<META http-equiv="Content-Type" content="text/html; charset=utf-8">
		
		(with that tag not closed)
 -->
 

	<xsl:include href="docx2xhtml-core.xslt" />
   
</xsl:stylesheet>
