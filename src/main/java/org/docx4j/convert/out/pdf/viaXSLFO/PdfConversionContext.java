package org.docx4j.convert.out.pdf.viaXSLFO;

import org.docx4j.convert.out.AbstractWmlConversionContext;
import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.convert.out.AbstractMessageWriter;
import org.docx4j.convert.out.AbstractModelRegistry;
import org.docx4j.model.images.ConversionImageHandler;
import org.w3c.dom.traversal.NodeIterator;

public class PdfConversionContext extends AbstractWmlConversionContext {
	protected InField inFieldTracker = new InField();
	
	//The model registry is per output type a singleton
	protected static final AbstractModelRegistry PDF_MODEL_REGISTRY = 
		new AbstractModelRegistry() {
			@Override
			protected void registerDefaultConverterInstances() {
				registerConverter(new TableWriter());
				registerConverter(new SymbolWriter());
			}
		};
			
	//The message writer for pdf	
	protected static final AbstractMessageWriter PDF_MESSAGE_WRITER = new AbstractMessageWriter() {
		@Override
		protected String getOutputPrefix() {
			return "<fo:block xmlns:fo=\"http://www.w3.org/1999/XSL/Format\"  " 
					+ "font-size=\"12pt\" "
		        	+ "color=\"red\" "
		        	+ "font-family=\"sans-serif\" "
		        	+ "line-height=\"15pt\" "
		        	+ "space-after.optimum=\"3pt\" "
		        	+ "text-align=\"justify\"> ";
		}
		@Override
		protected String getOutputSuffix() {
			return "</fo:block>";
		}
	};

	public PdfConversionContext(PdfSettings settings) {
		super(PDF_MODEL_REGISTRY, PDF_MESSAGE_WRITER, settings);
	}
	
	@Override
	protected ConversionImageHandler initializeImageHandler(AbstractConversionSettings settings, ConversionImageHandler handler) {
		if (handler == null) {
			//setup a private image handler if there is none in the configuration.
			handler = (settings.getImageDirPath() != null ? 
					new PDFConversionImageHandler(settings.getImageDirPath(), true) : 
					new PDFConversionImageHandler());
		}
		return handler;
	}
	
	protected InField getInFieldTracker() {
		return inFieldTracker;
	}
	
	public void inFieldUpdateState(NodeIterator fldCharNodeIt) {
		getInFieldTracker().updateState(fldCharNodeIt);
	}
	
	public boolean inFieldGetState() {
		return getInFieldTracker().getState();
	}
	
}
