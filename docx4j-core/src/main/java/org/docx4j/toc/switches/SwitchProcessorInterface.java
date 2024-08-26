package org.docx4j.toc.switches;

import org.docx4j.model.PropertyResolver;
import org.docx4j.toc.StyleBasedOnHelper;
import org.docx4j.toc.TocEntry;

public interface SwitchProcessorInterface {
	
	public TocEntry getEntry();
	
	public void setPageNumbers(boolean pageNumbers);

	public boolean isStyleFound();

	public StyleBasedOnHelper getStyleBasedOnHelper();

	public void setStyleFound(boolean b);
	
	public PropertyResolver getPropertyResolver();
	
	// Only used by USwitch, which can force stop
	public void setProceed(boolean proceed);
}
