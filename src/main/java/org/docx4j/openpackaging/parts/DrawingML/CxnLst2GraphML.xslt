<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dgm="http://schemas.openxmlformats.org/drawingml/2006/diagram"
                xmlns:g="http://graphml.graphdrawing.org/xmlns"  
                xmlns:y="http://www.yworks.com/xml/graphml"
>

<!-- 

/*
 *  Copyright 2011, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */


 -->  
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
        
        
<!--        <xsl:apply-templates select="//dgm:cxnLst/dgm:cxn" mode="nodes"/> -->
  
        <xsl:apply-templates select="//dgm:ptLst/dgm:pt" mode="nodes"/>
        
        <xsl:apply-templates select="//dgm:cxnLst/dgm:cxn" mode="edges"/>
      
      </g:graph>
    </g:graphml>

  </xsl:template>  

<xsl:template match="dgm:pt" mode="nodes" >
  <!-- doesn't seem to matter if there are duplicates, so we can be lazy -->
  <g:node id="{@modelId}">
      <g:data key="d5"/>
      <g:data key="d6">
        <y:ShapeNode>
          <y:Geometry height="30.0" width="30.0" x="155.625" y="0.0"/>
          <xsl:choose>
              <xsl:when test="@type='pres' and (starts-with(dgm:prSet/@presStyleLbl,'parChTrans'))">
                <y:BorderStyle color="#000000" type="line" width="1.0"/>
                <y:Fill color="#FF99CC" transparent="false"/>
                <y:NodeLabel alignment="center" autoSizePolicy="content" borderDistance="0.0" fontFamily="Dialog" fontSize="13" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="19.92626953125" modelName="internal" modelPosition="c" textColor="#000000" visible="true" width="23.5" x="3.25" y="5.036865234375"><xsl:value-of select="@modelId"/> <xsl:value-of select="dgm:prSet/@presStyleLbl"/> ] <xsl:value-of select="dgm:prSet/@presAssocID"/></y:NodeLabel>
                <y:Shape type="rectangle"/>
              </xsl:when>
              <xsl:when test="@type='parTrans' or @type='sibTrans'">
                <y:BorderStyle color="#000000" type="line" width="1.0"/>
                <y:Fill color="#FF99CC" transparent="false"/>
                <y:NodeLabel alignment="center" autoSizePolicy="content" borderDistance="0.0" fontFamily="Dialog" fontSize="13" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="19.92626953125" modelName="internal" modelPosition="c" textColor="#000000" visible="true" width="23.5" x="3.25" y="5.036865234375"><xsl:value-of select="@modelId"/> <xsl:value-of select="@type"/> ] <xsl:value-of select="@cxnId"/></y:NodeLabel>
                <y:Shape type="rectangle"/>
              </xsl:when>
              <xsl:when test="@type='pres'">
                <y:BorderStyle hasColor="false" type="line" width="1.0"/>
                <y:Fill color="#FF0000" transparent="true"/>
                <y:NodeLabel alignment="center" autoSizePolicy="content" borderDistance="0.0" fontFamily="Dialog" fontSize="13" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="19.92626953125" modelName="internal" modelPosition="c" textColor="#000000" visible="true" width="23.5" x="3.25" y="5.036865234375"><xsl:value-of select="@modelId"/> <xsl:value-of select="dgm:prSet/@presName"/> ] <xsl:value-of select="dgm:prSet/@presAssocID"/></y:NodeLabel>
                <y:Shape type="rectangle"/>
              </xsl:when>
              <xsl:when test="@type">
                <y:BorderStyle color="#000000" type="line" width="1.0"/>
                <y:Fill color="#FFCC00" transparent="false"/>
                <y:NodeLabel alignment="center" autoSizePolicy="content" borderDistance="0.0" fontFamily="Dialog" fontSize="13" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="19.92626953125" modelName="internal" modelPosition="c" textColor="#000000" visible="true" width="23.5" x="3.25" y="5.036865234375"><xsl:value-of select="@modelId"/> <xsl:value-of select="@type"/></y:NodeLabel>
                <y:Shape type="rectangle"/>
              </xsl:when>
              <xsl:otherwise>
                <y:BorderStyle color="#000000" type="line" width="1.0"/>
                <y:Fill color="#FFCC00" transparent="false"/>
                <y:NodeLabel alignment="center" autoSizePolicy="content" borderDistance="0.0" fontFamily="Dialog" fontSize="13" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="19.92626953125" modelName="internal" modelPosition="c" textColor="#000000" visible="true" width="23.5" x="3.25" y="5.036865234375"><xsl:value-of select="@modelId"/></y:NodeLabel>
                <y:Shape type="rectangle"/>
              </xsl:otherwise>
            </xsl:choose>
        </y:ShapeNode>
      </g:data>
    </g:node>
              
