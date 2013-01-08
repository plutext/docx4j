package org.pptx4j.convert.out.svginhtml;

import org.docx4j.convert.out.AbstractConversionContext;
import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.convert.out.AbstractMessageWriter;
import org.docx4j.convert.out.html.HTMLConversionImageHandler;
import org.docx4j.model.images.ConversionImageHandler;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.pptx4j.model.ResolvedLayout;

public class SvgConversionContext extends AbstractConversionContext {
	//The message writer for svg (the same as for html)	
	protected static final AbstractMessageWriter SVG_MESSAGE_WRITER = new AbstractMessageWriter() {
		@Override
		protected String getOutputSuffix() {
			return "<div style=\"color:red\" >";
		}
		@Override
		protected String getOutputPrefix() {
			return "</div>";
		}
	};		
	
	protected ResolvedLayout resolvedLayout = null;
	
	protected SvgConversionContext(SvgExporter.SvgSettings conversionSettings, ResolvedLayout resolvedLayout) {
		super(SVG_MESSAGE_WRITER, conversionSettings);
		this.resolvedLayout = resolvedLayout;
	}

	@Override
	protected void initializeSettings(AbstractConversionSettings settings) {
	SvgExporter.SvgSettings svgSettings = null;
		if (settings == null) {
			settings = new SvgExporter.SvgSettings();
		}
		else if (!(settings instanceof SvgExporter.SvgSettings)) {
			throw new IllegalArgumentException("The class of the settings isn't SvgExporter.SvgSettings, it is " + settings.getClass().getName());
		}
		svgSettings = (SvgExporter.SvgSettings)settings;
		super.initializeSettings(svgSettings);
	}

	@Override
	protected OpcPackage initializeOpcPackage(AbstractConversionSettings settings, OpcPackage opcPackage) {
	OpcPackage ret = super.initializeOpcPackage(settings, opcPackage);
		if (!(ret instanceof PresentationMLPackage)) {
			throw new IllegalArgumentException("The opcPackage isn't a PresentationMLPackage, it is a " + ret.getClass().getName());
		}
		return ret;
	}

	@Override
	protected ConversionImageHandler initializeImageHandler(AbstractConversionSettings settings, ConversionImageHandler handler) {
	SvgExporter.SvgSettings svgSettings = (SvgExporter.SvgSettings)settings;
		if (handler == null) {
			handler = new HTMLConversionImageHandler(
						svgSettings.getImageDirPath(), 
						svgSettings.getImageTargetUri(), 
						svgSettings.isImageIncludeUUID());
		}
		return handler;
	}

	public PresentationMLPackage getPmlPackage() {
		return (PresentationMLPackage)getOpcPackage();
	}
	
	public ResolvedLayout getResolvedLayout() {
		return resolvedLayout;
	}
	
}
