# CR-035 phase 4: Excel's own edits of a chart's data, saved before and after, for the editor
# (ChartDataEditor) to be compared with part by part.  Each case starts from Word's default chart
# data as an Excel table (Table1, A1:D5) with a clustered column chart made from it (or the
# case's own data, chart type and setting up), saved as <case>-before.xlsx; then Excel makes one
# edit, as a user would (typing into cells, resizing the table, deleting a table's row or column),
# and the workbook is saved as <case>-after.xlsx.  Cases e1-e7 are the first round; r1-r14 the
# second (2026-10-12): a middle row and column deleted, a chart plotted by row, a duplicate
# header, dates, a line chart, data labels and a colour of the series' own, the chart filter,
# two levels of categories.  Beside them <case>.txt records the
# table's range and each series' formula before and after, and whether Excel grew the table itself
# (typing beside it) or the script had to resize it.  Into -Dir; a case already there is skipped,
# so a rerun resumes.  Copyright 2026, Plutext Pty Ltd; Apache-2.0.
#
#   -Visible   show Excel, so a dialog can be seen
param([string]$Dir = "X:\office_charts\phase4", [switch]$Visible)
$ErrorActionPreference = "Stop"
$tmp = Join-Path $env:TEMP "chart-edit-probes"
New-Item -ItemType Directory -Force -Path $Dir, $tmp | Out-Null
function Say($s) { Write-Host ("{0:HH:mm:ss} {1}" -f (Get-Date), $s) }

$xlColumnClustered = 51; $xlLineMarkers = 65; $xlSrcRange = 1; $xlYes = 1; $xlRows = 1

# The rows written from A1 in one assignment (cell by cell, Windows PowerShell's COM binder
# caches the type of a property's first assignment and fails on the next)
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

# One value into one cell, as a 1x1 array for the same reason
function Put($ws, $address, $value) {
  $arr = New-Object 'object[,]' 1, 1
  $arr[0, 0] = $value
  $ws.Range($address).Value2 = $arr
}

$default = @(
  @(" ", "Series 1", "Series 2", "Series 3"),   # A1 a space, as in Word's embedded workbook
  @("Category 1", 4.3, 2.4, 2),
  @("Category 2", 2.5, 4.4, 2),
  @("Category 3", 3.5, 1.8, 3),
  @("Category 4", 4.5, 2.8, 5))

$excel = New-Object -ComObject Excel.Application
$excel.Visible = [bool]$Visible
$excel.DisplayAlerts = $false

function SaveTo($wb, $name) {
  $local = Join-Path $tmp $name
  if (Test-Path $local) { Remove-Item -Force $local }
  $wb.SaveCopyAs($local)
  Copy-Item -Force $local (Join-Path $Dir $name)
  Say "  wrote $name"
}

# The table's range and the chart's series formulas, for the case's report
function State($lo, $ch) {
  $lines = @("  table " + $lo.Range.Address($false, $false))
  # every series, those hidden with the chart filter too
  for ($i = 1; $i -le $ch.FullSeriesCollection().Count; $i++) {
    $s = $ch.FullSeriesCollection($i)
    $f = try { $s.Formula } catch { "(no formula: $($_.Exception.Message))" }
    $lines += "  series ${i}: " + $f + $(if ($s.IsFiltered) { " (filtered)" } else { "" })
  }
  $lines
}

# Grows the table to $address where Excel did not grow it as the cells were typed
function Grow($ws, $lo, $address) {
  if ($lo.Range.Address($false, $false) -eq $address) { $script:report += "table grown by Excel as typed" }
  else {
    $script:report += ("table not grown by Excel ({0}); resized to {1}" -f $lo.Range.Address($false, $false), $address)
    $lo.Resize($ws.Range($address))
  }
}

# A case: its own data, range, chart type and setting up, where given (-Data, -Range, -Type,
# -Setup), else Word's default data in A1:D5 as a clustered column chart
function Case($name, [scriptblock]$edit, $Data = $default, $Range = "A1:D5", $Type = $xlColumnClustered,
    [scriptblock]$Setup = $null) {
  if (Test-Path (Join-Path $Dir "$name-after.xlsx")) { Say "$name is there already"; return }
  Say "case $name"
  $wb = $excel.Workbooks.Add()
  try {
    $ws = $wb.Worksheets.Item(1)
    Fill $ws $Data
    $lo = $ws.ListObjects.Add($xlSrcRange, $ws.Range($Range), $null, $xlYes)
    $lo.Name = "Table1"
    $shape = $ws.Shapes.AddChart2(-1, $Type, 300, 20, 432, 252)
    $shape.Chart.SetSourceData($ws.Range($Range))
    if ($Setup) { & $Setup $wb $ws $lo $shape.Chart }
    SaveTo $wb "$name-before.xlsx"
    $script:report = @("before:") + (State $lo $shape.Chart)
    & $edit $wb $ws $lo $shape.Chart
    $script:report += @("after:") + (State $lo $shape.Chart)
    SaveTo $wb "$name-after.xlsx"
    $script:report | Set-Content -Encoding UTF8 (Join-Path $Dir "$name.txt")
  } finally { $wb.Close($false) }
}

