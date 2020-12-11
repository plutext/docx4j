For these examples, a pre-requisite is a remote server, where you have installed:
- Microsoft Word 
- documents4j server

The easiest way to install documents4j server is to build the documents4j-server-standalone-shaded.jar from
the source code at https://github.com/documents4j/documents4j and then run it using:

    java -jar documents4j-server-standalone-shaded.jar http://[SERVER_IP]:9998
    
This zip file provides the client end code.  You'll need to configure it to point at your
server.  This is done in your docx4j properties file, or programmatically:

		Docx4jProperties.setProperty("docx4j.convert.out.documents4j.remote.Uri", "http://192.168.0.31:9998");

Unless you are using Maven, these samples all require:
- the Documents4j remote jar files and the docx4j-documents4j-remote jar (provided in this zip)
- docx4j and its deps (provided in the separate community zip)
- unless you are using a Java which ships with JAXB, the one of docx4j-JAXB-ReferenceImpl|MOXy

If you are using Maven, just use the pom provided.

Then you should be able to run one of the samples in the src dir.
		
		