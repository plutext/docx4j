package org.docx4j.convert.out.common;

import java.io.IOException;
import java.io.OutputStream;

import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exporter are responsible to create the environment for the export process. 
 * They take care of the pre- and post processing (FORenderer). 
 * The conversion process itself is done with the …ExporterDelegate
 * 
 * @since 3.0
 */
public abstract class AbstractExporter<CS extends AbstractConversionSettings, CC extends AbstractConversionContext, PK extends OpcPackage> implements Exporter<CS> {
	protected static Logger log = LoggerFactory.getLogger(AbstractExporter.class);
	
	protected AbstractExporter() {
	}
	
	@Override
	public void export(CS conversionSettings, OutputStream outputStream) throws Docx4JException {
	PK preprocessedPackage = null;
	ConversionSectionWrappers sectionWrappers = null;
	CC conversionContext = null;
	OutputStream intermediateOutputStream = null;
	long startTime = System.currentTimeMillis();
	long currentTime = startTime;
	
		try {
			log.debug("Start conversion");
			preprocessedPackage = preprocess(conversionSettings);
			currentTime = logDebugStep("Preprocessing", currentTime);
			sectionWrappers = createWrappers(conversionSettings, preprocessedPackage);
			currentTime = logDebugStep("Create section wrappers", currentTime);
			conversionContext = createContext(conversionSettings, preprocessedPackage, sectionWrappers);
			currentTime = logDebugStep("Create conversion context", currentTime);
			intermediateOutputStream = createIntermediateOutputStream(outputStream);
			process(conversionSettings, conversionContext, intermediateOutputStream);
			currentTime = logDebugStep("Processing", currentTime);
			postprocess(conversionSettings, conversionContext, intermediateOutputStream, outputStream);
			currentTime = logDebugStep("Postprocessing", currentTime);
			logDebugStep("Conversion done", startTime);
			
		} catch (Exception e) {
			log.error("Exception exporting package", e);
			throw new Docx4JException("Exception exporting package", e);
		} finally {
			// Clean-up
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected long logDebugStep(String stepLabel, long startTime) {
	long currentTime = 0;
		if (log.isDebugEnabled()) {
			currentTime = System.currentTimeMillis();
			log.debug(stepLabel + ", " + Long.toString(currentTime - startTime) + "ms");
		}
		return currentTime;
	}

	protected abstract PK preprocess(CS conversionSettings) throws Docx4JException;

	protected abstract ConversionSectionWrappers createWrappers(CS conversionSettings,  PK preprocessedPackage) throws Docx4JException;

	protected abstract CC createContext(CS conversionSettings, PK preprocessedPackage, ConversionSectionWrappers sectionWrappers);

	protected OutputStream createIntermediateOutputStream(OutputStream outputStream) throws Docx4JException {
		//default: no intermediate OutputStream needed (html)
		return outputStream;
	}

	protected abstract void process(CS conversionSettings, CC conversionContext, OutputStream outputStream) throws Docx4JException;
	
	protected void postprocess(CS conversionSettings, AbstractConversionContext conversionContext, OutputStream intermediateOutputStream, OutputStream outputStream) throws Docx4JException {
		//default: no postprocess needed, output has been done on target OutputStream
	}
	
}
