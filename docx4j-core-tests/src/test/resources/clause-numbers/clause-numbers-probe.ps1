# CR-074 phase 0's Word probe: what Word shows for a numbered paragraph as a cross-reference, and what its own Insert
# Cross-reference writes. Part 1 opens cn-1-labels.docx (built by build_clause_numbers.py here): every numbered paragraph
# is listed with the label Word draws (ListFormat.ListString) and Word's numbered items (GetCrossReferenceItems, what the
# Insert Cross-reference dialog lists); then Word updates the 655 REF fields over the paragraphs' bookmarks (\w, \n, \r,
# \t, \p, \d, \h), every field's code and result is listed, and the document is saved as cn-1-labels-word365.docx; last,
# case T's fields are updated again with no markup shown and with the original shown, and listed. Part 2 opens
# cn-2-insert.docx (the same lists, no bookmarks or fields) and has Word insert its own cross-references to numbered items
# (full, no and relative context; as a hyperlink or not; with above/below; with a separator; the paragraph text), lists
# every field and every bookmark, hidden ones included, and saves cn-2-insert-word365.docx.
# Driving Word as cr068-probe.ps1 does: inputs copied to a local folder first, SaveAs with [ref] arguments, each COM step
# shown before it is made, an output already there skipped, so a rerun after a hang resumes. Copyright 2026, Plutext Pty
# Ltd; Apache-2.0.
#
#   -Visible   show Word, so a dialog can be seen
param([string]$Dir = "X:\clause-numbers", [switch]$Visible)
$ErrorActionPreference = "Stop"
$tmp = Join-Path $env:TEMP "clause-numbers-probe"
New-Item -ItemType Directory -Force -Path $tmp | Out-Null
$logFile = Join-Path $Dir "clause-numbers-log.txt"
function Say($s) { $line = "{0:HH:mm:ss} {1}" -f (Get-Date), $s; Write-Host $line; Add-Content -Path $logFile -Value $line -Encoding UTF8 }
function Step($s) { Write-Host ("{0:HH:mm:ss}   - {1}" -f (Get-Date), $s) }

function SaveAs($doc, $name, [int]$format) {
  $p = Join-Path $tmp $name
  if (Test-Path $p) { Remove-Item -Force $p }
  Step "SaveAs $name"
  [string]$sp = $p.ToString(); [int]$fmt = $format
  $doc.SaveAs([ref]$sp, [ref]$fmt)
  if (Test-Path $p) { Copy-Item -Force $p $Dir } else { Say "$name : Word wrote no file" }
}

function Close($doc) { [int]$dontSave = 0; $doc.Close([ref]$dontSave) }

function Open($name) {
  Copy-Item -Force (Join-Path $Dir $name) (Join-Path $tmp $name)
  Step "Documents.Open $name"
  return $word.Documents.Open((Join-Path $tmp $name), $false, $false, $false)
}

function Esc($s) { return ($s -replace "`r", "\r" -replace "`n", "\n" -replace "`t", "\t" -replace [char]11, "\v" -replace [char]7, "\a" -replace [char]19, "{" -replace [char]20, "|" -replace [char]21, "}") }

function Case($tag, [scriptblock]$body) {
  Step $tag
  try { & $body } catch { Say "$tag FAILED: $($_.Exception.Message)" }
}

# The id a paragraph's text starts with (L6, F-L6, X1)
function IdOf($range) { return ((Esc $range.Paragraphs.Item(1).Range.Text).Trim() -split '\s')[0] }

# The first main-story paragraph (cells included) whose text starts with the prefix
function Para($doc, $prefix) {
  foreach ($p in $doc.Paragraphs) { if ($p.Range.Text.StartsWith($prefix)) { return $p } }
  throw "no paragraph starting '$prefix'"
}

