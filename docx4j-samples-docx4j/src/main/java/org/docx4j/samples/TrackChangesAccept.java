/*
 *  Copyright 2017, Plutext Pty Ltd.
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


import java.io.File;

import org.docx4j.Docx4J;
import org.docx4j.model.revisions.TrackedChange;
import org.docx4j.model.revisions.TrackedChanges;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Accepts a document's tracked changes, as Word's Accept All does, in every story: the main
 * document, headers, footers, footnotes, endnotes, comments, text boxes and equations (CR-043).
 * Lists them first; {@link TrackChangesReject} is the other way round, and
 * {@code TrackedChanges.acceptAll(pkg, filter)} accepts only some (by author, type, kind, date,
 * part, or inside a content control or bookmark).
 *
 * <p>Until 17.3.3 this sample ran AcceptChanges.xslt over the main document part, which accepted
 * insertions and deletions there only, and missed paragraph marks and formatting changes.</p>
 */
public class TrackChangesAccept extends AbstractSample {

	public static void main(String[] args) throws Exception {

		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
	    	inputfilepath = System.getProperty("user.dir") + "/OUT_CompareRsid_Newer.docx";
		}
		System.out.println(inputfilepath);
		boolean SAVE = true;

		WordprocessingMLPackage wordMLPackage = Docx4J.load(new java.io.File(inputfilepath));

		// what there is, grouped as Word's Review pane and Office JS list it
		for (TrackedChange change : TrackedChanges.in(wordMLPackage)) {
			System.out.println(change.getType() + " " + change.getKind() + " by " + change.getAuthor()
					+ " at " + change.getDate() + ": " + change.getText());
		}
		System.out.println(TrackedChanges.report(wordMLPackage).toJson());

		int n = TrackedChanges.acceptAll(wordMLPackage);
		System.out.println("Accepted " + n);

		if (SAVE) {
			String outputfilepath = System.getProperty("user.dir") + "/OUT_TrackChangesAccept.docx";
			Docx4J.save(wordMLPackage, new File(outputfilepath), Docx4J.FLAG_NONE);
			System.out.println("Saved: " + outputfilepath);
		}
	}
}
