// Copyright 2026, Plutext Pty Ltd. This file is part of docx4j (Apache License 2.0).
// CR-043 phase 3, the ports' agreement run: docx4j-core-ts's acceptAll/rejectAll over a fixture,
// per Body (main document, headers, footers: its scope), saved for docx4j to compare.
import { readFile, writeFile } from 'node:fs/promises';
import { basename, join } from 'node:path';
const core = await import(process.argv[2] + '/dist/index.mjs');
const { WordprocessingMLPackage } = core;
const out = process.argv[3];
for (const file of process.argv.slice(4)) {
  for (const what of ['accept', 'reject']) {
    const name = basename(file, '.docx') + '.' + what + '.docx';
    try {
      const pkg = await WordprocessingMLPackage.load(new Uint8Array(await readFile(file)));
      const main = pkg.getMainDocumentPart();
      const bodies = [await pkg.getBody()];
      for (const part of [...(main.headerParts ?? []), ...(main.footerParts ?? [])]) bodies.push(await part.getBody());
      let n = 0;
      for (const body of bodies) n += what === 'accept' ? body.acceptAll() : body.rejectAll();
      await writeFile(join(out, name), await pkg.save());
      console.log('OK', name, n);
    } catch (e) {
      console.log('FAIL', name, String(e && e.message || e).slice(0, 200));
    }
  }
}
