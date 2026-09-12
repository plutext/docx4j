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
package org.docx4j.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RStyle;
import org.docx4j.wml.Style;
import org.junit.Test;

/**
 * One PropertyResolver shared by several threads - two exports of one package at once -
 * gives every thread the answer a single thread would get, while a style is being
 * added to the part underneath them (CR-015 phase 3: the caches are concurrent and
 * resolution writes nothing into the styles part).
 *
 * @since 17.1.1
 */
public class PropertyResolverConcurrencyTest {

	private static final org.docx4j.wml.ObjectFactory F = Context.getWmlObjectFactory();

	private static final int THREADS = 8;
	private static final int STYLES = 40;
	private static final int STEPS = 400;

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		List<Style> list = mdp.getStyleDefinitionsPart().getJaxbElement().getStyle();
		// a chain of 40 paragraph styles, each based on the previous, each stating its own size
		String basedOn = "Normal";
		for (int i = 0; i < STYLES; i++) {
			Style s = F.createStyle();
			s.setType("paragraph");
			s.setStyleId("P" + i);
			Style.Name n = F.createStyleName();
			n.setVal("P" + i);
			s.setName(n);
			Style.BasedOn b = F.createStyleBasedOn();
			b.setVal(basedOn);
			s.setBasedOn(b);
			RPr r = F.createRPr();
			HpsMeasure sz = F.createHpsMeasure();
			sz.setVal(BigInteger.valueOf(20 + 2 * i));
			r.setSz(sz);
			s.setRPr(r);
			PPr p = F.createPPr();
			PPrBase.Spacing sp = F.createPPrBaseSpacing();
			sp.setAfter(BigInteger.valueOf(10 * i));
			p.setSpacing(sp);
			s.setPPr(p);
			list.add(s);
			basedOn = "P" + i;
		}
		// and a few character styles
		for (int i = 0; i < 5; i++) {
			Style s = F.createStyle();
			s.setType("character");
			s.setStyleId("C" + i);
			Style.Name n = F.createStyleName();
			n.setVal("C" + i);
			s.setName(n);
			RPr r = F.createRPr();
			HpsMeasure sz = F.createHpsMeasure();
			sz.setVal(BigInteger.valueOf(100 + i));
			r.setSz(sz);
			s.setRPr(r);
			list.add(s);
		}
		return pkg;
	}

	/** One thread's walk: a fixed pseudo-random sequence of questions, answered as strings. */
	private static List<String> walk(PropertyResolver pr, int seed) throws Exception {
		Random random = new Random(seed);
		List<String> answers = new ArrayList<String>();
		for (int step = 0; step < STEPS; step++) {
			String pStyle = "P" + random.nextInt(STYLES);
			int kind = random.nextInt(4);
			if (kind == 0) {
				PPr e = pr.getEffectivePPr(pStyle);
				answers.add(pStyle + " after=" + e.getSpacing().getAfter());
			} else if (kind == 1) {
				RPr e = pr.getEffectiveRPr((RPr) null, pPr(pStyle));
				answers.add(pStyle + " sz=" + e.getSz().getVal());
			} else if (kind == 2) {
				RPr e = pr.getEffectiveRPr(rPr("C" + random.nextInt(5)), pPr(pStyle));
				answers.add(pStyle + " C sz=" + e.getSz().getVal());
			} else {
				PPr direct = pPr(pStyle);
				direct.setJc(F.createJc());
				PPr e = pr.getEffectivePPr(direct);
				answers.add(pStyle + " direct after=" + e.getSpacing().getAfter());
			}
		}
		return answers;
	}

	@Test
	public void everyThreadGetsTheSingleThreadedAnswers() throws Exception {
		// the reference: one thread, a fresh resolver, per seed
		List<List<String>> expected = new ArrayList<List<String>>();
		for (int t = 0; t < THREADS; t++) {
			expected.add(walk(new PropertyResolver(pkg()), t));
		}

		final WordprocessingMLPackage shared = pkg();
		final MainDocumentPart mdp = shared.getMainDocumentPart();
		final CountDownLatch go = new CountDownLatch(1);
		ExecutorService pool = Executors.newFixedThreadPool(THREADS + 1);
		List<Future<List<String>>> futures = new ArrayList<Future<List<String>>>();
		for (int t = 0; t < THREADS; t++) {
			final int seed = t;
			futures.add(pool.submit(new Callable<List<String>>() {
				public List<String> call() throws Exception {
					go.await();
					return walk(mdp.getPropertyResolver(), seed);
				}
			}));
		}
		// meanwhile a style is added to the part (the 17.0.4 late-add case), repeatedly
		Future<?> adder = pool.submit(new Callable<Void>() {
			public Void call() throws Exception {
				go.await();
				for (int i = 0; i < 20; i++) {
					Style s = F.createStyle();
					s.setType("character");
					s.setStyleId("Late" + i);
					Style.Name n = F.createStyleName();
					n.setVal("Late" + i);
					s.setName(n);
					synchronized (mdp) {
						mdp.getStyleDefinitionsPart().getJaxbElement().getStyle().add(s);
					}
					assertTrue(mdp.getPropertyResolver().getEffectiveRPr("Late" + i) != null);
					Thread.sleep(1);
				}
				return null;
			}
		});
		go.countDown();
		for (int t = 0; t < THREADS; t++) {
			assertEquals("thread " + t, expected.get(t), futures.get(t).get());
		}
		adder.get();
		pool.shutdown();
		assertTrue("every late style is found afterwards", mdp.getPropertyResolver().getEffectiveRPr("Late19") != null);
	}

	@Test
	public void lazyCreationUnderContentionYieldsOneResolver() throws Exception {
		final MainDocumentPart mdp = pkg().getMainDocumentPart();
		final CountDownLatch go = new CountDownLatch(1);
		ExecutorService pool = Executors.newFixedThreadPool(THREADS);
		List<Future<PropertyResolver>> futures = new ArrayList<Future<PropertyResolver>>();
		for (int t = 0; t < THREADS; t++) {
			futures.add(pool.submit(new Callable<PropertyResolver>() {
				public PropertyResolver call() throws Exception {
					go.await();
					return mdp.getPropertyResolver();
				}
			}));
		}
		go.countDown();
		PropertyResolver first = futures.get(0).get();
		for (Future<PropertyResolver> f : futures) assertSame(first, f.get());
		pool.shutdown();
	}

	private static PPr pPr(String pStyle) {
		PPr p = F.createPPr();
		PPrBase.PStyle ps = F.createPPrBasePStyle();
		ps.setVal(pStyle);
		p.setPStyle(ps);
		return p;
	}

	private static RPr rPr(String rStyle) {
		RPr r = F.createRPr();
		RStyle rs = F.createRStyle();
		rs.setVal(rStyle);
		r.setRStyle(rs);
		return r;
	}
}
