
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"

	xmlns:java="http://xml.apache.org/xalan/java"
	xmlns:xalan="http://xml.apache.org/xalan"
	
	xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006"
	
	xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
	xmlns:x="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
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
  	
	    <xsl:when test="parent::w:r or parent::w:p or parent::w:numPicBullet or parent::x:workbook">
				<!-- The schema admits mc:AlternateContent here (w:r since 3.3.8; w:p and
				     w:numPicBullet since 17.1.1, CR-021 phase 2; x:workbook, Excel's x15
				     absPath, in sml.xsd all along), so JAXB keeps both branches and the
				     consumers choose one through org.docx4j.jaxb.McSelection.  This
				     stylesheet is reached for every package format whenever JAXB rejected
				     an element SOMEWHERE in the part - not only an mc:AlternateContent, so
				     a workbook with an xr:revisionPtr comes through here too - and an
				     element under one of these parents is passed through whole.
				     PresentationML's admitted parents (p:spTree, p:grpSp, p:controls) join
				     this list in CR-021 phase 3, with the pptx4j consumers. -->
			<xsl:variable name="dummyRetain" 
				select="java:org.docx4j.utils.XSLTUtils.logWarn(concat('mc:AlternateContent in ', name(..), '; retaining'))" />
		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()"/>
		    </xsl:copy>
		</xsl:when>

		<!-- Any other parent: in WordprocessingML one the survey of CR-021 phase 0
		     (835 documents) never saw; in the other formats a parent the schema may
		     admit but whose consumers have not yet been made branch-aware (PresentationML
		     is CR-021 phase 3), or one it does not (a chart's c:chartSpace, a
		     spreadsheet drawing's xdr:oneCellAnchor).  The element is resolved to the one
		     branch docx4j draws (org.docx4j.jaxb.McSelection's rule, through
		     XSLTUtils.mcPrefersChoice: the first Choice whose Requires prefixes are all in
		     docx4j.jaxb.mc.preferChoice, else the Fallback, else dropped), with a warning
		     naming the parent.  A VML-first branch that lived here from 2012 never ran
		     and is gone. -->
		<xsl:when test="mc:Choice[java:org.docx4j.utils.XSLTUtils.mcPrefersChoice(string(@Requires))]">
			<xsl:variable name="dummyParent"
				select="java:org.docx4j.utils.XSLTUtils.logWarn(concat('mc:AlternateContent in ', name(..), ' is not kept by the preprocessor (CR-021: kept in w:r, w:p, w:numPicBullet, x:workbook); resolving it'))" />

  			<xsl:variable name="chosen"
  				select="mc:Choice[java:org.docx4j.utils.XSLTUtils.mcPrefersChoice(string(@Requires))][1]"/>
			<xsl:variable name="logging"
				select="java:org.docx4j.utils.XSLTUtils.logWarn(concat('Selecting mc:Choice Requires=', $chosen/@Requires))" />

  			<xsl:apply-templates select="$chosen/*"/>

  		</xsl:when>

  		<xsl:when test="mc:Fallback">
  		
  			<xsl:variable name="message" 
  				select="concat('mc:AlternateContent in ', name(..), ' is not kept by the preprocessor (CR-021: kept in w:r, w:p, w:numPicBullet, x:workbook); selecting its Fallback ', name(mc:Fallback/*[1]))" />  			
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

	     A nested *paragraph* is written as a paragraph of its own since 17.1.1 - see the
	     w:p template further down; the hoisting here is the fallback for the shapes it
	     does not reach.

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

	<!-- A paragraph nested in a w:hyperlink or a w:r is a paragraph of its own: Word gives
	     it its own line and its own w:pPr - style, spacing, indent.  17.1.0 hoisted its runs
	     into the paragraph around it (the templates below, kept for the shapes this one
	     does not reach), which kept the text but lost the paragraph properties: a corpus
	     document's 76 article summaries, each a w:p inside the w:hyperlink of the paragraph
	     before it, in a style whose w:i is the only italic they have, came out roman, with
	     no space after and no indent, where Word draws 45 italic lines - 0.60 to 0.47 of
	     line parity and three pages.

	     So the outer paragraph is split around each nested one: what precedes the nested
	     paragraph stays in the outer paragraph (its containers - the hyperlink, a run -
	     copied with only that part of their content), the nested paragraph follows as a
	     sibling with its own w:pPr, and what follows it goes into a further paragraph
	     carrying the outer w:pPr.  A segment with nothing but its containers in it is not
	     written, so a nested paragraph that ends its hyperlink adds no empty line.  Only
	     the shallow shapes are matched (a w:p one or two levels down, in a w:hyperlink or a
	     w:r): a w:p deeper inside a run is a text box's, and legal.  @since 17.1.1 -->

	<xsl:template match="w:p[w:hyperlink/w:p or w:r/w:p or w:hyperlink/w:r/w:p or w:r/w:r/w:p]">
		<xsl:variable name="logging"
			select="java:org.docx4j.utils.XSLTUtils.logWarn('w:p nested in w:hyperlink or w:r; split into paragraphs of its own')" />
		<xsl:variable name="outer" select="." />
		<xsl:variable name="nested" select="w:hyperlink/w:p | w:r/w:p | w:hyperlink/w:r/w:p | w:r/w:r/w:p" />
		<!-- the outer content before the first nested paragraph -->
		<xsl:variable name="lead">
			<xsl:call-template name="paragraph-segment">
				<xsl:with-param name="outer" select="$outer" />
				<xsl:with-param name="nested" select="$nested" />
				<xsl:with-param name="before" select="$nested[1]" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="leadHasContent">
			<xsl:call-template name="segment-has-content">
				<xsl:with-param name="segment" select="$lead" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:if test="string($leadHasContent)">
			<w:p>
				<xsl:apply-templates select="$outer/@*" />
				<xsl:apply-templates select="$outer/w:pPr" />
				<xsl:copy-of select="xalan:nodeset($lead)/node()" />
			</w:p>
		</xsl:if>
		<xsl:for-each select="$nested">
			<xsl:variable name="i" select="position()" />
			<!-- the outer content between this nested paragraph and the next (or the end) -->
			<xsl:variable name="tail">
				<xsl:call-template name="paragraph-segment">
					<xsl:with-param name="outer" select="$outer" />
					<xsl:with-param name="nested" select="$nested" />
					<xsl:with-param name="after" select="." />
					<xsl:with-param name="before" select="$nested[$i + 1]" />
				</xsl:call-template>
			</xsl:variable>
			<xsl:variable name="tailHasContent">
				<xsl:call-template name="segment-has-content">
					<xsl:with-param name="segment" select="$tail" />
				</xsl:call-template>
			</xsl:variable>
			<w:p>
				<xsl:apply-templates select="@*" />
				<xsl:apply-templates select="w:pPr" />
				<!-- a segment with no content of its own is not written as a paragraph (it
				     would be an empty line Word does not draw); the bookmarks it holds, which
				     are anchors, move into this paragraph instead -->
				<xsl:if test="$i = 1 and not(string($leadHasContent))">
					<xsl:copy-of select="xalan:nodeset($lead)//w:bookmarkStart | xalan:nodeset($lead)//w:bookmarkEnd" />
				</xsl:if>
				<xsl:apply-templates select="*[not(self::w:pPr)]" />
				<xsl:if test="not(string($tailHasContent))">
					<xsl:copy-of select="xalan:nodeset($tail)//w:bookmarkStart | xalan:nodeset($tail)//w:bookmarkEnd" />
				</xsl:if>
			</w:p>
			<xsl:if test="string($tailHasContent)">
				<w:p>
					<xsl:apply-templates select="$outer/@*" />
					<xsl:apply-templates select="$outer/w:pPr" />
					<xsl:copy-of select="xalan:nodeset($tail)/node()" />
				</w:p>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>

	<!-- One segment of the split: the outer paragraph's content between the nested
	     paragraph $after (or the start) and the nested paragraph $before (or the end),
	     transformed, without the w:p around it. -->
	<xsl:template name="paragraph-segment">
		<xsl:param name="outer" />
		<xsl:param name="nested" />
		<xsl:param name="after" select="/.." />
		<xsl:param name="before" select="/.." />
		<xsl:variable name="afterSet" select="$after/following::*" />
		<xsl:variable name="beforeSet" select="$before/preceding::*" />
		<xsl:apply-templates select="$outer/*" mode="segment">
			<xsl:with-param name="nested" select="$nested" />
			<xsl:with-param name="after" select="$after" />
			<xsl:with-param name="before" select="$before" />
			<xsl:with-param name="afterSet" select="$afterSet" />
			<xsl:with-param name="beforeSet" select="$beforeSet" />
		</xsl:apply-templates>
	</xsl:template>

	<!-- 'y' if the segment holds anything beyond its containers, run properties,
	     proofing marks and bookmarks - i.e. something Word would give a line to. -->
	<xsl:template name="segment-has-content">
		<xsl:param name="segment" />
		<xsl:if test="xalan:nodeset($segment)//*[not(self::w:hyperlink or self::w:r or self::w:rPr or ancestor::w:rPr
				or self::w:proofErr or self::w:bookmarkStart or self::w:bookmarkEnd)]">y</xsl:if>
	</xsl:template>

	<!-- The outer paragraph's children, restricted to the range: a nested paragraph is
	     skipped (it is written on its own), a container holding one is copied with only
	     the part of its content that lies in the range - a w:r so holding one is split as
	     the w:r[w:r or w:p] template splits it - and anything else is copied whole if it
	     lies in the range. -->
	<xsl:template match="*" mode="segment">
		<xsl:param name="nested" />
		<xsl:param name="after" />
		<xsl:param name="before" />
		<xsl:param name="afterSet" />
		<xsl:param name="beforeSet" />
		<xsl:choose>
			<xsl:when test="self::w:pPr or count(. | $nested) = count($nested)" />
			<xsl:when test="descendant::*[count(. | $nested) = count($nested)]">
				<xsl:choose>
					<xsl:when test="self::w:r">
						<xsl:variable name="outerRPr" select="w:rPr" />
						<xsl:for-each select="*[not(self::w:rPr)]">
							<xsl:choose>
								<xsl:when test="count(. | $nested) = count($nested)" />
								<xsl:when test="self::w:r or descendant::*[count(. | $nested) = count($nested)]">
									<xsl:apply-templates select="." mode="segment">
										<xsl:with-param name="nested" select="$nested" />
										<xsl:with-param name="after" select="$after" />
										<xsl:with-param name="before" select="$before" />
										<xsl:with-param name="afterSet" select="$afterSet" />
										<xsl:with-param name="beforeSet" select="$beforeSet" />
									</xsl:apply-templates>
								</xsl:when>
								<xsl:when test="(not($after) or count(. | $afterSet) = count($afterSet))
										and (not($before) or count(. | $beforeSet) = count($beforeSet))">
									<w:r>
										<xsl:apply-templates select="$outerRPr" />
										<xsl:apply-templates select="." />
									</w:r>
								</xsl:when>
							</xsl:choose>
						</xsl:for-each>
					</xsl:when>
					<xsl:otherwise>
						<xsl:copy>
							<xsl:apply-templates select="@*" />
							<xsl:apply-templates select="*" mode="segment">
								<xsl:with-param name="nested" select="$nested" />
								<xsl:with-param name="after" select="$after" />
								<xsl:with-param name="before" select="$before" />
								<xsl:with-param name="afterSet" select="$afterSet" />
								<xsl:with-param name="beforeSet" select="$beforeSet" />
							</xsl:apply-templates>
						</xsl:copy>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="(not($after) or count(. | $afterSet) = count($afterSet))
					and (not($before) or count(. | $beforeSet) = count($beforeSet))">
				<xsl:apply-templates select="." />
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<!-- The hoisting fallback for a nested paragraph the split above does not reach (one
	     further down, or in a hyperlink that is not the paragraph's own child): a
	     w:hyperlink takes runs, so the nested paragraph's own content stands as it is -->
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
