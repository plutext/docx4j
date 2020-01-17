
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
    xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml"
    xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml" 
    xmlns:w15="http://schemas.microsoft.com/office/word/2012/wordml" 
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
	xmlns:dyn="http://exslt.org/dynamic"
	xmlns:xalan="http://xml.apache.org/xalan"
    version="1.0"
        exclude-result-prefixes="java xalan w a o v WX aml w10 pkg wp pic">	
        
<!--  

Example: Swagger/OpenAPI: color the table and add a different VML.

In this XSLT, you can apply arbitrary transformations to your OpenXML.

		w:sdtPr/w:tag/@w:val='od:finish=xyz'
		
	  by convention would be used to invoke a template named xyz.

	  This XSLT is applied after binding has been performed.

This is an example, for Swagger/OpenAPI.	  

 -->        
        

<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="yes" />

<xsl:param name="customXmlDataStorageParts"/> <!-- select="'passed in'"-->	
<xsl:param name="wmlPackage"/> <!-- select="'passed in'"-->	
<xsl:param name="sourcePart"/> <!-- select="'passed in'"-->	
<xsl:param name="xPathsMap"/> <!-- select="'passed in'"-->	

<!--  use this to pass in values which templates can be sensitive to -->
<xsl:param name="finisherParams"/> <!-- select="'passed in'"-->	

  <xsl:template match="/ | @*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="w:sdt">  
  <!-- 
  	<xsl:variable name="tag" select="string(w:sdtPr/w:tag/@w:val)"/>

	<xsl:variable name="parent" select="local-name(ancestor::*[self::w:body or self::w:hdr or self::w:ftr or self::w:p or self::w:r or self::w:tbl or self::w:tr or self::w:tc][1])" />
	<xsl:variable name="child"  select="local-name(descendant::*[self::w:p or self::w:r or self::w:t or self::w:tbl or self::w:tr or self::w:tc][1])" />
 -->
	<xsl:choose>

		<xsl:when
			test="contains(string(w:sdtPr/w:tag/@w:val), 'od:finish')">
			
			<!--  call extension function to get template name -->
			<xsl:variable name="tagVal" select="string(w:sdtPr/w:tag/@w:val)" />
			<xsl:variable name="templateName" select="java:org.docx4j.model.datastorage.XsltFinisher.getTemplateName($tagVal)" />			

			<!--  then invoke it  -->
			<xsl:comment><xsl:value-of select="$templateName"/></xsl:comment>
			<!--  the following doesn't work
				<xsl:call-template name="{$templateName}"/>
				but might in Saxon: https://www.oxygenxml.com/archives/xsl-list/200005/msg01114.html
				with saxon:allow-avt="yes"
				
				So instead:
					 -->
		
			<xsl:choose>
		
				<xsl:when test="$templateName = 'method'">
					<xsl:call-template name="OpenAPIMethod" />
				</xsl:when>
				<xsl:when test="$templateName = 'graphic'">
					<xsl:call-template name="OpenAPIGraphic" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:comment>Missing action for <xsl:value-of select="$templateName"/></xsl:comment>
				    <xsl:copy>
				      <xsl:apply-templates select="@*|node()"/>
				    </xsl:copy>
		
				</xsl:otherwise>
			</xsl:choose>
		</xsl:when>
		<xsl:otherwise>

		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()"/>
		    </xsl:copy>

		</xsl:otherwise>
	</xsl:choose>
  </xsl:template>

