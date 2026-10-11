# CR-034 phase 0: Office's PDFs of the theme-colour probes (ThemeColourProbes), for
# tools/office_colours.py to read.  Each probe is opened read-only in the application that
# made its format - Word, PowerPoint or Excel, directly, with no docx4j in between - and
# exported as PDF into goldens\ beside it.  Copyright 2026, Plutext Pty Ltd; Apache-2.0.
param([string]$Dir = "X:\fidelity\theme-colours")
$out = Join-Path $Dir "goldens"
New-Item -ItemType Directory -Force -Path $out | Out-Null

$docx = Get-ChildItem -Path $Dir -Filter *.docx | Where-Object { $_.Name -notlike '~$*' }
if ($docx) {
  $word = New-Object -ComObject Word.Application
  $word.Visible = $false
  $word.DisplayAlerts = 0
  try {
    foreach ($f in $docx) {
      $doc = $word.Documents.Open($f.FullName, $false, $true)   # ConfirmConversions, ReadOnly
      try { $doc.ExportAsFixedFormat((Join-Path $out ($f.BaseName + ".pdf")), 17) ; Write-Host ("Word: " + $f.Name) }   # 17 = wdExportFormatPDF
      finally { $doc.Close(0) }
    }
  } finally { $word.Quit(); [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($word) }
}

$pptx = Get-ChildItem -Path $Dir -Filter *.pptx | Where-Object { $_.Name -notlike '~$*' }
if ($pptx) {
  $ppt = New-Object -ComObject PowerPoint.Application
  try {
    foreach ($f in $pptx) {
      $pres = $ppt.Presentations.Open($f.FullName, -1, 0, 0)   # ReadOnly, Untitled, WithWindow
      try { $pres.SaveAs((Join-Path $out ($f.BaseName + ".pdf")), 32) ; Write-Host ("PowerPoint: " + $f.Name) }   # 32 = ppSaveAsPDF
      finally { $pres.Close() }
    }
  } finally { $ppt.Quit(); [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($ppt) }
}

$xlsx = Get-ChildItem -Path $Dir -Filter *.xlsx | Where-Object { $_.Name -notlike '~$*' }
if ($xlsx) {
  $excel = New-Object -ComObject Excel.Application
  $excel.Visible = $false
  $excel.DisplayAlerts = $false
  try {
    foreach ($f in $xlsx) {
      $wb = $excel.Workbooks.Open($f.FullName, 0, $true)
      try { $wb.ExportAsFixedFormat(0, (Join-Path $out ($f.BaseName + ".pdf"))) ; Write-Host ("Excel: " + $f.Name) }   # 0 = xlTypePDF
      finally { $wb.Close($false) }
    }
  } finally { $excel.Quit(); [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($excel) }
}
