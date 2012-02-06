
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

<!--  <xsl:output method="html" encoding="utf-8" omit-xml-declaration="yes" indent="yes"/> -->
   <!-- Use method="html" if you need IE9 to display quotes properly (it
   		still doesn't use UTF 8 though!). Chrome works either way. 
   		NB, with IE9, and XML output, 
   		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
   		doesn't help.-->
   
<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="no" 
doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
      doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>
<!--  indent="no" gives a better result for things like subscripts, because it stops
      the user-agent from replacing a carriage return in the HTML with a space in the output. -->
<!-- TODO strict dtd -->


<xsl:param name="wmlPackage"/> <!-- select="'passed in'"-->	
<xsl:param name="modelStates"/> <!-- select="'passed in'"-->	
<xsl:param name="imageHandler"/>
   
<!-- Used in extension function for mapping fonts --> 		
<xsl:param name="fontMapper"/> <!-- select="'passed in'"-->	
<xsl:param name="fontFamilyStack"/> <!-- select="'passed in'"-->

<xsl:param name="conditionalComments"/> <!-- select="'passed in'"-->


<!-- Allow the user to inject extra stuff into the output HTML --> 		
<xsl:param name="userCSS"/> 		<!-- select="'passed in'"-->
<xsl:param name="userScript"/> 		<!-- select="'passed in'"-->
<xsl:param name="userBodyTop"/> 	<!-- select="'passed in'"-->
<xsl:param name="userBodyTail"/> 	<!-- select="'passed in'"-->
	
<!--  Input to this transform is the Main Document Part. -->


<xsl:template match="/w:document">

	<xsl:variable name="dummy"
		select="java:org.docx4j.convert.out.html.HtmlExporterNG2.log('/pkg:package')" />
		
		
	<html>
		<head>
			<style>
				<xsl:comment>

					/*paged media */ div.header {display: none }
					div.footer {display: none } /*@media print { */
					<xsl:if
						test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasDefaultHeader($wmlPackage, 1)">
						div.header {display: block; position: running(header) }
					</xsl:if>
					<xsl:if
						test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasDefaultFooter($wmlPackage, 1)">
						div.footer {display: block; position: running(footer) }
					</xsl:if>

					@page { size: A4; margin: 10%; @top-center {
					content: element(header) } @bottom-center {
					content: element(footer) } }


					/*font definitions*/

					/*element styles*/ .del
					{text-decoration:line-through;color:red;}
					<xsl:choose>
						<xsl:when test="/w:document/w:settings/w:trackRevisions">
  					          .ins {text-decoration:none;background:#c0ffc0;padding:1px;}
						</xsl:when>
						<xsl:otherwise>
						  .ins {text-decoration:none;background:#c0ffc0;padding:1px;}
						</xsl:otherwise>
					</xsl:choose>


					/* Word style definitions */
					<xsl:copy-of
						select="java:org.docx4j.convert.out.html.HtmlExporterNG2.getCssForStyles( 
		  											$wmlPackage)" />

					/* TABLE CELL STYLES */
					<xsl:variable name="tables" select="./w:body//w:tbl" />
					<xsl:copy-of
						select="java:org.docx4j.convert.out.html.HtmlExporterNG2.getCssForTableCells( 
		  											$wmlPackage, $tables)" />

					
				</xsl:comment>
			</style>

			<xsl:value-of select="$userCSS" disable-output-escaping="yes" />

			<script type="text/javascript">
				<!-- For collapsible content controls -->
				function toggleDiv(divid){
					if(document.getElementById(divid).style.display == 'none'){
						document.getElementById(divid).style.display = 'block';
					}else{
						document.getElementById(divid).style.display = 'none';
					}
				}
								
			</script>
			
			<!-- User script -->
			<xsl:value-of select="$userScript" disable-output-escaping="yes" />
			
		</head>

		<body>
			<xsl:call-template name="pretty-print-block" />

			<xsl:value-of select="$userBodyTop" disable-output-escaping="yes" />

			<xsl:call-template name="pretty-print-block" />

			<!--
				Headers and footers. Note that only the default is supported (ie if
				you are using others they won't appear). To implement support for
				others, you'll need to get the corresponding CSS right. For that,
				see http://www.w3.org/TR/css3-page/#margin-boxes
				http://www.w3.org/TR/2007/WD-css3-gcpm-20070504
				http://www.w3.org/TR/css3-content/ Appropriate extension functions
				similar to the below already exist
			-->
			<xsl:if
				test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasDefaultHeader($wmlPackage, 1)">
				<div class="header">
					<xsl:apply-templates
						select="java:org.docx4j.model.structure.HeaderFooterPolicy.getDefaultHeader($wmlPackage, 1)" />
				</div>
			</xsl:if>

			<!--  Info -->
			<xsl:copy-of
				select="java:org.docx4j.convert.out.html.HtmlExporterNG2.message( 'TO HIDE THESE MESSAGES, TURN OFF log4j debug level logging for org.docx4j.convert.out.html.HtmlExporterNG2 ' )" />

			<xsl:call-template name="pretty-print-block" />

			<div class="document">
				<xsl:apply-templates select="w:body|w:cfChunk" />
			</div>

			<xsl:call-template name="pretty-print-block" />

			<!--  Footnotes and endnotes -->
			<xsl:if
				test="java:org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart.hasFootnotesPart($wmlPackage)">
				<div class="footnotes">
					<xsl:apply-templates
						select="java:org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart.getFootnotes($wmlPackage)" />
				</div>
			</xsl:if>

			<xsl:call-template name="pretty-print-block" />

			<xsl:if
				test="java:org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart.hasEndnotesPart($wmlPackage)">
				<div class="endnotes">
					<xsl:apply-templates
						select="java:org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart.getEndnotes($wmlPackage)" />
				</div>
			</xsl:if>

			<xsl:call-template name="pretty-print-block" />

			<xsl:if
				test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasDefaultFooter($wmlPackage, 1)">
				<div class="footer">
					<xsl:apply-templates
						select="java:org.docx4j.model.structure.HeaderFooterPolicy.getDefaultFooter($wmlPackage, 1)" />
				</div>
			</xsl:if>
			
			<xsl:call-template name="pretty-print-block" />

			<xsl:value-of select="$userBodyTail" disable-output-escaping="yes" />
			
			<xsl:call-template name="pretty-print-block" />
		</body>
	</html>
</xsl:template>

	<xsl:template match="/">
		<xsl:apply-templates select="*" />
	</xsl:template>

	<xsl:template match="w:body">
		<xsl:apply-templates select="*" />
	</xsl:template>

<xsl:template name="pretty-print-block">
  <xsl:text>
  
  </xsl:text>
</xsl:template>
  
  <!--  the extension functions fetch these
        for processing -->
  <xsl:template match="w:hdr|w:ftr">
  	<xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="w:p">
  
  	<xsl:call-template name="pretty-print-block"/>
  
 			<!--  Invoke an extension function, so we can use
 			      docx4j to populate the fo:block -->
 		
		<xsl:variable name="pPrNode" select="w:pPr" />  	 		
		<xsl:variable name="pStyleVal" select="string( w:pPr/w:pStyle/@w:val )" />  

		<xsl:variable name="numId" select="string( w:pPr/w:numPr/w:numId/@w:val )" />  
		<xsl:variable name="levelId" select="string( w:pPr/w:numPr/w:ilvl/@w:val )" />  


		<xsl:variable name="childResults">
			<xsl:choose>
				<xsl:when test="ancestor::w:tbl and count(child::node())=0">
					<!-- A row that has no content will be displayed by browsers
					     (Firefox at least) with a microscopic row height.
					     
					     Rather than put dummy content here - an nbsp or something -
					     i've set a height in the global td style. This could be
					     improved, by only setting it on tr's which need it.  
					
						span>STUFF</span-->
				</xsl:when>
				<xsl:when test="count(child::node())=1 and count(w:pPr)=1">
					<!--  Don't count an empty paragraph -->
					<!--  Don't apply templates, since there is nothing to do. -->
				</xsl:when>				
				<xsl:otherwise>
					<!--  At present, this doesn't use HTML OL|UL and LI;
					      we'll do that when we have a document model to work from -->								
					<xsl:copy-of select="
						java:org.docx4j.convert.out.html.AbstractHtmlExporter.getNumberXmlNode( 
					  					$wmlPackage, $pPrNode, $pStyleVal, $numId, $levelId)" />					
					<xsl:apply-templates/>				
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:variable name="pPrNode" select="w:pPr" />  	

	  	<xsl:copy-of select="
	  		java:org.docx4j.convert.out.html.HtmlExporterNG2.createBlockForPPr( 
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
	
		  	<xsl:copy-of select="java:org.docx4j.convert.out.html.HtmlExporterNG2.createBlockForRPr( 
		  		$wmlPackage, $pStyleVal, $rPrNode, $childResults)" />
	  		
	  	</xsl:when>
	  	<xsl:otherwise>
        	<xsl:apply-templates/>
	  	</xsl:otherwise>
	  </xsl:choose>					
		
  </xsl:template>

  <xsl:template match="w:t[@xml:space='preserve']">
  	<span style="white-space:pre-wrap;"><xsl:value-of select="."/></span>
  	<!--  Good for FF3, and WebKit; not honoured by IE7 though.  Yawn. -->
  </xsl:template>  	

  <xsl:template match="w:t">  	
  	<xsl:value-of select="."/>
  </xsl:template>

<!-- This is an extension point.
     You can register your own SdtTagHandlers, which
     tailor the HTML output based on the content of
     w:sdtPr/w:tag.
     See the SdtWriter class for details. -->
	<xsl:template match="w:sdt">

		<xsl:variable name="childResults">
			<xsl:apply-templates select="w:sdtContent/*" />
		</xsl:variable>

		<xsl:variable name="sdtPrNode" select="./w:sdtPr" />

		<xsl:copy-of
			select="java:org.docx4j.convert.out.html.SdtWriter.toNode(
  			$wmlPackage, $sdtPrNode, 
  			$childResults)" />

	</xsl:template>


  
  <xsl:template match="w:lastRenderedPageBreak" />
  
  
  <!--  TODO - ignored for now -->
  <xsl:template match="w:sectPr"/>

  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  +++++++++++++++++++ image support +++++++++++++++++++++++ -->
  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

  <xsl:template match="w:drawing">
	<xsl:apply-templates select="*"/>
  </xsl:template>

  <xsl:template match="wp:inline|wp:anchor">
  
  	 <xsl:variable name="wpinline" select="."/>
  	
  	<xsl:choose>
  		<!--  sanity check -->
  		<xsl:when test="./a:graphic/a:graphicData/pic:pic">
  		
  	<xsl:copy-of select="java:org.docx4j.model.images.WordXmlPictureE20.createHtmlImgE20( 
  			$wmlPackage, 
  			$imageHandler,
  			$wpinline)" />
  		</xsl:when>
  		<xsl:otherwise>
  		
			<xsl:copy-of 
				select="java:org.docx4j.convert.out.html.HtmlExporterNG2.notImplemented(., ' without pic:pic ' )" />  	  		
  		</xsl:otherwise>  	
  	</xsl:choose>
    
  </xsl:template>
  
    <!--  E1.0 images  -->
	<xsl:template match="w:pict">
	
		<xsl:choose>
			<xsl:when test="./v:shape/v:imagedata">
	
			  	<xsl:variable name="wpict" select="."/>
			  	
			  	<xsl:copy-of select="java:org.docx4j.model.images.WordXmlPictureE10.createHtmlImgE10( 
			  			$wmlPackage, 
			  			$imageHandler,
			  			$wpict)" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:comment>TODO: handle w:pict containing other than ./v:shape/v:imagedata</xsl:comment>
			<xsl:copy-of 
				select="java:org.docx4j.convert.out.html.HtmlExporterNG2.notImplemented(., ' without v:imagedata ' )" />  	  		
			</xsl:otherwise>
		</xsl:choose>  			
	
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
	  	<xsl:call-template name="pretty-print-block"/>

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
	  	<xsl:copy-of select="java:org.docx4j.convert.out.Converter.toNode($tblNode, $childResults, $modelStates)"/>
	  			  		
  </xsl:template>

