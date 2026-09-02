# Why Source Access Matters for LLM-Assisted Programming

*Canonical version:
[Why Source Access Matters for LLM-Assisted Programming](https://www.docx4java.org/source-access-llm-assisted-programming.html)
on the docx4j website. This copy is a mirror for GitHub readers.*

An AI coding assistant is only as good as what it can read. That single fact
changes how dependencies should be chosen — and improves the economics of both
using and contributing to open source.

## An assistant reasons from what it can read

When you ask an AI assistant to work with a library, everything it produces is
grounded in the material available to it: what was in its training data, and
what it can retrieve or open while it works. For an open-source dependency
that material is the whole story — the implementation, the tests that pin its
behaviour, the git history with each change's reasoning in its commit message,
the issue discussions where edge cases were argued out. Modern coding agents
don't just recall this from training; they *read it live*, grepping the actual
source of the library on your classpath.

For a closed-source dependency, the same assistant has the API documentation
and whatever forum posts leaked into its training data. Everything below the
API surface is guesswork — and guesswork is precisely where assistants
hallucinate.

## Answers come from the source, not from a support queue

Debugging is the dramatic case, but it isn't the common one. Most of a
developer's questions about a library are ordinary: which entry point to use,
what a parameter accepts, why a getter returns null after load, whether an
operation is thread-safe, what the intended sequence of calls is.

Against a closed library, those questions are answered by the published API
documentation — and when that runs out, by searching forum posts, filing an
issue or a support request, and waiting. The real cost isn't the answer; it's
the latency, multiplied by the dozens of such questions in any serious
integration.

Against an open library, the assistant answers immediately, from ground truth:
the implementation itself, and — often better — the test suite and runnable
samples, which are executable, maintained examples of intended use. A mature
test suite is hundreds of worked examples of how to construct things, each
pinned to expected behaviour assertion by assertion — for an assistant, often
richer raw material than the documentation. The git history answers the
questions documentation never does: *why* it works this way, what it
replaced, which behaviours are deliberate.

Concretely, from recent AI-assisted sessions against docx4j itself: an
undocumented constructor flag that lets two exporters share one set of
extracted images; a code comment explaining why the HTML serializer must
*not* pretty-print (a line break before a subscript renders as a visible
space) — which stopped a plausible "improvement" that would have corrupted
output; the exact list of directories the font subsystem scans, which turned
a mysterious CI-only test failure into a one-line diagnosis. None of these
appear in any documentation. Each was a five-second search of the source.

This improves the economics of **using** open source before any code changes
hands: support-shaped questions become self-serve, and their latency leaves
your critical path. It helps the project too — routine questions that would
once have landed in the forum are absorbed by the assistant, which is part of
how a small maintainer team scales.

## The debugging loop, closed and open

The difference is starkest when behaviour surprises you.

**Against a closed library:**

1. The assistant reasons from documentation and forum posts — the only
   material available to it.
2. The investigation stops at the library boundary. The assistant guesses;
   you file a vendor ticket.
3. The fix arrives on the vendor's schedule, in a future release, if your use
   case makes their cut.
4. Every workaround you write in the meantime is yours to maintain, forever.

**Against an open library:**

1. The assistant reads the implementation, the tests, the git history and the
   issue discussions.
2. A surprise gets traced to the responsible line of library code — often
   with the commit that introduced it, and the reasoning in its message.
3. The assistant patches the library locally *and drafts the upstream pull
   request*, with the test that proves the fix.
4. Once merged, maintenance of that fix belongs to the project. Your fork
   delta returns to zero.

A checkout you can *build* upgrades the loop again, from reading to
experiment: the assistant doesn't have to be right, it can check. Recent
docx4j examples: a suspected regression was ruled out by running the previous
release's jars and the current build against the same inputs and comparing
outputs cell by cell; a build-tool rewrite was validated by diffing its
output byte-for-byte against the tool it replaced. Source you can execute
beats source you can only read — and only open source offers either.

## The economics of contributing have inverted, too

Contributing a fix or feature upstream used to be a cost you weighed against
quietly maintaining a private patch: writing the minimal reproduction, the
failing test, the fix itself, regression coverage, the pull-request write-up.
Reasonable teams often chose the private patch, and paid for that choice at
every subsequent upgrade.

With an assistant doing the mechanical work, the contribution is cheap — and
what it buys is not goodwill but **freedom from maintaining that code
yourself**. Once your fix is merged, it ships in every future release; your
delta against upstream is zero; the project's test suite guards your fix from
regression on your behalf.

> Upstreaming used to be generosity. With AI assistance, it is the cheapest
> maintenance strategy available.

This only works, of course, against projects that accept pull requests and
review them in reasonable time. A responsive upstream is now a measurable
engineering asset, not a nice-to-have.

## Depth matters: the whole stack has to be open

Root causes do not respect package boundaries. An investigation that begins in
your application code may end three dependencies down — and it can only get
there if every layer is readable.

A worked example from this project:
[docx4j](https://github.com/plutext/docx4j) (Apache License v2) converts Word
documents to PDF via Apache FOP, and its object model is generated from the
OpenXML schemas themselves. In recent releases, reported issues have gone from
analysis to merged, tested fixes in days, with AI assistance — because the
assistant could read the whole path from the reported symptom, through
docx4j's binding and conversion pipeline, into FOP's rendering internals. Bugs
that turned out to live in docx4j were fixed in docx4j; fixes that belonged in
Apache FOP were sent to Apache FOP as pull requests. At every step, the git
history said *why* the code was the way it was, which is the difference
between a correct fix and a plausible one.

A closed-source vendor cannot offer this loop at any price: you cannot read
what they will not show, and you cannot fix what you cannot read.

## What to check before adopting a dependency

The source-access checklist:

- Is the **full source** available — not just "source available on request",
  but on tap, where your tools can read it?
- Are the **git history and issue tracker** public and substantive?
- Are its **dependencies** open too, all the way down the paths you'll
  exercise?
- Does the project **accept pull requests**, and review them in reasonable
  time?
- Does the **licence** permit patching and redistribution while you wait for
  a merge?

Each "yes" makes your assistant materially more capable against that
dependency. A "no" anywhere is a place where, one day, an investigation will
stop and a workaround will become permanent.

One practical note once you've adopted: make sure the assistant is reading
code that actually exists. Where parts of a library's API are generated at
build time — docx4j's OpenXML object model, for instance — a bare clone
doesn't yet contain the classes you'll use most; build it once so it does, or
point your tools at the `-sources` jars your package manager already
downloads.

## Frequently asked questions

**Does source access help even when nothing is broken?**

Yes — most of what a team asks about a library is "how do I…?", not "why is
this broken?". With source access, the assistant answers those from the
implementation, the tests and the samples, at assistant speed; no forum post,
issue, or support request, and no waiting on someone else's queue.

**Doesn't vendor support substitute for source access?**

Support gets you an answer on the vendor's schedule, scoped to what they will
disclose. Source access gets your own tooling an answer now. The two aren't
exclusive — but only one of them compounds with better assistants every year.

**Is this argument specific to AI-assisted development?**

The debugging asymmetry always existed; assistants changed its magnitude.
Reading an unfamiliar codebase used to take an engineer days, so in practice
few looked. An assistant reads it in minutes, so source access converts from a
theoretical freedom into throughput you use weekly.

**Does open source mean the assistant already knows the library?**

Largely, yes: open codebases, their documentation and their public discussions
are in the training data of every major model, so assistants arrive knowing
the idioms. But training-data knowledge blurs every era of a library
together: an assistant remembering docx4j may mix the `javax.xml.bind` years
with `jakarta`, long-renamed methods with current ones, or defaults that
flipped a release ago. The stronger effect is at development time — the
assistant reading the exact version on your classpath dispels exactly those
confusions, which are otherwise its most confident mistakes.

## Where this argument came from

This page generalises a point first made in
[docx4j vs POI vs Aspose.Words](https://www.docx4java.org/docx4j-vs-poi-vs-aspose.html),
a comparison of the main ways to work with Word documents from Java. If you're
choosing a document library, start there.

---

*Published by Plutext, developers of the Apache-licensed
[docx4j](https://github.com/plutext/docx4j).*
