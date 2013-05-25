package org.docx4j.convert.out.pdf.viaXSLFO;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.writer.AbstractHyperlinkWriter;

import org.docx4j.model.HyperlinkModel;
import org.docx4j.wml.P;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class HyperlinkWriter extends AbstractHyperlinkWriter {
	
	  private final static Logger log = Logger.getLogger(HyperlinkWriter.class);
	  
/*
 * Replace:
 *     <fo:basic-link color="blue" text-decoration="underline" >
	<xsl:variable name="relId"><xsl:value-of select="string(@r:id)"/></xsl:variable>
      
	<xsl:variable name="hTemp" 
		select="java:org.docx4j.convert.out.Converter.resolveHref(
		             $conversionContext, $relId )" />
		                   
      <xsl:variable name="href">
          <xsl:value-of select="$hTemp"/>
        <xsl:choose>
          <xsl:when test="@w:anchor"><xsl:value-of select="@w:anchor"/></xsl:when>
          <xsl:when test="@w:bookmark"><xsl:value-of select="@w:bookmark"/></xsl:when>
          <xsl:when test="@w:arbLocation"><xsl:value-of select="@w:arbLocation"/></xsl:when>
        </xsl:choose>
      </xsl:variable>
      
        <xsl:choose>
          <xsl:when test="@w:bookmark">
            <xsl:attribute name="internal-destination"><xsl:value-of select="$href"/></xsl:attribute>
          </xsl:when>
		<!--  TODO: id for anchor, heading etc? Re headers, I think Word may just insert a bookmark -->
          <xsl:when test="@w:arbLocation">
            <xsl:attribute name="internal-destination"><xsl:value-of select="$href"/></xsl:attribute>
          </xsl:when>
          <xsl:when test="@w:anchor">
            <xsl:attribute name="internal-destination"><xsl:value-of select="$href"/></xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
          	<!--  TODO file? -->
	        <xsl:attribute name="external-destination">url(<xsl:value-of select="$href"/>)</xsl:attribute>          
          </xsl:otherwise>
        </xsl:choose>
      
 		<xsl:apply-templates />      
    </fo:basic-link>
    
 */
	  @Override
	  protected Node toNode(AbstractWmlConversionContext context, 
			  HyperlinkModel hm, Document doc) throws TransformerException {
		  
		    P.Hyperlink hyperlink = hm.getHyperlink();	  
		  
		  	String hTemp = resolveHref(context, hyperlink.getId() );
		  	hTemp = this.convertExtension(hTemp, context.getHyperlinkExtensionMappings());
		  	
		  	
		    Element foBasicLink = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:basic-link");
		    foBasicLink.setAttribute("color", "blue" );		    
		    foBasicLink.setAttribute("text-decoration", "underline" );		    

		  	if (hyperlink.getAnchor()!=null) {
			    foBasicLink.setAttribute("internal-destination", hTemp+"#"+ hyperlink.getAnchor());		    
		  	} else if (hyperlink.getDocLocation()!=null) {
		  		// review this.
			    foBasicLink.setAttribute("internal-destination", hTemp+"#"+ hyperlink.getDocLocation());		    
		  	} else {
			    foBasicLink.setAttribute("external-destination", hTemp); // URL??		    		  		
		  	}
		    
			XmlUtils.treeCopy(hm.getContent(), foBasicLink);
			
			DocumentFragment docfrag = doc.createDocumentFragment();	    
		    docfrag.appendChild(foBasicLink);
			
		    
	    
	    return docfrag;
	  }
	  
	  
	}
