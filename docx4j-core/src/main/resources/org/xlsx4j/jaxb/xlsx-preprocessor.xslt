
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"

	xmlns:java="http://xml.apache.org/xalan/java"
	xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006"
	
 	xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
 		
	xmlns:purls="http://purl.oclc.org/ooxml/spreadsheetml/main" 
	xmlns:s="http://schemas.openxmlformats.org/spreadsheetml/2006/main"

 	xmlns:purla="http://purl.oclc.org/ooxml/drawingml/main"
	xmlns:purlpic="http://purl.oclc.org/ooxml/drawingml/picture"
	xmlns:purlr="http://purl.oclc.org/ooxml/officeDocument/relationships"
 
 
 
	xmlns:purlep="http://purl.oclc.org/ooxml/officeDocument/extendedProperties"
	xmlns:ep="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties"
	
	xmlns:purlcp="http://purl.oclc.org/ooxml/officeDocument/customProperties"
	xmlns:cp="http://schemas.openxmlformats.org/officeDocument/2006/custom-properties"
	
	version="1.0" exclude-result-prefixes="java">	
      
<!--     
<p:sld xmlns:v="urn:schemas-microsoft-com:vml" xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" xmlns:xdr="http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing" xmlns:c="http://schemas.openxmlformats.org/drawingml/2006/chart" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture" xmlns:ns12="http://schemas.openxmlformats.org/drawingml/2006/lockedCanvas" xmlns:ns6="http://schemas.openxmlformats.org/drawingml/2006/chartDrawing" xmlns:ns11="http://schemas.openxmlformats.org/drawingml/2006/compatibility" xmlns:dgm="http://schemas.openxmlformats.org/drawingml/2006/diagram">
-->

<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="yes" />

  <xsl:template match="/">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

	<xsl:template match="purls:workbook/@*" />

	<xsl:template match="@*">
		<xsl:choose>

			<xsl:when
				test="namespace-uri() = 'http://schemas.openxmlformats.org/spreadsheetml/2006/main'">
				<xsl:attribute name="{local-name(.)}"
					namespace="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
					<xsl:value-of select="." />
				</xsl:attribute>
			</xsl:when>

			<xsl:when
				test="namespace-uri() = 'http://purl.oclc.org/ooxml/officeDocument/relationships'">
				<xsl:attribute name="{local-name(.)}"
					namespace="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
					<xsl:value-of select="." />
				</xsl:attribute>
			</xsl:when>

			<xsl:when
				test="namespace-uri() = 'http://purl.oclc.org/ooxml/spreadsheetml/main'">
				<xsl:attribute name="{local-name(.)}"
					namespace="http://schemas.openxmlformats.org/spreadsheetml/2006/main">

					<xsl:choose>
						<!-- TODO convert "175pt" -->
						<xsl:when
							test="substring(., string-length(.) - 1) = 'pt'">
<!--							<xsl:value-of
								select="substring(., 1, string-length(.) - 2)" /> -->
								<xsl:value-of select="round(substring-before(., 'pt') * 20)"/>
						</xsl:when>

						<xsl:otherwise>
							<xsl:value-of select="." />
						</xsl:otherwise>
					</xsl:choose>

				</xsl:attribute>
			</xsl:when>

<!-- drawingml attributes ahave no prefix and are in no namespace at all 
			<xsl:when
				test="namespace-uri() = 'http://purl.oclc.org/ooxml/drawingml/main'">
				<xsl:attribute name="{local-name(.)}"
					namespace="http://schemas.openxmlformats.org/drawingml/2006/main">
					
					<xsl:value-of select="." />

				</xsl:attribute>
			</xsl:when>
