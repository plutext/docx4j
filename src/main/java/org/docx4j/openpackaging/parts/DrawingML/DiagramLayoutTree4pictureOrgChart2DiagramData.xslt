<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dgm="http://schemas.openxmlformats.org/drawingml/2006/diagram"
                xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" 
                xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
    <xsl:output method="xml" indent="yes"/>

	<xsl:param name="list" />
	  
    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

  <xsl:template match="/">

    <dgm:dataModel xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:ns6="http://schemas.openxmlformats.org/schemaLibrary/2006/main" xmlns:c="http://schemas.openxmlformats.org/drawingml/2006/chart" xmlns:ns8="http://schemas.openxmlformats.org/drawingml/2006/chartDrawing" xmlns:dgm="http://schemas.openxmlformats.org/drawingml/2006/diagram" xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture" xmlns:ns11="http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing" xmlns:dsp="http://schemas.microsoft.com/office/drawing/2008/diagram" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:ns15="urn:schemas-microsoft-com:office:excel" xmlns:w10="urn:schemas-microsoft-com:office:word" xmlns:ns17="urn:schemas-microsoft-com:office:powerpoint" xmlns:odx="http://opendope.org/xpaths" xmlns:odc="http://opendope.org/conditions" xmlns:odq="http://opendope.org/questions" xmlns:odi="http://opendope.org/components" xmlns:ns23="http://schemas.openxmlformats.org/officeDocument/2006/bibliography" xmlns:ns24="http://schemas.openxmlformats.org/drawingml/2006/compatibility" xmlns:ns25="http://schemas.openxmlformats.org/drawingml/2006/lockedCanvas">
      <!--  xsl:variable name="list" select="document('sample-list-boss-plus-212.xml')" /-->
      <dgm:ptLst>
        
        <!-- read the list again to create basic nodes-->
          <xsl:apply-templates select="$list" mode="list2pt"/>

        <xsl:for-each select="//dgm:layoutNode">
          <xsl:apply-templates select="." />
        </xsl:for-each>

      </dgm:ptLst>
      <dgm:cxnLst>
        <xsl:comment>list2cxn</xsl:comment>
        <xsl:apply-templates select="$list" mode="list2cxn"/>
        
        <xsl:comment>cxn</xsl:comment>
        <xsl:apply-templates select="dgm:layoutNode" mode="cxn"/>
      </dgm:cxnLst>
      <dgm:bg/>
      <dgm:whole/>
    </dgm:dataModel>        
    
  </xsl:template>

  <xsl:template match="node[@id='0']" mode="list2pt">
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



  <xsl:template match="node[not(@id='0')]" mode="list2pt">
    <dgm:pt modelId="{@id}">
      <dgm:prSet phldrT="21"/> <!-- ?? -->
      <dgm:spPr/>
      <dgm:t>
        <a:bodyPr/>
        <a:lstStyle/>
        <a:p>
          <a:r>
            <a:rPr lang="en-AU"/>
            <a:t>
              <xsl:value-of select="@val"/>
            </a:t>
          </a:r>
        </a:p>
      </dgm:t>
    </dgm:pt>

    <xsl:apply-templates mode="list2pt"/>

    <dgm:pt cxnId="cxn{../@id}-{@id}" type="parTrans" modelId="pT{@id}">  <!-- invent naming conventions -->
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
	          it is not specified in the layout! -->
	     <dgm:pt type="pres" modelId="parChTransID{../@id}-{@id}">
	       <dgm:prSet presStyleCnt="1" presStyleIdx="{@presStyleIdx}" 
	       				presStyleLbl="parChTrans1D{count(ancestor::*)}" presName="Name37" 
	       				presAssocID="pT{@id}"/>
	       				<!--  or Name35? -->
	       <dgm:spPr/>
	     </dgm:pt>    
	</xsl:if>

    <dgm:pt cxnId="cxn{../@id}-{@id}" type="sibTrans" modelId="sT{@id}">
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
          <dgm:hierBranch val="init"/>
        </dgm:presLayoutVars>
      </dgm:prSet>
      <dgm:spPr/>
    </dgm:pt>
  </xsl:template>

  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'rootComposite')]">
    <dgm:pt type="pres" modelId="{@modelId}">
      <dgm:prSet presStyleCnt="0" presName="{@name}" presAssocID="{@presAssocID}"/>
      <!--  @presStyleCnt="0" always -->
      <dgm:spPr/>
    </dgm:pt>
  </xsl:template>

  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'rootText')]">
    <dgm:pt type="pres" modelId="{@modelId}">
      <dgm:prSet presStyleCnt="{@presStyleCnt}" presStyleIdx="{@presStyleIdx}" presStyleLbl="node{@depth}" presName="rootText1" presAssocID="{@presAssocID}">
        <!--  @presStyleCnt=number of elements on this level
              @presStyleIdx=index of this elemnet, starting at 0              
              @styleLbl=node0,node2,node3,node4  -->
        <dgm:presLayoutVars>
          <dgm:chPref val="3"/>
        </dgm:presLayoutVars>
      </dgm:prSet>
      <dgm:spPr/>
    </dgm:pt>
  </xsl:template>
  
  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'rootPict')]">
    <dgm:pt type="pres" modelId="{@modelId}">
      <dgm:prSet presStyleCnt="{@presStyleCnt}" presStyleIdx="{@presStyleIdx}" presStyleLbl="alignImgPlace1" presName="{@name}" presAssocID="{@presAssocID}"/>
      <!--  @presStyleCnt=number of pictures in total in diagram
            @presStyleIdx=index of this picture, starting at 0   -->
      <dgm:spPr>
        <a:blipFill rotWithShape="false">
          <a:blip r:embed="rId1"/>
          <a:stretch>
            <a:fillRect/>
          </a:stretch>
        </a:blipFill>
      </dgm:spPr>
    </dgm:pt>
  </xsl:template>

  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'rootConnector')]">
    <dgm:pt type="pres" modelId="{@modelId}">
      <dgm:prSet presStyleCnt="{@presStyleCnt}" presStyleIdx="{@presStyleIdx}" presStyleLbl="node{@depth}" presName="{@name}" presAssocID="{@presAssocID}"/>
      <!--  @presStyleCnt=number of elements on this level
              @presStyleIdx=index of this elemnet, starting at 0              
              @styleLbl=asst0,node2,node3,node4  -->
      <dgm:spPr/>
    </dgm:pt>
  </xsl:template>


  <xsl:template match="node[@id='0']"  mode="list2cxn">
    <xsl:apply-templates mode="list2cxn"/>
    <!--
    <dgm:cxn sibTransId="sT0" parTransId="pT0" destOrd="0" srcOrd="0" destId="1" srcId="0" modelId="cxn0-1"/>
     -->
  </xsl:template>

  <xsl:template match="node[not(@id='0')]"  mode="list2cxn">
    <dgm:cxn sibTransId="sT{@id}" parTransId="pT{@id}" destOrd="0" srcOrd="0" destId="{@id}" srcId="{../@id}" modelId="cxn{../@id}-{@id}"/>
    <xsl:if test="not(@id='1')">
	    <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
	                 srcId="pT{@id}" destId="parChTransID{../@id}-{@id}"
	                 destOrd="0" srcOrd="1"
	                 modelId="{generate-id()}"
	                 type="presOf" />
    </xsl:if>
                     
    <xsl:apply-templates mode="list2cxn"/>
  </xsl:template>
  
  
  
  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'hierChild')]" mode="cxn">
    <xsl:choose>
      <xsl:when test="@name='hierChild1'">
        <!-- connection from real root -->
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
                 srcId="0" destId="{@modelId}"
                 destOrd="0" srcOrd="1"
                 modelId="{generate-id()}"
                 type="presOf" />
      </xsl:when>
      <xsl:otherwise>
        <!-- cxn from parent hierRoot-->
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" 
                 srcId="{../@modelId}" destId="{@modelId}" 
                 destOrd="0" srcOrd="1"  
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
             destOrd="0" srcOrd="1"
                 modelId="{generate-id()}a"
             type="presParOf" />
             
        <!--  cxn from *hierChild* to.
        	  We do it here rather than on the parent child, 
        	  since this has the value we need for @presAssocID  -->
	    <xsl:if test="@name!='hierRoot1'"> 
	        
		    <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
		                 srcId="{../@modelId}" destId="parChTransID{../../@presAssocID}-{@presAssocID}"
		                 destOrd="0" srcOrd="0"
		                 modelId="{generate-id()}b"
		                 type="presParOf" />
		</xsl:if>
             
             
    <xsl:apply-templates mode="cxn"/>
  </xsl:template>

  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'rootComposite')]" mode="cxn">
    <!-- cxn from parent hierRoot-->
    <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
             srcId="{../@modelId}" destId="{@modelId}"
             destOrd="0" srcOrd="1"
                 modelId="{generate-id()}"
             type="presParOf" />
    <xsl:apply-templates mode="cxn"/>
  </xsl:template>

  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'rootText')]" mode="cxn">
    <!-- cxn from parent rootComposite-->
    <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
             srcId="{../@modelId}" destId="{@modelId}"
             destOrd="0" srcOrd="1"
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

  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'rootConnector')]" mode="cxn">
    <!-- cxn from parent rootComposite-->
    <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
             srcId="{../@modelId}" destId="{@modelId}"
             destOrd="0" srcOrd="1"
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
