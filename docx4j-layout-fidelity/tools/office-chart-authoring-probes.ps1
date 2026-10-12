# CR-036 phase 0: Office creating charts and editing their data, the oracles and templates of
# CR-036's phases 1 to 3.  One run on the Windows VM drives Word, Excel and PowerPoint (365):
#
#   P3  Word inserts each of the twelve chart types (InlineShapes.AddChart2, Word's default data),
#       p3-<type>.docx and its PDF; two charts in one document; a chart whose data is then set.
#   P1  Excel charts the same data: Word's embedded workbook (from p3-<type>.docx) opened in Excel,
#       AddChart2 over its table, p1-<type>.xlsx and the chart's PNG; and a date axis (a "Date"
#       header over d-mmm dates, which CR-035's r8 lacked).
#   P2  Excel's chart settings on a clustered column, a file each: a title's text, no title, the
#       legend at each side and none, axis titles, data labels, the value axis's number format 0.0.
#   P4  PowerPoint inserts each type (Shapes.AddChart2), p4-<type>.pptx and its PDF; a chart whose
#       data is then set.
#   P5  Word's and PowerPoint's own data edits through ChartData: a value, a category added, a
#       series added, a series removed (and a category added to Word's pie), each saved by Word or
#       PowerPoint itself before and after, p5w-<case>-before/after.docx, p5p-...pptx.
#   P6  (phase 2) Excel's charts of other data: P2's settings on the other types, categories of
#       another kind (dates, text, numbers), other shapes (2 by 2, 1 by 1, eight series, eight
#       points), and plain ranges as Excel reads them (another sheet, wide, numbers, no header).
#
# Into -Dir (default X:\office_charts\cr036); a file already there is skipped, so a rerun resumes.
# Beside each workbook a .txt reports its chart's type and its series' formulas (what Select Data
# shows); Word's and PowerPoint's charts aren't asked (it can wake their data), their references
# are read from the files.  Each COM step is logged before it is made, so that a hang shows where.
# -Only p1,p3 runs only those parts.  Copyright 2026, Plutext Pty Ltd; Apache-2.0.
#
#   -Visible   show the applications, so a dialog (or a chart data window left open) can be seen
#   -Types column-clustered,pie   only those of the twelve types (P1, P3, P4)
param([string]$Dir = "X:\office_charts\cr036", [switch]$Visible, [string[]]$Only = @(), [string[]]$Types = @())
$ErrorActionPreference = "Stop"
# -Only p1,p2 reaches a script run with -File as one string: split lists on commas
$Only = @($Only | ForEach-Object { $_ -split ',' } | Where-Object { $_ })
$Types = @($Types | ForEach-Object { $_ -split ',' } | Where-Object { $_ })
Add-Type -AssemblyName System.IO.Compression.FileSystem
$tmp = Join-Path $env:TEMP "chart-authoring-probes"
New-Item -ItemType Directory -Force -Path $Dir, $tmp | Out-Null
function Say($s) { Write-Host ("{0:HH:mm:ss} {1}" -f (Get-Date), $s) }
# Each COM step logged before it is made, so that a hang shows where it is
function Step($s) { Write-Host ("{0:HH:mm:ss}   - {1}" -f (Get-Date), $s) }
function Want($part) { return ($Only.Count -eq 0) -or ($Only -contains $part) }
function Done($name) { return Test-Path (Join-Path $Dir $name) }

# The twelve types (CR-036 4.3), by Excel's XlChartType, which Word and PowerPoint share
$chartTypes = [ordered]@{
  "column-clustered" = 51; "column-stacked" = 52; "column-stacked-100" = 53
  "bar-clustered" = 57; "bar-stacked" = 58; "bar-stacked-100" = 59
  "line" = 4; "line-markers" = 65; "pie" = 5; "doughnut" = -4120; "area" = 1; "scatter" = -4169 }
if ($Types.Count -gt 0) {
  foreach ($k in @($chartTypes.Keys)) { if ($Types -notcontains $k) { $chartTypes.Remove($k) } }
}
$xlSrcRange = 1; $xlYes = 1; $xlCategory = 1; $xlValue = 2
$wdFormatDocumentDefault = 16; $wdExportFormatPDF = 17
$ppLayoutBlank = 12; $ppSaveAsDefault = 11; $ppFixedFormatTypePDF = 2; $msoTrue = -1

