# CR-037 phase 0: Office's drawings of the render probes, as numbers.  One run on the Windows VM
# drives Excel, Word and PowerPoint (365) over the probes ChartRenderProbes made (docx4j's charts,
# each an xlsx and a docx, R1's a pptx too; probes.tsv lists them):
#
#   excel\<name>.json        Excel's layout of the chart (its object model, in points of the chart
#                            area): the chart area, the plot area and its inside, the title, the
#                            legend and its entries, each axis's bounds, unit, labels' orientation,
#                            spacing, format and font, each series' points (and a pie's slice
#                            locations) and data labels; the chart first sized to the probe's size.
#                            A second dump after another refresh goes to <name>.2.json only where
#                            it differs (the layout not settled).
#   excel\<name>.png         Chart.Export's picture
#   word\<name>.pdf          Word's PDF of the docx (SaveAs format 17)
#   word\<name>.json         Word's layout of its chart, the same dump (-NoAppLayout skips it)
#   powerpoint\<name>.pdf    PowerPoint's PDF of the pptx (R1), and powerpoint\<name>.json
#   R10 (style in probes.tsv): Excel's gallery style applied (Chart.ChartStyle) by Excel and by Word
#   before the drawing; the styled files saved as excel\<name>-styled.xlsx, word\<name>-styled.docx.
#
# -Dir is the probes' folder's parent (default X:\office_charts\cr037, the probes in its probes\);
# a probe whose outputs are there already is skipped, so a rerun resumes.  A probe that fails is
# noted beside the others (<name>.failed.txt) and the run goes on.  Each COM step is logged before
# it is made, so that a hang shows where.  Copyright 2026, Plutext Pty Ltd; Apache-2.0.
#
#   -Only r1,r2      only those sets          -Names r1-pie,r2-log   only those probes
#   -Apps excel,word only those applications (excel, word, powerpoint)
#   -Hidden          Excel hidden (by default it is shown, so that its charts are laid out and drawn);
#   -Visible         Word and PowerPoint shown (hidden by default)
#   -NoAppLayout     no layout dump from Word's or PowerPoint's chart (if reading it hangs)
param([string]$Dir = "X:\office_charts\cr037", [string[]]$Only = @(), [string[]]$Names = @(), [string[]]$Apps = @(),
  [switch]$Hidden, [switch]$Visible, [switch]$NoAppLayout)
$ErrorActionPreference = "Stop"
# lists reach a script run with -File as one string: split them on commas
$Only = @($Only | ForEach-Object { $_ -split ',' } | Where-Object { $_ })
$Names = @($Names | ForEach-Object { $_ -split ',' } | Where-Object { $_ })
$Apps = @($Apps | ForEach-Object { $_ -split ',' } | Where-Object { $_ })
Add-Type -AssemblyName System.Drawing
$probesDir = Join-Path $Dir "probes"
$tmp = Join-Path $env:TEMP "chart-render-probes"
$out = @{ excel = (Join-Path $Dir "excel"); word = (Join-Path $Dir "word"); powerpoint = (Join-Path $Dir "powerpoint") }
New-Item -ItemType Directory -Force -Path $tmp, $out.excel, $out.word, $out.powerpoint | Out-Null
function Say($s) { Write-Host ("{0:HH:mm:ss} {1}" -f (Get-Date), $s) }
function Step($s) { Write-Host ("{0:HH:mm:ss}   - {1}" -f (Get-Date), $s) }
function WantApp($a) { return ($Apps.Count -eq 0) -or ($Apps -contains $a) }

$probes = @(Import-Csv -Delimiter "`t" (Join-Path $probesDir "probes.tsv") | Where-Object {
  (($Only.Count -eq 0) -or ($Only -contains $_.set.ToLower())) -and (($Names.Count -eq 0) -or ($Names -contains $_.name)) })
Say "$($probes.Count) probes"

