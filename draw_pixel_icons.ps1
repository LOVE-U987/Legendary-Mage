# Legendary-Mage 像素图标生成器 v3
# 完全重写 - 避免所有PowerShell作用域问题
Add-Type -AssemblyName System.Drawing

$spellDir = 'c:\Users\97128\Documents\GitHub\Legendary-Mage\src\main\resources\assets\legendarymage\textures\gui\spell_icons'
$effectDir = 'c:\Users\97128\Documents\GitHub\Legendary-Mage\src\main\resources\assets\legendarymage\textures\mob_effect'

function NewBmp { New-Object System.Drawing.Bitmap 32, 32 }
function GetG($bmp) { [System.Drawing.Graphics]::FromImage($bmp) }
function C($r,$g,$b) { [System.Drawing.Color]::FromArgb($r,$g,$b) }
function CA($a,$r,$g,$b) { [System.Drawing.Color]::FromArgb($a,$r,$g,$b) }

# ============================================================
# 所有图标函数 - 每个函数独立完整绘制
# ============================================================

function Draw-ElementalBurst {
    $bmp = NewBmp; $g = GetG $bmp
    $g.SmoothingMode = 'None'; $g.InterpolationMode = 'NearestNeighbor'; $g.PixelOffsetMode = 'Half'
    
    # 颜色
    $Purp = C 155 89 182; $LightP = C 190 140 220
    $Red = C 255 77 0; $Ice = C 153 204 255; $Yellow = C 255 215 0
    
    # 外圈冲击波环
    $pen = [System.Drawing.Pen]::new((CA 40 155 89 182), 1); $g.DrawEllipse($pen, 2, 2, 28, 28); $pen.Dispose()
    $pen = [System.Drawing.Pen]::new((CA 60 155 89 182), 1); $g.DrawEllipse($pen, 5, 5, 22, 22); $pen.Dispose()
    $pen = [System.Drawing.Pen]::new((CA 80 155 89 182), 1); $g.DrawEllipse($pen, 8, 8, 16, 16); $pen.Dispose()
    
    # 三道元素弧线: 火(右上), 冰(左下), 雷(左上)
    $pen = [System.Drawing.Pen]::new($Red, 2)
    $g.DrawLine($pen, 22, 8, 27, 11); $g.DrawLine($pen, 27, 11, 26, 16)
    $pen.Dispose()
    
    $pen = [System.Drawing.Pen]::new($Ice, 2)
    $g.DrawLine($pen, 9, 22, 6, 27); $g.DrawLine($pen, 6, 27, 10, 26)
    $pen.Dispose()
    
    $pen = [System.Drawing.Pen]::new($Yellow, 2)
    $g.DrawLine($pen, 5, 8, 9, 5); $g.DrawLine($pen, 9, 5, 13, 7)
    $pen.Dispose()
    
    # 12个螺旋点 (直接内联，避免变量作用域问题)
    for ($i = 0; $i -lt 12; $i++) {
        $angle = [Math]::PI * 2 / 12 * $i
        $r2 = 4 + [Math]::Sin($angle * 3) * 2
        $px = 16 + [Math]::Cos($angle) * $r2
        $py = 16 + [Math]::Sin($angle) * $r2
        
        if ($i % 3 -eq 0) { $bc = $Red }
        elseif ($i % 3 -eq 1) { $bc = $Ice }
        else { $bc = $Yellow }
        
        $brush = [System.Drawing.SolidBrush]::new($bc)
        $g.FillEllipse($brush, $px-1, $py-1, 3, 3)
        $brush.Dispose()
    }
    
    # 紫色核心
    $brush = [System.Drawing.SolidBrush]::new($Purp); $g.FillEllipse($brush, 13, 13, 7, 7); $brush.Dispose()
    $brush = [System.Drawing.SolidBrush]::new($LightP); $g.FillEllipse($brush, 15, 15, 3, 3); $brush.Dispose()
    
    $g.Dispose(); $bmp.Save("$spellDir/elemental_burst.png", [System.Drawing.Imaging.ImageFormat]::Png); $bmp.Dispose()
    Write-Host "  ✅ 元素爆发"
}

