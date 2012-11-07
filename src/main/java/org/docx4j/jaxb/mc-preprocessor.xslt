
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"

	xmlns:java="http://xml.apache.org/xalan/java"
	xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006"

	version="1.0" exclude-result-prefixes="java">	
        
<!--  This is a mc:AlternateContent pre-processor.
      It selects the mc:Fallback content, which 
      docx4j 2.7.0 ought to be able to handle.  
      
      See MainDocumentPart's unmarshall method 
      for an example of how it is invoked. -->


<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="yes" />

  <xsl:template match="/ | @*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mc:AlternateContent">  
  
	<xsl:variable name="dummy" 
		select="java:org.docx4j.utils.XSLTUtils.logWarn('Found some mc:AlternateContent')" />
		
  	<xsl:choose>
  		<xsl:when test="mc:Fallback">
  		
  			<xsl:variable name="message" 
  				select="concat('Selecting ', name(mc:Fallback/*[1]) )" />  			
			<xsl:variable name="logging" 
				select="java:org.docx4j.utils.XSLTUtils.logWarn($message)" />
				
  			<xsl:copy-of select="mc:Fallback/*"/>
  			
  		</xsl:when>
  		<xsl:otherwise> 
			<xsl:variable name="logging" 
				select="java:org.docx4j.utils.XSLTUtils.logWarn('Missing mc:Fallback!  What to do?')" />
			<!--  Hope for the best.. -->
  			<xsl:copy-of select="mc:Choice[1]/*"/>
  		</xsl:otherwise>  		
  	</xsl:choose>    
  </xsl:template>

   
</xsl:stylesheet>