# Values written in one assignment (cell by cell, Windows PowerShell's COM binder caches the type
# of a property's first assignment and fails on the next)
function Fill($ws, $rows, $at = "A1") {
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
  $ws.Range($at).Resize($n, $m).Value2 = $arr
}
function Put($ws, $address, $value) {
  $arr = New-Object 'object[,]' 1, 1
  $arr[0, 0] = $value
  $ws.Range($address).Value2 = $arr
}

# Word's default data, in case an insert of Word's can't be opened (P1 then charts this)
$defaultData = @(@(" ", "Series 1", "Series 2", "Series 3"), @("Category 1", 4.3, 2.4, 2),
  @("Category 2", 2.5, 4.4, 2), @("Category 3", 3.5, 1.8, 3), @("Category 4", 4.5, 2.8, 5))
$pieData = @(@(" ", "Sales"), @("1st Qtr", 8.2), @("2nd Qtr", 3.2), @("3rd Qtr", 1.4), @("4th Qtr", 1.2))
# text series names: "2025" and "2026" read as numbers made SetSourceData take row 1 as data (Jason's run)
$ourData = @(@(" ", "Plan", "Actual"), @("North", 1.5, 2.5), @("South", 3.5, 4.5))

# A chart's series as Select Data shows them, and its type
function Report($chart, $file, $lines = @()) {
  if (-not $file.EndsWith(".xlsx")) { return }
  try {
    $lines += "chart type $($chart.ChartType)"
    for ($i = 1; $i -le $chart.FullSeriesCollection().Count; $i++) {
      $s = $chart.FullSeriesCollection($i)
      $f = try { $s.Formula } catch { "(no formula)" }
      $lines += "  series ${i}: $f"
    }
  } catch { $lines += "report failed: $($_.Exception.Message)" }
  $lines | Set-Content -Encoding UTF8 (Join-Path $Dir ([IO.Path]::ChangeExtension($file, ".txt")))
}

# A probe that fails is logged beside the others (<name>.failed.txt) and the run goes on
function Failed($name, $err) {
  Say "  FAILED $name : $($err.Exception.Message)"
  @("failed: $($err.Exception.Message)", $err.ScriptStackTrace) |
    Set-Content -Encoding UTF8 (Join-Path $Dir ([IO.Path]::ChangeExtension($name, ".failed.txt")))
}

# Word saved and closed as office-colours.ps1 (CR-034) does on this VM: SaveAs with each argument a
# [ref] to a plain [string] or [int] (a PowerShell-wrapped value, or SaveAs2 or ExportAsFixedFormat,
# stalls Word there), and Close with a [ref]
function WordSave($doc, $path, [int]$format) {
  [string]$p = ([string]$path).ToString()
  [int]$f = $format
  $doc.SaveAs([ref]$p, [ref]$f)
}
function WordClose($doc) { [int]$dontSave = 0; $doc.Close([ref]$dontSave) }

# A PDF of a document (SaveAs format 17) or a presentation (SaveAs format 32), as office-colours.ps1
# makes them; a failure noted, not fatal
function Pdf($office, $local, [switch]$Word) {
  $pdf = [IO.Path]::ChangeExtension($local, ".pdf")
  Step "SaveAs PDF"
  try {
    if ($Word) { WordSave $office $pdf 17 }
    else { [string]$p = $pdf.ToString(); $office.SaveAs($p, 32) }
    Copy-Out $pdf
  } catch { Say "  (no PDF of $([IO.Path]::GetFileName($local)): $($_.Exception.Message))" }
}

# A probe starting afresh: its failure note from an earlier run removed
function Fresh($name) {
  $note = Join-Path $Dir ([IO.Path]::ChangeExtension($name, ".failed.txt"))
  if (Test-Path $note) { Remove-Item -Force $note }
}

function Copy-Out($local) { Copy-Item -Force $local $Dir; Say "  wrote $([IO.Path]::GetFileName($local))" }

# Word's and PowerPoint's AddChart2 leave the chart's data open in an Excel window ("Chart in
# Microsoft Word"), which is closed as a user closes it, by its title bar's close (WM_CLOSE): from a
# script, Workbook.Close() and Close(True) returned and left it open.  (Word's hangs in Jason's runs,
# 2026-10-12, were SaveAs2 itself, which hung on a document with no chart too: see WordSave.)
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

# A chart redrawn from its data before it is saved: PowerPoint saved a scripted edit's chart with its
# references grown but no caches (Jason's run, 2026-10-12); Word had redrawn
function Redraw($chart) {
  if (-not $chart) { return }
  Step "Chart.Refresh"
  try { $chart.Refresh() } catch { Step "  (Refresh: $($_.Exception.Message))" }
  Start-Sleep -Seconds 1
}

