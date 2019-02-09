module docx4j_openxml_objects {

    requires transitive java.xml.bind;
	requires org.slf4j;
    
    exports org.docx4j.bibliography;
    exports org.docx4j.com.microsoft.schemas.office.webextensions.taskpanes_2010_11;
    exports org.docx4j.com.microsoft.schemas.office.webextensions.webextension_2010_11;
    exports org.docx4j.com.microsoft.schemas.office.word.x2006.wordml;
    exports org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingDrawing;
    exports org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape;
    exports org.docx4j.com.microsoft.schemas.office.x2006.encryption;
    exports org.docx4j.com.microsoft.schemas.office.x2006.keyEncryptor.certificate;
    exports org.docx4j.com.microsoft.schemas.office.x2006.keyEncryptor.password;
    exports org.docx4j.customXmlProperties;
    exports org.docx4j.customxml;
    exports org.docx4j.dml;
    exports org.docx4j.dml.chart;
    exports org.docx4j.dml.chart.x2007;
    exports org.docx4j.dml.chartDrawing;
    exports org.docx4j.dml.compatibility;
    exports org.docx4j.dml.diagram;
    exports org.docx4j.dml.diagram2008;
    exports org.docx4j.dml.lockedCanvas;
    exports org.docx4j.dml.picture;
    exports org.docx4j.dml.spreadsheetdrawing;
    exports org.docx4j.dml.wordprocessingDrawing;
    exports org.docx4j.docProps.core;
    exports org.docx4j.docProps.core.dc.elements;
    exports org.docx4j.docProps.core.dc.terms;
    exports org.docx4j.docProps.coverPageProps;
    exports org.docx4j.docProps.custom;
    exports org.docx4j.docProps.extended;
    exports org.docx4j.docProps.variantTypes;
    exports org.docx4j.math;
    exports org.docx4j.mce;
    exports org.docx4j.relationships;
    exports org.docx4j.sharedtypes;
    exports org.docx4j.vml;
    exports org.docx4j.vml.officedrawing;
    exports org.docx4j.vml.presentationDrawing;
    exports org.docx4j.vml.root;
    exports org.docx4j.vml.spreadsheetDrawing;
    exports org.docx4j.vml.wordprocessingDrawing;
    exports org.docx4j.w14;
    exports org.docx4j.w15;
    exports org.docx4j.w15symex;
    exports org.docx4j.w16cid;
    exports org.docx4j.wml;
    exports org.docx4j.xmlPackage;
    exports org.jvnet.jaxb2_commons.ppp;

}
