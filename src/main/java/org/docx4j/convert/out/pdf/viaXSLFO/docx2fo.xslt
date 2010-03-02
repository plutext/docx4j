
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
        exclude-result-prefixes="java w a o v WX aml w10 pkg wp pic r">	

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

     ======================================= 
     
     TODO:  w:hyperlink, cross references.
     
     -->
        
        <!--  Note definition of xmlns:r is different 
              from the definition in an _rels file
              (where it is http://schemas.openxmlformats.org/package/2006/relationships)  -->


<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="no" />
<!--  indent="no" gives a better result for things like subscripts, because it stops
      the user-agent from replacing a carriage return in the XSL FO with a space in the output. -->

<xsl:param name="wmlPackage"/> <!-- select="'passed in'"-->	
<xsl:param name="modelStates"/> <!-- select="'passed in'"-->	
<xsl:param name="imageDirPath"/>
   
<!-- Used in extension function for mapping fonts --> 		
<xsl:param name="substituterInstance"/> <!-- select="'passed in'"-->	
<xsl:param name="fontFamilyStack"/> <!-- select="'passed in'"-->
	
<xsl:param name="docxWikiMenu"/>		
<xsl:param name="docID"/>

<xsl:template name="pretty-print-block">
  <xsl:text>
  
  </xsl:text>
</xsl:template>

<!--  Not used, if we just pass in document.xml -->
<xsl:template match="/pkg:package">

	<xsl:apply-templates select="pkg:part/pkg:xmlData/w:document"/>

  </xsl:template>

  <xsl:template match="w:document">
  
		<xsl:variable name="logging" 
			select="java:org.docx4j.convert.out.pdf.PdfConversion.log('/pkg:package')" />

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
					
					We handle headers/footers, but not yet on a per section basis.	
					
					See http://www.dpawson.co.uk/xsl/sect3/headers.html				
				-->

					<!--  First Page -->
					<xsl:if
						test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasFirstHeaderOrFooter($wmlPackage)">
						<fo:simple-page-master master-name="firstpage"
							page-height="297mm" page-width="210mm" margin-top="10mm"
							margin-bottom="10mm" margin-left="25mm" margin-right="20mm">
							
							<fo:region-body margin-top="20mm"
								margin-bottom="20mm" margin-left="0mm" margin-right="0mm" />


							<!--  First Page Header -->
							<xsl:if
								test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasFirstHeader($wmlPackage)">
								<fo:region-before
									region-name="xsl-region-before-firstpage" extent="10mm" />
							</xsl:if>

							<!--  First Page Footer -->
							<xsl:if
								test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasFirstFooter($wmlPackage)">
								<fo:region-after
									region-name="xsl-region-after-firstpage" extent="10mm" />
							</xsl:if>

						</fo:simple-page-master>
					</xsl:if>
					<xsl:if
						test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasEvenOrOddHeaderOrFooter($wmlPackage)">
						<!-- layout for the even page -->
						<fo:simple-page-master master-name="evenpage"
							page-height="297mm" page-width="210mm" margin-top="10mm"
							margin-bottom="10mm" margin-left="25mm" margin-right="20mm">
							
							<fo:region-body margin-top="20mm"
								margin-bottom="20mm" margin-left="0mm" margin-right="0mm" />
							<xsl:if
								test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasEvenHeader($wmlPackage)">
								<fo:region-before
									region-name="xsl-region-before-evenpage" extent="10mm" />
							</xsl:if>
							<xsl:if
								test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasEvenFooter($wmlPackage)">
								<fo:region-after
									region-name="xsl-region-after-evenpage" extent="10mm" />
							</xsl:if>
						</fo:simple-page-master>
						<!-- layout for the odd page -->
						<fo:simple-page-master master-name="oddpage"
							page-height="297mm" page-width="210mm" margin-top="10mm"
							margin-bottom="10mm" margin-left="25mm" margin-right="20mm">
							
							<fo:region-body margin-top="20mm"
								margin-bottom="20mm" margin-left="0mm" margin-right="0mm" />
							<xsl:if
								test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasOddHeader($wmlPackage)">
								<fo:region-before
									region-name="xsl-region-before-default" extent="10mm" />
							</xsl:if>
							<xsl:if
								test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasOddFooter($wmlPackage)">
								<fo:region-after
									region-name="xsl-region-after-default" extent="10mm" />
							</xsl:if>
						</fo:simple-page-master>
					</xsl:if>
					<xsl:if
						test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasDefaultHeaderOrFooter($wmlPackage)">
						<fo:simple-page-master master-name="default"
							page-height="297mm" page-width="210mm" margin-top="10mm"
							margin-bottom="10mm" margin-left="25mm" margin-right="20mm">
							
							<fo:region-body margin-top="20mm"
								margin-bottom="20mm" margin-left="0mm" margin-right="0mm" />
							<xsl:if
								test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasDefaultHeader($wmlPackage)">
								<fo:region-before
									region-name="xsl-region-before-default" extent="10mm" />
							</xsl:if>
							<xsl:if
								test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasDefaultFooter($wmlPackage)">
								<fo:region-after
									region-name="xsl-region-after-default" extent="10mm" />
							</xsl:if>
						</fo:simple-page-master>
					</xsl:if>

					<fo:simple-page-master master-name="simple"
						page-height="29.7cm" page-width="21cm" margin-top="1cm"
						margin-bottom="2cm" margin-left="2.5cm" margin-right="2.5cm">
						<fo:region-body margin-top="3cm" />
						<fo:region-before extent="3cm" />
						<fo:region-after extent="1.5cm" />
					</fo:simple-page-master>


				<fo:page-sequence-master master-name="twoside">

					<fo:repeatable-page-master-alternatives>
							<xsl:if
								test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasFirstHeaderOrFooter($wmlPackage)">
								<fo:conditional-page-master-reference
									master-reference="firstpage" page-position="first" />
							</xsl:if>
							
							<xsl:choose>
								<xsl:when test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasEvenHeaderOrFooter($wmlPackage)">
									<fo:conditional-page-master-reference
										master-reference="evenpage" odd-or-even="even" />
									<fo:conditional-page-master-reference
										master-reference="oddpage" odd-or-even="odd" />
								</xsl:when>
								<xsl:when test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasDefaultHeaderOrFooter($wmlPackage)">
									<fo:conditional-page-master-reference
										master-reference="default" />	
								</xsl:when>
								<xsl:otherwise>
									<fo:conditional-page-master-reference
										master-reference="simple" />																
								</xsl:otherwise>
							</xsl:choose>
<!-- 
							<xsl:if
								test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasDefaultFooter($wmlPackage)">
								<fo:conditional-page-master-reference
									master-reference="default" />
							</xsl:if>
							<xsl:if
								test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasOddHeaderOrFooter($wmlPackage)">
								<fo:conditional-page-master-reference
									master-reference="oddpage" odd-or-even="odd" />
							</xsl:if>
							<xsl:if
								test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasEvenHeaderOrFooter($wmlPackage)">
								<fo:conditional-page-master-reference
									master-reference="evenpage" odd-or-even="even" />
							</xsl:if>
 -->							
					</fo:repeatable-page-master-alternatives>
				</fo:page-sequence-master>
			</fo:layout-master-set>

			<!-- start page-sequence
				here comes the text (contained in flow objects)
				the page-sequence can contain different fo:flows
				the attribute value of master-name refers to the page layout
				which is to be used to layout the text contained in this
				page-sequence-->
			<fo:page-sequence master-reference="twoside">

				<!--  First Page Header -->
				<xsl:if
					test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasFirstHeader($wmlPackage)">
					<fo:static-content
						flow-name="xsl-region-before-firstpage">

						<xsl:apply-templates
							select="java:org.docx4j.model.structure.HeaderFooterPolicy.getFirstHeader($wmlPackage)" />

					</fo:static-content>
				</xsl:if>

				<!--  First Page Footer -->
				<xsl:if
					test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasFirstFooter($wmlPackage)">

					<fo:static-content
						flow-name="xsl-region-after-firstpage">
						<xsl:apply-templates
							select="java:org.docx4j.model.structure.HeaderFooterPolicy.getFirstFooter($wmlPackage)" />
					</fo:static-content>
				</xsl:if>
				<xsl:if
					test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasEvenHeader($wmlPackage)">
					<fo:static-content
						flow-name="xsl-region-before-evenpage">
						<xsl:apply-templates
							select="java:org.docx4j.model.structure.HeaderFooterPolicy.getEvenHeader($wmlPackage)" />
					</fo:static-content>
				</xsl:if>
				<xsl:if
					test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasEvenFooter($wmlPackage)">
					<fo:static-content
						flow-name="xsl-region-after-evenpage">
						<xsl:apply-templates
							select="java:org.docx4j.model.structure.HeaderFooterPolicy.getEvenFooter($wmlPackage)" />
					</fo:static-content>
				</xsl:if>
<!-- 				
				<xsl:if
					test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasOddHeader($wmlPackage)">
					<fo:static-content
						flow-name="xsl-region-before-default">
						<xsl:apply-templates
							select="java:org.docx4j.model.structure.HeaderFooterPolicy.getOddHeader($wmlPackage)" />
					</fo:static-content>
				</xsl:if>				
				<xsl:if
					test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasOddFooter($wmlPackage)">
					<fo:static-content
						flow-name="xsl-region-after-default">
						<xsl:apply-templates
							select="java:org.docx4j.model.structure.HeaderFooterPolicy.getOddFooter($wmlPackage)" />
					</fo:static-content>
				</xsl:if>
 -->				
				<xsl:if
					test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasDefaultHeader($wmlPackage)">
					<fo:static-content
						flow-name="xsl-region-before-default">
						<xsl:apply-templates
							select="java:org.docx4j.model.structure.HeaderFooterPolicy.getDefaultHeader($wmlPackage)" />
					</fo:static-content>
				</xsl:if>
				<xsl:if
					test="java:org.docx4j.model.structure.HeaderFooterPolicy.hasDefaultFooter($wmlPackage)">
					<fo:static-content
						flow-name="xsl-region-after-default">
						<xsl:apply-templates
							select="java:org.docx4j.model.structure.HeaderFooterPolicy.getDefaultFooter($wmlPackage)" />
					</fo:static-content>
				</xsl:if>
				
				<xsl:if
					test="java:org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart.hasFootnotesPart($wmlPackage)">
					<fo:static-content flow-name="xsl-footnote-separator">
					    <fo:block>
					      <fo:leader leader-pattern="rule"
					                 leader-length="100%"
					                 rule-style="solid"
					                 rule-thickness="0.5pt"/>
					    </fo:block>
					  </fo:static-content>				
						</xsl:if>

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

					<xsl:apply-templates select="w:body/*" />

				  	<xsl:call-template name="pretty-print-block"/>

					<xsl:if
						test="java:org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart.hasEndnotesPart($wmlPackage)">
						
				        <fo:block space-before="44pt" font-weight="bold" font-size="14pt">
				          <xsl:text>Endnotes</xsl:text>
				        </fo:block>
						
						<xsl:apply-templates
								select="java:org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart.getEndnotes($wmlPackage)" />
					</xsl:if>

				  	<xsl:call-template name="pretty-print-block"/>

				</fo:flow><!-- closes the flow element-->
			</fo:page-sequence><!-- closes the page-sequence -->

			<!-- end: defines page layout -->




		</fo:root>
  </xsl:template>

  <!--  the extension functions fetch these
        for processing -->
  <xsl:template match="w:hdr|w:ftr">
  	<xsl:apply-templates/>
  </xsl:template>

  <!-- each paragraph is encapsulated in a block element
       the attributes of the block define
       font-family and size, line-heigth etc. -->
  <xsl:template match="w:p">
  
    	<xsl:call-template name="pretty-print-block"/>
  
 			<!--  Invoke an extension function, so we can use
 			      docx4j to populate the fo:block -->
 		
		<xsl:variable name="childResults">
			<xsl:apply-templates/>
		</xsl:variable>
		
		<xsl:variable name="pPrNode" select="w:pPr" />  	
		<xsl:variable name="pStyleVal" select="string( w:pPr/w:pStyle/@w:val )" />  	

	  	<xsl:copy-of select="java:org.docx4j.convert.out.pdf.viaXSLFO.Conversion.createBlockForPPr( 
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
			
			<!-- <xsl:variable name="pPrNode" select="../w:pPr" />  -->  	
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

  <xsl:template match="w:ins">
  	<fo:inline color="blue"
  			   text-decoration="underline">
  		<xsl:apply-templates/>
  	</fo:inline>
  </xsl:template>  	

  <xsl:template match="w:del">
  		<xsl:apply-templates/>
  </xsl:template>  	

  <xsl:template match="w:delText">
  	<fo:inline color="red"
  				text-decoration="line-through">
  		<xsl:apply-templates/>
  	</fo:inline>
  </xsl:template>  	
  
  <xsl:template match="w:sdt">
  	<xsl:apply-templates select="w:sdtContent/*"/>
  </xsl:template>


  
  <xsl:template match="w:lastRenderedPageBreak" />

  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  +++++++++++++++++++ image support +++++++++++++++++++++++ -->
  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

  <xsl:template match="w:drawing">
	<xsl:apply-templates select="*"/>
  </xsl:template>

  <xsl:template match="wp:inline|wp:anchor">
  
  	<xsl:variable name="wpinline" select="."/>
  	
   	<xsl:copy-of select="java:org.docx4j.model.images.WordXmlPicture.createXslFoImgE20( 
   			$wmlPackage, string($imageDirPath),
  			$wpinline)" />
    
  </xsl:template>
  

