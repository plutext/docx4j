/* Copyright © 2026, Oracle and/or its affiliates. */
package org.docx4j;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.ResourceUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class LoadAndSaveTests {
    private static final Logger log = LoggerFactory.getLogger(LoadAndSaveTests.class);

    @Test
    public void loadAndSaveDocx() throws IOException, Docx4JException {
        java.io.InputStream is = ResourceUtils.getResource("loadAndSave.docx");

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(is);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        wordMLPackage.save(baos);

        log.info("Finished loading and saving docx. Bytes: {}", baos.size());
    }

    @Test
    public void loadAndSaveXlsx() throws IOException, Docx4JException {
        java.io.InputStream is = ResourceUtils.getResource("loadAndSave.xlsx");

        SpreadsheetMLPackage spreadsheetMLPackage = SpreadsheetMLPackage.load(is);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        spreadsheetMLPackage.save(baos);

        log.info("Finished loading and saving xlsx. Bytes: {}", baos.size());
    }

    @Test
    public void loadAndSavePptx() throws IOException, Docx4JException {
        java.io.InputStream is = ResourceUtils.getResource("loadAndSave.pptx");

        PresentationMLPackage presentationMLPackage = PresentationMLPackage.load(is);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        presentationMLPackage.save(baos);

        log.info("Finished loading and saving pptx. Bytes: {}", baos.size());
    }
}
