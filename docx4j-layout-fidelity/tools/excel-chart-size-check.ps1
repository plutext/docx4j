# CR-037 phase 3: Excel's report of the size of each chart a workbook's two-cell anchor places,
# against which docx4j's ChartAnchorSize (the cells summed by ECMA-376's column width rule) is
# checked.  For each workbook in -Dir (chart_size_probes.py's), Excel opens a local copy read-only
# and writes <Dir>\excel\<workbook>.json: each chart's Left, Top, Width and Height (points) and
# its corner cells; the sheet's standard width and height; columns A to T (width in points and in
# characters, hidden) and rows 1 to 25 (height, hidden).  Nothing is saved back.  Copyright 2026,
# Plutext Pty Ltd; Apache-2.0.  The first run (2026-10-13, the VM's display at 125%: 120 dots an
# inch) is in docx4j-chart-render's test resources, charts/sizes/excel-sizes.tsv.
#
#   -Hidden    don't show Excel (shown by default, so that it lays the sheet out as a user sees it)
#   -OutName N the folder under -Dir for the reports (default excel): Excel lays the sheet out in
#              pixels of the screen, so a run with the display at another scale goes to its own
param([string]$Dir = "X:\office_charts\cr037\sizes", [switch]$Hidden, [string]$OutName = "excel")
$ErrorActionPreference = "Stop"
$tmp = Join-Path $env:TEMP "chart-size-check"
New-Item -ItemType Directory -Force -Path $tmp | Out-Null
$reports = Join-Path $Dir $OutName
New-Item -ItemType Directory -Force -Path $reports | Out-Null
function Say($s) { Write-Host ("{0:HH:mm:ss} {1}" -f (Get-Date), $s) }

$excel = New-Object -ComObject Excel.Application
$excel.Visible = -not [bool]$Hidden
$excel.DisplayAlerts = $false
try {
  foreach ($f in Get-ChildItem $Dir -Filter *.xlsx) {
    $json = Join-Path $reports ($f.BaseName + ".json")
    if (Test-Path $json) { Say "$($f.Name): done already"; continue }
    Say $f.Name
    $local = Join-Path $tmp $f.Name
    Copy-Item -Force $f.FullName $local
    $wb = $null
    try {
      $wb = $excel.Workbooks.Open($local, 0, $true)
      $sheets = @()
      foreach ($ws in $wb.Worksheets) {
        $ws.Activate()
        $charts = @()
        foreach ($co in $ws.ChartObjects()) {
          $charts += [ordered]@{
            name = [string]$co.Name; left = [double]$co.Left; top = [double]$co.Top
            width = [double]$co.Width; height = [double]$co.Height
            topLeft = [string]$co.TopLeftCell.Address($false, $false)
            bottomRight = [string]$co.BottomRightCell.Address($false, $false)
          }
        }
        $columns = @()
        for ($i = 1; $i -le 20; $i++) {
          $c = $ws.Columns($i)
          $columns += [ordered]@{ n = $i; width = [double]$c.Width; chars = [double]$c.ColumnWidth; hidden = [bool]$c.Hidden }
        }
        $rows = @()
        for ($i = 1; $i -le 25; $i++) {
          $r = $ws.Rows($i)
          $rows += [ordered]@{ n = $i; height = [double]$r.Height; hidden = [bool]$r.Hidden }
        }
        $sheets += [ordered]@{
          name = [string]$ws.Name; standardWidth = [double]$ws.StandardWidth; standardHeight = [double]$ws.StandardHeight
          charts = $charts; columns = $columns; rows = $rows
        }
      }
      $report = [ordered]@{ workbook = $f.Name; excel = [string]$excel.Version; build = [string]$excel.Build; sheets = $sheets }
      $report | ConvertTo-Json -Depth 6 | Set-Content -Encoding UTF8 $json
    } catch {
      ("FAILED: " + $_.Exception.Message) | Set-Content -Encoding UTF8 (Join-Path $reports ($f.BaseName + ".failed.txt"))
      Say "  failed: $($_.Exception.Message)"
    } finally {
      if ($wb -ne $null) { $wb.Close($false) }
    }
  }
} finally {
  $excel.Quit()
  [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($excel)
}
Say "done"
