# Copyright 2026, Plutext Pty Ltd. This file is part of docx4j (Apache License 2.0).
"""CR-043 phase 3, the ports' agreement run: docx4j-python's accept_all/reject_all of every story
(docx4j_py.model.revisions, its CR-010; until 2026-10-18 this ran Body.accept_all, the body alone)."""
import os, sys, traceback
sys.path.insert(0, sys.argv[1])
from docx4j_py import WordprocessingMLPackage
from docx4j_py.model import revisions
out = sys.argv[2]
for path in sys.argv[3:]:
    for what in ("accept", "reject"):
        name = os.path.basename(path)[:-5] + "." + what + ".docx"
        try:
            pkg = WordprocessingMLPackage.load(path)
            n = (revisions.accept_all if what == "accept" else revisions.reject_all)(pkg)
            with open(os.path.join(out, name), "wb") as f:
                f.write(pkg.save())
            print("OK", name, n)
        except Exception as e:
            print("FAIL", name, type(e).__name__, str(e)[:160])
