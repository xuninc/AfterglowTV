#!/usr/bin/env python3
"""
Install the Afterglow TV brand artwork into the Android resource tree.

Inputs:
  Set AFTERGLOW_ICON_SRC to the icon-only PNG.
  Set AFTERGLOW_BANNER_SRC to the icon + wordmark PNG.

Outputs (all under app/src/main/res/):
  mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher_vault.png       (square icon)
  drawable-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher_vault_art.png (adaptive fg, 108×108 mdpi)
  drawable/app_banner.png                                              (320×180 banner, default)
  drawable-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/app_banner.png             (320×180 banner per density)
"""
from __future__ import annotations
import os
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "app" / "src" / "main" / "res"

ICON_SRC = Path(os.environ.get("AFTERGLOW_ICON_SRC", ROOT / "docs" / "branding" / "source" / "114x114.png"))
BANNER_SRC = Path(os.environ.get("AFTERGLOW_BANNER_SRC", ROOT / "docs" / "branding" / "source" / "512x512-afterglow.png"))

DENSITIES = {
    "mdpi":     1.0,
    "hdpi":     1.5,
    "xhdpi":    2.0,
    "xxhdpi":   3.0,
    "xxxhdpi":  4.0,
}

# Banner palette echoes for the gradient backdrop behind the wordmark
SURFACE_DEEP = (22, 4, 39)     # deep purple from the source
SURFACE_BASE = (52, 14, 78)    # mid-purple
SURFACE_COOL = (74, 25, 102)   # warmer accent purple
ACCENT_PINK = (255, 122, 200)  # text accent — matches the source wordmark


def find_font(size: int) -> ImageFont.FreeTypeFont:
    configured = os.environ.get("AFTERGLOW_FONT")
    if configured and Path(configured).exists():
        return ImageFont.truetype(configured, size)
    return ImageFont.load_default()


def write_icon():
    print(f"Source icon: {ICON_SRC} ({ICON_SRC.stat().st_size} bytes)")
    src = Image.open(ICON_SRC).convert("RGBA")
    # The source icon at 114x114 should be upscaled gracefully to higher densities.
    # mdpi launcher icons are 48dp = 48px, xxxhdpi = 192px.
    for bucket, scale in DENSITIES.items():
        target = int(48 * scale)
        img = src.resize((target, target), Image.LANCZOS)
        out_dir = RES / f"mipmap-{bucket}"
        out_dir.mkdir(parents=True, exist_ok=True)
        out = out_dir / "ic_launcher_vault.png"
        img.save(out, "PNG", optimize=True)
        print(f"  wrote {out.relative_to(ROOT)}  ({target}x{target})")


def write_adaptive_foreground():
    """108×108 mdpi adaptive icon — content lives in the inner 72×72 safe zone."""
    print(f"Source for adaptive fg: {ICON_SRC}")
    src = Image.open(ICON_SRC).convert("RGBA")
    for bucket, scale in DENSITIES.items():
        canvas_size = int(108 * scale)
        safe_size = int(72 * scale)
        # Render the icon inside the safe zone, slightly oversized for visual mass
        icon_size = int(safe_size * 1.05)
        icon = src.resize((icon_size, icon_size), Image.LANCZOS)
        bg = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
        x = (canvas_size - icon_size) // 2
        y = (canvas_size - icon_size) // 2
        bg.paste(icon, (x, y), icon)
        out_dir = RES / f"drawable-{bucket}"
        out_dir.mkdir(parents=True, exist_ok=True)
        out = out_dir / "ic_launcher_vault_art.png"
        bg.save(out, "PNG", optimize=True)
        print(f"  wrote {out.relative_to(ROOT)}  ({canvas_size}x{canvas_size})")


