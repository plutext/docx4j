
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fo="http://www.w3.org/1999/XSL/Format"
    version="1.0"
        >	

  <!-- 
    Copyright 2014, Plutext Pty Ltd.
    
    This file is part of docx4j.

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
        


<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="no" />

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="fo:block">

	<xsl:choose>
	
		<xsl:when test="./fo:table/fo:table-body/fo:table-row/fo:table-cell/fo:block='#TEXTBOX#' ">
			
			<!-- <xsl:variable name="pContents" select="./*[position() > 1]" /> -->
			<!--  TODO FIXME, will the text box always be the first child???  -->
			
			<!--  we don't need the block itself -->
			<xsl:apply-templates select="./fo:table" mode="injector">
				<xsl:with-param name="pContents" select="./*[position() > 1]" />
			</xsl:apply-templates>
			
			
		</xsl:when>
		
		<xsl:otherwise>
		  <xsl:copy>
		    <xsl:apply-templates select="@*|node()"/>
		  </xsl:copy>
		</xsl:otherwise>
	
	</xsl:choose>


</xsl:template>


<xsl:template match="@*|node()" mode="injector">
	<xsl:param name="pContents" select="/.."/>

  <xsl:copy>
    <xsl:apply-templates select="@*|node()" mode="injector" >
    	<xsl:with-param name="pContents" select="$pContents"/>
    </xsl:apply-templates>		    
  </xsl:copy>
</xsl:template>

<xsl:template match="fo:block" mode="injector">
	<xsl:param name="pContents" select="/.."/>

	<xsl:choose>
	
		<xsl:when test="text()='#TEXTBOX#' ">

			<fo:block>
			
				<xsl:copy-of select="$pContents" />
			
			</fo:block>			
			
		</xsl:when>
		
		<xsl:otherwise>
		  <xsl:copy>
		    <xsl:apply-templates select="@*|node()" mode="injector" >
		    	<xsl:with-param name="pContents" select="$pContents"/>
		    </xsl:apply-templates>		    
		  </xsl:copy>
		</xsl:otherwise>
	
	</xsl:choose>


</xsl:template>


</xsl:stylesheet>
