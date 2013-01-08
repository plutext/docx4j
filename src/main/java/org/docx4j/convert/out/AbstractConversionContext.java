package org.docx4j.convert.out;

import java.util.HashMap;
import java.util.Map;

import org.docx4j.model.images.ConversionImageHandler;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.traversal.NodeIterator;

public abstract class AbstractConversionContext {
	public static final String CONVERSION_CONTEXT_ID = "conversionContext";
	
	protected static final AbstractMessageWriter DUMMY_WRITER = new AbstractMessageWriter() {
		@Override
		public DocumentFragment notImplemented(NodeIterator nodes, String message) {return null;}
		@Override
		public DocumentFragment message(String message) {return null;}
		@Override
		protected String getOutputSuffix() {return null;}
		@Override
		protected String getOutputPrefix() {return null;}
	};
	
	private Map<String, Object> xsltParameters = null;
	private OpcPackage opcPackage = null;
	private ConversionImageHandler imageHandler = null;
	private AbstractMessageWriter messageWriter = null;

	protected AbstractConversionContext(AbstractMessageWriter messageWriter, AbstractConversionSettings conversionSettings) {
		initializeSettings(conversionSettings);
		this.messageWriter = initializeMessageWriter(messageWriter);
	}
	
	protected void initializeSettings(AbstractConversionSettings settings) {
		if (settings != null) {
			if (settings.getWmlPackage() == null) {
				throw new IllegalArgumentException("The OpcPackage is missing in the settings.");
			}
			this.imageHandler = initializeImageHandler(settings, settings.getImageHandler());
			this.opcPackage = initializeOpcPackage(settings, settings.getWmlPackage());
			this.xsltParameters = initializeXsltParameters(settings, settings.getSettings());
		}
	}

	protected ConversionImageHandler initializeImageHandler(AbstractConversionSettings settings, ConversionImageHandler handler) {
		return handler;
	}

	protected OpcPackage initializeOpcPackage(AbstractConversionSettings settings, OpcPackage opcPackage) {
		return opcPackage;
	}

	protected Map<String, Object> initializeXsltParameters(AbstractConversionSettings settings, Map<String, Object> settingParameters) {
	Map<String, Object> ret = new HashMap<String, Object>();
		if (settingParameters != null) {
			ret.putAll(settingParameters);
			//remove from the parameters items that get duplicated in the context
			ret.remove(AbstractConversionSettings.IMAGE_HANDLER);
			ret.remove(AbstractConversionSettings.WML_PACKAGE);
	
			//these are implicitly included in the ImageHandler
			ret.remove(AbstractConversionSettings.IMAGE_INCLUDE_UUID);
			ret.remove(AbstractConversionSettings.IMAGE_DIR_PATH);
		}
		ret.put(CONVERSION_CONTEXT_ID, this);
		return ret;
	}

	protected AbstractMessageWriter initializeMessageWriter(AbstractMessageWriter writer) {
		return (writer != null ? writer : DUMMY_WRITER);
	}
	
	//Only for the subclasses
	protected OpcPackage getOpcPackage() {
		return opcPackage;
	}

	public ConversionImageHandler getImageHandler() {
		return imageHandler;
	}

	public AbstractMessageWriter getMessageWriter() {
		return messageWriter;
	}

	public Map<String, Object> getXsltParameters() {
		return xsltParameters;
	}
	

}
