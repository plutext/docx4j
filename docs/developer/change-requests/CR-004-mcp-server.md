# CR: docx4j MCP server (expose the engine to AI agents via Model Context Protocol)

Status: PROPOSED (2026-09-01)
Scope: a NEW satellite artifact (working name `docx4j-mcp`) — no changes to
docx4j-core beyond what the tools need; repo/module placement is an open question (§7)
Related: the "Why docx4j" website pages and Docx4j_from_Python.md (both answer
"I want docx4j's engine but don't write Java" — MCP is a third, stronger answer);
CR-002-fo-exporter-parity.md / CR-003-html-exporter-parity.md (the fast visitor exporters
are what a conversion tool should run on)

## 1. Background

Model Context Protocol (MCP) is the open protocol AI agents use to call external
tools: Claude Desktop, Claude Code, IDE assistants, and custom agent frameworks all
speak it, and MCP registries are becoming a discovery channel of their own.  A
server exposes *tools* (typed operations), optionally *resources* (readable data)
and *prompts*; clients connect over stdio (local process) or streamable HTTP.

The official Java SDK is `io.modelcontextprotocol.sdk` (currently 2.0.x,
`mcp-core`, BOM available; maintained with the Spring AI team; Tier 2 in the MCP
SDK tiering).  It requires **Java 17+**, provides sync and async server APIs, and
ships both stdio and streamable-HTTP transports in the core module with no web
framework dependency.  (Exact server API shapes to be pinned down in the phase 0
spike — do not trust recalled class names.)

### Why this makes sense for docx4j

Agents are already producing and consuming Word documents, badly.  The popular
"Word MCP servers" are python-docx wrappers that build documents element by
element; Claude's own code-execution sandbox has python-docx preinstalled.  That
is "good enough" for casual generation — and visibly falls down exactly where
docx4j is strong:

1. **Template filling.** An agent writing raw OOXML (or driving python-docx call
   by call) to produce a contract/invoice/report is slow, token-hungry and
   error-prone.  docx4j's OpenDoPE binding turns that into one deterministic
   call — `Docx4J.bind(pkg, xmlData, FLAG_BIND_INSERT_XML | FLAG_BIND_BIND_XML |
   FLAG_BIND_REMOVE_SDT | FLAG_BIND_REMOVE_XML)` — preserving all authored
   formatting, with conditions and repeats.  Nothing in the MCP ecosystem offers
   this.  The 17.0.4 NonXSLT binding default makes it fast, too.
2. **HTML→docx** (docx4j-ImportXHTML): agents author content naturally in
   Markdown/HTML; converting that into a properly styled docx is the natural
   bridge.
3. **docx→PDF / docx→HTML**: the 17.0.4 visitor exporters are at feature parity
   and roughly 10x faster than the XSLT pathway — right-sized for a tool an
   agent may call in a loop.

Secondary tools fall straight out of the module map: text/structure extraction
(agents reading a docx without vision tokens), tracked-changes handling,
anonymization (docx4j-docx-anon), comparison (docx4j-diffx), and merge/append —
the last being Plutext's commercial MergeDocx, which makes the server a natural
freemium funnel (§5).

### What this is NOT

- Not a dependency or feature of docx4j-core; a separate runnable artifact.
- Not a mirror of the docx4j API as tools.  Fine-grained tools ("add paragraph",
  "set bold") would be chattier and worse than what agents already do.  The value
  is a small number of coarse, deterministic, high-level operations.
- Not a replacement for using docx4j as a library; it is a reach/distribution
  surface (and a showcase).

## 2. Product shape

- **Artifact**: `docx4j-mcp`, a runnable shaded jar (`java -jar docx4j-mcp.jar`
  runs the stdio server).  The docx4j-bundle shading experience applies (JPMS
  descriptors excluded from the shade, JAXB runtime included — see the bundle
  notes from 17.0.x).
- **Java 17 baseline** for this artifact only (MCP SDK requirement); the library
  stays at 11.  Build-JDK is already ≥17, so a per-module
  `<maven.compiler.release>17</maven.compiler.release>` suffices if it lives in
  the reactor.
- **Transports**: stdio first (local agent, file paths, no auth story needed).
  Streamable HTTP is a later phase with a real upload/auth design (§6, phase 5).
- **Depends on**: docx4j-core, docx4j-JAXB-ReferenceImpl, docx4j-export-fo (+
  fonts), docx4j-ImportXHTML (optional, reflection or optional dependency —
  mirroring how the binding pathway treats it), docx4j-docx-anon / docx4j-diffx
  in the extended phase.
