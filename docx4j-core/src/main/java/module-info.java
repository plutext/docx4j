module org.docx4j.core {
	
    requires java.xml.crypto;
    requires org.plutext.jaxb.svg11;
//    requires jcl.over.slf4j;
    requires org.apache.commons.codec;
    requires org.apache.commons.compress;
    requires org.apache.commons.lang3;
    requires wmf2svg;
    
	requires transitive org.docx4j.openxml_objects;
	requires transitive org.docx4j.openxml_objects_pml;
	requires transitive org.docx4j.openxml_objects_sml;
    
	requires transitive jakarta.activation;
	
	requires org.slf4j;
	
    requires transitive antlr.runtime;
    requires transitive java.desktop;
    requires transitive java.xml;
    requires transitive jdk.xml.dom;
    requires transitive mbassador;
    requires transitive org.apache.commons.io;
    requires transitive docx4j_xalan_serializer;
//    requires transitive slf4j.api;
    requires transitive stringtemplate;
    requires transitive docx4j_xalan_interpretive;
    requires transitive xmlgraphics.commons;
	requires error.prone.annotations;
	requires org.checkerframework.checker.qual;
//	requires org.eclipse.persistence.moxy;
//	requires org.eclipse.persistence.core;
	
	requires jdk.unsupported; // for org/docx4j/com/google/common/cache/  package sun.misc is not visible
	requires java.logging;
	requires org.apache.fontbox;
	requires qdox;
	requires jakarta.xml.bind;
    
    exports org.docx4j;
    exports org.docx4j.convert.in;
    exports org.docx4j.convert.in.word2003xml;
    exports org.docx4j.convert.in.xhtml;
    exports org.docx4j.convert.out;
    exports org.docx4j.convert.out.common;
    exports org.docx4j.convert.out.common.preprocess;
    exports org.docx4j.convert.out.common.wrappers;
    exports org.docx4j.convert.out.common.writer;
    exports org.docx4j.convert.out.flatOpcXml;
    exports org.docx4j.convert.out.fopconf;
    exports org.docx4j.convert.out.html;
    exports org.docx4j.document.wordprocessingml;
    exports org.docx4j.events;
    exports org.docx4j.finders;
    exports org.docx4j.fonts;
    exports org.docx4j.fonts.fop;
    exports org.docx4j.fonts.fop.apps;
    exports org.docx4j.fonts.fop.fonts;
    exports org.docx4j.fonts.fop.fonts.apps;
    exports org.docx4j.fonts.fop.fonts.autodetect;
    exports org.docx4j.fonts.fop.fonts.base14;
    exports org.docx4j.fonts.fop.fonts.substitute;
    exports org.docx4j.fonts.fop.fonts.truetype;
    exports org.docx4j.fonts.fop.fonts.type1;
    exports org.docx4j.fonts.fop.util;
    exports org.docx4j.fonts.foray.font.format;
    exports org.docx4j.fonts.microsoft;
    exports org.docx4j.fonts.substitutions;
    exports org.docx4j.jaxb;
    exports org.docx4j.model;
    exports org.docx4j.model.bookmarks;
    exports org.docx4j.model.datastorage;
    exports org.docx4j.model.datastorage.migration;
    exports org.docx4j.model.datastorage.xpathtracker;
    exports org.docx4j.model.fields;
    exports org.docx4j.model.fields.docproperty;
    exports org.docx4j.model.fields.merge;
    exports org.docx4j.model.images;
    exports org.docx4j.model.listnumbering;
    exports org.docx4j.model.properties;
    exports org.docx4j.model.properties.paragraph;
    exports org.docx4j.model.properties.run;
    exports org.docx4j.model.properties.table;
    exports org.docx4j.model.properties.table.tblStyle;
    exports org.docx4j.model.properties.table.tc;
    exports org.docx4j.model.properties.table.tr;
    exports org.docx4j.model.sdt;
    exports org.docx4j.model.shapes;
    exports org.docx4j.model.structure;
    exports org.docx4j.model.structure.jaxb;
    exports org.docx4j.model.styles;
    exports org.docx4j.model.table;
    exports org.docx4j.openpackaging;
    exports org.docx4j.openpackaging.contenttype;    
    exports org.docx4j.openpackaging.exceptions;
    exports org.docx4j.openpackaging.io;
    exports org.docx4j.openpackaging.io3;
    exports org.docx4j.openpackaging.io3.stores;
    exports org.docx4j.openpackaging.packages;
    exports org.docx4j.openpackaging.parts;
    exports org.docx4j.openpackaging.parts.DrawingML;
    exports org.docx4j.openpackaging.parts.WordprocessingML;
    exports org.docx4j.openpackaging.parts.opendope;
    exports org.docx4j.openpackaging.parts.relationships;
    exports org.docx4j.openpackaging.parts.webextensions;
    exports org.docx4j.org.apache.poi;
    exports org.docx4j.org.apache.poi.hpsf;
    exports org.docx4j.org.apache.poi.hpsf.wellknown;
    exports org.docx4j.org.apache.poi.hssf;
    exports org.docx4j.org.apache.poi.poifs.common;
    exports org.docx4j.org.apache.poi.poifs.crypt;
    exports org.docx4j.org.apache.poi.poifs.crypt.agile;
    exports org.docx4j.org.apache.poi.poifs.crypt.binaryrc4;
    exports org.docx4j.org.apache.poi.poifs.crypt.cryptoapi;
    exports org.docx4j.org.apache.poi.poifs.crypt.standard;
    exports org.docx4j.org.apache.poi.poifs.dev;
    exports org.docx4j.org.apache.poi.poifs.eventfilesystem;
    exports org.docx4j.org.apache.poi.poifs.filesystem;
    exports org.docx4j.org.apache.poi.poifs.nio;
    exports org.docx4j.org.apache.poi.poifs.property;
    exports org.docx4j.org.apache.poi.poifs.storage;
    exports org.docx4j.org.apache.poi.util;

    exports org.docx4j.org.apache.xerces.util;
    exports org.docx4j.org.apache.xml.security;
    exports org.docx4j.org.apache.xml.security.c14n;
    exports org.docx4j.org.apache.xml.security.c14n.helper;
    exports org.docx4j.org.apache.xml.security.c14n.implementations;
    exports org.docx4j.org.apache.xml.security.exceptions;
    exports org.docx4j.org.apache.xml.security.signature;
    exports org.docx4j.org.apache.xml.security.transforms;
    exports org.docx4j.org.apache.xml.security.transforms.implementations;
    exports org.docx4j.org.apache.xml.security.transforms.params;
    exports org.docx4j.org.apache.xml.security.utils;
    exports org.docx4j.toc;
    exports org.docx4j.toc.switches;
    exports org.docx4j.utils;

    exports org.docx4j.openpackaging.parts.PresentationML;
    exports org.docx4j.openpackaging.parts.SpreadsheetML;

    exports org.pptx4j;
    exports org.pptx4j.convert.out;
    exports org.pptx4j.convert.out.svginhtml;
    exports org.pptx4j.jaxb;
    exports org.pptx4j.model;
    exports org.xlsx4j.exceptions;
    exports org.xlsx4j.jaxb;
    exports org.xlsx4j.model;
    exports org.xlsx4j.org.apache.poi.ss.format;
    exports org.xlsx4j.org.apache.poi.ss.usermodel;
    exports org.xlsx4j.org.apache.poi.ss.util;
    
    exports org.glox4j.openpackaging.packages;

    exports org.opendope.SmartArt.dataHierarchy;
    exports org.opendope.answers;
    exports org.opendope.components;
    exports org.opendope.conditions;
    exports org.opendope.questions;
    exports org.opendope.xpaths;

    opens org.opendope.SmartArt.dataHierarchy to jakarta.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.opendope.answers to jakarta.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.opendope.components to jakarta.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.opendope.conditions to jakarta.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.opendope.questions to jakarta.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.opendope.xpaths to jakarta.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;

    opens org.docx4j.openpackaging.contenttype to jakarta.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;
    opens org.docx4j.model.structure.jaxb  to jakarta.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;

    opens org.docx4j.model.datastorage;//  to java.xml.bind, com.sun.xml.bind; // for HistgramQNameTest

    // Resource folders must be open! See https://stackoverflow.com/questions/45166757/loading-classes-and-resources-in-java-9/45173837#45173837  
	opens org.pptx4j.convert.out.svginhtml;
	opens org.docx4j.toc;
	//opens org.docx4j.org.apache.xml.security.resource;
	opens org.docx4j.openpackaging.parts.WordprocessingML;
	opens org.docx4j.openpackaging.parts.PresentationML;
	opens org.docx4j.openpackaging.parts.DrawingML;
	opens org.docx4j.openpackaging.packages;
	opens org.docx4j.model.shapes;
	opens org.docx4j.model.fields;
	// opens org.docx4j.model.datastorage; // opened above
	opens org.docx4j.jaxb;
	opens org.docx4j.fonts.substitutions;
	opens org.docx4j.fonts.microsoft;
	opens org.docx4j.convert.out.html;
	opens org.docx4j.convert.in.word2003xml;
    
    
}