<xsl:template match="w:tblPr"/>  
<xsl:template match="w:tblPrEx"/>  
<xsl:template match="w:tblGrid"/>  
<xsl:template match="w:tr|w:tc">
	<xsl:copy>
		<!--xsl:apply-templates select="@*"/-->	
		<xsl:apply-templates/>
	</xsl:copy>
</xsl:template>  
<xsl:template match="w:tcPr"/>
<xsl:template match="w:trPr"/>

  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  +++++++++++++++++++  other stuff  +++++++++++++++++++++++ -->
  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->


	<xsl:template match="w:proofErr" />

	<xsl:template match="w:softHyphen">
		<xsl:text>&#xAD;</xsl:text>
	</xsl:template>

	<xsl:template match="w:noBreakHyphen">
		<xsl:text disable-output-escaping="yes">&amp;#8209;</xsl:text>
	</xsl:template>

  <xsl:template match="w:br">
    <br>
      <xsl:attribute name="clear">
        <xsl:choose>
          <xsl:when test="@w:clear">
            <xsl:value-of select="@w:clear"/>
          </xsl:when>
          <xsl:otherwise>all</xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:if test="@w:type = 'page'">
        <xsl:attribute name="style">page-break-before:always</xsl:attribute>
      </xsl:if>
    </br>
  </xsl:template>

