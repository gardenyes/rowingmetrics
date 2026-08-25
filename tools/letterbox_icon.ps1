Add-Type -AssemblyName System.Drawing
$srcPath = (Join-Path $PSScriptRoot "..\app\src\main\res\drawable\ic_launcher_photo.png")
$src = [System.Drawing.Image]::FromFile($srcPath)
$box = 512
$bmp = New-Object System.Drawing.Bitmap $box, $box
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.Clear([System.Drawing.Color]::FromArgb(255, 0, 0, 0))
$ratio = [Math]::Min([double]$box / $src.Width, [double]$box / $src.Height)
$w = [int][Math]::Round($src.Width * $ratio)
$h = [int][Math]::Round($src.Height * $ratio)
$x = [int](($box - $w) / 2)
$y = [int](($box - $h) / 2)
$g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
$g.DrawImage($src, $x, $y, $w, $h)
$src.Dispose()
$tmpPath = $srcPath + ".tmp.png"
$bmp.Save($tmpPath, [System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose()
$bmp.Dispose()
Move-Item -Force $tmpPath $srcPath
