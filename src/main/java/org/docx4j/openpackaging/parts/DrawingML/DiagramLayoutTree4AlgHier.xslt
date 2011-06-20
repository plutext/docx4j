<?xml version="1.0" encoding="utf-8"?>
<!-- 
 *
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

 */ -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:java="http://xml.apache.org/xalan/java" 
                xmlns:dgm="http://schemas.openxmlformats.org/drawingml/2006/diagram"
                xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" 
                xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
                xmlns:odgm="http://opendope.org/SmartArt/DataHierarchy"
                exclude-result-prefixes="java">

                
    <xsl:output method="xml" indent="yes"/>

	<xsl:param name="list" />
	<xsl:param name="DiagramDataPart"/>
	  
    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

  <xsl:template match="/">


    <dgm:dataModel xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:ns6="http://schemas.openxmlformats.org/schemaLibrary/2006/main" xmlns:c="http://schemas.openxmlformats.org/drawingml/2006/chart" xmlns:ns8="http://schemas.openxmlformats.org/drawingml/2006/chartDrawing" xmlns:dgm="http://schemas.openxmlformats.org/drawingml/2006/diagram" xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture" xmlns:ns11="http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing" xmlns:dsp="http://schemas.microsoft.com/office/drawing/2008/diagram" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:ns15="urn:schemas-microsoft-com:office:excel" xmlns:w10="urn:schemas-microsoft-com:office:word" xmlns:ns17="urn:schemas-microsoft-com:office:powerpoint" xmlns:odx="http://opendope.org/xpaths" xmlns:odc="http://opendope.org/conditions" xmlns:odq="http://opendope.org/questions" xmlns:odi="http://opendope.org/components" xmlns:ns23="http://schemas.openxmlformats.org/officeDocument/2006/bibliography" xmlns:ns24="http://schemas.openxmlformats.org/drawingml/2006/compatibility" xmlns:ns25="http://schemas.openxmlformats.org/drawingml/2006/lockedCanvas">
      <dgm:ptLst>
        
        <!-- read the list again to create basic nodes-->
          <xsl:apply-templates select="$list/odgm:SmartArtDataHierarchy/odgm:list" mode="list2pt"/>

        <xsl:for-each select="//dgm:layoutNode">
          <xsl:apply-templates select="." />
        </xsl:for-each>

      </dgm:ptLst>
      <dgm:cxnLst>
        <xsl:comment>list2cxn</xsl:comment>
        <xsl:apply-templates select="$list/odgm:SmartArtDataHierarchy/odgm:list" mode="list2cxn"/>
        
        <xsl:comment>cxn</xsl:comment>
        <xsl:apply-templates select="dgm:layoutNode" mode="cxn"/>
      </dgm:cxnLst>
      <dgm:bg/>
      <dgm:whole/>
    </dgm:dataModel>        
    
  </xsl:template>


  <xsl:template match="odgm:listItem[@id='0']" mode="list2pt" priority="5"> 
  <!-- <xsl:template match="*[local-name()='listItem' and @id='0']" mode="list2pt" priority="5">--> 
  	<xsl:comment>Bingo! <xsl:value-of select="name()"/></xsl:comment>
    <dgm:pt type="doc" modelId="0">
      <dgm:prSet phldr="true" csCatId="accent1" csTypeId="urn:microsoft.com/office/officeart/2005/8/colors/accent1_2" 
                 qsCatId="simple" qsTypeId="urn:microsoft.com/office/officeart/2005/8/quickstyle/simple1" 
                 loCatId="hierarchy" loTypeId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"/>
      <dgm:spPr/>
      <dgm:t>
        <a:bodyPr/>
        <a:lstStyle/>
        <a:p>
          <a:endParaRPr lang="en-AU"/>
        </a:p>
      </dgm:t>
    </dgm:pt>

    <xsl:apply-templates mode="list2pt"/>

  </xsl:template>

  <xsl:template match="*" mode="list2pt">
  	<xsl:comment>list2pt Matched <xsl:value-of select="name()"/></xsl:comment>
  	<xsl:apply-templates mode="list2pt" />
  	</xsl:template>
  <xsl:template match="odgm:textBody" mode="list2pt" />
  <xsl:template match="odgm:sibTransBody" mode="list2pt" />
  <xsl:template match="odgm:image" mode="list2pt" />

