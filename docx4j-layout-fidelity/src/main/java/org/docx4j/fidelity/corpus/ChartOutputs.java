/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.fidelity.corpus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.pptx4j.convert.out.svginhtml.SvgExporter;

/**
 * docx4j's outputs of Office's own charts, for CR-037 phase 3's gate
 * ({@code tools/chart_outputs.py}): each docx through the PDF output (the visitor, or the XSLT
 * with {@code -Dxslt=true}), each pptx's first slide through the SVG-in-HTML output, its page the
 * slide's size so that a browser prints it at PowerPoint's place.  Number format 14's dates in
 * the VM's short date ({@code -DshortDate}, d/mm/yyyy by default), as Office drew them.
 *
 * <pre>
 * java -cp "$CP" org.docx4j.fidelity.corpus.ChartOutputs pdf  in.docx=out.pdf ...
 * java -cp "$CP" org.docx4j.fidelity.corpus.ChartOutputs html in.pptx=out.xhtml ...
 * </pre>
 *
 * Each output's time is printed (milliseconds, the load included).
 */
public class ChartOutputs {

	public static void main(String[] args) throws Exception {
		if (args.length < 2 || !("pdf".equals(args[0]) || "html".equals(args[0]))) {
			System.err.println("usage: ChartOutputs pdf|html in=out ...");
			System.exit(2);
		}
		Docx4jProperties.setProperty("docx4j.chart.render.shortDate", System.getProperty("shortDate", "d/mm/yyyy"));
		boolean xslt = Boolean.getBoolean("xslt");
		for (int i = 1; i < args.length; i++) {
			int eq = args[i].lastIndexOf('=');
			File in = new File(args[i].substring(0, eq)), out = new File(args[i].substring(eq + 1));
			long t0 = System.nanoTime();
			if ("pdf".equals(args[0])) {
				WordprocessingMLPackage pkg = WordprocessingMLPackage.load(in);
				FOSettings settings = Docx4J.createFOSettings();
				settings.setOpcPackage(pkg);
				settings.setApacheFopMime("application/pdf");
				try (OutputStream os = new FileOutputStream(out)) {
					Docx4J.toFO(settings, os, xslt ? Docx4J.FLAG_EXPORT_PREFER_XSL : Docx4J.FLAG_EXPORT_PREFER_NONXSL);
				}
			} else {
				PresentationMLPackage pkg = (PresentationMLPackage) PresentationMLPackage.load(in);
				String html = SvgExporter.svg(pkg, pkg.getMainPresentationPart().getSlide(0));
				long cx = pkg.getMainPresentationPart().getJaxbElement().getSldSz().getCx();
				long cy = pkg.getMainPresentationPart().getJaxbElement().getSldSz().getCy();
				String page = "<style>@page { size: " + cx / 12700.0 + "pt " + cy / 12700.0 + "pt; margin: 0 } body { margin: 0 }</style>";
				try (Writer w = new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8)) {
					w.write(html.replaceFirst("</head>", page + "</head>"));
				}
			}
			System.out.println(out.getName() + "\t" + (System.nanoTime() - t0) / 1000000);
		}
	}
}
