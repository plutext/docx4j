
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
    xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
    xmlns:o="urn:schemas-microsoft-com:office:office"
    xmlns:v="urn:schemas-microsoft-com:vml"
    xmlns:WX="http://schemas.microsoft.com/office/word/2003/auxHint"
    xmlns:aml="http://schemas.microsoft.com/aml/2001/core"
    xmlns:w10="urn:schemas-microsoft-com:office:word"
    xmlns:w15="http://schemas.microsoft.com/office/word/2012/wordml" 
	xmlns:pkg="http://schemas.microsoft.com/office/2006/xmlPackage"		    
	xmlns:java="http://xml.apache.org/xalan/java" 
	xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
	xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
	xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture"
	xmlns:xalan="http://xml.apache.org/xalan"
    version="1.0"
        exclude-result-prefixes="java w a o v WX aml w10 pkg wp pic">	
        

<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="yes" />

<xsl:param name="OpenDoPEIntegrityAfterBinding"/> <!-- select="'passed in'"-->	

  <xsl:template match="/ | @*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>


  <xsl:template match="w:sdt[count(w:sdtPr/w:text)=1]">
  
  	<xsl:choose>
  		<xsl:when test="count(w:sdtContent//w:bookmarkStart) &gt; 0">
  		
  			<!-- <xsl:variable name="log1" select="java:org.docx4j.model.datastorage.OpenDoPEIntegrityAfterBinding.log(
										'detected w:bookmarkStart')" />  -->
  		
  			<!-- move bookmark start outside (bookmark end is ok, tested Word 2016) -->
  			<xsl:copy-of select="w:sdtContent//w:bookmarkStart"/> <!--  no need to add w:displacedByCustomXml="next" -->
  			<xsl:apply-templates select="." mode="skipBookmarkStart" />
  		</xsl:when>
  		<xsl:otherwise>
  			<!--  OK -->
		    <xsl:copy>
				<xsl:apply-templates select="@*|node()"/>  	
		    </xsl:copy>  		
  		</xsl:otherwise>  	
  	</xsl:choose>
  
  </xsl:template>  

  <xsl:template match="/ | @*|node()" mode="skipBookmarkStart">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="skipBookmarkStart"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="w:bookmarkStart" mode="skipBookmarkStart" />
   
</xsl:stylesheet>
