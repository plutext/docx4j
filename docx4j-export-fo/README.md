# docx4j-export-FO

Export docx to PDF via XSL FO, using FOP

With docx4j 8, this maven module builds as an optional jar. (From 3.3.0 to 6.1.x, this was an optional separate project at https://github.com/plutext/docx4j-export-fo )

docx4j will use export-FO automatically if it finds it in your classpath.

## Alternatives

The default PDF Converter is an eval version of Plutext's commercial renderer, which takes 
a completely different approach, to offer much better fidelity and performance.  For more on this, please see https://converter-eval.plutext.com/client_java.html

To use that, do not put docx4j-export-fo on your classpath. 

To avoid information leakage, the default configuration of  PDF Converter assumes local host.  So to 
get it working, you either need to download/install it locally, or set docx4j property "com.plutext.converter.URL", 
for example:

```
Docx4jProperties.setProperty(
    "com.plutext.converter.URL", 
    "https://converter-eval.plutext.com:443/v1/00000000-0000-0000-0000-000000000000/convert");
```



