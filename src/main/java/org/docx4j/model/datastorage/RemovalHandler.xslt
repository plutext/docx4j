<?xml version="1.0" encoding="UTF-8"?>
<!--
 ! Copyright 2011 Plutext Pty Ltd and individual contributors.
 !
 ! Licensed under the Apache License, Version 2.0 (the "License");
 ! you may not use this file except in compliance with the License.
 ! You may obtain a copy of the License at
 !
 !     http://www.apache.org/licenses/LICENSE-2.0
 !
 ! Unless required by applicable law or agreed to in writing, software
 ! distributed under the License is distributed on an "AS IS" BASIS,
 ! WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ! See the License for the specific language governing permissions and
 ! limitations under the License.
 !-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
  xmlns:aml="http://schemas.microsoft.com/aml/2001/core"
  xmlns:java="http://xml.apache.org/xalan/java"
  xmlns:o="urn:schemas-microsoft-com:office:office"
  xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture"
  xmlns:pkg="http://schemas.microsoft.com/office/2006/xmlPackage"
  xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
  xmlns:v="urn:schemas-microsoft-com:vml"
  xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
  xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
  xmlns:WX="http://schemas.microsoft.com/office/word/2003/auxHint"
  xmlns:w10="urn:schemas-microsoft-com:office:word"
	xmlns:xalan="http://xml.apache.org/xalan"  
  exclude-result-prefixes="a aml java o pic pkg r v w wp WX w10">

  <xsl:param name="all" select="false" /> <!-- keep contents (unless empty since this triggers that), but remove sdt -->
  <xsl:param name="all_but_placeholders" select="false" />
  <xsl:param name="all_but_placeholders_content" select="false" />
  <xsl:param name="types" select="'repeat condition'" />

  <xsl:output indent="yes" omit-xml-declaration="no" standalone="yes" />

  <xsl:param name="xpath" select="$all or $all_but_placeholders or $all_but_placeholders_content or contains($types, 'xpath')" />
  <xsl:param name="empty" select="$xpath or contains($types, 'empty')" /> <!--  by default, SDTs which are considered empty are removed, INCLUDING CONTENTS  -->
  <xsl:param name="repeat" select="$all or $all_but_placeholders or $all_but_placeholders_content or contains($types, 'repeat')" />
  <xsl:param name="condition" select="$all or $all_but_placeholders or $all_but_placeholders_content or contains($types, 'condition')" />

  <xsl:template match="/ | @* | node()">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" />
    </xsl:copy>
  </xsl:template>
  
  <!-- 
  
	  NOTE: The location path //para[1] does not mean the same as the location path /descendant::para[1]. 
	  
	             //para[1] selects all descendant para elements that are the first para children of their parents.
	  /descendant::para[1] selects the first descendant para element; 
   -->

  <xsl:template match="w:sdt">
    <xsl:choose>
    
       <!--  picture content control: need this special case, since contains no w:t
             and we have a test sensitive to w:t below -->
      <xsl:when
        test="count(w:sdtPr/w:picture)=1 and $xpath and contains(w:sdtPr/w:tag/@w:val, 'od:xpath=')">
        <xsl:apply-templates select="w:sdtContent/node()" />
       </xsl:when>
      <xsl:when test="$all and (count(w:sdtPr/w:picture)=1) ">
        <xsl:apply-templates select="w:sdtContent/node()" />
       </xsl:when>
      <xsl:when test="count(w:sdtPr/w:picture)=1 ">
        <xsl:copy-of select="."/>
       </xsl:when>
      <xsl:when test="$xpath and contains(string(w:sdtPr/w:tag/@w:val), 'od:Handler=picture')">
        <xsl:apply-templates select="w:sdtContent/node()" />
       </xsl:when>

      <!--  @since 6.1.0 --> 
      <xsl:when test="$all_but_placeholders and (count(w:sdtPr/w:showingPlcHdr)=1)">
      	<!--  keep placeholder sdt -->
        <xsl:copy-of select="."/>
       </xsl:when>       

      <!--  @since 6.1.0 --> 
      <xsl:when test="$all_but_placeholders_content and (count(w:sdtPr/w:showingPlcHdr)=1)">
      	<!--  keep placeholder sdt -->
        <xsl:copy-of select="./w:sdtContent/node()"/>
       </xsl:when>       
        
      <!-- if we are to remove SDTs referencing empty XML nodes, remove the resulting element entirely
           EXCEPT for a tc, which we keep.  Consider a parameter which allows for the tc to be removed? -->
      <xsl:when
        test="$empty and contains(w:sdtPr/w:tag/@w:val, 'od:xpath=') and string(w:sdtContent//w:t) = ''">

		  <!--  A normal bind can be wrapped around a tc (!); assume nothing more complex -->
          	<xsl:if test="./descendant::w:tc">
          		<w:tc>
					<xsl:copy-of select="./descendant::w:tcPr[1]" />				
					<w:p />
          		</w:tc>
          	</xsl:if>
        
            <!-- If there is drawing only -->
			<xsl:if test="./descendant::w:drawing">
				<w:p>
					<w:pPr />
					<w:r>
						<w:drawing>
							<xsl:copy-of select="./descendant::wp:inline[1]" />
						</w:drawing>
					</w:r>
				</w:p>
			</xsl:if>

            <!-- altChunk eg processed Flat OPC XML-->
            <xsl:if test="./descendant::w:altChunk">
                 <xsl:copy-of select="./descendant::w:altChunk[1]" />
            </xsl:if>
			
      </xsl:when>

      <xsl:when
        test="$repeat and contains(w:sdtPr/w:tag/@w:val, 'od:resultRepeatZero=')
            or $condition and contains(w:sdtPr/w:tag/@w:val, 'od:resultConditionFalse=')
            " >

		  <!--  A normal bind can be wrapped around a tc (!); assume nothing more complex -->
          	<xsl:if test="./descendant::w:tc">
          		<w:tc>
					<xsl:copy-of select="./descendant::w:tcPr[1]" />				
					<w:p />
          		</w:tc>
          	</xsl:if>
            
      </xsl:when>
      
      
      <!--  @since 3.3.0 --> 
      <xsl:when test="$all">
        <xsl:apply-templates select="w:sdtContent/node()" />
       </xsl:when>       
      
      <!--  excluded when key 'xpath' is used -->
      <xsl:when
        test="($xpath and contains(w:sdtPr/w:tag/@w:val, 'od:xpath='))
            or ($xpath and contains(w:sdtPr/w:tag/@w:val, 'od:ContentType='))
            or ($xpath and contains(w:sdtPr/w:tag/@w:val, 'od:progid='))
            ">
        <xsl:apply-templates select="w:sdtContent/node()" />
      </xsl:when>

      <xsl:when
        test="$condition and contains(w:sdtPr/w:tag/@w:val, 'od:condition=')">
        <xsl:apply-templates select="w:sdtContent/node()" />
      </xsl:when>

      <!--  excluded by Quantifier.DEFAULT, or when key 'repeat' is used -->
      <xsl:when
        test="($repeat and contains(w:sdtPr/w:tag/@w:val, 'od:repeat='))
            or ($repeat and contains(w:sdtPr/w:tag/@w:val, 'od:rptd='))
            or ($repeat and contains(w:sdtPr/w:tag/@w:val, 'w15:resultRepeatZero='))
            ">
        <xsl:apply-templates select="w:sdtContent/node()" />
      </xsl:when>

      

      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@* | node()" />
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  

  

	<!--  Ensure tc has content.  
	      Although we do something similar in OpenDoPEIntegrity, removing od:resultRepeatZero 
	      or od:resultConditionFalse w:sdt via this stylesheet might mean that content is now gone. -->
	<xsl:template match="w:tc">

		<xsl:variable name="results">
			<xsl:apply-templates select="@*|node()" />
		</xsl:variable>

		<xsl:copy>
			<xsl:choose>
				<xsl:when test="count(xalan:nodeset($results)//w:p)=0">
					<xsl:copy-of select="w:tcPr" />				
					<w:p />
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="$results" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>

	</xsl:template>

  <!-- A table may contain zero rows, but a row may not contain zero cells -->
	<xsl:template match="w:tr">

		<xsl:variable name="results">
			<xsl:apply-templates select="@*|node()" />
		</xsl:variable>

		<xsl:choose>
			<xsl:when test="count(xalan:nodeset($results)/w:tc)=0" /> <!--  remove this row -->
			<xsl:otherwise>
				<xsl:copy>
					<xsl:copy-of select="$results" />
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>
   
</xsl:stylesheet>
