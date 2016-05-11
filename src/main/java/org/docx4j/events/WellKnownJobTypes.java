package org.docx4j.events;

/**
 * A job is generally understood to relate to multiple documents (pkgs).
 * @author jharrop
 *
 */
public enum WellKnownJobTypes implements JobIdentifier {
	
	ANONYMOUS,
	BIND,
	//DOCX4J_INIT,
	MERGE; // a job can involve multiple documents (packages)

}
