
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"

	xmlns:java="http://xml.apache.org/xalan/java"
	xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006"
	
	xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
 	xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
 		
	xmlns:wordml201011="http://schemas.microsoft.com/office/word/2010/11/wordml"
	xmlns:w15="http://schemas.microsoft.com/office/word/2012/wordml"

	xmlns:purlw="http://purl.oclc.org/ooxml/wordprocessingml/main"
 	xmlns:purla="http://purl.oclc.org/ooxml/drawingml/main"
	xmlns:purlwp="http://purl.oclc.org/ooxml/drawingml/wordprocessingDrawing" 	
	xmlns:purlpic="http://purl.oclc.org/ooxml/drawingml/picture"
	xmlns:purlr="http://purl.oclc.org/ooxml/officeDocument/relationships"
 
	xmlns:purlep="http://purl.oclc.org/ooxml/officeDocument/extendedProperties"
	xmlns:ep="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties"
	version="1.0" exclude-result-prefixes="java purlw purla purlwp purlpic purlr purlep">	
        
<!--  This preprocessor does 3 things:

		1. for mc:AlternateContent, it selects the mc:Fallback content
		
		2. it corrects common validity issues with docx from various sources (eg Google Docs)
		
		3. it can import Strict docx files
      
      See MainDocumentPart's unmarshall method 
      for an example of how it is invoked.
      
      You can override this with your own version.  Point at yours via
      docx4j property  docx4j.jaxb.JaxbValidationEventHandler
      
      NB, we do retain mc:AlternateContent in
      some places.  For example, 3.3.8 will retain
      it in w:r.
      
      -->


<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="yes" />

  <xsl:template match="/">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

	<xsl:template match="@*">
		<xsl:choose>

			<xsl:when
				test="namespace-uri() = 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'">
				<xsl:attribute name="{local-name(.)}"
					namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
					<xsl:value-of select="." />
				</xsl:attribute>
			</xsl:when>

			<xsl:when
				test="namespace-uri() = 'http://purl.oclc.org/ooxml/officeDocument/relationships'">
				<xsl:attribute name="{local-name(.)}"
					namespace="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
					<xsl:value-of select="." />
				</xsl:attribute>
			</xsl:when>

			<xsl:when
				test="namespace-uri() = 'http://purl.oclc.org/ooxml/wordprocessingml/main'">
				<xsl:attribute name="{local-name(.)}"
					namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main">

					<xsl:choose>
						<!-- TODO convert "175.8pt" 
						<xsl:when
							test="contains(., '.')">
							<xsl:value-of
								select="substring-before(., '.')" />
						</xsl:when>-->
						<!-- TODO convert "175pt" -->
						<xsl:when
							test="substring(., string-length(.) - 1) = 'pt'">
<!--							<xsl:value-of
								select="substring(., 1, string-length(.) - 2)" /> -->
								<xsl:value-of select="round(substring-before(., 'pt') * 20)"/>
						</xsl:when>

						<xsl:otherwise>
							<xsl:value-of select="." />
						</xsl:otherwise>
					</xsl:choose>

				</xsl:attribute>
			</xsl:when>

<!-- drawingml attributes ahave no prefix and are in no namespace at all 
			<xsl:when
				test="namespace-uri() = 'http://purl.oclc.org/ooxml/drawingml/main'">
				<xsl:attribute name="{local-name(.)}"
					namespace="http://schemas.openxmlformats.org/drawingml/2006/main">
					
					<xsl:value-of select="." />

				</xsl:attribute>
			</xsl:when>
-->

			<xsl:when
				test="namespace-uri() = ''">
				<xsl:attribute name="{local-name(.)}">
					
					<xsl:choose>
						<!-- TODO convert "%" to thousandths of a percent --> 
						<xsl:when
							test="substring(., string-length(.) ) = '%'">
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
				<xsl:copy>
					<xsl:apply-templates select="@*" />
				</xsl:copy>
			</xsl:otherwise>

		</xsl:choose>

	</xsl:template>

<!--
	<xsl:template match="purlw:*">
		<xsl:element name="{name(.)}"
			namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
			<xsl:apply-templates select="@*|node()" />
		</xsl:element>
	</xsl:template>
