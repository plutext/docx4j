
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
    xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006"		
    version="1.0"
        exclude-result-prefixes="java w a o v WX aml w10 pkg wp pic r">	

  <!-- 
    Copyright 200?-2012, Plutext Pty Ltd.
    
    This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

  -->
        

<!-- =======================================

	 This is an XSLT to convert WordML2FO.
	 
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
     
     TODO:  cross references.
     
     -->
        
        <!--  Note definition of xmlns:r is different 
              from the definition in an _rels file
              (where it is http://schemas.openxmlformats.org/package/2006/relationships)  -->


<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="no" />
<!--  indent="no" gives a better result for things like subscripts, because it stops
      the user-agent from replacing a carriage return in the XSL FO with a space in the output. -->


<xsl:param name="conversionContext"/> <!-- select="'passed in'"-->	
   
<!-- Used in extension function for mapping fonts --> 		
<xsl:param name="substituterInstance"/> <!-- select="'passed in'"-->	
<xsl:param name="fontFamilyStack"/> <!-- select="'passed in'"-->
	
<xsl:param name="docxWikiMenu"/>		
<xsl:param name="docID"/>



<xsl:template name="pretty-print-block">
  <xsl:text>
  
  </xsl:text>
</xsl:template>

<!--  Not used, if we just pass in document.xml
<xsl:template match="/pkg:package">

	<xsl:apply-templates select="pkg:part/pkg:xmlData/w:document"/>

  </xsl:template>

  <xsl:template match="w:document">
  
  -->
  
  
  <xsl:template match="sections">
  
		<xsl:variable name="logging" 
			select="java:org.docx4j.convert.out.common.XsltCommonFunctions.logInfo($conversionContext, '/pkg:package')" />

		<fo:root>

		  	<xsl:copy-of select="java:org.docx4j.convert.out.fo.XsltFOFunctions.getLayoutMasterSetFragment( 
		  		$conversionContext)" />
		  	<!--  creates something like
		  	
					  <layout-master-set xmlns="http://www.w3.org/1999/XSL/Format">
					    <simple-page-master master-name="s1-firstpage" margin-bottom="10mm" margin-left="10mm" margin-right="10mm" margin-top="10mm" page-height="297mm" page-width="210mm">
					    </simple-page-master>
					    :
					    <page-sequence-master master-name="s1">
					      <repeatable-page-master-alternatives>
					        <conditional-page-master-reference master-reference="s1-firstpage" page-position="first"/>
					        <conditional-page-master-reference master-reference="s1-evenpage" odd-or-even="even"/>
					        <conditional-page-master-reference master-reference="s1-oddpage" odd-or-even="odd"/>
					      </repeatable-page-master-alternatives>
					    </page-sequence-master>
					    <page-sequence-master master-name="s2">
					      <repeatable-page-master-alternatives>
					        <conditional-page-master-reference master-reference="s2-simple"/>
					      </repeatable-page-master-alternatives>
					    </page-sequence-master>
					  </layout-master-set>
		  	
		  	 -->	
	  		
	  		<xsl:apply-templates select="section"/>

		</fo:root>
  </xsl:template>


	<xsl:template match="section">
			<xsl:variable name="dummy"
				select="java:org.docx4j.convert.out.common.XsltCommonFunctions.moveNextSection($conversionContext)" />

			<xsl:variable name="pageNumberFormat" 
				select="java:org.docx4j.convert.out.fo.XsltFOFunctions.getPageNumberFormat($conversionContext)" />
	
			<!-- start page-sequence
				here comes the text (contained in flow objects)
				the page-sequence can contain different fo:flows
				the attribute value of master-name refers to the page layout
				which is to be used to layout the text contained in this
				page-sequence-->
				
			<xsl:variable name="pageCountVal" 
				select="java:org.docx4j.convert.out.fo.XsltFOFunctions.getForcePageCount($conversionContext)"/>
				
				
			<fo:page-sequence master-reference="{@name}" format="{$pageNumberFormat}" id="section_{@name}" force-page-count="{$pageCountVal}">
				<xsl:if test="java:org.docx4j.convert.out.fo.XsltFOFunctions.hasPgNumTypeStart($conversionContext)">
					<xsl:attribute name="initial-page-number"><xsl:value-of 
						select="java:org.docx4j.convert.out.fo.XsltFOFunctions.getPageNumberInitial($conversionContext)"/></xsl:attribute>
				</xsl:if>

				<!--  First Page Header -->
				<xsl:if
					test="java:org.docx4j.convert.out.common.XsltCommonFunctions.hasFirstHeader($conversionContext)">
					
					<xsl:variable name="partname" 
						select="java:org.docx4j.convert.out.common.XsltCommonFunctions.inFirstHeader($conversionContext)" />
					
					<fo:static-content
						flow-name="xsl-region-before-firstpage">

						<xsl:apply-templates
							select="java:org.docx4j.convert.out.common.XsltCommonFunctions.getFirstHeader($conversionContext)" />

					</fo:static-content>
				</xsl:if>
				
				<!--  First Page Footer -->
				<xsl:if
					test="java:org.docx4j.convert.out.common.XsltCommonFunctions.hasFirstFooter($conversionContext)">

					<xsl:variable name="partname" 
						select="java:org.docx4j.convert.out.common.XsltCommonFunctions.inFirstFooter($conversionContext)" />

					<fo:static-content
						flow-name="xsl-region-after-firstpage">
						<xsl:apply-templates
							select="java:org.docx4j.convert.out.common.XsltCommonFunctions.getFirstFooter($conversionContext)" />
					</fo:static-content>
				</xsl:if>

				<!--  Default/Odd Page Header -->
				<xsl:if
					test="java:org.docx4j.convert.out.common.XsltCommonFunctions.hasDefaultHeader($conversionContext)">
					
					<xsl:variable name="partname" 
						select="java:org.docx4j.convert.out.common.XsltCommonFunctions.inDefaultHeader($conversionContext)" />
					
					<fo:static-content
						flow-name="xsl-region-before-default">
						<xsl:apply-templates
							select="java:org.docx4j.convert.out.common.XsltCommonFunctions.getDefaultHeader($conversionContext)" />
					</fo:static-content>
				</xsl:if>

				<!--  Default/Odd Page Footer -->
				<xsl:if
					test="java:org.docx4j.convert.out.common.XsltCommonFunctions.hasDefaultFooter($conversionContext)">
					
					<xsl:variable name="partname" 
						select="java:org.docx4j.convert.out.common.XsltCommonFunctions.inDefaultFooter($conversionContext)" />
					
					<fo:static-content
						flow-name="xsl-region-after-default">
						<xsl:apply-templates
							select="java:org.docx4j.convert.out.common.XsltCommonFunctions.getDefaultFooter($conversionContext)" />
					</fo:static-content>
				</xsl:if>
							
				<!--  Footnotes Part -->
				<xsl:if
					test="java:org.docx4j.convert.out.common.XsltCommonFunctions.hasFootnotesPart($conversionContext)">
					<fo:static-content flow-name="xsl-footnote-separator">
					    <fo:block>
					      <fo:leader leader-pattern="rule"
					                 leader-length="100%"
					                 rule-style="solid"
					                 rule-thickness="0.5pt"/>
					    </fo:block>
					  </fo:static-content>				
				</xsl:if>
				
				<!--  Even Page Header -->
				<xsl:if
					test="java:org.docx4j.convert.out.common.XsltCommonFunctions.hasEvenHeader($conversionContext)">
					
					<xsl:variable name="partname" 
						select="java:org.docx4j.convert.out.common.XsltCommonFunctions.inEvenHeader($conversionContext)" />
					
					<fo:static-content
						flow-name="xsl-region-before-evenpage">
						<xsl:apply-templates
							select="java:org.docx4j.convert.out.common.XsltCommonFunctions.getEvenHeader($conversionContext)" />
					</fo:static-content>
				</xsl:if>
				
				<!--  Even Page Footer -->
				<xsl:if
					test="java:org.docx4j.convert.out.common.XsltCommonFunctions.hasEvenFooter($conversionContext)">
					
					<xsl:variable name="partname" 
						select="java:org.docx4j.convert.out.common.XsltCommonFunctions.inEvenFooter($conversionContext)" />
					
					<fo:static-content
						flow-name="xsl-region-after-evenpage">
						<xsl:apply-templates
							select="java:org.docx4j.convert.out.common.XsltCommonFunctions.getEvenFooter($conversionContext)" />
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

					<xsl:variable name="partname" 
						select="java:org.docx4j.convert.out.common.XsltCommonFunctions.setCurrentPartMainDocument($conversionContext)" />

					<!--  Info 
					<xsl:copy-of 
						select="java:org.docx4j.convert.out.common.XsltCommonFunctions.message($conversionContext, 'TO HIDE THESE MESSAGES, TURN OFF log4j debug level logging for org.docx4j.convert.out.Converter ' )" />  	  		
 -->
					<!--<xsl:apply-templates select="w:body/*" />-->
					<xsl:apply-templates select="*" />

				  	<xsl:call-template name="pretty-print-block"/>

					<xsl:if
						test="java:org.docx4j.convert.out.common.XsltCommonFunctions.hasEndnotesPart($conversionContext)">
						
				        <fo:block space-before="44pt" font-weight="bold" font-size="14pt">
				          <xsl:text>Endnotes</xsl:text>
				        </fo:block>
						
						<xsl:apply-templates
								select="java:org.docx4j.convert.out.common.XsltCommonFunctions.getEndnotes($conversionContext)" />
					</xsl:if>

				  	<xsl:call-template name="pretty-print-block"/>

				</fo:flow><!-- closes the flow element-->
			</fo:page-sequence><!-- closes the page-sequence -->

			<!-- end: defines page layout -->
	
	
	</xsl:template>

  <!--  the extension functions fetch these
        for processing -->
  <xsl:template match="w:hdr|w:ftr">
  	<xsl:apply-templates/>
  </xsl:template>

  <!-- each paragraph is encapsulated in a block element
       the attributes of the block define
       font-family and size, line-height etc. -->
  <xsl:template match="w:p">
  
