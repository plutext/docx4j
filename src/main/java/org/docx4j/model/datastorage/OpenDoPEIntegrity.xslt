
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
  
    <!--  remove w15 repeats, so w:sdt structure encountered in bind.xslt is as expected -->
    <xsl:template match="w:sdt[w:sdtPr/w15:repeatingSection]" priority="5">
        <!--  keep contents only -->
        <xsl:apply-templates select="w:sdtContent/*"/>
    </xsl:template>

    <xsl:template match="w:sdt[w:sdtPr/w15:repeatingSectionItem]" priority="5">
        <!--  keep contents only -->
        <xsl:apply-templates select="w:sdtContent/*"/>
    </xsl:template>
  
  <!-- Integrity: w:tr/w:sdt must contain w:tc, and w:tc must be non-empty
  
       this matches an sdt surrounding a tc (with or without nested tables)
  
   -->
  <xsl:template match="w:sdt[count(ancestor::w:tr)=count(ancestor::w:tbl) and count(ancestor::w:tc)=(count(ancestor::w:tbl)-1) ]">
  
  <!--  apply fix if necessary to deepest first -->
  	<xsl:variable name="results">
		<xsl:apply-templates select="@*|node()"/>  	
  	</xsl:variable>
  
  	<xsl:choose>
  	
  		<!--  the tc must contain block content (eg w:p or a nested table, and somewhere in that
  			  there ought to be a w:p -->
  		<xsl:when test="count(xalan:nodeset($results)//w:p)=0"> <!--  still need to do something, in spite of fixing the deepest already -->
  			<w:sdt>
  				<xsl:copy-of select="w:sdtPr"/>
  				<w:sdtContent>
  					<w:tc> 
						<xsl:copy-of select="./descendant::w:tcPr[1]" />					  					
  						<w:p/>
  					</w:tc>
  				</w:sdtContent>
  			</w:sdt>
  		</xsl:when>
  		<xsl:otherwise>
		    <xsl:copy>
		      <xsl:copy-of select="$results"/>
		    </xsl:copy>  		
  		</xsl:otherwise>
  	</xsl:choose>
  
  </xsl:template>  
  
  
  
  <!-- Integrity: w:tc/w:sdt must be non-empty  
  
       this matches a block level sdt inside a tc (whether or not the table is nested)
       
       We don't attempt to fix the case of a w:tc containing nothing, since OpenDopE shouldn't introduce that. 
       
       This template populates a w:sdt, even where that would not be strictly necessary (ie there is sibling content)		
  
   -->
  <xsl:template match="w:sdt[count(ancestor::w:tc)=count(ancestor::w:tbl) and count(ancestor::w:p)=0 ]">

  <!--  apply fix if necessary to deepest first -->
  	<xsl:variable name="results">
		<xsl:apply-templates select="@*|node()"/>  	
  	</xsl:variable>

    <xsl:choose>
  	
  		<xsl:when test="count(xalan:nodeset($results)//w:p)=0"> <!--  still need to do something, in spite of fixing the deepest already -->
  			<w:sdt>
  				<xsl:copy-of select="w:sdtPr"/>
  				<w:sdtContent>
  						<w:p/>
  				</w:sdtContent>
  			</w:sdt>
  		</xsl:when>
  		<xsl:otherwise>
		    <xsl:copy>
		      <xsl:copy-of select="$results"/>
		    </xsl:copy>  		
  		</xsl:otherwise>
  	</xsl:choose>
  
  </xsl:template>  
   
</xsl:stylesheet>
