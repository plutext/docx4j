
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
    xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
    xmlns:o="urn:schemas-microsoft-com:office:office"
    xmlns:v="urn:schemas-microsoft-com:vml"
    xmlns:WX="http://schemas.microsoft.com/office/word/2003/auxHint"
    xmlns:aml="http://schemas.microsoft.com/aml/2001/core"
    xmlns:w10="urn:schemas-microsoft-com:office:word"
    xmlns:w15="http://schemas.microsoft.com/office/word/2012/wordml" 
	xmlns:pkg="http://schemas.microsoft.com/office/2006/xmlPackage"		    
	xmlns:java="http://xml.apache.org/xalan/java" 
	xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
	xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
	xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture"
	xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006"
    version="1.0"
        exclude-result-prefixes="java w a o v WX aml w10 pkg wp pic mc">	
        

<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="yes" />

<xsl:param name="OpenDoPEIntegrity"/> <!-- select="'passed in'"-->	

  <xsl:template match="/ | @*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="w:commentRangeStart">  
  	<xsl:choose>
  		<xsl:when test="java:org.docx4j.model.datastorage.OpenDoPEIntegrity.encountered(
										$OpenDoPEIntegrity,'commentRangeStart',
										string(@w:id) )" />
			<!--  Drop it -->										
  		<xsl:otherwise>   		
		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()"/>
		    </xsl:copy>  		
  		</xsl:otherwise>  		
  	</xsl:choose>    
  </xsl:template>

  <xsl:template match="w:commentRangeEnd">  
  	<xsl:choose>
  		<xsl:when test="java:org.docx4j.model.datastorage.OpenDoPEIntegrity.encountered(
										$OpenDoPEIntegrity,'commentRangeEnd',
										string(@w:id) )" />
			<!--  Drop it -->										
  		<xsl:otherwise>   		
		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()"/>
		    </xsl:copy>  		
  		</xsl:otherwise>  		
  	</xsl:choose>    
  </xsl:template>

  <xsl:template match="w:commentReference">  
  	<xsl:choose>
  		<xsl:when test="java:org.docx4j.model.datastorage.OpenDoPEIntegrity.encountered(
										$OpenDoPEIntegrity,'commentReference',
										string(@w:id) )" />
			<!--  Drop it -->										
  		<xsl:otherwise>   		
		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()"/>
		    </xsl:copy>  		
  		</xsl:otherwise>  		
  	</xsl:choose>    
  </xsl:template>

  <xsl:template match="w:footnoteReference">  
  	<xsl:choose>
  		<xsl:when test="java:org.docx4j.model.datastorage.OpenDoPEIntegrity.encountered(
										$OpenDoPEIntegrity,'footnoteReference',
										string(@w:id) )" />
			<!--  Drop it -->										
  		<xsl:otherwise>   		
		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()"/>
		    </xsl:copy>  		
  		</xsl:otherwise>  		
  	</xsl:choose>    
  </xsl:template>

  <xsl:template match="w:endnoteReference">  
  	<xsl:choose>
  		<xsl:when test="java:org.docx4j.model.datastorage.OpenDoPEIntegrity.encountered(
										$OpenDoPEIntegrity,'endnoteReference',
										string(@w:id) )" />
			<!--  Drop it -->										
  		<xsl:otherwise>   		
		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()"/>
		    </xsl:copy>  		
  		</xsl:otherwise>  		
  	</xsl:choose>    
  </xsl:template>
  
    <!--  remove w15 repeats, so w:sdt structure encountered in bind.xslt is as expected.
          Not a repeating section OpenDoPEHandler marked w15:resultRepeatZero (its node set was
          empty): that one stays whole, its template item inside it, so that bind.xslt leaves it
          as Word shows a repeating section with no items (the identity template copies both). -->
    <xsl:template match="w:sdt[w:sdtPr/w15:repeatingSection][not(contains(string(w:sdtPr/w:tag/@w:val), 'w15:resultRepeatZero'))]" priority="5">
        <!--  keep contents only -->
        <xsl:apply-templates select="w:sdtContent/*"/>
    </xsl:template>

    <xsl:template match="w:sdt[w:sdtPr/w15:repeatingSectionItem][not(ancestor::w:sdt[contains(string(w:sdtPr/w:tag/@w:val), 'w15:resultRepeatZero')])]" priority="5">
        <!--  keep contents only -->
        <xsl:apply-templates select="w:sdtContent/*"/>
    </xsl:template>
  
  <!-- Integrity: w:tr/w:sdt must contain a w:tc (a row may not have zero cells)
  
       this matches an sdt surrounding a tc (with or without nested tables); the tc's own
       content is the w:tc template's business
  
   -->
  <xsl:template match="w:sdt[count(ancestor::w:tr)=count(ancestor::w:tbl) and count(ancestor::w:tc)=(count(ancestor::w:tbl)-1) ]">
  
  <!--  apply fix if necessary to deepest first -->
  	<xsl:variable name="results">
		<xsl:apply-templates select="@*|node()"/>  	
  	</xsl:variable>
  
  	<xsl:choose>
  	
  		<xsl:when test="count(xalan:nodeset($results)//w:tc)=0"> <!--  still need to do something, in spite of fixing the deepest already -->
  			<w:sdt>
  				<xsl:copy-of select="w:sdtPr"/>
  				<w:sdtContent>
  					<w:tc> 
						<xsl:copy-of select="./descendant::w:tcPr[1]" />					  					
  						<w:p/>
  					</w:tc>
  				</w:sdtContent>
  			</w:sdt>
  		</xsl:when>
  		<xsl:otherwise>
		    <xsl:copy>
		      <xsl:copy-of select="$results"/>
		    </xsl:copy>  		
  		</xsl:otherwise>
  	</xsl:choose>
  
  </xsl:template>  

  <!-- Integrity: a w:tc holds at least one block, and its last block, as Word reads it, is a w:p
       (docs/developer/word-table-rules.md, rules 1 and 2; measured 2026-10-22: Word looks through
       block-level controls and reads one branch of an mc:AlternateContent). Where it does not, an
       empty w:p is appended to the cell, after any wrapper, which is Word's own repair. A cell ending
       in a wrapped paragraph gets nothing. -->
  <xsl:template match="w:tc">
  	<xsl:variable name="results">
		<xsl:apply-templates select="@*|node()"/>  	
  	</xsl:variable>
  	<xsl:variable name="endsWithP">
  		<xsl:call-template name="last-block-is-p"><xsl:with-param name="parent" select="xalan:nodeset($results)"/></xsl:call-template>
  	</xsl:variable>
    <xsl:copy>
      <xsl:copy-of select="$results"/>
      <xsl:if test="$endsWithP != 'yes'"><w:p/></xsl:if>
    </xsl:copy>  		
  </xsl:template>  

  <!-- Word's cell rule (docs/developer/word-table-rules.md, rule 2): a cell's last block, as Word
       reads it, is a w:p. 'yes' when the last block child of $parent is a w:p, looked through w:sdt
       and w:customXml; an mc:AlternateContent counts only when every branch does (Word checks the
       branch it reads; a spare paragraph after one it does not read is harmless). 'no' with no block. -->
  <xsl:template name="last-block-is-p">
    <xsl:param name="parent"/>
    <xsl:variable name="blocks" select="$parent/w:p|$parent/w:tbl|$parent/w:sdt|$parent/w:customXml|$parent/mc:AlternateContent"/>
    <xsl:variable name="last" select="$blocks[last()]"/>
    <xsl:choose>
      <xsl:when test="not($last)">no</xsl:when>
      <xsl:when test="$last/self::w:p">yes</xsl:when>
      <xsl:when test="$last/self::w:sdt">
        <xsl:call-template name="last-block-is-p"><xsl:with-param name="parent" select="$last/w:sdtContent"/></xsl:call-template>
      </xsl:when>
      <xsl:when test="$last/self::w:customXml">
        <xsl:call-template name="last-block-is-p"><xsl:with-param name="parent" select="$last"/></xsl:call-template>
      </xsl:when>
      <xsl:when test="$last/self::mc:AlternateContent">
        <xsl:variable name="failing">
          <xsl:for-each select="$last/mc:Choice|$last/mc:Fallback">
            <xsl:variable name="branch"><xsl:call-template name="last-block-is-p"><xsl:with-param name="parent" select="."/></xsl:call-template></xsl:variable>
            <xsl:if test="$branch != 'yes'">x</xsl:if>
          </xsl:for-each>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="string-length($failing) = 0">yes</xsl:when>
          <xsl:otherwise>no</xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>no</xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  
  
  <!-- Integrity: w:tc/w:sdt must be non-empty  
  
       this matches a block level sdt inside a tc (whether or not the table is nested)
       
       Only an sdt left with no content at all is populated; what the cell ends with is the w:tc
       template's business (until 17.3.4 this padded every sdt in a cell with no w:p inside it,
       a nested table in a control among them, which Word does not ask for).
  
   -->
  <xsl:template match="w:sdt[count(ancestor::w:tc)=count(ancestor::w:tbl) and count(ancestor::w:p)=0 ]">

  <!--  apply fix if necessary to deepest first -->
  	<xsl:variable name="results">
		<xsl:apply-templates select="@*|node()"/>  	
  	</xsl:variable>

    <xsl:choose>
  	
  		<xsl:when test="count(xalan:nodeset($results)/w:sdtContent/*)=0"> <!--  still need to do something, in spite of fixing the deepest already -->
  			<w:sdt>
  				<xsl:copy-of select="w:sdtPr"/>
  				<w:sdtContent>
  						<w:p/>
  				</w:sdtContent>
  			</w:sdt>
  		</xsl:when>
  		<xsl:otherwise>
		    <xsl:copy>
		      <xsl:copy-of select="$results"/>
		    </xsl:copy>  		
  		</xsl:otherwise>
  	</xsl:choose>
  
  </xsl:template>  
   
</xsl:stylesheet>
