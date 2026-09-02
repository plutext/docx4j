import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Patches the XJC output in target/generated-sources/xjc (parent pointers,
 * SdtElement signatures, VML attribute order, etc).  Cross-platform
 * replacement for modify-generated-sources.sh, which needed bash/perl/sed/
 * find and so stopped docx4j building on native Windows (see
 * TODO_Windows_builds.md).  Invoked by exec-maven-plugin at process-sources
 * via single-file source launch (JEP 330): {@code java ModifyGeneratedSources.java}
 * — no compile step, no dependencies.  Must stay Java 11 source compatible
 * (so: no text blocks); the search/replace strings below are byte-exact
 * transcriptions of the shell script's heredocs, INCLUDING trailing spaces
 * in Javadoc lines and the tab/space mix in the Highlight/Styles blocks —
 * do not let an editor or formatter "clean" them.
 *
 * Same contract as the script it replaces:
 * - idempotent: each patch is guarded by a docx4j:patched:&lt;id&gt; marker, so
 *   running "mvn install" twice is a no-op;
 * - fails the build (exit 1) if a search string is not found — that means
 *   the XJC output has drifted and the patch needs updating.
 *
 * Line endings: content is normalized to \n for matching and written back
 * with the platform line separator — which is what XJC just emitted, so on
 * every platform the patched files keep the convention of the unpatched ones.
 */
public class ModifyGeneratedSources {

	private static Path root;

