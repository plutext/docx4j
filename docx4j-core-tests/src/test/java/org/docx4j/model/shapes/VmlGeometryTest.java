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
package org.docx4j.model.shapes;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.docx4j.dml.STPathFillMode;
import org.junit.Test;

/**
 * VML geometry (CR-083 phase 4): Part 4's formulas as it rounds them, the path's commands, and Word's own watermark
 * shapetype (type 136) evaluated as Word draws it.
 */
public class VmlGeometryTest {

	private static final double E = 1e-6;

	/** Word's shapetype 136 (Plain Text), as it writes it with every text watermark (phase 4 findings §1.3). */
	private static final List<String> F136 = Arrays.asList("sum #0 0 10800", "prod #0 2 1", "sum 21600 0 @1",
			"sum 0 0 @2", "sum 21600 0 @3", "if @0 @3 0", "if @0 21600 @1", "if @0 0 @2", "if @0 @4 21600", "mid @5 @6",
			"mid @8 @5", "mid @7 @8", "mid @6 @7", "sum @6 0 @5");
	private static final String P136 = "m@7,l@8,m@5,21600l@6,21600e";

	private static VmlGeometry.Definition d136(String adj) {
		return new VmlGeometry.Definition().path(P136).formulas(F136).coordsize("21600,21600")
				.adj(VmlGeometry.adj(VmlGeometry.adj(adj), VmlGeometry.adj("10800")));
	}

	/** Every point a path visits, as x, y pairs. */
	private static List<double[]> points(Path2D p) {
		List<double[]> out = new ArrayList<double[]>();
		double[] c = new double[6];
		for (PathIterator it = p.getPathIterator(null); !it.isDone(); it.next()) {
			int t = it.currentSegment(c);
			int n = t == PathIterator.SEG_CUBICTO ? 3 : t == PathIterator.SEG_QUADTO ? 2 : t == PathIterator.SEG_CLOSE ? 0 : 1;
			for (int i = 0; i < n; i++) out.add(new double[] { c[2 * i], c[2 * i + 1] });
		}
		return out;
	}

	private static void assertPoints(double[][] expected, Path2D p) {
		List<double[]> got = points(p);
		assertEquals("points: " + toString(got), expected.length, got.size());
		for (int i = 0; i < expected.length; i++) {
			assertEquals("x" + i, expected[i][0], got.get(i)[0], 1e-6);
			assertEquals("y" + i, expected[i][1], got.get(i)[1], 1e-6);
		}
	}

	private static String toString(List<double[]> pts) {
		StringBuilder sb = new StringBuilder();
		for (double[] p : pts) sb.append(String.format("(%.3f,%.3f)", p[0], p[1]));
		return sb.toString();
	}

	@Test
	public void theWatermarksShapetypeIsItsTopAndBottomLines() {
		ShapeOutlines o = VmlGeometry.outlines(d136(null), 468, 117);
		assertNull(o.getProblem());
		assertEquals(1, o.getOutlines().size());
		List<Path2D.Double> lines = VmlGeometry.subPaths(o.getOutlines().get(0).getPath());
		assertEquals(2, lines.size());
		assertPoints(new double[][] { { 0, 0 }, { 468, 0 } }, lines.get(0));
		assertPoints(new double[][] { { 0, 117 }, { 468, 117 } }, lines.get(1));
		assertEquals(new Rectangle2D.Double(0, 0, 468, 117), o.getTextRectangle());
	}

	@Test
	public void theWatermarksShapetypeAdjusted() {
		// adj 14000: @0 3200, @1 28000, @2 -6400, @3 6400, @4 15200; top 0 to 15200, bottom 6400 to 21600
		ShapeOutlines o = VmlGeometry.outlines(d136("14000"), 216, 216);
		List<Path2D.Double> lines = VmlGeometry.subPaths(o.getOutlines().get(0).getPath());
		assertPoints(new double[][] { { 0, 0 }, { 152, 0 } }, lines.get(0));
		assertPoints(new double[][] { { 64, 216 }, { 216, 216 } }, lines.get(1));
		// a value left out of the shape's adj is the shapetype's (real3's ",10800")
		assertArrayEquals(new Integer[] { 10800, 10800 }, VmlGeometry.adj(VmlGeometry.adj(",10800"), VmlGeometry.adj("10800,5400")));
		assertArrayEquals(new Integer[] { null, 10800 }, VmlGeometry.adj(",10800"));
	}

