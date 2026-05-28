package org.docx4j.convert.out.fo.renderers;

import java.util.ArrayList;
import java.util.List;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.fonts.CustomFontCollection;
import org.apache.fop.fonts.DefaultFontConfigurator;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontConfigurator;
import org.apache.fop.fonts.FontEventAdapter;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.fonts.base14.Base14FontCollection;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;
import org.apache.fop.render.pdf.PDFDocumentHandler;
import org.apache.fop.render.pdf.PDFRendererConfig;
import org.apache.fop.render.pdf.PDFRendererConfig.PDFRendererConfigParser;

/**
 * @since 11.5.14
 */
public final class NonCachingPdfDocumentHandlerConfigurator
        implements IFDocumentHandlerConfigurator {

    private final FOUserAgent userAgent;
    private final Configuration suppliedPdfRendererConfig;

    public NonCachingPdfDocumentHandlerConfigurator(
            FOUserAgent userAgent,
            Configuration suppliedPdfRendererConfig) {
        this.userAgent = userAgent;
        this.suppliedPdfRendererConfig = suppliedPdfRendererConfig;
    }

    @Override
    public void configure(IFDocumentHandler documentHandler)
            throws FOPException {
        /*
         * No-op.
         *
         * PDFDocumentHandler.mergeRendererOptionsConfig(...) is package-private /
         * not visible outside org.apache.fop.render.pdf.
         *
         * For a font-only override, we do not need to merge PDF renderer options here.
         */
    }

    @Override
    public void setupFontInfo(String mimeType, FontInfo fontInfo)
            throws FOPException {

        PDFRendererConfig config = parsePdfConfig();

        FontManager fontManager = userAgent.getFontManager();

        List<FontCollection> fontCollections =
                new ArrayList<FontCollection>();

        fontCollections.add(new Base14FontCollection(
                fontManager.isBase14KerningEnabled()));

        FontConfigurator fontConfigurator =
                new DefaultFontConfigurator(
                        fontManager,
                        new FontEventAdapter(userAgent.getEventBroadcaster()),
                        userAgent.validateUserConfigStrictly());

        List<EmbedFontInfo> embedFontInfoList =
                fontConfigurator.configure(config.getFontInfoConfig());

        fontCollections.add(new CustomFontCollection(
                fontManager.getResourceResolver(),
                embedFontInfoList,
                userAgent.isComplexScriptFeaturesEnabled()));

        fontManager.setup(
                fontInfo,
                fontCollections.toArray(
                        new FontCollection[fontCollections.size()]));
    }

    private PDFRendererConfig parsePdfConfig() throws FOPException {
        RendererConfig config = new PDFRendererConfigParser()
                .build(userAgent, suppliedPdfRendererConfig);

        return (PDFRendererConfig) config;
    }
}