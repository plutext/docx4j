
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
        
<!--  Example: shading a row in the invoice example .

	  See ContentControlBindingExtensions for an example of how to use this. 

	  In this XSLT, you can apply arbitrary transformations to your OpenXML.

		w:sdtPr/w:tag/@w:val='od:finish=xyz'
		
	  by convention would be used to invoke a template named xyz.

	  This XSLT is applied after binding has been performed.
	  
	  A typical use is to apply formatting depending on the bound values.
	  
	  This is an example, for use with the invoice template in sample-docs/word/databinding.

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
			
		<xsl:variable name="dummy"
	select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.log(string(w:sdtPr/w:tag/@w:val))" />
			
			
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
		
				<xsl:when test="$templateName = 't_r'">
					<xsl:call-template name="invoiceShade" />
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

<!--  Example: shading a row in the invoice example  -->  
  <xsl:template name="invoiceShade">  

	
	<!-- get the XPath of the 2nd cell; but most flexible to just pass sdtPr -->
	<xsl:variable name="sdtPr" select=".//w:sdtPr[w:alias/@w:val = 'Price']" />

     <xsl:variable name="res"
     select="java:org.docx4j.model.datastorage.XsltFinisher.getXPathValue(
                 $xPathsMap,      
                 $wmlPackage,                               
                 $customXmlDataStorageParts,
                 $sdtPr )" />

	<xsl:comment><xsl:value-of select="$res"/></xsl:comment>
	
	<xsl:choose>
		
		<xsl:when test="$res = '$30'">
		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()" mode="invoice_Action"/> <!--  use a different mode to do what we want -->
		    </xsl:copy>			
		</xsl:when>
		<xsl:otherwise>
		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()"/>
		    </xsl:copy>

		</xsl:otherwise>
	
	
	</xsl:choose>

  </xsl:template>


	<xsl:template match="/ | @*|node()" mode="invoice_Action">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"
				mode="invoice_Action" />
		</xsl:copy>
	</xsl:template>
  
  <!--  in this example, color the row   -->
    <xsl:template match="w:tcPr" mode="invoice_Action">
    
    	<xsl:variable name="fillColor"
     		select="java:org.docx4j.model.datastorage.XsltFinisher.getParam($finisherParams, 't_r', 'fillColor')"/>
    
    	<w:tcPr>
    		<w:shd w:color="auto" w:fill="{$fillColor}" w:val="clear"/>
	      	<xsl:apply-templates select="@*|node()" mode="invoice_Action" />
	    </w:tcPr>
  </xsl:template>
  
  
  </xsl:stylesheet>