	@Test
	public void wordsSpellingOfASlantedPathIsTheSamePath() {
		// Word's save of phase 0's wv20-42 (phase 4 findings §1.2): values left out are 0, r is relative
		ShapeOutlines saved = VmlGeometry.outlines(new VmlGeometry.Definition().path("m,l21600,10800m,21600r21600,e")
				.coordsize("21600,21600"), 288, 144);
		ShapeOutlines input = VmlGeometry.outlines(new VmlGeometry.Definition().path("m0,0l21600,10800m0,21600l21600,21600e")
				.coordsize("21600,21600"), 288, 144);
		assertNull(saved.getProblem());
		double[][] expected = { { 0, 0 }, { 288, 72 }, { 0, 144 }, { 288, 144 } };
		assertPoints(expected, saved.getOutlines().get(0).getPath());
		assertPoints(expected, input.getOutlines().get(0).getPath());
	}

	@Test
	public void theStandardsArrow() {
		// Part 4 §19.1.2.4's example, a blue arrow
		VmlGeometry.Definition d = new VmlGeometry.Definition().coordsize("21600,21600")
				.adj(VmlGeometry.adj("18000,5400,10800"))
				.formulas(Arrays.asList("val #0", "val #1", "val #2", "sum height 0 #1", "sum #2 0 #1", "sum width 0 #0",
						"prod @5 @4 #2", "sum width 0 @6"))
				.path("m @0,0 l @0,@1 0,@1 0,@3 @0,@3 @0,21600 21600,10800 x e");
		ShapeOutlines o = VmlGeometry.outlines(d, 21600, 21600);
		assertNull(o.getProblem());
		assertPoints(new double[][] { { 18000, 0 }, { 18000, 5400 }, { 0, 5400 }, { 0, 16200 }, { 18000, 16200 },
				{ 18000, 21600 }, { 21600, 10800 } }, o.getOutlines().get(0).getPath());
	}

	@Test
	public void theOperationsRoundAsPart4Says() {
		assertEquals(2, VmlGeometry.operate("prod", 3, 1, 2)); // 1.5: half up
		assertEquals(-1, VmlGeometry.operate("prod", -3, 1, 2)); // -1.5: half up
		assertEquals(2, VmlGeometry.operate("product", 5, 1, 3)); // 1.67, the standard's spelling
		assertEquals(0, VmlGeometry.operate("prod", 5, 5, 0)); // division by zero
		assertEquals(-1, VmlGeometry.operate("mid", -3, 0, 0)); // toward 0
		assertEquals(1, VmlGeometry.operate("mid", 3, 0, 0));
		assertEquals(5, VmlGeometry.operate("mod", 3, 4, 0));
		assertEquals(45 * 65536, VmlGeometry.operate("atan2", 1, 1, 0));
		assertEquals(5400, VmlGeometry.operate("sin", 10800, 30 * 65536, 0));
		assertEquals(5400, VmlGeometry.operate("cos", 10800, 60 * 65536, 0));
		assertEquals(10, VmlGeometry.operate("cosatan2", 10, 1, 0));
		assertEquals(0, VmlGeometry.operate("sinatan2", 10, 1, 0));
		assertEquals(3, VmlGeometry.operate("sqrt", 10, 0, 0));
		assertEquals(0, VmlGeometry.operate("sqrt", -4, 0, 0));
		assertEquals(90 * 65536, VmlGeometry.operate("sumangle", 0, 90, 0));
		assertEquals(20, VmlGeometry.operate("ellipse", 0, 10, 20));
		assertEquals(0, VmlGeometry.operate("ellipse", 10, 10, 20));
		assertEquals(0, VmlGeometry.operate("ellipse", 30, 10, 20)); // past its axis
		assertEquals(2, VmlGeometry.operate("if", 0, 1, 2));
		assertEquals(1, VmlGeometry.operate("if", 1, 1, 2));
		assertEquals(-7, VmlGeometry.operate("min", -7, 3, 0));
		assertEquals(7, VmlGeometry.operate("abs", -7, 0, 0));
	}

	@Test
	public void referencesThatDontResolveAreZero() {
		// a forward @n, an #n past the adj list, a missing argument
		VmlGeometry.Definition d = new VmlGeometry.Definition().coordsize("100,100").adj(VmlGeometry.adj("40"))
				.formulas(Arrays.asList("sum @1 10 0", "sum #3 20 0", "val")).path("m@0,@1l@2,100e");
		ShapeOutlines o = VmlGeometry.outlines(d, 100, 100);
		assertNull(o.getProblem());
		assertPoints(new double[][] { { 10, 20 }, { 0, 100 } }, o.getOutlines().get(0).getPath());
	}

