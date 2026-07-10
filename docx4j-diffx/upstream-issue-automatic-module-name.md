# Draft issue for https://github.com/pageseeder/diffx (and pageseeder/xmlwriter)

Delete this file once filed.

**Title:** Please add Automatic-Module-Name to the jar manifest

**Body:**

Thanks for maintaining Diff-X!

docx4j 17.x (https://github.com/plutext/docx4j) has replaced its bundled fork
of the old com.topologi.diffx codebase with a dependency on pso-diffx 1.3.4.

docx4j is JPMS-modularised (its jars contain module-info.class). Because
pso-diffx 1.3.4 has neither a module-info nor an `Automatic-Module-Name`
manifest entry, it is treated as an automatic module whose name (`pso.diffx`)
is derived from the jar filename. That derived name is not guaranteed stable,
and jlink cannot process filename-derived automatic modules.

Could you add to the jar manifest:

    Automatic-Module-Name: org.pageseeder.diffx

(and equivalently `org.pageseeder.xmlwriter` for pso-xmlwriter)?

It is a one-line change in build.gradle.kts and is fully
backward-compatible for classpath users. A module-info.java would be even
better, but the manifest entry alone solves the stability problem.