<xsl:template match="odgm:listItem[not(@id='0')]" mode="list2pt"> 
	<xsl:variable name="thisID" select="string(@id)"/>
<!--  <xsl:template match="*[local-name()='listItem' and not(@id='0')]" mode="list2pt"  priority="5"> --> 
  	<xsl:comment><xsl:value-of select="name()"/> for <xsl:value-of select="@id"/></xsl:comment>
    <dgm:pt modelId="{@id}">
      <dgm:prSet phldrT="21"/> <!-- ?? -->
      <dgm:spPr/>
      
      <dgm:t>
        <a:bodyPr/>
        <a:lstStyle/>
        <xsl:for-each select="odgm:textBody/odgm:p">
        
	        <a:p>
	          <a:r>
	            <a:rPr sz="800" lang="en-AU"/>
	            <a:t><xsl:value-of select="."/></a:t>
	          </a:r>
	        </a:p>

        </xsl:for-each>
      </dgm:t>
      
      
      
      <xsl:copy-of select="$list/odgm:SmartArtDataHierarchy/odgm:texts/odgm:identifiedText[@id=$thisID]/dgm:t"/> 
      <!-- <xsl:copy-of select="$list//*[local-name()='identifiedText' and @id=$thisID]/*"/>-->
    </dgm:pt>

    <xsl:apply-templates mode="list2pt"/>
    
    <!--  Creation of parTrans and sibTrans is hardcoded here.
    
          A more correct approach would be to generate 
          
          	<dgm:layoutNode ??="parTrans">
          	
          as we parse layout.xml, and then from this, generate
          sibTrans and parChTrans.   -->

    <dgm:pt cxnId="cxn{../../@id}-{@id}" type="parTrans" modelId="pT{@id}">  <!-- invent naming conventions -->
      <dgm:prSet/>
      <dgm:spPr/>
      <dgm:t>
        <a:bodyPr/>
        <a:lstStyle/>
        <a:p>
          <a:endParaRPr lang="en-AU"/>
        </a:p>
      </dgm:t>
    </dgm:pt>
    
    <xsl:if test="not(@id='1')">
    	<xsl:comment>@id was <xsl:value-of select="@id"/></xsl:comment>
	    <!--  Make one of these for each parTrans.
	          It is required, and magic in the sense that 
	          it is not specified in the layout! 
	          (Nor is sibTrans below..) -->
	     <dgm:pt type="pres" modelId="parChTransID{../../@id}-{@id}">
	       <dgm:prSet presStyleCnt="{count(../odgm:listItem)}" 
	       				presStyleIdx="{count(preceding-sibling::odgm:listItem)}" 
	       				presStyleLbl="parChTrans1D{count(ancestor::odgm:listItem)}" 
	       				presName="Name37" 
	       				presAssocID="pT{@id}"/>
	       				<!--  or Name35? -->
	       				
	       				<!--  TODO @presStyleCnt should be the number of nodes on this level;
	       						   presStyleIdx should be the position of this one...
	       				      but incorrect values do not stop the SmartArt from generating.
	       				-->
	       <dgm:spPr/>
	     </dgm:pt>    
	</xsl:if>

    <dgm:pt cxnId="cxn{../../@id}-{@id}" type="sibTrans" modelId="sT{@id}">
      <dgm:prSet/>
      <dgm:spPr/>
      <xsl:choose>
      	<xsl:when test="odgm:sibTransBody">
			<xsl:variable name="stIDREF" select="string(odgm:sibTransBody/@contentRef)"/>      		
	        <xsl:copy-of select="$list/odgm:SmartArtDataHierarchy/odgm:texts/odgm:identifiedText[@id=$stIDREF]/dgm:t"/> 
      	</xsl:when>
      	<xsl:otherwise>
	      <dgm:t>
	        <a:bodyPr/>
	        <a:lstStyle/>
	        <a:p>
	          <a:endParaRPr lang="en-AU"/>
	        </a:p>
	      </dgm:t>      	
      	</xsl:otherwise>      
      </xsl:choose>
    </dgm:pt>

  </xsl:template>



  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'hierChild')]">

    <xsl:choose>
      <xsl:when test="@name='hierChild1'">
        <dgm:pt type="pres" modelId="{@modelId}">
          <dgm:prSet presStyleCnt="0" presName="hierChild1" presAssocID="{@presAssocID}">
            <!--  @presStyleCnt="0" always -->
            <dgm:presLayoutVars>
              <dgm:orgChart val="true"/>
              <dgm:chPref val="1"/>
              <dgm:dir/>
              <dgm:animOne val="branch"/>
              <dgm:animLvl val="lvl"/>
              <dgm:resizeHandles/>
            </dgm:presLayoutVars>
          </dgm:prSet>
          <dgm:spPr/>
        </dgm:pt>
      </xsl:when>
      <xsl:otherwise>
        <dgm:pt type="pres" modelId="{@modelId}">
          <dgm:prSet presStyleCnt="0" presName="{@name}" presAssocID="{@presAssocID}"/>
          <dgm:spPr/>
        </dgm:pt>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'hierRoot')]">
    <dgm:pt type="pres" modelId="{@modelId}">
      <dgm:prSet presStyleCnt="0" presName="{@name}" presAssocID="{@presAssocID}">
        <!--  @presStyleCnt="0" always -->
        <dgm:presLayoutVars>
          <dgm:hierBranch /> <!--  val="init" can cause right hanging layout  -->
        </dgm:presLayoutVars>
      </dgm:prSet>
      <dgm:spPr/>
    </dgm:pt>
  </xsl:template>

  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'rootComposite')
  	or starts-with(string(@name), 'composite')]">
    <dgm:pt type="pres" modelId="{@modelId}">
      <dgm:prSet presStyleCnt="0" presName="{@name}" presAssocID="{@presAssocID}"/>
      <!--  @presStyleCnt="0" always -->
      <dgm:spPr/>
    </dgm:pt>
  </xsl:template>


  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'rootText')]">
  
  	<xsl:choose>
  		<xsl:when test="@depth='1'"> <!--  special case: force node0 (match Word 2007) -->
  		
		    <dgm:pt type="pres" modelId="{@modelId}">
		      <dgm:prSet presStyleCnt="{@presStyleCnt}" presStyleIdx="{@presStyleIdx}" 
		      		presStyleLbl="node0" presName="rootText1" presAssocID="{@presAssocID}">
		        <!--  @presStyleCnt=number of elements on this level
		              @presStyleIdx=index of this elemnet, starting at 0              
		              @styleLbl=node0,node2,node3,node4  -->
		        <dgm:presLayoutVars>
		          <dgm:chPref val="3"/>
		        </dgm:presLayoutVars>
		      </dgm:prSet>
		      <dgm:spPr/>
		    </dgm:pt>
  		
  		</xsl:when>
  		<xsl:otherwise>
  		
		    <dgm:pt type="pres" modelId="{@modelId}">
		      <dgm:prSet presStyleCnt="{@presStyleCnt}" presStyleIdx="{@presStyleIdx}" 
		      		presStyleLbl="node{@depth}" presName="rootText" presAssocID="{@presAssocID}">
		      		<!--  Note @presName="rootText" (ie no trailing 1). -->
		      		
		        <!--  @presStyleCnt=number of elements on this level
		              @presStyleIdx=index of this elemnet, starting at 0              
		              @styleLbl=node0,node2,node3,node4  -->
		        <dgm:presLayoutVars>
		          <dgm:chPref val="3"/>
		        </dgm:presLayoutVars>
		      </dgm:prSet>
		      <dgm:spPr/>
		    </dgm:pt>
  		
  		</xsl:otherwise>  	
  	</xsl:choose>
  
  </xsl:template>
  
  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'rootPict')  or starts-with(string(@name), 'image')]">

	<xsl:variable name="presAssocID" select="string(@presAssocID)"/>
	<xsl:variable name="listItemParent" select="$list/odgm:SmartArtDataHierarchy/odgm:list//odgm:listItem[@id=$presAssocID]"/>
	<xsl:variable name="imageRef" select="$listItemParent/odgm:imageRef"/>
	<xsl:variable name="imageId" select="string($imageRef/@contentRef)"/>

	<xsl:choose>
		<xsl:when test="string($imageId)=''">
			<!--  no image ref-->
			<dgm:pt type="pres" modelId="{@modelId}">
				<dgm:prSet presStyleCnt="{@presStyleCnt}" presStyleIdx="{@presStyleIdx}"
					presStyleLbl="alignImgPlace1" presName="{@name}" presAssocID="{@presAssocID}" />
				<!--  no image -->
				<dgm:spPr />
				<dgm:t>
					<a:bodyPr />
					<a:lstStyle />
					<a:p>
						<a:endParaRPr lang="en-AU" />
					</a:p>
				</dgm:t>
			</dgm:pt>
		</xsl:when>
		<xsl:otherwise>
			<!--  image should be good -->
			<xsl:variable name="image"
				select="$list/odgm:SmartArtDataHierarchy/odgm:images/odgm:image[@id=$imageId]" />

			<xsl:variable name="relId"
				select="java:org.docx4j.openpackaging.parts.DrawingML.DiagramDataPart.addImage($DiagramDataPart, string($image))" />

			<dgm:pt type="pres" modelId="{@modelId}">
				<dgm:prSet presStyleCnt="{@presStyleCnt}" presStyleIdx="{@presStyleIdx}"
					presStyleLbl="alignImgPlace1" presName="{@name}" presAssocID="{@presAssocID}">
					<!--
						@presStyleCnt=number of pictures in total in diagram
						@presStyleIdx=index of this picture, starting at 0
					-->

					<xsl:if test="$imageRef/@custScaleX">
						<xsl:attribute name="custScaleX"><xsl:value-of
							select="$imageRef/@custScaleX" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="$imageRef/@custScaleY">
						<xsl:attribute name="custScaleY"><xsl:value-of
							select="$imageRef/@custScaleY" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="$imageRef/@custLinFactNeighborX">
						<xsl:attribute name="custLinFactNeighborX"><xsl:value-of
							select="$imageRef/@custLinFactNeighborX" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="$imageRef/@custLinFactNeighborY">
						<xsl:attribute name="custLinFactNeighborY"><xsl:value-of
							select="$imageRef/@custLinFactNeighborY" /></xsl:attribute>
					</xsl:if>
				</dgm:prSet>
				<xsl:choose>
					<xsl:when test="string($relId)=''">
						<!--  we couldn't event get a broken image icon back -->
						<dgm:spPr />
						<dgm:t>
							<a:bodyPr />
							<a:lstStyle />
							<a:p>
								<a:endParaRPr lang="en-AU" />
							</a:p>
						</dgm:t>
					</xsl:when>
					<xsl:otherwise>
						<!--  image should be good -->
						<dgm:spPr>
							<a:blipFill rotWithShape="false">
								<a:blip r:embed="{$relId}" />
								<a:stretch>
									<a:fillRect />
								</a:stretch>
							</a:blipFill>
						</dgm:spPr>
					</xsl:otherwise>
				</xsl:choose>
			</dgm:pt>
		</xsl:otherwise>
	</xsl:choose>
		
  </xsl:template>

  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'rootConnector')]">
    
  	<xsl:choose>
  		<xsl:when test="@depth='1'"> <!--  special case: incorrectly force cnt 1 (match Word 2007) -->
  		
		    <dgm:pt type="pres" modelId="{@modelId}">
		      <dgm:prSet presStyleCnt="1" presStyleIdx="{@presStyleIdx}" 
		      presStyleLbl="node{@depth}" presName="{@name}" presAssocID="{@presAssocID}"/>
		      <!--  @presStyleCnt=number of elements on this level
		              @presStyleIdx=index of this elemnet, starting at 0              
		              @styleLbl=asst0,node2,node3,node4  -->
		      <dgm:spPr/>
		    </dgm:pt>
  		
  		</xsl:when>
  		<xsl:otherwise>
  		
		    <dgm:pt type="pres" modelId="{@modelId}">
		      <dgm:prSet presStyleCnt="{@presStyleCnt}" presStyleIdx="{@presStyleIdx}" 
		      presStyleLbl="node{@depth}" presName="{@name}" presAssocID="{@presAssocID}"/>
		      <!--  @presStyleCnt=number of elements on this level
		              @presStyleIdx=index of this elemnet, starting at 0              
		              @styleLbl=asst0,node2,node3,node4  -->
		      <dgm:spPr/>
		    </dgm:pt>
  		
  		</xsl:otherwise>  	
  	</xsl:choose>
    
  </xsl:template>

