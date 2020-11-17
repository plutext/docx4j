
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
        

<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="yes" />

<xsl:param name="customXmlDataStorageParts"/> <!-- select="'passed in'"-->	
<xsl:param name="wmlPackage"/> <!-- select="'passed in'"-->	
<xsl:param name="sourcePart"/> <!-- select="'passed in'"-->	
<xsl:param name="xPathsMap"/> <!-- select="'passed in'"-->	
<xsl:param name="sequenceCounters"/>
<xsl:param name="bookmarkIdCounter"/>
<xsl:param name="bindingTraverserState"/>  <!--  TODO consolidate some/all above into $bindingTraverserState -->

  <xsl:template match="/ | @*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
  <!-- 
  <xsl:template match="*" mode="myeval">
  	<xsl:param name="expression">1. </xsl:param>
  	<xsl:param name="pos">3</xsl:param>
  	
		<xsl:variable name="dummy"
	select="java:org.docx4j.model.datastorage.BindingHandler.log(concat('$pos ', $pos))" />
	
		<xsl:variable name="dummy"
	select="java:org.docx4j.model.datastorage.BindingHandler.log(concat('position() ', position() ))" />
	
  	<xsl:choose>
  		<xsl:when test="position()=$pos">
			<xsl:variable name="dummy"
		select="java:org.docx4j.model.datastorage.BindingHandler.log(.)" />
	  		<xsl:value-of select="dyn:evaluate($expression)" /></xsl:when>
  		<xsl:otherwise /> 
  	</xsl:choose>
  	
  </xsl:template>
 -->

  <xsl:template match="*" mode="myeval">
  	<xsl:param name="expression">1. </xsl:param>
  	<xsl:param name="pos">3</xsl:param>
  	
		<xsl:variable name="dummy"
	select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.log(concat('$pos ', $pos))" />
	
		<xsl:variable name="dummy2"
	select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.log(concat('position() ', position() ))" />
	
  	<xsl:choose>
  		<xsl:when test="position()=$pos">
	  		<xsl:value-of select="dyn:evaluate($expression)" /></xsl:when>
  		<xsl:otherwise /> 
  	</xsl:choose>
  	
  </xsl:template>
 
 
<!-- docx4j 3.0.  If the w:sdtContent contains an existing w:drawing/wp:inline/a:graphic ..
     reuse it, so any formatting thus configured is used.
     We'll just replace the rId.. -->
  
  <xsl:template match=" @*|node()" mode="picture3">
	<xsl:param name="sdtPr" select="/.."/> 
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"  mode="picture3">
	    	<xsl:with-param name="sdtPr" select="$sdtPr"/>
	    </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  
  <xsl:template match="a:blip" mode="picture3" priority="1">
	<xsl:param name="sdtPr" select="/.."/> 
	<a:blip r:embed="{java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathInjectImageRelId(
								$wmlPackage,
								$sourcePart,
								$customXmlDataStorageParts,
								string($sdtPr/w:dataBinding/@w:storeItemID),
								string($sdtPr/w:dataBinding/@w:xpath),
								string($sdtPr/w:dataBinding/@w:prefixMappings) )}" />
<!--  if it was @r:link, it is now embedded -->
  </xsl:template>

<!-- docx4j 3.0.1.  Handle a rich text control which contains an image.
     Since this doesn't have a w:databinding, we can't just use mode="picture3"
     
     If the w:sdtContent contains an existing w:drawing/wp:inline/a:graphic ..
     reuse it, so any formatting thus configured is used.
     We'll just replace the rId.. -->
  <xsl:template match=" @*|node()" mode="picture3richtext">
	<xsl:param name="tag" select="/.."/> 
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"  mode="picture3richtext">
			    	<xsl:with-param name="tag" select="$tag"/>
	    </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  
  <xsl:template match="a:blip" mode="picture3richtext" priority="1">
	<xsl:param name="tag" select="/.."/> 
	<a:blip r:embed="{java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathInjectImageRelId(
								$wmlPackage,
								$sourcePart,
								$customXmlDataStorageParts,
								$xPathsMap,
								$tag )}" />
								
