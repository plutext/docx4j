<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:dfx="http://www.topologi.org/2004/Diff-X"
    xmlns:del="http://www.topologi.org/2004/Diff-X/Delete"
    xmlns:pkg="http://schemas.microsoft.com/office/2006/xmlPackage"
    xmlns:ns2="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
    xmlns:ns4="http://schemas.openxmlformats.org/schemaLibrary/2006/main"
    xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
	xmlns:java="http://xml.apache.org/xalan/java"    
  xmlns:xml="http://www.w3.org/XML/1998/namespace"
  xmlns:xalan="http://xml.apache.org/xalan"
                
  version="1.0" 
        exclude-result-prefixes="java">	
  

  <!-- 
  *  Copyright 2007, Plutext Pty Ltd.
  *
  *  This file is part of plutext-client-word2007.

  plutext-client-word2007 is free software: you can redistribute it and/or
  modify it under the terms of version 3 of the GNU General Public License
  as published by the Free Software Foundation.

  plutext-client-word2007 is distributed in the hope that it will be
  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with plutext-client-word2007.  If not, see
  <http://www.gnu.org/licenses/>.

  -->

  <xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" 
  indent="yes" xalan:indent-amount="4" />

<xsl:param name="author"/>


  <xsl:template match="/ | @*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>


  <xsl:template match="@del:val">
  	<xsl:attribute name="w:val"><xsl:value-of select="."/></xsl:attribute>
  </xsl:template>

<!-- 
  <xsl:template match="*[@dfx:insert='true']" >
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
    
  </xsl:template>
 -->
   
  <xsl:template match="w:rPr[@dfx:delete='true'] " mode="omitDeletions">
  	<xsl:comment>rPr had @dfx:delete='true', so left out.</xsl:comment>
  </xsl:template>

  
  <xsl:template match="@dfx:insert" />
  <xsl:template match="@dfx:delete" />
  


  <xsl:template match="w:r">

       <xsl:apply-templates select="w:t"/>

  </xsl:template>

  <xsl:template match="w:t">

       <xsl:apply-templates select="node()"/>

  </xsl:template>


  <xsl:template match="ins">
  
  			<xsl:variable name="id" 
				select="java:org.docx4j.diff.ParagraphDifferencer.getId()" />
  

    <w:ins w:id="{$id}" w:author="{$author}">  <!--  w:date is optional, so omit for now -->

      <w:r>
   		<xsl:apply-templates select="../../w:rPr" mode="omitDeletions"/>
        <w:t>
          <xsl:value-of select="."/> <!-- assume this is just text in a diffx ins element-->
        </w:t>
      </w:r>
    </w:ins>
    
  </xsl:template>

  <xsl:template match="del">

  			<xsl:variable name="id" 
				select="java:org.docx4j.diff.ParagraphDifferencer.getId()" />


    <w:del w:id="{$id}" w:author="{$author}">

      <w:r>
      	<xsl:apply-templates select="../../w:rPr"/>
        <w:delText>
          <xsl:value-of select="."/>
        </w:delText>
      </w:r>
    </w:del>

  </xsl:template>

  <xsl:template match="text()">
  
      <w:r>
   		<xsl:apply-templates select="../../w:rPr" mode="omitDeletions"/>
        <w:t>
          <xsl:value-of select="."/> 
        </w:t>
      </w:r>
    
  </xsl:template>


</xsl:stylesheet>