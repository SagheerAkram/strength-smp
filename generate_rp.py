#!/usr/bin/env python3
import os
import sys
import zipfile
import shutil
import hashlib
import math
from PIL import Image, ImageDraw

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

def make_book_texture(output_path):
    """Generate a premium 3D shaded book texture sheet with glowing cosmic radial gradients and detailed gold filigree."""
    print(f"Generating premium Book texture to {output_path}...")
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    
    # Colors
    VOID_DARK = (15, 5, 30, 255)       # Outer edge shadow
    PURPLE_DARK = (30, 12, 55, 255)    # Border/Shadow
    PURPLE_EDGE = (45, 20, 80, 255)    # Inside border
    PURPLE_MID = (75, 35, 130, 255)    # Base book color
    PURPLE_GLOW = (130, 65, 200, 255)  # Glowing center base
    
    GOLD_SHADOW = (130, 85, 15, 255)   # Shaded gold
    GOLD_BASE = (215, 165, 45, 255)    # Mid gold
    GOLD_LIGHT = (255, 220, 110, 255)  # Highlighted gold
    
    CREAM = (245, 230, 200, 255)
    CREAM_DARK = (212, 196, 160, 255)
    CREAM_EDGE = (190, 170, 135, 255)
    
    RUNE_PURPLE = (165, 95, 220, 255)
    RUNE_GLOW = (210, 150, 255, 255)
    RUNE_CENTER = (245, 220, 255, 255)

    def blend(c1, c2, t):
        return tuple(int(a * (1 - t) + b * t) for a, b in zip(c1, c2))

    # Helper to draw a shaded cover
    def draw_cover(start_x):
        for y in range(28):
            for x in range(28):
                # Coordinates relative to cover center (13.5, 13.5)
                dx = x - 13.5
                dy = y - 13.5
                dist = math.sqrt(dx*dx + dy*dy)
                max_dist = 18.0
                t = min(dist / max_dist, 1.0)
                
                # Cosmic radial gradient: glow in center, dark void at corners
                if t < 0.4:
                    col = blend(PURPLE_GLOW, PURPLE_MID, t / 0.4)
                else:
                    col = blend(PURPLE_MID, VOID_DARK, (t - 0.4) / 0.6)
                
                # Add a subtle diagonal galactic highlight
                diag = (x - y) / 28.0  # -1.0 to 1.0
                if -0.2 < diag < 0.2:
                    h_factor = (0.2 - abs(diag)) / 0.2 * 30
                    col = (
                        min(col[0] + int(h_factor), 255),
                        min(col[1] + int(h_factor), 255),
                        min(col[2] + int(h_factor * 1.5), 255),
                        255
                    )
                
                img.putpixel((start_x + x, y), col)
                
        # Shaded border
        for x in range(28):
            img.putpixel((start_x + x, 0), VOID_DARK)
            img.putpixel((start_x + x, 27), VOID_DARK)
        for y in range(28):
            img.putpixel((start_x, y), VOID_DARK)
            img.putpixel((start_x + 27, y), VOID_DARK)
            
        # Inner border
        for x in range(1, 27):
            img.putpixel((start_x + x, 1), PURPLE_DARK)
            img.putpixel((start_x + x, 26), PURPLE_DARK)
        for y in range(1, 27):
            img.putpixel((start_x + 1, y), PURPLE_DARK)
            img.putpixel((start_x + 26, y), PURPLE_DARK)

        # 3D shaded gold corner filigrees
        corners = [
            (2,2), (3,2), (4,2), (2,3), (3,3),
            (5,2), (2,5), (4,3), (3,4)
        ]
        for cx, cy in corners:
            img.putpixel((start_x + cx, cy), GOLD_BASE)
            img.putpixel((start_x + 27 - cx, cy), GOLD_BASE)
            img.putpixel((start_x + cx, 27 - cy), GOLD_BASE)
            img.putpixel((start_x + 27 - cx, 27 - cy), GOLD_BASE)
            
        highlights = [(2,2), (3,2), (2,3), (25,2), (25,3), (2,25)]
        shadows = [(4,3), (3,4), (23,3), (24,4), (4,24)]
        for cx, cy in highlights:
            img.putpixel((start_x + cx, cy), GOLD_LIGHT)
        for cx, cy in shadows:
            img.putpixel((start_x + cx, cy), GOLD_SHADOW)

    # Draw both covers
    draw_cover(0)
    draw_cover(28)

    # FRONT COVER REROLL SYMBOL (magical runic circle with glowing core)
    center_x, center_y = 14, 14
    for angle_step in range(40):
        angle = angle_step * (2 * math.pi / 40)
        px = int(center_x + 5.5 * math.cos(angle))
        py = int(center_y + 5.5 * math.sin(angle))
        img.putpixel((px, py), GOLD_BASE)
        if math.sin(angle) < 0:
            img.putpixel((px, py), GOLD_LIGHT)
        else:
            img.putpixel((px, py), GOLD_SHADOW)

    # Glow inside the ring
    for dy in range(-4, 5):
        for dx in range(-4, 5):
            dist = math.sqrt(dx*dx + dy*dy)
            if dist < 4.0:
                t = dist / 4.0
                col = blend(RUNE_GLOW, PURPLE_GLOW, t)
                img.putpixel((center_x + dx, center_y + dy), col)

    # Reroll cycle arrows in the center
    arrows = [
        (14, 11), (15, 11), (16, 12), (16, 13),
        (14, 17), (13, 17), (12, 16), (12, 15),
    ]
    for px, py in arrows:
        img.putpixel((px, py), GOLD_LIGHT)
    heads = [(16, 12), (17, 12), (16, 11), (12, 16), (11, 16), (12, 17)]
    for px, py in heads:
        img.putpixel((px, py), GOLD_LIGHT)
        
    # Central glowing dot
    img.putpixel((14, 14), RUNE_CENTER)
    img.putpixel((13, 14), RUNE_GLOW)
    img.putpixel((15, 14), RUNE_GLOW)
    img.putpixel((14, 13), RUNE_GLOW)
    img.putpixel((14, 15), RUNE_GLOW)

    # BACK COVER SYMBOL (simple gold diamond/shield)
    back_cx, back_cy = 42, 14
    for dy in range(-4, 5):
        for dx in range(-4, 5):
            if abs(dx) + abs(dy) <= 4:
                img.putpixel((back_cx + dx, back_cy + dy), GOLD_BASE)
    for dy in range(-3, 4):
        for dx in range(-3, 4):
            if abs(dx) + abs(dy) <= 3:
                img.putpixel((back_cx + dx, back_cy + dy), PURPLE_MID)
    img.putpixel((back_cx, back_cy), GOLD_LIGHT)

    # SPINE (0,28) to (6,56)
    for y in range(28, 56):
        img.putpixel((0, y), VOID_DARK)
        img.putpixel((1, y), PURPLE_DARK)
        img.putpixel((2, y), PURPLE_MID)
        img.putpixel((3, y), PURPLE_GLOW)
        img.putpixel((4, y), PURPLE_MID)
        img.putpixel((5, y), PURPLE_DARK)
    for x in range(1, 5):
        img.putpixel((x, 32), GOLD_BASE)
        img.putpixel((x, 33), GOLD_LIGHT)
        img.putpixel((x, 50), GOLD_BASE)
        img.putpixel((x, 51), GOLD_LIGHT)
    for px, py in [(3, 40), (2, 41), (3, 41), (4, 41), (3, 42)]:
        img.putpixel((px, py), GOLD_LIGHT)

    # PAGES TOP (6,28) to (34,40)
    for y in range(28, 40):
        for x in range(6, 34):
            img.putpixel((x, y), CREAM)
    for y_line in [30, 32, 34, 36, 38]:
        for x in range(8, 32):
            if (x + y_line) % 3 != 0:
                img.putpixel((x, y_line), CREAM_DARK)

    # PAGES EDGE (6,40) to (34,56)
    for y in range(40, 56):
        for x in range(6, 34):
            img.putpixel((x, y), CREAM if y % 2 == 0 else CREAM_EDGE)
    for x in range(6, 34):
        img.putpixel((x, 40), CREAM_DARK)
        img.putpixel((x, 55), CREAM_DARK)

    # CLASP (48,28) to (56,36)
    for y in range(28, 36):
        for x in range(48, 56):
            img.putpixel((x, y), GOLD_SHADOW)
    for y in range(29, 35):
        for x in range(49, 55):
            img.putpixel((x, y), GOLD_BASE)
    for y in range(30, 34):
        for x in range(50, 54):
            img.putpixel((x, y), (180, 20, 20, 255))
    img.putpixel((51, 31), (255, 100, 100, 255))

    # RUNE GLOW (56,28) to (64,36)
    for y in range(28, 36):
        for x in range(56, 64):
            img.putpixel((x, y), RUNE_PURPLE)
    for y in range(30, 34):
        for x in range(58, 62):
            img.putpixel((x, y), RUNE_GLOW)
    img.putpixel((60, 32), RUNE_CENTER)
    img.putpixel((59, 31), RUNE_CENTER)

    # SOLID PAGE COLOR (48,36) to (56,44)
    for y in range(36, 44):
        for x in range(48, 56):
            img.putpixel((x, y), CREAM)
    for x in range(48, 56):
        img.putpixel((x, 36), CREAM_DARK)
        img.putpixel((x, 43), CREAM_DARK)
    for y in range(36, 44):
        img.putpixel((48, y), CREAM_DARK)
        img.putpixel((55, y), CREAM_DARK)

    # SOLID PURPLE (56,36) to (64,44)
    for y in range(36, 44):
        for x in range(56, 64):
            img.putpixel((x, y), VOID_DARK)

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    img.save(output_path)

