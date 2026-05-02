import os
from PIL import Image

desktop = os.path.join(os.path.expanduser("~"), "Desktop")
images = [
    "reroll_book_showcase",
    "reroll_recipe",
    "strength_item_showcase",
    "strength_recipe"
]

print("Starting image optimization...")

for name in images:
    png_path = os.path.join(desktop, f"{name}.png")
    jpg_path = os.path.join(desktop, f"{name}_small.jpg")
    
    if os.path.exists(png_path):
        try:
            with Image.open(png_path) as img:
                rgb_img = img.convert('RGB')
                rgb_img.save(jpg_path, 'JPEG', quality=80, optimize=True)
                size_kb = os.path.getsize(jpg_path) / 1024
                print(f"Optimized {name} -> {size_kb:.1f} KB")
        except Exception as e:
            print(f"Failed to optimize {name}: {str(e)}")
    else:
        print(f"Could not find {name}.png on Desktop")

print("Optimization complete!")
