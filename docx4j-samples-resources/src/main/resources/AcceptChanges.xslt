<?xml version="1.0" encoding="UTF-8" ?>
<!--
  DEPRECATED since docx4j 17.3.3: use org.docx4j.model.revisions.TrackedChanges instead
  (TrackedChanges.acceptAll / rejectAll, and per change TrackedChanges.in(pkg) then accept() or
  reject(); see the TrackChangesAccept and TrackChangesReject samples).  That API accepts and rejects
  every kind of tracked change in every story, as Word does (CR-043).

  This stylesheet only accepts, over the main document part only.  It leaves formatting changes
  (w:rPrChange, w:pPrChange, ...) and move markup, drops a deleted paragraph mark without joining
  its paragraph to the next (Word joins them), and drops a whole table when its first row is deleted
  while keeping any other deleted row.  It is kept, unchanged, for code that still uses it.
-->
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
  xmlns:xml="http://www.w3.org/XML/1998/namespace"
  version="1.0"
        exclude-result-prefixes="java msxsl ext o v WX aml w10">


  <xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="yes" />


  <xsl:template match="/ | @*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="w:del" />

  <xsl:template match="w:ins" >
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <xsl:template match="w:tbl" >
  	<xsl:choose>
  		<xsl:when test="w:tr[1]/w:trPr/w:del">
  			<!-- Word deletes the entire table if this is present -->
  		</xsl:when>
  		<xsl:otherwise>
		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()"/>
		    </xsl:copy>
  		</xsl:otherwise>
  	</xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>