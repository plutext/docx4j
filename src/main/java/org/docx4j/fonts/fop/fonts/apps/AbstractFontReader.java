/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.fonts.fop.fonts.apps;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.docx4j.XmlUtils;
import org.docx4j.fonts.fop.util.CommandLineLogger;

/**
 * Abstract base class for the PFM and TTF Reader command-line applications.
 */
public abstract class AbstractFontReader {

    /** Logger instance */
    protected static Log log;

    /**
     * Main constructor.
     */
    protected AbstractFontReader() {
        // Create logger if necessary here to allow embedding of TTFReader in
        // other applications. There is a possible but harmless synchronization
        // issue.
        if (log == null) {
            log = LogFactory.getLog(AbstractFontReader.class);
        }
    }

    /**
     * Parse commandline arguments. put options in the HashMap and return
     * arguments in the String array
     * the arguments: -fn Perpetua,Bold -cn PerpetuaBold per.ttf Perpetua.xml
     * returns a String[] with the per.ttf and Perpetua.xml. The hash
     * will have the (key, value) pairs: (-fn, Perpetua) and (-cn, PerpetuaBold)
     * @param options Map that will receive options
     * @param args the command-line arguments
     * @return the arguments
     */
    protected static String[] parseArguments(Map options, String[] args) {
        List arguments = new java.util.ArrayList();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                if ("-d".equals(args[i]) || "-q".equals(args[i])) {
                    options.put(args[i], "");
                } else if ((i + 1) < args.length && !args[i + 1].startsWith("-")) {
                    options.put(args[i], args[i + 1]);
                    i++;
                } else {
                    options.put(args[i], "");
                }
            } else {
                arguments.add(args[i]);
            }
        }
        return (String[])arguments.toArray(new String[0]);
    }

    /**
     * Sets the logging level.
     * @param level the logging level ("debug", "info", "error" etc., see Jakarta Commons Logging)
     */
    protected static void setLogLevel(String level) {
        // Set the evel for future loggers.
        LogFactory.getFactory().setAttribute("level", level);
        if (log instanceof CommandLineLogger) {
            // Set the level for the logger creates already.
            ((CommandLineLogger) log).setLogLevel(level);
        }
    }

    /**
     * Determines the log level based of the options from the command-line.
     * @param options the command-line options
     */
    protected static void determineLogLevel(Map options) {
        //Determine log level
        if (options.get("-d") != null) {
            setLogLevel("debug");
        } else if (options.get("-q") != null) {
            setLogLevel("error");
        } else {
            setLogLevel("info");
        }
    }

    /**
     * Writes the generated DOM Document to a file.
     *
     * @param   doc The DOM Document to save.
     * @param   target The target filename for the XML file.
     * @throws TransformerException if an error occurs during serialization
     */
    public void writeFontXML(org.w3c.dom.Document doc, String target) throws TransformerException {
        writeFontXML(doc, new File(target));
    }

    /**
     * Writes the generated DOM Document to a file.
     *
     * @param   doc The DOM Document to save.
     * @param   target The target file for the XML file.
     * @throws TransformerException if an error occurs during serialization
     */
    public void writeFontXML(org.w3c.dom.Document doc, File target) throws TransformerException {
        log.info("Writing xml font file " + target + "...");

        try {
            OutputStream out = new java.io.FileOutputStream(target);
            out = new java.io.BufferedOutputStream(out);
            try {
                TransformerFactory factory = XmlUtils.getTransformerFactory();
                Transformer transformer = factory.newTransformer();
                transformer.transform(
                        new javax.xml.transform.dom.DOMSource(doc),
                        new javax.xml.transform.stream.StreamResult(out));
            } finally {
                out.close();
            }
        } catch (IOException ioe) {
            throw new TransformerException("Error writing the output file", ioe);
        }
    }

}
