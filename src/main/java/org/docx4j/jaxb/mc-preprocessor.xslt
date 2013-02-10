
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"

	xmlns:java="http://xml.apache.org/xalan/java"
	xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006"
	
	xmlns:wordml2010="http://schemas.microsoft.com/office/word/2010/wordml"
	xmlns:wordml201011="http://schemas.microsoft.com/office/word/2010/11/wordml"

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
  	<!-- 
  		<xsl:when test="mc:Choice[@Requires='v']">
  		
  			<xsl:variable name="message" 
  				select="string('Selecting mc:Choice[@Requires=v]')" />  			
			<xsl:variable name="logging" 
				select="java:org.docx4j.utils.XSLTUtils.logWarn($message)" />
				
  			<xsl:copy-of select="mc:Choice[@Requires='v']/*"/>

  		</xsl:when>   -->
  		<xsl:when test="mc:Fallback">
  		
  			<xsl:variable name="message" 
  				select="concat('Selecting ', name(mc:Fallback/*[1]) )" />  			
			<xsl:variable name="logging" 
				select="java:org.docx4j.utils.XSLTUtils.logWarn($message)" />
				
  			<xsl:copy-of select="mc:Fallback/*"/>
  			
  		</xsl:when>
  		<xsl:otherwise> 
			<xsl:variable name="logging" 
				select="java:org.docx4j.utils.XSLTUtils.logWarn('Missing mc:Fallback!  Dropping the mc:AlternateContent entirely.')" />
			<!--   
  			    <xsl:copy-of select="mc:Choice[1]/*"/>
  			-->
  		</xsl:otherwise>  		
  	</xsl:choose>    
  </xsl:template>

  <!--  Most JAXB implementations don't signal additional attributes as errors. -->
  <xsl:template match="@wordml2010:*" />  
  <xsl:template match="@wordml201011:*" />  
   
</xsl:stylesheet>
