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

  <xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="yes" />

<xsl:param name="author"/>


  <xsl:template match="/ | @*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>



  <!-- Any element which has a dfx:delete attribute can be deleted.
  
       This is ok in pPr, since we don't bother to track those changes.
       
       But if it occurs on w:p, w:r, or w:t, we will need to do better.
         -->
  <xsl:template match="*[@dfx:delete='true' and parent::w:pPr] "/>

  <xsl:template match="@del:val"/>


  <xsl:template match="*[@dfx:insert='true']" >
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@dfx:insert" />
  


  <xsl:template match="w:r">

	<xsl:for-each select="w:t">
	    <xsl:choose>
	        <xsl:when test="count(ins)">
	            <xsl:apply-templates/>
	        </xsl:when>
	        <xsl:when test="count(del)"> <!--  whether or not w:t[@dfx:delete='true'] -->
	            <xsl:apply-templates/>
	        </xsl:when>
	        <xsl:otherwise>
	            <xsl:copy-of select=".."/>
	        </xsl:otherwise>
	    </xsl:choose>
	</xsl:for-each>        

  </xsl:template>

  <xsl:template match="ins">
  
  			<xsl:variable name="id" 
				select="java:org.docx4j.diff.ParagraphDifferencer.getId()" />
  

    <w:ins w:id="{$id}" w:author="{$author}">  <!--  w:date is optional, so omit for now -->

      <w:r>
        <w:t>
          <xsl:value-of select="."/> <!-- assume this is just text in a diffx ins element-->
        </w:t>
      </w:r>
    </w:ins>
    
  </xsl:template>

  <xsl:template match="del">

    <w:del>

      <w:r>
        <w:delText>
          <xsl:value-of select="."/>
        </w:delText>
      </w:r>
    </w:del>

  </xsl:template>


</xsl:stylesheet>