try {
  # a value changed
  Case "e1-value" { param($wb, $ws, $lo, $ch) Put $ws "C3" 7.5 }
  # a category's label and a series' name changed
  Case "e2-labels" { param($wb, $ws, $lo, $ch) Put $ws "A3" "Second"; Put $ws "C1" "Revenue" }
  # a category added: typed into the row under the table, which Excel's table grows to take
  Case "e3-add-category" { param($wb, $ws, $lo, $ch)
    Put $ws "A6" "Category 5"; Put $ws "B6" 3.1; Put $ws "C6" 2.2; Put $ws "D6" 4
    Grow $ws $lo "A1:D6" }
  # a series added: typed into the column beside the table
  Case "e4-add-series" { param($wb, $ws, $lo, $ch)
    Put $ws "E1" "Series 4"; Put $ws "E2" 1.5; Put $ws "E3" 2.5; Put $ws "E4" 3.5; Put $ws "E5" 4.5
    Grow $ws $lo "A1:E5" }
  # a series removed: the table shrunk by its last column
  Case "e5-remove-series" { param($wb, $ws, $lo, $ch)
    $lo.Resize($ws.Range("A1:C5")); $ws.Range("D1:D5").ClearContents() }
  # a category removed: the table shrunk by its last row
  Case "e6-remove-category" { param($wb, $ws, $lo, $ch)
    $lo.Resize($ws.Range("A1:D4")); $ws.Range("A5:D5").ClearContents() }
  # a value made blank, and one made an error
  Case "e7-blank-and-error" { param($wb, $ws, $lo, $ch)
    $ws.Range("B3").ClearContents(); $ws.Range("D4").Formula = "=NA()" }

  # ---- round 2
  $addCategory = { param($wb, $ws, $lo, $ch)
    Put $ws "A6" "Category 5"; Put $ws "B6" 3.1; Put $ws "C6" 2.2; Put $ws "D6" 4
    Grow $ws $lo "A1:D6" }
  $addSeries = { param($wb, $ws, $lo, $ch)
    Put $ws "E1" "Series 4"; Put $ws "E2" 1.5; Put $ws "E3" 2.5; Put $ws "E4" 3.5; Put $ws "E5" 4.5
    Grow $ws $lo "A1:E5" }
  $byRow = { param($wb, $ws, $lo, $ch) $ch.PlotBy = $xlRows }
  $filtered = { param($wb, $ws, $lo, $ch)
    $ch.FullSeriesCollection(2).IsFiltered = $true
    $ch.ChartGroups(1).FullCategoryCollection(3).IsFiltered = $true }

  # a table's middle column and middle row deleted (Delete > Table Columns, Table Rows)
  Case "r1-delete-middle-series" { param($wb, $ws, $lo, $ch) $lo.ListColumns.Item(3).Delete() }
  Case "r2-delete-middle-category" { param($wb, $ws, $lo, $ch) $lo.ListRows.Item(2).Delete() }
  # the chart plotted by row (Switch Row/Column): a row typed under the table is a series, a
  # column beside it a category; a middle row and column deleted
  Case "r3-byrow-add-series" $addCategory -Setup $byRow
  Case "r4-byrow-add-category" $addSeries -Setup $byRow
  Case "r5-byrow-delete-series" { param($wb, $ws, $lo, $ch) $lo.ListRows.Item(2).Delete() } -Setup $byRow
  Case "r6-byrow-delete-category" { param($wb, $ws, $lo, $ch) $lo.ListColumns.Item(3).Delete() } -Setup $byRow
  # a series named as another is: Excel's table header
  Case "r7-duplicate-name" { param($wb, $ws, $lo, $ch) Put $ws "C1" "Series 1" }
  # date categories (d-mmm), a row typed under the table: r8 with a text label (Category 5), r8b
  # with the next date as a number; whether the new cell takes the column's format, and the axis
  Case "r8-date-category" $addCategory -Data @(
      @(" ", "Series 1", "Series 2", "Series 3"), @(46023, 4.3, 2.4, 2), @(46024, 2.5, 4.4, 2),
      @(46025, 3.5, 1.8, 3), @(46026, 4.5, 2.8, 5)) -Setup { param($wb, $ws, $lo, $ch)
      $ws.Range("A2:A5").NumberFormat = "d-mmm" }
  Case "r8b-date-number" { param($wb, $ws, $lo, $ch)
      Put $ws "A6" 46027; Put $ws "B6" 3.1; Put $ws "C6" 2.2; Put $ws "D6" 4
      Grow $ws $lo "A1:D6" } -Data @(
      @(" ", "Series 1", "Series 2", "Series 3"), @(46023, 4.3, 2.4, 2), @(46024, 2.5, 4.4, 2),
      @(46025, 3.5, 1.8, 3), @(46026, 4.5, 2.8, 5)) -Setup { param($wb, $ws, $lo, $ch)
      $ws.Range("A2:A5").NumberFormat = "d-mmm" }
  # a series added to a line chart (markers), to one whose last series shows data labels, and to
  # one whose last series has a colour of its own (red)
  Case "r9-line-add-series" $addSeries -Type $xlLineMarkers
  Case "r10-labels-add-series" $addSeries -Setup { param($wb, $ws, $lo, $ch)
      $ch.FullSeriesCollection(3).HasDataLabels = $true }
  Case "r11-colour-add-series" $addSeries -Setup { param($wb, $ws, $lo, $ch)
      $ch.FullSeriesCollection(3).Format.Fill.ForeColor.RGB = 255 }
  # Excel's chart filter (series 2 and category 3 hidden), then a row and a column added
  Case "r12-filtered-add-category" $addCategory -Setup $filtered
  Case "r13-filtered-add-series" $addSeries -Setup $filtered
  # two levels of categories (region over quarter), a row added under the table
  Case "r14-multilevel-add-category" { param($wb, $ws, $lo, $ch)
      Put $ws "B6" "Q3"; Put $ws "C6" 3.9
      Grow $ws $lo "A1:C6" } -Range "A1:C5" -Data @(
      @(" ", "  ", "Sales"), @("North", "Q1", 4.3), @("", "Q2", 2.5), @("South", "Q1", 3.5), @("", "Q2", 4.5))
} finally {
  $excel.Quit()
  [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($excel)
}
Say "done"