<!--
  <xsl:template match="w:br[not(@w:type = 'page')]">
		<xsl:text disable-output-escaping="yes">
		</xsl:text>
  </xsl:template>
  -->
  
  <xsl:template match="w:cr">
	<br clear="all" />
</xsl:template>
  
<!--  <w:sym w:font="Wingdings" w:char="F04A"/> -->
<xsl:template match="w:sym">

	<xsl:variable name="childResults">
		<xsl:apply-templates /> 
	</xsl:variable>

	<xsl:variable name="symNode" select="." />  			

     <xsl:copy-of select="java:org.docx4j.convert.out.Converter.toNode($symNode, 
			$childResults, $modelStates)" />
  		  			
</xsl:template>
  
  <xsl:template name="OutputTlcChar"> <!--  From MS stylesheet -->
    <xsl:param name="count" select="0"/>
    <xsl:param name="tlc" select="' '"/>
    <xsl:value-of select="$tlc"/>
    <xsl:if test="$count > 1">
      <xsl:call-template name="OutputTlcChar">
        <xsl:with-param name="count" select="$count - 1"/>
        <xsl:with-param name="tlc" select="$tlc"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

<!-- 

<w:p>
	<w:pPr><w:tabs><w:tab w:val="left" w:pos="4320"/></w:tabs></w:pPr>
	<w:r><w:t xml:space="preserve">Will tab.. </w:t></w:r><w:r>
	<w:tab/>
	<w:t>3 inches</w:t></w:r>