- **Client config snippets** (Claude Desktop `claude_desktop_config.json`,
  Claude Code `.mcp.json`) ship in the README and on a website page; listing in
  MCP registries once stable.

## 3. Tool surface

Contracts below are the design intent; JSON Schemas to be written in phase 1.
All file parameters are local paths validated against an allow-list of root
directories given at server start (§6).  Every tool that writes takes an
`output_path` and refuses to overwrite unless `overwrite: true`.

### Core (phase 1)

| Tool | Input | Output | Engine |
|---|---|---|---|
| `describe_template` | `template_path` | The data the template wants: OpenDoPE XPaths (with types/repeat/condition structure), or plain content-control tags/titles for non-OpenDoPE sdts; plus page count of styles/parts summary | OpenDoPE parts (`org.opendope.xpaths` etc.), SdtPr traversal |
| `fill_template` | `template_path`, `data` (XML string; JSON accepted and converted, §7), `output_path`, optional `remove_sdts` (default true) | the filled docx | `Docx4J.bind(...)` with the FLAG_BIND_* set; OpenDoPEHandler preprocessing incl. conditions/repeats |
| `convert_to_pdf` | `input_path`, `output_path`, optional `font_mapper` hints | PDF | Docx4J.toPDF via export-fo (visitor exporter; XSLT fallback flag exposed as an option) |
| `extract_text` | `input_path`, optional `structure: true` | plain text, or a structured outline (headings hierarchy, table cells, notes) | TraversalUtil / TextUtils |

`describe_template` is the essential complement to `fill_template`: it is what
lets an agent discover *what data to supply* without a human reading the docx.
The pair is the product.

### Conversion & authoring (phase 2)

| Tool | Input | Output | Engine |
|---|---|---|---|
| `html_to_docx` | `html` (string) or `input_path`, optional `styles_template_path` (docx whose styles apply), `output_path` | docx | docx4j-ImportXHTML; altChunk fallback if ImportXHTML absent |
| `convert_to_html` | `input_path`, `output_path` or inline return | HTML | Docx4J.toHTML (visitor exporter) |
| `markdown_to_docx` | `markdown` (string) or `input_path`, optional `styles_template_path`, `output_path` | docx (real styles/numbering/tables/footnotes; no HTML detour) | docx4j-markdown `MarkdownImporter` (shipped 17.0.4; remote images NOT fetched — same posture as §6) |
| `docx_to_markdown` | `input_path`, optional `image_dir_path`, `tracked_changes` (accept/markup) | markdown (CommonMark+GFM) | docx4j-markdown `MarkdownExporter` (shipped 17.0.4) |

### Extended (phase 4)

| Tool | Input | Output | Engine |
|---|---|---|---|
| `accept_tracked_changes` / `reject_tracked_changes` | `input_path`, `output_path` | docx | wml revision markup is fully modelled; operation code adapted from existing sample code |
| `anonymize` | `input_path`, `output_path` | docx with text scrambled, metadata stripped | docx4j-docx-anon |
| `compare` | `path_a`, `path_b` | a summary of differences (and optionally a marked-up docx) | docx4j-diffx |
| `merge_documents` | `input_paths[]`, `output_path` | docx | **MergeDocx (commercial)**: present in the tool list; without a licence key the tool returns a clear message + link (never a silent degraded merge). With `MERGEDOCX_LICENSE`/jar present, runs it. §5 |

### Resources and prompts (optional, phase 4+)

- Resources: expose an opened package's inventory (part names, styles in use,
  numbering summary) as readable resources for inspection/debugging.  Nice for a
  "docx inspector" story; strictly secondary to tools.
- Prompts: a `fill-template` prompt that walks a client through
  describe→gather-data→fill.  Cheap to add once tools exist.

## 4. Execution model

- **Stateless per call.**  Each tool call loads, operates, saves, closes.  No
  open-package session state: agents retry and parallelize, and docx4j load is
  fast relative to conversion.  (A session/handle model could come later if
  profiling demands it; it complicates crash/ordering semantics for no proven
  win.)
- **Concurrency**: the stdio transport is effectively serial per client; for
  HTTP later, note docx4j's process-wide state (`Docx4jProperties`
  programmatic settings, font caches) — per-request property mutation is
  forbidden in tool implementations; conversion settings go through
  FOSettings/HTMLSettings instances instead.
- **Fonts**: PDF quality depends on host fonts (the usual docx4j font-mapping
  story).  The server logs the substitutions it made and returns them in the
  tool result's metadata so the agent can tell the user ("Calibri rendered as
  Carlito").

## 5. Commercial angle (MergeDocx)

