<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  	xmlns:dfx="http://www.topologi.com/2005/Diff-X"
    xmlns:del="http://www.topologi.com/2005/Diff-X/Delete"
    xmlns:ins="http://www.topologi.com/2005/Diff-X"
    xmlns:pkg="http://schemas.microsoft.com/office/2006/xmlPackage"
    xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
 	xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"    
    xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
    xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture"
	xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"    
	xmlns:java="http://xml.apache.org/xalan/java"    
  xmlns:xml="http://www.w3.org/XML/1998/namespace"
  xmlns:xalan="http://xml.apache.org/xalan"
                
  version="1.0" 
        exclude-result-prefixes="java">	
  

  <!-- 
  *  Copyright 2007, Plutext Pty Ltd.
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
  -->

  <xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" 
  indent="yes" xalan:indent-amount="4" />  
  
<xsl:param name="Differencer"/>  
<xsl:param name="author"/>
<xsl:param name="date"/>
	<!--  NB: do not set date to an empty string,
		  or 
		  
java.lang.IllegalArgumentException: 
	at com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl$Parser.parseAndSetYear(XMLGregorianCalendarImpl.java:2850)
	at com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl$Parser.parse(XMLGregorianCalendarImpl.java:2737)
	at com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl.<init>(XMLGregorianCalendarImpl.java:433)
	at com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl.newXMLGregorianCalendar(DatatypeFactoryImpl.java:230)
	at com.sun.xml.bind.v2.model.impl.RuntimeBuiltinLeafInfoImpl$13.parse(RuntimeBuiltinLeafInfoImpl.java:546)		  
		  
		   -->
		   
<xsl:param name="docPartRelsLeft"/>
<xsl:param name="docPartRelsRight"/>
<xsl:param name="relsDiffIdentifier"/>
		   

<xsl:preserve-space elements="ins del w:t"/> 

  <xsl:template match="/ | @*|node()">
    <xsl:choose>
        <xsl:when test="parent::w:r"> <!--  handle eg w:br -->
            <w:r>
                <xsl:copy>
                  <xsl:apply-templates select="@*|node()"/>
                </xsl:copy>
            </w:r>
        </xsl:when>
        <xsl:otherwise>
            <xsl:copy>
              <xsl:apply-templates select="@*|node()"/>
            </xsl:copy>
        </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- do we really want to do this???? -->
  <xsl:template match="@del:val">
  	<xsl:attribute name="w:val"><xsl:value-of select="."/></xsl:attribute>
  </xsl:template>

  <xsl:template match="@del:*"/>

