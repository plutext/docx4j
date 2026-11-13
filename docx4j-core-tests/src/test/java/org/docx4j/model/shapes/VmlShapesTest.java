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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.vml.CTShape;
import org.docx4j.vml.CTTextPath;
import org.docx4j.vml.STTrueFalse;
import org.junit.Test;

/** A VML shape read with its shapetype (CR-083 phase 4): Word's watermark form, a type that doesn't resolve, groups. */
public class VmlShapesTest {

	private static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
			+ "xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" "
			+ "xmlns:w10=\"urn:schemas-microsoft-com:office:word\"";

	/** Shapetype 136 as Word writes it with a watermark (phase 4 findings §1.3). */
	private static final String T136 = "<v:shapetype id=\"_x0000_t136\" coordsize=\"21600,21600\" o:spt=\"136\" adj=\"10800\" "
			+ "path=\"m@7,l@8,m@5,21600l@6,21600e\"><v:formulas><v:f eqn=\"sum #0 0 10800\"/><v:f eqn=\"prod #0 2 1\"/>"
			+ "<v:f eqn=\"sum 21600 0 @1\"/><v:f eqn=\"sum 0 0 @2\"/><v:f eqn=\"sum 21600 0 @3\"/><v:f eqn=\"if @0 @3 0\"/>"
			+ "<v:f eqn=\"if @0 21600 @1\"/><v:f eqn=\"if @0 0 @2\"/><v:f eqn=\"if @0 @4 21600\"/><v:f eqn=\"mid @5 @6\"/>"
			+ "<v:f eqn=\"mid @8 @5\"/><v:f eqn=\"mid @7 @8\"/><v:f eqn=\"mid @6 @7\"/><v:f eqn=\"sum @6 0 @5\"/></v:formulas>"
			+ "<v:path textpathok=\"t\" o:connecttype=\"custom\" o:connectlocs=\"@9,0;@10,10800;@11,21600;@12,10800\" "
			+ "o:connectangles=\"270,180,90,0\"/><v:textpath on=\"t\" fitshape=\"t\"/>"
			+ "<v:handles><v:h position=\"#0,bottomRight\" xrange=\"6629,14971\"/></v:handles>"
			+ "<o:lock v:ext=\"edit\" text=\"t\" shapetype=\"t\"/></v:shapetype>";

	private static String mark(String id, String type, String text) {
		return "<v:shape id=\"" + id + "\" o:spid=\"_x0000_s2049\" type=\"" + type + "\" style=\"position:absolute;"
				+ "margin-left:0;margin-top:0;width:468pt;height:117pt;rotation:315;z-index:-251659776;"
				+ "mso-position-horizontal:center;mso-position-horizontal-relative:margin;mso-position-vertical:center;"
				+ "mso-position-vertical-relative:margin\" o:allowincell=\"f\" fillcolor=\"silver\" stroked=\"f\">"
				+ "<v:fill opacity=\".5\"/><v:textpath style=\"font-family:&quot;Aptos&quot;;font-size:1pt\" string=\"" + text
				+ "\"/><w10:wrap anchorx=\"margin\" anchory=\"margin\"/></v:shape>";
	}

	private static Object hdr(String picts) throws Exception {
		return XmlUtils.unmarshalString("<w:hdr " + NS + "><w:p><w:r>" + picts + "</w:r></w:p></w:hdr>");
	}

