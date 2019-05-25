module docx4j_diffx {

	requires org.slf4j;
	requires docx4j_core;
	requires docx4j_openxml_objects;
    
    exports com.topologi.diffx;
    exports com.topologi.diffx.algorithm;
    exports com.topologi.diffx.config;
    exports com.topologi.diffx.event;
    exports com.topologi.diffx.event.impl;
    exports com.topologi.diffx.format;
    exports com.topologi.diffx.load;
    exports com.topologi.diffx.load.text;
    exports com.topologi.diffx.sequence;
    exports com.topologi.diffx.util;
    exports com.topologi.diffx.xml;
    exports com.topologi.diffx.xml.dom;
    exports com.topologi.diffx.xml.esc;
    exports com.topologi.diffx.xml.sax;

    exports org.eclipse.compare;
    exports org.eclipse.compare.internal;
    exports org.eclipse.compare.rangedifferencer;

    exports org.docx4j.diff;
    
}
