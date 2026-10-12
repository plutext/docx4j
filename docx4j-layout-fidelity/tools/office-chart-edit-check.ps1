# CR-036 phase 1's Office check: Word and PowerPoint open each document in the folder given (docx4j's
# chart editor's outputs) and, for each, write to <folder>\check\<document>.txt whether it opened
# (a repair shows as a dialog with -Visible, or a failed open), and what Edit Data shows for each
# chart (its data sheet's used range, cell by cell), and save the document's PDF as Word or
# PowerPoint exports it, <folder>\check\<document>.pdf, to compare with their own edit's PDF.
# Nothing is saved back.  The ways of driving Word and PowerPoint are office-chart-authoring-probes'
# (Word SaveAs with [ref] arguments, the chart's data window closed by WM_CLOSE).  Copyright 2026,
# Plutext Pty Ltd; Apache-2.0.
#
#   -Visible   show the applications, so a repair dialog can be seen
#   -Tools     (CR-036 phase 3) after the PDF, each chart given the next style of the gallery and
#              another type and back, as a user's Chart Design tools would: "tools ok" or the error
param([string]$Dir = "X:\office_charts\cr036\docx4j", [switch]$Visible, [switch]$Tools)
$ErrorActionPreference = "Stop"
$tmp = Join-Path $env:TEMP "chart-edit-check"
$out = Join-Path $Dir "check"
New-Item -ItemType Directory -Force -Path $tmp, $out | Out-Null
function Say($s) { Write-Host ("{0:HH:mm:ss} {1}" -f (Get-Date), $s) }
function Step($s) { Write-Host ("{0:HH:mm:ss}   - {1}" -f (Get-Date), $s) }

Add-Type @"
using System; using System.Runtime.InteropServices; using System.Text;
public static class DataWindows {
  public delegate bool EnumProc(IntPtr h, IntPtr l);
  [DllImport("user32.dll")] static extern bool EnumWindows(EnumProc f, IntPtr l);
  [DllImport("user32.dll", CharSet = CharSet.Unicode)] static extern int GetWindowText(IntPtr h, StringBuilder s, int n);
  [DllImport("user32.dll")] static extern bool IsWindowVisible(IntPtr h);
  [DllImport("user32.dll")] static extern bool PostMessage(IntPtr h, uint m, IntPtr w, IntPtr l);
  public static int Close(string titlePart) {
    int n = 0;
    EnumWindows(delegate (IntPtr h, IntPtr l) {
      if (IsWindowVisible(h)) {
        StringBuilder sb = new StringBuilder(512);
        GetWindowText(h, sb, 512);
        if (sb.ToString().Contains(titlePart)) { PostMessage(h, 0x0010, IntPtr.Zero, IntPtr.Zero); n++; }
      }
      return true;
    }, IntPtr.Zero);
    return n;
  }
}
"@

# What Edit Data shows: the chart's data sheet opened, its used range read, the window closed
function EditData($chart) {
  $lines = @()
  Step "ChartData.Activate"
  $chart.ChartData.Activate()
  $wb = $null
  for ($i = 0; $i -lt 20 -and -not $wb; $i++) { $wb = $chart.ChartData.Workbook; if (-not $wb) { Start-Sleep -Milliseconds 500 } }
  if (-not $wb) { return @("Edit Data: no workbook within 10 seconds") }
  $ws = $null
  for ($i = 0; $i -lt 20 -and -not $ws; $i++) {
    try { $ws = $wb.Worksheets.Item(1) } catch { Start-Sleep -Milliseconds 500 }
  }
  $address = $ws.UsedRange.Address($false, $false)
  $lines += "Edit Data: $address"
  foreach ($row in $ws.UsedRange.Rows) {
    $cells = @()
    foreach ($c in $row.Cells) { $cells += [string]$c.Text }
    $lines += "  " + ($cells -join " | ")
  }
  try { $lines += "table " + $ws.ListObjects.Item(1).Range.Address($false, $false) } catch { $lines += "no table" }
  Start-Sleep -Seconds 1
  [void][DataWindows]::Close("Chart in Microsoft")
  Start-Sleep -Seconds 2
  return $lines
}

# The Chart Design tools on a chart: the next style, another type and back
function Tools($chart) {
  try {
    $style = $chart.ChartStyle; $type = $chart.ChartType
    $chart.ChartStyle = $style + 1
    $chart.ChartType = $(if ($type -eq 4) { 51 } else { 4 })
    $chart.ChartType = $type
    return "  tools ok: style $style to $($chart.ChartStyle), type $type to another and back"
  } catch { return "  TOOLS FAILED: $($_.Exception.Message)" }
}

$word = $null; $ppt = $null
try {
  $files = @(Get-ChildItem $Dir | Where-Object { $_.Extension -in ".docx", ".pptx" })
  foreach ($f in $files) {
    Say $f.Name
    $local = Join-Path $tmp $f.Name
    Copy-Item -Force $f.FullName $local
    $report = @()
    # a docx and a pptx of the same name: each report and PDF named with its extension too
    $base = $f.BaseName
    if (@($files | Where-Object { $_.BaseName -eq $f.BaseName }).Count -gt 1) { $base = $base + "-" + $f.Extension.TrimStart(".") }
    $pdf = Join-Path $tmp ($base + ".pdf")
    try {
      if ($f.Extension -eq ".docx") {
        if (-not $word) {
          $word = New-Object -ComObject Word.Application
          $word.Visible = [bool]$Visible; $word.DisplayAlerts = 0; $word.AutomationSecurity = 3
        }
        Step "Documents.Open"
        $doc = $word.Documents.Open($local, $false, $true, $false)   # read-only, as office-colours.ps1
        try {
          $report += "opened"
          foreach ($ic in $doc.InlineShapes) { if ($ic.HasChart) { $report += EditData $ic.Chart } }
          Step "SaveAs PDF"
          [string]$p = $pdf.ToString(); [int]$fmt = 17
          $doc.SaveAs([ref]$p, [ref]$fmt)
          Copy-Item -Force $pdf $out
          if ($Tools) { foreach ($ic in $doc.InlineShapes) { if ($ic.HasChart) { $report += Tools $ic.Chart } } }
        } finally { [int]$dontSave = 0; $doc.Close([ref]$dontSave) }
      } else {
        if (-not $ppt) { $ppt = New-Object -ComObject PowerPoint.Application }
        Step "Presentations.Open"
        $pres = $ppt.Presentations.Open($local, 0, 0, -1)   # ReadOnly no, Untitled no, WithWindow
        try {
          $report += "opened"
          foreach ($slide in $pres.Slides) {
            foreach ($sh in $slide.Shapes) { if ($sh.HasChart) { $report += EditData $sh.Chart } }
          }
          Step "SaveAs PDF"
          [string]$p = $pdf.ToString(); $pres.SaveAs($p, 32)   # 32 = ppSaveAsPDF, as the probes
          Copy-Item -Force $pdf $out
          if ($Tools) {
            foreach ($slide in $pres.Slides) { foreach ($sh in $slide.Shapes) { if ($sh.HasChart) { $report += Tools $sh.Chart } } }
          }
        } finally { $pres.Close() }
      }
    } catch { $report += "FAILED: $($_.Exception.Message)"; Say "  FAILED: $($_.Exception.Message)" }
    $report | Set-Content -Encoding UTF8 (Join-Path $out ($base + ".txt"))
  }
} finally {
  foreach ($app in @($word, $ppt)) {
    if ($app) { try { $app.Quit() } catch { }; [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($app) }
  }
}
Say "done"
