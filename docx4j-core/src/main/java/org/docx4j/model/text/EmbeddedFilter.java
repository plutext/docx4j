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
package org.docx4j.model.text;

/**
 * Decides, object by object, whether an embedded object is read under {@code embedded: true}
 * (WD REQ-522), through {@link TextOptions#embeddedFilter(EmbeddedFilter)}. It is asked once for
 * each object the text model would read (an embedded OOXML package, an OLE 1.0 package; REQ-512),
 * before the reading. An object it declines contributes nothing, and keeps its object run and the
 * run's {@link TextObject}, so that a host can read it with its own parsers, or hand it on, where it
 * stands. With no filter every object is read.
 *
 * <p>A filter that throws is taken as declining: the object is not read, and the result carries
 * {@code content.not_read} with the object's part name; the reading goes on, as for an
 * {@link ImageDescriber} that fails (REQ-514).</p>
 *
 * @since 17.3.5 (CR-070)
 */
@FunctionalInterface
public interface EmbeddedFilter {

	/**
	 * Whether to read the object.
	 *
	 * @param object the object, as its run carries it
	 * @param address where its content is read (WD REQ-519): the anchoring paragraph's address and
	 *        the object segment, {@code body/3/object:0}; its blocks' addresses begin with it
	 */
	boolean read(TextObject object, String address);
}
