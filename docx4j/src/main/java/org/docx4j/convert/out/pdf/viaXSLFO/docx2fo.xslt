
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
    xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
    xmlns:o="urn:schemas-microsoft-com:office:office"
    xmlns:v="urn:schemas-microsoft-com:vml"
    xmlns:WX="http://schemas.microsoft.com/office/word/2003/auxHint"
    xmlns:aml="http://schemas.microsoft.com/aml/2001/core"
    xmlns:w10="urn:schemas-microsoft-com:office:word"
	xmlns:pkg="http://schemas.microsoft.com/office/2006/xmlPackage"		    
	xmlns:java="http://xml.apache.org/xalan/java" 
	xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
	xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
	xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture"
	xmlns:fo="http://www.w3.org/1999/XSL/Format"
    version="1.0"
        exclude-result-prefixes="java w a o v WX aml w10 pkg wp pic">	

<!-- =======================================

	 This is the beginnings of a basic XSLT
	 to convert WordML2FO.
	 
	 I'm not aware of any more complete
	 open source offering.
	 
	 (  RenderX have their 
	    http://www.renderx.com/tools/word2fo.html
	    but the licence is restrictive,
	    and the 20070227 version is directed at
	    Word 2003 WordML.
	    
	    Some users of docx4j might wish to 
	    modify that to meet their personal 
	    needs.
	    
	    However, the docx4j distribution
	    needs an independent open source implementation. )
	    
	 This xslt relies on docx4j to tell it what
	 formatting values apply to a paragraph or run.
	 ie instead of trying to follow inheritance
	 hierarchies using xslt, an extension function
	 is called to do the work.
	 
	 Because of this, this stylesheet is of 
	 limited use to other projects :-( 

     ======================================= -->
        
        <!--  Note definition of xmlns:r is different 
              from the definition in an _rels file
              (where it is http://schemas.openxmlformats.org/package/2006/relationships)  -->


<!-- 
  <xsl:output method="html" encoding="utf-8" omit-xml-declaration="yes" indent="yes"/>
   -->
<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="yes" />

<xsl:param name="wmlPackage"/> <!-- select="'passed in'"-->	
<xsl:param name="imageDirPath"/>
   
<!-- Used in extension function for mapping fonts --> 		
<xsl:param name="substituterInstance"/> <!-- select="'passed in'"-->	
<xsl:param name="fontFamilyStack"/> <!-- select="'passed in'"-->
	
<xsl:param name="docxWikiMenu"/>		
<xsl:param name="docID"/>

<xsl:template match="/pkg:package">

		<xsl:variable name="logging" 
			select="java:org.docx4j.convert.out.pdf.PdfConversion.log('/pkg:package')" />

	<xsl:apply-templates select="pkg:part/pkg:xmlData/w:document"/>

  </xsl:template>

  <xsl:template match="w:document">
		<!-- example for a simple fo file. At the beginning the page layout is set.
		  Below fo:root there is always
		- a single fo:layout-master-set which defines one or more page layouts
		- an optional fo:declarations
		- and a sequence of one or more fo:page-sequences containing the text and formatting instructions
		-->
		
		<fo:root>
		
		  <fo:layout-master-set>
		  <!-- fo:layout-master-set defines in its children the page layout:
		       the pagination and layout specifications
		      - page-masters: have the role of describing the intended subdivisions
		                       of a page and the geometry of these subdivisions
		                      In this case there is only a simple-page-master which defines the
		                      layout for all pages of the text
		  -->
		    <!-- layout information 
		    
		    	  TODO: get this from sectPr -->
		    <fo:simple-page-master master-name="simple"
		                  page-height="29.7cm"
		                  page-width="21cm"
		                  margin-top="1cm"
		                  margin-bottom="2cm"
		                  margin-left="2.5cm"
		                  margin-right="2.5cm">
		      <fo:region-body margin-top="3cm"/>
		      <fo:region-before extent="3cm"/>
		      <fo:region-after extent="1.5cm"/>
		    </fo:simple-page-master>
		  </fo:layout-master-set>
		  <!-- end: defines page layout -->
		
		
		  <!-- start page-sequence
		       here comes the text (contained in flow objects)
		       the page-sequence can contain different fo:flows
		       the attribute value of master-name refers to the page layout
		       which is to be used to layout the text contained in this
		       page-sequence-->
		  <fo:page-sequence master-reference="simple">
		
		      <!-- start fo:flow
		           each flow is targeted
		           at one (and only one) of the following:
		           xsl-region-body (usually: normal text)
		           xsl-region-before (usually: header)
		           xsl-region-after  (usually: footer)
		           xsl-region-start  (usually: left margin)
		           xsl-region-end    (usually: right margin)
		           ['usually' applies here to languages with left-right and top-down
		            writing direction like English]
		           in this case there is only one target: xsl-region-body
		        -->
		    <fo:flow flow-name="xsl-region-body">
		
				<xsl:apply-templates select="w:body/*"/>		
		
		
		    </fo:flow> <!-- closes the flow element-->
		  </fo:page-sequence> <!-- closes the page-sequence -->
		</fo:root>
  </xsl:template>

  <!-- each paragraph is encapsulated in a block element
       the attributes of the block define
       font-family and size, line-heigth etc. -->
  <xsl:template match="w:p">
  
  	<xsl:choose>
  		<xsl:when test="w:pPr">
  			<!--  Invoke an extension function, so we can use
  			      docx4j to populate the fo:block -->
  		
			<xsl:variable name="childResults">
				<xsl:apply-templates/>
			</xsl:variable>
			
			<xsl:variable name="pPrNode" select="w:pPr" />  	
			<xsl:variable name="pStyleVal" select="string( w:pPr/w:pStyle/@w:val )" />  	
	
		  	<xsl:copy-of select="java:org.docx4j.convert.out.pdf.viaXSLFO.Conversion.createBlockForPPr( 
		  		$wmlPackage, $pPrNode, $pStyleVal, $childResults)" />
	  		
	  	</xsl:when>
	  	<xsl:otherwise>
	  		<!--  TODO: use sensible defaults here .. -->
		      <fo:block font-size="12pt"
		                font-family="sans-serif"
		                line-height="15pt"
		                space-after.optimum="3pt"
		                text-align="justify">
		        	<xsl:apply-templates/>
		      </fo:block>
	  	</xsl:otherwise>
	  </xsl:choose>					
		
		
  </xsl:template>

  <xsl:template match="w:pPr | w:rPr" /> <!--  handle via extension function -->

  <xsl:template match="w:r">  	
  	<xsl:choose>
  		<xsl:when test="w:rPr">
  			<!--  Invoke an extension function, so we can use
  			      docx4j to populate the fo:block -->
  		
			<xsl:variable name="childResults">
				<xsl:apply-templates/>
			</xsl:variable>
			
			<xsl:variable name="rPrNode" select="w:rPr" />  	
	
		  	<xsl:copy-of select="java:org.docx4j.convert.out.pdf.viaXSLFO.Conversion.createBlockForRPr( 
		  		$wmlPackage, $rPrNode, $childResults)" />
	  		
	  	</xsl:when>
	  	<xsl:otherwise>
        	<xsl:apply-templates/>
	  	</xsl:otherwise>
	  </xsl:choose>					
		
  </xsl:template>

  <xsl:template match="w:t">
  	<xsl:value-of select="."/>
  </xsl:template>  	
  
  <xsl:template match="w:sdt">
  	<xsl:apply-templates select="sdtContent"/>
  </xsl:template>

  <xsl:template match="*">
		      <fo:block font-size="12pt"
		        color="red"
                font-family="sans-serif"
                line-height="15pt"
                space-after.optimum="3pt"
                text-align="justify">
        NOT IMPLEMENTED: support for <xsl:value-of select="local-name(.)"/>
      </fo:block>  
  </xsl:template>
  
  <xsl:template match="w:br[@w:type = 'page']">
  	<fo:block break-before="page"/>
  </xsl:template>
  
  <xsl:template match="w:lastRenderedPageBreak" />
  
  
</xsl:stylesheet>
