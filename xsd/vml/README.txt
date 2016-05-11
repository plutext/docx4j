On 2009 08 30, vml.xsd was added to docx4j, principally to handle embedded images 
when saving as docx from Word 2003.

It used Ecma\ TC45\ OOXML\ Standard\ Schemas\ -\ Draft\ 1.3.

All it imported was:

        <xsd:import namespace="urn:schemas-microsoft-com:office:office"
                schemaLocation="vml-officedrawing.xsd" />

As from 2011 04 02, migrate to ECMA 376 1st ed proper.

This imports:
 
  <xsd:import namespace="urn:schemas-microsoft-com:office:office" 
  schemaLocation="vml-officeDrawing.xsd" />
  
  <xsd:import namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" 
  schemaLocation="wml.xsd" />
  
  <xsd:import namespace="urn:schemas-microsoft-com:office:word"
   schemaLocation="vml-wordprocessingDrawing.xsd" />
   
  <xsd:import namespace="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
   schemaLocation="shared-relationshipReference.xsd" />
   
  <xsd:import namespace="urn:schemas-microsoft-com:office:excel"
   schemaLocation="vml-spreadsheetDrawing.xsd" />
   
  <xsd:import namespace="urn:schemas-microsoft-com:office:powerpoint" 
  schemaLocation="vml-presentationDrawing.xsd" />
  
 
 vml-officeDrawing.xsd imports vml-main.xsd and vice versa!
 
 
 Had to replace 
 
  vml-officedrawing.xsd  (done - changed from Drawing to drawing)
  vml.xsd (use -main instead -done)
  vml-wordprocessingdrawing.xsd  (done - changed from Drawing to drawing)
  
Had to add:
  
   schemaLocation="vml-spreadsheetDrawing.xsd" />
  schemaLocation="vml-presentationDrawing.xsd" />
  
Had to add vml__ROOT.xsd for xjc.
                
It also contains a schema for the unqualified xml element, since MS/ECMA does not provide one.                

-----

July 2014, namespace for certain attributes in CT_SignatureLine in vml/vml-officedrawing are wrong!

