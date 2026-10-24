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
    version="1.0"
        exclude-result-prefixes="java w a o v WX aml w10 pkg wp pic">	
        
        <!--  Note definition of xmlns:r is different 
              from the definition in an _rels file
              (where it is http://schemas.openxmlformats.org/package/2006/relationships)  -->

              
<!--  The purpose of this stylesheet is to convert a simple field:
 * 
      <w:p>
      <w:fldSimple w:instr=" DATE ">
        <w:r>
          <w:rPr>
            <w:noProof/>
          </w:rPr>
          <w:instrText>4/12/2011</w:instrText> ???? 
        </w:r>
      </w:fldSimple>
      </w:p>
 *     
 * into its equivalent the complex form:
 * 
      <w:r>
        <w:fldChar w:fldCharType="begin"/>
        <w:instrText xml:space="preserve">DATE </w:instrText>
        <w:fldChar w:fldCharType="separate"/>

        <w:instrText>4/12/2011</w:instrText>

        <w:fldChar w:fldCharType="end"/>
      </w:r>


 -->

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>
      
  <xsl:template match="w:fldSimple" >
  	<!--  begin, instruction and separate in one run, in the first result run's formatting -->
  	<w:r>
  	  <xsl:copy-of select="w:r[1]/w:rPr" />
      <w:fldChar w:fldCharType="begin"><xsl:copy-of select="@w:fldLock | @w:dirty"/></w:fldChar><!-- the lock and dirty flag kept, since 17.3.5 -->
      <w:instrText xml:space="preserve"><xsl:value-of select="@w:instr"></xsl:value-of> </w:instrText>
      <w:fldChar w:fldCharType="separate"/>
  	</w:r>
  	<!--  the result as it was, since 17.3.5: each run with its formatting and content, and what lies between them,
  	      bookmarks say (#78). Until then the result's text went into one run, in the first run's formatting, and
  	      anything else was dropped. -->
  	<xsl:apply-templates select="node()" mode="fldSimpleResult"/>
  	<w:r>
  	  <xsl:copy-of select="w:r[1]/w:rPr" />
      <w:fldChar w:fldCharType="end"/>
  	</w:r>
  </xsl:template>

  <xsl:template match="w:r" mode="fldSimpleResult">
    <w:r>
      <xsl:apply-templates select="@*|node()"/>
    </w:r>
  </xsl:template>

  <xsl:template match="node()" mode="fldSimpleResult">
    <xsl:apply-templates select="."/>
  </xsl:template>
  
  <xsl:template match="w:instrText[local-name(../..)='fldSimple']" >
		<w:t><xsl:value-of select="."/></w:t>
	</xsl:template>  
              
</xsl:stylesheet>              