def make_scroll_texture(output_path):
    """Generate a clean 64x64 pixel art scroll texture sheet for the Death Certificate."""
    print(f"Generating programmatic Scroll texture to {output_path}...")
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    
    CREAM = (245, 230, 200, 255)
    CREAM_DARK = (212, 196, 160, 255)
    CREAM_LIGHT = (255, 248, 230, 255)
    CREAM_SHADOW = (180, 160, 130, 255)
    WOOD_DARK = (90, 55, 30, 255)
    WOOD_MID = (120, 80, 45, 255)
    WOOD_LIGHT = (150, 105, 65, 255)
    WAX_RED = (180, 30, 30, 255)
    WAX_DARK = (120, 15, 15, 255)
    WAX_LIGHT = (220, 50, 50, 255)
    TEXT_COLOR = (70, 60, 50, 255)
    
    # 1. Main parchment page: px X: 12 to 52, Y: 8 to 56
    for y in range(8, 56):
        for x in range(12, 52):
            noise = (x * 3 + y * 7) % 7 - 3
            r = min(255, max(0, CREAM[0] + noise))
            g = min(255, max(0, CREAM[1] + noise))
            b = min(255, max(0, CREAM[2] + noise))
            img.putpixel((x, y), (r, g, b, 255))
            
    # Draw scroll border lines
    for y in range(8, 56):
        img.putpixel((12, y), CREAM_SHADOW)
        img.putpixel((51, y), CREAM_DARK)
        
    # Draw writing/text on parchment
    for y_line in range(14, 48, 4):
        for x in range(16, 48):
            if (x // 3 + y_line) % 4 != 0:
                img.putpixel((x, y_line), TEXT_COLOR)
                
    # 2. Top Scroll Roll: px X: 8 to 56, Y: 56 to 64
    for y in range(56, 64):
        for x in range(8, 56):
            factor = (y - 56) / 7.0
            r = int(CREAM[0] * (0.8 + 0.4 * factor))
            g = int(CREAM[1] * (0.8 + 0.4 * factor))
            b = int(CREAM[2] * (0.8 + 0.4 * factor))
            img.putpixel((x, y), (r, g, b, 255))
            
    # 3. Bottom Scroll Roll: px X: 8 to 56, Y: 0 to 8
    for y in range(0, 8):
        for x in range(8, 56):
            factor = y / 7.0
            r = int(CREAM[0] * (0.8 + 0.4 * factor))
            g = int(CREAM[1] * (0.8 + 0.4 * factor))
            b = int(CREAM[2] * (0.8 + 0.4 * factor))
            img.putpixel((x, y), (r, g, b, 255))
            
    # 4. Roller Handles: px X: 0 to 4, Y: 8 to 12
    for y in range(8, 12):
        for x in range(0, 4):
            if (x + y) % 2 == 0:
                img.putpixel((x, y), WOOD_MID)
            else:
                img.putpixel((x, y), WOOD_DARK)
    img.putpixel((1, 9), WOOD_LIGHT)
    img.putpixel((2, 10), WOOD_LIGHT)
    
    # 5. Wax Seal: px X: 48 to 60, Y: 48 to 60
    for y in range(48, 60):
        for x in range(48, 60):
            dx = x - 54
            dy = y - 54
            dist = dx*dx + dy*dy
            if dist < 25:
                img.putpixel((x, y), WAX_RED)
            elif dist < 36:
                img.putpixel((x, y), WAX_DARK)
                
    # Draw simple skull/cross inside seal
    img.putpixel((54, 52), WAX_LIGHT)
    img.putpixel((53, 53), WAX_LIGHT)
    img.putpixel((54, 53), WAX_LIGHT)
    img.putpixel((55, 53), WAX_LIGHT)
    img.putpixel((54, 54), WAX_LIGHT)
    img.putpixel((52, 55), WAX_LIGHT)
    img.putpixel((56, 55), WAX_LIGHT)
    img.putpixel((53, 56), WAX_LIGHT)
    img.putpixel((55, 56), WAX_LIGHT)
    
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    img.save(output_path)

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

def make_duelist_sword_texture(output_path):
    print(f"Generating programmatic Sword texture to {output_path}...")
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    CYAN_GLOW = (0, 210, 255, 255)
    CYAN_CORE = (240, 255, 255, 255)
    BLUE_DARK = (0, 50, 150, 255)
    GOLD_BASE = (215, 165, 45, 255)
    GOLD_LIGHT = (255, 220, 110, 255)
    GOLD_DARK = (130, 85, 15, 255)
    STEEL = (160, 160, 160, 255)
    DARK_GREY = (60, 60, 60, 255)

    img.putpixel((3, 28), GOLD_DARK)
    img.putpixel((4, 27), GOLD_BASE)
    
    for i in range(4):
        x = 5 + i
        y = 26 - i
        img.putpixel((x, y), DARK_GREY if i%2==0 else STEEL)
        
    guard = [(8, 22), (9, 21), (9, 22), (10, 22), (8, 23), (7, 23), (10, 21), (11, 20)]
    for gx, gy in guard:
        img.putpixel((gx, gy), GOLD_BASE)
    img.putpixel((9, 22), GOLD_LIGHT)
    img.putpixel((8, 23), GOLD_DARK)
    img.putpixel((11, 20), GOLD_LIGHT)
    img.putpixel((7, 23), GOLD_DARK)
    
    for x in range(11, 27):
        y = 31 - x
        img.putpixel((x, y), CYAN_CORE)
        img.putpixel((x - 1, y), CYAN_GLOW)
        img.putpixel((x, y + 1), CYAN_GLOW)
        img.putpixel((x + 1, y), CYAN_GLOW)
        img.putpixel((x, y - 1), CYAN_GLOW)
        img.putpixel((x - 1, y - 1), BLUE_DARK)
        img.putpixel((x + 1, y + 1), BLUE_DARK)
        if x < 26:
            img.putpixel((x - 2, y), BLUE_DARK)
            img.putpixel((x, y + 2), BLUE_DARK)
            
    img.putpixel((27, 4), CYAN_CORE)
    img.putpixel((26, 4), BLUE_DARK)
    img.putpixel((27, 5), BLUE_DARK)
    
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    img.save(output_path)

def make_heirloom_axe_texture(output_path):
    print(f"Generating programmatic Axe texture to {output_path}...")
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    
    for i in range(22):
        hx = 4 + i
        hy = 27 - i
        img.putpixel((hx, hy), (90, 55, 30, 255) if i % 2 == 0 else (130, 85, 45, 255))
    
    img.putpixel((21, 10), (160, 160, 160, 255))
    img.putpixel((20, 11), (160, 160, 160, 255))
    img.putpixel((22, 9), (160, 160, 160, 255))
    
    RUBY_DARK = (120, 10, 25, 255)
    RUBY_MID = (200, 20, 40, 255)
    RUBY_LIGHT = (255, 100, 120, 255)
    STEEL_EDGE = (235, 235, 240, 255)
    
    for x in range(12, 22):
        for y in range(3, 11):
            dist = abs(x + y - 31)
            if 2 <= dist <= 9 and x <= y + 10:
                col = RUBY_MID
                if dist == 9 or y == 3 or x == 12:
                    col = STEEL_EDGE
                elif dist == 8:
                    col = RUBY_LIGHT
                elif dist < 5:
                    col = RUBY_DARK
                img.putpixel((x, y), col)
                
    for x in range(20, 30):
        for y in range(9, 19):
            dist = abs(x + y - 31)
            if 2 <= dist <= 9 and y <= x + 10:
                col = RUBY_MID
                if dist == 9 or x == 29 or y == 18:
                    col = STEEL_EDGE
                elif dist == 8:
                    col = RUBY_LIGHT
                elif dist < 5:
                    col = RUBY_DARK
                img.putpixel((x, y), col)
                
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    img.save(output_path)

def make_recurve_bow_texture(output_path):
    print(f"Generating programmatic Bow texture to {output_path}...")
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    
    GOLD_BASE = (215, 165, 45, 255)
    GOLD_LIGHT = (255, 220, 110, 255)
    GOLD_DARK = (130, 85, 15, 255)
    EMERALD = (10, 180, 100, 255)
    EMERALD_LIGHT = (50, 240, 150, 255)
    STRING = (220, 225, 235, 180)
    
    points = [
        (8,4, GOLD_DARK), (7,4, GOLD_BASE), (7,5, GOLD_BASE), (6,6, GOLD_LIGHT),
        (6,7, GOLD_BASE), (5,8, GOLD_BASE), (5,9, GOLD_LIGHT), (5,10, GOLD_BASE),
        (4,11, GOLD_BASE), (4,12, GOLD_LIGHT), (4,13, GOLD_BASE),
        (4,14, EMERALD), (4,15, EMERALD_LIGHT), (4,16, EMERALD), (4,17, EMERALD), (4,18, EMERALD),
        (5,15, EMERALD), (5,16, EMERALD_LIGHT), (5,17, EMERALD),
        (4,19, GOLD_BASE), (4,20, GOLD_LIGHT), (4,21, GOLD_BASE),
        (5,22, GOLD_BASE), (5,23, GOLD_LIGHT), (5,24, GOLD_BASE), (6,25, GOLD_BASE),
        (6,26, GOLD_LIGHT), (7,27, GOLD_BASE), (7,28, GOLD_BASE), (8,28, GOLD_DARK)
    ]
    
    for x, y, col in points:
        img.putpixel((x, y), col)
        if col == GOLD_BASE or col == GOLD_LIGHT:
            img.putpixel((x+1, y), GOLD_DARK)
            
    for y in range(4, 29):
        if img.getpixel((8, y))[3] == 0:
            img.putpixel((8, y), STRING)
            
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    img.save(output_path)

def make_heavy_crossbow_texture(output_path):
    print(f"Generating programmatic Crossbow texture to {output_path}...")
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    
    OBSIDIAN = (25, 20, 35, 255)
    OBSIDIAN_LIGHT = (65, 50, 90, 255)
    STEEL = (130, 135, 145, 255)
    STEEL_LIGHT = (195, 200, 205, 255)
    GOLD = (225, 175, 55, 255)
    STRING = (220, 220, 220, 200)
    
    for i in range(18):
        sx = 5 + i
        sy = 26 - i
        img.putpixel((sx, sy), OBSIDIAN if i%2==0 else OBSIDIAN_LIGHT)
        img.putpixel((sx+1, sy), OBSIDIAN)
        
    img.putpixel((5, 26), GOLD)
    img.putpixel((6, 26), GOLD)
    img.putpixel((21, 10), GOLD)
    img.putpixel((22, 9), GOLD)
    
    for i in range(-8, 9):
        lx = 17 + i
        ly = 14 + i
        img.putpixel((lx, ly), STEEL)
        img.putpixel((lx+1, ly-1), STEEL_LIGHT)
        
    for i in range(-8, 9):
        sx = 17 + i
        sy = 14 + i - 3
        if 0 <= sx < 32 and 0 <= sy < 32:
            if img.getpixel((sx, sy))[3] == 0:
                img.putpixel((sx, sy), STRING)
                
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    img.save(output_path)

def make_royal_trident_texture(output_path):
    print(f"Generating programmatic Trident texture to {output_path}...")
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    
    GOLD = (220, 170, 45, 255)
    GOLD_LIGHT = (255, 225, 115, 255)
    GOLD_DARK = (135, 90, 15, 255)
    SAPPHIRE = (0, 140, 245, 255)
    SAPPHIRE_LIGHT = (100, 220, 255, 255)
    
    for i in range(17):
        sx = 4 + i
        sy = 27 - i
        col = GOLD if i % 2 == 0 else SAPPHIRE
        img.putpixel((sx, sy), col)
        img.putpixel((sx+1, sy-1), GOLD_DARK if col == GOLD else SAPPHIRE_LIGHT)
        
    head_base = [(19, 10), (20, 11), (21, 12), (18, 9), (22, 13)]
    for hx, hy in head_base:
        img.putpixel((hx, hy), GOLD)
        
    for i in range(8):
        px = 20 + i
        py = 11 - i
        img.putpixel((px, py), SAPPHIRE if i < 6 else SAPPHIRE_LIGHT)
        
    prong2 = [(18,8), (19,7), (19,6), (20,5), (21,4), (22,3), (23,2)]
    for px, py in prong2:
        img.putpixel((px, py), GOLD if py > 4 else SAPPHIRE)
        img.putpixel((px+1, py), SAPPHIRE_LIGHT if py <= 4 else GOLD_LIGHT)
        
    prong3 = [(22,12), (23,11), (24,11), (25,10), (26,9), (27,8), (28,7)]
    for px, py in prong3:
        img.putpixel((px, py), GOLD if px < 26 else SAPPHIRE)
        img.putpixel((px, py+1), SAPPHIRE_LIGHT if px >= 26 else GOLD_DARK)
        
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    img.save(output_path)

def make_bulwark_shield_texture(output_path):
    print(f"Generating programmatic Shield texture to {output_path}...")
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    
    STEEL = (75, 80, 90, 255)
    STEEL_DARK = (45, 50, 55, 255)
    GOLD = (225, 175, 55, 255)
    GOLD_LIGHT = (255, 220, 110, 255)
    CYAN_GLOW = (0, 210, 255, 255)
    CYAN_DARK = (0, 100, 160, 255)
    
    for y in range(4, 28):
        if y < 18:
            half_w = 9
        else:
            half_w = 9 - (y - 17) // 1.1
            if half_w < 1:
                half_w = 1
                
        for x in range(16 - int(half_w), 17 + int(half_w)):
            is_border = (x == 16 - int(half_w)) or (x == 16 + int(half_w)) or (y == 4) or (y == 27)
            if is_border:
                img.putpixel((x, y), GOLD_LIGHT if x % 2 == 0 else GOLD)
            else:
                img.putpixel((x, y), STEEL if (x+y)%2 == 0 else STEEL_DARK)
                
    for y in range(8, 20):
        img.putpixel((16, y), CYAN_GLOW)
    img.putpixel((15, 10), CYAN_GLOW)
    img.putpixel((17, 10), CYAN_GLOW)
    img.putpixel((14, 11), CYAN_GLOW)
    img.putpixel((18, 11), CYAN_GLOW)
    img.putpixel((13, 12), CYAN_GLOW)
    img.putpixel((19, 12), CYAN_GLOW)
    img.putpixel((14, 13), CYAN_DARK)
    img.putpixel((18, 13), CYAN_DARK)
    img.putpixel((15, 14), CYAN_DARK)
    img.putpixel((17, 14), CYAN_DARK)
    
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    img.save(output_path)

def get_3d_crossbow_model(state="idle"):
    import json
    elements = [
        # Stock (body)
        {
            "name": "stock",
            "from": [7.0, 7.0, 0.0],
            "to": [9.0, 9.0, 16.0],
            "faces": {
                "north": {"uv": [0, 0, 2, 2], "texture": "#texture"},
                "south": {"uv": [0, 0, 2, 2], "texture": "#texture"},
                "west":  {"uv": [0, 0, 2, 16], "texture": "#texture"},
                "east":  {"uv": [0, 0, 2, 16], "texture": "#texture"},
                "up":    {"uv": [0, 0, 2, 16], "texture": "#texture"},
                "down":  {"uv": [0, 0, 2, 16], "texture": "#texture"}
            }
        },
        # Grip/Handle
        {
            "name": "grip",
            "from": [7.5, 4.0, 11.0],
            "to": [8.5, 7.0, 13.0],
            "faces": {
                "north": {"uv": [0, 0, 1, 3], "texture": "#texture"},
                "south": {"uv": [0, 0, 1, 3], "texture": "#texture"},
                "west":  {"uv": [0, 0, 2, 3], "texture": "#texture"},
                "east":  {"uv": [0, 0, 2, 3], "texture": "#texture"},
                "up":    {"uv": [0, 0, 1, 2], "texture": "#texture"},
                "down":  {"uv": [0, 0, 1, 2], "texture": "#texture"}
            }
        },
        # Left Limb
        {
            "name": "limb_left",
            "from": [-1.0, 7.5, 2.0],
            "to": [7.0, 8.5, 3.0],
            "faces": {
                "north": {"uv": [0, 0, 8, 1], "texture": "#texture"},
                "south": {"uv": [0, 0, 8, 1], "texture": "#texture"},
                "west":  {"uv": [0, 0, 1, 1], "texture": "#texture"},
                "east":  {"uv": [0, 0, 1, 1], "texture": "#texture"},
                "up":    {"uv": [0, 0, 8, 1], "texture": "#texture"},
                "down":  {"uv": [0, 0, 8, 1], "texture": "#texture"}
            }
        },
        # Right Limb
        {
            "name": "limb_right",
            "from": [9.0, 7.5, 2.0],
            "to": [17.0, 8.5, 3.0],
            "faces": {
                "north": {"uv": [0, 0, 8, 1], "texture": "#texture"},
                "south": {"uv": [0, 0, 8, 1], "texture": "#texture"},
                "west":  {"uv": [0, 0, 1, 1], "texture": "#texture"},
                "east":  {"uv": [0, 0, 1, 1], "texture": "#texture"},
                "up":    {"uv": [0, 0, 8, 1], "texture": "#texture"},
                "down":  {"uv": [0, 0, 8, 1], "texture": "#texture"}
            }
        }
    ]
    
    # Add String elements based on pulling state
    if state == "idle":
        elements.append({
            "name": "string",
            "from": [-0.5, 8.0, 2.2],
            "to": [16.5, 8.2, 2.8],
            "faces": {
                "north": {"uv": [0, 0, 16, 1], "texture": "#texture"},
                "south": {"uv": [0, 0, 16, 1], "texture": "#texture"},
                "up":    {"uv": [0, 0, 16, 1], "texture": "#texture"},
                "down":  {"uv": [0, 0, 16, 1], "texture": "#texture"}
            }
        })
    elif state == "pulling_0":
        elements.extend([
            {
                "name": "string_left",
                "from": [-0.5, 8.0, 2.2],
                "to": [8.0, 8.2, 6.0],
                "faces": {
                    "north": {"uv": [0, 0, 8, 1], "texture": "#texture"},
                    "south": {"uv": [0, 0, 8, 1], "texture": "#texture"},
                    "up":    {"uv": [0, 0, 8, 1], "texture": "#texture"},
                    "down":  {"uv": [0, 0, 8, 1], "texture": "#texture"}
                }
            },
            {
                "name": "string_right",
                "from": [8.0, 8.0, 2.2],
                "to": [16.5, 8.2, 6.0],
                "faces": {
                    "north": {"uv": [0, 0, 8, 1], "texture": "#texture"},
                    "south": {"uv": [0, 0, 8, 1], "texture": "#texture"},
                    "up":    {"uv": [0, 0, 8, 1], "texture": "#texture"},
                    "down":  {"uv": [0, 0, 8, 1], "texture": "#texture"}
                }
            }
        ])
    elif state == "pulling_1":
        elements.extend([
            {
                "name": "string_left",
                "from": [-0.5, 8.0, 2.2],
                "to": [8.0, 8.2, 10.0],
                "faces": {
                    "north": {"uv": [0, 0, 8, 1], "texture": "#texture"},
                    "south": {"uv": [0, 0, 8, 1], "texture": "#texture"},
                    "up":    {"uv": [0, 0, 8, 1], "texture": "#texture"},
                    "down":  {"uv": [0, 0, 8, 1], "texture": "#texture"}
                }
            },
            {
                "name": "string_right",
                "from": [8.0, 8.0, 2.2],
                "to": [16.5, 8.2, 10.0],
                "faces": {
                    "north": {"uv": [0, 0, 8, 1], "texture": "#texture"},
                    "south": {"uv": [0, 0, 8, 1], "texture": "#texture"},
                    "up":    {"uv": [0, 0, 8, 1], "texture": "#texture"},
                    "down":  {"uv": [0, 0, 8, 1], "texture": "#texture"}
                }
            }
        ])
    elif state in ["pulling_2", "arrow", "rocket"]:
        elements.extend([
            {
                "name": "string_left",
                "from": [-0.5, 8.0, 2.2],
                "to": [8.0, 8.2, 14.0],
                "faces": {
                    "north": {"uv": [0, 0, 8, 1], "texture": "#texture"},
                    "south": {"uv": [0, 0, 8, 1], "texture": "#texture"},
                    "up":    {"uv": [0, 0, 8, 1], "texture": "#texture"},
                    "down":  {"uv": [0, 0, 8, 1], "texture": "#texture"}
                }
            },
            {
                "name": "string_right",
                "from": [8.0, 8.0, 2.2],
                "to": [16.5, 8.2, 14.0],
                "faces": {
                    "north": {"uv": [0, 0, 8, 1], "texture": "#texture"},
                    "south": {"uv": [0, 0, 8, 1], "texture": "#texture"},
                    "up":    {"uv": [0, 0, 8, 1], "texture": "#texture"},
                    "down":  {"uv": [0, 0, 8, 1], "texture": "#texture"}
                }
            }
        ])
        
        if state == "arrow":
            elements.append({
                "name": "arrow",
                "from": [7.75, 8.0, 2.0],
                "to": [8.25, 8.5, 14.0],
                "faces": {
                    "north": {"uv": [0, 0, 1, 1], "texture": "#texture"},
                    "south": {"uv": [0, 0, 1, 1], "texture": "#texture"},
                    "west":  {"uv": [0, 0, 1, 12], "texture": "#texture"},
                    "east":  {"uv": [0, 0, 1, 12], "texture": "#texture"},
                    "up":    {"uv": [0, 0, 1, 12], "texture": "#texture"},
                    "down":  {"uv": [0, 0, 1, 12], "texture": "#texture"}
                }
            })
        elif state == "rocket":
            elements.append({
                "name": "rocket",
                "from": [7.5, 7.5, 2.0],
                "to": [8.5, 8.5, 10.0],
                "faces": {
                    "north": {"uv": [0, 0, 1, 1], "texture": "#texture"},
                    "south": {"uv": [0, 0, 1, 1], "texture": "#texture"},
                    "west":  {"uv": [0, 0, 1, 8], "texture": "#texture"},
                    "east":  {"uv": [0, 0, 1, 8], "texture": "#texture"},
                    "up":    {"uv": [0, 0, 1, 8], "texture": "#texture"},
                    "down":  {"uv": [0, 0, 1, 8], "texture": "#texture"}
                }
            })
            
    model_dict = {
        "textures": {
            "texture": "minecraft:item/heavy_crossbow",
            "particle": "minecraft:item/heavy_crossbow"
        },
        "elements": elements,
        "display": {
            "thirdperson_righthand": {
                "rotation": [ -90, 0, 0 ],
                "translation": [ 0, 1, 3 ],
                "scale": [ 0.9, 0.9, 0.9 ]
            },
            "thirdperson_lefthand": {
                "rotation": [ -90, 0, 0 ],
                "translation": [ 0, 1, 3 ],
                "scale": [ 0.9, 0.9, 0.9 ]
            },
            "firstperson_righthand": {
                "rotation": [ -90, 0, 0 ],
                "translation": [ 0, 3, 2 ],
                "scale": [ 0.8, 0.8, 0.8 ]
            },
            "firstperson_lefthand": {
                "rotation": [ -90, 0, 0 ],
                "translation": [ 0, 3, 2 ],
                "scale": [ 0.8, 0.8, 0.8 ]
            },
            "gui": {
                "rotation": [ 15, -45, 0 ],
                "translation": [ 0, 0, 0 ],
                "scale": [ 0.85, 0.85, 0.85 ]
            },
            "ground": {
                "rotation": [ 0, 0, 0 ],
                "translation": [ 0, 2, 0 ],
                "scale": [ 0.7, 0.7, 0.7 ]
            },
            "fixed": {
                "rotation": [ 0, 180, 0 ],
                "translation": [ 0, 0, 0 ],
                "scale": [ 1.0, 1.0, 1.0 ]
            }
        }
    }
    return json.dumps(model_dict, indent=2)

def copy_3d_harvested_assets(rp_dir):
    extracted_models_dir = "/home/sagheer/Desktop/StrengthSMP/scratch/extracted_assets/models"
    extracted_textures_dir = "/home/sagheer/Desktop/StrengthSMP/scratch/extracted_assets/textures"
    wm_resources_dir = "/home/sagheer/Desktop/StrengthSMP/scratch/wm_resources"
    
    dest_models_dir = os.path.join(rp_dir, "assets/minecraft/models/item")
    dest_textures_dir = os.path.join(rp_dir, "assets/minecraft/textures/item")
    
    # Map plugin names to harvested 3D model filenames
    mapping = {
        "duelist_sword": "katana",
        "claymore_sword": "greatsword_mineral",
        "twinblade_sword": "dod_foul_blade",
        "heirloom_axe": "old_world_damaged_axe",
        "royal_trident": "pso2_lance1",
        "scythe_trident": "young_kitsune_sword",
        "bulwark_shield": "pso2_shield1"
    }
    
    # Textures used by these models that need to be copied
    textures_to_copy = [
        "katana.png",
        "greatsword_mineral_blade.png",
        "greatsword_mineral_blade2.png",
        "dod_foul_blade_tex.png",
        "old_world_damaged_axe.png",
        "pso_sword1_laser.png",
        "pso_sword1_handle.png",
        "young_kitsune_sword_tex.png"
    ]
    
    # 1. Copy and process models (re-pointing texture namespace)
    for plugin_name, harvested_name in mapping.items():
        src_path = os.path.join(extracted_models_dir, f"{harvested_name}.json")
        dst_path = os.path.join(dest_models_dir, f"{plugin_name}.json")
        
        if os.path.exists(src_path):
            with open(src_path, "r") as f:
                content = f.read()
            
            # Replace strengthsmp:item/ prefix with minecraft:item/
            content = content.replace("strengthsmp:item/", "minecraft:item/")
            
            with open(dst_path, "w") as f:
                f.write(content)
            print(f"Copied & processed 3D model: {harvested_name} -> {plugin_name}")
            
    # 2. Copy textures
    for tex_file in textures_to_copy:
        src_path = os.path.join(extracted_textures_dir, tex_file)
        dst_path = os.path.join(dest_textures_dir, tex_file)
        if os.path.exists(src_path):
            shutil.copy2(src_path, dst_path)
            print(f"Copied texture: {tex_file} -> {dst_path}")
            
    # 3. Copy and process Artemis bow as Recurve Bow (with pulling animations)
    bow_src_models = os.path.join(wm_resources_dir, "assets/weaponmaster/models/artemis")
    bow_src_textures = os.path.join(wm_resources_dir, "assets/weaponmaster/textures/item")
    
    # Copy Recurve Bow textures
    shutil.copy2(os.path.join(bow_src_textures, "artemis.png"), os.path.join(dest_textures_dir, "recurve_bow.png"))
    shutil.copy2(os.path.join(bow_src_textures, "artemis_pulling_0.png"), os.path.join(dest_textures_dir, "recurve_bow_pulling_0.png"))
    shutil.copy2(os.path.join(bow_src_textures, "artemis_pulling_1.png"), os.path.join(dest_textures_dir, "recurve_bow_pulling_1.png"))
    shutil.copy2(os.path.join(bow_src_textures, "artemis_pulling_2.png"), os.path.join(dest_textures_dir, "recurve_bow_pulling_2.png"))
    print("Copied Recurve Bow textures (artemis -> recurve_bow)")
    
    # Copy Recurve Bow base model
    shutil.copy2(os.path.join(bow_src_models, "bow_base.json"), os.path.join(dest_models_dir, "recurve_bow_base.json"))
    
    # Process and write bow models
    bow_mapping = {
        "artemis.json": "recurve_bow.json",
        "artemis_pulling_0.json": "recurve_bow_pulling_0.json",
        "artemis_pulling_1.json": "recurve_bow_pulling_1.json",
        "artemis_pulling_2.json": "recurve_bow_pulling_2.json"
    }
    
    for src_name, dst_name in bow_mapping.items():
        src_path = os.path.join(bow_src_models, src_name)
        dst_path = os.path.join(dest_models_dir, dst_name)
        if os.path.exists(src_path):
            with open(src_path, "r") as f:
                content = f.read()
            
            # Replace weaponmaster:artemis/bow_base with minecraft:item/recurve_bow_base
            content = content.replace("weaponmaster:artemis/bow_base", "minecraft:item/recurve_bow_base")
            # Replace weaponmaster:item/artemis with minecraft:item/recurve_bow
            content = content.replace("weaponmaster:item/artemis", "minecraft:item/recurve_bow")
            
            with open(dst_path, "w") as f:
                f.write(content)
            print(f"Processed bow model: {src_name} -> {dst_name}")
            
    # 4. Generate and write 3D Heavy Crossbow models
    with open(os.path.join(dest_models_dir, "heavy_crossbow.json"), "w") as f:
        f.write(get_3d_crossbow_model("idle"))
    with open(os.path.join(dest_models_dir, "heavy_crossbow_pulling_0.json"), "w") as f:
        f.write(get_3d_crossbow_model("pulling_0"))
    with open(os.path.join(dest_models_dir, "heavy_crossbow_pulling_1.json"), "w") as f:
        f.write(get_3d_crossbow_model("pulling_1"))
    with open(os.path.join(dest_models_dir, "heavy_crossbow_pulling_2.json"), "w") as f:
        f.write(get_3d_crossbow_model("pulling_2"))
    with open(os.path.join(dest_models_dir, "heavy_crossbow_arrow.json"), "w") as f:
        f.write(get_3d_crossbow_model("arrow"))
    with open(os.path.join(dest_models_dir, "heavy_crossbow_firework.json"), "w") as f:
        f.write(get_3d_crossbow_model("rocket"))
    print("Generated 3D Heavy Crossbow models (all states)")

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
        
    # 2. Process/Generate high-quality source textures
    strength_in = os.path.join(brain_dir, "strength_orb_new_1779605101241.png")
    process_texture(strength_in, os.path.join(textures_dir, "strength_item.png"), size=64, threshold=30)
    
    # Generate Reroll Book and Death Certificate textures programmatically (ensures no text corruption!)
    make_book_texture(os.path.join(textures_dir, "reroll_item.png"))
    make_scroll_texture(os.path.join(textures_dir, "death_certificate.png"))
    make_duelist_sword_texture(os.path.join(textures_dir, "duelist_sword.png"))
    make_heirloom_axe_texture(os.path.join(textures_dir, "heirloom_axe.png"))
    make_recurve_bow_texture(os.path.join(textures_dir, "recurve_bow.png"))
    make_heavy_crossbow_texture(os.path.join(textures_dir, "heavy_crossbow.png"))
    make_royal_trident_texture(os.path.join(textures_dir, "royal_trident.png"))
    make_bulwark_shield_texture(os.path.join(textures_dir, "bulwark_shield.png"))
    
    # 3. Write custom item model files (referenced by both overrides and range_dispatch)
    models = {
        "strength_item": '''{
  "textures": {
    "texture": "minecraft:item/strength_item"
  },
  "display": {
    "thirdperson_righthand": {
      "rotation": [ 0, 90, 0 ],
      "translation": [ 0, 3, 1 ],
      "scale": [ 0.75, 0.75, 0.75 ]
    },
    "thirdperson_lefthand": {
      "rotation": [ 0, 90, 0 ],
      "translation": [ 0, 3, 1 ],
      "scale": [ 0.75, 0.75, 0.75 ]
    },
    "firstperson_righthand": {
      "rotation": [ 0, 90, 0 ],
      "translation": [ 0, 3.2, 1.15 ],
      "scale": [ 0.75, 0.75, 0.75 ]
    },
    "firstperson_lefthand": {
      "rotation": [ 0, 90, 0 ],
      "translation": [ 0, 3.2, 1.15 ],
      "scale": [ 0.75, 0.75, 0.75 ]
    },
    "ground": {
      "rotation": [ 0, 0, 0 ],
      "translation": [ 0, 2, 0 ],
      "scale": [ 0.5, 0.5, 0.5 ]
    },
    "gui": {
      "rotation": [ 30, 45, 0 ],
      "translation": [ 0, 0, 0 ],
      "scale": [ 1.65, 1.65, 1.65 ]
    },
    "head": {
      "rotation": [ 0, 180, 0 ],
      "translation": [ 0, 13, 7 ],
      "scale": [ 1, 1, 1 ]
    },
    "fixed": {
      "rotation": [ 0, 180, 0 ],
      "translation": [ 0, 0, 0 ],
      "scale": [ 0.5, 0.5, 0.5 ]
    }
  },
  "elements": [
    {
      "comment": "Core Gem",
      "from": [ 6, 5, 6 ],
      "to": [ 10, 11, 10 ],
      "faces": {
        "down":  { "uv": [ 6, 6, 10, 10 ], "texture": "#texture" },
        "up":    { "uv": [ 6, 6, 10, 10 ], "texture": "#texture" },
        "north": { "uv": [ 6, 5, 10, 11 ], "texture": "#texture" },
        "south": { "uv": [ 6, 5, 10, 11 ], "texture": "#texture" },
        "west":  { "uv": [ 6, 5, 10, 11 ], "texture": "#texture" },
        "east":  { "uv": [ 6, 5, 10, 11 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Core Top Cap",
      "from": [ 7, 11, 7 ],
      "to": [ 9, 13, 9 ],
      "faces": {
        "up":    { "uv": [ 7, 7, 9, 9 ], "texture": "#texture" },
        "north": { "uv": [ 7, 11, 9, 13 ], "texture": "#texture" },
        "south": { "uv": [ 7, 11, 9, 13 ], "texture": "#texture" },
        "west":  { "uv": [ 7, 11, 9, 13 ], "texture": "#texture" },
        "east":  { "uv": [ 7, 11, 9, 13 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Core Bottom Cap",
      "from": [ 7, 3, 7 ],
      "to": [ 9, 5, 9 ],
      "faces": {
        "down":  { "uv": [ 7, 7, 9, 9 ], "texture": "#texture" },
        "north": { "uv": [ 7, 3, 9, 5 ], "texture": "#texture" },
        "south": { "uv": [ 7, 3, 9, 5 ], "texture": "#texture" },
        "west":  { "uv": [ 7, 3, 9, 5 ], "texture": "#texture" },
        "east":  { "uv": [ 7, 3, 9, 5 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Horizontal Ring West",
      "from": [ 3, 7.5, 3 ],
      "to": [ 4, 8.5, 13 ],
      "faces": {
        "down":  { "uv": [ 3, 3, 4, 13 ], "texture": "#texture" },
        "up":    { "uv": [ 3, 3, 4, 13 ], "texture": "#texture" },
        "north": { "uv": [ 3, 7, 4, 9 ], "texture": "#texture" },
        "south": { "uv": [ 3, 7, 4, 9 ], "texture": "#texture" },
        "west":  { "uv": [ 3, 7, 13, 9 ], "texture": "#texture" },
        "east":  { "uv": [ 3, 7, 13, 9 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Horizontal Ring East",
      "from": [ 12, 7.5, 3 ],
      "to": [ 13, 8.5, 13 ],
      "faces": {
        "down":  { "uv": [ 12, 3, 13, 13 ], "texture": "#texture" },
        "up":    { "uv": [ 12, 3, 13, 13 ], "texture": "#texture" },
        "north": { "uv": [ 12, 7, 13, 9 ], "texture": "#texture" },
        "south": { "uv": [ 12, 7, 13, 9 ], "texture": "#texture" },
        "west":  { "uv": [ 3, 7, 13, 9 ], "texture": "#texture" },
        "east":  { "uv": [ 3, 7, 13, 9 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Horizontal Ring North",
      "from": [ 4, 7.5, 3 ],
      "to": [ 12, 8.5, 4 ],
      "faces": {
        "down":  { "uv": [ 4, 3, 12, 4 ], "texture": "#texture" },
        "up":    { "uv": [ 4, 3, 12, 4 ], "texture": "#texture" },
        "north": { "uv": [ 4, 7, 12, 9 ], "texture": "#texture" },
        "south": { "uv": [ 4, 7, 12, 9 ], "texture": "#texture" },
        "west":  { "uv": [ 3, 7, 4, 9 ], "texture": "#texture" },
        "east":  { "uv": [ 3, 7, 4, 9 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Horizontal Ring South",
      "from": [ 4, 7.5, 12 ],
      "to": [ 12, 8.5, 13 ],
      "faces": {
        "down":  { "uv": [ 4, 12, 12, 13 ], "texture": "#texture" },
        "up":    { "uv": [ 4, 12, 12, 13 ], "texture": "#texture" },
        "north": { "uv": [ 4, 7, 12, 9 ], "texture": "#texture" },
        "south": { "uv": [ 4, 7, 12, 9 ], "texture": "#texture" },
        "west":  { "uv": [ 12, 7, 13, 9 ], "texture": "#texture" },
        "east":  { "uv": [ 12, 7, 13, 9 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Vertical Ring West",
      "from": [ 3.5, 3.5, 7.5 ],
      "to": [ 4.5, 12.5, 8.5 ],
      "faces": {
        "down":  { "uv": [ 3, 7, 4, 8 ], "texture": "#texture" },
        "up":    { "uv": [ 3, 7, 4, 8 ], "texture": "#texture" },
        "north": { "uv": [ 3, 3, 4, 12 ], "texture": "#texture" },
        "south": { "uv": [ 3, 3, 4, 12 ], "texture": "#texture" },
        "west":  { "uv": [ 7, 3, 8, 12 ], "texture": "#texture" },
        "east":  { "uv": [ 7, 3, 8, 12 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Vertical Ring East",
      "from": [ 11.5, 3.5, 7.5 ],
      "to": [ 12.5, 12.5, 8.5 ],
      "faces": {
        "down":  { "uv": [ 11, 7, 12, 8 ], "texture": "#texture" },
        "up":    { "uv": [ 11, 7, 12, 8 ], "texture": "#texture" },
        "north": { "uv": [ 11, 3, 12, 12 ], "texture": "#texture" },
        "south": { "uv": [ 11, 3, 12, 12 ], "texture": "#texture" },
        "west":  { "uv": [ 7, 3, 8, 12 ], "texture": "#texture" },
        "east":  { "uv": [ 7, 3, 8, 12 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Vertical Ring Top",
      "from": [ 4.5, 11.5, 7.5 ],
      "to": [ 11.5, 12.5, 8.5 ],
      "faces": {
        "down":  { "uv": [ 4, 7, 11, 8 ], "texture": "#texture" },
        "up":    { "uv": [ 4, 7, 11, 8 ], "texture": "#texture" },
        "north": { "uv": [ 4, 11, 11, 12 ], "texture": "#texture" },
        "south": { "uv": [ 4, 11, 11, 12 ], "texture": "#texture" },
        "west":  { "uv": [ 7, 11, 8, 12 ], "texture": "#texture" },
        "east":  { "uv": [ 7, 11, 8, 12 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Vertical Ring Bottom",
      "from": [ 4.5, 3.5, 7.5 ],
      "to": [ 11.5, 4.5, 8.5 ],
      "faces": {
        "down":  { "uv": [ 4, 7, 11, 8 ], "texture": "#texture" },
        "up":    { "uv": [ 4, 7, 11, 8 ], "texture": "#texture" },
        "north": { "uv": [ 4, 3, 11, 4 ], "texture": "#texture" },
        "south": { "uv": [ 4, 3, 11, 4 ], "texture": "#texture" },
        "west":  { "uv": [ 7, 3, 8, 4 ], "texture": "#texture" },
        "east":  { "uv": [ 7, 3, 8, 4 ], "texture": "#texture" }
      }
    }
  ]
}''',
        "reroll_item": '''{
  "textures": {
    "texture": "minecraft:item/reroll_item"
  },
  "display": {
    "thirdperson_righthand": {
      "rotation": [ 75, 45, 0 ],
      "translation": [ 0, 1.0, 1.5 ],
      "scale": [ 0.75, 0.75, 0.75 ]
    },
    "thirdperson_lefthand": {
      "rotation": [ 75, 45, 0 ],
      "translation": [ 0, 1.0, 1.5 ],
      "scale": [ 0.75, 0.75, 0.75 ]
    },
    "firstperson_righthand": {
      "rotation": [ 30, 45, 0 ],
      "translation": [ 0, 1.5, 1.0 ],
      "scale": [ 0.75, 0.75, 0.75 ]
    },
    "firstperson_lefthand": {
      "rotation": [ 30, 45, 0 ],
      "translation": [ 0, 1.5, 1.0 ],
      "scale": [ 0.75, 0.75, 0.75 ]
    },
    "ground": {
      "rotation": [ 90, 0, 0 ],
      "translation": [ 0, 2, 0 ],
      "scale": [ 0.5, 0.5, 0.5 ]
    },
    "gui": {
      "rotation": [ 30, 45, 0 ],
      "translation": [ 0, 0, 0 ],
      "scale": [ 1.3, 1.3, 1.3 ]
    },
    "head": {
      "rotation": [ 0, 180, 0 ],
      "translation": [ 0, 13, 7 ],
      "scale": [ 1, 1, 1 ]
    },
    "fixed": {
      "rotation": [ 0, 180, 0 ],
      "translation": [ 0, 0, 0 ],
      "scale": [ 0.5, 0.5, 0.5 ]
    }
  },
  "elements": [
    {
      "comment": "Back Cover",
      "from": [ 4, 6.5, 4 ],
      "to": [ 12, 7.2, 12 ],
      "faces": {
        "down":  { "uv": [ 7, 0, 14, 7 ], "texture": "#texture" },
        "up":    { "uv": [ 14, 9, 16, 11 ], "texture": "#texture" },
        "north": { "uv": [ 14, 9, 16, 11 ], "texture": "#texture" },
        "south": { "uv": [ 14, 9, 16, 11 ], "texture": "#texture" },
        "west":  { "uv": [ 14, 9, 16, 11 ], "texture": "#texture" },
        "east":  { "uv": [ 14, 9, 16, 11 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Front Cover",
      "from": [ 4, 8.8, 4 ],
      "to": [ 12, 9.5, 12 ],
      "faces": {
        "down":  { "uv": [ 14, 9, 16, 11 ], "texture": "#texture" },
        "up":    { "uv": [ 0, 0, 7, 7 ], "texture": "#texture" },
        "north": { "uv": [ 14, 9, 16, 11 ], "texture": "#texture" },
        "south": { "uv": [ 14, 9, 16, 11 ], "texture": "#texture" },
        "west":  { "uv": [ 14, 9, 16, 11 ], "texture": "#texture" },
        "east":  { "uv": [ 14, 9, 16, 11 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Spine",
      "from": [ 3.2, 6.5, 4 ],
      "to": [ 4.0, 9.5, 12 ],
      "faces": {
        "down":  { "uv": [ 14, 9, 16, 11 ], "texture": "#texture" },
        "up":    { "uv": [ 14, 9, 16, 11 ], "texture": "#texture" },
        "north": { "uv": [ 14, 9, 16, 11 ], "texture": "#texture" },
        "south": { "uv": [ 14, 9, 16, 11 ], "texture": "#texture" },
        "west":  { "uv": [ 0, 7, 1.5, 14 ], "texture": "#texture" },
        "east":  { "uv": [ 14, 9, 16, 11 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Pages",
      "from": [ 4, 7.2, 4.5 ],
      "to": [ 11.5, 8.8, 11.5 ],
      "faces": {
        "down":  { "uv": [ 1.5, 7, 8.5, 10 ], "texture": "#texture" },
        "up":    { "uv": [ 1.5, 7, 8.5, 10 ], "texture": "#texture" },
        "north": { "uv": [ 1.5, 10, 8.5, 14 ], "texture": "#texture" },
        "south": { "uv": [ 1.5, 10, 8.5, 14 ], "texture": "#texture" },
        "west":  { "uv": [ 12, 9, 14, 11 ], "texture": "#texture" },
        "east":  { "uv": [ 1.5, 10, 8.5, 14 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Clasp",
      "from": [ 12, 7.5, 7 ],
      "to": [ 12.8, 8.5, 9 ],
      "faces": {
        "down":  { "uv": [ 12, 7, 14, 9 ], "texture": "#texture" },
        "up":    { "uv": [ 12, 7, 14, 9 ], "texture": "#texture" },
        "north": { "uv": [ 12, 7, 14, 9 ], "texture": "#texture" },
        "south": { "uv": [ 12, 7, 14, 9 ], "texture": "#texture" },
        "west":  { "uv": [ 12, 7, 14, 9 ], "texture": "#texture" },
        "east":  { "uv": [ 12, 7, 14, 9 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Floating Rune 1",
      "from": [ 4.5, 10.5, 5.5 ],
      "to": [ 5.3, 11.3, 6.3 ],
      "faces": {
        "north": { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" },
        "south": { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" },
        "west":  { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" },
        "east":  { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" },
        "up":    { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" },
        "down":  { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Floating Rune 2",
      "from": [ 11.5, 10.2, 10.2 ],
      "to": [ 12.3, 11.0, 11.0 ],
      "faces": {
        "north": { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" },
        "south": { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" },
        "west":  { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" },
        "east":  { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" },
        "up":    { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" },
        "down":  { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Floating Rune 3",
      "from": [ 5.0, 5.0, 10.5 ],
      "to": [ 5.8, 5.8, 11.3 ],
      "faces": {
        "north": { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" },
        "south": { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" },
        "west":  { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" },
        "east":  { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" },
        "up":    { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" },
        "down":  { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Floating Rune 4",
      "from": [ 11.0, 5.2, 4.5 ],
      "to": [ 11.8, 6.0, 5.3 ],
      "faces": {
        "north": { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" },
        "south": { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" },
        "west":  { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" },
        "east":  { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" },
        "up":    { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" },
        "down":  { "uv": [ 14, 7, 16, 9 ], "texture": "#texture" }
      }
    }
  ]
}''',
        "death_certificate": '''{
  "textures": {
    "texture": "minecraft:item/death_certificate"
  },
  "display": {
    "thirdperson_righthand": {
      "rotation": [ 25, 205, -10 ],
      "translation": [ 0, 2.5, 1 ],
      "scale": [ 0.75, 0.75, 0.75 ]
    },
    "thirdperson_lefthand": {
      "rotation": [ 25, 205, -10 ],
      "translation": [ 0, 2.5, 1 ],
      "scale": [ 0.75, 0.75, 0.75 ]
    },
    "firstperson_righthand": {
      "rotation": [ 25, 180, -10 ],
      "translation": [ 0, 1.5, -2 ],
      "scale": [ 0.85, 0.85, 0.85 ]
    },
    "firstperson_lefthand": {
      "rotation": [ 25, 180, -10 ],
      "translation": [ 0, 1.5, -2 ],
      "scale": [ 0.85, 0.85, 0.85 ]
    },
    "ground": {
      "rotation": [ 0, 0, 0 ],
      "translation": [ 0, 2, 0 ],
      "scale": [ 0.6, 0.6, 0.6 ]
    },
    "gui": {
      "rotation": [ 30, 45, 0 ],
      "translation": [ 0, 0, 0 ],
      "scale": [ 1.35, 1.35, 1.35 ]
    },
    "head": {
      "rotation": [ 0, 180, 0 ],
      "translation": [ 0, 13, 7 ],
      "scale": [ 1, 1, 1 ]
    },
    "fixed": {
      "rotation": [ 0, 180, 0 ],
      "translation": [ 0, 0, 0 ],
      "scale": [ 0.5, 0.5, 0.5 ]
    }
  },
  "elements": [
    {
      "comment": "Main Parchment Page",
      "from": [ 3, 2, 7.9 ],
      "to": [ 13, 14, 8.1 ],
      "faces": {
        "down":  { "uv": [ 3, 2, 13, 3 ], "texture": "#texture" },
        "up":    { "uv": [ 3, 2, 13, 3 ], "texture": "#texture" },
        "north": { "uv": [ 3, 2, 13, 14 ], "texture": "#texture" },
        "south": { "uv": [ 13, 2, 3, 14 ], "texture": "#texture" },
        "west":  { "uv": [ 3, 2, 4, 14 ], "texture": "#texture" },
        "east":  { "uv": [ 3, 2, 4, 14 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Top Scroll Roll",
      "from": [ 2.5, 13.5, 7.0 ],
      "to": [ 13.5, 14.5, 9.0 ],
      "faces": {
        "down":  { "uv": [ 2, 14, 14, 16 ], "texture": "#texture" },
        "up":    { "uv": [ 2, 14, 14, 16 ], "texture": "#texture" },
        "north": { "uv": [ 2, 14, 14, 16 ], "texture": "#texture" },
        "south": { "uv": [ 2, 14, 14, 16 ], "texture": "#texture" },
        "west":  { "uv": [ 0, 14, 2, 16 ], "texture": "#texture" },
        "east":  { "uv": [ 0, 14, 2, 16 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Bottom Scroll Roll",
      "from": [ 2.5, 1.5, 7.0 ],
      "to": [ 13.5, 2.5, 9.0 ],
      "faces": {
        "down":  { "uv": [ 2, 0, 14, 2 ], "texture": "#texture" },
        "up":    { "uv": [ 2, 0, 14, 2 ], "texture": "#texture" },
        "north": { "uv": [ 2, 0, 14, 2 ], "texture": "#texture" },
        "south": { "uv": [ 2, 0, 14, 2 ], "texture": "#texture" },
        "west":  { "uv": [ 0, 0, 2, 2 ], "texture": "#texture" },
        "east":  { "uv": [ 0, 0, 2, 2 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Top Left Roller Handle",
      "from": [ 1.5, 13.75, 7.5 ],
      "to": [ 2.5, 14.25, 8.5 ],
      "faces": {
        "north": { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" },
        "south": { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" },
        "west":  { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" },
        "east":  { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" },
        "up":    { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" },
        "down":  { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Top Right Roller Handle",
      "from": [ 13.5, 13.75, 7.5 ],
      "to": [ 14.5, 14.25, 8.5 ],
      "faces": {
        "north": { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" },
        "south": { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" },
        "west":  { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" },
        "east":  { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" },
        "up":    { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" },
        "down":  { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Bottom Left Roller Handle",
      "from": [ 1.5, 1.75, 7.5 ],
      "to": [ 2.5, 2.25, 8.5 ],
      "faces": {
        "north": { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" },
        "south": { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" },
        "west":  { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" },
        "east":  { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" },
        "up":    { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" },
        "down":  { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Bottom Right Roller Handle",
      "from": [ 13.5, 1.75, 7.5 ],
      "to": [ 14.5, 2.25, 8.5 ],
      "faces": {
        "north": { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" },
        "south": { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" },
        "west":  { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" },
        "east":  { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" },
        "up":    { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" },
        "down":  { "uv": [ 0, 2, 1, 3 ], "texture": "#texture" }
      }
    },
    {
      "comment": "Raised Wax Seal",
      "from": [ 6.5, 6.5, 7.3 ],
      "to": [ 9.5, 9.5, 7.9 ],
      "faces": {
        "north": { "uv": [ 12, 12, 15, 15 ], "texture": "#texture" },
        "south": { "uv": [ 12, 12, 15, 15 ], "texture": "#texture" },
        "west":  { "uv": [ 12, 12, 13, 15 ], "texture": "#texture" },
        "east":  { "uv": [ 12, 12, 13, 15 ], "texture": "#texture" },
        "up":    { "uv": [ 12, 12, 15, 13 ], "texture": "#texture" },
        "down":  { "uv": [ 12, 12, 15, 13 ], "texture": "#texture" }
      }
    }
  ]
}''',
        "duelist_sword": '''{
  "parent": "minecraft:item/handheld",
  "textures": {
    "layer0": "minecraft:item/duelist_sword"
  }
}''',
        "heirloom_axe": '''{
  "parent": "minecraft:item/handheld",
  "textures": {
    "layer0": "minecraft:item/heirloom_axe"
  }
}''',
        "recurve_bow": '''{
  "parent": "minecraft:item/handheld",
  "textures": {
    "layer0": "minecraft:item/recurve_bow"
  }
}''',
        "heavy_crossbow": '''{
  "parent": "minecraft:item/handheld",
  "textures": {
    "layer0": "minecraft:item/heavy_crossbow"
  }
}''',
        "royal_trident": '''{
  "parent": "minecraft:item/handheld",
  "textures": {
    "layer0": "minecraft:item/royal_trident"
  }
}''',
        "bulwark_shield": '''{
  "parent": "minecraft:item/handheld",
  "textures": {
    "layer0": "minecraft:item/bulwark_shield"
  }
}''',
        "claymore_sword": '''{
  "parent": "minecraft:item/handheld",
  "textures": {
    "layer0": "minecraft:item/claymore_sword"
  }
}''',
        "twinblade_sword": '''{
  "parent": "minecraft:item/handheld",
  "textures": {
    "layer0": "minecraft:item/twinblade_sword"
  }
}''',
        "scythe_trident": '''{
  "parent": "minecraft:item/handheld",
  "textures": {
    "layer0": "minecraft:item/scythe_trident"
  }
}'''
    }
    
    for item, content in models.items():
        model_path = os.path.join(models_dir, f"{item}.json")
        with open(model_path, "w") as f:
            f.write(content)
        print(f"Wrote custom 3D model JSON: {model_path}")
        
    # Copy & process community 3D models and textures
    copy_3d_harvested_assets(rp_dir)
    
    # 3.5 Write vanilla fallback models for crossbow/bow/trident states.
    # Our override files reference these but the clean step deletes them.
    vanilla_fallbacks = {
        # --- Crossbow vanilla states ---
        "crossbow_pulling_0": '''{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/crossbow_pulling_0"
  }
}''',
        "crossbow_pulling_1": '''{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/crossbow_pulling_1"
  }
}''',
        "crossbow_pulling_2": '''{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/crossbow_pulling_2"
  }
}''',
        "crossbow_arrow": '''{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/crossbow_arrow"
  }
}''',
        "crossbow_firework": '''{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/crossbow_firework"
  }
}''',
        # --- Bow vanilla pulling states ---
        "bow_pulling_0": '''{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/bow_pulling_0"
  }
}''',
        "bow_pulling_1": '''{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/bow_pulling_1"
  }
}''',
        "bow_pulling_2": '''{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/bow_pulling_2"
  }
}'''
    }
    
    for name, content in vanilla_fallbacks.items():
        path = os.path.join(models_dir, f"{name}.json")
        with open(path, "w") as f:
            f.write(content)
        print(f"Wrote vanilla fallback model: {name}.json")
        
    # 4. Scan custom weapons from custom_assets/assets/
    custom_assets_dir = os.path.join(workspace_dir, "custom_assets")
    
    namespaces = {
        "lunarset": 13000,
        "malikaset": 13100,
        "necros_set": 13200,
        "fairyset": 13300,
        "littledragonset": 13400,
        "infernoset": 13500
    }
    
    categories = {
        "sword": [],
        "axe": [],
        "shield": [],
        "bow": [],
        "crossbow": [],
        "trident": []
    }
    
    def get_model_ref(ns, name, suffix, fallback_suffix=None):
        target_file = f"{name}{suffix}.json"
        target_path = os.path.join(custom_assets_dir, "assets", ns, "models", target_file)
        if os.path.exists(target_path):
            return f"{ns}:{name}{suffix}"
        if fallback_suffix:
            fallback_file = f"{name}{fallback_suffix}.json"
            fallback_path = os.path.join(custom_assets_dir, "assets", ns, "models", fallback_file)
            if os.path.exists(fallback_path):
                return f"{ns}:{name}{fallback_suffix}"
        return f"{ns}:{name}"

    for ns, base in namespaces.items():
        ns_dir = os.path.join(custom_assets_dir, "assets", ns)
        if not os.path.exists(ns_dir):
            continue
        m_dir = os.path.join(ns_dir, "models")
        if not os.path.exists(m_dir):
            continue
            
        for f_name in os.listdir(m_dir):
            if not f_name.endswith(".json") or f_name.startswith("."):
                continue
            name = os.path.splitext(f_name)[0].lower()
            
            # Avoid secondary/state files in primary lists
            if any(x in name for x in ["pulling", "cast", "blocking", "charged", "firework", "cosmetics", "ia_auto_gen", "auto_generated"]):
                continue
                
            model_ref = f"{ns}:{name}"
            
            # Swords
            if name == "sword":
                categories["sword"].append((base + 1, model_ref, ns, name))
            elif name == "greatsword":
                categories["sword"].append((base + 2, model_ref, ns, name))
            elif name == "mace":
                categories["sword"].append((base + 3, model_ref, ns, name))
            elif name == "knife":
                categories["sword"].append((base + 4, model_ref, ns, name))
            # Axes
            elif name == "axe":
                categories["axe"].append((base + 10, model_ref, ns, name))
            elif name == "scythe":
                categories["axe"].append((base + 11, model_ref, ns, name))
            elif name == "hammer":
                categories["axe"].append((base + 12, model_ref, ns, name))
            # Shields
            elif name == "shield":
                categories["shield"].append((base + 20, model_ref, ns, name))
            # Bows
            elif name == "bow":
                categories["bow"].append((base + 30, model_ref, ns, name))
            # Crossbows
            elif name == "crossbow":
                categories["crossbow"].append((base + 40, model_ref, ns, name))
            # Tridents / Spears / Staffs
            elif name == "trident":
                categories["trident"].append((base + 50, model_ref, ns, name))
            elif name == "spear":
                categories["trident"].append((base + 51, model_ref, ns, name))
            elif name == "staff":
                categories["trident"].append((base + 52, model_ref, ns, name))

    print("\n[Resource Pack Build] Scanned custom weapon folders:")
    for cat, items in categories.items():
        print(f"  Category '{cat}': {len(items)} models found")

    # 4.1 Write static custom item overrides (Strength orb, Book, Paper)
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

    # 4.2 Write Swords
    sword_legacy = [
        {"cmd": 12348, "model": "minecraft:item/duelist_sword"},
        {"cmd": 12354, "model": "minecraft:item/claymore_sword"},
        {"cmd": 12355, "model": "minecraft:item/twinblade_sword"},
        {"cmd": 12360, "model": "minecraft:item/inferno_blade"},
        {"cmd": 12361, "model": "minecraft:item/frost_fang"},
        {"cmd": 12362, "model": "minecraft:item/void_reaper"}
    ]
    for cmd, model_ref, ns, name in categories["sword"]:
        sword_legacy.append({"cmd": cmd, "model": model_ref})
    sword_legacy.sort(key=lambda x: x["cmd"])

    for sword in ["netherite_sword", "diamond_sword", "golden_sword", "iron_sword", "stone_sword", "wooden_sword"]:
        overrides_list = [f'{{"predicate": {{"custom_model_data": {item["cmd"]}}}, "model": "{item["model"]}"}}' for item in sword_legacy]
        overrides_str = ",\n    ".join(overrides_list)
        with open(os.path.join(models_dir, f"{sword}.json"), "w") as f:
            f.write(f'''{{
  "parent": "minecraft:item/handheld",
  "textures": {{
    "layer0": "minecraft:item/{sword}"
  }},
  "overrides": [
    {overrides_str}
  ]
}}''')

        entries_list = [f'{{"threshold": {item["cmd"]}.0, "model": {{"type": "minecraft:model", "model": "{item["model"]}"}}}}' for item in sword_legacy]
        entries_str = ",\n      ".join(entries_list)
        with open(os.path.join(items_dir, f"{sword}.json"), "w") as f:
            f.write(f'''{{
  "model": {{
    "type": "minecraft:range_dispatch",
    "property": "minecraft:custom_model_data",
    "fallback": {{ "type": "minecraft:model", "model": "minecraft:item/{sword}" }},
    "entries": [
      {entries_str}
    ]
  }}
}}''')

    # 4.3 Write Axes
    axe_legacy = [
        {"cmd": 12349, "model": "minecraft:item/heirloom_axe"},
        {"cmd": 12363, "model": "minecraft:item/wardens_cleaver"},
        {"cmd": 12364, "model": "minecraft:item/ember_hatchet"},
        {"cmd": 12365, "model": "minecraft:item/crystal_vanguard"}
    ]
    for cmd, model_ref, ns, name in categories["axe"]:
        axe_legacy.append({"cmd": cmd, "model": model_ref})
    axe_legacy.sort(key=lambda x: x["cmd"])

    for axe in ["netherite_axe", "diamond_axe", "golden_axe", "iron_axe", "stone_axe", "wooden_axe"]:
        overrides_list = [f'{{"predicate": {{"custom_model_data": {item["cmd"]}}}, "model": "{item["model"]}"}}' for item in axe_legacy]
        overrides_str = ",\n    ".join(overrides_list)
        with open(os.path.join(models_dir, f"{axe}.json"), "w") as f:
            f.write(f'''{{
  "parent": "minecraft:item/handheld",
  "textures": {{
    "layer0": "minecraft:item/{axe}"
  }},
  "overrides": [
    {overrides_str}
  ]
}}''')

        entries_list = [f'{{"threshold": {item["cmd"]}.0, "model": {{"type": "minecraft:model", "model": "{item["model"]}"}}}}' for item in axe_legacy]
        entries_str = ",\n      ".join(entries_list)
        with open(os.path.join(items_dir, f"{axe}.json"), "w") as f:
            f.write(f'''{{
  "model": {{
    "type": "minecraft:range_dispatch",
    "property": "minecraft:custom_model_data",
    "fallback": {{ "type": "minecraft:model", "model": "minecraft:item/{axe}" }},
    "entries": [
      {entries_str}
    ]
  }}
}}''')

    # 4.4 Write Tridents
    trident_legacy = [
        {"cmd": 12352, "model": "minecraft:item/royal_trident"},
        {"cmd": 12356, "model": "minecraft:item/scythe_trident"},
        {"cmd": 12375, "model": "minecraft:item/poseidons_wrath"},
        {"cmd": 12376, "model": "minecraft:item/abyssal_spike"},
        {"cmd": 12377, "model": "minecraft:item/solar_lance"}
    ]
    for cmd, model_ref, ns, name in categories["trident"]:
        trident_legacy.append({"cmd": cmd, "model": model_ref})
    trident_legacy.sort(key=lambda x: x["cmd"])

    overrides_list = [f'{{"predicate": {{"custom_model_data": {item["cmd"]}}}, "model": "{item["model"]}"}}' for item in trident_legacy]
    overrides_str = ",\n    ".join(overrides_list)
    with open(os.path.join(models_dir, "trident.json"), "w") as f:
        f.write(f'''{{
  "parent": "minecraft:item/handheld",
  "textures": {{
    "layer0": "minecraft:item/trident"
  }},
  "overrides": [
    {overrides_str}
  ]
}}''')

    entries_list = [f'{{"threshold": {item["cmd"]}.0, "model": {{"type": "minecraft:model", "model": "{item["model"]}"}}}}' for item in trident_legacy]
    entries_str = ",\n      ".join(entries_list)
    with open(os.path.join(items_dir, "trident.json"), "w") as f:
        f.write(f'''{{
  "model": {{
    "type": "minecraft:range_dispatch",
    "property": "minecraft:custom_model_data",
    "fallback": {{ "type": "minecraft:model", "model": "minecraft:item/trident" }},
    "entries": [
      {entries_str}
    ]
  }}
}}''')

    # 4.5 Write Shields
    shield_legacy = [
        {"cmd": 12353, "model": "minecraft:item/bulwark_shield", "blocking": "minecraft:item/bulwark_shield"},
        {"cmd": 12366, "model": "minecraft:item/dragons_aegis", "blocking": "minecraft:item/dragons_aegis"},
        {"cmd": 12367, "model": "minecraft:item/sentinels_wall", "blocking": "minecraft:item/sentinels_wall"},
        {"cmd": 12368, "model": "minecraft:item/thorned_bulwark", "blocking": "minecraft:item/thorned_bulwark"}
    ]
    for cmd, model_ref, ns, name in categories["shield"]:
        blocking_ref = get_model_ref(ns, name, "_blocking")
        shield_legacy.append({"cmd": cmd, "model": model_ref, "blocking": blocking_ref})
    shield_legacy.sort(key=lambda x: x["cmd"])

    for s_file in ["shield", "shield_blocking"]:
        overrides_list = []
        for item in shield_legacy:
            model_to_use = item["blocking"] if s_file == "shield_blocking" else item["model"]
            overrides_list.append(f'{{"predicate": {{"custom_model_data": {item["cmd"]}}}, "model": "{model_to_use}"}}')
        overrides_str = ",\n    ".join(overrides_list)
        with open(os.path.join(models_dir, f"{s_file}.json"), "w") as f:
            f.write(f'''{{
  "parent": "builtin/entity",
  "textures": {{
    "particle": "minecraft:entity/shield_base_nopattern"
  }},
  "overrides": [
    {overrides_str}
  ]
}}''')

    entries_list = []
    for item in shield_legacy:
        entries_list.append(f'''      {{
        "threshold": {item["cmd"]}.0,
        "model": {{
          "type": "minecraft:condition",
          "property": "minecraft:using_item",
          "on_false": {{ "type": "minecraft:model", "model": "{item["model"]}" }},
          "on_true": {{ "type": "minecraft:model", "model": "{item["blocking"]}" }}
        }}
      }}''')
    entries_str = ",\n".join(entries_list)
    with open(os.path.join(items_dir, "shield.json"), "w") as f:
        f.write(f'''{{
  "model": {{
    "type": "minecraft:range_dispatch",
    "property": "minecraft:custom_model_data",
    "fallback": {{
      "type": "minecraft:condition",
      "property": "minecraft:using_item",
      "on_false": {{
        "type": "minecraft:special",
        "base": "minecraft:item/shield",
        "model": {{ "type": "minecraft:shield" }}
      }},
      "on_true": {{
        "type": "minecraft:special",
        "base": "minecraft:item/shield_blocking",
        "model": {{ "type": "minecraft:shield" }}
      }}
    }},
    "entries": [
{entries_str}
    ]
  }}
}}''')

    # 4.6 Write Bows
    bow_legacy = [
        {"cmd": 12350, "model": "minecraft:item/recurve_bow", "pull_0": "minecraft:item/recurve_bow_pulling_0", "pull_1": "minecraft:item/recurve_bow_pulling_1", "pull_2": "minecraft:item/recurve_bow_pulling_2"},
        {"cmd": 12369, "model": "minecraft:item/phoenix_wing", "pull_0": "minecraft:item/phoenix_wing_pulling_0", "pull_1": "minecraft:item/phoenix_wing_pulling_1", "pull_2": "minecraft:item/phoenix_wing_pulling_2"},
        {"cmd": 12370, "model": "minecraft:item/shadow_stalker", "pull_0": "minecraft:item/shadow_stalker_pulling_0", "pull_1": "minecraft:item/shadow_stalker_pulling_1", "pull_2": "minecraft:item/shadow_stalker_pulling_2"},
        {"cmd": 12371, "model": "minecraft:item/celestial_arc", "pull_0": "minecraft:item/celestial_arc_pulling_0", "pull_1": "minecraft:item/celestial_arc_pulling_1", "pull_2": "minecraft:item/celestial_arc_pulling_2"}
    ]
    for cmd, model_ref, ns, name in categories["bow"]:
        pull_0 = get_model_ref(ns, name, "_0")
        pull_1 = get_model_ref(ns, name, "_1")
        pull_2 = get_model_ref(ns, name, "_2")
        bow_legacy.append({"cmd": cmd, "model": model_ref, "pull_0": pull_0, "pull_1": pull_1, "pull_2": pull_2})
    bow_legacy.sort(key=lambda x: x["cmd"])

    overrides_list = [
        '{ "predicate": { "pulling": 1 }, "model": "minecraft:item/bow_pulling_0" }',
        '{ "predicate": { "pulling": 1, "pull": 0.65 }, "model": "minecraft:item/bow_pulling_1" }',
        '{ "predicate": { "pulling": 1, "pull": 0.9 }, "model": "minecraft:item/bow_pulling_2" }'
    ]
    for item in bow_legacy:
        overrides_list.append(f'{{ "predicate": {{ "custom_model_data": {item["cmd"]} }}, "model": "{item["model"]}" }}')
        overrides_list.append(f'{{ "predicate": {{ "custom_model_data": {item["cmd"]}, "pulling": 1 }}, "model": "{item["pull_0"]}" }}')
        overrides_list.append(f'{{ "predicate": {{ "custom_model_data": {item["cmd"]}, "pulling": 1, "pull": 0.65 }}, "model": "{item["pull_1"]}" }}')
        overrides_list.append(f'{{ "predicate": {{ "custom_model_data": {item["cmd"]}, "pulling": 1, "pull": 0.9 }}, "model": "{item["pull_2"]}" }}')
    overrides_str = ",\n    ".join(overrides_list)
    with open(os.path.join(models_dir, "bow.json"), "w") as f:
        f.write(f'''{{
  "parent": "minecraft:item/generated",
  "textures": {{
    "layer0": "minecraft:item/bow"
  }},
  "overrides": [
    {overrides_str}
  ]
}}''')

    entries_list = []
    for item in bow_legacy:
        entries_list.append(f'''      {{
        "threshold": {item["cmd"]}.0,
        "model": {{
          "type": "minecraft:condition",
          "property": "minecraft:using_item",
          "on_true": {{
            "type": "minecraft:range_dispatch",
            "property": "minecraft:use_duration",
            "scale": 0.05,
            "entries": [
              {{ "threshold": 0.65, "model": {{ "type": "minecraft:model", "model": "{item["pull_1"]}" }} }},
              {{ "threshold": 0.9, "model": {{ "type": "minecraft:model", "model": "{item["pull_2"]}" }} }}
            ],
            "fallback": {{
              "type": "minecraft:model",
              "model": "{item["pull_0"]}"
            }}
          }},
          "on_false": {{
            "type": "minecraft:model",
            "model": "{item["model"]}"
          }}
        }}
      }}''')
    entries_str = ",\n".join(entries_list)
    with open(os.path.join(items_dir, "bow.json"), "w") as f:
        f.write(f'''{{
  "model": {{
    "type": "minecraft:range_dispatch",
    "property": "minecraft:custom_model_data",
    "fallback": {{
      "type": "minecraft:condition",
      "property": "minecraft:using_item",
      "on_true": {{
        "type": "minecraft:range_dispatch",
        "property": "minecraft:use_duration",
        "scale": 0.05,
        "entries": [
          {{ "threshold": 0.65, "model": {{ "type": "minecraft:model", "model": "minecraft:item/bow_pulling_1" }} }},
          {{ "threshold": 0.9, "model": {{ "type": "minecraft:model", "model": "minecraft:item/bow_pulling_2" }} }}
        ],
        "fallback": {{
          "type": "minecraft:model",
          "model": "minecraft:item/bow_pulling_0"
        }}
      }},
      "on_false": {{
        "type": "minecraft:model",
        "model": "minecraft:item/bow"
      }}
    }},
    "entries": [
{entries_str}
    ]
  }}
}}''')

    # 4.7 Write Crossbows
    crossbow_legacy = [
        {"cmd": 12351, "model": "minecraft:item/heavy_crossbow", "pull_0": "minecraft:item/heavy_crossbow_pulling_0", "pull_1": "minecraft:item/heavy_crossbow_pulling_1", "pull_2": "minecraft:item/heavy_crossbow_pulling_2", "arrow": "minecraft:item/heavy_crossbow_arrow", "firework": "minecraft:item/heavy_crossbow_firework"},
        {"cmd": 12372, "model": "minecraft:item/siege_breaker", "pull_0": "minecraft:item/siege_breaker_pulling_0", "pull_1": "minecraft:item/siege_breaker_pulling_1", "pull_2": "minecraft:item/siege_breaker_pulling_2", "arrow": "minecraft:item/siege_breaker_arrow", "firework": "minecraft:item/siege_breaker_firework"},
        {"cmd": 12373, "model": "minecraft:item/nightfall_repeater", "pull_0": "minecraft:item/nightfall_repeater_pulling_0", "pull_1": "minecraft:item/nightfall_repeater_pulling_1", "pull_2": "minecraft:item/nightfall_repeater_pulling_2", "arrow": "minecraft:item/nightfall_repeater_arrow", "firework": "minecraft:item/nightfall_repeater_firework"},
        {"cmd": 12374, "model": "minecraft:item/radiant_arbalest", "pull_0": "minecraft:item/radiant_arbalest_pulling_0", "pull_1": "minecraft:item/radiant_arbalest_pulling_1", "pull_2": "minecraft:item/radiant_arbalest_pulling_2", "arrow": "minecraft:item/radiant_arbalest_arrow", "firework": "minecraft:item/radiant_arbalest_firework"}
    ]
    for cmd, model_ref, ns, name in categories["crossbow"]:
        pull_0 = get_model_ref(ns, name, "_0")
        pull_1 = get_model_ref(ns, name, "_1")
        pull_2 = get_model_ref(ns, name, "_2")
        arrow = get_model_ref(ns, name, "_arrow", "_charged")
        firework = get_model_ref(ns, name, "_firework", "_charged")
        crossbow_legacy.append({"cmd": cmd, "model": model_ref, "pull_0": pull_0, "pull_1": pull_1, "pull_2": pull_2, "arrow": arrow, "firework": firework})
    crossbow_legacy.sort(key=lambda x: x["cmd"])

    overrides_list = [
        '{ "predicate": { "pulling": 1 }, "model": "minecraft:item/crossbow_pulling_0" }',
        '{ "predicate": { "pulling": 1, "pull": 0.58 }, "model": "minecraft:item/crossbow_pulling_1" }',
        '{ "predicate": { "pulling": 1, "pull": 1.0 }, "model": "minecraft:item/crossbow_pulling_2" }',
        '{ "predicate": { "charged": 1 }, "model": "minecraft:item/crossbow_arrow" }',
        '{ "predicate": { "charged": 1, "firework": 1 }, "model": "minecraft:item/crossbow_firework" }'
    ]
    for item in crossbow_legacy:
        overrides_list.append(f'{{ "predicate": {{ "custom_model_data": {item["cmd"]} }}, "model": "{item["model"]}" }}')
        overrides_list.append(f'{{ "predicate": {{ "custom_model_data": {item["cmd"]}, "pulling": 1 }}, "model": "{item["pull_0"]}" }}')
        overrides_list.append(f'{{ "predicate": {{ "custom_model_data": {item["cmd"]}, "pulling": 1, "pull": 0.58 }}, "model": "{item["pull_1"]}" }}')
        overrides_list.append(f'{{ "predicate": {{ "custom_model_data": {item["cmd"]}, "pulling": 1, "pull": 1.0 }}, "model": "{item["pull_2"]}" }}')
        overrides_list.append(f'{{ "predicate": {{ "custom_model_data": {item["cmd"]}, "charged": 1 }}, "model": "{item["arrow"]}" }}')
        overrides_list.append(f'{{ "predicate": {{ "custom_model_data": {item["cmd"]}, "charged": 1, "firework": 1 }}, "model": "{item["firework"]}" }}')
    overrides_str = ",\n    ".join(overrides_list)
    with open(os.path.join(models_dir, "crossbow.json"), "w") as f:
        f.write(f'''{{
  "parent": "minecraft:item/generated",
  "textures": {{
    "layer0": "minecraft:item/crossbow_standby"
  }},
  "overrides": [
    {overrides_str}
  ]
}}''')

    entries_list = []
    for item in crossbow_legacy:
        entries_list.append(f'''      {{
        "threshold": {item["cmd"]}.0,
        "model": {{
          "type": "minecraft:condition",
          "property": "minecraft:using_item",
          "on_true": {{
            "type": "minecraft:range_dispatch",
            "property": "minecraft:crossbow/pull",
            "entries": [
              {{ "threshold": 0.58, "model": {{ "type": "minecraft:model", "model": "{item["pull_1"]}" }} }},
              {{ "threshold": 1.0, "model": {{ "type": "minecraft:model", "model": "{item["pull_2"]}" }} }}
            ],
            "fallback": {{
              "type": "minecraft:model",
              "model": "{item["pull_0"]}"
            }}
          }},
          "on_false": {{
            "type": "minecraft:select",
            "property": "minecraft:charge_type",
            "cases": [
              {{ "when": "arrow", "model": {{ "type": "minecraft:model", "model": "{item["arrow"]}" }} }},
              {{ "when": "rocket", "model": {{ "type": "minecraft:model", "model": "{item["firework"]}" }} }}
            ],
            "fallback": {{
              "type": "minecraft:model",
              "model": "{item["model"]}"
            }}
          }}
        }}
      }}''')
    entries_str = ",\n".join(entries_list)
    with open(os.path.join(items_dir, "crossbow.json"), "w") as f:
        f.write(f'''{{
  "model": {{
    "type": "minecraft:range_dispatch",
    "property": "minecraft:custom_model_data",
    "fallback": {{
      "type": "minecraft:condition",
      "property": "minecraft:using_item",
      "on_true": {{
        "type": "minecraft:range_dispatch",
        "property": "minecraft:crossbow/pull",
        "entries": [
          {{ "threshold": 0.58, "model": {{ "type": "minecraft:model", "model": "minecraft:item/crossbow_pulling_1" }} }},
          {{ "threshold": 1.0, "model": {{ "type": "minecraft:model", "model": "minecraft:item/crossbow_pulling_2" }} }}
        ],
        "fallback": {{
          "type": "minecraft:model",
          "model": "minecraft:item/crossbow_pulling_0"
        }}
      }},
      "on_false": {{
        "type": "minecraft:select",
        "property": "minecraft:charge_type",
        "cases": [
          {{ "when": "arrow", "model": {{ "type": "minecraft:model", "model": "minecraft:item/crossbow_arrow" }} }},
          {{ "when": "rocket", "model": {{ "type": "minecraft:model", "model": "minecraft:item/crossbow_firework" }} }}
        ],
        "fallback": {{
          "type": "minecraft:model",
          "model": "minecraft:item/crossbow"
        }}
      }}
    }},
    "entries": [
{entries_str}
    ]
  }}
}}''')

    # 6. Write pack.mcmeta with multi-version format support (34 to 46)
    with open(os.path.join(rp_dir, "pack.mcmeta"), "w") as f:
        f.write('''{
  "pack": {
    "pack_format": 34,
    "supported_formats": [34, 46],
    "description": "Strength SMP custom item textures"
  }
}''')
        
    # 6.5. Merge custom assets if folder exists, creating it if not
    custom_assets_dir = os.path.join(workspace_dir, "custom_assets")
    os.makedirs(os.path.join(custom_assets_dir, "assets/minecraft/models/item"), exist_ok=True)
    os.makedirs(os.path.join(custom_assets_dir, "assets/minecraft/textures/item"), exist_ok=True)
    
    def merge_dirs(src, dst):
        if not os.path.exists(src):
            return
        for item in os.listdir(src):
            s = os.path.join(src, item)
            d = os.path.join(dst, item)
            if os.path.isdir(s):
                os.makedirs(d, exist_ok=True)
                merge_dirs(s, d)
            else:
                shutil.copy2(s, d)
                print(f"Merged custom asset: {item} -> {d}")
                
    merge_dirs(custom_assets_dir, rp_dir)

    # 7. Zip up the resource pack
    zip_folder(rp_dir, os.path.join(workspace_dir, "src/main/resources/strengthsmp.zip"))
    zip_folder(rp_dir, os.path.join(workspace_dir, "strengthsmp.zip"))
    
    server_dest = "/home/sagheer/Desktop/Test server/plugins/StrengthSMP/strengthsmp.zip"
    if os.path.exists(os.path.dirname(server_dest)):
        shutil.copy(os.path.join(workspace_dir, "strengthsmp.zip"), server_dest)
        print(f"Directly copied strengthsmp.zip to server folder: {server_dest}")
        
    client_dest = "/home/sagheer/.minecraft/resourcepacks/strengthsmp.zip"
    if os.path.exists(os.path.dirname(client_dest)):
        shutil.copy(os.path.join(workspace_dir, "strengthsmp.zip"), client_dest)
        print(f"Directly copied strengthsmp.zip to client resource pack folder: {client_dest}")
        
    # 8. Print SHA-1
    sha1 = calculate_sha1(os.path.join(workspace_dir, "strengthsmp.zip"))
    
    # Update config.yml files with the new SHA-1 hash
    configs_to_update = [
        os.path.join(workspace_dir, "src/main/resources/config.yml"),
        "/home/sagheer/Desktop/Test server/plugins/StrengthSMP/config.yml"
    ]
    for config_path in configs_to_update:
        if os.path.exists(config_path):
            with open(config_path, "r") as f:
                content = f.read()
            import re
            new_content = re.sub(r'(hash:\s*")([a-f0-9]+)(")', r'\g<1>' + sha1 + r'\g<3>', content)
            new_content = re.sub(r'(hash:\s*)([a-f0-9]{40})\b', r'\g<1>"' + sha1 + '"', new_content)
            with open(config_path, "w") as f:
                f.write(new_content)
            print(f"Updated hash in config: {config_path}")

    print(f"\n=========================================")
    print(f"SUCCESS: Resource pack generated!")
    print(f"SHA-1 Hash: {sha1}")
    print(f"=========================================")

if __name__ == "__main__":
    main()