function Failed($folder, $name, $err) {
  Say "  FAILED $name : $($err.Exception.Message)"
  @("failed: $($err.Exception.Message)", $err.ScriptStackTrace) |
    Set-Content -Encoding UTF8 (Join-Path $folder "$name.failed.txt")
}
function Fresh($folder, $name) {
  $note = Join-Path $folder "$name.failed.txt"
  if (Test-Path $note) { Remove-Item -Force $note }
}
function Local($file) {
  $l = Join-Path $tmp ([IO.Path]::GetFileName($file))
  Copy-Item -Force $file $l
  return $l
}

# Word's and PowerPoint's chart data window, if a style change opens it, closed by its title bar
# (WM_CLOSE), as office-chart-authoring-probes.ps1 does
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
function CloseChartData { Start-Sleep -Milliseconds 500; [void][DataWindows]::Close("Chart in Microsoft") }

# ------------------------------------------------------------------ the layout dump

# A value, or null where the object model refuses it (a category axis has no MinimumScale, a
# blank point may have no geometry, Point.Left needs Excel 2013 ...).  Not for a collection: the
# pipeline would unroll it into a 0-based array (Item(1) then the second); those are assigned.
# (Its parameter is not $s: a script block's $s would be V's own, not the caller's series.)
function V([scriptblock]$__block) {
  try { $v = & $__block } catch { return $null }
  if ($v -is [single] -or $v -is [decimal]) { return [double]$v }
  return $v
}
function Box($o) {
  if ($null -eq $o) { return $null }
  [ordered]@{ left = V { $o.Left }; top = V { $o.Top }; width = V { $o.Width }; height = V { $o.Height } }
}
# Office's RGB (R + 256 G + 65536 B) as RRGGBB
function Hex($v) {
  if ($null -eq $v) { return $null }
  $i = [int]$v
  return "{0:X2}{1:X2}{2:X2}" -f ($i -band 0xFF), (($i -shr 8) -band 0xFF), (($i -shr 16) -band 0xFF)
}
function Font($o) {
  [ordered]@{ size = V { $o.Format.TextFrame2.TextRange.Font.Size }; name = V { $o.Format.TextFrame2.TextRange.Font.Name }
    size2 = V { $o.Font.Size } }
}
function Label($d) {
  if ($null -eq $d) { return $null }
  $b = Box $d
  $b.text = V { $d.Text }
  $b.position = V { $d.Position }
  $b.font = Font $d
  return $b
}

