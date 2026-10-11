# CR-035 phase 2: the chart probes the read model needs and the phase 0 fixtures lack, made by
# Excel itself (no docx4j in between), one workbook per case, into -Dir; then a chart pasted into
# PowerPoint.  Render them afterwards with office_charts\excel-goldens.ps1 -Dir <same folder>.
# Copyright 2026, Plutext Pty Ltd; Apache-2.0.
#
# Each workbook is made in a local temp folder and copied out (Office can stop on a hidden prompt
# for a file on a shared drive).  A workbook already in -Dir is skipped, so a rerun resumes.
#
#   -Visible   show Excel and PowerPoint, so a dialog can be seen
param([string]$Dir = "X:\office_charts\phase2", [switch]$Visible)
$ErrorActionPreference = "Stop"
$tmp = Join-Path $env:TEMP "chart-probes"
New-Item -ItemType Directory -Force -Path $Dir, $tmp | Out-Null
function Say($s) { Write-Host ("{0:HH:mm:ss} {1}" -f (Get-Date), $s) }

# Excel's constants (XlChartType and friends)
$xlArea = 1; $xlLine = 4; $xlLineMarkers = 65; $xlColumnClustered = 51; $xl3DColumn = -4100
$xlXYScatterSmooth = 72; $xlDoughnut = -4120; $xlRadarMarkers = 81; $xlBubble = 15
$xlStockHLC = 88; $xlSurface = 83; $xlBarOfPie = 71; $xlSecondary = 2; $xlWorkbookDefault = 51

# Word's default chart data: four categories, three series
$default = @(
  @("", "Series 1", "Series 2", "Series 3"),
  @("Category 1", 4.3, 2.4, 2),
  @("Category 2", 2.5, 4.4, 2),
  @("Category 3", 3.5, 1.8, 3),
  @("Category 4", 4.5, 2.8, 5))

# The rows written from A1 in one assignment of an object[,]: cell by cell, Windows PowerShell
# caches the type of a COM property's first assignment ("Series 1") and fails on the next (4.3),
# "Unable to cast object of type 'System.Double' to type 'System.String'".  "" is a blank cell.
function Fill($ws, $rows) {
  $n = $rows.Count; $m = 0
  foreach ($row in $rows) { if ($row.Count -gt $m) { $m = $row.Count } }
  $arr = New-Object 'object[,]' $n, $m
  for ($r = 0; $r -lt $n; $r++) {
    for ($c = 0; $c -lt $rows[$r].Count; $c++) {
      $v = $rows[$r][$c]
      if ($v -is [string] -and $v -eq "") { $v = $null }
      $arr[$r, $c] = $v
    }
  }
  $ws.Range("A1").Resize($n, $m).Value2 = $arr
}

function AddChart($ws, $type, $range) {
  # AddChart2(Style -1 = the type's default, type, left, top, width, height): Excel 2013 and later
  $shape = $ws.Shapes.AddChart2(-1, $type, 300, 20, 432, 252)
  $shape.Chart.SetSourceData($ws.Range($range))
  return $shape.Chart
}

$excel = New-Object -ComObject Excel.Application
$excel.Visible = [bool]$Visible
$excel.DisplayAlerts = $false

function Probe($name, [scriptblock]$build) {
  $target = Join-Path $Dir "$name.xlsx"
  if (Test-Path $target) { Say "$name.xlsx is there already"; return }
  Say "making $name.xlsx"
  $wb = $excel.Workbooks.Add()
  try {
    $ws = $wb.Worksheets.Item(1)
    & $build $wb $ws
    $local = Join-Path $tmp "$name.xlsx"
    if (Test-Path $local) { Remove-Item -Force $local }
    $wb.SaveAs($local, $xlWorkbookDefault)
  } finally { $wb.Close($false) }
  Copy-Item -Force (Join-Path $tmp "$name.xlsx") $target
}

