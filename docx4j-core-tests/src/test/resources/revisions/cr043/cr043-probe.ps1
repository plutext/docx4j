# CR-043 phase 0's Word probe: Word makes each case's tracked edit in cr043-base.docx through its object model
# and saves the tracked document, then accepts all and rejects all of it, each saved; the same accept and
# reject for tracked-changes-equations.docx. The saves are the goldens docx4j's TrackedChanges is held to.
# The base and its cases are docx4j-core-tests' Cr043ProbeBuilder (each case's text starts with its tag, C01,
# C02, ...). Driving Word as office-chart-edit-check.ps1 does (portfolio docs/office_output_methods.md 3.2):
# inputs copied to a local folder first, SaveAs with [ref] arguments, each COM step logged before it is made,
# an output already there skipped, so a rerun after a hang resumes. Copyright 2026, Plutext Pty Ltd;
# Apache-2.0.
#
#   -Visible   show Word, so a dialog can be seen
param([string]$Dir = "X:\revisions\cr043", [switch]$Visible)
$ErrorActionPreference = "Stop"
$tmp = Join-Path $env:TEMP "cr043-probe"
New-Item -ItemType Directory -Force -Path $tmp | Out-Null
$logFile = Join-Path $Dir "cr043-log.txt"
function Say($s) { $line = "{0:HH:mm:ss} {1}" -f (Get-Date), $s; Write-Host $line; Add-Content -Path $logFile -Value $line -Encoding UTF8 }
function Step($s) { Write-Host ("{0:HH:mm:ss}   - {1}" -f (Get-Date), $s) }
$Missing = [System.Reflection.Missing]::Value

function Save($doc, $name) {
  $p = Join-Path $tmp $name
  Step "SaveAs $name"
  [string]$sp = $p.ToString(); [int]$fmt = 16
  $doc.SaveAs([ref]$sp, [ref]$fmt)
  return $p
}

function Close($doc) { [int]$dontSave = 0; $doc.Close([ref]$dontSave) }

# The first paragraph whose text starts with the tag and a space (body paragraphs and table cells alike)
function Para($doc, $tag) {
  foreach ($p in $doc.Paragraphs) { if ($p.Range.Text.StartsWith($tag + " ")) { return $p } }
  throw "no paragraph $tag"
}

# The first paragraph whose text starts with the prefix
function ParaStarting($doc, $prefix) {
  foreach ($p in $doc.Paragraphs) { if ($p.Range.Text.StartsWith($prefix)) { return $p } }
  throw "no paragraph starting $prefix"
}

function Tbl($doc, $tag) {
  foreach ($t in $doc.Tables) { if ($t.Range.Text.StartsWith($tag + " ")) { return $t } }
  throw "no table $tag"
}

# An empty range just before a range's last character (a paragraph's mark, a story's final mark)
function AtEnd($range) { $r = $range.Duplicate; [void]$r.SetRange($range.End - 1, $range.End - 1); return $r }

# The paragraph mark of a paragraph, as a range
function Mark($p) { $r = $p.Range.Duplicate; [void]$r.SetRange($p.Range.End - 1, $p.Range.End); return $r }

# Delete the first occurrence of a word inside a range
function DeleteWord($range, $word) {
  $r = $range.Duplicate
  if (-not $r.Find.Execute($word)) { throw "no '$word'" }
  $r.Delete() | Out-Null
}

function Case($tag, [scriptblock]$body) {
  Step $tag
  try { & $body; Say "$tag ok" } catch { Say "$tag FAILED: $($_.Exception.Message)" }
}

# Tracked changes left in each story, by story type (1 main, 2 footnotes, 3 endnotes, 4 comments, 5 text frames,
# 6-11 headers and footers)
function Remaining($doc) {
  $parts = @()
  foreach ($s in $doc.StoryRanges) {
    $st = $s
    while ($st) { $parts += ("{0}:{1}" -f $st.StoryType, $st.Revisions.Count); $st = $st.NextStoryRange }
  }
  return ($parts -join " ")
}

