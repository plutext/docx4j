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
<xsl:param name="date"/>

<xsl:preserve-space elements="ins del w:t"/> 

  <xsl:template match="/ | @*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>


  <!-- do we really want to do this???? -->
  <xsl:template match="@del:val">
  	<xsl:attribute name="w:val"><xsl:value-of select="."/></xsl:attribute>
  </xsl:template>

  <xsl:template match="@del:*"/>

<!-- 
  <xsl:template match="*[@dfx:insert='true']" >
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
    
  </xsl:template>
 -->
   

  
  <xsl:template match="@dfx:insert" />
  
  <!--  
  <w:p dfx:delete="true"><w:r dfx:delete="true"><w:rPr dfx:delete="true"><w:noProof dfx:delete="true" /></w:rPr><w:drawing dfx:delete="true"><wp:inline dfx:delete="true" del:distB="0" del:distL="0" del:distR="0" del:distT="0"><wp:extent dfx:delete="true" del:cx="3143250" del:cy="2286000" /><wp:effectExtent dfx:delete="true" del:b="0" del:l="19050" del:r="0" del:t="0" /><wp:docPr dfx:delete="true" del:descr="http://venturebeat.com/wp-content/uploads/2009/01/imvu.jpg" del:id="2" del:name="Picture 1" /><wp:cNvGraphicFramePr dfx:delete="true"><a:graphicFrameLocks dfx:delete="true" del:noChangeAspect="1" /></wp:cNvGraphicFramePr><a:graphic dfx:delete="true"><a:graphicData dfx:delete="true" del:uri="http://schemas.openxmlformats.org/drawingml/2006/picture"><pic:pic dfx:delete="true"><pic:nvPicPr dfx:delete="true"><pic:cNvPr dfx:delete="true" del:descr="http://venturebeat.com/wp-content/uploads/2009/01/imvu.jpg" del:id="0" del:name="Picture 1" /><pic:cNvPicPr dfx:delete="true"><a:picLocks dfx:delete="true" del:noChangeArrowheads="1" del:noChangeAspect="1" /></pic:cNvPicPr></pic:nvPicPr><pic:blipFill dfx:delete="true"><a:blip dfx:delete="true" del:link="rId4" /><a:srcRect dfx:delete="true" /><a:stretch dfx:delete="true"><a:fillRect dfx:delete="true" /></a:stretch></pic:blipFill><pic:spPr dfx:delete="true" del:bwMode="auto"><a:xfrm dfx:delete="true"><a:off dfx:delete="true" del:x="0" del:y="0" /><a:ext dfx:delete="true" del:cx="3143250" del:cy="2286000" /></a:xfrm><a:prstGeom dfx:delete="true" del:prst="rect"><a:avLst dfx:delete="true" /></a:prstGeom><a:noFill dfx:delete="true" /><a:ln dfx:delete="true" del:w="9525"><a:noFill dfx:delete="true" /><a:miter dfx:delete="true" del:lim="800000" /><a:headEnd dfx:delete="true" /><a:tailEnd dfx:delete="true" /></a:ln></pic:spPr></pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>
  -->
  <xsl:template match="*[@dfx:delete]">
    <!-- Drop this element, but retain its children
         unless they to have the dfx:delete attribute -->
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="w:rPr[@dfx:delete='true'] " mode="omitDeletions">
  	<xsl:comment>rPr had @dfx:delete='true', so left out.</xsl:comment>
  </xsl:template>

  
  <!--  
  <w:p dfx:delete="true"><w:r dfx:delete="true"><w:rPr dfx:delete="true"><w:noProof dfx:delete="true" /></w:rPr><w:drawing dfx:delete="true"><wp:inline dfx:delete="true" del:distB="0" del:distL="0" del:distR="0" del:distT="0"><wp:extent dfx:delete="true" del:cx="3143250" del:cy="2286000" /><wp:effectExtent dfx:delete="true" del:b="0" del:l="19050" del:r="0" del:t="0" /><wp:docPr dfx:delete="true" del:descr="http://venturebeat.com/wp-content/uploads/2009/01/imvu.jpg" del:id="2" del:name="Picture 1" /><wp:cNvGraphicFramePr dfx:delete="true"><a:graphicFrameLocks dfx:delete="true" del:noChangeAspect="1" /></wp:cNvGraphicFramePr><a:graphic dfx:delete="true"><a:graphicData dfx:delete="true" del:uri="http://schemas.openxmlformats.org/drawingml/2006/picture"><pic:pic dfx:delete="true"><pic:nvPicPr dfx:delete="true"><pic:cNvPr dfx:delete="true" del:descr="http://venturebeat.com/wp-content/uploads/2009/01/imvu.jpg" del:id="0" del:name="Picture 1" /><pic:cNvPicPr dfx:delete="true"><a:picLocks dfx:delete="true" del:noChangeArrowheads="1" del:noChangeAspect="1" /></pic:cNvPicPr></pic:nvPicPr><pic:blipFill dfx:delete="true"><a:blip dfx:delete="true" del:link="rId4" /><a:srcRect dfx:delete="true" /><a:stretch dfx:delete="true"><a:fillRect dfx:delete="true" /></a:stretch></pic:blipFill><pic:spPr dfx:delete="true" del:bwMode="auto"><a:xfrm dfx:delete="true"><a:off dfx:delete="true" del:x="0" del:y="0" /><a:ext dfx:delete="true" del:cx="3143250" del:cy="2286000" /></a:xfrm><a:prstGeom dfx:delete="true" del:prst="rect"><a:avLst dfx:delete="true" /></a:prstGeom><a:noFill dfx:delete="true" /><a:ln dfx:delete="true" del:w="9525"><a:noFill dfx:delete="true" /><a:miter dfx:delete="true" del:lim="800000" /><a:headEnd dfx:delete="true" /><a:tailEnd dfx:delete="true" /></a:ln></pic:spPr></pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>
  -->  
  <xsl:template match="*[@dfx:delete]" />
  


  <xsl:template match="w:r">

       <xsl:apply-templates select="w:t|w:tab|w:drawing|w:commentReference|w:sym"/> 
      <!-- NB: this XSLT drops run content other than these. What else to keep? -->

  </xsl:template>

  <!-- We don't lose our rPr or w:t elements here,
  because they are put in by the template
  matching on text().  -->

  <xsl:template match="w:t">

       <xsl:apply-templates select="node()"/>

  </xsl:template>

  <xsl:template match="w:drawing | w:commentReference | w:sym">
    <w:r>
      <xsl:apply-templates select="../../w:rPr" mode="omitDeletions"/>
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
        <!-- Handle 
