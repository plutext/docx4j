docx4j-MOXy-JAXBContext
=======================

Config to use the EclipseLink MOXy (JAXB) runtime 

This version uses:
```
			<dependency>
				<groupId>org.eclipse.persistence</groupId>
				<artifactId>org.eclipse.persistence.moxy</artifactId>
-				<version>4.0.2</version> 
			</dependency>
```
If you need to, you can use v3.0.1.

To do so would also require using jakarta.xml.bind-api 3.0.1. 

The org.eclipse.persistence.moxy 4.0.2 JAXB implementation is discovered if present because 
org.eclipse.persistence.moxy-4.0.2.jar/META-INF/services/jakarta.xml.bind.JAXBContextFactory
contains org.eclipse.persistence.jaxb.XMLBindingContextFactory

To facilitate discovery of the 3.0.1 implementation (for any user who wishes to downgrade), 
we retain
src/main/resources/META-INF/services/jakarta.xml.bind.JAXBContext containing
jakarta.xml.bind.JAXBContext.  

