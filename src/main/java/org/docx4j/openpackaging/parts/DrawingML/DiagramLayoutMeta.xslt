<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:gen="dummy-namespace-for-the-generated-xslt"                
                xmlns:dgm="http://schemas.openxmlformats.org/drawingml/2006/diagram" 
                xmlns:dh="http://opendope.org/SmartArt/DataHierarchy">

<!--  apply this xslt to a layout part, 
      in order to create an xslt which can be applied to an ordered list,
      in order to create a hierarchy of dgm:layoutNode,
      which, finally, can be transformed to a data part! -->
      
    <xsl:namespace-alias stylesheet-prefix="gen" result-prefix="xsl"/>
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

  <xsl:template match="/">
    <gen:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:msxsl="urn:schemas-microsoft-com:xslt" exclude-result-prefixes="msxsl"
    xmlns:dh="http://opendope.org/SmartArt/DataHierarchy">
    <xsl:for-each select="//dgm:forEach[@axis='ch']">
      <xsl:apply-templates select="." mode="create-named"/>
    </xsl:for-each>
    
    <gen:template match="/">
      <!-- Don't apply to the root of our list -->
      <gen:for-each select="/dh:SmartArtDataHierarchy/dh:list/dh:listItem/dh:list">
        <gen:comment> <gen:value-of select="name()" /></gen:comment>
	    <xsl:apply-templates select="/dgm:layoutDef/dgm:layoutNode"/>
      </gen:for-each>
    </gen:template>

<!-- 
	  <gen:template match="dh:list">
	  	<gen:apply-templates />
	  </gen:template>
	  
	  <gen:template match="dh:textBody" />	  
	  <gen:template match="dh:sibTransBody" />	  
	  <gen:template match="dh:image" />
 -->
 
    </gen:stylesheet>
  </xsl:template>


  <xsl:template match="dgm:forEach[@axis='ch']">
    <gen:call-template name="{@name}" />
</xsl:template>


  <xsl:template match="dgm:forEach[@ref]">
    <gen:call-template name="{@ref}" />
