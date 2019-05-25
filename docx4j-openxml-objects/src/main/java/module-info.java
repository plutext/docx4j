module docx4j_openxml_objects {

    requires java.xml.bind;
	requires org.slf4j;
    
    exports org.docx4j.bibliography;
    
    exports org.docx4j.com.microsoft.schemas.office.drawing.x2010.chartDrawing;
    exports org.docx4j.com.microsoft.schemas.office.drawing.x2010.diagram;
    exports org.docx4j.com.microsoft.schemas.office.drawing.x2010.main;
    exports org.docx4j.com.microsoft.schemas.office.drawing.x2010.picture;
    exports org.docx4j.com.microsoft.schemas.office.drawing.x2012.chart;
    exports org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle;
    exports org.docx4j.com.microsoft.schemas.office.drawing.x2012.main;
    
    exports org.docx4j.com.microsoft.schemas.office.drawing.x2013.main.command;
    exports org.docx4j.com.microsoft.schemas.office.drawing.x2014.chart.ac;
    exports org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;
    exports org.docx4j.com.microsoft.schemas.office.drawing.x2014.chart;
    exports org.docx4j.com.microsoft.schemas.office.drawing.x2014.main;
    exports org.docx4j.com.microsoft.schemas.office.drawing.x201611.diagram;
    exports org.docx4j.com.microsoft.schemas.office.drawing.x201611.main;
    exports org.docx4j.com.microsoft.schemas.office.drawing.x201612.diagram;
    exports org.docx4j.com.microsoft.schemas.office.drawing.x2016.ink;
    exports org.docx4j.com.microsoft.schemas.office.drawing.x2016.SVG.main;
    exports org.docx4j.com.microsoft.schemas.office.drawing.x201703.chart;
    exports org.docx4j.com.microsoft.schemas.office.drawing.x2017.decorative;
    exports org.docx4j.com.microsoft.schemas.office.drawing.x2017.model3d;
    exports org.docx4j.com.microsoft.schemas.office.drawing.x2018.animation.model3d;
    exports org.docx4j.com.microsoft.schemas.office.drawing.x2018.animation;
    exports org.docx4j.com.microsoft.schemas.office.drawing.x2018.hyperlinkcolor;
    
    exports org.docx4j.com.microsoft.schemas.ink.x2010.main;
    
    exports org.docx4j.com.microsoft.schemas.office.powerpoint.x2014.inkAction;
    
    exports org.docx4j.com.microsoft.schemas.office.thememl.x2012.main;
    
//    exports org.xlsx4j.schemas.microsoft.com.office.excel.x2010.spreadsheetDrawing;    
    exports org.docx4j.com.microsoft.schemas.office.webextensions.taskpanes_2010_11;
    exports org.docx4j.com.microsoft.schemas.office.webextensions.webextension_2010_11;
    
    exports org.docx4j.com.microsoft.schemas.office.word.x2006.wordml;
    
    exports org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingCanvas;
    exports org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingDrawing;
    exports org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingGroup;
    exports org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape;

    exports org.docx4j.com.microsoft.schemas.office.word.x2012.wordprocessingDrawing;
    
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
    
    exports org.docx4j.org.w3.x1998.math.mathML;
    exports org.docx4j.org.w3.x2003.inkML;
    
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

    opens org.docx4j.bibliography; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;

    opens org.docx4j.com.microsoft.schemas.office.drawing.x2010.chartDrawing; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.drawing.x2010.diagram; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.drawing.x2010.main; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.drawing.x2010.picture; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.drawing.x2012.chart; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.drawing.x2012.main; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    
    opens org.docx4j.com.microsoft.schemas.office.drawing.x2013.main.command; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.drawing.x2014.chart.ac; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.drawing.x2014.chart; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.drawing.x2014.main; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.drawing.x201611.diagram; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.drawing.x201611.main; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.drawing.x201612.diagram; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.drawing.x2016.ink; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.drawing.x2016.SVG.main; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.drawing.x201703.chart; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.drawing.x2017.decorative; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.drawing.x2017.model3d; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.drawing.x2018.animation.model3d; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.drawing.x2018.animation; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.drawing.x2018.hyperlinkcolor; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    
    opens org.docx4j.com.microsoft.schemas.office.powerpoint.x2014.inkAction; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    
    
    opens org.docx4j.com.microsoft.schemas.ink.x2010.main; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
//    opens org.xlsx4j.schemas.microsoft.com.office.excel.x2010.spreadsheetDrawing; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    
    opens org.docx4j.com.microsoft.schemas.office.thememl.x2012.main ; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    
    opens org.docx4j.com.microsoft.schemas.office.webextensions.taskpanes_2010_11; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.webextensions.webextension_2010_11; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.word.x2006.wordml; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    
    opens org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingDrawing; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingGroup; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingCanvas; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;

    opens org.docx4j.com.microsoft.schemas.office.word.x2012.wordprocessingDrawing; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    
    opens org.docx4j.com.microsoft.schemas.office.x2006.encryption; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.x2006.keyEncryptor.certificate; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.com.microsoft.schemas.office.x2006.keyEncryptor.password; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.customXmlProperties; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.customxml; //; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.dml; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.dml.chart; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.dml.chart.x2007; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.dml.chartDrawing; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.dml.compatibility; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.dml.diagram; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.dml.diagram2008; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.dml.lockedCanvas; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.dml.picture; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.dml.spreadsheetdrawing; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.dml.wordprocessingDrawing; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.docProps.core; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.docProps.core.dc.elements; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.docProps.core.dc.terms; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.docProps.coverPageProps; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.docProps.custom; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.docProps.extended; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.docProps.variantTypes; //; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.math; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.mce; //; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    
    opens org.docx4j.org.w3.x1998.math.mathML; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.org.w3.x2003.inkML; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;

    
    opens org.docx4j.relationships; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.sharedtypes; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.vml; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.vml.officedrawing; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.vml.presentationDrawing; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.vml.root; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.vml.spreadsheetDrawing; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.vml.wordprocessingDrawing; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.w14; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.w15; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.w15symex; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.w16cid; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.wml; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.xmlPackage; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.jvnet.jaxb2_commons.ppp; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    
}