function CloseChartData($chart) {
  Start-Sleep -Seconds 2
  $n = [DataWindows]::Close("Chart in Microsoft")
  Step "closed $n chart data window(s)"
  Start-Sleep -Seconds 2
  if ([DataWindows]::Close("Chart in Microsoft") -gt 0) { Step "closed another"; Start-Sleep -Seconds 2 }
}

# ChartData.Workbook, once Excel has it open (straight after Activate it can still be null)
function DataWorkbook($chart) {
  for ($i = 0; $i -lt 20; $i++) {
    $wb = $chart.ChartData.Workbook
    if ($wb) { return $wb }
    Start-Sleep -Milliseconds 500
  }
  throw "the chart's data workbook didn't open within 10 seconds"
}

# A chart's data workbook opened in Excel, so that cells can be written (Workbook needs Activate first)
function DataSheet($chart) {
  Step "ChartData.Activate"
  $chart.ChartData.Activate()
  Step "ChartData.Workbook"
  $wb = DataWorkbook $chart
  # its sheet can be missing for a moment after the workbook appears (DISP_E_BADINDEX, Jason's run)
  Step "its first worksheet"
  for ($i = 0; $i -lt 20; $i++) {
    try { $ws = $wb.Worksheets.Item(1); if ($ws) { return @($wb, $ws) } } catch { }
    try { $ws = $wb.ActiveSheet; if ($ws) { return @($wb, $ws) } } catch { }
    Start-Sleep -Milliseconds 500
  }
  throw "the chart's data worksheet didn't open within 10 seconds"
}

# Grows (or shrinks) the data's table to the address, where Excel didn't as cells were typed
function Table($ws, $address) {
  $lo = $ws.ListObjects.Item(1)
  if ($lo.Range.Address($false, $false) -ne $address) { $lo.Resize($ws.Range($address)) }
  return $lo.Range.Address($false, $false)
}

