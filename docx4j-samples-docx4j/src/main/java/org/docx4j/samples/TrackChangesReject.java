/*
 *  Copyright 2026, Plutext Pty Ltd.
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
import org.docx4j.model.revisions.TrackedChangeFilter;
import org.docx4j.model.revisions.TrackedChanges;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Rejects tracked changes, as Word's Reject All does, in every story (CR-043): one author's first,
 * with a filter, then one change at a time - each listing is valid until the next accept or
 * reject, so the list is read again after each - then the rest at once.
 */
public class TrackChangesReject extends AbstractSample {

	public static void main(String[] args) throws Exception {

		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
			inputfilepath = System.getProperty("user.dir") + "/OUT_CompareRsid_Newer.docx";
		}
		System.out.println(inputfilepath);
		boolean SAVE = true;

		WordprocessingMLPackage wordMLPackage = Docx4J.load(new File(inputfilepath));

		// one author's changes, wherever they are
		java.util.List<TrackedChange> changes = TrackedChanges.in(wordMLPackage);
		if (!changes.isEmpty()) {
			String author = changes.get(0).getAuthor();
			int n = TrackedChanges.rejectAll(wordMLPackage, new TrackedChangeFilter().authors(author));
			System.out.println("Rejected " + n + " by " + author);
		}

		// one at a time: a listed change is stale once another has been resolved
		for (int i = 0; i < 3; i++) {
			changes = TrackedChanges.in(wordMLPackage);
			if (changes.isEmpty()) break;
			TrackedChange change = changes.get(0);
			System.out.println("Rejecting " + change);
			change.reject();
		}

		// and the rest
		System.out.println("Rejected " + TrackedChanges.rejectAll(wordMLPackage) + " more");

		if (SAVE) {
			String outputfilepath = System.getProperty("user.dir") + "/OUT_TrackChangesReject.docx";
			Docx4J.save(wordMLPackage, new File(outputfilepath), Docx4J.FLAG_NONE);
			System.out.println("Saved: " + outputfilepath);
		}
	}
}
