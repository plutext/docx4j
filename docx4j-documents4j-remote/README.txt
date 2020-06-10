For this project, a pre-requisite is a remote server, where you have installed:
- Microsoft Word 
- documents4j server

The easiest way to install documents4j server is to build the documents4j-server-standalone-shaded.jar from
the source code at https://github.com/documents4j/documents4j and then run it using:

    java -jar documents4j-server-standalone-shaded.jar http://[SERVER_IP]:9998

For examples of how to perform conversion, please see docx4j-samples-documents4j-remote, which provides 
the client end code.  