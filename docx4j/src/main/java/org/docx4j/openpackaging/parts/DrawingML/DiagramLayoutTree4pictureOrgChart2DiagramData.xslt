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

    <!--
    <dgm:pt cxnId="cxn0-1" type="parTrans" modelId="pT0"> 
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

    <dgm:pt cxnId="cxn0-1" type="sibTrans" modelId="sT0">
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
    -->

  </xsl:template>

  <xsl:template match="node[@id='0']"  mode="list2cxn">
    <xsl:apply-templates mode="list2cxn"/>
    <!--
    <dgm:cxn sibTransId="sT0" parTransId="pT0" destOrd="0" srcOrd="0" destId="1" srcId="0" modelId="cxn0-1"/>
     -->
  </xsl:template>

  <xsl:template match="node[@id!='0']"  mode="list2cxn">
    <dgm:cxn sibTransId="sT{@id}" parTransId="pT{@id}" destOrd="0" srcOrd="0" destId="{@id}" srcId="{../@id}" modelId="cxn{../@id}-{@id}"/>
    <xsl:apply-templates mode="list2cxn"/>
  </xsl:template>


  <xsl:template match="node[@id!='0']" mode="list2pt">
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
  
  
    <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'hierChild')]" mode="cxn">

    <xsl:choose>
      <xsl:when test="@name='hierChild1'">
        <!-- connection from real rool -->
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
                 srcId="0" destId="{@modelId}"
                 destOrd="0" srcOrd="1"
                 type="presOf" />
      </xsl:when>
      <xsl:otherwise>
        <!-- cxn from parent hierRoot-->
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" 
                 srcId="{../@modelId}" destId="{@modelId}" 
                 destOrd="0" srcOrd="1"  
                 type="presParOf" />
      </xsl:otherwise>
    </xsl:choose>

      <xsl:apply-templates mode="cxn"/>

  </xsl:template>

  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'hierRoot')]" mode="cxn">
    <!-- cxn from parent hierChild-->
    <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
             srcId="{../@modelId}" destId="{@modelId}" 
             destOrd="0" srcOrd="1"
             type="presParOf" />
    <xsl:apply-templates mode="cxn"/>
  </xsl:template>

  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'rootComposite')]" mode="cxn">
    <!-- cxn from parent hierRoot-->
    <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
             srcId="{../@modelId}" destId="{@modelId}"
             destOrd="0" srcOrd="1"
             type="presParOf" />
    <xsl:apply-templates mode="cxn"/>
  </xsl:template>

  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'rootText')]" mode="cxn">
    <!-- cxn from parent rootComposite-->
    <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
             srcId="{../@modelId}" destId="{@modelId}"
             destOrd="0" srcOrd="1"
             type="presParOf" />
    
    <!-- cxn from list proper -->
    <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
             srcId="{@presAssocID}" destId="{@modelId}"
             destOrd="0" srcOrd="1"
             type="presOf" />

    <xsl:apply-templates mode="cxn"/>
  </xsl:template>

  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'rootPict')]" mode="cxn">
    <!-- cxn from parent rootComposite-->
    <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
             srcId="{../@modelId}" destId="{@modelId}"
             destOrd="0" srcOrd="1"
             type="presParOf" />
    <xsl:apply-templates mode="cxn"/>
  </xsl:template>

  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'rootConnector')]" mode="cxn">
    <!-- cxn from parent rootComposite-->
    <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
             srcId="{../@modelId}" destId="{@modelId}"
             destOrd="0" srcOrd="1"
             type="presParOf" />

    <!-- cxn from list proper -->
    <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"
             srcId="{@presAssocID}" destId="{@modelId}"
             destOrd="0" srcOrd="1"
             type="presOf" />

    <xsl:apply-templates mode="cxn"/>
  </xsl:template>



  <!-- ********************* -->
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
          <dgm:t>
            <a:bodyPr/>
            <a:lstStyle/>
            <a:p>
              <a:endParaRPr lang="en-AU"/>
            </a:p>
          </dgm:t>
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
      <dgm:t>
        <a:bodyPr/>
        <a:lstStyle/>
        <a:p>
          <a:endParaRPr lang="en-AU"/>
        </a:p>
      </dgm:t>
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
      <dgm:t>
        <a:bodyPr/>
        <a:lstStyle/>
        <a:p>
          <a:endParaRPr lang="en-AU"/>
        </a:p>
      </dgm:t>
    </dgm:pt>
  </xsl:template>

  <xsl:template match="dgm:layoutNode[starts-with(string(@name), 'rootConnector')]">
    <dgm:pt type="pres" modelId="{@modelId}">
      <dgm:prSet presStyleCnt="{@presStyleCnt}" presStyleIdx="{@presStyleIdx}" presStyleLbl="node{@depth}" presName="{@name}" presAssocID="{@presAssocID}"/>
      <!--  @presStyleCnt=number of elements on this level
              @presStyleIdx=index of this elemnet, starting at 0              
              @styleLbl=asst0,node2,node3,node4  -->
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

  <!--
        <dgm:pt type="doc" modelId="0">
          <dgm:prSet phldr="true" csCatId="accent1" csTypeId="urn:microsoft.com/office/officeart/2005/8/colors/accent1_2" qsCatId="simple" qsTypeId="urn:microsoft.com/office/officeart/2005/8/quickstyle/simple1" loCatId="hierarchy" loTypeId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart"/>
          <dgm:spPr/>
          <dgm:t>
            <a:bodyPr/>
            <a:lstStyle/>
            <a:p>
              <a:endParaRPr lang="en-AU"/>
            </a:p>
          </dgm:t>
        </dgm:pt>
        <dgm:pt modelId="1">
          <dgm:prSet phldrT="21"/>
          <dgm:spPr/>
          <dgm:t>
            <a:bodyPr/>
            <a:lstStyle/>
            <a:p>
              <a:r>
                <a:rPr lang="en-AU"/>
                <a:t>1</a:t>
              </a:r>
            </a:p>
          </dgm:t>
        </dgm:pt>
        <dgm:pt modelId="2">
          <dgm:prSet phldrT="212"/>
          <dgm:spPr/>
          <dgm:t>
            <a:bodyPr/>
            <a:lstStyle/>
            <a:p>
              <a:r>
                <a:rPr lang="en-AU"/>
                <a:t>21</a:t>
              </a:r>
            </a:p>
          </dgm:t>
        </dgm:pt>
        <dgm:pt cxnId="29" type="parTrans" modelId="3">
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
        <dgm:pt cxnId="29" type="sibTrans" modelId="4">
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
        <dgm:pt cxnId="24" type="parTrans" modelId="5">
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
        <dgm:pt cxnId="24" type="sibTrans" modelId="6">
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
        <dgm:pt type="pres" modelId="{00000000-0000-0000-0000-000000000007}">
          <dgm:prSet presStyleCnt="0" presName="hierChild1" presAssocID="0">
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
          <dgm:t>
            <a:bodyPr/>
            <a:lstStyle/>
            <a:p>
              <a:endParaRPr lang="en-AU"/>
            </a:p>
          </dgm:t>
        </dgm:pt>
        <dgm:pt type="pres" modelId="{00000000-0000-0000-0000-000000000008}">
          <dgm:prSet presStyleCnt="0" presName="hierRoot1" presAssocID="1">
            <dgm:presLayoutVars>
              <dgm:hierBranch val="init"/>
            </dgm:presLayoutVars>
          </dgm:prSet>
          <dgm:spPr/>
        </dgm:pt>
        <dgm:pt type="pres" modelId="{00000000-0000-0000-0000-000000000009}">
          <dgm:prSet presStyleCnt="0" presName="rootComposite1" presAssocID="1"/>
          <dgm:spPr/>
        </dgm:pt>
        <dgm:pt type="pres" modelId="{00000000-0000-0000-0000-000000000010}">
          <dgm:prSet presStyleCnt="1" presStyleIdx="0" presStyleLbl="node0" presName="rootText1" presAssocID="1">
            <dgm:presLayoutVars>
              <dgm:chPref val="3"/>
            </dgm:presLayoutVars>
          </dgm:prSet>
          <dgm:spPr/>
          <dgm:t>
            <a:bodyPr/>
            <a:lstStyle/>
            <a:p>
              <a:endParaRPr lang="en-AU"/>
            </a:p>
          </dgm:t>
        </dgm:pt>
        <dgm:pt type="pres" modelId="{00000000-0000-0000-0000-000000000011}">
          <dgm:prSet presStyleCnt="2" presStyleIdx="0" presStyleLbl="alignImgPlace1" presName="rootPict1" presAssocID="1"/>
          <dgm:spPr>
            <a:blipFill rotWithShape="false">
              <a:blip r:embed="rId1"/>
              <a:stretch>
                <a:fillRect/>
              </a:stretch>
            </a:blipFill>
          </dgm:spPr>
          <dgm:t>
            <a:bodyPr/>
            <a:lstStyle/>
            <a:p>
              <a:endParaRPr lang="en-AU"/>
            </a:p>
          </dgm:t>
        </dgm:pt>
        <dgm:pt type="pres" modelId="{00000000-0000-0000-0000-000000000012}">
          <dgm:prSet presStyleCnt="0" presStyleIdx="0" presStyleLbl="node1" presName="rootConnector1" presAssocID="1"/>
          <dgm:spPr/>
          <dgm:t>
            <a:bodyPr/>
            <a:lstStyle/>
            <a:p>
              <a:endParaRPr lang="en-AU"/>
            </a:p>
          </dgm:t>
        </dgm:pt>
        <dgm:pt type="pres" modelId="{00000000-0000-0000-0000-000000000013}">
          <dgm:prSet presStyleCnt="0" presName="hierChild2" presAssocID="1"/>
          <dgm:spPr/>
        </dgm:pt>
        <dgm:pt type="pres" modelId="{00000000-0000-0000-0000-000000000014}">
          <dgm:prSet presStyleCnt="1" presStyleIdx="0" presStyleLbl="parChTrans1D2" presName="Name37" presAssocID="5"/>
          <dgm:spPr/>
        </dgm:pt>
        <dgm:pt type="pres" modelId="{00000000-0000-0000-0000-000000000015}">
          <dgm:prSet presStyleCnt="0" presName="hierRoot2" presAssocID="2">
            <dgm:presLayoutVars>
              <dgm:hierBranch val="init"/>
            </dgm:presLayoutVars>
          </dgm:prSet>
          <dgm:spPr/>
        </dgm:pt>
        <dgm:pt type="pres" modelId="{00000000-0000-0000-0000-000000000016}">
          <dgm:prSet presStyleCnt="0" presName="rootComposite" presAssocID="2"/>
          <dgm:spPr/>
        </dgm:pt>
        <dgm:pt type="pres" modelId="{00000000-0000-0000-0000-000000000017}">
          <dgm:prSet presStyleCnt="1" presStyleIdx="0" presStyleLbl="node2" presName="rootText" presAssocID="2">
            <dgm:presLayoutVars>
              <dgm:chPref val="3"/>
            </dgm:presLayoutVars>
          </dgm:prSet>
          <dgm:spPr/>
          <dgm:t>
            <a:bodyPr/>
            <a:lstStyle/>
            <a:p>
              <a:endParaRPr lang="en-AU"/>
            </a:p>
          </dgm:t>
        </dgm:pt>
        <dgm:pt type="pres" modelId="{00000000-0000-0000-0000-000000000018}">
          <dgm:prSet presStyleCnt="2" presStyleIdx="1" presStyleLbl="alignImgPlace1" presName="rootPict" presAssocID="2"/>
          <dgm:spPr>
            <a:blipFill rotWithShape="false">
              <a:blip r:embed="rId2"/>
              <a:stretch>
                <a:fillRect/>
              </a:stretch>
            </a:blipFill>
          </dgm:spPr>
          <dgm:t>
            <a:bodyPr/>
            <a:lstStyle/>
            <a:p>
              <a:endParaRPr lang="en-AU"/>
            </a:p>
          </dgm:t>
        </dgm:pt>
        <dgm:pt type="pres" modelId="{00000000-0000-0000-0000-000000000019}">
          <dgm:prSet presStyleCnt="1" presStyleIdx="0" presStyleLbl="node2" presName="rootConnector" presAssocID="2"/>
          <dgm:spPr/>
          <dgm:t>
            <a:bodyPr/>
            <a:lstStyle/>
            <a:p>
              <a:endParaRPr lang="en-AU"/>
            </a:p>
          </dgm:t>
        </dgm:pt>
        <dgm:pt type="pres" modelId="{00000000-0000-0000-0000-000000000020}">
          <dgm:prSet presStyleCnt="0" presName="hierChild4" presAssocID="2"/>
          <dgm:spPr/>
        </dgm:pt>
        <dgm:pt type="pres" modelId="{00000000-0000-0000-0000-000000000021}">
          <dgm:prSet presStyleCnt="0" presName="hierChild5" presAssocID="2"/>
          <dgm:spPr/>
        </dgm:pt>
        <dgm:pt type="pres" modelId="{00000000-0000-0000-0000-000000000022}">
          <dgm:prSet presStyleCnt="0" presName="hierChild3" presAssocID="1"/>
          <dgm:spPr/>
        </dgm:pt>

