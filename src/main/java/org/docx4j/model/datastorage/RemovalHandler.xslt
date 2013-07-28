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
  exclude-result-prefixes="a aml java o pic pkg r v w wp WX w10">

  <xsl:param name="all" select="false" />
  <xsl:param name="types" select="'repeat condition'" />

  <xsl:output indent="yes" omit-xml-declaration="no" standalone="yes" />

  <xsl:param name="xpath" select="$all or contains($types, 'xpath')" />
  <xsl:param name="empty" select="$xpath or contains($types, 'empty')" />
  <xsl:param name="repeat" select="$all or contains($types, 'repeat')" />
  <xsl:param name="condition" select="$all or contains($types, 'condition')" />

  <xsl:template match="/ | @* | node()">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="w:sdt">
    <xsl:choose>
      <!-- if we are to remove SDTs referencing empty XML nodes, remove the resulting element entirely -->
      <xsl:when
        test="$empty and contains(w:sdtPr/w:tag/@w:val, 'od:xpath=') and string(w:sdtContent//w:t) = ''" />
      <xsl:when
        test="$xpath and contains(w:sdtPr/w:tag/@w:val, 'od:xpath=')
            or $repeat and contains(w:sdtPr/w:tag/@w:val, 'od:repeat=')
            or $repeat and contains(w:sdtPr/w:tag/@w:val, 'od:rptd=')
            or $condition and contains(w:sdtPr/w:tag/@w:val, 'od:condition=')
            ">
        <xsl:apply-templates select="w:sdtContent/node()" />
      </xsl:when>
      <xsl:when
        test="$repeat and contains(w:sdtPr/w:tag/@w:val, 'od:resultRepeatZero=')
            or $condition and contains(w:sdtPr/w:tag/@w:val, 'od:resultConditionFalse=')
            " />
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@* | node()" />
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="w:tc">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" />
      <!-- if somewhere in the process, the paragraph was lost, add an empty
        one, cf. http://www.docx4java.org/forums/data-binding-java-f16/what-remained-when-removesdtcellsonfailedcondition-went-t1593.html -->
      <xsl:if test="not(.//w:p)">
        <w:p />
      </xsl:if>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="w:tbl">
    <xsl:choose>
      <xsl:when
        test="w:tr/w:tc/w:sdt and count(.//w:tc) = 1 and string(.//w:t) = ''">
        <!-- if the table has a cell level SDT and contains only one cell and
          that is empty, remove the entire table this works around Word behaviour when
          instrumenting single cell tables, cf. http://www.docx4java.org/forums/data-binding-java-f16/what-remained-when-removesdtcellsonfailedcondition-went-t1593.html -->
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@* | node()" />
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