<!--  Example: Swagger/OpenAPI: color the table and add a different VML  -->  
<!-- 
  
  We could have an extension function to evaluateCondition.  
  
  The challenge is: how do we have an XPath context which knows where we are in the Answers hierarchy
  (ie repeats got expanded/indexed)?
  
  	Could define like an od:xpath?  And binding step expands repeats etc, and its available here as an arg. Call it an fcontext?  
  	
  Thats do-able. But simpler:  have a plain bind on the document surface, use that, then remove/replace it.

  In general, that's the recommended technique:
  
  1.  place your od:finish tag on an ancestor of the fragment you want to alter
  
  2.  somewhere inside it, have your bound sdt
  
  3.  optionally delete it as part of the transform.  In this example, we replace it  
  	
 -->
 
   <xsl:template name="OpenAPIMethod">  
 
	<!-- easiest to get the value of the XPath; though we could fetch the value from sdtContent -->
	<xsl:variable name="sdtPr" select=".//w:sdtPr[w:alias/@w:val = 'yaml_verb_DUMMY']" />

     <xsl:variable name="method"
     select="java:org.docx4j.model.datastorage.XsltFinisher.getXPathValue(
                 $xPathsMap,      
                 $wmlPackage,                               
                 $customXmlDataStorageParts,
                 $sdtPr )" />

	<xsl:choose>
		
		<xsl:when test="$method = 'get'">
			<!--  table is already the correct color -->
		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()" /> <!--  will switch back to this mode later when we hit graphic -->
		    </xsl:copy>			
		</xsl:when>
		<xsl:otherwise>
			<!--  change table color -->
		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()" mode="OpenAPI_Method"> <!--  use a different mode to do what we want -->
		      	<xsl:with-param name="method"><xsl:value-of select="$method"/></xsl:with-param>
		      	</xsl:apply-templates>
		    </xsl:copy>			
		</xsl:otherwise>
	
	</xsl:choose>
 
  </xsl:template>
  
  
  
	<xsl:template match="/ | @*|node()" mode="OpenAPI_Method">
		<xsl:param name="method">foo</xsl:param>
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"
				mode="OpenAPI_Method" >
		      	<xsl:with-param name="method"><xsl:value-of select="$method"/></xsl:with-param>
		      	</xsl:apply-templates>
		</xsl:copy>
	</xsl:template>
 
 	<!--  alter w:tblPr/w:shd -->
	<xsl:template match="w:tblPr" mode="OpenAPI_Method">
		<xsl:param name="method">foo</xsl:param>
	
		<w:tblPr>
			<xsl:copy-of select="w:tblW" />
			<xsl:copy-of select="w:tblBorders" />
			<xsl:copy-of select="w:tblLayout" />
			
			<xsl:choose>
				<xsl:when test="$method = 'post' ">
					<w:shd w:color="auto" w:fill="E2EFD9" w:themeFill="accent6" w:themeFillTint="33" w:val="clear"/>				
				</xsl:when>
				<xsl:when test="$method = 'delete' ">
					<w:shd w:color="auto" w:fill="FBD0CD" w:val="clear"/>				
				</xsl:when>
				<xsl:when test="$method = 'put' ">
					<w:shd w:color="auto" w:fill="FFF2CC" w:themeFill="accent4" w:themeFillTint="33" w:val="clear"/>				
				</xsl:when>
				<xsl:otherwise>
					<xsl:comment>Unexpected <xsl:value-of select="$method"/></xsl:comment>
				</xsl:otherwise>			
			</xsl:choose>			
			
			<xsl:copy-of select="w:tblCellMar" />
			<xsl:copy-of select="w:tblLook" />


			<xsl:apply-templates select="w:tblPr"
				mode="OpenAPI_Method" >
		      	<xsl:with-param name="method"><xsl:value-of select="$method"/></xsl:with-param>
		      	</xsl:apply-templates>
						
			<xsl:copy-of select="w:tblGrid" />
		</w:tblPr>
	
	</xsl:template>
 
 	<!--  alter w:tcPr/w:shd -->
	<xsl:template match="w:shd" mode="OpenAPI_Method">
		<xsl:param name="method">foo</xsl:param>

		<xsl:choose>
				<xsl:when test="$method = 'post' ">
					<w:shd w:color="auto" w:fill="E2EFD9" w:themeFill="accent6" w:themeFillTint="33" w:val="clear"/>				
				</xsl:when>
				<xsl:when test="$method = 'delete' ">
					<w:shd w:color="auto" w:fill="FBD0CD" w:val="clear"/>				
				</xsl:when>
				<xsl:when test="$method = 'put' ">
					<w:shd w:color="auto" w:fill="FFF2CC" w:themeFill="accent4" w:themeFillTint="33" w:val="clear"/>				
				</xsl:when>
				<xsl:otherwise>
					<xsl:comment>Unexpected <xsl:value-of select="$method"/></xsl:comment>
				</xsl:otherwise>			
		</xsl:choose>
	
	</xsl:template>
	
  <xsl:template match="w:sdt" mode="OpenAPI_Method">  
	<xsl:choose>

		<xsl:when
			test="contains(string(w:sdtPr/w:alias/@w:val), 'yaml_verb_DUMMY')">
			<xsl:call-template name="OpenAPIGraphic"/>			
		</xsl:when>
		<xsl:otherwise>

		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()"  mode="OpenAPI_Method"/> <!--  stay in this mode, since sdt contain tc which we need to shade -->
		    </xsl:copy>

		</xsl:otherwise>
	</xsl:choose>
  </xsl:template>
	
   <xsl:template name="OpenAPIGraphic">  
 
	<!-- easiest to get the value of the XPath; though we could fetch the value from sdtContent -->
	<xsl:variable name="sdtPr" select="./w:sdtPr" />

     <xsl:variable name="method"
     select="java:org.docx4j.model.datastorage.XsltFinisher.getXPathValue(
                 $xPathsMap,      
                 $wmlPackage,                               
                 $customXmlDataStorageParts,
                 $sdtPr )" />

	<xsl:choose>

		<xsl:when test="$method = 'get'">
			<!-- replace it with -->
			<w:r>
				<w:pict>
					<v:roundrect w14:anchorId="02AD168A"
						id="Rectangle: Rounded Corners 2" o:spid="_x0000_s1026"
						style="width:59.4pt;height:22.9pt;visibility:visible;mso-wrap-style:square;mso-left-percent:-10001;mso-top-percent:-10001;mso-position-horizontal:absolute;mso-position-horizontal-relative:char;mso-position-vertical:absolute;mso-position-vertical-relative:line;mso-left-percent:-10001;mso-top-percent:-10001;v-text-anchor:middle"
						arcsize="10923f"
						o:gfxdata="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&#xA;AADmBQAAAAA=&#xA;"
						fillcolor="#4472c4 [3204]" strokecolor="#1f3763 [1604]"
						strokeweight="1pt">
						<v:stroke joinstyle="miter" />
						<v:textbox>
							<w:txbxContent>
								<w:p w14:paraId="7F3B55D4" w14:textId="3D28538D">
									<w:pPr>
										<w:jc w:val="center" />
									</w:pPr>
									<w:r>
										<w:t>GET</w:t>
									</w:r>
								</w:p>
							</w:txbxContent>
						</v:textbox>
						<w10:anchorlock />
					</v:roundrect>
				</w:pict>
			</w:r>
		</xsl:when>
		<xsl:when test="$method = 'post'">
			<!-- replace it with -->
			<w:r>
				<w:pict>
					<v:roundrect w14:anchorId="02AD168A"
						id="Rectangle: Rounded Corners 2" o:spid="_x0000_s1026"
						style="width:59.4pt;height:22.9pt;visibility:visible;mso-wrap-style:square;mso-left-percent:-10001;mso-top-percent:-10001;mso-position-horizontal:absolute;mso-position-horizontal-relative:char;mso-position-vertical:absolute;mso-position-vertical-relative:line;mso-left-percent:-10001;mso-top-percent:-10001;v-text-anchor:middle"
						arcsize="10923f"
						o:gfxdata="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&#xA;AADmBQAAAAA=&#xA;"
						fillcolor="#4472c4 [3204]" strokecolor="#1f3763 [1604]"
						strokeweight="1pt">
						<v:stroke joinstyle="miter" />
						<v:textbox>
							<w:txbxContent>
								<w:p w14:paraId="7F3B55D4" w14:textId="3D28538D">
									<w:pPr>
										<w:jc w:val="center" />
									</w:pPr>
									<w:r>
										<w:t>POST</w:t>
									</w:r>
								</w:p>
							</w:txbxContent>
						</v:textbox>
						<w10:anchorlock />
					</v:roundrect>
				</w:pict>
			</w:r>
		</xsl:when>
		<xsl:otherwise>
			<!--  TODO -->
		</xsl:otherwise>
	
	</xsl:choose>
 
  </xsl:template>
	
  
  </xsl:stylesheet>