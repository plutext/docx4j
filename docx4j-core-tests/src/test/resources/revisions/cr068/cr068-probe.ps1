# CR-068 phase 0's Word probe: what Word makes of a manual redline converted to tracked changes. For each of the
# docx4j-built inputs (the base and the three conversions CR-068 predicts, written by ManualRedlineProbeBuilder in
# docx4j-core-tests), Word opens it (a failed open is a refusal or a repair), its revisions are listed story by story
# with type, author, date and text (what the Review pane shows), it is saved as plain text as it is, and Word's Accept
# All and Reject All of it are each saved as docx and as plain text. First Word makes its own tracked deletion of the
# struck text in the base (the same texts the default conversion deletes, and M11's paragraph whole), for the shape
# Word gives a deletion. Last, the dates probe's revisions are listed, for which date Word shows.
# Driving Word as cr043-probe.ps1 does (portfolio docs/office_output_methods.md 3.2): inputs copied to a local folder
# first, SaveAs with [ref] arguments, each COM step shown before it is made, an output already there skipped, so a
# rerun after a hang resumes. Copyright 2026, Plutext Pty Ltd; Apache-2.0.
#
#   -Visible   show Word, so a dialog can be seen
param([string]$Dir = "X:\revisions\cr068", [switch]$Visible)
$ErrorActionPreference = "Stop"
$tmp = Join-Path $env:TEMP "cr068-probe"
New-Item -ItemType Directory -Force -Path $tmp | Out-Null
$logFile = Join-Path $Dir "cr068-log.txt"
function Say($s) { $line = "{0:HH:mm:ss} {1}" -f (Get-Date), $s; Write-Host $line; Add-Content -Path $logFile -Value $line -Encoding UTF8 }
function Step($s) { Write-Host ("{0:HH:mm:ss}   - {1}" -f (Get-Date), $s) }

# Save in a format (16 docx, 2 plain text), then copy to the share; says so when Word writes no file (WD-001)
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

# Every story of the document (1 main, 2 footnotes, 4 comments, 5 text frames, 6-11 headers and footers)
function Stories($doc) {
  $list = @()
  foreach ($s in $doc.StoryRanges) { $st = $s; while ($st) { $list += $st; $st = $st.NextStoryRange } }
  return $list
}

function Esc($s) { return ($s -replace "`r", "\r" -replace "`n", "\n" -replace "`t", "\t" -replace [char]11, "\v" -replace [char]7, "\a") }

# The revisions of every story: type (1 insert, 2 delete, ...), author, date as Word shows it (the VM's clock), text
function Revisions($doc, $label) {
  $total = 0
  foreach ($st in (Stories $doc)) {
    $n = $st.Revisions.Count
    if ($n -eq 0) { continue }
    $total += $n
    for ($i = 1; $i -le $n; $i++) {
      $rv = $st.Revisions.Item($i)
      Say ("  {0} story {1} #{2}: type {3} author '{4}' date {5:yyyy-MM-dd HH:mm:ss} text '{6}'" -f $label, $st.StoryType, $i,
        $rv.Type, $rv.Author, $rv.Date, (Esc $rv.Range.Text))
    }
  }
  Say ("{0}: {1} revisions in all" -f $label, $total)
}

# The first main-story paragraph (cells included) whose text starts with the prefix
function Para($doc, $prefix) {
  foreach ($p in $doc.Paragraphs) { if ($p.Range.Text.StartsWith($prefix)) { return $p } }
  throw "no paragraph starting '$prefix'"
}

function Tbl($doc, $prefix) {
  foreach ($t in $doc.Tables) { if ($t.Range.Text.StartsWith($prefix)) { return $t } }
  throw "no table starting '$prefix'"
}

# The first occurrence of a text inside a range, as a range
function Found($range, $text) {
  $r = $range.Duplicate
  if (-not $r.Find.Execute($text)) { throw "no '$text'" }
  return $r
}

function Case($tag, [scriptblock]$body) {
  Step $tag
  try { & $body; Say "$tag ok" } catch { Say "$tag FAILED: $($_.Exception.Message)" }
}