<xsl:template match="w:pict">

	<xsl:choose>
		<xsl:when test="./v:shape/v:imagedata">

	  	<xsl:variable name="wpict" select="."/>
		  	
		  	<xsl:copy-of select="java:org.docx4j.model.images.WordXmlPicture.createXslFoImgE10( 
		  	$wmlPackage, string($imageDirPath),
  			$wpict)" />
		</xsl:when>
		<xsl:otherwise>
			<xsl:comment>TODO: handle w:pict containing other than ./v:shape/v:imagedata</xsl:comment>
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

		<xsl:variable name="childResults">
			<xsl:apply-templates /> <!--  select="*[not(name()='w:tblPr' or name()='w:tblGrid')]" /-->
		</xsl:variable>

		<xsl:variable name="tblNode" select="." />  			

		<!--  Create the XSL FO table in Java -->
	  	<xsl:copy-of select="java:org.docx4j.convert.out.Converter.toNode($tblNode, $childResults, $modelStates)"/>	  		
	  			  		
  </xsl:template>
  
<xsl:template match="w:tblPr|w:trPr"/>  
<xsl:template match="w:tblGrid"/>  
<xsl:template match="w:tr|w:tc">
	<xsl:copy>
		<!--xsl:apply-templates select="@*"/-->	
		<xsl:apply-templates/>
	</xsl:copy>
