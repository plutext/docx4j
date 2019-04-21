
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">	
        
    
  <xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="no" 
		doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
      	doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>
       
<!--  indent="no" gives a better result for things like subscripts, because it stops
      the user-agent from replacing a carriage return in the HTML with a space in the output. -->
      
   <!-- Known issue: IE9 doesn't display quotes properly (it
   		still doesn't use UTF 8 though!). 
   		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
   		doesn't help.
   		
   		If this is important to you, you need to use html output method,
   		which is available via docx2html.xslt -->
      
	<xsl:include href="docx2xhtml-core.xslt" />
   
</xsl:stylesheet>
