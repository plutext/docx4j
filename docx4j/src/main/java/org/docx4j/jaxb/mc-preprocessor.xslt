
<xsl:stylesheet 

	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	
	xmlns:java="http://xml.apache.org/xalan/java" 
	
	xmlns:o="urn:schemas-microsoft-com:office:office"
	xmlns:v="urn:schemas-microsoft-com:vml"
	xmlns:w10="urn:schemas-microsoft-com:office:word"
	
	xmlns:aml="http://schemas.microsoft.com/aml/2001/core"
	
	xmlns:WX="http://schemas.microsoft.com/office/word/2003/auxHint"
	
	xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
	xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math" 
	xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" 
	xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture"
	xmlns:pkg="http://schemas.microsoft.com/office/2006/xmlPackage"		    
	xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
	xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
	xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml" 
	xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
	
	xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml"
	xmlns:wp14="http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing" 
	xmlns:wpc="http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas" 
	xmlns:wpg="http://schemas.microsoft.com/office/word/2010/wordprocessingGroup" 
	xmlns:wpi="http://schemas.microsoft.com/office/word/2010/wordprocessingInk" 
	xmlns:wps="http://schemas.microsoft.com/office/word/2010/wordprocessingShape"
	 	
    version="1.0"
        exclude-result-prefixes="java w a o v WX aml w10 pkg wp pic">	
        
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