function Draw-ElementalPrism {
    $bmp = NewBmp; $g = GetG $bmp
    $g.SmoothingMode = 'None'; $g.InterpolationMode = 'NearestNeighbor'; $g.PixelOffsetMode = 'Half'
    
    $Purp = C 155 89 182; $LightP = C 190 140 220; $White = C 255 255 255
    $Red = C 255 77 0; $Ice = C 153 204 255; $Yellow = C 255 215 0
    
    # 六边形
    $pts = @()
    for ($i = 0; $i -lt 6; $i++) {
        $a = [Math]::PI / 3 * $i - [Math]::PI / 6
        $pts += [System.Drawing.Point]::new([int](16 + [Math]::Cos($a) * 9), [int](16 + [Math]::Sin($a) * 9))
    }
    $hx = [System.Drawing.Point[]]$pts
    
    $brush = [System.Drawing.SolidBrush]::new((CA 80 155 89 182))
    $g.FillPolygon($brush, $hx); $brush.Dispose()
    
    $pen = [System.Drawing.Pen]::new($Purp, 2)
    $g.DrawPolygon($pen, $hx); $pen.Dispose()
    
    # 双层菱形
    $pen = [System.Drawing.Pen]::new($LightP, 1)
    $d1 = [System.Drawing.Point[]]@([System.Drawing.Point]::new(16,10),[System.Drawing.Point]::new(21,16),[System.Drawing.Point]::new(16,22),[System.Drawing.Point]::new(11,16))
    $d2 = [System.Drawing.Point[]]@([System.Drawing.Point]::new(16,12),[System.Drawing.Point]::new(19,16),[System.Drawing.Point]::new(16,20),[System.Drawing.Point]::new(13,16))
    $g.DrawPolygon($pen, $d1); $g.DrawPolygon($pen, $d2); $pen.Dispose()
    
    # 6道折射光线
    $colors = @($Red, $Ice, $Yellow, $Red, $Ice, $Yellow)
    for ($i = 0; $i -lt 6; $i++) {
        $a = [Math]::PI / 3 * $i
        $x1 = [int](16 + [Math]::Cos($a) * 10); $y1 = [int](16 + [Math]::Sin($a) * 10)
        $x2 = [int](16 + [Math]::Cos($a) * 15); $y2 = [int](16 + [Math]::Sin($a) * 15)
        $pen = [System.Drawing.Pen]::new($colors[$i], 1)
        $g.DrawLine($pen, $x1, $y1, $x2, $y2); $pen.Dispose()
        $brush = [System.Drawing.SolidBrush]::new($colors[$i])
        $g.FillEllipse($brush, $x2-1, $y2-1, 3, 3); $brush.Dispose()
    }
    
    $brush = [System.Drawing.SolidBrush]::new($White); $g.FillEllipse($brush, 15, 15, 3, 3); $brush.Dispose()
    $g.Dispose(); $bmp.Save("$spellDir/elemental_prism.png", [System.Drawing.Imaging.ImageFormat]::Png); $bmp.Dispose()
    Write-Host "  ✅ 元素棱镜"
}

function Draw-GiantSnowball {
    $bmp = NewBmp; $g = GetG $bmp
    $g.SmoothingMode = 'None'; $g.InterpolationMode = 'NearestNeighbor'; $g.PixelOffsetMode = 'Half'
    
    $White = C 255 255 255; $Ice = C 153 204 255; $LightI = C 200 230 255; $DarkIce = C 80 130 200
    
    # 雪球主体
    $brush = [System.Drawing.SolidBrush]::new($White); $g.FillEllipse($brush, 5, 5, 22, 22); $brush.Dispose()
    $brush = [System.Drawing.SolidBrush]::new((CA 100 153 204 255)); $g.FillEllipse($brush, 7, 7, 18, 18); $brush.Dispose()
    $brush = [System.Drawing.SolidBrush]::new((CA 60 153 204 255)); $g.FillEllipse($brush, 9, 9, 14, 14); $brush.Dispose()
    
    # 冰晶碎屑
    $shards = @((-14,-8), (-12,-14), (12,-10), (14,-4), (-10,10), (8,12))
    foreach ($s in $shards) {
        $brush = [System.Drawing.SolidBrush]::new($LightI)
        $g.FillRectangle($brush, 16+$s[0], 16+$s[1], 2, 2); $brush.Dispose()
    }
    
    # 雪花粒子
    $brush = [System.Drawing.SolidBrush]::new($White); $g.FillEllipse($brush, 9, 3, 3, 3); $brush.Dispose()
    $brush = [System.Drawing.SolidBrush]::new($LightI); $g.FillEllipse($brush, 19, 2, 3, 3); $brush.Dispose()
    $brush = [System.Drawing.SolidBrush]::new($White); $g.FillEllipse($brush, 7, 24, 3, 3); $brush.Dispose()
    $brush = [System.Drawing.SolidBrush]::new($Ice); $g.FillEllipse($brush, 23, 21, 3, 3); $brush.Dispose()
    
    # 冲击波环
    $pen = [System.Drawing.Pen]::new((CA 60 153 204 255), 1); $g.DrawEllipse($pen, 2, 2, 28, 28); $pen.Dispose()
    
    # 速度线
    $pen = [System.Drawing.Pen]::new((CA 50 153 204 255), 1)
    $g.DrawLine($pen, 2, 11, -2, 8); $g.DrawLine($pen, 1, 16, -4, 14); $g.DrawLine($pen, 2, 21, -2, 22)
    $pen.Dispose()
    
    $g.Dispose(); $bmp.Save("$spellDir/giant_snowball.png", [System.Drawing.Imaging.ImageFormat]::Png); $bmp.Dispose()
    Write-Host "  ✅ 巨雪球"
}

