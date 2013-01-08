package org.docx4j.convert.out;

import java.util.Map;

import org.docx4j.model.PropertyResolver;
import org.docx4j.model.TransformState;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;

public abstract class AbstractWmlConversionContext extends AbstractConversionContext {
	
	private Map<String, TransformState> transformStates = null;
	private AbstractModelRegistry modelRegistry = null;
	
	//The part for the part tracker 
	protected Part currentPart = null;
	
	//The counters for the footnote and endnote number 
	protected int footnoteNumberCounter = 0;
	protected int endnoteNumberCounter = 0;
	
	

	protected AbstractWmlConversionContext(AbstractModelRegistry modelRegistry, AbstractMessageWriter messageWriter, AbstractConversionSettings conversionSettings) {
		super(messageWriter, conversionSettings);
		this.modelRegistry = initializeModelRegistry(modelRegistry);
		this.transformStates = initializeTransformStates();
	}

	@Override
	protected OpcPackage initializeOpcPackage(AbstractConversionSettings conversionSettings, OpcPackage opcPackage) {
	OpcPackage ret = super.initializeOpcPackage(conversionSettings, opcPackage);
		if (!(ret instanceof WordprocessingMLPackage)) {
			throw new IllegalArgumentException("The opcPackage isn't a WordprocessingMLPackage, it is a " + ret.getClass().getName());
		}
		return ret;
	}

	protected AbstractModelRegistry initializeModelRegistry(AbstractModelRegistry registry) {
		return registry;
	}
	
	protected Map<String, TransformState> initializeTransformStates() {
		return getModelRegistry().createTransformStates();
	}
	
	public TransformState getTransformState(String name) {
		return (transformStates != null ? transformStates.get(name) : null);
	}
	
	public WordprocessingMLPackage getWmlPackage() {
		return (WordprocessingMLPackage)getOpcPackage();
	}
	
	public AbstractModelRegistry getModelRegistry() {
		return modelRegistry;
	}
	
	public PropertyResolver getPropertyResolver() {
		return getWmlPackage().getMainDocumentPart().getPropertyResolver();
	}
	
    public int getNextEndnoteNumber() {
    	return ++endnoteNumberCounter;
    }

    public int getNextFootnoteNumber() {
    	return ++footnoteNumberCounter;
    }

	public void setCurrentPart(Part currentPart) {
		this.currentPart = currentPart;
	}

	public Part getCurrentPart() {
		return currentPart;		
	}
	
	public void setCurrentPartMainDocument() {
		setCurrentPart(getWmlPackage().getMainDocumentPart());
	}
}
