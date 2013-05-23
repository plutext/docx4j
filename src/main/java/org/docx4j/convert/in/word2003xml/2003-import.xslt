<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:transit="http://www.docx4java.org/word2003xml/to/wordprocessingml/2006"             
xmlns:aml="http://schemas.microsoft.com/aml/2001/core"
                xmlns:w03="http://schemas.microsoft.com/office/word/2003/wordml"
                xmlns:wx="http://schemas.microsoft.com/office/word/2003/auxHint"
                xmlns:wsp="http://schemas.microsoft.com/office/word/2003/wordml/sp2"
                xmlns:sl="http://schemas.microsoft.com/schemaLibrary/2003/core"
                xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml"
                xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006"
                xmlns:wpc="http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas"
                xmlns:dt="uuid:C2F41010-65B3-11d1-A29F-00AA00C14882"
                xmlns:o="urn:schemas-microsoft-com:office:office"
                xmlns:v="urn:schemas-microsoft-com:vml"
                xmlns:w10="urn:schemas-microsoft-com:office:word"
                xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
    xmlns:msxsl="urn:schemas-microsoft-com:xslt" exclude-result-prefixes="msxsl"
>
  
  <!-- This stylesheet is the first phase of a proof of concept of converting Word 2003 XML,
       to ECMA 376.  
       
       It produces output matching:
       
        <xsd:element name="transition03to06"> 
             <xsd:complexType>
               <xsd:sequence>
                 <xsd:element ref="w:fonts" />
                 <xsd:element ref="w:numbering" />
                 <xsd:element ref="w:styles" />
                 <xsd:element ref="w:body" />
               </xsd:sequence>
             </xsd:complexType>
             </xsd:element>

      where wordDocument is in namespace http://www.docx4java.org/word2003xml/to/wordprocessingml/2006
      and its children are real http://schemas.openxmlformats.org/wordprocessingml/2006/main content.

       -->

       <xsl:output method="xml" indent="yes"/>

    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

  <xsl:template match="w03:wordDocument">
    <!-- Put this in the transit namespace and drop its attributes eg: w:macrosPresent="no" w:embeddedObjPresent="no" w:ocxPresent="no" xml:space="preserve" -->
    <xsl:element namespace="http://www.docx4java.org/word2003xml/to/wordprocessingml/2006" name="transition03to06">
      <xsl:apply-templates select="node()"/>
    </xsl:element>
  </xsl:template>

  <!-- move descendents to modern namespace-->
  <xsl:template match="w03:*">
    <xsl:element namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" name="{local-name()}">
        <xsl:apply-templates select="@* | node()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="@*">
    <xsl:attribute namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" name="{local-name()}">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>

  <!--  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  +++ ignoreSubtree ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <xsl:template match="w03:ignoreSubtree" />


  <!--  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  +++ o:DocumentProperties +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!-- TODO convert these-->
  <xsl:template match="o:DocumentProperties" />


  <!--  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  +++ fonts ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <xsl:template match="w03:fonts">
    <xsl:element namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" name="fonts">
      <xsl:apply-templates select="@* | node()"/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="w03:panose-1">
    <xsl:element namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" name="panose">
      <xsl:apply-templates select="@* | node()"/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="@w03:usb-0" >
    <xsl:attribute namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" name="usb0">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>
  <xsl:template match="@w03:usb-1" >
    <xsl:attribute namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" name="usb1">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>
  <xsl:template match="@w03:usb-2" >
    <xsl:attribute namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" name="usb2">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>
  <xsl:template match="@w03:usb-3" >
    <xsl:attribute namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" name="usb3">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>
  <xsl:template match="@w03:csb-0" >
    <xsl:attribute namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" name="csb0">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>
  <xsl:template match="@w03:csb-1" >
    <xsl:attribute namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" name="csb1">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>
  

  <!--  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  +++ lists ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <xsl:template match="w03:lists">
    <xsl:element namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" name="numbering">
      <xsl:apply-templates select="@* | node()"/>
    </xsl:element>
  </xsl:template>
  <xsl:template match="w03:defaultFonts" /> <!-- drop for now -->

    <!-- listDef becomes abstractNum-->
  <xsl:template match="w03:listDef">
    <xsl:element namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" name="abstractNum">
      <xsl:apply-templates select="@* | node()"/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="@w03:listDefId" >
    <xsl:attribute namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" name="abstractNumId">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>
  
  <xsl:template match="w03:lsid">
    <xsl:element namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" name="nsid">
      <xsl:apply-templates select="@* | node()"/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="w03:plt">
    <xsl:element namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" name="multiLevelType">
      <xsl:apply-templates select="@* | node()"/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="@w03:tplc" />
  <xsl:template match="@w03:tentative" />
  <xsl:template match="@w03:hint" />
  

  <!-- list becomes num-->
  <xsl:template match="w03:list">
    <xsl:element namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" name="num">
      <xsl:apply-templates select="@* | node()"/>
    </xsl:element>
  </xsl:template>
  <xsl:template match="@w03:ilfo" >
    <xsl:attribute namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" name="numId">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>
  
  <xsl:template match="w03:ilst">
    <xsl:element namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" name="abstractNumId">
      <xsl:apply-templates select="@* | node()"/>
    </xsl:element>
  </xsl:template>

  <!--  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  +++ styles +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <xsl:template match="w03:styles">
    <xsl:element namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" name="{local-name()}">
      <xsl:apply-templates select="@* | node()"/>
    </xsl:element>
  </xsl:template>
  <xsl:template match="w03:versionOfBuiltInStylenames" />

  <!--  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  +++ shapeDefaults ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
    <xsl:template match="w03:shapeDefaults" />

  <!--  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  +++ docPr ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <xsl:template match="w03:docPr" />

  <!--  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  +++ body +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <xsl:template match="w03:body">
    <xsl:element namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" name="{local-name()}">
      <xsl:apply-templates select="@* | node()"/>
    </xsl:element>
  </xsl:template>

  <!-- drop wx-->
  <xsl:template match="wx:*">
      <xsl:apply-templates select="node()"/>
  </xsl:template>

  <!-- drop aml for now.  TODO convert to WordML -->
  <xsl:template match="aml:*">
    <xsl:apply-templates select="node()"/>
  </xsl:template>
  <xsl:template match="w03:delText" />

  <!-- drop image data for now.  TODO come back and get this later.. -->
  <xsl:template match="w03:pict" />
  <xsl:template match="w03:binData" />


</xsl:stylesheet>
