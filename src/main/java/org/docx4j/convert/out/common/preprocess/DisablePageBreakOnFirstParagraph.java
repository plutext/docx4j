package org.docx4j.convert.out.common.preprocess;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;

/**
 * Workaround for https://issues.apache.org/bugzilla/show_bug.cgi?id=54094  
 * You can disable this step if you are using FOP post 1.1
 */
public class DisablePageBreakOnFirstParagraph {
	public static void process(WordprocessingMLPackage wmlPackage) {
		Object o = wmlPackage.getMainDocumentPart().getContent().get(0);
		if (o instanceof P
				&& ((P)o).getPPr()!=null) {
			PPr pPr = ((P)o).getPPr();
			BooleanDefaultTrue val = new BooleanDefaultTrue();
			val.setVal(Boolean.FALSE);
			pPr.setPageBreakBefore(val);
		}
	}
}