function Labels($doc, $what) {
  $n = 0
  foreach ($p in $doc.ListParagraphs) { $n++ }
  Say "$what : $n list paragraphs"
  # ListParagraphs runs from the end of the document; list them in document order
  foreach ($p in $doc.Paragraphs) {
    $lf = $p.Range.ListFormat
    if ($lf.ListType -eq 0) { continue }
    Say ("  label {0} '{1}' level {2} type {3}" -f (IdOf $p.Range), (Esc $lf.ListString), $lf.ListLevelNumber, $lf.ListType)
  }
}

function Items($doc, $what) {
  $items = $null
  try { $items = $doc.GetCrossReferenceItems(0) } catch { [object]$t = 0; $items = $doc.GetCrossReferenceItems([ref]$t) }
  $i = 0; $list = @()
  foreach ($it in $items) { $i++; $list += [string]$it; Say ("  item {0} '{1}'" -f $i, (Esc ([string]$it))) }
  Say "$what : $i numbered items"
  return ,$list
}

function Fields($doc, $what, $only) {
  $i = 0
  foreach ($f in $doc.Fields) {
    $i++
    $id = IdOf $f.Code
    if ($only -and -not ($id -match $only)) { continue }
    Say ("  field {0} in {1}: type {2} code '{3}' result '{4}'" -f $i, $id, $f.Type, (Esc $f.Code.Text).Trim(), (Esc $f.Result.Text))
  }
  Say "$what : $i fields"
}

# The paragraphs holding fields, as their text reads with the results (one line each, for reading by eye)
function Lines($doc, $what, $only) {
  foreach ($p in $doc.Paragraphs) {
    if ($p.Range.Fields.Count -eq 0) { continue }
    $id = IdOf $p.Range
    if ($only -and -not ($id -match $only)) { continue }
    Say ("  line {0}" -f (Esc $p.Range.Text))
  }
  Say "$what : lines done"
}

function Bookmarks($doc) {
  $doc.Bookmarks.ShowHidden = $true
  foreach ($b in $doc.Bookmarks) {
    Say ("  bookmark {0} {1}-{2} in {3}: '{4}'" -f $b.Name, $b.Range.Start, $b.Range.End, (IdOf $b.Range), (Esc $b.Range.Text))
  }
}

# A collapsed range at the end of a paragraph's text, before its mark
function EndOf($p) { $r = $p.Range.Duplicate; $e = $r.End - 1; [void]$r.SetRange($e, $e); return $r }

# Word's Insert Cross-reference, Reference type "Numbered item" (0), at the end of the paragraph starting $at
function XRef($doc, $items, $at, $target, [int]$kind, [bool]$hyper, [bool]$position, [bool]$separate, [string]$separator) {
  $idx = 0
  for ($i = 0; $i -lt $items.Count; $i++) { if ($items[$i] -match "(^|\s)$target(\s|$)") { $idx = $i + 1; break } }
  if ($idx -eq 0) { throw "no numbered item $target" }
  $key = switch ($kind) { -4 { "w" } -3 { "n" } -2 { "r" } -1 { "text" } }
  $r = EndOf (Para $doc "$at "); $r.InsertAfter(" $target$key=");
  $r = EndOf (Para $doc "$at ")
  Step "InsertCrossReference $at $target kind $kind item $idx hyperlink $hyper position $position separate $separate"
  try {
    $r.InsertCrossReference(0, $kind, [string]$idx, $hyper, $position, $separate, $separator)
  } catch {
    [object]$t = 0; [object]$it = [string]$idx; [object]$h = $hyper; [object]$po = $position; [object]$se = $separate; [object]$ss = $separator
    $r.InsertCrossReference([ref]$t, $kind, [ref]$it, [ref]$h, [ref]$po, [ref]$se, [ref]$ss)
  }
  Say ("  inserted at {0}: {1} kind {2} (item {3} '{4}') hyperlink {5} position {6} separate {7} '{8}'" -f $at, $target, $kind, $idx,
    (Esc $items[$idx - 1]), $hyper, $position, $separate, $separator)
}