</xsl:template>

  <xsl:template match="dgm:forEach[@axis='self']">
  	<gen:if test="local-name()='listItem'">
    <!-- <gen:if test="local-name()='{@ptType}'"> -->
    <xsl:apply-templates/>
    </gen:if>
  </xsl:template>

  <xsl:template match="dgm:forEach[@axis='precedSib']">
    <gen:for-each select="preceding-sibling::*">
    <xsl:apply-templates/>
    </gen:for-each>
  </xsl:template>

  <!-- @concrete assists with mapping to the real list items
       in the next xslt. -->
  <xsl:template match="dgm:layoutNode" >
    <xsl:choose>
      <xsl:when test="starts-with(string(@name), 'hierRoot')">
        <xsl:copy>
          <xsl:attribute name="presAssocID">{@id}</xsl:attribute>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:when>
      <xsl:when test="starts-with(string(@name), 'rootText')">
        <xsl:copy>
          <xsl:attribute name="presAssocID">{@id}</xsl:attribute>
          <xsl:attribute name="presStyleCnt">{count(../dh:listItem)}</xsl:attribute>
          <xsl:attribute name="presStyleIdx">{position()-1}</xsl:attribute>
          <xsl:attribute name="depth">{count(ancestor::dh:listItem)}</xsl:attribute>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:when>
      <xsl:when test="starts-with(string(@name), 'rootPict')">
        <xsl:copy>
          <xsl:attribute name="presAssocID">{@id}</xsl:attribute>
          <xsl:attribute name="presStyleCnt">{count(//*)-1}</xsl:attribute><!--  FIXME when image rep is finalised -->
          <xsl:attribute name="presStyleIdx">{count(preceding::dh:listItem) + count(ancestor::dh:listItem)}</xsl:attribute>
          <xsl:attribute name="depth">{count(ancestor::dh:listItem)}</xsl:attribute> <!--  not needed -->
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:when>
      <xsl:when test="starts-with(string(@name), 'rootConnector')">
        <xsl:copy>
          <xsl:attribute name="presAssocID">{@id}</xsl:attribute>
          <xsl:attribute name="presStyleCnt">{count(../dh:listItem)}</xsl:attribute>
          <xsl:attribute name="presStyleIdx">{position()-1}</xsl:attribute>
          <xsl:attribute name="depth">{count(ancestor::dh:listItem)}</xsl:attribute>
          <!-- TODO special case for level 1 -->
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:when>
      <!--  Special case for "/" template where match is
            on odgm:list, rather than odgm:listItem;  
            odgm:list does not have @id.
            TODO Avoid hardcoded 0 somehow. -->
      <xsl:when test="starts-with(string(@name), 'hierChild') and count(@id)=0">
        <xsl:copy>
          <xsl:attribute name="presAssocID">0</xsl:attribute>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:when>
      <xsl:when test="starts-with(string(@name), 'hierChild')">
        <xsl:copy>
          <xsl:attribute name="presAssocID">{@id}</xsl:attribute>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:when>
      <xsl:otherwise> <!--  unexpected -->
      	<xsl:comment>Unexpected @name </xsl:comment>
        <xsl:copy>
          <xsl:attribute name="presAssocID">{@id}</xsl:attribute>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

<!-- 
  <xsl:template match="dgm:layoutNode" >
    <xsl:choose>
      <xsl:when test="starts-with(string(@name), 'rootText')">
        <xsl:copy>
          <xsl:attribute name="concrete">{@id}</xsl:attribute>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:when>
      <xsl:when test="starts-with(string(@name), 'rootConnector')">
        <xsl:copy>
          <xsl:attribute name="concrete">{@id}</xsl:attribute>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
 -->
 
  <xsl:template match="dgm:varLst"/>
  <xsl:template match="dgm:choose"/>
  <xsl:template match="dgm:shape"/>
  <xsl:template match="dgm:presOf"/>
  <xsl:template match="dgm:constrLst"/>
  <xsl:template match="dgm:ruleLst"/>
  <xsl:template match="dgm:alg"/>

  <xsl:template match="dgm:forEach[@axis='ch']"  mode="create-named">
    
      <xsl:choose>
        <xsl:when test="@ptType='asst'">
          <gen:template name="{@name}">
            <gen:for-each select="{@ptType}">
            <xsl:apply-templates/>
          </gen:for-each>
          </gen:template>
        </xsl:when>
        <xsl:when test="@ptType='nonAsst'">
          <gen:template name="{@name}">        
          <gen:for-each select="dh:list/dh:listItem">
            <!-- <gen:for-each select="*[local-name()!='asst']"> -->
              <xsl:apply-templates/>
            </gen:for-each>
          </gen:template>
        </xsl:when>
        <xsl:otherwise>
          <gen:template name="{@name}">
            <gen:for-each select="*">
            <xsl:apply-templates/>
          </gen:for-each>
          </gen:template>
        </xsl:otherwise>
      </xsl:choose>
      
  </xsl:template>


  <xsl:template match="dgm:forEach[@ref]"  mode="create-named" />


  <xsl:template match="dgm:forEach[@axis='self']"  mode="create-named">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="dgm:forEach[@axis='precedSib']"  mode="create-named">
    <xsl:apply-templates/>
  </xsl:template>


  <xsl:template match="dgm:varLst"  mode="create-named"/>
  <xsl:template match="dgm:choose"  mode="create-named"/>
  <xsl:template match="dgm:shape"  mode="create-named"/>
  <xsl:template match="dgm:presOf"  mode="create-named"/>
  <xsl:template match="dgm:constrLst"  mode="create-named"/>
  <xsl:template match="dgm:ruleLst"  mode="create-named"/>
  <xsl:template match="dgm:alg"  mode="create-named"/>

  
  <!--
    <gen:template match="dgm:layoutNode">

    <dgm:pt type="pres" modelId="n">
      <dgm:prSet presStyleCnt="n" presName="{@name}" pressAssocID="n">

      </dgm:prSet>

    </dgm:pt>

    <xsl:apply-templates/>

    </gen:template>

    <gen:template match="dgm:layoutNode">
      <xsl:apply-templates select="/dgm:layoutDef/dgm:layoutNode"/>
    </gen:template>
    -->

</xsl:stylesheet>