-->

	<xsl:template match="node()">
	
		<xsl:choose>
		
			<xsl:when test="self::*"><!-- elements -->
				
				<xsl:choose>
		
					<xsl:when
						test="namespace-uri() = 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'">
						<xsl:element name="{local-name(.)}"
							namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
						      <xsl:apply-templates select="@*|node()"/>
						</xsl:element>
					</xsl:when>
		
					<xsl:when
						test="namespace-uri() = 'http://purl.oclc.org/ooxml/wordprocessingml/main'">
						<xsl:element name="{local-name(.)}"
							namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
						      <xsl:apply-templates select="@*|node()"/>
						</xsl:element>
					</xsl:when>
		
					<xsl:when
						test="namespace-uri() = 'http://purl.oclc.org/ooxml/drawingml/main'">
						<xsl:element name="{local-name(.)}"
							namespace="http://schemas.openxmlformats.org/drawingml/2006/main">
						      <xsl:apply-templates select="@*|node()"/>
						</xsl:element>
					</xsl:when>
		
					<xsl:when
						test="namespace-uri() = 'http://purl.oclc.org/ooxml/drawingml/wordprocessingDrawing'">
						<xsl:element name="{local-name(.)}"
							namespace="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing">
						      <xsl:apply-templates select="@*|node()"/>
						</xsl:element>
					</xsl:when>
		
					<xsl:when
						test="namespace-uri() = 'http://purl.oclc.org/ooxml/drawingml/picture'">
						<xsl:element name="{local-name(.)}"
							namespace="http://schemas.openxmlformats.org/drawingml/2006/picture">
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


	<xsl:template match="purlep:Properties">
		<ep:Properties>
			<xsl:apply-templates select="@*|node()" />
		</ep:Properties>
	</xsl:template>


	<xsl:template match="purlw:document">
		<w:document
			xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
			xmlns:aink="http://schemas.microsoft.com/office/drawing/2016/ink"
			xmlns:am3d="http://schemas.microsoft.com/office/drawing/2017/model3d"
			xmlns:cx="http://schemas.microsoft.com/office/drawing/2014/chartex"
			xmlns:cx1="http://schemas.microsoft.com/office/drawing/2015/9/8/chartex"
			xmlns:cx2="http://schemas.microsoft.com/office/drawing/2015/10/21/chartex"
			xmlns:cx3="http://schemas.microsoft.com/office/drawing/2016/5/9/chartex"
			xmlns:cx4="http://schemas.microsoft.com/office/drawing/2016/5/10/chartex"
			xmlns:cx5="http://schemas.microsoft.com/office/drawing/2016/5/11/chartex"
			xmlns:cx6="http://schemas.microsoft.com/office/drawing/2016/5/12/chartex"
			xmlns:cx7="http://schemas.microsoft.com/office/drawing/2016/5/13/chartex"
			xmlns:cx8="http://schemas.microsoft.com/office/drawing/2016/5/14/chartex"
			xmlns:o="urn:schemas-microsoft-com:office:office"
			xmlns:oel="http://schemas.microsoft.com/office/2019/extlst"
			xmlns:v="urn:schemas-microsoft-com:vml"
			xmlns:w10="urn:schemas-microsoft-com:office:word"
			xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml"
			xmlns:w15="http://schemas.microsoft.com/office/word/2012/wordml"
			xmlns:w16="http://schemas.microsoft.com/office/word/2018/wordml"
			xmlns:w16cex="http://schemas.microsoft.com/office/word/2018/wordml/cex"
			xmlns:w16cid="http://schemas.microsoft.com/office/word/2016/wordml/cid"
			xmlns:w16du="http://schemas.microsoft.com/office/word/2023/wordml/word16du"
			xmlns:w16sdtdh="http://schemas.microsoft.com/office/word/2020/wordml/sdtdatahash"
			xmlns:w16sdtfl="http://schemas.microsoft.com/office/word/2024/wordml/sdtformatlock"
			xmlns:w16se="http://schemas.microsoft.com/office/word/2015/wordml/symex"
			xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml"
			xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
			xmlns:wp14="http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing"
			xmlns:wpi="http://schemas.microsoft.com/office/word/2010/wordprocessingInk"		
			xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"				
			>
			<!-- TODO: fix xmlns:m="http://purl.oclc.org/ooxml/officeDocument/math"
						
 -->
			<xsl:apply-templates select="@*|node()" />
		</w:document>
	</xsl:template>

	<!-- this is only here to replace some of the purl namespaces-->
	<xsl:template match="w15:people">
		<w15:people
			xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
			xmlns:aink="http://schemas.microsoft.com/office/drawing/2016/ink"
			xmlns:am3d="http://schemas.microsoft.com/office/drawing/2017/model3d"
			xmlns:cx="http://schemas.microsoft.com/office/drawing/2014/chartex"
			xmlns:cx1="http://schemas.microsoft.com/office/drawing/2015/9/8/chartex"
			xmlns:cx2="http://schemas.microsoft.com/office/drawing/2015/10/21/chartex"
			xmlns:cx3="http://schemas.microsoft.com/office/drawing/2016/5/9/chartex"
			xmlns:cx4="http://schemas.microsoft.com/office/drawing/2016/5/10/chartex"
			xmlns:cx5="http://schemas.microsoft.com/office/drawing/2016/5/11/chartex"
			xmlns:cx6="http://schemas.microsoft.com/office/drawing/2016/5/12/chartex"
			xmlns:cx7="http://schemas.microsoft.com/office/drawing/2016/5/13/chartex"
			xmlns:cx8="http://schemas.microsoft.com/office/drawing/2016/5/14/chartex"
			xmlns:o="urn:schemas-microsoft-com:office:office"
			xmlns:oel="http://schemas.microsoft.com/office/2019/extlst"
			xmlns:v="urn:schemas-microsoft-com:vml"
			xmlns:w10="urn:schemas-microsoft-com:office:word"
			xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml"
			xmlns:w15="http://schemas.microsoft.com/office/word/2012/wordml"
			xmlns:w16="http://schemas.microsoft.com/office/word/2018/wordml"
			xmlns:w16cex="http://schemas.microsoft.com/office/word/2018/wordml/cex"
			xmlns:w16cid="http://schemas.microsoft.com/office/word/2016/wordml/cid"
			xmlns:w16du="http://schemas.microsoft.com/office/word/2023/wordml/word16du"
			xmlns:w16sdtdh="http://schemas.microsoft.com/office/word/2020/wordml/sdtdatahash"
			xmlns:w16sdtfl="http://schemas.microsoft.com/office/word/2024/wordml/sdtformatlock"
			xmlns:w16se="http://schemas.microsoft.com/office/word/2015/wordml/symex"
			xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml"
			xmlns:wp14="http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing"
			xmlns:wpi="http://schemas.microsoft.com/office/word/2010/wordprocessingInk"			
			>
			<!-- TODO: fix xmlns:m="http://purl.oclc.org/ooxml/officeDocument/math"
						xmlns:r="http://purl.oclc.org/ooxml/officeDocument/relationships"
			xmlns:wp="http://purl.oclc.org/ooxml/drawingml/wordprocessingDrawing"
 -->
			<xsl:apply-templates select="@*|node()" />
		</w15:people>
	</xsl:template>

	<xsl:template match="purla:theme">
		<a:theme>
			<xsl:apply-templates select="@*|node()" />
		</a:theme>
	</xsl:template>

	<xsl:template match="purlw:document/@purlw:conformance" />
	
	<!-- <w:zoom w:percent="228%"/>-->
	<xsl:template match="purlw:zoom"/>
  
  <!--
	<a:graphicData uri="http://purl.oclc.org/ooxml/drawingml/picture">
	should be uri="http://schemas.openxmlformats.org/drawingml/2006/picture"
	
	a:blip r:embed="rId8"
	
  -->
  <xsl:template match="@uri">
	<xsl:attribute name="uri">http://schemas.openxmlformats.org/drawingml/2006/picture</xsl:attribute>
  </xsl:template>
  
  <!-- end of purl (strict) importing --> 
  
  <xsl:template match="mc:AlternateContent">  
  
	<xsl:variable name="dummy" 
		select="java:org.docx4j.utils.XSLTUtils.logWarn('Found some mc:AlternateContent')" />
		
  	<xsl:choose>
  	
	    <xsl:when test="parent::w:r">
				<!--  v3.3.8 is OK with mc:AlternateContent in a run  -->
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

			<!--  wps:txbx/w:txbxContent .. this works
  		      So TODO make choosing this configurable via docx4j.properties 
  		
  		<xsl:when test="mc:Choice[@Requires='wps']">
  		
  			<xsl:variable name="message" 
  				select="string('Selecting mc:Choice[@Requires=wps]')" />  			
			<xsl:variable name="logging" 
				select="java:org.docx4j.utils.XSLTUtils.logWarn($message)" />
				
  			<xsl:copy-of select="mc:Choice[@Requires='wps']/*"/>

  		</xsl:when>
  		 -->
  		   
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
   
</xsl:stylesheet>
