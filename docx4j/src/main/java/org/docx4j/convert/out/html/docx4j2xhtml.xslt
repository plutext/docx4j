
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
    version="1.0"
        exclude-result-prefixes="java w a o v WX aml w10 pkg wp pic">	
        
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
<xsl:param name="fontMapper"/> <!-- select="'passed in'"-->	
<xsl:param name="fontFamilyStack"/> <!-- select="'passed in'"-->

<xsl:param name="conditionalComments"/> <!-- select="'passed in'"-->
	

<xsl:param name="docxWikiMenu"/>		
<!-- 
<xsl:param name="docxWiki"/>		
<xsl:param name="docxWikiSdtID"/>		
<xsl:param name="docxWikiSdtVersion"/>
 -->		
<xsl:param name="docID"/>


<xsl:template match="/w:document">

			<xsl:variable name="dummy" 
				select="java:org.docx4j.convert.out.html.HtmlExporter.log('/pkg:package')" />


    <html>
      <head>
		
        <style>
          <xsl:comment>

						/*paged media */ 
						div.header {display: none }
						div.footer {display: none } 
						/*@media print { */
						<xsl:if
							test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasDefaultHeader($wmlPackage)">
							div.header {display: block; position: running(header) }
						</xsl:if>
						<xsl:if
							test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasDefaultFooter($wmlPackage)">
							div.footer {display: block; position: running(footer) }
						</xsl:if>

						@page { size: A4; margin: 10%; @top-center {
						content: element(header) } @bottom-center {
						content: element(footer) } }


						/*font definitions*/

						/*element styles*/ del
						{text-decoration:line-through;color:red;}
						<xsl:choose>
							<xsl:when
								test="/w:document/w:settings/w:trackRevisions">
								ins
								{text-decoration:underline;color:teal;}
							</xsl:when>
							<xsl:otherwise>
								ins {text-decoration:none;}
							</xsl:otherwise>
						</xsl:choose>

						/*class styles*/

						<xsl:if test="$docxWikiMenu=true()">
							/*docxwiki*/ .docxwiki-headline { color:
							black; background: none; font-weight:
							normal; margin: 0; padding-top: .5em;
							padding-bottom: .17em; border-bottom: 1px
							solid #aaa; }

							.editsection { font-size: 80%; font-weight:
							normal; }

							div.editsection { float: right; margin-left:
							5px; }
						</xsl:if>
						
						/* Word style definitions */
						<xsl:copy-of select="java:org.docx4j.convert.out.html.HtmlExporterNG.getCssForStyles( 
		  											$wmlPackage)"/>


          </xsl:comment>
        </style>
      </head>

      <body>

		<!--  Headers and footers.
		      Note that only the default is supported (ie if you are using
		      others they won't appear).  To implement support for others,
		      you'll need to get the corresponding CSS right.  For that, see
		         http://www.w3.org/TR/css3-page/#margin-boxes 
				 http://www.w3.org/TR/2007/WD-css3-gcpm-20070504		         
		         http://www.w3.org/TR/css3-content/
		      Appropriate extension functions similar to the below already exist 
		       -->
		<xsl:if
			test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasDefaultHeader($wmlPackage)">
			<div class="header">
				<xsl:apply-templates
					select="java:org.docx4j.model.structure.HeaderFooterPolicy.getDefaultHeader($wmlPackage)" />
			</div>
		</xsl:if>
		<xsl:if
			test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasDefaultFooter($wmlPackage)">
			<div class="footer">
				<xsl:apply-templates
					select="java:org.docx4j.model.structure.HeaderFooterPolicy.getDefaultFooter($wmlPackage)" />
			</div>
		</xsl:if>
             
        <xsl:if test="$docxWikiMenu='true'">        
			<div style="text-align:right">
				<a href="/alfresco/docxwiki/edit{$docID}">edit</a>, 
				<a href="{$docID}">download</a>, 
				<a href="/alfresco/docx2web{$docID}">(ttw)</a> 				
			</div>        
        </xsl:if>

		<xsl:apply-templates select="w:body|w:cfChunk"/>

      </body>
    </html>
  </xsl:template>

  <xsl:template match="/">
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <xsl:template match="w:body">
    <xsl:apply-templates select="*"/>
  </xsl:template>

  
  <!--  the extension functions fetch these
        for processing -->
  <xsl:template match="w:hdr|w:ftr">
  	<xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="w:p">
  
 			<!--  Invoke an extension function, so we can use
 			      docx4j to populate the fo:block -->
 		
		<xsl:variable name="pStyleVal" select="string( w:pPr/w:pStyle/@w:val )" />  

		<xsl:variable name="numId" select="string( w:pPr/w:numPr/w:numId/@w:val )" />  
		<xsl:variable name="levelId" select="string( w:pPr/w:numPr/w:ilvl/@w:val )" />  


		<xsl:variable name="childResults">
			<!--  Numbering; consider further what CSS to use,
			      and whether this should be done in createBlockForPPr extension -->
			<span> <!--  TODO span should be inside extension, so we don't get it if
			             there is no number.  But this simple way of numbering
			             should be formatted using better css? -->								
			  	<xsl:copy-of select="java:org.docx4j.convert.out.html.HtmlExporter.getNumberXmlNode( $wmlPackage, 
			  			$pStyleVal, $numId, $levelId)" />					
			</span>
			<xsl:apply-templates/>
		</xsl:variable>



		
		<xsl:variable name="pPrNode" select="w:pPr" />  	
		

	  	<xsl:copy-of select="java:org.docx4j.convert.out.html.HtmlExporterNG.createBlockForPPr( 
	  		$wmlPackage, $pPrNode, $pStyleVal, $childResults)" />
		
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
			
			<xsl:variable name="pStyleVal" select="string( ../w:pPr/w:pStyle/@w:val )" />  			
			
			<xsl:variable name="rPrNode" select="w:rPr" />  	
	
		  	<xsl:copy-of select="java:org.docx4j.convert.out.html.HtmlExporterNG.createBlockForRPr( 
		  		$wmlPackage, $pStyleVal, $rPrNode, $childResults)" />
	  		
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
  	<xsl:apply-templates select="w:sdtContent/*"/>
  </xsl:template>


  <xsl:template match="w:br[@w:type = 'page']">
  	<!--  TODO -->
  </xsl:template>
  
  <xsl:template match="w:lastRenderedPageBreak" />
  
