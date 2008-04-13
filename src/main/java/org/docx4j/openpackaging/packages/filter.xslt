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

<!-- One line for each of the things in FilterSettings --> 		
<xsl:param name="removeProofErrors"/> <!-- select="'passed in'"-->	
<xsl:param name="removeContentControls"/>
<xsl:param name="removeRsids"/>	
	
		
<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="w:proofErr">
	
	<xsl:choose>
		<xsl:when test="$removeProofErrors=true()">
			<!-- ignore it -->
		</xsl:when>
		<xsl:otherwise>
		  <xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		  </xsl:copy>
		</xsl:otherwise>
	</xsl:choose>
	
</xsl:template>	

<xsl:template match="w:sdt">
	
	<xsl:choose>
		<xsl:when test="$removeContentControls=true()">
			<xsl:apply-templates select="w:sdtContent/*"/>
		</xsl:when>
		<xsl:otherwise>
		  <xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		  </xsl:copy>
		</xsl:otherwise>
	</xsl:choose>
	
</xsl:template>	
	

<xsl:template match="@w:rsidRPr | @w:rsidDel | @w:rsidR | @w:rsidSect | @w:rsidTr | @w:rsidP | @w:rsidRDefault | w:rsids | w:rsidRoot | w:rsid  ">
	<!-- rsids contains rsidRoot and rsid -->
	
	<xsl:choose>
		<xsl:when test="$removeRsids=true()">
			<!-- ignore it -->
		</xsl:when>
		<xsl:otherwise>
		  <xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		  </xsl:copy>
		</xsl:otherwise>
	</xsl:choose>
	
</xsl:template>	
	
		
	
		
<!--	
<xsl:template match="pkg:part[pkg:xmlData/w:document]">
   <pkg:part>
        <pkg:xmlData>	   
<w:document>
                <w:body>
                    <w:p>
                        <w:r>
                            <w:t>Hello World!
								
								<xsl:value-of select="$removeProofErrors"/>
							
								<xsl:if test="$removeProofErrors=true()">
									
									Soon, we'll remove proof errors :)
									
								</xsl:if>

								<xsl:if test="$removeProofErrors=false()">
									
									Who would want to get rid of such things?
									
								</xsl:if>

							</w:t>
                        </w:r>
                    </w:p>
                </w:body>
            </w:document>	
        </pkg:xmlData>
    </pkg:part>	 
</xsl:template>
-->



</xsl:stylesheet>