<!--     	<xsl:call-template name="pretty-print-block"/>   -->
  
 			<!--  Invoke an extension function, so we can use
 			      docx4j to populate the fo:block -->
 		
		<xsl:variable name="childResults">
			<xsl:apply-templates/>
		</xsl:variable>
		
		<xsl:variable name="pPrNode" select="w:pPr" />  	
		<xsl:variable name="pStyleVal" select="string( w:pPr/w:pStyle/@w:val )" />  
		
<!-- Uncomment to provide hints as to where in the docx processing is up to.. 		
		<xsl:variable name="logging" 
			select="java:org.docx4j.convert.out.common.XsltCommonFunctions.logInfo($conversionContext, string($childResults))" />
	 -->		

	  	<xsl:copy-of select="java:org.docx4j.convert.out.fo.XsltFOFunctions.createBlockForPPr( 
  			$conversionContext, $pPrNode, $pStyleVal, $childResults)" />

		
  </xsl:template>

  <xsl:template match="w:pPr | w:rPr" /> <!--  handle via extension function -->

  <xsl:template match="w:r">
  
  	<xsl:choose>
  		<xsl:when test="java:org.docx4j.convert.out.common.XsltCommonFunctions.isInComplexFieldDefinition($conversionContext)" >
  			<!-- in a field, so ignore, unless this run contains fldChar -->
		  	<xsl:if test="w:fldChar"><xsl:apply-templates/></xsl:if>
  			
  		</xsl:when>
  		<xsl:otherwise>
  		
		  	<xsl:choose>
  				<xsl:when test="w:rPr">
		  			<!--  Invoke an extension function, so we can use
  					      docx4j to populate the fo:block -->
  		
					<xsl:variable name="childResults">
						<xsl:apply-templates/>
					</xsl:variable>
			
					<xsl:variable name="pPrNode" select="../w:pPr" />   	
					<xsl:variable name="rPrNode" select="w:rPr" />  	
	
				  	<xsl:copy-of select="java:org.docx4j.convert.out.fo.XsltFOFunctions.createBlockForRPr( 
				  		$conversionContext, $pPrNode, $rPrNode, $childResults)" />
	  		
			  	</xsl:when>
	  			<xsl:otherwise>
		        	<xsl:apply-templates/>
	  			</xsl:otherwise>
			  </xsl:choose>					
  		
  		</xsl:otherwise>
  		
  	</xsl:choose>
    	
		
  </xsl:template>

	<xsl:template match="w:t[parent::w:r]">
	
		<xsl:variable name="pPrNode" select="../../w:pPr" />  	
		<xsl:variable name="rPrNode" select="../w:rPr" />  	
		<xsl:variable name="textNode" select="." />  	
	
		<xsl:copy-of select="java:org.docx4j.convert.out.common.XsltCommonFunctions.fontSelector( 
		  		$conversionContext, $pPrNode, $rPrNode, $textNode)" />
				
	</xsl:template>

  <xsl:template match="w:t[not(parent::w:r)]">  	
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
  	<xsl:choose>
  		<xsl:when test="contains(./w:sdtPr/w:tag/@w:val, 'XSLT_')">
  			<!-- An SDT we've inserted to handle adjacent borders/shading nodes -->
  			<xsl:value-of select="java:org.docx4j.convert.out.common.XsltCommonFunctions.logWarn($conversionContext, 'XSLT_')" />

			<xsl:variable name="childResults">
	  			<xsl:apply-templates select="w:sdtContent/*"/>
			</xsl:variable>
		
		  	<xsl:choose>
  				<xsl:when test="./w:sdtContent/w:p[1]/w:pPr">
  				
					<xsl:variable name="pPrNode" select="./w:sdtContent/w:p[1]/w:pPr" />
				  	
					<xsl:variable name="pStyleVal" select="string( w:pPr/w:pStyle/@w:val )" />  	

			  		<xsl:copy-of select="java:org.docx4j.convert.out.fo.XsltFOFunctions.createBlockForSdt( 
	  					$conversionContext, $pPrNode, $pStyleVal, $childResults, string(./w:sdtPr/w:tag/@w:val))" />
  				
  				</xsl:when>
  				<xsl:when test="./w:sdtContent/w:sdt/w:sdtContent/w:p[1]/w:pPr">
  				
					<xsl:variable name="pPrNode" select="./w:sdtContent/w:sdt/w:sdtContent/w:p[1]/w:pPr" />
				  	
					<xsl:variable name="pStyleVal" select="string( w:pPr/w:pStyle/@w:val )" />  	

			  		<xsl:copy-of select="java:org.docx4j.convert.out.fo.XsltFOFunctions.createBlockForSdt( 
	  					$conversionContext, $pPrNode, $pStyleVal, $childResults, string(./w:sdtPr/w:tag/@w:val))" />
  				
  				</xsl:when>
  				<xsl:when test="./w:sdtPr/w:rPr">
  				
					<xsl:variable name="rPrNode" select="./w:sdtPr/w:rPr" />

			  		<xsl:copy-of select="java:org.docx4j.convert.out.fo.XsltFOFunctions.createInlineForSdt( 
	  					$conversionContext, $rPrNode, $childResults, string(./w:sdtPr/w:tag/@w:val))" />
  				
  				</xsl:when>  				
		  		<xsl:otherwise>
		  			<!-- Should not happen. -->
  					<xsl:apply-templates select="w:sdtContent/*"/>
  				</xsl:otherwise>  				  				
  			</xsl:choose>

  			<xsl:value-of select="java:org.docx4j.convert.out.common.XsltCommonFunctions.logWarn($conversionContext, '.. XSLT_ done')" />

  		
  		</xsl:when>
  		<xsl:otherwise>
  			<xsl:apply-templates select="w:sdtContent/*"/>
  		</xsl:otherwise>
  	</xsl:choose>
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
  	
  	<xsl:choose>
  		<!--  sanity check -->
  		<xsl:when test="./a:graphic/a:graphicData/pic:pic">
  		
		   	<xsl:copy-of select="java:org.docx4j.model.images.WordXmlPictureE20.createXslFoImgE20( 
		   			$conversionContext,
		  			$wpinline)" />  		
  		</xsl:when>
  		<xsl:otherwise>
  		
			<xsl:copy-of 
				select="java:org.docx4j.convert.out.common.XsltCommonFunctions.notImplemented($conversionContext,., ' without pic:pic ' )" />  	  		
  		</xsl:otherwise>  	
  	</xsl:choose>
  	
    
  </xsl:template>
  

