<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dgm="http://schemas.openxmlformats.org/drawingml/2006/diagram"
                xmlns:g="http://graphml.graphdrawing.org/xmlns"  
                xmlns:y="http://www.yworks.com/xml/graphml"
>
  
  <!-- This stylesheet generates GraphML from the cxnLst, which can be
       visualised in the yEd graph editor. 
  
       Intended audience: docx4j developers working to extend docx4j's SmartArt support.
  -->
  
    
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

  <xsl:template match="/">

    <g:graphml  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">
      
      <g:key for="graphml" id="d0" yfiles.type="resources"/>
      <g:key for="port" id="d1" yfiles.type="portgraphics"/>
      <g:key for="port" id="d2" yfiles.type="portgeometry"/>
      <g:key for="port" id="d3" yfiles.type="portuserdata"/>
      <g:key attr.name="url" attr.type="string" for="node" id="d4"/>
      <g:key attr.name="description" attr.type="string" for="node" id="d5"/>
      <g:key for="node" id="d6" yfiles.type="nodegraphics"/>
      <g:key attr.name="url" attr.type="string" for="edge" id="d7"/>
      <g:key attr.name="description" attr.type="string" for="edge" id="d8"/>
      <g:key for="edge" id="d9" yfiles.type="edgegraphics"/>
      
      <g:graph id="G" edgedefault="directed">
        
        <xsl:apply-templates select="//dgm:cxnLst/dgm:cxn" mode="nodes"/>
        
        <xsl:apply-templates select="//dgm:cxnLst/dgm:cxn" mode="edges"/>
      
      </g:graph>
    </g:graphml>

  </xsl:template>  
  
<xsl:template match="dgm:cxn" mode="nodes" >
  <!-- doesn't seem to matter if there are duplicates, so we can be lazy -->
  <g:node id="{@srcId}">
      <g:data key="d5"/>
      <g:data key="d6">
        <y:ShapeNode>
          <y:Geometry height="30.0" width="30.0" x="155.625" y="0.0"/>
          <y:Fill color="#FFCC00" transparent="false"/>
          <y:BorderStyle color="#000000" type="line" width="1.0"/>
          <y:NodeLabel alignment="center" autoSizePolicy="content" borderDistance="0.0" fontFamily="Dialog" fontSize="13" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="19.92626953125" modelName="internal" modelPosition="c" textColor="#000000" visible="true" width="23.5" x="3.25" y="5.036865234375"><xsl:value-of select="@srcId"/></y:NodeLabel>
          <y:Shape type="rectangle"/>
        </y:ShapeNode>
      </g:data>
    </g:node>
  <g:node id="{@destId}">
      <g:data key="d5"/>
      <g:data key="d6">
        <y:ShapeNode>
          <y:Geometry height="30.0" width="30.0" x="155.625" y="0.0"/>
          <y:Fill color="#FFCC00" transparent="false"/>
          <y:BorderStyle color="#000000" type="line" width="1.0"/>
          <y:NodeLabel alignment="center" autoSizePolicy="content" borderDistance="0.0" fontFamily="Dialog" fontSize="13" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="19.92626953125" modelName="internal" modelPosition="c" textColor="#000000" visible="true" width="23.5" x="3.25" y="5.036865234375"><xsl:value-of select="@destId"/></y:NodeLabel>
          <y:Shape type="rectangle"/>
        </y:ShapeNode>
      </g:data>
    </g:node>
              
</xsl:template>

<xsl:template match="dgm:cxn" mode="edges" >
  <!-- doesn't seem to matter if there are duplicates, so we can be lazy -->
    <g:edge source="{@srcId}" target="{@destId}"/>
              
</xsl:template>
  
</xsl:stylesheet>