function View($doc, [int]$markup, [int]$view) {
  $v = $doc.Windows.Item(1).View
  $v.RevisionsFilter.Markup = $markup
  $v.RevisionsFilter.View = $view
  Say ("view: markup {0} view {1}" -f $v.RevisionsFilter.Markup, $v.RevisionsFilter.View)
}

$word = $null
try {
  $word = New-Object -ComObject Word.Application
  $word.Visible = [bool]$Visible; $word.DisplayAlerts = 0; $word.AutomationSecurity = 3
  Say ("Word {0} build {1}, user '{2}'" -f $word.Version, $word.Build, $word.UserName)

  # 1. Word's labels, numbered items and REF results over the bookmarks
  if (Test-Path (Join-Path $Dir "cn-1-labels-word365.docx")) { Say "cn-1-labels-word365.docx there already: skipped" } else {
    $doc = Open "cn-1-labels.docx"
    try {
      Case "labels" { Labels $doc "cn-1" }
      Case "items" { [void](Items $doc "cn-1") }
      Case "update" { Step "Fields.Update"; $e = $doc.Fields.Update(); Say "Fields.Update: $e (0 = no field in error)" }
      Case "fields" { Fields $doc "cn-1 updated" $null }
      Case "lines" { Lines $doc "cn-1 updated" $null }
      SaveAs $doc "cn-1-labels-word365.docx" 16
      Case "no markup" {
        View $doc 0 0; [void]$doc.Fields.Update()
        Labels $doc "cn-1 no markup"; Fields $doc "cn-1 no markup" '^(F-)?T\d'; Lines $doc "cn-1 no markup" '^(F-)?T\d'
      }
      Case "original" {
        View $doc 0 1; [void]$doc.Fields.Update()
        Labels $doc "cn-1 original"; Fields $doc "cn-1 original" '^(F-)?T\d'; Lines $doc "cn-1 original" '^(F-)?T\d'
      }
    } finally { Close $doc }
  }

  # 2. Word's own cross-references to numbered items
  if (Test-Path (Join-Path $Dir "cn-2-insert-word365.docx")) { Say "cn-2-insert-word365.docx there already: skipped" } else {
    $doc = Open "cn-2-insert.docx"
    try {
      Case "labels" { Labels $doc "cn-2" }
      $items = Items $doc "cn-2"
      foreach ($t in @("L6", "L9", "L11", "A3", "A6", "A7", "R4", "M3", "N2", "G3", "S2", "Y2")) {
        foreach ($k in @(-4, -3, -2)) { Case "X1 $t $k" { XRef $doc $items "X1" $t $k $true $false $false " " } }
      }
      Case "X2" { XRef $doc $items "X2" "L6" -4 $false $false $false " " }
      Case "X3" { XRef $doc $items "X3" "L6" -2 $true $true $false " " }
      Case "X4" { XRef $doc $items "X4" "L6" -4 $true $false $true "-" }
      Case "X5" { XRef $doc $items "X5" "L6" -1 $true $false $false " " }
      Case "X6" { XRef $doc $items "X6" "L6" -4 $true $false $false " " }
      foreach ($t in @("L6", "L7", "L11", "L5")) { Case "L9 $t" { XRef $doc $items "L9" $t -2 $true $false $false " " } }
      foreach ($t in @("L6", "L9", "L12")) { Case "L11 $t" { XRef $doc $items "L11" $t -2 $true $false $false " " } }
      Case "fields" { Fields $doc "cn-2 inserted" $null }
      Case "lines" { Lines $doc "cn-2 inserted" $null }
      Case "bookmarks" { Bookmarks $doc }
      SaveAs $doc "cn-2-insert-word365.docx" 16
    } finally { Close $doc }
  }
} finally {
  if ($word) { try { $word.Quit() } catch { }; [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($word) }
}
Say "done"