<xsl:template match="w:pict">

	<xsl:choose>
		<xsl:when test="./v:shape/v:imagedata">

	  		<xsl:variable name="wpict" select="."/>
		  	
		  	<xsl:copy-of select="java:org.docx4j.model.images.WordXmlPictureE10.createXslFoImgE10( 
		  	$conversionContext,
  			$wpict)" />
		</xsl:when>
		<xsl:when test="./v:shape/v:textbox">
			<!--  from 3.0.1 -->
			<xsl:variable name="childResults">
				<xsl:apply-templates  select="./v:shape/v:textbox/w:txbxContent/*" /> 
			</xsl:variable>
	
			<xsl:variable name="currentNode" select="." />  			
	
			<!--  Create the XSL FO table in Java -->
		  	<xsl:copy-of select="java:org.docx4j.convert.out.common.XsltCommonFunctions.toNode(
		  		$conversionContext,$currentNode, $childResults)"/>	  		
			
		</xsl:when>
		<xsl:when test="./v:rect/v:textbox">
			<!--  from 3.0.1 -->
			<xsl:variable name="childResults">
				<xsl:apply-templates  select="./v:rect/v:textbox/w:txbxContent/*" /> 
			</xsl:variable>
	
			<xsl:variable name="currentNode" select="." />  			
	
			<!--  Create the XSL FO table in Java -->
		  	<xsl:copy-of select="java:org.docx4j.convert.out.common.XsltCommonFunctions.toNode(
		  		$conversionContext,$currentNode, $childResults)"/>	  		
			
		</xsl:when>
		<xsl:otherwise>
			<xsl:comment>TODO: handle w:pict containing other than ./v:shape/v:imagedata</xsl:comment>
			<xsl:copy-of 
				select="java:org.docx4j.convert.out.common.XsltCommonFunctions.notImplemented($conversionContext,., ' without v:imagedata ' )" />  	  		
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

		<xsl:variable name="currentNode" select="." />  			

		<!--  Create the XSL FO table in Java -->
	  	<xsl:copy-of select="java:org.docx4j.convert.out.common.XsltCommonFunctions.toNode($conversionContext,$currentNode, $childResults)"/>	  		
	  			  		
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
		<!-- There is no glyph for NON-BREAKING HYPHEN &#x2011; in many fonts, so 
		     use an ordinary hyphen with a zero-width no-break space character &#xFEFF; 
		     as suggested at http://www.sagehill.net/docbookxsl/PrintCustomEx.html#Hyphenation  -->
		<xsl:text disable-output-escaping="yes">-&#xFEFF;</xsl:text>
	</xsl:template>

	<xsl:template match="w:br">
	
		<!--  Is there a w:br immediately before this one?
		
		      If this is the first child of this w:r, and the w:r is preceded by another w:r, look at its last child
		
			  If this is not the first child of this w:r, look at the preceding sibling
		 -->
		<xsl:variable name="predecessor"> 
			<xsl:choose>
				<xsl:when test="count(preceding-sibling::w:rPr)=1 and count(preceding-sibling::*)>1">
					<!--  take rPr into account -->
					<xsl:value-of select="local-name(preceding-sibling::*[1])"/>
				</xsl:when>
				<xsl:when test="count(preceding-sibling::w:rPr)=0 and count(preceding-sibling::*)>0">
					<xsl:value-of select="local-name(preceding-sibling::*[1])"/>
				</xsl:when>
				<xsl:when test="count(../preceding-sibling::*)>0">
					<xsl:value-of select="local-name(../preceding-sibling::*[1]/*[last()])"/>
				</xsl:when>
				<xsl:otherwise/>
			</xsl:choose>
		</xsl:variable>
		