	public static void main(String[] args) throws Exception {

		root = Paths.get(args.length > 0 ? args[0] : "target/generated-sources/xjc");
		if (!Files.isDirectory(root)) {
			fail("Not a directory: " + root.toAbsolutePath());
		}

		///////////////////////////////////////////////////////////////////
		// Update constructors for custom collection type, ArrayListDocx4j
		///////////////////////////////////////////////////////////////////

		System.out.println("Updating ArrayListDocx4j constructor to pass \"this\" as argument");
		final int[] changed = { 0 };
		try (Stream<Path> s = Files.walk(root)) {
			s.filter(p -> p.toString().endsWith(".java")).forEach(p -> {
				try {
					String content = read(p);
					if (content.contains("new ArrayListDocx4j<>();")) {
						write(p, content.replace("new ArrayListDocx4j<>();", "new ArrayListDocx4j<>(this);"));
						changed[0]++;
					}
				} catch (IOException e) {
					throw new java.io.UncheckedIOException(e);
				}
			});
		}
		System.out.println("  updated " + changed[0] + " files");

		////////////
		// Document
		////////////

		System.out.println("Updating Document to set parent when body is set");
		patchOnce("Document-setBody-parent",
			"    public void setBody(Body value) {\n"
			+ "        this.body = value;\n"
			+ "    }",
			"    public void setBody(Body value) {\n"
			+ "        this.body = value;\n"
			+ "        value.setParent(this);\n"
			+ "    }\n"
			+ "    // docx4j:patched:Document-setBody-parent",
			"org/docx4j/wml/Document.java");

		////////
		// PML
		////////

		System.out.println("Updating CTSection to lazily create sldIdLst");
		patchOnce("CTSection-getSldIdLst-lazy-create",
			"    public CTSectionSlideIdList getSldIdLst() {\n"
			+ "        return sldIdLst;\n"
			+ "    }",
			"    public CTSectionSlideIdList getSldIdLst() {\n"
			+ "        if (sldIdLst == null) {\n"
			+ "            sldIdLst = new CTSectionSlideIdList();\n"
			+ "        }\n"
			+ "        return sldIdLst;\n"
			+ "    }\n"
			+ "    // docx4j:patched:CTSection-getSldIdLst-lazy-create",
			"org/pptx4j/com/microsoft/schemas/office/powerpoint/x2010/main/CTSection.java");

		/////////////////////////
		// SdtElement Interface
		/////////////////////////

		System.out.println("Updating methods for classes that need to implement SdtElement");

		// SdtRun
		patchOnce("SdtRun-setSdtContent",
			"    public void setSdtContent(CTSdtContentRun value) {\n"
			+ "        this.sdtContent = value;\n"
			+ "    }",
			"    public void setSdtContent(SdtContent value) {\n"
			+ "        this.sdtContent = (CTSdtContentRun)value;\n"
			+ "        ((CTSdtContentRun)value).setParent(this); // unmarshalling does this automatically; this helps user in other cases\n"
			+ "    }\n"
			+ "    // docx4j:patched:SdtRun-setSdtContent",
			"org/docx4j/wml/SdtRun.java");

		patchOnce("SdtRun-setSdtPr",
			SET_SDT_PR_SEARCH,
			SET_SDT_PR_REPLACEMENT + "    // docx4j:patched:SdtRun-setSdtPr",
			"org/docx4j/wml/SdtRun.java");

		// SdtBlock
		patchOnce("SdtBlock-setSdtContent",
			"    public void setSdtContent(SdtContentBlock value) {\n"
			+ "        this.sdtContent = value;\n"
			+ "    }",
			"    public void setSdtContent(SdtContent value) {\n"
			+ "        this.sdtContent = (SdtContentBlock)value;\n"
			+ "        ((SdtContentBlock)value).setParent(this); // unmarshalling does this automatically; this helps user in other cases\n"
			+ "    }\n"
			+ "    // docx4j:patched:SdtBlock-setSdtContent",
			"org/docx4j/wml/SdtBlock.java");

		patchOnce("SdtBlock-setSdtPr",
			SET_SDT_PR_SEARCH,
			SET_SDT_PR_REPLACEMENT + "    // docx4j:patched:SdtBlock-setSdtPr",
			"org/docx4j/wml/SdtBlock.java");

		// CTSdtCell
		patchOnce("CTSdtCell-setSdtContent",
			"    public void setSdtContent(CTSdtContentCell value) {\n"
			+ "        this.sdtContent = value;\n"
			+ "    }",
			"    public void setSdtContent(SdtContent value) {\n"
			+ "        this.sdtContent = (CTSdtContentCell)value;\n"
			+ "        ((CTSdtContentCell)value).setParent(this); // unmarshalling does this automatically; this helps user in other cases\n"
			+ "    }\n"
			+ "    // docx4j:patched:CTSdtCell-setSdtContent",
			"org/docx4j/wml/CTSdtCell.java");

		patchOnce("CTSdtCell-setSdtPr",
			SET_SDT_PR_SEARCH,
			SET_SDT_PR_REPLACEMENT + "    // docx4j:patched:CTSdtCell-setSdtPr",
			"org/docx4j/wml/CTSdtCell.java");

		// CTSdtRow (note: "(CTSdtContentRow) value" with a space, as in the script)
		patchOnce("CTSdtRow-setSdtContent",
			"    public void setSdtContent(CTSdtContentRow value) {\n"
			+ "        this.sdtContent = value;\n"
			+ "    }",
			"    public void setSdtContent(SdtContent value) {\n"
			+ "        this.sdtContent = (CTSdtContentRow) value;\n"
			+ "        ((CTSdtContentRow) value).setParent(this); // unmarshalling does this automatically; this helps user in other cases\n"
			+ "    }\n"
			+ "    // docx4j:patched:CTSdtRow-setSdtContent",
			"org/docx4j/wml/CTSdtRow.java");

		patchOnce("CTSdtRow-setSdtPr",
			SET_SDT_PR_SEARCH,
			SET_SDT_PR_REPLACEMENT + "    // docx4j:patched:CTSdtRow-setSdtPr",
			"org/docx4j/wml/CTSdtRow.java");

		//////////////////////
		// WML ObjectFactory
		//////////////////////

		System.out.println("Updating WML ObjectFactory to add get() method returning factory instance");
		patchOnce("WmlObjectFactory-get",
			"public class ObjectFactory {",
			"public class ObjectFactory {\n"
			+ "\n"
			+ "    private static ObjectFactory thisObjectFactory;\n"
			+ "\n"
			+ "    public static ObjectFactory get() {\n"
			+ "        if (thisObjectFactory==null) {\n"
			+ "            thisObjectFactory=new ObjectFactory();\n"
			+ "        }\n"
			+ "        return thisObjectFactory;\n"
			+ "    }\n"
			+ "    // docx4j:patched:WmlObjectFactory-get",
			"org/docx4j/wml/ObjectFactory.java");

		//////////////////////
		// DML ObjectFactory
		//////////////////////

		System.out.println("Updating DML ObjectFactory to make _UserShapes_QNAME public");
		patchOnce("DmlChartObjectFactory-UserShapesQname",
			"private static final QName _UserShapes_QNAME =",
			"// docx4j:patched:DmlChartObjectFactory-UserShapesQname\n"
			+ "public static final QName _UserShapes_QNAME =",
			"org/docx4j/dml/chart/ObjectFactory.java");

		/////////////
		// VML Line
		/////////////

		System.out.println("Updating CTLine field order");
		// Note the trailing spaces in the Javadoc search strings ("     * ").

		patchOnce("CTLine-remove-generated-from",
			"    /**\n"
			+ "     * Line Start\n"
			+ "     * \n"
			+ "     */\n"
			+ "    @XmlAttribute(name = \"from\")\n"
			+ "    protected String from;",
			"    // docx4j:patched:CTLine-remove-generated-from",
			"org/docx4j/vml/CTLine.java");

		patchOnce("CTLine-remove-generated-to",
			"    /**\n"
			+ "     * Line End Point\n"
			+ "     * \n"
			+ "     */\n"
			+ "    @XmlAttribute(name = \"to\")\n"
			+ "    protected String to;",
			"    // docx4j:patched:CTLine-remove-generated-to",
			"org/docx4j/vml/CTLine.java");

		patchOnce("CTLine-remove-generated-id",
			"    /**\n"
			+ "     * Unique Identifier\n"
			+ "     * \n"
			+ "     */\n"
			+ "    @XmlAttribute(name = \"id\")\n"
			+ "    protected String vmlId;",
			"    // docx4j:patched:CTLine-remove-generated-id",
			"org/docx4j/vml/CTLine.java");

		patchOnce("CTLine-attribute-order",
			"    /**\n"
			+ "     * Shape Styling Properties\n"
			+ "     * \n"
			+ "     */\n"
			+ "    @XmlAttribute(name = \"style\")\n"
			+ "    protected String style;",
			"    /*\n"
			+ "     * docx4j:patched:CTLine-attribute-order\n"
			+ "     * Word is sensitive to the order of the id, style, from, and to attributes; see https://github.com/plutext/docx4j/issues/469\n"
			+ "     */\n"
			+ "    /**\n"
			+ "     * Unique Identifier\n"
			+ "     * \n"
			+ "     */\n"
			+ "    @XmlAttribute(name = \"id\")\n"
			+ "    protected String vmlId;\n"
			+ "    /**\n"
			+ "     * Shape Styling Properties\n"
			+ "     * \n"
			+ "     */\n"
			+ "    @XmlAttribute(name = \"style\")\n"
			+ "    protected String style;\n"
			+ "    /**\n"
			+ "     * Line Start\n"
			+ "     * \n"
			+ "     */\n"
			+ "    @XmlAttribute(name = \"from\")\n"
			+ "    protected String from;\n"
			+ "    /**\n"
			+ "     * Line End Point\n"
			+ "     * \n"
			+ "     */\n"
			+ "    @XmlAttribute(name = \"to\")\n"
			+ "    protected String to;",
			"org/docx4j/vml/CTLine.java");

		///////
		// Id
		///////

		System.out.println("Adding Id.java equals/hashCode methods");
		patchOnce("Id-equals-hashCode",
			"    @Override\n"
			+ "    public Object copy() {\n"
			+ "        Id copy = new Id();\n"
			+ "        return copyTo(copy);\n"
			+ "    }\n"
			+ "\n"
			+ "}",
			"    @Override\n"
			+ "    public Object copy() {\n"
			+ "        Id copy = new Id();\n"
			+ "        return copyTo(copy);\n"
			+ "    }\n"
			+ "\n"
			+ "    public boolean equals(Object obj) {\n"
			+ "        if (obj instanceof Id) {\n"
			+ "            return val.equals(((Id) obj).getVal());\n"
			+ "        } else {\n"
			+ "            return false;\n"
			+ "        }\n"
			+ "    }\n"
			+ "\n"
			+ "    public int hashCode() {\n"
			+ "        if (val == null) {\n"
			+ "            java.math.BigInteger newIdVal = java.math.BigInteger.valueOf(Math.abs(new java.util.Random().nextInt()));\n"
			+ "            this.setVal(newIdVal);\n"
			+ "            org.slf4j.LoggerFactory.getLogger(Id.class).warn(\"Generated Id val \" + newIdVal);\n"
			+ "        }\n"
			+ "\n"
			+ "        // Natural and good enough...\n"
			+ "        return val.intValue();\n"
			+ "    }\n"
			+ "\n"
			+ "    // docx4j:patched:Id-equals-hashCode\n"
			+ "\n"
			+ "}",
			"org/docx4j/wml/Id.java");

		//////////
		// Style
		//////////

		System.out.println("Updating Style.java customStyle default");
		patchOnce("Style-customStyle-default",
			"    public boolean isCustomStyle() {\n"
			+ "        if (customStyle == null) {\n"
			+ "            return true;\n"
			+ "        } else {\n"
			+ "            return customStyle;\n"
			+ "        }\n"
			+ "    }",
			"    public boolean isCustomStyle() {\n"
			+ "        if (customStyle == null) {\n"
			+ "            // the style shall be assumed to be a built-in style: https://github.com/plutext/docx4j/issues/641\n"
			+ "            return false;\n"
			+ "        } else {\n"
			+ "            return customStyle;\n"
			+ "        }\n"
			+ "    }\n"
			+ "    // docx4j:patched:Style-customStyle-default",
			"org/docx4j/wml/Style.java");

		//////////////
		// Highlight
		//////////////

		System.out.println("Updating Highlight.java with custom methods");
		// The replacement's tab/space mix is exactly the shell script's.
		patchOnce("Highlight-custom-methods",
			"    public void setVal(String value) {\n"
			+ "        this.val = value;\n"
			+ "    }",
			"    public void setVal(String value) {\n"
			+ "\n"
			+ "    \tif (value==null) {\n"
			+ "    \t\tthis.val = value;\n"
			+ "    \t\treturn;\n"
			+ "    \t}\n"
			+ "\n"
			+ "    \tboolean inEnumeration = false;\n"
			+ "    \tfor (int i = 0; i<colors.length; i++) {\n"
			+ "    \t\tif (value.equals(colors[i][0])) {\n"
			+ "    \t\t\tinEnumeration = true;\n"
			+ "    \t\t\tbreak;\n"
			+ "    \t\t}\n"
			+ "    \t}\n"
			+ "\n"
			+ "    \tif (inEnumeration) {\n"
			+ "    \t\tthis.val = value;\n"
			+ "    \t\treturn;\n"
			+ "    \t} else if (value.trim().startsWith(\"#\")) {\n"
			+ "    \t\tvalue=value.trim().substring(1).toUpperCase();\n"
			+ "\n"
			+ "        \tfor (int i = 0; i<colors.length; i++) {\n"
			+ "        \t\tif (value.equals(colors[i][1])) {\n"
			+ "        \t\t\tval = colors[i][0];\n"
			+ "        \t\t\treturn;\n"
			+ "        \t\t}\n"
			+ "        \t}\n"
			+ "\n"
			+ "    \t\tlog.error(\"use enumerated color, or implement algorithm to map to closest color: '\" + value + \"'\");\n"
			+ "\n"
			+ "    \t} else if (value.trim().contains(\"rgb\")) {\n"
			+ "\n"
			+ "    \t\tlog.warn(\"TODO: implement rgb to color for '\" + value + \"'\");\n"
			+ "    \t}\n"
			+ "\t\tlog.error(\"Can't set w:highlight from '\" + value + \"'\");\n"
			+ "    \tthis.val = null;\n"
			+ "    }\n"
			+ "\n"
			+ "    public String getHexVal() {\n"
			+ "\n"
			+ "    \tif (val==null) return null;\n"
			+ "\n"
			+ "    \tfor (int i = 0; i<colors.length; i++) {\n"
			+ "    \t\tif (val.equals(colors[i][0])) {\n"
			+ "    \t\t\treturn \"#\" + colors[i][1];\n"
			+ "    \t\t}\n"
			+ "    \t}\n"
			+ "\t\tlog.error(\"Unexpected w:highlight value '\" + val + \"'\");\n"
			+ "\t\treturn null;\n"
			+ "    }\n"
			+ "\n"
			+ "\tprotected static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Highlight.class);\n"
			+ "\n"
			+ "\t// See http://www.w3.org/TR/css3-color/#svg-color, except for darkYellow (for which I've used gold)\n"
			+ "\tprivate final static String[][] colors = { { \"black\", \"000000\" }, { \"blue\", \"0000FF\" },\n"
			+ "\t\t\t{ \"cyan\", \"00FFFF\" }, { \"green\", \"008000\" },\n"
			+ "\t\t\t{ \"magenta\", \"FF00FF\" }, { \"red\", \"FF0000\" },\n"
			+ "\t\t\t{ \"yellow\", \"FFFF00\" }, { \"white\", \"FFFFFF\" },\n"
			+ "\t\t\t{ \"darkBlue\", \"00008B\" }, { \"darkCyan\", \"008B8B\" },\n"
			+ "\t\t\t{ \"darkGreen\", \"006400\" }, { \"darkMagenta\", \"8B008B\" },\n"
			+ "\t\t\t{ \"darkRed\", \"8B0000\" }, { \"darkYellow\", \"FFD700\" },\n"
			+ "\t\t\t{ \"darkGray\", \"A9A9A9\" }, { \"lightGray\", \"D3D3D3\" } };\n"
			+ "\n"
			+ "    // docx4j:patched:Highlight-custom-methods",
			"org/docx4j/wml/Highlight.java");

		///////////
		// Styles
		///////////

		System.out.println("Updating Styles.java with custom methods");
		// Note the trailing tab after "} else {" — exactly as the script had it.
		patchOnce("Styles-custom-methods",
			"            public Boolean isQFormat() {\n"
			+ "                return qFormat;\n"
			+ "            }",
			"            public Boolean isQFormat() {\n"
			+ "            \t\n"
			+ "            \tif (qFormat==null) {\n"
			+ "            \t\treturn ((Styles.LatentStyles)this.parent).isDefQFormat();\n"
			+ "            \t} else {            \t\n"
			+ "            \t\treturn qFormat;\n"
			+ "            \t}\n"
			+ "            }\n"
			+ "            // docx4j:patched:Styles-custom-methods",
			"org/docx4j/wml/Styles.java");

		System.out.println("Done!");
	}

