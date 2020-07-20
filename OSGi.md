OSGi
======


As of docx4j 8, docx4j is more OSGi-friendly.

docx4j supports your choice of JAXB implementation: the one in Oracle Java 6 to 8, the Reference Implementation, MOXy, or IBMs.  You'll need to specify your preferred docx4j-JAXB-* artifact; see below.

Build
---------------

First, check the docx4j version number in git/docx4j/docx4j-core/feature.xml matches  git/docx4j/pom.xml

What version of Java are you using?  If Java 8 and its internal JAXB, then in git/docx4j/pom.xml you may need to comment out the jakarta.xml.bind-api 2.3.2 dep. 

```
mvn install -POSGi
```

Or quickest, 

```
mvn install -POSGi -DskipTests
```

 

Sample project
---------------

https://github.com/plutext/docx4j-OSGi-HelloWorld is a sample project

HelloWorld implements BundleActivator, and shows that marshalling works:

```
        P p = Context.getWmlObjectFactory().createP();
        System.out.println(XmlUtils.marshaltoString(p));
```

Clone the repo, then update the pom to use your preferred docx4j-JAXB-* artifact, then 


```
mvn install
```

Note: I experienced some issues using Karaf 4.2.6 and MOXy; see https://stackoverflow.com/questions/62988480/karaf-wants-optional-imports

Run it in Karaf
---------------

I used Karaf 4.2.6 and openjdk version "1.8.0_242"

If you are using docx4j-JAXB-Internal, you'll need to edit etc/config.properties, adding com.sun.xml.internal.bind.marshaller to org.osgi.framework.system.packages.extra

```		
	org.osgi.framework.system.packages.extra = \
	    org.apache.karaf.branding, \
	    sun.misc, \
	    com.sun.xml.internal.bind.marshaller, \
```

Start karaf:

```
bin/karaf start
```

Download feature.xml then add its file URL as a repo:

```
karaf@root()> feature:repo-add file:///home/jharrop/git/docx4j/docx4j-core/feature.xml
                                   
Adding feature url file:///home/jharrop/git/docx4j/feature.xml
```

Install:

```
karaf@root()> feature:install docx4j-deps/8.2
```

Now you should see something like:

```
karaf@root()> bundle:list
START LEVEL 100 , List Threshold: 50
ID │ State  │ Lvl │ Version        │ Name
───┼────────┼─────┼────────────────┼─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
23 │ Active │  80 │ 4.2.6       │ Apache Karaf :: OSGi Services :: Event
45 │ Active │  80 │ 2.8.1       │ checker-qual
46 │ Active │  80 │ 2.9.0       │ Jackson-annotations
47 │ Active │  80 │ 2.9.9       │ Jackson-core
48 │ Active │  80 │ 2.9.9       │ jackson-databind
49 │ Active │  80 │ 3.0.0       │ Expression Language 3.0 API
50 │ Active │  80 │ 1.3.2       │ mbassador
51 │ Active │  80 │ 1.12.0      │ Apache Commons Codec
52 │ Active │  80 │ 1.18.0      │ Apache Commons Compress
53 │ Active │  80 │ 2.6.0       │ Apache Commons IO
54 │ Active │  80 │ 3.9.0       │ Apache Commons Lang
55 │ Active │  80 │ 8.2.1       │ docx4j-openxml-objects
56 │ Active │  80 │ 8.2.1       │ docx4j-openxml-objects-pml
57 │ Active │  80 │ 8.2.1       │ docx4j-openxml-objects-sml
58 │ Active │  80 │ 8.2.1       │ jaxb-svg11
59 │ Active │  80 │ 1.8.0.beta2 │ slf4j-api
60 │ Active │  80 │ 0           │ wrap_file__home_jharrop_.m2_repository_com_google_errorprone_error_prone_annotations_2.3.3_error_prone_annotations-2.3.3.jar
61 │ Active │  80 │ 0           │ wrap_file__home_jharrop_.m2_repository_com_thedeanda_lorem_2.1_lorem-2.1.jar
62 │ Active │  80 │ 0           │ wrap_file__home_jharrop_.m2_repository_net_arnx_wmf2svg_0.9.8_wmf2svg-0.9.8.jar
63 │ Active │  80 │ 0           │ wrap_file__home_jharrop_.m2_repository_org_antlr_antlr-runtime_3.5.2_antlr-runtime-3.5.2.jar
64 │ Active │  80 │ 0           │ wrap_file__home_jharrop_.m2_repository_org_antlr_stringtemplate_3.2.1_stringtemplate-3.2.1.jar
65 │ Active │  80 │ 0           │ wrap_file__home_jharrop_.m2_repository_org_apache_httpcomponents_httpclient_4.5.8_httpclient-4.5.8.jar
66 │ Active │  80 │ 0           │ wrap_file__home_jharrop_.m2_repository_org_apache_httpcomponents_httpcore_4.4.11_httpcore-4.4.11.jar
67 │ Active │  80 │ 0           │ wrap_file__home_jharrop_.m2_repository_org_apache_xmlgraphics_xmlgraphics-commons_2.3_xmlgraphics-commons-2.3.jar
68 │ Active │  80 │ 0           │ wrap_file__home_jharrop_.m2_repository_org_docx4j_org_apache_xalan-interpretive_8.0.0_xalan-interpretive-8.0.0.jar
69 │ Active │  80 │ 0           │ wrap_file__home_jharrop_.m2_repository_org_docx4j_org_apache_xalan-serializer_8.0.0_xalan-serializer-8.0.0.jar

```