<!-- 	<xsl:variable name="logging" 
			select="java:org.docx4j.convert.out.common.XsltCommonFunctions.logInfo($conversionContext, $predecessor)" />
			 -->			
		
		<xsl:choose>
			<xsl:when test="$predecessor='br' and not(@w:type='page')">
				<!-- special case; see discussion in BrWriter -->
				<fo:block white-space-treatment="preserve" linefeed-treatment="preserve"><xsl:text> <!-- one carriage return -->				
</xsl:text></fo:block>
			</xsl:when>
			<xsl:otherwise>
				<!--  usual case -->
				<xsl:variable name="childResults">
					<xsl:apply-templates /> 
				</xsl:variable>
			
				<xsl:variable name="currentNode" select="." />  			
			
			     <xsl:copy-of select="java:org.docx4j.convert.out.common.XsltCommonFunctions.toNode($conversionContext,$currentNode, 
						$childResults)" />
			
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

	<xsl:variable name="currentNode" select="." />  			

     <xsl:copy-of select="java:org.docx4j.convert.out.common.XsltCommonFunctions.toNode($conversionContext,$currentNode, 
			$childResults)" />
  		  			
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
	<w:r><w:t xml:space="preserve">Will tab.. </w:t></w:r>
	<w:r>
		<w:tab/>
		<w:t>3 inches</w:t>
	</w:r>
