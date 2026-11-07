/*
 * Copyright 2026, Plutext Pty Ltd.
 * This file is part of docx4j.
 */
import java.io.*;
import java.util.*;
import org.docx4j.TextUtils;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.*;
import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
/** Scratch: what WORD_READS selects at the root, and the documents in that branch, the tree untouched. */
public class RootTrial {
	public static void main(String[] a) throws Exception {
		for (String f : a) {
			WordprocessingMLPackage p = WordprocessingMLPackage.load(new File(f));
			Object root = p.getMainDocumentPart().getJaxbElement();
			AlternateContent ac = (AlternateContent) root;
			Object b = McSplice.WORD_READS.select(ac);
			String n = new File(f).getName().replace(".docx", "");
			if (b == null) { System.out.println(n + "\tbranch=none (WORD_READS keeps it whole)"); continue; }
			List<Object> any = b instanceof AlternateContent.Choice ? ((AlternateContent.Choice) b).getAny() : ((AlternateContent.Fallback) b).getAny();
			StringBuilder words = new StringBuilder(), kinds = new StringBuilder();
			SectPr last = null;
			for (Object o : any) {
				Object u = XmlUtils.unwrap(o);
				kinds.append(u.getClass().getSimpleName()).append(' ');
				if (u instanceof Document) {
					StringWriter w = new StringWriter();
					TextUtils.extractText(u, w);
					words.append(w).append(" / ");
					last = ((Document) u).getBody().getSectPr();
				}
			}
			String page = last == null ? "?" : last.getPgSz().getW() + "x" + last.getPgSz().getH() + " top " + last.getPgMar().getTop();
			System.out.println(n + "\tbranch=" + b.getClass().getSimpleName() + "\t" + kinds + "\t" + words + "\tlast section " + page);
		}
	}
}