-->

			<xsl:when
				test="namespace-uri() = ''">
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

			<xsl:otherwise>
				<xsl:copy>
					<xsl:apply-templates select="@*" />
				</xsl:copy>
			</xsl:otherwise>

		</xsl:choose>

	</xsl:template>

	<xsl:template match="*">
		<xsl:choose>

			<xsl:when
				test="namespace-uri() = 'http://schemas.openxmlformats.org/spreadsheetml/2006/main'">
				<xsl:element name="{local-name(.)}"
					namespace="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
				      <xsl:apply-templates select="@*|node()"/>
				</xsl:element>
			</xsl:when>

			<xsl:when
				test="namespace-uri() = 'http://purl.oclc.org/ooxml/spreadsheetml/main'">
				<xsl:element name="{local-name(.)}"
					namespace="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
				      <xsl:apply-templates select="@*|node()"/>
				</xsl:element>
			</xsl:when>

			<xsl:when
				test="namespace-uri() = 'http://purl.oclc.org/ooxml/officeDocument/customProperties'">
				<xsl:element name="{local-name(.)}"
					namespace="http://schemas.openxmlformats.org/officeDocument/2006/custom-properties">
				      <xsl:apply-templates select="@*|node()"/>
				</xsl:element>
			</xsl:when>

			<xsl:when
				test="namespace-uri() = 'http://purl.oclc.org/ooxml/officeDocument/extendedProperties'">
				<xsl:element name="{local-name(.)}"
					namespace="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties">
				      <xsl:apply-templates select="@*|node()"/>
				</xsl:element>
			</xsl:when>

			<xsl:when
				test="namespace-uri() = 'http://purl.oclc.org/ooxml/officeDocument/docPropsVTypes'">
				<xsl:element name="{local-name(.)}"
					namespace="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
				      <xsl:apply-templates select="@*|node()"/>
				</xsl:element>
			</xsl:when>

			<xsl:when
				test="namespace-uri() = 'http://purl.oclc.org/ooxml/drawingml/main'">
				<xsl:element name="{local-name(.)}"
					namespace="http://schemas.openxmlformats.org/drawingml/2006/main">
				      <xsl:apply-templates select="@*|node()"/>
				</xsl:element>
			</xsl:when>
			
			<xsl:when
				test="namespace-uri() = 'http://purl.oclc.org/ooxml/drawingml/spreadsheetDrawing'">
				<xsl:element name="{local-name(.)}"
					namespace="http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing">
				      <xsl:apply-templates select="@*|node()"/>
				</xsl:element>
			</xsl:when>
			

			<xsl:when
				test="namespace-uri() = 'http://purl.oclc.org/ooxml/drawingml/picture'">
				<xsl:element name="{local-name(.)}"
					namespace="http://schemas.openxmlformats.org/drawingml/2006/picture">
				      <xsl:apply-templates select="@*|node()"/>
				</xsl:element>
			</xsl:when>

			<xsl:when
				test="namespace-uri() = 'http://purl.oclc.org/ooxml/officeDocument/customXml'">
				<xsl:element name="{local-name(.)}"
					namespace="http://schemas.openxmlformats.org/officeDocument/2006/customXml">
				      <xsl:apply-templates select="@*|node()"/>
				</xsl:element>
			</xsl:when>

			<xsl:otherwise>
				<xsl:copy>
					<xsl:apply-templates select="@*|node()" />
				</xsl:copy>
			</xsl:otherwise>

		</xsl:choose>

	</xsl:template>


	<xsl:template match="purlep:Properties">
		<ep:Properties>
			<xsl:apply-templates select="@*|node()" />
		</ep:Properties>
	</xsl:template>
	<xsl:template match="purlcp:Properties">
		<cp:Properties>
			<xsl:apply-templates select="@*|node()" />
		</cp:Properties>
	</xsl:template>

<!--
	<xsl:template match="purlp:sld">
		<p:sld
			xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"							
			>
			<xsl:apply-templates select="@*|node()" />
		</p:sld>
	</xsl:template>
-->

	<xsl:template match="purla:theme">
		<a:theme>
			<xsl:apply-templates select="@*|node()" />
		</a:theme>
	</xsl:template>

<!--
	<xsl:template match="purlw:*">
		<xsl:element name="{name(.)}"
			namespace="http://schemas.openxmlformats.org/presentationml/2006/main">
			<xsl:apply-templates select="@*|node()" />
		</xsl:element>
	</xsl:template>
-->
	
  
  <!--
	<a:graphicData uri="http://purl.oclc.org/ooxml/drawingml/picture">
	should be uri="http://schemas.openxmlformats.org/drawingml/2006/picture"
	
	a:blip r:embed="rId8"

  <xsl:template match="@uri">
	<xsl:attribute name="uri">http://schemas.openxmlformats.org/drawingml/2006/picture</xsl:attribute>
  </xsl:template>
	
  -->
  
   
</xsl:stylesheet>
