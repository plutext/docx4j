module org.docx4j.chart.render {

	requires org.slf4j;
	requires org.docx4j.core;
	requires org.docx4j.generated_objects;
	requires jakarta.xml.bind;

	requires org.apache.xmlgraphics.batik.svggen;
	requires org.apache.xmlgraphics.batik.dom;

	// nothing exported: the renderer's insides stay internal until the Enterprise jar needs
	// them (CR-037 D7); it is reached through docx4j-core's ChartRenderer
	provides org.docx4j.model.chart.ChartRenderer
		with org.docx4j.chart.render.ClassicChartRenderer;

}