function Draw-TriDirectionalArrow {
    $bmp = NewBmp; $g = GetG $bmp
    $g.SmoothingMode = 'None'; $g.InterpolationMode = 'NearestNeighbor'; $g.PixelOffsetMode = 'Half'
    
    $Purp = C 155 89 182; $LightP = C 190 140 220
    $Ice = C 153 204 255; $LightI = C 200 230 255
    $Red = C 255 77 0; $Orange = C 255 140 0; $Yellow = C 255 215 0; $White = C 255 255 255
    
    # 三支扇形的箭
    $arrows = @(
        @(-7, 3, 6, -12, $Ice, $LightI),
        @(0, 5, 0, -15, $Red, $Orange),
        @(7, 3, -6, -12, $Yellow, $White)
    )
    foreach ($a in $arrows) {
        $pen = [System.Drawing.Pen]::new($a[4], 2)
        $g.DrawLine($pen, 16+$a[0], 16+$a[1], 16+$a[2], 16+$a[3]); $pen.Dispose()
        $brush = [System.Drawing.SolidBrush]::new($a[5])
        $g.FillEllipse($brush, 16+$a[2]-1, 16+$a[3]-1, 3, 3); $brush.Dispose()
        # 尾迹
        $pen = [System.Drawing.Pen]::new((CA 50 $a[4].R $a[4].G $a[4].B), 1)
        $g.DrawLine($pen, 16+$a[0]*2, 16+$a[1]*2, 16+$a[0]*3, 16+$a[1]*3)
        $pen.Dispose()
    }
    
    # 核心
    $brush = [System.Drawing.SolidBrush]::new($Purp); $g.FillEllipse($brush, 13, 13, 7, 7); $brush.Dispose()
    $brush = [System.Drawing.SolidBrush]::new($LightP); $g.FillEllipse($brush, 15, 15, 3, 3); $brush.Dispose()
    $g.Dispose(); $bmp.Save("$spellDir/tri_directional_arrow.png", [System.Drawing.Imaging.ImageFormat]::Png); $bmp.Dispose()
    Write-Host "  ✅ 三向之矢"
}

function Draw-TrailTest {
    $bmp = NewBmp; $g = GetG $bmp
    $g.SmoothingMode = 'None'; $g.InterpolationMode = 'NearestNeighbor'; $g.PixelOffsetMode = 'Half'
    
    $Purp = C 155 89 182; $LightP = C 190 140 220
    
    # 烧瓶主体
    $brush = [System.Drawing.SolidBrush]::new((CA 60 155 89 182)); $g.FillEllipse($brush, 10, 10, 13, 13); $brush.Dispose()
    $brush = [System.Drawing.SolidBrush]::new((CA 80 190 140 220)); $g.FillEllipse($brush, 11, 11, 10, 10); $brush.Dispose()
    
    # 瓶颈
    $pen = [System.Drawing.Pen]::new($Purp, 2)
    $g.DrawLine($pen, 13, 11, 14, 7); $g.DrawLine($pen, 19, 11, 18, 7); $g.DrawLine($pen, 13, 7, 19, 7)
    $pen.Dispose()
    
    # 火花
    $sparks = @((-8,-8,1,$LightP), (-10,-4,1,$LightP), (-12,0,1,$LightP), (8,-10,1,$LightP), (10,-6,1,$LightP), (12,-2,1,$LightP))
    foreach ($s in $sparks) {
        $brush = [System.Drawing.SolidBrush]::new($s[3])
        $g.FillEllipse($brush, 16+$s[0]-$s[2], 16+$s[1]-$s[2], $s[2]*2+1, $s[2]*2+1)
        $brush.Dispose()
    }
    
    $g.Dispose(); $bmp.Save("$spellDir/trail_test.png", [System.Drawing.Imaging.ImageFormat]::Png); $bmp.Dispose()
    Write-Host "  ✅ 拖尾测试"
}

