package org.docx4j.model.datastorage;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.docx4j.XmlUtils;
import org.docx4j.utils.ResourceUtils;

public class XsltProviderImpl implements XsltProvider {

    // Cache to store compiled Templates, keyed by filename
    private final Map<String, Templates> cache = new ConcurrentHashMap<>();

    @Override
    public Templates getFinisherXslt(String filename) throws TransformerConfigurationException {
        // computeIfAbsent handles the "check-then-act" race condition automatically
        try {
            return cache.computeIfAbsent(filename, key -> {
                try {
                    return compileTemplates(key);
                } catch (TransformerConfigurationException e) {
                    // Wrap checked exception to throw it out of the lambda
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof TransformerConfigurationException) {
                throw (TransformerConfigurationException) e.getCause();
            }
            throw e;
        }
    }

    private Templates compileTemplates(String filename) throws TransformerConfigurationException {
        Source xsltSource;
        try {
            xsltSource = new StreamSource(
                        		ResourceUtils.getResource(filename));
        } catch (IOException e) {
            throw new TransformerConfigurationException("Failed to load XSLT resource: " + e.getMessage(), e);
        }
        return XmlUtils.getTransformerTemplate(xsltSource);
    }
    
    /**
     * Optional: Clear the cache if XSLT files are updated without restarting the app.
     */
    public void flushCache() {
        cache.clear();
    }
}