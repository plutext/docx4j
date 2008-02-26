<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:oor="http://openoffice.org/2001/registry">

<!-- 
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 * -->

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
