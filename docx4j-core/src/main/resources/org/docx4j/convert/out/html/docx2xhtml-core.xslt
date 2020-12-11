
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
    xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math"
    xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006"
    version="1.0"
        exclude-result-prefixes="java w a o v WX aml w10 pkg wp pic">	
        
        <!--  Note definition of xmlns:r is different 
              from the definition in an _rels file
              (where it is http://schemas.openxmlformats.org/package/2006/relationships)  -->

<!-- Uncomment for MathML (1 of 3); this file is part of Microsoft Office, and not provided in docx4j. Change it to output UTF-8  
    <xsl:include href="OMML2MML.xslt" />
 -->

<xsl:param name="conversionContext"/> <!-- select="'passed in'"-->	

   
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
		select="java:org.docx4j.convert.out.common.XsltCommonFunctions.logInfo($conversionContext, '/pkg:package')" />
	<xsl:variable name="dummy2"
		select="java:org.docx4j.convert.out.common.XsltCommonFunctions.moveNextSection($conversionContext)" />
		
		
	<html>

		<xsl:copy-of select="java:org.docx4j.convert.out.html.XsltHTMLFunctions.appendHeadElement($conversionContext)" />

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
				test="java:org.docx4j.convert.out.common.XsltCommonFunctions.hasDefaultHeader($conversionContext)">
				<div class="header">
					<xsl:variable name="setCurrentPartDefaultHeader"
						select="java:org.docx4j.convert.out.common.XsltCommonFunctions.setCurrentPartDefaultHeader($conversionContext)" />
					<xsl:apply-templates
						select="java:org.docx4j.convert.out.common.XsltCommonFunctions.getDefaultHeader($conversionContext)" />
					<xsl:variable name="backagain"
						select="java:org.docx4j.convert.out.common.XsltCommonFunctions.setCurrentPartMainDocument($conversionContext)" />
				</div>
			</xsl:if>

			<!--  Info -->
			<xsl:copy-of
				select="java:org.docx4j.convert.out.common.XsltCommonFunctions.message($conversionContext, 'TO HIDE THESE MESSAGES, TURN OFF debug level logging for org.docx4j.convert.out.common.writer.AbstractMessageWriter ' )" />

			<xsl:call-template name="pretty-print-block" />

			<div class="document">
				<xsl:apply-templates select="w:body|w:cfChunk" />
			</div>

			<xsl:call-template name="pretty-print-block" />

			<!--  Footnotes and endnotes -->
			<xsl:if
				test="java:org.docx4j.convert.out.common.XsltCommonFunctions.hasFootnotesPart($conversionContext)">
				<div class="footnotes">
					<xsl:apply-templates
						select="java:org.docx4j.convert.out.common.XsltCommonFunctions.getFootnotes($conversionContext)" />
				</div>
			</xsl:if>

			<xsl:call-template name="pretty-print-block" />

			<xsl:if
				test="java:org.docx4j.convert.out.common.XsltCommonFunctions.hasEndnotesPart($conversionContext)">
				<div class="endnotes">
					<xsl:apply-templates
						select="java:org.docx4j.convert.out.common.XsltCommonFunctions.getEndnotes($conversionContext)" />
				</div>
			</xsl:if>

			<xsl:call-template name="pretty-print-block" />

			<xsl:if
				test="java:org.docx4j.convert.out.common.XsltCommonFunctions.hasDefaultFooter($conversionContext)">
				
				<div class="footer">
					<xsl:variable name="setCurrentPartDefaultFooter"
						select="java:org.docx4j.convert.out.common.XsltCommonFunctions.setCurrentPartDefaultFooter($conversionContext)" />
					<xsl:apply-templates
						select="java:org.docx4j.convert.out.common.XsltCommonFunctions.getDefaultFooter($conversionContext)" />
					<xsl:variable name="backagain"
						select="java:org.docx4j.convert.out.common.XsltCommonFunctions.setCurrentPartMainDocument($conversionContext)" />
				</div>
			</xsl:if>
			
			<xsl:call-template name="pretty-print-block" />

			<xsl:value-of select="$userBodyTail" disable-output-escaping="yes" />
			
			<xsl:call-template name="pretty-print-block" />
            
<!-- Uncomment for MathML (2 of 3)
            <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.4/MathJax.js?config=MML_HTMLorMML-full"><xsl:comment/></script>
            -->
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


		<xsl:variable name="childResults" >
			<xsl:choose>
				<xsl:when test="ancestor::w:tbl and count(child::node())=0">
					<!-- A row that has no content will be displayed by browsers
					     (Firefox at least) with a microscopic row height.
					     
					     Rather than put dummy content here - an nbsp or something -
					     i've set a height in the global td style. This could be
					     improved, by only setting it on tr's which need it.  
					
						span>STUFF</span-->
				</xsl:when>
				<xsl:when test="contains( string(../../w:sdtPr/w:tag/@w:val), 'HTML_ELEMENT' )">
					<!--  use HTML OL|UL and LI; -->								
					<!--  don't number in this case, since we'll let li insert those -->
					<xsl:apply-templates />		
				</xsl:when>
				<xsl:when test="count(child::node())=1 and count(w:pPr)=1">
					<!--  Do count an 'empty' paragraph (one with a w:pPr node only) -->
					<xsl:value-of select="
						java:org.docx4j.convert.out.html.XsltHTMLFunctions.getNumberXmlNode( 
					  					$conversionContext, $pPrNode, $pStyleVal, $numId, $levelId)"/>
					<!--  Don't apply templates, since there is nothing to do. -->
				</xsl:when>				
				<xsl:otherwise>
					<xsl:value-of select="
						java:org.docx4j.convert.out.html.XsltHTMLFunctions.getNumberXmlNode( 
					  					$conversionContext, $pPrNode, $pStyleVal, $numId, $levelId)" />		
					<xsl:apply-templates/>				
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
        <!--  avoid javax.xml.transform.TransformerException: Duplicate variable declaration -->
		<xsl:variable name="pPrNode2" select="w:pPr" />  
		
		<xsl:choose>
			<xsl:when test="contains( string(../../w:sdtPr/w:tag/@w:val), 'HTML_ELEMENT' )">
				<xsl:copy-of select="java:org.docx4j.convert.out.html.XsltHTMLFunctions.createListItemBlockForPPr( 
			 							$conversionContext, $pPrNode2, $pStyleVal, $childResults)" />
			</xsl:when>
			<!--  Usual case -->
			<xsl:otherwise>
				<xsl:copy-of select="java:org.docx4j.convert.out.html.XsltHTMLFunctions.createBlockForPPr( 
			 							$conversionContext, $pPrNode2, $pStyleVal, $childResults)" />
			</xsl:otherwise>
		</xsl:choose>
			
		
  </xsl:template>

