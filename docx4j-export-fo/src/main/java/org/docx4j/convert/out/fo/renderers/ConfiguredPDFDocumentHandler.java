package org.docx4j.convert.out.fo.renderers;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.DefaultConfigurationBuilder;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;
import org.apache.fop.render.pdf.PDFDocumentHandler;
import org.apache.fop.render.pdf.PDFRendererConfigurator;
import org.docx4j.XmlUtils;
import org.docx4j.fonts.fop.util.FopConfigUtil;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 11.5.14
 */
public class ConfiguredPDFDocumentHandler extends PDFDocumentHandler {
	
	protected static Logger log = LoggerFactory.getLogger(ConfiguredPDFDocumentHandler.class);

    private final Configuration pdfRendererConfiguration;
    private final String pdfRendererXml;

    public ConfiguredPDFDocumentHandler(
            IFContext context,
            org.docx4j.convert.out.fopconf.Fop fopconf) throws Docx4JException {
    	
        super(context);
        
        pdfRendererXml = XmlUtils.marshaltoString( 
        		FopConfigUtil.get(fopconf.getRenderers(), "application/pdf"), 
        		Context.getFopConfigContext());
		/*
		<fop version="1.0">
		    <renderers>
		        <renderer mime="application/pdf">
		            <fonts>
		                <font simulate-style="false" embed-url="file:/usr/share/fonts/TTF/DejaVuSerif-BoldItalic.ttf">
		                    <font-triplet name="DejaVu Serif" style="italic" weight="bold"/>
		                </font>
	 */
		
		try {
			pdfRendererConfiguration = parsePdfRendererConfiguration(pdfRendererXml);
		} catch (Exception e) {
			throw new Docx4JException(e.getMessage(), e);
		}	
    }
    

    
    private static Configuration parsePdfRendererConfiguration(String rendererXml)
            throws Exception {

        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();

        try (ByteArrayInputStream in = new ByteArrayInputStream(
                rendererXml.getBytes(StandardCharsets.UTF_8))) {
            return builder.build(in);
        }
    }    
    
    @Override
    public IFDocumentHandlerConfigurator getConfigurator() {
        FOUserAgent userAgent = getUserAgent();

        if (true) {
            return new NonCachingPdfDocumentHandlerConfigurator(
                    getUserAgent(),
                    pdfRendererConfiguration);        	
        } else {
        	// Caches the differing font sets found across documents.
        	// No appreciable difference in speed compared to NonCachingPdfDocumentHandlerConfigurator
        	// TODO if to be used:
        	// 1. fix error below.
        	// 2. if used, ensure fonts are written in some canonical order
	        return new PDFRendererConfigurator(
	                userAgent,
	                new DelegatingPDFRendererConfigParser(pdfRendererConfiguration, pdfRendererXml));
	        
	        /* NB, this sometimes causes errors like:
	         * 
				java.lang.NullPointerException: Cannot invoke "org.w3c.dom.Node.getNodeName()" because "n" is null
					at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.configuration.DefaultConfiguration.getChildren(DefaultConfiguration.java:140)
					at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.apps.FOUserAgent.getRendererConfiguration(FOUserAgent.java:691)
					at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.apps.FOUserAgent.getRendererConfig(FOUserAgent.java:668)
					at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.render.PrintRendererConfigurator.getRendererConfig(PrintRendererConfigurator.java:91)
					
					at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.render.PrintRendererConfigurator.getRendererConfig(PrintRendererConfigurator.java:80)

					at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.render.pdf.PDFRendererConfigurator.configure(PDFRendererConfigurator.java:45)
					at org.docx4j.export_fo/org.docx4j.convert.out.fo.renderers.FORendererApacheFOP.render(FORendererApacheFOP.java:268)
					at org.docx4j.export_fo/org.docx4j.convert.out.fo.renderers.FORendererApacheFOP.render(FORendererApacheFOP.java:183)
					at org.docx4j.export_fo/org.docx4j.convert.out.fo.AbstractFOExporter.postprocess(AbstractFOExporter.java:168)
					at org.docx4j.export_fo/org.docx4j.convert.out.fo.AbstractFOExporter.postprocess(AbstractFOExporter.java:1)
					at org.docx4j.core/org.docx4j.convert.out.common.AbstractExporter.export(AbstractExporter.java:83)
					... 7 more
					
					or
					
				java.lang.NullPointerException: Cannot invoke "org.w3c.dom.Node.getNodeName()" because "n" is null
					at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.configuration.DefaultConfiguration.getChildren(DefaultConfiguration.java:140)
					at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.apps.FOUserAgent.getRendererConfiguration(FOUserAgent.java:691)
					at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.apps.FOUserAgent.getRendererConfig(FOUserAgent.java:668)
					at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.render.PrintRendererConfigurator.getRendererConfig(PrintRendererConfigurator.java:91)
					
					at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.render.PrintRendererConfigurator.getCustomFontCollection(PrintRendererConfigurator.java:147)
					at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.render.PrintRendererConfigurator.setupFontInfo(PrintRendererConfigurator.java:127)

					at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.render.intermediate.IFUtil.setupFonts(IFUtil.java:170)
					at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.render.intermediate.IFRenderer.setupFontInfo(IFRenderer.java:187)
					at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.area.RenderPagesModel.<init>(RenderPagesModel.java:75)
					at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.area.AreaTreeHandler.setupModel(AreaTreeHandler.java:135)
					at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.area.AreaTreeHandler.<init>(AreaTreeHandler.java:105)
					at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.render.RendererFactory.createFOEventHandler(RendererFactory.java:363)
					at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.fo.FOTreeBuilder.<init>(FOTreeBuilder.java:109)
					at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.apps.Fop.createDefaultHandler(Fop.java:104)
					at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.apps.Fop.<init>(Fop.java:78)
					at org.apache.xmlgraphics.fop.core@2.11/org.apache.fop.apps.FOUserAgent.newFop(FOUserAgent.java:189)
					at org.docx4j.export_fo/org.docx4j.convert.out.fo.renderers.FORendererApacheFOP.render(FORendererApacheFOP.java:274)
					at org.docx4j.export_fo/org.docx4j.convert.out.fo.renderers.FORendererApacheFOP.render(FORendererApacheFOP.java:183)
					at org.docx4j.export_fo/org.docx4j.convert.out.fo.AbstractFOExporter.postprocess(AbstractFOExporter.java:168)
					at org.docx4j.export_fo/org.docx4j.convert.out.fo.AbstractFOExporter.postprocess(AbstractFOExporter.java:1)
					at org.docx4j.core/org.docx4j.convert.out.common.AbstractExporter.export(AbstractExporter.java:83)
					... 7 more					
		         */
        }
    }   
    
}



