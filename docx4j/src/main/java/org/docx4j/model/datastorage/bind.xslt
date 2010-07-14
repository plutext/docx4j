
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

<xsl:param name="customXmlDataStorageParts"/> <!-- select="'passed in'"-->	

  <xsl:template match="/ | @*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="w:sdt">  
  	<xsl:choose>
  		<xsl:when test="w:sdtPr/w:dataBinding">
		    <xsl:copy>
		      <xsl:copy-of select="w:sdtPr"/>
	  		<!-- xsl:variable name="dummy" 
	  			select="java:org.docx4j.openpackaging.parts.CustomXmlDataStoragePart.log('Entering bind mode')"/-->
		      <xsl:apply-templates select="w:sdtContent" mode="bind">		      
		      	<xsl:with-param name="storeItemID"  select="string(w:sdtPr/w:dataBinding/@w:storeItemID)" />
	  			<xsl:with-param name="xpath"  select="string(w:sdtPr/w:dataBinding/@w:xpath)" />
  				<xsl:with-param name="prefixMappings"  select="string(w:sdtPr/w:dataBinding/@w:prefixMappings)" />
		      </xsl:apply-templates> 
		    </xsl:copy>  		  			
  		</xsl:when>
  		<xsl:otherwise>
		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()"/>
		    </xsl:copy>  		
  		</xsl:otherwise>  		
  	</xsl:choose>    
  </xsl:template>

  <xsl:template match="@*"  mode="bind">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="w:*" mode="bind">  
      	<xsl:param name="storeItemID" />
		<xsl:param name="xpath" />
		<xsl:param name="prefixMappings" />
  		<!-- >xsl:variable name="dummy" 
  			select="java:org.docx4j.openpackaging.parts.CustomXmlDataStoragePart.log('In w:* bind')"/-->
		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()" mode="bind">
		      	<xsl:with-param name="storeItemID"  select="$storeItemID" />
	  			<xsl:with-param name="xpath"  select="$xpath" />
  				<xsl:with-param name="prefixMappings"  select="$prefixMappings" />
		      </xsl:apply-templates> 
		    </xsl:copy>  		  			
  </xsl:template>
  
  <xsl:template match="w:r" mode="bind" priority="2">  
      	<xsl:param name="storeItemID" />
		<xsl:param name="xpath" />
		<xsl:param name="prefixMappings" />
  		<!-- >xsl:variable name="dummy" 
  			select="java:org.docx4j.openpackaging.parts.CustomXmlDataStoragePart.log('In w:r bind')"/-->

				<!-- TEMP: insert a new w:r, containing result -->
				<w:r>
					<w:t><xsl:value-of
						select="java:org.docx4j.openpackaging.parts.CustomXmlDataStoragePart.xpathGetString(
									$customXmlDataStorageParts,
									$storeItemID,
									$xpath,
									$prefixMappings)" /></w:t>
				</w:r>
<!-- INSTEAD OF
		<xsl:choose>
			<xsl:when test="w:rPr/w:rStyle/@w:val='Entry' or w:rPr/w:rStyle/@w:val='PlaceholderText'">
				<w:r>
					<w:rPr>
						<w:rStyle w:val="Entry"/>
						<xsl:copy-of select="w:rPr/*[not(self::w:rStyle)]"/>
					</w:rPr>
					<w:t><xsl:value-of
						select="java:org.docx4j.openpackaging.parts.CustomXmlDataStoragePart.xpathGetString(
									$customXmlDataStorageParts,
									$storeItemID,
									$xpath,
									$prefixMappings)" /></w:t>
				</w:r>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
		-->
  </xsl:template>
   
</xsl:stylesheet>
