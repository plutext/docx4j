
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
	xmlns:dyn="http://exslt.org/dynamic"
    version="1.0"
        exclude-result-prefixes="java w a o v WX aml w10 pkg wp pic">	
        

<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="yes" />

<xsl:param name="customXmlDataStorageParts"/> <!-- select="'passed in'"-->	
<xsl:param name="wmlPackage"/> <!-- select="'passed in'"-->	
<xsl:param name="sourcePart"/> <!-- select="'passed in'"-->	
<xsl:param name="xPathsPart"/> <!-- select="'passed in'"-->	

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
	
		<xsl:variable name="dummy"
	select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.log(concat('position() ', position() ))" />
	
  	<xsl:choose>
  		<xsl:when test="position()=$pos">
	  		<xsl:value-of select="dyn:evaluate($expression)" /></xsl:when>
  		<xsl:otherwise /> 
  	</xsl:choose>
  	
  </xsl:template>
 
 
  <xsl:template match="w:sdt">  
  
  	<xsl:variable name="tag" select="string(w:sdtPr/w:tag/@w:val)"/>
  	
  	<xsl:choose>

  		<xsl:when test="w:sdtPr/w:dataBinding and w:sdtPr/w:picture">
  			<!--  honour w:dataBinding -->
			<xsl:copy>
			     <xsl:apply-templates select="w:sdtPr"/>
			     
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
										local-name(..),
										local-name(w:sdtContent/*[1]),
										string(w:sdtContent//wp:extent[1]/@cx), 
										string(w:sdtContent//wp:extent[1]/@cy))" />
			     </w:sdtContent>
			</xsl:copy>
		</xsl:when>

  		<xsl:when test="w:sdtPr/w:dataBinding and w:sdtPr/w:date">
  			<!--  honour w:dataBinding -->
			<xsl:copy>
			     <xsl:apply-templates select="w:sdtPr"/>
			     
			     <xsl:if test="w:stdEndPr">
			     	<xsl:copy-of select="w:sdtEndPr"/>
		     	</xsl:if>
			     
			     <w:sdtContent>
							<xsl:copy-of
							select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathDate(
										$wmlPackage,
										$sourcePart,
										$customXmlDataStorageParts,
										string(w:sdtPr/w:dataBinding/@w:storeItemID),
										string(w:sdtPr/w:dataBinding/@w:xpath),
										string(w:sdtPr/w:dataBinding/@w:prefixMappings),
										local-name(..),
										local-name(w:sdtContent/*[1]),
										w:sdtPr/w:date)" />
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
  				  
  				  For the run-level case, there is an argument for writing w:dataBinding,
  				  since this gives the user visual feedback in the Add-In, and that's probably
  				  worth it for this common case.
  			
  			 -->
			<xsl:copy>
			     <xsl:apply-templates select="w:sdtPr"/>
			     
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
										$wmlPackage,
										$sourcePart,
										$customXmlDataStorageParts,
										$xPathsPart,
										local-name(..),
										local-name(w:sdtContent/*[1]),
										w:sdtPr/w:rPr,
										$tag )" />
				  		</xsl:when>				  		
				  		<xsl:when test="w:sdtContent/w:tr">
				  			<!--  no reason in principle why we couldn't convert
				  			      an XHTML tr to w:tr.
				  			      
				  			      TODO currently xhtmlrenderer NPEs
				  			        
				  		      	  A future version could possibly read/re-use w:sdtContent/w:tr/w:trPr
				  			       -->
							<xsl:copy-of
							select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.convertXHTML(
										$wmlPackage,
										$sourcePart,
										$customXmlDataStorageParts,
										$xPathsPart,
										local-name(..),
										local-name(w:sdtContent/*[1]),
										w:sdtPr/w:rPr,
										$tag )" />
				  		</xsl:when>				  		
				  		<xsl:when test="w:sdtContent/w:tc">
				  			<!--  no reason in principle why we couldn't convert
				  			      an XHTML table cell to w:tc.
				  			      
				  			      TODO currently xhtmlrenderer NPEs
				  			        
				  			       -->
							<xsl:copy-of
							select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.convertXHTML(
										$wmlPackage,
										$sourcePart,
										$customXmlDataStorageParts,
										$xPathsPart,
										local-name(..),
										local-name(w:sdtContent/*[1]),
										w:sdtPr/w:rPr,
										$tag )" />
				  		</xsl:when>				  		
				  		<xsl:when test="w:sdtContent/w:p">
				  		
			  				<!--  
			  				
			  				preserve existing w:pPr mode? 
			  					<xsl:copy-of select="w:sdtContent/w:p/w:pPr"/>
			  				-->
			  				
			  				<!--  create runs -->
							<xsl:copy-of
							select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.convertXHTML(
										$wmlPackage,
										$sourcePart,
										$customXmlDataStorageParts,
										$xPathsPart,
										local-name(..),
										local-name(w:sdtContent/*[1]),
										w:sdtPr/w:rPr,
										$tag )" />
				  		</xsl:when>
				  		<xsl:otherwise>  <!--  run level 
				  		
				  			  Note also that in the w:p/w:sdt case, w:sdtContent is empty
				  			  since Word puts nothing there without a dataBinding.  run-level
				  			  sdt may be the same?  So in practice the xsl:otherwise is used.
				  		
				  		--> 
				  			<!--  can we insert a fragment ie multiple runs? --> 		
							<xsl:copy-of
							select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.convertXHTML(
										$wmlPackage,
										$sourcePart,
										$customXmlDataStorageParts,
										$xPathsPart,
										local-name(..),
										local-name(w:sdtContent/*[1]),
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
										$xPathsPart,
										string(w:sdtPr/w:tag/@w:val))" />
							
		
		    <xsl:variable name="result" >
				<xsl:apply-templates select="$vNodeSet" mode="myeval">
					<xsl:with-param name="expression" ><xsl:value-of select="$expression"/></xsl:with-param>
					<xsl:with-param name="pos" ><xsl:value-of select="$pos"/></xsl:with-param>
				</xsl:apply-templates>
			</xsl:variable>		
			
 			<xsl:variable name="dummy"
				select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.log(concat('result ', $result))" />
 				
										
			<xsl:choose>
				<xsl:when test="contains($result, 'true')">
				    <xsl:copy>
				      <xsl:apply-templates select="@*|node()"/>
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
			<xsl:copy>
			     <xsl:apply-templates select="w:sdtPr"/>
			     
			     <xsl:if test="w:stdEndPr">
			     	<xsl:copy-of select="w:sdtEndPr"/>
		     	</xsl:if>
			     
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
														$wmlPackage,
														$sourcePart,
														$customXmlDataStorageParts,
														$xPathsPart,
														string(w:sdtPr/w:tag/@w:val),
														local-name(..),
														local-name(w:sdtContent/*[1]),
														w:sdtPr/w:rPr,
														$multiLine)" />
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
													$wmlPackage,
													$sourcePart,
													$customXmlDataStorageParts,
													$xPathsPart,
													string(w:sdtPr/w:tag/@w:val),
													local-name(..),
													local-name(w:sdtContent/*[1]),
													w:sdtPr/w:rPr,
													$multiLine)" />
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
												$wmlPackage,
												$sourcePart,
												$customXmlDataStorageParts,
												$xPathsPart,
												string(w:sdtPr/w:tag/@w:val),
												local-name(..),
												local-name(w:sdtContent/*[1]),
												w:sdtPr/w:rPr,
												$multiLine)" />
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
											$wmlPackage,
											$sourcePart,
											$customXmlDataStorageParts,
											$xPathsPart,
											string(w:sdtPr/w:tag/@w:val),
											local-name(..),
											local-name(w:sdtContent/*[1]),
											w:sdtPr/w:rPr,
											$multiLine)" />
							</w:p>
				  		</xsl:when>
				  		<xsl:otherwise>  <!--  run level --> 
				  			<!--  can we insert a fragment ie multiple runs? --> 		
							<xsl:copy-of
							select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathGenerateRuns(
										$wmlPackage,
										$sourcePart,
										$customXmlDataStorageParts,
										$xPathsPart,
										string(w:sdtPr/w:tag/@w:val),
										local-name(..),
										local-name(w:sdtContent/*[1]),
										w:sdtPr/w:rPr,
										$multiLine)" />
				  		</xsl:otherwise>  		
				  	</xsl:choose>    
			     </w:sdtContent>
			     
			</xsl:copy>  		  			
  		</xsl:when>
  		
  		<xsl:when test="w:sdtPr/w:dataBinding and not(w:sdtPr/w:richText) and not(w:sdtPr/w:docPartGallery)">
  			<!--  honour w:dataBinding -->
			<xsl:copy>
			     <xsl:apply-templates select="w:sdtPr"/>
			     
			     <xsl:if test="w:stdEndPr">
			     	<xsl:copy-of select="w:sdtEndPr"/>
		     	</xsl:if>
			     
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
														$wmlPackage,
														$sourcePart,
														$customXmlDataStorageParts,
														string(w:sdtPr/w:dataBinding/@w:storeItemID),
														string(w:sdtPr/w:dataBinding/@w:xpath),
														string(w:sdtPr/w:dataBinding/@w:prefixMappings),
										local-name(..),
										local-name(w:sdtContent/*[1]),
														w:sdtPr/w:rPr,
														$multiLine,
														$tag )" />
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
													$wmlPackage,
													$sourcePart,
													$customXmlDataStorageParts,
													string(w:sdtPr/w:dataBinding/@w:storeItemID),
													string(w:sdtPr/w:dataBinding/@w:xpath),
													string(w:sdtPr/w:dataBinding/@w:prefixMappings),
										local-name(..),
										local-name(w:sdtContent/*[1]),
													w:sdtPr/w:rPr,
													$multiLine,
													$tag )" />
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
												$wmlPackage,
												$sourcePart,
												$customXmlDataStorageParts,
												string(w:sdtPr/w:dataBinding/@w:storeItemID),
												string(w:sdtPr/w:dataBinding/@w:xpath),
												string(w:sdtPr/w:dataBinding/@w:prefixMappings),
										local-name(..),
										local-name(w:sdtContent/*[1]),
												w:sdtPr/w:rPr,
												$multiLine,
												$tag )" />
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
											$wmlPackage,
											$sourcePart,
											$customXmlDataStorageParts,
											string(w:sdtPr/w:dataBinding/@w:storeItemID),
											string(w:sdtPr/w:dataBinding/@w:xpath),
											string(w:sdtPr/w:dataBinding/@w:prefixMappings),
										local-name(..),
										local-name(w:sdtContent/*[1]),
											w:sdtPr/w:rPr,
											$multiLine,
											$tag )" />
							</w:p>
				  		</xsl:when>
				  		<xsl:otherwise>  <!--  run level --> 
				  			<!--  can we insert a fragment ie multiple runs? --> 		
							<xsl:copy-of
							select="java:org.docx4j.model.datastorage.BindingTraverserXSLT.xpathGenerateRuns(
										$wmlPackage,
										$sourcePart,
										$customXmlDataStorageParts,
										string(w:sdtPr/w:dataBinding/@w:storeItemID),
										string(w:sdtPr/w:dataBinding/@w:xpath),
										string(w:sdtPr/w:dataBinding/@w:prefixMappings),
										local-name(..),
										local-name(w:sdtContent/*[1]),
										w:sdtPr/w:rPr,
										$multiLine,
										$tag )" />
				  		</xsl:otherwise>  		
				  	</xsl:choose>    
			     </w:sdtContent>
			     
			</xsl:copy>  		  			
  		</xsl:when>
  		
  		
  		<xsl:otherwise> <!--  no w:dataBinding, or one spec says to ignore -->  		
		    <xsl:copy>
		      <xsl:apply-templates select="@*|node()"/>
		    </xsl:copy>  		
  		</xsl:otherwise>  		
  	</xsl:choose>    
  </xsl:template>


  <!-- Remove these, so missing data does not result
       in Click here to enter text in Word -->
  <xsl:template match="w:placeholder"/>  

   
</xsl:stylesheet>
