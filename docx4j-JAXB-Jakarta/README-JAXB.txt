<dependency>
  <groupId>jakarta.xml.bind</groupId>
  <artifactId>jakarta.xml.bind-api</artifactId>
  <version>2.3.2</version>
</dependency>

had module name java.xml.bind
and packages javax.xml.bind


<dependency>
  <groupId>jakarta.xml.bind</groupId>
  <artifactId>jakarta.xml.bind-api</artifactId>
  <version>3.0.0</version>
</dependency>

changes it to module name jakarta.xml.bind
and packages jakarta.xml.bind

This change breaks everything!

See further https://eclipse-ee4j.github.io/jakartaee-platform/jakartaee9/JakartaEE9ReleasePlan

which says To be included in the Jakarta EE 9 release a specification MUST move their API package names from the top level javax package to the top level jakarta package. 

See https://eclipse-ee4j.github.io/jaxb-ri/

Looks like MOXy will support it https://www.eclipse.org/eclipselink/releases/3.0.php

https://jakarta.ee/specifications/xml-binding/3.0/

So docx4j plan: an 11.3 which moves to jakarta.