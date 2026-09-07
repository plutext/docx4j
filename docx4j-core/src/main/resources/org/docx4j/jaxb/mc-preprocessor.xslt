
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"

	xmlns:java="http://xml.apache.org/xalan/java"
	xmlns:xalan="http://xml.apache.org/xalan"
	
	xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006"
	
	xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
	xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml"
	
 	xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
 		
	xmlns:wordml201011="http://schemas.microsoft.com/office/word/2010/11/wordml"
	xmlns:w15="http://schemas.microsoft.com/office/word/2012/wordml"

	xmlns:purlw="http://purl.oclc.org/ooxml/wordprocessingml/main"
 	xmlns:purla="http://purl.oclc.org/ooxml/drawingml/main"
 
	xmlns:purlcp="http://purl.oclc.org/ooxml/officeDocument/customProperties"
	xmlns:purlep="http://purl.oclc.org/ooxml/officeDocument/extendedProperties"
		
	version="1.0" exclude-result-prefixes="java purlw purlep purla purlcp xalan">	
        
<!--  This preprocessor does 3 things:

		1. for mc:AlternateContent, it selects the mc:Fallback content
		
		2. it corrects common validity issues with docx from various sources (eg Google Docs)
		
		3. it can import Strict docx files.  But see https://github.com/plutext/docx4j/issues/655
      
      See MainDocumentPart's unmarshall method 
      for an example of how it is invoked.
      
      You can override this with your own version.  Point at yours via
      docx4j property  docx4j.jaxb.JaxbValidationEventHandler
      
      NB, we do retain mc:AlternateContent in
      some places.  For example, 3.3.8 will retain
      it in w:r.
      
      -->


