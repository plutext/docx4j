
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"

	xmlns:java="http://xml.apache.org/xalan/java"
	xmlns:xalan="http://xml.apache.org/xalan"
	
	xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006"
	xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math"
	
	xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
	xmlns:x="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
	xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"
	xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml"
	
 	xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
 	xmlns:xdr="http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing"
 	xmlns:c="http://schemas.openxmlformats.org/drawingml/2006/chart"
 		
	xmlns:wordml201011="http://schemas.microsoft.com/office/word/2010/11/wordml"
	xmlns:w15="http://schemas.microsoft.com/office/word/2012/wordml"

	xmlns:purlw="http://purl.oclc.org/ooxml/wordprocessingml/main"
 	xmlns:purla="http://purl.oclc.org/ooxml/drawingml/main"
 	xmlns:purlwp="http://purl.oclc.org/ooxml/drawingml/wordprocessingDrawing"
 
	xmlns:purlcp="http://purl.oclc.org/ooxml/officeDocument/customProperties"
	xmlns:purlep="http://purl.oclc.org/ooxml/officeDocument/extendedProperties"
		
	version="1.0" exclude-result-prefixes="m java purlw purlep purla purlcp purlwp xalan">	
        
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
      it in w:r.  Since 17.3.4 (CR-051, D3) the keep rule
      is the schema's admissions, so in WordprocessingML
      it is kept wherever Word reads it, and the branch is
      chosen after unmarshal (org.docx4j.jaxb.McBlockLevel).
      
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

	<!-- Strict puts the 2010 shape, group and canvas elements (transitional wps:, wpg:,
	     wpc:) in the wordprocessingDrawing namespace: wp:wsp, wp:wgp, wp:wpc and their
	     children. Mapped by name for the three roots, and by the nearest such ancestor
	     for what is inside them (a wsp inside a wgp is wps; a grpSp or graphicFrame
	     inside a wgp is wpg). Until 2026-09-21 these became wp:wsp etc., unknown in the
	     transitional wp namespace, and Word refused the document. -->
	<xsl:template match="purlwp:*[local-name()='wsp' or local-name()='wgp' or local-name()='wpc'
	                              or ancestor::purlwp:*[local-name()='wsp' or local-name()='wgp' or local-name()='wpc']]"
	              priority="2">
		<xsl:variable name="owner">
			<xsl:choose>
				<xsl:when test="local-name()='wsp' or local-name()='wgp' or local-name()='wpc'">
					<xsl:value-of select="local-name()"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="local-name(ancestor::purlwp:*[local-name()='wsp' or local-name()='wgp' or local-name()='wpc'][1])"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="new-namespace">
			<xsl:choose>
				<xsl:when test="$owner='wsp'">http://schemas.microsoft.com/office/word/2010/wordprocessingShape</xsl:when>
				<xsl:when test="$owner='wgp'">http://schemas.microsoft.com/office/word/2010/wordprocessingGroup</xsl:when>
				<xsl:otherwise>http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:element name="{local-name()}" namespace="{$new-namespace}">
			<xsl:apply-templates select="@*|node()"/>
		</xsl:element>
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
						
					
						<!-- WordprocessingML points to half-points: the attributes typed ST_HpsMeasure
						     (w:sz is above; szCs, kern, position, and the ruby hps* here). 2026-09-21 -->
						<xsl:when test="namespace-uri(..) = 'http://purl.oclc.org/ooxml/wordprocessingml/main'
						                and (local-name(..)='szCs' or local-name(..)='kern' or local-name(..)='position'
						                     or local-name(..)='hps' or local-name(..)='hpsRaise' or local-name(..)='hpsBaseText')
						                and substring(., string-length(.) - 1) = 'pt'">

							<xsl:variable name="dummy2" select="java:org.docx4j.jaxb.JaxbValidationEventHandler.logXml(..)" />

							<xsl:value-of select="round(substring-before(., 'pt') * 2)" />

						</xsl:when>

						<!-- WordprocessingML points to twips: everything else Word writes in points in a
						     strict document (w:spacing before/after/line, w:ind, table widths, borders ...),
						     bound as integers on the transitional side. Until 2026-09-21 this branch passed
						     the value through, so every strict document Word saves failed to load on its
						     styles part (docDefaults w:spacing w:line="12.95pt" = 259). w:line under
						     lineRule="auto" is right at x20 too: Word writes the 240ths as points. -->
						<xsl:when test="namespace-uri(..) = 'http://purl.oclc.org/ooxml/wordprocessingml/main'
						                and substring(., string-length(.) - 1) = 'pt'">

							<xsl:variable name="dummy2" select="java:org.docx4j.jaxb.JaxbValidationEventHandler.logXml(..)" />

							<xsl:value-of select="round(substring-before(., 'pt') * 20)"/>

						</xsl:when>

						<!-- points in another strict namespace: passed through, as before -->
						<xsl:when test="substring(., string-length(.) - 1) = 'pt'">	
													
							<xsl:variable name="dummy2" select="java:org.docx4j.jaxb.JaxbValidationEventHandler.logXml(..)" />
								
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
  	
	    <xsl:when test="not(parent::*) or parent::w:r or parent::w:p or parent::w:hyperlink or parent::w:fldSimple or parent::w:smartTag or parent::w:dir or parent::w:bdo
	    		or parent::w:numPicBullet
	    		or parent::w:body or parent::w:hdr or parent::w:ftr or parent::w:tc or parent::w:footnote or parent::w:endnote or parent::w:comment
	    		or parent::w:txbxContent or parent::w:docPartBody or parent::w:tbl or parent::w:tr
	    		or parent::w:sdtContent or parent::w:customXml
	    		or parent::mc:Choice or parent::mc:Fallback
	    		or parent::x:workbook or parent::x:worksheet or parent::x:controls
	    		or parent::p:spTree or parent::p:grpSp or parent::p:controls
	    		or parent::xdr:wsDr or parent::xdr:twoCellAnchor or parent::xdr:oneCellAnchor or parent::xdr:absoluteAnchor or parent::xdr:grpSp
	    		or parent::c:chartSpace">
				<!-- The schema admits mc:AlternateContent here (w:r since 3.3.8; w:p and
				     w:numPicBullet since 17.2.0, CR-021 phase 2; x:workbook, Excel's x15
				     absPath, in sml.xsd all along), so JAXB keeps both branches and the
				     consumers choose one through org.docx4j.jaxb.McSelection.
				     Since 17.3.4 (CR-051, D3) the WordprocessingML list is a flat list of
				     the schema's admitted parents: the EG_PContent family (w:p, w:hyperlink,
				     w:fldSimple, w:smartTag, w:dir, w:bdo), every block container (w:body,
				     w:hdr, w:ftr, w:tc, w:footnote, w:endnote, w:comment, w:txbxContent,
				     w:docPartBody), w:tbl and w:tr (D2), w:sdtContent and w:customXml at
				     every level, and mc:Choice and mc:Fallback for nested elements.  A
				     block-, row- or cell-level element so kept reaches the splice of
				     org.docx4j.jaxb.McBlockLevel after unmarshal, on every path, so the
				     branch is chosen in one place; before 17.3.4 a part sent through here
				     for ANY reason also had its run-level content controls', hyperlinks'
				     and simple fields' elements resolved (CR-051 section 1).  This
				     stylesheet is reached for every package format whenever JAXB rejected
				     an element SOMEWHERE in the part - not only an mc:AlternateContent, so
				     a workbook with an xr:revisionPtr comes through here too - and an
				     element under one of these parents is passed through whole.
				     PresentationML's admitted parents (p:spTree and p:grpSp, CT_GroupShape;
				     p:controls, CT_ControlList) joined in CR-021 phase 3, with the pptx4j
				     consumers (the placeholder walks, pptx2svginhtml.xslt) reading one
				     branch through McSelection.  The DrawingML hosts (a spreadsheet
				     drawing's root and its anchors, dml-spreadsheetDrawing.xsd; a chart's
				     c:chartSpace, dml-chart.xsd) joined in CR-024: a check box's a14 shape
				     with an EMPTY Fallback, a slicer's or timeline's graphicFrame, and the
				     c14 chart style were all resolved to Office 2007's view - for the
				     check box, a bare xdr:wsDr - by the branch below. -->
			<xsl:variable name="dummyRetain" 
				select="java:org.docx4j.utils.XSLTUtils.logWarn(concat('mc:AlternateContent in ', name(..), '; retaining'))" />
		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()"/>
		    </xsl:copy>
		</xsl:when>

		<!-- Any other parent: in WordprocessingML a property container (w:rPr, w:pPr,
		     w:tblPr, w:sectPr), which the survey of CR-021 phase 0 (835 documents) never
		     saw; in the other formats a parent the schema does
		     does not admit (since CR-024 none is known: the survey of the
		     repository's Office files and the corpus found only the hosts
		     above).  The element is resolved to the one
		     branch docx4j draws (org.docx4j.jaxb.McSelection's rule, through
		     XSLTUtils.mcPrefersChoice: the first Choice whose Requires prefixes are all in
		     docx4j.jaxb.mc.preferChoice, else the Fallback, else - since 17.3.6, WD REQ-305 -
		     the first Choice Word understands, else nothing: the element goes, as it did until
		     17.3.4, every Choice with it, since a schema that doesn't admit it can't keep it), with a warning
		     naming the parent.  A VML-first branch that lived here from 2012 never ran
		     and is gone. -->
		<xsl:when test="mc:Choice[java:org.docx4j.utils.XSLTUtils.mcPrefersChoice(string(@Requires))]">
			<xsl:variable name="dummyParent"
				select="java:org.docx4j.utils.XSLTUtils.logWarn(concat('mc:AlternateContent in ', name(..), ' is not kept by the preprocessor (CR-021/CR-024/CR-051: kept wherever the schema admits it); resolving it'))" />

  			<xsl:variable name="chosen"
  				select="mc:Choice[java:org.docx4j.utils.XSLTUtils.mcPrefersChoice(string(@Requires))][1]"/>
			<xsl:variable name="logging"
				select="java:org.docx4j.utils.XSLTUtils.logWarn(concat('Selecting mc:Choice Requires=', $chosen/@Requires))" />

  			<xsl:apply-templates select="$chosen/*"/>

  		</xsl:when>

  		<xsl:when test="mc:Fallback">
  		
  			<xsl:variable name="message" 
  				select="concat('mc:AlternateContent in ', name(..), ' is not kept by the preprocessor (CR-021/CR-024/CR-051: kept wherever the schema admits it); selecting its Fallback ', name(mc:Fallback/*[1]))" />  			
			<xsl:variable name="logging" 
				select="java:org.docx4j.utils.XSLTUtils.logWarn($message)" />
				
  			<xsl:copy-of select="mc:Fallback/*"/>
  			
  		</xsl:when>
  		<xsl:when test="mc:Choice[java:org.docx4j.utils.XSLTUtils.mcUnderstandsRequires(string(@Requires), namespace::*)]">
  			<!-- No Fallback, and no Choice preferred: the first Choice Word understands (WD REQ-305). -->
			<xsl:variable name="logging" 
				select="java:org.docx4j.utils.XSLTUtils.logWarn(concat('mc:AlternateContent in ', name(..), ' is not kept by the preprocessor and has no mc:Fallback; selecting its first understood mc:Choice'))" />
  			<xsl:apply-templates select="mc:Choice[java:org.docx4j.utils.XSLTUtils.mcUnderstandsRequires(string(@Requires), namespace::*)][1]/*"/>
  		</xsl:when>
  		<xsl:when test="mc:Choice">
  			<!-- No Fallback, and no Choice Word understands: nothing is selected (WD REQ-305). -->
			<xsl:variable name="logging" 
				select="java:org.docx4j.utils.XSLTUtils.logWarn(concat('mc:AlternateContent in ', name(..), ' is not kept by the preprocessor, has no mc:Fallback and no mc:Choice Word understands; nothing selected (Requires=', mc:Choice[1]/@Requires, ')'))" />
  		</xsl:when>
  		<xsl:otherwise> 
			<xsl:variable name="logging" 
				select="java:org.docx4j.utils.XSLTUtils.logWarn('mc:AlternateContent with no branch at all; dropping it.')" />
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

	     A nested *paragraph* is written as a paragraph of its own since 17.2.0 - see the
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
	     w:r): a w:p deeper inside a run is a text box's, and legal.  @since 17.2.0 -->

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
					<xsl:apply-templates select="$outer/w:pPr">
						<xsl:with-param name="head" select="false()" />
					</xsl:apply-templates>
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

	<!-- A w:br outside a w:r (CR-073).  Word 365 reads one directly in a w:p, a w:hyperlink or
	     another element that holds runs, one inside a w:t or a w:instrText, and one directly in
	     w:body, as a break at that point in the text, of its own type, and keeps all the text
	     (the br-placement probe, docx4j-core-tests/src/test/resources/br-placement).  JAXB has no
	     place for it there, so the second unmarshal dropped it, and inside a w:t the text before
	     it too.  docx4j's load survey found one in 206,351 of 3.7 million Common Crawl documents,
	     89% of them a web-to-docx generator's blank line, <w:p><w:br/></w:p>.

	     The break goes in a run of its own, with the run properties Word gives it: the paragraph
	     mark's while no run precedes it in its paragraph, none after one (round 2: never the
	     next run's or the previous run's).  A text element holding elements becomes its stretches
	     of text and its breaks, in order, in the same run; a w:t nested in a w:t gives its text.
	     A w:br directly in w:body starts the next paragraph.  One WARN a part.  @since 17.3.6 -->

	<xsl:param name="mainDocumentPart" select="false()" />

	<xsl:template match="/" priority="1">
		<xsl:variable name="breaks" select="count(//w:br[parent::w:p or parent::w:hyperlink or parent::w:smartTag
				or parent::w:customXml or parent::w:fldSimple or parent::w:ins or parent::w:del or parent::w:moveFrom
				or parent::w:moveTo or parent::w:sdtContent or parent::w:dir or parent::w:bdo or parent::w:body])" />
		<xsl:variable name="texts" select="count(//w:t[*] | //w:instrText[*] | //w:delText[*] | //w:delInstrText[*])" />
		<xsl:if test="$breaks + $texts > 0">
			<xsl:variable name="logging"
				select="java:org.docx4j.utils.XSLTUtils.logWarn(concat('w:br outside a w:r: ', $breaks,
					' outside a run, ', $texts, ' text elements holding elements; read as Word reads them (CR-073)'))" />
		</xsl:if>
		<xsl:choose>
			<!-- A main document part's root (CR-079 D9; WD REQ-319): a w: root Word doesn't know reads as if
			     its tags were absent, what it holds standing where a body's content stands, and w:sdtContent
			     as a body holding what it holds.  What it may hold is REQ-319's list; anything else stays as
			     it is, and the read fails, naming it.  Only MainDocumentPart sets the parameter, as no other
			     part is measured.  @since 17.3.6 -->
			<xsl:when test="$mainDocumentPart and *[self::w:sdtContent
					or (self::w:* and not(contains($wml-names, concat(' ', local-name(), ' ')))
						and not(*[not(self::w:p or self::w:tbl or self::w:sdt[not(w:sdtContent/w:r)] or self::w:body or self::w:sectPr
							or (self::w:* and not(self::w:sdt) and not(contains($wml-names, concat(' ', local-name(), ' ')))))]))]">
				<w:document>
					<xsl:copy-of select="*/@mc:Ignorable" />
					<w:body>
						<xsl:for-each select="*">
							<xsl:call-template name="root-content" />
						</xsl:for-each>
					</w:body>
				</w:document>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="node()" />
			</xsl:otherwise>
		</xsl:choose>
		<xsl:variable name="readThrough" select="java:org.docx4j.utils.XSLTUtils.warnReadThrough()" />
	</xsl:template>

	<!-- (1) directly in an element that holds runs, in a paragraph: a math run (m:r) holds a
	     w:br legally, so the parents are named; the nearest paragraph-or-block ancestor must be
	     a w:p, so a block-level w:sdtContent or w:customXml is not taken for an inline one -->
	<xsl:template match="w:br[parent::w:p or parent::w:hyperlink or parent::w:smartTag or parent::w:customXml
			or parent::w:fldSimple or parent::w:ins or parent::w:del or parent::w:moveFrom or parent::w:moveTo
			or parent::w:sdtContent or parent::w:dir or parent::w:bdo]
			[ancestor::*[self::w:p or self::w:body or self::w:tc or self::w:txbxContent or self::w:hdr or self::w:ftr
				or self::w:footnote or self::w:endnote or self::w:comment or self::w:docPartBody][1][self::w:p]]">
		<xsl:variable name="pid" select="generate-id(ancestor::w:p[1])" />
		<w:r>
			<!-- no run before it in its paragraph: on its path from the paragraph, no earlier
			     sibling holds one -->
			<xsl:if test="not(ancestor-or-self::*[generate-id(ancestor::w:p[1]) = $pid]/preceding-sibling::*[descendant-or-self::w:r])">
				<xsl:call-template name="mark-run-properties">
					<xsl:with-param name="p" select="ancestor::w:p[1]" />
				</xsl:call-template>
			</xsl:if>
			<w:br><xsl:apply-templates select="@*" /></w:br>
		</w:r>
	</xsl:template>

	<!-- the paragraph mark's run properties, as a run's: without what only a mark has -->
	<xsl:template name="mark-run-properties">
		<xsl:param name="p" />
		<xsl:variable name="props" select="$p/w:pPr/w:rPr/*[not(self::w:ins or self::w:del or self::w:moveFrom
				or self::w:moveTo or self::w:rPrChange)]" />
		<xsl:if test="$props">
			<w:rPr><xsl:apply-templates select="$props" /></w:rPr>
		</xsl:if>
	</xsl:template>

	<!-- (2) a text element holding elements: its text, its breaks, and the text of a text
	     element nested in it, in order; whitespace between them kept only where xml:space
	     preserves it -->
	<xsl:template match="w:t[*] | w:instrText[*] | w:delText[*] | w:delInstrText[*]">
		<xsl:apply-templates select="node()" mode="split-text">
			<xsl:with-param name="name" select="local-name()" />
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="text()" mode="split-text">
		<xsl:param name="name" />
		<xsl:variable name="space" select="string(../ancestor-or-self::*[@xml:space][1]/@xml:space)" />
		<xsl:if test="$space = 'preserve' or normalize-space(.) != ''">
			<xsl:element name="w:{$name}" namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
				<xsl:if test="$space != ''">
					<xsl:attribute name="xml:space"><xsl:value-of select="$space" /></xsl:attribute>
				</xsl:if>
				<xsl:value-of select="." />
			</xsl:element>
		</xsl:if>
	</xsl:template>

	<xsl:template match="w:br" mode="split-text">
		<w:br><xsl:apply-templates select="@*" /></w:br>
	</xsl:template>

	<!-- A nested text element gives its text.  Any other w: element goes with all it holds,
	     a w:br included, as does one in a namespace outside docx4j's understood set: Word's
	     check 76 (76n, 76u, 76w, 76z), CR-076's rule 2.  One in no namespace, or in a known
	     namespace other than w:, gives its text: Word refuses the first and is not measured on
	     the second.  The test is here, not in match patterns: as two patterns it took the
	     stylesheet's time up by 14% to 24% on the largest documents, though this mode is
	     rarely entered. -->
	<xsl:template match="*" mode="split-text">
		<xsl:param name="name" />
		<xsl:if test="self::w:t or self::w:instrText or self::w:delText or self::w:delInstrText
				or (not(self::w:*) and (namespace-uri() = ''
					or java:org.docx4j.utils.XSLTUtils.isUnderstoodNamespace(string(namespace-uri()))))">
			<xsl:apply-templates select="node()" mode="split-text">
				<xsl:with-param name="name" select="$name" />
			</xsl:apply-templates>
		</xsl:if>
	</xsl:template>

	<!-- (3) directly in w:body: Word moves it to the start of the next paragraph, after its
	     w:pPr, with that mark's run properties.  Where no paragraph follows (unmeasured), or
	     the next one is split by the nested-paragraph template above, it is a paragraph of
	     its own -->
	<xsl:template match="w:body/w:br">
		<xsl:variable name="next" select="following-sibling::*[not(self::w:br)][1]" />
		<xsl:if test="not($next[self::w:p][not(w:hyperlink/w:p or w:r/w:p or w:hyperlink/w:r/w:p or w:r/w:r/w:p)])">
			<w:p><w:r><w:br><xsl:apply-templates select="@*" /></w:br></w:r></w:p>
		</xsl:if>
	</xsl:template>

	<xsl:template match="w:body/w:p[preceding-sibling::*[1][self::w:br]]
			[not(w:hyperlink/w:p or w:r/w:p or w:hyperlink/w:r/w:p or w:r/w:r/w:p)]/w:pPr">
		<w:pPr><xsl:apply-templates select="@*|node()" /></w:pPr>
		<xsl:call-template name="body-breaks">
			<xsl:with-param name="p" select=".." />
		</xsl:call-template>
		<xsl:call-template name="runs-in-pPr" />
	</xsl:template>

	<xsl:template match="w:body/w:p[not(w:pPr)][preceding-sibling::*[1][self::w:br]]
			[not(w:hyperlink/w:p or w:r/w:p or w:hyperlink/w:r/w:p or w:r/w:r/w:p)]">
		<w:p>
			<xsl:apply-templates select="@*" />
			<xsl:call-template name="body-breaks">
				<xsl:with-param name="p" select="." />
			</xsl:call-template>
			<xsl:apply-templates select="node()" />
		</w:p>
	</xsl:template>

	<!-- the breaks directly before the paragraph, each in a run of its own -->
	<xsl:template name="body-breaks">
		<xsl:param name="p" />
		<xsl:for-each select="$p/preceding-sibling::w:br[generate-id(following-sibling::*[not(self::w:br)][1]) = generate-id($p)]">
			<w:r>
				<xsl:call-template name="mark-run-properties">
					<xsl:with-param name="p" select="$p" />
				</xsl:call-template>
				<w:br><xsl:apply-templates select="@*" /></w:br>
			</w:r>
		</xsl:for-each>
	</xsl:template>

	<!-- An element Word doesn't know, read as Word reads it (CR-076, from core-ts's Word check
	     76, rounds 1 to 8).  An element in the w: namespace whose name the schema doesn't have,
	     standing in a content host, gives way to its element children; its attributes and any
	     text directly in it go (76t41).  Elsewhere it stays, and the second unmarshal drops it
	     with all it holds, as Word drops it from a w:pPr or a w:rPr.

	     The host is the nearest ancestor that isn't such an element, so depth doesn't matter
	     (76t7, 76t35, 76t36).  mc: elements don't count (76t44, 76t45), and any other namespace
	     stops the walk (76k; a w:madeUp in an m:r isn't read through to the w:p beyond).  The
	     twenty hosts are those measured, by declaration and not by tag: w:ins, w:del, w:moveFrom,
	     w:moveTo, w:footnote and w:endnote are also declared for things no file measures, told
	     apart here by parent.

	     XSLT 1.0 allows no variable in a match pattern, so this matches every w: element no named
	     template takes.  Its other branch does what the identity template's usual-prefix branch
	     does.  The tests for w:pPr, w:p and w:sectPr are in templates of their own with plain
	     patterns below: in this one, which every w: element reaches, they took the stylesheet's
	     time up by 10% to 15% on the largest documents.  @since 17.3.6 -->

	<!-- xsd/wml/wml.xsd's element names; UnknownElementsTest holds this equal to the schema -->
	<xsl:variable name="wml-names" select="' abstractNum abstractNumId active activeRecord activeWritingStyle addressFieldName adjustLineHeightInTable adjustRightInd alias aliases alignBordersAndEdges alignTablesRowByRow allowPNG allowSpaceOfSameStyleInTable altChunk altChunkPr altName alwaysMergeEmptyNamespace alwaysShowPlaceholderText annotationRef applyBreakingRules attachedSchema attachedTemplate attr autoCaption autoCaptions autoFormatOverride autoHyphenation autoRedefine autoSpaceDE autoSpaceDN autoSpaceLikeWord95 autofitToFirstFixedWidthCell b bCs background balanceSingleByteDoubleByteWidth bar basedOn bdo bdr behavior behaviors between bibliography bidi bidiVisual blockQuote body bodyDiv bookFoldPrinting bookFoldPrintingSheets bookFoldRevPrinting bookmarkEnd bookmarkStart bordersDoNotSurroundFooter bordersDoNotSurroundHeader bottom br cachedColBalance calcOnExit calendar cantSplit caps caption captions category cellDel cellIns cellMerge characterSpacingControl charset checkBox checkErrors checked citation clickAndTypeStyle clrSchemeMapping cnfStyle col colDelim color cols column comboBox comment commentRangeEnd commentRangeStart commentReference comments compat compatSetting connectString consecutiveHyphenLimit contextualSpacing continuationSeparator control convMailMergeEsc cr cs customXml customXmlDelRangeEnd customXmlDelRangeStart customXmlInsRangeEnd customXmlInsRangeStart customXmlMoveFromRangeEnd customXmlMoveFromRangeStart customXmlMoveToRangeEnd customXmlMoveToRangeStart customXmlPr dataBinding dataSource dataType date dateFormat dayLong dayShort ddList decimalSymbol default defaultTabStop defaultTableStyle del delInstrText delText description destination dir dirty displayBackgroundShape displayHangulFixedWidth displayHorizontalDrawingGridEvery displayVerticalDrawingGridEvery div divBdr divId divs divsChild doNotAutoCompressPictures doNotAutofitConstrainedTables doNotBreakConstrainedForcedTable doNotBreakWrappedTables doNotDemarcateInvalidXml doNotDisplayPageBoundaries doNotEmbedSmartTags doNotExpandShiftReturn doNotHyphenateCaps doNotIncludeSubdocsInStats doNotLeaveBackslashAlone doNotOrganizeInFolder doNotRelyOnCSS doNotSaveAsSingleFile doNotShadeFormData doNotSnapToGridInCell doNotSuppressBlankLines doNotSuppressIndentation doNotSuppressParagraphBorders doNotTrackFormatting doNotTrackMoves doNotUseEastAsianBreakRules doNotUseHTMLParagraphAutoSpacing doNotUseIndentAsNumberingTabStop doNotUseLongFileNames doNotUseMarginsForDrawingGridOrigin doNotValidateAgainstSchema doNotVertAlignCellWithSp doNotVertAlignInTxbx doNotWrapTextWithPunct docDefaults docGrid docPart docPartBody docPartCategory docPartGallery docPartList docPartObj docPartPr docPartUnique docParts docVar docVars document documentProtection documentType drawing drawingGridHorizontalOrigin drawingGridHorizontalSpacing drawingGridVerticalOrigin drawingGridVerticalSpacing dropDownList dstrike dynamicAddress eastAsianLayout effect em embedBold embedBoldItalic embedItalic embedRegular embedSystemFonts embedTrueTypeFonts emboss enabled encoding end endnote endnotePr endnoteRef endnoteReference endnotes entryMacro equation evenAndOddHeaders exitMacro fHdr family ffData fieldMapData fitText flatBorders fldChar fldData fldSimple font fonts footerReference footnote footnoteLayoutLikeWW8 footnotePr footnoteRef footnoteReference footnotes forceUpgrade forgetLastTabAlignment formProt format formsDesign frame frameLayout framePr frameset framesetSplitbar ftr gallery glossaryDocument gridAfter gridBefore gridCol gridSpan group growAutofit guid gutterAtTop hMerge hdr hdrShapeDefaults headerReference headerSource helpText hidden hideGrammaticalErrors hideMark hideSpellingErrors highlight hps hpsBaseText hpsRaise hyperlink hyphenationZone i iCs id ignoreMixedContent ilvl imprint ind ins insideH insideV instrText isLgl jc keepLines keepNext kern kinsoku lang lastRenderedPageBreak latentStyles layoutRawTableWidth layoutTableRowsApart left legacy lid lineWrapLikeWord6 link linkStyles linkToQuery linkedToFile listEntry listItem listSeparator lnNumType lock locked lsdException lvl lvlJc lvlOverride lvlPicBulletId lvlRestart lvlText mailAsAttachment mailMerge mailSubject mainDocumentType mappedName marBottom marH marLeft marRight marTop marW matchSrc maxLength mirrorIndents mirrorMargins monthLong monthShort moveFrom moveFromRangeEnd moveFromRangeStart moveTo moveToRangeEnd moveToRangeStart movie multiLevelType mwSmallCaps name next noBorder noBreakHyphen noColumnBalance noEndnote noExtraLineSpacing noLeading noLineBreaksAfter noLineBreaksBefore noProof noPunctuationKerning noResizeAllowed noSpaceRaiseLower noTabHangInd noWrap notTrueType nsid num numFmt numId numIdMacAtCleanup numPicBullet numPr numRestart numStart numStyleLink numbering numberingChange oMath object odso optimizeForBrowser outline outlineLvl overflowPunct p pBdr pPr pPrChange pPrDefault pStyle pageBreakBefore panose1 paperSrc permEnd permStart personal personalCompose personalReply pgBorders pgMar pgNum pgNumType pgSz pict picture pitch pixelsPerInch placeholder pos position printBodyTextBeforeHeader printColBlack printFormsData printFractionalCharacterWidth printPostScriptOverText printTwoOnOne printerSettings proofErr proofState ptab qFormat query r rFonts rPr rPrChange rPrDefault rStyle readModeInkLockDown recipientData recipients relyOnVML removeDateAndTime removePersonalInformation result revisionView richText right rsid rsidRoot rsids rt rtl rtlGutter ruby rubyAlign rubyBase rubyPr saveFormsData saveInvalidXml savePreviewPicture saveSmartTagsAsXml saveSubsetFonts saveThroughXslt saveXmlDataOnly scrollbar sdt sdtContent sdtEndPr sdtPr sectPr sectPrChange selectFldWithFirstOrLastChar semiHidden separator settings shadow shapeDefaults shapeLayoutLikeWW8 shd showBreaksInFrames showEnvelope showXMLTags showingPlcHdr sig size sizeAuto smallCaps smartTag smartTagPr smartTagType snapToGrid softHyphen sourceFileName spaceForUL spacing spacingInWholePoints specVanish splitPgBreakAndParaMark src start startOverride statusText storeMappedDataAs strictFirstAndLastChars strike style styleLink styleLockQFSet styleLockTheme stylePaneFormatFilter stylePaneSortMethod styles subDoc subFontBySize suff summaryLength suppressAutoHyphens suppressBottomSpacing suppressLineNumbers suppressOverlap suppressSpBfAfterPgBrk suppressSpacingAtTopOfPage suppressTopSpacing suppressTopSpacingWP swapBordersFacingPages sym sz szCs t tab table tabs tag targetScreenSz tbl tblBorders tblCaption tblCellMar tblCellSpacing tblDescription tblGrid tblGridChange tblHeader tblInd tblLayout tblLook tblOverlap tblPr tblPrChange tblPrEx tblPrExChange tblStyle tblStyleColBandSize tblStylePr tblStyleRowBandSize tblW tblpPr tc tcBorders tcFitText tcMar tcPr tcPrChange tcW temporary text textAlignment textDirection textInput textboxTightWrap themeFontLang titlePg tl2br tmpl top topLinePunct tr tr2bl trHeight trPr trPrChange trackRevisions truncateFontHeightsLikeWP6 txbxContent type types u udl uiCompat97To2003 uiPriority ulTrailSpace underlineTabInNumList unhideWhenUsed uniqueTag updateFields useAltKinsokuLineBreakRules useAnsiKerningPairs useFELayout useNormalStyleForList usePrinterMetrics useSingleBorderforContiguousCells useWord2002TableStyleRules useWord97LineBreakRules useXSLTWhenSaving vAlign vMerge vanish vertAlign view viewMergedData w wAfter wBefore webHidden webSettings widowControl wordWrap wpJustification wpSpaceWidth wrapTrailSpaces writeProtection yearLong yearShort zoom '" />

	<!-- What such a root holds: a w:sectPr directly in it goes, a w:body's own content stands, and a further
	     unknown element is read through here, as w:* needs a known ancestor to read through one. -->
	<xsl:template name="root-content">
		<xsl:for-each select="*[not(self::w:sectPr)]">
			<xsl:choose>
				<xsl:when test="self::w:body">
					<xsl:apply-templates select="node()" />
				</xsl:when>
				<xsl:when test="self::w:* and not(contains($wml-names, concat(' ', local-name(), ' ')))">
					<xsl:call-template name="root-content" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="." />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="w:*">
		<xsl:choose>
			<xsl:when test="not(contains($wml-names, concat(' ', local-name(), ' ')))
				and ancestor::*[not(self::w:* or self::mc:*) or contains($wml-names, concat(' ', local-name(), ' '))][1]
					[self::w:p or self::w:body or self::w:tc or self::w:r or self::w:tbl or self::w:tr or self::w:sdtContent
					or self::w:hyperlink or self::w:hdr or self::w:ftr or self::w:comment or self::w:fldSimple
					or self::w:smartTag or self::w:customXml
					or self::w:ins[not(parent::w:numPr or parent::w:rPr or parent::w:trPr or parent::m:r or parent::m:ctrlPr)]
					or self::w:del[not(parent::w:rPr or parent::w:trPr or parent::m:r or parent::m:ctrlPr)]
					or self::w:moveFrom[not(parent::w:rPr)] or self::w:moveTo[not(parent::w:rPr)]
					or self::w:footnote[not(parent::w:footnotePr)] or self::w:endnote[not(parent::w:endnotePr)]]">
				<xsl:variable name="logging" select="java:org.docx4j.utils.XSLTUtils.noteReadThrough()" />
				<xsl:variable name="host"
					select="ancestor::*[not(self::w:* or self::mc:*) or contains($wml-names, concat(' ', local-name(), ' '))][1]" />
				<xsl:for-each select="*">
					<xsl:choose>
						<!-- 76t42: a break.  CR-073's rule 1 names its parents, so it wouldn't take this -->
						<xsl:when test="self::w:br and $host[self::w:p or self::w:hyperlink]">
							<w:r><w:br><xsl:apply-templates select="@*" /></w:br></w:r>
						</xsl:when>
						<!-- a shape Word refuses: the w:p template takes those it opens -->
						<xsl:when test="self::w:p and $host[self::w:p or self::w:hyperlink]">
							<xsl:apply-templates select="*[not(self::w:pPr)]" />
						</xsl:when>
						<xsl:otherwise>
							<xsl:apply-templates select="." />
						</xsl:otherwise>
					</xsl:choose>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<xsl:element namespace="{namespace-uri()}" name="{local-name(.)}">
					<xsl:apply-templates select="@*|node()" />
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- A w:r directly in an unknown element directly in a paragraph's w:pPr, in the w:pPr under
	     its w:pPrChange or in its w:sectPr, goes to the head of the paragraph, in order (76t27,
	     76t37, 76t39, 76t40, 76t50).  Anything else in the element stays with it and goes
	     (76t5, 76t38): CR-076's rule 1b.  A segment after the first of a split paragraph doesn't
	     repeat them. -->
	<xsl:template match="w:pPr">
		<xsl:param name="head" select="true()" />
		<xsl:element namespace="{namespace-uri()}" name="{local-name(.)}">
			<xsl:apply-templates select="@*|node()" />
		</xsl:element>
		<xsl:if test="$head">
			<xsl:call-template name="runs-in-pPr" />
		</xsl:if>
	</xsl:template>

	<xsl:template name="runs-in-pPr">
		<xsl:if test="parent::w:p and (*/w:r or w:pPrChange/w:pPr/*/w:r or w:sectPr/*/w:r)">
			<xsl:for-each select="(* | w:pPrChange/w:pPr/* | w:sectPr/*)[w:r][not(contains($wml-names, concat(' ', local-name(), ' ')))]">
				<xsl:variable name="logging" select="java:org.docx4j.utils.XSLTUtils.noteReadThrough()" />
				<xsl:apply-templates select="w:r" />
			</xsl:for-each>
		</xsl:if>
	</xsl:template>

	<!-- A w:p held by an unknown element in a w:p: a held w:p's mark ends a paragraph.  Where the
	     element is the host's last and holds nothing else at any depth (76t43, 76t56, 76t58, 76t68,
	     76t69, 76t70), the first held w:p's runs join the host, with its w:pPr if it has one and the
	     host's if not; a further one is a paragraph of its own; and a paragraph follows with the
	     host's w:pPr and nothing else, the host's own mark.  Word refuses the other shapes measured,
	     a run after the element or none before it (76t53, 76t54, 76t55, 76t57, 76t59), so for those
	     docx4j reads what it can: the catch-all's runs where they stand. -->
	<xsl:template match="w:p">
		<xsl:choose>
			<xsl:when test="*[last()][not(contains($wml-names, concat(' ', local-name(), ' ')))]/descendant::w:p">
				<xsl:variable name="holder" select="*[last()]" />
				<xsl:variable name="me" select="generate-id()" />
				<xsl:variable name="level" select="$holder/descendant::*[contains($wml-names, concat(' ', local-name(), ' '))]
						[generate-id(ancestor::*[contains($wml-names, concat(' ', local-name(), ' '))][1]) = $me]" />
				<xsl:choose>
					<xsl:when test="$level[self::w:p] and not($level[not(self::w:p)])">
						<xsl:variable name="logging" select="java:org.docx4j.utils.XSLTUtils.noteReadThrough()" />
						<xsl:variable name="first" select="$level[1]" />
						<xsl:element namespace="{namespace-uri()}" name="{local-name(.)}">
							<xsl:apply-templates select="@*" />
							<xsl:choose>
								<xsl:when test="$first/w:pPr"><xsl:apply-templates select="$first/w:pPr" /></xsl:when>
								<xsl:otherwise><xsl:apply-templates select="w:pPr" /></xsl:otherwise>
							</xsl:choose>
							<xsl:apply-templates select="node()[not(self::w:pPr)][count(. | $holder) != 1]" />
							<xsl:apply-templates select="$first/node()[not(self::w:pPr)]" />
						</xsl:element>
						<xsl:apply-templates select="$level[position() > 1]" />
						<xsl:call-template name="the-hosts-mark" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="paragraph" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="paragraph" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- 76t71: in a w:hyperlink last in the w:p, the held w:p's runs stay in the link, read by the
	     catch-all, and the host's mark follows.  Word's save has a complex field there, its end in
	     that next paragraph. -->
	<xsl:template name="paragraph">
		<xsl:element namespace="{namespace-uri()}" name="{local-name(.)}">
			<xsl:apply-templates select="@*|node()" />
		</xsl:element>
		<xsl:if test="*[last()][self::w:hyperlink]/*[last()][w:p][not(contains($wml-names, concat(' ', local-name(), ' ')))]">
			<xsl:call-template name="the-hosts-mark" />
		</xsl:if>
	</xsl:template>

	<xsl:template name="the-hosts-mark">
		<w:p>
			<xsl:apply-templates select="w:pPr">
				<xsl:with-param name="head" select="false()" />
			</xsl:apply-templates>
		</w:p>
	</xsl:template>

	<!-- 76t50: the w:sectPr of a paragraph goes, with its section break, where it holds a run in
	     an unknown element.  The body's own is not measured. -->
	<xsl:template match="w:sectPr">
		<xsl:if test="not(parent::w:pPr and */w:r and *[w:r][not(contains($wml-names, concat(' ', local-name(), ' ')))])">
			<xsl:element namespace="{namespace-uri()}" name="{local-name(.)}">
				<xsl:apply-templates select="@*|node()" />
			</xsl:element>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>