<!-- 
  
		<w:hyperlink r:id="rId4" w:history="true">
			<w:r>
				<w:rPr>
				    <w:rStyle w:val="Hyperlink"/>
				</w:rPr>
				<w:t>hyperlink</w:t>
			</w:r>
		</w:hyperlink>
-->  
  <xsl:template match="w:hyperlink">
    <a style="text-decoration:none;">
	<xsl:variable name="relId"><xsl:value-of select="string(@r:id)"/></xsl:variable>
      
	<xsl:variable name="hTemp" 
		select="java:org.docx4j.convert.out.html.HtmlExporter.resolveHref(
		             $wmlPackage, $relId )" />
		                   
      <xsl:variable name="href">
          <xsl:value-of select="$hTemp"/>
        <xsl:choose>
          <xsl:when test="@w:anchor">
            #<xsl:value-of select="@w:anchor"/>
          </xsl:when>
          <xsl:when test="@w:bookmark">
            #<xsl:value-of select="@w:bookmark"/>
          </xsl:when>
          <xsl:when test="@w:arbLocation">
            # <xsl:value-of select="@w:arbLocation"/>
          </xsl:when>
        </xsl:choose>
      </xsl:variable>
      <xsl:if test="not(href='')">
        <xsl:attribute name="href">
          <xsl:value-of select="$href"/>
        </xsl:attribute>
      </xsl:if>
      
<!-- 
      <xsl:for-each select="@w:tgtFrame">
        <xsl:attribute name="target">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:for-each select="@w:tooltip">
        <xsl:attribute name="title">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
 -->
 		<xsl:apply-templates />      
    </a>
  </xsl:template>
  
  <!--  TODO - ignored for now -->
  <xsl:template match="w:sectPr"/>

  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  +++++++++++++++++++ image support +++++++++++++++++++++++ -->
  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

  <xsl:template match="w:drawing">
	<xsl:apply-templates select="*"/>
  </xsl:template>

  <xsl:template match="wp:inline|wp:anchor">
  
  	<xsl:variable name="pictureData" select="./a:graphic/a:graphicData/pic:pic/pic:blipFill"/>
  	<xsl:variable name="picSize" select="./wp:extent"/>
  	<xsl:variable name="picLink" select="./wp:docPr/a:hlinkClick"/>
  	<xsl:variable name="linkDataNode" select="./a:graphic/a:graphicData/pic:pic/pic:blipFill/a:blip"/>
  	
  	<xsl:copy-of select="java:org.docx4j.model.images.WordXmlPicture.createHtmlImgE20( $wmlPackage, string($imageDirPath),
  			$pictureData, $picSize, $picLink, $linkDataNode)" />
    
  </xsl:template>

  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  +++++++++++++++++++ table support +++++++++++++++++++++++ -->
  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->


<!-- 
                    <w:tbl>
                        <w:tblPr>
                            <w:tblStyle w:val="TableGrid"/>
                            <w:tblW w:type="auto" w:w="0"/>
                            <w:tblLook w:val="04A0"/>
                        </w:tblPr>
                        <w:tblGrid>
                            <w:gridCol w:w="3561"/>
                            <w:gridCol w:w="3561"/>
                            <w:gridCol w:w="3561"/>
                        </w:tblGrid>
                        <w:tr>
                            <w:tc>
                                <w:tcPr>

 -->
  <xsl:template match="w:tbl">

		<xsl:variable name="tblNode" select="." />  			

		<xsl:variable name="childResults">
			<xsl:apply-templates /> <!-- select="*[not(name()='w:tblPr' or name()='w:tblGrid')]" /-->
		</xsl:variable>

<!--
		<xsl:comment>debug start</xsl:comment>
			<xsl:copy-of select="$childResults"/>
		<xsl:comment>debug end</xsl:comment>
  -->
  
		<!--  Create the HTML table in Java --> 
	  	<xsl:copy-of select="java:org.docx4j.convert.out.Converter.toNode($tblNode, $childResults)"/>
	  			  		
  </xsl:template>

<xsl:template match="w:tblPr"/>  
<xsl:template match="w:tblGrid"/>  
<xsl:template match="w:tr|w:tc">
	<xsl:copy>
		<!--xsl:apply-templates select="@*"/-->	
		<xsl:apply-templates/>
	</xsl:copy>
</xsl:template>  
<xsl:template match="w:tcPr"/>
<xsl:template match="w:trPr"/>


   
  <xsl:template match="*">
		      <div
		        color="red">
        NOT IMPLEMENTED: support for <xsl:value-of select="local-name(.)"/>
      		</div> 
      		 
  </xsl:template>
   
   
</xsl:stylesheet>
