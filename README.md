README
======


OSGi-friendly docx4j (Experimental) 
---------------

This is an OSGi-friendly branch of docx4j, currently tested on Java 8.

docx4j usually supports your choice of JAXB implementation: the one in Oracle Java 6 to 8, the Reference Implementation, MOXy, or IBMs.  In this branch,
we currently only support the JAXB included in Oracle Java 6 to 8.  So in Karaf's config, you need to expose com.sun.xml.internal.bind.marshaller (see further below)

Subject to that, it is docx4j 6.1.2.

Build
---------------

```
mvn install
```

Or quickest, 

```
mvn install -DskipTests
```

This creates a jar file with all deps embedded.  (However, karaf doesn't 
seem able to use them?)
 

Sample project
---------------

https://github.com/plutext/docx4j-OSGi-HelloWorld is a sample project

HelloWorld implements BundleActivator, and shows that marshalling works:

```
        P p = Context.getWmlObjectFactory().createP();
        System.out.println(XmlUtils.marshaltoString(p));
```

Clone the repo, then:

```
mvn install
```

Run it in Karaf
---------------

I used Karaf 4.2.4 and openjdk version "1.8.0_202"

Edit etc/config.properties, adding com.sun.xml.internal.bind.marshaller to org.osgi.framework.system.packages.extra

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

Download [feature.xml](https://github.com/plutext/docx4j/blob/VERSION_6_1_0_OSGi/feature.xml) then add its file URL as a repo:

```
karaf@root()> feature:repo-add file:///home/jharrop/git/docx4j/feature.xml                                                                                                                                     
Adding feature url file:///home/jharrop/git/docx4j/feature.xml
```

Install:

```
karaf@root()> feature:install docx4j-deps/6.1
```

Now you should see something like:

```
karaf@root()> bundle:list
START LEVEL 100 , List Threshold: 50
ID │ State  │ Lvl │ Version │ Name
───┼────────┼─────┼─────────┼─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
23 │ Active │  80 │ 4.2.4   │ Apache Karaf :: OSGi Services :: Event
45 │ Active │  80 │ 2.7.9   │ Jackson-annotations
46 │ Active │  80 │ 2.7.9   │ Jackson-core
47 │ Active │  80 │ 2.7.9   │ jackson-databind
48 │ Active │  80 │ 20.0.0  │ Guava: Google Core Libraries for Java
49 │ Active │  80 │ 3.0.0   │ Expression Language 3.0 API
50 │ Active │  80 │ 1.2.4   │ mbassador
51 │ Active │  80 │ 1.11.0  │ Apache Commons Codec
52 │ Active │  80 │ 1.12.0  │ Apache Commons Compress
53 │ Active │  80 │ 2.5.0   │ Apache Commons IO
54 │ Active │  80 │ 3.5.0   │ Apache Commons Lang
55 │ Active │  80 │ 0       │ wrap_file__home_jharrop_.m2_repository_com_thedeanda_lorem_2.1_lorem-2.1.jar
56 │ Active │  80 │ 0       │ wrap_file__home_jharrop_.m2_repository_net_arnx_wmf2svg_0.9.8_wmf2svg-0.9.8.jar
57 │ Active │  80 │ 0       │ wrap_file__home_jharrop_.m2_repository_org_antlr_antlr-runtime_3.5.2_antlr-runtime-3.5.2.jar
58 │ Active │  80 │ 0       │ wrap_file__home_jharrop_.m2_repository_org_antlr_stringtemplate_3.2.1_stringtemplate-3.2.1.jar
59 │ Active │  80 │ 0       │ wrap_file__home_jharrop_.m2_repository_org_apache_avalon_framework_avalon-framework-api_4.3.1_avalon-framework-api-4.3.1.jar
60 │ Active │  80 │ 0       │ wrap_file__home_jharrop_.m2_repository_org_apache_avalon_framework_avalon-framework-impl_4.3.1_avalon-framework-impl-4.3.1.jar
61 │ Active │  80 │ 0       │ wrap_file__home_jharrop_.m2_repository_org_apache_httpcomponents_httpclient_4.5.6_httpclient-4.5.6.jar
62 │ Active │  80 │ 0       │ wrap_file__home_jharrop_.m2_repository_org_apache_httpcomponents_httpcore_4.4.10_httpcore-4.4.10.jar
63 │ Active │  80 │ 0       │ wrap_file__home_jharrop_.m2_repository_org_apache_xmlgraphics_xmlgraphics-commons_2.3_xmlgraphics-commons-2.3.jar
64 │ Active │  80 │ 0       │ wrap_file__home_jharrop_.m2_repository_xalan_serializer_2.7.2_serializer-2.7.2.jar
65 │ Active │  80 │ 0       │ wrap_file__home_jharrop_.m2_repository_xalan_xalan_2.7.2_xalan-2.7.2.jar
```

Now let's try the sample project:

```
karaf@root()> bundle:install mvn:org.docx4j/docx4j-OSGi-HelloWorld/6.1.3-SNAPSHOT
Bundle ID: 67
karaf@root()> bundle:start 67
Hello world.
<w:p xmlns:dsp="http://schemas.microsoft.com/office/drawing/2008/diagram" xmlns:cppr="http://schemas.microsoft.com/office/2006/coverPageProps" xmlns:odx="http://opendope.org/xpaths" xmlns:c14="http://schemas.microsoft.com/office/drawing/2007/8/2/chart" xmlns:xdr="http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing" xmlns:odgm="http://opendope.org/SmartArt/DataHierarchy" xmlns:w16se="http://schemas.microsoft.com/office/word/2015/wordml/symex" xmlns:wp14="http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing" xmlns:dgm="http://schemas.openxmlformats.org/drawingml/2006/diagram" xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture" xmlns:we="http://schemas.microsoft.com/office/webextensions/webextension/2010/11" xmlns:pvml="urn:schemas-microsoft-com:office:powerpoint" xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" xmlns:w10="urn:schemas-microsoft-com:office:word" xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" xmlns:sl="http://schemas.openxmlformats.org/schemaLibrary/2006/main" xmlns:w15="http://schemas.microsoft.com/office/word/2012/wordml" xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:comp="http://schemas.openxmlformats.org/drawingml/2006/compatibility" xmlns:b="http://schemas.openxmlformats.org/officeDocument/2006/bibliography" xmlns:c="http://schemas.openxmlformats.org/drawingml/2006/chart" xmlns:xvml="urn:schemas-microsoft-com:office:excel" xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math" xmlns:oda="http://opendope.org/answers" xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml" xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:odc="http://opendope.org/conditions" xmlns:cdr="http://schemas.openxmlformats.org/drawingml/2006/chartDrawing" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:odi="http://opendope.org/components" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:lc="http://schemas.openxmlformats.org/drawingml/2006/lockedCanvas" xmlns:wps="http://schemas.microsoft.com/office/word/2010/wordprocessingShape" xmlns:odq="http://opendope.org/questions" xmlns:wetp="http://schemas.microsoft.com/office/webextensions/taskpanes/2010/11" xmlns:w16cid="http://schemas.microsoft.com/office/word/2016/wordml/cid"/>
it worked.
```


Open Questions
--------------

The sample project's jar bundles docx4j-OSGi and its dependencies.  Karaf is able to use the bundled docx4j-OSGi.jar, but not the other jars?  (For the other jars we need feature.xml - why?) 

Is Karaf OK with versions with 3 dots eg 1.2.3.4?  I've avoided using jackson-databind 2.7.9.4 and mbassador 1.2.4.2 for this reason.

