
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
<xsl:param name="wmlPackage"/> <!-- select="'passed in'"-->	
<xsl:param name="sourcePart"/> <!-- select="'passed in'"-->	

  <xsl:template match="/ | @*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="w:sdt">  
  	<xsl:choose>
  		<xsl:when test="w:sdtPr/w:dataBinding and not(w:sdtPr/w:richText) and not(w:sdtPr/w:docPartGallery)">
  			<!--  honour w:dataBinding -->
			<xsl:copy>
			     <xsl:apply-templates select="w:sdtPr"/>
			     
			     <xsl:if test="w:stdEndPr">
			     	<xsl:copy-of select="w:sdtEndPr"/>
		     	</xsl:if>
			     
			     <w:sdtContent>
			     	<xsl:variable name="multiLine" select="w:sdtPr/w:text/@w:multiLine='1' or w:sdtPr/w:text/@w:multiLine='true' or w:sdtPr/w:text/@w:multiLine='yes'" /> 
			     	
				  	<xsl:choose>
				  		<xsl:when test="w:sdtPr/w:picture">
							<xsl:copy-of
							select="java:org.docx4j.model.datastorage.BindingHandler.xpathInjectImage(
										$wmlPackage,
										$sourcePart,
										$customXmlDataStorageParts,
										string(w:sdtPr/w:dataBinding/@w:storeItemID),
										string(w:sdtPr/w:dataBinding/@w:xpath),
										string(w:sdtPr/w:dataBinding/@w:prefixMappings),
										local-name(..),
										string(w:sdtContent//wp:extent[1]/@cx), 
										string(w:sdtContent//wp:extent[1]/@cy))" />
				  		</xsl:when>
				  		<xsl:when test="w:sdtContent/w:tbl">
				  			<w:tbl>
				  				<xsl:copy-of select="w:sdtContent/w:tbl/w:tblPr"/>
				  				<xsl:copy-of select="w:sdtContent/w:tbl/w:tblGrid"/>
					  			<w:tr>
					  				<xsl:copy-of select="w:sdtContent/w:tbl/w:tr/w:trPr"/>
						  			<w:tc>
						  				<xsl:copy-of select="w:sdtContent/w:tbl/w:tr/w:trPr/w:tc/w:tcPr"/>
							  			<w:p>
							  				<xsl:copy-of select="w:sdtContent/w:tbl/w:tr/w:tc/w:p/w:pPr"/>
							  				
											<xsl:copy-of
											select="java:org.docx4j.model.datastorage.BindingHandler.xpathGenerateRuns(
														$wmlPackage,
														$customXmlDataStorageParts,
														string(w:sdtPr/w:dataBinding/@w:storeItemID),
														string(w:sdtPr/w:dataBinding/@w:xpath),
														string(w:sdtPr/w:dataBinding/@w:prefixMappings),
														w:sdtPr/w:rPr,
														$multiLine )" />
										</w:p>
									</w:tc>
								</w:tr>
							</w:tbl>
				  		</xsl:when>				  		
				  		<xsl:when test="w:sdtContent/w:tr">
				  			<w:tr>
				  				<xsl:copy-of select="w:sdtContent/w:tr/w:trPr"/>
					  			<w:tc>
					  				<xsl:copy-of select="w:sdtContent/w:tr/w:trPr/w:tc/w:tcPr"/>
						  			<w:p>
						  				<xsl:copy-of select="w:sdtContent/w:tr/w:tc/w:p/w:pPr"/>
						  				
										<xsl:copy-of
										select="java:org.docx4j.model.datastorage.BindingHandler.xpathGenerateRuns(
													$wmlPackage,
													$customXmlDataStorageParts,
													string(w:sdtPr/w:dataBinding/@w:storeItemID),
													string(w:sdtPr/w:dataBinding/@w:xpath),
													string(w:sdtPr/w:dataBinding/@w:prefixMappings),
													w:sdtPr/w:rPr,
													$multiLine )" />
									</w:p>
								</w:tc>
							</w:tr>
				  		</xsl:when>				  		
				  		<xsl:when test="w:sdtContent/w:tc">
				  			<w:tc>
				  				<!--  preserve existing w:tcPr -->
				  				<xsl:copy-of select="w:sdtContent/w:tc/w:tcPr"/>
					  			<w:p>
					  				<!--  preserve existing w:pPr -->
					  				<xsl:copy-of select="w:sdtContent/w:tc/w:p/w:pPr"/>
					  				
					  				<!--  create runs -->
									<xsl:copy-of
									select="java:org.docx4j.model.datastorage.BindingHandler.xpathGenerateRuns(
												$wmlPackage,
												$customXmlDataStorageParts,
												string(w:sdtPr/w:dataBinding/@w:storeItemID),
												string(w:sdtPr/w:dataBinding/@w:xpath),
												string(w:sdtPr/w:dataBinding/@w:prefixMappings),
												w:sdtPr/w:rPr,
												$multiLine )" />
								</w:p>
							</w:tc>
				  		</xsl:when>				  		
				  		<xsl:when test="w:sdtContent/w:p">
				  			<w:p>
				  				<!--  preserve existing w:pPr -->
				  				<xsl:copy-of select="w:sdtContent/w:p/w:pPr"/>
				  				
				  				<!--  create runs -->
								<xsl:copy-of
								select="java:org.docx4j.model.datastorage.BindingHandler.xpathGenerateRuns(
											$wmlPackage,
											$customXmlDataStorageParts,
											string(w:sdtPr/w:dataBinding/@w:storeItemID),
											string(w:sdtPr/w:dataBinding/@w:xpath),
											string(w:sdtPr/w:dataBinding/@w:prefixMappings),
											w:sdtPr/w:rPr,
											$multiLine )" />
							</w:p>
				  		</xsl:when>
				  		<xsl:otherwise>  <!--  run level --> 
				  			<!--  can we insert a fragment ie multiple runs? --> 		
							<xsl:copy-of
							select="java:org.docx4j.model.datastorage.BindingHandler.xpathGenerateRuns(
										$wmlPackage,
										$customXmlDataStorageParts,
										string(w:sdtPr/w:dataBinding/@w:storeItemID),
										string(w:sdtPr/w:dataBinding/@w:xpath),
										string(w:sdtPr/w:dataBinding/@w:prefixMappings),
										w:sdtPr/w:rPr,
										$multiLine )" />
				  		</xsl:otherwise>  		
				  	</xsl:choose>    
			     </w:sdtContent>
			     
			</xsl:copy>  		  			
  		</xsl:when>
  		<xsl:otherwise> <!--  no w:dataBinding, or one spec says to ignore -->  		
		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()"/>
		    </xsl:copy>  		
  		</xsl:otherwise>  		
  	</xsl:choose>    
  </xsl:template>


  <!-- Remove these, so missing data does not result
       in Click here to enter text in Word -->
  <xsl:template match="w:placeholder"/>  

   
</xsl:stylesheet>
