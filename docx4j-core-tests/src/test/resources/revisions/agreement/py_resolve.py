# Copyright 2026, Plutext Pty Ltd. This file is part of docx4j (Apache License 2.0).
"""CR-043 phase 3, the ports' agreement run: docx4j-python's accept_all/reject_all (the body: its scope)."""
import os, sys, traceback
sys.path.insert(0, sys.argv[1])
from docx4j_py import WordprocessingMLPackage
out = sys.argv[2]
for path in sys.argv[3:]:
    for what in ("accept", "reject"):
        name = os.path.basename(path)[:-5] + "." + what + ".docx"
        try:
            pkg = WordprocessingMLPackage.load(path)
            body = pkg.body
            n = body.accept_all() if what == "accept" else body.reject_all()
            with open(os.path.join(out, name), "wb") as f:
                f.write(pkg.save())
            print("OK", name, n)
        except Exception as e:
            print("FAIL", name, type(e).__name__, str(e)[:160])