function Draw-EnderEcho {
    $bmp = NewBmp; $g = GetG $bmp
    $g.SmoothingMode = 'None'; $g.InterpolationMode = 'NearestNeighbor'; $g.PixelOffsetMode = 'Half'
    
    $EnderP = C 120 50 180; $Purp = C 155 89 182; $LightP = C 190 140 220; $White = C 255 255 255
    
    # 回声波纹
    $pen = [System.Drawing.Pen]::new((CA 30 120 50 180), 1); $g.DrawEllipse($pen, 2, 2, 28, 28); $pen.Dispose()
    $pen = [System.Drawing.Pen]::new((CA 50 120 50 180), 1); $g.DrawEllipse($pen, 5, 5, 22, 22); $pen.Dispose()
    $pen = [System.Drawing.Pen]::new((CA 70 155 89 182), 1); $g.DrawEllipse($pen, 8, 8, 16, 16); $pen.Dispose()
    
    # 末影珍珠
    $brush = [System.Drawing.SolidBrush]::new($EnderP); $g.FillEllipse($brush, 10, 10, 13, 13); $brush.Dispose()
    $brush = [System.Drawing.SolidBrush]::new($Purp); $g.FillEllipse($brush, 12, 12, 9, 9); $brush.Dispose()
    $brush = [System.Drawing.SolidBrush]::new($LightP); $g.FillEllipse($brush, 14, 14, 5, 5); $brush.Dispose()
    $brush = [System.Drawing.SolidBrush]::new($White); $g.FillEllipse($brush, 16, 14, 2, 2); $brush.Dispose()
    
    # 末影粒子
    $particles = @((-4,-10,2), (5,-8,2), (-10,3,2), (10,4,2), (-12,6,2), (12,8,2))
    foreach ($p in $particles) {
        $brush = [System.Drawing.SolidBrush]::new($EnderP)
        $g.FillRectangle($brush, 16+$p[0], 16+$p[1], $p[2], $p[2]); $brush.Dispose()
    }
    $smallP = @((6,-3), (-7,5), (3,9), (-5,7))
    foreach ($p in $smallP) {
        $brush = [System.Drawing.SolidBrush]::new($LightP)
        $g.FillRectangle($brush, 16+$p[0], 16+$p[1], 1, 1); $brush.Dispose()
    }
    
    $g.Dispose(); $bmp.Save("$effectDir/ender_echo_buff.png", [System.Drawing.Imaging.ImageFormat]::Png); $bmp.Dispose()
    Write-Host "  ✅ 终末回响"
}

function Draw-LightningRod {
    $bmp = NewBmp; $g = GetG $bmp
    $g.SmoothingMode = 'None'; $g.InterpolationMode = 'NearestNeighbor'; $g.PixelOffsetMode = 'Half'
    
    $Gray = C 128 128 128; $LGray = C 200 200 200; $Yellow = C 255 215 0; $Orange = C 255 140 0; $Ice = C 153 204 255
    
    # 避雷针主体
    $pen = [System.Drawing.Pen]::new($Gray, 3); $g.DrawLine($pen, 15, 8, 15, 24); $pen.Dispose()
    $brush = [System.Drawing.SolidBrush]::new($Gray); $g.FillRectangle($brush, 11, 22, 10, 3); $brush.Dispose()
    $brush = [System.Drawing.SolidBrush]::new($LGray); $g.FillRectangle($brush, 14, 5, 3, 3); $brush.Dispose()
    
    # 闪电
    $pen = [System.Drawing.Pen]::new($Yellow, 2)
    $g.DrawLine($pen, 12, 0, 15, 4); $g.DrawLine($pen, 15, 4, 18, 6)
    $g.DrawLine($pen, 18, 6, 15, 9); $g.DrawLine($pen, 15, 9, 19, 11)
    $pen.Dispose()
    
    # 冰晶
    $pen = [System.Drawing.Pen]::new($Ice, 1)
    $g.DrawLine($pen, 10, 14, 7, 11); $g.DrawLine($pen, 7, 11, 5, 13)
    $g.DrawLine($pen, 22, 12, 26, 9); $g.DrawLine($pen, 26, 9, 28, 11)
    $pen.Dispose()
    
    # 电火花
    $brush = [System.Drawing.SolidBrush]::new($Yellow); $g.FillEllipse($brush, 12, 1, 3, 3); $brush.Dispose()
    $brush = [System.Drawing.SolidBrush]::new($Yellow); $g.FillEllipse($brush, 20, 4, 3, 3); $brush.Dispose()
    $brush = [System.Drawing.SolidBrush]::new($Yellow); $g.FillEllipse($brush, 10, 9, 3, 3); $brush.Dispose()
    $brush = [System.Drawing.SolidBrush]::new($Orange); $g.FillEllipse($brush, 19, 6, 3, 3); $brush.Dispose()
    
    $g.Dispose(); $bmp.Save("$effectDir/lightning_rod_buff.png", [System.Drawing.Imaging.ImageFormat]::Png); $bmp.Dispose()
    Write-Host "  ✅ 避雷针"
}

