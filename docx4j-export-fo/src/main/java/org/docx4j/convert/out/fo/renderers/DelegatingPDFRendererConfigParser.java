package org.docx4j.convert.out.fo.renderers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.render.RendererConfig.RendererConfigParser;
import org.apache.fop.render.pdf.PDFRendererConfig.PDFRendererConfigParser;

/**
 * @since 11.5.14
 */
public final class DelegatingPDFRendererConfigParser implements RendererConfigParser {
	
	/* The problem with this approach is that 
	 * FopFactory.getRendererConfig(...) caches renderer config per MIME type, 
	 * using configCreator.getMimeType() as the key.
	 * This cache is not designed to be disabled. 
	 * 
	 * So here instead we return a synthetic key.
	 * But we don't want to cache a gazillion different font combinations
	 * (eg in a public facing service).
	 * It might be ok though if there is a reasonable number of resulting
	 * cached configs.    
	 * 
	 * Because of this, ConfiguredPDFDocumentHandler DOES NOT CURRENTLY USE 
	 * this approach.  See comments there.
	 */

    private final Configuration suppliedPdfRendererConfig;
    private final String pdfRendererXml;
    
    private final PDFRendererConfigParser delegate = new PDFRendererConfigParser();

    public DelegatingPDFRendererConfigParser(
            Configuration suppliedPdfRendererConfig, 
            String pdfRendererXml) {
        this.suppliedPdfRendererConfig = suppliedPdfRendererConfig;
        this.pdfRendererXml = pdfRendererXml;
    }

    @Override
    public RendererConfig build(
            FOUserAgent userAgent,
            Configuration ignoredRendererConfiguration)
            throws FOPException {

        /*
         * Delegate to FOP's real PDF parser, but force it to parse
         * our supplied <renderer mime="application/pdf">...</renderer>
         * Configuration node.
         */
        return delegate.build(userAgent, suppliedPdfRendererConfig);
    }

    @Override
    public String getMimeType() {
    	
    	// return MimeConstants.MIME_PDF;    	
    	return "application/pdf;fonts=" + sha256Hex(pdfRendererXml);
    }
    
    private static String sha256Hex(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Could not compute SHA-256", e);
        }
    }    
    
}