<!--  *********************************************************************** -->
<!--  ******************  dgm:cxnLst entries  ******************************* -->
<!--  *********************************************************************** -->

<!--  Note: we make a little bit of effort to have @srcOrd match Word 2007,
      but not much, since (a) these values don't seem to matter at all,
      and (b) in some cases Word emits several cxn elements with the
      same value (for a given @srcId). -->

  <xsl:template match="odgm:listItem[@id='0']"  mode="list2cxn" priority="5">
    <xsl:apply-templates mode="list2cxn"/>
    <!--
    <dgm:cxn sibTransId="sT0" parTransId="pT0" destOrd="0" srcOrd="0" destId="1" srcId="0" modelId="cxn0-1"/>
     -->
  </xsl:template>

  <xsl:template match="odgm:listItem[not(@id='0')]"  mode="list2cxn" priority="5">
    <dgm:cxn sibTransId="sT{@id}" parTransId="pT{@id}" destOrd="0" 
    srcOrd="{count(preceding-sibling::odgm:listItem)}" destId="{@id}" srcId="{../../@id}" 
    modelId="cxn{../../@id}-{@id}"/>
    <xsl:if test="not(@id='1')">
	    <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
	                 srcId="pT{@id}" destId="parChTransID{../../@id}-{@id}"
	                 destOrd="0" srcOrd="1"
	                 modelId="{generate-id()}"
	                 type="presOf" />
    </xsl:if>
                     
    <xsl:apply-templates mode="list2cxn"/>
  </xsl:template>
  
  <xsl:template match="*" mode="list2cxn">
  	<xsl:apply-templates mode="list2cxn" />
  	</xsl:template>
  <xsl:template match="odgm:textBody" mode="list2cxn" />
  <xsl:template match="odgm:sibTransBody" mode="list2cxn" />  
  <xsl:template match="odgm:image" mode="list2cxn" />
  
  
  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'hierChild')]" mode="cxn">
    <xsl:choose>
      <xsl:when test="@name='hierChild1'">
        <!-- connection from real root -->
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
                 srcId="0" destId="{@modelId}"
                 destOrd="0" srcOrd="0"
                 modelId="{generate-id()}"
                 type="presOf" />
      </xsl:when>
      <xsl:otherwise>
        <!-- cxn from parent hierRoot-->
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" 
                 srcId="{../@modelId}" destId="{@modelId}" 
                 destOrd="0" 
                 srcOrd="{count(preceding-sibling::dgm:layoutNode)}"  
                 modelId="{generate-id()}"
                 type="presParOf" />
                 
        <!--  cxn to  
	    <xsl:if test="count(*)"> 
	        
		    <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
		                 srcId="{@modelId}" destId="parChTransID{../../@presAssocID}-{@presAssocID}"
		                 destOrd="0" srcOrd="0"
		                 modelId="{generate-id()}"
		                 type="presParOf" />
		</xsl:if>-->
      </xsl:otherwise>
    </xsl:choose>

      <xsl:apply-templates mode="cxn"/>

  </xsl:template>
  

  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'hierRoot')]" mode="cxn">
    <!-- cxn from parent hierChild-->
    <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
             srcId="{../@modelId}" destId="{@modelId}" 
             destOrd="0" 
             srcOrd="{ (count(preceding-sibling::dgm:layoutNode)*2)+1 }"
                 modelId="{generate-id()}a"
             type="presParOf" />
             <!--  srcOrd="{ (count(preceding-sibling::dgm:layoutNode)*2)+1 }"  -->
             <!--  srcOrd="{ ((position()-1)*2)+1 }"  -->
             
        <!--  cxn from *hierChild* to.
        	  We do it here rather than on the parent child, 
        	  since this has the value we need for @presAssocID  -->
	    <xsl:if test="@name!='hierRoot1'"> 
	        
		    <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
		                 srcId="{../@modelId}" destId="parChTransID{../../@presAssocID}-{@presAssocID}"
		                 destOrd="0" 
             srcOrd="{ (count(preceding-sibling::dgm:layoutNode)*2) }"
		                 modelId="{generate-id()}b"
		                 type="presParOf" />
		</xsl:if>
             <!--  srcOrd="{ (count(preceding-sibling::dgm:layoutNode)*2) }" -->
             <!--  srcOrd="{ (position()-1)*2 }" -->
             
    <xsl:apply-templates mode="cxn"/>
  </xsl:template>

  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'rootComposite')]" mode="cxn">
    <!-- cxn from parent hierRoot-->
    <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
             srcId="{../@modelId}" destId="{@modelId}"
             destOrd="0" srcOrd="0"
                 modelId="{generate-id()}"
             type="presParOf" />
    <xsl:apply-templates mode="cxn"/>
  </xsl:template>

  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'rootText')]" mode="cxn">
    <!-- cxn from parent rootComposite-->
    <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
             srcId="{../@modelId}" destId="{@modelId}"
             destOrd="0" srcOrd="0"
                 modelId="{generate-id()}a"
             type="presParOf" /> 
    
    <!-- cxn from list proper -->
    <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
             srcId="{@presAssocID}" destId="{@modelId}"
             destOrd="0" srcOrd="1"
                 modelId="{generate-id()}n"
             type="presOf" /> <!--  ID is based on the source node, so take care to differentiate! -->

    <xsl:apply-templates mode="cxn"/>
  </xsl:template>

  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'rootPict')]" mode="cxn">
    <!-- cxn from parent rootComposite-->
    <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
             srcId="{../@modelId}" destId="{@modelId}"
             destOrd="0" srcOrd="1"
                 modelId="{generate-id()}"
             type="presParOf" />
    <xsl:apply-templates mode="cxn"/>
  </xsl:template>

  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'image')]" mode="cxn" />

  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'rootConnector')]" mode="cxn">
    <!-- cxn from parent rootComposite-->
    <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
             srcId="{../@modelId}" destId="{@modelId}"
             destOrd="0" srcOrd="2"
                 modelId="{generate-id()}a"
             type="presParOf" />

    <!-- cxn from list proper -->
    <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
             srcId="{@presAssocID}" destId="{@modelId}"
             destOrd="0" srcOrd="1"
                 modelId="{generate-id()}b"
             type="presOf" />

    <xsl:apply-templates mode="cxn"/>
  </xsl:template>


</xsl:stylesheet>