(1)        
            <w:drawing><wp:inline del:distB="0" del:distL="0" del:distR="0" del:distT="0">
              <wp:extent cx="466725" cy="381000" /><wp:effectExtent b="0" l="19050" r="9525" t="0" /><wp:docPr id="5" name="Picture 1" /><wp:cNvGraphicFramePr><a:graphicFrameLocks 
              noChangeAspect="true" 
              del:noChangeAspect="1" /></wp:cNvGraphicFramePr><a:graphic><a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture"><pic:pic><pic:nvPicPr><pic:cNvPr id="0" name="Picture 1" /><pic:cNvPicPr><a:picLocks noChangeArrowheads="true" noChangeAspect="true" del:noChangeArrowheads="1" del:noChangeAspect="1" />      
              
            until canonicaliser does it better
(2)

    <w:r dfx:insert="true">
      <w:rPr dfx:insert="true">
        <w:rStyle dfx:insert="true" w:val="CommentReference" />
      </w:rPr>
      <w:commentReference dfx:insert="true" w:id="0" />
   </w:r>  

(3)      <w:sym w:font="Wingdings" w:char="F04A" />

        -->
      </xsl:copy>
    </w:r>
  </xsl:template>


  <xsl:template match="w:tab[parent::w:r]"> <!-- so we don't match tab in properties -->

    <w:r>
      <xsl:apply-templates select="../w:rPr" mode="omitDeletions"/>
      <w:tab/> 
    </w:r>

  </xsl:template>


  <xsl:template match="ins">
  
  			<xsl:variable name="id" 
				select="java:org.docx4j.diff.ParagraphDifferencer.getId()" />
  

    <w:ins w:id="{$id}" w:author="{$author}" w:date="{$date}">  <!--  w:date is optional -->

      <w:r>
   		<xsl:apply-templates select="../../w:rPr" mode="omitDeletions"/>
        <w:t xml:space="preserve"><xsl:value-of select="."/></w:t>
      </w:r>
    </w:ins>
    
  </xsl:template>

  <xsl:template match="del">

  			<xsl:variable name="id" 
				select="java:org.docx4j.diff.ParagraphDifferencer.getId()" />


    <w:del w:id="{$id}" w:author="{$author}" w:date="{$date}">  <!--  w:date is optional -->

      <w:r>
      	<xsl:apply-templates select="../../w:rPr"/>
        <w:delText><xsl:value-of select="."/></w:delText>
      </w:r>
    </w:del>

  </xsl:template>

  <xsl:template match="text()">
  
      <w:r>
   		<xsl:apply-templates select="../../w:rPr" mode="omitDeletions"/>
        <w:t xml:space="preserve"><xsl:value-of select="."/></w:t>
      </w:r>
    
  </xsl:template>

  <xsl:template match="w:rPr[not(@dfx:delete='true')] " mode="omitDeletions">
    <!--  Handle eg
       
       <w:rPr dfx:insert="true"><w:b dfx:insert="true" /></w:rPr><
    
    -->
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>


</xsl:stylesheet>