</xsl:template>  
<xsl:template match="w:tcPr"/>

  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  +++++++++++++++++++  other stuff  +++++++++++++++++++++++ -->
  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

  
<xsl:template match="w:sectPr">
	<xsl:comment>sectPr ignored</xsl:comment>
</xsl:template>


	<xsl:template match="w:proofErr" />

	<xsl:template match="w:softHyphen">
		<xsl:text>&#xAD;</xsl:text>
	</xsl:template>

	<xsl:template match="w:noBreakHyphen">
		<xsl:text disable-output-escaping="yes">&amp;#8209;</xsl:text>
	</xsl:template>

	<xsl:template match="w:br">
		<xsl:choose>
			<xsl:when test="@w:type = 'page'">
				<fo:block break-before="page" />
			</xsl:when>
			<xsl:otherwise>
				<fo:block white-space-treatment="preserve"> </fo:block>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

<xsl:template match="w:cr">
	<fo:block white-space-treatment="preserve"> </fo:block>
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
    <fo:basic-link color="blue" text-decoration="underline" >
	<xsl:variable name="relId"><xsl:value-of select="string(@r:id)"/></xsl:variable>
      
	<xsl:variable name="hTemp" 
		select="java:org.docx4j.convert.out.html.HtmlExporter.resolveHref(
		             $wmlPackage, $relId )" />
		                   
      <xsl:variable name="href">
          <xsl:value-of select="$hTemp"/>
        <xsl:choose>
          <xsl:when test="@w:anchor"><xsl:value-of select="@w:anchor"/></xsl:when>
          <xsl:when test="@w:bookmark"><xsl:value-of select="@w:bookmark"/></xsl:when>
          <xsl:when test="@w:arbLocation"><xsl:value-of select="@w:arbLocation"/></xsl:when>
        </xsl:choose>
      </xsl:variable>
      
        <xsl:choose>
          <xsl:when test="@w:bookmark">
            <xsl:attribute name="internal-destination"><xsl:value-of select="$href"/></xsl:attribute>
          </xsl:when>
		<!--  TODO: id for anchor, heading etc? Re headers, I think Word may just insert a bookmark -->
          <xsl:when test="@w:arbLocation">
            <xsl:attribute name="internal-destination"><xsl:value-of select="$href"/></xsl:attribute>
          </xsl:when>
          <xsl:when test="@w:anchor">
            <xsl:attribute name="internal-destination"><xsl:value-of select="$href"/></xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
          	<!--  TODO file? -->
	        <xsl:attribute name="external-destination">url(<xsl:value-of select="$href"/>)</xsl:attribute>          
          </xsl:otherwise>
        </xsl:choose>
      
 		<xsl:apply-templates />      
    </fo:basic-link>
  </xsl:template>