	// shared by the four setSdtPr patches (identical search; per-class marker)
	private static final String SET_SDT_PR_SEARCH =
			"    public void setSdtPr(SdtPr value) {\n"
			+ "        this.sdtPr = value;\n"
			+ "    }";
	private static final String SET_SDT_PR_REPLACEMENT =
			"    public void setSdtPr(SdtPr value) {\n"
			+ "        this.sdtPr = value;\n"
			+ "        value.setParent(this); // unmarshalling does this automatically; this helps user in other cases\n"
			+ "    }\n";

	// ------------------------------------------------------------------

	/**
	 * Replace the first occurrence of {@code search} in {@code file}, unless
	 * the file already contains the marker for {@code id}.  Exits with
	 * status 1 if the file or search string cannot be found.
	 */
	private static void patchOnce(String id, String search, String replacement, String file)
			throws IOException {
		Path p = root.resolve(file);
		if (!Files.isRegularFile(p)) {
			fail("File not found: " + file);
		}
		String content = read(p);
		String marker = "docx4j:patched:" + id;
		if (content.contains(marker)) {
			System.out.println(file + " already patched (" + id + "); skipping");
			return;
		}
		int i = content.indexOf(search);
		if (i < 0) {
			fail("Search string not found in " + file + " (" + id + ") - has the XJC output drifted?");
		}
		System.out.println("Performing replacement in " + file);
		write(p, content.substring(0, i) + replacement + content.substring(i + search.length()));
	}

	/** Read as UTF-8, normalized to \n so the patch strings match on any platform. */
	private static String read(Path p) throws IOException {
		return new String(Files.readAllBytes(p), StandardCharsets.UTF_8).replace("\r\n", "\n");
	}

	/** Write as UTF-8 with the platform line separator (what XJC just emitted). */
	private static void write(Path p, String content) throws IOException {
		String eol = System.lineSeparator();
		if (!"\n".equals(eol)) {
			content = content.replace("\n", eol);
		}
		Files.write(p, content.getBytes(StandardCharsets.UTF_8));
	}

	private static void fail(String message) {
		System.err.println(message);
		System.exit(1);
	}
}
