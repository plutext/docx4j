package org.docx4j.openpackaging.exceptions;

/**
 * Marker interface for any exception indicating that an OpenXML package 
 * is structurally flawed or invalid.
 */
public interface FlawedPackage {

	// defining these ensures any implementing class is a Throwable.
    String getMessage();
    Throwable getCause();
}