<!-- 
  <xsl:template match="*[@dfx:insert='true']" >
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
    
  </xsl:template>
 -->
   
  
  <!--  
  
  	In the case of <w:p dfx:delete="true">, 
  	there are 3 distinct cases we need to handle:
  	
  	The first is simple.  It occurs when a paragraph has been
  	deleted.  For that we want to end up with:
  	
  		<w:p>
  			<w:pPr>
  				<w:rPr><w:del ...> <~~~~~~~
  				
  			<w:r><w:delText>....
  			
  	The second is more complex.  It occurs
  	when diffing two paragraphs results in:
  	
  		<w:p dfx:insert="true">
  			<w:p dfx:delete="true">
  			 
  
    The third is the converse of the second.
    I haven't seen it in a real document, but ...
    
  
  -->
  <xsl:template match="w:p[@dfx:delete and not(parent::w:p)]">
  	<!--  We need a paragraph in the document, 
  	      to contain the deleted content. -->
  	<xsl:comment>Handling simple deleted w:p</xsl:comment>
  	<w:p>
    	<xsl:apply-templates/>
    </w:p>
  </xsl:template>
  
  <xsl:template match="w:p[@dfx:delete and parent::w:p]">
    <!-- Drop this element, since there is a suitable
    	 parent p in the document to contain the
    	 content; 
    	 retain children
         unless they to have the dfx:delete attribute -->
  	<xsl:comment>Handling diffx nested w:p</xsl:comment>
    <xsl:apply-templates/>
  </xsl:template>
  
  <!--  Just in case? -->
  <xsl:template match="w:p[@dfx:insert and parent::w:p]">
    <!-- Drop this element, since there is a suitable
    	 parent p in the document to contain the
    	 content -->
  	<xsl:comment>Handling diffx nested w:p case3</xsl:comment>
    <xsl:apply-templates/>
  </xsl:template>

  
  <xsl:template match="@dfx:insert" />
  
  
  <xsl:template match="*[@dfx:delete and not(self::w:p)]">
    <!-- Drop this element, but retain its children
         unless they to have the dfx:delete attribute.
         
         A consequence of this is that images will 
         disappear without trace?
         
  		<w:p dfx:delete="true">
  			<w:r dfx:delete="true">
  				<w:rPr dfx:delete="true"><w:noProof dfx:delete="true" /></w:rPr>
  				<w:drawing dfx:delete="true"><wp:inline dfx:delete="true" del:distB="0" del:distL="0" del:distR="0" del:distT="0"><wp:extent dfx:delete="true" del:cx="3143250" del:cy="2286000" /><wp:effectExtent dfx:delete="true" del:b="0" del:l="19050" del:r="0" del:t="0" /><wp:docPr dfx:delete="true" del:descr="http://venturebeat.com/wp-content/uploads/2009/01/imvu.jpg" del:id="2" del:name="Picture 1" /><wp:cNvGraphicFramePr dfx:delete="true"><a:graphicFrameLocks dfx:delete="true" del:noChangeAspect="1" /></wp:cNvGraphicFramePr><a:graphic dfx:delete="true"><a:graphicData dfx:delete="true" del:uri="http://schemas.openxmlformats.org/drawingml/2006/picture"><pic:pic dfx:delete="true"><pic:nvPicPr dfx:delete="true"><pic:cNvPr dfx:delete="true" del:descr="http://venturebeat.com/wp-content/uploads/2009/01/imvu.jpg" del:id="0" del:name="Picture 1" /><pic:cNvPicPr dfx:delete="true"><a:picLocks dfx:delete="true" del:noChangeArrowheads="1" del:noChangeAspect="1" /></pic:cNvPicPr></pic:nvPicPr><pic:blipFill dfx:delete="true"><a:blip dfx:delete="true" del:link="rId4" /><a:srcRect dfx:delete="true" /><a:stretch dfx:delete="true"><a:fillRect dfx:delete="true" /></a:stretch></pic:blipFill><pic:spPr dfx:delete="true" del:bwMode="auto"><a:xfrm dfx:delete="true"><a:off dfx:delete="true" del:x="0" del:y="0" /><a:ext dfx:delete="true" del:cx="3143250" del:cy="2286000" /></a:xfrm><a:prstGeom dfx:delete="true" del:prst="rect"><a:avLst dfx:delete="true" /></a:prstGeom><a:noFill dfx:delete="true" /><a:ln dfx:delete="true" del:w="9525"><a:noFill dfx:delete="true" /><a:miter dfx:delete="true" del:lim="800000" /><a:headEnd dfx:delete="true" /><a:tailEnd dfx:delete="true" /></a:ln></pic:spPr></pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing>
  			</w:r>
  		</w:p>
         
         
          -->
    <xsl:apply-templates/>
  </xsl:template>

    

  <xsl:template match="w:rPr[@dfx:delete='true'] " mode="omitDeletions">
  	<xsl:comment>rPr had @dfx:delete='true', so left out.</xsl:comment>
  </xsl:template>

  <xsl:template match="w:rPr[not(@dfx:delete='true')] " mode="omitDeletions">
    <!--  Handle eg
       
       <w:rPr dfx:insert="true"><w:b dfx:insert="true" /></w:rPr><
    
    -->
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>


  <xsl:template match="w:r">
  
    <xsl:for-each select="*">
    
        <xsl:choose>
            <xsl:when test="self::w:t and count(dfx:del)>0">
                <!-- handle: <w:t>This is sample <dfx:del>doc</dfx:del></w:t>  -->
                <xsl:apply-templates select="." mode="splitText" />
            </xsl:when>
            <xsl:when test="self::w:rPr" />
            <xsl:otherwise>
                <xsl:apply-templates select="." />
            </xsl:otherwise>
        
        </xsl:choose>
    
    </xsl:for-each>

      <!-- NB: 
           <xsl:apply-templates select="w:t|w:tab|w:drawing|w:commentReference|w:sym|w:footnoteReference|w:endnoteReference|w:pPr"/> 
      		1.  note w:pPr (!), that's required because diffX might create
      		
				    <w:r dfx:insert="true">
				      <w:pPr dfx:delete="true">
				        <w:drawing dfx:insert="true">      		
      		
      		2. this XSLT drops run content other than these. What else to keep? -->

  </xsl:template>

  <!-- handle: <w:t>This is sample <dfx:del>doc</dfx:del></w:t>  -->
  <xsl:template match="w:t" mode="splitText">
  
    <xsl:for-each select="node()">
    
        <xsl:choose>
            <xsl:when test="self::dfx:del">
                <xsl:variable name="id" 
                            select="java:org.docx4j.diff.Differencer.getId()" />
                <w:del w:id="{$id}" w:author="{$author}" w:date="{$date}">  <!--  w:date is optional -->
                    <w:r>
                        <xsl:copy-of select="../../w:rPr" />
                        <w:delText>
                            <xsl:copy-of select="../@xml:space"></xsl:copy-of>
                            <xsl:value-of select="."/>
                        </w:delText>
                    </w:r>
                </w:del>
            
            </xsl:when>
            <xsl:when test="self::dfx:ins">
                <xsl:variable name="id" 
                            select="java:org.docx4j.diff.Differencer.getId()" />
                <w:ins w:id="{$id}" w:author="{$author}" w:date="{$date}">  <!--  w:date is optional -->
                    <w:r>
                        <xsl:apply-templates select="../../w:rPr" mode="omitDeletions"/>
                        <w:t>
                            <xsl:copy-of select="../@xml:space"></xsl:copy-of>
                            <xsl:value-of select="."/>
                        </w:t>
                    </w:r>
                </w:ins>
            
            </xsl:when>
            <xsl:otherwise>
                    <w:r>
                        <xsl:copy-of select="../../w:rPr" />
                        <w:t>
                            <xsl:copy-of select="../@xml:space"></xsl:copy-of>
                            <xsl:value-of select="."/>
                        </w:t>
                    </w:r>
            </xsl:otherwise>
        
        </xsl:choose>
    
    </xsl:for-each>
 </xsl:template>

  <xsl:template match="w:t">

       <xsl:apply-templates select="node()"/>

  </xsl:template>
  
  <xsl:template match="w:t[ancestor::dfx:del]">

     <w:delText>
        <xsl:copy-of select="@*" />
        <xsl:value-of select="*"/>     
     </w:delText>

  </xsl:template>

	<xsl:template match="text()[not(ancestor::wp:*)]">

		<xsl:choose>
			<xsl:when test="parent::w:instrText"><xsl:value-of select="." /></xsl:when>
			<xsl:otherwise>
				<w:r>
					<xsl:apply-templates select="../../w:rPr"
						mode="omitDeletions" />
					<w:t xml:space="preserve"><xsl:value-of select="." /></w:t>
				</w:r>

			</xsl:otherwise>
		</xsl:choose>
    
  </xsl:template>




  <!-- Handle  <w:sym w:font="Wingdings" w:char="F04A" /> -->
  <xsl:template match="w:sym">
    <w:r>
      <xsl:apply-templates select="../../w:rPr" mode="omitDeletions"/>
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
    </w:r>
  </xsl:template>
  
  <xsl:template match="w:sectPr">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
  </xsl:template>
  
  <!-- Drop these.
       If you want them, you'll need to attend to their r:id, using
       java:org.docx4j.diff.Differencer.registerRelationship  
       (see example below) -->
  <xsl:template match="w:headerReference" />
  <xsl:template match="w:footerReference" />

  <!--  w:drawing: there are 3 cases:
  
        (1) drawing deleted (ie present in RHS only)        
        (2) drawing, inserted in LHS
        
        (3) normal drawing, present in LHS & RHS
  
    -->
        
  <xsl:template match="w:drawing" priority="3">
  
	<xsl:variable name="logdummy" 
		select="java:org.docx4j.diff.Differencer.log('in my w:drawing template')" /> 

  	<xsl:choose>
  		<xsl:when test="@dfx:delete='true'">
			<xsl:variable name="id" 
						select="java:org.docx4j.diff.Differencer.getId()" />
		    <w:del w:id="{$id}" w:author="{$author}" w:date="{$date}">  <!--  w:date is optional -->
		      <w:r>
			      <xsl:copy>
			        <xsl:apply-templates select="node()"/> <!--  drop @ -->
			      </xsl:copy>
		      </w:r>
		    </w:del>    		
  		</xsl:when>
  		<xsl:when test="@dfx:insert='true'">
			<xsl:variable name="id" 
						select="java:org.docx4j.diff.Differencer.getId()" />
		    <w:ins w:id="{$id}" w:author="{$author}" w:date="{$date}">  <!--  w:date is optional -->
		      <w:r>
			      <xsl:copy>
			        <xsl:apply-templates select="node()"/> <!--  drop @ -->
			      </xsl:copy>
		      </w:r>
		    </w:ins>    		  		
  		</xsl:when>
  		<xsl:otherwise>
		      <w:r>
			      <xsl:copy>
			        <xsl:apply-templates select="node()"/> <!--  drop @, though there shouldn't be any -->
			      </xsl:copy>
		      </w:r>
  		</xsl:otherwise>
	</xsl:choose>  		
  
  </xsl:template>
    
    
    <xsl:template match="a:blip" priority="5">
    
    	<xsl:choose>
		    <!--  case (1) drawing deleted (ie present in RHS only)        -->
    		<xsl:when test="@dfx:delete='true'">
    			<!--  Handle link|embed -->
    			<xsl:choose>
    				<xsl:when test="count(@del:link)=1">
    					<xsl:variable name="oldid" select="string(@del:link)" />
    					<xsl:variable name="newid" select="concat($oldid, 'R', $relsDiffIdentifier)" /> <!--  From RIGHT rels -->
    					<xsl:variable name="dummy" 
    					     select="java:org.docx4j.diff.Differencer.registerRelationship(
    					     	$Differencer, $docPartRelsRight, $oldid, $newid)" />
    					<a:blip r:link="{$newid}" />
    				</xsl:when>
    				<xsl:otherwise> <!--  r:embed -->
    					<xsl:variable name="oldid" select="string(@del:embed)" />
    					<xsl:variable name="newid" select="concat($oldid, 'R', $relsDiffIdentifier)" /> <!--  From RIGHT rels -->
    					<xsl:variable name="dummy" 
    					     select="java:org.docx4j.diff.Differencer.registerRelationship(
    					     	$Differencer, $docPartRelsRight, $oldid, $newid)" />
    					<a:blip r:embed="{$newid}" />    				
    				</xsl:otherwise>
    			</xsl:choose>    		
    		</xsl:when>
		  <!--  cases:
		        (2) drawing, inserted in LHS
		        (3) normal drawing, present in LHS & RHS  
		    -->
			<xsl:otherwise>
    			<!--  Handle link|embed -->
    			
				<xsl:variable name="logdummy" 
					select="java:org.docx4j.diff.Differencer.log('in a:blip, case 2 and 3')" /> 
    			
    			<xsl:choose>
    				<xsl:when test="count(@r:link)=1">
    					<xsl:variable name="oldid" select="string(@r:link)" />
    					<xsl:variable name="newid" select="concat($oldid, 'L', $relsDiffIdentifier)" /> <!--  LEFT -->
    					<xsl:variable name="dummy" 
    					     select="java:org.docx4j.diff.Differencer.registerRelationship(
    					     	$Differencer, $docPartRelsLeft, $oldid, $newid)" />
    					<a:blip r:link="{$newid}" />
    				</xsl:when>
    				<xsl:otherwise> <!--  r:embed -->
    					<xsl:variable name="oldid" select="string(@r:embed)" />
    					<xsl:variable name="newid" select="concat($oldid, 'L', $relsDiffIdentifier)" /> <!--  LEFT -->
    					<xsl:variable name="dummy" 
    					     select="java:org.docx4j.diff.Differencer.registerRelationship(
    					     	$Differencer, $docPartRelsLeft, $oldid, $newid)" />
    					<a:blip r:embed="{$newid}" />    				
    				</xsl:otherwise>
    			</xsl:choose>    					
			</xsl:otherwise>
		</xsl:choose>    
    
    </xsl:template>

	<!--  Recover deleted drawing 
	
		<w:drawing dfx:delete="true">
		    <wp:inline dfx:delete="true">
		        <wp:extent dfx:delete="true" del:cx="1533525" del:cy="1000125" />
		        <wp:effectExtent dfx:delete="true" del:b="0" del:l="19050" del:r="9525" del:t="0" />
		        <wp:docPr dfx:delete="true" del:id="2" del:name="Picture 1" />
		        <wp:cNvGraphicFramePr dfx:delete="true">
		            <a:graphicFrameLocks dfx:delete="true" del:noChangeAspect="true" />
		        </wp:cNvGraphicFramePr>
		        <a:graphic dfx:delete="true">
		            <a:graphicData dfx:delete="true" del:uri="http://schemas.openxmlformats.org/drawingml/2006/picture">
		                <pic:pic dfx:delete="true">
		                    <pic:nvPicPr dfx:delete="true">
		                        <pic:cNvPr dfx:delete="true" del:id="0" del:name="Picture 1" />
		                        <pic:cNvPicPr dfx:delete="true">
		                            <a:picLocks dfx:delete="true" del:noChangeArrowheads="true" del:noChangeAspect="true" />
		                        </pic:cNvPicPr>
		                    </pic:nvPicPr>
		                    <pic:blipFill dfx:delete="true">
		                        <a:blip dfx:delete="true" del:link="rId4" />
		                        <a:srcRect dfx:delete="true" />
		                        <a:stretch dfx:delete="true">
		                            <a:fillRect dfx:delete="true" />
		                        </a:stretch>
		                    </pic:blipFill>
		                    <pic:spPr dfx:delete="true" del:bwMode="auto">
		                        <a:xfrm dfx:delete="true">
		                            <a:off dfx:delete="true" del:x="0" del:y="0" />
		                            <a:ext dfx:delete="true" del:cx="1533525" del:cy="1000125" />
		                        </a:xfrm>
		                        <a:prstGeom dfx:delete="true" del:prst="rect">
		                            <a:avLst dfx:delete="true" />
		                        </a:prstGeom>
		                        <a:noFill dfx:delete="true" />
		                        <a:ln dfx:delete="true" del:w="9525">
		                            <a:noFill dfx:delete="true" />
		                            <a:miter dfx:delete="true" del:lim="800000" />
		                            <a:headEnd dfx:delete="true" />
		                            <a:tailEnd dfx:delete="true" />
		                        </a:ln>
		                    </pic:spPr>
		                </pic:pic>
		            </a:graphicData>
		        </a:graphic>
		    </wp:inline>
		</w:drawing>	
	-->
	
	<xsl:template match="@dfx:delete[ancestor::w:drawing]" priority="5"/>
    
    <!--  all the deleted attributes are just in the default namespace -->
	<xsl:template match="@del:*[ancestor::w:drawing]"  priority="5">
		<xsl:attribute name="{local-name(.)}">
			<xsl:value-of select="." />
		</xsl:attribute>	
	</xsl:template>
    
	<xsl:template match="*[@dfx:delete and ancestor::w:drawing]" priority="4">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
	</xsl:template>

	<!--  Fix inserted drawing?  No, nothing to do here, since @dfx:insert="true"
	      is removed above.   
	
		<w:r dfx:insert="true"><w:rPr dfx:insert="true"><w:noProof dfx:insert="true" />
		    </w:rPr>
		    <w:drawing dfx:insert="true">
		        <wp:inline dfx:insert="true">
		            <wp:extent dfx:insert="true" cx="457200" cy="400050" />
		            <wp:effectExtent dfx:insert="true" b="0" l="19050" r="0" t="0" />
		            <wp:docPr dfx:insert="true" id="5" name="Picture 3" />
		            <wp:cNvGraphicFramePr dfx:insert="true">
		                <a:graphicFrameLocks dfx:insert="true" noChangeAspect="true" />
		            </wp:cNvGraphicFramePr>
		            <a:graphic dfx:insert="true">
		                <a:graphicData dfx:insert="true" uri="http://schemas.openxmlformats.org/drawingml/2006/picture">
		                    <pic:pic dfx:insert="true">
		                        <pic:nvPicPr dfx:insert="true">
		                            <pic:cNvPr dfx:insert="true" id="0" name="Picture 3" />
		                            <pic:cNvPicPr dfx:insert="true">
		                                <a:picLocks dfx:insert="true" noChangeArrowheads="true" noChangeAspect="true" />
		                            </pic:cNvPicPr>
		                        </pic:nvPicPr>
		                        <pic:blipFill dfx:insert="true">
		                            <a:blip dfx:insert="true" r:link="rId6" />
		                            <a:srcRect dfx:insert="true" />
		                            <a:stretch dfx:insert="true">
		                                <a:fillRect dfx:insert="true" />
		                            </a:stretch>
		                        </pic:blipFill>
		                        <pic:spPr dfx:insert="true" bwMode="auto">
		                            <a:xfrm dfx:insert="true">
		                                <a:off dfx:insert="true" x="0" y="0" />
		                                <a:ext dfx:insert="true" cx="457200" cy="400050" />
		                            </a:xfrm>
		                            <a:prstGeom dfx:insert="true" prst="rect">
		                                <a:avLst dfx:insert="true" />
		                            </a:prstGeom>
		                            <a:noFill dfx:insert="true" />
		                            <a:ln dfx:insert="true" w="9525">
		                                <a:noFill dfx:insert="true" />
		                                <a:miter dfx:insert="true" lim="800000" />
		                                <a:headEnd dfx:insert="true" />
		                                <a:tailEnd dfx:insert="true" />
		                            </a:ln>
		                        </pic:spPr>
		                    </pic:pic>
		                </a:graphicData>
		            </a:graphic>
		        </wp:inline>
		    </w:drawing>
		</w:r>	
		
	-->

  <!--  w:hyperlink            @r:id  
  
	  <w:hyperlink dfx:insert="true" r:id="rId5" w:history="true">
	  	<w:r>
	  		<w:rPr dfx:insert="true"><w:rStyle dfx:insert="true" w:val="Hyperlink" /></w:rPr>
	  		<w:t>
	  			<ins>http://slashdot.org</ins>
	  			<del>3</del>
	  		</w:t>
	  	</w:r>
	  </w:hyperlink>
	  
	  Word 2007 tracks the insertion/deletion of hyperlinks using 
	  w:ins and w:del around the corresponding fields.
	  
	  We could replicate that, I guess.
	  
	  But for now, *we don't track the hyperlink itself*; just the text inside it. 
	  	    
  
  -->
  <xsl:template match="w:hyperlink" priority="5">
  
  	<xsl:choose>
  		<xsl:when test="@dfx:delete='true'">
			<xsl:variable name="id" 
						select="java:org.docx4j.diff.Differencer.getId()" />
		    
				<xsl:variable name="oldid" select="string(@del:id)" />
				<xsl:variable name="newid" select="concat($oldid, 'R', $relsDiffIdentifier)" /> <!--  From RIGHT rels -->
				<xsl:variable name="dummy" 
				     select="java:org.docx4j.diff.Differencer.registerRelationship(
				     	$Differencer, $docPartRelsRight, $oldid, $newid)" />
				<w:hyperlink r:id="{$newid}">
			    	<xsl:apply-templates select="@*|node()"/>
				</w:hyperlink>
  		</xsl:when>
  		<xsl:when test="@dfx:insert='true'">
			<xsl:variable name="id" 
						select="java:org.docx4j.diff.Differencer.getId()" />
				<xsl:variable name="oldid" select="string(@r:id)" />
				<xsl:variable name="newid" select="concat($oldid, 'L', $relsDiffIdentifier)" /> <!--  LEFT -->
				<xsl:variable name="dummy" 
				     select="java:org.docx4j.diff.Differencer.registerRelationship(
				     	$Differencer, $docPartRelsLeft, $oldid, $newid)" />
				<w:hyperlink r:id="{$newid}">
			    	<xsl:apply-templates select="@*|node()"/>
				</w:hyperlink>
  		</xsl:when>
  		<xsl:otherwise>
				<xsl:variable name="oldid" select="string(@r:id)" />
				<xsl:variable name="newid" select="concat($oldid, 'L', $relsDiffIdentifier)" /> <!--  LEFT -->
				<xsl:variable name="dummy" 
				     select="java:org.docx4j.diff.Differencer.registerRelationship(
				     	$Differencer, $docPartRelsLeft, $oldid, $newid)" />
				<w:hyperlink r:id="{$newid}">
			    	<xsl:apply-templates select="@*|node()"/>
				</w:hyperlink>
  		</xsl:otherwise>
	</xsl:choose>  		
  
  </xsl:template>
  
  <!--  
    TODO        
        w:object/v:imagedata
        
        w:object/o:OLEObject  
    
  -->

  <!--  comments, footnotes, endnotes are simply stripped.
        Handling these properly requires composing new parts,
        for which an extension function similar to
        registerRelationship would be required.
        
        Quite feasible, but a TODO. -->
  <xsl:template match="w:commentReference | w:commentRangeStart | w:commentRangeEnd" />

  <xsl:template match="w:footnoteReference | w:endnoteReference" />



  <xsl:template match="w:tab[parent::w:r]"> <!-- so we don't match tab in properties -->

    <w:r>
      <xsl:apply-templates select="../w:rPr" mode="omitDeletions"/>
      <w:tab/> 
    </w:r>

  </xsl:template>


  <xsl:template match="dfx:ins|ins" > <!--  2018 Feb: both these occur! -->
    <xsl:variable name="id" 
                select="java:org.docx4j.diff.Differencer.getId()" />
    <w:ins w:id="{$id}" w:author="{$author}" w:date="{$date}">  <!--  w:date is optional -->
          <xsl:apply-templates select="@*|node()"/>
        <!--   
              <w:r>
                <xsl:apply-templates select="../../w:rPr" mode="omitDeletions"/>
                <w:t xml:space="preserve"><xsl:value-of select="."/></w:t>
              </w:r>
         -->
    </w:ins>
    
  </xsl:template>

  <xsl:template match="dfx:del|del" > <!--  2018 Feb: both these occur! -->
    <xsl:variable name="id" 
                select="java:org.docx4j.diff.Differencer.getId()" />
    <w:del w:id="{$id}" w:author="{$author}" w:date="{$date}">  <!--  w:date is optional -->
          <xsl:apply-templates select="@*|node()"/>
        <!--   
          <w:r>
          	<xsl:apply-templates select="../../w:rPr"/>
            <w:delText><xsl:value-of select="."/></w:delText>
          </w:r>
         -->
    </w:del>
    
  </xsl:template>



