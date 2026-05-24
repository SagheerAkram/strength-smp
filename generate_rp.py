#!/usr/bin/env python3
import os
import sys
import zipfile
import shutil
import hashlib
from PIL import Image

def process_texture(input_path, output_path, size=64, threshold=30):
    """Process a texture: black bg -> transparent, crop, resize."""
    print(f"Processing: {os.path.basename(input_path)} -> {os.path.basename(output_path)}")
    img = Image.open(input_path).convert("RGBA")
    pixels = img.load()
    w, h = img.size

    # Convert near-black pixels to transparent
    for y in range(h):
        for x in range(w):
            r, g, b, a = pixels[x, y]
            if r < threshold and g < threshold and b < threshold:
                pixels[x, y] = (0, 0, 0, 0)

    # Crop to bounding box of non-transparent pixels
    bbox = img.getbbox()
    if bbox:
        img = img.crop(bbox)

    # Resize to target size (square) using nearest-neighbor for crisp pixels
    img = img.resize((size, size), Image.NEAREST)

    # Save
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    img.save(output_path, "PNG")
    print(f"  Saved to {output_path} ({size}x{size})")

def zip_folder(folder_path, output_path):
    print(f"Zipping {folder_path} to {output_path}...")
    with zipfile.ZipFile(output_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
        for root, dirs, files in os.walk(folder_path):
            for file in files:
                file_path = os.path.join(root, file)
                arcname = os.path.relpath(file_path, folder_path)
                zipf.write(file_path, arcname)
    print(f"Zip created successfully: {output_path}")

def calculate_sha1(file_path):
    sha1 = hashlib.sha1()
    with open(file_path, 'rb') as f:
        while True:
            data = f.read(65536)
            if not data:
                break
            sha1.update(data)
    return sha1.hexdigest()

def main():
    brain_dir = "/home/sagheer/.gemini/antigravity/brain/2cc7f399-824e-4256-8129-8ab6da91a3a4"
    workspace_dir = "/home/sagheer/Desktop/StrengthSMP"
    rp_dir = os.path.join(workspace_dir, "resourcepack")
    
    # 1. Clean up existing resource pack directory to prevent stale/glitched files
    textures_dir = os.path.join(rp_dir, "assets/minecraft/textures/item")
    models_dir = os.path.join(rp_dir, "assets/minecraft/models/item")
    items_dir = os.path.join(rp_dir, "assets/minecraft/items")
    
    for d in [textures_dir, models_dir, items_dir]:
        if os.path.exists(d):
            shutil.rmtree(d)
        os.makedirs(d, exist_ok=True)
        
    # 2. Process high-quality source textures
    strength_in = os.path.join(brain_dir, "strength_orb_new_1779605101241.png")
    reroll_in = os.path.join(brain_dir, "reroll_book_new_1779605136478.png")
    death_in = os.path.join(brain_dir, "death_cert_new_1779605157300.png")
    
    process_texture(strength_in, os.path.join(textures_dir, "strength_item.png"), size=64, threshold=30)
    process_texture(reroll_in, os.path.join(textures_dir, "reroll_item.png"), size=64, threshold=30)
    process_texture(death_in, os.path.join(textures_dir, "death_certificate.png"), size=64, threshold=30)
    
    # 3. Write custom item model files (referenced by both overrides and range_dispatch)
    custom_items = ["strength_item", "reroll_item", "death_certificate"]
    for item in custom_items:
        model_path = os.path.join(models_dir, f"{item}.json")
        with open(model_path, "w") as f:
            f.write(f'''{{
  "parent": "minecraft:item/generated",
  "textures": {{
    "layer0": "minecraft:item/{item}"
  }}
}}''')
        print(f"Wrote custom model JSON: {model_path}")
        
    # 4. Write vanilla overrides model files (for pre-1.21.4 compatibility)
    # Nautilus Shell -> strength_item
    with open(os.path.join(models_dir, "nautilus_shell.json"), "w") as f:
        f.write('''{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/nautilus_shell"
  },
  "overrides": [
    {
      "predicate": {
        "custom_model_data": 12345
      },
      "model": "minecraft:item/strength_item"
    }
  ]
}''')
        
    # Book -> reroll_item
    with open(os.path.join(models_dir, "book.json"), "w") as f:
        f.write('''{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/book"
  },
  "overrides": [
    {
      "predicate": {
        "custom_model_data": 12346
      },
      "model": "minecraft:item/reroll_item"
    }
  ]
}''')
        
    # Paper -> death_certificate
    with open(os.path.join(models_dir, "paper.json"), "w") as f:
        f.write('''{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/paper"
  },
  "overrides": [
    {
      "predicate": {
        "custom_model_data": 12347
      },
      "model": "minecraft:item/death_certificate"
    }
  ]
}''')
        
    # 5. Write vanilla overrides item definition files (for 1.21.4+ compatibility)
    # Nautilus Shell -> strength_item (CMD 12345)
    with open(os.path.join(items_dir, "nautilus_shell.json"), "w") as f:
        f.write('''{
  "model": {
    "type": "minecraft:range_dispatch",
    "property": "minecraft:custom_model_data",
    "fallback": {
      "type": "minecraft:model",
      "model": "minecraft:item/nautilus_shell"
    },
    "entries": [
      {
        "threshold": 12345.0,
        "model": {
          "type": "minecraft:model",
          "model": "minecraft:item/strength_item"
        }
      }
    ]
  }
}''')
        
    # Book -> reroll_item (CMD 12346)
    with open(os.path.join(items_dir, "book.json"), "w") as f:
        f.write('''{
  "model": {
    "type": "minecraft:range_dispatch",
    "property": "minecraft:custom_model_data",
    "fallback": {
      "type": "minecraft:model",
      "model": "minecraft:item/book"
    },
    "entries": [
      {
        "threshold": 12346.0,
        "model": {
          "type": "minecraft:model",
          "model": "minecraft:item/reroll_item"
        }
      }
    ]
  }
}''')
        
    # Paper -> death_certificate (CMD 12347)
    with open(os.path.join(items_dir, "paper.json"), "w") as f:
        f.write('''{
  "model": {
    "type": "minecraft:range_dispatch",
    "property": "minecraft:custom_model_data",
    "fallback": {
      "type": "minecraft:model",
      "model": "minecraft:item/paper"
    },
    "entries": [
      {
        "threshold": 12347.0,
        "model": {
          "type": "minecraft:model",
          "model": "minecraft:item/death_certificate"
        }
      }
    ]
  }
}''')
        
    # 6. Write pack.mcmeta with multi-version format support (34 to 46)
    with open(os.path.join(rp_dir, "pack.mcmeta"), "w") as f:
        f.write('''{
  "pack": {
    "pack_format": 34,
    "supported_formats": [34, 46],
    "description": "Strength SMP custom item textures"
  }
}''')
        
    # Copy pack.png if exists
    pack_png = os.path.join(brain_dir, "media__1779602219850.png")
    if os.path.exists(pack_png):
        shutil.copy(pack_png, os.path.join(rp_dir, "pack.png"))
        
    # 7. Zip up the resource pack
    zip_folder(rp_dir, os.path.join(workspace_dir, "src/main/resources/strengthsmp.zip"))
    zip_folder(rp_dir, os.path.join(workspace_dir, "strengthsmp.zip"))
    
    server_dest = "/home/sagheer/Desktop/Test server/plugins/StrengthSMP/strengthsmp.zip"
    if os.path.exists(os.path.dirname(server_dest)):
        shutil.copy(os.path.join(workspace_dir, "strengthsmp.zip"), server_dest)
        print(f"Directly copied strengthsmp.zip to server folder: {server_dest}")
        
    # 8. Print SHA-1
    sha1 = calculate_sha1(os.path.join(workspace_dir, "strengthsmp.zip"))
    print(f"\n=========================================")
    print(f"SUCCESS: Resource pack generated!")
    print(f"SHA-1 Hash: {sha1}")
    print(f"=========================================")

if __name__ == "__main__":
    main()