$excel = $null; $word = $null; $ppt = $null
try {
  # ------------------------------------------------------------------ P3: Word
  if (Want "p3") {
    $word = New-Object -ComObject Word.Application
    $word.Visible = [bool]$Visible
    $word.DisplayAlerts = 0
    $word.AutomationSecurity = 3   # msoAutomationSecurityForceDisable, as office-colours.ps1
    function WordDoc($name, [scriptblock]$body) {
      if (Done $name) { Say "$name is there already"; return }
      Say "P3 $name"; Fresh $name
      Step "Documents.Add"
      $doc = $word.Documents.Add()
      try {
        $chart = & $body $doc
        $local = Join-Path $tmp $name
        if (Test-Path $local) { Remove-Item -Force $local }
        Step "SaveAs"
        WordSave $doc $local $wdFormatDocumentDefault
        Step "saved"
        Copy-Out $local
        Pdf $doc $local -Word
        if ($chart) { Report $chart $name }
      } catch { Failed $name $_ } finally { try { WordClose $doc } catch { } }
    }
    # a control: Word saving a document with no chart, so a hang at a save can be told from the chart's
    WordDoc "p3-no-chart.docx" { param($doc) $doc.Range().Text = "No chart."; $null }
    foreach ($t in $chartTypes.Keys) {
      WordDoc "p3-$t.docx" { param($doc)
        Step "InlineShapes.AddChart2"
        $ic = $doc.InlineShapes.AddChart2(-1, $chartTypes[$t], $doc.Range(0, 0))
        CloseChartData $ic.Chart
        $ic.Chart }
    }
    # two charts in one document: how the second's parts are named
    WordDoc "p3-two-charts.docx" { param($doc)
      Step "InlineShapes.AddChart2 (first)"
      $a = $doc.InlineShapes.AddChart2(-1, 51, $doc.Range(0, 0))
      CloseChartData $a.Chart
      $doc.Range().InsertParagraphAfter()
      $end = $doc.Range($doc.Content.End - 1, $doc.Content.End - 1)
      Step "InlineShapes.AddChart2 (second)"
      $b = $doc.InlineShapes.AddChart2(-1, 4, $end)
      CloseChartData $b.Chart
      $b.Chart }
    # a chart whose data is then set (phase 3's oracle for ChartCreator with the same data)
    WordDoc "p3-data-set.docx" { param($doc)
      Step "InlineShapes.AddChart2"
      $ic = $doc.InlineShapes.AddChart2(-1, 51, $doc.Range(0, 0))
      CloseChartData $ic.Chart
      $wb, $ws = DataSheet $ic.Chart
      # the table shrunk first, then the cells outside it cleared (clearing the whole table first left
      # no table: DISP_E_BADINDEX, Jason's run), then the data written
      Step "the table to A1:C3"
      [void](Table $ws "A1:C3")
      $ws.Range("D1:D5").ClearContents(); $ws.Range("A4:C5").ClearContents()
      Fill $ws $ourData
      Step "SetSourceData"
      $ic.Chart.SetSourceData("Sheet1!A1:C3")
      Step "Workbook.Close(True)"
      $wb.Close($true); CloseChartData $ic.Chart; Redraw $ic.Chart
      $ic.Chart }
  }

  # ------------------------------------------------------------------ P1 and P2: Excel
  if ((Want "p1") -or (Want "p2") -or (Want "p6")) {
    $excel = New-Object -ComObject Excel.Application
    $excel.Visible = [bool]$Visible
    $excel.DisplayAlerts = $false
    function SaveWb($wb, $name, $chart) {
      $local = Join-Path $tmp $name
      if (Test-Path $local) { Remove-Item -Force $local }
      $wb.SaveCopyAs($local)
      Copy-Out $local
      if ($chart) {
        $png = [IO.Path]::ChangeExtension($local, ".png")
        [void]$chart.Export($png)
        Copy-Out $png
        Report $chart $name
      }
    }
    # Word's embedded workbook from p3-<type>.docx, to chart the same data
    function WordsData($t) {
      $docx = Join-Path $Dir "p3-$t.docx"
      if (-not (Test-Path $docx)) { return $null }
      $zip = [IO.Compression.ZipFile]::OpenRead($docx)
      try {
        $e = $zip.Entries | Where-Object { $_.FullName -like "word/embeddings/*.xlsx" } | Select-Object -First 1
        if (-not $e) { return $null }
        $dest = Join-Path $tmp "words-$t.xlsx"
        [IO.Compression.ZipFileExtensions]::ExtractToFile($e, $dest, $true)
        return $dest
      } finally { $zip.Dispose() }
    }
    function Workbook($t) {
      $words = try { WordsData $t } catch { $null }
      if ($words) { return $excel.Workbooks.Open($words) }
      Say "  (no p3-$t.docx: Word's default data written)"
      $wb = $excel.Workbooks.Add()
      $ws = $wb.Worksheets.Item(1)
      $data = if ($t -eq "pie" -or $t -eq "doughnut") { $pieData } else { $defaultData }
      Fill $ws $data
      $lo = $ws.ListObjects.Add($xlSrcRange, $ws.UsedRange, $null, $xlYes)
      $lo.Name = "Table1"
      return $wb
    }
    function Chart($wb, $type) {
      $ws = $wb.Worksheets.Item(1)
      # by its address: a Range assigned from an expression is unrolled into its cells (an Object[])
      $address = if ($ws.ListObjects.Count -gt 0) { $ws.ListObjects.Item(1).Range.Address($true, $true) }
                 else { $ws.UsedRange.Address($true, $true) }
      $shape = $ws.Shapes.AddChart2(-1, $type, 300, 20, 432, 252)
      $shape.Chart.SetSourceData($ws.Range($address))
      return $shape.Chart
    }
    if (Want "p1") {
      foreach ($t in $chartTypes.Keys) {
        $name = "p1-$t.xlsx"
        if (Done $name) { Say "$name is there already"; continue }
        Say "P1 $name"; Fresh $name
        $wb = $null
        try { $wb = Workbook $t; SaveWb $wb $name (Chart $wb $chartTypes[$t]) }
        catch { Failed $name $_ } finally { if ($wb) { try { $wb.Close($false) } catch { } } }
      }
      # a date axis: a "Date" header over dates (d-mmm)
      $name = "p1-date-axis.xlsx"
      if (-not (Done $name)) {
        Say "P1 $name"; Fresh $name
        $wb = $excel.Workbooks.Add()
        try {
          $ws = $wb.Worksheets.Item(1)
          Fill $ws @(@("Date", "Series 1", "Series 2", "Series 3"), @(46023, 4.3, 2.4, 2), @(46024, 2.5, 4.4, 2),
            @(46025, 3.5, 1.8, 3), @(46026, 4.5, 2.8, 5))
          $ws.Range("A2:A5").NumberFormat = "d-mmm"
          $lo = $ws.ListObjects.Add($xlSrcRange, $ws.Range("A1:D5"), $null, $xlYes); $lo.Name = "Table1"
          SaveWb $wb $name (Chart $wb 51)
        } catch { Failed $name $_ } finally { try { $wb.Close($false) } catch { } }
      }
    }
    if (Want "p2") {
      $settings = [ordered]@{
        "title-text" = { param($c) $c.HasTitle = $true; $c.ChartTitle.Text = "Sales" }
        "no-title" = { param($c) $c.HasTitle = $false }
        "legend-bottom" = { param($c) $c.HasLegend = $true; $c.Legend.Position = -4107 }
        "legend-top" = { param($c) $c.HasLegend = $true; $c.Legend.Position = -4160 }
        "legend-left" = { param($c) $c.HasLegend = $true; $c.Legend.Position = -4131 }
        "legend-right" = { param($c) $c.HasLegend = $true; $c.Legend.Position = -4152 }
        "no-legend" = { param($c) $c.HasLegend = $false }
        "axis-titles" = { param($c)
          $c.Axes($xlCategory).HasTitle = $true; $c.Axes($xlCategory).AxisTitle.Text = "Quarter"
          $c.Axes($xlValue).HasTitle = $true; $c.Axes($xlValue).AxisTitle.Text = "Units" }
        "data-labels" = { param($c) $c.ApplyDataLabels() }
        "number-format" = { param($c) $c.Axes($xlValue).TickLabels.NumberFormat = "0.0" } }
      foreach ($k in $settings.Keys) {
        $name = "p2-$k.xlsx"
        if (Done $name) { Say "$name is there already"; continue }
        Say "P2 $name"; Fresh $name
        $wb = $null
        try {
          $wb = Workbook "column-clustered"
          $c = Chart $wb 51
          & $settings[$k] $c
          SaveWb $wb $name $c
        } catch { Failed $name $_ } finally { if ($wb) { try { $wb.Close($false) } catch { } } }
      }
    }

    # ------------------------------------------------------------------ P6: Excel, for phase 2
    # What ChartCreator needs beyond P1 and P2 (CR-036 phase 2), each a new workbook of its own data
    # in a table (Table1), charted by AddChart2 over it as P1 is:
    #   p6-<type>-<setting>.xlsx   P2's settings on the other eleven types (no axes for pie, doughnut)
    #   p6-<type>-dates.xlsx       a "Date" header over d-mmm dates, for each type (P1 has the column's)
    #   p6-area-text.xlsx, p6-scatter-text.xlsx  text categories, where the type's template has others
    #   p6-<type>-numbers.xlsx     numbers as categories under a " " corner
    #   p6-<type>-two-by-two.xlsx, -one-by-one, -eight-series, -eight-points   other shapes of data
    #   p6-range-<case>.xlsx       a chart over a plain range (no table) with Excel's own reading of
    #                              it (SetSourceData with no PlotBy): on another sheet, wider than
    #                              tall, numbers in the first column, no header row
    if (Want "p6") {
      # a new workbook, its first sheet holding the rows at A1, as a table unless -NoTable
      function NewBook($rows, $format = $null, [switch]$NoTable) {
        $wb = $excel.Workbooks.Add()
        $ws = $wb.Worksheets.Item(1)
        Fill $ws $rows
        if ($format) { $ws.Range("A2").Resize($rows.Count - 1, 1).NumberFormat = $format }
        if (-not $NoTable) {
          $lo = $ws.ListObjects.Add($xlSrcRange, $ws.Range("A1").Resize($rows.Count, $rows[0].Count), $null, $xlYes)
          $lo.Name = "Table1"
        }
        return $wb
      }
      function Probe($name, [scriptblock]$body) {
        if (Done $name) { Say "$name is there already"; return }
        Say "P6 $name"; Fresh $name
        $wb = $null
        # the body's last two outputs: the workbook and its chart (anything a COM call emits before them aside)
        try { $r = @(& $body); $wb = $r[-2]; SaveWb $wb $name $r[-1] }
        catch { Failed $name $_ } finally { if ($wb) { try { $wb.Close($false) } catch { } } }
      }
      $dates = @(@("Date", "Series 1", "Series 2", "Series 3"), @(46023, 4.3, 2.4, 2), @(46024, 2.5, 4.4, 2),
        @(46025, 3.5, 1.8, 3), @(46026, 4.5, 2.8, 5))
      $numbers = @(@(" ", "Series 1", "Series 2"), @(2021, 4.3, 2.4), @(2022, 2.5, 4.4), @(2023, 3.5, 1.8),
        @(2024, 4.5, 2.8))
      $xy = @(@("X", "Plan", "Actual"), @(1.5, 2, 3), @(3.5, 4, 5))
      $one = @(@(" ", "Plan"), @("North", 1.5))
      $eightSeries = @(@(" ", "S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8"), @("A", 1, 2, 3, 4, 5, 6, 7, 8),
        @("B", 2, 3, 4, 5, 6, 7, 8, 9), @("C", 3, 4, 5, 6, 7, 8, 9, 10))
      $eightPoints = @(@(" ", "Sales"), @("P1", 8), @("P2", 7), @("P3", 6), @("P4", 5), @("P5", 4), @("P6", 3),
        @("P7", 2), @("P8", 1))
      function DataFor($t) { if ($t -eq "pie" -or $t -eq "doughnut") { $pieData } else { $defaultData } }

      $p6settings = [ordered]@{
        "title-text" = { param($c) $c.HasTitle = $true; $c.ChartTitle.Text = "Sales" }
        "axis-titles" = { param($c)
          $c.Axes($xlCategory).HasTitle = $true; $c.Axes($xlCategory).AxisTitle.Text = "Quarter"
          $c.Axes($xlValue).HasTitle = $true; $c.Axes($xlValue).AxisTitle.Text = "Units" }
        "data-labels" = { param($c) $c.ApplyDataLabels() }
        "number-format" = { param($c) $c.Axes($xlValue).TickLabels.NumberFormat = "0.0" } }
      foreach ($t in $chartTypes.Keys) {
        if ($t -eq "column-clustered") { continue }
        foreach ($k in $p6settings.Keys) {
          if (($t -eq "pie" -or $t -eq "doughnut") -and ($k -eq "axis-titles" -or $k -eq "number-format")) { continue }
          Probe "p6-$t-$k.xlsx" { $wb = NewBook (DataFor $t); $c = Chart $wb $chartTypes[$t]; [void](& $p6settings[$k] $c); $wb, $c }
        }
      }
      foreach ($t in $chartTypes.Keys) {
        if ($t -ne "column-clustered") {
          Probe "p6-$t-dates.xlsx" { $wb = NewBook $dates "d-mmm"; $wb, (Chart $wb $chartTypes[$t]) }
        }
      }
      Probe "p6-area-text.xlsx" { $wb = NewBook $defaultData; $wb, (Chart $wb $chartTypes["area"]) }
      Probe "p6-scatter-text.xlsx" { $wb = NewBook $ourData; $wb, (Chart $wb $chartTypes["scatter"]) }
      foreach ($t in @("column-clustered", "line", "pie")) {
        Probe "p6-$t-numbers.xlsx" { $wb = NewBook $numbers; $wb, (Chart $wb $chartTypes[$t]) }
      }
      foreach ($t in $chartTypes.Keys) {
        $d = if ($t -eq "scatter") { $xy } else { $ourData }
        Probe "p6-$t-two-by-two.xlsx" { $wb = NewBook $d; $wb, (Chart $wb $chartTypes[$t]) }
      }
      foreach ($t in @("column-clustered", "line-markers", "pie", "scatter")) {
        Probe "p6-$t-one-by-one.xlsx" { $wb = NewBook $one; $wb, (Chart $wb $chartTypes[$t]) }
      }
      foreach ($t in @("column-clustered", "line-markers", "area")) {
        Probe "p6-$t-eight-series.xlsx" { $wb = NewBook $eightSeries; $wb, (Chart $wb $chartTypes[$t]) }
      }
      foreach ($t in @("pie", "doughnut")) {
        Probe "p6-$t-eight-points.xlsx" { $wb = NewBook $eightPoints; $wb, (Chart $wb $chartTypes[$t]) }
      }

      # plain ranges, Excel's own reading: AddChart2 then SetSourceData with no PlotBy
      function RangeChart($wb, $ws, $range, $type = 51) {
        $shape = $wb.Worksheets.Item(1).Shapes.AddChart2(-1, $type, 300, 20, 432, 252)
        $shape.Chart.SetSourceData($ws.Range($range))
        return $shape.Chart
      }
      Probe "p6-range-other-sheet.xlsx" {
        $wb = $excel.Workbooks.Add()
        $data = $wb.Worksheets.Add([Type]::Missing, $wb.Worksheets.Item(1))
        $data.Name = "My Data"
        $rows = @(@("", "Series 1", "Series 2", "Series 3")) + $defaultData[1..4]
        Fill $data $rows "B3"
        $wb, (RangeChart $wb $data "B3:E7") }
      Probe "p6-range-wide.xlsx" {
        $wb = NewBook @(@("", "Q1", "Q2", "Q3", "Q4"), @("North", 1, 2, 3, 4), @("South", 5, 6, 7, 8)) -NoTable
        $ws = $wb.Worksheets.Item(1); $wb, (RangeChart $wb $ws "A1:E3") }
      Probe "p6-range-numbers.xlsx" {
        $wb = NewBook @(@("", "Series 1", "Series 2"), @(2021, 4.3, 2.4), @(2022, 2.5, 4.4), @(2023, 3.5, 1.8)) -NoTable
        $ws = $wb.Worksheets.Item(1); $wb, (RangeChart $wb $ws "A1:C4") }
      Probe "p6-range-numbers-header.xlsx" {
        $wb = NewBook @(@("Year", "Series 1", "Series 2"), @(2021, 4.3, 2.4), @(2022, 2.5, 4.4), @(2023, 3.5, 1.8)) -NoTable
        $ws = $wb.Worksheets.Item(1); $wb, (RangeChart $wb $ws "A1:C4") }
      Probe "p6-range-no-header.xlsx" {
        $wb = NewBook @(@(4.3, 2.4), @(2.5, 4.4), @(3.5, 1.8), @(4.5, 2.8)) -NoTable
        $ws = $wb.Worksheets.Item(1); $wb, (RangeChart $wb $ws "A1:B4") }
      Probe "p6-range-text-corner.xlsx" {
        $wb = NewBook @(@("Region", "Plan", "Actual"), @("North", 1.5, 2.5), @("South", 3.5, 4.5), @("East", 2, 3)) -NoTable
        $ws = $wb.Worksheets.Item(1); $wb, (RangeChart $wb $ws "A1:C4") }
    }
  }

  # ------------------------------------------------------------------ P4: PowerPoint
  if (Want "p4") {
    $ppt = New-Object -ComObject PowerPoint.Application
    function PptDeck($name, [scriptblock]$body) {
      if (Done $name) { Say "$name is there already"; return }
      Say "P4 $name"; Fresh $name
      Step "Presentations.Add"
      $pres = $ppt.Presentations.Add($msoTrue)
      try {
        $slide = $pres.Slides.Add(1, $ppLayoutBlank)
        $chart = & $body $slide
        $local = Join-Path $tmp $name
        if (Test-Path $local) { Remove-Item -Force $local }
        Step "SaveAs"
        [string]$lp = $local.ToString(); $pres.SaveAs($lp, $ppSaveAsDefault)
        Copy-Out $local
        Pdf $pres $local
        if ($chart) { Report $chart $name }
      } catch { Failed $name $_ } finally { try { $pres.Close() } catch { } }
    }
    foreach ($t in $chartTypes.Keys) {
      PptDeck "p4-$t.pptx" { param($slide)
        Step "Shapes.AddChart2"
        $sh = $slide.Shapes.AddChart2(-1, $chartTypes[$t], 100, 100, 432, 252)
        CloseChartData $sh.Chart
        $sh.Chart }
    }
    PptDeck "p4-data-set.pptx" { param($slide)
      Step "Shapes.AddChart2"
      $sh = $slide.Shapes.AddChart2(-1, 51, 100, 100, 432, 252)
      CloseChartData $sh.Chart
      $wb, $ws = DataSheet $sh.Chart
      Step "the table to A1:C3"
      [void](Table $ws "A1:C3")
      $ws.Range("D1:D5").ClearContents(); $ws.Range("A4:C5").ClearContents()
      Fill $ws $ourData
      Step "SetSourceData"
      $sh.Chart.SetSourceData("Sheet1!A1:C3")
      Step "Workbook.Close(True)"
      $wb.Close($true); CloseChartData $sh.Chart; Redraw $sh.Chart
      $sh.Chart }
  }

  # ------------------------------------------------------------------ P5: Word's and PowerPoint's own edits
  if (Want "p5") {
    if (-not $word) {
      $word = New-Object -ComObject Word.Application; $word.Visible = [bool]$Visible
      $word.DisplayAlerts = 0; $word.AutomationSecurity = 3
    }
    if (-not $ppt) { $ppt = New-Object -ComObject PowerPoint.Application }
    $edits = [ordered]@{
      "value" = @(51, { param($ws) Put $ws "C3" 7.5 })
      "add-category" = @(51, { param($ws)
        Put $ws "A6" "Category 5"; Put $ws "B6" 3.1; Put $ws "C6" 2.2; Put $ws "D6" 4
        [void](Table $ws "A1:D6") })
      "add-series" = @(51, { param($ws)
        Put $ws "E1" "Series 4"; Put $ws "E2" 1.5; Put $ws "E3" 2.5; Put $ws "E4" 3.5; Put $ws "E5" 4.5
        [void](Table $ws "A1:E5") })
      "remove-series" = @(51, { param($ws) [void](Table $ws "A1:C5"); $ws.Range("D1:D5").ClearContents() })
      "pie-add-category" = @(5, { param($ws) Put $ws "A6" "5th Qtr"; Put $ws "B6" 2.6; [void](Table $ws "A1:B6") }) }
    foreach ($k in $edits.Keys) {
      $type = $edits[$k][0]; $edit = $edits[$k][1]
      # Word: saved by Word before and after the edit
      $before = "p5w-$k-before.docx"; $after = "p5w-$k-after.docx"
      if (-not (Done $after)) {
        Say "P5 Word $k"; Fresh $after
        $doc = $word.Documents.Add()
        try {
          Step "InlineShapes.AddChart2"
          $ic = $doc.InlineShapes.AddChart2(-1, $type, $doc.Range(0, 0))
          CloseChartData $ic.Chart
          $l1 = Join-Path $tmp $before; if (Test-Path $l1) { Remove-Item -Force $l1 }
          Step "SaveAs (before)"
          WordSave $doc $l1 $wdFormatDocumentDefault; Copy-Out $l1
          $wb, $ws = DataSheet $ic.Chart
          Step "the edit"
          & $edit $ws
          # the chart catches up with the cells a moment later: closed at once, Word and PowerPoint
          # saved a cache without the last cell typed (D6), while the workbook had it (Jason's run)
          Start-Sleep -Seconds 3
          Step "Workbook.Close(True)"
          $wb.Close($true); CloseChartData $null; Redraw $ic.Chart
          $l2 = Join-Path $tmp $after; if (Test-Path $l2) { Remove-Item -Force $l2 }
          Step "SaveAs (after)"
          WordSave $doc $l2 $wdFormatDocumentDefault; Copy-Out $l2
          Pdf $doc $l2 -Word
          Report $ic.Chart $after
        } catch { Failed $after $_ } finally { try { WordClose $doc } catch { } }
      }
      if ($k -eq "pie-add-category") { continue }
      # PowerPoint: the same, saved by PowerPoint
      $before = "p5p-$k-before.pptx"; $after = "p5p-$k-after.pptx"
      if (-not (Done $after)) {
        Say "P5 PowerPoint $k"; Fresh $after
        $pres = $ppt.Presentations.Add($msoTrue)
        try {
          $slide = $pres.Slides.Add(1, $ppLayoutBlank)
          Step "Shapes.AddChart2"
          $sh = $slide.Shapes.AddChart2(-1, $type, 100, 100, 432, 252)
          CloseChartData $sh.Chart
          $l1 = Join-Path $tmp $before; if (Test-Path $l1) { Remove-Item -Force $l1 }
          [string]$lp = $l1.ToString(); $pres.SaveAs($lp, $ppSaveAsDefault); Copy-Out $l1
          $wb, $ws = DataSheet $sh.Chart
          Step "the edit"
          & $edit $ws
          # the chart catches up with the cells a moment later: closed at once, Word and PowerPoint
          # saved a cache without the last cell typed (D6), while the workbook had it (Jason's run)
          Start-Sleep -Seconds 3
          Step "Workbook.Close(True)"
          $wb.Close($true); CloseChartData $null; Redraw $sh.Chart
          $l2 = Join-Path $tmp $after; if (Test-Path $l2) { Remove-Item -Force $l2 }
          [string]$lp = $l2.ToString(); $pres.SaveAs($lp, $ppSaveAsDefault); Copy-Out $l2
          Pdf $pres $l2
          Report $sh.Chart $after
        } catch { Failed $after $_ } finally { try { $pres.Close() } catch { } }
      }
    }
  }
} finally {
  foreach ($app in @($excel, $word, $ppt)) {
    if ($app) {
      try { $app.Quit() } catch { }
      [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($app)
    }
  }
}
Say "done"
