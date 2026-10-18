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
package org.docx4j.model.math;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The LaTeX commands docx4j's math converters share: {@link OmmlToLatex} here, and
 * docx4j-markdown's {@code LatexToOmml}, which reads LaTeX with the same tables.
 *
 * @since 17.3.4 (CR-046 D3; formerly inside docx4j-markdown's {@code LatexToOmml})
 */
public final class LatexSymbols {

	private LatexSymbols() {}

	private static final Map<String, String> nary = new HashMap<>();
	private static final Map<String, String> symbols = new HashMap<>();
	/** The function names LaTeX sets upright ({@code \sin}, {@code \log}, ...). */
	public static final Set<String> FUNCTION_NAMES = Set.of(
			"sin", "cos", "tan", "cot", "sec", "csc",
			"arcsin", "arccos", "arctan", "sinh", "cosh", "tanh",
			"log", "ln", "lg", "exp", "max", "min", "sup", "inf",
			"lim", "arg", "det", "dim", "gcd", "mod");

	static {
		nary.put("sum", "∑");
		nary.put("prod", "∏");
		nary.put("coprod", "∐");
		nary.put("int", "∫");
		nary.put("iint", "∬");
		nary.put("iiint", "∭");
		nary.put("oint", "∮");
		nary.put("bigcup", "⋃");
		nary.put("bigcap", "⋂");

		// greek
		String[][] greek = {
			{"alpha", "α"}, {"beta", "β"}, {"gamma", "γ"}, {"delta", "δ"},
			{"epsilon", "ϵ"}, {"varepsilon", "ε"}, {"zeta", "ζ"}, {"eta", "η"},
			{"theta", "θ"}, {"vartheta", "ϑ"}, {"iota", "ι"}, {"kappa", "κ"},
			{"lambda", "λ"}, {"mu", "μ"}, {"nu", "ν"}, {"xi", "ξ"},
			{"pi", "π"}, {"varpi", "ϖ"}, {"rho", "ρ"}, {"varrho", "ϱ"},
			{"sigma", "σ"}, {"varsigma", "ς"}, {"tau", "τ"}, {"upsilon", "υ"},
			{"phi", "ϕ"}, {"varphi", "φ"}, {"chi", "χ"}, {"psi", "ψ"},
			{"omega", "ω"},
			{"Gamma", "Γ"}, {"Delta", "Δ"}, {"Theta", "Θ"}, {"Lambda", "Λ"},
			{"Xi", "Ξ"}, {"Pi", "Π"}, {"Sigma", "Σ"}, {"Upsilon", "Υ"},
			{"Phi", "Φ"}, {"Psi", "Ψ"}, {"Omega", "Ω"},
		};
		for (String[] g : greek) {
			symbols.put(g[0], g[1]);
		}

		// operators, relations, arrows, misc
		String[][] table = {
			{"pm", "±"}, {"mp", "∓"}, {"times", "×"}, {"cdot", "⋅"}, {"div", "÷"},
			{"ast", "∗"}, {"star", "⋆"}, {"circ", "∘"}, {"bullet", "•"},
			{"oplus", "⊕"}, {"ominus", "⊖"}, {"otimes", "⊗"}, {"oslash", "⊘"},
			{"le", "≤"}, {"leq", "≤"}, {"ge", "≥"}, {"geq", "≥"},
			{"ne", "≠"}, {"neq", "≠"}, {"approx", "≈"}, {"sim", "∼"},
			{"simeq", "≃"}, {"cong", "≅"}, {"equiv", "≡"}, {"propto", "∝"},
			{"ll", "≪"}, {"gg", "≫"}, {"prec", "≺"}, {"succ", "≻"},
			{"to", "→"}, {"rightarrow", "→"}, {"leftarrow", "←"},
			{"Rightarrow", "⇒"}, {"Leftarrow", "⇐"},
			{"leftrightarrow", "↔"}, {"Leftrightarrow", "⇔"},
			{"mapsto", "↦"}, {"uparrow", "↑"}, {"downarrow", "↓"},
			{"longrightarrow", "⟶"}, {"longleftarrow", "⟵"},
			{"longleftrightarrow", "⟷"}, {"Longrightarrow", "⟹"},
			{"Longleftarrow", "⟸"}, {"Longleftrightarrow", "⟺"},
			{"implies", "⟹"}, {"iff", "⟺"}, {"mid", "∣"},
			{"infty", "∞"}, {"partial", "∂"}, {"nabla", "∇"},
			{"hbar", "ℏ"}, {"ell", "ℓ"}, {"Re", "ℜ"}, {"Im", "ℑ"},
			{"aleph", "ℵ"}, {"wp", "℘"}, {"prime", "′"}, {"degree", "°"},
			{"cdots", "⋯"}, {"ldots", "…"}, {"dots", "…"}, {"vdots", "⋮"},
			{"ddots", "⋱"},
			{"in", "∈"}, {"notin", "∉"}, {"ni", "∋"},
			{"subset", "⊂"}, {"subseteq", "⊆"}, {"supset", "⊃"}, {"supseteq", "⊇"},
			{"cup", "∪"}, {"cap", "∩"}, {"setminus", "∖"},
			{"emptyset", "∅"}, {"varnothing", "∅"},
			{"forall", "∀"}, {"exists", "∃"}, {"nexists", "∄"}, {"neg", "¬"},
			{"wedge", "∧"}, {"land", "∧"}, {"vee", "∨"}, {"lor", "∨"},
			{"angle", "∠"}, {"perp", "⊥"}, {"parallel", "∥"}, {"mid", "∣"},
			{"langle", "⟨"}, {"rangle", "⟩"},
		};
		for (String[] s : table) {
			symbols.put(s[0], s[1]);
		}
	}

	/** The n-ary operators by command ({@code sum} to U+2211, ...). */
	public static final Map<String, String> NARY = Collections.unmodifiableMap(nary);

	/** The symbols by command: Greek letters, operators, relations, arrows ({@code alpha} to U+03B1, ...). */
	public static final Map<String, String> SYMBOLS = Collections.unmodifiableMap(symbols);
}