</w:p>

 -->
<xsl:template match="w:tab"> 
	<!--  Use this simple-minded approach from MS stylesheet,
	      until our document model can do better.   -->
    <xsl:call-template name="OutputTlcChar">
      <xsl:with-param name="tlc">
        <xsl:text disable-output-escaping="yes">&#160;</xsl:text>
      </xsl:with-param>
      <xsl:with-param name="count" select="3"/>
    </xsl:call-template>
  </xsl:template>

<xsl:template match="w:smartTag">
    <xsl:apply-templates />
</xsl:template>

	<xsl:template match="w:smartTagPr" />

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
    <a>
	<xsl:variable name="relId"><xsl:value-of select="string(@r:id)"/></xsl:variable>
      
	<xsl:variable name="hTemp" 
		select="java:org.docx4j.convert.out.html.AbstractHtmlExporter.resolveHref(
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
   
  <xsl:template match="w:bookmarkStart">
    <a name="{@w:name}"/>
  </xsl:template>
   
<xsl:template match="w:bookmarkEnd" />

  <xsl:template match="w:ins">
  	<span class="ins">
  		<xsl:apply-templates/>
  	</span>
  </xsl:template>  	

  <xsl:template match="w:del">
  		<xsl:apply-templates/>
  </xsl:template>  	

  <xsl:template match="w:delText">
  	<span class="del">
  		<xsl:apply-templates/>
  	</span>
  </xsl:template>  	
  
  <xsl:template match="w:footnoteReference">  
    <xsl:variable name="fn"><xsl:value-of select="java:org.docx4j.convert.out.html.HtmlExporterNG2.getNextFootnoteNumber($modelStates)"/></xsl:variable>
  	<span style="vertical-align: top; font-size: xx-small">
  		<!--  Bidirectional --><a name="fs{$fn}"><a href="#fn{$fn}"><xsl:value-of select="$fn"/></a></a></span>  
  </xsl:template>
  <xsl:template match="w:endnoteReference ">  
    <xsl:variable name="fn"><xsl:value-of select="java:org.docx4j.convert.out.html.HtmlExporterNG2.getNextEndnoteNumber($modelStates)"/></xsl:variable>
  	<span style="vertical-align: top; font-size: xx-small">
  		<!--  Bidirectional --><a name="es{$fn}"><a href="#en{$fn}"><xsl:value-of select="$fn"/></a></a></span>  
  </xsl:template>

  <!--  The number in the note itself -->
  <xsl:template match="w:footnoteRef">
    <xsl:variable name="fn"><xsl:value-of select="count(../../../preceding-sibling::*)-1"/></xsl:variable>
  	<span style="vertical-align: top; font-size: xx-small"><a name="fn{$fn}"><a href="#fs{$fn}"><xsl:value-of select="$fn"/></a></a></span>      
  </xsl:template>  
  <xsl:template match="w:endnoteRef">
    <xsl:variable name="fn"><xsl:value-of select="count(../../../preceding-sibling::*)-1"/></xsl:variable>
  	<span style="vertical-align: top; font-size: xx-small"><a name="en{$fn}"><a href="#es{$fn}"><xsl:value-of select="$fn"/></a></a></span>      
  </xsl:template>  

  <xsl:template match="w:footnotes | w:endnotes">
  		<xsl:apply-templates/>  
  </xsl:template>
  
  <xsl:template match="w:footnote[@w:id='0'] | w:endnote[@w:id='0']"/>
  
  <xsl:template match="w:footnote[@w:id!='0'] | w:endnote[@w:id!='0']">
  	<xsl:apply-templates/>  
  </xsl:template>
  
<!--  tmp bookmarks -->

  <xsl:template match="w:fldSimple" >
		<xsl:copy-of 
			select="java:org.docx4j.convert.out.html.HtmlExporterNG2.notImplemented(., 'no support for fields' )" />  	
  </xsl:template>
  <xsl:template match="w:fldChar" >
		<xsl:copy-of 
			select="java:org.docx4j.convert.out.html.HtmlExporterNG2.notImplemented(., '' )" />  	
  </xsl:template>
  <xsl:template match="w:instrText" >
		<xsl:copy-of 
			select="java:org.docx4j.convert.out.html.HtmlExporterNG2.notImplemented(., 'no support for fields' )" />  	
  </xsl:template>

  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  +++++++++++++++++++  no match     +++++++++++++++++++++++ -->
  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

  <xsl:template match="*[ancestor::w:body]" priority="-1"> <!--  ignore eg page number field in footer -->
		<xsl:copy-of 
			select="java:org.docx4j.convert.out.html.HtmlExporterNG2.notImplemented(., '' )" />  	      		 
  </xsl:template>
   
</xsl:stylesheet>