<!--  special case where diffx picks up a delete and insert as changes 
      to a single w:drawing  
      
      A new w:drawing inserted next to an existing w:drawing seems to
      confuse diffx, leading it to produce something which
      includes:
      
									
									<a:prstGeom dfx:insert="true"
										prst="rect">
										<a:avLst dfx:insert="true" />
									</a:prstGeom>
									<a:noFill dfx:insert="true" />
									<a:ln dfx:insert="true" w="9525">
										<a:noFill dfx:insert="true" />
										<a:miter dfx:insert="true"
											lim="800000" />
										<a:headEnd dfx:insert="true" />
										<a:tailEnd dfx:insert="true" />
									</a:ln>
									
									
									<a:prstGeom dfx:delete="true"
										del:prst="rect">
										<a:avLst dfx:delete="true" />
									</a:prstGeom>
									<a:noFill dfx:delete="true" />
									<a:ln dfx:delete="true"
										del:w="9525">
										<a:noFill dfx:delete="true" />
										<a:miter dfx:delete="true"
											del:lim="800000" />
										<a:headEnd dfx:delete="true" />
										<a:tailEnd dfx:delete="true" />
									</a:ln>

		WORKAROUND: In an effort to avoid using this template on something which
		includes that, instead of 
		
			match="w:drawing[not(@dfx:delete='true') and not(@dfx:insert='true') and 
						(descendant::*/@del:* or descendant::*/@dfx:*)] "
						
		require descendant::wp:docPr to have @del:*.
		
		NB also, there is a strange problem in which templates of mode deletedimage,
		insertedimage don't seem to be doing the right thing with 
		attributes if @dfx:insert="true" or @dfx:delete="true" is true???
		The WORKAROUND hides this...
		
      
      -->      
