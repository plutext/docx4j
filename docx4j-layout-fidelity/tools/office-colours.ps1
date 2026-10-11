# CR-034 phase 0: Office's PDFs of the theme-colour probes (ThemeColourProbes), for
# tools/office_colours.py to read.  Each probe is copied to a local temp folder (as documents4j
# does: Office can stop on a hidden prompt for a file on a network or shared drive), opened
# read-only in the application that made its format - Word, PowerPoint or Excel, with no docx4j
# in between - and exported as PDF into goldens\ beside it, named <file name>.pdf (the docx and
# pptx of P3 share a base name).  A PDF already there is skipped, so
# a rerun resumes.  Copyright 2026, Plutext Pty Ltd; Apache-2.0.
#
#   -Visible   show the applications, so a dialog (repair, trust, sign-in) can be seen
#   -Only      Word, PowerPoint or Excel: one application only
param([string]$Dir = "X:\fidelity\theme-colours", [switch]$Visible, [string]$Only = "")
$ErrorActionPreference = "Stop"
$out = Join-Path $Dir "goldens"
$tmp = Join-Path $env:TEMP "office-colours"
New-Item -ItemType Directory -Force -Path $out, $tmp | Out-Null

function Say($s) { Write-Host ("{0:HH:mm:ss} {1}" -f (Get-Date), $s) }

function Todo($ext) {
  Get-ChildItem -Path $Dir -Filter "*.$ext" | Where-Object {
    $_.Name -notlike '~$*' -and -not (Test-Path (Join-Path $out ($_.Name + ".pdf")))
  }
}

# copy in, convert locally, copy the PDF out
function Local($f) {
  $in = Join-Path $tmp $f.Name
  Copy-Item -Force $f.FullName $in
  return $in
}
function Publish($f) {
  $pdf = Join-Path $tmp ($f.Name + ".pdf")
  Copy-Item -Force $pdf (Join-Path $out ($f.Name + ".pdf"))
  Say ("  wrote goldens\" + $f.Name + ".pdf")
}

foreach ($app in "WINWORD", "POWERPNT", "EXCEL") {
  if (Get-Process -Name $app -ErrorAction SilentlyContinue) {
    Say ("$app.EXE is already running; if this script stalls in that application, end it (taskkill /IM $app.EXE /F) and run again")
  }
}

$docx = @(Todo "docx")
if ($docx.Count -and ($Only -eq "" -or $Only -eq "Word")) {
  Say "starting Word"
  $word = New-Object -ComObject Word.Application
  $word.Visible = [bool]$Visible
  $word.DisplayAlerts = 0
  $word.AutomationSecurity = 3   # msoAutomationSecurityForceDisable
  try {
    foreach ($f in $docx) {
      $in = Local $f
      Say ("Word: opening " + $f.Name)
      # as documents4j's word_convert.vbs (which WordGoldenRunner uses on this VM): Open with
      # FileName, ConfirmConversions, ReadOnly, AddToRecentFiles; then SaveAs format 17 (PDF).
      # (ExportAsFixedFormat stalled here, 2026-10-12.)
      $doc = $word.Documents.Open($in, $false, $true, $false)
      try {
        Say "  saving as PDF"
        [string]$pdf = (Join-Path $tmp ($f.Name + ".pdf")).ToString()   # a plain string, not a PSObject
        [int]$format = 17   # wdFormatPDF
        $doc.SaveAs([ref]$pdf, [ref]$format)
      } finally { [int]$dontSave = 0; $doc.Close([ref]$dontSave) }
      Publish $f
    }
  } finally { $word.Quit(); [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($word) }
}

$pptx = @(Todo "pptx")
if ($pptx.Count -and ($Only -eq "" -or $Only -eq "PowerPoint")) {
  Say "starting PowerPoint"
  # PowerPoint runs one instance: if it was already running (someone's work open in it), leave it running
  $pptWasRunning = [bool](Get-Process -Name POWERPNT -ErrorAction SilentlyContinue)
  $ppt = New-Object -ComObject PowerPoint.Application
  $ppt.AutomationSecurity = 3
  try {
    foreach ($f in $pptx) {
      $in = Local $f
      Say ("PowerPoint: opening " + $f.Name)
      $pres = $ppt.Presentations.Open($in, -1, 0, $(if ($Visible) { -1 } else { 0 }))   # ReadOnly, Untitled, WithWindow
      try {
        Say "  exporting"
        $pres.SaveAs((Join-Path $tmp ($f.Name + ".pdf")), 32)   # 32 = ppSaveAsPDF
      } finally { $pres.Close() }
      Publish $f
    }
  } finally {
    if (-not $pptWasRunning) { $ppt.Quit() }
    [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($ppt)
  }
}

$xlsx = @(Todo "xlsx")
if ($xlsx.Count -and ($Only -eq "" -or $Only -eq "Excel")) {
  Say "starting Excel"
  $excel = New-Object -ComObject Excel.Application
  $excel.Visible = [bool]$Visible
  $excel.DisplayAlerts = $false
  $excel.AutomationSecurity = 3
  try {
    foreach ($f in $xlsx) {
      $in = Local $f
      Say ("Excel: opening " + $f.Name)
      $wb = $excel.Workbooks.Open($in, 0, $true)
      try {
        Say "  exporting"
        $wb.ExportAsFixedFormat(0, (Join-Path $tmp ($f.Name + ".pdf")))   # 0 = xlTypePDF
      } finally { $wb.Close($false) }
      Publish $f
    }
  } finally { $excel.Quit(); [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($excel) }
}
Say "done"
