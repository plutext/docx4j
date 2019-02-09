
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns="http://www.w3.org/1999/xhtml" 
    xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"
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
	xmlns:xlink="http://www.w3.org/1999/xlink"
    version="1.0"
        exclude-result-prefixes="java w a o v WX aml w10 pkg wp pic xlink r p">	
<!-- 	xmlns="http://www.w3.org/2000/svg"
 -->        
        <!--  Note definition of xmlns:r is different 
              from the definition in an _rels file
              (where it is http://schemas.openxmlformats.org/package/2006/relationships)  -->

<!-- 
	<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="no" />
   -->
<!--  indent="no" gives a better result for things like subscripts, because it stops
      the user-agent from replacing a carriage return in the HTML with a space in the output. 
      
      but, for now, make indent yes-->
<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="yes" 
doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
     doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>

<!-- either strict or transitional work for inline SVG 

	NB: file suffix must end with .xhtml in order to see the SVG in a browser

--> 

<!--  Input to this transform is a Shape Tree p:spTree. 

	Output is SVG in HTML.

 	<text x="{$x}" y="{$y}"><xsl:value-of select="a:p/a:r/a:t"/></text>

	  textArea (SVG 1.2 Tiny) - doesn't work in Chrome 4.0.249.64 or FF 3.5.7
      so its not a feasible solution as at January 2010 :-(  
      See http://www.w3.org/TR/SVGTiny12/examples/textArea01.svg
      from http://www.w3.org/TR/SVGTiny12/text.html#TextInAnArea
      Opera 9.5 apparently supports it though!

	  Inkscape uses flowPara, but browsers can't render this.
      
      Could use mixed svg html, but that's unsatisfactory.  Will have to do for now.
      Either svg in html, or html foreignObject in svg. 

      carto textFlow.js works nicely in FF, but not Chrome :-(

-->

	<xsl:param name="conversionContext"/> <!-- select="'passed in'"-->	
	


<xsl:template match="/">
	<html xmlns="http://www.w3.org/1999/xhtml" 
      xmlns:svg="http://www.w3.org/2000/svg"
      xmlns:xlink="http://www.w3.org/1999/xlink">
		<head>
			<title>Slide output proof of concept</title>
	        <style>
	          <xsl:comment>
							
					/* Word style definitions */
					<xsl:copy-of select="java:org.pptx4j.convert.out.svginhtml.SvgExporter.getCssForStyles( 
	  											$conversionContext)"/>
	
	          </xsl:comment>
	        </style>
			
		</head>
		<body>
			<xsl:apply-templates/>		
		</body>
	</html>	
</xsl:template>

<xsl:template match="p:spTree">
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="p:nvGrpSpPr"/>
<xsl:template match="p:grpSpPr"/>

 <xsl:template match="p:sp">
 	<xsl:apply-templates select="p:txBody"/>
 </xsl:template>

 <xsl:template match="p:txBody">
 
    <!--  I'd like to get JAXB representation of a:lstStyle just once,
          then pass it in to each a:p as a parameter,
          but Xalan doesn't seem to allow an arbitrary
          Java object to be passed around like that?
          
	    	<xsl:variable name="lstStyle"><xsl:value-of select="java:org.pptx4j.convert.out.svginhtml.SvgExporter.unmarshalFormatting(a:lstStyle)"/></xsl:variable>
	    
	    	with value-of, it becomes a string; with copy-of, its a node list.
	    
	 	<xsl:apply-templates select="a:p">
	 		<xsl:with-param name="lstStyle" select="$lstStyle"/>
	 	</xsl:apply-templates>

           -->
 
 	<!--  Convert from EMU at 96dpi -->
 	<xsl:variable name="x"><xsl:value-of select="round(number(../p:spPr/a:xfrm/a:off/@x)*96 div 914400)"/></xsl:variable>
 	<xsl:variable name="y"><xsl:value-of select="round(number(../p:spPr/a:xfrm/a:off/@y)*96 div 914400)"/></xsl:variable>
 	<xsl:variable name="cx"><xsl:value-of select="round(number(../p:spPr/a:xfrm/a:ext/@cx)*96 div 914400)"/></xsl:variable>
 	<xsl:variable name="cy"><xsl:value-of select="round(number(../p:spPr/a:xfrm/a:ext/@cy)*96 div 914400)"/></xsl:variable>
 	
 	<!--  At present, docx4j doesn't do text boxes in its docx html,
 	      so handle the box here.  -->
 	<xsl:choose>
 		<xsl:when test="java:org.pptx4j.convert.out.svginhtml.SvgExporter.isDebugEnabled()">
 			
 			<!-- Use border: red dashed -->
 			
		 	<div style="position: absolute; width:{$cx}px; height:{$cy}px; left:{$x}px; top:{$y}px; border: red dashed;">
	 			<xsl:apply-templates select="a:p"/>
 			</div>
 		
 		</xsl:when>
 		<xsl:otherwise>
 		
		 	<div style="position: absolute; width:{$cx}px; height:{$cy}px; left:{$x}px; top:{$y}px;">
	 			<xsl:apply-templates select="a:p"/>
 			</div> 		
 		
 		</xsl:otherwise>
 	</xsl:choose>
 	      
 </xsl:template>

 <xsl:template match="p:cxnSp">

		<xsl:variable name="shape" select="."/>

	  	<xsl:copy-of select="java:org.pptx4j.convert.out.svginhtml.SvgExporter.shapeToSVG(
	  		$conversionContext, $shape)" />
 
 </xsl:template>


 <xsl:template match="a:p">
		<xsl:variable name="lvl"><xsl:value-of select="number(a:pPr/@lvl)"/></xsl:variable>
		<xsl:variable name="cNvPrName"><xsl:value-of select="string(../../p:nvSpPr/p:cNvPr/@name)"/></xsl:variable>
		<xsl:variable name="phType"><xsl:value-of select="string(../../p:nvSpPr/p:nvPr/p:ph/@type)"/></xsl:variable>
		<xsl:variable name="childResults"><xsl:apply-templates select="a:r"/></xsl:variable>
		<xsl:variable name="lvlPPr">
			<xsl:choose>
				<xsl:when test="count(../a:lstStyle)=0"/>
				<xsl:when test="a:pPr/@lvl"><xsl:copy-of select="../a:lstStyle/*[$lvl]"/></xsl:when>
				<xsl:otherwise><xsl:copy-of select="../a:lstStyle/*[1]"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
	  	<xsl:copy-of select="java:org.pptx4j.convert.out.svginhtml.SvgExporter.createBlockForP(
	  		$conversionContext,
	  		$lvl,
	  		$cNvPrName, $phType,
	  		 $childResults, $lvlPPr)" />
 </xsl:template>

 <xsl:template match="a:r">
		<xsl:variable name="rPr" select="a:rPr"/>
		<xsl:variable name="childResults"><xsl:apply-templates select="a:t"/></xsl:variable>
	  	<xsl:copy-of select="java:org.pptx4j.convert.out.svginhtml.SvgExporter.createBlockForR(
	  		$conversionContext, $rPr,
	  		 $childResults)" />
 </xsl:template>

  <xsl:template match="a:t">  	
  	<xsl:value-of select="."/>
  </xsl:template>  	

  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  +++++++++++++++++++ image support +++++++++++++++++++++++ -->
  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->


  <xsl:template match="p:pic">
    		
  	<xsl:copy-of select="java:org.pptx4j.convert.out.svginhtml.PictureExporter.createHtmlImg( 
  			$conversionContext,
  			.)" />
    
  </xsl:template>

  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  +++++++++++++++++++  no match     +++++++++++++++++++++++ -->
  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

  <xsl:template match="*" priority="-1"> 
		      <div
		        color="red">
        NOT IMPLEMENTED: support for <xsl:value-of select="local-name(.)"/>
      		</div> 
      		 
  </xsl:template>

   
</xsl:stylesheet>