function Dump($chart, $app, $probe, $chartObject) {
  $d = [ordered]@{ probe = $probe.name; app = $app; size = @([double]$probe.width, [double]$probe.height) }
  $d.chartType = V { $chart.ChartType }
  $d.chartStyle = V { $chart.ChartStyle }
  $d.displayBlanksAs = V { $chart.DisplayBlanksAs }
  if ($chartObject) { $d.chartObject = Box $chartObject }
  $d.chartArea = Box $chart.ChartArea
  $pa = $chart.PlotArea
  $d.plotArea = Box $pa
  $d.plotArea.insideLeft = V { $pa.InsideLeft }; $d.plotArea.insideTop = V { $pa.InsideTop }
  $d.plotArea.insideWidth = V { $pa.InsideWidth }; $d.plotArea.insideHeight = V { $pa.InsideHeight }
  $d.hasTitle = V { $chart.HasTitle }
  if ($d.hasTitle) {
    $t = $chart.ChartTitle
    $d.title = Box $t
    $d.title.text = V { $t.Text }
    $d.title.orientation = V { $t.Orientation }
    $d.title.font = Font $t
  }
  $d.hasLegend = V { $chart.HasLegend }
  if ($d.hasLegend) {
    $l = $chart.Legend
    $d.legend = Box $l
    $d.legend.position = V { $l.Position }
    $d.legend.font = Font $l
    $entries = @()
    $le = $null; try { $le = $l.LegendEntries() } catch { }
    if ($null -ne $le) {
      for ($i = 1; $i -le $le.Count; $i++) {
        $e = $le.Item($i)
        $b = Box $e
        $b.key = Box (V { $e.LegendKey })
        $entries += $b
      }
    }
    $d.legend.entries = $entries
  }
  $axes = @()
  foreach ($group in 1, 2) {
    foreach ($type in 1, 2, 3) {
      $a = V { $chart.Axes($type, $group) }
      if ($null -eq $a) { continue }
      $x = [ordered]@{ type = $type; group = $group }
      $b = Box $a
      foreach ($k in $b.Keys) { $x[$k] = $b[$k] }
      foreach ($p in "MinimumScale", "MaximumScale", "MajorUnit", "MinorUnit", "MinimumScaleIsAuto", "MaximumScaleIsAuto",
          "MajorUnitIsAuto", "ScaleType", "LogBase", "Crosses", "CrossesAt", "ReversePlotOrder", "AxisBetweenCategories",
          "TickLabelSpacing", "TickLabelSpacingIsAuto", "TickMarkSpacing", "CategoryType", "BaseUnit", "MajorTickMark",
          "TickLabelPosition", "HasMajorGridlines", "HasMinorGridlines", "DisplayUnit") {
        $x[$p.Substring(0, 1).ToLower() + $p.Substring(1)] = V { $a.$p }
      }
      $tl = V { $a.TickLabels }
      if ($tl) {
        $x.tickLabels = [ordered]@{ orientation = V { $tl.Orientation }; offset = V { $tl.Offset }; numberFormat = V { $tl.NumberFormat }
          numberFormatLinked = V { $tl.NumberFormatLinked }; fontSize = V { $tl.Font.Size }; fontName = V { $tl.Font.Name } }
      }
      $x.hasTitle = V { $a.HasTitle }
      if ($x.hasTitle) {
        $at = $a.AxisTitle
        $x.title = Box $at
        $x.title.text = V { $at.Text }
        $x.title.orientation = V { $at.Orientation }
        $x.title.font = Font $at
      }
      $axes += $x
    }
  }
  $d.axes = $axes
  $series = @()
  $sc = $null; try { $sc = $chart.SeriesCollection() } catch { }
  $n = if ($null -ne $sc) { $sc.Count } else { 0 }
  for ($i = 1; $i -le $n; $i++) {
    $s = $sc.Item($i)
    $x = [ordered]@{ index = $i; name = V { $s.Name }; chartType = V { $s.ChartType }; axisGroup = V { $s.AxisGroup }
      plotOrder = V { $s.PlotOrder }; fill = Hex (V { $s.Format.Fill.ForeColor.RGB }); line = Hex (V { $s.Format.Line.ForeColor.RGB })
      lineWeight = V { $s.Format.Line.Weight }; lineVisible = V { $s.Format.Line.Visible }; markerStyle = V { $s.MarkerStyle }
      markerSize = V { $s.MarkerSize }; explosion = V { $s.Explosion }; hasDataLabels = V { $s.HasDataLabels } }
    $x.values = @(V { $s.Values })
    $x.xValues = @(V { $s.XValues })
    $pie = @(5, -4120, 69, 80, -4102, 70) -contains $d.chartType
    $points = @()
    $pc = $null; try { $pc = $s.Points() } catch { }
    $m = if ($null -ne $pc) { $pc.Count } else { 0 }
    for ($j = 1; $j -le $m; $j++) {
      $p = $pc.Item($j)
      $y = Box $p
      $y.fill = Hex (V { $p.Format.Fill.ForeColor.RGB })
      $y.explosion = V { $p.Explosion }
      if ($pie) {
        # Point.PieSliceLocation(XlPieSliceLocation 1 x / 2 y, XlPieSliceIndex 1 to 9)
        $loc = [ordered]@{}
        for ($k = 1; $k -le 9; $k++) { $loc["$k"] = @((V { $p.PieSliceLocation(1, $k) }), (V { $p.PieSliceLocation(2, $k) })) }
        $y.slice = $loc
      }
      if (V { $p.HasDataLabel }) { $y.dataLabel = Label (V { $p.DataLabel }) }
      $points += $y
    }
    $x.points = $points
    $series += $x
  }
  $d.series = $series
  return $d
}

function WriteJson($d, $path) { $d | ConvertTo-Json -Depth 12 | Set-Content -Encoding UTF8 $path }

# ------------------------------------------------------------------ Excel

