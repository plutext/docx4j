package org.docx4j.convert.out.html;

import org.docx4j.convert.out.AbstractWmlConversionContext;
import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.convert.out.AbstractMessageWriter;
import org.docx4j.convert.out.AbstractModelRegistry;
import org.docx4j.convert.out.html.AbstractHtmlExporter.HtmlSettings;
import org.docx4j.fonts.Mapper;
import org.docx4j.model.images.ConversionImageHandler;

public class HTMLConversionContext extends AbstractWmlConversionContext {
	protected Mapper fontMapper = null;
	
	//The model registry is per output type a singleton
	protected static final AbstractModelRegistry HTML_MODEL_REGISTRY = 
		new AbstractModelRegistry() {
			@Override
			protected void registerDefaultConverterInstances() {
				registerConverter(new TableWriter());
				registerConverter(new SymbolWriter());
			}
		};

	//The message writer for html	
	protected static final AbstractMessageWriter HTML_MESSAGE_WRITER = new AbstractMessageWriter() {
		@Override
		protected String getOutputSuffix() {
			return "<div style=\"color:red\" >";
		}
		@Override
		protected String getOutputPrefix() {
			return "</div>";
		}
	};		
	
	public HTMLConversionContext(HtmlSettings settings) {
		super(HTML_MODEL_REGISTRY, HTML_MESSAGE_WRITER, settings);
	}

	@Override
	protected void initializeSettings(AbstractConversionSettings settings) {
	HtmlSettings htmlSettings = null;
		if (settings == null) {
			settings = new HtmlSettings();
		}
		else if (!(settings instanceof HtmlSettings)) {
			throw new IllegalArgumentException("The class of the settings isn't HtmlSettings, it is " + settings.getClass().getName());
		}
		htmlSettings = (HtmlSettings)settings;
		super.initializeSettings(htmlSettings);
		fontMapper = htmlSettings.getFontMapper();
		if (fontMapper == null) {
			fontMapper = getWmlPackage().getFontMapper();
		}
	}

	@Override
	protected ConversionImageHandler initializeImageHandler(AbstractConversionSettings settings, ConversionImageHandler handler) {
	HtmlSettings htmlSettings = (HtmlSettings)settings;
		if (handler == null) {
			handler = new HTMLConversionImageHandler(
						htmlSettings.getImageDirPath(), 
						htmlSettings.getImageTargetUri(), 
						htmlSettings.isImageIncludeUUID());
		}
		return handler;
	}

	public Mapper getFontMapper() {
		return fontMapper;
	}
}
