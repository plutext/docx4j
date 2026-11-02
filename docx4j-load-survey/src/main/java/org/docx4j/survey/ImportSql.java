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
package org.docx4j.survey;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes import.sql beside the TSVs: it replaces the catalogue's docx4j_load table (and its
 * messages table) with every row the TSVs hold. sqlite3's .import reads into staging tables of
 * text; the inserts turn an empty field into NULL and let the column types convert the numbers.
 */
final class ImportSql {

	private ImportSql() {
	}

	static Path write(Path out) throws IOException {
		Path abs = out.toAbsolutePath().normalize();
		StringBuilder sb = new StringBuilder();
		sb.append("-- Written by docx4j-load-survey: loads ").append(Coordinator.MAIN).append(" and ").append(Coordinator.MESSAGES)
				.append(" into the catalogue,\n-- replacing its docx4j_load tables.  sqlite3 CATALOGUE.db < ").append(abs.resolve("import.sql")).append("\n");
		sb.append(".bail on\n");
		table(sb, "docx4j_load", Column.values().length, i -> Column.values()[i].sqlName(), i -> Column.values()[i].sqlType,
				i -> Column.values()[i].description);
		table(sb, "docx4j_load_messages", Column.Message.values().length, i -> Column.Message.values()[i].sqlName(),
				i -> Column.Message.values()[i].sqlType, i -> Column.Message.values()[i].description);
		sb.append(".mode tabs\n");
		sb.append(".import --skip 1 '").append(abs.resolve(Coordinator.MAIN)).append("' docx4j_load_import\n");
		sb.append(".import --skip 1 '").append(abs.resolve(Coordinator.MESSAGES)).append("' docx4j_load_messages_import\n");
		insert(sb, "docx4j_load", Column.names(), "INSERT OR REPLACE");
		insert(sb, "docx4j_load_messages", Column.Message.names(), "INSERT");
		sb.append("DROP TABLE docx4j_load_import;\nDROP TABLE docx4j_load_messages_import;\n");
		sb.append("CREATE INDEX docx4j_load_outcome ON docx4j_load(outcome);\n");
		sb.append("CREATE INDEX docx4j_load_messages_sha256 ON docx4j_load_messages(sha256);\n");
		sb.append(".mode list\nSELECT outcome, count(*) FROM docx4j_load GROUP BY 1 ORDER BY 2 DESC;\n");
		Path p = abs.resolve("import.sql");
		Files.write(p, sb.toString().getBytes(StandardCharsets.UTF_8));
		return p;
	}

	private static void table(StringBuilder sb, String name, int n, java.util.function.IntFunction<String> col,
			java.util.function.IntFunction<String> type, java.util.function.IntFunction<String> doc) {
		sb.append("DROP TABLE IF EXISTS ").append(name).append(";\nCREATE TABLE ").append(name).append(" (\n");
		for (int i = 0; i < n; i++) {
			sb.append("  ").append(col.apply(i)).append(' ').append(type.apply(i)).append(i < n - 1 ? "," : "")
					.append("  -- ").append(doc.apply(i)).append('\n');
		}
		sb.append(");\nDROP TABLE IF EXISTS ").append(name).append("_import;\nCREATE TABLE ").append(name).append("_import (");
		for (int i = 0; i < n; i++) sb.append(i > 0 ? ", " : "").append(col.apply(i)).append(" TEXT");
		sb.append(");\n");
	}

	private static void insert(StringBuilder sb, String name, String[] cols, String verb) {
		sb.append(verb).append(" INTO ").append(name).append(" SELECT ");
		for (int i = 0; i < cols.length; i++) sb.append(i > 0 ? ", " : "").append("NULLIF(").append(cols[i]).append(", '')");
		sb.append(" FROM ").append(name).append("_import;\n");
	}
}
