
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"

	xmlns:java="http://xml.apache.org/xalan/java"
	xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006"
	
 	xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
 		
	xmlns:purls="http://purl.oclc.org/ooxml/spreadsheetml/main" 
	xmlns:s="http://schemas.openxmlformats.org/spreadsheetml/2006/main"

 	xmlns:purla="http://purl.oclc.org/ooxml/drawingml/main"
	xmlns:purlpic="http://purl.oclc.org/ooxml/drawingml/picture"
 
	xmlns:purlcp="http://purl.oclc.org/ooxml/officeDocument/customProperties"
	xmlns:purlep="http://purl.oclc.org/ooxml/officeDocument/extendedProperties"
	xmlns:ep="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties"
		
	version="1.0" exclude-result-prefixes="java">	
      
<!--    
	
  This preprocessor does 2 things:
				
		1. it can import Strict docx files
	
		2. it could correct common validity issues with docx from various sources (eg Google Docs)
	
	For mc:AlternateContent, it could also select the mc:Fallback content.  But it doesn't right now.
	 
-->

<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="yes" />

	<xsl:variable name="strict-prefix" select="'http://purl.oclc.org/ooxml/'"/>
    <xsl:variable name="usual-prefix" select="'http://schemas.openxmlformats.org/'"/>
    <xsl:variable name="add2006" select="'2006'"/>

    <xsl:template name="calculate-new-namespace">
        <xsl:param name="old-uri"/>
        <xsl:variable name="uri-stripped-prefix" 
            select="substring-after($old-uri, $strict-prefix)"/>
            
        <xsl:variable name="X" select="substring-before($uri-stripped-prefix, 
                                     substring-after($uri-stripped-prefix, '/'))"/>
                                     
        <xsl:variable name="Y" select="substring-after($uri-stripped-prefix, 
                                    substring-before($uri-stripped-prefix, '/'))"/>

        <xsl:value-of select="concat($usual-prefix, $X, $add2006, $Y)"/>
    </xsl:template>	

	<xsl:template match="node()|/">
		<xsl:param name="old-uri" select="namespace-uri()"/>
	
		<xsl:choose>
		
			<xsl:when test="self::*|/"><!-- elements -->
				
				<xsl:choose>
		
					<xsl:when test="starts-with(namespace-uri(), $usual-prefix)">
						<!-- leavie it alone-->
						<xsl:element namespace="{namespace-uri()}" name="{local-name(.)}">
						      <xsl:apply-templates select="@*|node()"/>
						</xsl:element>
					</xsl:when>
		
					<xsl:when test="starts-with(namespace-uri(), $strict-prefix)">						
				        <xsl:variable name="new-namespace">
				            <xsl:call-template name="calculate-new-namespace">
				                <xsl:with-param name="old-uri" select="$old-uri"/>
				            </xsl:call-template>
				        </xsl:variable>
				
				        <xsl:element name="{local-name()}" namespace="{$new-namespace}">
				            <xsl:apply-templates select="@*|node()"/>
				        </xsl:element>
        			</xsl:when>
        			
					<xsl:otherwise>
						<xsl:copy>
							<xsl:apply-templates select="@*|node()" />
						</xsl:copy>
					</xsl:otherwise>
		
				</xsl:choose>
			
			</xsl:when>
			<xsl:otherwise> <!-- text nodes, comment nodes, and processing instruction nodes -->
				<xsl:copy>
					<xsl:apply-templates select="@*|node()" />
				</xsl:copy>
			</xsl:otherwise>

		</xsl:choose>

	</xsl:template>

	<!-- special case, because customProperties becomes custom-properties!	-->
	<xsl:template match="purlcp:*">
		<xsl:element name="{local-name(.)}"
			namespace="http://schemas.openxmlformats.org/officeDocument/2006/custom-properties">
		      <xsl:apply-templates select="@*|node()"/>
		</xsl:element>
	</xsl:template>

	<!-- special case, because extendedProperties becomes extended-properties!	-->
	<xsl:template match="purlep:*">
		<xsl:element name="{local-name(.)}"
			namespace="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties">
		      <xsl:apply-templates select="@*|node()"/>
		</xsl:element>
	</xsl:template>




	<xsl:template match="purls:workbook/@*" />

	<xsl:template match="@*">
		<xsl:param name="old-uri" select="namespace-uri()"/>	
		<xsl:choose>

			<xsl:when test="starts-with(namespace-uri(), $strict-prefix)">						
		        <xsl:variable name="new-namespace">
		            <xsl:call-template name="calculate-new-namespace">
		                <xsl:with-param name="old-uri" select="$old-uri"/>
		            </xsl:call-template>
		        </xsl:variable>
		
		        <xsl:attribute name="{local-name()}" namespace="{$new-namespace}">
					<xsl:choose>
					
						<!-- points to twips-->
						<xsl:when test="substring(., string-length(.) - 1) = 'pt'">	
													
							<xsl:variable name="dummy2" select="java:org.docx4j.jaxb.JaxbValidationEventHandler.logXml(..)" />
								
							<!--xsl:value-of select="round(substring-before(., 'pt') * 20)"/-->
							<xsl:value-of select="." />
							
						</xsl:when>
						
						<!-- % to thousands of a percent -->						
						<xsl:when test="substring(., string-length(.) ) = '%'">
		
							<xsl:variable name="dummy2" select="java:org.docx4j.jaxb.JaxbValidationEventHandler.logXml(..)" />
							
							<xsl:variable name="cleanValue" select="translate(., '%', '')"/>
   						    <!--xsl:value-of select="$cleanValue * 1000"/-->
							<xsl:value-of select="." />
   						    							
						</xsl:when>
						
						<xsl:otherwise>
							<xsl:value-of select="." />
						</xsl:otherwise>
					</xsl:choose>
		        </xsl:attribute>
			</xsl:when>

			<xsl:when test="namespace-uri() = ''">
				<xsl:attribute name="{local-name(.)}">
					
					<xsl:choose>
						<!-- TODO convert "%" to thousandths of a percent --> 
						<xsl:when
							test="substring(., string-length(.) ) = '%'">
								<xsl:variable name="cleanValue" select="translate(., '%', '')"/>
   						        <xsl:value-of select="$cleanValue * 1000"/>							
						</xsl:when>

						<xsl:otherwise>
							<xsl:value-of select="." />
						</xsl:otherwise>
					</xsl:choose>

				</xsl:attribute>
			</xsl:when>

			<xsl:otherwise>  <!-- eg http://schemas.openxmlformats.org/spreadsheetml/2006/main'-->
				<xsl:copy-of select="."/>
			</xsl:otherwise>

		</xsl:choose>

	</xsl:template>

  
  <!--
	<a:graphicData uri="http://purl.oclc.org/ooxml/drawingml/picture">
	should be uri="http://schemas.openxmlformats.org/drawingml/2006/picture"
	
	a:blip r:embed="rId8"

  -->
  
  <xsl:template match="purla:graphicData/@uri">
	
	<xsl:choose>
	
		<xsl:when test="starts-with(., $strict-prefix)">		
						
	        <xsl:variable name="new-namespace">
	            <xsl:call-template name="calculate-new-namespace">
	                <xsl:with-param name="old-uri" select="."/>
	            </xsl:call-template>
	        </xsl:variable>
	
	        <xsl:attribute name="uri" >
				<xsl:value-of select="$new-namespace" />
	        </xsl:attribute>
		</xsl:when>
		
		<xsl:otherwise>
			<xsl:copy-of select="."/>
		</xsl:otherwise>
	
	</xsl:choose>
	
  </xsl:template>
  
  
   
</xsl:stylesheet>
