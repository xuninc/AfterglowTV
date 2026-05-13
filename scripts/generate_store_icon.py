#!/usr/bin/env python3
"""Generate the 512x512 fully-opaque Fire TV / Amazon Appstore icon.

Amazon requires a square 512x512 PNG with NO alpha channel — corners go to
a solid color, no transparency. Composes the supplied Afterglow icon onto
a solid deep-purple background matching the icon's own body color, then
flattens to RGB.

Source: C:/Users/Corey/Downloads/512x512-afterglow.png (or 114x114.png as
        fallback — both are scaled cleanly to 512x512).
Output: docs/branding/afterglow-firetv-icon-512.png
"""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
OUT_DIR = ROOT / "docs" / "branding"
OUT = OUT_DIR / "afterglow-firetv-icon-512.png"

# Source preference: the 512×512 wordmark image (better resolution), but we
# want JUST the icon — so prefer the icon-only 114×114 source upscaled. If
# the user has a higher-res icon-only file, drop it at the path below.
SOURCES = [
    Path("C:/Users/Corey/Downloads/afterglow-icon-square.png"),  # preferred
    Path("C:/Users/Corey/Downloads/114x114.png"),                # fallback
]

# Deep aubergine matching the icon's own rounded-square body color
BG = (22, 4, 39)  # #160427

def main():
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    src_path = next((p for p in SOURCES if p.exists()), None)
    if src_path is None:
        raise SystemExit(f"No source icon found. Checked: {[str(p) for p in SOURCES]}")
    print(f"Source: {src_path}")

    src = Image.open(src_path).convert("RGBA")
    # Upscale or downscale to 512×512 with LANCZOS for crisp edges
    icon = src.resize((512, 512), Image.LANCZOS)

    # Composite onto solid background — flattens alpha channel completely
    canvas = Image.new("RGB", (512, 512), BG)
    canvas.paste(icon, (0, 0), icon)   # alpha used only during paste, then dropped

    canvas.save(OUT, "PNG", optimize=True)
    print(f"Wrote: {OUT.relative_to(ROOT)}  (512×512 RGB, no alpha)")

    # Verify
    check = Image.open(OUT)
    print(f"Verify: mode={check.mode}  size={check.size}")
    assert check.mode == "RGB", "Output has alpha — Amazon will reject"
    print("OK — fully opaque, ready for store submission.")

if __name__ == "__main__":
    main()
