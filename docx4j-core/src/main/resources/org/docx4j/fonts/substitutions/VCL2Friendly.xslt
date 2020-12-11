<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:oor="http://openoffice.org/2001/registry">

<!-- 
 *  Copyright 2007-2020, Plutext Pty Ltd.
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

<!--  PURPOSE: transform VCL.xcu to create FontSubstitutions.xml,
               which unmarshalls into more intuitive Java.  -->
 
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="node[@oor:name='FontSubstitutions']">
		<FontSubstitutions>		
			<xsl:apply-templates/>
		</FontSubstitutions>	
	</xsl:template>
	<xsl:template match="node[@oor:name='en']" priority="2">
			<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="node[@oor:op='replace']">
		<replace>
<!--			<xsl:copy-of select="@oor:name"/>-->
			<xsl:attribute name="name">
				<xsl:value-of select="@oor:name"/>
			</xsl:attribute>
			<!-- Ensure consistent order, to simplify schema -->
			<xsl:apply-templates select="prop[@oor:name='SubstFonts']"/>
			<xsl:apply-templates select="prop[@oor:name='SubstFontsMS']"/>
			<xsl:apply-templates select="prop[@oor:name='SubstFontsPS']"/>
			<xsl:apply-templates select="prop[@oor:name='SubstFontsHTML']"/>
			<xsl:apply-templates select="prop[@oor:name='FontWeight']"/>
			<xsl:apply-templates select="prop[@oor:name='FontWidth']"/>
			<xsl:apply-templates select="prop[@oor:name='FontType']"/>
		</replace>	
	</xsl:template>	
	<xsl:template match="prop">
		<xsl:element name="{@oor:name}">
			<xsl:value-of select="value"/>
		</xsl:element>
	</xsl:template>	
</xsl:stylesheet>