<!--  Bullets, non-breaking spaces, quotes etc:-

      As a general principle, don't use character entities, since these
      don't play nicely with extension functions.
      
      Instead, *use UTF-8 at the Java end*. 
      
      This seems to work nicely.
      
      Note, that with HTML output method, \u2022 bullet is converted to &bull; 
        and \u00A0 is converted to &nbsp; (I'm not sure how it knows to do
        that .. see http://s-n-ushakov.blogspot.com.au/2011/09/xslt-entities-java-xalan.html
        and http://stackoverflow.com/questions/31870/using-a-html-entity-in-xslt-e-g-nbsp
        but nowhere do I tell it what &bull; is!)
        
      With XML output, the characters appear as their proper UTF 8 output.
      
      So either output method is fine.  (Though IE needs to use correct encoding ..
      it seems to ignore encoding in XML declaration!)
      
      For completeness, note that I couldn't get character entities processing
      properly through the extension functions.
      
	  Findings:
	  1.  can return as text using value-of, provided you disable output escape.
	  2.  to round trip certain entities as strings (don't disable output escaping!) 
	  3.  ** can't create an entity as text, and return as DF, use in copy-of
	      (so use of createTextNode in XmlUtils.treeCopy may cause problems .. 
	       it converts a single & to &amp; and there seems to be no way around that,
	       short of changing it to <amp> there ...  
	  4.  (can round trip an entity as a node) 
       
      But as noted above, there is *no reason* ever to use an entity in our code 
      .. just use the UTF 8 character, inserted at Java end as \u...
      (not that XSLT end as &#...)
      
      The workaround described at http://stackoverflow.com/questions/10842856/text-node-content-escaped-by-xalan-extension
      doesn't work in some cases, because in Java (XmlUtils.treecopy) createTextNode
      seems to automatically convert a single '&' to &amp; 	  
 -->


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
					<xsl:variable name="childResults">
						<xsl:apply-templates/>
					</xsl:variable>
					
					<xsl:variable name="pStyleVal" select="string( ../w:pPr/w:pStyle/@w:val )" />  			
					
					<xsl:variable name="rPrNode" select="w:rPr" />  	
			
				  	<xsl:copy-of select="java:org.docx4j.convert.out.html.XsltHTMLFunctions.createBlockForRPr( 
				  		$conversionContext, $pStyleVal, $rPrNode, $childResults)" />
				  		
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

<!-- This is an extension point.
     You can register your own SdtTagHandlers, which
     tailor the HTML output based on the content of
     w:sdtPr/w:tag.
     See the SdtWriter class for details. -->
	<xsl:template match="w:sdt">

		<xsl:variable name="childResults">
			<xsl:apply-templates select="w:sdtContent/*" />
		</xsl:variable>

		<xsl:variable name="currentNode" select="./w:sdtPr" />

		<xsl:copy-of
			select="java:org.docx4j.convert.out.html.XsltHTMLFunctions.toSdtNode(
  			$conversionContext, $currentNode, 
  			$childResults)"  />

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
  			$conversionContext, 
  			$wpinline)" />
  		</xsl:when>
  		<xsl:otherwise>
  		
			<xsl:copy-of 
				select="java:org.docx4j.convert.out.common.XsltCommonFunctions.notImplemented($conversionContext,., ' without pic:pic ' )" />  	  		
  		</xsl:otherwise>  	
  	</xsl:choose>
    
  </xsl:template>
  
    <!--  E1.0 images  -->
	<xsl:template match="w:pict">
	
		<xsl:choose>
			<xsl:when test="./v:shape/v:imagedata">
	
			  	<xsl:variable name="wpict" select="."/>
			  	
			  	<xsl:copy-of select="java:org.docx4j.model.images.WordXmlPictureE10.createHtmlImgE10( 
			  			$conversionContext, 
			  			$wpict)" />
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

		<xsl:variable name="currentNode" select="." />  			

		<xsl:variable name="childResults">
			<xsl:apply-templates /> <!-- select="*[not(name()='w:tblPr' or name()='w:tblGrid')]" /-->
		</xsl:variable>