<!-- <w:bookmarkStart w:id="0" w:name="mybm"/>  -->
<xsl:template match="w:bookmarkStart" >
	<fo:inline id="{@w:name}"/>
</xsl:template>

<xsl:template match="w:bookmarkEnd" />


  <xsl:template match="w:footnoteReference">

	<xsl:variable name="fn"><xsl:value-of select="java:org.docx4j.convert.out.html.HtmlExporterNG2.getNextFootnoteNumber($modelStates)"/></xsl:variable>
	<xsl:variable name="id"><xsl:value-of select="string(@w:id)"/></xsl:variable>
	  
	<fo:footnote>
	
	    <fo:inline baseline-shift="super"
	               font-size="smaller"><xsl:value-of select="$fn"/></fo:inline>
	               
	    <fo:footnote-body>
	      <fo:list-block provisional-label-separation="0pt"
	                     provisional-distance-between-starts="18pt"
	                     space-after.optimum="6pt">
	        <fo:list-item>
	          <fo:list-item-label end-indent="label-end()">
	            <fo:block><xsl:value-of select="$fn"/></fo:block>
	          </fo:list-item-label>
	          <fo:list-item-body start-indent="body-start()">
	            <fo:block><xsl:apply-templates
					select="java:org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart.getFootnote($wmlPackage, $id)" /></fo:block>
	          </fo:list-item-body>
	        </fo:list-item>
	      </fo:list-block>
	    </fo:footnote-body>
	  </fo:footnote>
	    
  </xsl:template>
  
  <xsl:template match="w:footnote">
  	<xsl:apply-templates/>  
  </xsl:template>
  
  <!--  The number in the note itself -->
  <xsl:template match="w:footnoteRef" />
  


  <xsl:template match="w:endnoteReference ">  
    <xsl:variable name="fn"><xsl:value-of select="java:org.docx4j.convert.out.html.HtmlExporterNG2.getNextEndnoteNumber($modelStates)"/></xsl:variable>
    <fo:inline baseline-shift="super"
	               font-size="smaller"><xsl:value-of select="$fn"/></fo:inline>
  </xsl:template>

  <!--  The number in the note itself -->
  <xsl:template match="w:endnoteRef">
    <xsl:variable name="fn"><xsl:value-of select="count(../../../preceding-sibling::*)-1"/></xsl:variable>
	<fo:inline baseline-shift="super" font-size="smaller"><xsl:value-of select="$fn"/></fo:inline>
  </xsl:template>  

  <xsl:template match="w:endnotes">
  		<xsl:apply-templates/>  
  </xsl:template>
  
  <xsl:template match="w:endnote[@w:id='0']"/>
  
  <xsl:template match="w:endnote[@w:id!='0']">
  	<xsl:apply-templates/>  
  </xsl:template>
    
  <xsl:template match="w:continuationSeparator" />

  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  +++++++++++++++++++  no match     +++++++++++++++++++++++ -->
  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

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

</xsl:stylesheet>