$word = $null
try {
  $word = New-Object -ComObject Word.Application
  $word.Visible = [bool]$Visible; $word.DisplayAlerts = 0; $word.AutomationSecurity = 3
  Say ("Word {0} build {1}" -f $word.Version, $word.Build)

  foreach ($f in @("cr043-base.docx", "cr043-picture.png", "tracked-changes-equations.docx")) {
    Copy-Item -Force (Join-Path $Dir $f) (Join-Path $tmp $f)
  }
  $picture = Join-Path $tmp "cr043-picture.png"

  # 1. the tracked edits
  $tracked = Join-Path $Dir "cr043-tracked.docx"
  if (Test-Path $tracked) { Say "cr043-tracked.docx there already: skipped" } else {
    Step "Documents.Open cr043-base.docx"
    $doc = $word.Documents.Open((Join-Path $tmp "cr043-base.docx"), $false, $false, $false)
    try {
      # untracked set-up first
      $doc.TrackRevisions = $false
      Case "C22 set-up" {
        $s = $doc.Shapes.AddTextbox(1, 320, 120, 200, 60, (Para $doc "C22").Range)
        $s.TextFrame.TextRange.Text = "C22 text box text, a word to delete: DELETEME. Inserted text goes at the end."
      }
      $doc.TrackRevisions = $true; $doc.TrackFormatting = $true; $doc.TrackMoves = $true

      # sections first: C07 changes section B's margin, C06 deletes the break that ends section A
      Case "C07" { $doc.Sections.Item(2).PageSetup.LeftMargin = 36 }
      Case "C06" { (Mark (Para $doc "C06")).Delete() | Out-Null }
      Case "C01" { [void]$doc.Fields.Add((AtEnd (Para $doc "C01").Range), 33) }
      Case "C02" { (Para $doc "C02").Range.Fields.Item(1).Delete() }
      Case "C04" { [void]$doc.InlineShapes.AddPicture($picture, $false, $true, (AtEnd (Para $doc "C04").Range)) }
      Case "C05" { (Para $doc "C05").Range.InlineShapes.Item(1).Delete() }
      Case "C08" { (Mark (Para $doc "C08")).Delete() | Out-Null }
      Case "C09" { (Mark (Para $doc "C09")).Delete() | Out-Null }
      Case "C09 cell end" {
        $last = $null
        foreach ($p in $doc.Paragraphs) { if ($p.Range.Text.StartsWith("C09 last")) { $last = $p } }
        (Mark $last).Delete() | Out-Null
      }
      Case "C11" { DeleteWord (Para $doc "C11").Range "BOOKMARKED" }
      Case "C11b" {
        $r = AtEnd (Para $doc "C11b").Range
        $r.InsertAfter("C11b INSERTED WORDS")
        [void]$doc.Bookmarks.Add("c11b_inserted", $r)
      }
      # C21 before C12 and C12b, which change the endnotes' order
      Case "C21" {
        $note = $null
        foreach ($n in $doc.Endnotes) { if ($n.Range.Text.Contains("C21")) { $note = $n } }
        DeleteWord $note.Range "DELETEME"
        (AtEnd $note.Range).InsertAfter(" C21 inserted.")
      }
      Case "C12" { $doc.Endnotes.Item(1).Reference.Delete() | Out-Null }
      Case "C12b" { [void]$doc.Endnotes.Add((AtEnd (Para $doc "C12b").Range), $Missing, "C12b inserted endnote text.") }
      Case "C13" { (Tbl $doc "C13").Delete() }
      Case "C14" { [void](Tbl $doc "C14").Columns.Add() }
      Case "C15" {
        $from = $doc.Range((Para $doc "C15").Range.Start, (Para $doc "C15 move three:").Range.End)
        $from.Select(); $word.Selection.Cut()
        $cell = (Tbl $doc "C15").Cell(1, 1).Range
        $to = $cell.Duplicate; [void]$to.SetRange($cell.Start, $cell.Start)
        $to.Select(); $word.Selection.Paste()
      }
      Case "C16" { [void]$doc.Hyperlinks.Add((AtEnd (Para $doc "C16").Range), "https://www.docx4java.org/", $Missing, $Missing, "C16 inserted link") }
      Case "C16b" {
        $cc = $doc.ContentControls.Add(0, (AtEnd (Para $doc "C16b").Range))
        $cc.Range.Text = "C16b control text"
      }
      Case "C17" { (Para $doc "C17").Range.Font.Bold = 1 }
      Case "C18" {
        $c = $doc.Comments.Item(1).Range
        DeleteWord $c "DELETEME"
        (AtEnd $c).InsertAfter(" C18 inserted.")
      }
      Case "C19" {
        $h = $doc.Sections.Item(1).Headers.Item(1).Range
        DeleteWord $h "DELETEME"
        (AtEnd $doc.Sections.Item(1).Headers.Item(1).Range).InsertAfter(" C19 inserted.")
      }
      Case "C20" {
        $h = $doc.Sections.Item(1).Footers.Item(1).Range
        DeleteWord $h "DELETEME"
        (AtEnd $doc.Sections.Item(1).Footers.Item(1).Range).InsertAfter(" C20 inserted.")
      }
      Case "C22" {
        $t = $doc.Shapes.Item(1).TextFrame.TextRange
        DeleteWord $t "DELETEME"
        (AtEnd $doc.Shapes.Item(1).TextFrame.TextRange).InsertAfter(" C22 inserted.")
      }
      Case "C23" { (Tbl $doc "C23").Rows.Item(2).LeftIndent = 36 }
      Case "C25" {
        $t = $doc.Tables.Add((ParaStarting $doc "C25 placeholder").Range, 2, 2)
        for ($r = 1; $r -le 2; $r++) { for ($c = 1; $c -le 2; $c++) { $t.Cell($r, $c).Range.Text = "C25 r${r}c${c}" } }
      }
      Say ("tracked: {0} revisions in the main story; by story {1}" -f $doc.Revisions.Count, (Remaining $doc))
      $p = Save $doc "cr043-tracked.docx"
      Copy-Item -Force $p $Dir
    } finally { Close $doc }
  }

  # 2. Word's Accept All and Reject All, of the tracked document and of the equations
  foreach ($job in @(
      @("cr043-tracked.docx", "cr043-accept-all.docx", "accept"),
      @("cr043-tracked.docx", "cr043-reject-all.docx", "reject"),
      @("tracked-changes-equations.docx", "equations-accept-all.docx", "accept"),
      @("tracked-changes-equations.docx", "equations-reject-all.docx", "reject"))) {
    $in = $job[0]; $out = $job[1]; $how = $job[2]
    if (Test-Path (Join-Path $Dir $out)) { Say "$out there already: skipped"; continue }
    Copy-Item -Force (Join-Path $Dir $in) (Join-Path $tmp $in)
    Step "Documents.Open $in"
    $doc = $word.Documents.Open((Join-Path $tmp $in), $false, $false, $false)
    try {
      Say ("{0}: before {1}" -f $out, (Remaining $doc))
      Step "$how all"
      if ($how -eq "accept") { $doc.AcceptAllRevisions() } else { $doc.RejectAllRevisions() }
      Say ("{0}: after {1}" -f $out, (Remaining $doc))
      $p = Save $doc $out
      Copy-Item -Force $p $Dir
    } catch { Say "$out FAILED: $($_.Exception.Message)" } finally { Close $doc }
  }
} finally {
  if ($word) { try { $word.Quit() } catch { }; [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($word) }
}
Say "done"
