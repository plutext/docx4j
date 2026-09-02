Attribute VB_Name = "MathmlToDocx"
' ---------------------------------------------------------------------------
' Corpus generator for CR-math-omml-mathml.
'
' Batch-converts a folder of MathML files into Word documents (so docx4j can
' extract the OMML), and captures Word's own MathML for each equation (the
' export oracle). Word is used purely as a black box: we never touch or ship
' Microsoft's OMML2MML.XSL / MML2OMML.XSL.
'
' For each  <inFolder>\NAME.mml  it writes:
'   <outFolder>\NAME.docx       - Word's OMML  (input for OmmlFixtureExtractor)
'   <outFolder>\NAME.word.mml   - Word's MathML for that equation (export oracle)
'
' PREREQUISITES
'   * Microsoft Word for Windows, version 2605 or later (older builds do not
'     reliably auto-convert pasted MathML to an OfficeMath equation).
'   * Equation Tools > Conversions > tick "Copy MathML to the clipboard as
'     plain text" (needed for the .word.mml capture). Set once; Word persists it.
'   * Trust access is not needed; this uses only Word + the Forms DataObject.
'
' MECHANISM
'   MathML -> OMML : the MathML text is placed on the clipboard and pasted;
'                    Word converts it to an OfficeMath equation on paste.
'   OMML -> MathML : with the setting above on, selecting the equation and
'                    copying puts Word's MathML on the clipboard as plain text.
'
' CAVEATS (read the README)
'   * Reliable conversion is via clipboard PASTE of the MathML, not opening an
'     HTML page in Word. This macro pastes.
'   * The paste-auto-convert behaviour is version dependent; if a file does not
'     become an equation, fall back to the manual procedure in the README for
'     that case. The macro logs any file that produced no OfficeMath.
'   * Authored against the documented behaviour; verify on your Word build with
'     a couple of files before running the whole corpus.
' ---------------------------------------------------------------------------
Option Explicit

' Sleep, for backing off when the clipboard is momentarily busy.
#If VBA7 Then
    Private Declare PtrSafe Sub Sleep Lib "kernel32" (ByVal dwMilliseconds As Long)
#Else
    Private Declare Sub Sleep Lib "kernel32" (ByVal dwMilliseconds As Long)
#End If

' EDIT THESE TWO PATHS, then run BatchConvert.
Private Const IN_FOLDER As String = "C:\corpus\mml"
Private Const OUT_FOLDER As String = "C:\corpus\out"

' The clipboard is a shared, single-owner resource; OpenClipboard fails if
' another process (a clipboard-history tool, or Word's own Copy/Paste) holds it.
' Retry a few times with a short backoff rather than aborting the whole run.
Private Const CLIP_RETRIES As Long = 30
Private Const CLIP_WAIT_MS As Long = 60

Public Sub BatchConvert()

    Dim inFolder As String, outFolder As String
    inFolder = EnsureTrailingSlash(IN_FOLDER)
    outFolder = EnsureTrailingSlash(OUT_FOLDER)

    Dim total As Long, ok As Long, noEqn As Long
    Dim logMsg As String

    ' Gather all .mml names FIRST. Dir() is stateful and non-reentrant, so we must
    ' not call Dir() for the resume check inside a Dir() enumeration loop.
    Dim names As Collection
    Set names = New Collection
    Dim fname As String
    fname = Dir(inFolder & "*.mml")
    Do While Len(fname) > 0
        names.Add fname
        fname = Dir()
    Loop

    Dim fso As Object
    Set fso = CreateObject("Scripting.FileSystemObject")

    Dim idx As Long
    For idx = 1 To names.count
        fname = names(idx)
        total = total + 1
        Dim base As String
        base = Left$(fname, Len(fname) - 4)          ' strip ".mml"

        ' Resumable: skip a case whose .docx already exists, so a re-run after a
        ' transient failure continues instead of redoing work.
        If fso.FileExists(outFolder & base & ".docx") Then
            GoTo ContinueLoop
        End If

        Dim mathml As String
        mathml = ReadAllText(inFolder & fname)

        Dim doc As Document
        Set doc = Application.Documents.Add(Visible:=False)

        ' --- MathML -> OMML: paste the MathML; Word converts it on paste ---
        PutTextOnClipboard mathml
        doc.Content.Select
        Selection.EndKey Unit:=wdStory
        On Error Resume Next
        Selection.Paste
        On Error GoTo 0
        DoEvents                                     ' let the paste settle

        If doc.OMaths.count = 0 Then
            noEqn = noEqn + 1
            logMsg = logMsg & "NO EQUATION: " & fname & vbCrLf
        Else
            ' --- OMML -> MathML: copy the (first) equation, read Word's MathML ---
            ' requires "Copy MathML to the clipboard as plain text" to be enabled
            doc.OMaths(1).Range.Select
            Selection.Copy
            DoEvents                                 ' let the copy settle
            Dim wordMml As String
            wordMml = GetTextFromClipboard()
            If Len(wordMml) > 0 Then
                WriteAllText outFolder & base & ".word.mml", wordMml
            End If
            ok = ok + 1
        End If

        ' save Word's OMML as .docx for OmmlFixtureExtractor
        doc.SaveAs2 fileName:=outFolder & base & ".docx", _
                    FileFormat:=wdFormatXMLDocument
        doc.Close SaveChanges:=False

ContinueLoop:
    Next idx

    MsgBox "MathML files: " & total & vbCrLf & _
           "equations produced: " & ok & vbCrLf & _
           "no equation (needs manual): " & noEqn & vbCrLf & vbCrLf & _
           logMsg, vbInformation, "MathmlToDocx"
End Sub