	@Test
	public void namedValues() {
		List<String> eqns = Arrays.asList("val width", "val height", "val xcenter", "val ycenter", "val pixelWidth",
				"val emuWidth2", "val pixelLineWidth", "val hasfill", "val lineDrawn");
		VmlGeometry.Definition d = new VmlGeometry.Definition().coordsize("200,100").coordorigin("100,50").paint(false, true, 0.75);
		long[] f = VmlGeometry.formulas(eqns, new Integer[0], new VmlGeometry.Space(d, 72, 36));
		assertArrayEquals(new long[] { 200, 100, 200, 100, 96, 457200, 1, 0, 1 }, f);
	}

	@Test
	public void coordoriginMovesTheSpace() {
		ShapeOutlines o = VmlGeometry.outlines(new VmlGeometry.Definition().coordsize("1000,1000").coordorigin("1000,1000")
				.path("m1000,1000l2000,2000e"), 50, 50);
		assertPoints(new double[][] { { 0, 0 }, { 50, 50 } }, o.getOutlines().get(0).getPath());
		// absent or not positive: 1,000
		o = VmlGeometry.outlines(new VmlGeometry.Definition().coordsize("0,-5").path("m0,0l1000,1000e"), 10, 20);
		assertPoints(new double[][] { { 0, 0 }, { 10, 20 } }, o.getOutlines().get(0).getPath());
	}

	@Test
	public void setsAndTheirFillAndStroke() {
		ShapeOutlines o = VmlGeometry.outlines(new VmlGeometry.Definition().coordsize("10,10")
				.path("m0,0l10,0nfem0,10l10,10nse"), 10, 10);
		assertEquals(2, o.getOutlines().size());
		assertEquals(STPathFillMode.NONE, o.getOutlines().get(0).getFill());
		assertTrue(o.getOutlines().get(0).isStroked());
		assertEquals(STPathFillMode.NORM, o.getOutlines().get(1).getFill());
		assertFalse(o.getOutlines().get(1).isStroked());
		assertEquals(Path2D.WIND_EVEN_ODD, o.getOutlines().get(0).getPath().getWindingRule());
	}

	@Test
	public void repeatedParametersRepeatTheCommand() {
		ShapeOutlines o = VmlGeometry.outlines(new VmlGeometry.Definition().coordsize("10,10")
				.path("m0,0 l 1,1 2,2 3,3 r1,0,0,1 e"), 10, 10);
		assertPoints(new double[][] { { 0, 0 }, { 1, 1 }, { 2, 2 }, { 3, 3 }, { 4, 3 }, { 4, 4 } },
				o.getOutlines().get(0).getPath());
		// @ delimits, as Word writes it
		o = VmlGeometry.outlines(new VmlGeometry.Definition().coordsize("10,10").formulas(Arrays.asList("val 2", "val 5"))
				.path("m@0@1l@1@0e"), 10, 10);
		assertPoints(new double[][] { { 2, 5 }, { 5, 2 } }, o.getOutlines().get(0).getPath());
	}

	@Test
	public void quadrantsAlternate() {
		// qx from (0,0) to (10,10) starts horizontal; its second pair, a qy, from (10,10) to (0,20) starts vertical
		ShapeOutlines o = VmlGeometry.outlines(new VmlGeometry.Definition().coordsize("100,100")
				.path("m0,0qx10,10,0,20e"), 100, 100);
		List<double[]> p = points(o.getOutlines().get(0).getPath());
		assertEquals(7, p.size());
		assertEquals(0, p.get(1)[1], E); // first control on the start's horizontal
		assertEquals(10, p.get(2)[0], E); // second control on the end's vertical
		assertEquals(10, p.get(4)[0], E); // the qy starts vertical
		assertEquals(20, p.get(5)[1], E); // and ends horizontal
	}

	@Test
	public void aQuadraticWithNoStartIsClosedFromItsEnd() {
		ShapeOutlines o = VmlGeometry.outlines(new VmlGeometry.Definition().coordsize("10,10").path("qb0,10,10,10,10,0e"), 10, 10);
		List<double[]> p = points(o.getOutlines().get(0).getPath());
		// move to the end (10,0); quad via (0,10) to the midpoint (5,10); quad via (10,10) to (10,0)
		assertEquals(10, p.get(0)[0], E);
		assertEquals(0, p.get(0)[1], E);
		assertEquals(5, p.get(2)[0], E);
		assertEquals(10, p.get(2)[1], E);
	}