The server ships with the merge tool visible but licence-gated.  This is the
honest version of freemium: the agent (and its user) discovers that merge is a
solved problem one licence away, at exactly the moment they need it.  Requires:
a licence-key mechanism the server can check, wording for the unlicensed
response, and a decision on whether MergeDocx's jar is fetched separately
(likely, since it is closed source).  Plutext decision needed on pricing/keying
for this channel — flagging, not designing, here.

## 6. Security

- **Path allow-list**: server start takes `--root <dir>` (repeatable); every
  path parameter must resolve inside a root (symlinks resolved before the
  check).  No default root — refuse to start without one, so a
  copy-pasted config can't silently expose $HOME.
- **Untrusted input**: every docx is attacker-controlled (agents fetch files
  from anywhere).  Zip-bomb/entity-expansion posture: document what docx4j
  already guards (XML security properties in XmlUtils) and add size/entry
  limits at the server boundary; fuzz the loaders with the usual evil-zip
  corpus before the HTTP phase.
- **No network fetches** by the server itself (no template-by-URL) in v1; the
  agent's own fetch tools can do that, keeping this server's threat model
  file-only.
- **HTTP transport (phase 5) is where auth lives**: bearer token minimum, and
  uploads instead of paths.  Out of scope until the local server has earned it.

## 7. Risks / open questions

- **Repo placement**: separate GitHub repo (`plutext/docx4j-mcp`, the
  ImportXHTML pattern) vs a reactor module.  Separate repo keeps the Java 17
  floor, the MCP SDK's release cadence, and registry versioning out of the
  library's; reactor keeps CI simple.  Leaning separate repo, with this CR as
  the planning home either way.  DECISION NEEDED (jharrop).
- **JSON→XML for `fill_template`**: OpenDoPE binds XPaths against the custom
  XML part, so XML is the native payload.  Agents prefer JSON.  Options: (a)
  accept both, converting JSON with a documented canonical mapping; (b) XML
  only, and let `describe_template` emit a skeleton XML the agent fills in.
  (b) is simpler and self-teaching; (a) is friendlier.  Prototype both in
  phase 1 with real agent transcripts before fixing the contract.
- **MCP SDK maturity**: Tier 2, 2.0.x — expect some API movement; pin the BOM
  and keep the server thin over it.
- **Prompt-injection surface**: tool *results* (extracted text, describe output)
  flow into the agent's context; a hostile docx contains hostile text.  That is
  inherent to any reader tool — document it, and keep results clearly data-
  shaped (no instructions in our own result phrasing).
- **Maintenance load**: a new user-facing product for a solo-maintained
  project.  Phases 1-3 are deliberately small; adoption signals (registry
  stats, issues) should gate phases 4-5.
- **Does anyone come?**  Cheap to find out: the phase 3 deliverable includes a
  website page and registry listing; if fill_template gets no traction in a
  couple of months, stop at phase 3.

## 8. Phases

0. **Spike** (S): stand up the SDK's stdio server with one toy tool from Claude
   Code; verify the 2.0.x server API shapes, shading, and the
   Java-17-module-over-Java-11-libs combination.  Throwaway code; findings
   recorded here.
1. **Core tools** (M): `describe_template`, `fill_template`, `convert_to_pdf`,
   `extract_text`; path allow-list; JSON-vs-XML experiment (§7) resolved.
   Tests: JUnit against the tool handlers directly (no MCP client needed for
   logic), plus one end-to-end stdio smoke test.
2. **Conversion & authoring** (S-M): `html_to_docx`, `convert_to_html`;
   ImportXHTML optional-dependency handling.
3. **Packaging & distribution** (S-M): shaded runnable jar, client config
   snippets, README + website page (fits the "Why docx4j" family), registry
   listing.  This is the ship-it milestone.
4. **Extended tools** (M): tracked changes, anonymize, compare, the MergeDocx
   licence gate; resources/prompts if warranted.
5. **Hosted/HTTP** (M-L, only on demonstrated demand): streamable HTTP
   transport, uploads, auth, hardening (§6).

## 9. Suggested sequencing and effort (rough)

| Phase | Effort | Value |
|-------|--------|-------|
| 0 Spike | S | De-risks everything else |
| 1 Core tools | M | The product: describe+fill is the differentiator |
| 2 Conversion/authoring | S-M | Broadens to the most-asked agent tasks |
| 3 Packaging/distribution | S-M | Without this, nothing above exists publicly |
| 4 Extended tools | M | Depth + the MergeDocx funnel |
| 5 Hosted/HTTP | M-L | Only if adoption justifies the security surface |

If only phases 0-3 are ever done, that is a complete, shippable product:
template filling, PDF conversion and text extraction for any MCP-capable agent,
in a jar.
