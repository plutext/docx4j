module org.docx4j.openxml_objects_pml {
	
    requires java.xml.bind;
	requires org.slf4j;
	requires org.docx4j.openxml_objects;
    
    exports org.pptx4j.pml;
    exports org.pptx4j.com.microsoft.schemas.office.powerpoint.x201606.main;
    exports org.pptx4j.com.microsoft.schemas.office.powerpoint.x2010.main;
    exports org.pptx4j.com.microsoft.schemas.office.powerpoint.x2012.main;
    exports org.pptx4j.com.microsoft.schemas.office.powerpoint.x2013.main.command;
    exports org.pptx4j.com.microsoft.schemas.office.powerpoint.x201509.main;
    exports org.pptx4j.com.microsoft.schemas.office.powerpoint.x201510.main;
    exports org.pptx4j.com.microsoft.schemas.office.powerpoint.x2015.main;
    exports org.pptx4j.com.microsoft.schemas.office.powerpoint.x2016.sectionzoom;
    exports org.pptx4j.com.microsoft.schemas.office.powerpoint.x2016.slidezoom;
    exports org.pptx4j.com.microsoft.schemas.office.powerpoint.x2016.summaryzoom;
    exports org.pptx4j.com.microsoft.schemas.office.powerpoint.x201710.main;
    exports org.pptx4j.com.microsoft.schemas.office.powerpoint.x201703.main;
    exports org.pptx4j.com.microsoft.schemas.office.powerpoint.x201804.main;

    opens org.pptx4j.pml;
    opens org.pptx4j.com.microsoft.schemas.office.powerpoint.x201606.main; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.pptx4j.com.microsoft.schemas.office.powerpoint.x2010.main; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.pptx4j.com.microsoft.schemas.office.powerpoint.x2012.main; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.pptx4j.com.microsoft.schemas.office.powerpoint.x2013.main.command; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.pptx4j.com.microsoft.schemas.office.powerpoint.x201509.main; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.pptx4j.com.microsoft.schemas.office.powerpoint.x201510.main; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.pptx4j.com.microsoft.schemas.office.powerpoint.x2015.main; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.pptx4j.com.microsoft.schemas.office.powerpoint.x2016.sectionzoom; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.pptx4j.com.microsoft.schemas.office.powerpoint.x2016.slidezoom; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.pptx4j.com.microsoft.schemas.office.powerpoint.x2016.summaryzoom; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.pptx4j.com.microsoft.schemas.office.powerpoint.x201710.main; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.pptx4j.com.microsoft.schemas.office.powerpoint.x201703.main; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.pptx4j.com.microsoft.schemas.office.powerpoint.x201804.main; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    

}
