# Unknown elements (CR-076)

core-ts's Word check 76, rounds 1 to 11: what Word 365 does with an element it doesn't know.

- `<name>.docx`: the sources, from `test/fixtures/check76/build.mjs` in docx4j-core-ts. They were built for the purpose and
  hold no one's content.
- `<name>-word365.docx`: Word's save of each. Rounds 1 to 5 by hand on 5 and 6 November 2026, rounds 6 to 11 through the
  bridge on 6 November, Word 16.0.20326.20232. A hand save can carry the Genko add-in's traces (a `footnotes.xml`,
  `w:hdrShapeDefaults`), which no comparison here reads.
- No save beside a source: Word refused it. `76e`, `76o` to `76s`, `76v`, `76x`, `76y`, `76t28`, `76t46` to `76t49`, `76t53` to `76t55`, `76t57`, `76t59`.

`UnknownElementsTest` compares each with Word's save. To rebuild, run `build.mjs` and copy from core-ts's `test/check76/`.