</xsl:template>

<xsl:template match="dgm:cxn[@parTransId]" mode="edges" >
    <g:edge source="{@srcId}" target="{@parTransId}">
      <g:data key="d9">
        <y:PolyLineEdge>
          <y:Path sx="-7.5" sy="15.0" tx="0.0" ty="-15.0">
            <y:Point x="147.49761904761905" y="45.0"/>
            <y:Point x="54.68690476190476" y="45.0"/>
          </y:Path>
          <y:LineStyle color="#000000" type="dashed" width="1.0"/>
          <y:Arrows source="none" target="standard"/>
          <xsl:if test="@type='presOf'">
            <y:EdgeLabel alignment="center" distance="2.0" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="18.701171875" modelName="six_pos" modelPosition="tail" preferredPlacement="anywhere" ratio="0.5" textColor="#000000" visible="true" width="39.34375" x="-90.81071486700148" y="63.5869140625">presOf</y:EdgeLabel>
          </xsl:if>
          <xsl:if test="@sibTransId">
            <y:EdgeLabel alignment="center" distance="2.0" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="18.701171875" modelName="six_pos" modelPosition="tail" preferredPlacement="anywhere" ratio="0.5" textColor="#000000" visible="true" width="39.34375" x="-90.81071486700148" y="63.5869140625">trans <xsl:value-of select="@modelId"/></y:EdgeLabel>
          </xsl:if>
          <y:BendStyle smoothed="false"/>
        </y:PolyLineEdge>
      </g:data>
    </g:edge>
    <g:edge source="{@parTransId}" target="{@destId}">
      <g:data key="d9">
        <y:PolyLineEdge>
          <y:Path sx="-7.5" sy="15.0" tx="0.0" ty="-15.0">
            <y:Point x="147.49761904761905" y="45.0"/>
            <y:Point x="54.68690476190476" y="45.0"/>
          </y:Path>
          <y:LineStyle color="#000000" type="dashed" width="1.0"/>
          <y:Arrows source="none" target="standard"/>
          <xsl:if test="@type='presOf'">
            <y:EdgeLabel alignment="center" distance="2.0" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="18.701171875" modelName="six_pos" modelPosition="tail" preferredPlacement="anywhere" ratio="0.5" textColor="#000000" visible="true" width="39.34375" x="-90.81071486700148" y="63.5869140625">presOf</y:EdgeLabel>
          </xsl:if>
          <xsl:if test="@sibTransId">
            <y:EdgeLabel alignment="center" distance="2.0" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="18.701171875" modelName="six_pos" modelPosition="tail" preferredPlacement="anywhere" ratio="0.5" textColor="#000000" visible="true" width="39.34375" x="-90.81071486700148" y="63.5869140625">trans <xsl:value-of select="@modelId"/></y:EdgeLabel>
          </xsl:if>
          <y:BendStyle smoothed="false"/>
        </y:PolyLineEdge>
      </g:data>
    </g:edge>

    <g:edge source="{@srcId}" target="{@sibTransId}">
      <g:data key="d9">
        <y:PolyLineEdge>
          <y:Path sx="-7.5" sy="15.0" tx="0.0" ty="-15.0">
            <y:Point x="147.49761904761905" y="45.0"/>
            <y:Point x="54.68690476190476" y="45.0"/>
          </y:Path>
          <y:LineStyle color="#000000" type="dashed" width="1.0"/>
          <y:Arrows source="none" target="standard"/>
          <xsl:if test="@type='presOf'">
            <y:EdgeLabel alignment="center" distance="2.0" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="18.701171875" modelName="six_pos" modelPosition="tail" preferredPlacement="anywhere" ratio="0.5" textColor="#000000" visible="true" width="39.34375" x="-90.81071486700148" y="63.5869140625">presOf</y:EdgeLabel>
          </xsl:if>
          <xsl:if test="@sibTransId">
            <y:EdgeLabel alignment="center" distance="2.0" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="18.701171875" modelName="six_pos" modelPosition="tail" preferredPlacement="anywhere" ratio="0.5" textColor="#000000" visible="true" width="39.34375" x="-90.81071486700148" y="63.5869140625">trans <xsl:value-of select="@modelId"/></y:EdgeLabel>
          </xsl:if>
          <y:BendStyle smoothed="false"/>
        </y:PolyLineEdge>
      </g:data>
    </g:edge>
    <g:edge source="{@sibTransId}" target="{@destId}">
      <g:data key="d9">
        <y:PolyLineEdge>
          <y:Path sx="-7.5" sy="15.0" tx="0.0" ty="-15.0">
            <y:Point x="147.49761904761905" y="45.0"/>
            <y:Point x="54.68690476190476" y="45.0"/>
          </y:Path>
          <y:LineStyle color="#000000" type="dashed" width="1.0"/>
          <y:Arrows source="none" target="standard"/>
          <xsl:if test="@type='presOf'">
            <y:EdgeLabel alignment="center" distance="2.0" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="18.701171875" modelName="six_pos" modelPosition="tail" preferredPlacement="anywhere" ratio="0.5" textColor="#000000" visible="true" width="39.34375" x="-90.81071486700148" y="63.5869140625">presOf</y:EdgeLabel>
          </xsl:if>
          <xsl:if test="@sibTransId">
            <y:EdgeLabel alignment="center" distance="2.0" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="18.701171875" modelName="six_pos" modelPosition="tail" preferredPlacement="anywhere" ratio="0.5" textColor="#000000" visible="true" width="39.34375" x="-90.81071486700148" y="63.5869140625">trans <xsl:value-of select="@modelId"/></y:EdgeLabel>
          </xsl:if>
          <y:BendStyle smoothed="false"/>
        </y:PolyLineEdge>
      </g:data>
    </g:edge>

