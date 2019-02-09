package org.docx4j.samples;

public abstract class AbstractSample {
	
	protected static String inputfilepath;	
	protected static String outputfilepath;
	
	protected static void getInputFilePath(String[] args) throws IllegalArgumentException {

		if (args.length==0) throw new IllegalArgumentException("Input file arg missing");

		inputfilepath = args[0];
	}
	
	protected static void getOutputFilePath(String[] args) throws IllegalArgumentException {

		if (args.length<2) throw new IllegalArgumentException("Output file arg missing");

		outputfilepath = args[1];
	}
	

}
