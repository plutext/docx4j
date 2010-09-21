The files in this package are from FOP 0.95 r756437 (i think),
with some patches applied.

Then the package was renamed, so docx4j can use
this for font stuff, but FOP 1.0 for PDF output.

This was, i think, (though with original package names): 

<!--
		<dependency>
			<groupId>docx4j</groupId>
			<artifactId>fop-patched</artifactId>
			<version>0.95.756437</version>
		</dependency>
		-->
			<!--  its really 95.756434, but i had to re-build it to remove
			      Class-Path from the MANIFEST.MF, which was 
			      being used by Java Web Start to try to fetch certain
			      jars. So the number is incremented (0.95.756435).
			      And then, Panose.java moved to original foray package (0.95.756436) 
			      
			      0.95.756437 has fop r891181 of 20091216 applied, which adds support for
			      symbol character maps (eg Wingdings) to TTFFile
			        
			      -->