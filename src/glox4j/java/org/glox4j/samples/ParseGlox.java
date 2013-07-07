package org.glox4j.samples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.XmlUtils;
import org.docx4j.samples.AbstractSample;
import org.glox4j.openpackaging.packages.GloxPackage;

public class ParseGlox extends AbstractSample {
	
	private static Logger log = LoggerFactory.getLogger(ParseGlox.class);						

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
//			inputfilepath = System.getProperty("user.dir") + "/sample-docs/glox/Hier2Level.glox";
			inputfilepath = System.getProperty("user.dir") + "/sample-docs/glox/Picture Organization Chart.glox";
		}
		
		GloxPackage gloxPackage = GloxPackage.load(new java.io.File(inputfilepath));
		
		String title0 = gloxPackage.getDiagramLayoutHeaderPart().getJaxbElement().getTitle().get(0).getVal();
		System.out.println("Title: " + title0);
		
		String desc0 = gloxPackage.getDiagramLayoutHeaderPart().getJaxbElement().getDesc().get(0).getVal();
		System.out.println("Description: " + desc0);
		
		String layoutXml = XmlUtils.marshaltoString(gloxPackage.getDiagramLayoutPart().getJaxbElement() , true, true);
		System.out.println(layoutXml);
		

	}
}

