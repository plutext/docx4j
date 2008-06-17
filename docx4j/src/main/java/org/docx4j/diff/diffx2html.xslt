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

  <xsl:output method="html" encoding="utf-8" omit-xml-declaration="no" 
  indent="yes" xalan:indent-amount="4" />


<!--  

	WARNING: this xslt exists solely to help in testing the diff
	output, by allowing it to be viewed easily as HTML.

	It is not intended for production.

 -->

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
  

  <xsl:template match="w:p">
  
  	<p>
      <xsl:apply-templates select="@*|node()"/>  	
  	</p>
  
  </xsl:template>

  <xsl:template match="w:r">

     <xsl:apply-templates/>

  </xsl:template>

  <xsl:template match="w:t[@dfx:insert='true']" >

    <span style="text-decoration: underline;color:red;"> 

     <xsl:apply-templates/>

	</span>
	    
  </xsl:template>


  <xsl:template match="ins">

    <span style="text-decoration: underline;color:red;"> 

          <xsl:value-of select="."/> <!-- assume this is just text in a diffx ins element-->

	</span>
	    
  </xsl:template>

  <xsl:template match="del">

    <span style="text-decoration: line-through;color:red;"> 

          <xsl:value-of select="."/>
          
	</span>

  </xsl:template>

  <xsl:template match="w:t[@dfx:delete='true']" >

    <span style="text-decoration: line-through;color:red;"> 

     <xsl:apply-templates/>

	</span>
	    
  </xsl:template>


</xsl:stylesheet>