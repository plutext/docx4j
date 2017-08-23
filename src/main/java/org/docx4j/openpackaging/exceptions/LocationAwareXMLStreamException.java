package org.docx4j.openpackaging.exceptions;


public class LocationAwareXMLStreamException extends Exception {

	 javax.xml.stream.Location location;
	
	public javax.xml.stream.Location getLocation() {
		return location;
	}

	public LocationAwareXMLStreamException(String msg, javax.xml.stream.XMLStreamException e, javax.xml.stream.Location location) {
		super(msg, e);
		this.location = location;
	}
	
//	public LocationAwareXMLStreamException(String msg) {
//		super(msg);
//	}
	
//	public LocationAwareXMLStreamException(String msg, Exception e) {
//		super(msg, e);
//	}

//	public LocationAwareXMLStreamException(String msg, Throwable t) {
//		super(msg, t);
//	}
	

}
