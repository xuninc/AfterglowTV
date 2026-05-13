#!/usr/bin/env python3
"""Slice the user-provided 1360x768 Afterglow TV banner into Android density variants.

Source: C:/Users/Corey/Downloads/app_banner.png
Targets: app/src/main/res/{drawable,drawable-mdpi,...drawable-xxxhdpi}/app_banner.png

Android TV's <application android:banner=...> resource picks the right
density at runtime from the bucket. We supply LANCZOS-downscaled copies
of the real banner art instead of programmatically composing.
"""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "app" / "src" / "main" / "res"
SRC = Path("C:/Users/Corey/Downloads/app_banner.png")

# (relative dir, target dimensions)
TARGETS = [
    ("drawable",         (320,  180)),
    ("drawable-mdpi",    (320,  180)),
    ("drawable-hdpi",    (480,  270)),
    ("drawable-xhdpi",   (640,  360)),
    ("drawable-xxhdpi",  (960,  540)),
    ("drawable-xxxhdpi", (1280, 720)),
]

def main():
    if not SRC.exists():
        raise SystemExit(f"Missing source: {SRC}")
    src = Image.open(SRC).convert("RGBA")
    print(f"Source: {SRC.name}  {src.size}")
    for sub, size in TARGETS:
        out_dir = RES / sub
        out_dir.mkdir(parents=True, exist_ok=True)
        out = out_dir / "app_banner.png"
        scaled = src.resize(size, Image.LANCZOS)
        scaled.save(out, "PNG", optimize=True)
        print(f"  wrote {out.relative_to(ROOT)}  ({size[0]}x{size[1]})")
    print("Done.")

if __name__ == "__main__":
    main()
