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
package org.docx4j.openpackaging.io3;

import org.docx4j.jaxb.McLoadMode;

/**
 * How a package is loaded (CR-075 D1), one overload a source in place of one a setting. A setting left
 * unset follows its property.
 *
 * <pre>
 * WordprocessingMLPackage pkg = WordprocessingMLPackage.load(file, new LoadOptions().repairMode(RepairMode.WORD));
 * </pre>
 *
 * @since 17.3.6 (CR-075 phase 1)
 */
public final class LoadOptions {

	private McLoadMode mcLoadMode;
	private RepairMode repairMode;

	/** What an unmarshal does with an mc:AlternateContent at block, row or cell level; null follows {@value McLoadMode#PROPERTY}. */
	public LoadOptions mcLoadMode(McLoadMode mode) {
		this.mcLoadMode = mode;
		return this;
	}

	/** What the load does with a package Word repairs; null follows {@value RepairMode#PROPERTY}. */
	public LoadOptions repairMode(RepairMode mode) {
		this.repairMode = mode;
		return this;
	}

	/** The mode given, or null where it follows the property. */
	public McLoadMode getMcLoadMode() {
		return mcLoadMode;
	}

	/** The mode given, or null where it follows the property. */
	public RepairMode getRepairMode() {
		return repairMode;
	}

	/** The mode in force: the one given, else the property's. */
	public RepairMode effectiveRepairMode() {
		return repairMode != null ? repairMode : RepairMode.fromProperty();
	}
}