<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="yes" />
	
	<xsl:variable name="strict-prefix" select="'http://purl.oclc.org/ooxml/'"/>
    <xsl:variable name="usual-prefix" select="'http://schemas.openxmlformats.org/'"/>
    <xsl:variable name="add2006" select="'2006'"/>

    <xsl:template name="calculate-new-namespace">
        <xsl:param name="old-uri"/>
        <xsl:variable name="uri-stripped-prefix" 
            select="substring-after($old-uri, $strict-prefix)"/>
            
        <xsl:variable name="X" select="substring-before($uri-stripped-prefix, 
                                     substring-after($uri-stripped-prefix, '/'))"/>
                                     
        <xsl:variable name="Y" select="substring-after($uri-stripped-prefix, 
                                    substring-before($uri-stripped-prefix, '/'))"/>

        <xsl:value-of select="concat($usual-prefix, $X, $add2006, $Y)"/>
    </xsl:template>	

	<xsl:template match="node()|/">
		<xsl:param name="old-uri" select="namespace-uri()"/>
	
		<xsl:choose>
		
			<xsl:when test="self::*|/"><!-- elements -->
				
				<xsl:choose>
							
					<xsl:when test="starts-with(namespace-uri(), $usual-prefix)">
						<!-- leave it alone-->
						<xsl:element namespace="{namespace-uri()}" name="{local-name(.)}">
						      <xsl:apply-templates select="@*|node()"/>
						</xsl:element>
					</xsl:when>
		
					<xsl:when test="starts-with(namespace-uri(), $strict-prefix)">						
				        <xsl:variable name="new-namespace">
				            <xsl:call-template name="calculate-new-namespace">
				                <xsl:with-param name="old-uri" select="$old-uri"/>
				            </xsl:call-template>
				        </xsl:variable>
				
				        <xsl:element name="{local-name()}" namespace="{$new-namespace}">
				            <xsl:apply-templates select="@*|node()"/>
				        </xsl:element>
        			</xsl:when>
        			
					<xsl:otherwise>
						<xsl:copy>
							<xsl:apply-templates select="@*|node()" />
						</xsl:copy>
					</xsl:otherwise>
		
				</xsl:choose>
			
			</xsl:when>
			<xsl:otherwise> <!-- text nodes, comment nodes, and processing instruction nodes -->
				<xsl:copy>
					<xsl:apply-templates select="@*|node()" />
				</xsl:copy>
			</xsl:otherwise>

		</xsl:choose>

	</xsl:template>

	<!-- special case, because customProperties becomes custom-properties!	-->
	<xsl:template match="purlcp:*">
		<xsl:element name="{local-name(.)}"
			namespace="http://schemas.openxmlformats.org/officeDocument/2006/custom-properties">
		      <xsl:apply-templates select="@*|node()"/>
		</xsl:element>
	</xsl:template>

	<!-- special case, because extendedProperties becomes extended-properties!	-->
	<xsl:template match="purlep:*">
		<xsl:element name="{local-name(.)}"
			namespace="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties">
		      <xsl:apply-templates select="@*|node()"/>
		</xsl:element>
	</xsl:template>

	<!-- soecial case, http://schemas.microsoft.com/office/word/2006/wordml to w:  --> 
	<xsl:template match="wne:txbxContent" >
		<w:txbxContent>
		      <xsl:apply-templates select="@*|node()"/>		
		</w:txbxContent>	
	</xsl:template>


	<!-- this is only here to replace some of the purl namespaces-->
	<xsl:template match="w15:people">
		<w15:people xmlns:w15="http://schemas.microsoft.com/office/word/2012/wordml">
			<xsl:apply-templates select="@*|node()" />
		</w15:people>
	</xsl:template>

	<xsl:template match="@*">
		<xsl:param name="old-uri" select="namespace-uri()"/>
		<xsl:choose>

			<xsl:when test="starts-with(namespace-uri(), $strict-prefix)">						
		        <xsl:variable name="new-namespace">
		            <xsl:call-template name="calculate-new-namespace">
		                <xsl:with-param name="old-uri" select="$old-uri"/>
		            </xsl:call-template>
		        </xsl:variable>
		
		        <xsl:attribute name="{local-name()}" namespace="{$new-namespace}">
					<xsl:choose>
					
						<!-- font sizes <w:sz w:val="48.40pt"/> convert to half points -->
					    <xsl:when test="parent::purlw:sz and substring(., string-length(.) - 1) = 'pt'">
					    
							<xsl:variable name="dummy2" select="java:org.docx4j.jaxb.JaxbValidationEventHandler.logXml(..)" />
								
							<xsl:value-of select="round(substring-before(., 'pt') * 2)" />								
							
						</xsl:when>
						
						<!-- sectPr points to twips-->						
						<!-- w:pgSz -->						
						<xsl:when test="(local-name()='h' or local-name()='w') and parent::purlw:pgSz and  substring(., string-length(.) - 1) = 'pt'">	
													
							<xsl:variable name="dummy2" select="java:org.docx4j.jaxb.JaxbValidationEventHandler.logXml(..)" />
								
							<xsl:value-of select="round(substring-before(., 'pt') * 20)"/>
							
						</xsl:when>
						<!-- <w:pgMar w:top="72pt" w:right="36pt" w:bottom="72pt" w:left="36pt" w:header="35.40pt" w:footer="35.40pt" w:gutter="0pt"/>
						     <w:cols w:space="35.40pt"/> -->
						<xsl:when test="(parent::purlw:pgMar or parent::purlw:cols) and  substring(., string-length(.) - 1) = 'pt'">	
													
							<xsl:variable name="dummy2" select="java:org.docx4j.jaxb.JaxbValidationEventHandler.logXml(..)" />
								
							<xsl:value-of select="round(substring-before(., 'pt') * 20)"/>
							
						</xsl:when>
						
					
						<!-- points to twips-->						
						<xsl:when test="substring(., string-length(.) - 1) = 'pt'">	
													
							<xsl:variable name="dummy2" select="java:org.docx4j.jaxb.JaxbValidationEventHandler.logXml(..)" />
								
							<!--xsl:value-of select="round(substring-before(., 'pt') * 20)"/-->
							<xsl:value-of select="." />
							
						</xsl:when>
						
						<!-- % to thousands of a percent -->						
						<xsl:when test="substring(., string-length(.) ) = '%'">
		
							<xsl:variable name="dummy2" select="java:org.docx4j.jaxb.JaxbValidationEventHandler.logXml(..)" />
							
							<xsl:variable name="cleanValue" select="translate(., '%', '')"/>
   						    <!--xsl:value-of select="$cleanValue * 1000"/-->
							<xsl:value-of select="." />
   						    							
						</xsl:when>
						
						<xsl:otherwise>
							<xsl:value-of select="." />
						</xsl:otherwise>
					</xsl:choose>
		        </xsl:attribute>
			</xsl:when>

			<xsl:when test="namespace-uri() = ''"> <!-- eg theme part percentages -->
				<xsl:attribute name="{local-name(.)}">
					
					<xsl:choose>
					
						<!-- points to twips-->
						<xsl:when test="substring(., string-length(.) - 1) = 'pt'">	
													
							<xsl:variable name="dummy2" select="java:org.docx4j.jaxb.JaxbValidationEventHandler.logXml(..)" />
								
							<!--xsl:value-of select="round(substring-before(., 'pt') * 20)"/-->
							<xsl:value-of select="." />
							
						</xsl:when>
						
						<!-- % to thousands of a percent -->						
						<xsl:when test="substring(., string-length(.) ) = '%'">
		
							<xsl:variable name="dummy2" select="java:org.docx4j.jaxb.JaxbValidationEventHandler.logXml(..)" />
							
							<xsl:variable name="cleanValue" select="translate(., '%', '')"/>
   						    <xsl:value-of select="$cleanValue * 1000"/>
   						    							
						</xsl:when>
						
						<xsl:otherwise>
							<xsl:value-of select="." />
						</xsl:otherwise>
					</xsl:choose>

				</xsl:attribute>
			</xsl:when>

			<xsl:otherwise>
				<!-- for example: 
						http://schemas.microsoft.com/office/word/2010/wordml,  
						http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing
						
				<xsl:variable name="dummy" select="java:org.docx4j.jaxb.JaxbValidationEventHandler.log(concat('Not altering ', namespace-uri() ))" />			
						
						-->
				<xsl:copy-of select="."/>
			</xsl:otherwise>

		</xsl:choose>

	</xsl:template>
	

	<xsl:template match="purlw:document/@purlw:conformance" />
	
	<!-- <w:zoom w:percent="228%"/>-->
	<xsl:template match="purlw:zoom"/>
  
  <!--
	<a:graphicData uri="http://purl.oclc.org/ooxml/drawingml/picture">
	should be uri="http://schemas.openxmlformats.org/drawingml/2006/picture"
	
	etc
  -->
  <xsl:template match="purla:graphicData/@uri">
	
	<xsl:choose>
	
		<xsl:when test="starts-with(., $strict-prefix)">		
						
	        <xsl:variable name="new-namespace">
	            <xsl:call-template name="calculate-new-namespace">
	                <xsl:with-param name="old-uri" select="."/>
	            </xsl:call-template>
	        </xsl:variable>
	
	        <xsl:attribute name="uri" >
				<xsl:value-of select="$new-namespace" />
	        </xsl:attribute>
		</xsl:when>
		
		<xsl:otherwise>
			<xsl:copy-of select="."/>
		</xsl:otherwise>
	
	</xsl:choose>
	
  </xsl:template>
  
  <!-- end of purl (strict) importing --> 
  
  <xsl:template match="mc:AlternateContent">  
  
	<xsl:variable name="dummy" 
		select="java:org.docx4j.utils.XSLTUtils.logWarn('Found some mc:AlternateContent')" />
		
  	<xsl:choose>
  	
	    <xsl:when test="parent::w:r">
				<!--  v3.3.8 is OK with mc:AlternateContent in a run; the exporters choose
				      the branch (AbstractVisitorExporterGenerator, docx2fo.xslt)  -->
			<xsl:variable name="dummyRetain" 
				select="java:org.docx4j.utils.XSLTUtils.logWarn('mc:AlternateContent present in run; retaining')" />
		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()"/>
		    </xsl:copy>
		</xsl:when>

			<!-- See comment in SlidePart as to why we don't do this!
  	
  		<xsl:when test="mc:Choice[@Requires='v']">
  		
  			<xsl:variable name="message" 
  				select="string('Selecting mc:Choice[@Requires=v]')" />  			
			<xsl:variable name="logging" 
				select="java:org.docx4j.utils.XSLTUtils.logWarn($message)" />
				
  			<xsl:copy-of select="mc:Choice[@Requires='v']/*"/>

  		</xsl:when>   -->

		<!-- Word draws the first mc:Choice whose @Requires names a namespace it
		     understands (ECMA-376 Part 3 10.2.1), and reaches the mc:Fallback only when
		     it understands none of them.  Which prefixes we claim - wps by default,
		     whose wps:txbx/w:txbxContent both exporters do draw - is
		     docx4j.jaxb.mc.preferChoice; see XSLTUtils.mcPreferredChoiceRequires for the
		     measurement.  Empty restores the pre-17.1.0 fallback-always behaviour.
		     @since 17.1.0 -->
  		<xsl:when test="mc:Choice[java:org.docx4j.utils.XSLTUtils.mcPrefersChoice(string(@Requires))]">

  			<xsl:variable name="chosen"
  				select="mc:Choice[java:org.docx4j.utils.XSLTUtils.mcPrefersChoice(string(@Requires))][1]"/>
			<xsl:variable name="logging"
				select="java:org.docx4j.utils.XSLTUtils.logWarn(concat('Selecting mc:Choice Requires=', $chosen/@Requires))" />

  			<xsl:apply-templates select="$chosen/*"/>

  		</xsl:when>

  		<xsl:when test="mc:Fallback">
  		
  			<xsl:variable name="message" 
  				select="concat('Selecting ', name(mc:Fallback/*[1]) )" />  			
			<xsl:variable name="logging" 
				select="java:org.docx4j.utils.XSLTUtils.logWarn($message)" />
				
  			<xsl:copy-of select="mc:Fallback/*"/>
  			
  		</xsl:when>
  		<xsl:otherwise> 
			<xsl:variable name="logging" 
				select="java:org.docx4j.utils.XSLTUtils.logWarn('Missing mc:Fallback!  Dropping the mc:AlternateContent entirely.')" />
				<!--   
  			    <xsl:copy-of select="mc:Choice[1]/*"/>
  			-->
  		</xsl:otherwise>  		
  	</xsl:choose>    
  </xsl:template>

	<!--  Most JAXB implementations don't signal additional attributes as errors. -->
  <xsl:template match="@wordml201011:*" />


	<!-- Workaround for Google Docs as at 20140225 <w:tblW w:w="10206.0" w:type="dxa"/> 
       See http://www.docx4java.org/forums/docx-java-f6/problem-with-document-created-by-google-docs-t1802.html
       Google Docs make the same error in many places.. 
       
       and at 201504 <w:pgSz w:h="16839.0" w:w="11907.0"/>
       See http://www.docx4java.org/forums/docx-java-f6/parsing-error-when-reading-a-document-from-google-docs-t2160.html
       
       pandoc 2.2.1 makes the same error on tbl:w; see https://github.com/plutext/docx4j/issues/298
        -->
  
  <xsl:template match="@w:w" >

  	  <xsl:choose>
			<!--  limit fix to certain cases -->
  		<xsl:when test="../@w:type='dxa' or local-name(..)='pgSz' or local-name(..)='gridCol' or local-name(..)='tblW' or local-name(..)='tblInd'  ">
		  	<xsl:attribute name="w:w"><xsl:value-of select="format-number(., '#')" /></xsl:attribute>
  		</xsl:when>
  		<xsl:otherwise>
		    <xsl:copy-of select="."/>
  		</xsl:otherwise>
  	</xsl:choose> 
  	
  </xsl:template> 

  <xsl:template match="w:pgSz/@w:h" >
		  	<xsl:attribute name="w:h"><xsl:value-of select="format-number(., '#')" /></xsl:attribute>
  </xsl:template> 
  
  <xsl:template match="w:spacing/@w:line" >
		  	<xsl:attribute name="w:line"><xsl:value-of select="format-number(., '#')" /></xsl:attribute>
  </xsl:template> 
  
  <xsl:template match="w:spacing/@w:after" >
           <xsl:attribute name="w:after"><xsl:value-of select="format-number(., '#')" /></xsl:attribute>
  </xsl:template>  
  
  <xsl:template match="w:ind/@w:hanging" >  <!--  20170504 w:hanging="141.99999999999994" -->
           <xsl:attribute name="w:hanging"><xsl:value-of select="format-number(., '#')" /></xsl:attribute>
  </xsl:template>  

  <xsl:template match="w:ind/@w:firstLine" >  <!--  2025 10, v11.5.7 -->
           <xsl:attribute name="w:firstLine"><xsl:value-of select="format-number(., '#')" /></xsl:attribute>
  </xsl:template>  

  <xsl:template match="w:trHeight/@w:val" >
           <xsl:attribute name="w:val"><xsl:value-of select="format-number(., '#')" /></xsl:attribute>
  </xsl:template>


	<!-- 
        <w:pBdr>
          <w:top w:sz="7" w:space="1.8" w:color="#333437" w:val="single"/>
          <w:left w:sz="7" w:space="0" w:color="#000000" w:val="single"/>
          <w:bottom w:sz="3" w:space="7.2" w:color="#323539" w:val="double"/>
          <w:right w:sz="7" w:space="0" w:color="#000000" w:val="single"/>
        </w:pBdr>  
   -->
  <xsl:template match="@w:space" >
  	  <xsl:choose>
  		<xsl:when test="local-name(..)='top' or local-name(..)='left' or local-name(..)='bottom' or local-name(..)='right'">
		  	<xsl:attribute name="w:space"><xsl:value-of select="format-number(., '#')" /></xsl:attribute>
  		</xsl:when>
  		<xsl:otherwise>
		    <xsl:copy-of select="."/>
  		</xsl:otherwise>
  	</xsl:choose> 
  </xsl:template>


	<!-- Workaround for Microsoft SQLServer Reporting Service (SSRS) 2012, which generates invalid docx, for example:
  
    <w:sectPr w:rsidRPr="" w:rsidDel="" w:rsidR="" w:rsidSect="">
      <w:pgSz w:w="11905" w:h="16837"/>
      <w:pgMar w:top="1133" w:right="1133" w:bottom="1133" w:left="1133" w:header="" w:footer="" w:gutter=""/>
    </w:sectPr>
    
       
       http://connect.microsoft.com/SQLServer/feedback/details/614558/word-export-sets-margin-top-margin-bottom-to-0mm says 
	   "Word and SSRS treat page headers and footers differently. Word actually positions them inside the page margins, 
	    whereas SSRS positions them inside the area that the margins surround. As a result, in Word, the page margins 
	    do not control the distance between the top edge of the page and that of the page header (or similarly for the page footer).        
        Instead, Word has separate "Header from Top" and "Footer from Bottom" properties to control those distances. 
        Since RDL does not have equivalent properties, the Word renderer sets these properties to zero."
       
       But it is actually setting them to blank! Here we honor the intent by making them zero.

       For SSRS exporting to Word generally, see http://technet.microsoft.com/en-us/library/dd283105.aspx 
      
   -->
   
  <xsl:template match="@w:rsidRPr[not(string())]" />
  <xsl:template match="@w:rsidDel[not(string())]" />
  <xsl:template match="@w:rsidR[not(string())]" />
  <xsl:template match="@w:rsidSect[not(string())]" />
  
  <xsl:template match="@w:header[not(string())]" >
  	<xsl:attribute name="w:header">0</xsl:attribute>
  </xsl:template>
  
  <xsl:template match="@w:footer[not(string())]" >
  	<xsl:attribute name="w:footer">0</xsl:attribute>
  </xsl:template>
   
  <xsl:template match="@w:gutter[not(string())]" />

	<!-- Malformed nesting from broken producers: a w:r directly inside a w:r, and a w:p
	     directly inside a w:r or a w:hyperlink.  Neither is in the content model, so JAXB
	     reports "unexpected element" and (once this preprocessor has let unmarshalling
	     continue) the whole subtree is discarded silently - one corpus document lost 412
	     lines that way, and 10 documents of three corpora hold the shape.  Word renders
	     them, so the content is hoisted into the legal position around it.

	     The run holding the nested content is *split*: each nested run becomes a sibling
	     run, keeping its own w:rPr, and each of the outer run's own children is wrapped in
	     a run carrying the outer w:rPr.  Flattening the nested runs' content into the outer
	     run instead - which is what 17.1.0 first did - loses their character formatting and
	     their whitespace: on a document whose hyperlinks hold
	     w:hyperlink/w:r/(w:rPr, w:r, w:r, ...) with the words in w:rStyle="Highlight" runs
	     and the spaces between them in runs of their own, Word paints
	     "Orban Viktor >> Mondatok" in the highlighted style and we painted
	     "OrbanViktor>> Mondatok" - the lone-space runs gone - on 466 lines.
	     Only a direct child is matched: a w:r deeper inside a w:r is the ordinary shape of
	     a text box (w:r/w:pict/v:textbox/w:txbxContent/w:p/w:r), which is perfectly legal.
	     @since 17.1.0 -->

	<xsl:template match="w:r[w:r or w:p]">
		<xsl:variable name="logging"
			select="java:org.docx4j.utils.XSLTUtils.logWarn('w:r or w:p nested directly in w:r; splitting the run')" />
		<xsl:variable name="outerRPr" select="w:rPr" />
		<xsl:for-each select="*[not(self::w:rPr)]">
			<xsl:choose>
				<xsl:when test="self::w:r">
					<xsl:apply-templates select="." />
				</xsl:when>
				<xsl:when test="self::w:p">
					<xsl:apply-templates select="*[not(self::w:pPr)]" mode="unwrap-paragraphs" />
				</xsl:when>
				<xsl:otherwise>
					<w:r>
						<xsl:apply-templates select="$outerRPr" />
						<xsl:apply-templates select="." />
					</w:r>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>

	<!-- a w:hyperlink takes runs, so the nested paragraph's own content stands as it is -->
	<xsl:template match="w:hyperlink/w:p">
		<xsl:variable name="logging"
			select="java:org.docx4j.utils.XSLTUtils.logWarn('w:p nested directly in w:hyperlink; hoisting its content')" />
		<xsl:apply-templates select="*[not(self::w:pPr)]" mode="unwrap-paragraphs" />
	</xsl:template>

	<!-- a paragraph nested where only runs are legal contributes the runs it holds; a
	     run, a hyperlink or anything else legal there is copied as it stands, with its
	     own properties -->
	<xsl:template match="w:p" mode="unwrap-paragraphs">
		<xsl:apply-templates select="*[not(self::w:pPr)]" mode="unwrap-paragraphs" />
	</xsl:template>

	<xsl:template match="w:sdt | w:sdtContent" mode="unwrap-paragraphs">
		<xsl:apply-templates select="*[not(self::w:sdtPr) and not(self::w:sdtEndPr)]" mode="unwrap-paragraphs" />
	</xsl:template>

	<xsl:template match="node()" mode="unwrap-paragraphs">
		<xsl:apply-templates select="." />
	</xsl:template>

</xsl:stylesheet>