<!--  if it was @r:link, it is now embedded -->
  </xsl:template>
  
  
  <xsl:template match="w:sdt">  
  
  	<xsl:variable name="tag" select="string(w:sdtPr/w:tag/@w:val)"/>

	<xsl:variable name="parent" select="local-name(ancestor::*[self::w:body or self::w:hdr or self::w:ftr or self::w:p or self::w:r or self::w:tbl or self::w:tr or self::w:tc][1])" />
	<xsl:variable name="child"  select="local-name(descendant::*[self::w:p or self::w:r or self::w:t or self::w:tbl or self::w:tr or self::w:tc][1])" />
  	
  	<xsl:choose>
  	
  		<xsl:when test="contains(string(w:sdtPr/w:tag/@w:val), 'w15:resultRepeatZero')">
			     <xsl:copy-of select="."/>  		
		</xsl:when>  

		<!--  11.1.8 width=n|auto rich text cc containing w:drawing -->
  		<xsl:when test="contains(string(w:sdtPr/w:tag/@w:val), 'od:Handler=picture') and contains(string(w:sdtPr/w:tag/@w:val), 'width=')">
  		
			<xsl:copy>
			     <xsl:copy-of select="w:sdtPr"/>
			     
			     <xsl:if test="w:stdEndPr">
			     	<xsl:copy-of select="w:sdtEndPr"/>
		     	</xsl:if>
	
			     <w:sdtContent>
			     <!--  Let BPAI work out size -->
						<xsl:copy-of
						select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathInjectImage(
									$wmlPackage,
									$sourcePart,
									$customXmlDataStorageParts,
									$xPathsMap,
									$tag,
									$parent,
									$child)" /> 
			     </w:sdtContent>
			     
			</xsl:copy>
				
		</xsl:when>  		

		<!--  3.0.1 rich text cc containing w:drawing -->
  		<xsl:when test="contains(string(w:sdtPr/w:tag/@w:val), 'od:Handler=picture')">
  		
			<xsl:copy>
			     <xsl:copy-of select="w:sdtPr"/>
			     
			     <xsl:if test="w:stdEndPr">
			     	<xsl:copy-of select="w:sdtEndPr"/>
		     	</xsl:if>
	
			    <xsl:apply-templates select="w:sdtContent" mode="picture3richtext"> 
			    	<xsl:with-param name="tag" select="$tag"/>
			    </xsl:apply-templates>
			     
			</xsl:copy>
				
		</xsl:when>  		

  		<xsl:when test="w:sdtPr/w:dataBinding and w:sdtPr/w14:checkbox">
  			<!--  since 3.2.2, honour w:dataBinding -->
  			
			<xsl:copy>
				 <w:sdtPr>
				     <xsl:apply-templates select="w:sdtPr"/>
				 </w:sdtPr>
			     
			     <xsl:if test="w:stdEndPr">
			     	<xsl:copy-of select="w:sdtEndPr"/>
		     	</xsl:if>
			     
			     <w:sdtContent>
					<xsl:choose>
						<xsl:when test="w:sdtContent/w:tc">
							<w:tc>
								<xsl:if test="w:sdtContent/w:tc/w:tcPr">
									<xsl:copy-of select="w:sdtContent/w:tc/w:tcPr"/>
								</xsl:if>
								<!-- xsl:copy-of select="w:sdtContent/w:tc/w:p" /-->
								<xsl:copy-of
								select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.w14Checkbox(
												$wmlPackage,
												$sourcePart,
												$customXmlDataStorageParts,
												w:sdtPr,
												'tc',
												$child)" />
							</w:tc>
						</xsl:when>
						<xsl:otherwise>
								<xsl:copy-of
								select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.w14Checkbox(
												$wmlPackage,
												$sourcePart,
												$customXmlDataStorageParts,
												w:sdtPr,
												$parent,
												$child)" />
						</xsl:otherwise>
					</xsl:choose>			     
			     </w:sdtContent>
			</xsl:copy>    			
		</xsl:when>  		

  		<xsl:when test="w:sdtPr/w:dataBinding and w:sdtPr/w:picture">
  			<!--  honour w:dataBinding -->
  			
  			<xsl:choose>
  				<xsl:when test="w:sdtContent//a:blip"><!-- docx4j 3.0 -->
  					
					<xsl:copy>
					     <xsl:copy-of select="w:sdtPr"/>
					     
					     <xsl:if test="w:stdEndPr">
					     	<xsl:copy-of select="w:sdtEndPr"/>
				     	</xsl:if>
		
					    <xsl:apply-templates select="w:sdtContent" mode="picture3"> 
					    	<xsl:with-param name="sdtPr" select="w:sdtPr"/>
					    </xsl:apply-templates>
					     
					</xsl:copy>
  				
  				</xsl:when>
  				<xsl:otherwise>
  					<!--  fallback to pre v3 approach -->
					<xsl:copy>
					     <xsl:copy-of select="w:sdtPr"/>
					     
					     <xsl:if test="w:stdEndPr">
					     	<xsl:copy-of select="w:sdtEndPr"/>
				     	</xsl:if>
		
					     
					     <w:sdtContent>
									<xsl:copy-of
									select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathInjectImage(
												$wmlPackage,
												$sourcePart,
												$customXmlDataStorageParts,
												string(w:sdtPr/w:dataBinding/@w:storeItemID),
												string(w:sdtPr/w:dataBinding/@w:xpath),
												string(w:sdtPr/w:dataBinding/@w:prefixMappings),
												$parent,
												$child,
												string(w:sdtContent//wp:extent[1]/@cx), 
												string(w:sdtContent//wp:extent[1]/@cy))" />
					     </w:sdtContent>
					</xsl:copy>
  				
  				</xsl:otherwise>
  			</xsl:choose>
		</xsl:when>

		<xsl:when test="w:sdtPr/w:dataBinding and w:sdtPr/w:date">
		<!--  honour w:dataBinding -->
			<xsl:copy>
				<xsl:copy-of select="w:sdtPr"/>

				<xsl:if test="w:stdEndPr">
					<xsl:copy-of select="w:sdtEndPr"/>
				</xsl:if>

				<w:sdtContent>
					<xsl:choose>
						<xsl:when test="w:sdtContent/w:tc">
							<w:tc>
								<xsl:if test="w:sdtContent/w:tc/w:tcPr">
									<xsl:copy-of select="w:sdtContent/w:tc/w:tcPr"/>
								</xsl:if>
								<xsl:copy-of
								select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathDate(
											$wmlPackage,
											$sourcePart,
											$customXmlDataStorageParts,
                                            w:sdtPr,
											$parent,
											$child,
											w:sdtPr/w:date)" />
							</w:tc>
						</xsl:when>
						<xsl:otherwise>
							<xsl:copy-of
							select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathDate(
										$wmlPackage,
										$sourcePart, 
										$customXmlDataStorageParts,
                                        w:sdtPr,
										$parent,
										$child,
										w:sdtPr/w:date)" />
						</xsl:otherwise>
					</xsl:choose>
				</w:sdtContent>
			</xsl:copy>
		</xsl:when>
  	
  		<xsl:when test="contains( string(w:sdtPr/w:tag/@w:val), 'od:ContentType=application/xhtml+xml' )">
  			<!--  Convert XHTML.
  			
  				  For all except the run-level case, we need to be able to have block level content.
  				  Which means we need to be inserting into a rich text control,
  				  which in turn means there can't be a w:sdtPr/w:dataBinding.  
  				  
  				  So the convertXHTML extension
  				  function must read xpath from the w:tag, which in turn means the Word Add-In 
  				  editor must write that.
  				    			
  			 -->
  			 
  			 <xsl:choose>

				<xsl:when
					test="java:org.docx4j.model.datastorage.BindingTraverserXSLT.importXHTMLMissing()"> 
					
					<!-- add an altChunk -->
			
					<xsl:copy>
						<xsl:copy-of select="w:sdtPr" />
						<w:sdtContent>
						
							<xsl:copy-of
							select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.convertXHTMLtoAltChunk(
										$bindingTraverserState,
										$wmlPackage,
										$sourcePart,
										$customXmlDataStorageParts,
										$xPathsMap,
										w:sdtPr,
										$parent,
										$child,
										$sequenceCounters,
										$bookmarkIdCounter )" />
							
						</w:sdtContent>
					</xsl:copy>
				</xsl:when>
				
	  			 <xsl:otherwise> <!--  rely on -ImportXTML -->
					<xsl:copy>
					     <xsl:copy-of select="w:sdtPr"/>
					     
					     <xsl:if test="w:stdEndPr">
					     	<xsl:copy-of select="w:sdtEndPr"/>
				     	</xsl:if>
					     
					     <w:sdtContent>
					     	
						  	<xsl:choose>
						  		<!--  Note that all branches of this choose currently do the same 
						  			  thing, so all it does is provide documentation for the various cases.
						  			    -->
						  		<xsl:when test="w:sdtContent/w:tbl">
						  		<!--  When the sdt contains a table, assume block level content is to replace it
						  		      (most likely, an XHTML table?).
						  		      
						  		      Tested/works for 2.8.0
						  		      
						  		      A future version could possibly read/re-use w:sdtContent/w:tbl/w:tblPr
						  		      if there were the same number of columns?
						  		       -->
									<xsl:copy-of
									select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.convertXHTML(
												$bindingTraverserState,
												$wmlPackage,
												$sourcePart,
												$customXmlDataStorageParts,
												$xPathsMap,
												w:sdtPr,
												$parent,
												$child,
												$sequenceCounters,
												$bookmarkIdCounter )" />
						  		</xsl:when>				  		
						  		<xsl:when test="w:sdtContent/w:tr">
						  			<!--  no reason in principle why we couldn't convert
						  			      an XHTML tr to w:tr.
						  			      
						  			      TODO currently xhtmlrenderer NPEs
						  			        
						  		      	  A future version could possibly read/re-use w:sdtContent/w:tr/w:trPr
						  			       -->
									<xsl:copy-of
									select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.convertXHTML(
												$bindingTraverserState,
												$wmlPackage,
												$sourcePart,
												$customXmlDataStorageParts,
												$xPathsMap,
												w:sdtPr,
												$parent,
												$child,
												$sequenceCounters,
												$bookmarkIdCounter )" />
						  		</xsl:when>				  		
						  		<xsl:when test="w:sdtContent/w:tc">
						  			<!--  no reason in principle why we couldn't convert
						  			      an XHTML table cell to w:tc.
						  			      
						  			      TODO currently xhtmlrenderer NPEs
						  			        
						  			       -->
									<xsl:copy-of
									select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.convertXHTML(
												$bindingTraverserState,
												$wmlPackage,
												$sourcePart,
												$customXmlDataStorageParts,
												$xPathsMap,
												w:sdtPr,
												$parent,
												$child,
												$sequenceCounters,
												$bookmarkIdCounter )" />
						  		</xsl:when>				  		
						  		<xsl:when test="w:sdtContent/w:p">
						  		
					  				<!--  
					  				
					  				preserve existing w:pPr mode? 
					  					<xsl:copy-of select="w:sdtContent/w:p/w:pPr"/>
					  				-->
					  				
					  				<!--  create runs -->
									<xsl:copy-of
									select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.convertXHTML(
												$bindingTraverserState,
												$wmlPackage,
												$sourcePart,
												$customXmlDataStorageParts,
												$xPathsMap,
												w:sdtPr,
												$parent,
												$child,
												$sequenceCounters,
												$bookmarkIdCounter )" />
						  		</xsl:when>
						  		<xsl:otherwise>  <!--  run level 
						  		
								       <w:sdtContent>
								          <w:r>
								            <w:rPr>				  			 
						  		
						  		--> 
						  			<!--  can we insert a fragment ie multiple runs? --> 		
									<xsl:copy-of
									select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.convertXHTML(
												$bindingTraverserState,
												$wmlPackage,
												$sourcePart,
												$customXmlDataStorageParts,
												$xPathsMap,
												w:sdtPr,
												$parent,
												$child,
												$sequenceCounters,
												$bookmarkIdCounter )" />
						  		</xsl:otherwise>  		
						  	</xsl:choose>    
					     </w:sdtContent>
					     
					</xsl:copy>  		  			
	  			 </xsl:otherwise>
  			 
  			 </xsl:choose>
  			 
  		</xsl:when>
  		
  		<xsl:when test="contains( string(w:sdtPr/w:tag/@w:val), 'od:progid=Word.Document' )">
  			<!--  Convert escaped Flat OPC XML.
  			
  				  We're inserting into a rich text control,
  				  which in turn means there can't be a w:sdtPr/w:dataBinding
  				  (though there could be a w15:dataBinding)  
  				  
  				  So (prior to w15:dataBinding) the extension function must read xpath from the w:tag, which in turn means the Word Add-In 
  				  editor must write that.
  				  
  				  201807: the NoXML/FabDocx AddIn writes the w15:dataBinding element. 

				  	TODO: handle the case where there is no OpenDoPE tag.  				    			
  			 -->
			<xsl:copy>
			     <xsl:copy-of select="w:sdtPr"/>
			     
			     <xsl:if test="w:stdEndPr">
			     	<xsl:copy-of select="w:sdtEndPr"/>
		     	</xsl:if>
			     
			     <w:sdtContent>

					<xsl:choose>
				  		<xsl:when test="w:sdtContent/w:tc">
							<w:tc>
								<xsl:copy-of select="w:sdtContent/w:tc/w:tcPr"/>
								<xsl:copy-of
								select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.convertFlatOPC(
											$wmlPackage,
											$sourcePart,
											$customXmlDataStorageParts,
											$xPathsMap,
											$parent,
											$child,
											w:sdtPr/w:rPr,
											$tag )" />
							</w:tc>			     											
						</xsl:when>
						<xsl:otherwise>
							<xsl:copy-of
							select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.convertFlatOPC(
										$wmlPackage,
										$sourcePart,
										$customXmlDataStorageParts,
										$xPathsMap,
										$parent,
										$child,
										w:sdtPr/w:rPr,
										$tag )" />
						
						</xsl:otherwise>
					</xsl:choose>
										
			     </w:sdtContent>
			     
			</xsl:copy>  		  			
  		</xsl:when>
				  		
				  		  		
  		
  		<xsl:when test="contains(string(w:sdtPr/w:tag/@w:val), 'od:RptPosCon')">
  		
  		<!-- conditions are already evaluated, except those which are position dependent 
  		
  			 the position relates to the first ancestor repeat.
  			 
  			 See generally, http://stackoverflow.com/a/11464354/1031689
  		-->
  		
  		<!--  We need to calculate our position. See http://stackoverflow.com/questions/2606186/find-position-of-a-node-within-a-nodeset-using-xpath?rq=1
  		 -->
			<xsl:variable name="vNode" select="ancestor::w:sdt[contains(string(w:sdtPr/w:tag/@w:val), 'od:rptd')]" /> 
			<xsl:variable name="repeatTag" select="$vNode/w:sdtPr/w:tag/@w:val" />
				<!--  We could match on repeat ID and repeat instance ID, 
				 	  but no need, because the tag is cloned, so its contents will be the same across instances -->
			
			<!-- <xsl:variable name="repeatParent" select="ancestor::*[contains(string(w:sdt/w:sdtPr/w:tag/@w:val), 'od:rptd')][1]" />  --> 
			<xsl:variable name="repeatParent" select="$vNode/.." /> 
			
			<xsl:variable name="vNodeSet" select="$repeatParent/w:sdt[contains(string(w:sdtPr/w:tag/@w:val), $repeatTag)]" /> 

 			<xsl:variable name="dummy"
				select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.log(concat('vNodeSet', count($vNodeSet)))" />
 
			<xsl:variable name="vPrecNodes" select="$vNode/preceding::node()"/>
			
			 <xsl:variable name="vAncNodes" select="$vNode/ancestor::node()"/>
			
			 <xsl:variable name="vPrecInNodeSet" select=
			  "$vNodeSet
			      [count(.|$vPrecNodes) = count($vPrecNodes)
			      or
			       count(.|$vAncNodes) = count($vAncNodes)
			      ]
					  "/>
			<xsl:variable name="pos" select="count($vPrecInNodeSet) +1" />

		
			<xsl:variable name="expression"
				select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.getRepeatPositionCondition(								
										$xPathsMap,
										string(w:sdtPr/w:tag/@w:val))" />
							
		
		    <xsl:variable name="result" >
				<xsl:apply-templates select="$vNodeSet" mode="myeval">
					<xsl:with-param name="expression" ><xsl:value-of select="$expression"/></xsl:with-param>
					<xsl:with-param name="pos" ><xsl:value-of select="$pos"/></xsl:with-param>
				</xsl:apply-templates>
			</xsl:variable>		
			
 			<xsl:variable name="dummy2"
        				select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.log(concat('result ', $result))" />
        
        
            <xsl:choose>
                <xsl:when test="contains($result, 'true')">
        
                    <xsl:variable name="content">
                        <w:sdtContent>
                            <xsl:apply-templates select="w:sdtContent/*"/>
                        </w:sdtContent>
                    </xsl:variable>
        
                    <xsl:copy>
                    
                        <!--  if fragment contains w:hyperlink, then remove stuff from sdtPr -->
                        <w:sdtPr>
	                        <xsl:apply-templates select="w:sdtPr">
	                            <xsl:with-param
	                                name="content"
	                                select="xalan:nodeset($content)"/>
	                        </xsl:apply-templates>
                        </w:sdtPr>
        
                        <xsl:if test="w:stdEndPr">
                            <xsl:copy-of select="w:sdtEndPr"/>
                        </xsl:if>
        
                        <xsl:copy-of select="$content"/>
                    </xsl:copy>
        
                </xsl:when>
                <xsl:otherwise>
        					<!--  omit -->
                </xsl:otherwise>
            </xsl:choose>				
          		
          		</xsl:when>
  		
  		<xsl:when test="contains(string(w:sdtPr/w:tag/@w:val), 'od:xpath')">
  			<!--  honour extended bind (Word databinding only works when a element is returned);
  				  (this used to be in OpenDoPEHandler, but moved 13 Sept 2011 for docx4j 2.7.1.
  			      here we support boolean, integer. What to do with node-set?? -->

  			<xsl:variable name="content">
			     <w:sdtContent>
			     	<xsl:variable name="multiLine" select="w:sdtPr/w:text/@w:multiLine='1' or w:sdtPr/w:text/@w:multiLine='true' or w:sdtPr/w:text/@w:multiLine='yes'" /> 
			     	
				  	<xsl:choose>
				  		<xsl:when test="w:sdtContent/w:tbl">
				  			<w:tbl>
				  				<xsl:copy-of select="w:sdtContent/w:tbl/w:tblPr"/>
				  				<xsl:copy-of select="w:sdtContent/w:tbl/w:tblGrid"/>
					  			<w:tr>
					  				<xsl:copy-of select="w:sdtContent/w:tbl/w:tr/w:trPr"/>
						  			<w:tc>
						  				<xsl:copy-of select="w:sdtContent/w:tbl/w:tr/w:trPr/w:tc/w:tcPr"/>
							  			<w:p>
							  				<xsl:copy-of select="w:sdtContent/w:tbl/w:tr/w:tc/w:p/w:pPr"/>
							  				
											<xsl:copy-of
											select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathGenerateRuns(
														$bindingTraverserState,
														$wmlPackage,
														$sourcePart,
														$customXmlDataStorageParts,
														$xPathsMap,
														w:sdtPr,
														$parent,
														$child,
														$multiLine, $bookmarkIdCounter)" />
										</w:p>
									</w:tc>
								</w:tr>
							</w:tbl>
				  		</xsl:when>				  		
				  		<xsl:when test="w:sdtContent/w:tr">
				  			<w:tr>
				  				<xsl:copy-of select="w:sdtContent/w:tr/w:trPr"/>
					  			<w:tc>
					  				<xsl:copy-of select="w:sdtContent/w:tr/w:trPr/w:tc/w:tcPr"/>
						  			<w:p>
						  				<xsl:copy-of select="w:sdtContent/w:tr/w:tc/w:p/w:pPr"/>
						  				
										<xsl:copy-of
										select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathGenerateRuns(
													$bindingTraverserState,										
													$wmlPackage,
													$sourcePart,
													$customXmlDataStorageParts,
													$xPathsMap,
													w:sdtPr,
													$parent,
													$child,
													$multiLine, $bookmarkIdCounter)" />
									</w:p>
								</w:tc>
							</w:tr>
				  		</xsl:when>				  		
				  		<xsl:when test="w:sdtContent/w:tc">
				  			<w:tc>
				  				<!--  preserve existing w:tcPr -->
				  				<xsl:copy-of select="w:sdtContent/w:tc/w:tcPr"/>
					  			<w:p>
					  				<!--  preserve existing w:pPr -->
					  				<xsl:copy-of select="w:sdtContent/w:tc/w:p/w:pPr"/>
					  				
					  				<!--  create runs -->
									<xsl:copy-of
									select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathGenerateRuns(
												$bindingTraverserState,										
												$wmlPackage,
												$sourcePart,
												$customXmlDataStorageParts,
												$xPathsMap,
												w:sdtPr,
												$parent,
												$child,
												$multiLine, $bookmarkIdCounter)" />
								</w:p>
							</w:tc>
				  		</xsl:when>				  		
				  		<xsl:when test="w:sdtContent/w:p">
				  			<w:p>
				  				<!--  preserve existing w:pPr -->
				  				<xsl:copy-of select="w:sdtContent/w:p/w:pPr"/>
				  				
				  				<!--  create runs -->
								<xsl:copy-of
								select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathGenerateRuns(
											$bindingTraverserState,										
											$wmlPackage,
											$sourcePart,
											$customXmlDataStorageParts,
											$xPathsMap,
											w:sdtPr,
											$parent,
											$child,
											$multiLine, $bookmarkIdCounter)" />
							</w:p>
				  		</xsl:when>
				  		<xsl:otherwise>  <!--  run level --> 
				  			<!--  can we insert a fragment ie multiple runs? --> 		
							<xsl:copy-of
							select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathGenerateRuns(
										$bindingTraverserState,										
										$wmlPackage,
										$sourcePart,
										$customXmlDataStorageParts,
										$xPathsMap,
										w:sdtPr,
										$parent,
										$child,
										$multiLine, $bookmarkIdCounter)" />
				  		</xsl:otherwise>  		
				  	</xsl:choose>    
			     </w:sdtContent>
			</xsl:variable>
			
			<xsl:copy>
	  				
				<!--  if fragment contains w:hyperlink, then remove stuff from sdtPr -->
				<w:sdtPr>
				     <xsl:apply-templates select="w:sdtPr">
						<xsl:with-param name="content" select="xalan:nodeset($content)"/>
					</xsl:apply-templates>
				</w:sdtPr>
			     
			     <xsl:if test="w:stdEndPr">
			     	<xsl:copy-of select="w:sdtEndPr"/>
		     	</xsl:if>
			
				<xsl:copy-of select="$content"/>			     
			</xsl:copy>  		  	
					  			
  		</xsl:when>
  		
  		<xsl:when test="w:sdtPr/w15:dataBinding and not(w:sdtPr/w:richText) and not(w:sdtPr/w:docPartGallery)">
  			<!--  honour w15:dataBinding, from docx4j 3.3.7 
  			
  					(Flat OPC richText is handled earlier, provided it also has an OpenDoPE tag)
  			-->
  			
  			<xsl:variable name="content">
			     <w:sdtContent>
			     	<xsl:variable name="multiLine" select="w:sdtPr/w:text/@w:multiLine='1' or w:sdtPr/w:text/@w:multiLine='true' or w:sdtPr/w:text/@w:multiLine='yes'" /> 
			     	
				  	<xsl:choose>
				  		<xsl:when test="w:sdtContent/w:tbl">
				  			<w:tbl>
				  				<xsl:copy-of select="w:sdtContent/w:tbl/w:tblPr"/>
				  				<xsl:copy-of select="w:sdtContent/w:tbl/w:tblGrid"/>
					  			<w:tr>
					  				<xsl:copy-of select="w:sdtContent/w:tbl/w:tr/w:trPr"/>
						  			<w:tc>
						  				<xsl:copy-of select="w:sdtContent/w:tbl/w:tr/w:trPr/w:tc/w:tcPr"/>
							  			<w:p>
							  				<xsl:copy-of select="w:sdtContent/w:tbl/w:tr/w:tc/w:p/w:pPr"/>
							  				
											<xsl:copy-of
											select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathGenerateRuns(
														$bindingTraverserState,										
														$wmlPackage,
														$sourcePart,
														$customXmlDataStorageParts,
														string(w:sdtPr/w15:dataBinding/@w:storeItemID),
														string(w:sdtPr/w15:dataBinding/@w:xpath),
														string(w:sdtPr/w15:dataBinding/@w:prefixMappings),
														w:sdtPr,
														$parent,
														$child,
														$multiLine, $bookmarkIdCounter)" />
										</w:p>
									</w:tc>
								</w:tr>
							</w:tbl>
				  		</xsl:when>				  		
				  		<xsl:when test="w:sdtContent/w:tr">
				  			<w:tr>
				  				<xsl:copy-of select="w:sdtContent/w:tr/w:trPr"/>
					  			<w:tc>
					  				<xsl:copy-of select="w:sdtContent/w:tr/w:trPr/w:tc/w:tcPr"/>
						  			<w:p>
						  				<xsl:copy-of select="w:sdtContent/w:tr/w:tc/w:p/w:pPr"/>
						  				
										<xsl:copy-of
										select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathGenerateRuns(
													$bindingTraverserState,										
													$wmlPackage,
													$sourcePart,
													$customXmlDataStorageParts,
													string(w:sdtPr/w15:dataBinding/@w:storeItemID),
													string(w:sdtPr/w15:dataBinding/@w:xpath),
													string(w:sdtPr/w15:dataBinding/@w:prefixMappings),
													w:sdtPr,
													$parent,
													$child,
													$multiLine, $bookmarkIdCounter)" />
									</w:p>
								</w:tc>
							</w:tr>
				  		</xsl:when>				  		
				  		<xsl:when test="w:sdtContent/w:tc">
				  			<w:tc>
				  				<!--  preserve existing w:tcPr -->
				  				<xsl:copy-of select="w:sdtContent/w:tc/w:tcPr"/>
					  			<w:p>
					  				<!--  preserve existing w:pPr -->
					  				<xsl:copy-of select="w:sdtContent/w:tc/w:p/w:pPr"/>
					  				
					  				<!--  create runs -->
									<xsl:copy-of
									select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathGenerateRuns(
												$bindingTraverserState,										
												$wmlPackage,
												$sourcePart,
												$customXmlDataStorageParts,
												string(w:sdtPr/w15:dataBinding/@w:storeItemID),
												string(w:sdtPr/w15:dataBinding/@w:xpath),
												string(w:sdtPr/w15:dataBinding/@w:prefixMappings),
												w:sdtPr,
												$parent,
												$child,
												$multiLine, $bookmarkIdCounter)" />
								</w:p>
							</w:tc>
				  		</xsl:when>				  		
				  		<xsl:when test="w:sdtContent/w:p">
				  			<w:p>
				  				<!--  preserve existing w:pPr -->
				  				<xsl:copy-of select="w:sdtContent/w:p/w:pPr"/>
				  				
				  				<!--  create runs -->
								<xsl:copy-of
								select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathGenerateRuns(
											$bindingTraverserState,										
											$wmlPackage,
											$sourcePart,
											$customXmlDataStorageParts,
											string(w:sdtPr/w15:dataBinding/@w:storeItemID),
											string(w:sdtPr/w15:dataBinding/@w:xpath),
											string(w:sdtPr/w15:dataBinding/@w:prefixMappings),
											w:sdtPr,
											$parent,
											$child,
											$multiLine, $bookmarkIdCounter)" />
							</w:p>
				  		</xsl:when>
				  		<xsl:otherwise>  <!--  run level --> 
				  			<!--  can we insert a fragment ie multiple runs? --> 		
							<xsl:copy-of
							select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathGenerateRuns(
										$bindingTraverserState,										
										$wmlPackage,
										$sourcePart,
										$customXmlDataStorageParts,
										string(w:sdtPr/w15:dataBinding/@w:storeItemID),
										string(w:sdtPr/w15:dataBinding/@w:xpath),
										string(w:sdtPr/w15:dataBinding/@w:prefixMappings),
										w:sdtPr,
										$parent,
										$child,
										$multiLine, $bookmarkIdCounter)" />
				  		</xsl:otherwise>  		
				  	</xsl:choose>    
			     </w:sdtContent>
			</xsl:variable>

<!--   			
			<xsl:variable name="dummy"
				select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.logXml( xalan:nodeset($content) )" />  	
  	 -->		
  			
			<xsl:copy>
			
				<!--  if fragment contains w:hyperlink, then remove stuff from sdtPr -->
				<w:sdtPr>
				     <xsl:apply-templates select="w:sdtPr">
						<xsl:with-param name="content" select="xalan:nodeset($content)"/>
					</xsl:apply-templates>
				</w:sdtPr>
			     
			     <xsl:if test="w:stdEndPr">
			     	<xsl:copy-of select="w:sdtEndPr"/>
		     	</xsl:if>
			
				<xsl:copy-of select="$content"/>			     
			</xsl:copy>  		  	
			
					
  		</xsl:when>

        <xsl:when test="w:sdtPr/w:dataBinding and not(w:sdtPr/w:richText) and not(w:sdtPr/w:docPartGallery)">
            <!--  honour w:dataBinding -->
            
            <xsl:variable name="content">
                 <w:sdtContent>
                    <xsl:variable name="multiLine" select="w:sdtPr/w:text/@w:multiLine='1' or w:sdtPr/w:text/@w:multiLine='true' or w:sdtPr/w:text/@w:multiLine='yes'" /> 
                    
                    <xsl:choose>
                        <xsl:when test="w:sdtContent/w:tbl">
                            <w:tbl>
                                <xsl:copy-of select="w:sdtContent/w:tbl/w:tblPr"/>
                                <xsl:copy-of select="w:sdtContent/w:tbl/w:tblGrid"/>
                                <w:tr>
                                    <xsl:copy-of select="w:sdtContent/w:tbl/w:tr/w:trPr"/>
                                    <w:tc>
                                        <xsl:copy-of select="w:sdtContent/w:tbl/w:tr/w:trPr/w:tc/w:tcPr"/>
                                        <w:p>
                                            <xsl:copy-of select="w:sdtContent/w:tbl/w:tr/w:tc/w:p/w:pPr"/>
                                            
                                            <xsl:copy-of
                                            select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathGenerateRuns(
                                                        $bindingTraverserState,                                     
                                                        $wmlPackage,
                                                        $sourcePart,
                                                        $customXmlDataStorageParts,
                                                        string(w:sdtPr/w:dataBinding/@w:storeItemID),
                                                        string(w:sdtPr/w:dataBinding/@w:xpath),
                                                        string(w:sdtPr/w:dataBinding/@w:prefixMappings),
                                                        w:sdtPr,
                                                        $parent,
                                                        $child,
                                                        $multiLine, $bookmarkIdCounter)" />
                                        </w:p>
                                    </w:tc>
                                </w:tr>
                            </w:tbl>
                        </xsl:when>                     
                        <xsl:when test="w:sdtContent/w:tr">
                            <w:tr>
                                <xsl:copy-of select="w:sdtContent/w:tr/w:trPr"/>
                                <w:tc>
                                    <xsl:copy-of select="w:sdtContent/w:tr/w:trPr/w:tc/w:tcPr"/>
                                    <w:p>
                                        <xsl:copy-of select="w:sdtContent/w:tr/w:tc/w:p/w:pPr"/>
                                        
                                        <xsl:copy-of
                                        select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathGenerateRuns(
                                                    $bindingTraverserState,                                     
                                                    $wmlPackage,
                                                    $sourcePart,
                                                    $customXmlDataStorageParts,
                                                    string(w:sdtPr/w:dataBinding/@w:storeItemID),
                                                    string(w:sdtPr/w:dataBinding/@w:xpath),
                                                    string(w:sdtPr/w:dataBinding/@w:prefixMappings),
                                                    w:sdtPr,
                                                    $parent,
                                                    $child,
                                                    $multiLine, $bookmarkIdCounter)" />
                                    </w:p>
                                </w:tc>
                            </w:tr>
                        </xsl:when>                     
                        <xsl:when test="w:sdtContent/w:tc">
                            <w:tc>
                                <!--  preserve existing w:tcPr -->
                                <xsl:copy-of select="w:sdtContent/w:tc/w:tcPr"/>
                                <w:p>
                                    <!--  preserve existing w:pPr -->
                                    <xsl:copy-of select="w:sdtContent/w:tc/w:p/w:pPr"/>
                                    
                                    <!--  create runs -->
                                    <xsl:copy-of
                                    select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathGenerateRuns(
                                                $bindingTraverserState,                                     
                                                $wmlPackage,
                                                $sourcePart,
                                                $customXmlDataStorageParts,
                                                string(w:sdtPr/w:dataBinding/@w:storeItemID),
                                                string(w:sdtPr/w:dataBinding/@w:xpath),
                                                string(w:sdtPr/w:dataBinding/@w:prefixMappings),
                                                w:sdtPr,
                                                $parent,
                                                $child,
                                                $multiLine, $bookmarkIdCounter)" />
                                </w:p>
                            </w:tc>
                        </xsl:when>                     
                        <xsl:when test="w:sdtContent/w:p">
                            <w:p>
                                <!--  preserve existing w:pPr -->
                                <xsl:copy-of select="w:sdtContent/w:p/w:pPr"/>
                                
                                <!--  create runs -->
                                <xsl:copy-of
                                select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathGenerateRuns(
                                            $bindingTraverserState,                                     
                                            $wmlPackage,
                                            $sourcePart,
                                            $customXmlDataStorageParts,
                                            string(w:sdtPr/w:dataBinding/@w:storeItemID),
                                            string(w:sdtPr/w:dataBinding/@w:xpath),
                                            string(w:sdtPr/w:dataBinding/@w:prefixMappings),
                                            w:sdtPr,
                                            $parent,
                                            $child,
                                            $multiLine, $bookmarkIdCounter)" />
                            </w:p>
                        </xsl:when>
                        <xsl:otherwise>  <!--  run level --> 
                            <!--  can we insert a fragment ie multiple runs? -->        
                            <xsl:copy-of
                            select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathGenerateRuns(
                                        $bindingTraverserState,                                     
                                        $wmlPackage,
                                        $sourcePart,
                                        $customXmlDataStorageParts,
                                        string(w:sdtPr/w:dataBinding/@w:storeItemID),
                                        string(w:sdtPr/w:dataBinding/@w:xpath),
                                        string(w:sdtPr/w:dataBinding/@w:prefixMappings),
                                        w:sdtPr,
                                        $parent,
                                        $child,
                                        $multiLine, $bookmarkIdCounter)" />
                        </xsl:otherwise>        
                    </xsl:choose>    
                 </w:sdtContent>
            </xsl:variable>

<!--            
            <xsl:variable name="dummy"
                select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.logXml( xalan:nodeset($content) )" />    
     -->        
            
            <xsl:copy>
            
                <!--  if fragment contains w:hyperlink, then remove stuff from sdtPr -->
                <w:sdtPr>
	                 <xsl:apply-templates select="w:sdtPr">
	                    <xsl:with-param name="content" select="xalan:nodeset($content)"/>
	                </xsl:apply-templates>
                </w:sdtPr>
                 
                 <xsl:if test="w:stdEndPr">
                    <xsl:copy-of select="w:sdtEndPr"/>
                </xsl:if>
            
                <xsl:copy-of select="$content"/>                 
            </xsl:copy>             
            
                    
        </xsl:when>
  		
  		
  		<xsl:otherwise> <!--  no w:dataBinding, or one spec says to ignore -->  		
			<xsl:variable name="dummy3"
				select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.log( 
							concat(' processing contents only of ', string(w:sdtPr/w:tag/@w:val) ) )" />  	
		    <xsl:copy>
			    <xsl:copy-of select="w:sdtPr"/>	<!--  avoid template match="w:sdtPr" -->	    
		      	<xsl:apply-templates select="w:sdtContent"/>
		    </xsl:copy>  		
  		</xsl:otherwise>  		
  	</xsl:choose>    
  </xsl:template>


  <xsl:template match="w:sdtPr">  
  	<xsl:param name="content"></xsl:param>
  	
		<xsl:variable name="dummy"
	select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.log( string(w:tag/@w:val) )" />  	
  	
  	<xsl:choose>
  		<xsl:when test="$content and count($content//w:hyperlink)>0">
		  <!-- A content control with SdtPr w:dataBinding and w:text
			   which contains a w:hyperlink will prevent Word 2007 from
			   opening the docx, so if there is a w:hyperlink,
			   make sure  w:dataBinding and w:text are not present.  -->
			<xsl:variable name="dummy2"
			    select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.log( '.. stripping w:dataBinding and w:text' )" />  	
  			<xsl:apply-templates  mode="word2007hyperlinkfix" />
  		</xsl:when>
  		<xsl:when test="$content and count($content//w:rStyle[@w:val = 'PlaceholderText' ])>0">
  			<!--  If the sdt content contains <w:rStyle w:val="PlaceholderText"/> add w:showingPlcHdr
  			
  				  RemovalHandler will use w:showingPlcHdr to retain this sdt in the Quantifier.ALL_BUT_PLACEHOLDERS case.
  				  
  				  RemovalHandler could just check for this rStyle value directly, but w:showingPlcHdr is more correct
  				  (since in the future we could support placeholders which have some different stylename)
  			
  			 -->
	        <w:showingPlcHdr/>
		    <xsl:apply-templates select="@*|node()"/>
  		</xsl:when>
  		<xsl:otherwise>
		    <xsl:apply-templates select="@*|node()"/>
  		</xsl:otherwise>
  	</xsl:choose>
  	
  </xsl:template>
  
  <!-- 
          <w14:checkbox>
            <w14:checked w14:val="1"/>
            <w14:checkedState w14:val="2612" w14:font="MS Gothic"/>
            <w14:uncheckedState w14:val="2610" w14:font="MS Gothic"/>
          </w14:checkbox>
   -->
  <xsl:template match="w14:checked" >   
   
  	<xsl:variable name="attrval"><xsl:value-of select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.w14CheckboxAttr(
						$customXmlDataStorageParts,
						../..)" /></xsl:variable>
	<w14:checked w14:val="{$attrval}"/>
  </xsl:template>

  <!-- Remove these if the sdt contains a w:hyperlink -->
  <xsl:template match="w:dataBinding" mode="word2007hyperlinkfix"/>  
  <xsl:template match="w:text"  mode="word2007hyperlinkfix"/>  
  <xsl:template match="/ | @*|node()" mode="word2007hyperlinkfix">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>


  <!-- Remove these, so missing data does not result
       in Click here to enter text in Word -->
  <xsl:template match="w:placeholder"/>  

  <!--  v3.3.0, allows us to keep track of cell width, 
        so imported bare XHTML images can be scaled if necessary  -->
  <xsl:template match="w:tc">  
  
  <xsl:variable name="tc" select="." />
  
  <xsl:variable name="dummy"
	select="java:org.docx4j.model.datastorage.BindingTraverserState.enteredTc( $bindingTraverserState, $tc )" />  	

    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>

  <xsl:variable name="dummy2"
	select="java:org.docx4j.model.datastorage.BindingTraverserState.exitedTc( $bindingTraverserState )" />  	
      
  </xsl:template>

	<xsl:template match="w:tbl">

		<xsl:variable name="tbl" select="." />

		<xsl:variable name="dummy"
			select="java:org.docx4j.model.datastorage.BindingTraverserState.enteredTbl( $bindingTraverserState, $tbl )" />

		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>

		<xsl:variable name="dummy2"
			select="java:org.docx4j.model.datastorage.BindingTraverserState.exitedTbl( $bindingTraverserState )" />

	</xsl:template>
   
</xsl:stylesheet>