' Re-capture Word's MathML for each already-generated .docx (the OMML -> MathML
' export oracle), WITHOUT re-importing. Run this after ticking "Copy MathML to
' the clipboard as plain text" (Equation Tools > Conversions): the first pass
' captured Word's UnicodeMath linear text ("1/2", "?(...)") because that setting
' was off. Overwrites the .word.mml files; leaves the .docx untouched.
Public Sub CaptureMathMLOnly()

    Dim outFolder As String
    outFolder = EnsureTrailingSlash(OUT_FOLDER)

    Dim names As Collection
    Set names = New Collection
    Dim fn As String
    fn = Dir(outFolder & "*.docx")
    Do While Len(fn) > 0
        names.Add fn
        fn = Dir()
    Loop

    Dim i As Long, captured As Long, emptyCount As Long, notMathml As Long
    Dim logMsg As String
    For i = 1 To names.count
        fn = names(i)
        Dim base As String
        base = Left$(fn, Len(fn) - 5)                ' strip ".docx"

        Dim doc As Document
        Set doc = Documents.Open(fileName:=outFolder & fn, ReadOnly:=True, Visible:=False)
        If doc.OMaths.count > 0 Then
            doc.OMaths(1).Range.Select
            Selection.Copy
            DoEvents
            Dim m As String
            m = GetTextFromClipboard()
            If Len(m) = 0 Then
                emptyCount = emptyCount + 1
                logMsg = logMsg & "EMPTY: " & fn & vbCrLf
            Else
                WriteAllText outFolder & base & ".word.mml", m
                captured = captured + 1
                ' sanity: real MathML contains a <math ...> tag
                If InStr(m, "<math") = 0 Then
                    notMathml = notMathml + 1
                End If
            End If
        End If
        doc.Close SaveChanges:=False
    Next i

    Dim warn As String
    If notMathml > 0 Then
        warn = vbCrLf & vbCrLf & "WARNING: " & notMathml & " capture(s) contained no <math> " & _
               "tag - the 'Copy MathML to the clipboard as plain text' setting is still off."
    End If
    MsgBox "docx: " & names.count & vbCrLf & _
           "captured: " & captured & vbCrLf & _
           "empty: " & emptyCount & warn & vbCrLf & vbCrLf & logMsg, vbInformation, "CaptureMathMLOnly"
End Sub

' --- helpers ---------------------------------------------------------------

Private Function EnsureTrailingSlash(ByVal p As String) As String
    If Right$(p, 1) <> "\" Then p = p & "\"
    EnsureTrailingSlash = p
End Function

' UTF-8 file I/O via ADODB.Stream. VBA's native Open/Print writes the system
' ANSI codepage, which destroys the Unicode math characters Word's MathML uses
' (integral U+222B, infinity U+221E, braces U+FE37/8, combining marks, …) —
' they come out as "?". Read/write UTF-8 so those survive.
Private Function ReadAllText(ByVal path As String) As String
    Dim st As Object
    Set st = CreateObject("ADODB.Stream")
    st.Type = 2                 ' adTypeText
    st.Charset = "utf-8"
    st.Open
    st.LoadFromFile path
    ReadAllText = st.ReadText(-1)   ' adReadAll
    st.Close
End Function

Private Sub WriteAllText(ByVal path As String, ByVal text As String)
    ' Write UTF-8 without a BOM: ADODB adds one, so copy past it via a bytes stream.
    Dim src As Object, dst As Object
    Set src = CreateObject("ADODB.Stream")
    src.Type = 2                ' adTypeText
    src.Charset = "utf-8"
    src.Open
    src.WriteText text
    ' reposition to just after the 3-byte UTF-8 BOM, read the rest as bytes
    src.Position = 0
    src.Type = 1               ' adTypeBinary
    src.Position = 3
    Dim bytes() As Byte
    bytes = src.Read
    src.Close
    Set dst = CreateObject("ADODB.Stream")
    dst.Type = 1               ' adTypeBinary
    dst.Open
    dst.Write bytes
    dst.SaveToFile path, 2     ' adSaveCreateOverWrite
    dst.Close
End Sub

' MSForms.DataObject via late binding (no project reference required)
Private Function NewDataObject() As Object
    Set NewDataObject = CreateObject("new:{1C3B4210-F441-11CE-B9EA-00AA006B1A69}")
End Function

' PutInClipboard / GetFromClipboard call the Win32 OpenClipboard, which fails
' ("OpenClipboard Failed") when another process holds the clipboard for a moment.
' Retry with a short backoff rather than aborting the run.
Private Sub PutTextOnClipboard(ByVal s As String)
    Dim d As Object, i As Long
    For i = 1 To CLIP_RETRIES
        Set d = NewDataObject()
        d.SetText s
        On Error Resume Next
        Err.Clear
        d.PutInClipboard
        If Err.Number = 0 Then
            On Error GoTo 0
            Exit Sub
        End If
        On Error GoTo 0
        DoEvents
        Sleep CLIP_WAIT_MS
    Next i
    Err.Raise vbObjectError + 513, "PutTextOnClipboard", _
              "Clipboard stayed busy after " & CLIP_RETRIES & " tries " & _
              "(close any clipboard-history/manager tool and rerun; the macro resumes)."
End Sub

Private Function GetTextFromClipboard() As String
    Dim d As Object, i As Long
    For i = 1 To CLIP_RETRIES
        Set d = NewDataObject()
        On Error Resume Next
        Err.Clear
        d.GetFromClipboard
        If Err.Number = 0 Then
            GetTextFromClipboard = d.GetText()
            On Error GoTo 0
            Exit Function
        End If
        On Error GoTo 0
        DoEvents
        Sleep CLIP_WAIT_MS
    Next i
    ' Non-fatal: return empty so the .docx (the OMML) is still saved.
    GetTextFromClipboard = ""
End Function
