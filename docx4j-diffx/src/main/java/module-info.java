module org.docx4j.docx4j_diffx {

	requires org.slf4j;
	requires org.docx4j.core;
	requires jakarta.xml.bind;

	// automatic modules (no Automatic-Module-Name manifest entries as at pso-diffx 1.3.4 / pso-xmlwriter 1.1.1)
	requires pso.diffx;
	requires pso.xmlwriter;

    exports org.eclipse.compare;
    exports org.eclipse.compare.internal;
    exports org.eclipse.compare.rangedifferencer;

    exports org.docx4j.diff;
    opens org.docx4j.diff;  // so the XSLT can be loaded by ResourceUtils

}