$excel = $null; $word = $null; $ppt = $null
function Logs { @(Get-ChildItem $env:TEMP -Filter "error*.xml" -ErrorAction SilentlyContinue | ForEach-Object { $_.FullName }) }
try {
  if (WantApp "excel") {
    $excel = New-Object -ComObject Excel.Application
    $excel.Visible = -not $Hidden
    $excel.DisplayAlerts = $false
    try { $excel.WindowState = -4137 } catch { }   # xlMaximized
    foreach ($p in $probes) {
      $name = $p.name
      $json = Join-Path $out.excel "$name.json"
      if (Test-Path $json) { continue }
      Say "Excel $name"; Fresh $out.excel $name
      $wb = $null
      try {
        $local = Local (Join-Path $probesDir "$name.xlsx")
        $before = Logs
        Step "Workbooks.Open"
        $wb = $excel.Workbooks.Open($local)
        $repaired = @(Logs | Where-Object { $before -notcontains $_ }).Count -gt 0
        $ws = $wb.Worksheets.Item(1)
        $ws.Activate()
        $co = $ws.ChartObjects().Item(1)
        Step "sized $($p.width) by $($p.height)"
        $co.Width = [double]$p.width; $co.Height = [double]$p.height
        try { $excel.ActiveWindow.ScrollRow = 1; $excel.ActiveWindow.ScrollColumn = 1; $co.Activate() } catch { }
        $ch = $co.Chart
        if ($p.style) {
          Step "ChartStyle $($p.style)"
          $ch.ChartStyle = [int]$p.style
        }
        Step "Chart.Refresh"
        try { $ch.Refresh() } catch { }
        Start-Sleep -Milliseconds 300
        $png = Join-Path $tmp "$name.png"
        if (Test-Path $png) { Remove-Item -Force $png }
        Step "Chart.Export"
        [void]$ch.Export($png)
        # an empty picture of a chart Excel hasn't drawn yet: bring it into view and try again
        for ($try = 0; ((-not (Test-Path $png)) -or ((Get-Item $png).Length -eq 0)) -and ($try -lt 3); $try++) {
          try { $ws.Activate(); $co.Activate() } catch { }
          Start-Sleep -Milliseconds 500
          [void]$ch.Export($png)
        }
        Step "the layout"
        $d = Dump $ch "excel" $p $co
        # the layout again after another refresh: written only where it moved
        try { $ch.Refresh() } catch { }
        Start-Sleep -Milliseconds 300
        $again = Dump $ch "excel" $p $co
        if (($again | ConvertTo-Json -Depth 12) -ne ($d | ConvertTo-Json -Depth 12)) {
          WriteJson $again (Join-Path $out.excel "$name.2.json")
          Say "  (the layout moved on a second refresh: $name.2.json)"
        }
        $d.excelVersion = "$($excel.Version) build $($excel.Build)"
        $d.repaired = $repaired
        if ((Test-Path $png) -and (Get-Item $png).Length -gt 0) {
          $img = [System.Drawing.Image]::FromFile($png)
          $d.png = [ordered]@{ width = $img.Width; height = $img.Height; dpiX = $img.HorizontalResolution }
          $img.Dispose()
          Copy-Item -Force $png $out.excel
        } else { $d.png = $null; Say "  (picture empty)" }
        if ($p.style) {
          $styled = Join-Path $tmp "$name-styled.xlsx"
          if (Test-Path $styled) { Remove-Item -Force $styled }
          $wb.SaveCopyAs($styled)
          Copy-Item -Force $styled $out.excel
        }
        WriteJson $d $json
        Say "  wrote $name.json"
      } catch { Failed $out.excel $name $_ } finally { if ($wb) { try { $wb.Close($false) } catch { } } }
    }
  }

  # ------------------------------------------------------------------ Word

  # Saved and closed as office-colours.ps1 (CR-034) does on this VM: SaveAs with each argument a
  # [ref] to a plain [string] or [int] (SaveAs2 and ExportAsFixedFormat stall Word there)
  function WordSave($doc, $path, [int]$format) {
    [string]$s = ([string]$path).ToString()
    [int]$f = $format
    $doc.SaveAs([ref]$s, [ref]$f)
  }
  function WordClose($doc) { [int]$dontSave = 0; $doc.Close([ref]$dontSave) }

  if (WantApp "word") {
    $word = New-Object -ComObject Word.Application
    $word.Visible = [bool]$Visible
    $word.DisplayAlerts = 0
    $word.AutomationSecurity = 3   # msoAutomationSecurityForceDisable
    foreach ($p in $probes) {
      $name = $p.name
      $pdf = Join-Path $out.word "$name.pdf"
      if (Test-Path $pdf) { continue }
      Say "Word $name"; Fresh $out.word $name
      $doc = $null
      try {
        $local = Local (Join-Path $probesDir "$name.docx")
        Step "Documents.Open"
        $doc = $word.Documents.Open($local, $false, $true, $false)   # read-only, as office-colours.ps1
        $ish = $doc.InlineShapes.Item(1)
        if ($p.style) {
          Step "ChartStyle $($p.style)"
          $ish.Chart.ChartStyle = [int]$p.style
          CloseChartData
          $styled = Join-Path $tmp "$name-styled.docx"
          if (Test-Path $styled) { Remove-Item -Force $styled }
          Step "SaveAs (styled)"
          WordSave $doc $styled 16
          Copy-Item -Force $styled $out.word
        }
        $lpdf = Join-Path $tmp "$name.pdf"
        if (Test-Path $lpdf) { Remove-Item -Force $lpdf }
        Step "SaveAs PDF"
        WordSave $doc $lpdf 17
        if (-not $NoAppLayout) {
          Step "the layout"
          $d = Dump $ish.Chart "word" $p $null
          $d.wordVersion = "$($word.Version) build $($word.Build)"
          WriteJson $d (Join-Path $out.word "$name.json")
        }
        Copy-Item -Force $lpdf $out.word
        Say "  wrote $name.pdf"
      } catch { Failed $out.word $name $_ } finally { if ($doc) { try { WordClose $doc } catch { } } }
    }
  }

  # ------------------------------------------------------------------ PowerPoint

  if (WantApp "powerpoint") {
    $pptProbes = @($probes | Where-Object { $_.hosts -like "*pptx*" })
    if ($pptProbes.Count -gt 0) {
      $ppt = New-Object -ComObject PowerPoint.Application
      foreach ($p in $pptProbes) {
        $name = $p.name
        $pdf = Join-Path $out.powerpoint "$name.pdf"
        if (Test-Path $pdf) { continue }
        Say "PowerPoint $name"; Fresh $out.powerpoint $name
        $pres = $null
        try {
          $local = Local (Join-Path $probesDir "$name.pptx")
          Step "Presentations.Open"
          # ReadOnly, not Untitled, with a window (msoTrue -1, msoFalse 0), as office-chart-edit-check.ps1
          $pres = $ppt.Presentations.Open($local, -1, 0, -1)
          $shape = $null
          foreach ($s in $pres.Slides.Item(1).Shapes) { if ($s.HasChart) { $shape = $s } }
          $lpdf = Join-Path $tmp "$name.pdf"
          if (Test-Path $lpdf) { Remove-Item -Force $lpdf }
          Step "SaveAs PDF"
          [string]$pp = $lpdf.ToString(); $pres.SaveAs($pp, 32)
          if (-not $NoAppLayout -and $shape) {
            Step "the layout"
            $d = Dump $shape.Chart "powerpoint" $p $null
            $d.powerpointVersion = "$($ppt.Version) build $($ppt.Build)"
            $d.frame = Box $shape
            WriteJson $d (Join-Path $out.powerpoint "$name.json")
          }
          Copy-Item -Force $lpdf $out.powerpoint
          Say "  wrote $name.pdf"
        } catch { Failed $out.powerpoint $name $_ } finally { if ($pres) { try { $pres.Close() } catch { } } }
      }
    }
  }
} finally {
  foreach ($app in @($excel, $word)) {
    if ($app) {
      try { $app.Quit() } catch { }
      [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($app)
    }
  }
  # PowerPoint is single-instance: quit only if no presentation of the user's is open
  if ($ppt) {
    try { if ($ppt.Presentations.Count -eq 0) { $ppt.Quit() } } catch { }
    [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($ppt)
  }
}
Say "done"