def make_banner(width: int, height: int) -> Image.Image:
    """320×180 mdpi launcher banner — icon on the left, wordmark on the right."""
    img = Image.new("RGBA", (width, height), SURFACE_DEEP)
    draw = ImageDraw.Draw(img)

    # Subtle vertical gradient to give the banner depth
    for y in range(height):
        t = y / max(1, height - 1)
        if t < 0.5:
            k = t / 0.5
            r = int(SURFACE_DEEP[0] + (SURFACE_BASE[0] - SURFACE_DEEP[0]) * k)
            g = int(SURFACE_DEEP[1] + (SURFACE_BASE[1] - SURFACE_DEEP[1]) * k)
            b = int(SURFACE_DEEP[2] + (SURFACE_BASE[2] - SURFACE_DEEP[2]) * k)
        else:
            k = (t - 0.5) / 0.5
            r = int(SURFACE_BASE[0] + (SURFACE_COOL[0] - SURFACE_BASE[0]) * k)
            g = int(SURFACE_BASE[1] + (SURFACE_COOL[1] - SURFACE_BASE[1]) * k)
            b = int(SURFACE_BASE[2] + (SURFACE_COOL[2] - SURFACE_BASE[2]) * k)
        draw.line([(0, y), (width, y)], fill=(r, g, b, 255))

    # Off-screen accent glow at upper-right for the "afterglow" feel
    glow = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    gx, gy = int(width * 0.85), int(height * 0.20)
    max_radius = int(width * 0.55)
    for r in range(max_radius, 0, -2):
        a = int(60 * (1 - r / max_radius) ** 2.2)
        gd.ellipse([gx - r, gy - r, gx + r, gy + r], fill=(255, 122, 60, a))
    glow = glow.filter(ImageFilter.GaussianBlur(radius=max(2, width // 60)))
    img.paste(glow, (0, 0), glow)

    # Paste the icon vertically centred on the left
    icon = Image.open(ICON_SRC).convert("RGBA")
    icon_h = int(height * 0.78)
    icon_w = icon_h  # square
    icon = icon.resize((icon_w, icon_h), Image.LANCZOS)
    icon_x = int(width * 0.06)
    icon_y = (height - icon_h) // 2
    img.paste(icon, (icon_x, icon_y), icon)

    # Wordmark to the right of the icon
    draw = ImageDraw.Draw(img)
    primary_size = int(height * 0.30)
    sub_size = int(height * 0.13)
    primary_font = find_font(primary_size)
    sub_font = find_font(sub_size)
    wordmark = "Afterglow"
    sub_text = "TV"

    text_x = icon_x + icon_w + int(width * 0.04)
    text_y = (height - primary_size) // 2 - int(height * 0.04)

    # Soft glow under the wordmark
    underglow = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    ug = ImageDraw.Draw(underglow)
    ug.text((text_x, text_y), wordmark, font=primary_font, fill=(*ACCENT_PINK, 120))
    underglow = underglow.filter(ImageFilter.GaussianBlur(radius=max(2, width // 50)))
    img.paste(underglow, (0, 0), underglow)

    # Wordmark in light cream
    draw.text((text_x, text_y), wordmark, font=primary_font, fill=(255, 234, 246, 255))

    # Subscript "TV" in pink accent on its own baseline below "Afterglow"
    bbox = draw.textbbox((text_x, text_y), wordmark, font=primary_font)
    sub_x = bbox[2] + int(width * 0.025)
    sub_y = bbox[3] - sub_size - int(height * 0.04)
    draw.text((sub_x, sub_y), sub_text, font=sub_font, fill=ACCENT_PINK)

    return img.convert("RGB")


def write_banner():
    print("Composing banner...")
    base_w, base_h = 320, 180
    # default drawable/ (no density)
    default_dir = RES / "drawable"
    default_dir.mkdir(parents=True, exist_ok=True)
    img = make_banner(base_w, base_h)
    out = default_dir / "app_banner.png"
    img.save(out, "PNG", optimize=True)
    print(f"  wrote {out.relative_to(ROOT)}  ({base_w}x{base_h})")

    for bucket, scale in DENSITIES.items():
        w = int(base_w * scale)
        h = int(base_h * scale)
        out_dir = RES / f"drawable-{bucket}"
        out_dir.mkdir(parents=True, exist_ok=True)
        img = make_banner(w, h)
        out = out_dir / "app_banner.png"
        img.save(out, "PNG", optimize=True)
        print(f"  wrote {out.relative_to(ROOT)}  ({w}x{h})")


def main():
    if not ICON_SRC.exists():
        raise SystemExit(f"Missing source: {ICON_SRC}")
    if not BANNER_SRC.exists():
        raise SystemExit(f"Missing source: {BANNER_SRC}")
    write_icon()
    write_adaptive_foreground()
    write_banner()
    print("Done — rebuild :app:assembleRelease to bake into the APK.")


if __name__ == "__main__":
    main()
