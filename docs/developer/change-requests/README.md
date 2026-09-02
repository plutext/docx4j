# Change requests

Documented change requests (CRs) live here: a CR describes a non-trivial piece of
proposed work — background, gap/impact analysis, proposed approach, phased plan,
risks — and is updated as decisions are made and phases land.

Convention:

- One markdown file per CR, named `CR-<nnn>-<short-slug>.md`, where `<nnn>` is
  the next number in sequence (numbered in implementation order, from 001).
- Record decisions inline (who decided what, and the date) rather than rewriting
  history; when a phase ships, note the commit hash against it.
- A CR is done when all its phases are shipped or explicitly abandoned; keep the
  file (with its final status) for the record.
