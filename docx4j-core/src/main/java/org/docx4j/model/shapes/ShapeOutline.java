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

import java.awt.geom.Path2D;

import org.docx4j.dml.STPathFillMode;

/**
 * One path of a shape's geometry ({@code a:path}), in points from the shape's top left, y down (CR-080 phase 1).
 *
 * @since 17.3.7
 */
public final class ShapeOutline {

	private final Path2D.Double path;
	private final STPathFillMode fill;
	private final boolean stroked;
	private final boolean extrusionOk;

	ShapeOutline(Path2D.Double path, STPathFillMode fill, boolean stroked, boolean extrusionOk) {
		this.path = path;
		this.fill = fill;
		this.stroked = stroked;
		this.extrusionOk = extrusionOk;
	}

	/** The path, in points from the shape's top left, y down. */
	public Path2D.Double getPath() {
		return path;
	}

	/**
	 * How the path is filled: {@code norm} with the shape's fill, {@code none} not at all, and {@code lighten},
	 * {@code lightenLess}, {@code darken} and {@code darkenLess} with the fill lightened or darkened.
	 */
	public STPathFillMode getFill() {
		return fill;
	}

	/** Whether the path is drawn with the shape's line. */
	public boolean isStroked() {
		return stroked;
	}

	/** Whether a 3-D extrusion may use the path ({@code extrusionOk}). */
	public boolean isExtrusionOk() {
		return extrusionOk;
	}
}