<xsl:template match="w:drawing[not(@dfx:delete='true') and not(@dfx:insert='true') and 
			(descendant::wp:docPr/@del:*)] " priority="4">
			
	<xsl:variable name="logdummy" 
		select="java:org.docx4j.diff.Differencer.log('Special case .. bifurcating w:drawing diff')" /> 
			
	<xsl:variable name="id" 
						select="java:org.docx4j.diff.Differencer.getId()" />
			<!--  a copy of it deleted -->
		    <w:del w:id="{$id}" w:author="{$author}" w:date="{$date}">  <!--  w:date is optional -->
		      <w:r>
			      <xsl:copy>
			        <xsl:apply-templates select="node()" mode="deletedimage"/> <!--  drop @ -->
			      </xsl:copy>
		      </w:r>
		    </w:del>    		
	
			<!--  a copy of it inserted -->
			<xsl:variable name="id2" 
						select="java:org.docx4j.diff.Differencer.getId()" />
		    <w:ins w:id="{$id2}" w:author="{$author}" w:date="{$date}">  <!--  w:date is optional -->
		      <w:r>
			      <xsl:copy>
			        <xsl:apply-templates select="node()" mode="insertedimage" /> <!--  drop @ -->
			      </xsl:copy>
		      </w:r>
		    </w:ins>    		  		

