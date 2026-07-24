/*
 *  Copyright 2007-2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.

    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */

package org.docx4j.samples;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

import jakarta.activation.DataHandler;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.model.images.CidConversionImageHandler;
import org.docx4j.model.images.CidConversionImageHandler.CidImage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Convert a docx to HTML suitable for an email body: images are
 * referenced as cid: URIs, and attached to the message in a
 * multipart/related, each MIME part carrying the matching Content-ID.
 *
 * This is the approach to use for email, in preference to
 * DataUriConversionImageHandler (base64 data URIs): notably, Gmail
 * clips messages whose HTML source exceeds about 100KB, which base64
 * embedding will typically exceed immediately.
 *
 * This sample writes the message to an .eml file (which you can open
 * in a mail client to inspect).  To send it instead, set From/To and
 * SMTP session properties, then Transport.send(message).
 */
public class ConvertOutHtmlToEmail extends AbstractSample {

	// Config for non-command line version
	static {
		inputfilepath = System.getProperty("user.dir") + "/sample-docs/sample-docxv2.docx";
	}

	public static void main(String[] args) throws Exception {

		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
		}

		System.out.println("Loading file from " + inputfilepath);
		WordprocessingMLPackage wordMLPackage = Docx4J.load(new java.io.File(inputfilepath));

		// HTML conversion, collecting images for attachment
		HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
		CidConversionImageHandler imageHandler = new CidConversionImageHandler();
		htmlSettings.setImageHandler(imageHandler);
		htmlSettings.setOpcPackage(wordMLPackage);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Docx4J.toHTML(htmlSettings, os, Docx4J.FLAG_NONE);
		String html = os.toString("UTF-8");

		// Assemble the message: multipart/related containing the HTML
		// plus one inline part per image
		Session session = Session.getInstance(new Properties());
		MimeMessage message = new MimeMessage(session);
		message.setSubject("docx4j HTML email sample", "UTF-8");

		MimeMultipart related = new MimeMultipart("related");

		MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setContent(html, "text/html; charset=UTF-8");
		related.addBodyPart(htmlPart);

		for (CidImage image : imageHandler.getImages()) {
			MimeBodyPart imagePart = new MimeBodyPart();
			imagePart.setDataHandler(new DataHandler(
					new ByteArrayDataSource(image.getBytes(), image.getContentType())));
			imagePart.setContentID(image.getContentIdHeader()); // angle brackets required
			imagePart.setFileName(image.getName());
			imagePart.setDisposition(MimeBodyPart.INLINE);
			related.addBodyPart(imagePart);
		}

		message.setContent(related);
		message.saveChanges();

		File emlFile = new File(inputfilepath + ".eml");
		try (OutputStream fos = new FileOutputStream(emlFile)) {
			message.writeTo(fos);
		}
		System.out.println("Saved: " + emlFile + " with "
				+ imageHandler.getImages().size() + " image(s) attached");
	}

}