<!--
		<xsl:comment>debug start</xsl:comment>
			<xsl:copy-of select="$childResults"/>
		<xsl:comment>debug end</xsl:comment>
  -->
  
		<!--  Create the HTML table in Java --> 
	  	<xsl:copy-of select="java:org.docx4j.convert.out.common.XsltCommonFunctions.toNode($conversionContext, $currentNode, $childResults)"/>
	  			  		
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
	
		<xsl:variable name="childResults">
			<xsl:apply-templates /> 
		</xsl:variable>
	
		<xsl:variable name="currentNode" select="." />  			
	
	     <xsl:copy-of select="java:org.docx4j.convert.out.common.XsltCommonFunctions.toNode($conversionContext, $currentNode, 
				$childResults)" />
	  		  			
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

	<xsl:variable name="currentNode" select="." />  			

     <xsl:copy-of select="java:org.docx4j.convert.out.common.XsltCommonFunctions.toNode($conversionContext, $currentNode, 
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
	<xsl:variable name="childResults">
		<xsl:apply-templates /> 
	</xsl:variable>

	<xsl:variable name="currentNode" select="." />  			

     <xsl:copy-of select="java:org.docx4j.convert.out.common.XsltCommonFunctions.toNode($conversionContext,$currentNode, 
			$childResults)" />
  </xsl:template>
   
  <xsl:template match="w:bookmarkStart">
	<xsl:variable name="childResults">
		<xsl:apply-templates /> 
	</xsl:variable>

	<xsl:variable name="currentNode" select="." />  			

     <xsl:copy-of select="java:org.docx4j.convert.out.common.XsltCommonFunctions.toNode($conversionContext, $currentNode, 
			$childResults)" />
  </xsl:template>
  
<xsl:template match="w:bookmarkStart[@w:name='_GoBack']" />  
   
<xsl:template match="w:bookmarkEnd" />

  <xsl:template match="w:ins|w:moveTo">
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
  
  <xsl:template match="w:moveFrom">
  	<span class="del">
  		<xsl:apply-templates select=".//w:r"/>
  	</span>
  </xsl:template>  	
  
  <xsl:template match="w:moveFromRangeStart|w:moveFromRangeEnd|w:moveToRangeStart|w:moveToRangeEnd" />
  
  <xsl:template match="w:footnoteReference">  
    <xsl:variable name="fn"><xsl:value-of select="java:org.docx4j.convert.out.common.XsltCommonFunctions.getNextFootnoteNumber($conversionContext)"/></xsl:variable>
  	<span style="vertical-align: top; font-size: xx-small">
  		<!--  Bidirectional --><a name="fs{$fn}"><a href="#fn{$fn}"><xsl:value-of select="$fn"/></a></a></span>  
  </xsl:template>
  <xsl:template match="w:endnoteReference ">  
    <xsl:variable name="fn"><xsl:value-of select="java:org.docx4j.convert.out.common.XsltCommonFunctions.getNextEndnoteNumber($conversionContext)"/></xsl:variable>
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

<!-- Uncomment for MathML (3 of 3) 
  <xsl:template match="m:oMathPara" >
    <math xmlns='http://www.w3.org/1998/Math/MathML'>
            <xsl:apply-templates/>
    </math>    
  </xsl:template>
-->
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

  <xsl:template match="*[ancestor::w:body]" priority="-1"> <!--  ignore eg page number field in footer -->
		<xsl:copy-of 
			select="java:org.docx4j.convert.out.common.XsltCommonFunctions.notImplemented($conversionContext,., '' )" />  	      		 
  </xsl:template>
   
</xsl:stylesheet>