try {
  # the chart kinds the phase 0 fixtures lack, each from Word's default data
  Probe "p2-area" { param($wb, $ws) Fill $ws $default; [void](AddChart $ws $xlArea "A1:D5") }
  Probe "p2-doughnut" { param($wb, $ws) Fill $ws $default; [void](AddChart $ws $xlDoughnut "A1:B5") }
  Probe "p2-radar" { param($wb, $ws) Fill $ws $default; [void](AddChart $ws $xlRadarMarkers "A1:D5") }
  Probe "p2-surface" { param($wb, $ws) Fill $ws $default; [void](AddChart $ws $xlSurface "A1:D5") }
  Probe "p2-bar-of-pie" { param($wb, $ws)
    Fill $ws @(@("", "Sales"), @("North", 8.2), @("South", 3.2), @("East", 1.4), @("West", 1.2), @("Central", 0.9), @("Other", 0.4))
    [void](AddChart $ws $xlBarOfPie "A1:B7") }
  Probe "p2-3d-column" { param($wb, $ws) Fill $ws $default; [void](AddChart $ws $xl3DColumn "A1:D5") }
  Probe "p2-scatter" { param($wb, $ws)
    Fill $ws @(@("X", "Y"), @(0.7, 2.7), @(1.8, 3.2), @(2.6, 0.8), @(3.9, 4.1), @(4.4, 2.2))
    [void](AddChart $ws $xlXYScatterSmooth "A1:B6") }
  Probe "p2-bubble" { param($wb, $ws)
    Fill $ws @(@("X", "Y", "Size"), @(0.7, 2.7, 10), @(1.8, 3.2, 4), @(2.6, 0.8, 8))
    [void](AddChart $ws $xlBubble "A1:C4") }
  Probe "p2-stock" { param($wb, $ws)
    # dates as serial numbers, 2026-01-01 to 2026-01-05 (as Excel shows them)
    Fill $ws @(@("Date", "High", "Low", "Close"), @(46023, 55, 11, 25), @(46024, 57, 12, 38),
      @(46025, 57, 13, 50), @(46026, 58, 11, 35), @(46027, 58, 25, 43))
    $ws.Range("A2:A6").NumberFormat = "yyyy-mm-dd"
    [void](AddChart $ws $xlStockHLC "A1:D6") }

  # a date axis: a line chart over dates
  Probe "p2-date-axis" { param($wb, $ws)
    Fill $ws @(@("Date", "Visits"), @(46023, 120), @(46030, 135), @(46037, 128), @(46044, 150), @(46051, 162))
    $ws.Range("A2:A6").NumberFormat = "d-mmm"
    [void](AddChart $ws $xlLineMarkers "A1:B6") }

  # a combination chart: series 1 and 2 clustered columns, series 3 a line on a secondary axis
  Probe "p2-combo-secondary" { param($wb, $ws)
    Fill $ws @(@("", "Series 1", "Series 2", "Share"), @("Category 1", 4.3, 2.4, 0.21), @("Category 2", 2.5, 4.4, 0.35),
      @("Category 3", 3.5, 1.8, 0.48), @("Category 4", 4.5, 2.8, 0.62))
    $ch = AddChart $ws $xlColumnClustered "A1:D5"
    $s = $ch.FullSeriesCollection(3); $s.ChartType = $xlLine; $s.AxisGroup = $xlSecondary }

  # a chartsheet: the chart on a sheet of its own, its data on Sheet1
  Probe "p2-chartsheet" { param($wb, $ws)
    Fill $ws $default
    $cs = $wb.Charts.Add()
    $cs.ChartType = $xlColumnClustered
    $cs.SetSourceData($ws.Range("A1:D5"))
    $cs.Name = "Chart1" }

  # gaps: a blank cell and a #N/A in a line chart's values
  Probe "p2-gaps" { param($wb, $ws)
    Fill $ws $default
    $ws.Range("B3").ClearContents()
    $ws.Range("C4").Formula = "=NA()"
    [void](AddChart $ws $xlLineMarkers "A1:D5") }

  # multi-level categories: region over quarter
  Probe "p2-multilevel" { param($wb, $ws)
    Fill $ws @(@("", "", "Sales"), @("North", "Q1", 4.3), @("", "Q2", 2.5), @("South", "Q1", 3.5), @("", "Q2", 4.5))
    [void](AddChart $ws $xlColumnClustered "A1:C5") }

  # a series and a category filtered out with Excel's chart filter
  Probe "p2-filtered" { param($wb, $ws)
    Fill $ws $default
    $ch = AddChart $ws $xlColumnClustered "A1:D5"
    $ch.FullSeriesCollection(2).IsFiltered = $true
    $ch.ChartGroups(1).FullCategoryCollection(3).IsFiltered = $true }

  # a chart pasted into PowerPoint (the workbook kept for its source, the pptx beside it)
  $pptxTarget = Join-Path $Dir "p2-pasted.pptx"
  if (-not (Test-Path $pptxTarget)) {
    Say "making p2-pasted.pptx"
    $wb = $excel.Workbooks.Add()
    try {
      $ws = $wb.Worksheets.Item(1)
      Fill $ws $default
      $ch = AddChart $ws $xlColumnClustered "A1:D5"
      $ch.ChartArea.Copy()
      $pptWasRunning = [bool](Get-Process -Name POWERPNT -ErrorAction SilentlyContinue)
      $ppt = New-Object -ComObject PowerPoint.Application
      try {
        $pres = $ppt.Presentations.Add($(if ($Visible) { -1 } else { 0 }))
        try {
          $slide = $pres.Slides.Add(1, 12)   # ppLayoutBlank
          [void]$slide.Shapes.Paste()        # the default paste: an embedded workbook, the destination theme
          $local = Join-Path $tmp "p2-pasted.pptx"
          if (Test-Path $local) { Remove-Item -Force $local }
          $pres.SaveAs($local)
        } finally { $pres.Close() }
      } finally {
        if (-not $pptWasRunning) { $ppt.Quit() }
        [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($ppt)
      }
      Copy-Item -Force (Join-Path $tmp "p2-pasted.pptx") $pptxTarget
    } finally { $wb.Close($false) }
  }
} finally {
  $excel.Quit()
  [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($excel)
}
Say "done"