	@Test
	public void anAngleEllipseOfATurnIsTheWholeEllipse() {
		ShapeOutlines o = VmlGeometry.outlines(new VmlGeometry.Definition().coordsize("21600,21600")
				.path("al10800,10800,10800,10800,0,23592960e"), 100, 50);
		Rectangle2D b = o.getOutlines().get(0).getPath().getBounds2D();
		assertEquals(0, b.getMinX(), 1e-3);
		assertEquals(100, b.getMaxX(), 1e-3);
		assertEquals(0, b.getMinY(), 1e-3);
		assertEquals(50, b.getMaxY(), 1e-3);
		// a sweep past a turn is a turn
		ShapeOutlines more = VmlGeometry.outlines(new VmlGeometry.Definition().coordsize("21600,21600")
				.path("al10800,10800,10800,10800,0,2000000000e"), 100, 50);
		assertEquals(points(o.getOutlines().get(0).getPath()).size(), points(more.getOutlines().get(0).getPath()).size());
	}

	@Test
	public void counterclockwiseAndClockwiseArcsAreTheTwoHalves() {
		// from the right (21600,10800) to the left (0,10800) of the circle: ar counterclockwise goes over the top on the
		// page (y down), wr clockwise under the bottom
		String box = "0,0,21600,21600,21600,10800,0,10800";
		ShapeOutlines ccw = VmlGeometry.outlines(new VmlGeometry.Definition().coordsize("21600,21600").path("ar" + box + "e"), 100, 100);
		ShapeOutlines cw = VmlGeometry.outlines(new VmlGeometry.Definition().coordsize("21600,21600").path("wr" + box + "e"), 100, 100);
		Rectangle2D a = ccw.getOutlines().get(0).getPath().getBounds2D(), b = cw.getOutlines().get(0).getPath().getBounds2D();
		assertEquals(0, a.getMinY(), 1e-3);
		assertEquals(50, a.getMaxY(), 1e-3);
		assertEquals(50, b.getMinY(), 1e-3);
		assertEquals(100, b.getMaxY(), 1e-3);
		// equal radial points: the whole ellipse
		ShapeOutlines whole = VmlGeometry.outlines(new VmlGeometry.Definition().coordsize("21600,21600")
				.path("ar0,0,21600,21600,21600,10800,21600,10800e"), 100, 100);
		assertEquals(100, whole.getOutlines().get(0).getPath().getBounds2D().getHeight(), 1e-3);
	}

	@Test
	public void theTextBoxRectangle() {
		ShapeOutlines o = VmlGeometry.outlines(new VmlGeometry.Definition().coordsize("1000,1000")
				.formulas(Arrays.asList("val 250")).textboxrect("0,@0,850,1000;0,0,10,10").path("m0,0l1000,1000e"), 100, 200);
		assertEquals(new Rectangle2D.Double(0, 50, 85, 150), o.getTextRectangle());
	}

	@Test
	public void noPathNoOutlines() {
		ShapeOutlines o = VmlGeometry.outlines(new VmlGeometry.Definition(), 100, 100);
		assertNull(o.getProblem());
		assertTrue(o.getOutlines().isEmpty());
	}

	@Test
	public void whatCantBeReadOrIsPastALimit() {
		assertEquals(ShapeOutlines.BAD_GEOMETRY,
				VmlGeometry.outlines(new VmlGeometry.Definition().path("m0,0z10,10e"), 10, 10).getProblem());
		assertEquals(ShapeOutlines.BAD_GEOMETRY, VmlGeometry.outlines(new VmlGeometry.Definition()
				.formulas(Arrays.asList("frobnicate 1 2 3")).path("m0,0l1,1e"), 10, 10).getProblem());
		assertEquals(ShapeOutlines.BAD_GEOMETRY, VmlGeometry.outlines(new VmlGeometry.Definition()
				.formulas(Arrays.asList("val nosuchvalue")).path("m0,0l1,1e"), 10, 10).getProblem());
		List<String> many = new ArrayList<String>();
		for (int i = 0; i < 129; i++) many.add("val 1");
		assertEquals(ShapeOutlines.OVER_LIMIT,
				VmlGeometry.outlines(new VmlGeometry.Definition().formulas(many).path("m0,0l1,1e"), 10, 10).getProblem());
		StringBuilder sub = new StringBuilder();
		for (int i = 0; i < 300; i++) sub.append("m0,").append(i).append("l1,").append(i);
		assertEquals(ShapeOutlines.OVER_LIMIT,
				VmlGeometry.outlines(new VmlGeometry.Definition().path(sub.append("e").toString()), 10, 10).getProblem());
		// a result past 32 bits is clamped, not wrapped
		assertEquals(Integer.MAX_VALUE, VmlGeometry.formulas(Arrays.asList("prod 2000000000 2000000000 1"), new Integer[0],
				new VmlGeometry.Space(new VmlGeometry.Definition(), 10, 10))[0]);
	}
}