-->
  
<!--         <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" destOrd="0" srcOrd="0" destId="{00000000-0000-0000-0000-000000000014}" srcId="5" type="presOf" modelId="23"/>
        <dgm:cxn sibTransId="6" parTransId="5" destOrd="0" srcOrd="0" destId="2" srcId="1" modelId="24"/>
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" destOrd="0" srcOrd="1" destId="{00000000-0000-0000-0000-000000000012}" srcId="1" type="presOf" modelId="25"/>
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" destOrd="0" srcOrd="1" destId="{00000000-0000-0000-0000-000000000019}" srcId="2" type="presOf" modelId="26"/>
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" destOrd="0" srcOrd="0" destId="{00000000-0000-0000-0000-000000000010}" srcId="1" type="presOf" modelId="27"/>
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" destOrd="0" srcOrd="0" destId="{00000000-0000-0000-0000-000000000017}" srcId="2" type="presOf" modelId="28"/>
        <dgm:cxn sibTransId="4" parTransId="3" destOrd="0" srcOrd="0" destId="1" srcId="0" modelId="29"/>
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" destOrd="0" srcOrd="0" destId="{00000000-0000-0000-0000-000000000007}" srcId="0" type="presOf" modelId="30"/>
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" destOrd="0" srcOrd="0" destId="{00000000-0000-0000-0000-000000000008}" srcId="{00000000-0000-0000-0000-000000000007}" type="presParOf" modelId="31"/>
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" destOrd="0" srcOrd="0" destId="{00000000-0000-0000-0000-000000000009}" srcId="{00000000-0000-0000-0000-000000000008}" type="presParOf" modelId="32"/>
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" destOrd="0" srcOrd="0" destId="{00000000-0000-0000-0000-000000000010}" srcId="{00000000-0000-0000-0000-000000000009}" type="presParOf" modelId="33"/>
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" destOrd="0" srcOrd="1" destId="{00000000-0000-0000-0000-000000000011}" srcId="{00000000-0000-0000-0000-000000000009}" type="presParOf" modelId="34"/>
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" destOrd="0" srcOrd="2" destId="{00000000-0000-0000-0000-000000000012}" srcId="{00000000-0000-0000-0000-000000000009}" type="presParOf" modelId="35"/>
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" destOrd="0" srcOrd="1" destId="{00000000-0000-0000-0000-000000000013}" srcId="{00000000-0000-0000-0000-000000000008}" type="presParOf" modelId="36"/>
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" destOrd="0" srcOrd="0" destId="{00000000-0000-0000-0000-000000000014}" srcId="{00000000-0000-0000-0000-000000000013}" type="presParOf" modelId="37"/>
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" destOrd="0" srcOrd="1" destId="{00000000-0000-0000-0000-000000000015}" srcId="{00000000-0000-0000-0000-000000000013}" type="presParOf" modelId="38"/>
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" destOrd="0" srcOrd="0" destId="{00000000-0000-0000-0000-000000000016}" srcId="{00000000-0000-0000-0000-000000000015}" type="presParOf" modelId="39"/>
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" destOrd="0" srcOrd="0" destId="{00000000-0000-0000-0000-000000000017}" srcId="{00000000-0000-0000-0000-000000000016}" type="presParOf" modelId="40"/>
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" destOrd="0" srcOrd="1" destId="{00000000-0000-0000-0000-000000000018}" srcId="{00000000-0000-0000-0000-000000000016}" type="presParOf" modelId="41"/>
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" destOrd="0" srcOrd="2" destId="{00000000-0000-0000-0000-000000000019}" srcId="{00000000-0000-0000-0000-000000000016}" type="presParOf" modelId="42"/>
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" destOrd="0" srcOrd="1" destId="{00000000-0000-0000-0000-000000000020}" srcId="{00000000-0000-0000-0000-000000000015}" type="presParOf" modelId="43"/>
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" destOrd="0" srcOrd="2" destId="{00000000-0000-0000-0000-000000000021}" srcId="{00000000-0000-0000-0000-000000000015}" type="presParOf" modelId="44"/>
        <dgm:cxn presId="urn:microsoft.com/office/officeart/2005/8/layout/pictureOrgChart" destOrd="0" srcOrd="2" destId="{00000000-0000-0000-0000-000000000022}" srcId="{00000000-0000-0000-0000-000000000008}" type="presParOf" modelId="45"/>
-->
  
  
</xsl:stylesheet>