</w:p>

 -->
<xsl:template match="w:tab"> 

	<xsl:variable name="p" select="ancestor::*[self::w:p][1]" />
	
	<xsl:choose>
		<xsl:when test="count($p/w:pPr/w:tabs/w:tab[1][@w:leader='dot' and @w:val='right'])=1">
						
		  <fo:leader leader-length.minimum="12pt" leader-length.optimum="40pt"
		    leader-length.maximum="100%" leader-pattern="dots">
		  </fo:leader>
						
		</xsl:when>		
		<xsl:otherwise>
		
			<!--  Use this simple-minded approach from MS stylesheet,
			      until our document model can do better.   -->
		    <xsl:call-template name="OutputTlcChar">
		      <xsl:with-param name="tlc">
		        <xsl:text disable-output-escaping="yes">&#160;</xsl:text>
		      </xsl:with-param>
		      <xsl:with-param name="count" select="3"/>
		    </xsl:call-template>
		
		</xsl:otherwise>
	
	</xsl:choose>


</xsl:template>

<!-- 
      <w:ptab w:relativeTo="margin" w:alignment="right" w:leader="none"/>
    </w:r>
     -->
<xsl:template match="w:ptab[@w:alignment='right']"> 

	  <fo:leader leader-length.minimum="12pt" leader-length.maximum="100%" leader-length.optimum="100%" 
	  leader-pattern="space"  leader-alignment="reference-area" />

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
	<xsl:variable name="childResults">
		<xsl:apply-templates /> 
	</xsl:variable>

	<xsl:variable name="currentNode" select="." />  			

     <xsl:copy-of select="java:org.docx4j.convert.out.common.XsltCommonFunctions.toNode($conversionContext,$currentNode, 
			$childResults)" />
  </xsl:template>

