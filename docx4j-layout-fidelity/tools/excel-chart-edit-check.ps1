# CR-035 phase 4's Office check: Excel opens each workbook in the folders given (docx4j's chart
# editor's outputs, and Excel's own edits beside them, to compare with) and, for each, writes to
# <folder>\check\<workbook>.txt whether Excel repaired it (a repair log new in %TEMP%, or an open
# that failed), each table's range, each chart's series formulas (what Select Data shows), and
# saves each chart as <folder>\check\<workbook>-<n>.png.  Read-only: nothing is saved back.
# Copyright 2026, Plutext Pty Ltd; Apache-2.0.
#
#   -Visible   show Excel, so a repair dialog can be seen
param([string[]]$Dirs = @("X:\office_charts\phase4\docx4j", "X:\office_charts\phase4"), [switch]$Visible)
$ErrorActionPreference = "Stop"
$tmp = Join-Path $env:TEMP "chart-edit-check"
New-Item -ItemType Directory -Force -Path $tmp | Out-Null
function Say($s) { Write-Host ("{0:HH:mm:ss} {1}" -f (Get-Date), $s) }
function Logs { @(Get-ChildItem $env:TEMP -Filter "error*.xml" -ErrorAction SilentlyContinue | ForEach-Object { $_.FullName }) }

$excel = New-Object -ComObject Excel.Application
$excel.Visible = [bool]$Visible
$excel.DisplayAlerts = $false
try {
  foreach ($dir in $Dirs) {
    if (-not (Test-Path $dir)) { Say "no folder $dir"; continue }
    $out = Join-Path $dir "check"
    New-Item -ItemType Directory -Force -Path $out | Out-Null
    foreach ($f in Get-ChildItem $dir -Filter *.xlsx) {
      Say $f.Name
      $local = Join-Path $tmp $f.Name
      Copy-Item -Force $f.FullName $local
      $report = @()
      $before = Logs
      $wb = $null
      try { $wb = $excel.Workbooks.Open($local, 0, $true) }
      catch { $report += "OPEN FAILED: $($_.Exception.Message)" }
      $new = @(Logs | Where-Object { $before -notcontains $_ })
      if ($new.Count -gt 0) {
        $report += "REPAIRED: Excel wrote a repair log"
        foreach ($l in $new) { $report += (Get-Content -Raw $l) }
      } elseif ($wb -ne $null) { $report += "opened, no repair" }
      if ($wb -ne $null) {
        $n = 0
        foreach ($ws in $wb.Worksheets) {
          foreach ($lo in $ws.ListObjects) { $report += "table $($lo.Name) $($lo.Range.Address($false, $false))" }
          foreach ($co in $ws.ChartObjects()) {
            $n++
            $ch = $co.Chart
            $report += "chart $n"
            for ($i = 1; $i -le $ch.FullSeriesCollection().Count; $i++) {
              $s = $ch.FullSeriesCollection($i)
              $sf = try { $s.Formula } catch { "(no formula)" }
              $report += "  series ${i}: " + $sf + $(if ($s.IsFiltered) { " (filtered)" } else { "" })
            }
            $png = Join-Path $tmp ("{0}-{1}.png" -f $f.BaseName, $n)
            [void]$ch.Export($png)
            Copy-Item -Force $png $out
          }
        }
        $wb.Close($false)
      }
      $report | Set-Content -Encoding UTF8 (Join-Path $out ($f.BaseName + ".txt"))
    }
  }
} finally {
  $excel.Quit()
  [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($excel)
}
Say "done"
