module org.docx4j.chart.render {

	requires org.slf4j;
	// transitive: the api's types are docx4j-core's (ChartFonts, ThemeColors), and through it the
	// generated objects' (a:spPr, a:txPr) and AWT's
	requires transitive org.docx4j.core;
	requires org.docx4j.generated_objects;
	requires jakarta.xml.bind;

	requires org.apache.xmlgraphics.batik.svggen;
	requires org.apache.xmlgraphics.batik.dom;
	requires org.apache.xmlgraphics.batik.awt.util; // SVGGraphics2D's superclass (Eclipse wants it named)

	// the renderer's frame, for a renderer of other charts (a chartex renderer: CR-039, CR-037 D7);
	// the renderer itself stays internal, reached through docx4j-core's ChartRenderer
	exports org.docx4j.chart.render.api;

	provides org.docx4j.model.chart.ChartRenderer
		with org.docx4j.chart.render.ClassicChartRenderer;

}
