import sys
import os
from PIL import Image, ImageOps

def process_black_bg(input_path, output_path, threshold=20):
    print(f"Processing black background for: {input_path}")
    img = Image.open(input_path).convert("RGBA")
    datas = img.getdata()
    
    new_data = []
    for item in datas:
        # check if it's very close to black
        r, g, b, a = item
        if r < threshold and g < threshold and b < threshold:
            new_data.append((0, 0, 0, 0))
        else:
            # apply a soft alpha gradient if it's close to black to smooth out edges
            # if max(r,g,b) is below 50, we scale alpha down
            max_val = max(r, g, b)
            if max_val < 60:
                scale = (max_val - threshold) / (60 - threshold)
                scale = max(0.0, min(1.0, scale))
                new_data.append((r, g, b, int(255 * scale)))
            else:
                new_data.append((r, g, b, 255))
                
    img.putdata(new_data)
    
    # Trim transparent borders
    bbox = img.getbbox()
    if bbox:
        img = img.crop(bbox)
        
    # Resize to square (e.g. 256x256)
    size = 256
    squared = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    # resize while maintaining aspect ratio
    img.thumbnail((size - 20, size - 20), Image.Resampling.LANCZOS)
    # paste centered
    offset = ((size - img.width) // 2, (size - img.height) // 2)
    squared.paste(img, offset)
    
    squared.save(output_path, "PNG")
    print(f"Saved to: {output_path}")

def process_white_bg(input_path, output_path, threshold=235):
    print(f"Processing white background for: {input_path}")
    img = Image.open(input_path).convert("RGBA")
    datas = img.getdata()
    
    new_data = []
    for item in datas:
        r, g, b, a = item
        # check if it's very close to white
        if r > threshold and g > threshold and b > threshold:
            new_data.append((0, 0, 0, 0))
        else:
            # soft alpha gradient for near-white pixels
            min_val = min(r, g, b)
            if min_val > 210:
                scale = (threshold - min_val) / (threshold - 210)
                scale = max(0.0, min(1.0, scale))
                new_data.append((r, g, b, int(255 * scale)))
            else:
                new_data.append((r, g, b, 255))
                
    img.putdata(new_data)
    
    # Trim transparent borders
    bbox = img.getbbox()
    if bbox:
        img = img.crop(bbox)
        
    # Resize to square (e.g. 256x256)
    size = 256
    squared = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    img.thumbnail((size - 20, size - 20), Image.Resampling.LANCZOS)
    offset = ((size - img.width) // 2, (size - img.height) // 2)
    squared.paste(img, offset)
    
    squared.save(output_path, "PNG")
    print(f"Saved to: {output_path}")

if __name__ == "__main__":
    os.makedirs("/home/sagheer/Desktop/StrengthSMP/resourcepack/assets/minecraft/textures/item", exist_ok=True)
    
    # Paths from generation
    strength_in = "/home/sagheer/.gemini/antigravity/brain/2cc7f399-824e-4256-8129-8ab6da91a3a4/strength_item_texture_1779444602225.png"
    reroll_in = "/home/sagheer/.gemini/antigravity/brain/2cc7f399-824e-4256-8129-8ab6da91a3a4/reroll_item_texture_1779444629442.png"
    death_in = "/home/sagheer/.gemini/antigravity/brain/2cc7f399-824e-4256-8129-8ab6da91a3a4/death_certificate_texture_1779444654720.png"
    
    process_black_bg(strength_in, "/home/sagheer/Desktop/StrengthSMP/resourcepack/assets/minecraft/textures/item/strength_item.png", threshold=15)
    process_black_bg(reroll_in, "/home/sagheer/Desktop/StrengthSMP/resourcepack/assets/minecraft/textures/item/reroll_item.png", threshold=15)
    process_white_bg(death_in, "/home/sagheer/Desktop/StrengthSMP/resourcepack/assets/minecraft/textures/item/death_certificate.png", threshold=245)
