<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" 
	xmlns:o="urn:schemas-microsoft-com:office:office" 
	xmlns:v="urn:schemas-microsoft-com:vml" 
	xmlns:WX="http://schemas.microsoft.com/office/word/2003/auxHint"
	xmlns:aml="http://schemas.microsoft.com/aml/2001/core"
	xmlns:w10="urn:schemas-microsoft-com:office:word"
	xmlns:pkg="http://schemas.microsoft.com/office/2006/xmlPackage"		
        xmlns:msxsl="urn:schemas-microsoft-com:xslt"
   	xmlns:ext="http://www.xmllab.net/wordml2html/ext"
	xmlns:java="http://xml.apache.org/xalan/java"
	version="1.0"
        exclude-result-prefixes="java msxsl ext w o v WX aml w10">	

	
<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="yes" />
	<!-- doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" -->

<!-- 

<p class="Normal-P"> <span class="Normal-H">blagh blagh</span></p>
<p class="Normal-P">&nbsp; blagh blagh <br /></p> 

should become

<w:p>
	<w:r>
	    <w:t></w:t>
	</w:r>
</w:p>


 -->

<xsl:template match="/ | html">
	
		<xsl:apply-templates select="//body" />

</xsl:template>


<xsl:template match="body">
	
	<w:sdtContent>
		<xsl:apply-templates/>
	</w:sdtContent>

</xsl:template>

<xsl:template match="br"/> <!-- ignore for now -->

<xsl:template match="p">
	<w:p>
		<xsl:apply-templates select="node()|@*"/>	
	</w:p>
</xsl:template>

<xsl:template match="span">
	<w:r>
		<w:t>
			<xsl:apply-templates/>	
		</w:t>
	</w:r>
</xsl:template>

<xsl:template match="text()">
	<xsl:choose>
		<xsl:when test="name(..)='span'">
			<xsl:value-of select="."/>
		</xsl:when>
		<xsl:when test="name(..)='p'">
			<w:r>
				<w:t>
					<xsl:value-of select="."/>
				</w:t>
			</w:r>
		</xsl:when>
		<xsl:otherwise>
			<xsl:comment>What to do with text '<xsl:value-of select="."/>' in <xsl:value-of select="name(..)"/> element?</xsl:comment>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>


<xsl:template match="@class[string(.)='Normal-P']">
	<!--  this is the default, so it can be ignored -->
</xsl:template>



</xsl:stylesheet>

