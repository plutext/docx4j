package org.glox4j.samples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.XmlUtils;
import org.glox4j.openpackaging.packages.GloxPackage;

public class ParseGlox  {
	
	private static Logger log = LoggerFactory.getLogger(ParseGlox.class);						

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/Hier2Level.glox";
//		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/Picture Organization Chart.glox";
		
		GloxPackage gloxPackage = GloxPackage.load(new java.io.File(inputfilepath));
		
		String title0 = gloxPackage.getDiagramLayoutHeaderPart().getJaxbElement().getTitle().get(0).getVal();
		System.out.println("Title: " + title0);
		
		String desc0 = gloxPackage.getDiagramLayoutHeaderPart().getJaxbElement().getDesc().get(0).getVal();
		System.out.println("Description: " + desc0);
		
		String layoutXml = XmlUtils.marshaltoString(gloxPackage.getDiagramLayoutPart().getJaxbElement() , true, true);
		System.out.println(layoutXml);
		

	}
}