</xsl:template>

  <xsl:template match="@*|node()" mode="deletedimage">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="deletedimage"/>
    </xsl:copy>
  </xsl:template>
  
  <!--  WORKAROUND 2 .. this is currently untested.  To test it, 
        you have to disable Workaround 1, then test on a document
        in which a new image is inserted next to an existing one.  -->
  
	    <!--  If there is an unexpected mixture of delete and insert,
	          in deletedimage mode, lets try keeping the deleted bit
	          (as opposed to the inserted bit?) -->  
		<xsl:template match="@dfx:delete" priority="5" mode="deletedimage"/>    
		<xsl:template match="@del:*"  priority="5" mode="deletedimage">
			<xsl:attribute name="{local-name(.)}">
				<xsl:value-of select="." />
			</xsl:attribute>	
		</xsl:template>
		<xsl:template match="*[@dfx:insert]" priority="5" mode="deletedimage" />	
		    
	    <!--  If there is an unexpected mixture of delete and insert,
	          in insertedimage mode, lets try keeping the inserted bit
	          (as opposed to the deleted bit?) -->  
		<xsl:template match="@dfx:insert" priority="5" mode="insertedimage"/>    
		<xsl:template match="*[@dfx:delete]" priority="5" mode="insertedimage" />	
    
  <!-- end WORKAROUND 2 .. this is untested -->
  

  <xsl:template match="@*|node()" mode="insertedimage">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="insertedimage"/>
    </xsl:copy>
  </xsl:template>

    <xsl:template match="wp:extent|a:ext"  mode="deletedimage">
    
    	<xsl:variable name="cx">
    		<xsl:choose>
    			<xsl:when test="@del:cx">
    				<xsl:value-of select="string(@del:cx)"/>
    			</xsl:when>
    			<xsl:otherwise>
    				<xsl:value-of select="string(@cx)"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		    			
    	<xsl:variable name="cy">
    		<xsl:choose>
    			<xsl:when test="@del:cy">
    				<xsl:value-of select="string(@del:cy)"/>
    			</xsl:when>
    			<xsl:otherwise>
    				<xsl:value-of select="string(@cy)"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:choose>
			<xsl:when test="self::wp:extent">
				<wp:extent cx="{$cx}" cy="{$cy}" />
			</xsl:when>		
			<xsl:when test="self::a:ext">
				<a:ext cx="{$cx}" cy="{$cy}" />
			</xsl:when>		
		</xsl:choose>	

