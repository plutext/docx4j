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
package org.docx4j.markdown.math;

import org.docx4j.math.CTOMath;
import org.docx4j.math.CTOMathPara;

/**
 * OMML to LaTeX, now {@link org.docx4j.model.math.OmmlToLatex} in docx4j-core (CR-046 D3), so
 * that docx4j's plain text writes math as LaTeX too. This class forwards to it, and is kept
 * for one release.
 *
 * @deprecated since 17.3.4; use {@link org.docx4j.model.math.OmmlToLatex}
 */
@Deprecated
public class OmmlToLatex {

	private final org.docx4j.model.math.OmmlToLatex converter = new org.docx4j.model.math.OmmlToLatex();

	public String convertOMath(CTOMath oMath) throws OmmlMathException {
		try {
			return converter.convertOMath(oMath);
		} catch (org.docx4j.model.math.OmmlMathException e) {
			throw new OmmlMathException(e.getMessage());
		}
	}

	/** All the para's equations, joined (display math re-exports on one line). */
	public String convertOMathPara(CTOMathPara oMathPara) throws OmmlMathException {
		try {
			return converter.convertOMathPara(oMathPara);
		} catch (org.docx4j.model.math.OmmlMathException e) {
			throw new OmmlMathException(e.getMessage());
		}
	}
}