	private static List<CTShape> shapes(Object root) {
		final List<CTShape> out = new ArrayList<CTShape>();
		new TraversalUtil(root, new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				Object u = XmlUtils.unwrap(o);
				if (u instanceof CTShape && !out.contains(u)) out.add((CTShape) u);
				if (u instanceof org.docx4j.vml.CTGroup) {
					for (jakarta.xml.bind.JAXBElement<?> j : ((org.docx4j.vml.CTGroup) u).getPathOrFormulasOrHandles()) {
						if (j.getValue() instanceof CTShape && !out.contains(j.getValue())) out.add((CTShape) j.getValue());
					}
				}
				return null;
			}
		});
		return out;
	}

	@Test
	public void wordsWatermarkIsReadWithItsShapetype() throws Exception {
		Object root = hdr("<w:pict>" + T136 + mark("PowerPlusWaterMarkObject1", "#_x0000_t136", "WATERMARK") + "</w:pict>");
		CTShape shape = shapes(root).get(0);
		VmlShapes.Resolved r = VmlShapes.resolve(shape, root);
		assertNotNull(r.getShapetype());
		// on and fitshape from the shapetype, string and style from the shape
		assertTrue(r.isTextPath());
		assertEquals(STTrueFalse.T, r.child(CTTextPath.class, CTTextPath::getFitshape));
		assertEquals("WATERMARK", r.child(CTTextPath.class, CTTextPath::getString));
		assertTrue(r.child(CTTextPath.class, CTTextPath::getStyle).contains("font-size:1pt"));
		assertTrue(r.getStyle().contains("rotation:315"));
		assertTrue(r.isFilled());
		assertFalse(r.isStroked());
		assertEquals("silver", r.attribute(CTShape::getFillcolor, org.docx4j.vml.CTShapetype::getFillcolor));
		assertEquals(".5", r.child(org.docx4j.vml.CTFill.class, org.docx4j.vml.CTFill::getOpacity));
		assertEquals(14, r.getFormulas().size());
		// the geometry: the top and bottom lines of the box
		ShapeOutlines o = VmlGeometry.outlines(r.definition(), 468, 117);
		assertNull(o.getProblem());
		assertEquals(2, VmlGeometry.subPaths(o.getOutlines().get(0).getPath()).size());
		assertEquals(new Rectangle2D.Double(0, 0, 468, 117), o.getTextRectangle());
		// found from the shape's parents too
		assertSame(r.getShapetype(), VmlShapes.resolve(shape, null).getShapetype());
	}

	@Test
	public void aLaterPictCarriesOnlyTheType() throws Exception {
		Object root = hdr("<w:pict>" + T136 + mark("a", "#_x0000_t136", "ONE") + "</w:pict></w:r><w:r><w:pict>"
				+ mark("b", "#_x0000_t136", "TWO") + "</w:pict>");
		List<CTShape> shapes = shapes(root);
		assertEquals(2, shapes.size());
		VmlShapes.Resolved second = VmlShapes.resolve(shapes.get(1), root);
		assertNotNull(second.getShapetype());
		assertTrue(second.isTextPath());
		assertEquals("TWO", second.child(CTTextPath.class, CTTextPath::getString));
	}

	@Test
	public void aTypeThatDoesntResolveHasNoPath() throws Exception {
		// phase 0's wv20-41: the shape points at an id no shapetype has
		Object root = hdr("<w:pict>" + T136 + mark("a", "#_x0000_t136w", "WordArt") + "</w:pict>");
		VmlShapes.Resolved r = VmlShapes.resolve(shapes(root).get(0), root);
		assertNull(r.getShapetype());
		assertNull(r.getPath());
		// still WordArt (Word draws whatever on says, wv43-11), but with no path: nothing to draw
		assertTrue(r.isTextPath());
		assertTrue(VmlGeometry.outlines(r.definition(), 100, 100).getOutlines().isEmpty());
	}

	@Test
	public void theShapesAdjOverItsShapetypes() throws Exception {
		Object root = hdr("<w:pict>" + T136 + mark("a", "#_x0000_t136", "X").replace("<v:shape ", "<v:shape adj=\",7000\" ")
				+ "</w:pict>");
		VmlShapes.Resolved r = VmlShapes.resolve(shapes(root).get(0), root);
		assertArrayEquals(new Integer[] { 10800, 7000 }, r.getAdj());
	}

	@Test
	public void aGroupsShapetypeIsFound() throws Exception {
		// real3's form: a group in a w:object, its shapetype inside the group (phase 4 findings §1.4)
		String shape = "<v:shape id=\"_x0000_s2057\" type=\"#_x0000_t136\" style=\"position:absolute;left:6860;top:4460;"
				+ "width:630;height:180\" adj=\",10800\" fillcolor=\"blue\" strokecolor=\"blue\"><v:stroke joinstyle=\"miter\"/>"
				+ "<v:shadow color=\"#868686\"/><v:textpath style=\"font-family:&quot;Arial Black&quot;;font-size:8pt;"
				+ "v-text-kern:t\" trim=\"t\" fitpath=\"t\" string=\"Name\"/></v:shape>";
		Object root = hdr("<w:object><v:group id=\"_x0000_s2055\" style=\"position:absolute;margin-left:319.05pt;"
				+ "margin-top:-8.8pt;width:158.6pt;height:80.2pt;z-index:251666432\" coordorigin=\"6761,3418\" "
				+ "coordsize=\"3007,1499\">" + T136 + shape + "</v:group></w:object>");
		List<CTShape> shapes = shapes(root);
		assertEquals(1, shapes.size());
		VmlShapes.Resolved r = VmlShapes.resolve(shapes.get(0), root);
		assertNotNull(r.getShapetype());
		assertTrue(r.isTextPath());
		assertTrue(r.isStroked()); // legacy WordArt is outlined unless it says not
		assertEquals(0.75, r.strokeWeightPt(), 1e-9);
		assertEquals(STTrueFalse.T, r.child(CTTextPath.class, CTTextPath::getTrim));
		assertNull(r.child(org.docx4j.vml.CTShadow.class, org.docx4j.vml.CTShadow::getOn));
	}

	/** An adj past its handle's range is held to it, as Word evaluates it (cr083-phase4's wv42-49: 23313 as 14971) */
	@Test
	public void anAdjIsHeldToItsHandlesRange() throws Exception {
		String t = T136.replace("adj=\"10800\"", "adj=\"10800\"");
		Object root = hdr("<w:pict>" + t + mark("a", "#_x0000_t136", "X").replace("<v:shape ", "<v:shape adj=\"23313\" ") + "</w:pict>");
		assertArrayEquals(new Integer[] { 14971 }, VmlShapes.resolve(shapes(root).get(0), root).getAdj());
		root = hdr("<w:pict>" + t + mark("a", "#_x0000_t136", "X").replace("<v:shape ", "<v:shape adj=\"100\" ") + "</w:pict>");
		assertArrayEquals(new Integer[] { 6629 }, VmlShapes.resolve(shapes(root).get(0), root).getAdj());
	}

	/** on="f" doesn't stop Word drawing it (cr083-phase4's wv43-11) */
	@Test
	public void onOffIsStillWordArt() throws Exception {
		Object root = hdr("<w:pict>" + T136 + mark("a", "#_x0000_t136", "X").replace("<v:textpath ", "<v:textpath on=\"f\" ") + "</w:pict>");
		assertTrue(VmlShapes.resolve(shapes(root).get(0), root).isTextPath());
	}

	/** Two shapetypes of one id before the shape: the second, as Word draws them (cr083-phase4's wv43-07; review 2026-11-13) */
	@Test
	public void theLastShapetypeOfAnIdWins() throws Exception {
		String first = T136.replace("path=\"m@7,l@8,m@5,21600l@6,21600e\"", "path=\"m0,0l21600,10800m0,21600l21600,21600e\"");
		String second = T136.replace("path=\"m@7,l@8,m@5,21600l@6,21600e\"", "path=\"m0,10800l21600,0m0,21600l21600,21600e\"");
		Object root = hdr("<w:pict>" + first + second + mark("a", "#_x0000_t136", "X") + "</w:pict>");
		assertEquals("m0,10800l21600,0m0,21600l21600,21600e", VmlShapes.resolve(shapes(root).get(0), root).getPath());
	}

	/** A shapetype after its shape doesn't count: the shape has none (cr083-phase4b's wv49-05, Word draws nothing) */
	@Test
	public void aShapetypeAfterItsShapeDoesntCount() throws Exception {
		Object root = hdr("<w:pict>" + mark("a", "#_x0000_t136", "X") + T136 + "</w:pict>");
		VmlShapes.Resolved r = VmlShapes.resolve(shapes(root).get(0), root);
		assertNull(r.getShapetype());
		assertNull(r.getPath());
	}

	/**
	 * One id before the shape and again in a later w:pict: the one before (cr083-phase4b's wv49-06, Word's ink down to
	 * the right as the first's top line)
	 */
	@Test
	public void theShapetypeBeforeTheShapeWins() throws Exception {
		String first = T136.replace("path=\"m@7,l@8,m@5,21600l@6,21600e\"", "path=\"m0,0l21600,10800m0,21600l21600,21600e\"");
		String second = T136.replace("path=\"m@7,l@8,m@5,21600l@6,21600e\"", "path=\"m0,10800l21600,0m0,21600l21600,21600e\"");
		Object root = hdr("<w:pict>" + first + mark("a", "#_x0000_t136", "X") + "</w:pict></w:r><w:r><w:pict>" + second
				+ "</w:pict></w:r><w:r><w:pict>" + mark("b", "#_x0000_t136", "Y") + "</w:pict>");
		List<CTShape> s = shapes(root);
		assertEquals("m0,0l21600,10800m0,21600l21600,21600e", VmlShapes.resolve(s.get(0), root).getPath());
		// a shape after both: the second
		assertEquals("m0,10800l21600,0m0,21600l21600,21600e", VmlShapes.resolve(s.get(1), root).getPath());
	}

	/** A copy of the shape (the XSLT's routes unmarshal their node) is placed in the part by its attributes */
	@Test
	public void aCopyOfTheShapeIsFoundInThePart() throws Exception {
		String first = T136.replace("path=\"m@7,l@8,m@5,21600l@6,21600e\"", "path=\"m0,0l21600,10800m0,21600l21600,21600e\"");
		String second = T136.replace("path=\"m@7,l@8,m@5,21600l@6,21600e\"", "path=\"m0,10800l21600,0m0,21600l21600,21600e\"");
		Object root = hdr("<w:pict>" + first + mark("a", "#_x0000_t136", "X") + "</w:pict></w:r><w:r><w:pict>" + second
				+ "</w:pict>");
		Object copy = XmlUtils.unwrap(XmlUtils.unmarshalString("<w:pict " + NS + ">" + mark("a", "#_x0000_t136", "X") + "</w:pict>"));
		CTShape detached = shapes(copy).get(0);
		assertEquals("m0,0l21600,10800m0,21600l21600,21600e", VmlShapes.resolve(detached, root).getPath());
	}

	/** A conversion's cache: indexed once, a miss remembered rather than indexing the part again each time */
	@Test
	public void aConversionsCache() throws Exception {
		Object root = hdr("<w:pict>" + T136 + mark("a", "#_x0000_t136", "X") + "</w:pict></w:r><w:r><w:pict>"
				+ mark("b", "#_x0000_tnone", "Y") + "</w:pict>");
		java.util.Map<Object, VmlShapes.Index> cache = new java.util.IdentityHashMap<Object, VmlShapes.Index>();
		List<CTShape> s = shapes(root);
		assertNotNull(VmlShapes.resolve(s.get(0), root, cache).getShapetype());
		VmlShapes.Index first = cache.get(root);
		assertNull(VmlShapes.resolve(s.get(1), root, cache).getShapetype());
		VmlShapes.Index after = cache.get(root);
		assertNull(VmlShapes.resolve(s.get(1), root, cache).getShapetype());
		assertSame("a miss once indexed again is remembered", after, cache.get(root));
		assertNotNull(first);
	}

	@Test
	public void lengths() {
		assertEquals(1, VmlShapes.length("1pt", 0), 1e-9);
		assertEquals(72, VmlShapes.length("1in", 0), 1e-9);
		assertEquals(1.5, VmlShapes.length("2px", 0), 1e-9);
		assertEquals(1.5, VmlShapes.length("2", 0), 1e-9);
		assertEquals(1, VmlShapes.length("12700emu", 0), 1e-9);
		assertEquals(9, VmlShapes.length(null, 9), 1e-9);
		assertEquals(9, VmlShapes.length("wide", 9), 1e-9);
	}
}