$word = $null
try {
  $word = New-Object -ComObject Word.Application
  $word.Visible = [bool]$Visible; $word.DisplayAlerts = 0; $word.AutomationSecurity = 3
  Say ("Word {0} build {1}, user '{2}'" -f $word.Version, $word.Build, $word.UserName)

  # 1. Word's own tracked deletion of the struck text the default conversion deletes
  $own = Join-Path $Dir "manual-redline-word.docx"
  if (Test-Path $own) { Say "manual-redline-word.docx there already: skipped" } else {
    $doc = Open "manual-redline.docx"
    try {
      $doc.TrackRevisions = $true
      Case "M01" { (Found (Para $doc "M01 ").Range "1.316").Delete() | Out-Null }
      Case "M02" { (Found (Tbl $doc "M02 ").Range "1.316").Delete() | Out-Null }
      Case "M03" { (Found (Para $doc "M03 ").Range "Late delivery incurs no penalty. ").Delete() | Out-Null }
      Case "M04" {
        $t = (Tbl $doc "M04 ").Range
        foreach ($x in @("`$40.00", "Gizmo", "`$99.00")) { (Found $t $x).Delete() | Out-Null }
      }
      Case "M09" { (Found (Para $doc "M09 ").Range "example.com/old-terms").Delete() | Out-Null }
      Case "M10 text box" { (Found $doc.StoryRanges.Item(5) "`$40.00").Delete() | Out-Null }
      Case "M10 footnote" { (Found $doc.Footnotes.Item(1).Range "1.316").Delete() | Out-Null }
      Case "M10 header" { (Found $doc.Sections.Item(1).Headers.Item(1).Range "Draft ").Delete() | Out-Null }
      Case "M11" { (Para $doc "M11 This").Range.Delete() | Out-Null }
      Case "M13" { (Found (Para $doc "M13 ").Range "EUR 500").Delete() | Out-Null }
      Case "M14" {
        (Found (Para $doc "M14 ").Range "third struck run ").Delete() | Out-Null
        (Found (Para $doc "M14 ").Range "first struck run, second struck run, ").Delete() | Out-Null
      }
      Case "M15" {
        $p = (Para $doc "M15 ").Range
        $a = Found $p "old A"; $c = Found $p "old C "
        $r = $p.Duplicate; [void]$r.SetRange($a.Start, $c.End); $r.Delete() | Out-Null
      }
      Case "M17" { (Found (Para $doc "M17 ").Range "Acme Pty Ltd").Delete() | Out-Null }
      Revisions $doc "word-own (tracked)"
      SaveAs $doc "manual-redline-word.docx" 16
    } finally { Close $doc }
  }

  # 2. each input: its revisions and plain text as it is, then Word's Accept All and Reject All, each as docx and text
  foreach ($job in @(@("manual-redline.docx", "base"), @("manual-redline-default.docx", "default"),
      @("manual-redline-optins.docx", "optins"), @("manual-redline-d9.docx", "d9"), @("manual-redline-word.docx", "own"))) {
    $in = $job[0]; $key = $job[1]
    if (-not (Test-Path (Join-Path $Dir $in))) { Say "$in missing: skipped"; continue }
    if (Test-Path (Join-Path $Dir "word-$key-asis.txt")) { Say "word-$key-asis.txt there already: skipped" } else {
      try {
        $doc = Open $in
        Say "$in opened"
        try { Revisions $doc $key; SaveAs $doc "word-$key-asis.txt" 2 } finally { Close $doc }
      } catch { Say "$in FAILED: $($_.Exception.Message)" }
    }
    foreach ($how in @("accept", "reject")) {
      if (Test-Path (Join-Path $Dir "word-$key-$how-all.docx")) { Say "word-$key-$how-all.docx there already: skipped"; continue }
      try {
        $doc = Open $in
        try {
          Step "$how all"
          if ($how -eq "accept") { $doc.AcceptAllRevisions() } else { $doc.RejectAllRevisions() }
          $left = 0; foreach ($st in (Stories $doc)) { $left += $st.Revisions.Count }
          Say ("{0} {1} all: {2} revisions left" -f $key, $how, $left)
          SaveAs $doc "word-$key-$how-all.docx" 16
          SaveAs $doc "word-$key-$how-all.txt" 2
        } finally { Close $doc }
      } catch { Say "$in $how FAILED: $($_.Exception.Message)" }
    }
  }

  # 3. which date Word shows: w:date alone, w16du:dateUtc alone, both disagreeing, both as the conversions write them
  try {
    $doc = Open "manual-redline-dates.docx"
    try { Revisions $doc "dates" } finally { Close $doc }
  } catch { Say "manual-redline-dates.docx FAILED: $($_.Exception.Message)" }
} finally {
  if ($word) { try { $word.Quit() } catch { }; [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($word) }
}
Say "done"