Now let's try the sample project:

```
karaf@root()> bundle:install mvn:org.docx4j/docx4j-OSGi-HelloWorld/8.2.1
Bundle ID: 70
karaf@root()> bundle:start 70
Hello world.
SLF4J: No SLF4J providers were found.
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#noProviders for further details.
NamespacePrefixMapper: org.docx4j.jaxb.suninternal.NamespacePrefixMapper
<w:p ... />
Marshalling worked.
org.docx4j.wml.P
```

Run it in ServiceMix 7.0.1
--------------------------

For this, I used docx4j-JAXB-ReferenceImpl.  I made it depend on (instead of some other jaxb-impl):

```
<dependency>
  <groupId>org.apache.servicemix.bundles</groupId>
  <artifactId>org.apache.servicemix.bundles.jaxb-impl</artifactId>
  <version>2.2.11_1</version>
  <type>bundle</type>
</dependency>
```

and rebuilt it.  Following that, it works:

 ```____                  _          __  __ _      
/ ___|  ___ _ ____   _(_) ___ ___|  \/  (_)_  __
\___ \ / _ \ '__\ \ / / |/ __/ _ \ |\/| | \ \/ /
 ___) |  __/ |   \ V /| | (_|  __/ |  | | |>  < 
|____/ \___|_|    \_/ |_|\___\___|_|  |_|_/_/\_\

  Apache ServiceMix (7.0.1)

Hit '<tab>' for a list of available commands
and '[cmd] --help' for help on a specific command.
Hit '<ctrl-d>' or 'system:shutdown' to shutdown ServiceMix.

karaf@root>feature:repo-add file:///home/jharrop/git/docx4j-8.0.0/docx4j-core/feature.xml
Adding feature url file:///home/jharrop/git/docx4j-8.0.0/docx4j-core/feature.xml
karaf@root>feature:install docx4j-deps/8.0
karaf@root>bundle:install mvn:org.docx4j/docx4j-OSGi-HelloWorld/8.0.0-SNAPSHOT
Bundle ID: 246
karaf@root>bundle:start 246
Hello world.
SLF4J: No SLF4J providers were found.
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#noProviders for further details.
NamespacePrefixMapper: org.docx4j.jaxb.ri.NamespacePrefixMapper
<w:p xmlns:dsp="http://schemas.microsoft.com/office/drawing/2008/diagram" xmlns:cppr="http://schemas.microsoft.com/office/2006/coverPageProps" xmlns:odx="http://opendope.org/xpaths" xmlns:c14="http://schemas.microsoft.com/office/drawing/2007/8/2/chart" xmlns:xdr="http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing" xmlns:odgm="http://opendope.org/SmartArt/DataHierarchy" xmlns:w16se="http://schemas.microsoft.com/office/word/2015/wordml/symex" xmlns:wp14="http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing" xmlns:dgm="http://schemas.openxmlformats.org/drawingml/2006/diagram" xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture" xmlns:we="http://schemas.microsoft.com/office/webextensions/webextension/2010/11" xmlns:pvml="urn:schemas-microsoft-com:office:powerpoint" xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" xmlns:w10="urn:schemas-microsoft-com:office:word" xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" xmlns:sl="http://schemas.openxmlformats.org/schemaLibrary/2006/main" xmlns:w15="http://schemas.microsoft.com/office/word/2012/wordml" xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:comp="http://schemas.openxmlformats.org/drawingml/2006/compatibility" xmlns:b="http://schemas.openxmlformats.org/officeDocument/2006/bibliography" xmlns:c="http://schemas.openxmlformats.org/drawingml/2006/chart" xmlns:xvml="urn:schemas-microsoft-com:office:excel" xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math" xmlns:oda="http://opendope.org/answers" xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml" xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:odc="http://opendope.org/conditions" xmlns:cdr="http://schemas.openxmlformats.org/drawingml/2006/chartDrawing" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:odi="http://opendope.org/components" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:lc="http://schemas.openxmlformats.org/drawingml/2006/lockedCanvas" xmlns:wps="http://schemas.microsoft.com/office/word/2010/wordprocessingShape" xmlns:odq="http://opendope.org/questions" xmlns:wetp="http://schemas.microsoft.com/office/webextensions/taskpanes/2010/11" xmlns:w16cid="http://schemas.microsoft.com/office/word/2016/wordml/cid"/>
it worked.
karaf@root>
```

  