function Draw-Plague {
    $bmp = NewBmp; $g = GetG $bmp
    $g.SmoothingMode = 'None'; $g.InterpolationMode = 'NearestNeighbor'; $g.PixelOffsetMode = 'Half'
    
    $DarkP = C 46 0 62; $LGray = C 200 200 200; $Gray = C 128 128 128; $Poison = C 76 175 80
    
    # 暗紫背景雾
    $brush = [System.Drawing.SolidBrush]::new((CA 50 46 0 62)); $g.FillEllipse($brush, 3, 3, 26, 26); $brush.Dispose()
    
    # 骷髅头 - 头骨顶部
    $brush = [System.Drawing.SolidBrush]::new($LGray); $g.FillEllipse($brush, 9, 4, 14, 14); $brush.Dispose()
    # 下巴
    $brush = [System.Drawing.SolidBrush]::new($LGray); $g.FillRectangle($brush, 10, 15, 12, 5); $brush.Dispose()
    
    # 眼窝
    $brush = [System.Drawing.SolidBrush]::new($DarkP); $g.FillEllipse($brush, 11, 11, 4, 5); $brush.Dispose()
    $brush = [System.Drawing.SolidBrush]::new($DarkP); $g.FillEllipse($brush, 17, 11, 4, 5); $brush.Dispose()
    
    # 眼睛
    $brush = [System.Drawing.SolidBrush]::new($Poison); $g.FillEllipse($brush, 12, 12, 2, 2); $brush.Dispose()
    $brush = [System.Drawing.SolidBrush]::new($Poison); $g.FillEllipse($brush, 18, 12, 2, 2); $brush.Dispose()
    
    # 鼻子
    $brush = [System.Drawing.SolidBrush]::new($Gray); $g.FillEllipse($brush, 15, 14, 2, 2); $brush.Dispose()
    
    # 牙齿
    $pen = [System.Drawing.Pen]::new($Gray, 1)
    $g.DrawLine($pen, 12, 18, 12, 20); $g.DrawLine($pen, 15, 18, 15, 20); $g.DrawLine($pen, 18, 18, 18, 20)
    $pen.Dispose()
    
    # 毒气云团
    $brush = [System.Drawing.SolidBrush]::new((CA 30 76 175 80))
    $g.FillEllipse($brush, 5, 2, 6, 6); $g.FillEllipse($brush, 21, 4, 8, 8)
    $g.FillEllipse($brush, 4, 20, 6, 6); $g.FillEllipse($brush, 22, 21, 6, 6)
    $brush.Dispose()
    
    # 毒气粒子
    $brush = [System.Drawing.SolidBrush]::new($Poison)
    $g.FillEllipse($brush, 7, 2, 3, 3); $g.FillEllipse($brush, 11, 0, 3, 3)
    $g.FillEllipse($brush, 19, 1, 3, 3); $g.FillEllipse($brush, 23, 2, 3, 3)
    $g.FillEllipse($brush, 15, 0, 2, 2); $g.FillEllipse($brush, 21, -1, 2, 2)
    $brush.Dispose()
    
    $g.Dispose(); $bmp.Save("$effectDir/plague_buff.png", [System.Drawing.Imaging.ImageFormat]::Png); $bmp.Dispose()
    Write-Host "  ✅ 瘟疫"
}

# ============================================================
# 执行
# ============================================================
Write-Host "🎨 Legendary-Mage 图标生成器 v3" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan

Draw-ElementalBurst
Draw-ElementalPrism
Draw-GiantSnowball
Draw-TriDirectionalArrow
Draw-TrailTest
Draw-EnderEcho
Draw-LightningRod
Draw-Plague

Write-Host "`n=== 验证 ===" -ForegroundColor Cyan
Get-ChildItem $spellDir, $effectDir -Include "elemental_burst.png","elemental_prism.png","giant_snowball.png","tri_directional_arrow.png","trail_test.png","ender_echo_buff.png","lightning_rod_buff.png","plague_buff.png" -Recurse | Select-Object Name, Length | Format-Table -AutoSize

Write-Host "`n✅ 全部完成!" -ForegroundColor Green