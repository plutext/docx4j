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

import java.util.Objects;

/**
 * A run's revision in the {@code markup} view (WD REQ-706): {@code w:ins} and {@code w:moveTo} are
 * insertions, {@code w:del} and {@code w:moveFrom} deletions, with their author and date as written.
 *
 * @since 17.3.4 (CR-046 phase 1)
 */
public final class TextRevision {

	public enum Type { INSERTION, DELETION }

	private final Type type;
	private final String author;
	private final String date;

	public TextRevision(Type type, String author, String date) {
		this.type = Objects.requireNonNull(type);
		this.author = author;
		this.date = date;
	}

	public Type getType() { return type; }
	public String getAuthor() { return author; }
	/** The date as the package wrote it ({@code w:date}), or null. */
	public String getDate() { return date; }

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof TextRevision)) return false;
		TextRevision r = (TextRevision) o;
		return type == r.type && Objects.equals(author, r.author) && Objects.equals(date, r.date);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, author, date);
	}

	@Override
	public String toString() {
		return type + (author == null ? "" : " by " + author) + (date == null ? "" : " " + date);
	}
}
