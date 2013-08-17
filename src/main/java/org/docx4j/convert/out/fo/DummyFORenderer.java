package org.docx4j.convert.out.fo;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.docx4j.convert.out.FORenderer;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** The Dummy FO Renderer doesn't render anything, it just outputs
 *  the fo document to the OutputStream. 
 * 
 */
public class DummyFORenderer implements FORenderer {
	protected static Logger log = LoggerFactory.getLogger(DummyFORenderer.class);
	protected static final FORenderer INSTANCE = new DummyFORenderer();
	
	public static FORenderer getInstance() {
		return INSTANCE;
	}
	
	@Override
	public void render(String foDocument, FOSettings settings,
			boolean twoPass,
			List<SectionPageInformation> pageNumberInformation,
			OutputStream outputStream) throws Docx4JException {
	Writer writer = null;
		if (twoPass) {
			log.warn("Using the DummyFORenderer with a two pass conversion, there might be placeholders in the output");
		}
		try {
			writer = new OutputStreamWriter(outputStream, "UTF-8");
			writer.write(foDocument);
			writer.flush();
		} catch (Exception e) {
			throw new Docx4JException("Exception while storing fo document to OutputStream: " + e.getMessage(), e);
		}
	}

}
