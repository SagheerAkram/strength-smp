$packDir = "C:\Users\SM\Desktop\StrengthSMP-Pack"
$itemModels = "$packDir\assets\minecraft\models\item"
$itemTextures = "$packDir\assets\minecraft\textures\item"

# Cleanup old dirs
if (Test-Path $packDir) { Remove-Item -Recurse -Force $packDir }

New-Item -ItemType Directory -Force -Path $itemModels
New-Item -ItemType Directory -Force -Path $itemTextures

# Function to write UTF-8 No BOM (Minecraft requirement)
function Write-JsonFile($path, $content) {
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($path, $content, $utf8NoBom)
}

# pack.mcmeta
$mcmeta = @'
{
  "pack": {
    "pack_format": 15,
    "description": "Official StrengthSMP Custom Items"
  }
}
'@
Write-JsonFile "$packDir\pack.mcmeta" $mcmeta

# Models
$overrides = @'
{
  "parent": "minecraft:item/generated",
  "textures": { "layer0": "minecraft:item/nautilus_shell" },
  "overrides": [ { "predicate": { "custom_model_data": 12345 }, "model": "minecraft:item/strength_core" } ]
}
'@
Write-JsonFile "$itemModels\nautilus_shell.json" $overrides

$book = @'
{
  "parent": "minecraft:item/generated",
  "textures": { "layer0": "minecraft:item/book" },
  "overrides": [ { "predicate": { "custom_model_data": 12346 }, "model": "minecraft:item/reroll_book" } ]
}
'@
Write-JsonFile "$itemModels\book.json" $book

$paper = @'
{
  "parent": "minecraft:item/generated",
  "textures": { "layer0": "minecraft:item/paper" },
  "overrides": [ { "predicate": { "custom_model_data": 12347 }, "model": "minecraft:item/soul_scroll" } ]
}
'@
Write-JsonFile "$itemModels\paper.json" $paper

# Item Models
Write-JsonFile "$itemModels\strength_core.json" '{"parent":"minecraft:item/generated","textures":{"layer0":"minecraft:item/strength_core"}}'
Write-JsonFile "$itemModels\reroll_book.json" '{"parent":"minecraft:item/generated","textures":{"layer0":"minecraft:item/reroll_book"}}'
Write-JsonFile "$itemModels\soul_scroll.json" '{"parent":"minecraft:item/generated","textures":{"layer0":"minecraft:item/soul_scroll"}}'

# Animation
Write-JsonFile "$itemTextures\strength_core.png.mcmeta" '{"animation":{"interpolate":true,"frametime":2,"frames":[0]}}'

# Textures
Copy-Item "C:\Users\SM\.gemini\antigravity\brain\36cffdd3-9b06-477a-8ec6-f92c00be22d5\strength_core_frame_1_1777787218627.png" "$itemTextures\strength_core.png"
Copy-Item "C:\Users\SM\.gemini\antigravity\brain\36cffdd3-9b06-477a-8ec6-f92c00be22d5\reroll_book_ledger_of_fate_1777786724367.png" "$itemTextures\reroll_book.png"
Copy-Item "C:\Users\SM\.gemini\antigravity\brain\36cffdd3-9b06-477a-8ec6-f92c00be22d5\death_certificate_soul_scroll_1777786862350.png" "$itemTextures\soul_scroll.png"

# Zip
Compress-Archive -Path "$packDir\*" -DestinationPath "C:\Users\SM\Desktop\StrengthSMP-Pack.zip" -Force
Remove-Item -Recurse -Force $packDir
Write-Host "✅ Resource Pack created on Desktop: StrengthSMP-Pack.zip"
Write-Host "⚠️ IMPORTANT: Upload this NEW zip to mc-packs.net and update your config!"