<!-- <w:bookmarkStart w:id="0" w:name="mybm"/>  -->
<xsl:template match="w:bookmarkStart" >
	<xsl:variable name="childResults">
		<xsl:apply-templates /> 
	</xsl:variable>

	<xsl:variable name="currentNode" select="." />  			

     <xsl:copy-of select="java:org.docx4j.convert.out.common.XsltCommonFunctions.toNode($conversionContext,$currentNode, 
			$childResults)" />
</xsl:template>

<xsl:template match="w:bookmarkStart[@w:name='_GoBack']" />

<xsl:template match="w:bookmarkEnd" />


  <xsl:template match="w:footnoteReference">

	<xsl:variable name="fn"><xsl:value-of select="java:org.docx4j.convert.out.common.XsltCommonFunctions.getNextFootnoteNumber($conversionContext)"/></xsl:variable>
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
					select="java:org.docx4j.convert.out.common.XsltCommonFunctions.getFootnote($conversionContext, $id)" /></fo:block>
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
    <xsl:variable name="fn"><xsl:value-of select="java:org.docx4j.convert.out.common.XsltCommonFunctions.getNextEndnoteNumber($conversionContext)"/></xsl:variable>
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

  <!--  Simple fields: several different types are supported, including PAGE, DATE, TIME, PRINTDATE, DOCPROPERTY -->
  <xsl:template match="w:fldSimple" >

		<xsl:variable name="childResults">
			<xsl:apply-templates/>
		</xsl:variable>

	  	<xsl:copy-of select="java:org.docx4j.convert.out.common.XsltCommonFunctions.toNode(
	  			$conversionContext,., $childResults)"/>	  		
  </xsl:template>

  <!--  Complex fields: update complex field definition level -->
  <xsl:template match="w:fldChar" >
		<xsl:copy-of 
			select="java:org.docx4j.convert.out.common.XsltCommonFunctions.updateComplexFieldDefinition($conversionContext, .)" />  	
  </xsl:template>
  
   <xsl:template match="w:customXml">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="w:customXmlPr" />  

  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  +++++++++++++++++++  alternate content     ++++++++++++++ -->
  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

	<xsl:template match="mc:AlternateContent" >
		<xsl:variable name="info" 
			select="concat('mc:AlternateContent selecting Fallback ie ignoring mc:Choice Requires ', 
					mc:Choice/@Requires, 
					' containing ', 
					local-name(mc:Choice/*[1]))"/>
		<xsl:variable name="dummy"
			select="java:org.docx4j.convert.out.common.XsltCommonFunctions.logInfo($conversionContext, 
			$info)" />
		<xsl:apply-templates select="mc:Fallback/*" />
	</xsl:template>

  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!--  +++++++++++++++++++  no match     +++++++++++++++++++++++ -->
  <!--  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

  <xsl:template match="*">
		<xsl:copy-of 
			select="java:org.docx4j.convert.out.common.XsltCommonFunctions.notImplemented($conversionContext,., '' )" />  	
  </xsl:template>

</xsl:stylesheet>