</xsl:template>

<xsl:template match="dgm:cxn[count(@parTransId)=0]" mode="edges" >
  <!-- doesn't seem to matter if there are duplicates, so we can be lazy -->
    <g:edge source="{@srcId}" target="{@destId}">
      <g:data key="d9">
        <y:PolyLineEdge>
          <y:Path sx="-7.5" sy="15.0" tx="0.0" ty="-15.0">
            <y:Point x="147.49761904761905" y="45.0"/>
            <y:Point x="54.68690476190476" y="45.0"/>
          </y:Path>
          <y:LineStyle color="#000000" type="line" width="1.0"/>
          <y:Arrows source="none" target="standard"/>
          <xsl:if test="@type='presOf'">
            <y:EdgeLabel alignment="center" distance="2.0" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="18.701171875" modelName="six_pos" modelPosition="tail" preferredPlacement="anywhere" ratio="0.5" textColor="#000000" visible="true" width="39.34375" x="-90.81071486700148" y="63.5869140625">presOf</y:EdgeLabel>
          </xsl:if>
          <xsl:if test="@sibTransId">
            <y:EdgeLabel alignment="center" distance="2.0" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="18.701171875" modelName="six_pos" modelPosition="tail" preferredPlacement="anywhere" ratio="0.5" textColor="#000000" visible="true" width="39.34375" x="-90.81071486700148" y="63.5869140625">trans <xsl:value-of select="@modelId"/></y:EdgeLabel>
          </xsl:if>
          <y:BendStyle smoothed="false"/>
        </y:PolyLineEdge>
      </g:data>
    </g:edge>
              
</xsl:template>

</xsl:stylesheet>
