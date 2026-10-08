Test fixtures (CR-002)
======================

corpus/             files the tests sign (RoundTripTest and others)

goldens/            files signed by Office.  GoldensTest runs on every file here:
                    it must validate (a name containing TAMPERED must not), and
                    we must sign the same parts and relationships Office did.

goldens/originals/  the unsigned files Office was given.  Also part of the
                    round-trip corpus.

Where the originals came from (all written by Office; none made by docx4j):

  minimal.pptx   docx4j pptx4j samples, table.pptx
  minimal.xlsx   Enterprise OLE samples, simple.xlsx
  rich.pptx      docx4j-core-tests loadAndSave.pptx: chart, png and svg images,
                 a modern comment (ppt/authors.xml), an embedded workbook
  notes.pptx     Enterprise MergePptx samples, pptx-basic.pptx: notes slides, image
  rich.xlsx      docx4j-core-tests loadAndSave.xlsx: chart, images, table, a
                 comment with a threaded comment and person part
  macro.pptm     Apache POI test-data, SimpleMacro.pptm (has ppt/vbaProject.bin)
  macro.xlsm     Apache POI test-data, SimpleMacro.xlsm (has xl/vbaProject.bin)

  customxml.docx docx4j-core-tests OpenDoPE/ranks-last-binding.docx: custom XML
                 data parts and docProps/custom.xml.  Not yet signed in Word.

Making the goldens (Microsoft 365 on the VM, certificate plutext-digsig-test.pfx
imported into the Windows user's Personal store):

  1. Open each file in goldens/originals in PowerPoint or Excel.
     File > Info > Protect Presentation / Protect Workbook > Add a Digital
     Signature.  Sign with the test certificate.
  2. Office saves the signature into the file it has open, so work on a copy:
     the signed file goes in goldens/ under the same name, and the file in
     goldens/originals stays as it is.
  3. Excel signature line: open a copy of minimal.xlsx, Insert > Text >
     Signature Line, save it as goldens/originals/sigline.xlsx; then sign that
     line (double click it) in a copy saved as goldens/sigline.xlsx.
  4. In PowerPoint, look for Insert > Signature Line.  CR-002 expects there is
     none; note it either way.
  5. Record the Office build (File > Account > About) in section 6 of the CR.

Then: mvn test -Dtest=GoldensTest
