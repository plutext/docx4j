/* Copyright © 2026, Oracle and/or its affiliates. */
module docx4j.xjc.copy {
	requires org.glassfish.jaxb.xjc;
	requires static org.jvnet.jaxb.plugins.runtime;
	requires static jakarta.xml.bind;

	exports org.docx4j.xjc.copy;

	// XJC plugin service descriptor is also present in META-INF/services.
}
