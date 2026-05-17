#!/usr/bin/env python3
"""Extract the app-icon square from the brand banner and write it
to every mipmap density Android needs, plus the high-res in-app
brand logo and a 512x512 store icon.

Usage:
    python scripts/install_icon_from_banner.py [path/to/banner.png]

Default banner source: AFTERGLOW_BANNER_SRC or docs/branding/source/app_banner.png.

The icon is the rounded-square on the left edge of the banner — bright
violet/orange against the darker purple backdrop. We auto-detect its
bounding box by finding the leftmost dense cluster of bright pixels;
if auto-detection fails, falls back to hard-coded coordinates.
"""
from __future__ import annotations

import sys
import os
from pathlib import Path

from PIL import Image

REPO_ROOT = Path(__file__).resolve().parent.parent
RES_ROOT = REPO_ROOT / "app" / "src" / "main" / "res"
BRANDING_DIR = REPO_ROOT / "docs" / "branding"

# Mipmap density targets — Android docs:
# https://developer.android.com/training/multiscreen/screendensities
MIPMAP_TARGETS = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}


def auto_detect_icon_bbox(banner: Image.Image) -> tuple[int, int, int, int]:
    """Find the rounded-square icon by detecting the leftmost cluster
    of pixels significantly brighter than the banner backdrop.

    Returns (left, top, right, bottom) inclusive."""
    width, height = banner.size
    gray = banner.convert("L")
    pixels = gray.load()

    # Scan only the leftmost third of the banner — the icon lives there
    # and we don't want the wordmark glow or letters polluting the search.
    scan_right = width // 3
    # Threshold: anything brighter than 80 (out of 255) is "non-background"
    # for a deep-purple banner backdrop.
    bright_threshold = 80

    cols_with_bright_pixels = []
    for x in range(scan_right):
        bright_count = sum(1 for y in range(height) if pixels[x, y] >= bright_threshold)
        if bright_count > height * 0.25:  # a column with substantial brightness
            cols_with_bright_pixels.append(x)

    if not cols_with_bright_pixels:
        # Fallback to known-good coordinates derived from the 1360x768 source.
        return (175, 235, 445, 505)

    left = min(cols_with_bright_pixels)
    right = max(cols_with_bright_pixels)

    rows_with_bright_pixels = []
    for y in range(height):
        bright_count = sum(1 for x in range(left, right + 1) if pixels[x, y] >= bright_threshold)
        if bright_count > (right - left) * 0.25:
            rows_with_bright_pixels.append(y)

    top = min(rows_with_bright_pixels)
    bottom = max(rows_with_bright_pixels)

    # Pad outward a touch so we don't slice the icon's rounded corners.
    pad = max(2, int(0.02 * max(right - left, bottom - top)))
    left = max(0, left - pad)
    top = max(0, top - pad)
    right = min(width - 1, right + pad)
    bottom = min(height - 1, bottom + pad)

    # Force square — pick the larger dimension and grow the shorter one centred.
    side = max(right - left, bottom - top)
    cx = (left + right) // 2
    cy = (top + bottom) // 2
    half = side // 2
    left = max(0, cx - half)
    top = max(0, cy - half)
    right = min(width - 1, left + side)
    bottom = min(height - 1, top + side)

    return (left, top, right, bottom)


def extract_icon(banner_path: Path) -> Image.Image:
    """Crop the icon square out of the banner and return at a clean 1024x1024."""
    banner = Image.open(banner_path).convert("RGBA")
    bbox = auto_detect_icon_bbox(banner)
    print(f"detected icon bbox: {bbox} (banner {banner.size})")
    icon = banner.crop(bbox)
    # Normalize to a square 1024x1024 working size.
    return icon.resize((1024, 1024), Image.LANCZOS)


def write_mipmaps(icon: Image.Image) -> list[Path]:
    written: list[Path] = []
    for density_dir, size in MIPMAP_TARGETS.items():
        out_dir = RES_ROOT / density_dir
        out_dir.mkdir(parents=True, exist_ok=True)
        resized = icon.resize((size, size), Image.LANCZOS)
        for filename in ("ic_launcher.png", "ic_launcher_round.png"):
            target = out_dir / filename
            resized.save(target, format="PNG", optimize=True)
            written.append(target)
    return written


def write_store_icon(icon: Image.Image) -> Path:
    """512x512 opaque PNG for Play Store / Amazon Appstore submission."""
    BRANDING_DIR.mkdir(parents=True, exist_ok=True)
    store = icon.resize((512, 512), Image.LANCZOS)
    # Composite on opaque dark purple in case the source has alpha edges.
    flat = Image.new("RGB", store.size, (22, 4, 39))  # AppColors.TiviSurfaceDeep
    flat.paste(store, mask=store.split()[-1] if store.mode == "RGBA" else None)
    target = BRANDING_DIR / "afterglow-icon-512.png"
    flat.save(target, format="PNG", optimize=True)
    return target


def write_brand_logo(icon: Image.Image) -> Path:
    """In-app brand logo used by AfterglowBrandStrip + WelcomeScreen.
    432px renders cleanly at 120dp on xxxhdpi panels (the largest size
    we draw the brand mark in the UI)."""
    drawable_nodpi = RES_ROOT / "drawable-nodpi"
    drawable_nodpi.mkdir(parents=True, exist_ok=True)
    logo = icon.resize((432, 432), Image.LANCZOS)
    target = drawable_nodpi / "afterglow_logo.png"
    logo.save(target, format="PNG", optimize=True)
    return target


def main() -> int:
    banner_path = (
        Path(sys.argv[1]) if len(sys.argv) > 1
        else Path(os.environ.get("AFTERGLOW_BANNER_SRC", REPO_ROOT / "docs" / "branding" / "source" / "app_banner.png"))
    )
    if not banner_path.exists():
        print(f"banner not found: {banner_path}", file=sys.stderr)
        return 2

    icon = extract_icon(banner_path)
    mipmaps = write_mipmaps(icon)
    store = write_store_icon(icon)
    logo = write_brand_logo(icon)

    print(f"\nwrote {len(mipmaps)} mipmap files:")
    for m in mipmaps:
        print(f"  {m.relative_to(REPO_ROOT)}")
    print(f"\nstore icon: {store.relative_to(REPO_ROOT)}")
    print(f"in-app brand logo: {logo.relative_to(REPO_ROOT)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
