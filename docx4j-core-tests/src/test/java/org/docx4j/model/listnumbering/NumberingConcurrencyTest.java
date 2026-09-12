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
package org.docx4j.model.listnumbering;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.Test;

/**
 * The same package numbered from several threads at once, each traversal with a
 * state of its own, gets the same labels every time: counters are no longer
 * per package (CR-014 gap 1).  Before 17.1.1 the increments interleaved.
 */
public class NumberingConcurrencyTest {

	@Test
	public void separateStatesDoNotInterleave() throws Exception {
		final WordprocessingMLPackage pkg = NumberingStoriesTest.load();
		final List<P> body = NumberingStoriesTest.numbered(pkg.getMainDocumentPart().getContent());
		// definitions built and the property resolver created before the threads start
		pkg.getMainDocumentPart().getNumberingDefinitionsPart().getEmulator();
		pkg.getMainDocumentPart().getPropertyResolver();
		assertEquals("1. 2. 3. 4. 5. 6.", NumberingStoriesTest.labels(pkg, body, new NumberingState()));

		final List<String> wrong = Collections.synchronizedList(new ArrayList<String>());
		ExecutorService pool = Executors.newFixedThreadPool(4);
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (int t = 0; t < 4; t++) {
			futures.add(pool.submit(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < 200; i++) {
						String got = NumberingStoriesTest.labels(pkg, body, new NumberingState());
						if (!"1. 2. 3. 4. 5. 6.".equals(got)) wrong.add(got);
					}
				}
			}));
		}
		for (Future<?> f : futures) f.get();
		pool.shutdown();
		assertEquals("mislabelled traversals: " + wrong, 0, wrong.size());
	}
}