</xsl:template>

<!--  

 <wp:docPr id="3" name="Picture 3" del:id="1" del:name="Picture 1" /> 

 <pic:cNvPr id="0" name="Picture 3" del:name="Picture 1" />

-->
    <xsl:template match="wp:docPr|pic:cNvPr"  mode="deletedimage">
    
    	<xsl:variable name="id">
    		<xsl:choose>
    			<xsl:when test="@del:id">
    				<xsl:value-of select="string(@del:id)"/>
    			</xsl:when>
    			<xsl:otherwise>
    				<xsl:value-of select="string(@id)"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		    			
    	<xsl:variable name="name">
    		<xsl:choose>
    			<xsl:when test="@del:name">
    				<xsl:value-of select="string(@del:name)"/>
    			</xsl:when>
    			<xsl:otherwise>
    				<xsl:value-of select="string(@name)"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:choose>
			<xsl:when test="self::wp:docPr">
				<wp:docPr id="{$id}" name="{$name}" />
			</xsl:when>		
			<xsl:when test="self::pic:cNvPr">
				<pic:cNvPr id="{$id}" name="{$name}" />
			</xsl:when>		
		</xsl:choose>	

</xsl:template>


    <!--  xsl:template match="wp:extent"  mode="insertedimage"
    
    		nothing special needed for this
    
    -->
  
  <!--  TODO: consolidate the following 2 templates to call a single named template,
        and pass in parameters LorR, and $docpartRels -->

    <xsl:template match="a:blip"  mode="deletedimage">
    
  			<xsl:choose>
  				<xsl:when test="count(@del:link)=1">
  					<xsl:variable name="oldid" select="string(@del:link)" />
  					<xsl:variable name="newid" select="concat($oldid, 'R', $relsDiffIdentifier)" /> <!--  From RIGHT rels -->
  					<xsl:variable name="dummy" 
  					     select="java:org.docx4j.diff.Differencer.registerRelationship(
  					     	$Differencer, $docPartRelsRight, $oldid, $newid)" />
  					<a:blip r:link="{$newid}" />
  				</xsl:when>
  				<xsl:when test="count(@del:link)=1">
  					<xsl:variable name="oldid" select="string(@del:embed)" />
  					<xsl:variable name="newid" select="concat($oldid, 'R', $relsDiffIdentifier)" /> <!--  From RIGHT rels -->
  					<xsl:variable name="dummy" 
  					     select="java:org.docx4j.diff.Differencer.registerRelationship(
  					     	$Differencer, $docPartRelsRight, $oldid, $newid)" />
  					<a:blip r:embed="{$newid}" />    				
  				</xsl:when>
  				<xsl:when test="count(@r:link)=1">
  					<xsl:variable name="oldid" select="string(@r:link)" />
  					<xsl:variable name="newid" select="concat($oldid, 'R', $relsDiffIdentifier)" /> <!--  From RIGHT rels -->
  					<xsl:variable name="dummy" 
  					     select="java:org.docx4j.diff.Differencer.registerRelationship(
  					     	$Differencer, $docPartRelsRight, $oldid, $newid)" />
  					<a:blip r:link="{$newid}" />
  				</xsl:when>
  				<xsl:when test="count(@r:embed)=1">
  					<xsl:variable name="oldid" select="string(@r:embed)" />
  					<xsl:variable name="newid" select="concat($oldid, 'R', $relsDiffIdentifier)" /> <!--  From RIGHT rels -->
  					<xsl:variable name="dummy" 
  					     select="java:org.docx4j.diff.Differencer.registerRelationship(
  					     	$Differencer, $docPartRelsRight, $oldid, $newid)" />
  					<a:blip r:embed="{$newid}" />
  				</xsl:when>
   				<xsl:otherwise> <!--  unexpected -->
   					<xsl:variable name="logdummy" 
						select="java:org.docx4j.diff.Differencer.log('UNEXPECTED - how to handle this a:blip?')" /> 
   				
				    <xsl:copy>
				      <xsl:apply-templates select="@*|node()" /> <!--  revert to normal mode -->
				    </xsl:copy>
   				</xsl:otherwise>
  			</xsl:choose>    		
    
    </xsl:template>

    <xsl:template match="a:blip"  mode="insertedimage">

  			<xsl:choose>
  				<xsl:when test="count(@del:link)=1">
  					<xsl:variable name="oldid" select="string(@del:link)" />
  					<xsl:variable name="newid" select="concat($oldid, 'L', $relsDiffIdentifier)" /> <!--  From RIGHT rels -->
  					<xsl:variable name="dummy" 
  					     select="java:org.docx4j.diff.Differencer.registerRelationship(
  					     	$Differencer, $docPartRelsLeft, $oldid, $newid)" />
  					<a:blip r:link="{$newid}" />
  				</xsl:when>
  				<xsl:when test="count(@del:link)=1">
  					<xsl:variable name="oldid" select="string(@del:embed)" />
  					<xsl:variable name="newid" select="concat($oldid, 'L', $relsDiffIdentifier)" /> <!--  From RIGHT rels -->
  					<xsl:variable name="dummy" 
  					     select="java:org.docx4j.diff.Differencer.registerRelationship(
  					     	$Differencer, $docPartRelsLeft, $oldid, $newid)" />
  					<a:blip r:embed="{$newid}" />    				
  				</xsl:when>
  				<xsl:when test="count(@r:link)=1">
  					<xsl:variable name="oldid" select="string(@r:link)" />
  					<xsl:variable name="newid" select="concat($oldid, 'L', $relsDiffIdentifier)" /> <!--  From RIGHT rels -->
  					<xsl:variable name="dummy" 
  					     select="java:org.docx4j.diff.Differencer.registerRelationship(
  					     	$Differencer, $docPartRelsLeft, $oldid, $newid)" />
  					<a:blip r:link="{$newid}" />
  				</xsl:when>
  				<xsl:when test="count(@r:embed)=1">
  					<xsl:variable name="oldid" select="string(@r:embed)" />
  					<xsl:variable name="newid" select="concat($oldid, 'L', $relsDiffIdentifier)" /> <!--  From RIGHT rels -->
  					<xsl:variable name="dummy" 
  					     select="java:org.docx4j.diff.Differencer.registerRelationship(
  					     	$Differencer, $docPartRelsLeft, $oldid, $newid)" />
  					<a:blip r:embed="{$newid}" />
  				</xsl:when>
   				<xsl:otherwise> <!--  unexpected -->
   					<xsl:variable name="logdummy" 
						select="java:org.docx4j.diff.Differencer.log('UNEXPECTED - how to handle this a:blip?')" /> 
   				
				    <xsl:copy>
				      <xsl:apply-templates select="@*|node()" /> <!--  revert to normal mode -->
				    </xsl:copy>
   				</xsl:otherwise>
  			</xsl:choose>    		
    
    
    </xsl:template>

	<!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
	<!-- ++++++ TABLES +++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
	<!-- +++++++ - deleted  ++++++++++++++++++++++++++++++++++++++++++++++++++ -->
	<!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

	<xsl:template match="w:tbl[@dfx:delete]">
		<!-- Handled at tr level -->
		<w:tbl>
			<xsl:apply-templates mode="deleted-table"/>
		</w:tbl>
	</xsl:template>

	<!--  drop @dfx:delete -->
	<xsl:template match="@dfx:delete" priority="5" mode="deleted-table" />

	<xsl:template match="@*|node()" mode="deleted-table">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" mode="deleted-table" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="@del:*" priority="5" mode="deleted-table">
		<xsl:attribute name="{local-name(.)}" namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
				<xsl:value-of select="." />
			</xsl:attribute>
	</xsl:template>
		    
	<!--  handle case of no w:trPr  -->	    
	<xsl:template match="w:tr[@dfx:delete]" mode="deleted-table">
	<w:tr>
		<xsl:choose>
			<xsl:when test="count(w:trPr)=0">
				<w:trPr>
					<xsl:variable name="id"
						select="java:org.docx4j.diff.Differencer.getId()" />
					<w:del w:id="{$id}" w:author="{$author}" w:date="{$date}" />  <!-- w:date is optional -->
				</w:trPr>
				<xsl:apply-templates mode="deleted-table"/>				
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="deleted-table"/>
			</xsl:otherwise>
		</xsl:choose>
	</w:tr>
	</xsl:template>
	
	<!--  existing w:trPr  -->	    
	<xsl:template match="w:trPr[@dfx:delete]" mode="deleted-table">

			<xsl:variable name="id" 
							select="java:org.docx4j.diff.Differencer.getId()" />
	
			<w:trPr>
				<xsl:apply-templates mode="deleted-table"/>
				
			    <w:del w:id="{$id}" w:author="{$author}" w:date="{$date}" />  <!--  w:date is optional -->
		    </w:trPr>		

	</xsl:template>
	
	<xsl:template match="w:tc[@dfx:delete]"  mode="deleted-table">
		<w:tc>
			<xsl:apply-templates /> <!--  normal mode, for w:p etc -->
		</w:tc>
	</xsl:template>
	
	<xsl:template match="w:tcPr[@dfx:delete]"> <!--  normal mode -->
		<w:tcPr>
			<xsl:apply-templates  mode="deleted-table" /> <!--  swap mode -->
		</w:tcPr>
	</xsl:template>
	
	<!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
	<!-- ++++++ TABLES +++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
	<!-- +++++++ - inserted  ++++++++++++++++++++++++++++++++++++++++++++++++++ -->
	<!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

	<xsl:template match="w:tbl[@dfx:insert]">
		<!-- Handled at tr level -->
		<w:tbl>
			<xsl:apply-templates mode="inserted-table"/>
		</w:tbl>
	</xsl:template>

	<!--  drop @dfx:delete -->
	<xsl:template match="@dfx:insert" priority="5" mode="inserted-table" />

	<xsl:template match="@*|node()" mode="inserted-table">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" mode="inserted-table" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="@ins:*" priority="5" mode="inserted-table" />
		    
	<!--  handle case of no w:trPr  -->	    
	<xsl:template match="w:tr[@dfx:insert]" mode="inserted-table">
	<w:tr>
		<xsl:choose>
			<xsl:when test="count(w:trPr)=0">
				<w:trPr>
					<xsl:variable name="id"
						select="java:org.docx4j.diff.Differencer.getId()" />
					<w:ins w:id="{$id}" w:author="{$author}" w:date="{$date}" />  <!-- w:date is optional -->
				</w:trPr>
				<xsl:apply-templates mode="inserted-table"/>				
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="inserted-table"/>
			</xsl:otherwise>
		</xsl:choose>
	</w:tr>
	</xsl:template>
	
	<!--  existing w:trPr  -->	    
	<xsl:template match="w:trPr[@dfx:insert]" mode="inserted-table">

			<xsl:variable name="id" 
							select="java:org.docx4j.diff.Differencer.getId()" />
	
			<w:trPr>
				<xsl:apply-templates mode="inserted-table"/>
				
			    <w:ins w:id="{$id}" w:author="{$author}" w:date="{$date}" />  <!--  w:date is optional -->
		    </w:trPr>		

	</xsl:template>
	
	<xsl:template match="w:tc[@dfx:insert]"  mode="inserted-table">
		<w:tc>
			<xsl:apply-templates /> <!--  normal mode, for w:p etc -->
		</w:tc>
	</xsl:template>
	
	<xsl:template match="w:tcPr[@dfx:insert]"> <!--  normal mode -->
		<w:tcPr>
			<xsl:apply-templates  mode="inserted-table" /> <!--  swap mode -->
		</w:tcPr>
	</xsl:template>
	


</xsl:stylesheet>