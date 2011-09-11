
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
    xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
    xmlns:o="urn:schemas-microsoft-com:office:office"
    xmlns:v="urn:schemas-microsoft-com:vml"
    xmlns:WX="http://schemas.microsoft.com/office/word/2003/auxHint"
    xmlns:aml="http://schemas.microsoft.com/aml/2001/core"
    xmlns:w10="urn:schemas-microsoft-com:office:word"
	xmlns:pkg="http://schemas.microsoft.com/office/2006/xmlPackage"		    
	xmlns:java="http://xml.apache.org/xalan/java" 
	xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
	xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
	xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture"
    version="1.0"
        exclude-result-prefixes="java w a o v WX aml w10 pkg wp pic">	
        

<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="yes" />

<xsl:param name="OpenDoPEIntegrity"/> <!-- select="'passed in'"-->	

  <xsl:template match="/ | @*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="w:commentRangeStart">  
  	<xsl:choose>
  		<xsl:when test="java:org.docx4j.model.datastorage.OpenDoPEIntegrity.encountered(
										$OpenDoPEIntegrity,'commentRangeStart',
										string(@w:id) )" />
			<!--  Drop it -->										
  		<xsl:otherwise>   		
		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()"/>
		    </xsl:copy>  		
  		</xsl:otherwise>  		
  	</xsl:choose>    
  </xsl:template>

  <xsl:template match="w:commentRangeEnd">  
  	<xsl:choose>
  		<xsl:when test="java:org.docx4j.model.datastorage.OpenDoPEIntegrity.encountered(
										$OpenDoPEIntegrity,'commentRangeEnd',
										string(@w:id) )" />
			<!--  Drop it -->										
  		<xsl:otherwise>   		
		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()"/>
		    </xsl:copy>  		
  		</xsl:otherwise>  		
  	</xsl:choose>    
  </xsl:template>

  <xsl:template match="w:commentReference">  
  	<xsl:choose>
  		<xsl:when test="java:org.docx4j.model.datastorage.OpenDoPEIntegrity.encountered(
										$OpenDoPEIntegrity,'commentReference',
										string(@w:id) )" />
			<!--  Drop it -->										
  		<xsl:otherwise>   		
		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()"/>
		    </xsl:copy>  		
  		</xsl:otherwise>  		
  	</xsl:choose>    
  </xsl:template>

  <xsl:template match="w:footnoteReference">  
  	<xsl:choose>
  		<xsl:when test="java:org.docx4j.model.datastorage.OpenDoPEIntegrity.encountered(
										$OpenDoPEIntegrity,'footnoteReference',
										string(@w:id) )" />
			<!--  Drop it -->										
  		<xsl:otherwise>   		
		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()"/>
		    </xsl:copy>  		
  		</xsl:otherwise>  		
  	</xsl:choose>    
  </xsl:template>

  <xsl:template match="w:endnoteReference">  
  	<xsl:choose>
  		<xsl:when test="java:org.docx4j.model.datastorage.OpenDoPEIntegrity.encountered(
										$OpenDoPEIntegrity,'endnoteReference',
										string(@w:id) )" />
			<!--  Drop it -->										
  		<xsl:otherwise>   		
		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()"/>
		    </xsl:copy>  		
  		</xsl:otherwise>  		
  	</xsl:choose>    
  </xsl:template>
   
</xsl:stylesheet>
